package com.sanxing.sesame.core.api;

import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.Env;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServer;
import javax.naming.InitialContext;

public class ContainerContext {
	private static Map<String, ContainerContext> instances = new HashMap();
	private MBeanServer mbeanServer;
	private Map<Object, Object> context = new HashMap();
	private Env env;
	private InitialContext serverContext;
	private String containerName;
	private ClassLoader containerCLassLoader = null;
	private BaseServer server;

	public static ContainerContext getInstance(String containerName) {
		ContainerContext answer = (ContainerContext) instances
				.get(containerName);
		if (answer == null) {
			throw new RuntimeException("unkown container");
		}
		return answer;
	}

	public ContainerContext(String containerName, Env env,
			InitialContext serverContext, MBeanServer mbeanServer,
			BaseServer _server) {
		this.containerName = containerName;
		this.env = env;
		this.serverContext = serverContext;
		instances.put(containerName, this);
		this.mbeanServer = mbeanServer;
		this.server = _server;

		JarFileClassLoader classLoader = new JarFileClassLoader(new URL[0],
				Thread.currentThread().getContextClassLoader(), false,
				new String[0], new String[] { "java.", "javax." });

		File libDir = new File(getContainerDir(), "lib");
		File classesDir = new File(getContainerDir(), "classes");

		classLoader.addJarDir(libDir);
		classLoader.addClassesDir(classesDir);
		this.containerCLassLoader = classLoader;
	}

	public void put(Object key, Object value) {
		this.context.put(key, value);
	}

	public Object get(Object key) {
		return this.context.get(key);
	}

	public Env getEnv() {
		return this.env;
	}

	public MBeanServer getMbeanServer() {
		return this.mbeanServer;
	}

	public InitialContext getServerJNDIContext() {
		return this.serverContext;
	}

	public File getContainerDir() {
		File file = new File(this.env.getHomeDir(), "work/containers/"
				+ this.containerName);
		if (!(file.exists())) {
			file.mkdirs();
			new File(file, "lib").mkdir();
			new File(file, "classes").mkdir();
		}
		return file;
	}

	public ClassLoader getContainerClassLoader() {
		return this.containerCLassLoader;
	}

	public String getContainerName() {
		return this.containerName;
	}

	public BaseServer getServer() {
		return this.server;
	}
}