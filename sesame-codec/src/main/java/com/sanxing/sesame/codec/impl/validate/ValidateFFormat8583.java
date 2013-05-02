package com.sanxing.sesame.codec.impl.validate;

import com.sanxing.sesame.binding.codec.FormatException;

public class ValidateFFormat8583 {
	private int len;
	private char blank = ' ';
	private String align;
	private String endian;
	private int compress = 0;
	private int id = 0;

	public ValidateFFormat8583(org.jdom2.Element element, org.jdom2.Element format)
			throws FormatException {
		String type = element.getAttributeValue("type");
		this.id = Integer.parseInt(format.getAttributeValue("id"));
		if ("string".equals(type)) {
			String lenStr = format.getAttributeValue("length");
			this.len = ValidateField.validateLength(
					element.getAttributeValue("name"), lenStr);

			this.align = format.getAttributeValue("align", "L");
			this.align = ValidateField.validateAlign(
					element.getAttributeValue("name"), this.align);

			String blankStr = format.getAttributeValue("blank");
			this.blank = ValidateField.validateBlank(
					element.getAttributeValue("name"), this.blank, blankStr);

			String compressStr = format.getAttributeValue("compress");
			this.compress = ValidateField.validateCompress(
					element.getAttributeValue("name"), compressStr);
		} else if ("int".equals(type)) {
			this.endian = format.getAttributeValue("endian", "big");
			this.endian = ValidateField.validateEndian(
					element.getAttributeValue("name"), this.endian);
		} else if ("hexBinary".equals(type)) {
			String lenStr = format.getAttributeValue("length");
			this.len = ValidateField.validateLength(
					element.getAttributeValue("name"), lenStr);
		} else {
			throw new FormatException("element:["
					+ element.getAttributeValue("name")
					+ "],attribute type is:[" + type + "] error!");
		}
	}

	public ValidateFFormat8583(String elementName, org.jdom2.Element format,
			String type) throws FormatException {
		this.id = Integer.parseInt(format.getAttributeValue("id"));
		if ("string".equals(type)) {
			String lenStr = format.getAttributeValue("length");
			this.len = ValidateField.validateLength(elementName, lenStr);

			this.align = format.getAttributeValue("align", "L");
			this.align = ValidateField.validateAlign(elementName, this.align);

			String blankStr = format.getAttributeValue("blank");
			this.blank = ValidateField.validateBlank(elementName, this.blank,
					blankStr);

			String compressStr = format.getAttributeValue("compress");
			this.compress = ValidateField.validateCompress(elementName,
					compressStr);
		} else if ("int".equals(type)) {
			this.endian = format.getAttributeValue("endian", "big");
			this.endian = ValidateField
					.validateEndian(elementName, this.endian);
		} else if ("hexBinary".equals(type)) {
			String lenStr = format.getAttributeValue("length");
			this.len = ValidateField.validateLength(elementName, lenStr);
		} else {
			throw new FormatException("element:[" + elementName
					+ "],attribute type is:[" + type + "] error!");
		}
	}

	public ValidateFFormat8583(String elementType, String elementName,
			org.w3c.dom.Element format) throws FormatException {
		this.id = Integer.parseInt(format.getAttribute("id"));
		if ("string".equals(elementType)) {
			String lenStr = format.getAttribute("length");
			this.len = ValidateField.validateLength(elementName, lenStr);

			this.align = format.getAttribute("align");
			this.align = ValidateField.validateAlign(elementName, this.align);

			String blankStr = format.getAttribute("blank");
			this.blank = ValidateField.validateBlank(elementName, this.blank,
					blankStr);

			String compressStr = format.getAttribute("compress");
			this.compress = ValidateField.validateCompress(elementName,
					compressStr);
		} else if ("int".equals(elementType)) {
			this.endian = format.getAttribute("endian");
			this.endian = ValidateField
					.validateEndian(elementName, this.endian);
		} else if ("hexBinary".equals(elementType)) {
			String lenStr = format.getAttribute("length");
			this.len = ValidateField.validateLength(elementName, lenStr);
		} else {
			throw new FormatException("element:[" + elementName
					+ "],attribute type is:[" + elementType + "] error!");
		}
	}

	public String getAlign() {
		return this.align;
	}

	public char getBlank() {
		return this.blank;
	}

	public int getCompress() {
		return this.compress;
	}

	public String getEndian() {
		return this.endian;
	}

	public int getLen() {
		return this.len;
	}

	public int getId() {
		return this.id;
	}
}