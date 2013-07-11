/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.ext.xslt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author ShangjieZhou
 */
public class Environment
{
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMddHHmmss" );

    public static String getSystemTime()
    {
        Calendar calendar = new GregorianCalendar();
        return dateFormat.format( calendar.getTime() );
    }

}
