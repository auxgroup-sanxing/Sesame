/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logs to System.out.
 */
public class SystemOutLogProxy
    extends LogProxy
{

    private Class clazz;

    public SystemOutLogProxy( final Class clazz )
    {
        this.clazz = clazz;
    }

    @Override
    public void debug( final String message, Object... args )
    {
        message( "DEBUG", message, args );
    }

    @Override
    public void info( final String message, final Object... args )
    {
        message( "INFO", message, args );
    }

    @Override
    public void error( final String message, final Throwable e )
    {
        error( message );
        e.printStackTrace( System.out );
    }

    @Override
    public void error( final String message, Object... args )
    {
        message( "ERROR", message, args );
    }

    @Override
    public void warn( final String message, Object... args )
    {
        message( "WARN", message, args );
    }

    private void message( final String level, final String message, Object... args )
    {
        final String timestamp = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( new Date() );
        System.out.println(
            timestamp + " [" + level + "] " + clazz.getSimpleName()
                + " - " + String.format( message.replace( "{}", "%s" ), args )
        );
    }

}
