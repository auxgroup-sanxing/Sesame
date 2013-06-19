package com.sanxing.sesame.resolver;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public interface EndpointResolver
{
    public abstract ServiceEndpoint resolveEndpoint( ComponentContext context, MessageExchange exchange, EndpointFilter filter )
        throws JBIException;

    public abstract ServiceEndpoint[] resolveAvailableEndpoints( ComponentContext context, MessageExchange exchange )
        throws JBIException;
}