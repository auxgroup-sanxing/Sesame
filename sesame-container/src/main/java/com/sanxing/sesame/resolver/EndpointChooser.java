package com.sanxing.sesame.resolver;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public abstract interface EndpointChooser {
	public abstract ServiceEndpoint chooseEndpoint(
			ServiceEndpoint[] paramArrayOfServiceEndpoint,
			ComponentContext paramComponentContext,
			MessageExchange paramMessageExchange);
}