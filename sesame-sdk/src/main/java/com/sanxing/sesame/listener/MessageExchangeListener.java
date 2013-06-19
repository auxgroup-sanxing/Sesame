package com.sanxing.sesame.listener;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

public interface MessageExchangeListener
{
    public abstract void onMessageExchange( MessageExchange exchange )
        throws MessagingException;
}