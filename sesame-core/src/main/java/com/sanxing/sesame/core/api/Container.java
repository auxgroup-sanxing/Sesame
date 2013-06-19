package com.sanxing.sesame.core.api;

public interface Container
{
    public abstract void init( ContainerContext context )
        throws Exception;

    public abstract void start()
        throws Exception;

    public abstract void stop()
        throws Exception;

    public abstract void shutdown()
        throws Exception;
}