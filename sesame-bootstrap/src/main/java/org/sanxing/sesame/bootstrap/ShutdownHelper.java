/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.bootstrap;

/**
 * Helper to cope with different mechanisms to shutdown.
 */
public class ShutdownHelper
{
    public static interface ShutdownDelegate
    {
        void doExit(int code);

        void doHalt(int code);
    }

    public static class JavaShutdownDelegate
        implements ShutdownDelegate
    {
        @Override
        public void doExit(final int code) {
            System.exit(code);
        }

        @Override
        public void doHalt(final int code) {
            Runtime.getRuntime().halt(code);
        }
    }

    private static ShutdownDelegate delegate = new JavaShutdownDelegate();

    public static ShutdownDelegate getDelegate() {
        if (delegate == null) {
            throw new IllegalStateException();
        }
        return delegate;
    }

    public static void setDelegate(final ShutdownDelegate delegate) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        ShutdownHelper.delegate = delegate;
    }

    public static void exit(final int code) {
        getDelegate().doExit(code);
    }

    public static void halt(final int code) {
        getDelegate().doHalt(code);
    }
}
