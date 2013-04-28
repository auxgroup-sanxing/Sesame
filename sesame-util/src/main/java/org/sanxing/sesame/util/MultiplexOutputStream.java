package org.sanxing.sesame.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiplexOutputStream extends OutputStream {
	List streams = new CopyOnWriteArrayList();

	public void add(OutputStream os) {
		this.streams.add(os);
	}

	public void remove(OutputStream os) {
		this.streams.remove(os);
	}

	public synchronized void write(int b) throws IOException {
		for (Iterator i = this.streams.iterator(); i.hasNext();) {
			OutputStream s = (OutputStream) i.next();
			s.write(b);
		}
	}

	public synchronized void write(byte[] b, int off, int len)
			throws IOException {
		for (Iterator i = this.streams.iterator(); i.hasNext();) {
			OutputStream s = (OutputStream) i.next();
			s.write(b, off, len);
		}
	}

	public void flush() throws IOException {
		for (Iterator i = this.streams.iterator(); i.hasNext();) {
			OutputStream s = (OutputStream) i.next();
			s.flush();
		}
	}

	public void close() throws IOException {
		for (Iterator i = this.streams.iterator(); i.hasNext();) {
			OutputStream s = (OutputStream) i.next();
			s.close();
		}
		this.streams.clear();
	}
}