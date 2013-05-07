package com.sanxing.sesame.jmx.mbean.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.AdminServer;
import com.sanxing.sesame.core.ManagedServer;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.core.event.ServerJoinEvent;
import com.sanxing.sesame.core.event.ServerLeaveEvent;
import com.sanxing.sesame.jmx.mbean.ServerManagerMBean;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomDriver;

public class ClusterAdmin
    implements ClusterAdminMBean
{
    private static final Logger LOG = LoggerFactory.getLogger( ClusterAdmin.class );

    private final Map<String, ServerInfo> managedServers = new HashMap();

    XStream xstream = new XStream( new JDomDriver() );

    private final AdminServer server;

    public ClusterAdmin( AdminServer _server )
    {
        server = _server;
        checkConfigs();
        addServer( _server.getConfig() );
        new Timer().schedule( new HearbeatChecker(), 0L, ManagedServer.HEART_BEAT_INTERVAL );
    }

    private void checkConfigs()
    {
        if ( !( managedServers.isEmpty() ) )
        {
            return;
        }
        try
        {
            File file = new File( Platform.getEnv().getHomeDir(), "conf/cluster.xml" );
            Document document = null;
            if ( file.exists() )
            {
                document = new SAXBuilder().build( file );
            }
            else
            {
                InputStream input = ServerInfo.class.getClassLoader().getResourceAsStream( "cluster.xml" );
                document = new SAXBuilder().build( input );
                XMLOutputter output = new XMLOutputter();
                output.setFormat( Format.getPrettyFormat() );
                output.output( document, new FileOutputStream( file ) );
            }
            List serverElements = document.getRootElement().getChildren( "server" );
            for ( int i = 0; i < serverElements.size(); ++i )
            {
                Element serverElement = (Element) serverElements.get( i );
                ServerInfo serverInfo = ServerInfo.fromElement( serverElement );
                managedServers.put( serverInfo.getServerName(), serverInfo );
            }
            LOG.info( "cluster.xml loaded" );
        }
        catch ( Exception e )
        {
            LOG.error( "please check your cluster.xml" );
        }
    }

    @Override
    public void addServer( ServerInfo server )
    {
        managedServers.put( server.getServerName(), server );
        persistenceManagedServers();
    }

    @Override
    public void removeServer( ServerInfo server )
    {
        if ( server.getServerState() != 0 )
        {
            throw new RuntimeException( "server [" + server.getServerName() + "] is running ,please shutdown first" );
        }
        managedServers.remove( server.getServerName() );
        persistenceManagedServers();
    }

    private void persistenceManagedServers()
    {
    }

    @Override
    public void upateServer( ServerInfo server )
    {
        if ( !( managedServers.containsKey( server.getServerName() ) ) )
        {
            throw new RuntimeException( "server [" + server.getServerName() + "] not defined in cluster" );
        }
        managedServers.put( server.getServerName(), server );
        ClusterEvent event = null;
        if ( server.getServerState() == 2 )
        {
            event = new ServerJoinEvent();
            event.setEventSource( server.getServerName() );
        }
        else if ( server.getServerState() == 0 )
        {
            event = new ServerLeaveEvent();
            event.setEventSource( server.getServerName() );
        }
        notifyNeighbors( event );
    }

    @Override
    public void notifyNeighbors( ClusterEvent event )
    {
        Iterator iterNames = managedServers.keySet().iterator();
        while ( iterNames.hasNext() )
        {
            String serverName = (String) iterNames.next();
            ServerInfo server = managedServers.get( serverName );
            if ( ( server.getServerName().equals( event.getEventSource() ) ) || ( server.getServerState() != 2 ) )
            {
                continue;
            }
            fireEvent( event, server.getServerName() );
        }
    }

    @Override
    public void fireEvent( ClusterEvent event, String serverName )
    {
        try
        {
            LOG.info( "notify neighbor [" + serverName + "] with event [" + event + "]" );
            ServerManagerMBean sm =
                MBeanHelper.getManagedMBean( ServerManagerMBean.class,
                    MBeanHelper.getMBeanNameOnServer( "server-manager", serverName ) );
            sm.listen( event );
        }
        catch ( Exception e )
        {
            LOG.error( "Fire event on server [" + serverName + "] error", e );
        }
    }

    @Override
    public List<ServerInfo> getAllServer()
    {
        List result = new LinkedList();
        Iterator iter = managedServers.keySet().iterator();
        while ( iter.hasNext() )
        {
            String serverName = (String) iter.next();
            result.add( managedServers.get( serverName ) );
        }
        return result;
    }

    @Override
    public ServerInfo getServerInfoByName( String name )
    {
        return managedServers.get( name );
    }

    @Override
    public List<ServerInfo> heartBeat( String serverName )
    {
        LOG.info( "heart beat from server [" + serverName + "]" + new Date() );
        List result = new LinkedList();
        Iterator serverNames = managedServers.keySet().iterator();
        while ( serverNames.hasNext() )
        {
            String name = (String) serverNames.next();
            if ( !( serverName.equals( name ) ) )
            {
                result.add( managedServers.get( name ) );
            }
        }
        return result;
    }

    @Override
    public synchronized ServerInfo updateState( String serverName, int status )
    {
        ServerInfo server = getServerInfoByName( serverName );
        server.setServerState( status );
        LOG.info( "updage server status [" + server + "]" );
        upateServer( server );
        return getServerInfoByName( "admin" );
    }

    class HearbeatChecker
        extends TimerTask
    {
        @Override
        public void run()
        {
            Iterator iter = managedServers.keySet().iterator();
            while ( iter.hasNext() )
            {
                String name = (String) iter.next();
                ServerInfo server = managedServers.get( name );
                if ( ( !( server.isAdmin() ) )
                    && ( System.currentTimeMillis() - server.getLastHearBeatTimestamp() > ManagedServer.HEART_BEAT_INTERVAL * 3L ) )
                {
                    ServerLeaveEvent event = new ServerLeaveEvent();
                    event.setEventObject( name );
                    event.setEventSource( "admin" );
                    notifyNeighbors( event );
                }
            }
        }
    }
}