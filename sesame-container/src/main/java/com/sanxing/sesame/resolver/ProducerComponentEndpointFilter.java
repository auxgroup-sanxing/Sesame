package com.sanxing.sesame.resolver;

import javax.jbi.component.Component;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public class ProducerComponentEndpointFilter
    implements EndpointFilter
{
    private final Component component;

    public ProducerComponentEndpointFilter( Component component )
    {
        this.component = component;
    }

    @Override
    public boolean evaluate( ServiceEndpoint endpoint, MessageExchange exchange )
    {
        return component.isExchangeWithProviderOkay( endpoint, exchange );
    }
}