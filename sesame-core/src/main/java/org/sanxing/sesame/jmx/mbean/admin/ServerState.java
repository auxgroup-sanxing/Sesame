package org.sanxing.sesame.jmx.mbean.admin;

public abstract interface ServerState {
	public static final int STARTING = 1;
	public static final int RUNNING = 2;
	public static final int SHUTTINGDOWN = 3;
	public static final int SHUTDOWN = 0;
}