package com.sanxing.sesame.jmx.mbean.admin;

import java.io.Serializable;

import org.jdom.Element;

public class ContainerInfo
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name;

    private String containerClazz;

    private Element cotnainerParams;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getContainerClazz()
    {
        return containerClazz;
    }

    public void setContainerClazz( String containerClazz )
    {
        this.containerClazz = containerClazz;
    }

    public Element getCotnainerParams()
    {
        return cotnainerParams;
    }

    public void setCotnainerParams( Element cotnainerParams )
    {
        this.cotnainerParams = cotnainerParams;
    }
}