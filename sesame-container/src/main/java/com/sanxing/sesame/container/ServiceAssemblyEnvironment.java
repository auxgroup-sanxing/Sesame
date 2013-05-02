package com.sanxing.sesame.container;

import com.sanxing.sesame.util.FileUtil;
import java.io.File;

public class ServiceAssemblyEnvironment {
	private File rootDir;
	private File installDir;
	private File susDir;
	private File stateFile;

	public File getInstallDir() {
		return this.installDir;
	}

	public void setInstallDir(File installRoot) {
		this.installDir = installRoot;
	}

	public File getSusDir() {
		return this.susDir;
	}

	public void setSusDir(File susRoot) {
		this.susDir = susRoot;
	}

	public File getStateFile() {
		return this.stateFile;
	}

	public void setStateFile(File stateFile) {
		this.stateFile = stateFile;
	}

	public File getRootDir() {
		return this.rootDir;
	}

	public void setRootDir(File rootDir) {
		this.rootDir = rootDir;
	}

	public File getServiceUnitDirectory(String componentName, String suName) {
		File compDir = FileUtil.getDirectoryPath(this.susDir, componentName);
		return FileUtil.getDirectoryPath(compDir, suName);
	}
}