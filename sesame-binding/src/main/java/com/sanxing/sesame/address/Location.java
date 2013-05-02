package com.sanxing.sesame.address;

import java.net.URI;
import org.w3c.dom.Element;

public class Location {
	public static final String LOCAL = "local";
	public static final String REMOTE = "remote";
	private URI uri;
	private Element config;
	private String style;

	public Location(URI uri, Element config, String style) {
		this.uri = uri;
		this.config = config;
		this.style = style;
	}

	public URI getURI() {
		return this.uri;
	}

	public Element getConfig() {
		return this.config;
	}

	public void setConfig(Element config) {
		this.config = config;
	}

	public String getStyle() {
		return this.style;
	}
}