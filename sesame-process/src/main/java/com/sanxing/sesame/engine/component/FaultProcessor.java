package com.sanxing.sesame.engine.component;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jbi.messaging.Fault;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import com.sanxing.sesame.constants.ExchangeConst;
import com.sanxing.sesame.exceptions.AppException;
import com.sanxing.sesame.exceptions.SystemException;
import com.sanxing.sesame.util.JdomUtil;

public class FaultProcessor
{
    private final Map<QName, FaultTransformer> transformers = new HashMap();

    private FaultTransformer getTransformer( String sourceId, String targetId )
    {
        return transformers.get( new QName( targetId, sourceId ) );
    }

    public boolean hasMapping( String sourceId, String targetId )
    {
        if ( ( sourceId == null ) || ( targetId == null ) )
        {
            return false;
        }

        FaultTransformer transformer = getTransformer( sourceId, targetId );
        return ( ( transformer != null ) && ( transformer.hasEntry() ) );
    }

    public Exception processException( String sourceId, String targetId, Exception exception )
        throws Exception
    {
        FaultTransformer transformer = getTransformer( sourceId, targetId );
        if ( transformer == null )
        {
            throw new Exception( "Transformer not found" );
        }

        String status = null;
        String statusText = null;
        if ( exception instanceof AppException )
        {
            status = ( (AppException) exception ).getGlobalErrCode();
            statusText = ( (AppException) exception ).getMessage();
        }
        else if ( exception instanceof SystemException )
        {
            status = ( (SystemException) exception ).getGlobalErrCode();
            statusText = ( (SystemException) exception ).getMessage();
        }

        Document result = transformer.transform( status, statusText );
        Element rootEl = result.getRootElement();
        String faultCode = rootEl.getChildText( "fault-code", rootEl.getNamespace() );
        String faultReason = rootEl.getChildText( "fault-reason", rootEl.getNamespace() );
        if ( exception instanceof AppException )
        {
            ( (AppException) exception ).setErrorCode( faultCode );
        }
        else if ( exception instanceof SystemException )
        {
            ( (SystemException) exception ).setErrorCode( faultCode );
        }
        else
        {
            exception = new RuntimeException( faultCode + "|" + faultReason );
        }
        return exception;
    }

    public Document processMessage( String sourceId, String targetId, Fault sourceFault )
        throws Exception
    {
        FaultTransformer transformer = getTransformer( sourceId, targetId );
        if ( transformer == null )
        {
            throw new Exception( "Transformer not found" );
        }

        String status = null;
        String statusText = null;
        Source source = sourceFault.getContent();
        Document sourceDoc = JdomUtil.source2JDOMDocument( source );

        XPath status_XPath = (XPath) sourceFault.getProperty( ExchangeConst.STATUS_XPATH );
        if ( status_XPath != null )
        {
            status = status_XPath.valueOf( sourceDoc );
        }
        XPath statusText_XPath = (XPath) sourceFault.getProperty( ExchangeConst.STATUS_TEXT_XPATH );
        if ( statusText_XPath != null )
        {
            statusText = statusText_XPath.valueOf( sourceDoc );
        }

        Document outputDoc = transformer.transform( status, statusText );
        return outputDoc;
    }

    public void init( File installRoot )
        throws JDOMException, IOException
    {
        transformers.clear();
        SAXBuilder builder = new SAXBuilder();

        FilenameFilter filter = new FilenameFilter()
        {
            @Override
            public boolean accept( File dir, String name )
            {
                return name.endsWith( ".fault" );
            }
        };
        File[] files = installRoot.listFiles( filter );

        for ( File file : files )
        {
            String targetId = file.getName().replaceFirst( "\\.fault$", "" );
            Document doc = builder.build( file );
            Element rootEl = doc.getRootElement();
            List list = rootEl.getChildren( "template", rootEl.getNamespace() );
            while ( list.size() > 0 )
            {
                Element templateEl = (Element) list.get( 0 );
                templateEl.detach();

                String sourceId = templateEl.getAttributeValue( "match" );

                QName qname = new QName( targetId, sourceId );
                Document document = new Document( templateEl );

                FaultTransformer transformer = new FaultTransformer( document );
                transformers.put( qname, transformer );
            }
        }
    }
}