package com.sanxing.sesame.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

public class StreamDataSource implements DataSource {
	private InputStream in;
	private String contentType;
	private String name;

	public StreamDataSource(InputStream in) {
		this(in, null, null);
	}

	public StreamDataSource(InputStream in, String contentType) {
		this(in, contentType, null);
	}

	public StreamDataSource(InputStream in, String contentType, String name) {
		this.in = in;
		this.contentType = contentType;
		this.name = name;
	}

	public InputStream getInputStream() throws IOException {
		if (this.in == null) {
			throw new IOException("no data");
		}
		return this.in;
	}

	public OutputStream getOutputStream() throws IOException {
		throw new IOException("getOutputStream() not supported");
	}

	public String getContentType() {
		return this.contentType;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}