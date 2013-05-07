package com.sanxing.sesame.mbean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.deployment.Descriptor;
import com.sanxing.sesame.deployment.DescriptorFactory;
import com.sanxing.sesame.deployment.ServiceUnit;
import com.sanxing.sesame.deployment.Services;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.MBeanInfoProvider;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.management.OperationInfoHelper;

public class ServiceUnitLifeCycle
    implements ServiceUnitMBean, MBeanInfoProvider
{
    private static final Logger LOG = LoggerFactory.getLogger( ServiceUnitLifeCycle.class );

    private final ServiceUnit serviceUnit;

    private String currentState = "Shutdown";

    private final String serviceAssembly;

    private final Registry registry;

    private PropertyChangeListener listener;

    private Services services;

    private final File rootDir;

    public ServiceUnitLifeCycle( ServiceUnit serviceUnit, String serviceAssembly, Registry registry, File rootDir )
    {
        this.serviceUnit = serviceUnit;
        this.serviceAssembly = serviceAssembly;
        this.registry = registry;
        this.rootDir = rootDir;
        Descriptor d = DescriptorFactory.buildDescriptor( rootDir );
        if ( d != null )
        {
            services = d.getServices();
        }
    }

    public void init()
        throws DeploymentException
    {
        LOG.info( "Initializing service unit: " + getName() );
        checkComponentStarted( "init" );
        ServiceUnitManager sum = getServiceUnitManager();
        File path = getServiceUnitRootPath();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( getComponentClassLoader() );
            sum.init( getName(), path.getAbsolutePath() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( cl );
        }
        currentState = "Stopped";
    }

    public void start()
        throws DeploymentException
    {
        LOG.info( "Starting service unit: " + getName() );
        checkComponentStarted( "start" );
        ServiceUnitManager sum = getServiceUnitManager();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( getComponentClassLoader() );
            sum.start( getName() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( cl );
        }
        currentState = "Started";
    }

    public void stop()
        throws DeploymentException
    {
        LOG.info( "Stopping service unit: " + getName() );
        checkComponentStarted( "stop" );
        ServiceUnitManager sum = getServiceUnitManager();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( getComponentClassLoader() );
            sum.stop( getName() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( cl );
        }
        currentState = "Stopped";
    }

    public void shutDown()
        throws DeploymentException
    {
        LOG.info( "Shutting down service unit: " + getName() );
        checkComponentStartedOrStopped( "shutDown" );
        ServiceUnitManager sum = getServiceUnitManager();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( getComponentClassLoader() );
            sum.shutDown( getName() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( cl );
        }
        currentState = "Shutdown";
    }

    @Override
    public String getCurrentState()
    {
        return currentState;
    }

    public boolean isShutDown()
    {
        return currentState.equals( "Shutdown" );
    }

    public boolean isStopped()
    {
        return currentState.equals( "Stopped" );
    }

    public boolean isStarted()
    {
        return currentState.equals( "Started" );
    }

    @Override
    public String getName()
    {
        return serviceUnit.getIdentification().getName();
    }

    @Override
    public String getDescription()
    {
        return serviceUnit.getIdentification().getDescription();
    }

    @Override
    public String getComponentName()
    {
        return serviceUnit.getTarget().getComponentName();
    }

    @Override
    public String getServiceAssembly()
    {
        return serviceAssembly;
    }

    @Override
    public String getDescriptor()
    {
        File suDir = getServiceUnitRootPath();
        return DescriptorFactory.getDescriptorAsText( suDir );
    }

    public Services getServices()
    {
        return services;
    }

    protected void checkComponentStarted( String task )
        throws DeploymentException
    {
        String componentName = getComponentName();
        String suName = getName();
        ComponentMBeanImpl lcc = registry.getComponent( componentName );
        if ( lcc == null )
        {
            throw ManagementSupport.componentFailure( "deploy", componentName, "Target component " + componentName
                + " for service unit " + suName + " is not installed" );
        }

        if ( !( lcc.isStarted() ) )
        {
            throw ManagementSupport.componentFailure( "deploy", componentName, "Target component " + componentName
                + " for service unit " + suName + " is not started" );
        }

        if ( lcc.getServiceUnitManager() == null )
        {
            throw ManagementSupport.componentFailure( "deploy", componentName, "Target component " + componentName
                + " for service unit " + suName + " does not accept deployments" );
        }
    }

    protected void checkComponentStartedOrStopped( String task )
        throws DeploymentException
    {
        String componentName = getComponentName();
        String suName = getName();
        ComponentMBeanImpl lcc = registry.getComponent( componentName );
        if ( lcc == null )
        {
            throw ManagementSupport.componentFailure( "deploy", componentName, "Target component " + componentName
                + " for service unit " + suName + " is not installed" );
        }

        if ( ( !( lcc.isStarted() ) ) && ( !( lcc.isStopped() ) ) )
        {
            throw ManagementSupport.componentFailure( "deploy", componentName, "Target component " + componentName
                + " for service unit " + suName + " is not started" );
        }

        if ( lcc.getServiceUnitManager() == null )
        {
            throw ManagementSupport.componentFailure( "deploy", componentName, "Target component " + componentName
                + " for service unit " + suName + " does not accept deployments" );
        }
    }

    protected File getServiceUnitRootPath()
    {
        return rootDir;
    }

    protected ServiceUnitManager getServiceUnitManager()
    {
        ComponentMBeanImpl lcc = registry.getComponent( getComponentName() );
        return lcc.getServiceUnitManager();
    }

    protected ClassLoader getComponentClassLoader()
    {
        ComponentMBeanImpl lcc = registry.getComponent( getComponentName() );

        return lcc.getComponent().getClass().getClassLoader();
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute( getObjectToManage(), "currentState", "current state of the service unit" );
        helper.addAttribute( getObjectToManage(), "name", "name of the service unit" );
        helper.addAttribute( getObjectToManage(), "componentName", "component name of the service unit" );
        helper.addAttribute( getObjectToManage(), "serviceAssembly", "service assembly name of the service unit" );
        helper.addAttribute( getObjectToManage(), "description", "description of the service unit" );
        return helper.getAttributeInfos();
    }

    @Override
    public MBeanOperationInfo[] getOperationInfos()
        throws JMException
    {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation( getObjectToManage(), "getDescriptor", "retrieve the jbi descriptor for this unit" );
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
        return "ServiceUnitAdaptor";
    }

    @Override
    public String getSubType()
    {
        return getComponentName();
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

    public String getKey()
    {
        return getComponentName() + "/" + getName();
    }
}