package com.sanxing.sesame.deployment;

public class Component {
	private String type;
	private String componentClassLoaderDelegation;
	private String bootstrapClassLoaderDelegation;
	private Identification identification;
	private String componentClassName;
	private String description;
	private ClassPath componentClassPath;
	private String bootstrapClassName;
	private ClassPath bootstrapClassPath;
	private SharedLibraryList[] sharedLibraries;
	private InstallationDescriptorExtension descriptorExtension;

	public Component() {
		this.componentClassLoaderDelegation = "parent-first";
		this.bootstrapClassLoaderDelegation = "parent-first";
	}

	public boolean isServiceEngine() {
		return ((this.type != null) && (this.type.equals("service-engine")));
	}

	public boolean isBindingComponent() {
		return ((this.type != null) && (this.type.equals("binding-component")));
	}

	public boolean isComponentClassLoaderDelegationParentFirst() {
		return isParentFirst(this.componentClassLoaderDelegation);
	}

	public boolean isComponentClassLoaderDelegationSelfFirst() {
		return isSelfFirst(this.componentClassLoaderDelegation);
	}

	public boolean isBootstrapClassLoaderDelegationParentFirst() {
		return isParentFirst(this.bootstrapClassLoaderDelegation);
	}

	public boolean isBootstrapClassLoaderDelegationSelfFirst() {
		return isSelfFirst(this.bootstrapClassLoaderDelegation);
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getComponentClassLoaderDelegation() {
		return this.componentClassLoaderDelegation;
	}

	public void setComponentClassLoaderDelegation(
			String componentClassLoaderDelegation) {
		this.componentClassLoaderDelegation = componentClassLoaderDelegation;
	}

	public String getBootstrapClassLoaderDelegation() {
		return this.bootstrapClassLoaderDelegation;
	}

	public void setBootstrapClassLoaderDelegation(
			String bootstrapClassLoaderDelegation) {
		this.bootstrapClassLoaderDelegation = bootstrapClassLoaderDelegation;
	}

	public Identification getIdentification() {
		return this.identification;
	}

	public void setIdentification(Identification identification) {
		this.identification = identification;
	}

	public String getComponentClassName() {
		return this.componentClassName;
	}

	public void setComponentClassName(String componentClassName) {
		this.componentClassName = componentClassName;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ClassPath getComponentClassPath() {
		return this.componentClassPath;
	}

	public void setComponentClassPath(ClassPath componentClassPath) {
		this.componentClassPath = componentClassPath;
	}

	public String getBootstrapClassName() {
		return this.bootstrapClassName;
	}

	public void setBootstrapClassName(String bootstrapClassName) {
		this.bootstrapClassName = bootstrapClassName;
	}

	public ClassPath getBootstrapClassPath() {
		return this.bootstrapClassPath;
	}

	public void setBootstrapClassPath(ClassPath bootstrapClassPath) {
		this.bootstrapClassPath = bootstrapClassPath;
	}

	public SharedLibraryList[] getSharedLibraries() {
		return this.sharedLibraries;
	}

	public void setSharedLibraries(SharedLibraryList[] sharedLibraries) {
		this.sharedLibraries = sharedLibraries;
	}

	public InstallationDescriptorExtension getDescriptorExtension() {
		return this.descriptorExtension;
	}

	public void setDescriptorExtension(
			InstallationDescriptorExtension descriptorExtension) {
		this.descriptorExtension = descriptorExtension;
	}

	protected boolean isParentFirst(String text) {
		return ((text != null) && (text.equalsIgnoreCase("parent-first")));
	}

	protected boolean isSelfFirst(String text) {
		return ((text != null) && (text.equalsIgnoreCase("self-first")));
	}
}