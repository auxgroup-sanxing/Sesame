package com.sanxing.sesame.component;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.service.ServiceUnit;

public class UnitManagerProxy implements ServiceUnitManager {
	private static Logger LOG = LoggerFactory.getLogger(UnitManagerProxy.class);

	private Map<String, ServiceUnit> serviceUnits = new Hashtable();
	private ComponentSupport component;
	private ServiceUnitManager manager;

	public UnitManagerProxy(ComponentSupport component,
			ServiceUnitManager serviceUnitManager) {
		this.component = component;
		this.manager = serviceUnitManager;
	}

	public String deploy(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		return this.manager.deploy(serviceUnitName, serviceUnitRootPath);
	}

	public String undeploy(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		return this.manager.undeploy(serviceUnitName, serviceUnitRootPath);
	}

	public void init(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		try {
			ServiceUnit serviceUnit = new ServiceUnit(new File(
					serviceUnitRootPath));
			this.serviceUnits.put(serviceUnitName, serviceUnit);
		} catch (JBIException e) {
			if (e.getCause() != null) {
				Throwable t = e.getCause();
				LOG.trace(t.getMessage(), t);
			}
			throw this.component.taskFailure("init service unit "
					+ serviceUnitName, e.getMessage());
		}
		this.manager.init(serviceUnitName, serviceUnitRootPath);
	}

	public void shutDown(String serviceUnitName) throws DeploymentException {
		this.manager.shutDown(serviceUnitName);
		this.serviceUnits.remove(serviceUnitName);
	}

	public void start(String serviceUnitName) throws DeploymentException {
		this.manager.start(serviceUnitName);
	}

	public void stop(String serviceUnitName) throws DeploymentException {
		this.manager.stop(serviceUnitName);
	}

	public ServiceUnit getServiceUnit(String serviceUnitName) {
		return ((ServiceUnit) this.serviceUnits.get(serviceUnitName));
	}

	public ServiceUnit getServiceUnit(QName serviceName) {
		for (Iterator iter = this.serviceUnits.values().iterator(); iter
				.hasNext();) {
			ServiceUnit unit = (ServiceUnit) iter.next();
			if (serviceName.equals(unit.getServiceName())) {
				return unit;
			}
		}
		return null;
	}
}