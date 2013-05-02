package com.sanxing.sesame.resolver;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public class FirstChoicePolicy implements EndpointChooser {
	public ServiceEndpoint chooseEndpoint(ServiceEndpoint[] endpoints,
			ComponentContext context, MessageExchange exchange) {
		if (endpoints.length == 0) {
			return null;
		}
		return endpoints[0];
	}
}