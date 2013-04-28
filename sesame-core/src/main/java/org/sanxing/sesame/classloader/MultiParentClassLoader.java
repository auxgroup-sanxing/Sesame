package org.sanxing.sesame.classloader;

import java.beans.Introspector;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class MultiParentClassLoader extends URLClassLoader {
	private final ClassLoader[] parents;
	private final boolean inverseClassLoading;
	private final String[] hiddenClasses;
	private final String[] nonOverridableClasses;
	private final String[] hiddenResources;
	private final String[] nonOverridableResources;
	private boolean destroyed;

	public MultiParentClassLoader(URL[] urls) {
		super(urls);

		this.destroyed = false;

		this.parents = new ClassLoader[] { ClassLoader.getSystemClassLoader() };
		this.inverseClassLoading = false;
		this.hiddenClasses = new String[0];
		this.nonOverridableClasses = new String[0];
		this.hiddenResources = new String[0];
		this.nonOverridableResources = new String[0];
	}

	public MultiParentClassLoader(URL[] urls, ClassLoader parent) {
		this(urls, parent, new ClassLoader[] { parent });
	}

	public MultiParentClassLoader(URL[] urls, ClassLoader parent,
			boolean inverseClassLoading, String[] hiddenClasses,
			String[] nonOverridableClasses) {
		this(urls, parent, new ClassLoader[] { parent }, inverseClassLoading,
				hiddenClasses, nonOverridableClasses);
	}

	public MultiParentClassLoader(URL[] urls, ClassLoader parent,
			URLStreamHandlerFactory factory) {
		this(urls, new ClassLoader[] { parent }, factory);
	}

	public MultiParentClassLoader(URL[] urls, ClassLoader parent, ClassLoader[] parents) {
		super(urls, parent);

		this.destroyed = false;

		this.parents = copyParents(parents);
		this.inverseClassLoading = false;
		this.hiddenClasses = new String[0];
		this.nonOverridableClasses = new String[0];
		this.hiddenResources = new String[0];
		this.nonOverridableResources = new String[0];
	}

	public MultiParentClassLoader(URL[] urls, ClassLoader parent, ClassLoader[] parents,
			boolean inverseClassLoading, Collection hiddenClasses,
			Collection nonOverridableClasses) {
		this(urls, parent, parents, inverseClassLoading, (String[]) hiddenClasses
				.toArray(new String[hiddenClasses.size()]),
				(String[]) nonOverridableClasses
						.toArray(new String[nonOverridableClasses.size()]));
	}

	public MultiParentClassLoader(URL[] urls, ClassLoader parent, ClassLoader[] parents,
			boolean inverseClassLoading, String[] hiddenClasses,
			String[] nonOverridableClasses) {
		super(urls, parent);

		this.destroyed = false;

		this.parents = copyParents(parents);
		this.inverseClassLoading = inverseClassLoading;
		this.hiddenClasses = hiddenClasses;
		this.nonOverridableClasses = nonOverridableClasses;
		this.hiddenResources = toResources(hiddenClasses);
		this.nonOverridableResources = toResources(nonOverridableClasses);
	}

	public MultiParentClassLoader(MultiParentClassLoader source) {
		this(source.getURLs(), ClassLoader.getSystemClassLoader(), deepCopyParents(source.parents),
				source.inverseClassLoading, source.hiddenClasses,
				source.nonOverridableClasses);
	}

	static ClassLoader copy(ClassLoader source) {
		if (source instanceof MultiParentClassLoader)
			return new MultiParentClassLoader((MultiParentClassLoader) source);
		if (source instanceof URLClassLoader) {
			return new URLClassLoader(((URLClassLoader) source).getURLs(),
					source.getParent());
		}
		return new URLClassLoader(new URL[0], source);
	}

	ClassLoader copy() {
		return copy(this);
	}

	private String[] toResources(String[] classes) {
		String[] resources = new String[classes.length];
		for (int i = 0; i < classes.length; ++i) {
			String className = classes[i];
			resources[i] = className.replace('.', '/');
		}
		return resources;
	}

	public MultiParentClassLoader(URL[] urls, ClassLoader[] parents,
			URLStreamHandlerFactory factory) {
		super(urls, parents[0], factory);

		this.destroyed = false;

		this.parents = copyParents(parents);
		this.inverseClassLoading = false;
		this.hiddenClasses = new String[0];
		this.nonOverridableClasses = new String[0];
		this.hiddenResources = new String[0];
		this.nonOverridableResources = new String[0];
	}

	private static ClassLoader[] copyParents(ClassLoader[] parents) {
		ClassLoader[] newParentsArray = new ClassLoader[parents.length];
		for (int i = 0; i < parents.length; ++i) {
			ClassLoader parent = parents[i];
			if (parent == null) {
				throw new RuntimeException("parent[" + i + "] is null");
			}
			newParentsArray[i] = parent;
		}
		return newParentsArray;
	}

	private static ClassLoader[] deepCopyParents(ClassLoader[] parents) {
		ClassLoader[] newParentsArray = new ClassLoader[parents.length];
		for (int i = 0; i < parents.length; ++i) {
			ClassLoader parent = parents[i];
			if (parent == null) {
				throw new RuntimeException("parent[" + i + "] is null");
			}
			if (parent instanceof MultiParentClassLoader) {
				parent = ((MultiParentClassLoader) parent).copy();
			}
			newParentsArray[i] = parent;
		}
		return newParentsArray;
	}

	public ClassLoader[] getParents() {
		return this.parents;
	}

	public void addURL(URL url) {
		super.addURL(url);
	}

	protected synchronized Class loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		Class cachedClass = findLoadedClass(name);
		if (cachedClass != null) {
			return resolveClass(cachedClass, resolve);
		}

		try {
			Class clazz = findClass(name);
			return resolveClass(clazz, resolve);
		} catch (ClassNotFoundException e) {
			if (!(isHiddenClass(name))) {
				for (int i = 0; i < this.parents.length; ++i) {
					ClassLoader parent = this.parents[i];
					try {
						Class clazz = parent.loadClass(name);
						return resolveClass(clazz, resolve);
					} catch (ClassNotFoundException ex) {
					}

				}

			}

			try {
				Class clazz = findClass(name);
				return resolveClass(clazz, resolve);
			} catch (ClassNotFoundException ex) {
				throw new ClassNotFoundException(name);
			}
		}
	}

	private boolean isNonOverridableClass(String name) {
		for (int i = 0; i < this.nonOverridableClasses.length; ++i) {
			if (name.startsWith(this.nonOverridableClasses[i])) {
				return true;
			}
		}
		return false;
	}

	private boolean isHiddenClass(String name) {
		for (int i = 0; i < this.hiddenClasses.length; ++i) {
			if (name.startsWith(this.hiddenClasses[i])) {
				return true;
			}
		}
		return false;
	}

	private Class resolveClass(Class clazz, boolean resolve) {
		if (resolve) {
			resolveClass(clazz);
		}
		return clazz;
	}

	public URL getResource(String name) {
		if (isDestroyed()) {
			return null;
		}

		if ((this.inverseClassLoading) && (!(isDestroyed()))
				&& (!(isNonOverridableResource(name)))) {
			URL url = findResource(name);
			if (url != null) {
				return url;
			}

		}

		if (!(isHiddenResource(name))) {
			for (int i = 0; i < this.parents.length; ++i) {
				ClassLoader parent = this.parents[i];
				URL url = parent.getResource(name);
				if (url != null) {
					return url;
				}

			}

		}

		if (!(isDestroyed())) {
			return findResource(name);
		}

		return null;
	}

	public Enumeration findResources(String name) throws IOException {
		if (isDestroyed()) {
			return Collections.enumeration(Collections.EMPTY_SET);
		}

		List resources = new ArrayList();

		if ((this.inverseClassLoading) && (!(isDestroyed()))) {
			List myResources = Collections.list(super.findResources(name));
			resources.addAll(myResources);
		}

		for (int i = 0; i < this.parents.length; ++i) {
			ClassLoader parent = this.parents[i];
			List parentResources = Collections.list(parent.getResources(name));
			resources.addAll(parentResources);
		}

		if ((!(this.inverseClassLoading)) && (!(isDestroyed()))) {
			List myResources = Collections.list(super.findResources(name));
			resources.addAll(myResources);
		}

		return Collections.enumeration(resources);
	}

	private boolean isNonOverridableResource(String name) {
		for (int i = 0; i < this.nonOverridableResources.length; ++i) {
			if (name.startsWith(this.nonOverridableResources[i])) {
				return true;
			}
		}
		return false;
	}

	private boolean isHiddenResource(String name) {
		for (int i = 0; i < this.hiddenResources.length; ++i) {
			if (name.startsWith(this.hiddenResources[i])) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean isDestroyed() {
		return this.destroyed;
	}

	public void destroy() {
		synchronized (this) {
			if (this.destroyed) {
				return;
			}
			this.destroyed = true;
		}

		Introspector.flushCaches();
	}
}