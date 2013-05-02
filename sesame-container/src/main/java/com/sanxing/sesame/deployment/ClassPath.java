package com.sanxing.sesame.deployment;

import java.util.Arrays;
import java.util.List;

public class ClassPath {
	private String[] pathElements = new String[0];

	public ClassPath() {
	}

	public ClassPath(String[] pathElements) {
		this.pathElements = pathElements;
	}

	public String[] getPathElements() {
		return this.pathElements;
	}

	public void setPathElements(String[] pathElements) {
		this.pathElements = pathElements;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ClassPath)) {
			return false;
		}

		ClassPath classPath = (ClassPath) o;

		return (Arrays.equals(this.pathElements, classPath.pathElements));
	}

	public int hashCode() {
		if (this.pathElements == null) {
			return 0;
		}
		int result = 1;
		for (int i = 0; i < this.pathElements.length; ++i) {
			result = 31
					* result
					+ ((this.pathElements[i] == null) ? 0
							: this.pathElements[i].hashCode());
		}
		return result;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer("ClassPath[");
		for (int i = 0; i < this.pathElements.length; ++i) {
			String pathElement = this.pathElements[i];
			if (i > 0) {
				buffer.append(", ");
			}
			buffer.append(pathElement);
		}
		return buffer.toString();
	}

	public List getPathList() {
		return Arrays.asList(this.pathElements);
	}

	public void setPathList(List list) {
		this.pathElements = new String[list.size()];
		list.toArray(this.pathElements);
	}
}