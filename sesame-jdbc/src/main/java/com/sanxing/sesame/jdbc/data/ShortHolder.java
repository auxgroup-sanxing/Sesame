/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.jdbc.data;

/**
 * @author ShangjieZhou
 */
public final class ShortHolder
{
    public short value;

    public ShortHolder()
    {
    }

    public ShortHolder( short value )
    {
        this.value = value;
    }
    
    public short getValue()
    {
        return value;
    }
    
    public void setValue(short value)
    {
        this.value = value;
    }
}
