package com.sanxing.sesame.jmx.mbean.admin;

import java.io.Serializable;
import org.jdom2.Element;

public class ContainerInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String containerClazz;
	private Element cotnainerParams;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContainerClazz() {
		return this.containerClazz;
	}

	public void setContainerClazz(String containerClazz) {
		this.containerClazz = containerClazz;
	}

	public Element getCotnainerParams() {
		return this.cotnainerParams;
	}

	public void setCotnainerParams(Element cotnainerParams) {
		this.cotnainerParams = cotnainerParams;
	}
}