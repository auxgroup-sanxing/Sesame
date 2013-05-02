package com.sanxing.sesame.servicedesc;

import com.sanxing.sesame.mbean.ComponentNameSpace;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import org.w3c.dom.DocumentFragment;

public class DynamicEndpoint extends AbstractEndpoint {
	private static final long serialVersionUID = -9084647509619730734L;
	private final QName serviceName;
	private final String endpointName;
	private final transient DocumentFragment epr;

	public DynamicEndpoint(ComponentNameSpace componentName,
			ServiceEndpoint endpoint, DocumentFragment epr) {
		super(componentName);
		this.serviceName = endpoint.getServiceName();
		this.endpointName = endpoint.getEndpointName();
		this.epr = epr;
	}

	public DocumentFragment getAsReference(QName operationName) {
		return this.epr;
	}

	public String getEndpointName() {
		return this.endpointName;
	}

	public QName[] getInterfaces() {
		return null;
	}

	public QName getServiceName() {
		return this.serviceName;
	}

	protected String getClassifier() {
		return "dynamic";
	}
}