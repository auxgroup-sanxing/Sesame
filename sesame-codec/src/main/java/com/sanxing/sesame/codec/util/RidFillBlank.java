package com.sanxing.sesame.codec.util;

import java.io.UnsupportedEncodingException;

import com.sanxing.sesame.binding.codec.FormatException;

public class RidFillBlank
{
    public static String ridRightBlank( byte[] buf, int blank, String type, String charset )
        throws FormatException
    {
        byte binaryZero = 0;
        String result = null;

        if ( type.equals( "string" ) )
        {
            for ( int i = buf.length - 1; i >= 0; --i )
            {
                if ( ( ( blank ^ buf[i] ) == 0 ) || ( ( binaryZero ^ buf[i] ) == 0 ) )
                {
                    continue;
                }
                try
                {
                    result = new String( buf, 0, i + 1, charset );
                }
                catch ( UnsupportedEncodingException e )
                {
                    throw new FormatException( e.getMessage(), e );
                }
            }

        }
        else
        {
            for ( int i = buf.length - 1; i >= 0; --i )
            {
                if ( ( ( blank ^ buf[i] ) == 0 ) && ( buf.length != 1 ) )
                {
                    continue;
                }
                try
                {
                    if ( "hexBinary".equals( type ) )
                    {
                        byte[] temp2 = new byte[i + 1];
                        System.arraycopy( buf, 0, temp2, 0, i + 1 );

                        result = HexBinary.encode( temp2 );
                    }
                    else
                    {
                        result = new String( buf, 0, i + 1, charset );
                    }
                }
                catch ( UnsupportedEncodingException e )
                {
                    throw new FormatException( e.getMessage(), e );
                }
            }

        }

        return ( ( result == null ) ? "" : result );
    }

    public static String ridLeftBlank( byte[] buf, int blank, String type, String charset )
        throws FormatException
    {
        String result = null;
        for ( int i = 0; i < buf.length; ++i )
        {
            if ( ( blank ^ buf[i] ) == 0 )
            {
                continue;
            }
            try
            {
                if ( "hexBinary".equals( type ) )
                {
                    byte[] temp2 = new byte[buf.length - i];
                    System.arraycopy( buf, i, temp2, 0, temp2.length );

                    result = HexBinary.encode( temp2 );
                }
                else
                {
                    result = new String( buf, i, buf.length - i, charset );
                }

            }
            catch ( UnsupportedEncodingException e )
            {
                throw new FormatException( e.getMessage(), e );
            }

        }

        label106: return ( ( result == null ) ? "" : result );
    }

    public static String fillBlank( String elementValue, int length, char blank, String align, String encodeCharset,
                                    String elementName )
        throws FormatException
    {
        StringBuffer sb = new StringBuffer();
        int len = 0;
        if ( elementValue != null )
        {
            try
            {
                len = elementValue.getBytes( encodeCharset ).length;
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new FormatException( e.getMessage(), e );
            }
        }

        if ( len > length )
        {
            throw new FormatException( "element:[" + elementName + "],length of elementValue:[" + elementValue
                + "] is more longer then xsd defind len:[" + length + "]" );
        }
        if ( len == length )
        {
            return elementValue;
        }

        if ( "L".equals( align ) )
        {
            sb.append( elementValue );
            for ( int i = 0; i < length - len; ++i )
            {
                sb.append( blank );
            }
        }
        else
        {
            for ( int i = 0; i < length - len; ++i )
            {
                sb.append( blank );
            }
            sb.append( elementValue );
        }

        return sb.toString();
    }
}