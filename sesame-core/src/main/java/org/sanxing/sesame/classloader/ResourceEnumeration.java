package org.sanxing.sesame.classloader;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ResourceEnumeration implements Enumeration {
	private Iterator iterator;
	private final String resourceName;
	private Object next;

	public ResourceEnumeration(Collection resourceLocations, String resourceName) {
		this.iterator = resourceLocations.iterator();
		this.resourceName = resourceName;
	}

	public boolean hasMoreElements() {
		fetchNext();
		return (this.next != null);
	}

	public Object nextElement() {
		fetchNext();

		Object next = this.next;
		this.next = null;

		if (next == null) {
			throw new NoSuchElementException();
		}
		return next;
	}

	private void fetchNext() {
		if (this.iterator == null) {
			return;
		}
		if (this.next != null)
			return;
		try {
			do {
				ResourceLocation resourceLocation = (ResourceLocation) this.iterator
						.next();
				ResourceHandle resourceHandle = resourceLocation
						.getResourceHandle(this.resourceName);
				if (resourceHandle != null) {
					this.next = resourceHandle.getUrl();
					return;
				}
			} while (this.iterator.hasNext());

			this.iterator = null;
		} catch (IllegalStateException e) {
			this.iterator = null;
			throw e;
		}
	}
}