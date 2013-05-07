package com.sanxing.sesame.container;

import java.io.File;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.LifeCycleMBean;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.naming.InitialContext;

import org.slf4j.LoggerFactory;

import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.BaseLifeCycle;
import com.sanxing.sesame.mbean.ArchiveManager;
import com.sanxing.sesame.mbean.AutoDeploymentService;
import com.sanxing.sesame.mbean.BaseSystemService;
import com.sanxing.sesame.mbean.CommandsService;
import com.sanxing.sesame.mbean.DeploymentService;
import com.sanxing.sesame.mbean.InstallationService;
import com.sanxing.sesame.mbean.ManagementContext;
import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.router.MessageRouter;
import com.sanxing.sesame.router.Router;
import com.sanxing.sesame.uuid.IdGenerator;

public class JBIContainer
    extends BaseLifeCycle
{
    public static final String DEFAULT_NAME = "sesame";

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( JBIContainer.class );

    private MBeanServer mbeanServer;

    protected Router router = new MessageRouter();

    protected ServiceUnitManager serviceManager;

    protected ManagementContext managementContext = new ManagementContext();

    protected EnvironmentContext environmentContext = new EnvironmentContext();

    protected InstallationService installationService = new InstallationService();

    protected DeploymentService deploymentService = new DeploymentService();

    protected AutoDeploymentService autoDeployService = new AutoDeploymentService();

    protected CommandsService adminCommandsService = new CommandsService();

    protected ArchiveManager archiveManager = new ArchiveManager();

    protected BaseSystemService[] services;

    protected Registry registry = new Registry();

    protected boolean autoEnlistInTransaction;

    protected boolean persistent;

    protected boolean notifyStatistics;

    protected transient Thread shutdownHook;

    protected ExecutorFactory executorFactory;

    private String rootDir;

    private String generatedRootDirPrefix = "../work";

    private boolean generateRootDir;

    private final AtomicBoolean started = new AtomicBoolean( false );

    private final AtomicBoolean containerInitialized = new AtomicBoolean( false );

    private final IdGenerator idGenerator = new IdGenerator();

    private long forceShutdown;

    @Override
    public String getName()
    {
        return "sesame";
    }

    public String getJmxDomain()
    {
        return "com.sanxing.sesame";
    }

    public String getServerName()
    {
        return "sesame";
    }

    public AtomicBoolean getStarted()
    {
        return started;
    }

    @Override
    public String getDescription()
    {
        return "Sesame JBI Container";
    }

    public BaseSystemService[] getServices()
    {
        return services;
    }

    public void setServices( BaseSystemService[] services )
    {
        this.services = services;
    }

    public ManagementContext getManagementContext()
    {
        return managementContext;
    }

    public EnvironmentContext getEnvironmentContext()
    {
        return environmentContext;
    }

    public Registry getRegistry()
    {
        return registry;
    }

    public MessageRouter getDefaultBroker()
    {
        if ( !( router instanceof MessageRouter ) )
        {
            throw new IllegalStateException( "Router is not a MessageRouter" );
        }
        return ( (MessageRouter) router );
    }

    public Router getRouter()
    {
        return router;
    }

    public String getInstallationDirPath()
    {
        File dir = environmentContext.getInstallationDir();
        return ( ( dir != null ) ? dir.getAbsolutePath() : "" );
    }

    public void setInstallationDirPath( String installationDir )
    {
        if ( ( installationDir != null ) && ( installationDir.length() > 0 ) )
        {
            environmentContext.setInstallationDir( new File( installationDir ) );
        }
    }

    public String getDeploymentDirPath()
    {
        File dir = environmentContext.getDeploymentDir();
        return ( ( dir != null ) ? dir.getAbsolutePath() : "" );
    }

    public void setDeploymentDirPath( String deploymentDir )
    {
        if ( ( deploymentDir != null ) && ( deploymentDir.length() > 0 ) )
        {
            environmentContext.setDeploymentDir( new File( deploymentDir ) );
        }
    }

    public DeploymentService getDeploymentService()
    {
        return deploymentService;
    }

    public InstallationService getInstallationService()
    {
        return installationService;
    }

    public AutoDeploymentService getAutoDeploymentService()
    {
        return autoDeployService;
    }

    public CommandsService getAdminCommandsService()
    {
        return adminCommandsService;
    }

    public String getGeneratedRootDirPrefix()
    {
        return generatedRootDirPrefix;
    }

    public void setGeneratedRootDirPrefix( String generatedRootDirPrefix )
    {
        this.generatedRootDirPrefix = generatedRootDirPrefix;
    }

    public boolean isGenerateRootDir()
    {
        return generateRootDir;
    }

    public long getForceShutdown()
    {
        return forceShutdown;
    }

    public void setForceShutdown( long forceShutdown )
    {
        this.forceShutdown = forceShutdown;
    }

    public void setGenerateRootDir( boolean generateRootDir )
    {
        this.generateRootDir = generateRootDir;
    }

    public MBeanServer getMBeanServer()
    {
        return mbeanServer;
    }

    public ArchiveManager getArchiveManager()
    {
        return archiveManager;
    }

    @Override
    public void init()
        throws JBIException
    {
        if ( !( containerInitialized.compareAndSet( false, true ) ) )
        {
            return;
        }
        LOG.info( "Sesame " + EnvironmentContext.getVersion() + " JBI Container (" + getName() + ") is starting" );
        LOG.info( "--------------------------------------------------------------------------------------------" );

        if ( executorFactory == null )
        {
            executorFactory = ExecutorFactory.getFactory();
        }
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "executor factory is [" + getExecutorFactory() + "]" );
        }
        managementContext.init( this );
        mbeanServer = managementContext.getMBeanServer();
        environmentContext.init( this );
        registry.init( this );
        router.init( this );

        archiveManager.init( this );
        adminCommandsService.init( this );
        installationService.init( this );
        deploymentService.init( this );
        autoDeployService.init( this );

        if ( services != null )
        {
            for ( int i = 0; i < services.length; ++i )
            {
                services[i].init( this );
            }
        }

        try
        {
            managementContext.registerMBean(
                ManagementContext.getContainerObjectName( getJmxDomain(), getServerName() ), this, LifeCycleMBean.class );
        }
        catch ( JMException e )
        {
            throw new JBIException( e );
        }
    }

    @Override
    public void start()
        throws JBIException
    {
        checkInitialized();
        if ( started.compareAndSet( false, true ) )
        {
            managementContext.start();
            environmentContext.start();

            if ( services != null )
            {
                for ( int i = 0; i < services.length; ++i )
                {
                    services[i].start();
                }
            }
            router.start();
            registry.start();
            installationService.start();
            deploymentService.start();
            autoDeployService.start();
            adminCommandsService.start();
            super.start();
            LOG.info( "Sesame JBI Container (" + getName() + ") started" );
        }
    }

    @Override
    public void stop()
        throws JBIException
    {
        checkInitialized();
        if ( started.compareAndSet( true, false ) )
        {
            LOG.info( "Sesame JBI Container (" + getName() + ") stopping" );
            adminCommandsService.stop();
            autoDeployService.stop();
            deploymentService.stop();
            installationService.stop();
            registry.stop();
            router.stop();
            if ( services != null )
            {
                for ( int i = services.length - 1; i >= 0; --i )
                {
                    services[i].stop();
                }
            }
            environmentContext.stop();
            managementContext.stop();
            super.stop();
        }
    }

    @Override
    public void shutDown()
        throws JBIException
    {
        LOG.info( "shutdown" );
        if ( containerInitialized.compareAndSet( true, false ) )
        {
            LOG.info( "Shutting down Sesame JBI Container (" + getName() + ") stopped" );
            removeShutdownHook();
            adminCommandsService.shutDown();
            autoDeployService.shutDown();
            deploymentService.shutDown();
            installationService.shutDown();

            router.shutDown();
            shutdownRegistry();
            shutdownServices();

            environmentContext.shutDown();

            super.shutDown();

            managementContext.shutDown();
            LOG.info( "Sesame JBI Container (" + getName() + ") stopped" );
        }
    }

    private void shutdownServices()
        throws JBIException
    {
        if ( services != null )
        {
            for ( int i = services.length - 1; i >= 0; --i )
            {
                services[i].shutDown();
            }
        }
    }

    private void shutdownRegistry()
        throws JBIException
    {
        FutureTask shutdown = new FutureTask( new Callable()
        {
            @Override
            public Boolean call()
                throws Exception
            {
                registry.shutDown();
                return Boolean.valueOf( true );
            }
        } );
        Thread daemonShutDownThread = new Thread( shutdown );
        daemonShutDownThread.setDaemon( true );
        daemonShutDownThread.start();
        try
        {
            if ( forceShutdown > 0L )
            {
                LOG.info( "Waiting another " + forceShutdown
                    + " ms for complete shutdown of the components and service assemblies" );
                shutdown.get( forceShutdown, TimeUnit.MILLISECONDS );
            }
            else
            {
                LOG.info( "Waiting for complete shutdown of the components and service assemblies" );
                shutdown.get();
            }
            LOG.info( "Components and service assemblies have been shut down" );
        }
        catch ( Exception e )
        {
            LOG.warn( "Unable to shutdown components and service assemblies normally: " + e, e );
            LOG.warn( "Forcing shutdown by cancelling all pending exchanges" );
            registry.cancelPendingExchanges();
        }
    }

    protected void addShutdownHook()
    {
        shutdownHook = new Thread( "Sesame-ShutdownHook" )
        {
            @Override
            public void run()
            {
                JBIContainer.this.containerShutdown();
            }
        };
        Runtime.getRuntime().addShutdownHook( shutdownHook );
    }

    protected void removeShutdownHook()
    {
        if ( shutdownHook == null )
        {
            return;
        }
        try
        {
            Runtime.getRuntime().removeShutdownHook( shutdownHook );
        }
        catch ( Exception e )
        {
            LOG.debug( "Caught exception, must be shutting down: " + e );
        }
    }

    protected void containerShutdown()
    {
        try
        {
            shutDown();
        }
        catch ( Throwable e )
        {
            System.err.println( "Failed to shut down: " + e );
        }
    }

    public synchronized InitialContext getNamingContext()
    {
        return null;
    }

    public synchronized Object getTransactionManager()
    {
        return null;
    }

    public synchronized String getRootDir()
    {
        if ( rootDir == null )
        {
            if ( isGenerateRootDir() )
            {
                rootDir = createRootDir();
            }
            else
            {
                rootDir = "." + File.separator + "work";
            }
            LOG.debug( "Defaulting to rootDir: " + rootDir );
        }
        return rootDir;
    }

    public synchronized void setRootDir( String root )
    {
        rootDir = root;
    }

    public Logger getLogger( String name, String resourceBundleName )
        throws MissingResourceException, JBIException
    {
        try
        {
            Logger logger = Logger.getLogger( name, resourceBundleName );

            return logger;
        }
        catch ( IllegalArgumentException e )
        {
            throw new JBIException( "A logger can not be created using resource bundle " + resourceBundleName );
        }
    }

    protected String createComponentID()
    {
        return idGenerator.generateId();
    }

    protected void checkInitialized()
        throws JBIException
    {
        if ( !( containerInitialized.get() ) )
        {
            throw new JBIException( "The Container is not initialized - please call init(...)" );
        }
    }

    public boolean isAutoEnlistInTransaction()
    {
        return autoEnlistInTransaction;
    }

    public void setAutoEnlistInTransaction( boolean autoEnlistInTransaction )
    {
        this.autoEnlistInTransaction = autoEnlistInTransaction;
    }

    public boolean isPersistent()
    {
        return persistent;
    }

    public void setPersistent( boolean persistent )
    {
        this.persistent = persistent;
    }

    public ExecutorFactory getExecutorFactory()
    {
        return executorFactory;
    }

    protected String createRootDir()
    {
        String prefix = getGeneratedRootDirPrefix();
        for ( int i = 1;; ++i )
        {
            File file = new File( prefix + i );
            if ( !( file.exists() ) )
            {
                file.mkdirs();
                return file.getAbsolutePath();
            }
        }
    }

    public String getLoggerName()
    {
        return "sesame.log";
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        MBeanAttributeInfo[] attrs = super.getAttributeInfos();

        AttributeInfoHelper helper = new AttributeInfoHelper();

        helper.addAttribute( getObjectToManage(), "loggerName", "Current log name of container" );

        return AttributeInfoHelper.join( attrs, helper.getAttributeInfos() );
    }

    @Override
    public String toString()
    {
        return "SESAME JBI Container :" + getName() + "@" + getCurrentState();
    }
}