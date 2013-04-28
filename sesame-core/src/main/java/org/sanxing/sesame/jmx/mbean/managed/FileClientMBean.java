package org.sanxing.sesame.jmx.mbean.managed;

public abstract interface FileClientMBean {
	public abstract String fetchFile(String paramString);

	public abstract String getDescription();
}