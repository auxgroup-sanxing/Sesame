package com.sanxing.sesame.mbean;

import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.MBeanBuilder;
import com.sanxing.sesame.management.MBeanInfoProvider;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.management.ParameterHelper;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.jbi.JBIException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementContext extends BaseSystemService implements
		ManagementContextMBean {
	private static final Logger LOG = LoggerFactory.getLogger(ManagementContext.class);

	protected Map<String, ObjectName> systemServices = new ConcurrentHashMap();

	private Map<ObjectName, Object> beanMap = new ConcurrentHashMap();
	private MBeanServer server;
	private JBIContainer container;
	private ExecutorService executors;

	public String getDescription() {
		return "JMX Management";
	}

	public void init(JBIContainer _container) throws JBIException {
		this.container = _container;
		this.server = this.container.getMBeanServer();
		this.executors = Executors.newCachedThreadPool();
		super.init(this.container);
	}

	public MBeanServer getMBeanServer() {
		return this.server;
	}

	protected Class<ManagementContextMBean> getServiceMBean() {
		return ManagementContextMBean.class;
	}

	public void start() throws JBIException {
		super.start();
	}

	public void stop() throws JBIException {
		super.stop();
	}

	public void shutDown() throws JBIException {
		super.shutDown();

		ObjectName[] beans = (ObjectName[]) this.beanMap.keySet().toArray(
				new ObjectName[this.beanMap.size()]);
		for (int i = 0; i < beans.length; ++i) {
			try {
				unregisterMBean(beans[i]);
			} catch (Exception e) {
				LOG.debug("Could not unregister mbean", e);
			}
		}

		this.executors.shutdown();
	}

	public ObjectName[] getBindingComponents() {
		return this.container.getRegistry().getBindingComponents();
	}

	public ObjectName getComponentByName(String componentName) {
		ComponentMBeanImpl component = this.container.getRegistry()
				.getComponent(componentName);
		return ((component != null) ? component.getMBeanName() : null);
	}

	public ObjectName[] getEngineComponents() {
		return this.container.getRegistry().getEngineComponents();
	}

	public String getSystemInfo() {
		return "Sesame JBI Container: version: "
				+ EnvironmentContext.getVersion();
	}

	public ObjectName getSystemService(String serviceName) {
		return ((ObjectName) this.systemServices.get(serviceName));
	}

	public ObjectName[] getSystemServices() {
		ObjectName[] result = null;
		Collection col = this.systemServices.values();
		result = new ObjectName[col.size()];
		col.toArray(result);
		return result;
	}

	public boolean isBinding(String componentName) {
		ComponentMBeanImpl component = this.container.getRegistry()
				.getComponent(componentName);
		return ((component != null) ? component.isBinding() : false);
	}

	public boolean isEngine(String componentName) {
		ComponentMBeanImpl component = this.container.getRegistry()
				.getComponent(componentName);
		return ((component != null) ? component.isEngine() : false);
	}

	public String startComponent(String componentName) throws JBIException {
		String result = "NOT FOUND: " + componentName;
		ObjectName objName = getComponentByName(componentName);
		if (objName != null) {
			ComponentMBeanImpl mbean = (ComponentMBeanImpl) this.beanMap
					.get(objName);
			if (mbean != null) {
				mbean.start();
				result = mbean.getCurrentState();
			}
		}
		return result;
	}

	public String stopComponent(String componentName) throws JBIException {
		String result = "NOT FOUND: " + componentName;
		ObjectName objName = getComponentByName(componentName);
		if (objName != null) {
			ComponentMBeanImpl mbean = (ComponentMBeanImpl) this.beanMap
					.get(objName);
			if (mbean != null) {
				mbean.stop();
				result = mbean.getCurrentState();
			}
		}
		return result;
	}

	public String shutDownComponent(String componentName) throws JBIException {
		String result = "NOT FOUND: " + componentName;
		ObjectName objName = getComponentByName(componentName);
		if (objName != null) {
			ComponentMBeanImpl mbean = (ComponentMBeanImpl) this.beanMap
					.get(objName);
			if (mbean != null) {
				mbean.shutDown();
				result = mbean.getCurrentState();
			}
		}
		return result;
	}

	public ObjectName createCustomComponentMBeanName(String type, String name) {
		Map result = new LinkedHashMap();
		result.put("ServerName", this.container.getServerName());
		result.put("Type", "Component");
		result.put("Name", sanitizeString(name));
		result.put("SubType", sanitizeString(type));
		return createObjectName(result);
	}

	public ObjectName createObjectName(MBeanInfoProvider provider) {
		Map props = createObjectNameProps(provider);
		return createObjectName(props);
	}

	public ObjectName createObjectName(String name) {
		ObjectName result = null;
		try {
			result = new ObjectName(name);
		} catch (MalformedObjectNameException e) {
			String error = "Could not create ObjectName for " + name;
			LOG.error(error, e);
			throw new RuntimeException(error);
		}
		return result;
	}

	public ObjectName createObjectName(String domain, Map<String, String> props) {
		StringBuffer sb = new StringBuffer();
		sb.append(domain).append(':');
		int i = 0;
		for (Iterator it = props.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			if (i++ > 0) {
				sb.append(",");
			}
			sb.append(entry.getKey()).append("=").append(entry.getValue());
		}
		ObjectName result = null;
		try {
			result = new ObjectName(sb.toString());
		} catch (MalformedObjectNameException e) {
			String error = "Could not create ObjectName for " + props;
			LOG.error(error, e);
			throw new RuntimeException(error);
		}
		return result;
	}

	public ObjectName createObjectName(Map<String, String> props) {
		return createObjectName(this.container.getJmxDomain(), props);
	}

	public Map<String, String> createObjectNameProps(MBeanInfoProvider provider) {
		return createObjectNameProps(provider, false);
	}

	public Map<String, String> createObjectNameProps(
			MBeanInfoProvider provider, boolean subTypeBeforeName) {
		Map result = new LinkedHashMap();
		result.put("ServerName", this.container.getServerName());
		result.put("Type", sanitizeString(provider.getType()));
		if ((subTypeBeforeName) && (provider.getSubType() != null)) {
			result.put("SubType", sanitizeString(provider.getSubType()));
		}
		result.put("Name", sanitizeString(provider.getName()));
		if ((!(subTypeBeforeName)) && (provider.getSubType() != null)) {
			result.put("SubType", sanitizeString(provider.getSubType()));
		}
		return result;
	}

	private static String sanitizeString(String in) {
		String result = null;
		if (in != null) {
			result = in.replace(':', '_');
			result = result.replace('/', '_');
			result = result.replace('\\', '_');
			result = result.replace('?', '_');
			result = result.replace('=', '_');
			result = result.replace(',', '_');
		}
		return result;
	}

	public void registerMBean(ObjectName name, MBeanInfoProvider resource,
			Class interfaceMBean) throws JMException {
		registerMBean(name, resource, interfaceMBean, resource.getDescription());
	}

	public void registerMBean(ObjectName name, Object resource,
			Class interfaceMBean, String description) throws JMException {
		Object mbean = MBeanBuilder.buildStandardMBean(resource,
				interfaceMBean, description, this.executors);
		if (this.server.isRegistered(name)) {
			this.server.unregisterMBean(name);
		}
		this.server.registerMBean(mbean, name);
		this.beanMap.put(name, resource);
	}

	public static ObjectName getSystemObjectName(String domainName,
			String serverName, Class interfaceType) {
		String tmp = domainName + ":ServerName=" + serverName
				+ ",Type=SystemService,Name="
				+ getSystemServiceName(interfaceType);
		ObjectName result = null;
		try {
			result = new ObjectName(tmp);
		} catch (MalformedObjectNameException e) {
			LOG.error("Failed to build ObjectName:", e);
		} catch (NullPointerException e) {
			LOG.error("Failed to build ObjectName:", e);
		}
		return result;
	}

	public static String getSystemServiceName(Class interfaceType) {
		String name = interfaceType.getName();
		name = name.substring(name.lastIndexOf(46) + 1);
		if (name.endsWith("MBean")) {
			name = name.substring(0, name.length() - 5);
		}
		return name;
	}

	public static ObjectName getContainerObjectName(String domainName,
			String serverName) {
		String tmp = domainName + ":ServerName=" + serverName
				+ ",Type=JBIContainer";
		ObjectName result = null;
		try {
			result = new ObjectName(tmp);
		} catch (MalformedObjectNameException e) {
			LOG.debug("Unable to build ObjectName", e);
		} catch (NullPointerException e) {
			LOG.debug("Unable to build ObjectName", e);
		}
		return result;
	}

	public void registerSystemService(BaseSystemService service,
			Class interfaceType) throws JBIException {
		try {
			String name = service.getName();
			if (this.systemServices.containsKey(name)) {
				throw new JBIException("A system service for the name " + name
						+ " is already registered");
			}
			ObjectName objName = createObjectName(service);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Registering system service: " + objName);
			}
			registerMBean(objName, service, interfaceType,
					service.getDescription());
			this.systemServices.put(name, objName);
		} catch (MalformedObjectNameException e) {
			throw new JBIException(e);
		} catch (JMException e) {
			throw new JBIException(e);
		}
	}

	public void unregisterSystemService(BaseSystemService service)
			throws JBIException {
		String name = service.getName();
		if (!(this.systemServices.containsKey(name))) {
			throw new JBIException("A system service for the name " + name
					+ " is not registered");
		}
		ObjectName objName = (ObjectName) this.systemServices.remove(name);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Unregistering system service: " + objName);
		}
		unregisterMBean(objName);
	}

	public void unregisterMBean(ObjectName name) throws JBIException {
		try {
			this.server.unregisterMBean(name);
			this.beanMap.remove(name);
		} catch (JMException e) {
			LOG.error("Failed to unregister mbean: " + name, e);
			throw new JBIException(e);
		}
	}

	public void unregisterMBean(Object bean) throws JBIException {
		for (Iterator i = this.beanMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			if (entry.getValue() == bean) {
				ObjectName name = (ObjectName) entry.getKey();
				unregisterMBean(name);
				return;
			}
		}
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(), "bindingComponents",
				"Get list of all binding components");
		helper.addAttribute(getObjectToManage(), "engineComponents",
				"Get list of all engine components");

		helper.addAttribute(getObjectToManage(), "systemInfo",
				"Return current version");
		helper.addAttribute(getObjectToManage(), "systemServices",
				"Get list of system services");
		return AttributeInfoHelper.join(super.getAttributeInfos(),
				helper.getAttributeInfos());
	}

	public MBeanOperationInfo[] getOperationInfos() throws JMException {
		OperationInfoHelper helper = new OperationInfoHelper();
		ParameterHelper ph = helper.addOperation(getObjectToManage(),
				"getComponentByName", 1, "look up Component by name");
		ph.setDescription(0, "name", "Component name");
		ph = helper.addOperation(getObjectToManage(), "getSystemService", 1,
				"look up System service by name");
		ph.setDescription(0, "name", "System name");
		ph = helper.addOperation(getObjectToManage(), "isBinding", 1,
				"Is Component a binding Component?");
		ph.setDescription(0, "name", "Component name");
		ph = helper.addOperation(getObjectToManage(), "isEngine", 1,
				"Is Component a service engine?");
		ph.setDescription(0, "name", "Component name");
		return OperationInfoHelper.join(super.getOperationInfos(),
				helper.getOperationInfos());
	}
}