package com.sanxing.sesame.mbean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sanxing.sesame.container.ServiceAssemblyEnvironment;
import com.sanxing.sesame.deployment.Connection;
import com.sanxing.sesame.deployment.Consumes;
import com.sanxing.sesame.deployment.DescriptorFactory;
import com.sanxing.sesame.deployment.ServiceAssembly;
import com.sanxing.sesame.deployment.Services;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.MBeanInfoProvider;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.util.XmlPersistenceSupport;

public class ServiceAssemblyLifeCycle
    implements ServiceAssemblyMBean, MBeanInfoProvider
{
    private static final Logger LOG = LoggerFactory.getLogger( ServiceAssemblyLifeCycle.class );

    private final ServiceAssembly serviceAssembly;

    private String currentState = "Shutdown";

    private ServiceUnitLifeCycle[] sus;

    private final Registry registry;

    private PropertyChangeListener listener;

    private final ServiceAssemblyEnvironment env;

    public ServiceAssemblyLifeCycle( ServiceAssembly sa, ServiceAssemblyEnvironment env, Registry registry )
    {
        serviceAssembly = sa;
        this.env = env;
        this.registry = registry;
    }

    protected void setServiceUnits( ServiceUnitLifeCycle[] serviceUnits )
    {
        sus = serviceUnits;
    }

    @Override
    public String start()
        throws Exception
    {
        return start( true );
    }

    public synchronized String start( boolean writeState )
        throws Exception
    {
        LOG.info( "Starting service assembly: " + getName() );
        try
        {
            startConnections();
        }
        catch ( JBIException e )
        {
            throw ManagementSupport.failure( "start", e.getMessage() );
        }

        List componentFailures = new ArrayList();
        for ( int i = 0; i < sus.length; ++i )
        {
            if ( !( sus[i].isShutDown() ) )
            {
                continue;
            }
            try
            {
                sus[i].init();
            }
            catch ( DeploymentException e )
            {
                componentFailures.add( getComponentFailure( e, "start", sus[i].getComponentName() ) );
            }
        }

        for ( int i = 0; i < sus.length; ++i )
        {
            if ( !( sus[i].isStopped() ) )
            {
                continue;
            }
            try
            {
                sus[i].start();
            }
            catch ( DeploymentException e )
            {
                componentFailures.add( getComponentFailure( e, "start", sus[i].getComponentName() ) );
            }
        }

        if ( componentFailures.size() == 0 )
        {
            currentState = "Started";
            if ( writeState )
            {
                writeRunningState();
            }

            return ManagementSupport.createSuccessMessage( "start" );
        }

        throw ManagementSupport.failure( "start", componentFailures );
    }

    @Override
    public String stop()
        throws Exception
    {
        return stop( true, false );
    }

    public synchronized String stop( boolean writeState, boolean forceInit )
        throws Exception
    {
        LOG.info( "Stopping service assembly: " + getName() );

        stopConnections();

        List componentFailures = new ArrayList();
        if ( forceInit )
        {
            for ( int i = 0; i < sus.length; ++i )
            {
                try
                {
                    sus[i].init();
                }
                catch ( DeploymentException e )
                {
                    componentFailures.add( getComponentFailure( e, "stop", sus[i].getComponentName() ) );
                }
            }
        }
        for ( int i = 0; i < sus.length; ++i )
        {
            if ( !( sus[i].isStarted() ) )
            {
                continue;
            }
            try
            {
                sus[i].stop();
            }
            catch ( DeploymentException e )
            {
                componentFailures.add( getComponentFailure( e, "stop", sus[i].getComponentName() ) );
            }
        }

        if ( componentFailures.size() == 0 )
        {
            currentState = "Stopped";
            if ( writeState )
            {
                writeRunningState();
            }

            return ManagementSupport.createSuccessMessage( "stop" );
        }
        throw ManagementSupport.failure( "stop", componentFailures );
    }

    @Override
    public String shutDown()
        throws Exception
    {
        return shutDown( true );
    }

    public synchronized String shutDown( boolean writeState )
        throws Exception
    {
        LOG.info( "Shutting down service assembly: " + getName() );
        List componentFailures = new ArrayList();
        for ( int i = 0; i < sus.length; ++i )
        {
            if ( !( sus[i].isStarted() ) )
            {
                continue;
            }
            try
            {
                sus[i].stop();
            }
            catch ( DeploymentException e )
            {
                componentFailures.add( getComponentFailure( e, "shutDown", sus[i].getComponentName() ) );
            }
        }

        for ( int i = 0; i < sus.length; ++i )
        {
            if ( !( sus[i].isStopped() ) )
            {
                continue;
            }
            try
            {
                sus[i].shutDown();
            }
            catch ( DeploymentException e )
            {
                componentFailures.add( getComponentFailure( e, "shutDown", sus[i].getComponentName() ) );
            }
        }

        if ( componentFailures.size() == 0 )
        {
            currentState = "Shutdown";
            if ( writeState )
            {
                writeRunningState();
            }

            return ManagementSupport.createSuccessMessage( "shutDown" );
        }
        throw ManagementSupport.failure( "shutDown", componentFailures );
    }

    @Override
    public String getCurrentState()
    {
        return currentState;
    }

    boolean isShutDown()
    {
        return currentState.equals( "Shutdown" );
    }

    boolean isStopped()
    {
        return currentState.equals( "Stopped" );
    }

    boolean isStarted()
    {
        return currentState.equals( "Started" );
    }

    @Override
    public String getName()
    {
        return serviceAssembly.getIdentification().getName();
    }

    @Override
    public String getDescription()
    {
        return serviceAssembly.getIdentification().getDescription();
    }

    public ServiceAssembly getServiceAssembly()
    {
        return serviceAssembly;
    }

    @Override
    public String getDescriptor()
    {
        File saDir = env.getInstallDir();
        return DescriptorFactory.getDescriptorAsText( saDir );
    }

    @Override
    public String toString()
    {
        return "ServiceAssemblyLifeCycle[name=" + getName() + ",state=" + getCurrentState() + "]";
    }

    void writeRunningState()
    {
        try
        {
            if ( env.getStateFile() != null )
            {
                String state = getCurrentState();
                Properties props = new Properties();
                props.setProperty( "state", state );
                XmlPersistenceSupport.write( env.getStateFile(), props );
            }
        }
        catch ( IOException e )
        {
            LOG.error( "Failed to write current running state for ServiceAssembly: " + getName(), e );
        }
    }

    String getRunningStateFromStore()
    {
        try
        {
            if ( ( env.getStateFile() != null ) && ( env.getStateFile().exists() ) )
            {
                Properties props = (Properties) XmlPersistenceSupport.read( env.getStateFile() );
                return props.getProperty( "state", "Shutdown" );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to read current running state for ServiceAssembly: " + getName(), e );
        }
        return null;
    }

    public synchronized void restore()
        throws Exception
    {
        String state = getRunningStateFromStore();
        if ( "Started".equals( state ) )
        {
            start( false );
        }
        else
        {
            stop( false, true );
            if ( "Shutdown".equals( state ) )
            {
                shutDown( false );
            }
        }
    }

    public ServiceUnitLifeCycle[] getDeployedSUs()
    {
        return sus;
    }

    protected void startConnections()
        throws JBIException
    {
        if ( ( serviceAssembly.getConnections() == null )
            || ( serviceAssembly.getConnections().getConnections() == null ) )
        {
            return;
        }
        Connection[] connections = serviceAssembly.getConnections().getConnections();
        for ( int i = 0; i < connections.length; ++i )
        {
            if ( connections[i].getConsumer().getInterfaceName() != null )
            {
                QName fromItf = connections[i].getConsumer().getInterfaceName();
                QName toSvc = connections[i].getProvider().getServiceName();
                String toEp = connections[i].getProvider().getEndpointName();
                registry.registerInterfaceConnection( fromItf, toSvc, toEp );
            }
            else
            {
                QName fromSvc = connections[i].getConsumer().getServiceName();
                String fromEp = connections[i].getConsumer().getEndpointName();
                QName toSvc = connections[i].getProvider().getServiceName();
                String toEp = connections[i].getProvider().getEndpointName();
                String link = getLinkType( fromSvc, fromEp );
                registry.registerEndpointConnection( fromSvc, fromEp, toSvc, toEp, link );
            }
        }
    }

    protected String getLinkType( QName svc, String ep )
    {
        for ( int i = 0; i < sus.length; ++i )
        {
            Services s = sus[i].getServices();
            if ( ( s != null ) && ( s.getConsumes() != null ) )
            {
                Consumes[] consumes = s.getConsumes();
                for ( int j = 0; j < consumes.length; ++j )
                {
                    if ( ( svc.equals( consumes[j].getServiceName() ) )
                        && ( ep.equals( consumes[j].getEndpointName() ) ) )
                    {
                        return consumes[j].getLinkType();
                    }
                }
            }
        }
        return null;
    }

    protected void stopConnections()
    {
        if ( ( serviceAssembly.getConnections() == null )
            || ( serviceAssembly.getConnections().getConnections() == null ) )
        {
            return;
        }
        Connection[] connections = serviceAssembly.getConnections().getConnections();
        for ( int i = 0; i < connections.length; ++i )
        {
            if ( connections[i].getConsumer().getInterfaceName() != null )
            {
                QName fromItf = connections[i].getConsumer().getInterfaceName();
                registry.unregisterInterfaceConnection( fromItf );
            }
            else
            {
                QName fromSvc = connections[i].getConsumer().getServiceName();
                String fromEp = connections[i].getConsumer().getEndpointName();
                registry.unregisterEndpointConnection( fromSvc, fromEp );
            }
        }
    }

    protected Element getComponentFailure( Exception exception, String task, String component )
    {
        Element result = null;
        String resultMsg = exception.getMessage();
        try
        {
            Document doc = parse( resultMsg );
            result = getElement( doc, "component-task-result" );
        }
        catch ( Exception e )
        {
            LOG.warn( "Could not parse result exception", e );
        }
        if ( result == null )
        {
            result =
                ManagementSupport.createComponentFailure( task, component, "Unable to parse result string", exception );
        }

        return result;
    }

    protected Document parse( String result )
        throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        factory.setIgnoringElementContentWhitespace( true );
        factory.setIgnoringComments( true );
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse( new InputSource( new StringReader( result ) ) );
    }

    protected Element getElement( Document doc, String name )
    {
        NodeList l = doc.getElementsByTagNameNS( "http://java.sun.com/xml/ns/jbi/management-message", name );
        return ( (Element) l.item( 0 ) );
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute( getObjectToManage(), "currentState", "current state of the assembly" );
        helper.addAttribute( getObjectToManage(), "name", "name of the assembly" );
        helper.addAttribute( getObjectToManage(), "description", "description of the assembly" );
        helper.addAttribute( getObjectToManage(), "serviceUnits", "list of service units contained in this assembly" );
        return helper.getAttributeInfos();
    }

    @Override
    public MBeanOperationInfo[] getOperationInfos()
        throws JMException
    {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation( getObjectToManage(), "start", "start the assembly" );
        helper.addOperation( getObjectToManage(), "stop", "stop the assembly" );
        helper.addOperation( getObjectToManage(), "shutDown", "shutdown the assembly" );
        helper.addOperation( getObjectToManage(), "getDescriptor", "retrieve the jbi descriptor for this assembly" );
        return helper.getOperationInfos();
    }

    @Override
    public Object getObjectToManage()
    {
        return this;
    }

    @Override
    public String getType()
    {
        return "ServiceAssembly";
    }

    @Override
    public String getSubType()
    {
        return null;
    }

    @Override
    public void setPropertyChangeListener( PropertyChangeListener l )
    {
        listener = l;
    }

    protected void firePropertyChanged( String name, Object oldValue, Object newValue )
    {
        PropertyChangeListener l = listener;
        if ( l != null )
        {
            PropertyChangeEvent event = new PropertyChangeEvent( this, name, oldValue, newValue );
            l.propertyChange( event );
        }
    }

    @Override
    public ObjectName[] getServiceUnits()
    {
        ObjectName[] names = new ObjectName[sus.length];
        for ( int i = 0; i < names.length; ++i )
        {
            names[i] = registry.getContainer().getManagementContext().createObjectName( sus[i] );
        }
        return names;
    }

    public ServiceAssemblyEnvironment getEnvironment()
    {
        return env;
    }
}