package org.sanxing.sesame.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;

public class SystemEnv {
	public static Properties getProperties() {
		Properties props = new Properties();
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = null;

			String osName = System.getProperty("os.name").toLowerCase();

			if (osName.indexOf("windows 9") > -1) {
				process = runtime.exec("command.com /c set");
			} else if ((osName.indexOf("nt") > -1)
					|| (osName.indexOf("windows 2") > -1)
					|| (osName.indexOf("windows xp") > -1)) {
				process = runtime.exec("cmd.exe /c set");
			} else {
				process = runtime.exec("env");
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				int pos = line.indexOf("=");

				if (pos != -1) {
					String key = line.substring(0, pos);
					String value = line.substring(pos + 1);

					props.setProperty(key, value);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return props;
	}

	public static void setProperties(Properties props) {
		Properties envProps = getProperties();

		Enumeration enu = envProps.propertyNames();

		while (enu.hasMoreElements()) {
			String key = (String) enu.nextElement();

			props.setProperty("env." + key, (String) envProps.get(key));
		}
	}
}