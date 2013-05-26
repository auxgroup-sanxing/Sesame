package com.sanxing.sesame.wssecurity.jaas;

import javax.security.auth.callback.Callback;

public class TimestampCallback
    implements Callback
{
    private String timestampAsString;

    public String getTimestampAsString()
    {
        return this.timestampAsString;
    }

    public void setTimestampAsString( String timestampAsString )
    {
        this.timestampAsString = timestampAsString;
    }
}