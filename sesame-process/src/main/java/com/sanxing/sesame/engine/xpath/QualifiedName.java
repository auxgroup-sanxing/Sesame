package com.sanxing.sesame.engine.xpath;

import java.io.Serializable;

public class QualifiedName implements Serializable {
	private static final long serialVersionUID = 2734958615642751535L;
	private String namespaceURI;
	private String localName;

	public QualifiedName(String namespaceURI, String localName) {
		if (namespaceURI == null)
			namespaceURI = "";
		this.namespaceURI = namespaceURI;
		this.localName = localName;
	}

	public int hashCode() {
		return (this.localName.hashCode() ^ this.namespaceURI.hashCode());
	}

	public boolean equals(Object o) {
		if(o == null) return false;
		
		QualifiedName other = (QualifiedName) o;

		return ((this.namespaceURI.equals(other.namespaceURI)) && (this.localName
				.equals(other.localName)));
	}

	public String getClarkForm() {
		if ("".equals(this.namespaceURI))
			return this.localName;
		return "{" + this.namespaceURI + "}" + ":" + this.localName;
	}
}