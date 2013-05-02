package com.sanxing.sesame.container;

import com.sanxing.sesame.mbean.BaseSystemService;
import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.mbean.ManagementContext;
import com.sanxing.sesame.util.FileUtil;
import com.sanxing.sesame.util.FileVersionUtil;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jbi.JBIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EnvironmentContext extends BaseSystemService implements
		EnvironmentContextMBean {
	private static final Log LOG = LogFactory.getLog(EnvironmentContext.class);
	private File jbiRootDir;
	private File componentsDir;
	private File installationDir;
	private File deploymentDir;
	private File sharedLibDir;
	private File serviceAssembliesDir;
	private File tmpDir;
	private File logDir;
	private Map envMap;
	private AtomicBoolean started;

	public EnvironmentContext() {
		this.envMap = new ConcurrentHashMap();
		this.started = new AtomicBoolean(false);
	}

	public static String getVersion() {
		String answer = null;
		Package p = Package.getPackage("com.sanxing.sesame");
		if (p != null) {
			answer = p.getImplementationVersion();
		}
		return ((answer != null) ? answer : "");
	}

	public String getDescription() {
		return "Manages Environment for the Container";
	}

	public File getComponentsDir() {
		return this.componentsDir;
	}

	public File getInstallationDir() {
		return this.installationDir;
	}

	public void setInstallationDir(File installationDir) {
		this.installationDir = installationDir;
	}

	public File getDeploymentDir() {
		return this.deploymentDir;
	}

	public void setDeploymentDir(File deploymentDir) {
		this.deploymentDir = deploymentDir;
	}

	public File getSharedLibDir() {
		return this.sharedLibDir;
	}

	public File getTmpDir() {
		if (this.tmpDir != null) {
			FileUtil.buildDirectory(this.tmpDir);
		}
		return this.tmpDir;
	}

	public File getLogDir() {
		if (this.logDir != null) {
			FileUtil.buildDirectory(this.logDir);
		}
		return this.logDir;
	}

	public File getServiceAssembliesDir() {
		return this.serviceAssembliesDir;
	}

	public void init(JBIContainer container) throws JBIException {
		super.init(container);
		this.jbiRootDir = new File(container.getRootDir());
		buildDirectoryStructure();
	}

	protected Class getServiceMBean() {
		return EnvironmentContextMBean.class;
	}

	public void start() throws JBIException {
		if (this.started.compareAndSet(false, true))
			super.start();
	}

	public void stop() throws JBIException {
		if (this.started.compareAndSet(true, false))
			super.stop();
	}

	public void shutDown() throws JBIException {
		super.shutDown();
		this.envMap.clear();
		this.container.getManagementContext().unregisterMBean(this);
	}

	public ComponentEnvironment registerComponent(ComponentMBeanImpl connector)
			throws JBIException {
		return registerComponent(null, connector);
	}

	public ComponentEnvironment registerComponent(ComponentEnvironment result,
			ComponentMBeanImpl connector) throws JBIException {
		if (result == null) {
			result = new ComponentEnvironment();
		}
		if (!(connector.isPojo())) {
			try {
				String name = connector.getComponentNameSpace().getName();
				if (result.getComponentRoot() == null) {
					File componentRoot = getComponentRootDir(name);
					FileUtil.buildDirectory(componentRoot);
					result.setComponentRoot(componentRoot);
				}
				if (result.getWorkspaceRoot() == null) {
					File privateWorkspace = createWorkspaceDirectory(name);
					result.setWorkspaceRoot(privateWorkspace);
				}
				if (result.getStateFile() == null) {
					File stateFile = FileUtil.getDirectoryPath(
							result.getComponentRoot(), "state.xml");
					result.setStateFile(stateFile);
				}
			} catch (IOException e) {
				throw new JBIException(e);
			}
		}
		result.setLocalConnector(connector);
		this.envMap.put(connector, result);
		return result;
	}

	public File getComponentRootDir(String componentName) {
		if (getComponentsDir() == null) {
			return null;
		}
		return FileUtil.getDirectoryPath(getComponentsDir(), componentName);
	}

	public File createComponentRootDir(String componentName) throws IOException {
		if (getComponentsDir() == null) {
			return null;
		}
		return FileUtil.getDirectoryPath(getComponentsDir(), componentName);
	}

	public File getNewComponentInstallationDir(String componentName)
			throws IOException {
		File result = getComponentRootDir(componentName);

		return FileVersionUtil.getNewVersionDirectory(result);
	}

	public File getComponentInstallationDir(String componentName)
			throws IOException {
		File result = getComponentRootDir(componentName);

		return FileVersionUtil.getLatestVersionDirectory(result);
	}

	public ComponentEnvironment getNewComponentEnvironment(String compName)
			throws IOException {
		File rootDir = FileUtil.getDirectoryPath(getComponentsDir(), compName);
		File instDir = FileVersionUtil.getNewVersionDirectory(rootDir);
		File workDir = FileUtil.getDirectoryPath(rootDir, "workspace");
		File stateFile = FileUtil.getDirectoryPath(rootDir, "state.xml");
		ComponentEnvironment env = new ComponentEnvironment();
		env.setComponentRoot(rootDir);
		env.setInstallRoot(instDir);
		env.setWorkspaceRoot(workDir);
		env.setStateFile(stateFile);
		return env;
	}

	public ComponentEnvironment getComponentEnvironment(String compName)
			throws IOException {
		File rootDir = FileUtil.getDirectoryPath(getComponentsDir(), compName);
		File instDir = FileVersionUtil.getLatestVersionDirectory(rootDir);
		File workDir = FileUtil.getDirectoryPath(rootDir, "workspace");
		File stateFile = FileUtil.getDirectoryPath(rootDir, "state.xml");
		ComponentEnvironment env = new ComponentEnvironment();
		env.setComponentRoot(rootDir);
		env.setInstallRoot(instDir);
		env.setWorkspaceRoot(workDir);
		env.setStateFile(stateFile);
		return env;
	}

	public ServiceAssemblyEnvironment getNewServiceAssemblyEnvironment(
			String saName) throws IOException {
		File rootDir = FileUtil.getDirectoryPath(getServiceAssembliesDir(),
				saName);
		File versDir = FileVersionUtil.getNewVersionDirectory(rootDir);
		File instDir = FileUtil.getDirectoryPath(versDir, "install");
		File susDir = FileUtil.getDirectoryPath(versDir, "sus");
		File stateFile = FileUtil.getDirectoryPath(rootDir, "state.xml");
		ServiceAssemblyEnvironment env = new ServiceAssemblyEnvironment();
		env.setRootDir(rootDir);
		env.setInstallDir(instDir);
		env.setSusDir(susDir);
		env.setStateFile(stateFile);
		return env;
	}

	public ServiceAssemblyEnvironment getServiceAssemblyEnvironment(
			String saName) {
		File rootDir = FileUtil.getDirectoryPath(getServiceAssembliesDir(),
				saName);
		File versDir = FileVersionUtil.getLatestVersionDirectory(rootDir);
		File instDir = FileUtil.getDirectoryPath(versDir, "install");
		File susDir = FileUtil.getDirectoryPath(versDir, "sus");
		File stateFile = FileUtil.getDirectoryPath(rootDir, "state.xml");
		ServiceAssemblyEnvironment env = new ServiceAssemblyEnvironment();
		env.setRootDir(rootDir);
		env.setInstallDir(instDir);
		env.setSusDir(susDir);
		env.setStateFile(stateFile);
		return env;
	}

	public File createWorkspaceDirectory(String componentName)
			throws IOException {
		File result = FileUtil.getDirectoryPath(getComponentsDir(),
				componentName);
		result = FileUtil.getDirectoryPath(result, "workspace");
		FileUtil.buildDirectory(result);
		return result;
	}

	public void unreregister(ComponentMBeanImpl connector) {
		this.envMap.remove(connector);
	}

	public void removeComponentRootDirectory(String componentName) {
		File file = getComponentRootDir(componentName);
		if (file != null)
			if (!(FileUtil.deleteFile(file)))
				LOG.warn("Failed to remove directory structure for component [version]: "
						+ componentName + " [" + file.getName() + ']');
			else
				LOG.info("Removed directory structure for component [version]: "
						+ componentName + " [" + file.getName() + ']');
	}

	public File createSharedLibraryDirectory(String name) {
		File result = FileUtil.getDirectoryPath(getSharedLibDir(), name);
		FileUtil.buildDirectory(result);
		return result;
	}

	public void removeSharedLibraryDirectory(String name) {
		File result = FileUtil.getDirectoryPath(getSharedLibDir(), name);
		FileUtil.deleteFile(result);
	}

	private void buildDirectoryStructure() throws JBIException {
		try {
			this.jbiRootDir = this.jbiRootDir.getCanonicalFile();
			if (!(this.jbiRootDir.exists())) {
				if (!(this.jbiRootDir.mkdirs()))
					throw new JBIException("Directory could not be created: "
							+ this.jbiRootDir.getCanonicalFile());
			} else if (!(this.jbiRootDir.isDirectory())) {
				throw new JBIException("Not a directory: "
						+ this.jbiRootDir.getCanonicalFile());
			}
			if (this.installationDir == null) {
				this.installationDir = FileUtil.getDirectoryPath(
						this.jbiRootDir, "install");
			}
			this.installationDir = this.installationDir.getCanonicalFile();
			if (this.deploymentDir == null) {
				this.deploymentDir = FileUtil.getDirectoryPath(this.jbiRootDir,
						"deploy");
			}
			this.deploymentDir = this.deploymentDir.getCanonicalFile();
			this.componentsDir = FileUtil.getDirectoryPath(this.jbiRootDir,
					"components").getCanonicalFile();
			this.tmpDir = FileUtil.getDirectoryPath(this.jbiRootDir, "temp")
					.getCanonicalFile();
			this.sharedLibDir = FileUtil.getDirectoryPath(this.jbiRootDir,
					"sharedlibs").getCanonicalFile();
			this.serviceAssembliesDir = FileUtil.getDirectoryPath(
					this.jbiRootDir, "service-assemblies").getCanonicalFile();
			this.logDir = FileUtil.getDirectoryPath(this.jbiRootDir, "log")
					.getCanonicalFile();

			FileUtil.buildDirectory(this.installationDir);
			FileUtil.buildDirectory(this.deploymentDir);
			FileUtil.buildDirectory(this.componentsDir);
			FileUtil.buildDirectory(this.tmpDir);
			FileUtil.buildDirectory(this.sharedLibDir);
			FileUtil.buildDirectory(this.serviceAssembliesDir);
			FileUtil.buildDirectory(this.logDir);
		} catch (IOException e) {
			throw new JBIException(e);
		}
	}

	public File getJbiRootDir() {
		return this.jbiRootDir;
	}
}