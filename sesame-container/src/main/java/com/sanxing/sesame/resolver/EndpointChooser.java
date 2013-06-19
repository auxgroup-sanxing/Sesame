package com.sanxing.sesame.resolver;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public interface EndpointChooser
{
    public abstract ServiceEndpoint chooseEndpoint( ServiceEndpoint[] endpoints, ComponentContext context,
                                                    MessageExchange exchange );
}