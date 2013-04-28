/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.bootstrap.monitor.commands;

import org.sanxing.sesame.bootstrap.ShutdownHelper;
import org.sanxing.sesame.bootstrap.monitor.CommandMonitorThread;

/**
 * Command to forcibly halt the JVM (via {@link ShutdownHelper#halt(int)}).
 */
public class HaltCommand
    implements CommandMonitorThread.Command
{

    public static final String NAME = "HALT";

    @Override
    public String getId()
    {
        return NAME;
    }

    @Override
    public boolean execute()
    {
        ShutdownHelper.halt(666);

        throw new Error("Unreachable statement");
    }

}
