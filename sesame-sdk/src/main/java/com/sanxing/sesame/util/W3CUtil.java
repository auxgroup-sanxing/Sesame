package com.sanxing.sesame.util;

import java.io.StringWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class W3CUtil {
	private static final Logger LOG = LoggerFactory.getLogger(W3CUtil.class);
	private static DocumentBuilderFactory dbf;
	private static Queue builders = new ConcurrentLinkedQueue();

	public static String getElementText(Element element) {
		StringBuffer buffer = new StringBuffer();
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			if ((node.getNodeType() == 3) || (node.getNodeType() == 4)) {
				buffer.append(node.getNodeValue());
			}
		}
		return buffer.toString();
	}

	public static String getChildText(Element element, String childName) {
		NodeList list = element.getElementsByTagName(childName);
		return ((list.getLength() > 0) ? list.item(0).getTextContent() : "");
	}

	public static void moveContent(Element from, Element to) {
		NodeList childNodes = from.getChildNodes();
		while (childNodes.getLength() > 0) {
			Node node = childNodes.item(0);
			from.removeChild(node);
			to.appendChild(node);
		}
	}

	public static void copyAttributes(Element from, Element to) {
		NamedNodeMap attributes = from.getAttributes();
		for (int i = 0; i < attributes.getLength(); ++i) {
			Attr node = (Attr) attributes.item(i);
			to.setAttributeNS(node.getNamespaceURI(), node.getName(),
					node.getValue());
		}
	}

	public static String asXML(Node node) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		StringWriter buffer = new StringWriter();
		transformer.transform(new DOMSource(node), new StreamResult(buffer));
		return buffer.toString();
	}

	public static String asIndentedXML(Node node) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty("indent", "yes");
		StringWriter buffer = new StringWriter();
		transformer.transform(new DOMSource(node), new StreamResult(buffer));
		return buffer.toString();
	}

	public static void addChildElement(Element element, String name,
			Object textValue) {
		Document document = element.getOwnerDocument();
		Element child = document.createElement(name);
		element.appendChild(child);
		if (textValue != null) {
			String text = textValue.toString();
			child.appendChild(document.createTextNode(text));
		}
	}

	public static QName createQName(Element element, String qualifiedName) {
		int index = qualifiedName.indexOf(58);
		if (index >= 0) {
			String prefix = qualifiedName.substring(0, index);
			String localName = qualifiedName.substring(index + 1);
			String uri = recursiveGetAttributeValue(element, "xmlns:" + prefix);
			return new QName(uri, localName, prefix);
		}
		String uri = recursiveGetAttributeValue(element, "xmlns");
		if (uri != null) {
			return new QName(uri, qualifiedName);
		}
		return new QName(qualifiedName);
	}

	public static String recursiveGetAttributeValue(Element element,
			String attributeName) {
		String answer = null;
		try {
			answer = element.getAttribute(attributeName);
		} catch (Exception e) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Caught exception looking up attribute: "
						+ attributeName + " on element: " + element
						+ ". Cause: " + e, e);
			}
		}
		if ((answer == null) || (answer.length() == 0)) {
			Node parentNode = element.getParentNode();
			if (parentNode instanceof Element) {
				return recursiveGetAttributeValue((Element) parentNode,
						attributeName);
			}
		}
		return answer;
	}

	public static Element getFirstChildElement(Node parent) {
		NodeList childs = parent.getChildNodes();
		for (int i = 0; i < childs.getLength(); ++i) {
			Node child = childs.item(i);
			if (child instanceof Element) {
				return ((Element) child);
			}
		}
		return null;
	}

	public static Element getNextSiblingElement(Element el) {
		for (Node n = el.getNextSibling(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) {
				return ((Element) n);
			}
		}
		return null;
	}

	public static QName getQName(Element el) {
		if (el == null)
			return null;
		if (el.getPrefix() != null) {
			return new QName(el.getNamespaceURI(), el.getLocalName(),
					el.getPrefix());
		}
		return new QName(el.getNamespaceURI(), el.getLocalName());
	}

	public static DocumentBuilder getBuilder()
			throws ParserConfigurationException {
		DocumentBuilder builder = (DocumentBuilder) builders.poll();
		if (builder == null) {
			if (dbf == null) {
				dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
			}
			builder = dbf.newDocumentBuilder();
		}
		return builder;
	}

	public static void releaseBuilder(DocumentBuilder builder) {
		builders.add(builder);
	}

	public static Document newDocument() throws ParserConfigurationException {
		DocumentBuilder builder = getBuilder();
		Document doc = builder.newDocument();
		releaseBuilder(builder);
		return doc;
	}
}