package com.sanxing.sesame.jaxp;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

public class StaxSource
    extends SAXSource
    implements XMLReader
{
    private final XMLStreamReader streamReader;

    private ContentHandler contentHandler;

    private LexicalHandler lexicalHandler;

    public StaxSource( XMLStreamReader streamReader )
    {
        this.streamReader = streamReader;
        setInputSource( new InputSource() );
    }

    @Override
    public XMLReader getXMLReader()
    {
        return this;
    }

    public XMLStreamReader getXMLStreamReader()
    {
        return streamReader;
    }

    protected void parse()
        throws SAXException
    {
        try
        {
            switch ( streamReader.getEventType() )
            {
                case 10:
                    break;
                case 12:
                    if ( lexicalHandler != null )
                    {
                        lexicalHandler.startCDATA();
                    }
                    int length = streamReader.getTextLength();
                    int start = streamReader.getTextStart();
                    char[] chars = streamReader.getTextCharacters();
                    contentHandler.characters( chars, start, length );
                    if ( lexicalHandler != null )
                    {
                        lexicalHandler.endCDATA();
                    }
                    break;
                case 4:
                    length = streamReader.getTextLength();
                    start = streamReader.getTextStart();
                    chars = streamReader.getTextCharacters();
                    contentHandler.characters( chars, start, length );
                    break;
                case 6:
                    length = streamReader.getTextLength();
                    start = streamReader.getTextStart();
                    chars = streamReader.getTextCharacters();
                    contentHandler.ignorableWhitespace( chars, start, length );
                    break;
                case 5:
                    if ( lexicalHandler != null )
                    {
                        length = streamReader.getTextLength();
                        start = streamReader.getTextStart();
                        chars = streamReader.getTextCharacters();
                        lexicalHandler.comment( chars, start, length );
                    }
                    break;
                case 11:
                    break;
                case 8:
                    contentHandler.endDocument();
                    return;
                case 2:
                    String uri = streamReader.getNamespaceURI();
                    String localName = streamReader.getLocalName();
                    String prefix = streamReader.getPrefix();
                    String qname =
                        ( ( prefix != null ) && ( prefix.length() > 0 ) ) ? prefix + ":" + localName : localName;
                    contentHandler.endElement( uri, localName, qname );

                    break;
                case 9:
                case 13:
                case 14:
                case 15:
                    break;
                case 3:
                    break;
                case 7:
                    contentHandler.startDocument();
                    break;
                case 1:
                    uri = streamReader.getNamespaceURI();
                    localName = streamReader.getLocalName();
                    prefix = streamReader.getPrefix();
                    qname = ( ( prefix != null ) && ( prefix.length() > 0 ) ) ? prefix + ":" + localName : localName;
                    contentHandler.startElement( ( uri == null ) ? "" : uri, localName, qname, getAttributes() );
            }

            streamReader.next();
        }
        catch ( XMLStreamException e )
        {
            SAXParseException spe;
            if ( e.getLocation() != null )
            {
                spe =
                    new SAXParseException( e.getMessage(), null, null, e.getLocation().getLineNumber(),
                        e.getLocation().getColumnNumber(), e );
            }
            else
            {
                spe = new SAXParseException( e.getMessage(), null, null, -1, -1, e );
            }
            spe.initCause( e );
            throw spe;
        }
    }

    protected String getQualifiedName()
    {
        String prefix = streamReader.getPrefix();
        if ( ( prefix != null ) && ( prefix.length() > 0 ) )
        {
            return prefix + ":" + streamReader.getLocalName();
        }
        return streamReader.getLocalName();
    }

    protected Attributes getAttributes()
    {
        AttributesImpl attrs = new AttributesImpl();

        for ( int i = 0; i < streamReader.getNamespaceCount(); ++i )
        {
            String prefix = streamReader.getNamespacePrefix( i );
            String uri = streamReader.getNamespaceURI( i );
            if ( uri == null )
            {
                uri = "";
            }

            if ( ( prefix == null ) || ( prefix.length() == 0 ) )
            {
                attrs.addAttribute( "", null, "xmlns", "CDATA", uri );
            }
            else
            {
                attrs.addAttribute( "http://www.w3.org/2000/xmlns/", prefix, "xmlns:" + prefix, "CDATA", uri );
            }
        }
        for ( int i = 0; i < streamReader.getAttributeCount(); ++i )
        {
            String uri = streamReader.getAttributeNamespace( i );
            String localName = streamReader.getAttributeLocalName( i );
            String prefix = streamReader.getAttributePrefix( i );
            String qName;
            if ( ( prefix != null ) && ( prefix.length() > 0 ) )
            {
                qName = prefix + ':' + localName;
            }
            else
            {
                qName = localName;
            }
            String type = streamReader.getAttributeType( i );
            String value = streamReader.getAttributeValue( i );
            if ( value == null )
            {
                value = "";
            }

            attrs.addAttribute( ( uri == null ) ? "" : uri, localName, qName, type, value );
        }
        return attrs;
    }

    @Override
    public boolean getFeature( String name )
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return false;
    }

    @Override
    public void setFeature( String name, boolean value )
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
    }

    @Override
    public Object getProperty( String name )
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return null;
    }

    @Override
    public void setProperty( String name, Object value )
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if ( "http://xml.org/sax/properties/lexical-handler".equals( name ) )
        {
            lexicalHandler = ( (LexicalHandler) value );
        }
        else
        {
            throw new SAXNotRecognizedException( name );
        }
    }

    @Override
    public void setEntityResolver( EntityResolver resolver )
    {
    }

    @Override
    public EntityResolver getEntityResolver()
    {
        return null;
    }

    @Override
    public void setDTDHandler( DTDHandler handler )
    {
    }

    @Override
    public DTDHandler getDTDHandler()
    {
        return null;
    }

    @Override
    public void setContentHandler( ContentHandler handler )
    {
        contentHandler = handler;
    }

    @Override
    public ContentHandler getContentHandler()
    {
        return contentHandler;
    }

    @Override
    public void setErrorHandler( ErrorHandler handler )
    {
    }

    @Override
    public ErrorHandler getErrorHandler()
    {
        return null;
    }

    @Override
    public void parse( InputSource input )
        throws SAXException
    {
        parse();
    }

    @Override
    public void parse( String systemId )
        throws SAXException
    {
        parse();
    }
}