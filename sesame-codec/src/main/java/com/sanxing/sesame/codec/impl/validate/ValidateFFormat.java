package com.sanxing.sesame.codec.impl.validate;

import com.sanxing.sesame.binding.codec.FormatException;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.w3c.dom.Element;

public class ValidateFFormat {
	private int len;
	private char blank = ' ';
	private String align;
	private String endian;

	public ValidateFFormat(XmlSchemaElement element, Element format)
			throws FormatException {
		String type = element.getSchemaType().getName();
		if (("string".equals(type)) || ("decimal".equals(type))
				|| ("hexBinary".equals(type))) {
			String lenStr = format.getAttribute("length");
			this.len = ValidateField.validateLength(element.getName(), lenStr);

			String blankStr = format.getAttribute("blank");
			this.blank = ValidateField.validateBlank(element.getName(),
					this.blank, blankStr);

			this.align = format.getAttribute("align");
			this.align = ValidateField.validateAlign(element.getName(),
					this.align);
		} else {
			this.endian = format.getAttribute("endian");
			this.endian = ValidateField.validateEndian(element.getName(),
					this.endian);
		}
	}

	public String getAlign() {
		return this.align;
	}

	public char getBlank() {
		return this.blank;
	}

	public int getLen() {
		return this.len;
	}

	public String getEndian() {
		return this.endian;
	}
}