package org.sanxing.sesame.classloader;

import java.net.URL;

public abstract class AbstractUrlResourceLocation implements ResourceLocation {
	private final URL codeSource;

	public AbstractUrlResourceLocation(URL codeSource) {
		this.codeSource = codeSource;
	}

	public final URL getCodeSource() {
		return this.codeSource;
	}

	public void close() {
	}

	public final boolean equals(Object o) {
		if (this == o)
			return true;
		if ((o == null) || (super.getClass() != o.getClass()))
			return false;

		AbstractUrlResourceLocation that = (AbstractUrlResourceLocation) o;
		return this.codeSource.equals(that.codeSource);
	}

	public final int hashCode() {
		return this.codeSource.hashCode();
	}

	public final String toString() {
		return "[" + super.getClass().getName() + ": " + this.codeSource + "]";
	}
}