package com.sanxing.sesame.service;

import java.io.Serializable;

import javax.xml.namespace.QName;

public class ReferenceEntry implements Serializable {
	private static final long serialVersionUID = 1415388341021885499L;
	private QName servcieName;
	private QName interfaceName;
	private QName operationName;
	private String endpointName;

	public QName getServcieName() {
		return this.servcieName;
	}

	public void setServcieName(QName servcieName) {
		this.servcieName = servcieName;
	}

	public QName getInterfaceName() {
		return this.interfaceName;
	}

	public void setInterfaceName(QName _interfaceName) {
		this.interfaceName = _interfaceName;
	}

	public QName getOperationName() {
		return this.operationName;
	}

	public void setOperationName(QName _operationName) {
		this.operationName = _operationName;
	}

	public String getEndpointName() {
		return this.endpointName;
	}

	public void setEndpointName(String endpointName) {
		this.endpointName = endpointName;
	}

	public String toString() {
		return "{servcieName=" + this.servcieName + ", interfaceName="
				+ this.interfaceName + ", endpointName=" + this.endpointName
				+ ", operationName=" + this.operationName + '}';
	}
}