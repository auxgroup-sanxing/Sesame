/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.core;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author ShangjieZhou
 */
public class Environment {
	
	public static final String SESAME_HOME = "SESAME_HOME";
	public static final String USER_DIR = "user.dir";
	public static final String MODE = "mode";
	public static final String PRODUCT = "pro";
	public static final String DEVELOP = "dev";
	public static final String ADMIN = "admin";
	public static final String ADMIN_HOST = "admin-host";
	public static final String ADMIN_PORT = "admin-port";
	public static final String DEFAULT_PORT = "2099";
	public static final String SERVER_NAME = "server-name";
	
	private String mode;
	private String serverName;
	private String adminHost;
	private int adminPort;
	private boolean admin;
	private File serverDir;
	private String domain = "org.sanxing.sesame";

	private boolean clustered = false;

	public Environment() {
		try {
			String strServerDir = System.getProperty(SESAME_HOME);
			if (strServerDir == null) {
				strServerDir = new File(System.getProperty(USER_DIR)).getParent();
			}
			this.serverDir = new File(strServerDir);
			this.serverDir.mkdirs();

			this.mode = System.getProperty(MODE, DEVELOP);

			this.adminHost = System.getProperty(ADMIN_HOST);

			InetAddress.getByName(this.adminHost);

			this.adminPort = Integer.parseInt(System.getProperty(ADMIN_PORT, DEFAULT_PORT));
			this.serverName = System.getProperty(SERVER_NAME, ADMIN);
			this.admin = ADMIN.equalsIgnoreCase(getServerName()) || DEVELOP.equalsIgnoreCase(getMode());
		} catch (UnknownHostException e) {
			throw new RuntimeException("Unkown admin host :" + this.adminHost);
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("Unkown admin port :" + this.adminPort);
		}
	}

	public File getHomeDir() {
		return this.serverDir;
	}

	public File getLogDir() {
		return new File(this.serverDir, "logs");
	}

	public boolean isAdmin() {
		return this.admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isClustered() {
		return this.clustered;
	}

	protected void setClustered(boolean value) {
		this.clustered = value;
	}

	public int getAdminPort() {
		return this.adminPort;
	}

	public void setAdminPort(int adminPort) {
		this.adminPort = adminPort;
	}

	public String getMode() {
		return this.mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public boolean isProduction() {
		return (!(DEVELOP.equalsIgnoreCase(getMode())));
	}

	public String getDomain() {
		return this.domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getServerName() {
		return this.serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getAdminHost() {
		return this.adminHost;
	}

	public void setAdminHost(String adminHost) {
		this.adminHost = adminHost;
	}
}
