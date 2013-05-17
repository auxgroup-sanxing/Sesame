package com.sanxing.adp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XJUtil
{
    public static final String STR_XSNS = "http://schemas.xmlsoap.org/soap/envelope/";

    public static final Namespace XSNS = Namespace.getNamespace( "http://schemas.xmlsoap.org/soap/envelope/" );

    private static Logger LOG = LoggerFactory.getLogger( XJUtil.class );

    static List<String> primitives = new LinkedList();

    static Map<String, String> xs_java_map;

    static
    {
        primitives.add( "String" );
        primitives.add( "Float" );
        primitives.add( "Double" );
        primitives.add( "Integer" );
        primitives.add( "java.math.BigDecimal" );
        primitives.add( "java.util.Date" );
        primitives.add( "Long" );
        primitives.add( "Boolean" );

        xs_java_map = new HashMap();

        xs_java_map.put( "string", "String" );
        xs_java_map.put( "float", "Float" );
        xs_java_map.put( "int", "Integer" );
        xs_java_map.put( "integer", "Integer" );
        xs_java_map.put( "double", "Double" );
        xs_java_map.put( "boolean", "Boolean" );
        xs_java_map.put( "decimal", "java.math.BigDecimal" );
        xs_java_map.put( "long", "Long" );
        xs_java_map.put( "date", "java.util.Date" );
        xs_java_map.put( "dateTime", "java.util.Date" );
        xs_java_map.put( "time", "java.util.Date" );
    }

    public static String ns2package( String nsUri )
    {
        int idx = nsUri.indexOf( 58 );
        String scheme = "";
        if ( idx >= 0 )
        {
            scheme = nsUri.substring( 0, idx );
            if ( ( scheme.equalsIgnoreCase( "http" ) ) || ( scheme.equalsIgnoreCase( "urn" ) ) )
            {
                nsUri = nsUri.substring( idx + 1 );
            }
        }

        ArrayList tokens = tokenize( nsUri, "/: " );
        if ( tokens.size() == 0 )
        {
            return null;
        }

        if ( tokens.size() > 1 )
        {
            String lastToken = (String) tokens.get( tokens.size() - 1 );
            idx = lastToken.lastIndexOf( 46 );
            if ( idx > 0 )
            {
                lastToken = lastToken.substring( 0, idx );
                tokens.set( tokens.size() - 1, lastToken );
            }

        }

        String domain = (String) tokens.get( 0 );
        idx = domain.indexOf( 58 );
        if ( idx >= 0 )
        {
            domain = domain.substring( 0, idx );
        }
        ArrayList r = reverse( tokenize( domain, ( scheme.equals( "urn" ) ) ? ".-" : "." ) );
        if ( ( (String) r.get( r.size() - 1 ) ).equalsIgnoreCase( "www" ) )
        {
            r.remove( r.size() - 1 );
        }

        tokens.addAll( 1, r );
        tokens.remove( 0 );

        for ( int i = 0; i < tokens.size(); ++i )
        {
            String token = (String) tokens.get( i );
            token = removeIllegalIdentifierChars( token );

            if ( !( NameUtil.isJavaIdentifier( token.toLowerCase() ) ) )
            {
                token = '_' + token;
            }

            tokens.set( i, token.toLowerCase() );
        }

        return combine( tokens, '.' );
    }

    private static String removeIllegalIdentifierChars( String token )
    {
        StringBuffer newToken = new StringBuffer();
        for ( int i = 0; i < token.length(); ++i )
        {
            char c = token.charAt( i );

            if ( ( i == 0 ) && ( !( Character.isJavaIdentifierStart( c ) ) ) )
            {
                newToken.append( '_' ).append( c );
            }
            else if ( !( Character.isJavaIdentifierPart( c ) ) )
            {
                newToken.append( '_' );
            }
            else
            {
                newToken.append( c );
            }
        }
        return newToken.toString();
    }

    private static ArrayList<String> tokenize( String str, String sep )
    {
        StringTokenizer tokens = new StringTokenizer( str, sep );
        ArrayList r = new ArrayList();

        while ( tokens.hasMoreTokens() )
        {
            r.add( tokens.nextToken() );
        }
        return r;
    }

    private static <T> ArrayList<T> reverse( List<T> a )
    {
        ArrayList r = new ArrayList();

        for ( int i = a.size() - 1; i >= 0; --i )
        {
            r.add( a.get( i ) );
        }
        return r;
    }

    private static String combine( List r, char sep )
    {
        StringBuilder buf = new StringBuilder( r.get( 0 ).toString() );

        for ( int i = 1; i < r.size(); ++i )
        {
            buf.append( sep );
            buf.append( r.get( i ) );
        }

        return buf.toString();
    }

    public static String ns2ClassName( QName ns )
    {
        return ns2package( ns.getNamespaceURI() ) + "." + StringUtils.capitalize( ns.getLocalPart() );
    }

    public static boolean isPrimitive( String javaType )
    {
        return primitives.contains( javaType );
    }

    public static String xsType2Java( String xsType )
    {
        String javaType = xs_java_map.get( xsType );
        if ( javaType == null )
        {
            throw new RuntimeException( "un support xs type[xs:" + xsType + "]" );
        }
        return javaType;
    }

    public static Object xmlPrimitiv2Java( String javaType, String xsType, String param )
        throws ParseException
    {
        Object paramObj = null;
        if ( javaType.equals( "String" ) )
        {
            paramObj = param;
        }
        else if ( javaType.equals( "Integer" ) )
        {
            paramObj = Integer.valueOf( Integer.parseInt( param ) );
        }
        else if ( javaType.equals( "Double" ) )
        {
            paramObj = Double.valueOf( Double.parseDouble( param ) );
        }
        else if ( javaType.equals( "Boolean" ) )
        {
            paramObj = Boolean.valueOf( Boolean.parseBoolean( param ) );
        }
        else if ( javaType.equals( "Long" ) )
        {
            paramObj = Long.valueOf( Long.parseLong( param ) );
        }
        else if ( javaType.endsWith( "Date" ) )
        {
            if ( xsType.equals( "date" ) )
            {
                try
                {
                    paramObj = new SimpleDateFormat( "yyyy-MM-dd" ).parse( param );
                }
                catch ( ParseException e )
                {
                    LOG.error( "xmlPrimitiv2Java err ", e );
                    throw e;
                }
            }
            else if ( xsType.equals( "dateTime" ) )
            {
                try
                {
                    String strDate = param.replace( "T", "" );
                    paramObj = new SimpleDateFormat( "yyyy-MM-ddhh:mm:ss" ).parse( strDate );
                }
                catch ( ParseException e )
                {
                    LOG.error( "xmlPrimitiv2Java err ", e );
                    throw e;
                }
            }
        }
        return paramObj;
    }
}