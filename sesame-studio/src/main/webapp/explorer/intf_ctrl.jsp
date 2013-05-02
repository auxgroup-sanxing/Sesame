<%@page import="org.apache.log4j.Logger"%>
<%@page import="com.sanxing.ads.*,com.sanxing.ads.utils.*,com.sanxing.ads.team.svn.*,com.sanxing.ads.team.*"%>
<%@page import="com.sanxing.statenet.jaxp.*"%>
<%@page import="com.ibm.wsdl.ImportImpl"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="org.json.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="javax.xml.parsers.*"%>
<%@page import="javax.wsdl.*"%>
<%@page import="javax.wsdl.factory.*"%>
<%@page import="javax.wsdl.xml.*"%>
<%@page import="javax.wsdl.extensions.*"%>
<%@page import="javax.wsdl.extensions.soap.*"%>
<%@page import="javax.wsdl.extensions.schema.*"%>
<%@page import="com.ibm.wsdl.extensions.schema.*" %>
<%@page import="com.sanxing.ads.IllegalNameException"%>
<%@page import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.*"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page language="java" contentType="text/xml; charset=utf-8" pageEncoding="utf-8"%>

<%!private final Logger logger = Logger.getLogger(this.getClass());

private WSDLReader getWSDLReader() throws WSDLException {
	WSDLFactory factory = WSDLFactory.newInstance();
	WSDLReader reader = factory.newWSDLReader();
	reader.setFeature("javax.wsdl.verbose", false);
	reader.setFeature("javax.wsdl.importDocuments", false);
	return reader;
}

private org.w3c.dom.Element createDocumentationEl(org.w3c.dom.Document doc, String documentation) {
	org.w3c.dom.Element element = doc.createElementNS(Namespaces.WSDL1_NAMESPACE, "documentation");
	org.w3c.dom.Text textNode = doc.createTextNode(documentation);
	element.appendChild(textNode);
	return element;
}

private org.w3c.dom.Element createSchemaEl(org.w3c.dom.Document doc, String targetNamespace) {
	org.w3c.dom.Element schemaEl = doc.createElementNS(Namespaces.XSD, "xs:schema");
	schemaEl.setAttribute("attributeFormDefault", "unqualified");
	schemaEl.setAttribute("elementFormDefault", "qualified");
	schemaEl.setAttribute("targetNamespace", targetNamespace);
	return schemaEl;
} 

private void createSchema(File schemaFile, String targetUri) throws Exception {
	Namespace xmlns = Namespace.getNamespace(Namespaces.XSD);
	Namespace xmlns_tns = Namespace.getNamespace("tns", targetUri);
	Document document = new Document();
	Element rootEl = new Element("schema", xmlns);
	rootEl.addNamespaceDeclaration(xmlns_tns);
	rootEl.setAttribute("elementFormDefault", "qualified");
	rootEl.setAttribute("attributeFormDefault", "unqualified");
	rootEl.setAttribute("targetNamespace", targetUri);
	document.setRootElement(rootEl);
	
	
	XMLOutputter outputter = new XMLOutputter();
	outputter.setFormat(Format.getPrettyFormat().setEncoding("utf-8").setIndent("  "));
	OutputStream fileStream = new FileOutputStream(schemaFile);
	try {
		outputter.output(document, fileStream);
	}
	finally {
		fileStream.close();
	}
}

// 读取接口列表
public String getPublicIntfs(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File intfFolder = new File(unitFolder.getParentFile().getParentFile(), "intf");
	
	JSONArray items = new JSONArray();
	if (intfFolder.exists()) {
		for (File intf : intfFolder.listFiles()) {
			if (intf.isDirectory()) {
				File intfFile = new File(intf, "public.wsdl");
				
				WSDLReader reader = this.getWSDLReader();
				Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfFile, true));
				String desc = wsdlDef.getDocumentationElement().getTextContent().trim();
				String intfName = intf.getName();
				
				JSONObject jso = new JSONObject();
				jso.put("intf", intfName);
				jso.put("desc", desc);
				items.put(jso);
			}
		}
	}
	
	JSONObject result = new JSONObject();
	result.put("items", items);
	response.setContentType("text/json; charset=utf-8");
	return result.toString();
}

