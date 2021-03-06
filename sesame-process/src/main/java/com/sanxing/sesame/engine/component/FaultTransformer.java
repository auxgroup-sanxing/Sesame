package com.sanxing.sesame.engine.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaultTransformer
{
    private static Logger LOG = LoggerFactory.getLogger( FaultTransformer.class );

    private final Map<String, Element> mapping = new HashMap();

    public FaultTransformer( Document descriptor )
        throws IOException
    {
        Element templateEl = descriptor.getRootElement();

        List<Element> cases = templateEl.getChildren( "when", templateEl.getNamespace() );
        for ( Element whenEl : cases )
        {
            mapping.put( whenEl.getAttributeValue( "status" ), whenEl );
        }
    }

    public boolean hasEntry()
    {
        return ( !( mapping.isEmpty() ) );
    }

    public Document transform( String status, String statusText )
    {
        Element rootEl = new Element( "fault" );
        Element codeEl = new Element( "fault-code" );
        String faultCode = null;
        String faultReason = null;
        Element entry = mapping.get( status );
        if ( entry == null )
        {
            entry = mapping.get( "*" );
        }
        if ( entry == null )
        {
            faultCode = status;
            faultReason = statusText;
        }
        else
        {
            faultCode = entry.getAttributeValue( "fault-code", status );
            faultReason = entry.getAttributeValue( "fault-reason", statusText );
        }
        codeEl.setText( faultCode );
        rootEl.addContent( codeEl );
        Element reasonEl = new Element( "fault-reason" );
        reasonEl.setText( faultReason );
        rootEl.addContent( reasonEl );
        Document result = new Document( rootEl );

        return result;
    }
}