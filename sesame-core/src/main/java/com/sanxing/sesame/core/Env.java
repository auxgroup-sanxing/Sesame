package com.sanxing.sesame.core;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Env {
	public static final String MODE = "mode";
	public static final String ADMIN_HOST = "admin-host";
	public static final String ADMIN_PORT = "admin-port";
	public static final String SERVER_NAME = "server-name";
	private String mode;
	private String serverName;
	private String adminHost;
	private int adminPort;
	private boolean admin;
	private File serverDir;
	private String domain = "com.sanxing.sesame";

	private boolean clustered = false;

	public Env() {
		try {
			String strServerDir = System.getProperty("SESAME_HOME");
			if (strServerDir == null) {
				strServerDir = new File(System.getProperty("user.dir"))
						.getParent();
			}
			this.serverDir = new File(strServerDir);
			this.serverDir.mkdirs();

			this.mode = System.getProperty("mode", "dev");

			this.adminHost = System.getProperty("admin-host");

			InetAddress.getByName(this.adminHost);

			this.adminPort = Integer.parseInt(System.getProperty("admin-port",
					"2099"));
			this.serverName = System.getProperty("server-name", "admin");
			this.admin = (("admin".equalsIgnoreCase(getServerName())) || ("dev"
					.equalsIgnoreCase(getMode())));
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
		return (!(getMode().equalsIgnoreCase("dev")));
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

	public String toString() {
		return "Env [admin=" + this.admin + ", adminHost=" + this.adminHost
				+ ", adminPort=" + this.adminPort + ", mode=" + this.mode
				+ ", serverDir=" + this.serverDir + ", serverName="
				+ this.serverName + "]";
	}
}