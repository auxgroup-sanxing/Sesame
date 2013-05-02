package com.sanxing.sesame.mbean;

import java.util.Properties;
import javax.jbi.management.LifeCycleMBean;

public abstract interface CommandsServiceMBean extends LifeCycleMBean {
	public abstract String installComponent(String paramString,
			Properties paramProperties, boolean paramBoolean) throws Exception;

	public abstract String uninstallComponent(String paramString)
			throws Exception;

	public abstract String installSharedLibrary(String paramString,
			boolean paramBoolean) throws Exception;

	public abstract String uninstallSharedLibrary(String paramString)
			throws Exception;

	public abstract String startComponent(String paramString) throws Exception;

	public abstract String stopComponent(String paramString) throws Exception;

	public abstract String shutdownComponent(String paramString)
			throws Exception;

	public abstract String deployServiceAssembly(String paramString,
			boolean paramBoolean) throws Exception;

	public abstract String undeployServiceAssembly(String paramString)
			throws Exception;

	public abstract String startServiceAssembly(String paramString)
			throws Exception;

	public abstract String stopServiceAssembly(String paramString)
			throws Exception;

	public abstract String shutdownServiceAssembly(String paramString)
			throws Exception;

	public abstract String installArchive(String paramString) throws Exception;

	public abstract String listComponents(boolean paramBoolean1,
			boolean paramBoolean2, boolean paramBoolean3, String paramString1,
			String paramString2, String paramString3) throws Exception;

	public abstract String listSharedLibraries(String paramString1,
			String paramString2) throws Exception;

	public abstract String listServiceAssemblies(String paramString1,
			String paramString2, String paramString3) throws Exception;
}