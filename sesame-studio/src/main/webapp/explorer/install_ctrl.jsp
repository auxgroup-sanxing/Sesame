<%@page import="java.io.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.net.*"%>
<%@page import="java.util.*,java.util.jar.*,java.util.regex.*"%>
<%@page import="java.util.zip.*"%>
<%@page import="javax.jbi.management.*"%>
<%@page import="javax.management.*"%>
<%@page import="javax.wsdl.*"%>
<%@page import="javax.wsdl.factory.WSDLFactory"%>
<%@page import="javax.wsdl.xml.*"%>
<%@page import="javax.wsdl.extensions.*"%>
<%@page import="javax.wsdl.extensions.schema.*"%>
<%@page import="javax.wsdl.extensions.soap.*"%>
<%@page import="javax.xml.namespace.QName"%>
<%@page import="javax.xml.parsers.*"%>

<%@page import="com.ibm.wsdl.extensions.schema.*" %>
<%@page import="com.ibm.wsdl.extensions.soap.*"%>

<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.studio.emu.*"%>
<%@page import="com.sanxing.studio.team.*"%>
<%@page import="com.sanxing.sesame.jaxp.*"%>

<%@page import="org.apache.commons.fileupload.*"%>
<%@page import="org.apache.commons.fileupload.disk.*"%>
<%@page import="org.apache.commons.fileupload.servlet.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.jdom.*,org.jdom.input.*,org.jdom.output.*,org.jdom.xpath.*"%>
<%@page import="org.json.*"%>
<%@page import="com.sanxing.adp.eclipse.ADPServiceProjectBuilder"%>
<%@page import="com.sanxing.sesame.sharelib.ShareLibManager"%>
<%@page import="com.sanxing.studio.deploy.DirectoryCopy"%>
<%@page import="com.sanxing.studio.utils.DeployReportUtil"%>
<%@page language="java" contentType="text/json; charset=utf-8" pageEncoding="utf-8"%>

