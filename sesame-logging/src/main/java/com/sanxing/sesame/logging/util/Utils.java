package com.sanxing.sesame.logging.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class Utils
{
    public static String dateToDateShort( java.util.Date date )
    {
        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        String dateString = formatter.format( date );
        return dateString;
    }

    public static String dateToTimeShort( java.util.Date date )
    {
        SimpleDateFormat formatter = new SimpleDateFormat( "HH:mm:ss:SSS" );
        String dateString = formatter.format( date );
        return dateString;
    }

    public static Timestamp dateToTimeStamp( java.util.Date date )
    {
        java.sql.Date time = new java.sql.Date( date.getTime() );
        Timestamp timestamp = new Timestamp( time.getTime() );
        return timestamp;
    }

    public static Document stringToXml( String buffer )
    {
        try
        {
            ByteArrayInputStream bais = null;
            bais = new ByteArrayInputStream( buffer.getBytes( "UTF-8" ) );
            SAXBuilder saxBuilder = new SAXBuilder();
            Document doc = saxBuilder.build( bais );
            return doc;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] splitStringByLength( String source, int length )
    {
        if ( ( source == null ) || ( source.length() == 0 ) )
        {
            return null;
        }
        int capacity = source.length() / length + 1;
        String[] array = new String[capacity];
        int beginIndex = 0;
        int endIndex = length;
        for ( int i = 0; i < capacity; ++i )
        {
            if ( endIndex > source.length() )
            {
                array[i] = source.substring( beginIndex );
            }
            else
            {
                array[i] = source.substring( beginIndex, endIndex );
            }
            beginIndex += length;
            endIndex += length;
        }
        return array;
    }

    public static void writeFile( File file, String content, String encoding )
    {
        try
        {
            BufferedWriter out = null;
            if ( !( file.exists() ) )
            {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream( file, true );
            out = new BufferedWriter( new OutputStreamWriter( fos, encoding ) );

            out.write( content );
            out.newLine();
            out.flush();
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( UnsupportedEncodingException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    public static Object getComponentByName( Object f, String name )
    {
        Field[] fields = f.getClass().getDeclaredFields();
        int i = 0;
        for ( int len = fields.length; i < len; ++i )
        {
            String varName = fields[i].getName();
            if ( !( varName.equalsIgnoreCase( name ) ) )
            {
                continue;
            }
            try
            {
                boolean accessFlag = fields[i].isAccessible();
                fields[i].setAccessible( true );
                Object o = fields[i].get( f );
                fields[i].setAccessible( accessFlag );
                return o;
            }
            catch ( IllegalArgumentException ex )
            {
                ex.printStackTrace();
            }
            catch ( IllegalAccessException ex )
            {
                ex.printStackTrace();
            }
        }
        return null;
    }
}