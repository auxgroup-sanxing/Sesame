package com.sanxing.sesame.runtime;

public interface ContainerLifecycle
{
    public abstract void onStartup();

    public abstract void onShutdown();
}