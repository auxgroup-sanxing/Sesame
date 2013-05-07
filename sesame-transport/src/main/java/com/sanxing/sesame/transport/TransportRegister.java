package com.sanxing.sesame.transport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.binding.transport.TransportFactory;
import com.sanxing.sesame.sharelib.ShareLibCallback;

public class TransportRegister
    implements ShareLibCallback
{
    private static Logger LOG = LoggerFactory.getLogger( TransportRegister.class );

    private final Map<String, Class<? extends Transport>> registered = new HashMap();

    @Override
    public void onDispose( File installationDir )
    {
        Set<String> schemes = registered.keySet();
        for ( String scheme : schemes )
        {
            Class clazz = registered.get( scheme );
            LOG.debug( "Deregister transport: " + scheme + " -> " + clazz.getName() );
            TransportFactory.unregister( scheme, clazz );
        }
    }

    @Override
    public void onInstall( File installationDir )
    {
        try
        {
            File[] list = installationDir.listFiles();
            for ( File path : list )
            {
                if ( ( path.isFile() ) && ( path.getName().endsWith( ".jar" ) ) )
                {
                    JarFile jar = new JarFile( path );
                    jar.getManifest().getAttributes( "" );
                    try
                    {
                        JarEntry entry = jar.getJarEntry( "META-INF/schemes" );
                        if ( entry == null )
                        {
                            continue;
                        }
                        InputStream input = jar.getInputStream( entry );
                        Properties properties = new Properties();
                        properties.load( input );
                        Enumeration keys = properties.keys();
                        while ( keys.hasMoreElements() )
                        {
                            String key = (String) keys.nextElement();
                            String value = properties.getProperty( key );
                            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                            Class clazz = classloader.loadClass( value ).asSubclass( Transport.class );
                            LOG.debug( "Register transport: " + key + " -> " + clazz.getName() );
                            TransportFactory.register( key, clazz );
                            registered.put( key, clazz );
                        }
                    }
                    finally
                    {
                        jar.close();
                    }
                }
            }
        }
        catch ( ClassNotFoundException e )
        {
            LOG.debug( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            LOG.debug( e.getMessage(), e );
        }
    }
}