package com.sanxing.sesame.exception;

import javax.xml.namespace.QName;

public class NoServiceEndpointAvailableException extends
		NoEndpointAvailableException {
	private static final long serialVersionUID = -2558426081896285761L;
	private final QName serviceName;
	private final String endpointName;

	public NoServiceEndpointAvailableException(QName serviceName,
			String endpointName) {
		super("Cannot find an instance of the service: " + serviceName
				+ " and endpoint: " + endpointName);
		this.serviceName = serviceName;
		this.endpointName = endpointName;
	}

	public QName getServiceName() {
		return this.serviceName;
	}

	public String getEndpointName() {
		return this.endpointName;
	}
}