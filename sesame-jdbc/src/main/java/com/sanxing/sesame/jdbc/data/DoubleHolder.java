/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.jdbc.data;

/**
 * @author ShangjieZhou
 */
public final class DoubleHolder
{
    public double value;

    public DoubleHolder()
    {
    }

    public DoubleHolder( double value )
    {
        this.value = value;
    }
    
    public double getValue()
    {
        return value;
    }
    
    public void setValue(double value)
    {
        this.value = value;
    }
}