<%!private static final Namespace XMLNS_ART = Namespace.getNamespace("sn",
			Namespaces.SESAME);

	private static final String REV_WORKING = "working";
	private static final String REV_HEAD = "head";

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private org.w3c.dom.Element createDocumentationEl(org.w3c.dom.Document doc,
			String documentation) {
		org.w3c.dom.Element element = doc.createElementNS(
				Namespaces.WSDL1_NAMESPACE, "documentation");
		//element.setPrefix("");
		org.w3c.dom.Text textNode = doc.createTextNode(documentation);
		element.appendChild(textNode);
		return element;
	}

	private org.w3c.dom.Element createSchemaEl(org.w3c.dom.Document doc,
			String targetNamespace) {
		org.w3c.dom.Element schemaEl = doc.createElementNS(Namespaces.XSD,
				"schema");
		schemaEl.setPrefix("xs");
		schemaEl.setAttribute("attributeFormDefault", "unqualified");
		schemaEl.setAttribute("elementFormDefault", "qualified");
		schemaEl.setAttribute("targetNamespace", targetNamespace);
		return schemaEl;
	}

	private Document createLibDescriptor(String version) throws IOException {
		Namespace xmlns = Namespace.getNamespace(Namespaces.JBI);
		Namespace xmlns_art = Namespace.getNamespace("sn", Namespaces.SESAME);
		Element rootEl = new Element("jbi", xmlns);
		rootEl.setAttribute("version", "1.0");
		rootEl.addNamespaceDeclaration(xmlns_art);
		Element libEl = new Element("shared-library", xmlns);
		rootEl.addContent(libEl);
		libEl.setAttribute("class-loader-delegation", "parent-first");
		libEl.setAttribute("version", version);
		Element identEl = new Element("identification", xmlns);
		libEl.addContent(identEl);
		Element nameEl = new Element("name", xmlns);
		nameEl.setText("transport_sl");
		identEl.addContent(nameEl);
		Element descEl = new Element("description", xmlns);
		descEl.setText("传输端子共享库");
		identEl.addContent(descEl);
		Element classpathEl = new Element("shared-library-class-path", xmlns);
		libEl.addContent(classpathEl);
		Element pathEl = new Element("path-element", xmlns);
		classpathEl.addContent(pathEl);

		Element extentionEl = new Element("callback-class", xmlns_art);
		extentionEl.setText("com.sanxing.sesame.transport.TransportRegister");
		libEl.addContent(extentionEl);

		return new Document(rootEl);
	}

	private Exception getDeepCause(Throwable t) {
		if (t.getCause() != null) {
			return getDeepCause(t.getCause());
		}
		else if (t instanceof InvocationTargetException) {
			Throwable target = ((InvocationTargetException) t).getTargetException();
			return getDeepCause(target);
		}
		else if (t instanceof Exception) {
			Exception ex = (Exception)t;
			if (ex.getMessage()!=null && ex.getMessage().startsWith("<?xml")) {
				try {
					SAXBuilder builder = new SAXBuilder();
					Document document = builder.build(new StringReader(ex.getMessage()));
					Element rootEl = document.getRootElement();
					XPath xpath = XPath.newInstance("jbi:component-task-result-details/jbi:task-result-details/"+
						"jbi:task-status-msg/jbi:msg-loc-info/jbi:loc-message/text()|jbi:jbi-task-result/jbi:frmwk-task-result/"+
						"jbi:frmwk-task-result-details/jbi:task-result-details/jbi:task-status-msg/"+
						"jbi:msg-loc-info/jbi:loc-message/text()");
					xpath.addNamespace("jbi", rootEl.getNamespaceURI());
					Text text = (Text) xpath.selectSingleNode(rootEl);
					System.out.println("Text: "+text);
					String exceptionMessage = text != null ? text.getText() : "";
					if (exceptionMessage.equals("")) {
						return (Exception) t;
					}
					else {
						return new RuntimeException(exceptionMessage, ex);
					}
				}
				catch (Exception e) {
					return (Exception) ex;
				}
			}
		}
		if (t instanceof Exception) {
			return (Exception) t;
		}
		else {
			return new RuntimeException(t.getMessage(), t);
		}
	}

	private File getFile(String filePath) {
		File file = Configuration.getWorkspaceFile(filePath);
		return file;
	}

	private void deleteFiles(File folder) throws Exception {
		File[] files = folder.listFiles();
		for (int i = files.length - 1; i >= 0; i--) {
			File entry = files[i];
			if (entry.isDirectory()) {
				deleteFiles(entry);
			}
			if (!entry.delete())
				throw new Exception("删除 \"" + entry + "\" 失败");
		}
	}

	private String getDocumentation(Document document) {
		try {
			Element rootEl = document.getRootElement();
			for (Iterator<?> iter = rootEl.getContent().iterator(); iter
					.hasNext();) {
				Content content = (Content) iter.next();
				if (content instanceof Comment)
					return ((Comment) content).getText();
				if (content instanceof Text)
					continue;
				break;
			}

			Element annoEl = rootEl.getChild("annotation", rootEl
					.getNamespace());
			if (annoEl != null) {
				return annoEl.getChildText("documentation", rootEl
						.getNamespace());
			} else {
				return rootEl.getChildText("documentation", rootEl
						.getNamespace());
			}
		} catch (Exception e) {
			return null;
		}
	}

	//获取服务单元的组件名
	private String getComponentName(File unit) throws IOException,
			JDOMException {
		File jbiFile = new File(unit, "jbi.xml");
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(jbiFile);
		Element rootEl = document.getRootElement();
		Namespace xmlns_comp = rootEl.getNamespace("comp");
		if (xmlns_comp != null) {
			String path = xmlns_comp.getURI();
			File compBundle = Configuration.getWarehouseFile(path); // TODO new File(unit.getParentFile(), path);
			try {
				if (compBundle.isDirectory()) {
					document = builder.build(new File(compBundle,
							"jbi.xml"));
				} else {
					JarFile jar = new JarFile(compBundle);
					try {
						JarEntry entry = jar.getJarEntry("jbi.xml");
						document = builder.build(jar.getInputStream(entry));
					} finally {
						jar.close();
					}
				}
				rootEl = document.getRootElement();
				Element idenEl, compEl = rootEl.getChild("component", rootEl
						.getNamespace());
				if (compEl != null
						&& (idenEl = compEl.getChild("identification", rootEl
								.getNamespace())) != null) {
					return idenEl.getChildText("name", rootEl.getNamespace());
				}
			} catch (IOException e) {
				throw new IOException(e.getMessage() + " " + compBundle);
			}
		}
		return null;
	}

	private String getJbiDescription(Document jbiDoc) {
		try {
			Element rootEl = jbiDoc.getRootElement();
			List<?> list = rootEl.getChildren();
			if (list.size() > 0) {
				Element idenEl, firstEl = (Element) list.get(0);
				if ((idenEl = firstEl.getChild("identification", rootEl
						.getNamespace())) != null) {
					return idenEl.getChildText("description", rootEl
							.getNamespace());
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private String getDescription(JarFile jarFile) throws IOException {
		JarEntry entry = jarFile.getJarEntry("jbi.xml");
		if (entry != null) {
			try {
				Document document = new SAXBuilder().build(jarFile
						.getInputStream(entry));
				Element rootEl = document.getRootElement();
				Element idenEl, compEl = rootEl.getChild("component", rootEl
						.getNamespace());

				if (compEl != null
						&& (idenEl = compEl.getChild("identification", rootEl
								.getNamespace())) != null) {
					return idenEl.getChildText("description", rootEl
							.getNamespace());
				}
			} catch (Exception e) {
			}
		}
		Attributes attrs = jarFile.getManifest().getMainAttributes();
		return attrs.getValue("Specification-Title");
	}

	private String getProject(String path) {
		int n = path.indexOf("/");
		return n > 0 ? path.substring(0, n) : path;
	}

	private void deleteSingleDescriptor(File descritorFile, File unit)
			throws Exception {
		SAXBuilder builder = new SAXBuilder();

		Document doc = builder.build(descritorFile);
		Element rootEl = doc.getRootElement();
		Element saEl = rootEl.getChild("service-assembly", rootEl
				.getNamespace());
		Element identEl = saEl
				.getChild("identification", rootEl.getNamespace());

		File descFile = new File(unit, "unit.wsdl");
		String name, desc = null;
		String componentName = null;
		if (descFile.exists()) {
			name = unit.getName();
			componentName = getComponentName(unit);
		} else {
			componentName = unit.getName();
			name = identEl.getChildText("name", rootEl.getNamespace());
			if (unit.getName().equals("schema")) {
				desc = "数据模型";
				name = name + "_xsd";
			} else {
				name = name + "_" + unit.getName();
			}
		}

		List suElList = saEl.getChildren("service-unit", rootEl.getNamespace());
		if (suElList != null && !suElList.isEmpty()) {
			for (Iterator itr = suElList.iterator(); itr.hasNext();) {
				Element suEl = (Element) itr.next();
				Element suIdentEl = suEl.getChild("identification", rootEl
						.getNamespace());
				String suName = suIdentEl.getChildText("name", rootEl
						.getNamespace());
				if (name.equals(suName)) {
					suEl.getParentElement().removeContent(suEl);
				}
			}

			OutputStream output = new FileOutputStream(descritorFile);
			try {
				JdomUtil.getPrettyOutputter().output(doc, output);
			} finally {
				output.close();
			}
		}
	}

	private void updateSingleDescriptor(File descritorFile, File unit)
			throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(descritorFile);
		Element rootEl = doc.getRootElement();
		Element saEl = rootEl.getChild("service-assembly", rootEl
				.getNamespace());
		Element identEl = saEl
				.getChild("identification", rootEl.getNamespace());
		identEl.getChild("name", rootEl.getNamespace()).setText(
				descritorFile.getParentFile().getParentFile().getName());

		File descFile = new File(unit, "unit.wsdl");
		String name, desc = null;
		String componentName = null;
		if (descFile.exists()) {
			name = unit.getName();
			componentName = getComponentName(unit);
		} else {
			componentName = unit.getName();
			name = identEl.getChildText("name", rootEl.getNamespace());
			if (unit.getName().equals("schema")) {
				desc = "数据模型";
				name = name + "_xsd";
			} else {
				name = name + "_" + unit.getName();
			}
		}
		Element suEl = new Element("service-unit", rootEl.getNamespace());
		Element idenEl, targetEl;
		suEl.addContent(idenEl = new Element("identification", rootEl
				.getNamespace()));
		idenEl.addContent(new Element("name", rootEl.getNamespace())
				.setText(name));
		idenEl.addContent(new Element("description", rootEl.getNamespace())
				.setText(desc));
		suEl
				.addContent(targetEl = new Element("target", rootEl
						.getNamespace()));
		targetEl.addContent(new Element("artifacts-zip", rootEl.getNamespace())
				.setText(unit.getName() + ".zip"));
		targetEl
				.addContent(new Element("component-name", rootEl.getNamespace())
						.setText(componentName));

		saEl.addContent(suEl);

		OutputStream output = new FileOutputStream(descritorFile);
		try {
			JdomUtil.getPrettyOutputter().output(doc, output);
		} finally {
			output.close();
		}
	}

	private void updateDescriptor(File descritorFile) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(descritorFile);
		Element rootEl = doc.getRootElement();
		Element saEl = rootEl.getChild("service-assembly", rootEl
				.getNamespace());
		saEl.removeChildren("service-unit", rootEl.getNamespace());
		OutputStream output = new FileOutputStream(descritorFile);
		try {
			JdomUtil.getPrettyOutputter().output(doc, output);
		} finally {
			output.close();
		}
	}

	private void updateDescriptor(File descritorFile, List<File> unitList)
			throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(descritorFile);
		Element rootEl = doc.getRootElement();
		Element saEl = rootEl.getChild("service-assembly", rootEl
				.getNamespace());
		Element identEl = saEl
				.getChild("identification", rootEl.getNamespace());
		identEl.getChild("name", rootEl.getNamespace()).setText(
				descritorFile.getParentFile().getParentFile().getName());
		saEl.removeChildren("service-unit", rootEl.getNamespace());
		for (File unit : unitList) {
			File descFile = new File(unit, "unit.wsdl");
			String name, desc = null;
			String componentName = null;
			if (descFile.exists()) {
				name = unit.getName();
				componentName = getComponentName(unit);
			} else {
				componentName = unit.getName();
				name = identEl.getChildText("name", rootEl.getNamespace());
				if (unit.getName().equals("schema")) {
					desc = "数据模型";
					name = name + "_xsd";
				} else {
					name = name + "_" + unit.getName();
				}
			}
			Element suEl = new Element("service-unit", rootEl.getNamespace());
			Element idenEl, targetEl;
			suEl.addContent(idenEl = new Element("identification", rootEl
					.getNamespace()));
			idenEl.addContent(new Element("name", rootEl.getNamespace())
					.setText(name));
			idenEl.addContent(new Element("description", rootEl.getNamespace())
					.setText(desc));
			suEl.addContent(targetEl = new Element("target", rootEl
					.getNamespace()));
			targetEl.addContent(new Element("artifacts-zip", rootEl
					.getNamespace()).setText(unit.getName() + ".zip"));
			targetEl.addContent(new Element("component-name", rootEl
					.getNamespace()).setText(componentName));

			saEl.addContent(suEl);
		}
		OutputStream output = new FileOutputStream(descritorFile);
		try {
			JdomUtil.getPrettyOutputter().output(doc, output);
		} finally {
			output.close();
		}
	}

	@SuppressWarnings("unchecked")
	private void zipUnit(File fileEntry, String path, ZipOutputStream zipOutput)
			throws Exception {
		if (fileEntry.isFile()) {
			String entyrName = fileEntry.getName();
			if (!entyrName.endsWith(".svn") && !entyrName.endsWith(".java")) {
				zipOutput.putNextEntry(new ZipEntry(path + fileEntry.getName()));
	
				if (fileEntry.getName().equals("unit.wsdl")) {
					WSDLReader reader = WSDLUtil.getWSDLReader();
					WSDLWriter writer = WSDLUtil.getWSDLWriter();
					reader.setFeature("javax.wsdl.verbose", false);
					reader.setFeature("javax.wsdl.importDocuments", true);
					Definition wsdlDef = reader.readWSDL(fileEntry.toURI()
							.toString());
					writer.writeWSDL(wsdlDef, zipOutput);
				} else {
					InputStream fileInput = new FileInputStream(fileEntry);
					byte[] buf = new byte[1024];
					for (int len; (len = fileInput.read(buf)) != -1;) {
						zipOutput.write(buf, 0, len);
					}
					fileInput.close();
				}
				zipOutput.closeEntry();
			}
		} else if (fileEntry.isDirectory()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return !name.endsWith(".svn") && !name.endsWith(".java");
				}
			 };
			path = path + fileEntry.getName() + "/";
			zipOutput.putNextEntry(new ZipEntry(path));
			zipOutput.closeEntry();
			File[] files = fileEntry.listFiles(filter);
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				zipUnit(file, path, zipOutput);
			}
		}
	}

	//导出组件安装包 jar
	public String exportComps(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String compress = request.getParameter("compress");
		String version = request.getParameter("revision");
		File root = Configuration.getWorkspaceRoot();
		//获取页面上用户选择的组件
		JSONObject selected = new JSONObject(request.getParameter("selected"));
		List<File> list = new ArrayList<File>();
		for (Iterator<?> keys = selected.keys(); keys.hasNext();) {
			String key = (String) keys.next();
			String value = selected.getString(key);
			File entry = new File(root, key.replaceFirst("\\/", project
					+ "/component"));
			list.add(entry);
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<div>点击下面的链接下载存档</div>");
		//将选择的组件打包
		List<File> zipList = new ArrayList<File>();
		for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
			File entry = (File) iter.next();
			File outFile = new File(getServletContext().getRealPath(
					"temp/" + entry.getName() + ".jar"));
			buffer.append("<div><a href=\"" + request.getContextPath()
					+ "/temp/" + outFile.getName()
					+ "\"><img src=\"../images/tool16/down.gif\"></img>下载 "
					+ outFile.getName() + "</a></div>");
			FileOutputStream fileOutput = new FileOutputStream(outFile);
			CheckedOutputStream csum = new CheckedOutputStream(fileOutput,
					new CRC32());
			JarOutputStream zipOut = new JarOutputStream(
					new BufferedOutputStream(csum));
			try {
				if (compress == null)
					zipOut.setLevel(0);
				zipOut.setComment("Sanxing Sesame Component");
				for (File file : entry.listFiles())
					zipUnit(file, "", zipOut);
			} finally {
				zipOut.close();
			}
			zipList.add(outFile);
		}
		//传输部署包到指定的部署地址
		URL url = new URL(request.getParameter("target"));
		if (url.getProtocol().equalsIgnoreCase("file")) {
			File targetFolder = new File(url.getFile());
			if (!targetFolder.exists()) {
				throw new Exception("指定的输出目录不存在: " + targetFolder);
			} else {
				targetFolder = new File(targetFolder, "install");
				FileUtil.buildDirectory(targetFolder);
			}

			for (File zipFile : zipList) {
				FileUtil.copyFile(zipFile, new File(targetFolder, zipFile
						.getName()));
			}
		} else {
			//其它部署方式以后再做
		}
		//记录用户的设置
		Map<String, String> properties = new Hashtable<String, String>();
		properties.put("compress", compress);
		properties.put("target", request.getParameter("target"));
		PrefsUtil.savePrefs(project, "default", "deployment", properties);
		//返回下载链接
		//response.setContentType("text/plain; charset=utf-8");
		JSONObject result = new JSONObject();
		result.put("success", true);
		result.put("html", buffer.toString());
		return result.toString();
	}

	/**
	 ** 导出服务集合部署包
	 **/
	public String exportSA(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String compress = request.getParameter("compress");
		String version = request.getParameter("revision");
		
		File buildPath = Application.getWorkspaceRoot();
		long revision = 0;
		if (!version.equals(REV_WORKING)) {
			buildPath = new File(getServletContext().getRealPath("temp/"));
			File projectPath = new File(buildPath, project);
			ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(project);
			long rev = version.equals(REV_HEAD) ? 0 : Long.parseLong(version);
			if (!synchronizer.isVersioned(projectPath)) {
				if (projectPath.exists())
					FileUtil.deleteFile(projectPath);
				revision = synchronizer.checkout(project, projectPath, rev);
			} else {
				revision = synchronizer.update(projectPath, rev);
			}
		}

		//获取页面上用户选择的服务单元
		JSONObject selected = new JSONObject(request.getParameter("selected"));
		List<File> list = new ArrayList<File>();

		// 编译ADP工程
		ADPServiceProjectBuilder adpProject = new ADPServiceProjectBuilder();

		for (Iterator<?> keys = selected.keys(); keys.hasNext();) {
			String key = (String) keys.next();
			String value = selected.getString(key);
			if (value.equals("folder")) {
				File folder = new File(buildPath, key);

				if (folder.exists()) {
					if (folder.getPath().indexOf(File.separator + "engine") != -1)
						adpProject.buildAll(folder.getAbsolutePath());

					File[] files = folder.listFiles();
					if (files.length > 0)
						for (int i = 0; i < files.length; i++) {
							File file = files[i];
							if (!file.getName().startsWith("."))
								list.add(files[i]);
						}
				}
			} else {
				File entry = new File(buildPath, key);
				list.add(entry);

				if (entry.exists()) {
					if (entry.getPath().indexOf(File.separator + "engine") != -1)
						adpProject.buildAll(entry.getAbsolutePath());
				}
			}
		}

		//添加 schema 服务单元
		File schemaUnit = new File(buildPath, project + "/schema");
		if (schemaUnit.exists() && schemaUnit.list().length > 0) {
			list.add(schemaUnit);
		}
		//更新项目的部署描述符
		File jbiFile = new File(buildPath, project + "/jbi.xml");
		updateDescriptor(jbiFile, list);
		//将选择的服务单元打包
		List<File> zipList = new ArrayList<File>();
		for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
			File entry = (File) iter.next();
			File outFile = new File(getServletContext().getRealPath(
					"temp/" + entry.getName() + ".zip"));
			FileOutputStream fileOutput = new FileOutputStream(outFile);
			CheckedOutputStream csum = new CheckedOutputStream(fileOutput,
					new CRC32());
			ZipOutputStream zipOut = new ZipOutputStream(
					new BufferedOutputStream(csum));
			try {
				if (compress == null)
					zipOut.setLevel(0);
				zipOut.setComment("Sanxing Sesame Service-Unit");
				for (File file : entry.listFiles()) {
					zipUnit(file, "", zipOut);
				}
			} finally {
				zipOut.close();
			}
			zipList.add(outFile);
		}

		//生成服务集合部署包
		File outFile = new File(getServletContext().getRealPath(
				"temp/" + project + "-sa.zip"));
		FileOutputStream fileOutput = new FileOutputStream(outFile);
		CheckedOutputStream csum = new CheckedOutputStream(fileOutput,
				new CRC32());
		ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(
				csum));
		try {
			if (compress == null)
				zipOut.setLevel(0);
			zipOut.setComment("Sanxing Sesame Service-Assembly");
			File entry = new File(buildPath, project + "/META-INF");
			FileUtil.zip(entry, "", zipOut);
			for (Iterator<?> entries = zipList.iterator(); entries.hasNext();) {
				entry = (File) entries.next();
				FileUtil.zip(entry, "", zipOut);
			}
		} finally {
			zipOut.close();
		}

		//部署到本地运行环境中
		MBeanServer mbeanServer = (MBeanServer) getServletContext()
				.getAttribute("MBeanServer");
		//MBeanServer mbeanServer = Platform.getLocalMBeanServer();
		if (mbeanServer != null) {
			ObjectName objName = new ObjectName(
					":Type=SystemService,Name=CommandsService,*");
			Set<ObjectInstance> set = mbeanServer.queryMBeans(objName, null);

			if (set.isEmpty()) {
				throw new Exception("没找到部署服务");
			} else {
				ObjectInstance instance = set.iterator().next();
				URL url = outFile.toURL();
				Object[] params = new Object[] { url.toString() };
				String[] signature = new String[] { "java.lang.String" };
				try {
					Object result = mbeanServer.invoke(
							instance.getObjectName(), "installArchive", params,
							signature);
					log.debug(result);
				} catch (Exception e) {
					throw getDeepCause(e);
				}
			}

		} else {
			throw new Exception("Sesame Studio 单独运行时不能使用此功能");
		}

		//记录用户的设置
		Properties properties = PrefsUtil.getPrefs(project, "default",
				"deployment");
		properties.put("compress", compress);
		properties.put("target", request.getParameter("target"));
		properties.put("revision." + revision, String.valueOf(revision));
		PrefsUtil.savePrefs(project, "default", "deployment", properties);
		//返回下载链接
		String html = "<div><b>版本: " + (revision == 0 ? "工作区最新版本" : revision)
				+ "</b><br/>点击下面的链接下载存档</div>";
		html += "<a href=\"" + request.getContextPath() + "/temp/"
				+ outFile.getName()
				+ "\"><img src=\"../images/tool16/down.gif\"></img>下载 "
				+ outFile.getName() + "</a>";
		JSONObject result = new JSONObject();
		result.put("success", true);
		result.put("html", html);
		return result.toString();
	}

	//安装组件、共享库或者传输端子
	@SuppressWarnings("unchecked")
	public String install(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String compress = request.getParameter("compress");
		String version = request.getParameter("revision");
		File root = Application.getWarehouseRoot();
		//获取页面上用户选择的组件
		JSONArray selected = new JSONArray(request.getParameter("selected"));
		List<File> list = new ArrayList<File>();
		for (int i = 0, len = selected.length(); i < len; i++) {
			String path = selected.getString(i);
			File entry = new File(root, path);
			list.add(entry);
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<div>点击下面的链接下载存档</div>");
		//将选择的组件打包
		List<File> zipList = new ArrayList<File>();
		for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
			File entry = (File) iter.next();
			File outFile = new File(getServletContext().getRealPath(
					"temp/" + entry.getName() + ".jar"));
			buffer.append("<div><a href=\"" + request.getContextPath()
					+ "/temp/" + outFile.getName()
					+ "\"><img src=\"../images/tool16/down.gif\"></img>下载 "
					+ outFile.getName() + "</a></div>");
			FileOutputStream fileOutput = new FileOutputStream(outFile);
			CheckedOutputStream csum = new CheckedOutputStream(fileOutput,
					new CRC32());
			JarOutputStream zipOut = new JarOutputStream(
					new BufferedOutputStream(csum));
			try {
				if (compress == null)
					zipOut.setLevel(0);
				zipOut.setComment("Sanxing Sesame Component");
				for (File file : entry.listFiles())
					zipUnit(file, "", zipOut);
			} finally {
				zipOut.close();
			}
			zipList.add(outFile);
		}

		//部署到本地运行环境中
		MBeanServer mbeanServer = (MBeanServer) getServletContext()
				.getAttribute("MBeanServer");
		//MBeanServer mbeanServer = Platform.getLocalMBeanServer();
		if (mbeanServer != null) {
			ObjectName objName = new ObjectName(
					":Type=SystemService,Name=CommandsService,*");
			Set<ObjectInstance> set = mbeanServer.queryMBeans(objName, null);

			if (set.isEmpty()) {
				throw new Exception("没找到部署服务");
			} else {
				ObjectInstance instance = set.iterator().next();
				for (File zipFile : zipList) {
					URL url = zipFile.toURL();
					Object[] params = new Object[] { url.toString() };
					String[] signature = new String[] { "java.lang.String" };
					try {
						Object result = mbeanServer.invoke(instance.getObjectName(), "installArchive", params, signature);
						log.debug(result);
					} 
					catch (MBeanException e) {
						if (e.getCause() != null) {
							throw (Exception) e.getCause();
						} else {
							throw e;
						}
					}
				}
			}

		} else {
			throw new Exception("Sesame Studio 单独运行时不能使用此功能");
		}
		//返回下载链接
		response.setContentType("text/plain; charset=utf-8");
		return buffer.toString();

	}

	// 安装传输端子
	@SuppressWarnings("unchecked")
	public String installTransport(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String compress = request.getParameter("compress");
		File root = Configuration.getWarehouseRoot();
		//获取页面上用户选择的组件
		Document descriptor = createLibDescriptor("1.0");
		Element rootEl = descriptor.getRootElement();
		Element classpathEl = rootEl.getChild("shared-library",
				rootEl.getNamespace()).getChild("shared-library-class-path",
				rootEl.getNamespace());
		JSONArray selected = new JSONArray(request.getParameter("selected"));
		List<File> list = new ArrayList<File>();
		for (int i = 0, len = selected.length(); i < len; i++) {
			String path = selected.getString(i);
			File entry = new File(root, path);
			Element pathEl = new Element("path-element", rootEl.getNamespace());
			pathEl.setText(entry.getName());
			classpathEl.addContent(pathEl);
			list.add(entry);
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<div>点击下面的链接下载存档</div>");
		File outFile = new File(getServletContext().getRealPath(
				"temp/transports.jar"));
		buffer.append("<div><a href=\"" + request.getContextPath() + "/temp/"
				+ outFile.getName()
				+ "\"><img src=\"../images/tool16/down.gif\"></img>下载 "
				+ outFile.getName() + "</a></div>");
		FileOutputStream fileOutput = new FileOutputStream(outFile);
		CheckedOutputStream csum = new CheckedOutputStream(fileOutput,
				new CRC32());
		JarOutputStream zipOut = new JarOutputStream(new BufferedOutputStream(
				csum));
		if (compress == null)
			zipOut.setLevel(0);
		zipOut.setComment("Sanxing Sesame Transport lib");
		//将选择的传输端子打包
		List<File> zipList = new ArrayList<File>();
		try {
			for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
				File entry = (File) iter.next();
				zipUnit(entry, "", zipOut);
			}
			String path = "";
			zipOut.putNextEntry(new ZipEntry(path));
			zipOut.closeEntry();

			path = path + "jbi.xml";
			zipOut.putNextEntry(new ZipEntry(path));
			JdomUtil.getPrettyOutputter().output(descriptor, zipOut);

			zipOut.closeEntry();
		} finally {
			zipOut.close();
		}

		zipList.add(outFile);
		//部署到本地运行环境中
		MBeanServer mbeanServer = (MBeanServer) getServletContext()
				.getAttribute("MBeanServer");
		//MBeanServer mbeanServer = Platform.getLocalMBeanServer();
		if (mbeanServer != null) {
			ObjectName objName = new ObjectName(
					":Type=SystemService,Name=CommandsService,*");
			Set<ObjectInstance> set = mbeanServer.queryMBeans(objName, null);

			if (set.isEmpty()) {
				throw new Exception("没找到部署服务");
			} else {
				ObjectInstance instance = set.iterator().next();
				for (File zipFile : zipList) {
					URL url = zipFile.toURL();
					Object[] params = new Object[] { url.toString() };
					String[] signature = new String[] { "java.lang.String" };
					try {
						Object result = mbeanServer.invoke(instance
								.getObjectName(), "installArchive", params,
								signature);
						log.debug(result);
					} catch (MBeanException e) {
						if (e.getCause() != null) {
							throw (Exception) e.getCause();
						} else {
							throw e;
						}
					}
				}
			}

		} else {
			throw new Exception("Sesame Studio 单独运行时不能使用此功能");
		}
		//返回下载链接
		response.setContentType("text/plain; charset=utf-8");
		return buffer.toString();
	}

	public String loadDeployPrefs(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		Properties properties = PrefsUtil.getPrefs(project, "default",
				"deployment");
		JSONObject data = new JSONObject();
		data.put("revision", REV_WORKING);
		JSONArray array = new JSONArray();
		for (Map.Entry<?, ?> entry : properties.entrySet()) {
			String key = String.valueOf(entry.getKey());
			if (key.startsWith("revision.")) {
				JSONObject item = new JSONObject();
				item.put("rev", key.replace("revision.", ""));
				item.put("label", entry.getValue());
				array.put(item);
			} else {
				data.put(key, entry.getValue());
			}
		}

		File projectDir = Application.getWorkspaceFile(project);
		if (SCM.isVersioned(projectDir)) {
			JSONObject obj = new JSONObject();
			obj.put("rev", REV_HEAD);
			obj.put("label", "版本库中最新版本");
			array.put(obj);
		} else {
			array = new JSONArray();
		}
		JSONObject obj = new JSONObject();
		obj.put("rev", REV_WORKING);
		obj.put("label", "工作区中最新版本");
		array.put(obj);

		JSONObject result = new JSONObject();
		result.put("revisions", array);
		result.put("data", data);
		result.put("success", true);
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	}

	public String loadInstallPrefs(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return loadDeployPrefs(request, response);
	}

	private List<File> extractRootFiles(File theFile, File targetDir)
			throws IOException {
		if (!theFile.exists()) {
			throw new IOException(theFile.getAbsolutePath() + " does not exist");
		}
		ArrayList<File> list = new ArrayList<File>();
		ZipFile zipFile;
		zipFile = new ZipFile(theFile);
		for (Enumeration<?> entries = zipFile.entries(); entries
				.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			File file = new File(targetDir, File.separator + entry.getName());
			if (!entry.isDirectory() && file.getParentFile().exists()) {
				list.add(file);
				FileUtil.copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(file)));
			}
		}
		zipFile.close();
		return list;
	}

	private List<File> getAllUnitFiles(String path) throws Exception {
		List<File> unitsFileList = new ArrayList<File>();
		// 编译ADP工程
		ADPServiceProjectBuilder adpProject = new ADPServiceProjectBuilder();
		File rootFile = Application.getWorkspaceFile(path);
		if (rootFile.exists()) {
			File proxyDir = new File(rootFile, "server");
			File engineDir = new File(rootFile, "engine");
			File clientDir = new File(rootFile, "client");
			if (proxyDir.exists()) {
				File[] units = proxyDir.listFiles();
				if (units.length > 0)
					for (File unit : units) {
						if (unit.isDirectory()
								&& !unit.getName().endsWith(".svn"))
							unitsFileList.add(unit);
					}
			}
			if (engineDir.exists()) {
				adpProject.buildAll(engineDir.getAbsolutePath());
				File[] units = engineDir.listFiles();
				if (units.length > 0)
					for (File unit : units) {
						if (unit.isDirectory()
								&& !unit.getName().endsWith(".svn"))
							unitsFileList.add(unit);
					}
			}
			if (clientDir.exists()) {
				File[] units = clientDir.listFiles();
				if (units.length > 0)
					for (File unit : units) {
						if (unit.isDirectory()
								&& !unit.getName().endsWith(".svn"))
							unitsFileList.add(unit);
					}
			}

			if (!proxyDir.exists() && !engineDir.exists()
					&& !clientDir.exists()) {
				File[] units = rootFile.listFiles();
				if (units.length > 0)
					for (File unit : units) {
						if (unit.isDirectory()
								&& !unit.getName().endsWith(".svn")) {
							adpProject.buildAll(unit.getAbsolutePath());
							unitsFileList.add(unit);
						}
					}
			}
		}
		return unitsFileList;
	}

	@SuppressWarnings("unchecked")
	private JSONObject changeDepence(JSONObject items, File compJbiFile)
			throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(compJbiFile);
		Element rootEl = doc.getRootElement();
		Element saEl = rootEl.getChild("component", rootEl.getNamespace());
		Element identEl = saEl
				.getChild("identification", rootEl.getNamespace());
		String text = identEl.getChildText("name", rootEl.getNamespace());
		String desc = this.getJbiDescription(doc);
		if (desc != null && !"".equals(desc)) {
			text = desc + "[" + text + "]";
		}

		List libList = new ArrayList();
		List sharelibList = rootEl.getChildren("shared-library", rootEl
				.getNamespace());
		if (sharelibList != null && !sharelibList.isEmpty()) {
			for (Iterator itr = sharelibList.iterator(); itr.hasNext();) {
				Element sharelibEl = (Element) itr.next();
				Element slIdentEl = sharelibEl.getChild("identification",
						rootEl.getNamespace());
				String libName = slIdentEl.getChildText("name", rootEl
						.getNamespace());
				String libDesc = slIdentEl.getChildText("description", rootEl
						.getNamespace());
				if (libDesc != null && !"".equals(libDesc))
					libName = libDesc + "[" + libName + "]";
				libList.add(libName);
			}
		}

		JSONArray compsArray = items.optJSONArray("comps");
		JSONArray libsArray = items.optJSONArray("libs");
		if (compsArray.length() > 0)
			for (int i = 0; i < compsArray.length(); i++) {
				JSONObject jso = compsArray.optJSONObject(i);
				if (text.equals(jso.optString("desc"))) {
					jso.put("checked", true);
					if (libsArray.length() > 0)
						for (int j = 0; j < libsArray.length(); j++) {
							JSONObject libJso = libsArray.getJSONObject(j);
							String libText = libJso.optString("desc");
							if (libList.contains(libText))
								libJso.put("checked", true);
						}
				}
			}
		return items;
	}

	// 获取项目资源树
	public String getProjects(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String check = request.getParameter("checked");
		boolean checked = ("".equals(path)) ? true : Boolean
				.parseBoolean(check);
		String depth = request.getParameter("depth");
		int depthCount = Integer.parseInt(depth);
		JSONArray result = new JSONArray();
		SAXBuilder builder = new SAXBuilder();

		switch (depthCount) {
		case 0: // 读取项目
			File workArea = Application.getWorkspaceRoot();
			if (workArea.exists()) {
				File[] projectList = workArea.listFiles();
				for (File project : projectList) {
					JSONObject item = new JSONObject();
					if (project.isDirectory()) {
						File jbiFile = new File(project, "jbi.xml");
						if (jbiFile.exists()) {
							Document doc = builder.build(jbiFile);
							Element rootEl = doc.getRootElement();
							Element saEl = rootEl.getChild("service-assembly",
									rootEl.getNamespace());
							Element identEl = saEl.getChild("identification",
									rootEl.getNamespace());
							String desc = identEl.getChildText("description",
									rootEl.getNamespace());
							String text = desc + "[" + project.getName() + "]";
							item.put("text", text);
							item.put("name", project.getName());
							item.put("checked", checked);
							item.put("leaf", false);
							result.put(item);
						}
					}
				}
			}
			break;
		case 1: // 读取项目下服务单元
			File projectDir = Application.getWorkspaceFile(path);
			if (projectDir.exists() && projectDir.isDirectory()) {
				File[] unitList = projectDir.listFiles();
				for (File unitDir : unitList) {
					if (unitDir.isDirectory()) {
						if (unitDir.listFiles().length > 0) {
							JSONObject item = new JSONObject();
							String text = null;
							if ("server".equals(unitDir.getName())) {
								text = "代理服务单元";
							} else if ("engine".equals(unitDir.getName())) {
								text = "引擎服务单元";
							} else if ("client".equals(unitDir.getName())) {
								text = "远程服务单元";
							} else {
								continue;
							}

							item.put("text", text);
							item.put("name", unitDir.getName());
							item.put("checked", checked);
							item.put("leaf", false);
							result.put(item);
						}
					}
				}
			}
			break;
		case 2: // 读取具体服务
			File unitDir = Application.getWorkspaceFile(path);
			if (unitDir.exists() && unitDir.isDirectory()) {
				File[] unitFiles = unitDir.listFiles();
				for (File unitFolder : unitFiles) {
					if (unitFolder.isDirectory()) {
						if (unitFolder.listFiles().length > 0) {
							JSONObject item = new JSONObject();
							String text = null;
							File unitFile = new File(unitFolder, "unit.wsdl");
							if (!unitFile.exists())
								continue;
							WSDLReader reader = WSDLUtil.getWSDLReader();
							Definition wsdlDef = reader
									.readWSDL(new WSDLLocatorImpl(unitFile,
											true));
							text = DOMUtil.getElementText(wsdlDef
									.getDocumentationElement())
									+ "[" + unitFolder.getName() + "]";
							item.put("text", text);
							item.put("name", unitFolder.getName());
							item.put("checked", checked);
							item.put("leaf", true);
							result.put(item);
						}
					}
				}
			}
			break;
		default:
			break;
		}

		response.setContentType("text/json;charset=utf-8");
		return result.toString();
	}

	// 获取资源文件
	@SuppressWarnings("unchecked")
	public String loadWarehouse(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		JSONArray compRS = new JSONArray();
		JSONArray libRS = new JSONArray();
		JSONArray transRS = new JSONArray();

		File warehouseDir = Configuration.getWarehouseRoot();
		SAXBuilder builder = new SAXBuilder();
		if (warehouseDir.exists()) {
			File componentsDir = Configuration.getWarehouseFile("components");
			File shareLib = Configuration.getWarehouseFile("lib");
			File transport = Configuration.getWarehouseFile("transport");

			// 组件
			if (componentsDir.exists()) {
				File[] comps = componentsDir.listFiles();
				if (comps.length > 0) {
					for (File comp : comps) {
						String filename = comp.getName();
						if (comp.getName().startsWith("."))
							continue;
						if (comp.isDirectory()) {
							JSONObject jso = new JSONObject();
							jso.put("desc", filename);
							File jbiFile = new File(comp, "jbi.xml");
							if (jbiFile.exists()) {
								String desc = this.getJbiDescription(builder
										.build(jbiFile));
								if (desc != null && !"".equals(desc))
									jso
											.put("desc", desc + "[" + filename
													+ "]");
								jso.put("checked", false);
								jso.put("path", "components/" + filename);
								compRS.put(jso);
							}
						}
					}
				}
			}

			// 共享库
			if (shareLib.exists()) {
				File[] libs = shareLib.listFiles();
				if (libs.length > 0) {
					for (File lib : libs) {
						String filename = lib.getName();
						if (lib.getName().startsWith("."))
							continue;
						if (lib.isDirectory()) {
							JSONObject jso = new JSONObject();
							int lastIndex = filename.lastIndexOf(".");
							jso.put("desc", lastIndex > 0 ? filename.substring(
									0, lastIndex) : filename);
							File jbiFile = new File(lib, "jbi.xml");
							if (jbiFile.exists()) {
								String desc = this.getJbiDescription(builder
										.build(jbiFile));
								if (desc != null && !"".equals(desc))
									jso
											.put("desc", desc + "[" + filename
													+ "]");
								jso.put("checked", false);
								jso.put("path", "lib/" + filename);
								libRS.put(jso);
							}
						}
					}
				}
			}

			// 传输端子	
			if (transport.exists()) {
				File[] transports = transport.listFiles();
				if (transports.length > 0) {
					for (File trans : transports) {
						if (trans.getName().startsWith("."))
							continue;
						if (trans.isFile()
								&& (trans.getName().endsWith(".jar"))) {
							JSONObject item = new JSONObject();
							String desc = null, filename = trans.getName();
							int lastIndex = filename.lastIndexOf(".");
							item.put("desc", lastIndex > 0 ? filename
									.substring(0, lastIndex) : filename);
							JarFile jar = new JarFile(trans);
							try {
								desc = this.getDescription(jar);
							} finally {
								jar.close();
							}
							if (desc != null)
								item.put("desc", desc + "[" + filename + "]");
							item.put("checked", true);
							item.put("path", "transport/" + filename);
							transRS.put(item);
						}
					}
				}
			}
		}

		JSONObject items = new JSONObject();
		items.put("comps", compRS);
		items.put("libs", libRS);
		items.put("trans", transRS);

		// TODO 分析服务单元引用的组件以及共享库
		String dataStr = request.getParameter("data");
		if (!"".equals(dataStr)) {
			List<File> untiList = new ArrayList<File>();
			List<File> listByPro = new ArrayList<File>();
			List<File> listByType = new ArrayList<File>();
			JSONArray data = new JSONArray(dataStr);
			if (data.length() > 0)
				for (int i = 0; i < data.length(); i++) {
					JSONObject jso = data.getJSONObject(i);
					int depth = jso.optInt("depth");
					String path = jso.optString("path");
					String name = jso.optString("name");

					switch (depth) {
					case 1: // 项目
						listByPro = getAllUnitFiles(path);
						if (!listByPro.isEmpty())
							for (Iterator itr = listByPro.iterator(); itr
									.hasNext();) {
								untiList.add((File) itr.next());
							}
						break;
					case 2: // 服务分类
						listByType = getAllUnitFiles(path);
						if (!listByType.isEmpty())
							for (Iterator itr = listByType.iterator(); itr
									.hasNext();) {
								untiList.add((File) itr.next());
							}
						break;
					case 3: // 服务单元
						untiList.add(Application.getWorkspaceFile(path));
						break;
					default:
						break;
					}
				}

			if (!untiList.isEmpty()) {
				for (Iterator itr = untiList.iterator(); itr.hasNext();) {
					File unitFolder = (File) itr.next();
					File jbiFile = new File(unitFolder, "jbi.xml");
					if (!jbiFile.exists())
						continue;
					Document doc = builder.build(jbiFile);
					String componentPath = doc.getRootElement().getNamespace(
							"comp").getURI().toString();
					File compFolder = Configuration
							.getWarehouseFile(componentPath);
					if (!compFolder.exists())
						continue;
					File compJbiFile = new File(compFolder, "jbi.xml");
					if (!compJbiFile.exists())
						continue;
					items = changeDepence(items, compJbiFile);
				}
			}
		}

		response.setContentType("text/json;charset=utf-8");
		return items.toString();
	}

	// 生成部署文件
	@SuppressWarnings("unchecked")
	public String finishDeploy(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String unitData = request.getParameter("unitData");
		String resourceData = request.getParameter("resourceData");
		String deployData = request.getParameter("deployData");
		JSONObject result = new JSONObject();
		ADPServiceProjectBuilder adpProject = new ADPServiceProjectBuilder();

		if (deployData != null && !"".equals(deployData)) {
			JSONObject deployJSO = new JSONObject(deployData);
			// 是否压缩
			String compress = deployJSO.optString("comporess");
			// 是否提供下载
			String download = deployJSO.optString("download");
			//  是否生成部署报告
			String report = deployJSO.optString("report");

			File targetFolder = new File(getServletContext().getRealPath(
					"deploy/"));
			FileUtil.deleteFile(targetFolder);
			FileUtil.buildDirectory(targetFolder);

			if (unitData != null && !"".equals(unitData)) {
				JSONArray unitJsonArray = new JSONArray(unitData);
				List<File> untiList = new ArrayList<File>();
				List<File> listByPro = new ArrayList<File>();
				List<File> listByType = new ArrayList<File>();
				if (unitJsonArray.length() > 0) {
					for (int i = 0; i < unitJsonArray.length(); i++) {
						JSONObject jso = unitJsonArray.getJSONObject(i);
						int depth = jso.optInt("depth");
						String path = jso.optString("path");
						String name = jso.optString("name");

						switch (depth) {
						case 1: // 项目
							listByPro = getAllUnitFiles(path);
							if (!listByPro.isEmpty())
								for (Iterator itr = listByPro.iterator(); itr
										.hasNext();) {
									untiList.add((File) itr.next());
								}
							break;
						case 2: // 服务分类
							listByType = getAllUnitFiles(path);
							if (!listByType.isEmpty())
								for (Iterator itr = listByType.iterator(); itr
										.hasNext();) {
									untiList.add((File) itr.next());
								}
							break;
						case 3: // 服务单元
							adpProject.buildAll(Application.getWorkspaceFile(
									path).getAbsolutePath());
							untiList.add(Application.getWorkspaceFile(path));
							break;
						default:
							break;
						}
					}
				}

				if (!untiList.isEmpty()) {
					for (Iterator itr = untiList.iterator(); itr.hasNext();) {
						File unitFolder = (File) itr.next();
						File projectFolder = unitFolder.getParentFile()
								.getParentFile();
						String projectName = unitFolder.getParentFile()
								.getParentFile().getName();

						//添加 schema 服务单元
						File schemaUnit = new File(projectFolder, "schema");
						if (schemaUnit.exists() && schemaUnit.list().length > 0) {
							File newProjectFolder = new File(targetFolder
									.getAbsolutePath()
									+ "/deploy/" + projectName);
							if (!newProjectFolder.exists())
								newProjectFolder.mkdirs();
							File outFile = new File(newProjectFolder
									.getAbsolutePath()
									+ File.separator
									+ schemaUnit.getName()
									+ ".zip");
							if (!outFile.exists()) {
								this.zipFile(outFile, schemaUnit, compress);
							}
						}

						//更新项目的部署描述符
						File sourceMetaFolder = new File(projectFolder,
								"META-INF");
						String destMetaFolder = targetFolder.getAbsolutePath()
								+ "/deploy/" + projectName + "/META-INF";
						File destMetaFolderFile = new File(destMetaFolder);
						if (!destMetaFolderFile.exists()) {
							DirectoryCopy.copyDirectory(sourceMetaFolder
									.getAbsolutePath(), destMetaFolder);
							File jbiFile = new File(destMetaFolderFile.getParent(),
									"jbi.xml");
							if (jbiFile.exists()) {
								updateDescriptor(jbiFile);
								updateSingleDescriptor(jbiFile, schemaUnit); // 更新schema描述
							}
						}

						// 打包服务单元到指定目录
						File outFile = new File(targetFolder.getAbsolutePath()
								+ "/deploy/" + projectName + File.separator
								+ unitFolder.getName() + ".zip");
						this.zipFile(outFile, unitFolder, compress);

						File jbiFile = new File(destMetaFolderFile.getParent(), "jbi.xml");
						updateSingleDescriptor(jbiFile, unitFolder);
					}
				}

				// 打包项目
				File deployFolder = new File(targetFolder.getAbsolutePath(),
						"deploy");
				File[] projectList = deployFolder.listFiles();
				if (projectList.length > 0) {
					for (File projectFolder : projectList) {
						if (!projectFolder.isDirectory())
							continue;
						File sourceFolder = new File(deployFolder
								.getAbsoluteFile()
								+ File.separator + projectFolder.getName());
						File outFile = new File(deployFolder.getAbsoluteFile()
								+ File.separator + projectFolder.getName()
								+ "-sa.zip");
						this.zipFile(outFile, sourceFolder, compress);
						// 打包完成之后删除原目录
						FileUtil.deleteFile(projectFolder);
					}
				}
			}

			// 创建资源文件
			if (resourceData != null && !"".equals(resourceData)) {
				JSONObject resourceJSO = new JSONObject(resourceData);
				JSONArray compArray = resourceJSO.optJSONArray("comp");
				JSONArray libArray = resourceJSO.optJSONArray("lib");
				JSONArray transArray = resourceJSO.optJSONArray("trans");

				File compFolder = new File(targetFolder.getAbsolutePath()
						+ "/intall/components");
				File libFolder = new File(targetFolder.getAbsolutePath()
						+ "/intall/lib");
				File transFolder = new File(targetFolder.getAbsolutePath()
						+ "/intall/transports");

				if (!compFolder.exists())
					compFolder.mkdirs();
				if (!libFolder.exists())
					libFolder.mkdirs();
				if (!transFolder.exists())
					transFolder.mkdirs();

				if (compArray.length() > 0)
					for (int i = 0; i < compArray.length(); i++) {
						JSONObject jso = compArray.getJSONObject(i);
						File folder = Configuration.getWarehouseFile(jso
								.optString("path"));
						File outFile = new File(compFolder.getAbsolutePath()
								+ File.separator + folder.getName() + ".jar");
						this.zipFile(outFile, folder, compress);
					}

				if (libArray.length() > 0)
					for (int i = 0; i < libArray.length(); i++) {
						JSONObject jso = libArray.getJSONObject(i);
						File folder = Configuration.getWarehouseFile(jso
								.optString("path"));
						File outFile = new File(libFolder.getAbsolutePath()
								+ File.separator + folder.getName() + ".zip");
						this.zipFile(outFile, folder, compress);
					}

				if (transArray.length() > 0)
					for (int i = 0; i < transArray.length(); i++) {
						JSONObject jso = transArray.getJSONObject(i);
						String fileName = jso.optString("path").replaceAll(
								".*\\/", "");
						File newFile = new File(transFolder, fileName);
						File sourceFile = Configuration.getWarehouseFile(jso
								.optString("path"));
						FileUtil.copyFile(sourceFile, newFile);
					}
			}

			// 创建部署报告
			if ("on".equals(report)) {
				File configFolder = new File(targetFolder.getAbsolutePath()
						+ "/config");
				if (!configFolder.exists())
					configFolder.mkdir();

				DeployReportUtil dru = new DeployReportUtil(configFolder
						.getAbsolutePath()
						+ "/sesameConfig.pdf");
				dru.buildReport();
			}

			// 最后打包提供下载
			File downloadFolder = new File(getServletContext().getRealPath(
					"download/"));
			FileUtil.deleteFile(downloadFolder);
			FileUtil.buildDirectory(downloadFolder);

			File outFile = new File(downloadFolder + File.separator
					+ "deploy.zip");
			this.zipFile(outFile, targetFolder, compress);

			StringBuffer buffer = new StringBuffer();
			buffer.append("<div><span><b style='color:blue;'>部署成功!</b></span><br><br>您可以选择点击下面的链接下载部署文件 或点击<确定>完成部署</div>");
			buffer.append("<div><a href=\"" + request.getContextPath()
					+ "/download/" + outFile.getName()
					+ "\"><img src=\"../images/tool16/down.gif\"></img>下载 "
					+ outFile.getName() + "</a></div>");
			result.put("success", true);
			result.put("html", buffer.toString());
		}

		response.setContentType("text/json;charset=utf-8");
		return result.toString();
	}

	// 打包文件到指定目录
	private void zipFile(File outFile, File sourceFolder, String compress)
			throws Exception {
		FileOutputStream fileOutput = new FileOutputStream(outFile);
		CheckedOutputStream csum = new CheckedOutputStream(fileOutput,
				new CRC32());
		ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(
				csum));
		try {
			if (compress == null || "off".equals(compress))
				zipOut.setLevel(0);
			zipOut.setComment("Sanxing Sesame Service-Unit");
			for (File file : sourceFolder.listFiles())
				zipUnit(file, "", zipOut);
		} catch (Exception e) {
			throw new Exception("打包文件" + outFile.getName() + "失败!");
		} finally {
			zipOut.close();
		}
	}

	//获取可以导出的服务单元
	public String getUnits(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String version = request.getParameter("revision");
		String node = request.getParameter("node");
		String path = request.getParameter("path");
		String check = request.getParameter("checked");
		boolean checked = check != null && check.equals("true");

		String project = this.getProject(path);
		File buildPath = Application.getWorkspaceRoot();
		long revision = 0;
		if (!version.equals(REV_WORKING)) {
			buildPath = new File(getServletContext().getRealPath("temp/"));
			File projectPath = new File(buildPath, project);
			ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(project);
			long rev = version.equals(REV_HEAD) ? 0 : Long.parseLong(version);
			if (!synchronizer.isVersioned(projectPath)) {
				if (projectPath.exists())
					FileUtil.deleteFile(projectPath);
				revision = synchronizer.checkout(project, projectPath, rev);
			} else {
				revision = synchronizer.update(projectPath, rev);
			}
		}

		JSONArray result = new JSONArray();
		File folder = new File(buildPath, path);
		log.debug(folder);
		if (folder.exists()) {
			String regex = request.getParameter("filter");
			Pattern pattern = regex != null && regex.length() > 0 ? Pattern
					.compile(regex, Pattern.CASE_INSENSITIVE) : null;

			SAXBuilder builder = new SAXBuilder();
			File[] files = folder.listFiles();
			for (int j = 0; j < files.length; j++) {
				File entry = files[j];
				if (entry.getName().startsWith(".")) {
					continue;
				}
				if (entry.isDirectory()
						&& (pattern == null || pattern.matcher(entry.getName())
								.matches())) {
					JSONObject item = new JSONObject();
					String desc = null, filename = entry.getName();
					int lastIndex = filename.lastIndexOf(".");
					item.put("text", lastIndex > 0 ? filename.substring(0,
							lastIndex) : filename);
					try {
						File unitFile = new File(entry, "unit.wsdl");
						desc = this.getDocumentation(builder.build(unitFile));
					} catch (Exception e) {
					}
					if (desc != null)
						item.put("text", desc + "[" + filename + "]");
					item.put("name", entry.getName());
					item.put("checked", checked);
					item.put("leaf", true);
					result.put(item);
				}
			}
		}
		return result.toString();
	}

	//获取可以安装的组件
	public String getComps(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		//String path = request.getParameter("path");
		String check = request.getParameter("checked");
		boolean checked = check != null && check.equals("true");
		JSONArray result = new JSONArray();
		File folder = Configuration.getWarehouseFile("components");//  TODO  //Configuration.getWorkspaceFile(project+"/component");
		if (folder.exists()) {
			String regex = request.getParameter("filter");
			Pattern pattern = regex != null && regex.length() > 0 ? Pattern
					.compile(regex, Pattern.CASE_INSENSITIVE) : null;

			SAXBuilder builder = new SAXBuilder();
			File[] files = folder.listFiles();
			for (int j = 0; j < files.length; j++) {
				File entry = files[j];
				if (entry.getName().startsWith(".")) {
					continue;
				}
				if (entry.isDirectory()
						&& (pattern == null || pattern.matcher(entry.getName())
								.matches())) {
					JSONObject item = new JSONObject();
					String desc = null, filename = entry.getName();
					int lastIndex = filename.lastIndexOf(".");
					item.put("text", lastIndex > 0 ? filename.substring(0,
							lastIndex) : filename);
					try {
						File jbiFile = new File(entry, "jbi.xml");
						desc = this.getJbiDescription(builder.build(jbiFile));
					} catch (Exception e) {
					}
					if (desc != null)
						item.put("text", desc + "[" + filename + "]");
					item.put("name", entry.getName());
					item.put("checked", checked);
					item.put("leaf", true);
					result.put(item);
				}
			}
		}
		return result.toString();
	}

	//获取可以安装的共享库
	public String getLibs(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String check = request.getParameter("checked");
		boolean checked = check != null && check.equals("true");
		JSONArray result = new JSONArray();
		File folder = Configuration.getWarehouseFile("lib");// TODO  //Configuration.getWorkspaceFile(project+"/lib");
		if (folder.exists()) {
			String regex = request.getParameter("filter");
			Pattern pattern = regex != null && regex.length() > 0 ? Pattern
					.compile(regex, Pattern.CASE_INSENSITIVE) : null;

			SAXBuilder builder = new SAXBuilder();
			File[] files = folder.listFiles();
			for (int j = 0; j < files.length; j++) {
				File entry = files[j];
				if (entry.getName().startsWith(".")) {
					continue;
				}
				if (entry.isDirectory()
						&& (pattern == null || pattern.matcher(entry.getName())
								.matches())) {
					JSONObject item = new JSONObject();
					String desc = null, filename = entry.getName();
					int lastIndex = filename.lastIndexOf(".");
					item.put("text", lastIndex > 0 ? filename.substring(0,
							lastIndex) : filename);
					try {
						File jbiFile = new File(entry, "jbi.xml");
						desc = this.getJbiDescription(builder.build(jbiFile));
					} catch (Exception e) {
					}
					if (desc != null)
						item.put("text", desc + "[" + filename + "]");
					item.put("name", entry.getName());
					item.put("checked", checked);
					item.put("leaf", true);
					result.put(item);
				}
			}
		}
		return result.toString();
	}

	//获取可以安装的传输库
	public String getTransports(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String check = request.getParameter("checked");
		boolean checked = check != null && check.equals("true");
		JSONArray result = new JSONArray();
		File folder = Configuration.getWarehouseFile("transport"); // TODO //Configuration.getWorkspaceFile(project+"/transport");
		if (folder.exists()) {
			String regex = request.getParameter("filter");
			Pattern pattern = regex != null && regex.length() > 0 ? Pattern
					.compile(regex, Pattern.CASE_INSENSITIVE) : null;

			SAXBuilder builder = new SAXBuilder();
			File[] files = folder.listFiles();
			for (int j = 0; j < files.length; j++) {
				File entry = files[j];
				if (entry.getName().startsWith(".")) {
					continue;
				}
				if (entry.isFile() && (entry.getName().endsWith(".jar"))) {
					JSONObject item = new JSONObject();
					String desc = null, filename = entry.getName();
					int lastIndex = filename.lastIndexOf(".");
					item.put("text", lastIndex > 0 ? filename.substring(0,
							lastIndex) : filename);
					JarFile jar = new JarFile(entry);
					try {
						desc = this.getDescription(jar);
					} finally {
						jar.close();
					}
					if (desc != null)
						item.put("text", desc + "[" + filename + "]");
					item.put("name", entry.getName());
					item.put("checked", checked);
					item.put("leaf", true);
					result.put(item);
				}
			}
		}
		return result.toString();
	}
	
	//卸载组件、共享库或者传输端子
	@SuppressWarnings("unchecked")
	public String uninstall(HttpServletRequest request, HttpServletResponse response) throws Exception {

		File root = Application.getWarehouseRoot();
		//获取页面上用户选择的组件
		JSONArray selected = new JSONArray(request.getParameter("selected"));
		List<File> list = new ArrayList<File>();
		for (int i = 0, len = selected.length(); i < len; i++) {
			String path = selected.getString(i);
			File entry = new File(root, path);
			list.add(entry);
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<div>卸载成功</div>");

		//从本地运行环境中卸载
		MBeanServer mbeanServer = (MBeanServer) getServletContext()
				.getAttribute("MBeanServer");
		//MBeanServer mbeanServer = Platform.getLocalMBeanServer();
		if (mbeanServer != null) {
			ObjectName objName = new ObjectName(
					":Type=SystemService,Name=CommandsService,*");
			Set<ObjectInstance> set = mbeanServer.queryMBeans(objName, null);

			if (set.isEmpty()) {
				throw new Exception("没找到部署服务");
			} 
			else {
				ObjectInstance instance = set.iterator().next();
				for (File file : list) {
					Object[] params = new Object[] { file.getName() };
					String[] signature = new String[] { "java.lang.String" };
					try {
						String parentName = file.getParentFile().getName();
						String operationName = parentName.equals("components") ? "uninstallComponent" : "uninstallSharedLibrary";
						Object result = mbeanServer.invoke(instance.getObjectName(), operationName, 
								params, signature);
						log.debug(result);
					} 
					catch (MBeanException e) {
						if (e.getCause() != null) {
							throw (Exception) e.getCause();
						} 
						else {
							throw e;
						}
					}
				}
			}

		} 
		else {
			throw new Exception("Sesame Studio 单独运行时不能使用此功能");
		}
		//返回下载链接
		response.setContentType("text/plain; charset=utf-8");
		return buffer.toString();

	}%>

<%
	String operation = request.getParameter("operation");
	if (operation == null) {
		operation = request.getMethod().toLowerCase();
	}
	
	HttpServletRequest webRequest = new WebServletRequest(request);
	HttpServletResponse webResponse = new WebServletResponse(response);
	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[] {
				HttpServletRequest.class, HttpServletResponse.class });
		String result = (String) method.invoke(this, new Object[] { webRequest, webResponse });
		out.clear();
		out.println(result);
	} 
	catch (NoSuchMethodException e) {
		throw new ServletException("[" + request.getMethod() + "]找不到相应的方法来处理指定的 operation: " + operation);
	}
	catch (Exception e) {
		Throwable t = this.getDeepCause(e);
		throw new ServletException(t.getMessage(), t);
	}
%>