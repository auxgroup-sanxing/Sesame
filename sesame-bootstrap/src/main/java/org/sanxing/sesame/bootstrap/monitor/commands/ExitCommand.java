/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.bootstrap.monitor.commands;

import org.sanxing.sesame.bootstrap.ShutdownHelper;
import org.sanxing.sesame.bootstrap.monitor.CommandMonitorThread;

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
        ShutdownHelper.exit(666);

        throw new Error("Unreachable statement");
    }

}