// 读取接口详细信息
@SuppressWarnings("unchecked")
public String loadData(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String intfPath = request.getParameter("intfPath");
	File folder = Configuration.getWorkspaceFile(intfPath);
	File intfFile = new File(folder, "public.wsdl");
	
	if (!intfFile.exists()) {
		throw new Exception("接口定义文件不存在!");
	}
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	WSDLReader reader = this.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfFile, true));
	
	JSONArray items = new JSONArray();
	Map ptMap = wsdlDef.getPortTypes();
	if (ptMap != null && !ptMap.isEmpty()) {
		Set<Map.Entry<?,?>> set = ptMap.entrySet();
		for (Iterator<?> iter=set.iterator(); iter.hasNext();) {
			Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
			PortType portType = (PortType)entry.getValue();
			
			List<?> operList = portType.getOperations();
			for (Iterator<?> it = operList.iterator(); it.hasNext(); ) {
				Operation operation = (Operation)it.next();
				String opera = operation.getName();
				File file = new File(folder, opera + ".xsd");
				JSONObject item = new JSONObject();
				item.put("interface", portType.getQName().getLocalPart());
				item.put("intfDesc", wsdlDef.getDocumentationElement().getTextContent());
				
				item.put("opera", opera);
				org.w3c.dom.Element docuEl = operation.getDocumentationElement();
				item.put("desc", docuEl!=null? DOMUtil.getElementText(docuEl):"");
				item.put("readOnly", !intfFile.canWrite());
				if (file.exists()) 
					item.put("lastModified", dateFormat.format(new java.util.Date(file.lastModified())));
				items.put(item);
			}
		}
	}
	
	JSONObject result = new JSONObject();
	result.put("items", items);
	result.put("count", items.length());
	response.setContentType("text/json; charset=utf-8");
	return result.toString();
}

// 保存接口数据
@SuppressWarnings("unchecked")
public String savePortTypes(HttpServletRequest request, HttpServletResponse response) throws Exception {
	request.setCharacterEncoding("utf-8");
	String data = URLDecoder.decode(request.getParameter("data"), "UTF-8");
	String intfPath = request.getParameter("intf");
	String intf = intfPath.replaceAll(".*?\\/", "");
	
	InputSource input = new InputSource(new StringReader(data));
	input.setEncoding(request.getCharacterEncoding());
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();
	org.w3c.dom.Document document = builder.parse(input);
	org.w3c.dom.Element inputRootEl = document.getDocumentElement();
	org.w3c.dom.NodeList inputPtList =  inputRootEl.getElementsByTagName("portType");

	File intfFolder = Configuration.getWorkspaceFile(intfPath);
	File intfFile = new File(intfFolder, "public.wsdl");
	WSDLReader reader = this.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfFile, true));
	
	for (int i = 0,len = inputPtList.getLength(); i<len; i++) {
		org.w3c.dom.Element inputPortTypeEl = (org.w3c.dom.Element)inputPtList.item(i);
		String desc = inputPortTypeEl.getAttribute("intfDesc");
		wsdlDef.getDocumentationElement().setTextContent(desc);
		PortType ptEl = wsdlDef.getPortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), intf));
		if (ptEl != null) {
			org.w3c.dom.Element ptDoc = ptEl.getDocumentationElement();
			if (ptDoc == null) {
				ptDoc = document.createElement("documentation");
				ptEl.setDocumentationElement(ptDoc);
			}
			ptDoc.setTextContent(desc);
		}
	}
	
	WSDLWriter writer = WSDLUtil.getWSDLWriter();
	OutputStream output = new FileOutputStream(intfFile);
	try {
		Writer out = new OutputStreamWriter(output, "utf-8");
		writer.writeWSDL(wsdlDef, out);
	}
	finally {
		output.close();
	}
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

