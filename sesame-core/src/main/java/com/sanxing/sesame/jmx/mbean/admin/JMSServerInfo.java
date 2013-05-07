package com.sanxing.sesame.jmx.mbean.admin;

import java.io.Serializable;

public class JMSServerInfo
    implements Serializable
{
    private String name;

    private int port;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }
}