package com.sanxing.sesame.mbean;

import com.sanxing.sesame.container.ComponentEnvironment;
import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.deployment.Component;
import com.sanxing.sesame.deployment.Descriptor;
import com.sanxing.sesame.deployment.DescriptorFactory;
import com.sanxing.sesame.deployment.Identification;
import com.sanxing.sesame.deployment.SharedLibrary;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.management.ParameterHelper;
import com.sanxing.sesame.router.Router;
import com.sanxing.sesame.util.FileUtil;
import com.sanxing.sesame.util.FileVersionUtil;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.InstallationServiceMBean;
import javax.jbi.management.InstallerMBean;
import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallationService extends BaseSystemService implements
		InstallationServiceMBean {
	private static final Logger LOG = LoggerFactory.getLogger(InstallationService.class);
	private EnvironmentContext environmentContext;
	private ManagementContext managementContext;
	private Map<String, InstallerMBeanImpl> installers;
	private Map<String, InstallerMBeanImpl> nonLoadedInstallers;

	public InstallationService() {
		this.installers = new ConcurrentHashMap();

		this.nonLoadedInstallers = new ConcurrentHashMap();
	}

	public String getDescription() {
		return "installs/uninstalls Components";
	}

	public synchronized ObjectName loadNewInstaller(String installJarURL) {
		try {
			ObjectName result = null;
			if (LOG.isDebugEnabled()) {
				LOG.debug("Loading new installer from " + installJarURL);
			}
			File tmpDir = ArchiveManager.unpackLocation(
					this.environmentContext.getTmpDir(), installJarURL);
			if (tmpDir != null) {
				Descriptor root = DescriptorFactory.buildDescriptor(tmpDir);
				if ((root != null) && (root.getComponent() != null)) {
					String componentName = root.getComponent()
							.getIdentification().getName();
					if (!(this.installers.containsKey(componentName))) {
						InstallerMBeanImpl installer = doInstallArchive(tmpDir,
								root);
						if (installer != null) {
							result = installer.getObjectName();
							this.installers.put(componentName, installer);
						}
					} else {
						throw new RuntimeException(
								"An installer already exists for "
										+ componentName);
					}
				} else {
					throw new RuntimeException(
							"Could not find Component from: " + installJarURL);
				}
			} else {
				throw new RuntimeException("location: " + installJarURL
						+ " isn't valid");
			}
			return result;
		} catch (Throwable t) {
			LOG.error("Deployment failed", t);
			if (t instanceof Error) {
				throw ((Error) t);
			}
			if (t instanceof RuntimeException) {
				throw ((RuntimeException) t);
			}

			throw new RuntimeException("Deployment failed: " + t.getMessage());
		}
	}

	public ObjectName loadInstaller(String aComponentName) {
		InstallerMBeanImpl installer = (InstallerMBeanImpl) this.installers
				.get(aComponentName);
		if (installer == null) {
			installer = (InstallerMBeanImpl) this.nonLoadedInstallers
					.get(aComponentName);
			if (installer != null) {
				this.installers.put(aComponentName, installer);
				this.nonLoadedInstallers.remove(aComponentName);
				try {
					ObjectName objectName = this.managementContext
							.createCustomComponentMBeanName("Installer",
									aComponentName);

					installer.setObjectName(objectName);
					this.managementContext.registerMBean(objectName, installer,
							InstallerMBean.class,
							"standard installation controls for a Component");
				} catch (Exception e) {
					throw new RuntimeException("Could not load installer", e);
				}
				return installer.getObjectName();
			}
		}
		return null;
	}

	private InstallerMBeanImpl createInstaller(String componentName)
			throws IOException, DeploymentException {
		File installationDir = this.environmentContext
				.getComponentInstallationDir(componentName);
		Descriptor root = DescriptorFactory.buildDescriptor(installationDir);
		Component descriptor = root.getComponent();

		InstallationContextImpl installationContext = new InstallationContextImpl(
				descriptor);
		installationContext.setInstall(false);
		installationContext.setInstallRoot(installationDir);

		File componentRoot = this.environmentContext
				.getComponentRootDir(componentName);
		ComponentContextImpl context = buildComponentContext(componentRoot,
				installationDir, componentName);
		installationContext.setContext(context);
		return new InstallerMBeanImpl(this.container, installationContext);
	}

	public boolean unloadInstaller(String componentName, boolean isToBeDeleted) {
		boolean result = false;
		try {
			this.container.getRouter().suspend();
			InstallerMBeanImpl installer = (InstallerMBeanImpl) this.installers
					.remove(componentName);
			result = installer != null;
			if (result) {
				this.container.getManagementContext()
						.unregisterMBean(installer);
				if (isToBeDeleted) {
					installer.uninstall();
				} else
					this.nonLoadedInstallers.put(componentName, installer);
			}
		} catch (JBIException e) {
			String errStr = "Problem shutting down Component: " + componentName;
			LOG.error(errStr, e);
		} finally {
			this.container.getRouter().resume();
		}
		return result;
	}

	public String installSharedLibrary(String aSharedLibURI) {
		String result = "";
		try {
			File tmpDir = ArchiveManager.unpackLocation(
					this.environmentContext.getTmpDir(), aSharedLibURI);
			if (tmpDir != null) {
				Descriptor root = DescriptorFactory.buildDescriptor(tmpDir);
				if (root == null) {
					throw new DeploymentException(
							"Could not find JBI descriptor");
				}
				SharedLibrary sl = root.getSharedLibrary();
				if (sl != null) {
					result = doInstallSharedLibrary(tmpDir, sl);
				} else
					throw new DeploymentException(
							"JBI descriptor is not a SharedLibrary descriptor");
			} else {
				throw new DeploymentException("Could not find JBI descriptor");
			}
		} catch (DeploymentException e) {
			LOG.error("Deployment failed", e);
		}
		return result;
	}

	public boolean uninstallSharedLibrary(String aSharedLibName) {
		this.container.getRegistry().unregisterSharedLibrary(aSharedLibName);
		this.environmentContext.removeSharedLibraryDirectory(aSharedLibName);
		return true;
	}

	public void init(JBIContainer container) throws JBIException {
		super.init(container);
		this.environmentContext = container.getEnvironmentContext();
		this.managementContext = container.getManagementContext();
		buildState();
	}

	protected Class getServiceMBean() {
		return InstallationServiceMBean.class;
	}

	public void install(String location, Properties props, boolean autoStart)
			throws DeploymentException {
		File tmpDir = ArchiveManager.unpackLocation(
				this.environmentContext.getTmpDir(), location);
		if (tmpDir != null) {
			Descriptor root = DescriptorFactory.buildDescriptor(tmpDir);
			if (root != null) {
				if (root.getComponent() == null) {
					throw new DeploymentException(
							"JBI descriptor is not a component descriptor");
				}
				install(tmpDir, props, root, autoStart);
			} else {
				throw new DeploymentException("Could not find JBI descriptor");
			}
		} else {
			throw new DeploymentException("Could not find JBI descriptor");
		}
	}

	protected void install(File tmpDir, Properties props, Descriptor root,
			boolean autoStart) throws DeploymentException {
		if (root.getComponent() != null) {
			String componentName = root.getComponent().getIdentification()
					.getName();
			if (this.installers.containsKey(componentName)) {
				throw new DeploymentException("Component " + componentName
						+ " is already installed");
			}
			InstallerMBeanImpl installer = doInstallArchive(tmpDir, root);
			if (installer == null)
				return;
			try {
				ObjectName on;
				MBeanServer mbs;
				Iterator it;
				if ((props != null) && (props.size() > 0)) {
					on = installer.getInstallerConfigurationMBean();
					if (on == null) {
						LOG.warn("Could not find installation configuration MBean. Installation properties will be ignored.");
					} else {
						mbs = this.managementContext.getMBeanServer();
						for (it = props.keySet().iterator(); it.hasNext();) {
							String key = (String) it.next();
							String val = props.getProperty(key);
							try {
								mbs.setAttribute(on, new Attribute(key, val));
							} catch (JMException e) {
								throw new DeploymentException(
										"Could not set installation property: ("
												+ key + " = " + val, e);
							}
						}
					}
				}

				installer.install();
			} catch (JBIException e) {
				throw new DeploymentException(e);
			}
			if (autoStart) {
				try {
					ComponentMBeanImpl lcc = this.container.getRegistry()
							.getComponent(componentName);
					if (lcc != null) {
						lcc.start();
					} else
						LOG.warn("No ComponentConnector found for Component "
								+ componentName);
				} catch (JBIException e) {
					String errStr = "Failed to start Component: "
							+ componentName;
					LOG.error(errStr, e);
					throw new DeploymentException(e);
				}
			}
			this.installers.put(componentName, installer);
		}
	}

	public MBeanOperationInfo[] getOperationInfos() throws JMException {
		OperationInfoHelper helper = new OperationInfoHelper();
		ParameterHelper ph = helper.addOperation(getObjectToManage(),
				"loadNewInstaller", 1, "load a new Installer ");
		ph.setDescription(0, "installJarURL", "URL locating the install Jar");
		ph = helper.addOperation(getObjectToManage(), "loadInstaller", 1,
				"load installer for a previously installed component");

		ph.setDescription(0, "componentName", "Name of the Component");
		ph = helper.addOperation(getObjectToManage(), "unloadInstaller", 2,
				"unload an installer");
		ph.setDescription(0, "componentName", "Name of the Component");
		ph.setDescription(1, "isToBeDeleted",
				"true if component is to be deleted");
		ph = helper.addOperation(getObjectToManage(), "installSharedLibrary",
				1, "Install a shared library jar");
		ph.setDescription(0, "sharedLibURI", "URI for the jar to be installed");
		ph = helper.addOperation(getObjectToManage(), "uninstallSharedLibrary",
				1, "Uninstall a shared library jar");
		ph.setDescription(0, "sharedLibName", "name of the shared library");
		ph = helper.addOperation(getObjectToManage(), "install", 1,
				"install and deplot an archive");
		ph.setDescription(0, "location", "location of archive");
		ph = helper.addOperation(getObjectToManage(), "install", 2,
				"install and deplot an archive");
		ph.setDescription(0, "location", "location of archive");
		ph.setDescription(1, "autostart", "automatically start the Component");
		return OperationInfoHelper.join(super.getOperationInfos(),
				helper.getOperationInfos());
	}

	protected InstallerMBeanImpl doInstallArchive(File tmpDirectory,
			Descriptor descriptor) throws DeploymentException {
		InstallerMBeanImpl installer = null;
		Component component = descriptor.getComponent();
		if (component != null) {
			installer = doInstallComponent(tmpDirectory, component);
		}
		return installer;
	}

	protected String doInstallSharedLibrary(File tmpDirectory,
			SharedLibrary descriptor) throws DeploymentException {
		String result = null;
		if (descriptor != null) {
			File installationDir = null;
			try {
				result = descriptor.getIdentification().getName();
				File rootDir = this.environmentContext
						.createSharedLibraryDirectory(result);
				installationDir = FileVersionUtil
						.getNewVersionDirectory(rootDir);
				if (!(tmpDirectory.renameTo(installationDir))) {
					throw new DeploymentException("Unable to rename "
							+ tmpDirectory + " to " + installationDir);
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("Moved " + tmpDirectory + " to "
							+ installationDir);
				}
				this.container.getRegistry().registerSharedLibrary(descriptor,
						installationDir);
			} catch (Exception e) {
				LOG.error("Deployment of Shared Library failed", e);

				throw new DeploymentException(e);
			} finally {
				FileUtil.deleteFile(tmpDirectory);
			}
		}
		return result;
	}

	protected InstallerMBeanImpl doInstallComponent(File tmpDirectory,
			Component descriptor) throws DeploymentException {
		InstallerMBeanImpl result = null;
		String name = descriptor.getIdentification().getName();
		try {
			File oldInstallationDir = this.environmentContext
					.getComponentInstallationDir(name);

			if (!(FileUtil.deleteFile(oldInstallationDir))) {
				LOG.warn("Failed to delete old installation directory: "
						+ oldInstallationDir.getPath());
			}
			File componentRoot = this.environmentContext
					.createComponentRootDir(name);

			File installationDir = this.environmentContext
					.getNewComponentInstallationDir(name);
			tmpDirectory.renameTo(installationDir);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Moved " + tmpDirectory + " to " + installationDir);
			}
			result = initializeInstaller(installationDir, componentRoot,
					descriptor);
			return result;
		} catch (IOException e) {
			throw new DeploymentException(e);
		}
	}

	private InstallerMBeanImpl initializeInstaller(File installationDir,
			File componentRoot, Component descriptor)
			throws DeploymentException {
		InstallerMBeanImpl result = null;
		try {
			String name = descriptor.getIdentification().getName();
			InstallationContextImpl installationContext = new InstallationContextImpl(
					descriptor);
			installationContext.setInstall(true);
			installationContext.setInstallRoot(installationDir);

			ComponentContextImpl context = buildComponentContext(componentRoot,
					installationDir, name);
			installationContext.setContext(context);
			result = new InstallerMBeanImpl(this.container, installationContext);

			ObjectName objectName = this.managementContext
					.createCustomComponentMBeanName("Installer", name);
			result.setObjectName(objectName);
			this.managementContext.registerMBean(objectName, result,
					InstallerMBean.class,
					"standard installation controls for a Component");
		} catch (Throwable e) {
			LOG.error("Deployment of Component failed", e);

			this.environmentContext.removeComponentRootDirectory(descriptor
					.getIdentification().getName());
			throw new DeploymentException(e);
		}
		return result;
	}

	protected void buildState() {
		buildSharedLibs();
		buildComponents();
	}

	protected boolean containsSharedLibrary(String name) {
		return (this.container.getRegistry().getSharedLibrary(name) != null);
	}

	protected void buildSharedLibs() {
		File top = this.environmentContext.getSharedLibDir();
		if ((top == null) || (!(top.exists())) || (!(top.isDirectory()))) {
			return;
		}
		File[] files = top.listFiles();
		if (files != null)
			for (int i = 0; i < files.length; ++i) {
				if (!(files[i].isDirectory())) {
					continue;
				}
				File dir = FileVersionUtil.getLatestVersionDirectory(files[i]);
				if (dir == null) {
					continue;
				}
				Descriptor root = DescriptorFactory.buildDescriptor(dir);
				if (root == null) {
					continue;
				}
				SharedLibrary sl = root.getSharedLibrary();
				if (sl == null)
					continue;
				try {
					this.container.getRegistry().registerSharedLibrary(sl, dir);
				} catch (Exception e) {
					LOG.error("Failed to initialize sharted library", e);
				}
			}
	}

	protected void buildComponents() {
		File top = this.environmentContext.getComponentsDir();
		if ((top == null) || (!(top.exists())) || (!(top.isDirectory()))) {
			return;
		}
		File[] files = top.listFiles();
		if (files != null)
			for (int i = 0; i < files.length; ++i) {
				if (!(files[i].isDirectory())) {
					continue;
				}
				File directory = files[i];
				try {
					buildComponent(directory);
				} catch (DeploymentException e) {
					LOG.error(
							"Could not build Component: " + directory.getName(),
							e);
					LOG.warn("Deleting Component directory: " + directory);
					FileUtil.deleteFile(directory);
				}
			}
	}

	protected void buildComponent(File componentDirectory)
			throws DeploymentException {
		try {
			String componentName = componentDirectory.getName();
			ComponentEnvironment env = this.container.getEnvironmentContext()
					.getComponentEnvironment(componentName);
			if (!(env.getStateFile().exists())) {
				FileUtil.deleteFile(componentDirectory);
			} else {
				InstallerMBeanImpl installer = createInstaller(componentName);
				installer.activateComponent();
				this.nonLoadedInstallers.put(componentName, installer);
			}
		} catch (Throwable e) {
			LOG.error(
					"Failed to deploy component: "
							+ componentDirectory.getName(), e);
			throw new DeploymentException(e);
		}
	}

	protected ComponentContextImpl buildComponentContext(File componentRoot,
			File installRoot, String name) throws IOException {
		ComponentNameSpace cns = new ComponentNameSpace(
				this.container.getName(), name);
		ComponentContextImpl context = new ComponentContextImpl(this.container,
				cns);
		ComponentEnvironment env = new ComponentEnvironment();
		FileUtil.buildDirectory(componentRoot);
		File privateWorkspace = this.environmentContext
				.createWorkspaceDirectory(name);
		env.setWorkspaceRoot(privateWorkspace);
		env.setComponentRoot(componentRoot);
		env.setInstallRoot(installRoot);
		context.setEnvironment(env);
		return context;
	}
}