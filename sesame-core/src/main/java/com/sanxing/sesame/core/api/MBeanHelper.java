package com.sanxing.sesame.core.api;

import com.sanxing.sesame.jmx.JMXServiceURLBuilder;
import com.sanxing.sesame.core.Environment;
import com.sanxing.sesame.core.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class MBeanHelper {
	static Map<String, JMXConnector> connectors = new HashMap();

	private static final Logger LOGGER = LoggerFactory.getLogger(MBeanHelper.class);

	private static JMXConnector getConnector(String serverName) {
		boolean valid = true;
		JMXConnector jmxServerConnector = null;
		if (connectors.containsKey(serverName)) {
			try {
				jmxServerConnector = (JMXConnector) connectors.get(serverName);
				jmxServerConnector.getMBeanServerConnection()
						.getDefaultDomain();
			} catch (Exception e) {
				valid = false;
				if (jmxServerConnector != null) {
					try {
						jmxServerConnector.close();
					} catch (IOException e1) {
						LOGGER.debug(e1.getMessage(), e1);
					}
				}
			}
		}

		if ((!(connectors.containsKey(serverName))) || (!(valid))) {
			JMXServiceURL jmxServerURL;
			if (serverName.equals("admin")) {
				jmxServerURL = Platform.getJmxServiceURLBuilder()
						.getAdminJMXServiceURL();
			} else {
				if (serverName.endsWith(Platform.getEnv().getServerName())) {
					jmxServerURL = Platform.getJmxServiceURLBuilder()
							.getLocalJMXServiceURL();
				} else
					jmxServerURL = Platform.getJmxServiceURLBuilder()
							.getJMXServiceURLByServerName(serverName);
			}
			try {
				Map environment = new HashMap();
				environment.put("java.naming.factory.initial",
						"com.sun.jndi.rmi.registry.RegistryContextFactory");
				jmxServerConnector = JMXConnectorFactory.connect(jmxServerURL,
						environment);

				jmxServerConnector.getMBeanServerConnection()
						.getDefaultDomain();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			connectors.put(serverName, jmxServerConnector);
		}

		return ((JMXConnector) connectors.get(serverName));
	}

	private static <T> T getAdminMBean(Class<T> clazz, ObjectName name) {
		try {
			JMXConnector adminServerConnector = getConnector("admin");
			MBeanServerConnection adminServerCon = adminServerConnector
					.getMBeanServerConnection();

			return MBeanServerInvocationHandler.newProxyInstance(
					adminServerCon, name, clazz, false);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	public static <T> T getAdminMBean(Class<T> clazz, String name) {
		try {
			ObjectName oname = ObjectName.getInstance(Platform.getEnv()
					.getDomain()
					+ ":ServerName=admin,Type=Platform"
					+ ",Name="
					+ name);
			return getAdminMBean(clazz, oname);
		} catch (Exception e) {
		}
		return null;
	}

	public static <T> T getManagedMBean(Class<T> clazz, ObjectName name) {
		try {
			JMXConnector jmxServerConnector = getConnector(Platform.getEnv()
					.getServerName());

			MBeanServerConnection adminServerCon = jmxServerConnector
					.getMBeanServerConnection();

			return MBeanServerInvocationHandler.newProxyInstance(
					adminServerCon, name, clazz, false);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	public static ObjectName getMBeanName(String beanName) {
		try {
			return ObjectName.getInstance(Platform.getEnv().getDomain()
					+ ":ServerName=" + Platform.getEnv().getServerName()
					+ ",name=" + beanName);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("error got mbean name");
		}
	}

	public static ObjectName getMBeanNameOnServer(String beanName,
			String serverName) {
		try {
			return ObjectName.getInstance(Platform.getEnv().getDomain()
					+ ":ServerName=" + serverName + ",Type=Platform" + ",Name="
					+ beanName);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("error got mbean name");
		}
	}

	public static ObjectName getMBeanName(String type, String beanName) {
		try {
			return ObjectName.getInstance(Platform.getEnv().getDomain()
					+ ":ServerName=" + Platform.getEnv().getServerName()
					+ ",Type=" + type + ",Name=" + beanName);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("error got mbean name");
		}
	}

	public static ObjectName getMBeanName(String type, String subType,
			String beanName) {
		try {
			return ObjectName.getInstance(Platform.getEnv().getDomain()
					+ ":ServerName=" + Platform.getEnv().getServerName()
					+ ",Type=" + type + ",SubType=" + subType + ",Name="
					+ beanName);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("error got mbean name");
		}
	}

	public static ObjectName getPlatformMBeanName(String beanName) {
		return getMBeanName("Platform", beanName);
	}

	public static void registerMBean(MBeanServer server, Object mbean,
			ObjectName name) {
		try {
			if (server.isRegistered(name))
				server.unregisterMBean(name);
		} catch (Throwable t) {
			LOGGER.error(t.getMessage());
		}
		try {
			server.registerMBean(mbean, name);
		} catch (Throwable t) {
			LOGGER.error("register mbean err", t);
			throw new RuntimeException("register mbean [" + name + "] err", t);
		}
	}
}