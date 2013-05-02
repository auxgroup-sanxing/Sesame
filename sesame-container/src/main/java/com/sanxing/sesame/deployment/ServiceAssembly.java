package com.sanxing.sesame.deployment;

public class ServiceAssembly {
	private Connections connections;
	private Identification identification;
	private ServiceUnit[] serviceUnits;
	private String state;

	public ServiceAssembly() {
		this.connections = new Connections();

		this.state = "";
	}

	public Connections getConnections() {
		return this.connections;
	}

	public Identification getIdentification() {
		return this.identification;
	}

	public ServiceUnit[] getServiceUnits() {
		return this.serviceUnits;
	}

	public String getState() {
		return this.state;
	}

	public void setConnections(Connections connections) {
		this.connections = connections;
	}

	public void setIdentification(Identification identification) {
		this.identification = identification;
	}

	public void setServiceUnits(ServiceUnit[] serviceUnits) {
		this.serviceUnits = serviceUnits;
	}

	public void setState(String state) {
		this.state = state;
	}
}