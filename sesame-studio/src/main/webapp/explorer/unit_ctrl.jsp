<%@page import="org.w3c.dom.NamedNodeMap"%>
<%@page import="org.w3c.dom.Node"%>
<%@page import="org.w3c.dom.Attr"%>
<%@page import="org.w3c.dom.NodeList"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="javax.wsdl.*"%>
<%@page import="javax.wsdl.factory.*"%>
<%@page import="javax.wsdl.xml.*"%>
<%@page import="javax.wsdl.extensions.*"%>
<%@page import="javax.wsdl.extensions.soap.*"%>
<%@page import="javax.wsdl.extensions.schema.*"%>
<%@page import="javax.xml.namespace.QName"%>
<%@page import="javax.xml.parsers.*"%>
<%@page import="org.apache.commons.fileupload.*"%>
<%@page import="org.apache.commons.fileupload.disk.*"%>
<%@page import="org.apache.commons.fileupload.servlet.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page
	import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.*"%>
<%@page import="org.json.*"%>
<%@page import="com.sanxing.adp.eclipse.ADPServiceProject"%>
<%@page import="com.sanxing.adp.eclipse.ADPServiceProjectBuilder"%>
<%@page import="com.sanxing.sesame.jaxp.*"%>
<%@page import="com.java2html.Java2HTML"%>
<%@page import="com.sanxing.studio.team.*"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.studio.IllegalNameException"%>
<%@page import="com.ibm.wsdl.extensions.schema.*"%>
<%@page import="com.ibm.wsdl.extensions.soap.*"%>
<%@page import="com.ibm.wsdl.ImportImpl"%>
<%@page import="java.net.*"%>
<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>
<%!private static final String BINDING_FILE = "binding.xml";

private final Logger logger = LoggerFactory.getLogger(this.getClass());

private static final long SIZE_BT=1024L;
private static final long SIZE_KB=SIZE_BT*1024L;
private static final long SIZE_MB=SIZE_KB*1024L;
private static final int SACLE=2;

// 计算文件大小
private String getFileSize(long size) {
	String fileSize = "";
	if(size>=0 && size<SIZE_BT) {
		fileSize = size+"B";
    }else if(size >= SIZE_BT && size<SIZE_KB) {
    	fileSize =  size/SIZE_BT+"KB";
    }else if(size >= SIZE_KB && size<SIZE_MB) {
    	fileSize = size/SIZE_KB+"MB";
    }
	return fileSize;
}

//更改WSDL Definition命名空间
private void alterNamespace(Definition wsdlDef, String targetNamespace) {
	String oldNamespace = wsdlDef.getTargetNamespace();

	wsdlDef.setTargetNamespace(targetNamespace);
	//处理消息
	Map<?,?> messages = wsdlDef.getMessages();
	for (Iterator<?> iter=messages.entrySet().iterator(); iter.hasNext(); ) {
		Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
		Message message = (Message)entry.getValue();
		message.setQName(new QName(targetNamespace, message.getQName().getLocalPart()));
		Map<?,?> parts = message.getParts();
		for (Iterator<?> iterator=parts.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<?,?> partEntry = (Map.Entry<?,?>)iterator.next();
			javax.wsdl.Part part = (javax.wsdl.Part)partEntry.getValue();
			if (part.getElementName()!=null && part.getElementName().getNamespaceURI().equals(oldNamespace)) { 
				part.setElementName(new QName(targetNamespace, part.getElementName().getLocalPart()));
			}
		}
	}

	Map<?,?> portTypes = wsdlDef.getPortTypes();
	for (Iterator<?> iter=portTypes.entrySet().iterator(); iter.hasNext(); ) {
		Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
		PortType portType = (PortType)entry.getValue();
		portType.setQName(new QName(targetNamespace, portType.getQName().getLocalPart()));
	}
	Map<?,?> bindings = wsdlDef.getBindings();
	for (Iterator<?> iter=bindings.entrySet().iterator(); iter.hasNext(); ) {
		Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
		Binding binding = (Binding)entry.getValue();
		binding.setQName(new QName(targetNamespace, binding.getQName().getLocalPart()));
	}
	Map<?,?> services = wsdlDef.getServices();
	for (Iterator<?> iter=services.entrySet().iterator(); iter.hasNext(); ) {
		Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
		Service service = (Service)entry.getValue();
		service.setQName(new QName(targetNamespace, service.getQName().getLocalPart()));
	}
}

private org.w3c.dom.Element createRoute(org.w3c.dom.Document document, String serviceName, String intfName, String operaName){
	org.w3c.dom.Element routeEl = document.createElementNS(Namespaces.SESAME, "route");
	routeEl.setPrefix("sn");
	org.w3c.dom.Element el = document.createElementNS(Namespaces.SESAME, "service-name");
	el.appendChild(document.createTextNode(serviceName));
	routeEl.appendChild(el);
	el = document.createElementNS(Namespaces.SESAME, "interface-name");
	el.appendChild(document.createTextNode(intfName));
	routeEl.appendChild(el);
	el = document.createElementNS(Namespaces.SESAME, "operation-name");
	el.appendChild(document.createTextNode(operaName));
	routeEl.appendChild(el);
   	return routeEl;
}

//生成初始的流程xml文件
private void createXML(File xmlFile) throws Exception{
	OutputStream output = new FileOutputStream(xmlFile);
	try {
		Writer writer = new OutputStreamWriter(output, "utf-8");
		writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		writer.write("<process/>");
		writer.flush();
	}
	finally {
		output.close();
	}
}

