package com.sanxing.sesame.classloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.Permission;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarFileUrlConnection extends JarURLConnection {
	public static final URL DUMMY_JAR_URL;
	private final URL url;
	private final JarFile jarFile;
	private final JarEntry jarEntry;
	private final URL jarFileUrl;

	static {
		try {
			DUMMY_JAR_URL = new URL("jar", "", -1, "file:dummy!/",
					new URLStreamHandler() {
						protected URLConnection openConnection(URL u) {
							throw new UnsupportedOperationException();
						}
					});
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public JarFileUrlConnection(URL url, JarFile jarFile, JarEntry jarEntry)
			throws MalformedURLException {
		super(DUMMY_JAR_URL);

		if (url == null)
			throw new NullPointerException("url is null");
		if (jarFile == null)
			throw new NullPointerException("jarFile is null");
		if (jarEntry == null)
			throw new NullPointerException("jarEntry is null");

		this.url = url;
		this.jarFile = jarFile;
		this.jarEntry = jarEntry;
		this.jarFileUrl = new File(jarFile.getName()).toURL();
	}

	public JarFile getJarFile() throws IOException {
		return this.jarFile;
	}

	public synchronized void connect() {
	}

	public URL getJarFileURL() {
		return this.jarFileUrl;
	}

	public String getEntryName() {
		return getJarEntry().getName();
	}

	public Manifest getManifest() throws IOException {
		return this.jarFile.getManifest();
	}

	public JarEntry getJarEntry() {
		return this.jarEntry;
	}

	public Attributes getAttributes() throws IOException {
		return getJarEntry().getAttributes();
	}

	public Attributes getMainAttributes() throws IOException {
		return getManifest().getMainAttributes();
	}

	public Certificate[] getCertificates() throws IOException {
		return getJarEntry().getCertificates();
	}

	public URL getURL() {
		return this.url;
	}

	public int getContentLength() {
		long size = getJarEntry().getSize();
		if (size > 2147483647L) {
			return -1;
		}
		return (int) size;
	}

	public long getLastModified() {
		return getJarEntry().getTime();
	}

	public synchronized InputStream getInputStream() throws IOException {
		return this.jarFile.getInputStream(this.jarEntry);
	}

	public Permission getPermission() throws IOException {
		URL jarFileUrl = new File(this.jarFile.getName()).toURL();
		return jarFileUrl.openConnection().getPermission();
	}

	public String toString() {
		return JarFileUrlConnection.class.getName() + ":" + this.url;
	}
}