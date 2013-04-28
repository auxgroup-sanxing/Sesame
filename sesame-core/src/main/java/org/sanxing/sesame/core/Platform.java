/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.core;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.naming.InitialContext;

import org.sanxing.sesame.jmx.JMXServiceURLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sesame Platform
 * 
 * @author ShangjieZhou
 */
public class Platform {
	
    private final Logger logger = LoggerFactory.getLogger( getClass() );
    
    private Environment env = new Environment();
	private MBeanServer mbeanServer;
	private JMXConnectorServer connectorServer;
	private JMXServiceURLBuilder jmxServiceURLBuilder;
	private BaseServer server;
	private InitialContext namingContext;
	private static final Platform instance = new Platform();

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
}
