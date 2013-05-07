package com.sanxing.sesame.mbean;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.container.ServiceAssemblyEnvironment;
import com.sanxing.sesame.deployment.Identification;
import com.sanxing.sesame.deployment.ServiceAssembly;
import com.sanxing.sesame.deployment.ServiceUnit;
import com.sanxing.sesame.deployment.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.management.JMException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceAssemblyRegistry {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceAssemblyRegistry.class);

	private Map<String, ServiceAssemblyLifeCycle> serviceAssemblies = new ConcurrentHashMap();
	private Registry registry;

	public ServiceAssemblyRegistry(Registry registry) {
		this.registry = registry;
	}

	public void start() {
	}

	public void stop() {
	}

	public void shutDown() {
	}

	public ServiceAssemblyLifeCycle register(ServiceAssembly sa,
			String[] suKeys, ServiceAssemblyEnvironment env)
			throws DeploymentException {
		String saName = sa.getIdentification().getName();
		if (!(this.serviceAssemblies.containsKey(saName))) {
			ServiceAssemblyLifeCycle salc = new ServiceAssemblyLifeCycle(sa,
					env, this.registry);
			List sus = new ArrayList();
			for (int i = 0; i < suKeys.length; ++i) {
				sus.add(this.registry.getServiceUnit(suKeys[i]));
			}
			salc.setServiceUnits((ServiceUnitLifeCycle[]) (ServiceUnitLifeCycle[]) sus
					.toArray(new ServiceUnitLifeCycle[sus.size()]));
			this.serviceAssemblies.put(saName, salc);
			try {
				ObjectName objectName = this.registry.getContainer()
						.getManagementContext().createObjectName(salc);
				this.registry
						.getContainer()
						.getManagementContext()
						.registerMBean(objectName, salc,
								ServiceAssemblyMBean.class);
			} catch (JMException e) {
				LOG.error("Could not register MBean for service assembly", e);
			}
			return salc;
		}
		return null;
	}

	public ServiceAssemblyLifeCycle register(ServiceAssembly sa,
			ServiceAssemblyEnvironment env) throws DeploymentException {
		List sus = new ArrayList();
		if (sa.getServiceUnits() != null) {
			for (int i = 0; i < sa.getServiceUnits().length; ++i) {
				String suKey = this.registry
						.registerServiceUnit(sa.getServiceUnits()[i], sa
								.getIdentification().getName(), env
								.getServiceUnitDirectory(
										sa.getServiceUnits()[i].getTarget()
												.getComponentName(), sa
												.getServiceUnits()[i]
												.getIdentification().getName()));

				sus.add(suKey);
			}
		}
		return register(sa, (String[]) sus.toArray(new String[sus.size()]), env);
	}

	public boolean unregister(String name) {
		ServiceAssemblyLifeCycle salc = (ServiceAssemblyLifeCycle) this.serviceAssemblies
				.remove(name);
		if (salc != null) {
			try {
				ServiceUnitLifeCycle[] sus = salc.getDeployedSUs();
				if (sus != null) {
					for (int i = 0; i < sus.length; ++i) {
						this.registry.unregisterServiceUnit(sus[i].getKey());
					}
				}
				this.registry.getContainer().getManagementContext()
						.unregisterMBean(salc);
			} catch (JBIException e) {
				LOG.error("Unable to unregister MBean for service assembly", e);
			}
			return true;
		}
		return false;
	}

	public ServiceAssemblyLifeCycle getServiceAssembly(String saName) {
		return ((ServiceAssemblyLifeCycle) this.serviceAssemblies.get(saName));
	}

	public String[] getDeployedServiceAssemblies() {
		String[] result = null;
		Set keys = this.serviceAssemblies.keySet();
		result = new String[keys.size()];
		keys.toArray(result);
		return result;
	}

	public String[] getDeployedServiceAssembliesForComponent(
			String componentName) {
		String[] result = null;

		Set tmpList = new HashSet();
		for (ServiceAssemblyLifeCycle salc : this.serviceAssemblies.values()) {
			ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
			if (sus != null) {
				for (int i = 0; i < sus.length; ++i) {
					if (sus[i].getTarget().getComponentName()
							.equals(componentName)) {
						tmpList.add(salc.getServiceAssembly()
								.getIdentification().getName());
					}
				}
			}
		}
		result = new String[tmpList.size()];
		tmpList.toArray(result);
		return result;
	}

	public String[] getComponentsForDeployedServiceAssembly(String saName) {
		String[] result = null;
		Set tmpList = new HashSet();
		ServiceAssemblyLifeCycle sa = getServiceAssembly(saName);
		if (sa != null) {
			ServiceUnit[] sus = sa.getServiceAssembly().getServiceUnits();
			if (sus != null) {
				for (int i = 0; i < sus.length; ++i) {
					tmpList.add(sus[i].getTarget().getComponentName());
				}
			}
		}
		result = new String[tmpList.size()];
		tmpList.toArray(result);
		return result;
	}

	public boolean isDeployedServiceUnit(String componentName, String suName) {
		boolean result = false;
		for (ServiceAssemblyLifeCycle salc : this.serviceAssemblies.values()) {
			ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
			if (sus != null) {
				for (int i = 0; i < sus.length; ++i) {
					if ((!(sus[i].getTarget().getComponentName()
							.equals(componentName)))
							|| (!(sus[i].getIdentification().getName()
									.equals(suName))))
						continue;
					result = true;
					break;
				}
			}
		}

		return result;
	}

	public Collection<ServiceAssemblyLifeCycle> getServiceAssemblies() {
		return this.serviceAssemblies.values();
	}
}