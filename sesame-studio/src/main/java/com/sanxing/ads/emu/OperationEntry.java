package com.sanxing.ads.emu;

import org.apache.ws.commons.schema.XmlSchema;
import org.jdom.Document;

public class OperationEntry {
	private XmlSchema schema;
	private Document data;
	private String interfaceName;

	public Document getData() {
		return this.data;
	}

	public void setData(Document data) {
		this.data = data;
	}

	public void setSchema(XmlSchema schema) {
		this.schema = schema;
	}

	public XmlSchema getSchema() {
		return this.schema;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getInterfaceName() {
		return this.interfaceName;
	}
}