// 创建操作
@SuppressWarnings("unchecked")
public String createOpera(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String intfPath = request.getParameter("intf");
	String intf = intfPath.replaceAll(".*?\\/", "");
	try {
		String oper = request.getParameter("opera").trim();
		File intfFolder = Configuration.getWorkspaceFile(intfPath);
		File file = new File(intfFolder, "public.wsdl");
		
		WSDLReader reader = this.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(file, true));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		org.w3c.dom.Document document = builder.newDocument();

		// 检查操作名称
		Map portTypes = wsdlDef.getPortTypes();
		for (Iterator iter=portTypes.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry entry = (Map.Entry)iter.next();
			PortType portType = (PortType)entry.getValue();
			Operation opera = portType.getOperation(oper, null, null);
			if (opera != null) {
				throw new IllegalNameException("操作名称已经存在，请重新指定");
			}
		}
		
		PortType portType = wsdlDef.getPortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), intf));
		if (portType == null) {
			portType = wsdlDef.createPortType();
			portType.setQName(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), intf));
			portType.setUndefined(false);
			wsdlDef.addPortType(portType);
		}
		Operation opera = wsdlDef.createOperation();
		opera.setName(oper);
		opera.setDocumentationElement(createDocumentationEl(document, request.getParameter("desc")));
		opera.setUndefined(false);
		portType.addOperation(opera);
		String namespaceUri = wsdlDef.getTargetNamespace();
		File schemaFile = new File(intfFolder, oper + ".xsd");
		createSchema(schemaFile, namespaceUri);
		String targetNamespace = wsdlDef.getTargetNamespace();
		Types types = wsdlDef.getTypes();
		if (types == null) {
			types = wsdlDef.createTypes();
			wsdlDef.setTypes(types);
		}
		Schema schema = null;
		List<?> list = types.getExtensibilityElements();
		if (list.size() > 0) {
			schema = (Schema)list.get(0);
		} else {
			schema = new SchemaImpl();
			schema.setRequired(true);
			schema.setElementType(new javax.xml.namespace.QName(Namespaces.XSD, "schema"));
			types.addExtensibilityElement(schema);
			org.w3c.dom.Element schemaEl = createSchemaEl(document, targetNamespace);
			schema.setElement(schemaEl);
		}
		
		org.w3c.dom.Element schemaEl = schema.getElement();
		org.w3c.dom.Element importEl = schemaEl.getOwnerDocument().createElementNS(Namespaces.XSD, "include");
		importEl.setPrefix("xs");
		importEl.setAttribute("schemaLocation", opera.getName()+".xsd");
		schemaEl.appendChild(importEl);

		WSDLWriter writer = WSDLUtil.getWSDLWriter();
		OutputStream fileStream = new FileOutputStream(file);
		try {
			writer.writeWSDL(wsdlDef, fileStream);
		}
		finally {
			fileStream.close();
		}
		
		response.setContentType("text/json; charset=utf-8");
		return "true";
	} finally{}
}


