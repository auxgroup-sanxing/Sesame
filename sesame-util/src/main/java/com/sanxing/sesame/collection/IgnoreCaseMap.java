package com.sanxing.sesame.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IgnoreCaseMap<V>
    implements Map<String, V>
{
    private Map<String, V> delegate = null;

    public IgnoreCaseMap()
    {
        this.delegate = new HashMap();
    }

    public IgnoreCaseMap( int initialCapacity )
    {
        this.delegate = new HashMap( initialCapacity );
    }

    public IgnoreCaseMap( int initialCapacity, float loadFactor )
    {
        this.delegate = new HashMap( initialCapacity, loadFactor );
    }

    private String convertKey( String key )
    {
        return key.toUpperCase();
    }

    @Override
    public void clear()
    {
        this.delegate.clear();
    }

    @Override
    public boolean containsKey( Object key )
    {
        return this.delegate.containsKey( convertKey( key.toString() ) );
    }

    @Override
    public boolean containsValue( Object value )
    {
        return this.delegate.containsValue( value );
    }

    @Override
    public Set<Map.Entry<String, V>> entrySet()
    {
        return this.delegate.entrySet();
    }

    @Override
    public boolean equals( Object o )
    {
        return this.delegate.equals( o );
    }

    @Override
    public V get( Object key )
    {
        return this.delegate.get( convertKey( key.toString() ) );
    }

    @Override
    public int hashCode()
    {
        return this.delegate.hashCode();
    }

    @Override
    public boolean isEmpty()
    {
        return this.delegate.isEmpty();
    }

    @Override
    public Set<String> keySet()
    {
        return this.delegate.keySet();
    }

    @Override
    public V put( String key, V value )
    {
        return this.delegate.put( convertKey( key.toString() ), value );
    }

    @Override
    public void putAll( Map<? extends String, ? extends V> map )
    {
        for ( Object element : map.entrySet() )
        {
            Map.Entry e = (Map.Entry) element;
            put( (String) e.getKey(), (V) e.getValue() );
        }
    }

    @Override
    public V remove( Object key )
    {
        return this.delegate.remove( convertKey( key.toString() ) );
    }

    @Override
    public int size()
    {
        return this.delegate.size();
    }

    @Override
    public Collection<V> values()
    {
        return this.delegate.values();
    }
}