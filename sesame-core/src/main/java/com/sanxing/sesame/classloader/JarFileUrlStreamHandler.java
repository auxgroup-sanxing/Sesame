package com.sanxing.sesame.classloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileUrlStreamHandler
    extends URLStreamHandler
{
    private URL expectedUrl;

    private JarFile jarFile = null;

    private JarEntry jarEntry = null;

    public static URL createUrl( JarFile jarFile, JarEntry jarEntry )
        throws MalformedURLException
    {
        return createUrl( jarFile, jarEntry, new File( jarFile.getName() ).toURL() );
    }

    public static URL createUrl( JarFile jarFile, JarEntry jarEntry, URL codeSource )
        throws MalformedURLException
    {
        JarFileUrlStreamHandler handler = new JarFileUrlStreamHandler( jarFile, jarEntry );
        URL url = new URL( "jar", "", -1, codeSource + "!/" + jarEntry.getName(), handler );
        handler.setExpectedUrl( url );
        return url;
    }

    public JarFileUrlStreamHandler()
    {
    }

    public JarFileUrlStreamHandler( JarFile jarFile, JarEntry jarEntry )
    {
        if ( jarFile == null )
        {
            throw new NullPointerException( "jarFile is null" );
        }
        if ( jarEntry == null )
        {
            throw new NullPointerException( "jarEntry is null" );
        }

        this.jarFile = jarFile;
        this.jarEntry = jarEntry;
    }

    public void setExpectedUrl( URL expectedUrl )
    {
        if ( expectedUrl == null )
        {
            throw new NullPointerException( "expectedUrl is null" );
        }
        this.expectedUrl = expectedUrl;
    }

    @Override
    public URLConnection openConnection( URL url )
        throws IOException
    {
        if ( ( expectedUrl == null ) || ( !( expectedUrl.equals( url ) ) ) )
        {
            if ( !( url.getProtocol().equals( "jar" ) ) )
            {
                throw new IllegalArgumentException( "Unsupported protocol " + url.getProtocol() );
            }

            String path = url.getPath();
            String[] chunks = path.split( "!/", 2 );

            if ( chunks.length == 1 )
            {
                throw new MalformedURLException( "Url does not contain a '!' character: " + url );
            }

            String file = chunks[0];
            String entryPath = chunks[1];

            if ( !( file.startsWith( "file:" ) ) )
            {
                return new URL( url.toExternalForm() ).openConnection();
            }
            file = file.substring( "file:".length() );

            File f = new File( file );
            if ( f.exists() )
            {
                jarFile = new JarFile( f );
            }

            if ( jarFile == null )
            {
                throw new FileNotFoundException( "Cannot find JarFile: " + file );
            }

            jarEntry = jarFile.getJarEntry( entryPath );
            if ( jarEntry == null )
            {
                throw new FileNotFoundException( "Entry not found: " + url );
            }
            expectedUrl = url;
        }

        return new JarFileUrlConnection( url, jarFile, jarEntry );
    }
}