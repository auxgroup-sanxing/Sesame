package com.sanxing.sesame.executors;

import java.util.concurrent.ExecutorService;

import com.sanxing.sesame.executors.impl.ExecutorFactoryImpl;

public abstract class ExecutorFactory
{
    public abstract ExecutorService createExecutor( String paramString );

    public abstract ExecutorService createExecutor( String paramString, Callback[] paramArrayOfCallback );

    public static ExecutorFactory getFactory()
    {
        return ExecutorFactoryImpl.getFactory();
    }
}