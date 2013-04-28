package org.sanxing.sesame.classloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

public class UrlResourceFinder implements ResourceFinder {
	private final Object lock = new Object();
	private final LinkedHashSet<URL> urls = new LinkedHashSet();
	private final LinkedHashMap<URL, ResourceLocation> classPath = new LinkedHashMap();
	private final LinkedHashSet<File> watchedFiles = new LinkedHashSet();

	private boolean destroyed = false;

	public UrlResourceFinder() {
	}

	public UrlResourceFinder(URL[] urls) {
		addUrls(urls);
	}

	public void destroy() {
		synchronized (this.lock) {
			if (this.destroyed) {
				return;
			}
			this.destroyed = true;
			this.urls.clear();
			for (ResourceLocation resourceLocation : this.classPath.values()) {
				resourceLocation.close();
			}
			this.classPath.clear();
		}
	}

	public ResourceHandle getResource(String resourceName) {
		synchronized (this.lock) {
			if (this.destroyed) {
				return null;
			}
			for (Map.Entry entry : getClassPath().entrySet()) {
				ResourceLocation resourceLocation = (ResourceLocation) entry
						.getValue();
				ResourceHandle resourceHandle = resourceLocation
						.getResourceHandle(resourceName);
				if ((resourceHandle != null)
						&& (!(resourceHandle.isDirectory()))) {
					return resourceHandle;
				}
			}
		}
		return null;
	}

	public URL findResource(String resourceName) {
		synchronized (this.lock) {
			if (this.destroyed) {
				return null;
			}
			for (Map.Entry entry : getClassPath().entrySet()) {
				ResourceLocation resourceLocation = (ResourceLocation) entry
						.getValue();
				ResourceHandle resourceHandle = resourceLocation
						.getResourceHandle(resourceName);
				if (resourceHandle != null) {
					return resourceHandle.getUrl();
				}
			}
		}
		return null;
	}

	public Enumeration findResources(String resourceName) {
		synchronized (this.lock) {
			return new ResourceEnumeration(new ArrayList(getClassPath()
					.values()), resourceName);
		}
	}

	public void addUrl(URL url) {
		addUrls(Collections.singletonList(url));
	}

	public URL[] getUrls() {
		synchronized (this.lock) {
			return ((URL[]) this.urls.toArray(new URL[this.urls.size()]));
		}
	}

	protected void addUrls(URL[] urls) {
		addUrls(Arrays.asList(urls));
	}

	protected void addUrls(List<URL> urls) {
		synchronized (this.lock) {
			if (this.destroyed) {
				throw new IllegalStateException(
						"UrlResourceFinder has been destroyed");
			}

			boolean shouldRebuild = this.urls.addAll(urls);
			if (shouldRebuild)
				rebuildClassPath();
		}
	}

	private LinkedHashMap<URL, ResourceLocation> getClassPath() {
		assert (Thread.holdsLock(this.lock)) : "This method can only be called while holding the lock";

		for (File file : this.watchedFiles) {
			if (file.canRead()) {
				rebuildClassPath();
				break;
			}
		}

		return this.classPath;
	}

	private void rebuildClassPath() {
		assert (Thread.holdsLock(this.lock)) : "This method can only be called while holding the lock";

		Map existingJarFiles = new LinkedHashMap(this.classPath);
		this.classPath.clear();

		LinkedList locationStack = new LinkedList(this.urls);
		ResourceLocation resourceLocation;
		try {
			while (!(locationStack.isEmpty())) {
				URL url = (URL) locationStack.removeFirst();

				if (this.classPath.containsKey(url)) {
					continue;
				}

				resourceLocation = (ResourceLocation) existingJarFiles.remove(url);

				if (resourceLocation == null) {
					try {
						File file = cacheUrl(url);
						resourceLocation = createResourceLocation(url, file);
					} catch (FileNotFoundException e) {
						if ("file".equals(url.getProtocol())) {
							File file = new File(url.getPath());
							this.watchedFiles.add(file);
						}
						continue;
					} catch (IOException ignored) {
						continue;
					} catch (UnsupportedOperationException ex) {
						System.out.println("The protocol for the JAR file's URL is not supported" + ex);
						continue;
					}

				} 
				this.classPath.put(resourceLocation.getCodeSource(), resourceLocation);

				List manifestClassPath = getManifestClassPath(resourceLocation);
				locationStack.addAll(0, manifestClassPath);
			}
		} catch (Error e) {
			destroy();
			throw e;
		}

		for (ResourceLocation location : (Collection<ResourceLocation>) existingJarFiles.values())
			location.close();
	}

	protected File cacheUrl(URL url) throws IOException {
		if (!("file".equals(url.getProtocol()))) {
			throw new UnsupportedOperationException(
					"Only local file jars are supported " + url);
		}

		File file = new File(url.getPath());
		if (!(file.exists())) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		if (!(file.canRead())) {
			throw new IOException("File is not readable: "
					+ file.getAbsolutePath());
		}
		return file;
	}

	protected ResourceLocation createResourceLocation(URL codeSource,
			File cacheFile) throws IOException {
		if (!(cacheFile.exists())) {
			throw new FileNotFoundException(cacheFile.getAbsolutePath());
		}
		if (!(cacheFile.canRead()))
			throw new IOException("File is not readable: "
					+ cacheFile.getAbsolutePath());
		ResourceLocation resourceLocation;
		if (cacheFile.isDirectory()) {
			resourceLocation = new DirectoryResourceLocation(cacheFile);
		} else
			resourceLocation = new JarResourceLocation(codeSource, cacheFile);

		return resourceLocation;
	}

	private List<URL> getManifestClassPath(ResourceLocation resourceLocation) {
		try {
			Manifest manifest = resourceLocation.getManifest();
			if (manifest == null) {
				return Collections.EMPTY_LIST;
			}

			String manifestClassPath = manifest.getMainAttributes().getValue(
					Attributes.Name.CLASS_PATH);
			if (manifestClassPath == null) {
				return Collections.EMPTY_LIST;
			}

			URL codeSource = resourceLocation.getCodeSource();
			LinkedList classPathUrls = new LinkedList();
			for (StringTokenizer tokenizer = new StringTokenizer(
					manifestClassPath, " "); tokenizer.hasMoreTokens();) {
				String entry = tokenizer.nextToken();
				try {
					URL entryUrl = new URL(codeSource, entry);
					classPathUrls.addLast(entryUrl);
				} catch (MalformedURLException localMalformedURLException) {
				}
			}
			return classPathUrls;
		} catch (IOException ignored) {
		}
		return Collections.EMPTY_LIST;
	}
}