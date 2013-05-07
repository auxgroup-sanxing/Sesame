package com.sanxing.sesame.jmx;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.Env;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.jmx.mbean.admin.ClusterAdminMBean;
import com.sanxing.sesame.jmx.mbean.admin.ServerInfo;

public class DefaultJMXServiceURLBuilder
    implements JMXServiceURLBuilder
{
    private static final Logger LOG = LoggerFactory.getLogger( DefaultJMXServiceURLBuilder.class );

    Registry reg = null;

    Env env = null;

    BaseServer server = null;

    private int port;

    public DefaultJMXServiceURLBuilder( BaseServer _server, Env env )
    {
        server = _server;
        this.env = env;
        port = env.getAdminPort();

        if ( reg != null )
        {
            return;
        }
        try
        {
            reg = LocateRegistry.getRegistry( port );
            reg.list();
            LOG.info( "RMI registry located: " + reg );
        }
        catch ( RemoteException e )
        {
            reg = null;
        }
        if ( reg != null )
        {
            return;
        }
        try
        {
            LOG.info( "Create RMI registry on port [" + port + "]" );
            FixedPortSocketFactory fcf = new FixedPortSocketFactory( port + 1 );
            reg = LocateRegistry.createRegistry( port, fcf, fcf );
        }
        catch ( RemoteException e )
        {
            LOG.error( "Can not create RMI registry on the server", e );
        }
    }

    public void shutdown()
    {
        try
        {
            LOG.info( "REG is shutdwon [" + UnicastRemoteObject.unexportObject( reg, false ) + "]" );
        }
        catch ( RemoteException e )
        {
            LOG.debug( e.getMessage(), e );
        }
    }

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    @Override
    public JMXServiceURL getAdminJMXServiceURL()
    {
        try
        {
            JMXServiceURL serviceURL =
                new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://" + env.getAdminHost() + ":" + port + "/admin" );
            if ( LOG.isDebugEnabled() )
            {
                LOG.info( "admin service url [" + serviceURL.toString() + "]" );
            }
            return serviceURL;
        }
        catch ( Exception e )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( e.getMessage(), e );
            }
            else
            {
                LOG.error( e.getMessage() );
            }
        }
        return null;
    }

    @Override
    public JMXServiceURL getLocalJMXServiceURL()
    {
        try
        {
            JMXServiceURL serviceURL =
                new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://" + server.getConfig().getIP() + ":" + port + "/"
                    + env.getServerName() );
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( "local jmx service url is [" + serviceURL.toString() + "]" );
            }
            return serviceURL;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
        return null;
    }

    @Override
    public JMXServiceURL getJMXServiceURLByServerName( String serverName )
    {
        try
        {
            ClusterAdminMBean clusterManager = MBeanHelper.getAdminMBean( ClusterAdminMBean.class, "cluster-manager" );
            ServerInfo target = clusterManager.getServerInfoByName( serverName );
            if ( target == null )
            {
                throw new RuntimeException( "server not exists " + serverName );
            }
            String targetHost = target.getIP();

            JMXServiceURL serviceURL =
                new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://" + targetHost + ":" + port + "/" + serverName );
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( "server url " + serverName + " is [" + serviceURL.toString() + "]" );
            }
            return serviceURL;
        }
        catch ( Exception e )
        {
            LOG.error( "get serviceURL for server [" + serverName + "] err", e );
            throw new RuntimeException( "build jmx service url for [ " + serverName + "]", e );
        }
    }
}