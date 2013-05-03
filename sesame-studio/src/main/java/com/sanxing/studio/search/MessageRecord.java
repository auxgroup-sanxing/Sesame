package com.sanxing.studio.search;

import javax.wsdl.Message;
import javax.xml.namespace.QName;

public class MessageRecord extends Record implements SearcherName {
	private Message message;

	public MessageRecord(Message message) {
		this.message = message;
		addField(new Column("message name", message.getQName().toString(), true));
	}

	public MessageRecord() {
	}

	public String getSearcherName() {
		return "message";
	}

	public Column getDescriptionColumn() {
		return null;
	}

	public Column getNameColumn() {
		return getColumnByName("message name");
	}
}