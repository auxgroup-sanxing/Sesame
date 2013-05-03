package com.sanxing.sesame.core;

import com.sanxing.sesame.executors.Callback;
import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.executors.impl.ExecutorFactoryImpl;
import com.sanxing.sesame.jmx.mbean.ServerManager;
import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.jmx.mbean.admin.ContainerInfo;
import com.sanxing.sesame.jmx.mbean.admin.ServerInfo;
import com.sanxing.sesame.jmx.mbean.managed.FileClient;
import com.sanxing.sesame.core.api.Container;
import com.sanxing.sesame.core.api.ContainerContext;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.core.jdbc.DataSourceInfo;
import com.sanxing.sesame.core.jdbc.DataSourceProvider;
import com.sanxing.sesame.core.jms.JMSProvider;
import com.sanxing.sesame.core.listener.ClusterListener;
import com.sanxing.sesame.util.SystemProperties;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

public class BaseServer {
	private static Logger LOG = LoggerFactory.getLogger(BaseServer.class);

	private List<ClusterListener> listeners = new LinkedList();

	private List<ContainerInfo> containersInfos = new LinkedList();

	private List<Container> runningContainers = new LinkedList();
	private ServerInfo myConfig;
	private ServerInfo[] neighbourconfigs;
	private JMSProvider jmsProvider;
	private DataSourceProvider datasourceProvider;
	private ExecutorFactory executorFactory = ExecutorFactory.getFactory();

	public ExecutorFactory getExecutorFactory() {
		return this.executorFactory;
	}

