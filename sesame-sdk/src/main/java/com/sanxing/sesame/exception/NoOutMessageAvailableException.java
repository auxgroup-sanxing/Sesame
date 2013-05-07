package com.sanxing.sesame.exception;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

public class NoOutMessageAvailableException
    extends MessagingException
{
    private static final long serialVersionUID = 3767803468542440558L;

    private final MessageExchange messageExchange;

    public NoOutMessageAvailableException( MessageExchange messageExchange )
    {
        super( "No out message available for message exchange: " + messageExchange );
        this.messageExchange = messageExchange;
    }

    public MessageExchange getMessageExchange()
    {
        return messageExchange;
    }
}