package com.sanxing.sesame.util;

public class HexBinary
{
    public static byte[] getClone( byte[] pHexBinary )
    {
        byte[] result = new byte[pHexBinary.length];
        System.arraycopy( pHexBinary, 0, result, 0, pHexBinary.length );
        return result;
    }

    public static byte[] decode( String hexString )
    {
        if ( hexString.length() % 2 != 0 )
        {
            throw new IllegalArgumentException( "A HexBinary string must have even length." );
        }
        byte[] result = new byte[hexString.length() / 2];
        int j = 0;
        for ( int i = 0; i < hexString.length(); )
        {
            char c = hexString.charAt( i++ );
            char d = hexString.charAt( i++ );
            byte b;
            if ( ( c >= '0' ) && ( c <= '9' ) )
            {
                b = (byte) ( c - '0' << 4 );
            }
            else
            {
                if ( ( c >= 'A' ) && ( c <= 'F' ) )
                {
                    b = (byte) ( c - 'A' + 10 << 4 );
                }
                else
                {
                    if ( ( c >= 'a' ) && ( c <= 'f' ) )
                    {
                        b = (byte) ( c - 'a' + 10 << 4 );
                    }
                    else
                    {
                        throw new IllegalArgumentException( "Invalid hex digit: " + c );
                    }
                }
            }
            if ( ( d >= '0' ) && ( d <= '9' ) )
            {
                b = (byte) ( b + (byte) ( d - '0' ) );
            }
            else if ( ( d >= 'A' ) && ( d <= 'F' ) )
            {
                b = (byte) ( b + (byte) ( d - 'A' + 10 ) );
            }
            else if ( ( d >= 'a' ) && ( d <= 'f' ) )
            {
                b = (byte) ( b + (byte) ( d - 'a' + 10 ) );
            }
            else
            {
                throw new IllegalArgumentException( "Invalid hex digit: " + d );
            }
            result[( j++ )] = b;
        }
        return result;
    }

    public static String encode( byte[] binary )
    {
        return encode( binary, 0, binary.length );
    }

    public static String encode( byte[] binary, int off, int len )
    {
        StringBuffer result = new StringBuffer();
        for ( int i = off; i < len; ++i )
        {
            byte b = binary[i];
            byte c = (byte) ( ( b & 0xF0 ) >> 4 );
            if ( c <= 9 )
            {
                result.append( (char) ( 48 + c ) );
            }
            else
            {
                result.append( (char) ( 65 + c - 10 ) );
            }
            c = (byte) ( b & 0xF );
            if ( c <= 9 )
            {
                result.append( (char) ( 48 + c ) );
            }
            else
            {
                result.append( (char) ( 65 + c - 10 ) );
            }
        }
        return result.toString();
    }
}