package com.sanxing.sesame.binding.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.stream.StreamSource;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;

public class BinarySource extends StreamSource {
	private String encoding = System.getProperty("file.encoding");
	private XmlSchema schema;
	private XmlSchemaElement rootElement;
	private String elementName;
	private byte[] byteArray;
	private Map<String, Object> properties = new Hashtable();

	public BinarySource() {
	}

	public BinarySource(InputStream inputStream) {
		super(inputStream);
	}

	public BinarySource(InputStream inputStream, String systemId) {
		super(inputStream, systemId);
	}

	public void setRootElement(XmlSchemaElement rootElement) {
		this.rootElement = rootElement;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public XmlSchemaElement getRootElement() {
		if (this.rootElement != null) {
			return this.rootElement;
		}
		if ((this.schema != null) && (this.elementName != null)) {
			return this.schema.getElementByName(this.elementName);
		}

		return null;
	}

	public InputStream getInputStream() {
		if (this.byteArray != null) {
			return new ByteArrayInputStream(this.byteArray);
		}

		return super.getInputStream();
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void setXMLSchema(XmlSchema schema) {
		this.schema = schema;
	}

	public XmlSchema getXMLSchema() {
		return this.schema;
	}

	public void setBytes(byte[] bytes) {
		this.byteArray = bytes;
	}

	public void setBytes(byte[] bytes, int length) {
		if (bytes.length == length) {
			this.byteArray = bytes;
		} else {
			this.byteArray = new byte[length];
			System.arraycopy(bytes, 0, this.byteArray, 0, length);
		}
	}

	public byte[] getBytes() {
		if (this.byteArray != null) {
			return this.byteArray;
		}
		try {
			InputStream inputStream = getInputStream();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int val;
			while ((val = inputStream.read()) != -1) {
				outputStream.write(val);
			}
			this.byteArray = outputStream.toByteArray();
			return this.byteArray;
		} catch (IOException e) {
		}
		return null;
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
}