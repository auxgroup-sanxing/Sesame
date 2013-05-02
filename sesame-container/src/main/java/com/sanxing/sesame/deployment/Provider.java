package com.sanxing.sesame.deployment;

import javax.xml.namespace.QName;

public class Provider {
	private QName serviceName;
	private String endpointName;

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
}