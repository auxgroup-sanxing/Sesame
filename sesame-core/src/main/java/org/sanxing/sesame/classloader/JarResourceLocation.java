package org.sanxing.sesame.classloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

public class JarResourceLocation extends AbstractUrlResourceLocation {
	private JarFile jarFile;
	private byte[] content;

	public JarResourceLocation(URL codeSource, File cacheFile)
			throws IOException {
		super(codeSource);
		try {
			this.jarFile = new JarFile(cacheFile);
		} catch (ZipException ze) {
			InputStream is = null;
			try {
				is = new FileInputStream(cacheFile);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[2048];
				int bytesRead = -1;
				while ((bytesRead = is.read(buffer)) != -1) {
					baos.write(buffer, 0, bytesRead);
				}
				this.content = baos.toByteArray();
			} finally {
				if (is != null)
					is.close();
			}
		}
	}

	public ResourceHandle getResourceHandle(String resourceName) {
		if(this.jarFile != null){
			try {
				JarEntry jarEntry = this.jarFile.getJarEntry(resourceName);
				if(jarEntry != null){
					return new JarEntryResourceHandle(jarEntry, this.jarFile.getInputStream(jarEntry));
				}
			} catch (IOException e) {
			}
		}else if(this.content != null){
			try {
				JarInputStream is = new JarInputStream(new ByteArrayInputStream(this.content));
				JarEntry jarEntry;
				while((jarEntry = is.getNextJarEntry()) != null) {
					if(jarEntry.getName().equals(resourceName)){
						return new JarEntryResourceHandle(jarEntry, is);
					}
				}
			} catch (IOException e) {
			}
		}
		return null;
	}

	public Manifest getManifest() throws IOException {
		if (this.jarFile != null)
			return this.jarFile.getManifest();
		try {
			JarInputStream is = new JarInputStream(new ByteArrayInputStream(
					this.content));
			return is.getManifest();
		} catch (IOException e) {
		}
		return null;
	}

	public void close() {
		if (this.jarFile != null)
			IoUtil.close(this.jarFile);
	}

	private class JarEntryResourceHandle extends AbstractResourceHandle {
		private final JarEntry jarEntry;
		private final InputStream is;

		public JarEntryResourceHandle(JarEntry jarEntry,
				InputStream inputStream) {
			this.jarEntry = jarEntry;
			this.is = inputStream;
		}

		public String getName() {
			return this.jarEntry.getName();
		}

		public URL getUrl() {
			try {
				return new URL("jar", "", -1,
						JarResourceLocation.this.getCodeSource() + "!/"
								+ this.jarEntry.getName());
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}

		public boolean isDirectory() {
			return this.jarEntry.isDirectory();
		}

		public URL getCodeSourceUrl() {
			return JarResourceLocation.this.getCodeSource();
		}

		public InputStream getInputStream() throws IOException {
			return this.is;
		}

		public int getContentLength() {
			return (int) this.jarEntry.getSize();
		}
	}
}