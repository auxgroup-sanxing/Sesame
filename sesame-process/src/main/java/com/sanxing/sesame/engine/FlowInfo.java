package com.sanxing.sesame.engine;

import org.jaxen.NamespaceContext;
import org.jaxen.SimpleNamespaceContext;
import org.jdom2.Element;

public class FlowInfo {
	private SimpleNamespaceContext namespaceCtx = new SimpleNamespaceContext();
	private String name;
	private String description;
	private String author;
	private Element flowDefination;

	public void addNSMapping(String URI, String prefix) {
		this.namespaceCtx.addNamespace(prefix, URI);
	}

	public NamespaceContext getNamespaceContext() {
		return this.namespaceCtx;
	}

	public Element getFlowDefination() {
		return this.flowDefination;
	}

	public void setFlowDefination(Element flowDefination) {
		this.flowDefination = flowDefination;
		if (!(validate()))
			throw new RuntimeException("invalid flow defination");
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	private boolean validate() {
		return true;
	}
}