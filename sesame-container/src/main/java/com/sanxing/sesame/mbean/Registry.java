package com.sanxing.sesame.mbean;

import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.container.ServiceAssemblyEnvironment;
import com.sanxing.sesame.deployment.ServiceAssembly;
import com.sanxing.sesame.deployment.ServiceUnit;
import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.resolver.URIResolver;
import com.sanxing.sesame.servicedesc.AbstractEndpoint;
import com.sanxing.sesame.servicedesc.DynamicEndpoint;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import com.sanxing.sesame.util.W3CUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Registry extends BaseSystemService implements RegistryMBean {
	private static final Logger LOG = LoggerFactory.getLogger(Registry.class);
	private ComponentRegistry componentRegistry;
	private EndpointRegistry endpointRegistry;
	private ServiceAssemblyRegistry serviceAssemblyRegistry;
	private Map<String, SharedLibrary> sharedLibraries;
	private Map<String, ServiceUnitLifeCycle> serviceUnits;
	private List<ServiceAssemblyLifeCycle> pendingAssemblies;
	private List<ComponentMBeanImpl> pendingComponents;
	private Executor executor;

	public Registry() {
		this.componentRegistry = new ComponentRegistry(this);
		this.endpointRegistry = new EndpointRegistry(this);

		this.serviceAssemblyRegistry = new ServiceAssemblyRegistry(this);
		this.serviceUnits = new ConcurrentHashMap();
		this.pendingAssemblies = new CopyOnWriteArrayList();
		this.sharedLibraries = new ConcurrentHashMap();
		this.pendingComponents = new CopyOnWriteArrayList();
	}

	public String getDescription() {
		return "Registry of Components/SU's and Endpoints";
	}

	protected Class getServiceMBean() {
		return RegistryMBean.class;
	}

	public ComponentRegistry getComponentRegistry() {
		return this.componentRegistry;
	}

	public EndpointRegistry getEndpointRegistry() {
		return this.endpointRegistry;
	}

	public void init(JBIContainer container) throws JBIException {
		super.init(container);
		this.executor = container.getExecutorFactory().createExecutor(
				"services.registry");
	}

	public void start() throws JBIException {
		this.componentRegistry.start();
		this.serviceAssemblyRegistry.start();
		super.start();
	}

	public void stop() throws JBIException {
		this.serviceAssemblyRegistry.stop();
		this.componentRegistry.stop();
		super.stop();
	}

	public void shutDown() throws JBIException {
		this.serviceAssemblyRegistry.shutDown();
		this.componentRegistry.shutDown();
		super.shutDown();
		this.container.getManagementContext().unregisterMBean(this);
	}

	protected EnvironmentContext getEnvironmentContext() {
		return this.container.getEnvironmentContext();
	}

	protected InternalEndpoint matchEndpointByName(ServiceEndpoint[] endpoints,
			String endpointName) {
		InternalEndpoint result = null;
		if ((endpoints != null) && (endpointName != null)
				&& (endpointName.length() > 0)) {
			for (int i = 0; i < endpoints.length; ++i) {
				if (endpoints[i].getEndpointName().equals(endpointName)) {
					result = (InternalEndpoint) endpoints[i];
					break;
				}
			}
		}
		return result;
	}

	public ServiceEndpoint activateEndpoint(ComponentContextImpl context,
			QName serviceName, String endpointName) throws JBIException {
		return this.endpointRegistry.registerInternalEndpoint(context,
				serviceName, endpointName);
	}

	public ServiceEndpoint[] getEndpointsForComponent(ComponentNameSpace cns) {
		return this.endpointRegistry.getEndpointsForComponent(cns);
	}

	public ServiceEndpoint[] getEndpointsForInterface(QName interfaceName) {
		return this.endpointRegistry.getEndpointsForInterface(interfaceName);
	}

	public void deactivateEndpoint(ComponentContext provider,
			InternalEndpoint serviceEndpoint) {
		this.endpointRegistry.unregisterInternalEndpoint(provider,
				serviceEndpoint);
	}

	public Document getEndpointDescriptor(ServiceEndpoint endpoint)
			throws JBIException {
		if (!(endpoint instanceof AbstractEndpoint)) {
			throw new JBIException(
					"Descriptors can not be queried for external endpoints");
		}
		AbstractEndpoint se = (AbstractEndpoint) endpoint;

		ComponentMBeanImpl component = getComponent(se.getComponentNameSpace());
		return component.getComponent().getServiceDescription(endpoint);
	}

	public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
		for (ComponentMBeanImpl connector : getComponents()) {
			ServiceEndpoint se = connector.getComponent()
					.resolveEndpointReference(epr);
			if (se != null) {
				return new DynamicEndpoint(connector.getComponentNameSpace(),
						se, epr);
			}
		}
		ServiceEndpoint se = resolveInternalEPR(epr);
		if (se != null) {
			return se;
		}
		return resolveStandardEPR(epr);
	}

	public ServiceEndpoint resolveInternalEPR(DocumentFragment epr) {
		if (epr == null) {
			throw new NullPointerException(
					"resolveInternalEPR(epr) called with null epr.");
		}
		NodeList nl = epr.getChildNodes();
		for (int i = 0; i < nl.getLength(); ++i) {
			Node n = nl.item(i);
			if (n.getNodeType() != 1) {
				continue;
			}
			Element el = (Element) n;

			if (el.getNamespaceURI() == null)
				continue;
			if (!(el.getNamespaceURI()
					.equals("http://java.sun.com/jbi/end-point-reference"))) {
				continue;
			}

			if (el.getLocalName() == null)
				continue;
			if (!(el.getLocalName().equals("end-point-reference"))) {
				continue;
			}
			String serviceName = el.getAttributeNS(el.getNamespaceURI(),
					"service-name");

			QName serviceQName = W3CUtil.createQName(el, serviceName);
			String endpointName = el.getAttributeNS(el.getNamespaceURI(),
					"end-point-name");
			return getInternalEndpoint(serviceQName, endpointName);
		}
		return null;
	}

	public ServiceEndpoint resolveStandardEPR(DocumentFragment epr) {
		try {
			NodeList children = epr.getChildNodes();
			for (int i = 0; i < children.getLength(); ++i) {
				Node n = children.item(i);
				if (n.getNodeType() != 1) {
					continue;
				}
				Element elem = (Element) n;
				String[] namespaces = { "http://www.w3.org/2005/08/addressing",
						"http://schemas.xmlsoap.org/ws/2004/08/addressing",
						"http://schemas.xmlsoap.org/ws/2004/03/addressing",
						"http://schemas.xmlsoap.org/ws/2003/03/addressing" };

				NodeList nl = null;
				for (int ns = 0; ns < namespaces.length; ++ns) {
					NodeList tnl = elem.getElementsByTagNameNS(namespaces[ns],
							"Address");
					if (tnl.getLength() == 1) {
						nl = tnl;
						break;
					}
				}
				if (nl != null) {
					Element address = (Element) nl.item(0);
					String uri = W3CUtil.getElementText(address);
					if (uri != null) {
						uri = uri.trim();
						if (uri.startsWith("endpoint:")) {
							uri = uri.substring("endpoint:".length());
							String[] parts = URIResolver.split3(uri);
							return getInternalEndpoint(
									new QName(parts[0], parts[1]), parts[2]);
						}
						if (uri.startsWith("service:")) {
							uri = uri.substring("service:".length());
							String[] parts = URIResolver.split2(uri);
							return getEndpoint(new QName(parts[0], parts[1]),
									parts[1]);
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.debug("Unable to resolve EPR: " + e);
		}
		return null;
	}

	public void registerExternalEndpoint(ComponentNameSpace cns,
			ServiceEndpoint externalEndpoint) throws JBIException {
		if (externalEndpoint != null)
			this.endpointRegistry.registerExternalEndpoint(cns,
					externalEndpoint);
	}

	public void deregisterExternalEndpoint(ComponentNameSpace cns,
			ServiceEndpoint externalEndpoint) {
		this.endpointRegistry.unregisterExternalEndpoint(cns, externalEndpoint);
	}

	public ServiceEndpoint getEndpoint(QName service, String name) {
		return this.endpointRegistry.getEndpoint(service, name);
	}

	public ServiceEndpoint getInternalEndpoint(QName service, String name) {
		return this.endpointRegistry.getInternalEndpoint(service, name);
	}

	public ServiceEndpoint[] getEndpointsForService(QName serviceName) {
		return this.endpointRegistry.getEndpointsForService(serviceName);
	}

	public ServiceEndpoint[] getExternalEndpoints(QName interfaceName) {
		return this.endpointRegistry
				.getExternalEndpointsForInterface(interfaceName);
	}

	public ServiceEndpoint[] getExternalEndpointsForService(QName serviceName) {
		return this.endpointRegistry
				.getExternalEndpointsForService(serviceName);
	}

	public ComponentMBeanImpl registerComponent(ComponentNameSpace name,
			String description, Component component, boolean binding,
			boolean service, String[] sharedLibs) throws JBIException {
		return this.componentRegistry.registerComponent(name, description,
				component, binding, service, sharedLibs);
	}

	public void deregisterComponent(ComponentMBeanImpl component) {
		this.componentRegistry.deregisterComponent(component);
	}

	public Collection<ComponentMBeanImpl> getComponents() {
		return this.componentRegistry.getComponents();
	}

	public ComponentMBeanImpl getComponent(ComponentNameSpace cns) {
		return this.componentRegistry.getComponent(cns);
	}

	public ComponentMBeanImpl getComponent(String name) {
		ComponentNameSpace cns = new ComponentNameSpace(
				this.container.getName(), name);
		return getComponent(cns);
	}

	public ObjectName[] getEngineComponents() {
		ObjectName[] result = null;
		List tmpList = new ArrayList();
		for (ComponentMBeanImpl lcc : getComponents()) {
			if ((!(lcc.isPojo())) && (lcc.isService())
					&& (lcc.getMBeanName() != null)) {
				tmpList.add(lcc.getMBeanName());
			}
		}
		result = new ObjectName[tmpList.size()];
		tmpList.toArray(result);
		return result;
	}

	public ObjectName[] getBindingComponents() {
		ObjectName[] result = null;
		List tmpList = new ArrayList();
		for (ComponentMBeanImpl lcc : getComponents()) {
			if ((!(lcc.isPojo())) && (lcc.isBinding())
					&& (lcc.getMBeanName() != null)) {
				tmpList.add(lcc.getMBeanName());
			}
		}
		result = new ObjectName[tmpList.size()];
		tmpList.toArray(result);
		return result;
	}

	public ObjectName[] getPojoComponents() {
		ObjectName[] result = null;
		List tmpList = new ArrayList();
		for (ComponentMBeanImpl lcc : getComponents()) {
			if ((lcc.isPojo()) && (lcc.getMBeanName() != null)) {
				tmpList.add(lcc.getMBeanName());
			}
		}
		result = new ObjectName[tmpList.size()];
		tmpList.toArray(result);
		return result;
	}

	public ServiceAssemblyLifeCycle registerServiceAssembly(ServiceAssembly sa,
			ServiceAssemblyEnvironment env) throws DeploymentException {
		return this.serviceAssemblyRegistry.register(sa, env);
	}

	public ServiceAssemblyLifeCycle registerServiceAssembly(ServiceAssembly sa,
			String[] suKeys, ServiceAssemblyEnvironment env)
			throws DeploymentException {
		return this.serviceAssemblyRegistry.register(sa, suKeys, env);
	}

	public boolean unregisterServiceAssembly(String saName) {
		return this.serviceAssemblyRegistry.unregister(saName);
	}

	public ServiceAssemblyLifeCycle getServiceAssembly(String saName) {
		return this.serviceAssemblyRegistry.getServiceAssembly(saName);
	}

	public ServiceUnitLifeCycle[] getDeployedServiceUnits(String componentName) {
		List tmpList = new ArrayList();
		for (ServiceUnitLifeCycle su : this.serviceUnits.values()) {
			if (su.getComponentName().equals(componentName)) {
				tmpList.add(su);
			}
		}
		ServiceUnitLifeCycle[] result = new ServiceUnitLifeCycle[tmpList.size()];
		tmpList.toArray(result);
		return result;
	}

	public Collection<ServiceUnitLifeCycle> getServiceUnits() {
		return this.serviceUnits.values();
	}

	public Collection<ServiceAssemblyLifeCycle> getServiceAssemblies() {
		return this.serviceAssemblyRegistry.getServiceAssemblies();
	}

	public String[] getDeployedServiceAssemblies() {
		return this.serviceAssemblyRegistry.getDeployedServiceAssemblies();
	}

	public String[] getDeployedServiceAssembliesForComponent(
			String componentName) {
		return this.serviceAssemblyRegistry
				.getDeployedServiceAssembliesForComponent(componentName);
	}

	public String[] getComponentsForDeployedServiceAssembly(String saName) {
		return this.serviceAssemblyRegistry
				.getComponentsForDeployedServiceAssembly(saName);
	}

	public boolean isSADeployedServiceUnit(String componentName, String suName) {
		return this.serviceAssemblyRegistry.isDeployedServiceUnit(
				componentName, suName);
	}

	public ServiceUnitLifeCycle getServiceUnit(String suKey) {
		return ((ServiceUnitLifeCycle) this.serviceUnits.get(suKey));
	}

	public String registerServiceUnit(ServiceUnit su, String saName, File suDir) {
		ServiceUnitLifeCycle sulc = new ServiceUnitLifeCycle(su, saName, this,
				suDir);
		this.serviceUnits.put(sulc.getKey(), sulc);
		try {
			ObjectName objectName = getContainer().getManagementContext()
					.createObjectName(sulc);
			getContainer().getManagementContext().registerMBean(objectName,
					sulc, ServiceUnitMBean.class);
		} catch (JMException e) {
			LOG.error("Could not register MBean for service unit", e);
		}
		return sulc.getKey();
	}

	public void unregisterServiceUnit(String suKey) {
		ServiceUnitLifeCycle sulc = (ServiceUnitLifeCycle) this.serviceUnits
				.remove(suKey);
		if (sulc == null)
			return;
		try {
			getContainer().getManagementContext().unregisterMBean(sulc);
		} catch (JBIException e) {
			LOG.error("Could not unregister MBean for service unit", e);
		}
	}

	public void registerSharedLibrary(
			com.sanxing.sesame.deployment.SharedLibrary sl,
			File installationDir) {
		SharedLibrary library = new SharedLibrary(sl, installationDir);
		this.sharedLibraries.put(library.getName(), library);
		try {
			ObjectName objectName = getContainer().getManagementContext()
					.createObjectName(library);
			getContainer().getManagementContext().registerMBean(objectName,
					library, SharedLibraryMBean.class);
		} catch (JMException e) {
			LOG.error("Could not register MBean for service unit", e);
		}
		checkPendingComponents();
	}

	public void unregisterSharedLibrary(String name) {
		SharedLibrary sl = (SharedLibrary) this.sharedLibraries.remove(name);
		if (sl == null)
			return;
		try {
			getContainer().getManagementContext().unregisterMBean(sl);
			sl.dispose();
		} catch (JBIException e) {
			LOG.error("Could not unregister MBean for shared library", e);
		}
	}

	public SharedLibrary getSharedLibrary(String name) {
		return ((SharedLibrary) this.sharedLibraries.get(name));
	}

	public Collection<SharedLibrary> getSharedLibraries() {
		return this.sharedLibraries.values();
	}

	public void registerEndpointConnection(QName fromSvc, String fromEp,
			QName toSvc, String toEp, String link) throws JBIException {
		this.endpointRegistry.registerEndpointConnection(fromSvc, fromEp,
				toSvc, toEp, link);
	}

	public void unregisterEndpointConnection(QName fromSvc, String fromEp) {
		this.endpointRegistry.unregisterEndpointConnection(fromSvc, fromEp);
	}

	public void registerInterfaceConnection(QName fromItf, QName toSvc,
			String toEp) throws JBIException {
		this.endpointRegistry.registerInterfaceConnection(fromItf, toSvc, toEp);
	}

	public void unregisterInterfaceConnection(QName fromItf) {
		this.endpointRegistry.unregisterInterfaceConnection(fromItf);
	}

	public void registerRemoteEndpoint(ServiceEndpoint endpoint) {
		this.endpointRegistry
				.registerRemoteEndpoint((InternalEndpoint) endpoint);
	}

	public void unregisterRemoteEndpoint(ServiceEndpoint endpoint) {
		this.endpointRegistry
				.unregisterRemoteEndpoint((InternalEndpoint) endpoint);
	}

	public void checkPendingAssemblies() {
		this.executor.execute(new Runnable() {
			public void run() {
				Registry.this.startPendingAssemblies();
			}
		});
	}

	public void addPendingAssembly(ServiceAssemblyLifeCycle sa) {
		if (!(this.pendingAssemblies.contains(sa)))
			this.pendingAssemblies.add(sa);
	}

	protected synchronized void startPendingAssemblies() {
		for (ServiceAssemblyLifeCycle sa : this.pendingAssemblies) {
			ServiceUnitLifeCycle[] sus = sa.getDeployedSUs();
			boolean ok = true;
			for (int i = 0; i < sus.length; ++i) {
				ComponentMBeanImpl c = getComponent(sus[i].getComponentName());
				if ((c == null) || (!(c.isStarted()))) {
					ok = false;
					break;
				}
			}
			if (ok)
				try {
					sa.restore();
					this.pendingAssemblies.remove(sa);
				} catch (Exception e) {
					LOG.error("Error trying to restore service assembly state",
							e);
				}
		}
	}

	public void checkPendingComponents() {
		this.executor.execute(new Runnable() {
			public void run() {
				Registry.this.startPendingComponents();
			}
		});
	}

	public void addPendingComponent(ComponentMBeanImpl comp) {
		if (!(this.pendingComponents.contains(comp)))
			this.pendingComponents.add(comp);
	}

	protected synchronized void startPendingComponents() {
		ComponentMBeanImpl lcc;
		for (Iterator i$ = this.pendingComponents.iterator(); i$.hasNext(); lcc = (ComponentMBeanImpl) i$
				.next())
			;
	}

	public ObjectName[] getComponentNames() {
		List tmpList = new ArrayList();
		for (ComponentMBeanImpl lcc : getComponents()) {
			tmpList.add(this.container.getManagementContext().createObjectName(
					lcc));
		}
		return ((ObjectName[]) tmpList.toArray(new ObjectName[tmpList.size()]));
	}

	public ObjectName[] getEndpointNames() {
		List tmpList = new ArrayList();
		for (Endpoint ep : this.container.getRegistry().getEndpointRegistry()
				.getEndpointMBeans()) {
			tmpList.add(this.container.getManagementContext().createObjectName(
					ep));
		}
		return ((ObjectName[]) tmpList.toArray(new ObjectName[tmpList.size()]));
	}

	public ObjectName[] getServiceAssemblyNames() {
		List tmpList = new ArrayList();
		for (ServiceAssemblyLifeCycle sa : getServiceAssemblies()) {
			tmpList.add(this.container.getManagementContext().createObjectName(
					sa));
		}
		return ((ObjectName[]) tmpList.toArray(new ObjectName[tmpList.size()]));
	}

	public ObjectName[] getServiceUnitNames() {
		List tmpList = new ArrayList();
		for (ServiceUnitLifeCycle su : this.serviceUnits.values()) {
			tmpList.add(this.container.getManagementContext().createObjectName(
					su));
		}
		return ((ObjectName[]) tmpList.toArray(new ObjectName[tmpList.size()]));
	}

	public ObjectName[] getSharedLibraryNames() {
		List tmpList = new ArrayList();
		for (SharedLibrary sl : this.sharedLibraries.values()) {
			tmpList.add(this.container.getManagementContext().createObjectName(
					sl));
		}
		return ((ObjectName[]) tmpList.toArray(new ObjectName[tmpList.size()]));
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(), "componentNames",
				"list of components");
		helper.addAttribute(getObjectToManage(), "serviceUnitNames",
				"list of service units");
		helper.addAttribute(getObjectToManage(), "serviceAssemblyNames",
				"list of service assemblies");
		helper.addAttribute(getObjectToManage(), "endpointNames",
				"list of endpoints");
		helper.addAttribute(getObjectToManage(), "sharedLibraryNames",
				"list of shared libraries");
		return AttributeInfoHelper.join(super.getAttributeInfos(),
				helper.getAttributeInfos());
	}

	public void cancelPendingExchanges() {
		for (ComponentMBeanImpl mbean : this.componentRegistry.getComponents()) {
			DeliveryChannel channel = mbean.getDeliveryChannel();
			if (channel == null)
				;
		}
	}
}