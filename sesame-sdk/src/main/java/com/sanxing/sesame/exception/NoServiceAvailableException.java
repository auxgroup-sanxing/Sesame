package com.sanxing.sesame.exception;

import javax.xml.namespace.QName;

public class NoServiceAvailableException extends NoEndpointAvailableException {
	private static final long serialVersionUID = -4259284775229065256L;
	private final QName serviceName;

	public NoServiceAvailableException(QName serviceName) {
		super("Cannot find an instance of the service: " + serviceName);
		this.serviceName = serviceName;
	}

	public QName getServiceName() {
		return this.serviceName;
	}
}