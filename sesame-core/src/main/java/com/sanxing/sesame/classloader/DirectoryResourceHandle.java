package com.sanxing.sesame.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class DirectoryResourceHandle
    extends AbstractResourceHandle
{
    private final String name;

    private final File file;

    private final Manifest manifest;

    private final URL url;

    private final URL codeSource;

    public DirectoryResourceHandle( String name, File file, File codeSource, Manifest manifest )
        throws MalformedURLException
    {
        this.name = name;
        this.file = file;
        this.codeSource = codeSource.toURL();
        this.manifest = manifest;
        url = file.toURL();
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public URL getUrl()
    {
        return url;
    }

    @Override
    public URL getCodeSourceUrl()
    {
        return codeSource;
    }

    @Override
    public boolean isDirectory()
    {
        return file.isDirectory();
    }

    @Override
    public InputStream getInputStream()
        throws IOException
    {
        if ( file.isDirectory() )
        {
            return new IoUtil.EmptyInputStream();
        }
        return new FileInputStream( file );
    }

    @Override
    public int getContentLength()
    {
        if ( ( file.isDirectory() ) || ( file.length() > 2147483647L ) )
        {
            return -1;
        }
        return (int) file.length();
    }

    @Override
    public Manifest getManifest()
        throws IOException
    {
        return manifest;
    }

    @Override
    public Attributes getAttributes()
        throws IOException
    {
        if ( manifest == null )
        {
            return null;
        }
        return manifest.getAttributes( getName() );
    }

    @Override
    public Certificate[] getCertificates()
    {
        return null;
    }
}