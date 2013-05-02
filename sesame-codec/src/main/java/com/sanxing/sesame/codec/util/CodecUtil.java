package com.sanxing.sesame.codec.util;

import com.sanxing.sesame.binding.codec.FormatException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAppInfo;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.xpath.XPath;
import org.w3c.dom.NodeList;

public class CodecUtil {
	public static Iterator<?> getElements(XmlSchemaType schemaType)
			throws FormatException {
		XmlSchemaComplexType complexType = (XmlSchemaComplexType) schemaType;
		if (!(complexType.getParticle() instanceof XmlSchemaSequence)) {
			throw new FormatException(
					"can not find the child element sequence!");
		}
		XmlSchemaSequence xsdSequence = (XmlSchemaSequence) complexType
				.getParticle();
		XmlSchemaObjectCollection coll = xsdSequence.getItems();
		Iterator elements = coll.getIterator();
		return elements;
	}

	public static org.w3c.dom.Element getXSDFromat(XmlSchemaElement element,
			String elementName) throws FormatException {
		org.w3c.dom.Element format = null;
		if (element.getAnnotation() == null) {
			throw new FormatException("element:[" + elementName
					+ "],can not find the child element [annotation]");
		}
		XmlSchemaObjectCollection annColl = element.getAnnotation().getItems();
		for (Iterator it = annColl.getIterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof XmlSchemaAppInfo) {
				XmlSchemaAppInfo appInfo = (XmlSchemaAppInfo) o;
				NodeList list = appInfo.getMarkup();
				format = getChildEleOfAppinfo(list, "format");
				if (format == null) {
					throw new FormatException("element:[" + elementName
							+ "],can not find the child element [format]");
				}
			}
		}
		if (format == null)
			throw new FormatException("element:[" + elementName
					+ "],can not find the element [appinfo]");
		return format;
	}

	public static org.w3c.dom.Element getChildEleOfAppinfo(NodeList list,
			String childElemName) {
		for (int i = 0; i < list.getLength(); ++i) {
			if (list.item(i).getNodeName().equals(childElemName)) {
				return ((org.w3c.dom.Element) list.item(i));
			}
		}
		return null;
	}

	public static void setByteOrder(String endian, ByteBuffer buf) {
		if ("big".equals(endian))
			buf.order(ByteOrder.BIG_ENDIAN);
		else
			buf.order(ByteOrder.LITTLE_ENDIAN);
	}

	private static boolean simpleElementHasFormat(XmlSchemaElement element)
			throws FormatException {
		boolean result = false;
		if (element.getAnnotation() == null) {
			throw new FormatException("element:[" + element.getName()
					+ "],can not find the child element [annotation]");
		}
		XmlSchemaObjectCollection annColl = element.getAnnotation().getItems();
		for (Iterator it = annColl.getIterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof XmlSchemaAppInfo) {
				result = true;
				break;
			}
		}

		return result;
	}

	private static boolean complexElementHasFormat(XmlSchemaElement element,
			XmlSchema schema) throws FormatException {
		boolean result = false;

		Iterator it = getElements(element.getSchemaType());

		if (it.hasNext()) {
			XmlSchemaElement childElement = (XmlSchemaElement) it.next();
			XmlSchemaType elementType = (childElement.getRefName() != null) ? schema
					.getElementByName(childElement.getRefName())
					.getSchemaType() : childElement.getSchemaType();

			if (childElement.getRefName() != null)
				childElement = schema.getElementByName(childElement
						.getRefName());
			if (elementType instanceof XmlSchemaComplexType)
				complexElementHasFormat(childElement, schema);
			else {
				result = simpleElementHasFormat(childElement);
			}
		}
		return result;
	}

	public static boolean hasFormat(XmlSchemaElement element, XmlSchema schema)
			throws FormatException {
		boolean result = false;
		XmlSchemaType xsType = (element.getRefName() != null) ? schema
				.getElementByName(element.getRefName()).getSchemaType()
				: element.getSchemaType();

		if (element.getRefName() != null) {
			element = schema.getElementByName(element.getRefName());
		}

		if (xsType instanceof XmlSchemaSimpleType)
			result = simpleElementHasFormat(element);
		else {
			result = complexElementHasFormat(element, schema);
		}
		return result;
	}

	public static org.jdom2.Element getComplexType(Document schemaDoc)
			throws FormatException {
		org.jdom2.Element root = schemaDoc.getRootElement();
		Namespace ns = root.getNamespace();
		org.jdom2.Element request;
		XPath xpath;
		try {
			xpath = XPath.newInstance("xsd:element[@name='request']");
			xpath.addNamespace("xsd", ns.getURI());
			request = (org.jdom2.Element) xpath.selectSingleNode(root);
			if (request == null)
				throw new FormatException("");
		} catch (Exception e) {
			throw new FormatException(e.getMessage(), e);
		}
		return request.getChild("complexType", ns);
	}

	public static String getEnvValue(String key) {
		return System.getProperty(key);
	}
}