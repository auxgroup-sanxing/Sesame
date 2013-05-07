package com.sanxing.sesame.jmx.mbean;

public abstract interface PlatformManagerMBean
{
    public abstract void shutdown();

    public abstract String getArch();

    public abstract String getOperatingSystem();

    public abstract String getJVM();
}