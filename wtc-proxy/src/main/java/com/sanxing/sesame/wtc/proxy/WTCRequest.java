/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.wtc.proxy;

/**
 * @author ShangjieZhou
 */
public class WTCRequest
{
    private String id;

    private byte[] content;

    /**
     * id
     * 
     * @return id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id
     */
    public void setId( String id )
    {
        this.id = id;
    }

    /**
     * content
     * 
     * @return content
     */
    public byte[] getContent()
    {
        return content;
    }

    /**
     * @param content
     */
    public void setContent( byte[] content )
    {
        this.content = content;
    }

}
