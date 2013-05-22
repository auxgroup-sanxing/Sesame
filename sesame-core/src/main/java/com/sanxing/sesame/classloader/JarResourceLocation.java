package com.sanxing.sesame.classloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

public class JarResourceLocation
    extends AbstractUrlResourceLocation
{
    private JarFile jarFile;

    private byte[] content;

    public JarResourceLocation( URL codeSource, File cacheFile )
        throws IOException
    {
        super( codeSource );
        try
        {
            jarFile = new JarFile( cacheFile );
        }
        catch ( ZipException ze )
        {
            InputStream is = null;
            try
            {
                is = new FileInputStream( cacheFile );
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int bytesRead = -1;
                while ( ( bytesRead = is.read( buffer ) ) != -1 )
                {
                    baos.write( buffer, 0, bytesRead );
                }
                content = baos.toByteArray();
            }
            finally
            {
                if ( is != null )
                {
                    is.close();
                }
            }
        }
    }

    @Override
    public ResourceHandle getResourceHandle( String resourceName )
    {
        if ( jarFile != null )
        {
            JarEntry jarEntry = jarFile.getJarEntry( resourceName );
            if ( jarEntry != null )
            {
                try 
                {
                    return new JarResourceHandle( jarFile, jarEntry, getCodeSource() );
                } catch (MalformedURLException localMalformedURLException) {
                }
            }
        }
        else if ( content != null )
        {
            try
            {
                JarInputStream is = new JarInputStream( new ByteArrayInputStream( content ) );
                JarEntry jarEntry;
                while ( ( jarEntry = is.getNextJarEntry() ) != null )
                {
                    if ( jarEntry.getName().equals( resourceName ) )
                    {
                        return new JarEntryResourceHandle( jarEntry, is );
                    }
                }
            }
            catch ( IOException e )
            {
            }
        }
        return null;
    }

    @Override
    public Manifest getManifest()
        throws IOException
    {
        if ( jarFile != null )
        {
            return jarFile.getManifest();
        }
        try
        {
            JarInputStream is = new JarInputStream( new ByteArrayInputStream( content ) );
            return is.getManifest();
        }
        catch ( IOException e )
        {
        }
        return null;
    }

    @Override
    public void close()
    {
        if ( jarFile != null )
        {
            IoUtil.close( jarFile );
        }
    }

    private class JarEntryResourceHandle
        extends AbstractResourceHandle
    {
        private final JarEntry jarEntry;

        private final InputStream is;

        public JarEntryResourceHandle( JarEntry jarEntry, InputStream inputStream )
        {
            this.jarEntry = jarEntry;
            is = inputStream;
        }

        @Override
        public String getName()
        {
            return jarEntry.getName();
        }

        @Override
        public URL getUrl()
        {
            try
            {
                return new URL( "jar", "", -1, getCodeSource() + "!/" + jarEntry.getName() );
            }
            catch ( MalformedURLException e )
            {
                throw new RuntimeException( e );
            }
        }

        @Override
        public boolean isDirectory()
        {
            return jarEntry.isDirectory();
        }

        @Override
        public URL getCodeSourceUrl()
        {
            return getCodeSource();
        }

        @Override
        public InputStream getInputStream()
            throws IOException
        {
            return is;
        }

        @Override
        public int getContentLength()
        {
            return (int) jarEntry.getSize();
        }
    }
}