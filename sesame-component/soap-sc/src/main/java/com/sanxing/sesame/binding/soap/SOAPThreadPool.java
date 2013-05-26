package com.sanxing.sesame.binding.soap;

import com.sanxing.sesame.executors.ExecutorFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.util.thread.ThreadPool;

public class SOAPThreadPool
    implements ThreadPool
{
    private Executor executor = ExecutorFactory.getFactory().createExecutor( "transports.http.soap" );

    public boolean dispatch( Runnable job )
    {
        this.executor.execute( job );
        return true;
    }

    public void join()
        throws InterruptedException
    {
        ( (ExecutorService) this.executor ).awaitTermination( 60L, TimeUnit.SECONDS );
    }

    public int getThreads()
    {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) this.executor;
        return executor.getMaximumPoolSize();
    }

    public int getIdleThreads()
    {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) this.executor;
        return executor.getMaximumPoolSize() - executor.getActiveCount();
    }

    public boolean isLowOnThreads()
    {
        return false;
    }
}