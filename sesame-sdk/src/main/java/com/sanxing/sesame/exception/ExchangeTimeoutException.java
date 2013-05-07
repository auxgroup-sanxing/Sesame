package com.sanxing.sesame.exception;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

public class ExchangeTimeoutException
    extends MessagingException
{
    private static final long serialVersionUID = -7875708001595840058L;

    private final MessageExchange exchange;

    public ExchangeTimeoutException( MessageExchange exchange )
    {
        super( "Exchange has timed out: " + exchange );
        this.exchange = exchange;
    }

    public MessageExchange getExchange()
    {
        return exchange;
    }
}