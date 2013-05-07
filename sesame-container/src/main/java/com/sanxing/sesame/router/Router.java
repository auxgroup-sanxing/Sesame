package com.sanxing.sesame.router;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;

import com.sanxing.sesame.container.JBIContainer;

public abstract interface Router
    extends RouterMBean
{
    public abstract JBIContainer getContainer();

    public abstract void init( JBIContainer paramJBIContainer )
        throws JBIException;

    public abstract void suspend();

    public abstract void resume();

    public abstract void sendExchangePacket( MessageExchange paramMessageExchange )
        throws JBIException;
}