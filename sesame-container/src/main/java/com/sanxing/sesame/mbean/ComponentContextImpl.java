package com.sanxing.sesame.mbean;

import com.sanxing.sesame.container.ActivationSpec;
import com.sanxing.sesame.container.ComponentEnvironment;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import java.io.File;
import java.util.MissingResourceException;
import java.util.logging.Logger;
import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.management.MBeanNames;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

public class ComponentContextImpl implements ComponentContext, MBeanNames {
	private static final Log LOG = LogFactory
			.getLog(ComponentContextImpl.class);
	private ComponentNameSpace componentName;
	private ComponentEnvironment environment;
	private JBIContainer container;
	private Component component;
	private DeliveryChannel deliveryChannel;
	private ActivationSpec activationSpec;
	private boolean activated;

	public ComponentContextImpl(JBIContainer container,
			ComponentNameSpace componentName) {
		this.componentName = componentName;
		this.container = container;
	}

	public void activate(Component comp, ComponentEnvironment env,
			ActivationSpec spec) {
		this.component = comp;
		this.environment = env;
		this.activationSpec = spec;
		this.activated = true;
	}

	public ComponentNameSpace getComponentNameSpace() {
		return this.componentName;
	}

	public String getComponentName() {
		return this.componentName.getName();
	}

	public Component getComponent() {
		return this.component;
	}

	public ServiceEndpoint activateEndpoint(QName serviceName,
			String endpointName) throws JBIException {
		checkActivated();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Component: " + this.componentName.getName()
					+ " activated endpoint: " + serviceName + " : "
					+ endpointName);
		}
		return this.container.getRegistry().activateEndpoint(this, serviceName,
				endpointName);
	}

	public ServiceEndpoint[] availableEndpoints(QName serviceName)
			throws JBIException {
		checkActivated();
		return this.container.getRegistry().getEndpointsForService(serviceName);
	}

	public void deactivateEndpoint(ServiceEndpoint endpoint)
			throws JBIException {
		checkActivated();
		this.container.getRegistry().deactivateEndpoint(this,
				(InternalEndpoint) endpoint);
	}

	public DeliveryChannel getDeliveryChannel() {
		return this.deliveryChannel;
	}

	public String getJmxDomainName() {
		return this.container.getName();
	}

	public ObjectName createCustomComponentMBeanName(String customName) {
		return this.container.getManagementContext()
				.createCustomComponentMBeanName(customName,
						this.componentName.getName());
	}

	public MBeanNames getMBeanNames() {
		return this;
	}

	public MBeanServer getMBeanServer() {
		return this.container.getMBeanServer();
	}

	public InitialContext getNamingContext() {
		return this.container.getNamingContext();
	}

	public Object getTransactionManager() {
		return this.container.getTransactionManager();
	}

	public String getWorkspaceRoot() {
		if (this.environment.getWorkspaceRoot() != null) {
			return this.environment.getWorkspaceRoot().getAbsolutePath();
		}
		return null;
	}

	public JBIContainer getContainer() {
		return this.container;
	}

	public ComponentEnvironment getEnvironment() {
		return this.environment;
	}

	public void setEnvironment(ComponentEnvironment ce) {
		this.environment = ce;
	}

	public void setContainer(JBIContainer container) {
		this.container = container;
	}

	public void setDeliveryChannel(DeliveryChannel deliveryChannel) {
		this.deliveryChannel = deliveryChannel;
	}

	public void registerExternalEndpoint(ServiceEndpoint externalEndpoint)
			throws JBIException {
		checkActivated();
		if (externalEndpoint == null) {
			throw new IllegalArgumentException(
					"externalEndpoint should be non null");
		}
		this.container.getRegistry().registerExternalEndpoint(
				getComponentNameSpace(), externalEndpoint);
	}

	public void deregisterExternalEndpoint(ServiceEndpoint externalEndpoint)
			throws JBIException {
		checkActivated();
		this.container.getRegistry().deregisterExternalEndpoint(
				getComponentNameSpace(), externalEndpoint);
	}

	public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
		checkActivated();
		return this.container.getRegistry().resolveEndpointReference(epr);
	}

	public ServiceEndpoint getEndpoint(QName service, String name) {
		checkActivated();
		return this.container.getRegistry().getEndpoint(service, name);
	}

	public Document getEndpointDescriptor(ServiceEndpoint endpoint)
			throws JBIException {
		checkActivated();
		return this.container.getRegistry().getEndpointDescriptor(endpoint);
	}

	public ServiceEndpoint[] getEndpoints(QName interfaceName) {
		checkActivated();
		return this.container.getRegistry().getEndpointsForInterface(
				interfaceName);
	}

	public ServiceEndpoint[] getEndpointsForService(QName serviceName) {
		checkActivated();
		return this.container.getRegistry().getEndpointsForService(serviceName);
	}

	public ServiceEndpoint[] getExternalEndpoints(QName interfaceName) {
		checkActivated();
		return this.container.getRegistry().getExternalEndpoints(interfaceName);
	}

	public ServiceEndpoint[] getExternalEndpointsForService(QName serviceName) {
		checkActivated();
		return this.container.getRegistry().getExternalEndpointsForService(
				serviceName);
	}

	public String getInstallRoot() {
		if (this.environment.getInstallRoot() != null) {
			return this.environment.getInstallRoot().getAbsolutePath();
		}
		return null;
	}

	public Logger getLogger(String suffix, String filename)
			throws MissingResourceException, JBIException {
		String name = (suffix != null) ? suffix : "";

		return this.container.getLogger(name, filename);
	}

	public ActivationSpec getActivationSpec() {
		return this.activationSpec;
	}

	private void checkActivated() {
		if (!(this.activated))
			throw new IllegalStateException("ComponentContext not activated");
	}
}