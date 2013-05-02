package com.sanxing.sesame.deployment;

public class Services {
	private boolean bindingComponent;
	private Provides[] provides;
	private Consumes[] consumes;

	public boolean isBindingComponent() {
		return this.bindingComponent;
	}

	public void setBindingComponent(boolean bindingComponent) {
		this.bindingComponent = bindingComponent;
	}

	public Provides[] getProvides() {
		return this.provides;
	}

	public void setProvides(Provides[] provides) {
		this.provides = provides;
	}

	public Consumes[] getConsumes() {
		return this.consumes;
	}

	public void setConsumes(Consumes[] consumes) {
		this.consumes = consumes;
	}
}