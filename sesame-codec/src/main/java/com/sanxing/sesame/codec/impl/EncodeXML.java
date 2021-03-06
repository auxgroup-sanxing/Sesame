package com.sanxing.sesame.codec.impl;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.Encoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLSource;

public class EncodeXML
    implements Encoder
{
    private static XMLOutputter outputter = new XMLOutputter();

    @Override
    public void destroy()
    {
    }

    @Override
    public void init( String workspaceRoot )
    {
    }

    @Override
    public void encode( XMLSource source, BinaryResult result )
        throws FormatException
    {
        if ( result.getEncoding() == null )
        {
            throw new FormatException( "charset not specified" );
        }
        try
        {
            Document message = source.getJDOMDocument();
            Format newFormat = Format.getRawFormat().setEncoding( result.getEncoding() );
            outputter.setFormat( newFormat );
            outputter.output( message, result.getOutputStream() );
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }
}