/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap.jsw;

import org.slf4j.Logger;
import com.sanxing.sesame.bootstrap.Launcher;
import com.sanxing.sesame.bootstrap.ShutdownHelper;
import org.tanukisoftware.wrapper.WrapperManager;

import static org.tanukisoftware.wrapper.WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT;

/**
 * JSW adapter for {@link Launcher}.
 */
public class JswLauncher
    extends WrapperListenerSupport
{
    private final Launcher launcher;

    public JswLauncher() {
        this.launcher = new Launcher()
        {
            @Override
            protected Logger createLogger() {
                return JswLauncher.this.log;
            }

            @Override
            public void commandStop() {
                WrapperManager.stopAndReturn(0);
            }

        };
    }

    @Override
    protected Integer doStart(final String[] args) throws Exception {
        if (WrapperManager.isControlledByNativeWrapper()) {
            log.info("JVM ID: {}, JVM PID: {}, Wrapper PID: {}, User: {}",
                     WrapperManager.getJVMId(), WrapperManager.getJavaPID(), WrapperManager.getWrapperPID(),
                     WrapperManager.getUser(false).getUser() );
        }

        return launcher.start(args);
    }

    @Override
    protected int doStop(final int code) throws Exception {
        launcher.stop();
        return code;
    }

    @Override
    protected void doControlEvent(final int code) {
        if (WRAPPER_CTRL_LOGOFF_EVENT == code && WrapperManager.isLaunchedAsService()) {
            log.debug("Launched as a service; ignoring event: {}", code);
        }
        else {
            log.debug("Stopping");
            WrapperManager.stop(0);
            throw new Error("unreachable");
        }
    }

    public static void main(final String[] args) throws Exception {
        ShutdownHelper.setDelegate(new JswShutdownDelegate());
        WrapperManager.start(new JswLauncher(), args);
    }
}
