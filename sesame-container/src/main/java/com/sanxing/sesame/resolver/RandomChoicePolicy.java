package com.sanxing.sesame.resolver;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public class RandomChoicePolicy
    implements EndpointChooser
{
    @Override
    public ServiceEndpoint chooseEndpoint( ServiceEndpoint[] endpoints, ComponentContext context,
                                           MessageExchange exchange )
    {
        int index = (int) ( Math.random() * endpoints.length );
        return endpoints[index];
    }
}