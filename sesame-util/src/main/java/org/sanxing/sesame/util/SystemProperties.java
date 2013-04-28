package org.sanxing.sesame.util;

import org.sanxing.sesame.exceptions.ErrMessages;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class SystemProperties {
	private static SystemProperties _instance = new SystemProperties();
	public static final String TMP_DIR = "java.io.tmpdir";
	private Map<String, String> _props = new ConcurrentHashMap();

	public static String get(String key, String defaultValue) {
		return GetterUtil.get(get(key), defaultValue);
	}

	public static String get(String key) {
		String value = (String) _instance._props.get(key);

		if (value == null) {
			value = System.getProperty(key);
		}

		return value;
	}

	public static String[] getArray(String key) {
		String value = get(key);

		if (value == null) {
			return new String[0];
		}
		return StringUtil.split(value);
	}

	private SystemProperties() {
		Properties sys = System.getProperties();
		Enumeration keys = sys.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			this._props.put(key, sys.getProperty(key));
		}
		String defaultFileName = System.getProperty(
				"sesame.application.properties",
				"/conf/application.properties");
		parsePropertyFile(defaultFileName);

		String appFiles = GetterUtil
				.get((String) this._props
						.get("sesame.application.ext.properties"), "");

		String[] fileNames = appFiles.split(",");
		for (String file : fileNames)
			parsePropertyFile(file);
	}

	private void parsePropertyFile(String fileName) {
		System.out.println("parsing property file [" + fileName + "]");
		try {
			InputStream input;
			if (fileName.startsWith("$classpath")) {
				fileName = fileName.substring(11);
				input = ErrMessages.class.getClassLoader().getResourceAsStream(
						fileName);
			} else {
				input = new FileInputStream(new File(
						System.getProperty("SESAME_HOME"), fileName));
			}

			Properties properites = new Properties();

			properites.load(input);
			Enumeration enumer = properites.propertyNames();

			while (enumer.hasMoreElements()) {
				String key = (String) enumer.nextElement();
				String value = properites.getProperty(key);
				if (System.getProperty("sesame.property.overide.sequence",
						"last").equals("last")) {
					this._props.put(key, value);
				} else if (!(this._props.containsKey(key)))
					this._props.put(key, value);
			}
		} catch (Exception e) {
			System.out.println("Load app propertie failed! [" + fileName + "]");
		}
	}
}