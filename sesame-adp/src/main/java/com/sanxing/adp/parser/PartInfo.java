package com.sanxing.adp.parser;

import javax.xml.namespace.QName;

public class PartInfo {
	public static final int IN = 0;
	public static final int OUT = 1;
	public static final int FAULT = 2;
	private int type;
	private String name;
	private QName elementName;
	private String javaType;
	private String xsType;

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return this.type;
	}

	public String getXsType() {
		return this.xsType;
	}

	public void setXsType(String xsType) {
		this.xsType = xsType;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public QName getElementName() {
		return this.elementName;
	}

	public void setElementName(QName elementName) {
		this.elementName = elementName;
	}

	public String getJavaType() {
		return this.javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public String toString() {
		return "PartInfo [elementName=" + this.elementName + ", javaType="
				+ this.javaType + ", name=" + this.name + ", type=" + this.type
				+ "]";
	}
}