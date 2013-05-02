package com.sanxing.adp;

import com.sanxing.sesame.exception.NotInitialisedYetException;
import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

public class BootstrapImpl implements Bootstrap {
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