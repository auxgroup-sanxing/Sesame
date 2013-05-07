package com.sanxing.sesame.jaxp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UniversalNamespaceResolver
    implements NamespaceContext
{
    private static final Logger LOG = LoggerFactory.getLogger( UniversalNamespaceResolver.class );

    private static final String DEFAULT_NS = "DEFAULT";

    private final Map<String, String> prefix2Uri = new HashMap();

    private final Map<String, String> uri2Prefix = new HashMap();

    public UniversalNamespaceResolver( Document document, boolean toplevelOnly )
    {
        examineNode( document.getFirstChild(), toplevelOnly );
        LOG.info( "The list of the cached namespaces:" );
        for ( String key : prefix2Uri.keySet() )
        {
            LOG.info( "prefix " + key + ": uri " + prefix2Uri.get( key ) );
        }
    }

    private void examineNode( Node node, boolean attributesOnly )
    {
        NamedNodeMap attributes = node.getAttributes();
        for ( int i = 0; i < attributes.getLength(); ++i )
        {
            Node attribute = attributes.item( i );
            storeAttribute( (Attr) attribute );
        }

        if ( !( attributesOnly ) )
        {
            NodeList chields = node.getChildNodes();
            for ( int i = 0; i < chields.getLength(); ++i )
            {
                Node chield = chields.item( i );
                if ( chield.getNodeType() == 1 )
                {
                    examineNode( chield, false );
                }
            }
        }
    }

    private void storeAttribute( Attr attribute )
    {
        if ( ( attribute.getNamespaceURI() == null )
            || ( !( attribute.getNamespaceURI().equals( "http://www.w3.org/2000/xmlns/" ) ) ) )
        {
            return;
        }
        if ( attribute.getNodeName().equals( "xmlns" ) )
        {
            putInCache( DEFAULT_NS, attribute.getNodeValue() );
        }
        else
        {
            putInCache( attribute.getLocalName(), attribute.getNodeValue() );
        }
    }

    private void putInCache( String prefix, String uri )
    {
        prefix2Uri.put( prefix, uri );
        uri2Prefix.put( uri, prefix );
    }

    @Override
    public String getNamespaceURI( String prefix )
    {
        if ( ( prefix == null ) || ( prefix.equals( "" ) ) )
        {
            return prefix2Uri.get( DEFAULT_NS );
        }

        return prefix2Uri.get( prefix );
    }

    @Override
    public String getPrefix( String namespaceURI )
    {
        return uri2Prefix.get( namespaceURI );
    }

    @Override
    public Iterator<String> getPrefixes( String namespaceURI )
    {
        List list = new ArrayList();
        for ( Object element : prefix2Uri.entrySet() )
        {
            Map.Entry entry = (Map.Entry) element;
            if ( namespaceURI.equals( entry.getValue() ) )
            {
                list.add( entry.getKey() );
            }
        }
        return list.iterator();
    }
}