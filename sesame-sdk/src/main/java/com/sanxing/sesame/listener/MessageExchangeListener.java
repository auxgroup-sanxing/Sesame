package com.sanxing.sesame.listener;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

public abstract interface MessageExchangeListener
{
    public abstract void onMessageExchange( MessageExchange paramMessageExchange )
        throws MessagingException;
}