package com.sanxing.sesame.resolver;

import javax.jbi.component.Component;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public class ConsumerComponentEndpointFilter implements EndpointFilter {
	private Component component;

	public ConsumerComponentEndpointFilter(Component component) {
		this.component = component;
	}

	public boolean evaluate(ServiceEndpoint endpoint, MessageExchange exchange) {
		return this.component.isExchangeWithConsumerOkay(endpoint, exchange);
	}
}