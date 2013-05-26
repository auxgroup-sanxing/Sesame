package com.sanxing.sesame.wssecurity.commons;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;

public class NamespaceCatalog
    implements NamespaceContext
{
    private Properties prefix2URI = new Properties();

    private Map URI2PrefixList = new HashMap();

    private static final List EMPTY_LIST = new LinkedList();

    public NamespaceCatalog( String catalogResourcePath )
        throws IOException
    {
        InputStream is = getClass().getResourceAsStream( catalogResourcePath );
        if ( is == null )
        {
            throw new IOException( "Cannot locate the namespace catalog resource: " + catalogResourcePath );
        }
        loadCatalog( is );
    }

    public NamespaceCatalog( InputStream catalogInputStream )
        throws IOException
    {
        loadCatalog( catalogInputStream );
    }

    private void loadCatalog( InputStream catalogInputStream )
        throws IOException
    {
        this.prefix2URI.load( catalogInputStream );
        loadSpecialPrefixes();
        computeURI2PrefixList();
    }

    private void loadSpecialPrefixes()
    {
        this.prefix2URI.setProperty( "", "" );
        this.prefix2URI.setProperty( "xml", "http://www.w3.org/XML/1998/namespace" );
        this.prefix2URI.setProperty( "xmlns", "http://www.w3.org/2000/xmlns/" );
    }

    private void computeURI2PrefixList()
    {
        Iterator i = this.prefix2URI.entrySet().iterator();
        while ( i.hasNext() )
        {
            Map.Entry e = (Map.Entry) i.next();
            String prefix = (String) e.getKey();
            String uri = (String) e.getValue();

            List prefixList = (List) this.URI2PrefixList.get( uri );
            if ( prefixList == null )
            {
                prefixList = new LinkedList();
                this.URI2PrefixList.put( uri, prefixList );
            }
            prefixList.add( prefix );
        }
    }

    public String getNamespaceURI( String prefix )
    {
        if ( prefix == null )
        {
            throw new IllegalArgumentException( "Cannot lookup the namespaceURI for null prefix" );
        }
        String namespaceURI = this.prefix2URI.getProperty( prefix );
        if ( namespaceURI == null )
        {
            namespaceURI = "";
        }
        return namespaceURI;
    }

    public String getPrefix( String namespaceURI )
    {
        Iterator i = getPrefixes( namespaceURI );
        if ( !i.hasNext() )
            return null;
        return (String) i.next();
    }

    public Iterator getPrefixes( String namespaceURI )
    {
        if ( namespaceURI == null )
        {
            throw new IllegalArgumentException( "Cannot lookup the prefix for null namespace" );
        }
        List prefixList = (List) this.URI2PrefixList.get( namespaceURI );
        if ( prefixList == null )
            return EMPTY_LIST.iterator();
        return prefixList.iterator();
    }
}