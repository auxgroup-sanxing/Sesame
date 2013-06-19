package com.sanxing.sesame.resolver;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public interface EndpointFilter
{
    public abstract boolean evaluate( ServiceEndpoint endpoint, MessageExchange exchange );
}