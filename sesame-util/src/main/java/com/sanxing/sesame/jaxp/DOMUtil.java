package com.sanxing.sesame.jaxp;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DOMUtil
{
    private static DocumentBuilderFactory documentBuilderFactory;

    public static Document createDocument()
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

    public static Document parse( InputStream is )
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

    public static void clearContent( Element element )
    {
        NodeList childs = element.getChildNodes();
        int n;
        while ( ( n = childs.getLength() ) > 0 )
        {
            element.removeChild( childs.item( n - 1 ) );
        }
    }

    public static Element getFirstChildElement( Node parent )
    {
        NodeList childs = parent.getChildNodes();
        for ( int i = 0; i < childs.getLength(); ++i )
        {
            Node child = childs.item( i );
            if ( child instanceof Element )
            {
                return ( (Element) child );
            }
        }
        return null;
    }

    public static String getElementText( Element element )
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

    public static Element getNextSiblingElement( Element el )
    {
        for ( Node n = el.getNextSibling(); n != null; n = n.getNextSibling() )
        {
            if ( n instanceof Element )
            {
                return ( (Element) n );
            }
        }
        return null;
    }

    public static Element createElement( Node parent, QName name )
    {
        Document doc = ( parent instanceof Document ) ? (Document) parent : parent.getOwnerDocument();
        Element element;
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

    public static QName createQName( Element element, String qualifiedName )
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
        if ( parent instanceof Element )
        {
            Element element = (Element) parent;
            String answer = element.getAttribute( attributeName );
            if ( ( answer == null ) || ( answer.length() == 0 ) )
            {
                Node parentNode = element.getParentNode();
                if ( parentNode instanceof Element )
                {
                    return recursiveGetAttributeValue( parentNode, attributeName );
                }
            }
            return answer;
        }

        return null;
    }

    protected static String getUniquePrefix( Element element )
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

    public static QName getQName( Element el )
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
            Document document =
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

    public static void main( String[] args )
    {
    }
}