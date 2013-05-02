package com.sanxing.sesame.resolver;

import com.sanxing.sesame.exception.NoInterfaceAvailableException;
import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

public class InterfaceNameEndpointResolver extends EndpointResolverSupport {
	private QName interfaceName;

	public InterfaceNameEndpointResolver() {
	}

	public InterfaceNameEndpointResolver(QName interfaceName) {
		this.interfaceName = interfaceName;
	}

	public ServiceEndpoint[] resolveAvailableEndpoints(
			ComponentContext context, MessageExchange exchange) {
		return context.getEndpoints(this.interfaceName);
	}

	public QName getInterfaceName() {
		return this.interfaceName;
	}

	public void setInterfaceName(QName interfaceName) {
		this.interfaceName = interfaceName;
	}

	protected JBIException createServiceUnavailableException() {
		return new NoInterfaceAvailableException(this.interfaceName);
	}
}