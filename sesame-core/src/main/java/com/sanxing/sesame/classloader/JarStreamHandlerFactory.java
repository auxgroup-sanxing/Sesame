package com.sanxing.sesame.classloader;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class JarStreamHandlerFactory
    implements URLStreamHandlerFactory
{
    @Override
    public URLStreamHandler createURLStreamHandler( String protocol )
    {
        if ( "jar".equalsIgnoreCase( protocol ) )
        {
            return new JarFileUrlStreamHandler();
        }
        return null;
    }
}