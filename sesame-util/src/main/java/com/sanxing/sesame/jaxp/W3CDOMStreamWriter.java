package com.sanxing.sesame.jaxp;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class W3CDOMStreamWriter
    extends DOMStreamWriter
{
    static final String XML_NS = "http://www.w3.org/2000/xmlns/";

    private final Stack stack;

    private final Document document;

    private Element currentNode;

    private NamespaceContext context;

    private final Map properties;

    public W3CDOMStreamWriter()
        throws ParserConfigurationException
    {
        this( DocumentBuilderFactory.newInstance().newDocumentBuilder() );
    }

    public W3CDOMStreamWriter( DocumentBuilder builder )
    {
        stack = new Stack();

        properties = new HashMap();

        document = builder.newDocument();
    }

    public W3CDOMStreamWriter( Document document )
    {
        stack = new Stack();

        properties = new HashMap();

        this.document = document;
    }

    public Document getDocument()
    {
        return document;
    }

    @Override
    public void writeStartElement( String local )
        throws XMLStreamException
    {
        newChild( document.createElement( local ) );
    }

    private void newChild( Element element )
    {
        if ( currentNode != null )
        {
            stack.push( currentNode );
            currentNode.appendChild( element );
        }
        else
        {
            document.appendChild( element );
        }

        W3CNamespaceContext ctx = new W3CNamespaceContext();
        ctx.setElement( element );
        context = ctx;

        currentNode = element;
    }

    @Override
    public void writeStartElement( String namespace, String local )
        throws XMLStreamException
    {
        newChild( document.createElementNS( namespace, local ) );
    }

    @Override
    public void writeStartElement( String prefix, String local, String namespace )
        throws XMLStreamException
    {
        if ( ( prefix == null ) || ( prefix.equals( "" ) ) )
        {
            writeStartElement( namespace, local );
        }
        else
        {
            newChild( document.createElementNS( namespace, prefix + ":" + local ) );
        }
    }

    @Override
    public void writeEmptyElement( String namespace, String local )
        throws XMLStreamException
    {
        writeStartElement( namespace, local );
    }

    @Override
    public void writeEmptyElement( String prefix, String namespace, String local )
        throws XMLStreamException
    {
        writeStartElement( prefix, namespace, local );
    }

    @Override
    public void writeEmptyElement( String local )
        throws XMLStreamException
    {
        writeStartElement( local );
    }

    @Override
    public void writeEndElement()
        throws XMLStreamException
    {
        if ( stack.size() > 0 )
        {
            currentNode = ( (Element) stack.pop() );
        }
        else
        {
            currentNode = null;
        }
    }

    @Override
    public void writeEndDocument()
        throws XMLStreamException
    {
    }

    @Override
    public void writeAttribute( String local, String value )
        throws XMLStreamException
    {
        Attr a = document.createAttribute( local );
        a.setValue( value );
        currentNode.setAttributeNode( a );
    }

    @Override
    public void writeAttribute( String prefix, String namespace, String local, String value )
        throws XMLStreamException
    {
        if ( prefix.length() > 0 )
        {
            local = prefix + ":" + local;
        }
        Attr a = document.createAttributeNS( namespace, local );
        a.setValue( value );
        currentNode.setAttributeNodeNS( a );
    }

    @Override
    public void writeAttribute( String namespace, String local, String value )
        throws XMLStreamException
    {
        Attr a = document.createAttributeNS( namespace, local );
        a.setValue( value );
        currentNode.setAttributeNodeNS( a );
    }

    @Override
    public void writeNamespace( String prefix, String namespace )
        throws XMLStreamException
    {
        if ( prefix.length() == 0 )
        {
            writeDefaultNamespace( namespace );
        }
        else
        {
            currentNode.setAttributeNS( "http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, namespace );
        }
    }

    @Override
    public void writeDefaultNamespace( String namespace )
        throws XMLStreamException
    {
        currentNode.setAttributeNS( "http://www.w3.org/2000/xmlns/", "xmlns", namespace );
    }

    @Override
    public void writeComment( String value )
        throws XMLStreamException
    {
        currentNode.appendChild( document.createComment( value ) );
    }

    @Override
    public void writeProcessingInstruction( String target )
        throws XMLStreamException
    {
        currentNode.appendChild( document.createProcessingInstruction( target, null ) );
    }

    @Override
    public void writeProcessingInstruction( String target, String data )
        throws XMLStreamException
    {
        currentNode.appendChild( document.createProcessingInstruction( target, data ) );
    }

    @Override
    public void writeCData( String data )
        throws XMLStreamException
    {
        currentNode.appendChild( document.createCDATASection( data ) );
    }

    @Override
    public void writeDTD( String arg0 )
        throws XMLStreamException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeEntityRef( String ref )
        throws XMLStreamException
    {
        currentNode.appendChild( document.createEntityReference( ref ) );
    }

    @Override
    public void writeStartDocument()
        throws XMLStreamException
    {
    }

    @Override
    public void writeStartDocument( String version )
        throws XMLStreamException
    {
        writeStartDocument();
    }

    @Override
    public void writeStartDocument( String encoding, String version )
        throws XMLStreamException
    {
        writeStartDocument();
    }

    @Override
    public void writeCharacters( String text )
        throws XMLStreamException
    {
        currentNode.appendChild( document.createTextNode( text ) );
    }

    @Override
    public void writeCharacters( char[] text, int start, int len )
        throws XMLStreamException
    {
        writeCharacters( new String( text, start, len ) );
    }

    @Override
    public String getPrefix( String uri )
        throws XMLStreamException
    {
        return context.getPrefix( uri );
    }

    @Override
    public void setPrefix( String arg0, String arg1 )
        throws XMLStreamException
    {
    }

    @Override
    public void setDefaultNamespace( String arg0 )
        throws XMLStreamException
    {
    }

    @Override
    public void setNamespaceContext( NamespaceContext ctx )
        throws XMLStreamException
    {
        context = ctx;
    }

    @Override
    public NamespaceContext getNamespaceContext()
    {
        return context;
    }

    @Override
    public Object getProperty( String prop )
        throws IllegalArgumentException
    {
        return properties.get( prop );
    }
}