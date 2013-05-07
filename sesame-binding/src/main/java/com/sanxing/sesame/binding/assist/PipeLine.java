package com.sanxing.sesame.binding.assist;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PipeLine
{
    private final InputStream in;

    private final OutputStream out;

    private boolean closed = false;

    public PipeLine( InputStream input, OutputStream output )
    {
        in = input;
        out = output;
    }

    public InputStream getInput()
    {
        return in;
    }

    public OutputStream getOutput()
    {
        return out;
    }

    public boolean isClosed()
    {
        return closed;
    }

    public void close()
        throws IOException
    {
        closed = true;
    }

    public int extractHead()
        throws IOException
    {
        return -1;
    }

    public byte[] exportHead( int len )
        throws IOException
    {
        return new byte[0];
    }

    public int read( byte[] buffer )
        throws IOException
    {
        return in.read( buffer );
    }

    public void read( byte[] buffer, int off, int len )
        throws IOException
    {
        len += off;
        int l;
        for ( int p = off; ( l = in.read( buffer, p, len - p ) ) < len - p; )
        {
            p += l;
        }
    }

    public void write( byte[] bytes, int off, int len )
        throws IOException
    {
        out.write( bytes, off, len );
    }
}