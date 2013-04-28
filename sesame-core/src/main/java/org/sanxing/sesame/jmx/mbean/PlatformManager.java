package org.sanxing.sesame.jmx.mbean;

import org.sanxing.sesame.core.Platform;

public class PlatformManager implements PlatformManagerMBean {
	public void shutdown() {
		Platform.shutdown();
	}

	public String getArch() {
		return System.getProperty("os.arch");
	}

	public String getOperatingSystem() {
		return System.getProperty("os.name") + " Version "
				+ System.getProperty("os.version");
	}

	public String getJVM() {
		return System.getProperty("java.vendor") + " "
				+ System.getProperty("java.version");
	}
}