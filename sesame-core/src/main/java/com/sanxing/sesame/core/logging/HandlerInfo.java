package com.sanxing.sesame.core.logging;

import java.io.Serializable;

public class HandlerInfo
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name;

    private String handlerClazz;

    public String getHandlerClazz()
    {
        return handlerClazz;
    }

    public void setHandlerClazz( String handlerClazz )
    {
        this.handlerClazz = handlerClazz;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}