package com.sanxing.sesame.component;

import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;

public class ServiceUnitManagerSupport implements ServiceUnitManager {
	public String deploy(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		return "<component-task-result>" + serviceUnitRootPath
				+ "</component-task-result>";
	}

	public void init(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
	}

	public void shutDown(String serviceUnitName) throws DeploymentException {
	}

	public void start(String serviceUnitName) throws DeploymentException {
	}

	public void stop(String serviceUnitName) throws DeploymentException {
	}

	public String undeploy(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		return serviceUnitRootPath;
	}
}