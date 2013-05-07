package com.sanxing.sesame.util;

import java.net.InetAddress;

public class NetworkUtil
{
    public static String getMyIP()
    {
        try
        {
            InetAddress address = InetAddress.getLocalHost();
            return address.getHostAddress();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }
}