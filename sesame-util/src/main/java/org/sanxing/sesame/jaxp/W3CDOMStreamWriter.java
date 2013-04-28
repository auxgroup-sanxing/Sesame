package org.sanxing.sesame.jaxp;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class W3CDOMStreamWriter extends DOMStreamWriter {
	static final String XML_NS = "http://www.w3.org/2000/xmlns/";
	private Stack stack;
	private Document document;
	private Element currentNode;
	private NamespaceContext context;
	private Map properties;

	public W3CDOMStreamWriter() throws ParserConfigurationException {
		this(DocumentBuilderFactory.newInstance().newDocumentBuilder());
	}

	public W3CDOMStreamWriter(DocumentBuilder builder) {
		this.stack = new Stack();

		this.properties = new HashMap();

		this.document = builder.newDocument();
	}

	public W3CDOMStreamWriter(Document document) {
		this.stack = new Stack();

		this.properties = new HashMap();

		this.document = document;
	}

	public Document getDocument() {
		return this.document;
	}

	public void writeStartElement(String local) throws XMLStreamException {
		newChild(this.document.createElement(local));
	}

	private void newChild(Element element) {
		if (this.currentNode != null) {
			this.stack.push(this.currentNode);
			this.currentNode.appendChild(element);
		} else {
			this.document.appendChild(element);
		}

		W3CNamespaceContext ctx = new W3CNamespaceContext();
		ctx.setElement(element);
		this.context = ctx;

		this.currentNode = element;
	}

	public void writeStartElement(String namespace, String local)
			throws XMLStreamException {
		newChild(this.document.createElementNS(namespace, local));
	}

	public void writeStartElement(String prefix, String local, String namespace)
			throws XMLStreamException {
		if ((prefix == null) || (prefix.equals("")))
			writeStartElement(namespace, local);
		else
			newChild(this.document.createElementNS(namespace, prefix + ":"
					+ local));
	}

	public void writeEmptyElement(String namespace, String local)
			throws XMLStreamException {
		writeStartElement(namespace, local);
	}

	public void writeEmptyElement(String prefix, String namespace, String local)
			throws XMLStreamException {
		writeStartElement(prefix, namespace, local);
	}

	public void writeEmptyElement(String local) throws XMLStreamException {
		writeStartElement(local);
	}

	public void writeEndElement() throws XMLStreamException {
		if (this.stack.size() > 0)
			this.currentNode = ((Element) this.stack.pop());
		else
			this.currentNode = null;
	}

	public void writeEndDocument() throws XMLStreamException {
	}

	public void writeAttribute(String local, String value)
			throws XMLStreamException {
		Attr a = this.document.createAttribute(local);
		a.setValue(value);
		this.currentNode.setAttributeNode(a);
	}

	public void writeAttribute(String prefix, String namespace, String local,
			String value) throws XMLStreamException {
		if (prefix.length() > 0) {
			local = prefix + ":" + local;
		}
		Attr a = this.document.createAttributeNS(namespace, local);
		a.setValue(value);
		this.currentNode.setAttributeNodeNS(a);
	}

	public void writeAttribute(String namespace, String local, String value)
			throws XMLStreamException {
		Attr a = this.document.createAttributeNS(namespace, local);
		a.setValue(value);
		this.currentNode.setAttributeNodeNS(a);
	}

	public void writeNamespace(String prefix, String namespace)
			throws XMLStreamException {
		if (prefix.length() == 0)
			writeDefaultNamespace(namespace);
		else
			this.currentNode.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:" + prefix, namespace);
	}

	public void writeDefaultNamespace(String namespace)
			throws XMLStreamException {
		this.currentNode.setAttributeNS("http://www.w3.org/2000/xmlns/",
				"xmlns", namespace);
	}

	public void writeComment(String value) throws XMLStreamException {
		this.currentNode.appendChild(this.document.createComment(value));
	}

	public void writeProcessingInstruction(String target)
			throws XMLStreamException {
		this.currentNode.appendChild(this.document.createProcessingInstruction(
				target, null));
	}

	public void writeProcessingInstruction(String target, String data)
			throws XMLStreamException {
		this.currentNode.appendChild(this.document.createProcessingInstruction(
				target, data));
	}

	public void writeCData(String data) throws XMLStreamException {
		this.currentNode.appendChild(this.document.createCDATASection(data));
	}

	public void writeDTD(String arg0) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	public void writeEntityRef(String ref) throws XMLStreamException {
		this.currentNode.appendChild(this.document.createEntityReference(ref));
	}

	public void writeStartDocument() throws XMLStreamException {
	}

	public void writeStartDocument(String version) throws XMLStreamException {
		writeStartDocument();
	}

	public void writeStartDocument(String encoding, String version)
			throws XMLStreamException {
		writeStartDocument();
	}

	public void writeCharacters(String text) throws XMLStreamException {
		this.currentNode.appendChild(this.document.createTextNode(text));
	}

	public void writeCharacters(char[] text, int start, int len)
			throws XMLStreamException {
		writeCharacters(new String(text, start, len));
	}

	public String getPrefix(String uri) throws XMLStreamException {
		return this.context.getPrefix(uri);
	}

	public void setPrefix(String arg0, String arg1) throws XMLStreamException {
	}

	public void setDefaultNamespace(String arg0) throws XMLStreamException {
	}

	public void setNamespaceContext(NamespaceContext ctx)
			throws XMLStreamException {
		this.context = ctx;
	}

	public NamespaceContext getNamespaceContext() {
		return this.context;
	}

	public Object getProperty(String prop) throws IllegalArgumentException {
		return this.properties.get(prop);
	}
}