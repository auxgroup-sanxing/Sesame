package com.sanxing.sesame.resolver;

import java.util.HashMap;
import java.util.Map;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

public class ServiceRoundRobinPolicy implements EndpointChooser {
	private Map<QName, Integer> lastIndexMap;
	private int lastIndex;

	public ServiceRoundRobinPolicy() {
		this.lastIndexMap = new HashMap();
	}

	public ServiceEndpoint chooseEndpoint(ServiceEndpoint[] endpoints,
			ComponentContext context, MessageExchange exchange) {
		if (endpoints.length == 0) {
			return null;
		}

		if (exchange.getService() == null) {
			return endpoints[0];
		}

		if (this.lastIndexMap.containsKey(exchange.getService())) {
			this.lastIndex = ((Integer) this.lastIndexMap.get(exchange
					.getService())).intValue();
		} else {
			this.lastIndex = 0;
		}

		if ((this.lastIndex >= endpoints.length) || (this.lastIndex < 0)) {
			this.lastIndex = 0;
		}

		ServiceEndpoint result = endpoints[(this.lastIndex++)];

		this.lastIndexMap.put(exchange.getService(),
				Integer.valueOf(this.lastIndex));

		return result;
	}
}