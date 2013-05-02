package com.sanxing.sesame.servicedesc;

import com.sanxing.sesame.mbean.ComponentNameSpace;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import org.w3c.dom.DocumentFragment;

public class ExternalEndpoint extends AbstractEndpoint {
	private static final long serialVersionUID = 4257588916448457889L;
	protected final ServiceEndpoint se;

	public ExternalEndpoint(ComponentNameSpace cns, ServiceEndpoint se) {
		super(cns);
		this.se = se;
	}

	public DocumentFragment getAsReference(QName operationName) {
		return this.se.getAsReference(operationName);
	}

	public String getEndpointName() {
		return this.se.getEndpointName();
	}

	public QName[] getInterfaces() {
		return this.se.getInterfaces();
	}

	public QName getServiceName() {
		return this.se.getServiceName();
	}

	protected String getClassifier() {
		return "external";
	}
}