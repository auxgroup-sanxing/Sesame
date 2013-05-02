package com.sanxing.sesame.engine.component;

import com.sanxing.sesame.exception.NotInitialisedYetException;
import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

public class BootstrapImpl implements Bootstrap {
	private static final Logger LOG = LoggerFactory.getLogger(BootstrapImpl.class);
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
		if (this.installContext == null) {
			throw new NotInitialisedYetException();
		}
		DocumentFragment fragment = this.installContext
				.getInstallationDescriptorExtension();
		if (fragment != null)
			LOG.debug("Installation Descriptor Extension Found");
		else
			LOG.debug("Installation Descriptor Extension Not Found !");
	}

	public void onUninstall() throws JBIException {
	}
}