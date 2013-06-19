package com.sanxing.sesame.executors;

public interface Callback
{
    public abstract void beforeExecute( Thread thead );

    public abstract void afterExecute( Throwable t );

    public abstract void terminated();
}