// 修改操作
@SuppressWarnings("unchecked")
public String modifyOpera(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String intfPath = request.getParameter("intf");
	String intf = intfPath.replaceAll(".*?\\/", "");
	
	String oper = request.getParameter("old-opera").trim();
	String newOpera = request.getParameter("opera").trim();
	String component = request.getParameter("component");

	File intfFolder = Configuration.getWorkspaceFile(intfPath);
	File intfFile = new File(intfFolder, "public.wsdl");
	
	// 从SVN改名
	File projectFolder = intfFolder.getParentFile().getParentFile();
	ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
	
	WSDLReader reader = this.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfFile, true));
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();
	org.w3c.dom.Document document = builder.newDocument();
	
	// 检查操作名称
	if (!newOpera.equals(oper)) {
		Map portTypes = wsdlDef.getPortTypes();
		for (Iterator iter=portTypes.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry entry = (Map.Entry)iter.next();
			PortType portType = (PortType)entry.getValue();
			Operation opera = portType.getOperation(newOpera, null, null);
			if (opera != null) {
				throw new IllegalNameException("操作名称已经存在，请重新指定");
			}
		}
	}
	
	PortType portType = wsdlDef.getPortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), intf));
	Operation opera = null;
	if (portType==null || (opera=portType.getOperation(oper, null, null))==null) {
		throw new IllegalNameException("操作不存在，可能已被删除");
	} 
	else {
		// 修改portType中的operation
		portType.setUndefined(false);
		opera.setUndefined(false);
		opera.setName(newOpera);
		opera.setDocumentationElement(createDocumentationEl(document, request.getParameter("desc")));
	   	List<?> list = opera.getExtensibilityElements();
	   	UnknownExtensibilityElement extElement;
	   	if (list.size() > 0) {
	   		extElement = (UnknownExtensibilityElement)list.get(0);
	   	}else {
	   		extElement = new UnknownExtensibilityElement();
	   		opera.addExtensibilityElement(extElement);
	   	}
	   	
	 	// 修改binding中的operation
	   	Map bindingMap = wsdlDef.getAllBindings();
	   	if (bindingMap != null && !bindingMap.isEmpty()) {
			for (Iterator itr = bindingMap.entrySet().iterator(); itr.hasNext();) {
				Map.Entry entry = (Map.Entry)itr.next();
				Binding binding = (Binding)entry.getValue();
				String name = binding.getQName().getLocalPart();
				String ptName = 
						(binding.getPortType() != null) ? binding.getPortType().getQName().getLocalPart() : "";
				if (intf.equals(ptName)) {
					BindingOperation bindingOpera = binding.getBindingOperation(oper, null, null);
					if (bindingOpera != null) {
						bindingOpera.setName(newOpera);
					} else {
						binding.removeBindingOperation(oper, null, null);
					}
				}
			}
	   	}
	}
	
	// 修改引用的shcema名称
	Types types = wsdlDef.getTypes();
	if (types != null) {
		List schemas = types.getExtensibilityElements();
		if (schemas != null && !schemas.isEmpty() )
		for (Iterator itr = schemas.iterator(); itr.hasNext();) {
			Schema schema = (Schema)itr.next();
			org.w3c.dom.Element el =   schema.getElement();
			
			org.w3c.dom.NodeList includes = el.getElementsByTagName("*");
			for (int i=0; i < includes.getLength(); i++) {
				org.w3c.dom.Element include = (org.w3c.dom.Element)includes.item(i);
				String location = include.getAttribute("schemaLocation");
				if (oper.equals(location.replaceAll("\\.xsd", ""))) {
					include.setAttribute("schemaLocation", newOpera + ".xsd");
					break;
				}
			}
		}
	}
	
	WSDLWriter writer = WSDLUtil.getWSDLWriter();
	OutputStream fileStream = new FileOutputStream(intfFile);
	try {
		writer.writeWSDL(wsdlDef, fileStream);
	}
	finally {
		fileStream.close();
	}
	
	File srcFile = new File(intfFolder, oper+".xsd");
	File destFile = new File(intfFolder, newOpera + ".xsd");
	if (srcFile.exists()) {
		LockUtil.renameFromSvn(sync, srcFile, destFile);
		srcFile.renameTo(destFile);
		String targetNamespace = wsdlDef.getTargetNamespace();
		SAXBuilder saxbuilder = new SAXBuilder();
		Document doc = saxbuilder.build(destFile);
		Element rootEl = doc.getRootElement();
		rootEl.setAttribute("targetNamespace", targetNamespace);
		Namespace xmlns_tns = rootEl.getNamespace("tns");
		if (xmlns_tns != null) rootEl.removeNamespaceDeclaration(xmlns_tns);
		xmlns_tns = Namespace.getNamespace("tns", targetNamespace);
		rootEl.addNamespaceDeclaration(xmlns_tns);
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat().setEncoding("utf-8").setIndent("  "));
		OutputStream output = new FileOutputStream(destFile);
		try {
			outputter.output(doc, output);
		} finally {
			output.close();
		}
	}
	
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

