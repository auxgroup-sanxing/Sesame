package com.sanxing.sesame.exception;

import javax.xml.namespace.QName;

public class NoInterfaceAvailableException extends NoEndpointAvailableException {
	private static final long serialVersionUID = -509652152833232925L;
	private final QName interfaceName;

	public NoInterfaceAvailableException(QName interfaceName) {
		super("Cannot find an instance of the service: " + interfaceName);
		this.interfaceName = interfaceName;
	}

	public QName getInterfaceName() {
		return this.interfaceName;
	}
}