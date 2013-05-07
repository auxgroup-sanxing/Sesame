/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap.jsw;

import org.tanukisoftware.wrapper.WrapperManager;

import com.sanxing.sesame.bootstrap.ShutdownHelper.ShutdownDelegate;

/**
 * JSW {@link ShutdownDelegate}.
 */
public class JswShutdownDelegate
    implements ShutdownDelegate
{
    @Override
    public void doExit( final int code )
    {
        WrapperManager.stop( code );
    }

    @Override
    public void doHalt( final int code )
    {
        WrapperManager.stopImmediate( code );
    }
}