// 删除操作
@SuppressWarnings("unchecked")
public String removeOpera(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("operas");
	JSONArray operas = new JSONArray(data);
	String intfPath = request.getParameter("intf");
	String intf = intfPath.replaceAll(".*?\\/", "");
	
	try {
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		File intfFolder = Configuration.getWorkspaceFile(intfPath);
		File intfFile = new File(intfFolder, "public.wsdl");
		
		// 从SVN删除文件
		File projectFolder = intfFolder.getParentFile().getParentFile();
		ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
		
		// 修改wsdl文件
		WSDLReader reader = WSDLUtil.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfFile, true));
	
		Types types = wsdlDef.getTypes();
		org.w3c.dom.NodeList includes = null;
		if (types != null) {
			List list = types.getExtensibilityElements();
			if (list.size() > 0) {
				Schema schema = (Schema)list.get(0);
				includes = schema.getElement().getElementsByTagNameNS("*", "include");
			}
		}

		for (int i=0,len=operas.length(); i<len; i++) {
			JSONObject operaObj = operas.getJSONObject(i);
			String opera = operaObj.getString("opera");
			
			if (includes != null && includes.getLength()>0) {
				for (int n=0; i<includes.getLength(); n++) {
					org.w3c.dom.Element elem = (org.w3c.dom.Element)includes.item(n);
					if (elem.getAttribute("schemaLocation").equals(opera + ".xsd")) {
						elem.getParentNode().removeChild(elem);
						break;
					}
				}
			}
			
			PortType portType = wsdlDef.getPortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), intf));
			Operation operation;
			if (portType != null && (operation=portType.getOperation(opera, null, null))!=null) {
				File xsdfile = new File(intfFolder, opera + ".xsd");
				if (xsdfile.exists()) {
					LockUtil.deleteFromSvn(sync, xsdfile); 
					xsdfile.delete();
				}
				
				if (portType.getOperations().size()==1) {
					wsdlDef.removePortType(portType.getQName());
					Collection<Binding> bindings = wsdlDef.getBindings().values();
					for (Binding binding : bindings) {
						if (binding.getPortType()==portType) {
							binding.setPortType(null);
						}
					}
				} else {
					String inputName = operation.getInput()!=null ?operation.getInput().getName() : null;
					String outputName = operation.getOutput()!=null ?operation.getOutput().getName() : null;
					portType.removeOperation(opera, inputName, outputName);
					Collection<Binding> bindings = wsdlDef.getBindings().values();
					for (Binding binding : bindings) {
						if (binding.getPortType()==portType) {
							binding.removeBindingOperation(opera, inputName, outputName);
						}
					}
				}
			}
		}
		WSDLWriter writer = WSDLUtil.getWSDLWriter();
		OutputStream fileStream = new FileOutputStream(intfFile);
		try {
			writer.writeWSDL(wsdlDef, fileStream);
		}
		finally {
			fileStream.close();
		}
		
		response.setContentType("text/json; charset=utf-8");
		return "true";
	} finally {}
}

