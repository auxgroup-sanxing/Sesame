package com.sanxing.sesame.jmx.mbean.admin;

import javax.management.ObjectName;

public interface LinkCreatorMBean
{
    public abstract void register( ObjectName name, String serverName );

    public abstract void unregister( ObjectName name, String serverName );
}