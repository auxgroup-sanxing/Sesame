package com.sanxing.sesame.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiplexOutputStream
    extends OutputStream
{
    List streams = new CopyOnWriteArrayList();

    public void add( OutputStream os )
    {
        streams.add( os );
    }

    public void remove( OutputStream os )
    {
        streams.remove( os );
    }

    @Override
    public synchronized void write( int b )
        throws IOException
    {
        for ( Iterator i = streams.iterator(); i.hasNext(); )
        {
            OutputStream s = (OutputStream) i.next();
            s.write( b );
        }
    }

    @Override
    public synchronized void write( byte[] b, int off, int len )
        throws IOException
    {
        for ( Iterator i = streams.iterator(); i.hasNext(); )
        {
            OutputStream s = (OutputStream) i.next();
            s.write( b, off, len );
        }
    }

    @Override
    public void flush()
        throws IOException
    {
        for ( Iterator i = streams.iterator(); i.hasNext(); )
        {
            OutputStream s = (OutputStream) i.next();
            s.flush();
        }
    }

    @Override
    public void close()
        throws IOException
    {
        for ( Iterator i = streams.iterator(); i.hasNext(); )
        {
            OutputStream s = (OutputStream) i.next();
            s.close();
        }
        streams.clear();
    }
}