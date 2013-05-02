package com.sanxing.sesame.resolver;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public abstract interface EndpointResolver {
	public abstract ServiceEndpoint resolveEndpoint(
			ComponentContext paramComponentContext,
			MessageExchange paramMessageExchange,
			EndpointFilter paramEndpointFilter) throws JBIException;

	public abstract ServiceEndpoint[] resolveAvailableEndpoints(
			ComponentContext paramComponentContext,
			MessageExchange paramMessageExchange) throws JBIException;
}