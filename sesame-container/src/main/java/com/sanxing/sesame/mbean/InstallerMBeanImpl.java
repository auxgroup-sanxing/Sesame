package com.sanxing.sesame.mbean;

import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.deployment.ClassPath;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.InstallerMBean;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InstallerMBeanImpl implements InstallerMBean {
	private static final Log LOG = LogFactory.getLog(InstallerMBeanImpl.class);
	private InstallationContextImpl context;
	private JBIContainer container;
	private ObjectName objectName;
	private ObjectName extensionMBeanName;
	private Bootstrap bootstrap;
	private boolean initialized;
	private JarFileClassLoader bootstrapLoader;
	private JarFileClassLoader componentLoader;

	public InstallerMBeanImpl(JBIContainer container, InstallationContextImpl ic)
			throws DeploymentException {
		this.container = container;
		this.context = ic;
		this.bootstrap = createBootstrap();
		initBootstrap();
	}

	private void initBootstrap() throws DeploymentException {
		try {
			if (!(this.initialized)) {
				try {
					if ((this.extensionMBeanName != null)
							&& (this.container.getMBeanServer() != null)
							&& (this.container.getMBeanServer()
									.isRegistered(this.extensionMBeanName))) {
						this.container.getMBeanServer().unregisterMBean(
								this.extensionMBeanName);
					}
				} catch (InstanceNotFoundException e) {
				} catch (MBeanRegistrationException e) {
				}
				this.bootstrap.init(this.context);
				this.extensionMBeanName = this.bootstrap
						.getExtensionMBeanName();
				this.initialized = true;
			}
		} catch (JBIException e) {
			LOG.error("Could not initialize bootstrap", e);
			throw new DeploymentException(e);
		}
	}

	protected void cleanUpBootstrap() throws DeploymentException {
		try {
			this.bootstrap.cleanUp();
		} catch (JBIException e) {
			throw new DeploymentException(e);
		} finally {
			this.initialized = false;
		}
	}

	private Bootstrap createBootstrap() throws DeploymentException {
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		com.sanxing.sesame.deployment.Component descriptor = this.context
				.getDescriptor();
		try {
			this.bootstrapLoader = buildBootClassLoader(
					this.context.getInstallRootAsDir(), descriptor
							.getBootstrapClassPath().getPathElements(),
					descriptor.isBootstrapClassLoaderDelegationParentFirst(),
					null);

			Thread.currentThread().setContextClassLoader(this.bootstrapLoader);
			Class bootstrapClass = this.bootstrapLoader.loadClass(descriptor
					.getBootstrapClassName());
			Bootstrap localBootstrap = (Bootstrap) bootstrapClass.newInstance();

			return localBootstrap;
		} catch (MalformedURLException e) {
			throw new DeploymentException(e);
		} catch (ClassNotFoundException e) {
			throw new DeploymentException(e);
		} catch (InstantiationException e) {
			throw new DeploymentException(e);
		} catch (IllegalAccessException e) {
			throw new DeploymentException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(oldCl);
		}
	}

	public String getInstallRoot() {
		return this.context.getInstallRoot();
	}

	public ObjectName install() throws JBIException {
		if (isInstalled()) {
			throw new DeploymentException("Component is already installed");
		}
		initBootstrap();
		this.bootstrap.onInstall();

		ObjectName result = null;
		try {
			result = activateComponent();
			ComponentMBeanImpl lcc = this.container.getRegistry().getComponent(
					this.context.getComponentName());
			lcc.persistRunningState();
			this.context.setInstall(false);
		} finally {
			cleanUpBootstrap();
		}
		return result;
	}

	public ObjectName activateComponent() throws JBIException {
		ObjectName result = null;
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		com.sanxing.sesame.deployment.Component descriptor = this.context
				.getDescriptor();
		try {
			List classPaths = this.context.getClassPathElements();
			this.componentLoader = buildClassLoader(
					this.context.getInstallRootAsDir(),
					(String[]) (String[]) classPaths
							.toArray(new String[classPaths.size()]),
					descriptor.isComponentClassLoaderDelegationParentFirst(),
					this.context.getSharedLibraries());

			Thread.currentThread().setContextClassLoader(this.componentLoader);
			Class componentClass = this.componentLoader.loadClass(descriptor
					.getComponentClassName());
			javax.jbi.component.Component component = (javax.jbi.component.Component) componentClass
					.newInstance();
			result = this.container.getAdminCommandsService()
					.activateComponent(this.context.getInstallRootAsDir(),
							component, this.context.getComponentDescription(),
							(ComponentContextImpl) this.context.getContext(),
							this.context.isBinding(), this.context.isEngine(),
							this.context.getSharedLibraries(), this.container);
		} catch (MalformedURLException e) {
			throw new DeploymentException(e);
		} catch (NoClassDefFoundError e) {
			throw new DeploymentException(e);
		} catch (ClassNotFoundException e) {
			throw new DeploymentException(e);
		} catch (InstantiationException e) {
			throw new DeploymentException(e);
		} catch (IllegalAccessException e) {
			throw new DeploymentException(e);
		} catch (JBIException e) {
			throw new DeploymentException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(oldCl);
		}
		return result;
	}

	public boolean isInstalled() {
		return (!(this.context.isInstall()));
	}

	public void uninstall() throws JBIException {
		if (!(isInstalled())) {
			throw new DeploymentException("Component is not installed");
		}
		String componentName = this.context.getComponentName();
		try {
			this.container.getAdminCommandsService().deactivateComponent(
					componentName);
			this.bootstrap.onUninstall();
			this.context.setInstall(true);
		} finally {
			cleanUpBootstrap();

			this.componentLoader.destroy();
			this.bootstrapLoader.destroy();

			System.gc();
			this.container.getEnvironmentContext()
					.removeComponentRootDirectory(componentName);
		}
	}

	public ObjectName getInstallerConfigurationMBean() throws JBIException {
		return this.extensionMBeanName;
	}

	public ObjectName getObjectName() {
		return this.objectName;
	}

	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}

	private JarFileClassLoader buildClassLoader(File dir,
			String[] classPathNames, boolean parentFirst, String[] shareLibNames)
			throws MalformedURLException, DeploymentException {
		ClassLoader[] parents = prepareParentClassLoader(shareLibNames);
		List urls = parseComponentClasspath(dir, classPathNames);
		JarFileClassLoader classLoader = new JarFileClassLoader(
				(URL[]) (URL[]) urls.toArray(new URL[urls.size()]), parents,
				!(parentFirst), new String[0],
				new String[] { "java.", "javax." });

		if (LOG.isTraceEnabled()) {
			LOG.trace("Component class loader: " + classLoader);
		}
		return classLoader;
	}

	private JarFileClassLoader buildBootClassLoader(File dir,
			String[] classPathNames, boolean parentFirst, String[] shareLibNames)
			throws MalformedURLException, DeploymentException {
		ClassLoader[] parents = prepareParentClassLoader(shareLibNames);
		List urls = parseComponentClasspath(dir, classPathNames);
		JarFileClassLoader classLoader = new JarFileClassLoader(
				(URL[]) (URL[]) urls.toArray(new URL[urls.size()]), parents,
				!(parentFirst), new String[0],
				new String[] { "java.", "javax." });

		if (LOG.isDebugEnabled()) {
			LOG.debug("Component class loader: " + classLoader);
		}
		return classLoader;
	}

	private ClassLoader[] prepareParentClassLoader(String[] shareLibNames)
			throws DeploymentException {
		ClassLoader[] parents;
		if ((shareLibNames != null) && (shareLibNames.length > 0)) {
			parents = new ClassLoader[shareLibNames.length + 1];
			for (int i = 0; i < shareLibNames.length; ++i) {
				SharedLibrary sl = this.container.getRegistry()
						.getSharedLibrary(shareLibNames[i]);

				if (sl == null) {
					throw new DeploymentException("Shared library "
							+ shareLibNames[i] + " is not installed");
				}
				parents[i] = sl.getClassLoader();
			}
			parents[shareLibNames.length] = super.getClass().getClassLoader();
		} else {
			parents = new ClassLoader[] { super.getClass().getClassLoader() };
		}
		return parents;
	}

	private List<URL> parseComponentClasspath(File dir, String[] classPathNames)
			throws MalformedURLException {
		List urls = new ArrayList();
		for (int i = 0; i < classPathNames.length; ++i) {
			File file = new File(dir, classPathNames[i]);
			if (!(file.exists())) {
				LOG.warn("Unable to add File " + file
						+ " to class path as it doesn't exist: "
						+ file.getAbsolutePath());
			}

			urls.add(file.toURL());
		}

		File[] libs = new File(dir, "lib").listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		if (libs != null) {
			for (File lib : libs) {
				urls.add(lib.toURL());
			}
		}
		return urls;
	}
}