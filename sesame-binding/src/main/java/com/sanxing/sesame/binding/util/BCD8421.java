package com.sanxing.sesame.binding.util;

import java.io.ByteArrayOutputStream;

public class BCD8421
{
    public static String decode( byte[] bytes, Align align, char ignored )
    {
        if ( bytes.length == 0 )
        {
            return "";
        }

        StringBuffer sbuf = new StringBuffer();
        for ( int i = 0; i < bytes.length; ++i )
        {
            int h = ( ( bytes[i] & 0xFF ) >> 4 ) + 48;
            sbuf.append( (char) h );
            int l = ( bytes[i] & 0xF ) + 48;
            sbuf.append( (char) l );
        }
        String result = sbuf.toString();
        if ( align == null )
        {
            return result;
        }
        if ( align == Align.RIGHT )
        {
            if ( result.charAt( 0 ) == ignored )
            {
                return result.substring( 1 );
            }
        }
        else if ( ( align == Align.LEFT ) && ( result.charAt( result.length() - 1 ) == ignored ) )
        {
            return result.substring( 0, result.length() - 1 );
        }

        return result;
    }

    public static byte[] encode( String str, Align align, char filler )
    {
        if ( ( filler < '0' ) || ( filler > '9' ) )
        {
            throw new IllegalArgumentException( "填充符有效范围是 '0'-'9'" );
        }

        if ( str.length() % 2 != 0 )
        {
            if ( align == Align.LEFT )
            {
                str = str + filler;
            }
            else
            {
                str = filler + str;
            }
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        char[] carray = str.toCharArray();
        for ( int i = 0; i < carray.length; i += 2 )
        {
            int high = carray[i] - '0';
            int low = carray[( i + 1 )] - '0';
            output.write( high << 4 | low );
        }
        return output.toByteArray();
    }

    public static class Align
    {
        public static final Align LEFT = new Align( "left" );

        public static final Align RIGHT = new Align( "right" );

        private final String code;

        private Align( String code )
        {
            this.code = code;
        }

        @Override
        public String toString()
        {
            return code;
        }
    }
}