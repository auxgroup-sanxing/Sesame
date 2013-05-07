package com.sanxing.sesame.executors.impl;

import java.io.Serializable;

public class ExecutorConfig
    implements Serializable
{
    private static final long serialVersionUID = -3296569827553190492L;

    private int corePoolSize = 10;

    private int maximumPoolSize = 2147483647;

    private long keepAliveTime = 60000L;

    private boolean threadDaemon = true;

    private int threadPriority = 5;

    private int queueSize = 100;

    private long shutdownDelay = 1000L;

    private boolean allowCoreThreadsTimeout = true;

    private String callbackClass;

    public int getCorePoolSize()
    {
        return corePoolSize;
    }

    public void setCorePoolSize( int corePoolSize )
    {
        this.corePoolSize = corePoolSize;
    }

    public long getKeepAliveTime()
    {
        return keepAliveTime;
    }

    public void setKeepAliveTime( long keepAlive )
    {
        keepAliveTime = keepAlive;
    }

    public int getMaximumPoolSize()
    {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize( int maximumPoolSize )
    {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getQueueSize()
    {
        return queueSize;
    }

    public void setQueueSize( int queueSize )
    {
        this.queueSize = queueSize;
    }

    public boolean isThreadDaemon()
    {
        return threadDaemon;
    }

    public void setThreadDaemon( boolean threadDaemon )
    {
        this.threadDaemon = threadDaemon;
    }

    public int getThreadPriority()
    {
        return threadPriority;
    }

    public void setThreadPriority( int threadPriority )
    {
        this.threadPriority = threadPriority;
    }

    public long getShutdownDelay()
    {
        return shutdownDelay;
    }

    public void setShutdownDelay( long shutdownDelay )
    {
        this.shutdownDelay = shutdownDelay;
    }

    public boolean isAllowCoreThreadsTimeout()
    {
        return allowCoreThreadsTimeout;
    }

    public void setAllowCoreThreadsTimeout( boolean allowCoreThreadsTimeout )
    {
        this.allowCoreThreadsTimeout = allowCoreThreadsTimeout;
    }

    public void setCallbackClass( String callbackClass )
    {
        this.callbackClass = callbackClass;
    }

    public String getCallbackClass()
    {
        return callbackClass;
    }

    @Override
    public String toString()
    {
        return "ExecutorConfig [allowCoreThreadsTimeout=" + allowCoreThreadsTimeout + ", corePoolSize=" + corePoolSize
            + ", keepAliveTime=" + keepAliveTime + ", maximumPoolSize=" + maximumPoolSize + ", queueSize=" + queueSize
            + ", shutdownDelay=" + shutdownDelay + ", threadDaemon=" + threadDaemon + ", threadPriority="
            + threadPriority + "]";
    }
}