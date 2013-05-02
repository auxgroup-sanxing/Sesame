package com.sanxing.sesame.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Manifest;

public abstract interface ResourceLocation {
	public abstract URL getCodeSource();

	public abstract ResourceHandle getResourceHandle(String paramString);

	public abstract Manifest getManifest() throws IOException;

	public abstract void close();
}