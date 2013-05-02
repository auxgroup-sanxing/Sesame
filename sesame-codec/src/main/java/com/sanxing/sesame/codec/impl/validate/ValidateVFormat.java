package com.sanxing.sesame.codec.impl.validate;

import com.sanxing.sesame.binding.codec.FormatException;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ValidateVFormat {
	private int headLength;
	private char headBlank = ' ';
	private String headAlign;
	private int headRadix = 10;
	private int id = 0;

	public ValidateVFormat(XmlSchemaElement element, Element format)
			throws FormatException {
		Element head = (Element) format.getElementsByTagName("head").item(0);
		if (head == null)
			throw new FormatException("element:[" + element.getName()
					+ "],element format,do not define child element[head]!");

		String headLengthStr = head.getAttribute("length");
		this.headLength = ValidateField.validateLength(element.getName(),
				headLengthStr);

		String headBlankStr = head.getAttribute("blank");
		this.headBlank = ValidateField.validateBlank(element.getName(),
				this.headBlank, headBlankStr);

		this.headAlign = head.getAttribute("align");
		this.headAlign = ValidateField.validateAlign(element.getName(),
				this.headAlign);

		this.headRadix = ValidateField.validateRadix(
				head.getAttribute("radix"), this.headRadix);

		String idStr = format.getAttribute("id");
		if (idStr != "")
			this.id = Integer.parseInt(idStr);
	}

	public String getHeadAlign() {
		return this.headAlign;
	}

	public char getHeadBlank() {
		return this.headBlank;
	}

	public int getHeadLength() {
		return this.headLength;
	}

	public int getHeadRadix() {
		return this.headRadix;
	}

	public int getId() {
		return this.id;
	}
}