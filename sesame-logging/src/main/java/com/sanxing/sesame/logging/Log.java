package com.sanxing.sesame.logging;

public interface Log
{
    public abstract boolean isDebugEnabled();

    public abstract boolean isInfoEnabled();

    public abstract boolean isTraceEnabled();

    public abstract void trace( Object message );

    public abstract void trace( Object message, LogRecord lr );

    public abstract void debug( Object message );

    public abstract void debug( Object message, LogRecord lr );

    public abstract void info( Object message );

    public abstract void info( Object message, LogRecord lr );

    public abstract void warn( Object message );

    public abstract void warn( Object message, LogRecord lr );

    public abstract void error( Object message );

    public abstract void error( Object message, LogRecord lr );

    public abstract void fatal( Object message );

    public abstract void fatal( Object message, LogRecord lr );
}