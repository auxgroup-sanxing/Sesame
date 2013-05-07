package com.sanxing.sesame.mbean;

import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.container.ServiceAssemblyEnvironment;
import com.sanxing.sesame.deployment.Descriptor;
import com.sanxing.sesame.deployment.DescriptorFactory;
import com.sanxing.sesame.deployment.Identification;
import com.sanxing.sesame.deployment.ServiceAssembly;
import com.sanxing.sesame.deployment.ServiceUnit;
import com.sanxing.sesame.deployment.Target;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.management.ParameterHelper;
import com.sanxing.sesame.util.FileUtil;
import com.sanxing.sesame.util.W3CUtil;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.jbi.JBIException;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.DeploymentServiceMBean;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DeploymentService extends BaseSystemService implements
		DeploymentServiceMBean {
	private static final Logger LOG = LoggerFactory.getLogger(DeploymentService.class);
	private EnvironmentContext environmentContext;
	private Registry registry;

	public void init(JBIContainer container) throws JBIException {
		this.environmentContext = container.getEnvironmentContext();
		this.registry = container.getRegistry();
		super.init(container);
		buildState();
	}

	protected Class<DeploymentServiceMBean> getServiceMBean() {
		return DeploymentServiceMBean.class;
	}

	public void start() throws JBIException {
		super.start();
		String[] sas = this.registry.getDeployedServiceAssemblies();
		for (int i = 0; i < sas.length; ++i)
			try {
				ServiceAssemblyLifeCycle sa = this.registry
						.getServiceAssembly(sas[i]);
				sa.restore();
			} catch (Exception e) {
				LOG.error("Unable to restore state for service assembly "
						+ sas[i], e);
			}
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(), "deployedServiceAssemblies",
				"list of deployed SAs");
		return AttributeInfoHelper.join(super.getAttributeInfos(),
				helper.getAttributeInfos());
	}

	public MBeanOperationInfo[] getOperationInfos() throws JMException {
		OperationInfoHelper helper = new OperationInfoHelper();
		ParameterHelper ph = helper.addOperation(getObjectToManage(), "deploy",
				1, "deploy An SA");
		ph.setDescription(0, "saZipURL", "location of SA zip file");
		ph = helper.addOperation(getObjectToManage(), "undeploy", 1,
				"undeploy An SA");
		ph.setDescription(0, "saName", "SA name");
		ph = helper.addOperation(getObjectToManage(),
				"getDeployedServiceUnitList", 1,
				"list of SU's currently deployed");

		ph.setDescription(0, "componentName", "Component name");
		ph = helper.addOperation(getObjectToManage(),
				"getServiceAssemblyDescriptor", 1, "Get descriptor for a SA");
		ph.setDescription(0, "saName", "SA name");
		ph = helper.addOperation(getObjectToManage(),
				"getDeployedServiceAssembliesForComponent", 1,
				"list of SA's for a Component");

		ph.setDescription(0, "componentName", "Component name");
		ph = helper.addOperation(getObjectToManage(),
				"getComponentsForDeployedServiceAssembly", 1,
				"list of Components  for a SA");

		ph.setDescription(0, "saName", "SA name");
		ph = helper.addOperation(getObjectToManage(), "isDeployedServiceUnit",
				2, "is SU deployed at a Component ?");
		ph.setDescription(0, "componentName", "Component name");
		ph.setDescription(1, "suName", "SU name");
		ph = helper.addOperation(getObjectToManage(), "canDeployToComponent",
				1, "Can a SU be deployed to a Component?");

		ph.setDescription(0, "componentName", "Component name");
		ph = helper
				.addOperation(getObjectToManage(), "start", 1, "start an SA");
		ph.setDescription(0, "saName", "SA name");
		ph = helper.addOperation(getObjectToManage(), "stop", 1, "stop an SA");
		ph.setDescription(0, "saName", "SA name");
		ph = helper.addOperation(getObjectToManage(), "shutDown", 1,
				"shutDown an SA");
		ph.setDescription(0, "saName", "SA name");
		ph = helper.addOperation(getObjectToManage(), "getState", 1,
				"Running state of an SA");
		ph.setDescription(0, "saName", "SA name");
		return OperationInfoHelper.join(super.getOperationInfos(),
				helper.getOperationInfos());
	}

	public String getDescription() {
		return "Allows admin tools to manage service deployments";
	}

	public String deploy(String saZipURL) throws Exception {
		try {
			if (saZipURL == null) {
				throw ManagementSupport.failure("deploy",
						"saZipURL must not be null");
			}
			File tmpDir = null;
			try {
				tmpDir = ArchiveManager.unpackLocation(
						this.environmentContext.getTmpDir(), saZipURL);
			} catch (Exception e) {
				throw ManagementSupport.failure("deploy",
						"Unable to unpack archive: " + saZipURL, e);
			}

			if (tmpDir == null) {
				throw ManagementSupport.failure("deploy",
						"Unable to find jbi descriptor: " + saZipURL);
			}
			Descriptor root = null;
			try {
				root = DescriptorFactory.buildDescriptor(tmpDir);
			} catch (Exception e) {
				throw ManagementSupport.failure("deploy",
						"Unable to build jbi descriptor: " + saZipURL, e);
			}
			if (root == null) {
				throw ManagementSupport.failure("deploy",
						"Unable to find jbi descriptor: " + saZipURL);
			}
			ServiceAssembly sa = root.getServiceAssembly();
			if (sa == null) {
				throw ManagementSupport.failure("deploy",
						"JBI descriptor is not an assembly descriptor: "
								+ saZipURL);
			}
			return deployServiceAssembly(tmpDir, sa);
		} catch (Exception e) {
			LOG.error("Error deploying service assembly", e);
			throw e;
		}
	}

	public String undeploy(String saName) throws Exception {
		if (saName == null) {
			throw ManagementSupport.failure("undeploy",
					"SA name must not be null");
		}
		ServiceAssemblyLifeCycle sa = this.registry.getServiceAssembly(saName);
		if (sa == null) {
			throw ManagementSupport.failure("undeploy",
					"SA has not been deployed: " + saName);
		}
		String state = sa.getCurrentState();
		if (!("Shutdown".equals(state))) {
			throw ManagementSupport.failure("undeploy",
					"SA must be shut down: " + saName);
		}
		try {
			try {
				sa.shutDown();
			} catch (Exception e) {
			}
			String result = null;
			String assemblyName = sa.getName();
			this.registry.unregisterServiceAssembly(assemblyName);
			ServiceUnitLifeCycle[] sus = sa.getDeployedSUs();
			if (sus != null) {
				for (int i = 0; i < sus.length; ++i) {
					undeployServiceUnit(sus[i]);
				}
			}
			FileUtil.deleteFile(sa.getEnvironment().getRootDir());

			return result;
		} catch (Exception e) {
			LOG.info("Unable to undeploy assembly", e);
			throw e;
		}
	}

	public String[] getDeployedServiceUnitList(String componentName)
			throws Exception {
		try {
			ServiceUnitLifeCycle[] sus = this.registry
					.getDeployedServiceUnits(componentName);
			String[] names = new String[sus.length];
			for (int i = 0; i < names.length; ++i) {
				names[i] = sus[i].getName();
			}
			return names;
		} catch (Exception e) {
			LOG.info("Unable to get deployed service unit list", e);
			throw e;
		}
	}

	public String[] getDeployedServiceAssemblies() throws Exception {
		try {
			return this.registry.getDeployedServiceAssemblies();
		} catch (Exception e) {
			LOG.info("Unable to get deployed service assemblies", e);
			throw e;
		}
	}

	public String getServiceAssemblyDescriptor(String saName) throws Exception {
		ServiceAssemblyLifeCycle sa = this.registry.getServiceAssembly(saName);
		if (sa != null) {
			return sa.getDescriptor();
		}
		return null;
	}

	public String[] getDeployedServiceAssembliesForComponent(
			String componentName) throws Exception {
		try {
			return this.registry
					.getDeployedServiceAssembliesForComponent(componentName);
		} catch (Exception e) {
			LOG.info("Error in getDeployedServiceAssembliesForComponent", e);
			throw e;
		}
	}

	public String[] getComponentsForDeployedServiceAssembly(String saName)
			throws Exception {
		try {
			return this.registry
					.getComponentsForDeployedServiceAssembly(saName);
		} catch (Exception e) {
			LOG.info("Error in getComponentsForDeployedServiceAssembly", e);
			throw e;
		}
	}

	public boolean isDeployedServiceUnit(String componentName, String suName)
			throws Exception {
		try {
			return this.registry.isSADeployedServiceUnit(componentName, suName);
		} catch (Exception e) {
			LOG.info("Error in isSADeployedServiceUnit", e);
			throw e;
		}
	}

	public boolean canDeployToComponent(String componentName) {
		ComponentMBeanImpl lcc = this.container.getRegistry().getComponent(
				componentName);
		return ((lcc != null) && (lcc.isStarted()) && (lcc
				.getServiceUnitManager() != null));
	}

	public String start(String serviceAssemblyName) throws Exception {
		try {
			ServiceAssemblyLifeCycle sa = this.registry
					.getServiceAssembly(serviceAssemblyName);
			return sa.start(true);
		} catch (Exception e) {
			LOG.info("Error in start", e);
			throw e;
		}
	}

	public String stop(String serviceAssemblyName) throws Exception {
		try {
			ServiceAssemblyLifeCycle sa = this.registry
					.getServiceAssembly(serviceAssemblyName);
			return sa.stop(true, false);
		} catch (Exception e) {
			LOG.info("Error in stop", e);
			throw e;
		}
	}

	public String shutDown(String serviceAssemblyName) throws Exception {
		try {
			ServiceAssemblyLifeCycle sa = this.registry
					.getServiceAssembly(serviceAssemblyName);
			return sa.shutDown(true);
		} catch (Exception e) {
			LOG.info("Error in shutDown", e);
			throw e;
		}
	}

	public String getState(String serviceAssemblyName) throws Exception {
		try {
			ServiceAssemblyLifeCycle sa = this.registry
					.getServiceAssembly(serviceAssemblyName);
			return sa.getCurrentState();
		} catch (Exception e) {
			LOG.info("Error in getState", e);
			throw e;
		}
	}

	protected boolean isSaDeployed(String serviceAssemblyName) {
		return (this.registry.getServiceAssembly(serviceAssemblyName) != null);
	}

	protected String deployServiceAssembly(File tmpDir, ServiceAssembly sa)
			throws Exception {
		String assemblyName = sa.getIdentification().getName();
		ServiceAssemblyEnvironment env = this.environmentContext
				.getNewServiceAssemblyEnvironment(assemblyName);
		File saDirectory = env.getInstallDir();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Moving " + tmpDir.getAbsolutePath() + " to "
					+ saDirectory.getAbsolutePath());
		}
		saDirectory.getParentFile().mkdirs();
		if (!(tmpDir.renameTo(saDirectory))) {
			throw ManagementSupport.failure("deploy", "Failed to rename "
					+ tmpDir + " to " + saDirectory);
		}

		ServiceUnit[] sus = sa.getServiceUnits();
		if (sus != null) {
			checkSus(saDirectory, sus);
		}

		int nbFailures = 0;
		List componentResults = new ArrayList();
		List suKeys = new ArrayList();
		if (sus != null) {
			for (int i = 0; i < sus.length; ++i) {
				File targetDir = null;
				String suName = sus[i].getIdentification().getName();
				String artifact = sus[i].getTarget().getArtifactsZip();
				String componentName = sus[i].getTarget().getComponentName();
				try {
					File artifactFile = new File(saDirectory, artifact);
					targetDir = env.getServiceUnitDirectory(componentName,
							suName);
					if (LOG.isDebugEnabled()) {
						LOG.debug("Unpack service unit archive " + artifactFile
								+ " to " + targetDir);
					}
					FileUtil.unpackArchive(artifactFile, targetDir);
				} catch (IOException e) {
					++nbFailures;
					componentResults.add(ManagementSupport
							.createComponentFailure("deploy", componentName,
									"Error unpacking service unit", e));

					continue;
				}

				boolean success = false;
				try {
					ComponentMBeanImpl lcc = this.container.getRegistry()
							.getComponent(componentName);
					ServiceUnitManager sum = lcc.getServiceUnitManager();
					ClassLoader cl = Thread.currentThread()
							.getContextClassLoader();
					try {
						Thread.currentThread().setContextClassLoader(
								lcc.getComponent().getClass().getClassLoader());
						String resultMsg = sum.deploy(suName,
								targetDir.getAbsolutePath());
						success = getComponentTaskResult(resultMsg,
								componentName, componentResults, true);
					} finally {
						Thread.currentThread().setContextClassLoader(cl);
					}
				} catch (Exception e) {
					getComponentTaskError(e, componentName, componentResults);
				}
				if (success)
					suKeys.add(this.registry.registerServiceUnit(sus[i],
							assemblyName, targetDir));
				else {
					++nbFailures;
				}

			}

		}

		if (nbFailures > 0) {
			for (Iterator iter = suKeys.iterator(); iter.hasNext();) {
				try {
					String suName = (String) iter.next();
					ServiceUnitLifeCycle su = this.registry
							.getServiceUnit(suName);
					undeployServiceUnit(su);
				} catch (Exception e) {
					LOG.warn("Error undeploying SU", e);
				}
			}

			FileUtil.deleteFile(saDirectory);
			throw ManagementSupport.failure("deploy", componentResults);
		}

		String[] deployedSUs = (String[]) suKeys.toArray(new String[suKeys
				.size()]);
		ServiceAssemblyLifeCycle salc = this.registry.registerServiceAssembly(
				sa, deployedSUs, env);
		salc.writeRunningState();

		if (nbFailures > 0) {
			return ManagementSupport.createWarningMessage("deploy",
					"Failed to deploy some service units", componentResults);
		}
		return ManagementSupport.createSuccessMessage("deploy",
				componentResults);
	}

	protected void checkSus(File saDirectory, ServiceUnit[] sus)
			throws Exception {
		for (int i = 0; i < sus.length; ++i) {
			String suName = sus[i].getIdentification().getName();
			String artifact = sus[i].getTarget().getArtifactsZip();
			String componentName = sus[i].getTarget().getComponentName();
			File artifactFile = new File(saDirectory, artifact);
			if (!(artifactFile.exists())) {
				throw ManagementSupport.failure("deploy", "Artifact "
						+ artifact + " not found for service unit " + suName);
			}
			ComponentMBeanImpl lcc = this.container.getRegistry().getComponent(
					componentName);
			if (lcc == null) {
				throw ManagementSupport.failure("deploy", "Target component "
						+ componentName + " for service unit " + suName
						+ " is not installed");
			}

			if (!(lcc.isStarted())) {
				throw ManagementSupport.failure("deploy", "Target component "
						+ componentName + " for service unit " + suName
						+ " is not started");
			}

			if (lcc.getServiceUnitManager() == null) {
				throw ManagementSupport.failure("deploy", "Target component "
						+ componentName + " for service unit " + suName
						+ " does not accept deployments");
			}

			if (isDeployedServiceUnit(componentName, suName))
				throw ManagementSupport.failure("deploy", "Service unit "
						+ suName + " is already deployed on component "
						+ componentName);
		}
	}

	protected void getComponentTaskError(Exception exception, String component,
			List<Element> results) {
		Element result = null;
		try {
			Document doc = parse(exception.getMessage());
			result = getElement(doc, "component-task-result");
		} catch (Exception e) {
			result = ManagementSupport.createComponentFailure("deploy",
					component, "Unable to parse result string", exception);
		}

		if (result != null)
			results.add(result);
	}

	protected boolean getComponentTaskResult(String resultMsg,
			String component, List<Element> results, boolean success) {
		Element result = null;
		try {
			Document doc = parse(resultMsg);
			result = getElement(doc, "component-task-result");
			Element e = getChildElement(result, "component-task-result-details");
			e = getChildElement(e, "task-result-details");
			e = getChildElement(e, "task-result");
			String r = W3CUtil.getElementText(e);
			if (!("SUCCESS".equals(r))) {
				success = false;
			}
		} catch (Exception e) {
			try {
				if (success) {
					result = ManagementSupport.createComponentWarning("deploy",
							component, "Unable to parse result string", e);
				} else {
					result = ManagementSupport.createComponentFailure("deploy",
							component, "Unable to parse result string", e);
				}
			} catch (Exception e2) {
				LOG.error(e2);
				result = null;
			}
		}
		if (result != null) {
			results.add(result);
		}
		return success;
	}

	protected Document parse(String result)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(result)));
	}

	protected Element getElement(Document doc, String name) {
		NodeList l = doc.getElementsByTagNameNS(
				"http://java.sun.com/xml/ns/jbi/management-message", name);
		return ((Element) l.item(0));
	}

	protected Element getChildElement(Element element, String name) {
		NodeList l = element.getElementsByTagNameNS(
				"http://java.sun.com/xml/ns/jbi/management-message", name);
		return ((Element) l.item(0));
	}

	protected void undeployServiceUnit(ServiceUnitLifeCycle su)
			throws DeploymentException {
		String name = su.getName();
		String componentName = su.getComponentName();
		File targetDir = su.getServiceUnitRootPath();
		this.registry.unregisterServiceUnit(su.getKey());

		ComponentMBeanImpl component = this.container.getRegistry()
				.getComponent(componentName);
		if (component != null) {
			ServiceUnitManager sum = component.getServiceUnitManager();
			if (sum != null) {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				try {
					Thread.currentThread().setContextClassLoader(
							component.getComponent().getClass()
									.getClassLoader());
					sum.undeploy(name, targetDir.getAbsolutePath());
				} finally {
					Thread.currentThread().setContextClassLoader(cl);
				}
				FileUtil.deleteFile(targetDir);
			}
		} else {
			FileUtil.deleteFile(targetDir);
		}
		LOG.info("UnDeployed ServiceUnitAdaptor " + name + " from Component: "
				+ componentName);
	}

	protected void buildState() {
		LOG.info("Restoring service assemblies");

		File top = this.environmentContext.getServiceAssembliesDir();
		if ((top == null) || (!(top.exists())) || (!(top.isDirectory()))) {
			return;
		}
		File[] files = top.listFiles();
		if (files == null) {
			return;
		}

		for (int i = 0; i < files.length; ++i)
			if (files[i].isDirectory()) {
				String assemblyName = files[i].getName();
				try {
					ServiceAssemblyEnvironment env = this.environmentContext
							.getServiceAssemblyEnvironment(assemblyName);
					Descriptor root = DescriptorFactory.buildDescriptor(env
							.getInstallDir());
					if (root != null) {
						ServiceAssembly sa = root.getServiceAssembly();
						if ((sa != null) && (sa.getIdentification() != null))
							this.registry.registerServiceAssembly(sa, env);
					}
				} catch (Exception e) {
					LOG.error("Failed to initialized service assembly: "
							+ assemblyName, e);
				}
			}
	}
}