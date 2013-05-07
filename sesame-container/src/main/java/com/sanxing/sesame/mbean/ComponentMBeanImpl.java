package com.sanxing.sesame.mbean;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.DeliveryChannel;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.container.ActivationSpec;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.BaseLifeCycle;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.messaging.DeliveryChannelImpl;
import com.sanxing.sesame.util.XmlPersistenceSupport;

public class ComponentMBeanImpl
    extends BaseLifeCycle
    implements ComponentMBean
{
    private static final Logger LOG = LoggerFactory.getLogger( ComponentMBeanImpl.class );

    private boolean exchangeThrottling;

    private long throttlingTimeout = 100L;

    private int throttlingInterval = 1;

    private Component component;

    private ComponentLifeCycle lifeCycle;

    private ServiceUnitManager suManager;

    private ComponentContextImpl context;

    private ActivationSpec activationSpec;

    private ObjectName mBeanName;

    private final JBIContainer container;

    private final ComponentNameSpace componentName;

    private String description = "POJO Component";

    private int queueCapacity = 1024;

    private boolean pojo;

    private final boolean binding;

    private final boolean service;

    private File stateFile;

    private final String[] sharedLibraries;

    public ComponentMBeanImpl( JBIContainer container, ComponentNameSpace name, String description,
                               Component component, boolean binding, boolean service, String[] sharedLibraries )
    {
        componentName = name;
        this.container = container;
        this.component = component;
        this.description = description;
        this.binding = binding;
        this.service = service;
        this.sharedLibraries = sharedLibraries;
    }

    public void dispose()
    {
        ClassLoader cl = component.getClass().getClassLoader();
        lifeCycle = null;
        suManager = null;
        component = null;
    }

    public ObjectName registerMBeans( ManagementContext ctx )
        throws JBIException
    {
        try
        {
            mBeanName = ctx.createObjectName( this );
            ctx.registerMBean( mBeanName, this, ComponentMBean.class );
            return mBeanName;
        }
        catch ( Exception e )
        {
            String errorStr = "Failed to register MBeans";
            LOG.error( errorStr, e );
            throw new JBIException( errorStr, e );
        }
    }

    public void unregisterMbeans( ManagementContext ctx )
        throws JBIException
    {
        ctx.unregisterMBean( mBeanName );
    }

    public void setContext( ComponentContextImpl ctx )
    {
        context = ctx;
        stateFile = ctx.getEnvironment().getStateFile();
    }

    @Override
    public ObjectName getExtensionMBeanName()
    {
        if ( ( isInitialized() ) || ( isStarted() ) || ( isStopped() ) )
        {
            return lifeCycle.getExtensionMBeanName();
        }
        return null;
    }

    @Override
    public String getName()
    {
        return componentName.getName();
    }

    @Override
    public String getType()
    {
        return "Component";
    }

    @Override
    public String getSubType()
    {
        return "LifeCycle";
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void init()
        throws JBIException
    {
        LOG.info( "Initializing component: " + getName() );
        if ( ( context != null ) && ( component != null ) )
        {
            DeliveryChannelImpl channel = new DeliveryChannelImpl( this );
            channel.setContext( context );
            context.setDeliveryChannel( channel );
            super.init();

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try
            {
                Thread.currentThread().setContextClassLoader( getLifeCycle().getClass().getClassLoader() );
                getLifeCycle().init( context );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( loader );
            }
        }
    }

    @Override
    public void start()
        throws JBIException
    {
        LOG.info( "Starting component: " + getName() );
        try
        {
            doStart();
            persistRunningState();
            getContainer().getRegistry().checkPendingAssemblies();
        }
        catch ( JBIException e )
        {
            LOG.error( "Could not start component", e );
            throw e;
        }
        catch ( RuntimeException e )
        {
            LOG.error( "Could not start component", e );
            throw e;
        }
        catch ( Error e )
        {
            LOG.error( "Could not start component", e );
            throw e;
        }
    }

    @Override
    public void stop()
        throws JBIException
    {
        LOG.info( "Stopping component: " + getName() );
        try
        {
            doStop();
            persistRunningState();
        }
        catch ( JBIException e )
        {
            LOG.error( "Could not stop component", e );
            throw e;
        }
        catch ( RuntimeException e )
        {
            LOG.error( "Could not start component", e );
            throw e;
        }
        catch ( Error e )
        {
            LOG.error( "Could not start component", e );
            throw e;
        }
    }

    @Override
    public void shutDown()
        throws JBIException
    {
        LOG.info( "Shutting down component: " + getName() );
        try
        {
            doShutDown();
            persistRunningState();
        }
        catch ( JBIException e )
        {
            LOG.error( "Could not shutDown component", e );
            throw e;
        }
        catch ( RuntimeException e )
        {
            LOG.error( "Could not start component", e );
            throw e;
        }
        catch ( Error e )
        {
            LOG.error( "Could not start component", e );
            throw e;
        }
    }

    public void setShutdownStateAfterInstall()
    {
        setCurrentState( "Shutdown" );
    }

    public void doStart()
        throws JBIException
    {
        if ( isShutDown() )
        {
            init();
        }
        if ( !( isStarted() ) )
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try
            {
                Thread.currentThread().setContextClassLoader( getLifeCycle().getClass().getClassLoader() );
                getLifeCycle().start();
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( loader );
            }
            super.start();
            initServiceAssemblies();
            startServiceAssemblies();
        }
    }

    public void doStop()
        throws JBIException
    {
        if ( ( isUnknown() ) || ( isStarted() ) )
        {
            stopServiceAssemblies();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try
            {
                Thread.currentThread().setContextClassLoader( getLifeCycle().getClass().getClassLoader() );
                getLifeCycle().stop();
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( loader );
            }
            super.stop();
        }
    }

    public void doShutDown()
        throws JBIException
    {
        if ( ( !( isUnknown() ) ) && ( !( isShutDown() ) ) )
        {
            doStop();
            shutDownServiceAssemblies();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try
            {
                Thread.currentThread().setContextClassLoader( getLifeCycle().getClass().getClassLoader() );
                getLifeCycle().shutDown();
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( loader );
            }
            if ( getDeliveryChannel() != null )
            {
                getDeliveryChannel().close();
                setDeliveryChannel( null );
            }
            lifeCycle = null;
            suManager = null;
        }
        super.shutDown();
    }

    public void setInitialRunningState()
        throws JBIException
    {
        if ( !( isPojo() ) )
        {
            String name = getName();
            String runningState = getRunningStateFromStore();
            LOG.info( "Setting running state for Component: " + name + " to " + runningState );
            if ( runningState != null )
            {
                if ( runningState.equals( "Started" ) )
                {
                    doStart();
                }
                else if ( runningState.equals( "Stopped" ) )
                {
                    doStart();
                    doStop();
                }
                else if ( runningState.equals( "Shutdown" ) )
                {
                    doShutDown();
                }
            }
        }
    }

    public void persistRunningState()
    {
        if ( !( isPojo() ) )
        {
            String name = getName();
            try
            {
                String currentState = getCurrentState();
                Properties props = new Properties();
                props.setProperty( "state", currentState );
                XmlPersistenceSupport.write( stateFile, props );
            }
            catch ( IOException e )
            {
                LOG.error( "Failed to write current running state for Component: " + name, e );
            }
        }
    }

    public String getRunningStateFromStore()
    {
        String result = "Unknown";
        String name = getName();
        try
        {
            Properties props = (Properties) XmlPersistenceSupport.read( stateFile );
            result = props.getProperty( "state", result );
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to read running state for Component: " + name, e );
        }
        return result;
    }

    public int getInboundQueueCapacity()
    {
        return queueCapacity;
    }

    public void setInboundQueueCapacity( int value )
    {
        if ( getDeliveryChannel() != null )
        {
            throw new IllegalStateException( "The component must be shut down before changing queue capacity" );
        }
        queueCapacity = value;
    }

    public DeliveryChannel getDeliveryChannel()
    {
        return context.getDeliveryChannel();
    }

    public void setDeliveryChannel( DeliveryChannel deliveryChannel )
    {
        context.setDeliveryChannel( deliveryChannel );
    }

    public ActivationSpec getActivationSpec()
    {
        return activationSpec;
    }

    public boolean isPojo()
    {
        return pojo;
    }

    public void setActivationSpec( ActivationSpec activationSpec )
    {
        this.activationSpec = activationSpec;
    }

    @Override
    public boolean isExchangeThrottling()
    {
        return exchangeThrottling;
    }

    @Override
    public void setExchangeThrottling( boolean value )
    {
        exchangeThrottling = value;
    }

    @Override
    public long getThrottlingTimeout()
    {
        return throttlingTimeout;
    }

    @Override
    public void setThrottlingTimeout( long value )
    {
        throttlingTimeout = value;
    }

    @Override
    public int getThrottlingInterval()
    {
        return throttlingInterval;
    }

    @Override
    public void setThrottlingInterval( int value )
    {
        throttlingInterval = value;
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute( getObjectToManage(), "componentType", "the type of this component (BC, SE, POJO)" );
        helper.addAttribute( getObjectToManage(), "inboundQueueCapacity", "capacity of the inbound queue" );
        helper.addAttribute( getObjectToManage(), "exchangeThrottling", "apply throttling" );
        helper.addAttribute( getObjectToManage(), "throttlingTimeout", "timeout for throttling" );
        helper.addAttribute( getObjectToManage(), "throttlingInterval", "exchange intervals before throttling" );
        helper.addAttribute( getObjectToManage(), "extensionMBeanName", "extension mbean name" );
        return AttributeInfoHelper.join( super.getAttributeInfos(), helper.getAttributeInfos() );
    }

    @Override
    public MBeanOperationInfo[] getOperationInfos()
        throws JMException
    {
        OperationInfoHelper helper = new OperationInfoHelper();
        return OperationInfoHelper.join( super.getOperationInfos(), helper.getOperationInfos() );
    }

    @Override
    public void firePropertyChanged( String name, Object oldValue, Object newValue )
    {
        super.firePropertyChanged( name, oldValue, newValue );
    }

    protected void initServiceAssemblies()
        throws DeploymentException
    {
    }

    protected void startServiceAssemblies()
        throws DeploymentException
    {
    }

    protected void stopServiceAssemblies()
        throws DeploymentException
    {
        Registry registry = getContainer().getRegistry();
        String[] sas = registry.getDeployedServiceAssembliesForComponent( getName() );
        for ( int i = 0; i < sas.length; ++i )
        {
            ServiceAssemblyLifeCycle sa = registry.getServiceAssembly( sas[i] );
            if ( !( sa.isStarted() ) )
            {
                continue;
            }
            try
            {
                sa.stop( false, false );
                registry.addPendingAssembly( sa );
            }
            catch ( Exception e )
            {
                LOG.error( "Error stopping service assembly " + sas[i] );
            }
        }
    }

    protected void shutDownServiceAssemblies()
        throws DeploymentException
    {
        Registry registry = getContainer().getRegistry();
        String[] sas = registry.getDeployedServiceAssembliesForComponent( getName() );
        for ( int i = 0; i < sas.length; ++i )
        {
            ServiceAssemblyLifeCycle sa = registry.getServiceAssembly( sas[i] );
            if ( !( sa.isStopped() ) )
            {
                continue;
            }
            try
            {
                sa.shutDown( false );
                registry.addPendingAssembly( sa );
            }
            catch ( Exception e )
            {
                LOG.error( "Error shutting down service assembly " + sas[i] );
            }
        }
    }

    public ComponentLifeCycle getLifeCycle()
    {
        if ( lifeCycle == null )
        {
            lifeCycle = component.getLifeCycle();
        }
        return lifeCycle;
    }

    public ServiceUnitManager getServiceUnitManager()
    {
        if ( suManager == null )
        {
            suManager = component.getServiceUnitManager();
        }
        return suManager;
    }

    public JBIContainer getContainer()
    {
        return container;
    }

    public Component getComponent()
    {
        return component;
    }

    public ComponentNameSpace getComponentNameSpace()
    {
        return componentName;
    }

    public ComponentContextImpl getContext()
    {
        return context;
    }

    public ObjectName getMBeanName()
    {
        return mBeanName;
    }

    public boolean isBinding()
    {
        return binding;
    }

    public boolean isService()
    {
        return service;
    }

    public void setPojo( boolean pojo )
    {
        this.pojo = pojo;
    }

    public boolean isEngine()
    {
        return service;
    }

    public String[] getSharedLibraries()
    {
        return sharedLibraries;
    }

    @Override
    public String getComponentType()
    {
        return ( ( isEngine() ) ? "service-engine" : ( isBinding() ) ? "binding-component" : "pojo" );
    }
}