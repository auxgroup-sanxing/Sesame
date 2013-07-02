/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.jdbc.data;

import java.util.Date;

/**
 * @author ShangjieZhou
 */
public final class DateHolder
{
    public Date value;

    public DateHolder()
    {
    }

    public DateHolder( Date value )
    {
        this.value = value;
    }
    
    public Date getValue()
    {
        return value;
    }
    
    public void setValue(Date value)
    {
        this.value = value;
    }
}
