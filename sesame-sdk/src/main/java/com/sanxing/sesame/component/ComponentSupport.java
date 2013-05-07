package com.sanxing.sesame.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import com.sanxing.sesame.exception.NoInMessageAvailableException;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.service.ServiceUnit;

public abstract class ComponentSupport
    extends BaseComponent
    implements Component
{
    private ClassLoader classLoader;

    private ComponentLifeCycle lifeCycle;

    private ServiceUnitManager serviceUnitManager;

    private final Properties properties = new Properties();

    protected ComponentSupport()
    {
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    protected ComponentSupport( QName service, String endpoint )
    {
        super( service, endpoint );
    }

    @Override
    protected void init()
        throws JBIException
    {
        super.init();

        File installFolder = new File( getContext().getInstallRoot() );
        File propertyFile = new File( installFolder, ".properties" );
        if ( !( propertyFile.exists() ) )
        {
            return;
        }
        try
        {
            InputStream in = new FileInputStream( propertyFile );
            try
            {
                properties.load( in );
            }
            finally
            {
                in.close();
            }
        }
        catch ( IOException e )
        {
            logger.error( e.getMessage(), e );
        }
    }

    @Override
    public ComponentLifeCycle getLifeCycle()
    {
        synchronized ( this )
        {
            if ( lifeCycle == null )
            {
                lifeCycle = createComponentLifeCycle();
            }
        }
        return lifeCycle;
    }

    @Override
    public ServiceUnitManager getServiceUnitManager()
    {
        synchronized ( this )
        {
            if ( serviceUnitManager == null )
            {
                ServiceUnitManager temp = createServiceUnitManager();

                if ( temp != null )
                {
                    serviceUnitManager = new UnitManagerProxy( this, temp );
                }
            }
        }
        return serviceUnitManager;
    }

    @Override
    public Document getServiceDescription( ServiceEndpoint endpoint )
    {
        UnitManagerProxy sumProxy = (UnitManagerProxy) getServiceUnitManager();
        if ( sumProxy == null )
        {
            return null;
        }

        ServiceUnit serviceUnit = sumProxy.getServiceUnit( endpoint.getEndpointName() );
        if ( serviceUnit != null )
        {
            try
            {
                WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
                Document doc = writer.getDocument( serviceUnit.getDefinition() );
                doc.setDocumentURI( serviceUnit.getDefinition().getDocumentBaseURI() );
                return doc;
            }
            catch ( WSDLException e )
            {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean isExchangeWithConsumerOkay( ServiceEndpoint endpoint, MessageExchange exchange )
    {
        return true;
    }

    @Override
    public boolean isExchangeWithProviderOkay( ServiceEndpoint endpoint, MessageExchange exchange )
    {
        return true;
    }

    protected abstract ServiceUnitManager createServiceUnitManager();

    protected String getProperty( String name )
    {
        return properties.getProperty( name );
    }

    protected Enumeration<String> propertyNames()
    {
        return (Enumeration<String>) properties.propertyNames();
    }

    protected ServiceUnit getServiceUnit( String serviceUnitName )
    {
        return ( (UnitManagerProxy) serviceUnitManager ).getServiceUnit( serviceUnitName );
    }

    protected ServiceUnit getServiceUnit( QName serviceName )
    {
        return ( (UnitManagerProxy) serviceUnitManager ).getServiceUnit( serviceName );
    }

    protected ComponentLifeCycle createComponentLifeCycle()
    {
        return this;
    }

    protected NormalizedMessage getInMessage( MessageExchange exchange )
        throws NoInMessageAvailableException
    {
        NormalizedMessage message = exchange.getMessage( "in" );
        if ( message == null )
        {
            throw new NoInMessageAvailableException( exchange );
        }
        return message;
    }

    protected ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public DeploymentException taskFailure( String task, String info )
    {
        return ManagementSupport.componentFailure( task, getContext().getComponentName(), info );
    }

    public abstract boolean isBindingComponent();

    public abstract boolean isEngineComponent();
}