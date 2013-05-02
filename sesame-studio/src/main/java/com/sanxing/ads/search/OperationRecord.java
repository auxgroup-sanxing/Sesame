package com.sanxing.ads.search;

import javax.wsdl.Operation;
import org.w3c.dom.Element;

public class OperationRecord extends Record implements SearcherName {
	private Operation operation;

	public OperationRecord(Operation operation) {
		this.operation = operation;
		if (operation.getDocumentationElement() != null) {
			addField(new Column("operation description", operation
					.getDocumentationElement().getTextContent(), true));
		}

		addField(new Column("operation name", operation.getName(), true));
	}

	public OperationRecord() {
	}

	public String getSearcherName() {
		return "operation";
	}

	public Column getDescriptionColumn() {
		return getColumnByName("operation description");
	}

	public Column getNameColumn() {
		return getColumnByName("operation name");
	}
}