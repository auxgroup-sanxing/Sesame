package com.sanxing.sesame.codec.util;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class HexBinary implements Serializable
{
    private static final long serialVersionUID = -5082403899986720767L;

    byte[] m_value;

    public static final int[] DEC = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

    public HexBinary()
    {
    }

    public HexBinary( String string )
    {
        m_value = decode( string );
    }

    public HexBinary( byte[] bytes )
    {
        m_value = bytes;
    }

    public byte[] getBytes()
    {
        return m_value;
    }

    @Override
    public String toString()
    {
        return encode( m_value );
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals( Object object )
    {
        String s1 = object.toString();
        String s2 = toString();
        return s1.equals( s2 );
    }

    public static byte[] decode( String digits )
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for ( int i = 0; i < digits.length(); i += 2 )
        {
            char c1 = digits.charAt( i );
            if ( i + 1 >= digits.length() )
            {
                throw new IllegalArgumentException();
            }
            char c2 = digits.charAt( i + 1 );
            byte b = 0;
            if ( ( c1 >= '0' ) && ( c1 <= '9' ) )
            {
                b = (byte) ( b + ( c1 - '0' ) * 16 );
            }
            else if ( ( c1 >= 'a' ) && ( c1 <= 'f' ) )
            {
                b = (byte) ( b + ( c1 - 'a' + 10 ) * 16 );
            }
            else if ( ( c1 >= 'A' ) && ( c1 <= 'F' ) )
            {
                b = (byte) ( b + ( c1 - 'A' + 10 ) * 16 );
            }
            else
            {
                throw new IllegalArgumentException();
            }
            if ( ( c2 >= '0' ) && ( c2 <= '9' ) )
            {
                b = (byte) ( b + c2 - '0' );
            }
            else if ( ( c2 >= 'a' ) && ( c2 <= 'f' ) )
            {
                b = (byte) ( b + c2 - 'a' + 10 );
            }
            else if ( ( c2 >= 'A' ) && ( c2 <= 'F' ) )
            {
                b = (byte) ( b + c2 - 'A' + 10 );
            }
            else
            {
                throw new IllegalArgumentException();
            }
            baos.write( b );
        }
        byte[] temp = baos.toByteArray();
        return baos.toByteArray();
    }

    public static String encode( byte[] bytes )
    {
        StringBuffer sb = new StringBuffer( bytes.length * 2 );
        for ( int i = 0; i < bytes.length; ++i )
        {
            sb.append( convertDigit( bytes[i] >> 4 ) );
            sb.append( convertDigit( bytes[i] & 0xF ) );
        }
        return sb.toString();
    }

    public static int convert2Int( byte[] hex )
    {
        if ( hex.length < 4 )
        {
            return 0;
        }
        if ( DEC[hex[0]] < 0 )
        {
            throw new IllegalArgumentException();
        }
        int len = DEC[hex[0]];
        len <<= 4;
        if ( DEC[hex[1]] < 0 )
        {
            throw new IllegalArgumentException();
        }
        len += DEC[hex[1]];
        len <<= 4;
        if ( DEC[hex[2]] < 0 )
        {
            throw new IllegalArgumentException();
        }
        len += DEC[hex[2]];
        len <<= 4;
        if ( DEC[hex[3]] < 0 )
        {
            throw new IllegalArgumentException();
        }
        len += DEC[hex[3]];
        return len;
    }

    private static char convertDigit( int value )
    {
        value &= 15;
        if ( value >= 10 )
        {
            return (char) ( value - 10 + 65 );
        }
        return (char) ( value + 48 );
    }
}