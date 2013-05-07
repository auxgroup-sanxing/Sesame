package com.sanxing.sesame.jaxp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

public class ExtendedXMLStreamReader
    extends StreamReaderDelegate
{
    private SimpleNamespaceContext context = new SimpleNamespaceContext();

    public ExtendedXMLStreamReader( XMLStreamReader delegate )
    {
        super( delegate );
    }

    @Override
    public NamespaceContext getNamespaceContext()
    {
        return context;
    }

    @Override
    public int nextTag()
        throws XMLStreamException
    {
        int eventType = next();
        while ( ( ( eventType == 4 ) && ( isWhiteSpace() ) ) || ( ( eventType == 12 ) && ( isWhiteSpace() ) )
            || ( eventType == 6 ) || ( eventType == 3 ) || ( eventType == 5 ) )
        {
            eventType = next();
        }
        if ( ( eventType != 1 ) && ( eventType != 2 ) )
        {
            throw new XMLStreamException( "expected start or end tag", getLocation() );
        }
        return eventType;
    }

    @Override
    public int next()
        throws XMLStreamException
    {
        int next = super.next();
        if ( next == 1 )
        {
            context = new SimpleNamespaceContext( context, getNamespaces() );
        }
        else if ( next == 2 )
        {
            context = context.getParent();
        }
        return next;
    }

    private Map getNamespaces()
    {
        Map ns = new HashMap();
        for ( int i = 0; i < getNamespaceCount(); ++i )
        {
            ns.put( getNamespacePrefix( i ), getNamespaceURI( i ) );
        }
        return ns;
    }

    public static class SimpleNamespaceContext
        implements ExtendedNamespaceContext
    {
        private SimpleNamespaceContext parent;

        private final Map namespaces;

        public SimpleNamespaceContext()
        {
            namespaces = new HashMap();
        }

        public SimpleNamespaceContext( SimpleNamespaceContext parent, Map namespaces )
        {
            this.parent = parent;
            this.namespaces = namespaces;
        }

        public SimpleNamespaceContext getParent()
        {
            return parent;
        }

        @Override
        public Iterator getPrefixes()
        {
            Set prefixes = new HashSet();
            for ( SimpleNamespaceContext context = this; context != null; context = context.parent )
            {
                prefixes.addAll( context.namespaces.keySet() );
            }
            return prefixes.iterator();
        }

        @Override
        public String getNamespaceURI( String prefix )
        {
            String uri = (String) namespaces.get( prefix );
            if ( ( uri == null ) && ( parent != null ) )
            {
                uri = parent.getNamespaceURI( prefix );
            }
            return uri;
        }

        @Override
        public String getPrefix( String namespaceURI )
        {
            for ( SimpleNamespaceContext context = this; context != null; context = context.parent )
            {
                for ( Iterator it = context.namespaces.keySet().iterator(); it.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) it.next();
                    if ( entry.getValue().equals( namespaceURI ) )
                    {
                        return ( (String) entry.getKey() );
                    }
                }
            }
            return null;
        }

        @Override
        public Iterator getPrefixes( String namespaceURI )
        {
            Set prefixes = new HashSet();
            for ( SimpleNamespaceContext context = this; context != null; context = context.parent )
            {
                for ( Iterator it = context.namespaces.keySet().iterator(); it.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) it.next();
                    if ( entry.getValue().equals( namespaceURI ) )
                    {
                        prefixes.add( entry.getKey() );
                    }
                }
            }
            return prefixes.iterator();
        }
    }
}