package com.sanxing.ads.console;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class VirtualOutput extends OutputStream {
	private static Logger LOG = Logger.getLogger(VirtualOutput.class);
	private BlockingQueue<String> queue;

	public VirtualOutput(int capacity) {
		this.queue = new LinkedBlockingQueue(capacity);
	}

	public void write(int b) throws IOException {
	}

	public void write(byte[] b, int off, int len) throws IOException {
		try {
			if (this.queue.remainingCapacity() == 0) {
				this.queue.take();
			}
			this.queue.put(new String(b, off, len));
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public String poll(long timeout, TimeUnit unit) throws InterruptedException {
		return ((String) this.queue.poll(timeout, unit));
	}

	public void close() throws IOException {
		this.queue.clear();
		super.close();
	}
}