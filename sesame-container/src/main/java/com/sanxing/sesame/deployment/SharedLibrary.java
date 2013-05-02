package com.sanxing.sesame.deployment;

public class SharedLibrary {
	private String classLoaderDelegation = "parent-first";
	private String version;
	private Identification identification;
	private ClassPath sharedLibraryClassPath;
	private String callbackClazz;

	public String getClassLoaderDelegation() {
		return this.classLoaderDelegation;
	}

	public void setClassLoaderDelegation(String classLoaderDelegation) {
		this.classLoaderDelegation = classLoaderDelegation;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Identification getIdentification() {
		return this.identification;
	}

	public void setIdentification(Identification identification) {
		this.identification = identification;
	}

	public ClassPath getSharedLibraryClassPath() {
		return this.sharedLibraryClassPath;
	}

	public void setSharedLibraryClassPath(ClassPath sharedLibraryClassPath) {
		this.sharedLibraryClassPath = sharedLibraryClassPath;
	}

	public boolean isParentFirstClassLoaderDelegation() {
		return ((this.classLoaderDelegation != null) && (this.classLoaderDelegation
				.equalsIgnoreCase("parent-first")));
	}

	public boolean isSelfFirstClassLoaderDelegation() {
		return ((this.classLoaderDelegation != null) && (this.classLoaderDelegation
				.equalsIgnoreCase("self-first")));
	}

	public String getCallbackClazz() {
		return this.callbackClazz;
	}

	public void setCallbackClazz(String callbackClazz) {
		this.callbackClazz = callbackClazz;
	}
}