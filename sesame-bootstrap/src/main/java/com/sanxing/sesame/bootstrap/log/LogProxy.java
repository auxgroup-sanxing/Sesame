/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap.log;

/**
 * A log proxy allowing redirecting output (e.g. in case that there is no slf4j available).
 */
public class LogProxy
{

    public void debug( final String message, Object... args )
    {
        // does nothing
    }

    public void info( final String message, final Object... args )
    {
        // does nothing
    }

    public void error( final String message, Object... args )
    {
        // does nothing
    }

    public void error( final String message, Throwable e )
    {
        // does nothing
    }

    public void warn( final String message, Object... args )
    {
        // does nothing
    }

    public static LogProxy getLogger( final Class clazz )
    {
        try
        {
            LogProxy.class.getClassLoader().loadClass( "org.slf4j.Logger" );
            return new Slf4jLogProxy( clazz );
        }
        catch ( ClassNotFoundException e )
        {
            return new SystemOutLogProxy( clazz );
        }
    }

}