	protected JMSProvider getJmsProvider() {
		if (this.jmsProvider == null) {
			String providerClazz = SystemProperties.get(
					"com.sanxing.sesame.core.jms.provider",
					"com.sanxing.sesame.core.jms.ActiveMQJMSProvider");
			try {
				this.jmsProvider = ((JMSProvider) Class.forName(providerClazz)
						.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		return this.jmsProvider;
	}

	public File getServerDir() {
		return Platform.getEnv().getHomeDir();
	}

	protected DataSourceProvider getDataSourceProvider(
			int transactionManagerType) {
		String providerClazz = null;
		if (transactionManagerType == 1)
			providerClazz = "com.sanxing.sesame.core.jdbc.STMProvider";
		else if (transactionManagerType == 2)
			providerClazz = "com.sanxing.sesame.core.jdbc.BTMProvider";
		else
			providerClazz = SystemProperties.get(
					"com.sanxing.sesame.core.jdbc.datasource.provider",
					"com.sanxing.sesame.core.jdbc.DBCPProvider");
		try {
			this.datasourceProvider = ((DataSourceProvider) Thread
					.currentThread().getContextClassLoader()
					.loadClass(providerClazz).newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return this.datasourceProvider;
	}

	protected void setMyConfig(ServerInfo myConfig) {
		this.myConfig = myConfig;
	}

	protected void setNeighboursConfigs(ServerInfo[] otherConfigs) {
		this.neighbourconfigs = otherConfigs;
	}

	public ServerInfo getConfig() {
		return this.myConfig;
	}

	public ServerInfo[] getNeighboursConfig() {
		return this.neighbourconfigs;
	}

	public InitialContext getNamingContext() {
		return Platform.getNamingContext();
	}

	public MBeanServer getMBeanServer() {
		return Platform.getLocalMBeanServer();
	}

	public void regsiterContainer(ContainerInfo container) {
		this.containersInfos.add(container);
	}

	public void listenClusterEvent(ClusterEvent event) {
		LOG.debug("Received event ................." + event);
		for (ClusterListener listener : this.listeners) {
			listener.setServer(this);
			listener.listen(event);
		}
	}

	public void addListener(ClusterListener listener) {
		listener.setServer(this);
		synchronized (this.listeners) {
			this.listeners.add(listener);
		}
	}

	public String getName() {
		return Platform.getEnv().getServerName();
	}

	public boolean isAdmin() {
		return Platform.getEnv().isAdmin();
	}

	public boolean isProduction() {
		return Platform.getEnv().isProduction();
	}

	public void shutdown() {
		for (Container container : this.runningContainers) {
			try {
				container.shutdown();
			} catch (Exception e) {
				LOG.debug("Shutdown container '" + container + "' failure", e);
			}
		}

		if (this.jmsProvider != null) {
			this.jmsProvider.release();
		}

		getConfig().setServerState(0);
	}

	public void start() {
	}

	protected void registerMBeans() {
		try {
			FileClient fileClient = new FileClient();
			MBeanHelper.registerMBean(getMBeanServer(), fileClient,
					MBeanHelper.getPlatformMBeanName("file-client"));
			ServerManager serverManager = new ServerManager(this);
			MBeanHelper.registerMBean(getMBeanServer(), serverManager,
					MBeanHelper.getPlatformMBeanName("server-manager"));
		} catch (Exception e) {
			throw new RuntimeException("Register mbean error", e);
		}
	}

	protected void startSystemService(ServerInfo serverInfo) {
		getJmsProvider().prepare(this, serverInfo.getJmsServiceInfo());
		for (DataSourceInfo dsi : serverInfo.getDatasourceInfos()) {
			getDataSourceProvider(dsi.getTransactionManager()).provide(this,
					dsi);
		}

		ExecutorFactoryImpl factoryImpl = ExecutorFactoryImpl.getFactory();
		factoryImpl.getConfigs().putAll(serverInfo.getExecutorInfos());
		this.executorFactory = factoryImpl;

		String value = System.getProperty("sesame.executors.callback", "");
		String[] classes = (value.length() > 0) ? value.split(",")
				: new String[0];
		Callback[] callbacks = new Callback[classes.length];
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String className;
		for (int i = 0; i < classes.length; ++i) {
			className = classes[i];
			try {
				Class clazz = loader.loadClass(className);
				Callback callback = (Callback) clazz.asSubclass(Callback.class)
						.newInstance();
				callbacks[i] = callback;
				LOG.debug("Executor Callback: " + callback);
			} catch (Exception e) {
				LOG.error("Load executor callback failure", e);
			}
		}

		for (String id : factoryImpl.getConfigs().keySet()) {
			Executor executor = this.executorFactory.createExecutor(id,
					callbacks);
			ObjectName name = MBeanHelper.getMBeanName("ThreadPool", id);
			try {
				getMBeanServer().registerMBean(executor, name);
				LOG.debug("Created thread pool .." + id
						+ factoryImpl.getConfigs().get(id));
			} catch (Exception e) {
				LOG.error("Fail to create thread pool", e);
			}
		}
	}

	protected void startContainers() {
		for (ContainerInfo ContainerInfo : this.myConfig.getContainerInfos()) {
			try {
				regsiterContainer(ContainerInfo);
			} catch (Exception e) {
				throw new RuntimeException("error to create container");
			}

		}

		ContainerInfo jbi = new ContainerInfo();
		jbi.setContainerClazz("com.sanxing.sesame.runtime.RuntimeContainer");
		jbi.setName("jbi");
		regsiterContainer(jbi);

		ContainerInfo webapps = new ContainerInfo();
		webapps.setContainerClazz("com.sanxing.sesame.runtime.WebAppContainer");
		webapps.setName("webapps");
		regsiterContainer(webapps);

		ClassLoader serverClassLoader = Thread.currentThread()
				.getContextClassLoader();

		for (ContainerInfo containerInfo : this.containersInfos) {
			try {
				LOG.info("Starting container [" + containerInfo.getName() + "]");
				ContainerContext context = new ContainerContext(
						containerInfo.getName(), Platform.getEnv(),
						getNamingContext(), getMBeanServer(), this);
				Element paramsEl = containerInfo.getCotnainerParams();
				if (paramsEl != null) {
					List<Element> paramList = paramsEl.getChildren();
					for (Element paramEl : paramList) {
						context.put(paramEl.getName(), paramEl.getText());
					}
				}

				ClassLoader containerClassLoader = context
						.getContainerClassLoader();
				Thread.currentThread().setContextClassLoader(
						containerClassLoader);
				Class containerClazz = containerClassLoader
						.loadClass(containerInfo.getContainerClazz());

				Container container = (Container) containerClazz.newInstance();

				container.init(context);
				container.start();
				this.runningContainers.add(container);
			} catch (Throwable e) {
				LOG.error(e.getMessage(), e);
			} finally {
				Thread.currentThread().setContextClassLoader(serverClassLoader);
			}
		}

		LOG.info("Containers on server started");
	}
}