package com.sanxing.sesame.mbean;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.container.ServiceAssemblyEnvironment;
import com.sanxing.sesame.deployment.Connection;
import com.sanxing.sesame.deployment.Connections;
import com.sanxing.sesame.deployment.Consumer;
import com.sanxing.sesame.deployment.Consumes;
import com.sanxing.sesame.deployment.DescriptorFactory;
import com.sanxing.sesame.deployment.Identification;
import com.sanxing.sesame.deployment.Provider;
import com.sanxing.sesame.deployment.ServiceAssembly;
import com.sanxing.sesame.deployment.Services;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.MBeanInfoProvider;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.util.XmlPersistenceSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ServiceAssemblyLifeCycle implements ServiceAssemblyMBean,
		MBeanInfoProvider {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceAssemblyLifeCycle.class);
	private ServiceAssembly serviceAssembly;
	private String currentState = "Shutdown";
	private ServiceUnitLifeCycle[] sus;
	private Registry registry;
	private PropertyChangeListener listener;
	private ServiceAssemblyEnvironment env;

	public ServiceAssemblyLifeCycle(ServiceAssembly sa,
			ServiceAssemblyEnvironment env, Registry registry) {
		this.serviceAssembly = sa;
		this.env = env;
		this.registry = registry;
	}

	protected void setServiceUnits(ServiceUnitLifeCycle[] serviceUnits) {
		this.sus = serviceUnits;
	}

	public String start() throws Exception {
		return start(true);
	}

	public synchronized String start(boolean writeState) throws Exception {
		LOG.info("Starting service assembly: " + getName());
		try {
			startConnections();
		} catch (JBIException e) {
			throw ManagementSupport.failure("start", e.getMessage());
		}

		List componentFailures = new ArrayList();
		for (int i = 0; i < this.sus.length; ++i) {
			if (!(this.sus[i].isShutDown()))
				continue;
			try {
				this.sus[i].init();
			} catch (DeploymentException e) {
				componentFailures.add(getComponentFailure(e, "start",
						this.sus[i].getComponentName()));
			}
		}

		for (int i = 0; i < this.sus.length; ++i) {
			if (!(this.sus[i].isStopped()))
				continue;
			try {
				this.sus[i].start();
			} catch (DeploymentException e) {
				componentFailures.add(getComponentFailure(e, "start",
						this.sus[i].getComponentName()));
			}
		}

		if (componentFailures.size() == 0) {
			this.currentState = "Started";
			if (writeState) {
				writeRunningState();
			}

			return ManagementSupport.createSuccessMessage("start");
		}

		throw ManagementSupport.failure("start", componentFailures);
	}

	public String stop() throws Exception {
		return stop(true, false);
	}

	public synchronized String stop(boolean writeState, boolean forceInit)
			throws Exception {
		LOG.info("Stopping service assembly: " + getName());

		stopConnections();

		List componentFailures = new ArrayList();
		if (forceInit) {
			for (int i = 0; i < this.sus.length; ++i) {
				try {
					this.sus[i].init();
				} catch (DeploymentException e) {
					componentFailures.add(getComponentFailure(e, "stop",
							this.sus[i].getComponentName()));
				}
			}
		}
		for (int i = 0; i < this.sus.length; ++i) {
			if (!(this.sus[i].isStarted()))
				continue;
			try {
				this.sus[i].stop();
			} catch (DeploymentException e) {
				componentFailures.add(getComponentFailure(e, "stop",
						this.sus[i].getComponentName()));
			}
		}

		if (componentFailures.size() == 0) {
			this.currentState = "Stopped";
			if (writeState) {
				writeRunningState();
			}

			return ManagementSupport.createSuccessMessage("stop");
		}
		throw ManagementSupport.failure("stop", componentFailures);
	}

	public String shutDown() throws Exception {
		return shutDown(true);
	}

	public synchronized String shutDown(boolean writeState) throws Exception {
		LOG.info("Shutting down service assembly: " + getName());
		List componentFailures = new ArrayList();
		for (int i = 0; i < this.sus.length; ++i) {
			if (!(this.sus[i].isStarted()))
				continue;
			try {
				this.sus[i].stop();
			} catch (DeploymentException e) {
				componentFailures.add(getComponentFailure(e, "shutDown",
						this.sus[i].getComponentName()));
			}
		}

		for (int i = 0; i < this.sus.length; ++i) {
			if (!(this.sus[i].isStopped()))
				continue;
			try {
				this.sus[i].shutDown();
			} catch (DeploymentException e) {
				componentFailures.add(getComponentFailure(e, "shutDown",
						this.sus[i].getComponentName()));
			}
		}

		if (componentFailures.size() == 0) {
			this.currentState = "Shutdown";
			if (writeState) {
				writeRunningState();
			}

			return ManagementSupport.createSuccessMessage("shutDown");
		}
		throw ManagementSupport.failure("shutDown", componentFailures);
	}

	public String getCurrentState() {
		return this.currentState;
	}

	boolean isShutDown() {
		return this.currentState.equals("Shutdown");
	}

	boolean isStopped() {
		return this.currentState.equals("Stopped");
	}

	boolean isStarted() {
		return this.currentState.equals("Started");
	}

	public String getName() {
		return this.serviceAssembly.getIdentification().getName();
	}

	public String getDescription() {
		return this.serviceAssembly.getIdentification().getDescription();
	}

	public ServiceAssembly getServiceAssembly() {
		return this.serviceAssembly;
	}

	public String getDescriptor() {
		File saDir = this.env.getInstallDir();
		return DescriptorFactory.getDescriptorAsText(saDir);
	}

	public String toString() {
		return "ServiceAssemblyLifeCycle[name=" + getName() + ",state="
				+ getCurrentState() + "]";
	}

	void writeRunningState() {
		try {
			if (this.env.getStateFile() != null) {
				String state = getCurrentState();
				Properties props = new Properties();
				props.setProperty("state", state);
				XmlPersistenceSupport.write(this.env.getStateFile(), props);
			}
		} catch (IOException e) {
			LOG.error(
					"Failed to write current running state for ServiceAssembly: "
							+ getName(), e);
		}
	}

	String getRunningStateFromStore() {
		try {
			if ((this.env.getStateFile() != null)
					&& (this.env.getStateFile().exists())) {
				Properties props = (Properties) XmlPersistenceSupport
						.read(this.env.getStateFile());
				return props.getProperty("state", "Shutdown");
			}
		} catch (Exception e) {
			LOG.error(
					"Failed to read current running state for ServiceAssembly: "
							+ getName(), e);
		}
		return null;
	}

	public synchronized void restore() throws Exception {
		String state = getRunningStateFromStore();
		if ("Started".equals(state)) {
			start(false);
		} else {
			stop(false, true);
			if ("Shutdown".equals(state))
				shutDown(false);
		}
	}

	public ServiceUnitLifeCycle[] getDeployedSUs() {
		return this.sus;
	}

	protected void startConnections() throws JBIException {
		if ((this.serviceAssembly.getConnections() == null)
				|| (this.serviceAssembly.getConnections().getConnections() == null)) {
			return;
		}
		Connection[] connections = this.serviceAssembly.getConnections()
				.getConnections();
		for (int i = 0; i < connections.length; ++i)
			if (connections[i].getConsumer().getInterfaceName() != null) {
				QName fromItf = connections[i].getConsumer().getInterfaceName();
				QName toSvc = connections[i].getProvider().getServiceName();
				String toEp = connections[i].getProvider().getEndpointName();
				this.registry.registerInterfaceConnection(fromItf, toSvc, toEp);
			} else {
				QName fromSvc = connections[i].getConsumer().getServiceName();
				String fromEp = connections[i].getConsumer().getEndpointName();
				QName toSvc = connections[i].getProvider().getServiceName();
				String toEp = connections[i].getProvider().getEndpointName();
				String link = getLinkType(fromSvc, fromEp);
				this.registry.registerEndpointConnection(fromSvc, fromEp,
						toSvc, toEp, link);
			}
	}

	protected String getLinkType(QName svc, String ep) {
		for (int i = 0; i < this.sus.length; ++i) {
			Services s = this.sus[i].getServices();
			if ((s != null) && (s.getConsumes() != null)) {
				Consumes[] consumes = s.getConsumes();
				for (int j = 0; j < consumes.length; ++j) {
					if ((svc.equals(consumes[j].getServiceName()))
							&& (ep.equals(consumes[j].getEndpointName()))) {
						return consumes[j].getLinkType();
					}
				}
			}
		}
		return null;
	}

	protected void stopConnections() {
		if ((this.serviceAssembly.getConnections() == null)
				|| (this.serviceAssembly.getConnections().getConnections() == null)) {
			return;
		}
		Connection[] connections = this.serviceAssembly.getConnections()
				.getConnections();
		for (int i = 0; i < connections.length; ++i)
			if (connections[i].getConsumer().getInterfaceName() != null) {
				QName fromItf = connections[i].getConsumer().getInterfaceName();
				this.registry.unregisterInterfaceConnection(fromItf);
			} else {
				QName fromSvc = connections[i].getConsumer().getServiceName();
				String fromEp = connections[i].getConsumer().getEndpointName();
				this.registry.unregisterEndpointConnection(fromSvc, fromEp);
			}
	}

	protected Element getComponentFailure(Exception exception, String task,
			String component) {
		Element result = null;
		String resultMsg = exception.getMessage();
		try {
			Document doc = parse(resultMsg);
			result = getElement(doc, "component-task-result");
		} catch (Exception e) {
			LOG.warn("Could not parse result exception", e);
		}
		if (result == null) {
			result = ManagementSupport.createComponentFailure(task, component,
					"Unable to parse result string", exception);
		}

		return result;
	}

	protected Document parse(String result)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(result)));
	}

	protected Element getElement(Document doc, String name) {
		NodeList l = doc.getElementsByTagNameNS(
				"http://java.sun.com/xml/ns/jbi/management-message", name);
		return ((Element) l.item(0));
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(), "currentState",
				"current state of the assembly");
		helper.addAttribute(getObjectToManage(), "name", "name of the assembly");
		helper.addAttribute(getObjectToManage(), "description",
				"description of the assembly");
		helper.addAttribute(getObjectToManage(), "serviceUnits",
				"list of service units contained in this assembly");
		return helper.getAttributeInfos();
	}

	public MBeanOperationInfo[] getOperationInfos() throws JMException {
		OperationInfoHelper helper = new OperationInfoHelper();
		helper.addOperation(getObjectToManage(), "start", "start the assembly");
		helper.addOperation(getObjectToManage(), "stop", "stop the assembly");
		helper.addOperation(getObjectToManage(), "shutDown",
				"shutdown the assembly");
		helper.addOperation(getObjectToManage(), "getDescriptor",
				"retrieve the jbi descriptor for this assembly");
		return helper.getOperationInfos();
	}

	public Object getObjectToManage() {
		return this;
	}

	public String getType() {
		return "ServiceAssembly";
	}

	public String getSubType() {
		return null;
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

	public ObjectName[] getServiceUnits() {
		ObjectName[] names = new ObjectName[this.sus.length];
		for (int i = 0; i < names.length; ++i) {
			names[i] = this.registry.getContainer().getManagementContext()
					.createObjectName(this.sus[i]);
		}
		return names;
	}

	public ServiceAssemblyEnvironment getEnvironment() {
		return this.env;
	}
}