// unit.wsdl中添加公共接口引用
@SuppressWarnings("unchecked")
public String addPublicIntf(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String project = unit.replaceAll("\\/.*", "/");
	String intf =  request.getParameter("intf");
	String intfpath = "../../intf/" + intf;
	String importNs = Namespaces.STATENET_PROJECT + project + "intf/" + intf;
	
	boolean used = false;
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File unitWsdl = new File(unitFolder, "unit.wsdl");
	WSDLReader reader = this.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(unitWsdl, true));
	Map importMap = wsdlDef.getImports();
	if (importMap != null && !importMap.isEmpty()) {
		Set<Map.Entry<?,?>> set = importMap.entrySet();
		for (Iterator<?> iter=set.iterator(); iter.hasNext();) {
			Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();	
			Vector importEl = (Vector)entry.getValue();
			ImportImpl obj = (ImportImpl)importEl.elementAt(0);
			String location = obj.getLocationURI();
			
			if ((intfpath+"/public.wsdl").equals(location)) {
					used = true;
					break;
			}
		}
	}
	
	if (used) {
		throw new Exception("此公共接口已经被该服务单元 ["+ unit.replaceAll(".*?\\/", "") +"] 引用!");
	}

	// 添加引用接口命名空间
	wsdlDef.addNamespace(intf, importNs);
	
	Import importEl = wsdlDef.createImport();
	importEl.setLocationURI(intfpath + "/public.wsdl");
	importEl.setNamespaceURI(importNs);
	wsdlDef.addImport(importEl);
	WSDLWriter writer = WSDLUtil.getWSDLWriter();
	FileOutputStream outStream = new FileOutputStream(new File(unitFolder, "unit.wsdl"));
	try {
		writer.writeWSDL(wsdlDef, outStream);
	} finally {
		outStream.close();
	}
	
	// 读取操作列表
	File intfWsdl = Configuration.getWorkspaceFile(project + File.separator + "intf" + File.separator + intf + File.separator + "public.wsdl");
	JSONArray items = new JSONArray();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	if (intfWsdl.exists()) {
		Definition intfWsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfWsdl, true));
		Map ptMap = intfWsdlDef.getPortTypes();
		if (ptMap != null && !ptMap.isEmpty()) {
			Set<Map.Entry<?,?>> set = ptMap.entrySet();
			for (Iterator<?> iter=set.iterator(); iter.hasNext();) {
				Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
				PortType portType = (PortType)entry.getValue();
				
				List<?> operList = portType.getOperations();
				for (Iterator<?> it = operList.iterator(); it.hasNext(); ) {
					Operation operation = (Operation)it.next();
					String opera = operation.getName();
					File file = new File(intfWsdl.getParentFile(), opera + ".xsd");
					JSONObject item = new JSONObject();
					item.put("interface", portType.getQName().getLocalPart());
					item.put("intfDesc", intfWsdlDef.getDocumentationElement().getTextContent());
					
					item.put("opera", opera);
					org.w3c.dom.Element docuEl = operation.getDocumentationElement();
					item.put("desc", docuEl!=null? DOMUtil.getElementText(docuEl):"");
					item.put("readOnly", true);
					item.put("isPublic", true);
					if (file.exists()) 
						item.put("lastModified", dateFormat.format(new java.util.Date(file.lastModified())));
					items.put(item);
				}
			}
		}
	} else {
		throw new Exception("公共接口定义文件不存在!");
	}
	
	JSONObject result = new JSONObject();
	result.put("items", items);
	result.put("count", items.length());
	response.setContentType("text/json; charset=utf-8");
	return result.toString();
}%>

<%
String operation = request.getParameter("operation");
WebServletResponse responseWrapper = new WebServletResponse(response);

if (operation != null) {
	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[]{HttpServletRequest.class, HttpServletResponse.class});
		String result = (String)method.invoke(this, new Object[]{new WebServletRequest(request), responseWrapper});
		out.clear();
		out.println(result);
	}
	catch (NoSuchMethodException e) {
		responseWrapper.sendError("["+request.getMethod()+"]找不到相应的方法来处理指定的 operation: "+operation);
	}
	catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		if (!(t instanceof FileNotFoundException))
			logger.error("", t);
		responseWrapper.sendError(t.getMessage());
	}
	catch (Exception e) {
		logger.error("", e);
		responseWrapper.sendError(e.getMessage());
	}
} else {
	responseWrapper.sendError("["+request.getMethod()+"]拒绝执行，没有指定 operation 参数");
}
%>