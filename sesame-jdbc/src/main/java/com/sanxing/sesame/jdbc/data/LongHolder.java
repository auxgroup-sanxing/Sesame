/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.jdbc.data;

/**
 * @author ShangjieZhou
 */
public final class LongHolder
{
    public long value;

    public LongHolder()
    {
    }

    public LongHolder( long value )
    {
        this.value = value;
    }
    
    public long getValue()
    {
        return value;
    }
    
    public void setValue(long value)
    {
        this.value = value;
    }
}
