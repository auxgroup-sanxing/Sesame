package com.sanxing.sesame.util.cache;

import com.sanxing.sesame.util.cache.impl.OSCacheFactory;

public class DefaultCacheFactory
{
    private static CacheFactory instance;

    public static CacheFactory getInstance()
    {
        if ( instance == null )
        {
            instance = new OSCacheFactory();
        }
        return instance;
    }
}