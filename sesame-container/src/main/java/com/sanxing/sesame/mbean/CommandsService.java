package com.sanxing.sesame.mbean;

import com.sanxing.sesame.container.ActivationSpec;
import com.sanxing.sesame.container.ComponentEnvironment;
import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.management.ParameterHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.management.DeploymentException;
import javax.management.JMException;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommandsService extends BaseSystemService implements
		CommandsServiceMBean {
	static Log LOG = LogFactory.getLog(CommandsService.class);

	public String getDescription() {
		return "Administrator Commands Service";
	}

	protected Class<?> getServiceMBean() {
		return CommandsServiceMBean.class;
	}

	public String installComponent(String file, Properties props,
			boolean deferException) throws Exception {
		if (deferException)
			updateExternalArchive(file, true);
		else {
			this.container.getInstallationService().install(file, props, false);
		}
		return ManagementSupport.createSuccessMessage("installComponent", file);
	}

	public String uninstallComponent(String name) throws Exception {
		ComponentMBeanImpl comp = this.container.getRegistry().getComponent(
				name);
		if (comp == null) {
			throw ManagementSupport.failure("uninstallComponent", "Component '"
					+ name + "' is not installed.");
		}
		/*if (!(comp.isShutDown())) {
			throw ManagementSupport.failure("uninstallComponent", "Component '"
					+ name + "' is not shut down.");
		}*/
		this.container.getInstallationService().loadInstaller(name);
		boolean success = this.container.getInstallationService().unloadInstaller(name, true);
		if (success) {
			return ManagementSupport.createSuccessMessage("uninstallComponent",
					name);
		}
		throw ManagementSupport.failure("uninstallComponent", name);
	}

	public String installSharedLibrary(String file, boolean deferException)
			throws Exception {
		if (deferException) {
			updateExternalArchive(file, true);
			return ManagementSupport.createSuccessMessage(
					"installSharedLibrary", file);
		}

		return this.container.getInstallationService().installSharedLibrary(
				file);
	}

	public String uninstallSharedLibrary(String name) throws Exception {
		SharedLibrary sl = this.container.getRegistry().getSharedLibrary(name);
		if (sl == null) {
			throw ManagementSupport.failure("uninstallSharedLibrary",
					"Shared library '" + name + "' is not installed.");
		}

		Collection components = this.container.getRegistry().getComponents();
		for (Iterator iter = components.iterator(); iter.hasNext();) {
			ComponentMBeanImpl comp = (ComponentMBeanImpl) iter.next();
			if (!(comp.isShutDown())) {
				String[] sls = comp.getSharedLibraries();
				if (sls != null) {
					for (int i = 0; i < sls.length; ++i) {
						if (name.equals(sls[i])) {
							throw ManagementSupport.failure(
									"uninstallSharedLibrary",
									"Shared library '" + name
											+ "' is used by component '"
											+ comp.getName() + "'.");
						}
					}
				}
			}
		}

		boolean success = this.container.getInstallationService()
				.uninstallSharedLibrary(name);
		if (success) {
			return ManagementSupport.createSuccessMessage(
					"uninstallSharedLibrary", name);
		}
		throw ManagementSupport.failure("uninstallSharedLibrary", name);
	}

	public String startComponent(String name) throws Exception {
		try {
			ComponentMBeanImpl lcc = this.container.getRegistry().getComponent(
					name);
			if (lcc == null) {
				throw new JBIException("Component " + name + " not found");
			}
			lcc.start();
			return ManagementSupport.createSuccessMessage("startComponent",
					name);
		} catch (JBIException e) {
			throw ManagementSupport.failure("startComponent", name, e);
		}
	}

	public String stopComponent(String name) throws Exception {
		try {
			ComponentMBeanImpl lcc = this.container.getRegistry().getComponent(
					name);
			if (lcc == null) {
				throw new JBIException("Component " + name + " not found");
			}
			lcc.stop();
			return ManagementSupport
					.createSuccessMessage("stopComponent", name);
		} catch (JBIException e) {
			throw ManagementSupport.failure("stopComponent", name, e);
		}
	}

	public String shutdownComponent(String name) throws Exception {
		try {
			ComponentMBeanImpl lcc = this.container.getRegistry().getComponent(
					name);
			if (lcc == null) {
				throw new JBIException("Component " + name + " not found");
			}
			lcc.shutDown();
			return ManagementSupport.createSuccessMessage("shutdownComponent",
					name);
		} catch (JBIException e) {
			throw ManagementSupport.failure("shutdownComponent", name, e);
		}
	}

	public String deployServiceAssembly(String file, boolean deferException)
			throws Exception {
		if (deferException) {
			updateExternalArchive(file, true);
			return ManagementSupport.createSuccessMessage(
					"deployServiceAssembly", file);
		}
		return this.container.getDeploymentService().deploy(file);
	}

	public String undeployServiceAssembly(String name) throws Exception {
		return this.container.getDeploymentService().undeploy(name);
	}

	public String startServiceAssembly(String name) throws Exception {
		return this.container.getDeploymentService().start(name);
	}

	public String stopServiceAssembly(String name) throws Exception {
		return this.container.getDeploymentService().stop(name);
	}

	public String shutdownServiceAssembly(String name) throws Exception {
		return this.container.getDeploymentService().shutDown(name);
	}

	public String installArchive(String location) throws Exception {
		updateExternalArchive(location, true);
		return ManagementSupport.createSuccessMessage("installArchive",
				location);
	}

	public String listComponents(boolean excludeSEs, boolean excludeBCs,
			boolean excludePojos, String requiredState,
			String sharedLibraryName, String serviceAssemblyName)
			throws Exception {
		if ((requiredState != null) && (requiredState.length() > 0)
				&& (!("Unknown".equalsIgnoreCase(requiredState)))
				&& (!("Shutdown".equalsIgnoreCase(requiredState)))
				&& (!("Stopped".equalsIgnoreCase(requiredState)))
				&& (!("Started".equalsIgnoreCase(requiredState)))) {
			throw ManagementSupport.failure("listComponents",
					"Required state '" + requiredState
							+ "' is not a valid state.");
		}

		Collection connectors = this.container.getRegistry().getComponents();
		List components = new ArrayList();
		for (Iterator iter = connectors.iterator(); iter.hasNext();) {
			ComponentMBeanImpl component = (ComponentMBeanImpl) iter.next();

			if ((excludeSEs) && (component.isService())) {
				continue;
			}

			if ((excludeBCs) && (component.isBinding())) {
				continue;
			}

			if ((excludePojos) && (component.isPojo())) {
				continue;
			}

			if ((requiredState != null)
					&& (requiredState.length() > 0)
					&& (!(requiredState.equalsIgnoreCase(component
							.getCurrentState())))) {
				continue;
			}

			if ((sharedLibraryName != null)
					&& (sharedLibraryName.length() > 0)
					&& (!(this.container.getInstallationService()
							.containsSharedLibrary(sharedLibraryName)))) {
				continue;
			}

			if ((serviceAssemblyName != null)
					&& (serviceAssemblyName.length() > 0)) {
				String[] saNames = this.container.getRegistry()
						.getDeployedServiceAssembliesForComponent(
								component.getName());
				boolean found = false;
				for (int i = 0; i < saNames.length; ++i) {
					if (serviceAssemblyName.equals(saNames[i])) {
						found = true;
						break;
					}
				}
				if (!(found)) {
					continue;
				}
			}
			components.add(component);
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version='1.0'?>\n");
		buffer.append("<component-info-list xmlns='http://java.sun.com/xml/ns/jbi/component-info-list' version='1.0'>\n");
		for (Iterator iter = components.iterator(); iter.hasNext();) {
			ComponentMBeanImpl component = (ComponentMBeanImpl) iter.next();
			buffer.append("  <component-info");
			if ((!(component.isBinding())) && (component.isService()))
				buffer.append(" type='service-engine'");
			else if ((component.isBinding()) && (!(component.isService()))) {
				buffer.append(" type='binding-component'");
			}
			buffer.append(" name='" + component.getName() + "'");
			buffer.append(" state='" + component.getCurrentState() + "'>\n");
			if (component.getDescription() != null) {
				buffer.append("    <description>");
				buffer.append(component.getDescription());
				buffer.append("</description>\n");
			}
			buffer.append("  </component-info>\n");
		}
		buffer.append("</component-info-list>");
		return buffer.toString();
	}

	public String listSharedLibraries(String componentName,
			String sharedLibraryName) throws Exception {
		Collection libs;
		if (sharedLibraryName != null) {
			SharedLibrary sl = this.container.getRegistry().getSharedLibrary(
					sharedLibraryName);
			if (sl == null)
				libs = Collections.EMPTY_LIST;
			else
				libs = Collections.singletonList(sl);
		} else {
			if (componentName != null) {
				libs = this.container.getRegistry().getSharedLibraries();
			} else
				libs = this.container.getRegistry().getSharedLibraries();
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version='1.0'?>\n");
		buffer.append("<component-info-list xmlns='http://java.sun.com/xml/ns/jbi/component-info-list' version='1.0'>\n");
		for (Iterator iter = libs.iterator(); iter.hasNext();) {
			SharedLibrary sl = (SharedLibrary) iter.next();
			buffer.append("  <component-info type='shared-library' name='")
					.append(sl.getName()).append("' state='Started'>");
			if (sl.getDescription() != null) {
				buffer.append("    <description>");
				buffer.append(sl.getDescription());
				buffer.append("</description>\n");
			}
			buffer.append("  </component-info>\n");
		}
		buffer.append("</component-info-list>");
		return buffer.toString();
	}

	public String listServiceAssemblies(String state, String componentName,
			String serviceAssemblyName) throws Exception {
		String[] result = null;
		if ((null != serviceAssemblyName) && (serviceAssemblyName.length() > 0))
			result = new String[] { serviceAssemblyName };
		else if ((null != componentName) && (componentName.length() > 0))
			result = this.container.getRegistry()
					.getDeployedServiceAssembliesForComponent(componentName);
		else {
			result = this.container.getRegistry()
					.getDeployedServiceAssemblies();
		}

		List assemblies = new ArrayList();
		for (int i = 0; i < result.length; ++i) {
			ServiceAssemblyLifeCycle sa = this.container.getRegistry()
					.getServiceAssembly(result[i]);
			if (sa == null)
				continue;
			if ((state != null) && (state.length() > 0)
					&& (!(state.equals(sa.getCurrentState())))) {
				continue;
			}
			assemblies.add(sa);
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version='1.0'?>\n");
		buffer.append("<service-assembly-info-list xmlns='http://java.sun.com/xml/ns/jbi/service-assembly-info-list' version='1.0'>\n");
		for (Iterator iter = assemblies.iterator(); iter.hasNext();) {
			ServiceAssemblyLifeCycle sa = (ServiceAssemblyLifeCycle) iter
					.next();
			buffer.append("  <service-assembly-info");
			buffer.append(" name='" + sa.getName() + "'");
			buffer.append(" state='" + sa.getCurrentState() + "'>\n");
			buffer.append("    <description>" + sa.getDescription()
					+ "</description>\n");

			ServiceUnitLifeCycle[] serviceUnitList = sa.getDeployedSUs();
			for (int i = 0; i < serviceUnitList.length; ++i) {
				buffer.append("    <service-unit-info");
				buffer.append(" name='" + serviceUnitList[i].getName() + "'");
				buffer.append(" state='" + serviceUnitList[i].getCurrentState()
						+ "'");
				buffer.append(" deployed-on='"
						+ serviceUnitList[i].getComponentName() + "'>\n");
				buffer.append("      <description>"
						+ serviceUnitList[i].getDescription()
						+ "</description>\n");
				buffer.append("    </service-unit-info>\n");
			}

			buffer.append("  </service-assembly-info>\n");
		}
		buffer.append("</service-assembly-info-list>");

		return buffer.toString();
	}

	public MBeanOperationInfo[] getOperationInfos() throws JMException {
		OperationInfoHelper helper = new OperationInfoHelper();
		ParameterHelper ph = helper.addOperation(getObjectToManage(),
				"installComponent", 3, "install a component");
		ph.setDescription(0, "file", "location of JBI Component to install");
		ph.setDescription(1, "properties", "component installation properties");
		ph.setDescription(1, "deferExceptions",
				"true if exceptions due to missing dependencies should be differed");

		ph = helper.addOperation(getObjectToManage(), "uninstallComponent", 1,
				"uninstall a component");
		ph.setDescription(0, "name", "component name to uninstall");

		ph = helper.addOperation(getObjectToManage(), "installSharedLibrary",
				1, "install a shared library");
		ph.setDescription(0, "file", "location of shared library to install");

		ph = helper.addOperation(getObjectToManage(), "uninstallSharedLibrary",
				1, "uninstall a shared library");
		ph.setDescription(0, "name", "name of shared library to uninstall");

		ph = helper.addOperation(getObjectToManage(), "installArchive", 1,
				"install an archive (component/SA etc)");
		ph.setDescription(0, "location", "file name or url to the location");

		ph = helper.addOperation(getObjectToManage(), "startComponent", 1,
				"start a component");
		ph.setDescription(0, "name", "name of component to start");

		ph = helper.addOperation(getObjectToManage(), "stopComponent", 1,
				"stop a component");
		ph.setDescription(0, "name", "name of component to stop");

		ph = helper.addOperation(getObjectToManage(), "shutdownComponent", 1,
				"shutdown a component");
		ph.setDescription(0, "name", "name of component to shutdown");

		ph = helper.addOperation(getObjectToManage(), "deployServiceAssembly",
				1, "deploy a service assembly");
		ph.setDescription(0, "file", "location of service assembly to deploy");

		ph = helper.addOperation(getObjectToManage(),
				"undeployServiceAssembly", 1, "undeploy a service assembly");
		ph.setDescription(0, "name", "name of service assembly to undeploy");

		ph = helper.addOperation(getObjectToManage(), "startServiceAssembly",
				1, "start a service assembly");
		ph.setDescription(0, "name", "name of service assembly to start");

		ph = helper.addOperation(getObjectToManage(), "stopServiceAssembly", 1,
				"stop a service assembly");
		ph.setDescription(0, "name", "name of service assembly to stop");

		ph = helper.addOperation(getObjectToManage(),
				"shutdownServiceAssembly", "shutdown a service assembly");
		ph.setDescription(0, "name", "name of service assembly to shutdown");

		ph = helper.addOperation(getObjectToManage(), "listComponents", 5,
				"list all components installed");
		ph.setDescription(0, "excludeSEs",
				"if true will exclude service engines");
		ph.setDescription(1, "excludeBCs",
				"if true will exclude binding components");
		ph.setDescription(1, "excludePojos",
				"if true will exclude pojos components");
		ph.setDescription(2, "requiredState",
				"component state to list, if null will list all");
		ph.setDescription(3, "sharedLibraryName", "shared library name to list");
		ph.setDescription(4, "serviceAssemblyName",
				"service assembly name to list");

		ph = helper.addOperation(getObjectToManage(), "listSharedLibraries", 2,
				"list shared library");
		ph.setDescription(0, "componentName", "component name");
		ph.setDescription(1, "sharedLibraryName", "shared library name");

		ph = helper.addOperation(getObjectToManage(), "listServiceAssemblies",
				3, "list service assemblies");
		ph.setDescription(0, "state", "service assembly state to list");
		ph.setDescription(1, "componentName", "component name");
		ph.setDescription(2, "serviceAssemblyName", "service assembly name");

		return OperationInfoHelper.join(super.getOperationInfos(),
				helper.getOperationInfos());
	}

	public void deactivateComponent(String componentName) throws JBIException {
		ComponentMBeanImpl component = this.container.getRegistry()
				.getComponent(componentName);
		if (component != null) {
			component.doShutDown();
			component.unregisterMbeans(this.container.getManagementContext());
			this.container.getRegistry().deregisterComponent(component);
			this.container.getEnvironmentContext().unreregister(component);
			component.dispose();
			LOG.info("Deactivating component " + componentName);
		} else {
			throw new JBIException("Could not find component " + componentName);
		}
	}

	public void deleteComponent(String id, JBIContainer jbiContainer)
			throws JBIException {
		deactivateComponent(id);
		this.container.getEnvironmentContext().removeComponentRootDirectory(id);
	}

	public ObjectName activateComponent(File installDir, Component component,
			String description, ComponentContextImpl context, boolean binding,
			boolean service, String[] sharedLibraries, JBIContainer jbiContainer)
			throws JBIException {
		ComponentNameSpace cns = context.getComponentNameSpace();
		ActivationSpec activationSpec = new ActivationSpec();
		activationSpec.setComponent(component);
		activationSpec.setComponentName(cns.getName());
		return activateComponent(installDir, component, description, context,
				activationSpec, false, binding, service, sharedLibraries);
	}

	private ObjectName activateComponent(File installationDir,
			Component component, String description,
			ComponentContextImpl context, ActivationSpec activationSpec,
			boolean pojo, boolean binding, boolean service,
			String[] sharedLibraries) throws JBIException {
		ObjectName result = null;

		ComponentNameSpace cns = new ComponentNameSpace(
				this.container.getName(), activationSpec.getComponentName());
		if (LOG.isDebugEnabled()) {
			LOG.info("Activating component for: " + cns + " with component: "
					+ component);
		}
		ComponentMBeanImpl componentManager = this.container.getRegistry()
				.registerComponent(cns, description, component, binding,
						service, sharedLibraries);
		if (componentManager != null) {
			componentManager.setPojo(pojo);
			ComponentEnvironment env = this.container.getEnvironmentContext()
					.registerComponent(context.getEnvironment(),
							componentManager);
			if (env.getInstallRoot() == null) {
				env.setInstallRoot(installationDir);
			}
			context.activate(component, env, activationSpec);
			componentManager.setContext(context);
			componentManager.setActivationSpec(activationSpec);

			if (componentManager.isPojo()) {
				componentManager.init();
			} else
				componentManager.doShutDown();

			result = componentManager.registerMBeans(this.container
					.getManagementContext());

			if ((componentManager.isPojo())
					&& (this.container.getStarted().get())) {
				componentManager.start();
			}
		}
		return result;
	}

	private ArchiveEntry updateExternalArchive(String location,
			boolean autoStart) throws DeploymentException {
		ArchiveEntry entry = new ArchiveEntry();
		entry.location = location;
		entry.lastModified = new Date();
		this.container.getArchiveManager().updateArchive(location, entry,
				autoStart);
		return entry;
	}
}