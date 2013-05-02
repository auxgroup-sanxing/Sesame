package com.sanxing.sesame.core;

import com.sanxing.sesame.jmx.mbean.admin.ClusterAdminMBean;
import com.sanxing.sesame.jmx.mbean.admin.LinkCreatorMBean;
import com.sanxing.sesame.jmx.mbean.admin.ServerInfo;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.core.event.ServerJoinEvent;
import com.sanxing.sesame.core.listener.join.ServerJoinListener;
import com.sanxing.sesame.util.GetterUtil;
import com.sanxing.sesame.util.SystemProperties;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedServer extends BaseServer {
	private Logger LOG = LoggerFactory.getLogger(ManagedServer.class);
	private boolean adminReachable;
	private Timer heartBeatTimer;
	public static final long HEART_BEAT_INTERVAL = GetterUtil.getInteger(
			SystemProperties.get("sesame.managed.server.hearbeat.interval"),
			30000);
	private MBeanServer proxiedMBeanServer;

	public boolean isAdminReachable() {
		return this.adminReachable;
	}

	protected void setAdminReachable(boolean adminReachablee) {
		this.adminReachable = adminReachablee;
	}

	public void start() {
		super.start();
		this.adminReachable = testConnectAdmin();
		Platform.getEnv().setClustered(this.adminReachable);

		ServerInfo serverInfo = prepareServerInfo();
		serverInfo.setServerState(1);

		setMyConfig(serverInfo);
		if (serverInfo == null)
			;
		Platform.getPlatform().startJMXServer();
		registerMBeans();

		startSystemService(serverInfo);
		this.LOG.info("System service started");

		startContainers();

		this.LOG.info("Container started");

		this.LOG.info("mbean registered");
		if (isAdminReachable()) {
			ServerJoinListener joinListener = new ServerJoinListener();
			addListener(joinListener);

			ClusterAdminMBean clusterAdmin = getClusterAdmin();
			clusterAdmin.updateState(getName(), 2);
			this.LOG.info("Update server state to running......");

			List<ServerInfo> servers = clusterAdmin.heartBeat(getName());
			ArrayList neighbours = new ArrayList();
			for (ServerInfo server : servers) {
				if ((server.getServerName().equals(getName()))
						&& (!(server.getServerName().equals("admin"))))
					continue;
				neighbours.add(server);
			}

			ServerInfo[] configs = new ServerInfo[neighbours.size()];
			setNeighboursConfigs((ServerInfo[]) neighbours.toArray(configs));

			ServerJoinEvent event = new ServerJoinEvent();
			event.setEventSource(getName());
			clusterAdmin.notifyNeighbors(event);

			this.heartBeatTimer = new Timer();
			this.heartBeatTimer.schedule(new HeartBeat(), 0L,
					HEART_BEAT_INTERVAL);
		}

		serverInfo.setServerState(2);
	}

	public void shutdown() {
		getConfig().setServerState(3);

		if (isAdminReachable()) {
			this.heartBeatTimer.cancel();

			ClusterAdminMBean clusterAdmin = getClusterAdmin();
			clusterAdmin.updateState(getName(), 0);
			this.LOG.info("Update server state to shutdown");
		}

		super.shutdown();
	}

	private ClusterAdminMBean getClusterAdmin() {
		ClusterAdminMBean clusterAdmin = (ClusterAdminMBean) MBeanHelper
				.getAdminMBean(ClusterAdminMBean.class, "cluster-manager");
		return clusterAdmin;
	}

	private ServerInfo prepareServerInfo() {
		try {
			ServerInfo answer = null;
			if (this.adminReachable) {
				ClusterAdminMBean clusterAdmin = (ClusterAdminMBean) MBeanHelper
						.getAdminMBean(ClusterAdminMBean.class,
								"cluster-manager");
				answer = clusterAdmin.getServerInfoByName(getName());
				if (answer == null)
					throw new RuntimeException(
							"Can not got serverinfo for server [" + getName()
									+ "] please check your server-name");
				ServerInfo.writeToFile(getServerDir(), answer);
			} else {
				answer = ServerInfo.fromFile(getServerDir(), getName());
				this.LOG.info("continue with local defination");
			}

			return answer;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("can not got serverinfo", e);
		}
	}

	private boolean testConnectAdmin() {
		JMXConnector connector = null;
		try {
			JMXServiceURL adminServerURL = Platform.getJmxServiceURLBuilder()
					.getAdminJMXServiceURL();
			Map environment = new HashMap();
			environment.put("java.naming.factory.initial",
					"com.sun.jndi.rmi.registry.RegistryContextFactory");
			connector = JMXConnectorFactory
					.connect(adminServerURL, environment);

			MBeanServerConnection connection = connector
					.getMBeanServerConnection();
			connection.getMBeanCount();
			this.LOG.info("Connected admin server, mbean count: "
					+ connection.getMBeanCount());
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (connector != null)
				try {
					connector.close();
				} catch (IOException e) {
					this.LOG.error(e.getMessage(), e);
				}
		}
	}

	public MBeanServer getMBeanServer() {
		MBeanServer server = super.getMBeanServer();
		if (!(this.adminReachable)) {
			return server;
		}
		return proxyMBeanServer(server);
	}

	private MBeanServer proxyMBeanServer(MBeanServer mbeanServer) {
		if (this.proxiedMBeanServer == null) {
			this.proxiedMBeanServer = ((MBeanServer) Proxy.newProxyInstance(
					super.getClass().getClassLoader(),
					new Class[] { MBeanServer.class }, new MBeanServerProxy(
							mbeanServer)));
		}

		return this.proxiedMBeanServer;
	}

	class HeartBeat extends TimerTask {
		private static final long MAX_TARDINESS = 1000L;

		public void run() {
			if (System.currentTimeMillis() - scheduledExecutionTime() >= 1000L)
				return;
			ManagedServer.this.getClusterAdmin().heartBeat(
					ManagedServer.this.getName());
		}
	}

	public class MBeanServerProxy implements InvocationHandler {
		private MBeanServer server;

		public MBeanServerProxy(MBeanServer paramMBeanServer) {
			this.server = paramMBeanServer;
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			try {
				if (method.getName().equals("unregisterMBean")) {
					unregisterAtAdmin((ObjectName) args[0]);
				}
				Object obj = method.invoke(this.server, args);
				if (method.getName().equals("registerMBean")) {
					if (ManagedServer.this.LOG.isTraceEnabled()) {
						ManagedServer.this.LOG.trace("Forward mbean ["
								+ args[1] + " to admin server");
					}
					link2admin((ObjectName) args[1]);
				}
				return obj;
			} catch (InvocationTargetException e) {
				Throwable t = e.getTargetException();
				throw t;
			}
		}

		private void link2admin(ObjectName name) {
			try {
				LinkCreatorMBean linker = (LinkCreatorMBean) MBeanHelper
						.getAdminMBean(LinkCreatorMBean.class,
								"admin-link-creator");

				linker.register(name, ManagedServer.this.getName());
			} catch (Throwable t) {
				if (ManagedServer.this.LOG.isDebugEnabled()) {
					ManagedServer.this.LOG.debug(t.getMessage());
				}
				System.err
						.println("can not connect to admin server, mbean is only registered locally");
			}
		}

		private void unregisterAtAdmin(ObjectName name) {
			try {
				LinkCreatorMBean registry = (LinkCreatorMBean) MBeanHelper
						.getAdminMBean(LinkCreatorMBean.class,
								"admin-link-creator");
				registry.unregister(name, ManagedServer.this.getName());
			} catch (Throwable t) {
				ManagedServer.this.LOG
						.error("Can not connect to Admin Server, mbean is only registered locally");
				if (ManagedServer.this.LOG.isTraceEnabled())
					ManagedServer.this.LOG.trace(t.getMessage());
			}
		}
	}
}