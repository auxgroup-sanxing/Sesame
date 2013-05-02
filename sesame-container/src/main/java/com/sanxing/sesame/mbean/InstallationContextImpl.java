package com.sanxing.sesame.mbean;

import com.sanxing.sesame.deployment.ClassPath;
import com.sanxing.sesame.deployment.Component;
import com.sanxing.sesame.deployment.Identification;
import com.sanxing.sesame.deployment.InstallationDescriptorExtension;
import com.sanxing.sesame.deployment.SharedLibraryList;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.InstallationContext;
import org.w3c.dom.DocumentFragment;

public class InstallationContextImpl implements InstallationContext {
	private Component descriptor;
	private File installRoot;
	private List<String> classPathElements = Collections.emptyList();
	private ComponentContext context;
	private boolean install = true;

	public InstallationContextImpl(Component descriptor) {
		this.descriptor = descriptor;
		if ((descriptor.getComponentClassPath() == null)
				|| (descriptor.getComponentClassPath().getPathElements() == null)
				|| (descriptor.getComponentClassPath().getPathElements().length <= 0)) {
			return;
		}
		String[] elems = descriptor.getComponentClassPath().getPathElements();
		for (int i = 0; i < elems.length; ++i) {
			if (File.separatorChar == '\\')
				elems[i] = elems[i].replace('/', '\\');
			else {
				elems[i] = elems[i].replace('\\', '/');
			}
		}
		setClassPathElements(Arrays.asList(elems));
	}

	public Component getDescriptor() {
		return this.descriptor;
	}

	public String[] getSharedLibraries() {
		return getSharedLibraries(this.descriptor.getSharedLibraries());
	}

	public String getComponentClassName() {
		return this.descriptor.getComponentClassName();
	}

	public List getClassPathElements() {
		return this.classPathElements;
	}

	public String getComponentName() {
		return this.descriptor.getIdentification().getName();
	}

	public ComponentContext getContext() {
		return this.context;
	}

	public String getInstallRoot() {
		return ((this.installRoot != null) ? this.installRoot.getAbsolutePath()
				: ".");
	}

	public File getInstallRootAsDir() {
		return this.installRoot;
	}

	public DocumentFragment getInstallationDescriptorExtension() {
		InstallationDescriptorExtension desc = this.descriptor
				.getDescriptorExtension();
		return ((desc != null) ? desc.getDescriptorExtension() : null);
	}

	public boolean isInstall() {
		return this.install;
	}

	public final void setClassPathElements(List classPathElements) {
		if (classPathElements == null) {
			throw new IllegalArgumentException("classPathElements is null");
		}
		if (classPathElements.isEmpty()) {
			throw new IllegalArgumentException("classPathElements is empty");
		}
		for (Iterator iter = classPathElements.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (!(obj instanceof String)) {
				throw new IllegalArgumentException(
						"classPathElements must contain element of type String");
			}
			String element = (String) obj;
			String sep = ("\\".equals(File.separator)) ? "/" : "\\";
			int offset = element.indexOf(sep);
			if (offset > -1) {
				throw new IllegalArgumentException(
						"classPathElements contains an invalid file separator '"
								+ sep + "'");
			}
			File f = new File(element);
			if (f.isAbsolute()) {
				throw new IllegalArgumentException(
						"classPathElements should not contain absolute paths");
			}
		}
		this.classPathElements = new ArrayList(classPathElements);
	}

	public void setContext(ComponentContext context) {
		this.context = context;
	}

	public void setInstall(boolean install) {
		this.install = install;
	}

	public void setInstallRoot(File installRoot) {
		this.installRoot = installRoot;
	}

	public boolean isBinding() {
		return this.descriptor.isBindingComponent();
	}

	public boolean isEngine() {
		return this.descriptor.isServiceEngine();
	}

	public String getComponentDescription() {
		return this.descriptor.getIdentification().getDescription();
	}

	private static String[] getSharedLibraries(
			SharedLibraryList[] sharedLibraries) {
		if ((sharedLibraries == null) || (sharedLibraries.length == 0)) {
			return null;
		}
		String[] names = new String[sharedLibraries.length];
		for (int i = 0; i < names.length; ++i) {
			names[i] = sharedLibraries[i].getName();
		}
		return names;
	}
}