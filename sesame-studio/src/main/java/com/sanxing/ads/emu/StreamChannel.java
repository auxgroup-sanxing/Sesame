package com.sanxing.ads.emu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class StreamChannel {
	private InputStream in;
	private OutputStream out;

	public StreamChannel(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	public InputStream getInput() {
		return this.in;
	}

	public OutputStream getOutput() {
		return this.out;
	}

	public abstract void close() throws IOException;
}