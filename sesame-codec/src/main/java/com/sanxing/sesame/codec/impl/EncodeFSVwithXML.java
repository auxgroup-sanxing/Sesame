package com.sanxing.sesame.codec.impl;

import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.codec.util.CodecUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class EncodeFSVwithXML extends EncodeFSV {
	private static XMLOutputter outputter = new XMLOutputter();

	public void encode(Element xmlElement, XmlSchema schema,
			OutputStream output, String encodeCharset) throws FormatException {
		if (xmlElement == null)
			throw new FormatException(
					"function messageEncode,the parameter [xmlElement] is null");
		XmlSchemaType xsdType = schema.getElementByName(xmlElement.getName())
				.getSchemaType();
		if (!(xsdType instanceof XmlSchemaComplexType))
			throw new FormatException(
					"in xsdDoc,can not find the child element:[complexType]");
		Iterator elements = CodecUtil.getElements(xsdType);

		List fsvElements = new ArrayList();
		while (elements.hasNext()) {
			XmlSchemaElement element = (XmlSchemaElement) elements.next();
			if (CodecUtil.hasFormat(element, schema)) {
				fsvElements.add(element);
			}
		}

		encodeMessage(fsvElements.iterator(), xmlElement, output,
				encodeCharset, schema);

		for (Iterator it = fsvElements.iterator(); it.hasNext();) {
			XmlSchemaElement element = (XmlSchemaElement) it.next();
			xmlElement.removeChild(element.getName());
		}

		try {
			Format newFormat = Format.getRawFormat().setEncoding(encodeCharset);
			outputter.setFormat(newFormat);
			outputter.output(xmlElement.getChildren(), output);
		} catch (IOException e) {
			throw new FormatException(e.getMessage(), e);
		}
	}
}