package com.sanxing.sesame.component;

import java.io.File;
import java.io.IOException;
import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.servicedesc.ServiceEndpoint;
import org.w3c.dom.DocumentFragment;

public class SchemaComponent extends ComponentSupport implements
		ServiceUnitManager {
	public void init(ComponentContext componentContext) throws JBIException {
		super.init(componentContext);
	}

	public boolean isBindingComponent() {
		return false;
	}

	public boolean isEngineComponent() {
		return false;
	}

	public String deploy(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		File unitFolder = new File(serviceUnitRootPath);
		File[] files = unitFolder.listFiles();
		try {
			for (File file : files) {
				if (!(file.renameTo(new File(unitFolder.getParentFile(), file
						.getName()))))
					throw new IOException("Failed to move " + file + " to "
							+ unitFolder.getParent());
			}
		} catch (IOException e) {
			throw taskFailure("deploy", e.getMessage());
		}
		return null;
	}

	public String undeploy(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		return null;
	}

	public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
		return null;
	}

	public void init(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
	}

	public void start(String serviceUnitName) throws DeploymentException {
	}

	public void stop(String serviceUnitName) throws DeploymentException {
	}

	public void shutDown(String serviceUnitName) throws DeploymentException {
	}

	public ServiceUnitManager getServiceUnitManager() {
		return this;
	}

	protected ServiceUnitManager createServiceUnitManager() {
		return null;
	}

	public String getDescription() {
		return "Schema 管理组件";
	}
}