package com.sanxing.sesame.executors.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.executors.Callback;
import com.sanxing.sesame.executors.ThreadLocalCacheCleaner;

public class SesameExecutor
    extends ThreadPoolExecutor
    implements SesameExecutorMBean
{
    private final Logger LOG = LoggerFactory.getLogger( SesameExecutor.class );

    private final long shutdownDelay;

    private final String id;

    private final ThreadLocal<Long> startTime = new ThreadLocal();

    private final AtomicLong numTasks = new AtomicLong();

    private final AtomicLong totalTime = new AtomicLong();

    private final AtomicLong lastWorkTime = new AtomicLong();

    private final AtomicBoolean monitor = new AtomicBoolean( true );

    private final List<Callback> callbacks = new ArrayList();

    public SesameExecutor( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                           BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                           RejectedExecutionHandler handler, long _shutdownDelay, String _id )
    {
        super( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler );
        shutdownDelay = _shutdownDelay;
        id = _id;
        addCallback( new ThreadLocalCacheCleaner() );
    }

    public void addCallback( Callback callback )
    {
        callbacks.add( callback );
    }

    public void removeCallback( Callback callback )
    {
        callbacks.remove( callback );
    }

    @Override
    protected void beforeExecute( Thread t, Runnable r )
    {
        super.beforeExecute( t, r );
        if ( monitor.get() )
        {
            //LOG.debug( String.format( "%s start %s", new Object[] { t, r } ) );
            startTime.set( Long.valueOf( System.nanoTime() ) );
            numTasks.incrementAndGet();
        }
        for ( Callback callback : callbacks )
        {
            callback.beforeExecute( t );
        }
    }

    @Override
    public void turnOnMonitor()
    {
        totalTime.set( 0L );
        monitor.set( true );
    }

    @Override
    public boolean isMonitorOn()
    {
        return monitor.get();
    }

    @Override
    public void shutdownMonitor()
    {
        monitor.set( false );
    }

    @Override
    protected void afterExecute( Runnable r, Throwable t )
    {
        try
        {
            if ( monitor.get() )
            {
                long endTime = System.nanoTime();
                long taskTime = endTime - startTime.get().longValue();
                totalTime.addAndGet( taskTime );
                lastWorkTime.set( taskTime );
                /*if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( String.format( "%s end %s, time=%dns",
                        new Object[] { Thread.currentThread(), r, Long.valueOf( taskTime ) } ) );
                }*/
            }
            for ( Callback callback : callbacks )
            {
                callback.afterExecute( t );
            }
        }
        finally
        {
            super.afterExecute( r, t );
        }
    }

    @Override
    protected void terminated()
    {
        try
        {
            long avg = ( numTasks.get() > 0L ) ? totalTime.get() / numTasks.get() : 0L;
            LOG.info( String.format( "Terminated: avg time=%dns", new Object[] { Long.valueOf( avg ) } ) );

            for ( Callback callback : callbacks )
            {
                callback.terminated();
            }
        }
        finally
        {
            super.terminated();
        }
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
        if ( ( isTerminated() ) || ( shutdownDelay <= 0L ) )
        {
            return;
        }
        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if ( SesameExecutor.this.awaitTermination( shutdownDelay, TimeUnit.MILLISECONDS ) )
                    {
                        return;
                    }
                    SesameExecutor.this.shutdownNow();
                }
                catch ( InterruptedException localInterruptedException )
                {
                }
            }
        } ).start();
    }

    @Override
    public long averageWorkTime()
    {
        long avg = ( numTasks.get() > 0L ) ? totalTime.get() / numTasks.get() : 0L;
        return avg;
    }

    @Override
    public long lastWorkTime()
    {
        long time = lastWorkTime.get();
        lastWorkTime.set( 0L );
        return time;
    }

    @Override
    public String getID()
    {
        return id;
    }
}