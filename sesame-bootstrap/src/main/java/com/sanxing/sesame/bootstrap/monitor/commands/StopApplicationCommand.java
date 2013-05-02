/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap.monitor.commands;

import com.sanxing.sesame.bootstrap.log.LogProxy;
import com.sanxing.sesame.bootstrap.monitor.CommandMonitorThread;

/**
 * Stop launcher.
 */
public class StopApplicationCommand
    implements CommandMonitorThread.Command
{

    private static final LogProxy log = LogProxy.getLogger( StopApplicationCommand.class );

    public static final String NAME = "STOP";

    private final Runnable shutdown;

    public StopApplicationCommand( final Runnable shutdown )
    {
        if ( shutdown == null )
        {
            throw new NullPointerException();
        }
        this.shutdown = shutdown;
    }

    @Override
    public String getId()
    {
        return NAME;
    }

    @Override
    public boolean execute()
    {
        log.debug( "Requesting application stop" );
        shutdown.run();

        // Do not terminate the monitor on application stop, leave that to the jvm death
        return false;
    }

}
