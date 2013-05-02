package com.sanxing.sesame.component;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

import com.sanxing.sesame.exception.NotInitialisedYetException;

public class BaseBootstrap implements Bootstrap {
	private InstallationContext installContext;
	private ObjectName extensionMBeanName;

	public void cleanUp() throws JBIException {
	}

	public ObjectName getExtensionMBeanName() {
		return this.extensionMBeanName;
	}

	public void init(InstallationContext ctx) throws JBIException {
		this.installContext = ctx;
	}

	public void onInstall() throws JBIException {
		if (this.installContext == null)
			throw new NotInitialisedYetException();
	}

	public void onUninstall() throws JBIException {
	}
}