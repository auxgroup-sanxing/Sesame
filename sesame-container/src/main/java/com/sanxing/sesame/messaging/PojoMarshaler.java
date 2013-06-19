package com.sanxing.sesame.messaging;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

public interface PojoMarshaler
{
    public static final String BODY = "com.sanxing.sesame.body";

    public abstract void marshal( MessageExchange exchange, NormalizedMessage message, Object body )
        throws MessagingException;

    public abstract Object unmarshal( MessageExchange exchange, NormalizedMessage message )
        throws MessagingException;
}