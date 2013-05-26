package com.sanxing.sesame.binding.soap;

import com.sanxing.sesame.address.AddressBook;
import com.sanxing.sesame.address.Location;
import com.sanxing.sesame.component.BindingComponent;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.service.ServiceUnit;
import com.sanxing.sesame.util.ServerDetector;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jbi.JBIException;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.w3c.dom.DocumentFragment;

public class SOAPServer
    extends BindingComponent
    implements ServiceUnitManager
{
    private static Logger LOG = LoggerFactory.getLogger( SOAPServer.class );

    private AtomicBoolean initialized = new AtomicBoolean( false );

    private AtomicBoolean started = new AtomicBoolean( false );

    private Server jettyServer;

    protected SAAJServlet rootServlet;

    protected void init()
        throws JBIException
    {
        if ( this.initialized.compareAndSet( false, true ) )
        {
            super.init();

            this.jettyServer = new Server();
            SOAPThreadPool pool = new SOAPThreadPool();
            this.jettyServer.setThreadPool( pool );

            ServletContextHandler contextHandler = new ServletContextHandler( 1 );
            contextHandler.setContextPath( "/" );
            this.rootServlet = new SAAJServlet();
            contextHandler.addServlet( new ServletHolder( this.rootServlet ), "/*" );
            this.jettyServer.setHandler( contextHandler );
        }
    }

    public void start()
        throws JBIException
    {
        if ( this.started.compareAndSet( false, true ) )
        {
            try
            {
                this.jettyServer.start();
            }
            catch ( Exception e )
            {
                LOG.error( "Start jetty failure", e );
                throw taskFailure( "start", e.getMessage() != null ? e.getMessage() : e.toString() );
            }
            super.start();
        }
    }

    public void stop()
        throws JBIException
    {
        if ( this.started.compareAndSet( true, false ) )
        {
            try
            {
                this.jettyServer.stop();
            }
            catch ( Exception e )
            {
                throw taskFailure( "stop", e.getMessage() != null ? e.getMessage() : e.toString() );
            }
            super.stop();
        }
    }

    public void shutDown()
        throws JBIException
    {
        if ( this.initialized.compareAndSet( true, false ) )
        {
            stop();
            this.jettyServer.destroy();
            super.shutDown();
        }
    }

    public void start( String serviceUnitName )
        throws DeploymentException
    {
        LOG.debug( "start serviceUnitName:" + serviceUnitName );
        ServiceUnit unit = getServiceUnit( serviceUnitName );
        try
        {
            Service service = unit.getService();
            Iterator portNames = service.getPorts().keySet().iterator();
            while ( portNames.hasNext() )
            {
                Port port = service.getPort( (String) portNames.next() );
                LOG.debug( "port=" + port.getName() );
                Iterator iter = port.getExtensibilityElements().iterator();
                if ( !iter.hasNext() )
                    throw new IOException( "Port address not specified" );
                ExtensibilityElement ee = (ExtensibilityElement) iter.next();
                SOAPAddress soapAddr = (SOAPAddress) ee;
                URI uri = new URI( soapAddr.getLocationURI() );
                if ( ( uri.getScheme() != null ) && ( uri.getHost() == null ) )
                {
                    throw new DeploymentException( " error uri [" + soapAddr.getLocationURI() + "]" );
                }
                String uriString = uri.toString();
                Location loc = AddressBook.find( uri.getScheme() != null ? uriString : soapAddr.getLocationURI() );
                if ( loc != null )
                {
                    URI real = loc.getURI();
                    uri =
                        new URI( real.getScheme(), real.getAuthority(), real.getPath()
                            + ( uri.getScheme() != null ? uri.getPath() : "" ), uri.getQuery(), uri.getFragment() );
                }
                URL url = new URL( uri.toString() );

                int portNumber = url.getPort() == -1 ? 80 : url.getPort();
                String realpath = url.getPath() == "" ? "/" : url.getPath();
                String endpointAddress = Integer.toString( portNumber ) + realpath;
                SOAPEndPoint endpoint = new SOAPEndPoint( this );
                endpoint.setServiceUnit( unit );
                endpoint.setPort( port );

                boolean exist = false;
                if ( this.jettyServer.getConnectors() != null )
                {
                    for ( Connector connector : this.jettyServer.getConnectors() )
                    {
                        if ( ( connector.getHost().equals( url.getHost() ) ) && ( connector.getPort() == portNumber ) )
                        {
                            exist = true;
                            break;
                        }
                    }
                }
                if ( !exist )
                {
                    Connector freshConnector = null;
                    if ( ServerDetector.isWebSphere() )
                    {
                        freshConnector = new SocketConnector();
                    }
                    else
                    {
                        freshConnector = new SelectChannelConnector();
                    }
                    freshConnector.setHost( url.getHost() );
                    freshConnector.setPort( portNumber );
                    this.jettyServer.addConnector( freshConnector );
                    freshConnector.start();
                }

                LOG.debug( "endpointAddress:" + endpointAddress );
                this.rootServlet.addMapping( endpointAddress, endpoint );
            }
        }
        catch ( Throwable e )
        {
            e.printStackTrace();
            LOG.error( e.getMessage(), e );
            throw taskFailure( "start " + serviceUnitName, e.getMessage() != null ? e.getMessage() : e.toString() );
        }
        super.start( serviceUnitName );
    }

    public void stop( String serviceUnitName )
        throws DeploymentException
    {
        super.stop( serviceUnitName );

        ServiceUnit unit = getServiceUnit( serviceUnitName );
        try
        {
            Service service = unit.getService();
            Map ports = service.getPorts();
            for ( Port port : (Collection<Port>) ports.values() )
            {
                Iterator iter = port.getExtensibilityElements().iterator();
                if ( !iter.hasNext() )
                    throw new IOException( "Port address not specified" );
                ExtensibilityElement ee = (ExtensibilityElement) iter.next();
                SOAPAddress soapAddr = (SOAPAddress) ee;
                URI uri = new URI( soapAddr.getLocationURI() );
                if ( ( uri.getScheme() != null ) && ( uri.getHost() == null ) )
                {
                    throw new DeploymentException( " error uri [" + soapAddr.getLocationURI() + "]" );
                }
                Location loc = AddressBook.find( uri.getScheme() != null ? uri.getHost() : soapAddr.getLocationURI() );
                if ( loc != null )
                {
                    URI real = loc.getURI();
                    uri =
                        new URI( real.getScheme(), real.getAuthority(), real.getPath()
                            + ( uri.getScheme() != null ? uri.getPath() : "" ), uri.getQuery(), uri.getFragment() );
                }
                URL url = new URL( uri.toString() );

                LOG.debug( this.jettyServer.getConnectors().toString() );
                int portNumber = url.getPort() == -1 ? 80 : url.getPort();
                for ( Connector connector : this.jettyServer.getConnectors() )
                {
                    if ( ( connector.getHost().equals( url.getHost() ) ) && ( connector.getPort() == portNumber ) )
                    {
                        connector.stop();
                        this.jettyServer.removeConnector( connector );
                        break;
                    }
                }
                LOG.debug( this.jettyServer.getConnectors().toString() );
                String endpointAddress = portNumber + url.getPath();
                this.rootServlet.removeMapping( endpointAddress );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw taskFailure( "stop " + serviceUnitName, e.getMessage() != null ? e.getMessage() : e.toString() );
        }
    }

    protected void checkInitialized()
        throws JBIException
    {
        if ( !this.initialized.get() )
            throw new JBIException( "The Component is not initialized - please call init(...)" );
    }

    public void onMessageExchange( MessageExchange exchange )
        throws MessagingException
    {
    }

    public ServiceEndpoint resolveEndpointReference( DocumentFragment epr )
    {
        return null;
    }

    protected ServiceUnitManager createServiceUnitManager()
    {
        return this;
    }

    public String deploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return ManagementSupport.createSuccessMessage( "deploy-" + serviceUnitName, "部署成功" );
    }

    public void init( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
    }

    public void shutDown( String serviceUnitName )
        throws DeploymentException
    {
    }

    public String undeploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return ManagementSupport.createSuccessMessage( "undeploy-" + serviceUnitName, "撤消部署成功" );
    }
}