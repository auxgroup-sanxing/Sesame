package org.sanxing.sesame.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public abstract interface ResourceHandle {
	public abstract String getName();

	public abstract URL getUrl();

	public abstract boolean isDirectory();

	public abstract URL getCodeSourceUrl();

	public abstract InputStream getInputStream() throws IOException;

	public abstract int getContentLength();

	public abstract byte[] getBytes() throws IOException;

	public abstract Manifest getManifest() throws IOException;

	public abstract Certificate[] getCertificates();

	public abstract Attributes getAttributes() throws IOException;

	public abstract void close();
}