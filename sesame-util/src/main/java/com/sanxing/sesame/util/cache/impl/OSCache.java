package com.sanxing.sesame.util.cache.impl;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import com.sanxing.sesame.util.cache.Cache;
import com.sanxing.sesame.util.cache.NoSuchObjectException;
import com.sanxing.sesame.util.cache.PeristenceBackend;

public class OSCache
    implements Cache
{
    private final PeristenceBackend back;

    private final String group;

    static GeneralCacheAdministrator cache = new GeneralCacheAdministrator();

    OSCache( PeristenceBackend back, String name )
    {
        this.back = back;
        group = name;
    }

    @Override
    public PeristenceBackend getBackend()
    {
        return back;
    }

    @Override
    public Object get( String key )
        throws NoSuchObjectException
    {
        Object obj = null;
        try
        {
            obj = cache.getFromCache( group + "_" + key );
        }
        catch ( NeedsRefreshException e )
        {
            boolean updated = false;
            try
            {
                obj = back.getFromPersitence( key );
                cache.putInCache( group + "_" + key, obj, new String[] { group } );
                updated = true;
            }
            catch ( Exception ee )
            {
                ee.printStackTrace();
                obj = e.getCacheContent();
                if ( !( updated ) )
                {
                    cache.cancelUpdate( group + "_" + key );
                }
            }
        }
        if ( obj == null )
        {
            throw new NoSuchObjectException( key );
        }
        return obj;
    }

    @Override
    public void flushAll()
    {
        cache.flushGroup( group );
    }

    @Override
    public void put( String key, Object obj )
    {
        cache.putInCache( group + "_" + key, obj, new String[] { group } );
    }

    @Override
    public void flush( String key )
    {
        cache.flushEntry( group + "_" + key );
    }
}