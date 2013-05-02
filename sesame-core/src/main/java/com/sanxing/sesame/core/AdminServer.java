package com.sanxing.sesame.core;

import com.sanxing.sesame.jmx.mbean.admin.ClusterAdmin;
import com.sanxing.sesame.jmx.mbean.admin.FileServer;
import com.sanxing.sesame.jmx.mbean.admin.LinkCreator;
import com.sanxing.sesame.jmx.mbean.admin.ServerInfo;
import com.sanxing.sesame.jmx.mbean.admin.ServerState;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.core.listener.ServerLeaveListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminServer extends BaseServer {
	private static Logger LOG = LoggerFactory.getLogger(AdminServer.class);

	private static Map<String, JMXConnector> connectorCache = new HashMap();

	public JMXConnector getJMXConnectorByServer(String serverName) {
		try {
			if (!(connectorCache.containsKey(serverName))) {
				JMXServiceURL managedServerURL = Platform
						.getJmxServiceURLBuilder()
						.getJMXServiceURLByServerName(serverName);
				if (LOG.isDebugEnabled()) {
					LOG.debug(managedServerURL.toString());
				}
				Map environment = new HashMap();
				environment.put("java.naming.factory.initial",
						"com.sun.jndi.rmi.registry.RegistryContextFactory");
				JMXConnector managedServerConnector = JMXConnectorFactory
						.newJMXConnector(managedServerURL, environment);
				connectorCache.put(serverName, managedServerConnector);
			}
			JMXConnector connector = (JMXConnector) connectorCache
					.get(serverName);
			connector.connect();
			return connector;
		} catch (IOException e) {
			throw new RuntimeException("Can not connect to jmx server", e);
		}
	}

	public void closeJMXConnector(String serverName) {
		if (connectorCache.containsKey(serverName)) {
			JMXConnector connector = (JMXConnector) connectorCache
					.get(serverName);
			connectorCache.remove(serverName);
			try {
				connector.close();
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
		}
	}

	public void start() {
		super.start();

		ServerInfo serverInfo = prepareServerInfo();
		LOG.info("got server info [" + serverInfo + "]");
		serverInfo.setServerState(ServerState.STARTING);

		setMyConfig(serverInfo);

		Platform.getPlatform().startJMXServer();

		registerMBeans();

		startSystemService(serverInfo);

		startContainers();

		ServerLeaveListener listener = new ServerLeaveListener();
		addListener(listener);

		serverInfo.setServerState(ServerState.RUNNING);
	}

	private ServerInfo prepareServerInfo() {
		ServerInfo server = null;
		try {
			server = ServerInfo.fromFile(getServerDir(), "admin");
		} catch (FileNotFoundException e) {
		}
		return server;
	}

	protected void registerMBeans() {
		super.registerMBeans();
		MBeanHelper.registerMBean(getMBeanServer(), new ClusterAdmin(this),
				MBeanHelper.getPlatformMBeanName("cluster-manager"));
		MBeanHelper.registerMBean(getMBeanServer(), new LinkCreator(this),
				MBeanHelper.getPlatformMBeanName("admin-link-creator"));
		MBeanHelper.registerMBean(getMBeanServer(), new FileServer(),
				MBeanHelper.getPlatformMBeanName("file-server"));
		LOG.info("Admin mbean registered");
	}
}