package org.sanxing.sesame.jmx.mbean.admin;

import org.sanxing.sesame.jmx.FilePackage;

public abstract interface FileServerMBean {
	public abstract FilePackage transfer(FilePackage paramFilePackage);

	public abstract String getDescription();
}