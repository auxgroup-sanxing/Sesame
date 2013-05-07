package com.sanxing.sesame.jaxp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sanxing.sesame.util.FastStack;

public abstract class DOMStreamReader
    implements XMLStreamReader
{
    protected Map properties = new HashMap();

    protected int currentEvent = 7;

    protected FastStack<ElementFrame> frames = new FastStack();

    protected ElementFrame frame;

    public DOMStreamReader( ElementFrame frame )
    {
        init( frame );
    }

    private void init( ElementFrame f )
    {
        frame = f;
        frames.push( frame );
        newFrame( f );
    }

    protected ElementFrame getCurrentFrame()
    {
        return frame;
    }

    @Override
    public Object getProperty( String key )
        throws IllegalArgumentException
    {
        return properties.get( key );
    }

    @Override
    public int next()
        throws XMLStreamException
    {
        if ( frame.ended )
        {
            frames.pop();
            if ( !( frames.empty() ) )
            {
                frame = frames.peek();
            }
            else
            {
                currentEvent = 8;
                return currentEvent;
            }
        }

        if ( !( frame.started ) )
        {
            frame.started = true;
            currentEvent = 1;
        }
        else if ( frame.currentAttribute < getAttributeCount() - 1 )
        {
            frame.currentAttribute += 1;
            currentEvent = 10;
        }
        else if ( frame.currentChild < getChildCount() - 1 )
        {
            frame.currentChild += 1;

            currentEvent = moveToChild( frame.currentChild );

            if ( currentEvent == 1 )
            {
                ElementFrame newFrame = getChildFrame( frame.currentChild );
                newFrame.started = true;
                frame = newFrame;
                frames.push( frame );
                currentEvent = 1;

                newFrame( newFrame );
            }
        }
        else
        {
            frame.ended = true;
            currentEvent = 2;
            endElement();
        }
        return currentEvent;
    }

    protected void newFrame( ElementFrame newFrame )
    {
    }

    protected void endElement()
    {
    }

    protected abstract int moveToChild( int paramInt );

    protected abstract ElementFrame getChildFrame( int paramInt );

    protected abstract int getChildCount();

    @Override
    public void require( int arg0, String arg1, String arg2 )
        throws XMLStreamException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract String getElementText()
        throws XMLStreamException;

    @Override
    public int nextTag()
        throws XMLStreamException
    {
        while ( hasNext() )
        {
            if ( 1 == next() )
            {
                return 1;
            }
        }

        return currentEvent;
    }

    @Override
    public boolean hasNext()
        throws XMLStreamException
    {
        return ( ( frames.size() != 0 ) || ( !( frame.ended ) ) );
    }

    @Override
    public void close()
        throws XMLStreamException
    {
    }

    @Override
    public abstract String getNamespaceURI( String paramString );

    @Override
    public boolean isStartElement()
    {
        return ( currentEvent == 1 );
    }

    @Override
    public boolean isEndElement()
    {
        return ( currentEvent == 2 );
    }

    @Override
    public boolean isCharacters()
    {
        return ( currentEvent == 4 );
    }

    @Override
    public boolean isWhiteSpace()
    {
        return ( currentEvent == 6 );
    }

    @Override
    public int getEventType()
    {
        return currentEvent;
    }

    @Override
    public int getTextCharacters( int sourceStart, char[] target, int targetStart, int length )
        throws XMLStreamException
    {
        char[] src = getText().toCharArray();

        if ( sourceStart + length >= src.length )
        {
            length = src.length - sourceStart;
        }

        for ( int i = 0; i < length; ++i )
        {
            target[( targetStart + i )] = src[( i + sourceStart )];
        }

        return length;
    }

    @Override
    public boolean hasText()
    {
        return ( ( currentEvent == 4 ) || ( currentEvent == 11 ) || ( currentEvent == 9 ) || ( currentEvent == 5 ) || ( currentEvent == 6 ) );
    }

    @Override
    public Location getLocation()
    {
        return new Location()
        {
            @Override
            public int getCharacterOffset()
            {
                return 0;
            }

            @Override
            public int getColumnNumber()
            {
                return 0;
            }

            @Override
            public int getLineNumber()
            {
                return 0;
            }

            @Override
            public String getPublicId()
            {
                return null;
            }

            @Override
            public String getSystemId()
            {
                return null;
            }
        };
    }

    @Override
    public boolean hasName()
    {
        return ( ( currentEvent == 1 ) || ( currentEvent == 2 ) );
    }

    @Override
    public String getVersion()
    {
        return null;
    }

    @Override
    public boolean isStandalone()
    {
        return false;
    }

    @Override
    public boolean standaloneSet()
    {
        return false;
    }

    @Override
    public String getCharacterEncodingScheme()
    {
        return null;
    }

    public static class ElementFrame
    {
        Object element;

        boolean started;

        boolean ended;

        int currentChild = -1;

        int currentAttribute = -1;

        int currentElement = -1;

        List<String> uris;

        List<String> prefixes;

        List attributes;

        final ElementFrame parent;

        public ElementFrame( Object element, ElementFrame parent )
        {
            this.element = element;
            this.parent = parent;
        }
    }
}