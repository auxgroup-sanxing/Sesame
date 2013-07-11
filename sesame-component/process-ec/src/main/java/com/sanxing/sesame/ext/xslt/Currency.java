/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.ext.xslt;

import java.math.BigDecimal;

/**
 * @author ShangjieZhou
 */
public class Currency
{
    public static String movePoint( double amount, int n )
    {
        return new BigDecimal( amount ).movePointRight( n ).toString();
    }
}
