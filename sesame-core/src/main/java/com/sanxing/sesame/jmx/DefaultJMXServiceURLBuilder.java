package com.sanxing.sesame.jmx;

import com.sanxing.sesame.jmx.mbean.admin.ClusterAdminMBean;
import com.sanxing.sesame.jmx.mbean.admin.ServerInfo;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.Env;
import com.sanxing.sesame.core.api.MBeanHelper;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.management.remote.JMXServiceURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultJMXServiceURLBuilder implements JMXServiceURLBuilder {
	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultJMXServiceURLBuilder.class);

	Registry reg = null;

	Env env = null;

	BaseServer server = null;
	private int port;

	public DefaultJMXServiceURLBuilder(BaseServer _server, Env env) {
		this.server = _server;
		this.env = env;
		this.port = env.getAdminPort();

		if (this.reg != null)
			return;
		try {
			this.reg = LocateRegistry.getRegistry(this.port);
			this.reg.list();
			LOG.info("RMI registry located: " + this.reg);
		} catch (RemoteException e) {
			this.reg = null;
		}
		if (this.reg != null)
			return;
		try {
			LOG.info("Create RMI registry on port [" + this.port + "]");
			FixedPortSocketFactory fcf = new FixedPortSocketFactory(
					this.port + 1);
			this.reg = LocateRegistry.createRegistry(this.port, fcf, fcf);
		} catch (RemoteException e) {
			LOG.error("Can not create RMI registry on the server", e);
		}
	}

	public void shutdown() {
		try {
			LOG.info("REG is shutdwon ["
					+ UnicastRemoteObject.unexportObject(this.reg, false) + "]");
		} catch (RemoteException e) {
			LOG.debug(e.getMessage(), e);
		}
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public JMXServiceURL getAdminJMXServiceURL() {
		try {
			JMXServiceURL serviceURL = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://" + this.env.getAdminHost()
							+ ":" + this.port + "/admin");
			if (LOG.isDebugEnabled()) {
				LOG.info("admin service url [" + serviceURL.toString() + "]");
			}
			return serviceURL;
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e.getMessage(), e);
			} else
				LOG.error(e.getMessage());
		}
		return null;
	}

	public JMXServiceURL getLocalJMXServiceURL() {
		try {
			JMXServiceURL serviceURL = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://"
							+ this.server.getConfig().getIP() + ":" + this.port
							+ "/" + this.env.getServerName());
			if (LOG.isTraceEnabled()) {
				LOG.trace("local jmx service url is [" + serviceURL.toString()
						+ "]");
			}
			return serviceURL;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public JMXServiceURL getJMXServiceURLByServerName(String serverName) {
		try {
			ClusterAdminMBean clusterManager = (ClusterAdminMBean) MBeanHelper
					.getAdminMBean(ClusterAdminMBean.class, "cluster-manager");
			ServerInfo target = clusterManager.getServerInfoByName(serverName);
			if (target == null) {
				throw new RuntimeException("server not exists " + serverName);
			}
			String targetHost = target.getIP();

			JMXServiceURL serviceURL = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://" + targetHost + ":"
							+ this.port + "/" + serverName);
			if (LOG.isTraceEnabled()) {
				LOG.trace("server url " + serverName + " is ["
						+ serviceURL.toString() + "]");
			}
			return serviceURL;
		} catch (Exception e) {
			LOG.error("get serviceURL for server [" + serverName + "] err", e);
			throw new RuntimeException("build jmx service url for [ "
					+ serverName + "]", e);
		}
	}
}