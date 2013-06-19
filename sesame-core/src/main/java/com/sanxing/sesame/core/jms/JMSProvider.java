package com.sanxing.sesame.core.jms;

import com.sanxing.sesame.core.BaseServer;

public interface JMSProvider
{
    public abstract void prepare( BaseServer server, JMSServiceInfo serviceInfo );

    public abstract void release();
}