package com.sanxing.sesame.jmx.mbean.admin;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.AdminServer;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.jmx.RemoteMBeanProxy;

public class LinkCreator
    implements LinkCreatorMBean
{
    private final AdminServer admin;

    private static final Logger LOG = LoggerFactory.getLogger( LinkCreator.class );

    public LinkCreator( AdminServer server )
    {
        admin = server;
    }

    @Override
    public void register( ObjectName name, String serverName )
    {
        try
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "linking mbean [" + name + "] @" + serverName + " to admin" );
            }
            ObjectName nameOnAdmin = name;
            JMXServiceURL managedServerURL =
                Platform.getJmxServiceURLBuilder().getJMXServiceURLByServerName( serverName );
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( managedServerURL.toString() );
            }
            JMXConnector managedServerConnector = admin.getJMXConnectorByServer( serverName );
            MBeanServerConnection managedServerCon = managedServerConnector.getMBeanServerConnection();
            RemoteMBeanProxy proxy = new RemoteMBeanProxy( name, managedServerCon );
            try
            {
                admin.getMBeanServer().unregisterMBean( nameOnAdmin );
            }
            catch ( Throwable localThrowable )
            {
            }
            admin.getMBeanServer().registerMBean( proxy, nameOnAdmin );
        }
        catch ( Exception e )
        {
            LOG.error( "link to managed server err", e );
        }
    }

    @Override
    public void unregister( ObjectName name, String serverName )
    {
        try
        {
            admin.getMBeanServer().unregisterMBean( name );
        }
        catch ( Throwable e )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( e.getMessage() );
            }
        }
    }
}