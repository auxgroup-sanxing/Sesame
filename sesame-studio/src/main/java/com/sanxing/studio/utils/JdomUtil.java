package com.sanxing.studio.utils;

import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

public class JdomUtil
{
    private static TransformerFactory transformerFactory;

    public static SAXBuilder newSAXBuilder()
    {
        SAXBuilder builder = new SAXBuilder( "org.apache.xerces.parsers.SAXParser", false );
        builder.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
        return builder;
    }

    public static SAXBuilder newSAXBuilder( boolean validate )
    {
        SAXBuilder builder = new SAXBuilder( "org.apache.xerces.parsers.SAXParser", validate );
        builder.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
        return builder;
    }

    public static Namespace getAdditionalNamespace( Element element, String uri )
    {
        List list = element.getAdditionalNamespaces();
        for ( Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Namespace ns = (Namespace) iter.next();
            if ( ns.getURI().equals( uri ) )
            {
                return ns;
            }
        }
        return null;
    }

    public static XMLOutputter getPrettyOutputter( String encoding )
    {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat( Format.getPrettyFormat().setEncoding( encoding ).setIndent( "  " ) );
        return outputter;
    }

    public static XMLOutputter getPrettyOutputter()
    {
        return getPrettyOutputter( "utf-8" );
    }

    public static JDOMSource DOMSource2JDOMSource( DOMSource domSource )
    {
        org.jdom.Document jdoc = new DOMBuilder().build( (org.w3c.dom.Document) domSource.getNode() );
        return new JDOMSource( jdoc );
    }

    public static Source JDOMElement2DOMSource( Element element )
        throws JDOMException
    {
        if ( element.getDocument() != null )
        {
            element.detach();
        }
        org.jdom.Document jdoc = new org.jdom.Document( element );

        org.w3c.dom.Document doc = new DOMOutputter().output( jdoc );
        return new DOMSource( doc );
    }

    public static Element source2JDOMElement( Source source )
        throws Exception
    {
        JDOMResult result = new JDOMResult();
        Transformer transformer = getTransformerFactory().newTransformer();
        transformer.transform( source, result );
        Element root = result.getDocument().getRootElement();
        root.detach();
        return root;
    }

    private static TransformerFactory getTransformerFactory()
    {
        if ( transformerFactory == null )
        {
            transformerFactory = TransformerFactory.newInstance();
        }
        return transformerFactory;
    }
}