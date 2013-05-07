package com.sanxing.sesame.classloader;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ResourceEnumeration
    implements Enumeration
{
    private Iterator iterator;

    private final String resourceName;

    private Object next;

    public ResourceEnumeration( Collection resourceLocations, String resourceName )
    {
        iterator = resourceLocations.iterator();
        this.resourceName = resourceName;
    }

    @Override
    public boolean hasMoreElements()
    {
        fetchNext();
        return ( next != null );
    }

    @Override
    public Object nextElement()
    {
        fetchNext();

        Object next = this.next;
        this.next = null;

        if ( next == null )
        {
            throw new NoSuchElementException();
        }
        return next;
    }

    private void fetchNext()
    {
        if ( iterator == null )
        {
            return;
        }
        if ( next != null )
        {
            return;
        }
        try
        {
            do
            {
                ResourceLocation resourceLocation = (ResourceLocation) iterator.next();
                ResourceHandle resourceHandle = resourceLocation.getResourceHandle( resourceName );
                if ( resourceHandle != null )
                {
                    next = resourceHandle.getUrl();
                    return;
                }
            }
            while ( iterator.hasNext() );

            iterator = null;
        }
        catch ( IllegalStateException e )
        {
            iterator = null;
            throw e;
        }
    }
}