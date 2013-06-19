package com.sanxing.sesame.jmx;

import javax.management.remote.JMXServiceURL;

public interface JMXServiceURLBuilder
{
    public abstract JMXServiceURL getLocalJMXServiceURL();

    public abstract JMXServiceURL getAdminJMXServiceURL();

    public abstract JMXServiceURL getJMXServiceURLByServerName( String serverName );
}