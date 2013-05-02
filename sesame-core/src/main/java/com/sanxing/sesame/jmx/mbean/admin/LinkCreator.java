package com.sanxing.sesame.jmx.mbean.admin;

import com.sanxing.sesame.jmx.JMXServiceURLBuilder;
import com.sanxing.sesame.jmx.RemoteMBeanProxy;
import com.sanxing.sesame.core.AdminServer;
import com.sanxing.sesame.core.Platform;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkCreator implements LinkCreatorMBean {
	private AdminServer admin;
	private static final Logger LOG = LoggerFactory.getLogger(LinkCreator.class);

	public LinkCreator(AdminServer server) {
		this.admin = server;
	}

	public void register(ObjectName name, String serverName) {
		try {
			if (LOG.isDebugEnabled())
				LOG.debug("linking mbean [" + name + "] @" + serverName
						+ " to admin");
			ObjectName nameOnAdmin = name;
			JMXServiceURL managedServerURL = Platform.getJmxServiceURLBuilder()
					.getJMXServiceURLByServerName(serverName);
			if (LOG.isDebugEnabled())
				LOG.debug(managedServerURL.toString());
			JMXConnector managedServerConnector = this.admin
					.getJMXConnectorByServer(serverName);
			MBeanServerConnection managedServerCon = managedServerConnector
					.getMBeanServerConnection();
			RemoteMBeanProxy proxy = new RemoteMBeanProxy(name,
					managedServerCon);
			try {
				this.admin.getMBeanServer().unregisterMBean(nameOnAdmin);
			} catch (Throwable localThrowable) {
			}
			this.admin.getMBeanServer().registerMBean(proxy, nameOnAdmin);
		} catch (Exception e) {
			LOG.error("link to managed server err", e);
		}
	}

	public void unregister(ObjectName name, String serverName) {
		try {
			this.admin.getMBeanServer().unregisterMBean(name);
		} catch (Throwable e) {
			if (LOG.isDebugEnabled())
				LOG.debug(e.getMessage());
		}
	}
}