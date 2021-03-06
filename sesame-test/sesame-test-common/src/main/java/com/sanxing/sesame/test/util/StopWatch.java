/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.test.util;

public class StopWatch
{
    private static final int MS_PER_SEC = 1000;

    private static final int SEC_PER_MIN = 60;

    private long start;

    public StopWatch()
    {
        reset();
    }

    public void reset()
    {
        start = System.currentTimeMillis();
    }

    public long elapsedTime()
    {
        return System.currentTimeMillis() - start;
    }

    public String formattedTime()
    {
        return formatTime( elapsedTime() );
    }

    public static String formatTime( long ms )
    {
        long secs = ms / MS_PER_SEC;

        long min = secs / SEC_PER_MIN;

        secs = secs % SEC_PER_MIN;

        String msg = "";

        if ( min > 1 )
        {
            msg = min + " minutes ";
        }
        else if ( min == 1 )
        {
            msg = "1 minute ";
        }

        if ( secs > 1 )
        {
            msg += secs + " seconds";
        }
        else if ( secs == 1 )
        {
            msg += "1 second";
        }
        else if ( min == 0 )
        {
            msg += "< 1 second";
        }
        return msg;
    }

    @Override
    public String toString()
    {
        return formattedTime();
    }

}
