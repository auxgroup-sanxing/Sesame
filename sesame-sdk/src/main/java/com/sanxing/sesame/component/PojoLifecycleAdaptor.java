package com.sanxing.sesame.component;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

public class PojoLifecycleAdaptor implements ComponentLifeCycle {
	private Object pojo;
	private QName service;
	private String endpoint;
	private ComponentContext context;
	private ObjectName extensionMBeanName;

	public PojoLifecycleAdaptor(Object pojo, QName service, String endpoint) {
		this.pojo = pojo;
		this.service = service;
		this.endpoint = endpoint;
	}

	public ObjectName getExtensionMBeanName() {
		return this.extensionMBeanName;
	}

	public void init(ComponentContext ctx) throws JBIException {
		this.context = ctx;
		if ((this.service != null) && (this.endpoint != null))
			ctx.activateEndpoint(this.service, this.endpoint);
	}

	public void shutDown() throws JBIException {
	}

	public void start() throws JBIException {
	}

	public void stop() throws JBIException {
	}

	public Object getPojo() {
		return this.pojo;
	}

	public void setExtensionMBeanName(ObjectName extensionMBeanName) {
		this.extensionMBeanName = extensionMBeanName;
	}

	public ComponentContext getContext() {
		return this.context;
	}
}