/*
生成服务的初始schema
*/
private void createSchema(File schemaFile, String targetUri) throws Exception
{
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

/*
*根据引擎服务的schema生成发布服务的schema
*/
private void generateSchema(File serviceFile, File schemaFile, String targetUri) throws Exception
{
	FileUtil.copyFile(serviceFile, schemaFile);
	SAXBuilder builder = new SAXBuilder();
	Document document = builder.build(schemaFile);
	Element rootEl = document.getRootElement();
	rootEl.setAttribute("targetNamespace", targetUri);
	Namespace xmlns_tns = rootEl.getNamespace("tns");
	if (xmlns_tns != null) {
		rootEl.removeNamespaceDeclaration(xmlns_tns);
		rootEl.addNamespaceDeclaration(Namespace.getNamespace("tns", targetUri));
	}
	
	
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

/**
*获取扩展元素
**/
private org.w3c.dom.Element getExtensionEl(List<?> extensionList) {
	Iterator<?> iter = extensionList.iterator();
	if (iter.hasNext()) {
		ExtensibilityElement extEl = (ExtensibilityElement)iter.next();
		if (extEl instanceof UnknownExtensibilityElement)
			return ((UnknownExtensibilityElement) extEl).getElement();
		else if (extEl instanceof SchemaImpl)
			return ((SchemaImpl) extEl).getElement();
		else
			return null;
	}
	else {
		return null;
	}
}

/**
*生成属性节点
**/
private Element getPropertiesEl(org.w3c.dom.Element el) {
	Element propEl = new DOMBuilder().build(el);
	propEl.setName("properties");
	propEl.setNamespace(Namespace.NO_NAMESPACE);
	Iterator<?> iter = propEl.getDescendants(new org.jdom.filter.Filter(){
		static final long serialVersionUID = 103984713947190L;
		public boolean matches(Object obj) {
			return obj instanceof Element;
		}
	});
	for (; iter.hasNext(); ) {
		((Element)iter.next()).setNamespace(Namespace.NO_NAMESPACE);
	}
	propEl.detach();
	return propEl;
}

private org.w3c.dom.Element createDocumentationEl(org.w3c.dom.Document doc, String documentation) {
	org.w3c.dom.Element element = doc.createElementNS(Namespaces.WSDL1_NAMESPACE, "documentation");
	org.w3c.dom.Text textNode = doc.createTextNode(documentation);
	element.appendChild(textNode);
	return element;
}

private org.w3c.dom.Element createSchemaEl(org.w3c.dom.Document doc, String targetNamespace) {
	org.w3c.dom.Element schemaEl = doc.createElementNS(Namespaces.XSD, "xs:schema");
	//schemaEl.setPrefix("xs");
	schemaEl.setAttribute("attributeFormDefault", "unqualified");
	schemaEl.setAttribute("elementFormDefault", "qualified");
	schemaEl.setAttribute("targetNamespace", targetNamespace);
	return schemaEl;
}

private JSONObject getPortTypeObject(PortType portType) throws JSONException {
	JSONObject result = new JSONObject();
	result.put("name", portType.getQName().getLocalPart());
	JSONArray items = new JSONArray();
	result.put("operations", items);
	List opList = portType.getOperations();
	if (opList != null && !opList.isEmpty()) {
		for (Iterator itr = opList.iterator(); itr.hasNext();) {
			Operation operation = (Operation)itr.next();
			JSONObject opera = new JSONObject();
			opera.put("name", operation.getName());
			opera.put("description", DOMUtil.getElementText(operation.getDocumentationElement()));
			items.put(opera);
		}
	}
	
	return result;
}

private WSDLReader getWSDLReader() throws WSDLException {
	WSDLFactory factory = WSDLFactory.newInstance();
	WSDLReader reader = factory.newWSDLReader();
	reader.setFeature("javax.wsdl.verbose", false);
	reader.setFeature("javax.wsdl.importDocuments", false);
	return reader;
}

@SuppressWarnings("unchecked")
public String load(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		Element unitEl = new Element("unit");
		File unitFolder = Application.getWorkspaceFile(unit);
		File unitFile = new File(unitFolder, "unit.wsdl");
		WSDLFactory factory = WSDLFactory.newInstance();
		WSDLReader reader = factory.newWSDLReader();
		reader.setFeature("javax.wsdl.verbose", false);
		reader.setFeature("javax.wsdl.importDocuments", false);
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(unitFile, true));

		Element attrEl = new Element("attributes");
		unitEl.addContent(attrEl);
		attrEl.setAttribute("name", wsdlDef.getQName().getLocalPart());
		attrEl.setAttribute("namespace", wsdlDef.getTargetNamespace());
		org.w3c.dom.Element docuEl = wsdlDef.getDocumentationElement();
		attrEl.setAttribute("documentation", docuEl != null? DOMUtil.getElementText(docuEl):"");
		    
		Iterator<Binding> bindings = wsdlDef.getBindings().values().iterator();
		if (bindings.hasNext()) {
			Binding binding = bindings.next();
			org.w3c.dom.Element paramsEl = this.getExtensionEl(binding.getExtensibilityElements());
			if (paramsEl != null) {
				unitEl.addContent(getPropertiesEl(paramsEl));
			}
		}
		
		Element endpointsEl = new Element("endpoints");
		unitEl.addContent(endpointsEl);
		
		Iterator<?> services = wsdlDef.getServices().entrySet().iterator();
		if (services.hasNext()) {
			Map.Entry<?,?> entry = (Map.Entry<?,?>)services.next();
			Service service = (Service)entry.getValue();
			attrEl.setAttribute("service-name", service.getQName().getLocalPart());
			Iterator<?> ports = service.getPorts().entrySet().iterator();
			while (ports.hasNext()) {
				Map.Entry<?,?> ent = (Map.Entry<?,?>)ports.next();
				Port port = (Port)ent.getValue();
				//绑定服务单元
				Element endpointEl = new Element("endpoint");
				endpointEl.setAttribute("name", port.getName());
				QName attExt = (QName)port.getExtensionAttribute(new QName(Namespaces.SESAME, "location"));
				endpointEl.setAttribute("location", attExt!=null ? attExt.getLocalPart() : "");
				List<?> extList = port.getExtensibilityElements();
				if (extList.size() > 0) {
					ExtensibilityElement extEl = (ExtensibilityElement)extList.get(0);
					if (extEl instanceof SOAPAddressImpl) {
						SOAPAddressImpl soapAddress = (SOAPAddressImpl)extEl;
						endpointEl.setAttribute("location", soapAddress.getLocationURI());
					}
				}
				endpointsEl.addContent(endpointEl);
			}
		}
		
		// database参数
		File jbiPath = new File(unitFolder, "jbi.xml");
		Dom4jUtil.initDocument(jbiPath);
		
		org.dom4j.Element jbiRoot = Dom4jUtil.getRootEl();
		String component = jbiRoot.getNamespaceForPrefix("comp").getText();

		if (component.indexOf("database") != -1) {
			org.dom4j.Element servicesEl = 
				(org.dom4j.Element) jbiRoot.selectSingleNode(jbiRoot.getPath() + "/*[name()='services']");
			
			String sqlText = "";
			if (servicesEl != null) 
				sqlText = servicesEl.attributeValue("datasource", "");
			
			Element paramEl = new Element("params");
			unitEl.addContent(paramEl);
			
			paramEl.setAttribute("dsn", sqlText);
		}
		
		Document doc = new Document(unitEl);
		XMLOutputter outputter = new XMLOutputter();
		outputter.getFormat().setEncoding(response.getCharacterEncoding());
		return outputter.outputString(doc);
	}
	finally
	{
	}
}

public String loadParams(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try{
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		Element unitEl = new Element("unit");
		
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File wsdlPath = new File(unitFolder, "unit.wsdl");
		Dom4jUtil.initDocument(wsdlPath);
		
		String component = Dom4jUtil.Binding.getName();
		
		// attributes节点
		Element attrEl = new Element("attributes");
		unitEl.addContent(attrEl);
		attrEl.setAttribute("name", Dom4jUtil.getName());
		attrEl.setAttribute("namespace", Dom4jUtil.getNameSpace());
		attrEl.setAttribute("documentation", Dom4jUtil.getDocumentation());
		attrEl.setAttribute("service-name", Dom4jUtil.Service.getName());
		
		// endpoints节点
		Element endpointsEl = new Element("endpoints");
		unitEl.addContent(endpointsEl);
		
		List ports = Dom4jUtil.Service.getPortList();
		if (ports != null && !ports.isEmpty())
			for (Iterator itr = ports.iterator();  itr.hasNext();) {
				org.dom4j.Element port = (org.dom4j.Element)itr.next();
				//绑定服务单元
				Element endpointEl = new Element("endpoint");
				endpointsEl.addContent(endpointEl);
				
				endpointEl.setAttribute("name", Dom4jUtil.Service.getPortAttrValue(port, "name"));
				
				org.dom4j.Element soapAddr = Dom4jUtil.getElement(port, "address");
				if (soapAddr != null) {
					endpointEl.setAttribute("location", soapAddr.attributeValue("location", ""));
					endpointEl.setAttribute("binding", port.attributeValue("binding").replaceAll(".*?:", ""));
				} else {
					endpointEl.setAttribute("location", "");
					endpointEl.setAttribute("binding", "");
				}
			}
		
		Document doc = new Document(unitEl);
		XMLOutputter outputter = new XMLOutputter();
		outputter.getFormat().setEncoding(response.getCharacterEncoding());
		return outputter.outputString(doc);
	}
	finally{
		
	}
}

public String getServiceName(HttpServletRequest request, HttpServletResponse response)  throws Exception { 
	String unit = request.getParameter("unit");
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File file = new File(unitFolder, "unit.wsdl");
	WSDLReader reader = this.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(file, true));
	
	String serviceName = "";
	Iterator<?> services = wsdlDef.getServices().entrySet().iterator();
	if (services.hasNext()) {
		Map.Entry<?,?> entry = (Map.Entry<?,?>)services.next();
		Service service = (Service)entry.getValue();
		serviceName = service.getQName().getLocalPart();
	}
	
	response.setContentType("text/plain;charset=utf-8");
	return serviceName;
}

public String addIntf(HttpServletRequest request, HttpServletResponse response)  throws Exception { 
	String unit = request.getParameter("unit");
	String intfName = request.getParameter("interface");
	try {
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, "unit.wsdl");
		WSDLReader reader = this.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(file, true));
		
		PortType  portType = wsdlDef.createPortType();
		portType.setQName(new QName(wsdlDef.getTargetNamespace(), intfName));
		portType.setUndefined(false);
		wsdlDef.addPortType(portType);
		
		
		WSDLWriter writer = WSDLUtil.getWSDLWriter();
		OutputStream fileStream = new FileOutputStream(file);
		try {
			writer.writeWSDL(wsdlDef, fileStream);
		} finally {
			fileStream.close();
		}
		response.setContentType("text/json; charset=utf-8");
		return "true";
	} finally {}
}

