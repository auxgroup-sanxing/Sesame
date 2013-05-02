package com.sanxing.sesame.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import javax.activation.DataSource;

public class ByteArrayDataSource implements DataSource, Serializable {
	private static final long serialVersionUID = 1L;
	private byte[] data;
	private String type;
	private String name = "unused";

	public ByteArrayDataSource(byte[] data, String type) {
		this.data = data;
		this.type = type;
	}

	public InputStream getInputStream() throws IOException {
		if (this.data == null) {
			throw new IOException("no data");
		}
		return new ByteArrayInputStream(this.data);
	}

	public OutputStream getOutputStream() throws IOException {
		throw new IOException("getOutputStream() not supported");
	}

	public String getContentType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
}