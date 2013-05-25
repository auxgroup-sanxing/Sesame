package com.sanxing.sesame.codec.impl;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLResult;
import com.sanxing.sesame.util.JdomUtil;

public class DecodeXML
    implements Decoder
{
    private final ThreadLocal<SAXBuilder> localBuilder = new ThreadLocal();

    @Override
    public void destroy()
    {
    }

    @Override
    public void init( String workspaceRoot )
    {
    }

    private SAXBuilder getSAXBuilder()
    {
        SAXBuilder builder = localBuilder.get();
        if ( builder == null )
        {
            localBuilder.set( builder = new SAXBuilder() );
            localBuilder.get().setFastReconfigure( true );
        }
        return builder;
    }

    @Override
    public void decode( BinarySource source, XMLResult result )
        throws FormatException
    {
        try
        {
            Document content = null;
            if ( source.getReader() != null )
            {
                content = getSAXBuilder().build( source.getReader() );
            }
            else if ( source.getInputStream() != null )
            {
                content = getSAXBuilder().build( source.getInputStream() );
            }
            else
            {
                content = getSAXBuilder().build( source.getSystemId() );
            }
            Element root = content.getRootElement();
            root.setNamespace( Namespace.NO_NAMESPACE );
            JdomUtil.allAdditionNamespace( root, Namespace.NO_NAMESPACE );
            result.setDocument( content );
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }
}