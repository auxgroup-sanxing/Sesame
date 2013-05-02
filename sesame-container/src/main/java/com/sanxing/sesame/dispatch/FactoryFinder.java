package com.sanxing.sesame.dispatch;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class FactoryFinder {
	private final String path;
	private final Map<String, Class<?>> classMap = new ConcurrentHashMap();

	public FactoryFinder(String path) {
		this.path = path;
	}

	public Object newInstance(String key) throws IllegalAccessException,
			InstantiationException, IOException, ClassNotFoundException {
		return newInstance(key, null);
	}

	public Object newInstance(String key, String propertyPrefix)
			throws IllegalAccessException, InstantiationException, IOException,
			ClassNotFoundException {
		if (propertyPrefix == null) {
			propertyPrefix = "";
		}
		Class clazz = (Class) this.classMap.get(propertyPrefix + key);
		if (clazz == null) {
			clazz = loadClass(doFindFactoryProperies(key), propertyPrefix);
			this.classMap.put(propertyPrefix + key, clazz);
		}
		return clazz.newInstance();
	}

	private Class loadClass(Properties properties, String propertyPrefix)
			throws ClassNotFoundException, IOException {
		String className = properties.getProperty(propertyPrefix + "class");
		if (className == null)
			throw new IOException("Expected property is missing: "
					+ propertyPrefix + "class");
		Class clazz;
		try {
			clazz = Thread.currentThread().getContextClassLoader()
					.loadClass(className);
		} catch (ClassNotFoundException e) {
			clazz = FactoryFinder.class.getClassLoader().loadClass(className);
		}

		return clazz;
	}

	private Properties doFindFactoryProperies(String key) throws IOException {
		String uri = this.path + key;

		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(uri);
		if (in == null) {
			in = FactoryFinder.class.getClassLoader().getResourceAsStream(uri);
			if (in == null) {
				throw new IOException(
						"Could not find factory class for resource: " + uri);
			}

		}

		BufferedInputStream reader = null;
		try {
			reader = new BufferedInputStream(in);
			Properties properties = new Properties();
			properties.load(reader);
			Properties localProperties1 = properties;

			return localProperties1;
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
	}
}