@SuppressWarnings("unchecked")
public String deleteIntf(HttpServletRequest request, HttpServletResponse response)  throws Exception {
	String unit = request.getParameter("unit");
	String intfName = request.getParameter("interface");
	String component = request.getParameter("component").replaceAll(".*/", "");
	boolean isImport = Boolean.valueOf(request.getParameter("isImport").toString());
	String bindingName =  null;
	
	try { 
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, "unit.wsdl");
		
		WSDLReader reader = WSDLUtil.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(file, true));
		
		// 接口为非公共接口时直接删除
		if (!isImport) {
			Map bindingMap = wsdlDef.getAllBindings();
			if (bindingMap != null && !bindingMap.isEmpty())
				for (Iterator itr = bindingMap.entrySet().iterator(); itr.hasNext();) {
					Map.Entry entry = (Map.Entry)itr.next();
					Binding binding = (Binding)entry.getValue();
					String ptName = 
							(binding.getPortType() != null) ? binding.getPortType().getQName().getLocalPart() : "";
					if (intfName.equals(ptName))
						wsdlDef.removeBinding(binding.getPortType().getQName());
				}
			
			PortType portType = 
				wsdlDef.getPortType(new QName(wsdlDef.getTargetNamespace(), intfName));
			if (portType != null) {
				List opList = portType.getOperations();
				if (opList != null && !opList.isEmpty()) {
					Binding binding = 
						wsdlDef.getBinding(new QName(wsdlDef.getTargetNamespace(), component));
					for (Iterator itr = opList.iterator(); itr.hasNext();) {
						Operation operation = (Operation)itr.next();
						if (operation != null) { 
							String opera = operation.getName();
							
							// 删除binding下对应的operation
							if (binding != null) {
								BindingOperation bindingOp = binding.getBindingOperation(opera, null, null);
								if (bindingOp != null) {
									binding.removeBindingOperation(opera, null, null);
								}
							}
							
							Types types = wsdlDef.getTypes();
							org.w3c.dom.NodeList includes = null;
							if (types != null) {
								List list = types.getExtensibilityElements();
								if (list.size() > 0) {
									Schema schema = (Schema)list.get(0);
									includes = schema.getElement().getElementsByTagNameNS("*", "include");
									
									if (includes != null && includes.getLength()>0) {
										for (int i=0; i<includes.getLength(); i++) {
											org.w3c.dom.Element elem = (org.w3c.dom.Element)includes.item(i);
											if (elem.getAttribute("schemaLocation").equals(opera+".xsd")) {
												elem.getParentNode().removeChild(elem);
											}
										}
									}
								}
							}
							
							File xsdfile = new File(unitFolder, opera + ".xsd");
							if (xsdfile.exists()) xsdfile.delete();
							File xmlfile = new File(unitFolder, opera + ".xml");
							if (xmlfile.exists()) xmlfile.delete();
						}
					}
				}
			}
			wsdlDef.removePortType(new QName(wsdlDef.getTargetNamespace(), intfName));
		} else {
			Map importMap = wsdlDef.getImports();
			if (importMap != null && !importMap.isEmpty()) {
				Set<Map.Entry<?,?>> set = importMap.entrySet();
				for (Iterator<?> iter=set.iterator(); iter.hasNext();) {
					Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();	
					Vector importEl = (Vector)entry.getValue();
					ImportImpl obj = (ImportImpl)importEl.elementAt(0);
					String location = obj.getLocationURI();
					
					if (location.matches(".*\\/" + intfName + "/public.wsdl$")) {
							wsdlDef.removeImport(obj);
							break;
					}
				}
			}
		}
		
		WSDLWriter writer = WSDLUtil.getWSDLWriter();
		OutputStream fileStream = new FileOutputStream(file);
		try {
			writer.writeWSDL(wsdlDef, fileStream);
		} finally {
			fileStream.close();
		}
		
		response.setContentType("text/json; charset=utf-8");
		return "true";
	}finally {}
}

@SuppressWarnings("unchecked")
public String createOpera(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
		String intf = request.getParameter("interface");
		String oper = request.getParameter("opera").trim();
		String route = request.getParameter("route");
		String component = request.getParameter("component");
		
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, "unit.wsdl");
		File jbiFile = new File(unitFolder, "jbi.xml");
		
		// 修改jbi.xml文件 for database-ec
		if (component.indexOf("database") != -1) {
			String sql = request.getParameter("sql");
			Dom4jUtil.initDocument(jbiFile);
			org.dom4j.Element jbiRoot = Dom4jUtil.getRootEl();
			org.dom4j.Element servicesEl = Dom4jUtil.JBI.getServices();
			if (servicesEl == null)
				servicesEl = jbiRoot.addElement("services");
		
			Dom4jUtil.addNamespace(servicesEl, "sn", Namespaces.SESAME);
			org.dom4j.Element newLink = servicesEl.addElement("sn:link");
			newLink.addAttribute("operation-name", oper);
			org.dom4j.Element sqlEl = newLink.addElement("sql-text");
			sqlEl.setText(sql);
			Dom4jUtil.saveFile(jbiFile);
		}
		
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
		
		PortType portType = wsdlDef.getPortType(new QName(wsdlDef.getTargetNamespace(), intf));
		if (portType == null) {
			portType = wsdlDef.createPortType();
			portType.setQName(new QName(wsdlDef.getTargetNamespace(), intf));
			portType.setUndefined(false);
			wsdlDef.addPortType(portType);
		}
		
		Operation opera = wsdlDef.createOperation();
		opera.setName(oper);
		opera.setDocumentationElement(createDocumentationEl(document, request.getParameter("desc")));
		opera.setUndefined(false);
		portType.addOperation(opera);
		String namespaceUri = wsdlDef.getTargetNamespace();
		//namespaceUri += (namespaceUri.endsWith("/")?"":"/");
		//create xmlFile
		File xmlFile = new File(unitFolder,oper+".xml");
		File schemaFile = new File(unitFolder, oper+".xsd");
		if (route != null) {
			UnknownExtensibilityElement operaExt = new UnknownExtensibilityElement();
			opera.addExtensibilityElement(operaExt);
			org.w3c.dom.Element routeEl = createRoute(document, request.getParameter("ref-svc"), 
					request.getParameter("ref-intf"), request.getParameter("ref-opera"));
			routeEl.setAttribute("endpoint", route);
			operaExt.setElement(routeEl);
			operaExt.setElementType(new QName(routeEl.getNamespaceURI(), routeEl.getLocalName()));
			if (route.length()>0) {
				File serviceFile = new File(unitFolder, "../../engine/"+route+".xsd");
			
				if (serviceFile.exists()) {
					generateSchema(serviceFile, schemaFile, namespaceUri);
				}
				else {
					createSchema(schemaFile, namespaceUri);
				}
			}
		}
		else {
			createXML(xmlFile);
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
			}
			else {
				schema = new SchemaImpl();
				schema.setRequired(true);
				schema.setElementType(new QName(Namespaces.XSD, "schema"));
				types.addExtensibilityElement(schema);
				org.w3c.dom.Element schemaEl = createSchemaEl(document, targetNamespace);
				schema.setElement(schemaEl);
			}
			
			org.w3c.dom.Element schemaEl = schema.getElement();
			org.w3c.dom.Element importEl = schemaEl.getOwnerDocument().createElementNS(Namespaces.XSD, "include");
			importEl.setPrefix("xs");
			importEl.setAttribute("schemaLocation", opera.getName()+".xsd");
			schemaEl.appendChild(importEl);
		}

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
	}
	finally
	{
	}
}

@SuppressWarnings("unchecked")
public String getInterfaces(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		File unitFolder = Application.getWorkspaceFile(unit);
		File unitFile = new File(unitFolder, "unit.wsdl");
		WSDLReader reader = this.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(unitFile, true));
		JSONArray items = new JSONArray();
		Set<Map.Entry<?,?>> set = wsdlDef.getPortTypes().entrySet();
		for (Iterator<?> iter=set.iterator(); iter.hasNext(); ) {
			Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
			PortType portType = (PortType)entry.getValue();
			JSONObject item = new JSONObject();
			String name = portType.getQName().getLocalPart();
			String desc = portType.getQName().getLocalPart();
			item.put("name", name);
			item.put("label", desc);
			items.put(item);
		}
		JSONObject result = new JSONObject();
		
		if (items.length() == 0) {
			JSONObject item = new JSONObject();
			item.put("name", "INQ");
			item.put("label", "INQ");
			items.put(item);
		}
		
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	}
	finally
	{
	}
}

