package com.sanxing.sesame.component.params;

import org.jdom.Element;

public class Parameter {
	public static int PARAM_TYPE_INT = 0;

	public static int PARAM_TYPE_BOOLEAN = 1;

	public static int PARAM_TYPE_STRING = 2;

	public static int PARAM_TYPE_DOUBLE = 3;
	private String name;
	private String value;
	private PARAMTYPE type = PARAMTYPE.PARAM_TYPE_STRING;
	private String comment;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return this.value;
	}

	public Object getTypedValue() {
		try {
			if (this.type.equals(PARAMTYPE.PARAM_TYPE_INT))
				return Integer.valueOf(Integer.parseInt(this.value));
			if (this.type.equals(PARAMTYPE.PARAM_TYPE_BOOLEAN))
				return Boolean.valueOf(Boolean.parseBoolean(this.value));
			if (this.type.equals(PARAMTYPE.PARAM_TYPE_DOUBLE)) {
				return Double.valueOf(Double.parseDouble(this.value));
			}
			return this.value;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void setValue(String value) {
		try {
			if (this.type.equals(PARAMTYPE.PARAM_TYPE_INT)) {
				Integer.parseInt(value);
			}
			if (this.type.equals(PARAMTYPE.PARAM_TYPE_BOOLEAN)) {
				Boolean.parseBoolean(value);
			}
			if (this.type.equals(PARAMTYPE.PARAM_TYPE_DOUBLE))
				Double.parseDouble(value);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw e;
		}

		this.value = value;
	}

	public PARAMTYPE getType() {
		return this.type;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public static Parameter newIntParameter() {
		Parameter param = new Parameter();
		param.type = PARAMTYPE.PARAM_TYPE_INT;
		return param;
	}

	public static Parameter newBooleanParameter() {
		Parameter param = new Parameter();
		param.type = PARAMTYPE.PARAM_TYPE_BOOLEAN;
		return param;
	}

	public static Parameter newStringParameter() {
		Parameter param = new Parameter();
		param.type = PARAMTYPE.PARAM_TYPE_STRING;
		return param;
	}

	public static Parameter newDoubleParameter() {
		Parameter param = new Parameter();
		param.type = PARAMTYPE.PARAM_TYPE_DOUBLE;
		return param;
	}

	public String toString() {
		return "Parameter [name=" + this.name + ", value=" + this.value
				+ ", type=" + this.type + ", comment=" + this.comment + "]";
	}

	public Element toElement() {
		Element element = new Element("param");
		element.setAttribute("name", this.name);
		element.setAttribute("type", this.type.name());
		element.setAttribute("comment", this.comment);
		element.setText(this.value);
		return element;
	}

	public static Parameter newParameter(PARAMTYPE type) {
		Parameter param = null;
		if (type.equals(PARAMTYPE.PARAM_TYPE_INT))
			param = newIntParameter();
		else if (type.equals(PARAMTYPE.PARAM_TYPE_DOUBLE))
			param = newDoubleParameter();
		else if (type.equals(PARAMTYPE.PARAM_TYPE_BOOLEAN))
			param = newBooleanParameter();
		else if (type.equals(PARAMTYPE.PARAM_TYPE_STRING)) {
			param = newStringParameter();
		}

		return param;
	}

	public static Parameter fromElement(Element element) {
		Parameter param = null;
		if (element.getAttributeValue("type").equals(
				PARAMTYPE.PARAM_TYPE_INT.name()))
			param = newIntParameter();
		else if (element.getAttributeValue("type").equals(
				PARAMTYPE.PARAM_TYPE_DOUBLE.name()))
			param = newDoubleParameter();
		else if (element.getAttributeValue("type").equals(
				PARAMTYPE.PARAM_TYPE_BOOLEAN.name()))
			param = newBooleanParameter();
		else if (element.getAttributeValue("type").equals(
				PARAMTYPE.PARAM_TYPE_STRING.name()))
			param = newStringParameter();
		else {
			throw new RuntimeException("unkown param type");
		}
		param.setComment(element.getAttributeValue("comment"));
		param.setName(element.getAttributeValue("name"));
		param.setValue(element.getText());
		return param;
	}

	public static enum PARAMTYPE {
		PARAM_TYPE_INT, PARAM_TYPE_BOOLEAN, PARAM_TYPE_STRING, PARAM_TYPE_DOUBLE;
	}
}