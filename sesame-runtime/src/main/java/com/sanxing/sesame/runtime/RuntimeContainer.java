package com.sanxing.sesame.runtime;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.Executor;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.address.AddressBook;
import com.sanxing.sesame.component.ClientComponent;
import com.sanxing.sesame.component.SchemaComponent;
import com.sanxing.sesame.container.ActivationSpec;
import com.sanxing.sesame.container.ComponentEnvironment;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.core.api.Container;
import com.sanxing.sesame.core.api.ContainerContext;
import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.jmx.mbean.admin.ServerInfo;
import com.sanxing.sesame.logging.lucene.LuceneTask;
import com.sanxing.sesame.logging.service.JMSTopicConsumer;
import com.sanxing.sesame.mbean.ComponentContextImpl;
import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.platform.events.ArchiveListener;
import com.sanxing.sesame.platform.events.ContainerJoinListener;
import com.sanxing.sesame.platform.events.EndpointsListener;
import com.sanxing.sesame.serial.SerialGenerator;
import com.sanxing.sesame.transport.Protocols;

public class RuntimeContainer
    extends JBIContainer
    implements Container
{
    private static final Logger LOG = LoggerFactory.getLogger( RuntimeContainer.class );

    private ContainerContext containerContext = null;

    @Override
    public void init( ContainerContext context )
        throws Exception
    {
        containerContext = context;

        executorFactory = containerContext.getServer().getExecutorFactory();

        setRootDir( new File( containerContext.getEnv().getHomeDir(), "work" ).toString() );

        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( context.getContainerClassLoader() );

            Protocols.init();

            AddressBook.init();

            SerialGenerator.initialize();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( saved );
        }

        init();

        containerContext.getServer().addListener( new ContainerJoinListener( this ) );
        if ( !( Platform.getEnv().isAdmin() ) )
        {
            containerContext.getServer().addListener( new ArchiveListener( this ) );
            containerContext.getServer().addListener( new EndpointsListener( this ) );
        }
        else
        {
            ServerInfo serverInfo = context.getServer().getConfig();
            startLogService( serverInfo );
        }
    }

    private boolean isLogServiceOn()
    {
        String start = System.getProperty( "sesame.logging.monitor", "no" );

        return ( ( start != null ) && ( start.equalsIgnoreCase( "yes" ) ) );
    }

    private void startLogService( ServerInfo serverInfo )
    {
        if ( !( isLogServiceOn() ) )
        {
            LOG.info( "To start log service, Please set sesame.logging.monitor equal to yes!!" );

            return;
        }

        JMSTopicConsumer receiver = new JMSTopicConsumer();
        Executor executor = ExecutorFactory.getFactory().createExecutor( "system.logginservice" );
        executor.execute( receiver );

        Timer timer = new Timer();
        LuceneTask task = new LuceneTask();
        timer.schedule( task, new Date(), 10000L );

        LOG.debug( "Start Logger Service success" );
    }

    public String getContainerName()
    {
        return containerContext.getContainerName();
    }

    @Override
    public void start()
    {
        try
        {
            SchemaComponent schema = new SchemaComponent();
            ActivationSpec activationSpec = new ActivationSpec();
            activationSpec.setComponent( schema );
            activationSpec.setComponentName( "schema" );
            activateComponent( schema, schema.getDescription(), activationSpec, true, schema.isBindingComponent(),
                schema.isEngineComponent(), null );

            ClientComponent client = ClientComponent.getInstance();
            activationSpec = new ActivationSpec();
            activationSpec.setComponent( client );
            activationSpec.setComponentName( "client" );
            activateComponent( client, client.getDescription(), activationSpec, true, client.isBindingComponent(),
                client.isEngineComponent(), null );

            super.start();
        }
        catch ( JBIException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    @Override
    public void shutdown()
    {
        try
        {
            LOG.debug( "Shutdown container:" + getName() );
            super.shutDown();
        }
        catch ( JBIException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    @Override
    public String getName()
    {
        return containerContext.getServer().getName();
    }

    @Override
    public String getJmxDomain()
    {
        return containerContext.getEnv().getDomain();
    }

    @Override
    public String getServerName()
    {
        return containerContext.getEnv().getServerName();
    }

    @Override
    public MBeanServer getMBeanServer()
    {
        return containerContext.getMbeanServer();
    }

    @Override
    public InitialContext getNamingContext()
    {
        return containerContext.getServerJNDIContext();
    }

    public ObjectName activateComponent( Component component, String description, ActivationSpec activationSpec,
                                         boolean pojo, boolean binding, boolean service, String[] sharedLibraries )
        throws JBIException
    {
        ComponentNameSpace cns = new ComponentNameSpace( getName(), activationSpec.getComponentName() );
        if ( registry.getComponent( cns ) != null )
        {
            throw new JBIException( "A component is already registered for " + cns );
        }
        ComponentContextImpl context = new ComponentContextImpl( this, cns );
        return activateComponent( new File( "." ), component, description, context, activationSpec, pojo, binding,
            service, sharedLibraries );
    }

    public ObjectName activateComponent( File installationDir, Component component, String description,
                                         ComponentContextImpl context, ActivationSpec activationSpec, boolean pojo,
                                         boolean binding, boolean service, String[] sharedLibraries )
        throws JBIException
    {
        ObjectName result = null;
        ComponentNameSpace cns = new ComponentNameSpace( getName(), activationSpec.getComponentName() );
        if ( LOG.isDebugEnabled() )
        {
            LOG.info( "Activating component for: " + cns + " with service: " + activationSpec.getService()
                + " component: " + component );
        }
        ComponentMBeanImpl lcc =
            registry.registerComponent( cns, description, component, binding, service, sharedLibraries );
        if ( lcc != null )
        {
            lcc.setPojo( pojo );
            ComponentEnvironment env = environmentContext.registerComponent( context.getEnvironment(), lcc );
            if ( env.getInstallRoot() == null )
            {
                env.setInstallRoot( installationDir );
            }
            context.activate( component, env, activationSpec );
            lcc.setContext( context );
            lcc.setActivationSpec( activationSpec );

            if ( lcc.isPojo() )
            {
                lcc.init();
            }
            else
            {
                lcc.doShutDown();
            }

            result = lcc.registerMBeans( managementContext );

            if ( ( lcc.isPojo() ) && ( isStarted() ) )
            {
                lcc.start();
            }
        }
        return result;
    }
}