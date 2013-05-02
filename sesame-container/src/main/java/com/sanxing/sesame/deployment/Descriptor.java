package com.sanxing.sesame.deployment;

public class Descriptor {
	private double version;
	private Component component;
	private SharedLibrary sharedLibrary;
	private ServiceAssembly serviceAssembly;
	private Services services;

	public double getVersion() {
		return this.version;
	}

	public void setVersion(double version) {
		this.version = version;
	}

	public Component getComponent() {
		return this.component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public SharedLibrary getSharedLibrary() {
		return this.sharedLibrary;
	}

	public void setSharedLibrary(SharedLibrary sharedLibrary) {
		this.sharedLibrary = sharedLibrary;
	}

	public ServiceAssembly getServiceAssembly() {
		return this.serviceAssembly;
	}

	public void setServiceAssembly(ServiceAssembly serviceAssembly) {
		this.serviceAssembly = serviceAssembly;
	}

	public Services getServices() {
		return this.services;
	}

	public void setServices(Services services) {
		this.services = services;
	}
}