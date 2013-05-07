package com.sanxing.sesame.component;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

public abstract interface MessageTransformer
{
    public abstract boolean transform( MessageExchange paramMessageExchange, NormalizedMessage paramNormalizedMessage1,
                                       NormalizedMessage paramNormalizedMessage2 )
        throws MessagingException;
}