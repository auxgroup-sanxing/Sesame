package com.sanxing.sesame.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JdomUtil
{
    private static DocumentBuilderFactory documentBuilderFactory;

    private static TransformerFactory transformerFactory;

    private static Format pretty = Format.getPrettyFormat();

    public static org.w3c.dom.Document createDocument()
    {
        try
        {
            return getDocumentBuilderFactory().newDocumentBuilder().newDocument();
        }
        catch ( ParserConfigurationException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public static org.w3c.dom.Document parse( InputStream is )
    {
        try
        {
            return getDocumentBuilderFactory().newDocumentBuilder().parse( is );
        }
        catch ( SAXException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        catch ( ParserConfigurationException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public static DOMSource JDOMSource2DOMSource( JDOMSource jdomSource )
    {
        try
        {
            Document doc = jdomSource.getDocument();
            org.w3c.dom.Document document = new DOMOutputter().output( doc );
            return new DOMSource( document );
        }
        catch ( JDOMException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory()
    {
        if ( documentBuilderFactory == null )
        {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware( true );
            documentBuilderFactory = f;
        }
        return documentBuilderFactory;
    }

    private static TransformerFactory getTransformerFactory()
    {
        if ( transformerFactory == null )
        {
            transformerFactory = TransformerFactory.newInstance();
        }

        return transformerFactory;
    }

    public static org.w3c.dom.Element getFirstChildElement( Node parent )
    {
        NodeList childs = parent.getChildNodes();
        for ( int i = 0; i < childs.getLength(); ++i )
        {
            Node child = childs.item( i );
            if ( child instanceof org.w3c.dom.Element )
            {
                return ( (org.w3c.dom.Element) child );
            }
        }
        return null;
    }

    public static String getElementText( org.w3c.dom.Element element )
    {
        StringBuffer buffer = new StringBuffer();
        NodeList nodeList = element.getChildNodes();
        int i = 0;
        for ( int size = nodeList.getLength(); i < size; ++i )
        {
            Node node = nodeList.item( i );
            if ( ( node.getNodeType() == 3 ) || ( node.getNodeType() == 4 ) )
            {
                buffer.append( node.getNodeValue() );
            }
        }
        return buffer.toString();
    }

    public static org.w3c.dom.Element getNextSiblingElement( org.w3c.dom.Element el )
    {
        for ( Node n = el.getNextSibling(); n != null; n = n.getNextSibling() )
        {
            if ( n instanceof org.w3c.dom.Element )
            {
                return ( (org.w3c.dom.Element) n );
            }
        }
        return null;
    }

    public static org.w3c.dom.Element createElement( Node parent, QName name )
    {
        org.w3c.dom.Document doc =
            ( parent instanceof org.w3c.dom.Document ) ? (org.w3c.dom.Document) parent : parent.getOwnerDocument();
        org.w3c.dom.Element element;
        if ( ( name.getPrefix() != null ) && ( name.getPrefix().length() > 0 ) )
        {
            element = doc.createElementNS( name.getNamespaceURI(), name.getPrefix() + ":" + name.getLocalPart() );
            String attr = recursiveGetAttributeValue( parent, "xmlns:" + name.getPrefix() );
            if ( ( attr == null ) || ( !( attr.equals( name.getNamespaceURI() ) ) ) )
            {
                element.setAttribute( "xmlns:" + name.getPrefix(), name.getNamespaceURI() );
            }
        }
        else if ( ( name.getNamespaceURI() != null ) && ( name.getNamespaceURI().length() > 0 ) )
        {
            element = doc.createElementNS( name.getNamespaceURI(), name.getLocalPart() );
            String attr = recursiveGetAttributeValue( parent, "xmlns" );
            if ( ( attr == null ) || ( !( attr.equals( name.getNamespaceURI() ) ) ) )
            {
                element.setAttribute( "xmlns", name.getNamespaceURI() );
            }
        }
        else
        {
            element = doc.createElementNS( null, name.getLocalPart() );
            String attr = recursiveGetAttributeValue( parent, "xmlns" );
            if ( ( attr == null ) || ( attr.length() > 0 ) )
            {
                element.setAttribute( "xmlns", "" );
            }
        }
        parent.appendChild( element );
        return element;
    }

    public static QName createQName( org.w3c.dom.Element element, String qualifiedName )
    {
        int index = qualifiedName.indexOf( 58 );
        if ( index >= 0 )
        {
            String prefix = qualifiedName.substring( 0, index );
            String localName = qualifiedName.substring( index + 1 );
            String uri = recursiveGetAttributeValue( element, "xmlns:" + prefix );
            return new QName( uri, localName, prefix );
        }
        String uri = recursiveGetAttributeValue( element, "xmlns" );
        if ( uri != null )
        {
            return new QName( uri, qualifiedName );
        }
        return new QName( qualifiedName );
    }

    public static String recursiveGetAttributeValue( Node parent, String attributeName )
    {
        if ( parent instanceof org.w3c.dom.Element )
        {
            org.w3c.dom.Element element = (org.w3c.dom.Element) parent;
            String answer = element.getAttribute( attributeName );
            if ( ( answer == null ) || ( answer.length() == 0 ) )
            {
                Node parentNode = element.getParentNode();
                if ( parentNode instanceof org.w3c.dom.Element )
                {
                    return recursiveGetAttributeValue( parentNode, attributeName );
                }
            }
            return answer;
        }
        return null;
    }

    protected static String getUniquePrefix( org.w3c.dom.Element element )
    {
        int n = 1;
        while ( true )
        {
            String nsPrefix = "ns" + n;
            if ( recursiveGetAttributeValue( element, "xmlns:" + nsPrefix ) == null )
            {
                return nsPrefix;
            }
            ++n;
        }
    }

    public static QName getQName( org.w3c.dom.Element el )
    {
        if ( el == null )
        {
            return null;
        }
        if ( el.getPrefix() != null )
        {
            return new QName( el.getNamespaceURI(), el.getLocalName(), el.getPrefix() );
        }
        return new QName( el.getNamespaceURI(), el.getLocalName() );
    }

    public static void validate()
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware( true );
            DocumentBuilder parser = dbf.newDocumentBuilder();
            org.w3c.dom.Document document =
                parser.parse( Thread.currentThread().getContextClassLoader().getResourceAsStream( "config/test.xml" ) );

            SchemaFactory factory = SchemaFactory.newInstance( "http://www.w3.org/2001/XMLSchema" );

            Source schemaFile =
                new StreamSource( Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "config/sesame-component.xsd" ) );

            Schema schema = factory.newSchema( schemaFile );

            Validator validator = schema.newValidator();
            try
            {
                validator.validate( new DOMSource( document ) );
            }
            catch ( SAXException e )
            {
                e.printStackTrace();
            }
        }
        catch ( ParserConfigurationException e )
        {
            e.printStackTrace();
        }
        catch ( SAXException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    public static String print( Source source )
    {
        try
        {
            XMLOutputter xmlOutputter = new XMLOutputter( pretty );
            return xmlOutputter.outputString(source2JDOMDocument( source ));
        }
        catch ( Exception e )
        {
        	// ignore
        }
        return "";
    }

    public static Source JDOMElement2DOMSource( Element element )
    {
        try
        {
            if ( element.getDocument() != null )
            {
                element.detach();
            }
            Document jdoc = new Document( element );

            return new JDOMSource( jdoc );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public static Source JDOMDocument2DOMSource( Document document )
    {
        try
        {
            org.w3c.dom.Document doc = new DOMOutputter().output( document );

            return new DOMSource( doc );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public static Document source2JDOMDocument( Source source )
    {
        if ( source instanceof JDOMSource )
        {
            return ( (JDOMSource) source ).getDocument();
        }
        try
        {
            JDOMResult result = new JDOMResult();
            Transformer transformer = getTransformerFactory().newTransformer();
            transformer.transform( source, result );
            return result.getDocument();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    public static JDOMSource DOMSource2JDOMSource( DOMSource domSource )
    {
        Document jdoc = new DOMBuilder().build( (org.w3c.dom.Document) domSource.getNode() );
        return new JDOMSource( jdoc );
    }

    public static void init()
    {
    }
}