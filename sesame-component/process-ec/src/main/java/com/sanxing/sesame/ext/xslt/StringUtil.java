/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.ext.xslt;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author ShangjieZhou
 */
public class StringUtil
{
    public static String format( String format, double arg )
    {
        return String.format( format, new Object[] { Double.valueOf( arg ) } );
    }

    public static String format( String format, int arg )
    {
        return String.format( format, new Object[] { Integer.valueOf( arg ) } );
    }

    public static String format( String format, String arg )
    {
        return String.format( format, new Object[] { arg } );
    }

    public static String replace( String str, String regex, String replacement )
    {
        if ( str == null )
        {
            return null;
        }

        return str.replaceAll( regex, replacement );
    }

    public static String div100( String str )
    {
        if ( str == null )
        {
            return null;
        }
        return String.format( "%.2f", new Object[] { Float.valueOf( Integer.parseInt( str ) / 100.0F ) } );
    }

    public static String div100( double real )
    {
        return div100( new Double( real ).intValue() );
    }
    
    public static String fromJson( String json, String expression )
    {
        JSONObject object = JSONObject.fromObject( json );
        String[] properties = expression.split( "\\." );
        Object value = null;
        for ( int i = 0; i < properties.length; i++ )
        {
            if ( object == null)
            {
                break;
            }
            String key = properties[i];
            if ( key.endsWith( "]" ) )
            {
                int index = key.indexOf( "[" );
                String[] idxs = key.substring( index + 1 ).split( "\\[" );
                key = key.substring( 0, index );
                JSONArray array = object.getJSONArray( key );
                for ( int j = 0; j < idxs.length; j++ )
                {
                    index = Integer.parseInt( idxs[j].substring( 0, idxs[j].indexOf( "]" ) ) );
                    value = array.get( index );
                    if ( j < idxs.length - 1 )
                    {
                        array = array.getJSONArray( index );
                    }
                }
            }
            else
            {
                value = object.get( key );
            }
            if ( i < properties.length - 1)
            {
                object = (JSONObject) value;
            }
        }
        return value == null ? "" : value.toString();
    }
}
