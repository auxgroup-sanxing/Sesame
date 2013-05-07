package com.sanxing.sesame.mbean;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jbi.JBIException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.MBeanBuilder;
import com.sanxing.sesame.management.MBeanInfoProvider;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.management.ParameterHelper;

public class ManagementContext
    extends BaseSystemService
    implements ManagementContextMBean
{
    private static final Logger LOG = LoggerFactory.getLogger( ManagementContext.class );

    protected Map<String, ObjectName> systemServices = new ConcurrentHashMap();

    private final Map<ObjectName, Object> beanMap = new ConcurrentHashMap();

    private MBeanServer server;

    private JBIContainer container;

    private ExecutorService executors;

    @Override
    public String getDescription()
    {
        return "JMX Management";
    }

    @Override
    public void init( JBIContainer _container )
        throws JBIException
    {
        container = _container;
        server = container.getMBeanServer();
        executors = Executors.newCachedThreadPool();
        super.init( container );
    }

    public MBeanServer getMBeanServer()
    {
        return server;
    }

    @Override
    protected Class<ManagementContextMBean> getServiceMBean()
    {
        return ManagementContextMBean.class;
    }

    @Override
    public void start()
        throws JBIException
    {
        super.start();
    }

    @Override
    public void stop()
        throws JBIException
    {
        super.stop();
    }

    @Override
    public void shutDown()
        throws JBIException
    {
        super.shutDown();

        ObjectName[] beans = beanMap.keySet().toArray( new ObjectName[beanMap.size()] );
        for ( int i = 0; i < beans.length; ++i )
        {
            try
            {
                unregisterMBean( beans[i] );
            }
            catch ( Exception e )
            {
                LOG.debug( "Could not unregister mbean", e );
            }
        }

        executors.shutdown();
    }

    @Override
    public ObjectName[] getBindingComponents()
    {
        return container.getRegistry().getBindingComponents();
    }

    @Override
    public ObjectName getComponentByName( String componentName )
    {
        ComponentMBeanImpl component = container.getRegistry().getComponent( componentName );
        return ( ( component != null ) ? component.getMBeanName() : null );
    }

    @Override
    public ObjectName[] getEngineComponents()
    {
        return container.getRegistry().getEngineComponents();
    }

    @Override
    public String getSystemInfo()
    {
        return "Sesame JBI Container: version: " + EnvironmentContext.getVersion();
    }

    @Override
    public ObjectName getSystemService( String serviceName )
    {
        return systemServices.get( serviceName );
    }

    @Override
    public ObjectName[] getSystemServices()
    {
        ObjectName[] result = null;
        Collection col = systemServices.values();
        result = new ObjectName[col.size()];
        col.toArray( result );
        return result;
    }

    @Override
    public boolean isBinding( String componentName )
    {
        ComponentMBeanImpl component = container.getRegistry().getComponent( componentName );
        return ( ( component != null ) ? component.isBinding() : false );
    }

    @Override
    public boolean isEngine( String componentName )
    {
        ComponentMBeanImpl component = container.getRegistry().getComponent( componentName );
        return ( ( component != null ) ? component.isEngine() : false );
    }

    @Override
    public String startComponent( String componentName )
        throws JBIException
    {
        String result = "NOT FOUND: " + componentName;
        ObjectName objName = getComponentByName( componentName );
        if ( objName != null )
        {
            ComponentMBeanImpl mbean = (ComponentMBeanImpl) beanMap.get( objName );
            if ( mbean != null )
            {
                mbean.start();
                result = mbean.getCurrentState();
            }
        }
        return result;
    }

    @Override
    public String stopComponent( String componentName )
        throws JBIException
    {
        String result = "NOT FOUND: " + componentName;
        ObjectName objName = getComponentByName( componentName );
        if ( objName != null )
        {
            ComponentMBeanImpl mbean = (ComponentMBeanImpl) beanMap.get( objName );
            if ( mbean != null )
            {
                mbean.stop();
                result = mbean.getCurrentState();
            }
        }
        return result;
    }

    @Override
    public String shutDownComponent( String componentName )
        throws JBIException
    {
        String result = "NOT FOUND: " + componentName;
        ObjectName objName = getComponentByName( componentName );
        if ( objName != null )
        {
            ComponentMBeanImpl mbean = (ComponentMBeanImpl) beanMap.get( objName );
            if ( mbean != null )
            {
                mbean.shutDown();
                result = mbean.getCurrentState();
            }
        }
        return result;
    }

    public ObjectName createCustomComponentMBeanName( String type, String name )
    {
        Map result = new LinkedHashMap();
        result.put( "ServerName", container.getServerName() );
        result.put( "Type", "Component" );
        result.put( "Name", sanitizeString( name ) );
        result.put( "SubType", sanitizeString( type ) );
        return createObjectName( result );
    }

    public ObjectName createObjectName( MBeanInfoProvider provider )
    {
        Map props = createObjectNameProps( provider );
        return createObjectName( props );
    }

    public ObjectName createObjectName( String name )
    {
        ObjectName result = null;
        try
        {
            result = new ObjectName( name );
        }
        catch ( MalformedObjectNameException e )
        {
            String error = "Could not create ObjectName for " + name;
            LOG.error( error, e );
            throw new RuntimeException( error );
        }
        return result;
    }

    public ObjectName createObjectName( String domain, Map<String, String> props )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( domain ).append( ':' );
        int i = 0;
        for ( Object element : props.entrySet() )
        {
            Map.Entry entry = (Map.Entry) element;
            if ( i++ > 0 )
            {
                sb.append( "," );
            }
            sb.append( entry.getKey() ).append( "=" ).append( entry.getValue() );
        }
        ObjectName result = null;
        try
        {
            result = new ObjectName( sb.toString() );
        }
        catch ( MalformedObjectNameException e )
        {
            String error = "Could not create ObjectName for " + props;
            LOG.error( error, e );
            throw new RuntimeException( error );
        }
        return result;
    }

    public ObjectName createObjectName( Map<String, String> props )
    {
        return createObjectName( container.getJmxDomain(), props );
    }

    public Map<String, String> createObjectNameProps( MBeanInfoProvider provider )
    {
        return createObjectNameProps( provider, false );
    }

    public Map<String, String> createObjectNameProps( MBeanInfoProvider provider, boolean subTypeBeforeName )
    {
        Map result = new LinkedHashMap();
        result.put( "ServerName", container.getServerName() );
        result.put( "Type", sanitizeString( provider.getType() ) );
        if ( ( subTypeBeforeName ) && ( provider.getSubType() != null ) )
        {
            result.put( "SubType", sanitizeString( provider.getSubType() ) );
        }
        result.put( "Name", sanitizeString( provider.getName() ) );
        if ( ( !( subTypeBeforeName ) ) && ( provider.getSubType() != null ) )
        {
            result.put( "SubType", sanitizeString( provider.getSubType() ) );
        }
        return result;
    }

    private static String sanitizeString( String in )
    {
        String result = null;
        if ( in != null )
        {
            result = in.replace( ':', '_' );
            result = result.replace( '/', '_' );
            result = result.replace( '\\', '_' );
            result = result.replace( '?', '_' );
            result = result.replace( '=', '_' );
            result = result.replace( ',', '_' );
        }
        return result;
    }

    public void registerMBean( ObjectName name, MBeanInfoProvider resource, Class interfaceMBean )
        throws JMException
    {
        registerMBean( name, resource, interfaceMBean, resource.getDescription() );
    }

    public void registerMBean( ObjectName name, Object resource, Class interfaceMBean, String description )
        throws JMException
    {
        Object mbean = MBeanBuilder.buildStandardMBean( resource, interfaceMBean, description, executors );
        if ( server.isRegistered( name ) )
        {
            server.unregisterMBean( name );
        }
        server.registerMBean( mbean, name );
        beanMap.put( name, resource );
    }

    public static ObjectName getSystemObjectName( String domainName, String serverName, Class interfaceType )
    {
        String tmp =
            domainName + ":ServerName=" + serverName + ",Type=SystemService,Name="
                + getSystemServiceName( interfaceType );
        ObjectName result = null;
        try
        {
            result = new ObjectName( tmp );
        }
        catch ( MalformedObjectNameException e )
        {
            LOG.error( "Failed to build ObjectName:", e );
        }
        catch ( NullPointerException e )
        {
            LOG.error( "Failed to build ObjectName:", e );
        }
        return result;
    }

    public static String getSystemServiceName( Class interfaceType )
    {
        String name = interfaceType.getName();
        name = name.substring( name.lastIndexOf( 46 ) + 1 );
        if ( name.endsWith( "MBean" ) )
        {
            name = name.substring( 0, name.length() - 5 );
        }
        return name;
    }

    public static ObjectName getContainerObjectName( String domainName, String serverName )
    {
        String tmp = domainName + ":ServerName=" + serverName + ",Type=JBIContainer";
        ObjectName result = null;
        try
        {
            result = new ObjectName( tmp );
        }
        catch ( MalformedObjectNameException e )
        {
            LOG.debug( "Unable to build ObjectName", e );
        }
        catch ( NullPointerException e )
        {
            LOG.debug( "Unable to build ObjectName", e );
        }
        return result;
    }

    public void registerSystemService( BaseSystemService service, Class interfaceType )
        throws JBIException
    {
        try
        {
            String name = service.getName();
            if ( systemServices.containsKey( name ) )
            {
                throw new JBIException( "A system service for the name " + name + " is already registered" );
            }
            ObjectName objName = createObjectName( service );
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Registering system service: " + objName );
            }
            registerMBean( objName, service, interfaceType, service.getDescription() );
            systemServices.put( name, objName );
        }
        catch ( MalformedObjectNameException e )
        {
            throw new JBIException( e );
        }
        catch ( JMException e )
        {
            throw new JBIException( e );
        }
    }

    public void unregisterSystemService( BaseSystemService service )
        throws JBIException
    {
        String name = service.getName();
        if ( !( systemServices.containsKey( name ) ) )
        {
            throw new JBIException( "A system service for the name " + name + " is not registered" );
        }
        ObjectName objName = systemServices.remove( name );
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "Unregistering system service: " + objName );
        }
        unregisterMBean( objName );
    }

    public void unregisterMBean( ObjectName name )
        throws JBIException
    {
        try
        {
            server.unregisterMBean( name );
            beanMap.remove( name );
        }
        catch ( JMException e )
        {
            LOG.error( "Failed to unregister mbean: " + name, e );
            throw new JBIException( e );
        }
    }

    public void unregisterMBean( Object bean )
        throws JBIException
    {
        for ( Object element : beanMap.entrySet() )
        {
            Map.Entry entry = (Map.Entry) element;
            if ( entry.getValue() == bean )
            {
                ObjectName name = (ObjectName) entry.getKey();
                unregisterMBean( name );
                return;
            }
        }
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute( getObjectToManage(), "bindingComponents", "Get list of all binding components" );
        helper.addAttribute( getObjectToManage(), "engineComponents", "Get list of all engine components" );

        helper.addAttribute( getObjectToManage(), "systemInfo", "Return current version" );
        helper.addAttribute( getObjectToManage(), "systemServices", "Get list of system services" );
        return AttributeInfoHelper.join( super.getAttributeInfos(), helper.getAttributeInfos() );
    }

    @Override
    public MBeanOperationInfo[] getOperationInfos()
        throws JMException
    {
        OperationInfoHelper helper = new OperationInfoHelper();
        ParameterHelper ph =
            helper.addOperation( getObjectToManage(), "getComponentByName", 1, "look up Component by name" );
        ph.setDescription( 0, "name", "Component name" );
        ph = helper.addOperation( getObjectToManage(), "getSystemService", 1, "look up System service by name" );
        ph.setDescription( 0, "name", "System name" );
        ph = helper.addOperation( getObjectToManage(), "isBinding", 1, "Is Component a binding Component?" );
        ph.setDescription( 0, "name", "Component name" );
        ph = helper.addOperation( getObjectToManage(), "isEngine", 1, "Is Component a service engine?" );
        ph.setDescription( 0, "name", "Component name" );
        return OperationInfoHelper.join( super.getOperationInfos(), helper.getOperationInfos() );
    }
}