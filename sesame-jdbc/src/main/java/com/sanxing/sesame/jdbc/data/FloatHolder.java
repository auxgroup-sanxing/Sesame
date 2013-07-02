/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.jdbc.data;

/**
 * @author ShangjieZhou
 */
public final class FloatHolder
{
    public float value;

    public FloatHolder()
    {
    }

    public FloatHolder( float value )
    {
        this.value = value;
    }
    
    public float getValue()
    {
        return value;
    }
    
    public void setValue(float value)
    {
        this.value = value;
    }
}
