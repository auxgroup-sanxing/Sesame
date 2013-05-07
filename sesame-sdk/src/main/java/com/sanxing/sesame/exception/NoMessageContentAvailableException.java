package com.sanxing.sesame.exception;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

public class NoMessageContentAvailableException
    extends MessagingException
{
    private static final long serialVersionUID = 7132552587885219999L;

    private final MessageExchange messageExchange;

    public NoMessageContentAvailableException( MessageExchange messageExchange )
    {
        super( "No message content in the inbound message for message exchange: " + messageExchange );
        this.messageExchange = messageExchange;
    }

    public MessageExchange getMessageExchange()
    {
        return messageExchange;
    }
}