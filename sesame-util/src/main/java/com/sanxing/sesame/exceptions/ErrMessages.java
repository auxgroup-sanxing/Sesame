package com.sanxing.sesame.exceptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.util.StringUtil;
import com.sanxing.sesame.util.SystemProperties;

public class ErrMessages
{
    private static final Logger LOG = LoggerFactory.getLogger( ErrMessages.class );

    private static Map<String, Map<String, String>> sections = new HashMap();

    static
    {
        parseMsgFile( "base_err.msg" );
        String temp = SystemProperties.get( "sesame.msg.err.files", "" );
        String[] msgFile = temp.split( "," );
        for ( String fileName : msgFile )
        {
            if ( fileName.equals( "" ) )
            {
                continue;
            }
            parseMsgFile( fileName );
        }
    }

    protected static void addErrorMsgFile( String fileName )
    {
        parseMsgFile( fileName );
    }

    private static void parseMsgFile( String fileName )
    {
        try
        {
            LOG.info( "parsing err msg " + fileName );
            File configFile = new File( System.getProperty( "SESAME_HOME" ) + "/conf/" + fileName );
            InputStream input;
            if ( ( configFile.exists() ) && ( configFile.isFile() ) )
            {
                input = new FileInputStream( configFile );
                LOG.info( "got msg file at sesame home" );
            }
            else
            {
                input = Thread.currentThread().getContextClassLoader().getResourceAsStream( fileName );
                LOG.info( "got msg file at classpath" );
            }
            if ( input == null )
            {
                LOG.error( "failt to find file " + fileName );
            }
            BufferedReader reader = new BufferedReader( new InputStreamReader( input, "UTF-8" ) );

            Map section = null;
            String line;
            while ( ( line = reader.readLine() ) != null )
            {
                String sectionName = "";

                if ( !( line.trim().equals( "" ) ) )
                {
                    if ( line.startsWith( "[" ) )
                    {
                        section = new HashMap();
                        sectionName = line.substring( 1, line.indexOf( "]" ) );
                        LOG.debug( "parse section [" + sectionName + "]" );
                        sections.put( sectionName, section );
                    }
                    else if ( line.trim().startsWith( "#" ) )
                    {
                        LOG.debug( "comment .." + line );
                    }
                    else
                    {
                        String[] temp = StringUtil.split( line, "=" );
                        String errCode = temp[0];
                        String errMsg = temp[1];
                        if ( LOG.isTraceEnabled() )
                        {
                            LOG.trace( "error code [" + errCode + "] err msg [" + errMsg + "]" );
                        }
                        section.put( errCode, errMsg );
                    }
                }
            }
        }
        catch ( IOException e )
        {
            LOG.error( "fail to parse err msg file [" + fileName + "]", e );
        }
    }

    public static String getErrMsg( String sectionName, String errKey )
    {
        Map section = sections.get( sectionName );

        if ( section != null )
        {
            return ( (String) section.get( errKey ) );
        }
        return "undefined err msg";
    }

    public static String getErrMsg( String sectionName, String errKey, String[] args )
    {
        String temp = getErrMsg( sectionName, errKey );
        for ( int i = 0; i < args.length; ++i )
        {
            temp = StringUtil.replace( temp, "${" + i + "}", args[i] );
        }
        return temp;
    }
}