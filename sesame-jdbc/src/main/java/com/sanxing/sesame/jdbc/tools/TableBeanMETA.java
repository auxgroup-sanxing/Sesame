package com.sanxing.sesame.jdbc.tools;

import java.util.LinkedList;
import java.util.List;

public class TableBeanMETA {
	private String clazzName;
	private String tableName;
	private List<ColumnFieldMETA> fields = new LinkedList();

	public String getClazzName() {
		return this.clazzName;
	}

	public void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void addField(ColumnFieldMETA field) {
		this.fields.add(field);
	}

	public List<ColumnFieldMETA> getFields() {
		return this.fields;
	}

	public String toString() {
		String temp = "TableBeanMETA [clazzName=" + this.clazzName
				+ " tableName=" + this.tableName + "]\n";
		temp = temp + "===================fields===============\n";
		for (ColumnFieldMETA field : this.fields) {
			temp = temp + field.toString() + "\n";
		}
		temp = temp + "===================fields===============\n";
		return temp;
	}
}