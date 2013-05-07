package com.sanxing.sesame.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.activation.DataSource;

public class ByteArrayDataSource
    implements DataSource, Serializable
{
    private static final long serialVersionUID = 1L;

    private final byte[] data;

    private final String type;

    private String name = "unused";

    public ByteArrayDataSource( byte[] data, String type )
    {
        this.data = data;
        this.type = type;
    }

    @Override
    public InputStream getInputStream()
        throws IOException
    {
        if ( data == null )
        {
            throw new IOException( "no data" );
        }
        return new ByteArrayInputStream( data );
    }

    @Override
    public OutputStream getOutputStream()
        throws IOException
    {
        throw new IOException( "getOutputStream() not supported" );
    }

    @Override
    public String getContentType()
    {
        return type;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}