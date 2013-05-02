package com.sanxing.sesame.mbean;

import com.sanxing.sesame.deployment.Descriptor;
import com.sanxing.sesame.deployment.DescriptorFactory;
import com.sanxing.sesame.deployment.Identification;
import com.sanxing.sesame.deployment.ServiceUnit;
import com.sanxing.sesame.deployment.Services;
import com.sanxing.sesame.deployment.Target;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.MBeanInfoProvider;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.management.OperationInfoHelper;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceUnitLifeCycle implements ServiceUnitMBean,
		MBeanInfoProvider {
	private static final Logger LOG = LoggerFactory
			.getLogger(ServiceUnitLifeCycle.class);
	private ServiceUnit serviceUnit;
	private String currentState = "Shutdown";
	private String serviceAssembly;
	private Registry registry;
	private PropertyChangeListener listener;
	private Services services;
	private File rootDir;

	public ServiceUnitLifeCycle(ServiceUnit serviceUnit,
			String serviceAssembly, Registry registry, File rootDir) {
		this.serviceUnit = serviceUnit;
		this.serviceAssembly = serviceAssembly;
		this.registry = registry;
		this.rootDir = rootDir;
		Descriptor d = DescriptorFactory.buildDescriptor(rootDir);
		if (d != null)
			this.services = d.getServices();
	}

	public void init() throws DeploymentException {
		LOG.info("Initializing service unit: " + getName());
		checkComponentStarted("init");
		ServiceUnitManager sum = getServiceUnitManager();
		File path = getServiceUnitRootPath();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					getComponentClassLoader());
			sum.init(getName(), path.getAbsolutePath());
		} finally {
			Thread.currentThread().setContextClassLoader(cl);
		}
		this.currentState = "Stopped";
	}

	public void start() throws DeploymentException {
		LOG.info("Starting service unit: " + getName());
		checkComponentStarted("start");
		ServiceUnitManager sum = getServiceUnitManager();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					getComponentClassLoader());
			sum.start(getName());
		} finally {
			Thread.currentThread().setContextClassLoader(cl);
		}
		this.currentState = "Started";
	}

	public void stop() throws DeploymentException {
		LOG.info("Stopping service unit: " + getName());
		checkComponentStarted("stop");
		ServiceUnitManager sum = getServiceUnitManager();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					getComponentClassLoader());
			sum.stop(getName());
		} finally {
			Thread.currentThread().setContextClassLoader(cl);
		}
		this.currentState = "Stopped";
	}

	public void shutDown() throws DeploymentException {
		LOG.info("Shutting down service unit: " + getName());
		checkComponentStartedOrStopped("shutDown");
		ServiceUnitManager sum = getServiceUnitManager();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					getComponentClassLoader());
			sum.shutDown(getName());
		} finally {
			Thread.currentThread().setContextClassLoader(cl);
		}
		this.currentState = "Shutdown";
	}

	public String getCurrentState() {
		return this.currentState;
	}

	public boolean isShutDown() {
		return this.currentState.equals("Shutdown");
	}

	public boolean isStopped() {
		return this.currentState.equals("Stopped");
	}

	public boolean isStarted() {
		return this.currentState.equals("Started");
	}

	public String getName() {
		return this.serviceUnit.getIdentification().getName();
	}

	public String getDescription() {
		return this.serviceUnit.getIdentification().getDescription();
	}

	public String getComponentName() {
		return this.serviceUnit.getTarget().getComponentName();
	}

	public String getServiceAssembly() {
		return this.serviceAssembly;
	}

	public String getDescriptor() {
		File suDir = getServiceUnitRootPath();
		return DescriptorFactory.getDescriptorAsText(suDir);
	}

	public Services getServices() {
		return this.services;
	}

	protected void checkComponentStarted(String task)
			throws DeploymentException {
		String componentName = getComponentName();
		String suName = getName();
		ComponentMBeanImpl lcc = this.registry.getComponent(componentName);
		if (lcc == null) {
			throw ManagementSupport.componentFailure("deploy", componentName,
					"Target component " + componentName + " for service unit "
							+ suName + " is not installed");
		}

		if (!(lcc.isStarted())) {
			throw ManagementSupport.componentFailure("deploy", componentName,
					"Target component " + componentName + " for service unit "
							+ suName + " is not started");
		}

		if (lcc.getServiceUnitManager() == null)
			throw ManagementSupport.componentFailure("deploy", componentName,
					"Target component " + componentName + " for service unit "
							+ suName + " does not accept deployments");
	}

	protected void checkComponentStartedOrStopped(String task)
			throws DeploymentException {
		String componentName = getComponentName();
		String suName = getName();
		ComponentMBeanImpl lcc = this.registry.getComponent(componentName);
		if (lcc == null) {
			throw ManagementSupport.componentFailure("deploy", componentName,
					"Target component " + componentName + " for service unit "
							+ suName + " is not installed");
		}

		if ((!(lcc.isStarted())) && (!(lcc.isStopped()))) {
			throw ManagementSupport.componentFailure("deploy", componentName,
					"Target component " + componentName + " for service unit "
							+ suName + " is not started");
		}

		if (lcc.getServiceUnitManager() == null)
			throw ManagementSupport.componentFailure("deploy", componentName,
					"Target component " + componentName + " for service unit "
							+ suName + " does not accept deployments");
	}

	protected File getServiceUnitRootPath() {
		return this.rootDir;
	}

	protected ServiceUnitManager getServiceUnitManager() {
		ComponentMBeanImpl lcc = this.registry.getComponent(getComponentName());
		return lcc.getServiceUnitManager();
	}

	protected ClassLoader getComponentClassLoader() {
		ComponentMBeanImpl lcc = this.registry.getComponent(getComponentName());

		return lcc.getComponent().getClass().getClassLoader();
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(), "currentState",
				"current state of the service unit");
		helper.addAttribute(getObjectToManage(), "name",
				"name of the service unit");
		helper.addAttribute(getObjectToManage(), "componentName",
				"component name of the service unit");
		helper.addAttribute(getObjectToManage(), "serviceAssembly",
				"service assembly name of the service unit");
		helper.addAttribute(getObjectToManage(), "description",
				"description of the service unit");
		return helper.getAttributeInfos();
	}

	public MBeanOperationInfo[] getOperationInfos() throws JMException {
		OperationInfoHelper helper = new OperationInfoHelper();
		helper.addOperation(getObjectToManage(), "getDescriptor",
				"retrieve the jbi descriptor for this unit");
		return helper.getOperationInfos();
	}

	public Object getObjectToManage() {
		return this;
	}

	public String getType() {
		return "ServiceUnitAdaptor";
	}

	public String getSubType() {
		return getComponentName();
	}

	public void setPropertyChangeListener(PropertyChangeListener l) {
		this.listener = l;
	}

	protected void firePropertyChanged(String name, Object oldValue,
			Object newValue) {
		PropertyChangeListener l = this.listener;
		if (l != null) {
			PropertyChangeEvent event = new PropertyChangeEvent(this, name,
					oldValue, newValue);
			l.propertyChange(event);
		}
	}

	public String getKey() {
		return getComponentName() + "/" + getName();
	}
}