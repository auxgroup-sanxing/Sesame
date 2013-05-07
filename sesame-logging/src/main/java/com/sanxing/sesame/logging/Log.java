package com.sanxing.sesame.logging;

public abstract interface Log
{
    public abstract boolean isDebugEnabled();

    public abstract boolean isInfoEnabled();

    public abstract boolean isTraceEnabled();

    public abstract void trace( Object paramObject );

    public abstract void trace( Object paramObject, LogRecord paramLogRecord );

    public abstract void debug( Object paramObject );

    public abstract void debug( Object paramObject, LogRecord paramLogRecord );

    public abstract void info( Object paramObject );

    public abstract void info( Object paramObject, LogRecord paramLogRecord );

    public abstract void warn( Object paramObject );

    public abstract void warn( Object paramObject, LogRecord paramLogRecord );

    public abstract void error( Object paramObject );

    public abstract void error( Object paramObject, LogRecord paramLogRecord );

    public abstract void fatal( Object paramObject );

    public abstract void fatal( Object paramObject, LogRecord paramLogRecord );
}