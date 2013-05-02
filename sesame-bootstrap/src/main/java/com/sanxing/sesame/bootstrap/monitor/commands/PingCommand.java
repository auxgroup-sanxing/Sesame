/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap.monitor.commands;

import com.sanxing.sesame.bootstrap.monitor.CommandMonitorThread;
import com.sanxing.sesame.bootstrap.log.LogProxy;

/**
 * Responds to pings (by doing nothing).
 */
public class PingCommand
    implements CommandMonitorThread.Command
{

    private static final LogProxy log = LogProxy.getLogger( PingCommand.class );

    public static final String NAME = "PING";

    @Override
    public String getId()
    {
        return NAME;
    }

    @Override
    public boolean execute()
    {
        log.debug( "Pinged" );
        return false;
    }

}
