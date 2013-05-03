package com.sanxing.studio.search;

import javax.wsdl.Service;
import javax.xml.namespace.QName;

public class ServiceRecord extends Record implements SearcherName {
	private Service service;
	private String name;
	private String description;

	public ServiceRecord(Service service, String description) {
		this.service = service;
		this.description = description;
		addField(new Column("service name", service.getQName().toString(), true));
		addField(new Column("service description", description, true));
	}

	public ServiceRecord(String name, String description) {
		this.name = name;
		this.description = description;
		addField(new Column("service name", name, true));
		addField(new Column("service description", description, true));
	}

	public ServiceRecord() {
	}

	public String getSearcherName() {
		return "service";
	}

	public Column getDescriptionColumn() {
		return getColumnByName("service description");
	}

	public Column getNameColumn() {
		return getColumnByName("service name");
	}
}