package com.sanxing.sesame.executors;

import com.sanxing.sesame.util.cache.ThreadLocalCache;

public class ThreadLocalCacheCleaner
    implements Callback
{
    @Override
    public void beforeExecute( Thread thead )
    {
    }

    @Override
    public void afterExecute( Throwable t )
    {
        ThreadLocalCache.clear();
    }

    @Override
    public void terminated()
    {
    }
}