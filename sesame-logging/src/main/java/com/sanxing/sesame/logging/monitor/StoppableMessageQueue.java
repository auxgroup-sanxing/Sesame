package com.sanxing.sesame.logging.monitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.logging.dao.BaseBean;

public class StoppableMessageQueue
{
    private final int MAXIMUM_PENDING_OFFERS = 2147483647;

    private final int MAXIMUM_MESSESGES = 2147483647;

    private final BlockingQueue<BaseBean> messageQueue = new LinkedBlockingQueue();

    private boolean isStopped = false;

    private final Semaphore semaphore = new Semaphore( 2147483647 );

    private static final Logger LOG = LoggerFactory.getLogger( StoppableMessageQueue.class );

    private static StoppableMessageQueue instance = null;

    public static StoppableMessageQueue getInstance()
    {
        if ( instance == null )
        {
            synchronized ( StoppableMessageQueue.class )
            {
                if ( instance == null )
                {
                    instance = new StoppableMessageQueue();
                }
            }
        }
        return instance;
    }

    public boolean putMessage( BaseBean message )
    {
        BaseBean trash;
        synchronized ( this )
        {
            if ( isStopped )
            {
                LOG.debug( "Queue is stopped!" );
                return false;
            }
            if ( messageQueue.size() >= 10000 )
            {
                trash = messageQueue.poll();
                LOG.debug( "Discard message: " + trash );
            }
            if ( !( semaphore.tryAcquire() ) )
            {
                throw new Error( "too many threads" );
            }
        }
        try
        {
            return messageQueue.offer( message );
        }
        finally
        {
            semaphore.release();
        }
    }

    public BaseBean getMessage( long timeout )
    {
        try
        {
            return messageQueue.poll( timeout, TimeUnit.MILLISECONDS );
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public Collection<BaseBean> shutDown()
    {
        synchronized ( this )
        {
            isStopped = true;
        }
        semaphore.acquireUninterruptibly( 2147483647 );
        Set returnCollection = new HashSet();
        messageQueue.drainTo( returnCollection );
        return returnCollection;
    }
}