package com.sanxing.sesame.util.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachableRepos
{
    Logger LOG = LoggerFactory.getLogger( CachableRepos.class );

    private final String name;

    private Cache cache = null;

    private static Map<String, CachableRepos> instance = new ConcurrentHashMap();

    public static CachableRepos getInstanceByName( String name )
    {
        if ( instance.get( name ) == null )
        {
            instance.put( name, new CachableRepos( name ) );
        }
        return instance.get( name );
    }

    private CachableRepos( String name )
    {
        this.name = name;
        cache = DefaultCacheFactory.getInstance().getCache( this.name );
        if ( cache == null )
        {
            throw new RuntimeException( "no such cache named [" + name + "]" );
        }
    }

    public Object getByKey( String key )
    {
        return cache.get( key );
    }

    public void delete( String key )
    {
        cache.flush( key );
        cache.getBackend().deleteFromPeristence( key );
    }

    public void update( String key, Object obj )
    {
        cache.flush( key );
        cache.getBackend().saveOrUpdateToPersistence( key, obj );
    }

    public String add( Object obj )
    {
        String key = cache.getBackend().addToPersistence( obj );
        if ( key != null )
        {
            cache.put( key, obj );
            return key;
        }
        return null;
    }
}