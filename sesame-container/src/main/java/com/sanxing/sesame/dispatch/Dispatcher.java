package com.sanxing.sesame.dispatch;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.messaging.MessageExchange;

import com.sanxing.sesame.router.Router;

public interface Dispatcher
    extends LifeCycleMBean
{
    public abstract void init( Router router )
        throws JBIException;

    public abstract String getDescription();

    public abstract String getName();

    public abstract void send( MessageExchange me )
        throws JBIException;

    public abstract void suspend();

    public abstract void resume();

    public abstract Router getRouter();

    public abstract boolean canHandle( MessageExchange me );
}