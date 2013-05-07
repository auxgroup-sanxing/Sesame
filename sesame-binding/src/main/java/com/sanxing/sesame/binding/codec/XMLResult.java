package com.sanxing.sesame.binding.codec;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

public class XMLResult
    implements Result
{
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private String systemId;

    private Source content;

    private final Map<String, Object> properties = new Hashtable();

    @Override
    public void setSystemId( String systemId )
    {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId()
    {
        return systemId;
    }

    public Set<String> getPropertyNames()
    {
        return properties.keySet();
    }

    public Object getProperty( String name )
    {
        return properties.get( name );
    }

    public void setProperty( String name, Object value )
    {
        properties.put( name, value );
    }

    public org.w3c.dom.Document getW3CDocument()
        throws TransformerException
    {
        if ( content instanceof DOMSource )
        {
            return ( (org.w3c.dom.Document) ( (DOMSource) content ).getNode() );
        }

        DOMResult result = new DOMResult();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform( content, result );
        return ( (org.w3c.dom.Document) result.getNode() );
    }

    public org.jdom.Document getJDOMDocument()
        throws TransformerException
    {
        if ( content instanceof JDOMSource )
        {
            return ( (JDOMSource) content ).getDocument();
        }

        JDOMResult result = new JDOMResult();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform( content, result );
        return result.getDocument();
    }

    public org.dom4j.Document getDOM4jDocument()
        throws TransformerException
    {
        if ( content instanceof DocumentSource )
        {
            return ( (DocumentSource) content ).getDocument();
        }

        DocumentResult result = new DocumentResult();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform( content, result );
        return result.getDocument();
    }

    public Source getContent()
    {
        return content;
    }

    public void setContent( Source content )
    {
        this.content = content;
    }

    public void setDocument( org.w3c.dom.Document document )
    {
        content = new DOMSource( document );
    }

    public void setDocument( org.jdom.Document document )
    {
        content = new JDOMSource( document );
    }

    public void setDocument( org.dom4j.Document document )
    {
        content = new DocumentSource( document );
    }
}