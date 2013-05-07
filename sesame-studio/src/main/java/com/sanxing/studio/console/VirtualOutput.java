package com.sanxing.studio.console;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualOutput
    extends OutputStream
{
    private static Logger LOG = LoggerFactory.getLogger( VirtualOutput.class );

    private final BlockingQueue<String> queue;

    public VirtualOutput( int capacity )
    {
        queue = new LinkedBlockingQueue( capacity );
    }

    @Override
    public void write( int b )
        throws IOException
    {
    }

    @Override
    public void write( byte[] b, int off, int len )
        throws IOException
    {
        try
        {
            if ( queue.remainingCapacity() == 0 )
            {
                queue.take();
            }
            queue.put( new String( b, off, len ) );
        }
        catch ( InterruptedException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }

    public String poll( long timeout, TimeUnit unit )
        throws InterruptedException
    {
        return queue.poll( timeout, unit );
    }

    @Override
    public void close()
        throws IOException
    {
        queue.clear();
        super.close();
    }
}