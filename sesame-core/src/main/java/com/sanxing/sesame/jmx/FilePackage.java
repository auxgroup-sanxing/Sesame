package com.sanxing.sesame.jmx;

import java.io.Serializable;
import java.util.Arrays;

public class FilePackage implements Serializable {
	private static final long serialVersionUID = -2442691602156220380L;
	private String fileName;
	private long currentPackage;
	private long pageSize;
	private byte[] packageData;

	public boolean isEnd() {
		return ((this.packageData == null) || (this.packageData.length < this.pageSize));
	}

	public String toString() {
		return "FilePackage [currentPackage=" + this.currentPackage
				+ ", fileName=" + this.fileName + ", packageData="
				+ Arrays.toString(this.packageData) + "]";
	}

	public FilePackage(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return this.fileName;
	}

	public long getCurrentPackage() {
		return this.currentPackage;
	}

	public void setCurrentPackage(long currentPackage) {
		this.currentPackage = currentPackage;
	}

	public long getPageSize() {
		return this.pageSize;
	}

	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
	}

	public byte[] getPackageData() {
		return this.packageData;
	}

	public void setPackageData(byte[] packageData) {
		this.packageData = packageData;
	}
}