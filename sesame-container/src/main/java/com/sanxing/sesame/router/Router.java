package com.sanxing.sesame.router;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;

import com.sanxing.sesame.container.JBIContainer;

public interface Router
    extends RouterMBean
{
    public abstract JBIContainer getContainer();

    public abstract void init( JBIContainer container )
        throws JBIException;

    public abstract void suspend();

    public abstract void resume();

    public abstract void sendExchangePacket( MessageExchange exchange )
        throws JBIException;
}