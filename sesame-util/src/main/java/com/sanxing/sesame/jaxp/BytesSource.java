package com.sanxing.sesame.jaxp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.transform.stream.StreamSource;

public class BytesSource
    extends StreamSource
{
    private final byte[] data;

    public BytesSource( byte[] data )
    {
        this.data = data;
    }

    public BytesSource( byte[] data, String systemId )
    {
        this.data = data;
        setSystemId( systemId );
    }

    @Override
    public InputStream getInputStream()
    {
        return new ByteArrayInputStream( data );
    }

    @Override
    public Reader getReader()
    {
        return new InputStreamReader( getInputStream() );
    }

    public byte[] getData()
    {
        return data;
    }

    @Override
    public String toString()
    {
        return "BytesSource[" + new String( data ) + "]";
    }
}