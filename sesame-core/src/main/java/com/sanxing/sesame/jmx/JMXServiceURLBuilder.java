package com.sanxing.sesame.jmx;

import javax.management.remote.JMXServiceURL;

public abstract interface JMXServiceURLBuilder {
	public abstract JMXServiceURL getLocalJMXServiceURL();

	public abstract JMXServiceURL getAdminJMXServiceURL();

	public abstract JMXServiceURL getJMXServiceURLByServerName(
			String paramString);
}