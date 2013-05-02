package com.sanxing.sesame.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarResourceHandle extends AbstractResourceHandle {
	private final JarFile jarFile;
	private final JarEntry jarEntry;
	private final URL url;
	private final URL codeSource;

	public JarResourceHandle(JarFile jarFile, JarEntry jarEntry, URL codeSource)
			throws MalformedURLException {
		this.jarFile = jarFile;
		this.jarEntry = jarEntry;
		this.url = JarFileUrlStreamHandler.createUrl(jarFile, jarEntry,
				codeSource);
		this.codeSource = codeSource;
	}

	public String getName() {
		return this.jarEntry.getName();
	}

	public URL getUrl() {
		return this.url;
	}

	public URL getCodeSourceUrl() {
		return this.codeSource;
	}

	public boolean isDirectory() {
		return this.jarEntry.isDirectory();
	}

	public InputStream getInputStream() throws IOException {
		return this.jarFile.getInputStream(this.jarEntry);
	}

	public int getContentLength() {
		return (int) this.jarEntry.getSize();
	}

	public Manifest getManifest() throws IOException {
		return this.jarFile.getManifest();
	}

	public Attributes getAttributes() throws IOException {
		return this.jarEntry.getAttributes();
	}

	public Certificate[] getCertificates() {
		return this.jarEntry.getCertificates();
	}
}