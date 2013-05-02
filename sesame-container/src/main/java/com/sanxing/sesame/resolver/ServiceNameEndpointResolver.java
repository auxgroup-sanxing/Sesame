package com.sanxing.sesame.resolver;

import com.sanxing.sesame.exception.NoServiceAvailableException;
import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

public class ServiceNameEndpointResolver extends EndpointResolverSupport {
	private QName serviceName;

	public ServiceNameEndpointResolver() {
	}

	public ServiceNameEndpointResolver(QName serviceName) {
		this.serviceName = serviceName;
	}

	public ServiceEndpoint[] resolveAvailableEndpoints(
			ComponentContext context, MessageExchange exchange) {
		return context.getEndpointsForService(this.serviceName);
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