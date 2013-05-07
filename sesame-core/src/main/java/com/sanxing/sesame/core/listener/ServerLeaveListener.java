package com.sanxing.sesame.core.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.AdminServer;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.event.ServerLeaveEvent;
import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;

public class ServerLeaveListener
    implements ClusterListener
{
    private static Logger LOG = LoggerFactory.getLogger( ServerLeaveListener.class );

    private BaseServer server;

    @Override
    public void setServer( BaseServer server )
    {
        this.server = server;
    }

    @Override
    public void listen( ClusterEvent event )
    {
        if ( event instanceof ServerLeaveEvent )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Server [" + event.getEventSource() + "] leaved" );
            }
            if ( server.isAdmin() )
            {
                String closedServer = event.getEventSource();
                ( (AdminServer) server ).closeJMXConnector( closedServer );
            }
        }
    }
}