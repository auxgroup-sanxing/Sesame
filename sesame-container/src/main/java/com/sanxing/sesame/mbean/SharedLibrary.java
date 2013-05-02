package com.sanxing.sesame.mbean;

import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.deployment.ClassPath;
import com.sanxing.sesame.deployment.Identification;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.MBeanInfoProvider;
import com.sanxing.sesame.sharelib.ShareLibCallback;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

public class SharedLibrary implements SharedLibraryMBean, MBeanInfoProvider {
	private com.sanxing.sesame.deployment.SharedLibrary library;
	private File installationDir;
	private ClassLoader classLoader;
	private ShareLibCallback callback;

	public SharedLibrary(com.sanxing.sesame.deployment.SharedLibrary library,
			File installationDir) {
		this.library = library;
		this.installationDir = installationDir;
		this.classLoader = createClassLoader();
		String strCallBackClazz = library.getCallbackClazz();
		if (strCallBackClazz != null) {
			ClassLoader oldOne = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(this.classLoader);
				if ((library.getCallbackClazz() != null)
						&& (library.getCallbackClazz().length() > 0)) {
					ShareLibCallback callback = (ShareLibCallback) this.classLoader
							.loadClass(library.getCallbackClazz())
							.newInstance();

					this.callback = callback;
					callback.onInstall(installationDir);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Thread.currentThread().setContextClassLoader(oldOne);
			}
		}
	}

	public void dispose() {
		ClassLoader oldOne = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.classLoader);
		if (this.callback != null) {
			this.callback.onDispose(this.installationDir);
		}

		Thread.currentThread().setContextClassLoader(oldOne);

		if (this.classLoader instanceof JarFileClassLoader) {
			((JarFileClassLoader) this.classLoader).destroy();
		}
		this.classLoader = null;
	}

	public com.sanxing.sesame.deployment.SharedLibrary getLibrary() {
		return this.library;
	}

	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	private ClassLoader createClassLoader() {
		boolean parentFirst = this.library.isParentFirstClassLoaderDelegation();

		ClassLoader parent = super.getClass().getClassLoader();

		ClassPath cp = this.library.getSharedLibraryClassPath();
		String[] classPathNames = cp.getPathElements();
		URL[] urls = new URL[classPathNames.length];
		for (int i = 0; i < classPathNames.length; ++i) {
			File file = new File(this.installationDir, classPathNames[i]);
			try {
				urls[i] = file.toURL();
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(classPathNames[i], e);
			}
		}
		return new JarFileClassLoader(urls, parent, !(parentFirst),
				new String[0], new String[] { "java.", "javax." });
	}

	public String getDescription() {
		return this.library.getIdentification().getDescription();
	}

	public String getName() {
		return this.library.getIdentification().getName();
	}

	public String getVersion() {
		return this.library.getVersion();
	}

	public Object getObjectToManage() {
		return this;
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(), "name",
				"name of the shared library");

		helper.addAttribute(getObjectToManage(), "description",
				"description of this shared library");

		helper.addAttribute(getObjectToManage(), "version",
				"version of this shared library");

		return helper.getAttributeInfos();
	}

	public MBeanOperationInfo[] getOperationInfos() throws JMException {
		return null;
	}

	public String getSubType() {
		return null;
	}

	public String getType() {
		return "SharedLibrary";
	}

	public void setPropertyChangeListener(PropertyChangeListener l) {
	}
}