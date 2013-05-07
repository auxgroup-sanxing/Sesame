package com.sanxing.sesame.messaging;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

public abstract interface PojoMarshaler
{
    public static final String BODY = "com.sanxing.sesame.body";

    public abstract void marshal( MessageExchange paramMessageExchange, NormalizedMessage paramNormalizedMessage,
                                  Object paramObject )
        throws MessagingException;

    public abstract Object unmarshal( MessageExchange paramMessageExchange, NormalizedMessage paramNormalizedMessage )
        throws MessagingException;
}