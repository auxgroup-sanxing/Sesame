package com.sanxing.sesame.util.cache;

public interface PeristenceBackend
{
    public abstract String getName();

    public abstract Object getFromPersitence( String key );

    public abstract void saveOrUpdateToPersistence( String key, Object obj );

    public abstract String addToPersistence( Object obj );

    public abstract void deleteFromPeristence( String key );
}