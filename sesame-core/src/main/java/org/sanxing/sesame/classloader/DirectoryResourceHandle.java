package org.sanxing.sesame.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class DirectoryResourceHandle extends AbstractResourceHandle {
	private final String name;
	private final File file;
	private final Manifest manifest;
	private final URL url;
	private final URL codeSource;

	public DirectoryResourceHandle(String name, File file, File codeSource,
			Manifest manifest) throws MalformedURLException {
		this.name = name;
		this.file = file;
		this.codeSource = codeSource.toURL();
		this.manifest = manifest;
		this.url = file.toURL();
	}

	public String getName() {
		return this.name;
	}

	public URL getUrl() {
		return this.url;
	}

	public URL getCodeSourceUrl() {
		return this.codeSource;
	}

	public boolean isDirectory() {
		return this.file.isDirectory();
	}

	public InputStream getInputStream() throws IOException {
		if (this.file.isDirectory()) {
			return new IoUtil.EmptyInputStream();
		}
		return new FileInputStream(this.file);
	}

	public int getContentLength() {
		if ((this.file.isDirectory()) || (this.file.length() > 2147483647L)) {
			return -1;
		}
		return (int) this.file.length();
	}

	public Manifest getManifest() throws IOException {
		return this.manifest;
	}

	public Attributes getAttributes() throws IOException {
		if (this.manifest == null) {
			return null;
		}
		return this.manifest.getAttributes(getName());
	}

	public Certificate[] getCertificates() {
		return null;
	}
}