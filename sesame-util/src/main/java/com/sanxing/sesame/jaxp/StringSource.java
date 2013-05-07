package com.sanxing.sesame.jaxp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.stream.StreamSource;

public class StringSource
    extends StreamSource
    implements Serializable
{
    private static final long serialVersionUID = -703716493720900L;

    private final String text;

    private String encoding;

    public StringSource( String text )
    {
        encoding = "UTF-8";

        if ( text == null )
        {
            throw new NullPointerException( "text can not be null" );
        }
        this.text = text;
    }

    public StringSource( String text, String systemId )
    {
        this( text );
        setSystemId( systemId );
    }

    public StringSource( String text, String systemId, String encoding )
    {
        this.encoding = "UTF-8";

        this.text = text;
        this.encoding = encoding;
        setSystemId( systemId );
    }

    @Override
    public InputStream getInputStream()
    {
        try
        {
            return new ByteArrayInputStream( text.getBytes( encoding ) );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Reader getReader()
    {
        return new StringReader( text );
    }

    @Override
    public String toString()
    {
        return "StringSource[" + text + "]";
    }

    public String getText()
    {
        return text;
    }
}