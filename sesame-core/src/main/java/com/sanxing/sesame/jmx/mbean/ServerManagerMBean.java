package com.sanxing.sesame.jmx.mbean;

import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;

public interface ServerManagerMBean
{
    public abstract void start();

    public abstract void stop();

    public abstract void listen( ClusterEvent event );

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getHostAddress();

    public abstract int getJmxPort();

    public abstract String getState();

    public abstract String getSystemCpu();

    public abstract String getJVMMemory();
}