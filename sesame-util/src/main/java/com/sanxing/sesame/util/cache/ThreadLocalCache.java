package com.sanxing.sesame.util.cache;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalCache
{
    static ThreadLocal<Map> context = new ThreadLocal();

    public static void put( Object key, Object value )
    {
        getMap().put( key, value );
    }

    private static Map getMap()
    {
        if ( context.get() == null )
        {
            context.set( new HashMap() );
        }
        return context.get();
    }

    public static Object get( Object key )
    {
        return getMap().get( key );
    }

    public static void clear()
    {
        getMap().clear();
    }
}