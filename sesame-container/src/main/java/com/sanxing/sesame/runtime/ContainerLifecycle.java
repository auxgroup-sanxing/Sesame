package com.sanxing.sesame.runtime;

public abstract interface ContainerLifecycle {
	public abstract void onStartup();

	public abstract void onShutdown();
}