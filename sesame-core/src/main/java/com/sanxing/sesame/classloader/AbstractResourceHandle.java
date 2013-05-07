package com.sanxing.sesame.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public abstract class AbstractResourceHandle
    implements ResourceHandle
{
    @Override
    public byte[] getBytes()
        throws IOException
    {
        InputStream in = getInputStream();
        try
        {
            byte[] bytes = IoUtil.getBytes( in );
            return bytes;
        }
        finally
        {
            IoUtil.close( in );
        }
    }

    @Override
    public Manifest getManifest()
        throws IOException
    {
        return null;
    }

    @Override
    public Certificate[] getCertificates()
    {
        return null;
    }

    @Override
    public Attributes getAttributes()
        throws IOException
    {
        Manifest m = getManifest();
        if ( m == null )
        {
            return null;
        }

        String entry = getUrl().getFile();
        return m.getAttributes( entry );
    }

    @Override
    public void close()
    {
    }

    @Override
    public String toString()
    {
        return "[" + getName() + ": " + getUrl() + "; code source: " + getCodeSourceUrl() + "]";
    }
}