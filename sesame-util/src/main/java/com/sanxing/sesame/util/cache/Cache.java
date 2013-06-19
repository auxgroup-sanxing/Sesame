package com.sanxing.sesame.util.cache;

public interface Cache
{
    public abstract Object get( String key )
        throws NoSuchObjectException;

    public abstract void flush( String key );

    public abstract void flushAll();

    public abstract void put( String key, Object obj );

    public abstract PeristenceBackend getBackend();
}