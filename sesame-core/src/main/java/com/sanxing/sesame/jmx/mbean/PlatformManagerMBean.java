package com.sanxing.sesame.jmx.mbean;

public interface PlatformManagerMBean
{
    public abstract void shutdown();

    public abstract String getArch();

    public abstract String getOperatingSystem();

    public abstract String getJVM();
}