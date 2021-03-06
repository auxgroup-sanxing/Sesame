package com.sanxing.sesame.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.ContextNotEmptyException;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.core.naming.JNDIUtil;
import com.sanxing.sesame.jmx.DefaultJMXServiceURLBuilder;
import com.sanxing.sesame.jmx.JMXServiceURLBuilder;
import com.sanxing.sesame.jmx.mbean.PlatformManager;
import com.sanxing.sesame.jmx.mbean.PlatformManagerMBean;

public class Platform
{
    private static final Logger LOG;

    private final Env env = new Env();

    private MBeanServer mbeanServer;

    private JMXConnectorServer connectorServer;

    private JMXServiceURLBuilder jmxServiceURLBuilder;

    private BaseServer server;

    private final InitialContext namingContext;

    private transient Thread shutdownHook;

    private static Platform instance;

    static
    {
        parsePropertyFile( new File( System.getProperty( "SESAME_HOME" ), "conf/system.properties" ) );

        Console.echo( System.getProperty( "sesame.echo", "on" ).equals( "on" ) );
        
        System.setOut( Console.out );
        
        System.setErr( Console.err );

        validateLicense();

        LOG = LoggerFactory.getLogger( Platform.class );

        instance = new Platform();
    }

    private static void validateLicense()
    {
    }

    private static void parsePropertyFile( File propertiesFile )
    {
        Properties properites = new Properties();
        try
        {
            properites.load( new FileInputStream( propertiesFile ) );
            Enumeration enumer = properites.propertyNames();

            while ( enumer.hasMoreElements() )
            {
                String key = (String) enumer.nextElement();
                String value = properites.getProperty( key );

                if ( System.getProperty( key ) == null )
                {
                    System.setProperty( key, value );
                }
            }
        }
        catch ( IOException e )
        {
            LOG.error( "Load conf/system.properties failure!" );
        }
    }

    private Platform()
    {
        namingContext = JNDIUtil.getInitialContext();

        if ( env.isAdmin() )
        {
            server = new AdminServer();
            env.setClustered( false );
        }
        else
        {
            env.setClustered( true );
            server = new ManagedServer();
        }
    }

    public static Platform getPlatform()
    {
        return instance;
    }

    public static JMXServiceURLBuilder getJmxServiceURLBuilder()
    {
        return instance.jmxServiceURLBuilder;
    }

    private void start()
    {
        try
        {
            addShutdownHook();
            jmxServiceURLBuilder = new DefaultJMXServiceURLBuilder( server, env );

            addSystemClusterListener();
            server.start();
            PlatformManagerMBean coreManager = new PlatformManager();
            MBeanHelper.registerMBean( server.getMBeanServer(), coreManager,
                MBeanHelper.getPlatformMBeanName( "core-manager" ) );
            Console.echo( "" );
            Console.echo( "--------------------------------------------------------------------------------" );
            Console.echo( "Sesame server (" + server.getName() + ") started" );
            Console.echo( "--------------------------------------------------------------------------------" );
        }
        catch ( Exception e )
        {
            LOG.error( "Start server err", e );
        }
    }

    private void stop()
    {
        Console.echo( "" );
        Console.echo( "--------------------------------------------------------------------------------" );
        Console.echo( "Shuting down " + server.getName() + "..." );
        Console.echo( "--------------------------------------------------------------------------------" );
        server.shutdown();
        try
        {
            MBeanServer mbeanServer = server.getMBeanServer();
            ObjectName objectName = new ObjectName( getEnv().getDomain() + ":*" );
            Set<ObjectName> set = mbeanServer.queryNames( objectName, null );
            for ( ObjectName name : set )
            {
                try
                {
                    if ( mbeanServer.isRegistered( name ) )
                    {
                        mbeanServer.unregisterMBean( name );
                    }
                }
                catch ( Throwable t )
                {
                    LOG.debug( name.toString() );
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( t.getMessage(), t );
                    }
                }
            }
            LOG.debug( "MBeans cleaned" );

            connectorServer.stop();
        }
        catch ( MalformedObjectNameException localMalformedObjectNameException )
        {
        }
        catch ( IOException e )
        {
            if ( ( e.getCause() != null ) && ( e.getCause() instanceof ContextNotEmptyException ) )
            {
                try
                {
                    InitialContext context = getNamingContext();
                    NamingEnumeration enumer = context.list( "" );
                    while ( enumer.hasMore() )
                    {
                        NameClassPair pair = (NameClassPair) enumer.next();
                        context.unbind( pair.getName() );
                    }
                }
                catch ( NamingException ex )
                {
                    LOG.debug( ex.getMessage(), ex );
                }
            }
            else
            {
                LOG.error( e.getMessage(), e );
            }
        }

        Console.echo( "--------------------------------------------------------------------------------" );
        Console.echo( "Sesame server (" + server.getName() + ") shutdown complete" );
        Console.echo( "--------------------------------------------------------------------------------" );
    }

    private void addShutdownHook()
    {
        shutdownHook = new Thread( "Sesame-Platform-ShutdownHook" )
        {
            @Override
            public void run()
            {
                Platform.this.stop();
            }
        };
        shutdownHook.setDaemon( true );

        Runtime.getRuntime().addShutdownHook( shutdownHook );
    }

    private void addSystemClusterListener()
    {
    }

    void startJMXServer()
    {
        try
        {
            LOG.info( "create mbean server on server [" + env.getServerName() + "]" );
            mbeanServer = MBeanServerFactory.createMBeanServer( env.getDomain() );
            JMXServiceURL serviceURL = jmxServiceURLBuilder.getLocalJMXServiceURL();
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer( serviceURL, null, mbeanServer );
            connectorServer.start();
            LOG.info( "jmx connector server started @" + serviceURL.toString() );
        }
        catch ( IOException e )
        {
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( e.getMessage(), e );
            }
            else
            {
                LOG.error( e.getMessage() );
            }
        }
    }

    public static Env getEnv()
    {
        return instance.env;
    }

    public static MBeanServer getLocalMBeanServer()
    {
        return instance.mbeanServer;
    }

    public static void startup()
    {
        instance.start();
    }

    public static void shutdown()
    {
        instance.stop();
    }

    public <T> T getAdminMBean( Class<T> clazz, ObjectName name )
    {
        try
        {
            JMXServiceURLBuilder builder = jmxServiceURLBuilder;
            JMXServiceURL adminServerURL = builder.getAdminJMXServiceURL();
            Map environment = new HashMap();
            environment.put( "java.naming.factory.initial", "com.sun.jndi.rmi.registry.RegistryContextFactory" );
            JMXConnector adminServerConnector = JMXConnectorFactory.connect( adminServerURL, environment );
            MBeanServerConnection adminServerCon = adminServerConnector.getMBeanServerConnection();

            return MBeanServerInvocationHandler.newProxyInstance( adminServerCon, name, clazz, false );
        }
        catch ( Exception e )
        {
        }
        return null;
    }

    public static InitialContext getNamingContext()
    {
        if ( instance.namingContext == null )
        {
            throw new RuntimeException( "Naming context initialized failure" );
        }
        return instance.namingContext;
    }

    @Override
    public String toString()
    {
        return env.toString();
    }
}