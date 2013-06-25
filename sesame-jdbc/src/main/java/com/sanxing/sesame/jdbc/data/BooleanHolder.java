/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.jdbc.data;

/**
 * @author ShangjieZhou
 */
public final class BooleanHolder
{
    public boolean value;

    public BooleanHolder()
    {
    }

    public BooleanHolder( boolean value )
    {
        this.value = value;
    }
    
    public boolean getValue()
    {
        return value;
    }
    
    public void setValue(boolean value)
    {
        this.value = value;
    }
}
