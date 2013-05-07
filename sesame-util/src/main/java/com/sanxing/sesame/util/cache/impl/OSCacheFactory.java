package com.sanxing.sesame.util.cache.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sanxing.sesame.util.cache.Cache;
import com.sanxing.sesame.util.cache.CacheFactory;
import com.sanxing.sesame.util.cache.PeristenceBackend;

public class OSCacheFactory
    implements CacheFactory
{
    private final Map<String, OSCache> caches = new ConcurrentHashMap();

    @Override
    public void addBackend( PeristenceBackend back )
    {
        OSCache cache = new OSCache( back, back.getName() );
        caches.put( back.getName(), cache );
    }

    @Override
    public Cache getCache( String cacheName )
    {
        return caches.get( cacheName );
    }
}