package com.sanxing.sesame.container;

import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.BaseLifeCycle;
import com.sanxing.sesame.mbean.ArchiveManager;
import com.sanxing.sesame.mbean.AutoDeploymentService;
import com.sanxing.sesame.mbean.BaseSystemService;
import com.sanxing.sesame.mbean.CommandsService;
import com.sanxing.sesame.mbean.DeploymentService;
import com.sanxing.sesame.mbean.InstallationService;
import com.sanxing.sesame.mbean.ManagementContext;
import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.router.MessageRouter;
import com.sanxing.sesame.router.Router;
import com.sanxing.sesame.uuid.IdGenerator;
import java.io.File;
import java.io.PrintStream;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.jbi.JBIException;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.LifeCycleMBean;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JBIContainer extends BaseLifeCycle {
	public static final String DEFAULT_NAME = "sesame";
	private static final Logger LOG = LoggerFactory.getLogger(JBIContainer.class);
	private MBeanServer mbeanServer;
	protected Router router = new MessageRouter();
	protected ServiceUnitManager serviceManager;
	protected ManagementContext managementContext = new ManagementContext();
	protected EnvironmentContext environmentContext = new EnvironmentContext();
	protected InstallationService installationService = new InstallationService();
	protected DeploymentService deploymentService = new DeploymentService();
	protected AutoDeploymentService autoDeployService = new AutoDeploymentService();
	protected CommandsService adminCommandsService = new CommandsService();
	protected ArchiveManager archiveManager = new ArchiveManager();
	protected BaseSystemService[] services;
	protected Registry registry = new Registry();
	protected boolean autoEnlistInTransaction;
	protected boolean persistent;
	protected boolean notifyStatistics;
	protected transient Thread shutdownHook;
	protected ExecutorFactory executorFactory;
	private String rootDir;
	private String generatedRootDirPrefix = "../work";
	private boolean generateRootDir;
	private AtomicBoolean started = new AtomicBoolean(false);
	private AtomicBoolean containerInitialized = new AtomicBoolean(false);
	private IdGenerator idGenerator = new IdGenerator();
	private long forceShutdown;

	public String getName() {
		return "sesame";
	}

	public String getJmxDomain() {
		return "com.sanxing.sesame";
	}

	public String getServerName() {
		return "sesame";
	}

	public AtomicBoolean getStarted() {
		return this.started;
	}

	public String getDescription() {
		return "Sesame JBI Container";
	}

	public BaseSystemService[] getServices() {
		return this.services;
	}

	public void setServices(BaseSystemService[] services) {
		this.services = services;
	}

	public ManagementContext getManagementContext() {
		return this.managementContext;
	}

	public EnvironmentContext getEnvironmentContext() {
		return this.environmentContext;
	}

	public Registry getRegistry() {
		return this.registry;
	}

	public MessageRouter getDefaultBroker() {
		if (!(this.router instanceof MessageRouter)) {
			throw new IllegalStateException("Router is not a MessageRouter");
		}
		return ((MessageRouter) this.router);
	}

	public Router getRouter() {
		return this.router;
	}

	public String getInstallationDirPath() {
		File dir = this.environmentContext.getInstallationDir();
		return ((dir != null) ? dir.getAbsolutePath() : "");
	}

	public void setInstallationDirPath(String installationDir) {
		if ((installationDir != null) && (installationDir.length() > 0))
			this.environmentContext
					.setInstallationDir(new File(installationDir));
	}

	public String getDeploymentDirPath() {
		File dir = this.environmentContext.getDeploymentDir();
		return ((dir != null) ? dir.getAbsolutePath() : "");
	}

	public void setDeploymentDirPath(String deploymentDir) {
		if ((deploymentDir != null) && (deploymentDir.length() > 0))
			this.environmentContext.setDeploymentDir(new File(deploymentDir));
	}

	public DeploymentService getDeploymentService() {
		return this.deploymentService;
	}

	public InstallationService getInstallationService() {
		return this.installationService;
	}

	public AutoDeploymentService getAutoDeploymentService() {
		return this.autoDeployService;
	}

	public CommandsService getAdminCommandsService() {
		return this.adminCommandsService;
	}

	public String getGeneratedRootDirPrefix() {
		return this.generatedRootDirPrefix;
	}

	public void setGeneratedRootDirPrefix(String generatedRootDirPrefix) {
		this.generatedRootDirPrefix = generatedRootDirPrefix;
	}

	public boolean isGenerateRootDir() {
		return this.generateRootDir;
	}

	public long getForceShutdown() {
		return this.forceShutdown;
	}

	public void setForceShutdown(long forceShutdown) {
		this.forceShutdown = forceShutdown;
	}

	public void setGenerateRootDir(boolean generateRootDir) {
		this.generateRootDir = generateRootDir;
	}

	public MBeanServer getMBeanServer() {
		return this.mbeanServer;
	}

	public ArchiveManager getArchiveManager() {
		return this.archiveManager;
	}

	public void init() throws JBIException {
		if (!(this.containerInitialized.compareAndSet(false, true)))
			return;
		LOG.info("Sesame " + EnvironmentContext.getVersion()
				+ " JBI Container (" + getName() + ") is starting");
		LOG.info("--------------------------------------------------------------------------------------------");

		if (this.executorFactory == null) {
			this.executorFactory = ExecutorFactory.getFactory();
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("executor factory is [" + getExecutorFactory() + "]");
		}
		this.managementContext.init(this);
		this.mbeanServer = this.managementContext.getMBeanServer();
		this.environmentContext.init(this);
		this.registry.init(this);
		this.router.init(this);

		this.archiveManager.init(this);
		this.adminCommandsService.init(this);
		this.installationService.init(this);
		this.deploymentService.init(this);
		this.autoDeployService.init(this);

		if (this.services != null) {
			for (int i = 0; i < this.services.length; ++i) {
				this.services[i].init(this);
			}
		}

		try {
			this.managementContext.registerMBean(ManagementContext
					.getContainerObjectName(getJmxDomain(), getServerName()),
					this, LifeCycleMBean.class);
		} catch (JMException e) {
			throw new JBIException(e);
		}
	}

	public void start() throws JBIException {
		checkInitialized();
		if (this.started.compareAndSet(false, true)) {
			this.managementContext.start();
			this.environmentContext.start();

			if (this.services != null) {
				for (int i = 0; i < this.services.length; ++i) {
					this.services[i].start();
				}
			}
			this.router.start();
			this.registry.start();
			this.installationService.start();
			this.deploymentService.start();
			this.autoDeployService.start();
			this.adminCommandsService.start();
			super.start();
			LOG.info("Sesame JBI Container (" + getName() + ") started");
		}
	}

	public void stop() throws JBIException {
		checkInitialized();
		if (this.started.compareAndSet(true, false)) {
			LOG.info("Sesame JBI Container (" + getName() + ") stopping");
			this.adminCommandsService.stop();
			this.autoDeployService.stop();
			this.deploymentService.stop();
			this.installationService.stop();
			this.registry.stop();
			this.router.stop();
			if (this.services != null) {
				for (int i = this.services.length - 1; i >= 0; --i) {
					this.services[i].stop();
				}
			}
			this.environmentContext.stop();
			this.managementContext.stop();
			super.stop();
		}
	}

	public void shutDown() throws JBIException {
		LOG.info("shutdown");
		if (this.containerInitialized.compareAndSet(true, false)) {
			LOG.info("Shutting down Sesame JBI Container (" + getName()
					+ ") stopped");
			removeShutdownHook();
			this.adminCommandsService.shutDown();
			this.autoDeployService.shutDown();
			this.deploymentService.shutDown();
			this.installationService.shutDown();

			this.router.shutDown();
			shutdownRegistry();
			shutdownServices();

			this.environmentContext.shutDown();

			super.shutDown();

			this.managementContext.shutDown();
			LOG.info("Sesame JBI Container (" + getName() + ") stopped");
		}
	}

	private void shutdownServices() throws JBIException {
		if (this.services != null)
			for (int i = this.services.length - 1; i >= 0; --i)
				this.services[i].shutDown();
	}

	private void shutdownRegistry() throws JBIException {
		FutureTask shutdown = new FutureTask(new Callable() {
			public Boolean call() throws Exception {
				JBIContainer.this.registry.shutDown();
				return Boolean.valueOf(true);
			}
		});
		Thread daemonShutDownThread = new Thread(shutdown);
		daemonShutDownThread.setDaemon(true);
		daemonShutDownThread.start();
		try {
			if (this.forceShutdown > 0L) {
				LOG.info("Waiting another "
						+ this.forceShutdown
						+ " ms for complete shutdown of the components and service assemblies");
				shutdown.get(this.forceShutdown, TimeUnit.MILLISECONDS);
			} else {
				LOG.info("Waiting for complete shutdown of the components and service assemblies");
				shutdown.get();
			}
			LOG.info("Components and service assemblies have been shut down");
		} catch (Exception e) {
			LOG.warn(
					"Unable to shutdown components and service assemblies normally: "
							+ e, e);
			LOG.warn("Forcing shutdown by cancelling all pending exchanges");
			this.registry.cancelPendingExchanges();
		}
	}

	protected void addShutdownHook() {
		this.shutdownHook = new Thread("Sesame-ShutdownHook") {
			public void run() {
				JBIContainer.this.containerShutdown();
			}
		};
		Runtime.getRuntime().addShutdownHook(this.shutdownHook);
	}

	protected void removeShutdownHook() {
		if (this.shutdownHook == null)
			return;
		try {
			Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
		} catch (Exception e) {
			LOG.debug("Caught exception, must be shutting down: " + e);
		}
	}

	protected void containerShutdown() {
		try {
			shutDown();
		} catch (Throwable e) {
			System.err.println("Failed to shut down: " + e);
		}
	}

	public synchronized InitialContext getNamingContext() {
		return null;
	}

	public synchronized Object getTransactionManager() {
		return null;
	}

	public synchronized String getRootDir() {
		if (this.rootDir == null) {
			if (isGenerateRootDir())
				this.rootDir = createRootDir();
			else {
				this.rootDir = "." + File.separator + "work";
			}
			LOG.debug("Defaulting to rootDir: " + this.rootDir);
		}
		return this.rootDir;
	}

	public synchronized void setRootDir(String root) {
		this.rootDir = root;
	}

	public Logger getLogger(String name, String resourceBundleName)
			throws MissingResourceException, JBIException {
		try {
			Logger logger = Logger.getLogger(name, resourceBundleName);

			return logger;
		} catch (IllegalArgumentException e) {
			throw new JBIException(
					"A logger can not be created using resource bundle "
							+ resourceBundleName);
		}
	}

	protected String createComponentID() {
		return this.idGenerator.generateId();
	}

	protected void checkInitialized() throws JBIException {
		if (!(this.containerInitialized.get()))
			throw new JBIException(
					"The Container is not initialized - please call init(...)");
	}

	public boolean isAutoEnlistInTransaction() {
		return this.autoEnlistInTransaction;
	}

	public void setAutoEnlistInTransaction(boolean autoEnlistInTransaction) {
		this.autoEnlistInTransaction = autoEnlistInTransaction;
	}

	public boolean isPersistent() {
		return this.persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public ExecutorFactory getExecutorFactory() {
		return this.executorFactory;
	}

	protected String createRootDir() {
		String prefix = getGeneratedRootDirPrefix();
		for (int i = 1;; ++i) {
			File file = new File(prefix + i);
			if (!(file.exists())) {
				file.mkdirs();
				return file.getAbsolutePath();
			}
		}
	}

	public String getLoggerName() {
		return "sesame.log";
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		MBeanAttributeInfo[] attrs = super.getAttributeInfos();

		AttributeInfoHelper helper = new AttributeInfoHelper();

		helper.addAttribute(getObjectToManage(), "loggerName",
				"Current log name of container");

		return AttributeInfoHelper.join(attrs, helper.getAttributeInfos());
	}

	public String toString() {
		return "SESAME JBI Container :" + getName() + "@" + getCurrentState();
	}
}