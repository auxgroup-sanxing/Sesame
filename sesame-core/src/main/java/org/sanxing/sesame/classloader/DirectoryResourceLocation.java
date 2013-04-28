package org.sanxing.sesame.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.jar.Manifest;

public class DirectoryResourceLocation extends AbstractUrlResourceLocation {
	private final File baseDir;
	private boolean manifestLoaded = false;
	private Manifest manifest;

	public DirectoryResourceLocation(File baseDir) throws MalformedURLException {
		super(baseDir.toURL());
		this.baseDir = baseDir;
	}

	public ResourceHandle getResourceHandle(String resourceName) {
		File file = new File(this.baseDir, resourceName);
		if ((!(file.exists())) || (!(isLocal(file)))) {
			return null;
		}
		try {
			ResourceHandle resourceHandle = new DirectoryResourceHandle(
					resourceName, file, this.baseDir, getManifestSafe());
			return resourceHandle;
		} catch (MalformedURLException e) {
		}
		return null;
	}

	private boolean isLocal(File file) {
		try {
			String base = this.baseDir.getCanonicalPath();
			String relative = file.getCanonicalPath();
			return relative.startsWith(base);
		} catch (IOException e) {
		}
		return false;
	}

	public Manifest getManifest() throws IOException {
		if (!(this.manifestLoaded)) {
			File manifestFile = new File(this.baseDir, "META-INF/MANIFEST.MF");

			if ((manifestFile.isFile()) && (manifestFile.canRead())) {
				FileInputStream in = null;
				try {
					in = new FileInputStream(manifestFile);
					this.manifest = new Manifest(in);
				} finally {
					IoUtil.close(in);
				}
			}
			this.manifestLoaded = true;
		}
		return this.manifest;
	}

	private Manifest getManifestSafe() {
		Manifest manifest = null;
		try {
			manifest = getManifest();
		} catch (IOException localIOException) {
		}
		return manifest;
	}
}