@SuppressWarnings("unchecked")
public String getAvailServices(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
		SAXBuilder builder = new SAXBuilder();

		File unitFolder = Application.getWorkspaceFile(unit);
		JSONArray items = new JSONArray();
		JSONObject firstItem = new JSONObject();
		firstItem.put("name", "");
		firstItem.put("label", "<自定义>");
		items.put(firstItem);

		File[] folders = new File(unitFolder, "../../engine").listFiles();
		WSDLReader reader = WSDLUtil.getWSDLReader();
		if (folders != null)
		for (int i=0; i<folders.length; i++) {
			File folder = folders[i];
			File unitFile = new File(folder, "unit.wsdl");
			if (!unitFile.exists()) continue;
			Definition wsdlDef = reader.readWSDL(unitFile.toURI().toString());
			Iterator<?> iter = wsdlDef.getServices().entrySet().iterator();
			if (!iter.hasNext()) continue;
			Service service = (Service)((Map.Entry<?,?>)iter.next()).getValue();
			String namespace = wsdlDef.getTargetNamespace();
			Set<Map.Entry<?,?>> set = wsdlDef.getPortTypes().entrySet();
			for (iter=set.iterator(); iter.hasNext(); ) {
				Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
				PortType portType = (PortType)entry.getValue();
				for (Iterator<?> it=portType.getOperations().iterator(); it.hasNext(); ) {
					Operation opera = (Operation)it.next();
					org.w3c.dom.Element docuEl = opera.getDocumentationElement();
					String label = docuEl != null ? DOMUtil.getElementText(docuEl) : null;
					JSONObject item = new JSONObject();
					item.put("service", "");
					item.put("intf", portType.getQName());
					item.put("opera", opera.getName());
					item.put("name", folder.getName()+"/"+opera.getName());
					item.put("label", label!=null ? "["+opera.getName()+"]"+label : opera);
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
	finally
	{
	}
}

public String getLocations(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	String transportType = request.getParameter("transportType");
	
	String style = "local";
	if (unit.indexOf("client") > -1)
		style = "remote";
	
	JSONArray items = new JSONArray();
	try
	{
		SAXBuilder builder = new SAXBuilder();
		File addressFile = Configuration.getAddressBookFile();
		if(addressFile.exists()) {
			Element root  = builder.build(addressFile).getRootElement();
			List<?> list = root.getChildren("location", root.getNamespace());
			for (Iterator<?> iter=list.iterator(); iter.hasNext(); ){
				Element locationEl = (Element)iter.next();
				JSONObject json = new JSONObject();
				String addrStyle = locationEl.getAttributeValue("style");
				if (!style.equals(addrStyle)) continue;
				
				String addrURL = locationEl.getAttributeValue("url");
				URI uri = new URI(addrURL);
				String trans = uri.getScheme();
				if (!transportType.equals(trans)) continue;
				
				json.put("name", locationEl.getAttributeValue("url"));
				json.put("url", locationEl.getAttributeValue("name"));
				items.put(json);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	}finally{}
}

//从lib目录获取类库列表
public String listJars(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try {
		File unitFolder = Application.getWorkspaceFile(unit);
		File libFolder = new File(unitFolder, "lib");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONArray items = new JSONArray();
		
		File[] files = libFolder.listFiles();
		if (files != null && files.length > 0) {
			for (int i=0; i<files.length; i++) {
				File file = files[i];
				JSONObject obj = new JSONObject();
				obj.put("filename", file.getName());
				obj.put("size", file.length());
				obj.put("lastModified", dateFormat.format(new Date(file.lastModified())));
				items.put(obj);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	}
	finally {
		
	}
}

public String loadFile(HttpServletRequest request, HttpServletResponse response)  throws Exception {
	String unit = request.getParameter("unit");
	String pathname = request.getParameter("path");
	
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File file = new File(unitFolder, pathname);
	
	if (pathname.endsWith(".json")) {
		response.setContentType("text/json; charset=utf-8");
	}
	else if (pathname.endsWith(".xml") || pathname.endsWith(".xsd")) {
		response.setContentType("text/xml; charset=utf-8");
	}
	else {
		response.setContentType("text/plain; charset=utf-8");
	}
	
	InputStream input = new FileInputStream(file);
	BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
	try {
		StringWriter result = new StringWriter();
		
		for (String line; (line=reader.readLine())!=null; ) {
			result.append(line);
			result.append("\n");
		}
		return result.toString();
	}
	finally {
		reader.close();
	}
}

//从 unit.wsdl 文件装载服务的接口列表，供服务单元使用
public String loadInterfaces(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try {
		File unitFolder = new File(Application.getWorkspaceRoot(), unit);
		// WSDL文件路径
		File wsdlPath = new File(unitFolder, "unit.wsdl");
		// jbi.xml文件路径
		File jbiPath = new File(unitFolder, "jbi.xml");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONArray items = new JSONArray();
		
		WSDLReader reader = WSDLUtil.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(wsdlPath, true));
		
		Map interfaceMap = wsdlDef.getPortTypes();
		for (Iterator itr = interfaceMap.entrySet().iterator(); itr.hasNext();) {
			Map.Entry entry = (Map.Entry)itr.next();
			PortType portType = (PortType)entry.getValue();
			JSONObject intfObj = this.getPortTypeObject(portType);
			items.put(intfObj);
		}

		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	}
	finally {
		
	}
}


// 从 unit.wsdl 文件装载服务的操作列表，供服务单元使用 
public String loadOperations(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Application.getWorkspaceFile(unit);
		File unitFile = new File(unitFolder, "unit.wsdl");
		// jbi.xml文件路径
		File jbiPath = new File(unitFolder, "jbi.xml");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		WSDLReader reader = WSDLUtil.newWSDLReader();
		reader.setFeature("javax.wsdl.importDocuments", true);
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(unitFile, true));

		Document document = builder.build(unitFile);
		Element rootEl = document.getRootElement();
		JSONArray items = new JSONArray();
		Map<?,?> portTypes = wsdlDef.getAllPortTypes();
		for (Iterator<?> iter=portTypes.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
			PortType portType = (PortType)entry.getValue();
			
			// 判断是否为引用的公共接口
			boolean isPublic = false;
			
			String baseURI = (portType.getDocumentationElement() != null) ? portType.getDocumentationElement().getBaseURI() : "";
			if (baseURI.contains("/intf/"))
				isPublic = true;
			List<?> operList = portType.getOperations();
			for (Iterator<?> it=operList.iterator(); it.hasNext(); ) {
				Operation operation = (Operation)it.next();
				String opera = operation.getName();
				File file = new File(unitFolder, opera+".xsd");
				JSONObject item = new JSONObject();
				item.put("interface", portType.getQName().getLocalPart());
				
				if (portType.getDocumentationElement() != null)
					item.put("intfDesc", DOMUtil.getElementText(portType.getDocumentationElement()));
				else
					item.put("intfDesc", "");
				item.put("opera", opera);
				item.put("isPublic", isPublic);
				item.put("locked", isPublic? "" : LockUtil.isOperaLocked(unit + File.separator + opera + ".xsd"));
				org.w3c.dom.Element docuEl = operation.getDocumentationElement();
				item.put("desc", docuEl!=null? DOMUtil.getElementText(docuEl):"");
				item.put("readOnly", !unitFile.canWrite() || isPublic);
				Iterator<?> extIter = operation.getExtensibilityElements().iterator();
				if (extIter.hasNext()) {
					org.w3c.dom.Element routeEl = ((UnknownExtensibilityElement)extIter.next()).getElement();
					item.put("route", routeEl.getAttribute("endpoint"));
					org.w3c.dom.NodeList list = routeEl.getElementsByTagName("service-name");
					item.put("ref-svc", list.getLength()>0?DOMUtil.getElementText((org.w3c.dom.Element)list.item(0)):"");
					list = routeEl.getElementsByTagName("interface-name");
					item.put("ref-intf", list.getLength()>0?DOMUtil.getElementText((org.w3c.dom.Element)list.item(0)):"");
					list = routeEl.getElementsByTagName("operation-name");
					item.put("ref-opera", list.getLength()>0?DOMUtil.getElementText((org.w3c.dom.Element)list.item(0)):"");
				}
				if (file.exists()) item.put("lastModified", dateFormat.format(new java.util.Date(file.lastModified()))  );
				items.put(item);
			}
		}
		
		
		// proerties from jbi.xml
		Dom4jUtil.initDocument(jbiPath);
		org.dom4j.Element services = Dom4jUtil.JBI.getServices();
		for(int j =0; j<items.length(); j++) {
			JSONObject item = items.getJSONObject(j);
			String operName = item.optString("opera");
			org.dom4j.Element linkEl = 
				(org.dom4j.Element)services.selectSingleNode("//*[local-name()='link'][@operation-name='"+ operName + "']");
			if (linkEl != null) {
				String sqlText = linkEl.elementTextTrim("sql-text");
				item.put("sql", sqlText);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	}
	finally
	{
	}
}

@SuppressWarnings("unchecked")
public String modifyOpera(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	String intf = request.getParameter("old-intf");
	String oper = request.getParameter("old-opera").trim();
	String newOpera = request.getParameter("opera").trim();
	String component = request.getParameter("component");

	File unitFolder = Application.getWorkspaceFile(unit);
	File unitFile = new File(unitFolder, "unit.wsdl");
	File jbiFile = new File(unitFolder, "jbi.xml");
	
	// 从SVN改名
	File projectFolder = unitFolder.getParentFile().getParentFile();
	ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
	
	// 修改jbi.xml文件
	if (component.indexOf("database") != -1) {
		String sql = request.getParameter("sql");
		Dom4jUtil.initDocument(jbiFile);
		org.dom4j.Element jbiRoot = Dom4jUtil.getRootEl();
		org.dom4j.Element servicesEl = Dom4jUtil.JBI.getServices();
		if (servicesEl == null) 
			servicesEl = jbiRoot.addElement("services");
		
		org.dom4j.Element link = 
			(org.dom4j.Element)servicesEl.selectSingleNode("//*[local-name()='link'][@operation-name='"+ oper + "']");
		Dom4jUtil.addNamespace(servicesEl, "sn", Namespaces.SESAME);
		if (link == null) {
			org.dom4j.Element newLink = servicesEl.addElement("sn:link");
			newLink.addAttribute("operation-name", newOpera);
			org.dom4j.Element sqlEl = newLink.addElement("sql-text");
			sqlEl.setText(sql);
			Dom4jUtil.saveFile(jbiFile);
		} else {
			String value = Dom4jUtil.getAttributeValue(link, "operation-name");
			if (oper.equals(value)) {
				org.dom4j.Element sqlEl = 
					(org.dom4j.Element)link.selectSingleNode("//*[local-name()='link'][@operation-name='" + oper + "']/*[name()='sql-text']");
				sqlEl.setText(sql);
				link.addAttribute("operation-name", newOpera);
				Dom4jUtil.saveFile(jbiFile);
			}
		}
	}
	
	WSDLReader reader = this.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(unitFile, true));
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
	
	PortType portType = wsdlDef.getPortType(new QName(wsdlDef.getTargetNamespace(), intf));
	Operation opera = null;
	if (portType==null || (opera=portType.getOperation(oper, null, null))==null) {
		throw new IllegalNameException("操作不存在，可能已被删除");
	}
	else {
		portType.setUndefined(false);
		opera.setUndefined(false);
		opera.setName(request.getParameter("opera"));
		opera.setDocumentationElement(createDocumentationEl(document, request.getParameter("desc")));
	   	List<?> list = opera.getExtensibilityElements();
	   	UnknownExtensibilityElement extElement;
	   	if (list.size() > 0) {
	   		extElement = (UnknownExtensibilityElement)list.get(0);
	   	} else {
	   		extElement = new UnknownExtensibilityElement();
	   		opera.addExtensibilityElement(extElement);
	   	}
	   	if (request.getParameter("route") != null) {
		   	org.w3c.dom.Element routeEl = createRoute(document, request.getParameter("ref-svc"), 
		   			request.getParameter("ref-intf"), request.getParameter("ref-opera"));
			routeEl.setAttribute("endpoint", request.getParameter("route"));
		   	extElement.setElement(routeEl);
		   	extElement.setElementType(new QName(routeEl.getNamespaceURI(), routeEl.getLocalName()));
	   	}
	   	
	   	String newIntf = request.getParameter("interface");
		if (!intf.equals(newIntf)) {
			PortType intfType = wsdlDef.getPortType(new QName(wsdlDef.getTargetNamespace(), newIntf));
			if (intfType == null) {
				intfType = wsdlDef.createPortType();
				intfType.setQName(new QName(wsdlDef.getTargetNamespace(), newIntf));
				intfType.setUndefined(false);
				wsdlDef.addPortType(intfType);
			}
	   		portType.removeOperation(request.getParameter("opera"), null, null);
	   		intfType.addOperation(opera);
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
			
			NodeList includes = el.getElementsByTagName("*");
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
	OutputStream fileStream = new FileOutputStream(unitFile);
	try {
		writer.writeWSDL(wsdlDef, fileStream);
	}
	finally {
		fileStream.close();
	}
	File srcFile = new File(unitFolder, oper+".xsd");
	File destFile = new File(unitFolder, request.getParameter("opera")+".xsd");
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
		}
		finally {
			output.close();
		}
	}
	srcFile = new File(unitFolder, oper+".xml");
	destFile = new File(unitFolder, request.getParameter("opera")+".xml");
	if (srcFile.exists()) {
		LockUtil.renameFromSvn(sync, srcFile, destFile);
		srcFile.renameTo(destFile);
	}
	
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

public String removeJar(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String data = request.getParameter("data");
	
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File libFolder = new File(unitFolder, "lib");
	JSONArray array = new JSONArray(data);
	for (int i=0,len=array.length(); i<len; i++) {
		JSONObject obj = array.getJSONObject(i);
		File jarFile = new File(libFolder, obj.getString("filename"));
		obj.put("success", jarFile.delete());
	}
	return array.toString();
}

public String removeOpera(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("operas");
	JSONArray operas = new JSONArray(data);
	String unit = request.getParameter("unit");
	String component = request.getParameter("component");
	try
	{
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		File unitFolder = Application.getWorkspaceFile(unit);
		File unitFile = new File(unitFolder, "unit.wsdl");
		File jbiFile = new File(unitFolder, "jbi.xml");
		
		// 从SVN删除文件
		File projectFolder = unitFolder.getParentFile().getParentFile();
		ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
		
		// 修改jbi.xml文件
		if (component.indexOf("database") != -1) {
			Dom4jUtil.initDocument(jbiFile);
			List artLinkList = Dom4jUtil.JBI.getArtLinkList();
			if (artLinkList != null && !artLinkList.isEmpty()) {
				for (int k=0,len=operas.length(); k<len; k++) {
					JSONObject operaObj = operas.getJSONObject(k);
					String opera = operaObj.getString("opera");
					
					for (int m=0; m<artLinkList.size(); m++) {
						org.dom4j.Element link = (org.dom4j.Element) artLinkList.get(m);
						String value = Dom4jUtil.getAttributeValue(link, "operation-name");
						if (opera.equals(value)) {
							link.getParent().remove(link);
							break;
						}
					}
				}
				Dom4jUtil.saveFile(jbiFile);
			}
		}
		
		
		// 修改wsdl文件
		WSDLReader reader = WSDLUtil.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(unitFile, true));
	
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
			String intf = operaObj.getString("interface");
			String opera = operaObj.getString("opera");
			
			if (includes != null && includes.getLength()>0) {
				for (int n=0; i<includes.getLength(); n++) {
					org.w3c.dom.Element elem = (org.w3c.dom.Element)includes.item(n);
					if (elem.getAttribute("schemaLocation").equals(opera+".xsd")) {
						elem.getParentNode().removeChild(elem);
						break;
					}
				}
			}
			
			PortType portType = wsdlDef.getPortType(new QName(wsdlDef.getTargetNamespace(), intf));
			Operation operation;
			if (portType != null && (operation=portType.getOperation(opera, null, null))!=null) {
				File xsdfile = new File(unitFolder, opera+".xsd");
				if (xsdfile.exists()) {
					LockUtil.deleteFromSvn(sync, xsdfile);
					xsdfile.delete();
				}
				
				File xmlfile = new File(unitFolder, opera+".xml");
				if (xmlfile.exists()) {
					LockUtil.deleteFromSvn(sync, xmlfile);
					xmlfile.delete();
				}
				
				if (portType.getOperations().size()==1) {
					wsdlDef.removePortType(portType.getQName());
					Collection<Binding> bindings = wsdlDef.getBindings().values();
					for (Binding binding : bindings) {
						if (binding.getPortType()==portType) {
							binding.setPortType(null);
						}
					}
				}
				else {
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
		OutputStream fileStream = new FileOutputStream(unitFile);
		try {
			writer.writeWSDL(wsdlDef, fileStream);
		}
		finally {
			fileStream.close();
		}
		response.setContentType("text/json; charset=utf-8");
		return "true";
	}
	finally
	{
	}
}

public String saveBinding(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	request.setCharacterEncoding("utf-8");
	String data = request.getParameter("data");
	String unit = request.getParameter("unit");
	
	InputSource input = new InputSource(new StringReader(data));
	input.setEncoding(request.getCharacterEncoding());
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();
	org.w3c.dom.Document document = builder.parse(input);

	File unitFolder = Configuration.getWorkspaceFile(unit);
	File unitFile = new File(unitFolder, "unit.wsdl");
	WSDLReader reader = WSDLUtil.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(unitFile, true));
	org.w3c.dom.Element unitEl = document.getDocumentElement();
	org.w3c.dom.Element attEl = (org.w3c.dom.Element)unitEl.getElementsByTagName("attributes").item(0);
	String targetNamespace = attEl.getAttribute("namespace");
	wsdlDef.addNamespace("tns", targetNamespace);
	wsdlDef.addNamespace("sn", Namespaces.SESAME);
	wsdlDef.addNamespace("xs", Namespaces.XSD);
	wsdlDef.addNamespace("soap", Namespaces.SOAP_NAMESPACE);
	wsdlDef.addNamespace("", Namespaces.WSDL1_NAMESPACE);
	wsdlDef.setQName(new QName(targetNamespace, unitFolder.getName()));
	wsdlDef.setDocumentationElement(createDocumentationEl(document, attEl.getAttribute("documentation")));
	if (!targetNamespace.equals(wsdlDef.getTargetNamespace())) {
		alterNamespace(wsdlDef, targetNamespace);
	}
	//处理类型定义
	Types types = wsdlDef.getTypes();
	if (types != null) {
		List<?> exts = types.getExtensibilityElements();
		for (Iterator<?> iter=exts.iterator(); iter.hasNext(); ) {
			ExtensibilityElement extEl = (ExtensibilityElement)iter.next();
			if (extEl instanceof Schema) {
				org.w3c.dom.Element el = ((Schema)extEl).getElement();
				el.setPrefix("xs");
				el.setAttribute("targetNamespace", targetNamespace);
			}
		}
	}
	else {
		types = wsdlDef.createTypes();
		Schema schema = new SchemaImpl();
		schema.setElement(createSchemaEl(document, targetNamespace));
		schema.setElementType(new QName(Namespaces.XSD, "schema"));
		types.addExtensibilityElement(schema);
		wsdlDef.setTypes(types);
	}

	QName serviceName = new QName(targetNamespace, attEl.getAttribute("service-name"));
	Iterator<?> services = wsdlDef.getServices().entrySet().iterator();
	Service service;
	if (services.hasNext()) {
		Map.Entry<?,?> entry = (Map.Entry<?,?>)services.next();
		service = (Service)entry.getValue();
	}
	else {
		service = wsdlDef.createService();
		wsdlDef.addService(service);
	}
	service.setQName(serviceName);

	Binding binding;
	Iterator<?> bindingIt = wsdlDef.getBindings().entrySet().iterator();
	if (bindingIt.hasNext()) {
		Map.Entry<?,?> entry = (Map.Entry<?,?>)bindingIt.next();
		binding = (Binding)entry.getValue();
	}
	else {
		binding = wsdlDef.createBinding();
		binding.setUndefined(false);
		wsdlDef.addBinding(binding);
		SOAPBinding soapBind = new SOAPBindingImpl();
		soapBind.setStyle("document");
		soapBind.setTransportURI("http://schemas.xmlsoap.org/soap/http");
		binding.addExtensibilityElement(soapBind);
	}
	String component = unitEl.getAttribute("component");
	binding.setQName(new QName(targetNamespace, new File(component).getName()));
	
	
	Iterator<?> portNames=service.getPorts().keySet().iterator();
	while (portNames.hasNext()) {
		String name = (String)portNames.next();
		service.removePort(name);
	}
	org.w3c.dom.Element endpointsEl = (org.w3c.dom.Element)unitEl.getElementsByTagName("endpoints").item(0);
	org.w3c.dom.NodeList epList = endpointsEl.getElementsByTagName("endpoint");
	for (int i=0,len=epList.getLength(); i<len; i++) {
		org.w3c.dom.Element epEl = (org.w3c.dom.Element)epList.item(i);
		Port port = wsdlDef.createPort();
		port.setName(epEl.getAttribute("name"));
		SOAPAddress soapAddr = new SOAPAddressImpl();
		soapAddr.setLocationURI(epEl.getAttribute("location"));
		soapAddr.setRequired(true);
		port.addExtensibilityElement(soapAddr);
		port.setBinding(binding);
		service.addPort(port);
	}
	WSDLWriter writer = WSDLUtil.getWSDLWriter();
	OutputStream output = new FileOutputStream(unitFile);
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


public String saveFile(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	request.setCharacterEncoding("utf-8");
	String unit =request.getParameter("unit");
	String pathname =request.getParameter("path");
	String data =  request.getParameter("data");

	File unitFolder = Configuration.getWorkspaceFile(unit);
	File file = new File(unitFolder, pathname);
	
	OutputStream output = new FileOutputStream(file);
	try {
		output.write(data.getBytes("utf-8"));
	} 
	finally {
		output.close();
	}
	
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

public String savePortTypes(HttpServletRequest request, HttpServletResponse response) throws Exception {
	request.setCharacterEncoding("utf-8");
	String data = URLDecoder.decode(request.getParameter("data"), "UTF-8");
	String unit = request.getParameter("unit");
	
	InputSource input = new InputSource(new StringReader(data));
	input.setEncoding(request.getCharacterEncoding());
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();
	org.w3c.dom.Document document = builder.parse(input);
	org.w3c.dom.Element inputRootEl = document.getDocumentElement();
	org.w3c.dom.NodeList inputPtList =  inputRootEl.getElementsByTagName("portType");

	File unitFolder = Configuration.getWorkspaceFile(unit);
	File unitFile = new File(unitFolder, "unit.wsdl");
	WSDLReader reader = this.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(unitFile, true));
	
	for (int i = 0,len = inputPtList.getLength(); i<len; i++) {
		org.w3c.dom.Element inputPortTypeEl = (org.w3c.dom.Element)inputPtList.item(i);
		String name = inputPortTypeEl.getAttribute("intfName");
		String desc = inputPortTypeEl.getAttribute("intfDesc");
		PortType ptEl = wsdlDef.getPortType(new QName(wsdlDef.getTargetNamespace(), name));
		if (ptEl != null) {
			org.w3c.dom.Element ptDoc = ptEl.getDocumentationElement();
			if (ptDoc == null) {
				ptDoc = document.createElement("documentation");
				ptEl.setDocumentationElement(ptDoc);
			}
			DOMUtil.clearContent(ptDoc);
			Node node = 
				ptDoc.getOwnerDocument().importNode(document.createTextNode(desc).cloneNode(true), true);
			ptDoc.appendChild(node);
		}
	}
	
	WSDLWriter writer = WSDLUtil.getWSDLWriter();
	OutputStream output = new FileOutputStream(unitFile);
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

public String saveInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	request.setCharacterEncoding("utf-8");
	String data = URLDecoder.decode(request.getParameter("data"), "UTF-8");
	String unit = request.getParameter("unit");

	InputSource input = new InputSource(new StringReader(data));
	input.setEncoding(request.getCharacterEncoding());
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();
	org.w3c.dom.Document document = builder.parse(input);

	File unitFolder = Application.getWorkspaceFile(unit);
	File unitFile = new File(unitFolder, "unit.wsdl");
	File jbiFile = new File(unitFolder, "jbi.xml");
	
	org.w3c.dom.Element unitEl = document.getDocumentElement();
	org.w3c.dom.Element attEl = (org.w3c.dom.Element)unitEl.getElementsByTagName("attributes").item(0);
	String targetNamespace = attEl.getAttribute("namespace");
	String component = unitEl.getAttribute("component");
	
	// database-ec组件数据源设置
	if (component.indexOf("database") != -1) {
		org.w3c.dom.Element paramsEl = 
			(org.w3c.dom.Element)unitEl.getElementsByTagName("params").item(0);
		String dsn = paramsEl.getAttribute("dsn");
		
		Dom4jUtil.initDocument(jbiFile);
		org.dom4j.Element jbiRoot = Dom4jUtil.JBI.getRoot();
		org.dom4j.Element servicesEl = Dom4jUtil.JBI.getServices();
		if (servicesEl == null) 
			servicesEl = jbiRoot.addElement("services");
		
		servicesEl.addAttribute("binding-component", "false");
		servicesEl.addAttribute(new org.dom4j.QName("datasource", new org.dom4j.Namespace("sn", Namespaces.SESAME)), dsn);
		Dom4jUtil.saveFile(jbiFile);
	}
	
	WSDLReader reader = WSDLUtil.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(unitFile, true));
	
	wsdlDef.addNamespace("tns", targetNamespace);
	wsdlDef.addNamespace("sn", Namespaces.SESAME);
	wsdlDef.addNamespace("xs", Namespaces.XSD);
	wsdlDef.addNamespace("", Namespaces.WSDL1_NAMESPACE);
	wsdlDef.setQName(new QName(targetNamespace, unitFolder.getName()));
	wsdlDef.setDocumentationElement(createDocumentationEl(document, attEl.getAttribute("documentation")));

	//处理类型定义
	Types types = wsdlDef.getTypes();
	if (types != null) {
		List<?> exts = types.getExtensibilityElements();
		for (Iterator<?> iter=exts.iterator(); iter.hasNext(); ) {
			ExtensibilityElement extEl = (ExtensibilityElement)iter.next();
			if (extEl instanceof Schema) {
				org.w3c.dom.Element el = ((Schema)extEl).getElement();
				el.setPrefix("xs");
				el.setAttribute("targetNamespace", targetNamespace);
				
				NodeList includeList = el.getChildNodes();
				if (includeList != null && includeList.getLength() > 0) {
					for (int i=0; i<includeList.getLength(); i++) {
						Node includeNode = includeList.item(i);
						NamedNodeMap attrs = includeNode.getAttributes();
						if (attrs != null && attrs.getLength() > 0) {
							Node schemaLocation = attrs.getNamedItem("schemaLocation");
							String schemaName = schemaLocation.getNodeValue();
							File schemaFile =  new File(unitFolder, schemaName);
							if (schemaFile.exists()) {
								SAXBuilder saxbuilder = new SAXBuilder();
								Document doc = saxbuilder.build(schemaFile);
								Element rootEl = doc.getRootElement();
								rootEl.setAttribute("targetNamespace", targetNamespace);
								Namespace xmlns_tns = rootEl.getNamespace("tns");
								if (xmlns_tns != null) rootEl.removeNamespaceDeclaration(xmlns_tns);
								xmlns_tns = Namespace.getNamespace("tns", targetNamespace);
								rootEl.addNamespaceDeclaration(xmlns_tns);
								
								XMLOutputter outputter = new XMLOutputter();
								outputter.setFormat(Format.getPrettyFormat().setEncoding("utf-8").setIndent("  "));
								OutputStream output = new FileOutputStream(schemaFile);
								try {
									outputter.output(doc, output);
								} finally {
									output.close();
								}
							}
						}
					}
				}
			}
		}
	} else {
		types = wsdlDef.createTypes();
		Schema schema = new SchemaImpl();
		schema.setElement(createSchemaEl(document, targetNamespace));
		schema.setElementType(new QName(Namespaces.XSD, "schema"));
		types.addExtensibilityElement(schema);
		wsdlDef.setTypes(types);
	}
	if (!targetNamespace.equals(wsdlDef.getTargetNamespace())) {
		alterNamespace(wsdlDef, targetNamespace);
	}
	
	Binding binding;
	Iterator<?> bindingIt = wsdlDef.getBindings().entrySet().iterator();
	if (bindingIt.hasNext()) {
		Map.Entry<?,?> entry = (Map.Entry<?,?>)bindingIt.next();
		binding = (Binding)entry.getValue();
	}
	else {
		binding = wsdlDef.createBinding();
		binding.setUndefined(false);
		wsdlDef.addBinding(binding);
	}
	binding.setQName(new QName(targetNamespace, new File(component).getName()));
	
	Iterator<?> extIter = binding.getExtensibilityElements().iterator();
	UnknownExtensibilityElement bindingExt;
	if (extIter.hasNext()) {
		bindingExt = (UnknownExtensibilityElement)extIter.next();
	}
	else {
		bindingExt = new UnknownExtensibilityElement();
		binding.addExtensibilityElement(bindingExt);
	}
	org.w3c.dom.Element paramsEl = document.createElementNS(Namespaces.SESAME, "params");
	paramsEl.setPrefix("sn");
	bindingExt.setElement(paramsEl);
	bindingExt.setElementType(new QName(paramsEl.getNamespaceURI(), paramsEl.getLocalName()));

	org.w3c.dom.Element propEl = (org.w3c.dom.Element)unitEl.getElementsByTagName("properties").item(0);
	if (propEl != null) {
		org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList) propEl.getChildNodes();
		for (; nodes.getLength()>0; ) {
			org.w3c.dom.Node node = (org.w3c.dom.Node)nodes.item(0);
			paramsEl.appendChild(node);
		}
	}
	
	WSDLWriter writer = WSDLUtil.getWSDLWriter();
	OutputStream fileStream = new FileOutputStream(unitFile);
	try {
		writer.writeWSDL(wsdlDef, fileStream);
	}
	finally {
		fileStream.close();
	}
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

@SuppressWarnings("unchecked")
public String createProject(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	File unitFolder = Configuration.getWorkspaceFile(unit);
	
	String projectName = "Adp_" + unit.replaceAll(".*?\\/", "");
	
	if (unitFolder.exists() && unitFolder.isDirectory()) {
		ADPServiceProject adpProject = new ADPServiceProject(projectName, unitFolder.getAbsolutePath());
	}
	
	response.setContentType("text/palin; charset=utf-8");
	return "true";
}

public String getProjectTree(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String path = request.getParameter("path").replaceAll("/+", "/");
	String node = request.getParameter("node");
	String filePath = "Adp_" + unit.replaceAll(".*?\\/", "") + path;
	
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File entry = new File(unitFolder, filePath);
	
	JSONArray result = new JSONArray();
	if (entry.exists() && entry.isDirectory()) {
		File[] files = entry.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isHidden()) continue;
			if ("bin".equals(file.getName())) continue;
			if ("classes".equals(file.getName())) continue;
			if ("docs".equals(file.getName())) continue;
			if (file.getName().startsWith(".")) continue;
			if ("build.xml".equals(file.getName())) continue;
			if ("pom.xml".equals(file.getName())) continue;
			if (file.getName().endsWith(".class") || file.getName().endsWith(".html") || file.getName().endsWith(".css")) continue;
			
			
			if (file.isDirectory()) {
				if (file.listFiles().length > 0) {
					JSONObject item = new JSONObject();
					item.put("text", file.getName());
					item.put("name", file.getName());
					item.put("allowRemove", false);
					item.put("leaf", false);
					result.put(item);
				}
			} else if (file.isFile()) {
				JSONObject item = new JSONObject();
				item.put("text", file.getName());
				String desc = "", filename = file.getName();
				item.put("name", file.getName());
				item.put("qtip", desc != null ? desc : "");
				item.put("leaf", true);
				item.put("allowRemove", false);
				result.put(item);
			}
		}
	}
	
	response.setContentType("text/json; charset=utf-8");
	return result.toString();
}

public String javaToHtml(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String projectName = "Adp_" + unit.replaceAll(".*?\\/", "");
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File projectFolder = new File(unitFolder, projectName);
	
	if (projectFolder.exists()) {
		Java2HTML java2HTML = new Java2HTML();
		java2HTML.setTitle("");
		java2HTML.setDestination(new File(projectFolder, "bin/src").getAbsolutePath());
		java2HTML.setMarginSize(4);
	    java2HTML.setTabSize(4);
	    java2HTML.setFooter(false);
	    
	    String[] javaSources = new String[] {projectFolder.getAbsolutePath()};
	    java2HTML.setJavaDirectorySource(javaSources);
	    java2HTML.buildJava2HTML();
	}
	
	File styleFille = new File(projectFolder, "bin/src/stylesheet.css");
	if (styleFille.exists()) {
		BufferedWriter writer = null;
		FileWriter fw = null;
		try {
			String fontSize = "body {font-size:14px;}";
			fw =  new FileWriter(styleFille, true);
			writer = new BufferedWriter(fw);
			writer.newLine();
			writer.append(fontSize);
			writer.flush();
		} finally {
			if (writer != null)
				writer.close();
			if (fw != null)
				fw.close();
		}
	}
	
	response.setContentType("text/plain; charset=utf-8");
	return "true";
}

@SuppressWarnings("unchecked")
public String showFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String path = request.getParameter("path").replaceAll("/+", "/");
	path = (path.indexOf("/src/main/java") > -1) ? path.replace( "/src/main/java", "/bin/src" ) : path;
	String node = request.getParameter("node");
	String filePath = "Adp_" + unit.replaceAll(".*?\\/", "") + path + ".html";
	if (filePath.indexOf("checkstyle_report") != -1) {
		filePath = filePath.replaceAll("\\.xml", "");
	}

	File unitFolder = Configuration.getWorkspaceFile(unit);
	File file = new File(unitFolder, filePath);
	
	StringBuilder content = new StringBuilder();
	String fileName = file.getName();
	if (file.exists() && file.isFile() && fileName.endsWith(".html")) {
		File htmlFile = Configuration.getWorkspaceFile(unit + "/" + filePath);
		content.append(htmlFile.getAbsolutePath());
	}
	
	response.setContentType("text/plain; charset=utf-8");
	return content.toString();
}

public String createReport(HttpServletRequest request, HttpServletResponse response) throws Exception { 
	String unit = request.getParameter("unit");
	String project = request.getParameter("project");
	ADPServiceProjectBuilder adp = new ADPServiceProjectBuilder();
	String path = unit + "/" + project + "/build.xml" ;
	File file = Configuration.getWorkspaceFile(path);
	if (file.exists())
		adp.buildProject(file.getAbsolutePath(), "checkstyle");
	
	response.setContentType("text/plain; charset=utf-8");
	return "true";
}

public String getAdpSvnPath(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String project = request.getParameter("project");
	String result = null;
	
	File location = Application.getWorkspaceFile(unit + File.separator + project);
	Map info = SCM.getVersionInfo(location);
	if (info != null) {
        result = "<b>版本库地址</b>: " + info.get("url");
        result += "<br/><b>版本号</b>: "+ info.get("revision");
        result += "<br/><b>提交者</b>: "+ info.get("author");
	}
	
	return result;
}

private File[] getXSDFiles(File path) throws Exception{
	File[] files = path.listFiles(new FileFilter(){
		public boolean accept(File file) {
			return file.getName().endsWith(".xsd");
		}
	});
	
	return files;
}

//锁定服务单元 
@SuppressWarnings("unchecked")
public String unitLock(HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("text/plain;charset=utf-8");
		
		JSONObject user = Authentication.getCurrentUser();
		String userName = user.optString("userid");
		
		String unit = request.getParameter("unit");
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File wsdlFile = new File(unitFolder, "unit.wsdl");
		
		File projectFolder = unitFolder.getParentFile().getParentFile();
		ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
		Map props = sync.info(wsdlFile);
		String lock = (String)props.get("lock");
		if(null == lock){
			File[] xsdFiles = getXSDFiles(unitFolder);
			for(File f:xsdFiles){
				if(!sync.isVersioned(f))
					continue;
				props = sync.info(f);
				lock = (String)props.get("lock");
				if(null != lock){
					if(!userName.equals((String)props.get("lock.owner"))) {
						throw new Exception("该服务单元对应的操作:["+f.getName().replaceAll("\\.xsd$","")+"],已经被用户:["+props.get("lock.owner")+"]锁定!");
					}
				}
			}
			
			// 锁定服务单元前进行更新操作
			File[] operaFiles = unitFolder.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.getName().endsWith(".xsd") || file.getName().endsWith(".xml") ||  file.getName().endsWith(".wsdl");
				}
			});
			for (File file : operaFiles) {
				sync.update(file, 0);
			}
			sync.lock(wsdlFile,"");
		}else{
			if(userName.equals((String)props.get("lock.owner")))
				return "success";
			else
				throw new Exception("该服务单元对应的WSDL文件:["+wsdlFile.getAbsolutePath()+"], 已经被用户:["+props.get("lock.owner")+"]锁定!");
		}
		return "success";
}

