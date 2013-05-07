package com.sanxing.sesame.jmx.mbean;

import com.sanxing.sesame.core.Platform;

public class PlatformManager
    implements PlatformManagerMBean
{
    @Override
    public void shutdown()
    {
        Platform.shutdown();
    }

    @Override
    public String getArch()
    {
        return System.getProperty( "os.arch" );
    }

    @Override
    public String getOperatingSystem()
    {
        return System.getProperty( "os.name" ) + " Version " + System.getProperty( "os.version" );
    }

    @Override
    public String getJVM()
    {
        return System.getProperty( "java.vendor" ) + " " + System.getProperty( "java.version" );
    }
}