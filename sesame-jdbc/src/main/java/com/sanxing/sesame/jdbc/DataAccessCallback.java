package com.sanxing.sesame.jdbc;

import com.sanxing.sesame.executors.Callback;

public class DataAccessCallback
    implements Callback
{
    @Override
    public void afterExecute( Throwable t )
    {
        TXHelper.destory();
    }

    @Override
    public void beforeExecute( Thread thead )
    {
        TXHelper.destory();
    }

    @Override
    public void terminated()
    {
    }
}