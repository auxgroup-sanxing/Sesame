package com.sanxing.sesame.jaxp;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class StAXSourceTransformer
    extends SourceTransformer
{
    private XMLInputFactory inputFactory;

    private XMLOutputFactory outputFactory;

    public StaxSource toStaxSource( Source source )
        throws XMLStreamException
    {
        if ( source instanceof StaxSource )
        {
            return ( (StaxSource) source );
        }
        XMLInputFactory factory = getInputFactory();
        XMLStreamReader reader = factory.createXMLStreamReader( source );
        return new StaxSource( reader );
    }

    public XMLStreamReader toXMLStreamReader( Source source )
        throws XMLStreamException, TransformerException
    {
        if ( source instanceof StaxSource )
        {
            return ( (StaxSource) source ).getXMLStreamReader();
        }
        if ( source instanceof DOMSource )
        {
            Node n = ( (DOMSource) source ).getNode();
            Element el =
                ( n instanceof Element ) ? (Element) n
                    : ( n instanceof Document ) ? ( (Document) n ).getDocumentElement() : null;
            if ( el != null )
            {
                return new W3CDOMStreamReader( el );
            }
        }
        XMLInputFactory factory = getInputFactory();
        try
        {
            return factory.createXMLStreamReader( source );
        }
        catch ( XMLStreamException e )
        {
        }
        return factory.createXMLStreamReader( toReaderFromSource( source ) );
    }

    @Override
    public DOMSource toDOMSource( Source source )
        throws ParserConfigurationException, IOException, SAXException, TransformerException
    {
        DOMSource answer = super.toDOMSource( source );
        if ( ( answer == null ) && ( source instanceof StaxSource ) )
        {
            answer = toDOMSourceFromStax( (StaxSource) source );
        }
        return answer;
    }

    @Override
    public SAXSource toSAXSource( Source source )
        throws IOException, SAXException, TransformerException
    {
        SAXSource answer = super.toSAXSource( source );
        if ( ( answer == null ) && ( source instanceof StaxSource ) )
        {
            answer = toSAXSourceFromStax( (StaxSource) source );
        }
        return answer;
    }

    public DOMSource toDOMSourceFromStax( StaxSource source )
        throws TransformerException
    {
        Transformer transformer = createTransfomer();
        DOMResult result = new DOMResult();
        transformer.transform( source, result );
        return new DOMSource( result.getNode(), result.getSystemId() );
    }

    public SAXSource toSAXSourceFromStax( StaxSource source )
    {
        return source;
    }

    public XMLInputFactory getInputFactory()
    {
        if ( inputFactory == null )
        {
            inputFactory = createInputFactory();
        }
        return inputFactory;
    }

    public void setInputFactory( XMLInputFactory inputFactory )
    {
        this.inputFactory = inputFactory;
    }

    public XMLOutputFactory getOutputFactory()
    {
        if ( outputFactory == null )
        {
            outputFactory = createOutputFactory();
        }
        return outputFactory;
    }

    public void setOutputFactory( XMLOutputFactory outputFactory )
    {
        this.outputFactory = outputFactory;
    }

    protected XMLInputFactory createInputFactory()
    {
        return XMLInputFactory.newInstance();
    }

    protected XMLOutputFactory createOutputFactory()
    {
        return XMLOutputFactory.newInstance();
    }
}