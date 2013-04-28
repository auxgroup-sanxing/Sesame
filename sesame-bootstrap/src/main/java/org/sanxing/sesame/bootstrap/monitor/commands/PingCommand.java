/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.bootstrap.monitor.commands;

import org.sanxing.sesame.bootstrap.monitor.CommandMonitorThread;
import org.sanxing.sesame.bootstrap.log.LogProxy;

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
