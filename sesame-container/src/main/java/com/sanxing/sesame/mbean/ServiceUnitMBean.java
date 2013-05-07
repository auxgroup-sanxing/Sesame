package com.sanxing.sesame.mbean;

public abstract interface ServiceUnitMBean
{
    public static final String STARTED = "Started";

    public static final String SHUTDOWN = "Shutdown";

    public static final String STOPPED = "Stopped";

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getComponentName();

    public abstract String getCurrentState();

    public abstract String getServiceAssembly();

    public abstract String getDescriptor();
}