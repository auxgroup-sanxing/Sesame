package com.sanxing.sesame.logging.impl;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogRecord;

public class SesameLogger
    implements Log, Serializable
{
    private static final long serialVersionUID = 1888531288057908895L;

    private transient Logger logger = null; //TODO: log appender

    private String name = null;

    public SesameLogger( String name )
    {
        this.name = name;
        logger = getLogger();
    }

    public Logger getLogger()
    {
        if ( logger == null )
        {
            logger = LoggerFactory.getLogger( name );
        }

        return logger;
    }

    @Override
    public void debug( Object message )
    {
        getLogger().debug( "" + message );
    }

    @Override
    public void debug( Object message, LogRecord lr )
    {
        getLogger().debug( message + "\n" + lr );
    }

    @Override
    public void error( Object message )
    {
        getLogger().error( "" + message );
    }

    @Override
    public void error( Object message, LogRecord lr )
    {
        getLogger().error( message + "\n" + lr );
    }

    @Override
    public void fatal( Object message )
    {
        getLogger().error( "" + message );
    }

    @Override
    public void fatal( Object message, LogRecord lr )
    {
        getLogger().error( message + "\n" + lr );
    }

    @Override
    public void info( Object message )
    {
        getLogger().info( "" + message );
    }

    @Override
    public void info( Object message, LogRecord lr )
    {
        getLogger().info( message + "\n" + lr );
    }

    @Override
    public void trace( Object message )
    {
        getLogger().trace( "" + message );
    }

    @Override
    public void trace( Object message, LogRecord lr )
    {
        getLogger().trace( message + "\n" + lr );
    }

    @Override
    public void warn( Object message )
    {
        getLogger().warn( "" + message );
    }

    @Override
    public void warn( Object message, LogRecord lr )
    {
        getLogger().warn( message + "\n" + lr );
    }

    @Override
    public boolean isDebugEnabled()
    {
        return getLogger().isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled()
    {
        return getLogger().isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled()
    {
        return getLogger().isTraceEnabled();
    }
}