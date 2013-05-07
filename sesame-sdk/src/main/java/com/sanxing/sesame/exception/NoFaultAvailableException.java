package com.sanxing.sesame.exception;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

public class NoFaultAvailableException
    extends MessagingException
{
    private static final long serialVersionUID = 7318587102192500534L;

    private final MessageExchange messageExchange;

    public NoFaultAvailableException( MessageExchange messageExchange )
    {
        super( "No Fault available for message exchange: " + messageExchange );
        this.messageExchange = messageExchange;
    }

    public MessageExchange getMessageExchange()
    {
        return messageExchange;
    }
}