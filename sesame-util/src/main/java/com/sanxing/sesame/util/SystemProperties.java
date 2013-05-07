package com.sanxing.sesame.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.exceptions.ErrMessages;

public class SystemProperties
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemProperties.class );

    private static SystemProperties _instance = new SystemProperties();

    public static final String TMP_DIR = "java.io.tmpdir";

    private final Map<String, String> _props = new ConcurrentHashMap();

    public static String get( String key, String defaultValue )
    {
        return GetterUtil.get( get( key ), defaultValue );
    }

    public static String get( String key )
    {
        String value = _instance._props.get( key );

        if ( value == null )
        {
            value = System.getProperty( key );
        }

        return value;
    }

    public static String[] getArray( String key )
    {
        String value = get( key );

        if ( value == null )
        {
            return new String[0];
        }
        return StringUtil.split( value );
    }

    private SystemProperties()
    {
        Properties sys = System.getProperties();
        Enumeration keys = sys.keys();
        while ( keys.hasMoreElements() )
        {
            String key = (String) keys.nextElement();
            _props.put( key, sys.getProperty( key ) );
        }
        String defaultFileName = System.getProperty( "sesame.application.properties", "/conf/application.properties" );
        parsePropertyFile( defaultFileName );

        String appFiles = GetterUtil.get( _props.get( "sesame.application.ext.properties" ), "" );

        String[] fileNames = appFiles.split( "," );
        for ( String file : fileNames )
        {
            parsePropertyFile( file );
        }
    }

    private void parsePropertyFile( String fileName )
    {
        LOG.info( "parsing property file [" + fileName + "]" );
        try
        {
            InputStream input;
            if ( fileName.startsWith( "$classpath" ) )
            {
                fileName = fileName.substring( 11 );
                input = ErrMessages.class.getClassLoader().getResourceAsStream( fileName );
            }
            else
            {
                input = new FileInputStream( new File( System.getProperty( "SESAME_HOME" ), fileName ) );
            }

            Properties properites = new Properties();

            properites.load( input );
            Enumeration enumer = properites.propertyNames();

            while ( enumer.hasMoreElements() )
            {
                String key = (String) enumer.nextElement();
                String value = properites.getProperty( key );
                if ( System.getProperty( "sesame.property.overide.sequence", "last" ).equals( "last" ) )
                {
                    _props.put( key, value );
                }
                else if ( !( _props.containsKey( key ) ) )
                {
                    _props.put( key, value );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Load app propertie failed! [" + fileName + "]" );
        }
    }
}