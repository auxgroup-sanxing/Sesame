package com.sanxing.sesame.container;

import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import java.io.File;

public class ComponentEnvironment {
	private File installRoot;
	private File workspaceRoot;
	private File componentRoot;
	private File stateFile;
	private ComponentMBeanImpl localConnector;

	public File getInstallRoot() {
		return this.installRoot;
	}

	public void setInstallRoot(File installRoot) {
		this.installRoot = installRoot;
	}

	public File getWorkspaceRoot() {
		return this.workspaceRoot;
	}

	public void setWorkspaceRoot(File workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	public ComponentMBeanImpl getLocalConnector() {
		return this.localConnector;
	}

	public void setLocalConnector(ComponentMBeanImpl localConnector) {
		this.localConnector = localConnector;
	}

	public File getComponentRoot() {
		return this.componentRoot;
	}

	public void setComponentRoot(File componentRoot) {
		this.componentRoot = componentRoot;
	}

	public File getStateFile() {
		return this.stateFile;
	}

	public void setStateFile(File stateFile) {
		this.stateFile = stateFile;
	}
}