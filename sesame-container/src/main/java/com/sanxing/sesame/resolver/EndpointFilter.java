package com.sanxing.sesame.resolver;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public abstract interface EndpointFilter {
	public abstract boolean evaluate(ServiceEndpoint paramServiceEndpoint,
			MessageExchange paramMessageExchange);
}