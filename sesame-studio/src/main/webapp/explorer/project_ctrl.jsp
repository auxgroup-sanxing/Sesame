<%@page import="com.sanxing.sesame.pwd.PasswordTool"%>
<%@page import="com.sanxing.studio.IllegalNameException"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.util.*,java.util.jar.*,java.util.regex.*"%>
<%@page import="java.util.zip.*"%>
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

<%@page import="org.apache.commons.fileupload.*"%>
<%@page import="org.apache.commons.fileupload.disk.*"%>
<%@page import="org.apache.commons.fileupload.servlet.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.jdom.*,org.jdom.input.*,org.jdom.output.*,org.jdom.xpath.*"%>
<%@page import="org.json.*"%>
<%@page import="com.sanxing.adp.eclipse.ADPServiceProjectBuilder"%>
<%@page import="com.sanxing.sesame.sharelib.ShareLibManager"%>

<%@page language="java" contentType="text/json; charset=utf-8" pageEncoding="utf-8"%>

<%!private static final Namespace XMLNS_ART = Namespace.getNamespace("sn", Namespaces.STATENET);

	private static final String REV_WORKING	= "working";
	private static final String REV_HEAD	= "head";
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private void createUnitDescriptor(File unitFolder, String component, boolean binding) 
			throws FileNotFoundException,  IOException
	{
		Namespace xmlns = Namespace.getNamespace(Namespaces.JBI);
		Namespace xmlns_comp = Namespace.getNamespace("comp", component);
		Element rootEl = new Element("jbi", xmlns);
		rootEl.setAttribute("version", "1.0");
		rootEl.addNamespaceDeclaration(xmlns_comp);
		Element servicesEl = new Element("services", xmlns);
		rootEl.addContent(servicesEl);
		servicesEl.setAttribute("binding-component", String.valueOf(binding));
		OutputStream outStream = new FileOutputStream(new File(unitFolder, "jbi.xml"));
		try {
			JdomUtil.getPrettyOutputter().output(new Document(rootEl), outStream);
		}
		finally {
			outStream.close();
		}
	}

	private org.w3c.dom.Element createDocumentationEl(org.w3c.dom.Document doc, String documentation) {
		org.w3c.dom.Element element = doc.createElementNS(Namespaces.WSDL1_NAMESPACE, "documentation");
		org.w3c.dom.Text textNode = doc.createTextNode(documentation);
		element.appendChild(textNode);
		return element;
	}

	private org.w3c.dom.Element createSchemaEl(org.w3c.dom.Document doc, String targetNamespace) {
		org.w3c.dom.Element schemaEl = doc.createElementNS(Namespaces.XSD, "schema");
		schemaEl.setPrefix("xs");
		schemaEl.setAttribute("attributeFormDefault", "unqualified");
		schemaEl.setAttribute("elementFormDefault", "qualified");
		schemaEl.setAttribute("targetNamespace", targetNamespace);
		return schemaEl;
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
			for (Iterator<?> iter = rootEl.getContent().iterator(); iter.hasNext();) {
				Content content = (Content) iter.next();
				if (content instanceof Comment)
					return ((Comment) content).getText();
				if (content instanceof Text)
					continue;
				break;
			}

			Element annoEl = rootEl.getChild("annotation", rootEl.getNamespace());
			if (annoEl != null) {
				return annoEl.getChildText("documentation", rootEl.getNamespace());
			}
			else {
				return rootEl.getChildText("documentation", rootEl.getNamespace());
			}
		}
		catch (Exception e) {
			return null;
		}
	}
	//获取服务单元的组件名
	private String getComponentName(File unit) throws IOException, JDOMException {
		File jbiFile = new File(unit, "jbi.xml");
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(jbiFile);
		Element rootEl = document.getRootElement();
		Namespace xmlns_comp = rootEl.getNamespace("comp");
		//Element servicesEl = rootEl.getChild("services", rootEl.getNamespace());
		if (xmlns_comp != null) {
			String path = xmlns_comp.getURI();
			File compBundle = new File(unit.getParentFile(), path);
			try {
				if (compBundle.isDirectory()) {
					document = builder.build(new File(compBundle, "jbi.xml"));
				}
				else {
					JarFile jar = new JarFile(compBundle);
					try {
						JarEntry entry = jar.getJarEntry("jbi.xml");
						document = builder.build(jar.getInputStream(entry));
					} 
					finally { 
						jar.close();
					}
				}
				rootEl = document.getRootElement();
				Element idenEl, compEl = rootEl.getChild("component", rootEl.getNamespace());
				if (compEl != null && (idenEl = compEl.getChild("identification", rootEl.getNamespace())) != null) {
					return idenEl.getChildText("name", rootEl.getNamespace());
				}
			}
			catch (IOException e) {
				throw new RuntimeException(e.getMessage()+" "+compBundle, e);
			}
		}
		return null;
	}

	private String getJbiDescription(Document jbiDoc) {
		try {
			Element rootEl = jbiDoc.getRootElement();
			List<?> list = rootEl.getChildren();
			if (list.size() > 0) {
				Element idenEl, firstEl = (Element)list.get(0);
				if ((idenEl = firstEl.getChild("identification", rootEl.getNamespace())) != null) {
					return idenEl.getChildText("description", rootEl.getNamespace());
				}
			}
		}
		catch (Exception e) {
		}
		return null;
	}

	private String getDescription(JarFile jarFile) throws IOException {
		JarEntry entry = jarFile.getJarEntry("jbi.xml");
		if (entry != null) {
			try {
				Document document = new SAXBuilder().build(jarFile.getInputStream(entry));
				Element rootEl = document.getRootElement();
				Element idenEl, compEl = rootEl.getChild("component", rootEl.getNamespace());

				if (compEl != null && (idenEl = compEl.getChild("identification", rootEl.getNamespace())) != null) {
					return idenEl.getChildText("description", rootEl.getNamespace());
				}
			}
			catch (Exception e) {
			}
		}
		Attributes attrs = jarFile.getManifest().getMainAttributes();
		return attrs.getValue("Specification-Title");
	}
	
	private String getProject(String path) {
		int n = path.indexOf("/");
		return n > 0 ? path.substring(0, n) : path;
	}
	
	private void validateUnitName(String unitName) {
		File root = Application.getWorkspaceRoot();
		File[] list = root.listFiles();
		for (int i=0; i<list.length; i++) {
			File project = list[i];
			File unitFolderEngine = new File(project, "engine/" + unitName);
			File unitFolderServer = new File(project, "server/" + unitName);
			File unitFolderClient = new File(project, "client/" + unitName);
			if (unitFolderEngine.exists() || unitFolderServer.exists() || unitFolderClient.exists()) {
				throw new IllegalNameException("服务单元名称不能重复，请重新指定");
			}
		}
	}
	
	private void unpackTemplate(File template, File unitFolder, String desc, String serviceName, String targetNamespace) 
		throws JDOMException, IOException
	{
		FileUtil.unpackArchive(template, unitFolder);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xsd");
			}
		};
		File[] schemas = unitFolder.listFiles(filter);
		if (schemas.length > 0) {
			SAXBuilder builder = new SAXBuilder();
			XMLOutputter outputter = JdomUtil.getPrettyOutputter();
			for (int i=0; i<schemas.length; i++) {
				File schemaFile = schemas[i];
				Document document = builder.build(schemaFile);
				Element schemaEl = document.getRootElement();
				schemaEl.setAttribute("targetNamespace", targetNamespace);
				schemaEl.removeNamespaceDeclaration(schemaEl.getNamespace("tns"));
				schemaEl.addNamespaceDeclaration(Namespace.getNamespace("tns", targetNamespace));
				FileOutputStream outStream = new FileOutputStream(schemaFile);
				try {
					outputter.output(document, outStream);
				}
				finally {
					outStream.close();
				}
			}
		}
		
		File unitFile = new File(unitFolder, "unit.wsdl");
		if (unitFile.exists()) {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(unitFile);
			Element definitionEl = document.getRootElement();
			definitionEl.setAttribute("name", unitFolder.getName());
			definitionEl.setAttribute("targetNamespace", targetNamespace);
			definitionEl.removeNamespaceDeclaration(definitionEl.getNamespace("tns"));
			definitionEl.addNamespaceDeclaration(Namespace.getNamespace("tns", targetNamespace));
			Element docuEl = definitionEl.getChild("documentation", definitionEl.getNamespace());
			if (docuEl != null) {
				docuEl.setText(desc);
			}
			Element serviceEl = definitionEl.getChild("service", definitionEl.getNamespace());
			if (serviceEl != null) {
				serviceEl.setAttribute("name", serviceName);
			}
			XPath xpath = XPath.newInstance("wsdl:types/xsd:schema");
			xpath.addNamespace(Namespace.getNamespace("wsdl", definitionEl.getNamespaceURI()));
			xpath.addNamespace(Namespace.getNamespace("xsd", Namespaces.XSD));
			Element schemaEl = (Element)xpath.selectSingleNode(definitionEl);
			if (schemaEl != null) {
				schemaEl.setAttribute("targetNamespace", targetNamespace);
			}
			FileOutputStream outStream = new FileOutputStream(unitFile);
			try {
				XMLOutputter outputter = JdomUtil.getPrettyOutputter();
				outputter.output(document, outStream);
			}
			finally {
				outStream.close();
			}
		}
	}
	
	//从版本库取出项目
	public String checkout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String url = request.getParameter("url");
		String username = request.getParameter("username");
		String password = request.getParameter("passwd");
		String path = url.substring(url.lastIndexOf('/')+1);
		long revision = Long.parseLong(request.getParameter("revision"));
		File wcPath = Application.getWorkspaceFile(path);
		
		if (username == null) {
			Element prefsEl = Configuration.getSCMPrefs();
			Element repoEl = (Element)XPath.selectSingleNode(prefsEl, "repository[@url='"+url+"']");
			if (repoEl != null) {
				username = repoEl.getAttributeValue("username");
				password = PasswordTool.decrypt(repoEl.getAttributeValue("password"));
			}
		}
		
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(wcPath, url, username, password);
		revision = synchronizer.checkout("", wcPath, revision);
		
		JSONObject result = new JSONObject();
		
		result.put("name", wcPath.getName());
		result.put("iconCls", "x-icon-project");
		result.put("allowRemove", true);
		result.put("revision", revision);
		result.put("status", " ");
		File propFile = new File(wcPath, "jbi.xml");
		if (propFile.exists()) {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(propFile);
			Element rootEl = doc.getRootElement();
			Element saEl = rootEl.getChild("service-assembly", rootEl.getNamespace());
			if (saEl != null) {
				Element indenEl = saEl.getChild("identification", rootEl.getNamespace());
				String projectName = indenEl.getChildText("description", rootEl.getNamespace());
				if (projectName != null) {
					result.put("desc", projectName);
					result.put("text", projectName + "[" + wcPath.getName() + "]");
				}
				Namespace ns = rootEl.getNamespace("tns");
				if (ns != null) {
					result.put("qtip", ns.getURI());
				}
			}
		}
		
		return result.toString();
	}

	public String loadEmulators(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		JSONArray result = new JSONArray();
		JSONObject item = new JSONObject();
		item.put("name", "com.sanxing.studio.emu.TcpServer");
		item.put("label", "Tcp 仿真服务器");
		result.put(item);
		item = new JSONObject();
		item.put("name", "com.sanxing.studio.emu.TcpClient");
		item.put("label", "Tcp 仿真客户端");
		result.put(item);
		return new JSONObject().put("items", result).toString();
	}

	public String loadComponents(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String type = request.getParameter("type");
		String role = request.getParameter("role");
		JSONArray result = new JSONArray();
		File compFolder = Configuration.getWarehouseFile("components");

		if (compFolder.exists()) {
			SAXBuilder builder = new SAXBuilder();
			File[] files = compFolder.listFiles();
			for (File entry : files) {
				Document document;
				File jbiFile;
				if (entry.isDirectory() && (jbiFile=new File(entry, "jbi.xml")).exists()) {
					document = builder.build(jbiFile);
				}else if (entry.getName().toLowerCase().endsWith(".jar")) {
					JarFile compJar = new JarFile(entry);
					JarEntry jbiXml = compJar.getJarEntry("jbi.xml");
					InputStream input = compJar.getInputStream(jbiXml);
					try {
						document = builder.build(input);
					}finally {
						input.close();
						compJar.close();
					}
				}else {
					continue;
				}
				
				Element rootEl = document.getRootElement();
				String label = null, name = null;
				Namespace ns = rootEl.getNamespace();
				Element compEl = rootEl.getChild("component", ns);
				if (compEl == null) continue;
				if (!compEl.getAttributeValue("type", "").equals(type)) continue;
				
				Element roleEl = compEl.getChild("role", rootEl.getNamespace("sn"));
				if (roleEl != null) {
					String roleStr = roleEl.getText();
					if (!role.equals(roleStr)) continue;
				}

				Element idenEl;
				if ((idenEl = compEl.getChild("identification", ns)) != null) {
					name = idenEl.getChildText("name", ns);
					label = idenEl.getChildText("description", ns);
				}
				
				JSONObject item = new JSONObject();
				item.put("component-name", name);
				item.put("component", "components/" + entry.getName());
				item.put("label", label+"("+name+")");
				result.put(item);
			}
		}
		return new JSONObject().put("items", result).toString();
	}
	
	// 读取引擎服务中引用的公共接口
	@SuppressWarnings("unchecked")
	private void engineImports(Element portTypeRootEl, Namespace ns, StringBuilder portTypeName, File unitWsdlFile, String project) throws Exception {
		List importList = portTypeRootEl.getChildren("import", ns);
		
		WSDLReader reader = WSDLUtil.getWSDLReader();
		if (importList != null && !importList.isEmpty()) {
			for (Iterator itr = importList.iterator(); itr.hasNext();) {
				Element importEl = (Element)itr.next();
				String importLocation = importEl.getAttributeValue("location");
				if (!"".equals(importLocation)) {
					File intfWsdlFile = Configuration.getWorkspaceFile(project + File.separator+ importLocation.replaceAll("\\..*?\\/", ""));
					if (intfWsdlFile.exists()) {
						Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfWsdlFile, true));
						Map ptMap = wsdlDef.getPortTypes();
						if (ptMap != null && !ptMap.isEmpty()) {
							Set<Map.Entry<?,?>> set = ptMap.entrySet();
							for (Iterator<?> iter=set.iterator(); iter.hasNext();) {
								Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
								PortType portType = (PortType)entry.getValue();
								List<?> operList = portType.getOperations();
								if (operList != null && !operList.isEmpty())
									portTypeName.append(portType.getQName().getLocalPart() + "@");
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public String loadEngineServices(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String type = request.getParameter("type");
		JSONArray result = new JSONArray();
		File engineFolder = this.getFile(project + "/engine");
		
		// 取得引擎服务名称
		if (engineFolder.exists()) {
			SAXBuilder builder = new SAXBuilder();
			File[] files = engineFolder.listFiles();
			for (File entry : files) {
				Document document;
				File wsdlFile;
				if (entry.isDirectory() && (wsdlFile = new File(entry, "unit.wsdl")).exists()) {
					document = builder.build(wsdlFile);
				} else  {continue;	}
				
				Element rootEl = document.getRootElement();
				Namespace ns = rootEl.getNamespace();
				String label = null, name = null, tns=null;
				StringBuilder portTypeName = new StringBuilder();
				
				name = rootEl.getAttributeValue("name");
				label = rootEl.getChildText("documentation", ns);
				
				List portTypeList = rootEl.getChildren("portType", ns);
				if (portTypeList != null && !portTypeList.isEmpty()) {
					for (Iterator itr = portTypeList.iterator(); itr.hasNext();) {
						Element portType = (Element)itr.next();
						String ptName = portType.getAttributeValue("name");
						portTypeName.append(ptName + "@");
					}
				}
				
				this.engineImports(rootEl, ns, portTypeName, wsdlFile, project);
				
				tns = rootEl.getAttributeValue("targetNamespace");
				JSONObject item = new JSONObject();
				item.put("service-name", name);
				item.put("engine-services", "../../engine/" + entry.getName() + "#" + tns + "#" + portTypeName.toString());
				item.put("label", label+"("+name+")");
				result.put(item);
			}
		}
		
		return new JSONObject().put("items", result).toString();
	}

	public String loadUnits(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String oriented = request.getParameter("oriented");
		JSONArray result = new JSONArray();
		File folder = this.getFile(project + "/"+oriented);

		if (folder.exists()) {
			SAXBuilder builder = new SAXBuilder();
			File[] files = folder.listFiles();
			for (File entry : files) {
				if (entry.isDirectory()) {
					JSONObject item = new JSONObject();
					Document document = builder.build(new File(entry, "unit.wsdl"));
					Element rootEl = document.getRootElement();
					String label = this.getDocumentation(document);
					if (label == null)	label = entry.getName();
					item.put("name", entry.getName());
					item.put("label", label);
					result.put(item);
				}
			}
		}
		return new JSONObject().put("items", result).toString();
	}

	public String createClient(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String unitName = request.getParameter("unit-name");
		String component = request.getParameter("component");
		String compName = request.getParameter("component-name");
		String initialWSDL = request.getParameter("initialWSDL");
		if (initialWSDL.contains("请输入")) 
			initialWSDL = "";
		
		File unitFolder = this.getFile(project + "/client/" + unitName);
		
		validateUnitName(unitName);
		
		if (!unitFolder.mkdir()) {
			throw new Exception("创建服务单元目录失败");
		}

		//复制组件包模板目录下的文件到新创建的服务单元目录
		File compBundle = Application.getWarehouseFile(component);
		File template = new File(compBundle, "template.zip");
		if (template.exists()) {
			String desc = request.getParameter("desc");
			String serviceName = request.getParameter("service-name");
			unpackTemplate(template, unitFolder, desc, serviceName, request.getParameter("namespace"));
		}
		
		//创建服务单元描述符
		createUnitDescriptor(unitFolder, component, true);
		
		File unitFile = new File(unitFolder, "unit.wsdl");
		if (initialWSDL != null && !"".equals(initialWSDL)) {
			// 解析导入的WSDL
			ImportWsdlUtil.WSDLAnalyser(initialWSDL, unitFolder.getAbsolutePath());
		}
		else if (unitFile.exists()) {
			return "true";
		}
		else {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document document = builder.newDocument();
	
			Definition wsdlDef = WSDLUtil.newDefinition();
			String targetNamespace = request.getParameter("namespace");
			wsdlDef.addNamespace("tns", targetNamespace);
			wsdlDef.addNamespace("sn", Namespaces.STATENET);
			wsdlDef.addNamespace("xs", Namespaces.XSD);
			wsdlDef.addNamespace("soap", Namespaces.SOAP_NAMESPACE);
			wsdlDef.addNamespace("", Namespaces.WSDL1_NAMESPACE);
			wsdlDef.setQName(new QName(targetNamespace, unitName));
			wsdlDef.setTargetNamespace(targetNamespace);
			wsdlDef.setDocumentationElement(createDocumentationEl(document, request.getParameter("desc").replaceAll("[\n]+", "")));
	
			Service service = wsdlDef.createService();
			wsdlDef.addService(service);
			service.setQName(new QName(targetNamespace, request.getParameter("service-name")));
			WSDLWriter writer = WSDLUtil.getWSDLWriter();
			FileOutputStream outStream = new FileOutputStream(new File(unitFolder, "unit.wsdl"));
			try {
				writer.writeWSDL(wsdlDef, outStream);
			}
			finally {
				outStream.close();
			}
		}
		return "true";
	}

    private List<File> extractRootFiles(File theFile, File targetDir) throws IOException {
        if (!theFile.exists()) {
            throw new IOException(theFile.getAbsolutePath() + " does not exist");
        }
        ArrayList<File> list = new ArrayList<File>();
        ZipFile zipFile;
        zipFile = new ZipFile(theFile);
        for (Enumeration<?> entries = zipFile.entries(); entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File file = new File(targetDir, File.separator + entry.getName());
            if (!entry.isDirectory() && file.getParentFile().exists()) {
            	list.add(file);
                FileUtil.copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
            }
        }
        zipFile.close();
        return list;
    }

	public String createInternalUnit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String unitName = request.getParameter("unit-name");
		String component = request.getParameter("component");
		String compName = request.getParameter("component-name");
		File unitFolder = this.getFile(project + "/engine/" + unitName);
		
		validateUnitName(unitName);
		
		if (!unitFolder.mkdir()) {
			throw new Exception("创建服务单元目录失败");
		}
		
		//复制组件包模板目录下的文件到新创建的服务单元目录
		File compBundle = Application.getWarehouseFile(component);
		File template = new File(compBundle, "template.zip");
		if (template.exists()) {
			String desc = request.getParameter("desc");
			unpackTemplate(template, unitFolder, desc, null, request.getParameter("namespace"));
		}
		//创建服务单元描述符
		createUnitDescriptor(unitFolder, component, false);
		
		File unitFile = new File(unitFolder, "unit.wsdl");
		if (unitFile.exists()) {
			return "true";
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		org.w3c.dom.Document document = builder.newDocument();

		Definition wsdlDef = WSDLUtil.newDefinition();
		String targetNamespace = request.getParameter("namespace");
		wsdlDef.addNamespace("tns", targetNamespace);
		wsdlDef.addNamespace("sn", Namespaces.STATENET);
		wsdlDef.addNamespace("xs", Namespaces.XSD);
		wsdlDef.addNamespace("soap", Namespaces.SOAP_NAMESPACE);
		wsdlDef.addNamespace("", Namespaces.WSDL1_NAMESPACE);
		wsdlDef.setQName(new QName(targetNamespace, unitName));
		wsdlDef.setTargetNamespace(targetNamespace);
		wsdlDef.setDocumentationElement(createDocumentationEl(document, request.getParameter("desc").replaceAll("[\n]+", "")));
		WSDLWriter writer = WSDLUtil.getWSDLWriter();
		FileOutputStream outStream = new FileOutputStream(unitFile);
		try {
			writer.writeWSDL(wsdlDef, outStream);
		}
		finally {
			outStream.close();
		}
		
		return "true";
	}

	@SuppressWarnings("unchecked")
	public String createProxyUnit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String unitName = request.getParameter("unit-name");
		String component = request.getParameter("component");
		String compName = request.getParameter("component-name");
		String engineService = request.getParameter("engine-services");
		
		File unitFolder = this.getFile(project + "/server/" + unitName);
		
		validateUnitName(unitName);

		if (!unitFolder.mkdir()) {
			throw new Exception("创建服务单元目录失败");
		}
		
		//复制组件包模板目录下的文件到新创建的服务单元目录
		File compBundle = Application.getWarehouseFile(component);
		File template = new File(compBundle, "template.zip");
		if (template.exists()) {
			String desc = request.getParameter("desc");
			String serviceName = request.getParameter("service-name");
			unpackTemplate(template, unitFolder, desc, serviceName, request.getParameter("namespace"));
		}
		// 读取engine wsdl
		Map ptMap = null;
		if (!"".equals(engineService)) {
			String enginePath = engineService.split("#")[0];
			File engineFile = new File(unitFolder, enginePath + "/unit.wsdl");
			Definition engineDef = WSDLUtil.getWSDLReader().readWSDL(new WSDLLocatorImpl(engineFile, true));
			Collection<PortType> portTypes = engineDef.getAllPortTypes().values();
			if (!portTypes.isEmpty()) {
				ptMap = new HashMap();
				for (PortType portType : portTypes ) {
					String key = portType.getQName().getLocalPart();
					List<Operation> operations = portType.getOperations();
					if (!operations.isEmpty()) {
						List<String> nameList = new ArrayList<String>();
						for (Operation opera : operations) {
							nameList.add(opera.getName());
						}
						ptMap.put(key, nameList);
					}
				}
			}
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		org.w3c.dom.Document document = builder.newDocument();

		Definition wsdlDef = WSDLUtil.newDefinition();
		String targetNamespace = request.getParameter("namespace");
		wsdlDef.addNamespace("tns", targetNamespace);
		wsdlDef.addNamespace("sn", Namespaces.STATENET);
		wsdlDef.addNamespace("xs", Namespaces.XSD);
		wsdlDef.addNamespace("soap", Namespaces.SOAP_NAMESPACE);
		wsdlDef.addNamespace("", Namespaces.WSDL1_NAMESPACE);
		wsdlDef.setQName(new QName(targetNamespace, unitName));
		wsdlDef.setTargetNamespace(targetNamespace);
		wsdlDef.setDocumentationElement(createDocumentationEl(document, request.getParameter("desc").replaceAll("[\n]+", "")));
		
		if (!"".equals(engineService)) {
			// 如果指定了引擎服务则创建时引入import并创建opertaion
			String enginePath = engineService.split("#")[0];
			String engine_prefix = enginePath.replaceAll(".*/", "");
			String engine_tns = engineService.split("#")[1];
			// 添加命名空间
			wsdlDef.addNamespace(engine_prefix, engine_tns);
			
			Import importEl = wsdlDef.createImport();
			importEl.setLocationURI(enginePath + "/unit.wsdl");
			importEl.setNamespaceURI(engine_tns);
			wsdlDef.addImport(importEl);
			
			// portType 列表
			String[] intfArray = engineService.split("#")[2].split("@");
			
			if (intfArray != null && intfArray.length > 0)
			for (int i=0; i<intfArray.length; i++) {
				String intfName = intfArray[i];
				if ("".equals(intfName) || intfName == null) continue;
				
				Binding binding = wsdlDef.createBinding();
				binding.setQName(new QName(targetNamespace, intfName + "-binding"));
				
				// binding type
				PortType pt = wsdlDef.createPortType();
				pt.setQName(new QName(engine_prefix + ":" + intfName));
				binding.setPortType(pt);
				
				binding.setUndefined(false);
				wsdlDef.addBinding(binding);
				
				// soap:binding
				SOAPBinding soapBinding = new SOAPBindingImpl();
				soapBinding.setStyle("document");
				soapBinding.setTransportURI("http://schemas.xmlsoap.org/soap/http");
				binding.addExtensibilityElement(soapBinding);
				
				// operation
				if (ptMap != null) {
					List operaList = (List) ptMap.get(intfName);
					if (operaList!=null && !operaList.isEmpty())
						for (Iterator itr = operaList.iterator(); itr.hasNext();) {
							String operaName = (String)itr.next();
							BindingOperation bo = wsdlDef.createBindingOperation();
							bo.setName(operaName);
							binding.addBindingOperation(bo);
							
							SOAPOperationImpl soapOpera = new SOAPOperationImpl();
							soapOpera.setSoapActionURI("");
							soapOpera.setStyle("document");
							bo.addExtensibilityElement(soapOpera);
							BindingInput bindingInput = wsdlDef.createBindingInput();
							bo.setBindingInput(bindingInput);
							SOAPBody soapBody = new SOAPBodyImpl();
							soapBody.setUse("literal");
							bindingInput.addExtensibilityElement(soapBody);
							BindingOutput bindingOutput = wsdlDef.createBindingOutput();
							bo.setBindingOutput(bindingOutput);
							bindingOutput.addExtensibilityElement(soapBody);
						}
				}
				
				UnknownExtensibilityElement bindingExt = new UnknownExtensibilityElement();
				binding.addExtensibilityElement(bindingExt);
				org.w3c.dom.Element bindEl = document.createElementNS(Namespaces.STATENET, "binding");
				bindEl.setPrefix("sn");
				bindEl.setAttribute("component-name", compName);
				bindEl.setAttribute("transport", "");
				bindEl.setAttribute("bindingType", "");
				if (compName.indexOf("soap") != -1) {
					bindEl.setAttribute("type", "SOAPBody");
					bindEl.setAttribute("xpath", "");
					bindEl.setAttribute("head", "");
					bindEl.setAttribute("transport", "http");
				}
				bindingExt.setElement(bindEl);
				bindingExt.setElementType(new QName(bindEl.getNamespaceURI(), bindEl.getLocalName()));
			}
		}
		
		
		Service service = wsdlDef.createService();
		wsdlDef.addService(service);
		service.setQName(new QName(targetNamespace, request.getParameter("service-name")));
		WSDLWriter writer = WSDLUtil.getWSDLWriter();
		FileOutputStream outStream = new FileOutputStream(new File(unitFolder, "unit.wsdl"));
		try {
			writer.writeWSDL(wsdlDef, outStream);
		}
		finally {
			outStream.close();
		}
		//创建服务单元描述符
		createUnitDescriptor(unitFolder, component, true);
		return "true";
	}

	public String createTestUnit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String unitName = request.getParameter("name");
		String endpoint = request.getParameter("endpoint");
		String emulator = request.getParameter("emulator");
		String oriented = request.getParameter("oriented");
		File unitFolder = this.getFile(project + "/tests/" + unitName);
		if (unitFolder.exists()) {
			throw new Exception("测试单元名称不能重复，请重新指定");
		}
		if (!unitFolder.mkdir()) {
			throw new Exception("创建测试单元目录失败");
		}
		Element rootEl = new Element("unit");
		Document document = new Document(rootEl);
		rootEl.setAttribute("emulator", String.valueOf(emulator));
		rootEl.setAttribute("oriented", String.valueOf(oriented));
		rootEl.setAttribute("endpoint", String.valueOf(endpoint));
		rootEl.setAttribute("service-name", String.valueOf(request.getParameter("service-name")));
		rootEl.setAttribute("namespace", String.valueOf(request.getParameter("namespace")));
		rootEl.addContent(new Element("documentation").setText(request.getParameter("desc")));
		FileOutputStream outStream = new FileOutputStream(new File(unitFolder, "unit.xml"));
		try {
			JdomUtil.getPrettyOutputter().output(document, outStream);
		}
		finally {
			outStream.close();
		}
		return "true";
	}

	public String createFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String name = request.getParameter("file-name");
		File file = Application.getWorkspaceFile(path + "/" + name);
		if (file.exists()) {
			throw new Exception("文件已存在，请重新指定名称");
		}
		if (!file.createNewFile()) {
			throw new Exception("创建文件失败");
		}
		
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(this.getProject(path));
		JSONObject result = new JSONObject();
		if (synchronizer != null) {
			Map prop = synchronizer.status(file);
			if (prop != null) {
				result.put("revision", prop.get("revision"));
				result.put("author", prop.get("author"));
				result.put("status", prop.get("status"));
			}
			else {
				result.put("status", "?");
			}
		}
		result.put("success", true);
		return result.toString();
	}

	public String createFolder(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String name = request.getParameter("file-name");
		File folder = Application.getWorkspaceFile(path + "/" + name);
		if (folder.exists()) {
			throw new Exception("目录已存在，请重新指定");
		}
		if (!folder.mkdirs()) {
			throw new Exception("创建目录失败");
		}
		
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(this.getProject(path));
		JSONObject result = new JSONObject();
		if (synchronizer != null) {
			Map prop = synchronizer.status(folder);
			if (prop != null) {
				result.put("revision", prop.get("revision"));
				result.put("author", prop.get("author"));
				result.put("status", prop.get("status"));
			}
			else {
				result.put("status", "?");
			}
		}
		result.put("success", true);
		return result.toString();
	}

	public String createProject(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String directory = request.getParameter("project-name");
		String uri = request.getParameter("namespace");
		String manager = request.getParameter("team-leader");
		String dependency = request.getParameter("dependency");
		
		File projectDir = Application.getWorkspaceFile(directory);
		if (projectDir.exists()) {
			throw new Exception("项目已存在，请重新指定");
		}
		if (!projectDir.mkdirs()) {
			throw new Exception("创建项目目录失败");
		}
		
		File propertyFile = new File(projectDir, "project.properties");
		Properties prop = new Properties();
		prop.setProperty("project.team-leader", manager);
		prop.setProperty("project.dependency", dependency);
		FileOutputStream out = new FileOutputStream(propertyFile);
		try {
			prop.store(out, null);
		}
		finally {
			out.close();
		}

		File file = new File(projectDir, "jbi.xml");

		Document document = new Document();
		Namespace jbiNS = Namespace.getNamespace(Namespaces.JBI);
		Namespace tns = Namespace.getNamespace("tns", uri);
		Element rootEl = new Element("jbi", jbiNS);
		rootEl.addNamespaceDeclaration(tns);
		rootEl.setAttribute("version", "1.0");
		document.setRootElement(rootEl);
		Element assemblyEl = new Element("service-assembly", jbiNS);
		rootEl.addContent(assemblyEl);
		Element identEl = new Element("identification", jbiNS);
		assemblyEl.addContent(identEl);
		Element nameEl = new Element("name", jbiNS);
		nameEl.setText(request.getParameter("project-name"));
		identEl.addContent(nameEl);
		Element descEl = new Element("description", jbiNS);
		descEl.setText(request.getParameter("project-desc"));
		identEl.addContent(descEl);
		XMLOutputter outputter = JdomUtil.getPrettyOutputter();
		FileOutputStream outStream = new FileOutputStream(file);
		try {
			outputter.output(document, outStream);
		}
		finally {
			outStream.close();
		}
		
		FileUtil.unpackArchive(new File(Configuration.getRealPath("WEB-INF/normal.zip")), projectDir);
		return "true";
	}

	public String createSchema(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String schemaName = request.getParameter("schema-name");
		File serviceFile = this.getFile(project + "/schema/" + schemaName + ".xsd");
		if (serviceFile.exists()) {
			throw new Exception("模型名不能重复，请重新指定");
		}
		String targetUri = PrefsUtil.getNamespaceUri(project);
		Document document = new Document();
		Element rootEl;
		Namespace xsdNS = Namespace.getNamespace(Namespaces.XSD);
		Namespace tnsNS = Namespace.getNamespace("tns", targetUri);
		document.setRootElement(rootEl = new Element("schema", xsdNS));
		rootEl.addNamespaceDeclaration(tnsNS);
		rootEl.setAttribute("targetNamespace", targetUri);
		rootEl.setAttribute("elementFormDefault", "qualified");
		rootEl.setAttribute("attributeFormDefault", "unqualified");
		Element annoEl = new Element("annotation", xsdNS);
		rootEl.addContent(annoEl);
		Element docmEl = new Element("documentation", xsdNS);
		annoEl.addContent(docmEl);
		docmEl.setText(request.getParameter("desc"));

		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat().setEncoding("utf-8").setIndent("  "));
		FileOutputStream outStream = new FileOutputStream(serviceFile);
		outputter.output(document, outStream);
		outStream.close();

		return "true";
	}
	
	// 创建公共接口
	@SuppressWarnings("unchecked")
	public String createPubIntf(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String intfName = request.getParameter("intf-name");
		String desc = request.getParameter("desc");
		
		File intfFolder = this.getFile(project + "/intf/" + intfName);
		if (intfFolder.exists())
			throw new Exception("接口名不能重复，请重新指定");
		
		if (!intfFolder.mkdir())
			throw new Exception("创建接口目录失败");
		
		File intfFile = new File(intfFolder, "public.wsdl");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		org.w3c.dom.Document document = builder.newDocument();

		Definition wsdlDef = WSDLUtil.newDefinition();
		String targetNamespace = request.getParameter("namespace");
		wsdlDef.addNamespace("tns", targetNamespace);
		wsdlDef.addNamespace("sn", Namespaces.STATENET);
		wsdlDef.addNamespace("xs", Namespaces.XSD);
		wsdlDef.addNamespace("soap", Namespaces.SOAP_NAMESPACE);
		wsdlDef.addNamespace("", Namespaces.WSDL1_NAMESPACE);
		wsdlDef.setQName(new QName(targetNamespace, intfName));
		wsdlDef.setTargetNamespace(targetNamespace);
		wsdlDef.setDocumentationElement(createDocumentationEl(document, desc.replaceAll("[\n]+", "")));
		
		/*
		PortType pt = wsdlDef.createPortType();
		pt.setQName(new QName(intfName));
		wsdlDef.addPortType(pt);
		*/
		
		WSDLWriter writer = WSDLUtil.getWSDLWriter();
		FileOutputStream outStream = new FileOutputStream(new File(intfFolder, "public.wsdl"));
		try {
			writer.writeWSDL(wsdlDef, outStream);
		} finally {
			outStream.close();
		}
		return "true";
	}
	
	// 创建共享类库
	public String createPlugin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String libName = request.getParameter("file");
		String version = request.getParameter("version");
		String desc = request.getParameter("desc");
		
		File shareLib = this.getFile(project + "/lib/" + libName);
		if (!shareLib.exists()) 
			shareLib.mkdirs();
		ShareLibManager slm = new ShareLibManager(shareLib.getAbsolutePath(), libName, version);
		slm.setDescription(desc);
		slm.addPathElement("");
		slm.persistence();
		
		return "true";
	}

	public String getProjects(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String exclusion = request.getParameter("exclusion");
		
		JSONArray items = new JSONArray();

		SAXBuilder builder = new SAXBuilder();
		File workRoot = Application.getWorkspaceRoot();
		File[] list = workRoot.listFiles();
		for (int i=0; i<list.length; i++) {
			File entry = list[i];
			if (entry.isDirectory() && !entry.getName().startsWith(".")) {
				if ((exclusion!=null) && exclusion.indexOf(entry.getName()) > -1) {
					continue;
				}
				JSONObject item = new JSONObject();
				item.put("iconCls", "x-icon-project");
				item.put("text", entry.getName());
				item.put("name", entry.getName());
				File propFile = new File(entry, "jbi.xml");
				if (propFile.exists()) {
					Document doc = builder.build(propFile);
					Element rootEl = doc.getRootElement();
					Element saEl = rootEl.getChild("service-assembly", rootEl.getNamespace());
					if (saEl != null) {
						Element indenEl = saEl.getChild("identification", rootEl.getNamespace());
						String projectName = indenEl.getChildText("description", rootEl.getNamespace());
						if (projectName != null) {
							item.put("desc", projectName);
							item.put("text", projectName);
						}
						Namespace ns = rootEl.getNamespace("tns");
						if (ns != null) {
							item.put("namespace", ns.getURI());
						}
					}
				}
				items.put(item);
			}
		}
		JSONObject result = new JSONObject();
		result.put("items", items);
		return result.toString();
	}
	
	//项目浏览器获取项目文件
	public String getProjectFiles(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String node = request.getParameter("node");
		String path = request.getParameter("path");
		String regex = request.getParameter("filter");
		
		JSONArray result = new JSONArray();
		if (path == null) {
			return result.toString();
		}
		
		ThreeWaySynchronizer synchronizer = null;
		File entry = path.equals("/") ? Application.getWorkspaceRoot() : Application.getWorkspaceFile(path.substring(2));
		if (entry.exists() && entry.isDirectory()) {
			Pattern pattern = regex != null && regex.length() > 0 ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
					: null;
			Pattern xmlPattern = Pattern.compile("^(.*)\\.(xml|xsd|wsdl)$", Pattern.CASE_INSENSITIVE);
			Pattern jarPattern = Pattern.compile("^(.*)\\.(jar|zip)$", Pattern.CASE_INSENSITIVE);

			SAXBuilder builder = new SAXBuilder();
			File[] files = entry.listFiles();
			for (int j = 0; j < files.length; j++) {
				File file = files[j];
				if (file.isHidden()) continue;
				if (file.getName().startsWith(".")) continue;
				
				if (file.isDirectory()) {
					JSONObject item = new JSONObject();
					item.put("iconCls", "x-icon-folder");
					item.put("text", file.getName());
					item.put("name", file.getName());
					
					if (path.equals("/")) {
						synchronizer = SCM.getSynchronizer(file.getName());
						//item.put("uiProvider", "project");
						item.put("iconCls", "x-icon-project");
						item.put("allowRemove", true);
						File propFile = new File(file, "jbi.xml");
						if (propFile.exists()) {
							Document doc = builder.build(propFile);
							Element rootEl = doc.getRootElement();
							Element saEl = rootEl.getChild("service-assembly", rootEl.getNamespace());
							if (saEl != null) {
								Element indenEl = saEl.getChild("identification", rootEl.getNamespace());
								String projectName = indenEl.getChildText("description", rootEl.getNamespace());
								if (projectName != null) {
									item.put("desc", projectName);
									item.put("text", projectName);
								}
								Namespace ns = rootEl.getNamespace("tns");
								if (ns != null) {
									item.put("qtip", ns.getURI());
								}
							}
						}
						File propertyFile = new File(file, "project.properties");
						if (propertyFile.exists()) {
							Properties prop = new Properties();
							InputStream input = new FileInputStream(propertyFile);
							prop.load(input);
							input.close();
							item.put("leader", prop.getProperty("project.team-leader"));
							item.put("reference", new JSONArray(prop.getProperty("project.dependency")));
						}
						
						if (synchronizer != null) {
							Map version = synchronizer.status(file);
							//System.out.println(version.toString());
							item.put("revision", version.get("revision"));
							item.put("author", version.get("author"));
							item.put("status", version.get("status"));
							item.put("reposUrl", version.get("url"));
						}
					}
					else {
						item.put("allowRemove", file.canWrite());
						
						String project = this.getProject(path.substring(2));
						synchronizer = SCM.getSynchronizer(project);
						Map properties;
						if (synchronizer != null && (properties=synchronizer.status(file))!=null) {
							item.put("revision", properties.get("committed-rev"));
							item.put("author", properties.get("author"));
							item.put("status", properties.get("status"));
						}
					}
					
					result.put(item);
				}
				else if (file.isFile() && (pattern == null || pattern.matcher(file.getName()).matches())) {
					JSONObject item = new JSONObject();
					item.put("text", file.getName());
					String desc = "", filename = file.getName();
					if (file.length() > 0) {
						try {
							if (xmlPattern.matcher(filename).matches())
								desc = this.getDocumentation(builder.build(file));
							else if (jarPattern.matcher(filename).matches()) {
								JarFile jar = new JarFile(file);
								try {	desc = this.getDescription(jar); } finally { jar.close();}
							}
						}
						catch (Exception e) {
						}
					}
					int pos = filename.lastIndexOf(".");
					item.put("name", file.getName());
					item.put("qtip", desc != null ? desc : "");
					item.put("iconCls", "x-icon-" + (pos >= 0 ? filename.substring(pos + 1) : ""));
					item.put("leaf", true);
					item.put("allowRemove", file.canWrite());
					if (!path.equals("/")) {
						synchronizer = SCM.getSynchronizer(this.getProject(path.substring(2)));
						Map properties;
						if (synchronizer != null && (properties=synchronizer.status(file))!=null) {
							item.put("revision", properties.get("committed-rev"));
							item.put("author", properties.get("author"));
							item.put("status", properties.get("status"));
						}
					}
					result.put(item);
				}
			}
		}
		return result.toString();
	}
	
	//项目查看器获取项目资源
	public String getResources(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String group = request.getParameter("group");
		JSONArray result = new JSONArray();
		File folder = Configuration.getWorkspaceFile(project + "/" + group);
		if (folder.exists()) {
			String regex = request.getParameter("filter");
			Pattern pattern = regex != null && regex.length() > 0 ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
					: null;
			Pattern xmlPattern = Pattern.compile("^(.*)\\.(xml|xsd|wsdl)$", Pattern.CASE_INSENSITIVE);
			Pattern jarPattern = Pattern.compile("^(.*)\\.(jar|zip)$", Pattern.CASE_INSENSITIVE);

			ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(project);
			Map version;
			
			SAXBuilder builder = new SAXBuilder();
			File[] entries = folder.listFiles();
			for (int j = 0; j < entries.length; j++) {
				File entry = entries[j];
				if (entry.getName().startsWith(".")) {
					continue;
				}
				if (pattern == null || !pattern.matcher(entry.getName()).matches()) {
					JSONObject item = new JSONObject();
					String desc = "", filename = entry.getName();
					int lastIndex = filename.lastIndexOf(".");
					item.put("text", lastIndex > 0 ? filename.substring(0, lastIndex) : filename);
					try {
						if (entry.isDirectory()) {
							if ("intf".equals(group)) {
								File file = new File(entry, "public.wsdl");
								desc = this.getDocumentation(builder.build(file));
							} else {
								File file = new File(entry, "unit.wsdl");
								if (file.exists()) {
									desc = this.getDocumentation(builder.build(file));
								} else if ((file=new File(entry, "jbi.xml")).exists()) {
									desc = this.getJbiDescription(builder.build(file));
								}
							}
						}
						else if (entry.length() > 0) {
							try {
								if (xmlPattern.matcher(filename).matches())
									desc = this.getDocumentation(builder.build(entry));
								else if (jarPattern.matcher(filename).matches()) {
									JarFile jar = new JarFile(entry);
									try {	desc = this.getDescription(jar); } finally { jar.close();}
								}
							}
							catch (Exception e) {
							}
						}
					}
					catch (Exception e) {
					}
					item.put("name", entry.getName());
					item.put("desc", desc != null ? desc : "");
					item.put("uiProvider", "resource");
					item.put("leaf", true);
					item.put("checked", true);
					
					if (synchronizer!=null && (version=synchronizer.status(entry)) != null) {
						item.put("revision", version.get("committed-rev"));
						item.put("author", version.get("author"));
						item.put("status", version.get("status"));
					}
					result.put(item);
				}
			}
		}
		return result.toString();
	}
	
	public String info(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String project = request.getParameter("project");
		
		String result = "<b>位置</b>: "+path;
		File location = Application.getWorkspaceFile(path);
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(this.getProject(path));
		Map info;
		if (synchronizer!=null && (info=synchronizer.status(location)) != null) {
	        result += "<br/><br/><b>版本库地址</b>: " + info.get("url");
	        result += "<br/><b>版本号</b>: "+ info.get("committed-rev");
	        result += "<br/><b>提交者</b>: "+ info.get("author");
		} else {
			result = result.replaceAll(".*?//", "/project");
		}
		
		return "<p style='background-color: white; padding:5px;'>"+result+"</p>";
	}
	
	public String modifyProject(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String directory = request.getParameter("project-name");
		String uri = request.getParameter("namespace");
		String manager = request.getParameter("team-leader");
		String dependency = request.getParameter("dependency");
		
		File projectDir = Application.getWorkspaceFile(directory);
		if (!projectDir.exists()) {
			throw new Exception("项目不存在，可能已被其他人删除");
		}
		
		File propertyFile = new File(projectDir, "project.properties");
		Properties prop = new Properties();
		if (propertyFile.exists()) {
			FileInputStream in = new FileInputStream(propertyFile);
			prop.load(in);
			in.close();
		}
		prop.setProperty("project.team-leader", manager);
		prop.setProperty("project.dependency", dependency);
		FileOutputStream out = new FileOutputStream(propertyFile);
		prop.store(out, null);
		out.close();

		SAXBuilder builder = new SAXBuilder();
		File file = new File(projectDir, "jbi.xml");
		Document document = builder.build(file);
		Element rootEl = document.getRootElement();
		Namespace tns = Namespace.getNamespace("tns", uri);
		rootEl.removeNamespaceDeclaration(rootEl.getNamespace("tns"));
		rootEl.addNamespaceDeclaration(tns);
		
		XPath xpath = XPath.newInstance("jbi:service-assembly/jbi:identification/jbi:description");
		xpath.addNamespace("jbi", rootEl.getNamespaceURI());
		Element descEl = (Element)xpath.selectSingleNode(rootEl);
		if (descEl != null) {
			descEl.setText(request.getParameter("project-desc"));
		}
		XMLOutputter outputter = JdomUtil.getPrettyOutputter();
		FileOutputStream outStream = new FileOutputStream(file);
		try {
			outputter.output(document, outStream);
		}
		finally {
			outStream.close();
		}
		
		FileUtil.unpackArchive(new File(Configuration.getRealPath("WEB-INF/normal.zip")), projectDir);
		return "true";
	}

	public String remove(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String files = request.getParameter("files");
		if (files != null) {
			//project explorer remove request
			JSONArray jsonFiles = new JSONArray(files);
			JSONArray effected = new JSONArray();
			JSONObject result = new JSONObject();
			try {
				for (int i = 0, len = jsonFiles.length(); i < len; i++) {
					String filepath = jsonFiles.getString(i);
					String project = this.getProject(filepath.substring(2));
					ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(project);
					File file = Application.getWorkspaceFile(filepath.substring(2));
					//从版本库中删除
					if (synchronizer!=null && SCM.isVersioned(file) && SCM.isVersioned(file.getParentFile())) {
						synchronizer.delete(file);
					}
					//删除本地物理文件
					if (file.isDirectory()) {
						deleteFiles(file);
					}
					file.delete();
					if (file.exists()) {
						throw new Exception("删除失败: \"" + file + "\"");
					}
					effected.put(filepath);
				}
				result.put("success", true);
			}
			catch (Exception e) {
				result.put("success", false);
				result.put("message", e.getMessage());
				result.put("effected", effected);
			}
			return result.toString();
		}

		String filePath = request.getParameter("file");
		File file = this.getFile(filePath);
		String project = this.getProject(filePath);
		//从版本库中删除
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(project);
		if (synchronizer!=null && synchronizer.isVersioned(file)) {
			synchronizer.delete(file);
		}
		//删除本地物理文件
		if (file.isDirectory()) {
			deleteFiles(file);
		}
		if (!file.delete()) {
			throw new Exception("删除失败: \"" + file + "\" ");
		}
		return "true";
	}

	public String rename(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String f = request.getParameter("file");
		String path = request.getParameter("path");
		String newName = request.getParameter("newName");
		
		String project;
		File newFile, file;
		if (path != null) {
			//project explorer
			file = Application.getWorkspaceFile(path.substring(2));
			newFile = new File(file.getParent(), newName);
			project = this.getProject(path.substring(2));
		}
		else {
			//project viewer
			file = this.getFile(f);
			String fileName = file.getName();
			int idx = fileName.lastIndexOf(".");
			String ext = idx>-1 ? fileName.substring(idx) : "";
			newFile = new File(file.getParent(), newName + ext);
			project = this.getProject(f);
		}

		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(project);
		if (synchronizer != null && synchronizer.isVersioned(file)) {
			synchronizer.move(file, newFile);
			return "true";
		}
		else {
			if (file.renameTo(newFile)) {
				if (file.isFile()) {
					//尝试更改对象的名称
					
				}
				return "true";
			}
			else {
				throw new Exception("重命名文件失败，文件可能已被删除");
			}
		}
	}

	public String post(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String contentType = request.getContentType();
		if (contentType.indexOf("multipart/form-data;") > -1) {
			response.setContentType("text/html; charset=utf-8");
			try {
				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setSizeThreshold(4096);
				ServletFileUpload upload = new ServletFileUpload(factory);
				upload.setHeaderEncoding("UTF-8");
				Map<String, Object> params = new HashMap<String, Object>();

				List<?> items = upload.parseRequest(request);
				for (Iterator<?> iter = items.iterator(); iter.hasNext();) {
					FileItem item = (FileItem) iter.next();
					if (item.isFormField()) {
						String value = item.getString();
						try {
							if (value != null)
								value = new String(value.getBytes("iso8859-1"), "utf-8");
						}
						catch (UnsupportedEncodingException e) {
						}
						params.put(item.getFieldName(), value);
					}
					else {
						params.put(item.getFieldName(), item);
					}
				}

				//处理项目管理器中简单的文件上传
				String opera = (String) params.get("operation");
				if (opera == null) {
				}
				else if (opera.equals("uploadFile")) {
					String path = (String) params.get("path");
					FileItem uploadFile = (FileItem) params.get("file-name");
					String filename = uploadFile.getName();
					int index = filename.lastIndexOf(File.separator);
					if (index > -1)
						filename = filename.substring(index + 1);
					File file = new File(Configuration.getRealPath(path.substring(1) + File.separator + filename));
					uploadFile.write(file);
					return "true";
				}
				else if (opera.equals("uploadCom")) {
					String project = (String) params.get("project");
					String component = (String) params.get("component");
					FileItem uploadItem = (FileItem) params.get("comp-jar");
					String filename = uploadItem.getName();
					
					int index = filename.lastIndexOf(File.separator);
					if (index > -1)
						filename = filename.substring(index + 1);
					if (!filename.endsWith(".jar")) {
						throw new Exception("组件包必须是扩展名为 .jar 的文件");
					}
					File compBundle = Configuration.getWorkspaceFile(project + "/component/" + filename.replaceFirst("\\.jar$", ""));
					if (compBundle.exists()) {
						throw new Exception("组件已经存在，请选择其它组件");
					}
					FileUtil.buildDirectory(compBundle);
					ZipInputStream zipInput = new ZipInputStream(uploadItem.getInputStream());
					try {
						FileUtil.unzip(zipInput, compBundle);
					}
					finally {
						zipInput.close();
					}
					
					// 读取组件描述
					File jbiFile = new File(compBundle,"jbi.xml");
					JSONArray jsoArray = new JSONArray();
					JSONObject jso = new JSONObject();
					jso.put("success", true);
					jso.put("text", compBundle.getName());
					if (jbiFile.exists()) {
						Dom4jUtil.initDocument(jbiFile);
						org.dom4j.Element root = Dom4jUtil.getRootEl();
						org.dom4j.Element descEl = 
							(org.dom4j.Element)root.selectSingleNode("//*[name()='component']/*[name()='identification']/*[name()='description']");
						String desc = descEl.getText();
						jso.put("desc", desc);
						jso.put("file", filename);
						jso.put("leaf", true);
						jso.put("uiProvider", "resource");
					}
					
					return jso.toString();
				}
				else if (opera.equals("uploadTransport")) {
					String project = (String) params.get("project");
					String component = (String) params.get("component");
					FileItem uploadItem = (FileItem) params.get("comp-jar");
					String filename = uploadItem.getName();
					
					int index = filename.lastIndexOf(File.separator);
					if (index > -1)
						filename = filename.substring(index + 1);
					if (!filename.endsWith(".jar")) {
						throw new Exception("类库必须是扩展名为 .jar 的文件");
					}
					File bundle = Configuration.getWorkspaceFile(project + "/transport/" + filename);
					if (bundle.exists()) {
						throw new Exception("类库已经存在，请重新选择");
					}
					uploadItem.write(bundle);
					
					// 读取组件描述
					JSONArray jsoArray = new JSONArray();
					JSONObject jso = new JSONObject();
					jso.put("success", true);
					jso.put("text", bundle.getName().replaceAll("\\.jar$", ""));
					String desc = "";
					JarFile jar = new JarFile(bundle);
					try {	desc = this.getDescription(jar); } finally { jar.close();}
					jso.put("desc", desc);
					jso.put("file", filename);
					jso.put("leaf", true);
					jso.put("uiProvider", "resource");
					
					return jso.toString();
				}
				else if (opera.equals("uploadUnit")) {
					String project = (String) params.get("project");
					String unitType = (String) params.get("type");
					String unitName = (String) params.get("serviceUnit");
					FileItem uploadItem = (FileItem) params.get("file");
					String filename = uploadItem.getName();
					int index = filename.lastIndexOf(File.separator);
					if (index > -1)
						filename = filename.substring(index + 1);
					if (!filename.endsWith(".zip")) {
						throw new Exception("服务包必须是扩展名为 .zip 的文件");
					}
					File unitFolder = Configuration.getWorkspaceFile(project + "/" + unitType + "/" + unitName);
					if (unitFolder.exists()) {
						throw new Exception("服务单元已经存在，请指定一个新的名称");
					}

					File unitFile = Configuration.getWorkspaceFile(project + "/" + unitType + "/" + unitName + ".zip");
					uploadItem.write(unitFile);

					FileUtil.buildDirectory(unitFolder);
					FileUtil.unpackArchive(unitFile, unitFolder);
					/*
			        ZipFile zipFile = new ZipFile(unitFile);
					try {
						final String path = "target/classes/";
				        for (Enumeration<?> entries = zipFile.entries(); entries.hasMoreElements();) {
				            ZipEntry entry = (ZipEntry) entries.nextElement();
				            if (entry.getName().startsWith(path)) {
				                File file = new File(unitFolder, entry.getName().replaceFirst(path, ""));
				                if (!FileUtil.buildDirectory(file.getParentFile())) {
				                    throw new IOException("Could not create directory: " + file.getParentFile());
				                }
				                if (!entry.isDirectory()) {
				                	OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
				                	InputStream input = zipFile.getInputStream(entry);
				                    FileUtil.copyInputStream(input, output);
				                    input.close();
				                    output.close();
				                }
				                else {
				                    if (!FileUtil.buildDirectory(file)) {
				                        throw new IOException("Could not create directory: " + file);
				                    }
				                }
				            }
				        }
					}
					finally {
				        zipFile.close();
					}
					*/
					return "true";
				}
				return "{success:false, error:\"未知操作\"}";
			}
			catch (Throwable e) {
				return "{success:false, error:\"" + e.getMessage() + "\"}";
			}
		}
		else {
			throw new Exception("[" + request.getMethod() + "]没有指定 operation 参数时，只接受文件上传");
		}
	}
	
	public String getProjectManagers(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray items = new JSONArray();
		File userFile = new File(Configuration.getRealPath("WEB-INF/user.xml"));
		if (userFile.exists()) {
			SAXBuilder builder = JdomUtil.newSAXBuilder();
			Document doc = builder.build(userFile);
			Element root = doc.getRootElement();
			List users = root.getChildren("user");
			
			if (users != null && !users.isEmpty()) {
				for (Iterator itr = users.iterator(); itr.hasNext();) {
					JSONObject jso = new JSONObject();
					Element user = (Element)itr.next();
					String fullName = user.getAttributeValue("fullname");
					String userId = user.getAttributeValue("userid");
					if ("root".equalsIgnoreCase(userId)) 
						continue;
					jso.put("label", fullName);
					jso.put("name", userId);
					items.put(jso);
				}
			}
		}
		
		result.put("items", items);
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	}%>

<%
	String operation = request.getParameter("operation");
	WebServletResponse responseWrapper = new WebServletResponse(response);

	if (operation == null) {
		operation = request.getMethod().toLowerCase();
	}
	
	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[] { HttpServletRequest.class,
				HttpServletResponse.class });
		String result = (String) method.invoke(this, new Object[] { new WebServletRequest(request),
				responseWrapper });
		out.clear();
		out.println(result);
	}
	catch (NoSuchMethodException e) {
		responseWrapper.sendError("["+request.getMethod()+"]找不到相应的方法来处理指定的 operation: "+operation);
	}
	catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		responseWrapper.sendError(t.getMessage());
	}
	catch (Exception e) {
		responseWrapper.sendError(e.getMessage());
	}
%>