package com.sanxing.sesame.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Element;

public class SchemaCollection {
	private static XmlSchemaCollection collection = new XmlSchemaCollection();

	public static XmlSchema getSchema(String systemId) {
		XmlSchema[] xsa = collection.getXmlSchema(systemId);
		if (xsa.length > 0)
			return xsa[(xsa.length - 1)];
		return null;
	}

	public static XmlSchema schemaForNamespace(String namespace) {
		return collection.schemaForNamespace(namespace);
	}

	public static XmlSchema[] getSchemas() {
		return collection.getXmlSchemas();
	}

	public static synchronized XmlSchema loadSchema(URL url) throws IOException {
		InputStream inputStream = url.openStream();
		try {
			StreamSource inputSource = new StreamSource();
			inputSource.setInputStream(inputStream);
			inputSource.setSystemId(url.toString());
			XmlSchema result = collection.read(inputSource, null);
			return result;
		} finally {
			inputStream.close();
		}
	}

	public static synchronized XmlSchema loadSchema(Element element,
			String systemId) throws IOException {
		XmlSchema result = collection.read(element, systemId);
		return result;
	}

	public static synchronized void removeSchema(URL url) throws IOException {
	}
}