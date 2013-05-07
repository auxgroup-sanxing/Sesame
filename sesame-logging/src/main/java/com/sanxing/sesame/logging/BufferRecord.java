package com.sanxing.sesame.logging;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class BufferRecord
    extends LogRecord
{
    private static final long serialVersionUID = -5301334472490708075L;

    private Format format = Format.STRING;

    private String encoding = System.getProperty( "file.encoding" );

    private boolean callout = false;

    public boolean isCallout()
    {
        return callout;
    }

    public void setCallout( boolean callout )
    {
        this.callout = callout;
    }

    public BufferRecord( long serial, byte[] buffer )
    {
        setSerial( serial );
        setBuffer( buffer );
    }

    public void setBuffer( byte[] buffer )
    {
        setContent( buffer );
    }

    public byte[] getBuffer()
    {
        if ( getContent() instanceof byte[] )
        {
            return ( (byte[]) getContent() );
        }

        return null;
    }

    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setFormat( Format format )
    {
        this.format = format;
    }

    public Format getFormat()
    {
        return format;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "{" );
        buf.append( " serial: " + getSerial() );
        if ( getAction() != null )
        {
            buf.append( ", action: '" + getAction() + "'" );
        }
        buf.append( ", content: " );
        if ( getBuffer() == null )
        {
            buf.append( "null" );
        }
        else if ( format == Format.HEX )
        {
            buf.append( encode( getBuffer() ) );
        }
        else
        {
            buf.append( "\"" );
            try
            {
                buf.append( new String( getBuffer(), getEncoding() ) );
            }
            catch ( UnsupportedEncodingException e )
            {
                buf.append( new String( getBuffer() ) );
            }
            buf.append( "\" " );
        }
        buf.append( "}" );
        return buf.toString();
    }

    public static String encode( byte[] pHexBinary )
    {
        StringBuffer result = new StringBuffer();
        for ( int i = 0; i < pHexBinary.length; ++i )
        {
            if ( i > 0 )
            {
                result.append( " " );
            }

            byte b = pHexBinary[i];
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

    public static class Format
        implements Serializable
    {
        private static final long serialVersionUID = -716887208692893784L;

        public static Format HEX = new Format();

        public static Format STRING = new Format();
    }
}