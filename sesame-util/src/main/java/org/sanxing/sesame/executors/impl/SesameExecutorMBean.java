package org.sanxing.sesame.executors.impl;

public abstract interface SesameExecutorMBean {
	public abstract int getActiveCount();

	public abstract int getLargestPoolSize();

	public abstract long getTaskCount();

	public abstract long getCompletedTaskCount();

	public abstract int getPoolSize();

	public abstract int getCorePoolSize();

	public abstract long averageWorkTime();

	public abstract long lastWorkTime();

	public abstract String getID();

	public abstract void turnOnMonitor();

	public abstract void shutdownMonitor();

	public abstract boolean isMonitorOn();
}