package com.sanxing.sesame.resolver;

import com.sanxing.sesame.exception.NoServiceAvailableException;
import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

public class ServiceAndEndpointNameResolver extends EndpointResolverSupport {
	private QName serviceName;
	private String endpointName;

	public ServiceAndEndpointNameResolver() {
	}

	public ServiceAndEndpointNameResolver(QName serviceName, String endpointName) {
		this.serviceName = serviceName;
		this.endpointName = endpointName;
	}

	public ServiceEndpoint[] resolveAvailableEndpoints(
			ComponentContext context, MessageExchange exchange)
			throws JBIException {
		ServiceEndpoint endpoint = context.getEndpoint(this.serviceName,
				this.endpointName);
		if (endpoint != null) {
			return new ServiceEndpoint[] { endpoint };
		}
		return new ServiceEndpoint[0];
	}

	public QName getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(QName serviceName) {
		this.serviceName = serviceName;
	}

	public String getEndpointName() {
		return this.endpointName;
	}

	public void setEndpointName(String endpointName) {
		this.endpointName = endpointName;
	}

	protected JBIException createServiceUnavailableException() {
		return new NoServiceAvailableException(this.serviceName);
	}
}