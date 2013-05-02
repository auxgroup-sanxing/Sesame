package com.sanxing.sesame.binding.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import javax.activation.DataSource;
import javax.xml.transform.Result;
import org.apache.ws.commons.schema.XmlSchema;

public class BinaryResult implements Result {
	private String elementName;
	private String systemId;
	private OutputStream outputStream;
	private String encoding = System.getProperty("file.encoding");
	private XmlSchema schema;
	private Map<String, Object> properties = new Hashtable();

	private Map<String, DataSource> attachments = new Hashtable();

	public BinaryResult() {
		setOutputStream(new ByteArrayOutputStream());
	}

	public BinaryResult(OutputStream outputStream) {
		setOutputStream(outputStream);
	}

	public BinaryResult(String systemId) {
		this.systemId = systemId;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public OutputStream getOutputStream() {
		return this.outputStream;
	}

	public byte[] getBytes() {
		if (this.outputStream instanceof ByteArrayOutputStream) {
			return ((ByteArrayOutputStream) this.outputStream).toByteArray();
		}

		return null;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getSystemId() {
		return this.systemId;
	}

	public void setXMLSchema(XmlSchema schema) {
		this.schema = schema;
	}

	public XmlSchema getXMLSchema() {
		return this.schema;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public String getElementName() {
		return this.elementName;
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

	public void addAttachment(String id, DataSource source) {
		this.attachments.put(id, source);
	}

	public void removeAttachment(String id) {
		this.attachments.remove(id);
	}

	public DataSource getAttachment(String id) {
		return ((DataSource) this.attachments.get(id));
	}

	public Set<String> getAttachmentNames() {
		return this.attachments.keySet();
	}

	public void write(byte[] b) throws IOException {
		this.outputStream.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		this.outputStream.write(b, off, len);
	}
}