package com.sanxing.sesame.deployment;

public class SharedLibraryList {
	private String version;
	private String name;

	public SharedLibraryList() {
	}

	public SharedLibraryList(String name) {
		this.name = name;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SharedLibraryList)) {
			return false;
		}

		SharedLibraryList sharedLibraryList = (SharedLibraryList) o;

		if (this.name != null && this.name.equals(sharedLibraryList.name)
				&& this.version != null
				&& this.version.equals(sharedLibraryList.version))
			return true;

		return false;
	}

	public int hashCode() {
		int result = (this.version != null) ? this.version.hashCode() : 0;
		result = ((29 * result) + this.name != null) ? this.name.hashCode() : 0;
		return result;
	}

	public String toString() {
		return "SharedLibraryList[version=" + this.version + "; name="
				+ this.name + "]";
	}
}