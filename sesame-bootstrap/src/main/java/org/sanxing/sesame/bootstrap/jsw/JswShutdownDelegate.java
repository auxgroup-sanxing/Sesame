/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.bootstrap.jsw;

import org.sanxing.sesame.bootstrap.ShutdownHelper.ShutdownDelegate;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 * JSW {@link ShutdownDelegate}.
 */
public class JswShutdownDelegate
    implements ShutdownDelegate
{
    @Override
    public void doExit(final int code) {
        WrapperManager.stop(code);
    }

    @Override
    public void doHalt(final int code) {
        WrapperManager.stopImmediate(code);
    }
}