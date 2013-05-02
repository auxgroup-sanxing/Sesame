/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs to SLF4J.
 */
public class Slf4jLogProxy
    extends LogProxy
{

    private Logger log = LoggerFactory.getLogger( this.getClass() );

    public Slf4jLogProxy( final Logger log )
    {
        this.log = log;
    }

    public Slf4jLogProxy( final Class clazz )
    {
        this( LoggerFactory.getLogger( clazz ) );
    }

    @Override
    public void debug( final String message, Object... args )
    {
        log.debug( message, args );
    }

    @Override
    public void info( final String message, final Object... args )
    {
        log.info( message, args );
    }

    @Override
    public void error( final String message, Object... args )
    {
        log.error( message, args );
    }

    @Override
    public void error( final String message, Throwable e )
    {
        log.error( message, e );
    }

    @Override
    public void warn( final String message, Object... args )
    {
        log.warn( message, args );
    }

}
