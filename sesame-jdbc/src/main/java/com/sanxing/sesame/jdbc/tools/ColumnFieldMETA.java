package com.sanxing.sesame.jdbc.tools;

public class ColumnFieldMETA {
	private String fieldName;
	private String columnName;
	private String columnType;
	private int length;
	private String desc;
	private boolean pk;

	public boolean isPk() {
		return this.pk;
	}

	public void setPk(boolean pk) {
		this.pk = pk;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getColumnName() {
		return this.columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getColumnType() {
		return this.columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String toString() {
		return "FieldMETA [columnName=" + this.columnName + ", columnType="
				+ this.columnType + ", desc=" + this.desc + ", fieldName="
				+ this.fieldName + ", length=" + this.length + "]";
	}
}