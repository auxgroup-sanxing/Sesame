package org.sanxing.sesame.core.api;

public abstract interface Container {
	public abstract void init(ContainerContext paramContainerContext)
			throws Exception;

	public abstract void start() throws Exception;

	public abstract void stop() throws Exception;

	public abstract void shutdown() throws Exception;
}