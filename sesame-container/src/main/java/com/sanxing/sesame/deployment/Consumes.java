package com.sanxing.sesame.deployment;

import javax.xml.namespace.QName;

public class Consumes {
	private QName interfaceName;
	private QName serviceName;
	private String endpointName;
	private String linkType;

	public Consumes() {
		this.linkType = "standard";
	}

	public QName getInterfaceName() {
		return this.interfaceName;
	}

	public void setInterfaceName(QName interfaceName) {
		this.interfaceName = interfaceName;
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

	public String getLinkType() {
		return this.linkType;
	}

	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	public boolean isStandardLink() {
		return ((this.linkType != null) && (this.linkType.equals("standard")));
	}

	public boolean isSoftLink() {
		return ((this.linkType != null) && (this.linkType.equals("soft")));
	}

	public boolean isHardLink() {
		return ((this.linkType != null) && (this.linkType.equals("hard")));
	}
}