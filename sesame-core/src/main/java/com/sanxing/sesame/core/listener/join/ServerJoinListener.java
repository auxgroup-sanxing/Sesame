package com.sanxing.sesame.core.listener.join;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.listener.ClusterListener;
import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;

public class ServerJoinListener
    implements ClusterListener
{
    private static Logger logger = LoggerFactory.getLogger( ServerJoinListener.class );

    private BaseServer server;

    @Override
    public void setServer( BaseServer server )
    {
        this.server = server;
    }

    @Override
    public void listen( ClusterEvent event )
    {
    }
}