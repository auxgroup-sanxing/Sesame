package com.sanxing.sesame.jaxp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

public class NamespaceContextImpl
    implements NamespaceContext
{
    private final Map<String, String> namespaces;

    public NamespaceContextImpl()
    {
        namespaces = new LinkedHashMap();
    }

    public NamespaceContextImpl( Map<String, String> namespaces )
    {
        this.namespaces = new LinkedHashMap( namespaces );
    }

    public Map<String, String> getNamespaces()
    {
        return namespaces;
    }

    public void setNamespaces( Map<String, String> namespaces )
    {
        this.namespaces.clear();
        if ( namespaces != null )
        {
            this.namespaces.putAll( namespaces );
        }
    }

    @Override
    public String getNamespaceURI( String prefix )
    {
        if ( prefix == null )
        {
            throw new IllegalArgumentException( "prefix argument was null" );
        }
        if ( prefix.equals( "xml" ) )
        {
            return "http://www.w3.org/XML/1998/namespace";
        }
        if ( prefix.equals( "xmlns" ) )
        {
            return "http://www.w3.org/2000/xmlns/";
        }
        if ( namespaces.containsKey( prefix ) )
        {
            String uri = namespaces.get( prefix );
            if ( uri.length() == 0 )
            {
                return null;
            }
            return uri;
        }

        return null;
    }

    @Override
    public String getPrefix( String nsURI )
    {
        if ( nsURI == null )
        {
            throw new IllegalArgumentException( "nsURI was null" );
        }
        if ( nsURI.length() == 0 )
        {
            throw new IllegalArgumentException( "nsURI was empty" );
        }
        if ( nsURI.equals( "http://www.w3.org/XML/1998/namespace" ) )
        {
            return "xml";
        }
        if ( nsURI.equals( "http://www.w3.org/2000/xmlns/" ) )
        {
            return "xmlns";
        }
        Iterator iter = namespaces.entrySet().iterator();
        while ( iter.hasNext() )
        {
            Map.Entry entry = (Map.Entry) iter.next();
            String uri = (String) entry.getValue();
            if ( uri.equals( nsURI ) )
            {
                return ( (String) entry.getKey() );
            }
        }
        if ( nsURI.length() == 0 )
        {
            return "";
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes( String nsURI )
    {
        if ( nsURI == null )
        {
            throw new IllegalArgumentException( "nsURI was null" );
        }
        if ( nsURI.length() == 0 )
        {
            throw new IllegalArgumentException( "nsURI was empty" );
        }
        if ( nsURI.equals( "http://www.w3.org/XML/1998/namespace" ) )
        {
            return Collections.singleton( "xml" ).iterator();
        }
        if ( nsURI.equals( "http://www.w3.org/2000/xmlns/" ) )
        {
            return Collections.singleton( "xmlns" ).iterator();
        }
        Set prefixes = null;
        for ( Map.Entry entry : namespaces.entrySet() )
        {
            String uri = (String) entry.getValue();
            if ( uri.equals( nsURI ) )
            {
                if ( prefixes == null )
                {
                    prefixes = new HashSet();
                }
                prefixes.add( entry.getKey() );
            }
        }
        if ( prefixes != null )
        {
            return Collections.unmodifiableSet( prefixes ).iterator();
        }
        if ( nsURI.length() == 0 )
        {
            return Collections.singleton( "" ).iterator();
        }
        List l = Collections.emptyList();
        return l.iterator();
    }
}