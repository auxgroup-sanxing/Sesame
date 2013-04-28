/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.bootstrap.monitor;

import org.junit.Test;
import org.sanxing.sesame.bootstrap.monitor.commands.PingCommand;
import org.sanxing.sesame.bootstrap.monitor.commands.StopMonitorCommand;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sanxing.sesame.bootstrap.monitor.CommandMonitorThread.LOCALHOST;

/**
 * Test for {@link KeepAliveThreadTest}.
 */
public class KeepAliveThreadTest
    extends TestSupport
{
    @Test
    public void keepAlive()
        throws Exception
    {
        CommandMonitorThread keepAliveThread = new CommandMonitorThread(
            0,
            new PingCommand(),
            new StopMonitorCommand()
        );
        keepAliveThread.start();

        final AtomicBoolean shutDown = new AtomicBoolean(false);

        KeepAliveThread thread = new KeepAliveThread(
            LOCALHOST,
            keepAliveThread.getPort(),
            100,
            1000,
            new Runnable()
            {
                @Override
                public void run() {
                    shutDown.set(true);
                }
            }
        );
        thread.start();

        Thread.sleep(2000);

        new CommandMonitorTalker(LOCALHOST, keepAliveThread.getPort()).send(StopMonitorCommand.NAME);
        keepAliveThread.join();

        thread.interrupt();
        thread.stopRunning();
        thread.join();

        assertThat(shutDown.get(), is(true));
    }
}
