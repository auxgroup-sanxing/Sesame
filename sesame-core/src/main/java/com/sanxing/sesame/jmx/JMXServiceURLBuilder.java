/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.jmx;

import javax.management.remote.JMXServiceURL;

/**
 * @author ShangjieZhou
 */
public abstract interface JMXServiceURLBuilder {
	
	public abstract JMXServiceURL getLocalJMXServiceURL();

	public abstract JMXServiceURL getAdminJMXServiceURL();

	public abstract JMXServiceURL getJMXServiceURLByServerName(
			String paramString);
}