//解锁服务单元 
public String unitUnlock(HttpServletRequest request, HttpServletResponse response) throws Exception{
	String unit = request.getParameter("unit");
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File wsdlFile = new File(unitFolder, "unit.wsdl");
	
	File projectFolder = unitFolder.getParentFile().getParentFile();
	
	ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
	
	if(null == sync ||(!sync.isVersioned(wsdlFile)))
		logger.debug(wsdlFile.getAbsoluteFile() + ",unlocked error.");
	
	Map props = sync.info(wsdlFile);
	String lock = (String)props.get("lock");
	if(lock != null){
	    try{
			sync.commit(unitFolder.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.getName().endsWith(".xsd") || file.getName().endsWith(".xml") || file.getName().endsWith(".wsdl");
				}
			}), "Auto commit by studio");
	    }
		catch (Exception e) {
			//e.printStackTrace();
		}
		
		props = sync.info(wsdlFile);
		lock = (String)props.get("lock");
		if(lock != null) {
			// false: 正常解锁; 
			// true:强制解锁,即可以解其他人加的锁
			sync.unlock(wsdlFile, false);
		}
	}
	response.setContentType("text/plain;charset=utf-8");
	return "success";
}

public String uploadJar(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			
			FileItem uploadItem = (FileItem) params.get("jar-file");
			String filename = uploadItem.getName();
			String unit = (String) params.get("unit");
			
			int index = filename.lastIndexOf(File.separator);
			if (index > -1) {
				filename = filename.substring(index + 1);
			}
			if (!filename.endsWith(".jar")) {
				throw new Exception("类库必须是扩展名为 .jar 的文件");
			}
			File unitFolder = Configuration.getWorkspaceFile(unit);
			if (!unitFolder.exists()) {
				throw new Exception("服务单元不存在，可能已被他人删除");
			}
			File libFolder = new File(unitFolder, "lib");
			if (!libFolder.exists() && !libFolder.mkdir()) {
				throw new Exception("创建 lib 目录失败");
			}
			File jarFile = new File(libFolder, filename);
			if (jarFile.exists()) {
				throw new Exception("类库已经存在，请选择其它类库");
			}

			uploadItem.write(jarFile);
			return "{success: true}";
		}
		catch (Throwable e) {
			logger.debug(e.getMessage(), e);
			return "{success: false, error:\"" + e.getMessage() + "\"}";
		}
	}
	else {
		throw new Exception("只接受类库文件上传请求");
	}
}%>

<%
	String operation = request.getParameter("operation");
	
	if (operation == null) {
		operation = request.getMethod().toLowerCase();
	}
	
	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[]{HttpServletRequest.class, HttpServletResponse.class});
		String result = (String)method.invoke(this, new Object[]{new WebServletRequest(request), new WebServletResponse(response)});
		out.clear();
		out.println(result);
	}
	catch (NoSuchMethodException e) {
		throw new ServletException("["+request.getMethod()+"]找不到相应的方法来处理指定的 operation: "+operation);
	}
	catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		throw new ServletException(t.getMessage(), t);
	}
	catch (Exception e) {
		throw new ServletException(e.getMessage(), e);
	}

%>