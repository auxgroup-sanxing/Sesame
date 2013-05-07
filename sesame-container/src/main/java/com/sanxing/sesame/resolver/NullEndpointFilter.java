package com.sanxing.sesame.resolver;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public class NullEndpointFilter
    implements EndpointFilter
{
    private static final NullEndpointFilter INSTANCE = new NullEndpointFilter();

    public static NullEndpointFilter getInstance()
    {
        return INSTANCE;
    }

    @Override
    public boolean evaluate( ServiceEndpoint endpoint, MessageExchange exchange )
    {
        return true;
    }
}