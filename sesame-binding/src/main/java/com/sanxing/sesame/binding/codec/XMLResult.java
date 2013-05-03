package com.sanxing.sesame.binding.codec;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

public class XMLResult implements Result {
	private static final TransformerFactory transformerFactory = TransformerFactory
			.newInstance();
	private String systemId;
	private Source content;
	private Map<String, Object> properties = new Hashtable();

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getSystemId() {
		return this.systemId;
	}

	public Set<String> getPropertyNames() {
		return this.properties.keySet();
	}

	public Object getProperty(String name) {
		return this.properties.get(name);
	}

	public void setProperty(String name, Object value) {
		this.properties.put(name, value);
	}

	public org.w3c.dom.Document getW3CDocument() throws TransformerException {
		if (this.content instanceof DOMSource) {
			return ((org.w3c.dom.Document) ((DOMSource) this.content).getNode());
		}

		DOMResult result = new DOMResult();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(this.content, result);
		return ((org.w3c.dom.Document) result.getNode());
	}

	public org.jdom.Document getJDOMDocument() throws TransformerException {
		if (this.content instanceof JDOMSource) {
			return ((JDOMSource) this.content).getDocument();
		}

		JDOMResult result = new JDOMResult();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(this.content, result);
		return result.getDocument();
	}

	public org.dom4j.Document getDOM4jDocument() throws TransformerException {
		if (this.content instanceof DocumentSource) {
			return ((DocumentSource) this.content).getDocument();
		}

		DocumentResult result = new DocumentResult();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(this.content, result);
		return result.getDocument();
	}

	public Source getContent() {
		return this.content;
	}

	public void setContent(Source content) {
		this.content = content;
	}

	public void setDocument(org.w3c.dom.Document document) {
		this.content = new DOMSource(document);
	}

	public void setDocument(org.jdom.Document document) {
		this.content = new JDOMSource(document);
	}

	public void setDocument(org.dom4j.Document document) {
		this.content = new DocumentSource(document);
	}
}