package com.sanxing.sesame.codec.impl.validate;

import com.sanxing.sesame.binding.codec.FormatException;
import org.jdom2.Namespace;
import org.w3c.dom.NodeList;

public class ValidateVFormat8583 {
	private int headLen;
	private char headBlank = ' ';
	private String headAlign;
	private int headCompress = 0;
	private int compress = 0;
	private int headRadix = 10;
	private int id = 0;

	public ValidateVFormat8583(org.jdom2.Element element,
			org.jdom2.Element format, Namespace xsd) throws FormatException {
		String elementName = element.getAttributeValue("name");

		this.id = Integer.parseInt(format.getAttributeValue("id"));
		org.jdom2.Element head = format.getChild("head", xsd);
		if (head == null)
			throw new FormatException("element:[" + elementName
					+ "],element format,do not define child element[head]!");

		String headLenStr = head.getAttributeValue("length");
		this.headLen = ValidateField.validateLength(elementName, headLenStr);

		String headCompressStr = head.getAttributeValue("compress");
		this.headCompress = ValidateField.validateCompress(elementName,
				headCompressStr);

		String blankStr = head.getAttributeValue("blank");
		this.headBlank = ValidateField.validateBlank(elementName,
				this.headBlank, blankStr);

		this.headAlign = head.getAttributeValue("align");
		this.headAlign = ValidateField.validateAlign(elementName,
				this.headAlign);

		int headRadix = 0;
		headRadix = ValidateField.validateRadix(
				head.getAttributeValue("radix"), headRadix);

		if (!("string".equals(element.getAttributeValue("type"))))
			return;
		this.compress = ValidateField.validateCompress(elementName,
				format.getAttributeValue("compress"));
	}

	public ValidateVFormat8583(String elementName, String type,
			org.jdom2.Element format, Namespace xsd) throws FormatException {
		this.id = Integer.parseInt(format.getAttributeValue("id"));
		org.jdom2.Element head = format.getChild("head", xsd);
		if (head == null)
			throw new FormatException("element:[" + elementName
					+ "],element format,do not define child element[head]!");

		String headLenStr = head.getAttributeValue("length");
		this.headLen = ValidateField.validateLength(elementName, headLenStr);

		String headCompressStr = head.getAttributeValue("compress");
		this.headCompress = ValidateField.validateCompress(elementName,
				headCompressStr);

		String blankStr = head.getAttributeValue("blank");
		this.headBlank = ValidateField.validateBlank(elementName,
				this.headBlank, blankStr);

		this.headAlign = head.getAttributeValue("align");
		this.headAlign = ValidateField.validateAlign(elementName,
				this.headAlign);

		int headRadix = 0;
		headRadix = ValidateField.validateRadix(
				head.getAttributeValue("radix"), headRadix);

		if (!("string".equals(type)))
			return;
		this.compress = ValidateField.validateCompress(elementName,
				format.getAttributeValue("compress"));
	}

	public ValidateVFormat8583(String elementType, String elementName,
			org.w3c.dom.Element format) throws FormatException {
		this.id = Integer.parseInt(format.getAttribute("id"));
		org.w3c.dom.Element head = (org.w3c.dom.Element) format
				.getElementsByTagName("head").item(0);
		if (head == null)
			throw new FormatException("element:[" + elementName
					+ "],element format,do not define child element[head]!");

		String headLenStr = head.getAttribute("length");
		this.headLen = ValidateField.validateLength(elementName, headLenStr);

		String headCompressStr = head.getAttribute("compress");
		this.headCompress = ValidateField.validateCompress(elementName,
				headCompressStr);

		String blankStr = head.getAttribute("blank");
		this.headBlank = ValidateField.validateBlank(elementName,
				this.headBlank, blankStr);

		this.headAlign = head.getAttribute("align");
		this.headAlign = ValidateField.validateAlign(elementName,
				this.headAlign);

		int headRadix = 0;
		headRadix = ValidateField.validateRadix(head.getAttribute("radix"),
				headRadix);

		if (!("string".equals(elementType)))
			return;
		this.compress = ValidateField.validateCompress(elementName,
				format.getAttribute("compress"));
	}

	public int getCompress() {
		return this.compress;
	}

	public String getHeadAlign() {
		return this.headAlign;
	}

	public char getHeadBlank() {
		return this.headBlank;
	}

	public int getHeadCompress() {
		return this.headCompress;
	}

	public int getHeadLen() {
		return this.headLen;
	}

	public int getHeadRadix() {
		return this.headRadix;
	}

	public int getId() {
		return this.id;
	}
}