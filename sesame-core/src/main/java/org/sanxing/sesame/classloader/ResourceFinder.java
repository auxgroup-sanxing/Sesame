package org.sanxing.sesame.classloader;

import java.net.URL;
import java.util.Enumeration;

public abstract interface ResourceFinder {
	public abstract URL findResource(String paramString);

	public abstract Enumeration findResources(String paramString);

	public abstract ResourceHandle getResource(String paramString);
}