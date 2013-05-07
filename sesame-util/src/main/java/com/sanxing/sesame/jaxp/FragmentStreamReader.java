package com.sanxing.sesame.jaxp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

public class FragmentStreamReader
    extends StreamReaderDelegate
    implements XMLStreamReader
{
    private static final int STATE_START_DOC = 0;

    private static final int STATE_FIRST_ELEM = 1;

    private static final int STATE_FIRST_RUN = 2;

    private static final int STATE_RUN = 3;

    private static final int STATE_END_DOC = 4;

    private int depth;

    private int state = 0;

    private int event = 7;

    private final List rootPrefixes;

    public FragmentStreamReader( XMLStreamReader parent )
    {
        super( parent );
        rootPrefixes = new ArrayList();
        NamespaceContext ctx = getParent().getNamespaceContext();
        if ( ctx instanceof ExtendedNamespaceContext )
        {
            Iterator it = ( (ExtendedNamespaceContext) ctx ).getPrefixes();
            while ( it.hasNext() )
            {
                String prefix = (String) it.next();
                rootPrefixes.add( prefix );
            }
        }
    }

    @Override
    public int getEventType()
    {
        return event;
    }

    @Override
    public boolean hasNext()
        throws XMLStreamException
    {
        return ( event != 8 );
    }

    @Override
    public int next()
        throws XMLStreamException
    {
        switch ( state )
        {
            case 0:
                state = 1;
                event = 7;
                break;
            case 1:
                state = 2;
                depth += 1;
                event = 1;
                break;
            case 2:
            case 3:
                state = 3;
                event = getParent().next();
                if ( event == 1 )
                {
                    depth += 1;
                    break;
                }
                if ( event != 2 )
                {
                    break;
                }
                depth -= 1;
                if ( depth != 0 )
                {
                    break;
                }
                state = 4;

                break;
            case 4:
                event = 8;
                break;
            default:
                throw new IllegalStateException();
        }
        return event;
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
    public int getNamespaceCount()
    {
        if ( state == 2 )
        {
            return rootPrefixes.size();
        }
        return getParent().getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix( int i )
    {
        if ( state == 2 )
        {
            return ( (String) rootPrefixes.get( i ) );
        }
        return getParent().getNamespacePrefix( i );
    }

    @Override
    public String getNamespaceURI( int i )
    {
        if ( state == 2 )
        {
            return getParent().getNamespaceContext().getNamespaceURI( (String) rootPrefixes.get( i ) );
        }
        return getParent().getNamespaceURI( i );
    }

    @Override
    public String getNamespaceURI( String prefix )
    {
        if ( state == 2 )
        {
            return getParent().getNamespaceContext().getNamespaceURI( prefix );
        }
        return getParent().getNamespaceURI( prefix );
    }

    @Override
    public boolean isStartElement()
    {
        return ( event == 1 );
    }
}