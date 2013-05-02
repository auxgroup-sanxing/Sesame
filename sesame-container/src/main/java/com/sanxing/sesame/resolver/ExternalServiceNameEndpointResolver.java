package com.sanxing.sesame.resolver;

import com.sanxing.sesame.exception.NoServiceAvailableException;
import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

public class ExternalServiceNameEndpointResolver extends
		EndpointResolverSupport {
	private QName serviceName;

	public ExternalServiceNameEndpointResolver() {
	}

	public ExternalServiceNameEndpointResolver(QName serviceName) {
		this.serviceName = serviceName;
	}

	public ServiceEndpoint[] resolveAvailableEndpoints(
			ComponentContext context, MessageExchange exchange) {
		return context.getExternalEndpointsForService(this.serviceName);
	}

	public QName getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(QName serviceName) {
		this.serviceName = serviceName;
	}

	protected JBIException createServiceUnavailableException() {
		return new NoServiceAvailableException(this.serviceName);
	}
}