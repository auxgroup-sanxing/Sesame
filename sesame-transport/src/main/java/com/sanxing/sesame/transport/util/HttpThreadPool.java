package com.sanxing.sesame.transport.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.thread.ThreadPool;

import com.sanxing.sesame.executors.ExecutorFactory;

public class HttpThreadPool
    implements ThreadPool
{
    private final ExecutorService executor = ExecutorFactory.getFactory().createExecutor( "transports.http" );

    @Override
    public boolean dispatch( Runnable job )
    {
        executor.execute( job );
        return true;
    }

    @Override
    public void join()
        throws InterruptedException
    {
        executor.awaitTermination( 60L, TimeUnit.SECONDS );
    }

    @Override
    public int getThreads()
    {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) this.executor;
        return executor.getMaximumPoolSize();
    }

    @Override
    public int getIdleThreads()
    {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) this.executor;
        return ( executor.getMaximumPoolSize() - executor.getActiveCount() );
    }

    @Override
    public boolean isLowOnThreads()
    {
        return false;
    }
}