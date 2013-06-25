/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.jdbc.data;

/**
 * @author ShangjieZhou
 */
public final class ByteHolder
{
    public byte value;

    public ByteHolder()
    {
    }

    public ByteHolder( byte value )
    {
        this.value = value;
    }
    
    public byte getValue()
    {
        return value;
    }
    
    public void setValue(byte value)
    {
        this.value = value;
    }
}
