package com.sanxing.sesame.codec.impl;

import java.io.InputStream;

import org.jdom.input.SAXBuilder;

import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLResult;

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
            if ( source.getReader() != null )
            {
                result.setDocument( getSAXBuilder().build( source.getReader() ) );
                return;
            }
            InputStream stream = source.getInputStream();
            if ( stream != null )
            {
                result.setDocument( getSAXBuilder().build( stream ) );
                return;
            }

            result.setDocument( getSAXBuilder().build( source.getSystemId() ) );
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }
}