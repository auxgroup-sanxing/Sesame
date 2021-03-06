package com.sanxing.sesame.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sanxing.sesame.jmx.mbean.PlatformManagerMBean;
import com.sanxing.sesame.jmx.mbean.ServerManagerMBean;

public class Shutdown
{
    public static String PM_MBEANNAME =
        "com.sanxing.sesame:ServerName=${server-name},Type=Platform,Name=platform-manager";

    public static String SM_MBeanName =
        "com.sanxing.sesame:ServerName=${server-name},Type=Platform,Name=server-manager";

    private String option = "-f";

    private String serverName = "admin";

    private final String adminHost = "127.0.0.1";

    private int adminPort = 2099;

    public Shutdown()
    {
        adminPort = Integer.parseInt( System.getProperty( "admin-port", "2099" ) );
    }

    private static <T> T getAdminMBean( Class<T> clazz, ObjectName name, String host, int port )
    {
        try
        {
            JMXServiceURL adminServerURL =
                new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/admin" );

            Map environment = new HashMap();
            environment.put( "java.naming.factory.initial", "com.sun.jndi.rmi.registry.RegistryContextFactory" );
            JMXConnector adminServerConnector = JMXConnectorFactory.connect( adminServerURL, environment );
            MBeanServerConnection adminServerCon = adminServerConnector.getMBeanServerConnection();

            return MBeanServerInvocationHandler.newProxyInstance( adminServerCon, name, clazz, false );
        }
        catch ( MalformedURLException e )
        {
            throw new RuntimeException( e.getMessage() );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Connect to admin server failure(maybe shutted down)\n" + e.getMessage() );
        }
    }

    private void parseArgs( String[] args )
    {
        option = "-h";

        serverName = "admin";

        if ( args.length == 2 )
        {
            if ( ( args[0].equalsIgnoreCase( "-h" ) ) || ( args[0].equalsIgnoreCase( "-hard" ) ) )
            {
                option = "-h";
            }
            else if ( ( args[0].equalsIgnoreCase( "-s" ) ) || ( args[0].equalsIgnoreCase( "-soft" ) ) )
            {
                option = "-s";
            }

            serverName = args[1];
        }
        else if ( args.length == 1 )
        {
            serverName = args[0];
        }
    }

    private static void shutdown( String adminHost, int adminPort, String serverName )
        throws MalformedObjectNameException
    {
        ObjectName pmName = ObjectName.getInstance( PM_MBEANNAME.replace( "${server-name}", serverName ) );

        PlatformManagerMBean pmm = getAdminMBean( PlatformManagerMBean.class, pmName, adminHost, adminPort );
        pmm.shutdown();
    }

    private static void stop( String adminHost, int adminPort, String serverName )
        throws MalformedObjectNameException
    {
        ObjectName smName = ObjectName.getInstance( SM_MBeanName.replace( "${server-name}", serverName ) );

        ServerManagerMBean sm = getAdminMBean( ServerManagerMBean.class, smName, adminHost, adminPort );
        sm.stop();
    }

    @Override
    public String toString()
    {
        return "Shutdown [adminHost=" + adminHost + ", adminPort=" + adminPort + ", option=" + option + ", serverName="
            + serverName + "]";
    }

    public static void main( String[] args )
    {
        String strServerDir = System.getProperty( "SESAME_HOME" );
        if ( strServerDir == null )
        {
            strServerDir = new File( System.getProperty( "user.dir" ) ).getParent();
            System.setProperty( "SESAME_HOME", strServerDir );
        }

        Properties properites = new Properties();
        File profile = new File( System.getProperty( "SESAME_HOME" ), "conf/system.properties" );
        try
        {
            properites.load( new FileInputStream( profile ) );
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
            System.out.println( "Load conf/system.properties failure!" );
        }

        Shutdown cmd = new Shutdown();
        try
        {
            cmd.parseArgs( args );

            if ( cmd.option.equals( "-s" ) )
            {
                System.out.println( "stopping .............." );
                stop( cmd.adminHost, cmd.adminPort, cmd.serverName );
                System.out.println( "server [" + cmd.serverName + "] is stopped" );
                return;
            }

            if ( cmd.option.equals( "-h" ) )
            {
                System.out.println( "Shutting down  " + cmd.serverName + " ......" );
                shutdown( cmd.adminHost, cmd.adminPort, cmd.serverName );
                System.out.println( "Server [" + cmd.serverName + "] is shutted down" );
            }
        }
        catch ( Exception e )
        {
            if ( e.getMessage() != null )
            {
                System.err.println( e.getMessage() );
            }
        }
    }
}