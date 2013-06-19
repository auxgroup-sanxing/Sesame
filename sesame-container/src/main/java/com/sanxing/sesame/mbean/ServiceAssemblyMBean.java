package com.sanxing.sesame.mbean;

import javax.management.ObjectName;

public interface ServiceAssemblyMBean
{
    public static final String STARTED = "Started";

    public static final String SHUTDOWN = "Shutdown";

    public static final String STOPPED = "Stopped";

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getCurrentState();

    public abstract String getDescriptor();

    public abstract ObjectName[] getServiceUnits();

    public abstract String start()
        throws Exception;

    public abstract String stop()
        throws Exception;

    public abstract String shutDown()
        throws Exception;
}