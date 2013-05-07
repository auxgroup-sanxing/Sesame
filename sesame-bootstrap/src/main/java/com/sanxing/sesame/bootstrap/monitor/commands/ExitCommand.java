/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap.monitor.commands;

import com.sanxing.sesame.bootstrap.ShutdownHelper;
import com.sanxing.sesame.bootstrap.monitor.CommandMonitorThread;

/**
 * Command to exit the JVM (via {@link ShutdownHelper#exit(int)}).
 */
public class ExitCommand
    implements CommandMonitorThread.Command
{

    public static final String NAME = "EXIT";

    @Override
    public String getId()
    {
        return NAME;
    }

    @Override
    public boolean execute()
    {
        ShutdownHelper.exit( 666 );

        throw new Error( "Unreachable statement" );
    }

}
