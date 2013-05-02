package com.sanxing.sesame.mbean;

import javax.jbi.JBIException;
import javax.jbi.management.AdminServiceMBean;

public abstract interface ManagementContextMBean extends AdminServiceMBean {
	public abstract String startComponent(String paramString)
			throws JBIException;

	public abstract String stopComponent(String paramString)
			throws JBIException;

	public abstract String shutDownComponent(String paramString)
			throws JBIException;
}