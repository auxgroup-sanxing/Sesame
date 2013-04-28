/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.bootstrap.monitor.commands;

import org.sanxing.sesame.bootstrap.monitor.CommandMonitorThread;
import org.sanxing.sesame.bootstrap.log.LogProxy;

/**
 * Stops command monitor.
 */
public class StopMonitorCommand
    implements CommandMonitorThread.Command
{

    private static final LogProxy log = LogProxy.getLogger( StopMonitorCommand.class );

    public static final String NAME = "STOP_MONITOR";

    @Override
    public String getId()
    {
        return NAME;
    }

    @Override
    public boolean execute()
    {
        log.debug( "Requesting monitor stop" );
        return true;
    }

}
