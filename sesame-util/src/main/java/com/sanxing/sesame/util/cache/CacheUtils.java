package com.sanxing.sesame.util.cache;

public abstract class CacheUtils
{
    public static void registerBackend( PeristenceBackend back )
    {
        DefaultCacheFactory.getInstance().addBackend( back );
    }
}