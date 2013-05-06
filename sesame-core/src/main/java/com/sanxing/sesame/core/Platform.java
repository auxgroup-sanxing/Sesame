package com.sanxing.sesame.core;

import com.sanxing.sesame.jmx.DefaultJMXServiceURLBuilder;
import com.sanxing.sesame.jmx.JMXServiceURLBuilder;
import com.sanxing.sesame.jmx.mbean.PlatformManager;
import com.sanxing.sesame.jmx.mbean.PlatformManagerMBean;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.core.naming.JNDIUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.ContextNotEmptyException;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Platform {
	private static final Logger LOG;
	private Env env = new Env();
	private MBeanServer mbeanServer;
	private JMXConnectorServer connectorServer;
	private JMXServiceURLBuilder jmxServiceURLBuilder;
	private BaseServer server;
	private InitialContext namingContext;
	private transient Thread shutdownHook;
	private static Platform instance;

	static {
		parsePropertyFile(new File(System.getProperty("SESAME_HOME"),
				"conf/sesame.properties"));

		Console.echo(System.getProperty("sesame.echo", "on").equals("on"));

		System.setOut(Console.out);
		System.setErr(Console.err);
		
		validateLicense();

		LOG = LoggerFactory.getLogger(Platform.class);

		instance = new Platform();
	}

	private static void validateLicense() {
	}

	private static void parsePropertyFile(File propertiesFile) {
		Properties properites = new Properties();
		try {
			properites.load(new FileInputStream(propertiesFile));
			Enumeration enumer = properites.propertyNames();

			while (enumer.hasMoreElements()) {
				String key = (String) enumer.nextElement();
				String value = properites.getProperty(key);

				if (System.getProperty(key) == null)
					System.setProperty(key, value);
			}
		} catch (IOException e) {
			System.out.println("Load conf/sesame.properties failure!");
		}
	}

	private Platform() {
		this.namingContext = JNDIUtil.getInitialContext();

		if (this.env.isAdmin()) {
			this.server = new AdminServer();
			this.env.setClustered(false);
		} else {
			this.env.setClustered(true);
			this.server = new ManagedServer();
		}
	}

	public static Platform getPlatform() {
		return instance;
	}

	public static JMXServiceURLBuilder getJmxServiceURLBuilder() {
		return instance.jmxServiceURLBuilder;
	}

	private void start() {
		try {
			addShutdownHook();
			this.jmxServiceURLBuilder = new DefaultJMXServiceURLBuilder(
					this.server, this.env);

			addSystemClusterListener();
			this.server.start();
			PlatformManagerMBean coreManager = new PlatformManager();
			MBeanHelper.registerMBean(this.server.getMBeanServer(),
					coreManager,
					MBeanHelper.getPlatformMBeanName("core-manager"));
			Console.echo("");
			Console.echo("--------------------------------------------------------------------------------");
			Console.echo("Sesame server (" + this.server.getName() + ") started");
			Console.echo("--------------------------------------------------------------------------------");
		} catch (Exception e) {
			LOG.error("Start server err", e);
		}
	}

	private void stop() {
		Console.echo("");
		Console.echo("--------------------------------------------------------------------------------");
		Console.echo("Shuting down " + this.server.getName() + "...");
		Console.echo("--------------------------------------------------------------------------------");
		this.server.shutdown();
		try {
			MBeanServer mbeanServer = this.server.getMBeanServer();
			ObjectName objectName = new ObjectName(getEnv().getDomain() + ":*");
			Set<ObjectName> set = mbeanServer.queryNames(objectName, null);
			for (ObjectName name : set) {
				try {
					if (mbeanServer.isRegistered(name))
						mbeanServer.unregisterMBean(name);
				} catch (Throwable t) {
					LOG.debug(name.toString());
					if (LOG.isDebugEnabled())
						LOG.debug(t.getMessage(), t);
				}
			}
			LOG.debug("MBeans cleaned");

			this.connectorServer.stop();
		} catch (MalformedObjectNameException localMalformedObjectNameException) {
		} catch (IOException e) {
			if ((e.getCause() != null)
					&& (e.getCause() instanceof ContextNotEmptyException)) {
				try {
					InitialContext context = getNamingContext();
					NamingEnumeration enumer = context.list("");
					while (enumer.hasMore()) {
						NameClassPair pair = (NameClassPair) enumer.next();
						context.unbind(pair.getName());
					}
				} catch (NamingException ex) {
					LOG.debug(ex.getMessage(), ex);
				}
			} else {
				LOG.error(e.getMessage(), e);
			}
		}
		
		Console.echo("--------------------------------------------------------------------------------");
		Console.echo("Sesame server (" + this.server.getName() + ") shutdown complete");
		Console.echo("--------------------------------------------------------------------------------");
	}

	private void addShutdownHook() {
		this.shutdownHook = new Thread("Sesame-Platform-ShutdownHook") {
			public void run() {
				Platform.this.stop();
			}
		};
		this.shutdownHook.setDaemon(true);

		Runtime.getRuntime().addShutdownHook(this.shutdownHook);
	}

	private void addSystemClusterListener() {
	}

	void startJMXServer() {
		try {
			LOG.info("create mbean server on server ["
					+ this.env.getServerName() + "]");
			this.mbeanServer = MBeanServerFactory.createMBeanServer(this.env
					.getDomain());
			JMXServiceURL serviceURL = this.jmxServiceURLBuilder
					.getLocalJMXServiceURL();
			this.connectorServer = JMXConnectorServerFactory
					.newJMXConnectorServer(serviceURL, null, this.mbeanServer);
			this.connectorServer.start();
			LOG.info("jmx connector server started @" + serviceURL.toString());
		} catch (IOException e) {
			if (LOG.isTraceEnabled()) {
				LOG.trace(e.getMessage(), e);
			} else
				LOG.error(e.getMessage());
		}
	}

	public static Env getEnv() {
		return instance.env;
	}

	public static MBeanServer getLocalMBeanServer() {
		return instance.mbeanServer;
	}

	public static void startup() {
		instance.start();
	}

	public static void shutdown() {
		instance.stop();
	}

	public <T> T getAdminMBean(Class<T> clazz, ObjectName name) {
		try {
			JMXServiceURLBuilder builder = this.jmxServiceURLBuilder;
			JMXServiceURL adminServerURL = builder.getAdminJMXServiceURL();
			Map environment = new HashMap();
			environment.put("java.naming.factory.initial",
					"com.sun.jndi.rmi.registry.RegistryContextFactory");
			JMXConnector adminServerConnector = JMXConnectorFactory.connect(
					adminServerURL, environment);
			MBeanServerConnection adminServerCon = adminServerConnector
					.getMBeanServerConnection();

			return MBeanServerInvocationHandler.newProxyInstance(
					adminServerCon, name, clazz, false);
		} catch (Exception e) {
		}
		return null;
	}

	public static InitialContext getNamingContext() {
		if (instance.namingContext == null) {
			throw new RuntimeException("Naming context initialized failure");
		}
		return instance.namingContext;
	}

	public String toString() {
		return this.env.toString();
	}
}