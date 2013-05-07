
<%@page import="java.net.URLDecoder"%>
<%@page import="org.dom4j.DocumentHelper"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.sesame.jaxp.DOMUtil"%>
<%@page import="com.sanxing.sesame.transport.Protocols"%>
<%@page import="com.ibm.wsdl.extensions.schema.*"%>
<%@page import="com.ibm.wsdl.extensions.soap.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="javax.wsdl.*"%>
<%@page import="javax.wsdl.xml.*"%>
<%@page import="javax.wsdl.extensions.*"%>
<%@page import="javax.wsdl.extensions.soap.*"%>
<%@page import="javax.wsdl.extensions.schema.Schema"%>
<%@page import="javax.xml.namespace.QName"%>
<%@page import="javax.xml.parsers.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page
	import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.*"%>
<%@page import="org.json.*"%>
<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>
<%!private static final String BINDING_FILE = "binding.xml";

private final Logger logger = LoggerFactory.getLogger(this.getClass());

//生成服务的初始schema
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

//根据引擎服务的schema生成发布服务的schema
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

private org.w3c.dom.Element createDocumentationEl(org.w3c.dom.Element sampleEl, String documentation) {
	org.w3c.dom.Element element = (org.w3c.dom.Element)sampleEl.cloneNode(false);
	org.w3c.dom.Text textNode = element.getOwnerDocument().createTextNode(documentation);
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

private JSONObject getBindingObject(Binding binding) throws JSONException {
	JSONObject result = new JSONObject();
	result.put("name", binding.getQName().getLocalPart());
	JSONArray items = new JSONArray();
	result.put("operations", items);
	List opList = binding.getBindingOperations();
	if (opList != null && !opList.isEmpty()) {
		for (Iterator itr = opList.iterator(); itr.hasNext();) {
			BindingOperation bindingOpera = (BindingOperation)itr.next();
			JSONObject opera = new JSONObject();
			opera.put("name", bindingOpera.getName());
			opera.put("description", DOMUtil.getElementText(bindingOpera.getDocumentationElement()));
			List list = bindingOpera.getExtensibilityElements();
			if (list.size() > 0) {
				ExtensibilityElement extEl = (ExtensibilityElement)list.get(0);
				if (extEl instanceof SOAPOperation) {
					SOAPOperation soapOpera = (SOAPOperation)extEl;
					opera.put("action", soapOpera.getSoapActionURI());
				}
			}
			items.put(opera);
		}
	}
	
	return result;
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

//--------------------------------------------------------------------------------------------------------------

@SuppressWarnings("unchecked")
public String load(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	File unitFolder = Configuration.getWorkspaceFile(unit);
	try{
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		Element unitEl = new Element("unit");
		
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
		
		// properties节点
		Element propEl = new Element("properties");
		unitEl.addContent(propEl);
		propEl.setAttribute("component-name", component);
		propEl.setAttribute("transport", Dom4jUtil.Binding.getTransport());
		
		// endpoints节点
		Element endpointsEl = new Element("endpoints");
		unitEl.addContent(endpointsEl);
		
		List ports = Dom4jUtil.Service.getPortList();
		for (Iterator itr = ports.iterator();  itr.hasNext();) {
			org.dom4j.Element port = (org.dom4j.Element)itr.next();
			//绑定服务单元
			Element endpointEl = new Element("endpoint");
			endpointsEl.addContent(endpointEl);
			
			endpointEl.setAttribute("name", Dom4jUtil.Service.getPortAttrValue(port, "name"));
			
			org.dom4j.Element soapAddr = Dom4jUtil.getElement(port, "address");
			endpointEl.setAttribute("location", Dom4jUtil.getAttributeValue(soapAddr, "location"));
		}
		
		// soap参数
		if (component.indexOf("soap") != -1) {
			Element paramEl = new Element("params");
			unitEl.addContent(paramEl);
			
			paramEl.setAttribute("xpath", Dom4jUtil.Binding.getXpath());
			paramEl.setAttribute("actiontype", Dom4jUtil.Binding.getActionType());
			paramEl.setAttribute("head", Dom4jUtil.Binding.getHead());
		}
		
		Document doc = new Document(unitEl);
		XMLOutputter outputter = new XMLOutputter();
		outputter.getFormat().setEncoding(response.getCharacterEncoding());
		return outputter.outputString(doc);
	}
	finally{}
}

@SuppressWarnings("unchecked")
public String getInterfaces(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try {
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File wsdlPath = new File(unitFolder, "unit.wsdl");
		Dom4jUtil.initDocument(wsdlPath);
		org.dom4j.Element root = Dom4jUtil.getRootEl();
		List portTypes = root.selectNodes("//*[name()='portType']");
		JSONArray items = new JSONArray();
		if (portTypes != null && portTypes.size() > 0)
		for(int i=0; i<portTypes.size(); i++) {
			org.dom4j.Element portType = (org.dom4j.Element) portTypes.get(i);
			JSONObject item = new JSONObject();
			item.put("name", Dom4jUtil.getAttributeValue(portType, "name"));
			item.put("label", Dom4jUtil.getAttributeValue(portType, "name"));
			items.put(item);
		}
		
		if (items.length() == 0) {
			JSONObject item = new JSONObject();
			item.put("name", "INQ");
			item.put("label", "INQ");
			items.put(item);
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	} finally{}
}

@SuppressWarnings("unchecked")
public String getAvailServices(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try {
		SAXBuilder builder = new SAXBuilder();

		File unitFolder = Configuration.getWorkspaceFile(unit);
		JSONArray items = new JSONArray();
		JSONObject firstItem = new JSONObject();
		firstItem.put("name", "");
		firstItem.put("label", "<自定义>");
		items.put(firstItem);

		File[] folders = new File(unitFolder, "../../engine").listFiles();
		if (folders != null)
		for (int i=0; i<folders.length; i++) {
			File folder = folders[i];
			File unitFile = new File(folder, "unit.wsdl");
			if (!unitFile.exists()) continue;
			
			Dom4jUtil.initDocument(unitFile);
			String Qname = "{" +  Dom4jUtil.getNameSpace() + "}";
			List opList = Dom4jUtil.PortType.getOperList();
			if (opList != null && opList.size() > 0)
			for (int j=0; j<opList.size(); j++) {
				org.dom4j.Element operation = (org.dom4j.Element)opList.get(j);
				JSONObject item = new JSONObject();
				
				String label = Dom4jUtil.PortType.getName();
				String opera = Dom4jUtil.getAttributeValue(operation, "name");
				String docName = Dom4jUtil.PortType.getElementValue(operation, "documentation");
				
				item.put("service", "");
				item.put("intf", Qname + Dom4jUtil.PortType.getName());
				item.put("opera", opera);
				item.put("name", folder.getName() + "/" + opera);
				item.put("label", "["+opera+"]" + docName);
				items.put(item);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	} finally{}
}

public String getLocations(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	String protocol = request.getParameter("protocol");
	String style = "local";
	if (unit.indexOf("client") > -1) {
		style = "remote";
	}
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
				String url = locationEl.getAttributeValue("url");
				if (!style.equals(addrStyle)) continue;
				if (!url.startsWith(protocol+"://")) continue;
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

@SuppressWarnings("unchecked")
public String getProtocols(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String component = request.getParameter("component");
	
	try {
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File compBundle = Configuration.getWarehouseFile(component);	// TODO

		Element bindingEl = new Element("binding");
		JSONArray items = new JSONArray();
		if (compBundle.isDirectory()) {
			File bindingXml = new File(compBundle, BINDING_FILE);
			if (bindingXml.exists()) {
				bindingEl = builder.build(bindingXml).getRootElement();
			}
		} 
		else {
			JarFile compJar = new JarFile(compBundle);
			try {
				JarEntry bindingXml = compJar.getJarEntry(BINDING_FILE);
				if (bindingXml != null) {
					InputStream input = compJar.getInputStream(bindingXml);
					bindingEl = builder.build(input).getRootElement();
				}
			}
			finally {
				compJar.close();
			}
		}
		
		Element allowable = bindingEl.getChild("allowable");
		if(allowable != null) {
			List list = allowable.getChildren("protocol");
			for (Iterator<?> iter=list.iterator(); iter.hasNext(); ){
				Element protocol = (Element)iter.next();
				String transport =  protocol.getAttributeValue("name");
				JSONObject json = new JSONObject();
				json.put("scheme", transport);
				items.put(json);
			}
		} 
		else {
			String [] trans = Protocols.list();
			if (trans.length > 0)
			for (String tran : trans) {
				JSONObject json = new JSONObject();
				json.put("scheme", tran);
				items.put(json);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	}
	finally{
		
	}
}

//从 unit.wsdl 文件装载服务的操作列表，供服务单元使用
@SuppressWarnings("unchecked")
public String loadBinding(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try {
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Application.getWorkspaceFile(unit);
		// WSDL文件路径
		File wsdlPath = new File(unitFolder, "unit.wsdl");
		// jbi.xml文件路径
		File jbiPath = new File(unitFolder, "jbi.xml");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONArray items = new JSONArray();
		
		WSDLReader reader = WSDLUtil.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(wsdlPath, true));
		
		Map serviceMap = wsdlDef.getServices();
		if (serviceMap.isEmpty()) {
			Map bindingMap = wsdlDef.getBindings();
			for (Iterator itr = bindingMap.entrySet().iterator(); itr.hasNext();) {
				Map.Entry entry = (Map.Entry)itr.next();
				Binding binding = (Binding)entry.getValue();
				JSONObject bindingObj = getBindingObject(binding);
				items.put(bindingObj);
			}
		}
		else {
			for (Iterator iter = serviceMap.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry)iter.next();
				Service service = (Service)entry.getValue();
				Map portMap = service.getPorts();
				for (Iterator ports = portMap.entrySet().iterator(); ports.hasNext();) {
					Map.Entry portEntry = (Map.Entry)ports.next();
					Port port = (Port)portEntry.getValue();
					
					JSONObject bindingObj = this.getBindingObject(port.getBinding());
					bindingObj.put("portName", port.getName());
					bindingObj.put("address", "");
					Iterator exts = port.getExtensibilityElements().iterator();
					if (exts.hasNext()) {
						ExtensibilityElement extEl = (ExtensibilityElement)exts.next();
						SOAPAddress addr = (SOAPAddress)extEl;
						bindingObj.put("address", addr.getLocationURI());
					}
					items.put(bindingObj);
				}
			}
		}
		
		/*
		// proerties from jbi.xml
		Dom4jUtil.initDocument(jbiPath);
		org.dom4j.Element services = Dom4jUtil.JBI.getServices();
		for(int j =0; j<items.length(); j++) {
			JSONObject item = items.getJSONObject(j);
			String operName = item.optString("opera");
			org.dom4j.Element linkEl = 
				(org.dom4j.Element)services.selectSingleNode("//*[local-name()='link'][@operation-name='"+ operName + "']");
			if (linkEl != null) {
				String route = Dom4jUtil.getAttributeValue(linkEl, "service-unit") + "/" +linkEl.elementText("operation-name");
				if ("/".equals(route)) 
					route = "";
				item.put("route", route);
				item.put("ref-svc", linkEl.element("service-name").getText());
				item.put("ref-intf", linkEl.element("interface-name").getText());
				item.put("ref-opera", linkEl.element("operation-name").getText());
			}
		} 
		*/
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	} 
	finally {
		
	}
}

//从 unit.wsdl 文件装载服务的操作列表，供服务单元使用
@SuppressWarnings("unchecked")
public String loadOperations(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try {
		SAXBuilder builder = new SAXBuilder();
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


@SuppressWarnings("unchecked")
public String deleteIntf(HttpServletRequest request, HttpServletResponse response)  throws Exception {
	String unit = request.getParameter("unit");
	String intfName = request.getParameter("interface");
	String component = request.getParameter("component").replaceAll(".*/", "");
	String bindingName =  null;
	
	try { 
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, "unit.wsdl");
		
		WSDLReader reader = WSDLUtil.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(file, true));
		
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
						BindingOperation bindingOp = binding.getBindingOperation(opera, null, null);
						if (bindingOp != null) {
							binding.removeBindingOperation(opera, null, null);
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
		File unitFolder = Configuration.getWorkspaceFile(unit);
		String wsdlPath = new File(unitFolder, "unit.wsdl").getAbsolutePath();
		Dom4jUtil.initDocument(wsdlPath);
		
		String intf = request.getParameter("interface");
		String oper = request.getParameter("opera").trim();
		
		String route = request.getParameter("route");
		if (route == null) route = "";
		String serviceUnit = "";
		String operationName = "";
		if (!"".equals(route))
			if(route.split("/").length > 0) {
				serviceUnit = route.split("/")[0];
				operationName = route.split("/")[1];
			}
				
		String desc = request.getParameter("desc");
		String refSvc = request.getParameter("ref-svc");
		if (refSvc == null) refSvc = "";
		String refIntf = request.getParameter("ref-intf");
		if (refIntf == null) refIntf = "";
		String refOpera = request.getParameter("ref-opera");
		if (refOpera == null) refOpera = "";
		String soapAction = request.getParameter("action");
		
		// 新增WSDL中binding operation元素
		org.dom4j.Element binding =  Dom4jUtil.Binding.getRoot();
		binding.addAttribute("type", "tns:" + intf);
		org.dom4j.Element optrEl = binding.addElement("operation");
		optrEl.addAttribute("name", oper);
		
		org.dom4j.Element soapOptr = optrEl.addElement("soap:operation");
		soapOptr.addAttribute("soapAction", soapAction);
		soapOptr.addAttribute("style", "document");
		
		org.dom4j.Element inputOptr = optrEl.addElement("input");
		org.dom4j.Element inputSoapBody = inputOptr.addElement("soap:body");
		inputSoapBody.addAttribute("use", "literal");
		
		org.dom4j.Element outputOptr = optrEl.addElement("output");
		org.dom4j.Element outputSoapBody = outputOptr.addElement("soap:body");
		outputSoapBody.addAttribute("use", "literal");
		
		// 新增WSDL中portType operation元素
		org.dom4j.Element newOptrEl =  Dom4jUtil.PortType.addOperation(intf);
		newOptrEl.addAttribute("name", oper);
		org.dom4j.Element doc = newOptrEl.addElement("documentation");
		doc.setText(desc);
		
		Dom4jUtil.PortType.createPutElement(newOptrEl, "input", "tns:" + oper + "Request");
		Dom4jUtil.PortType.createPutElement(newOptrEl, "output", "tns:" + oper + "Response");
		
		// 添加message节点
		Dom4jUtil.Message.createMessage(oper, true);
		Dom4jUtil.Message.createMessage(oper, false);
		
		// 生成schema文件
		String namespaceUri = Dom4jUtil.getAttributeValue(Dom4jUtil.getRootEl(), "targetNamespace");
		File schemaFile = new File(unitFolder, oper+".xsd");
		if (!"".equals(route)) {
			File serviceFile = new File(unitFolder, "../../engine/"+oper+".xsd");
			if (serviceFile.exists()) {
				generateSchema(serviceFile, schemaFile, namespaceUri+(namespaceUri.endsWith("/")?"":"/")+oper);
			}else {
				createSchema(schemaFile, namespaceUri);
			}
		}else {
			createSchema(schemaFile, namespaceUri);
		}
		
		// schema增加include元素
		org.dom4j.Element schema =  Dom4jUtil.Types.getSchema();
		org.dom4j.Namespace xmlns_xs = org.dom4j.Namespace.get("xs", Namespaces.XSD);
		org.dom4j.Element importEl = schema.addElement(new org.dom4j.QName("include", xmlns_xs));
		importEl.addAttribute("schemaLocation", oper + ".xsd");
		
		// 调整binding元素顺序
		org.dom4j.Element tmpBindingEl = (org.dom4j.Element)Dom4jUtil.Binding.getRoot().detach();
		Dom4jUtil.getRootEl().add(tmpBindingEl);
		
		Dom4jUtil.saveFile(wsdlPath);
		
		// 新增jbi.xml中sn:link元素
		File jbiPath = new File(unitFolder, "jbi.xml");
		Dom4jUtil.initDocument(jbiPath);
		org.dom4j.Element root = Dom4jUtil.JBI.getRoot();
		Dom4jUtil.addNamespace(root, "sn", Namespaces.SESAME);
		
		org.dom4j.Element service = Dom4jUtil.JBI.getServices();
		org.dom4j.Element newLink = service.addElement("sn:link");
		newLink.addAttribute("service-unit", serviceUnit);
		newLink.addAttribute("operation-name", oper);
		org.dom4j.Element operationNameEl = newLink.addElement("operation-name");
		operationNameEl.setText(operationName);
		org.dom4j.Element interfaceNameEl = newLink.addElement("interface-name");
		interfaceNameEl.setText(refIntf);
		org.dom4j.Element serviceNameEl = newLink.addElement("service-name");
		serviceNameEl.setText(refSvc);
		org.dom4j.Element epNameEl = newLink.addElement("endpoint-name");
		epNameEl.setText("");
		
		Dom4jUtil.saveFile(jbiPath);
		
		response.setContentType("text/json; charset=utf-8");
		return "true";
	}finally{}
}

@SuppressWarnings("unchecked")
public String modifyOpera(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	File unitFolder = Configuration.getWorkspaceFile(unit);

	File wsdlPath = new File(unitFolder, "unit.wsdl");
	File jbiPath = new File(unitFolder, "jbi.xml");
	Dom4jUtil.initDocument(wsdlPath);
	org.dom4j.Element root = Dom4jUtil.getRootEl();
	String targetNamespace = Dom4jUtil.getAttributeValue(root, "targetNamespace");
	
	String oldIntf = request.getParameter("old-intf");
	String oldOper = request.getParameter("old-opera").trim();
	String newOper = request.getParameter("opera").trim();
	String intf = request.getParameter("interface");
	String desc = request.getParameter("desc");
	String refSvc = request.getParameter("ref-svc");
	if (refSvc == null) refSvc = "";
	String refIntf = request.getParameter("ref-intf");
	if (refIntf == null) refIntf = "";
	String refOpera = request.getParameter("ref-opera");
	if (refOpera == null) refOpera = "";
	String soapAction = request.getParameter("action");
	
	String route = request.getParameter("route");
	if (route == null) route = "";
	String serviceUnit = "";
	String operationName = "";
	if (!"".equals(route))
		if(route.split("/").length > 0) {
			serviceUnit = route.split("/")[0];
			operationName = route.split("/")[1];
		}
	
	// 修改对应的schema文件
	File srcFile = new File(unitFolder, oldOper + ".xsd");
	File destFile = new File(unitFolder, newOper + ".xsd");
	if (srcFile.exists()) {
		srcFile.renameTo(destFile);
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
	srcFile = new File(unitFolder, oldOper + ".xml");
	destFile = new File(unitFolder, newOper + ".xml");
	if (srcFile.exists()) srcFile.renameTo(destFile);
	
	// 修改//schema/include节点中schemaLocation的值
	org.dom4j.Element schema = Dom4jUtil.Types.getSchema();
	List includes = Dom4jUtil.Types.getIncludeList();
	if (includes != null && includes.size() > 0) {
		for (int k=0; k<includes.size(); k++) {
			org.dom4j.Element include = (org.dom4j.Element)includes.get(k);
			String schemaLocation = 
				Dom4jUtil.getAttributeValue(include, "schemaLocation");
			if (oldOper.equals(schemaLocation.replaceAll("\\.\\.\\/", "").replaceAll("\\.xsd", ""))) {
				include.addAttribute("schemaLocation", schemaLocation.replaceAll(oldOper, newOper));
				break;
			}
		}
	}
	
	String portTypeName = Dom4jUtil.PortType.getName();
	List operList = Dom4jUtil.PortType.getOperList();
	List bindingOperList = Dom4jUtil.Binding.getOperList();
	
	if (!intf.equals(portTypeName) || operList == null || operList.size() == 0) {
		WebUtil.sendError(response, "操作不存在，可能已被删除");
		return "";
	} else {	
		// 修改wsdl文件中对应operation元素
		for (int i=0; i<operList.size(); i++) {
			org.dom4j.Element operEl = (org.dom4j.Element) operList.get(i);
			org.dom4j.Element bindingOperEl = null;
			if (bindingOperList.size() > i) {
				bindingOperEl =  (org.dom4j.Element)bindingOperList.get(i);
			}
			else {
				org.dom4j.Element bindingEl = Dom4jUtil.Binding.getRoot();
				bindingOperEl = bindingEl.addElement(new org.dom4j.QName("operation", bindingEl.getNamespace()));
				bindingOperEl.addAttribute("name", operEl.attributeValue("name"));
			}
			String operName = Dom4jUtil.getAttributeValue(operEl, "name");
			String bindingOperName = Dom4jUtil.getAttributeValue(bindingOperEl, "name"); 
			
			if (oldOper.equals(operName) && oldOper.equals(bindingOperName)) {
				Dom4jUtil.setAttributeValue(operEl, "name", newOper);
				org.dom4j.Element doc = 
					(org.dom4j.Element)operEl.selectSingleNode("//*[name()='portType']/*[name()='operation'][@name='" + oldOper + "']/*[name()='documentation']");
				if (doc != null)	doc.setText(desc);
				
				// 修改input/output属性
				org.dom4j.Element inputEl = Dom4jUtil.getElement(operEl, "input");
				inputEl.addAttribute("message", newOper + "Request");
				org.dom4j.Element outputEl = Dom4jUtil.getElement(operEl, "output");
				outputEl.addAttribute("message", newOper + "Response");
				
				Dom4jUtil.setAttributeValue(bindingOperEl, "name", newOper);
				org.dom4j.Element soapOperEl = Dom4jUtil.getElement(bindingOperEl, "operation");
				if (soapOperEl == null) {
					soapOperEl = bindingOperEl.addElement("soap:operation", Namespaces.SOAP_NAMESPACE);
				}
				Dom4jUtil.setAttributeValue(soapOperEl, "soapAction", soapAction);
				break;
			}
		}
		// 修改对应的message元素中的内容
		Dom4jUtil.Message.modifyMessage(oldOper, newOper);
		
		// 调整binding元素顺序
		org.dom4j.Element tmpBindingEl = (org.dom4j.Element)Dom4jUtil.Binding.getRoot().detach();
		Dom4jUtil.getRootEl().add(tmpBindingEl);
		Dom4jUtil.saveFile(wsdlPath);
		
		// 修改jbi文件中相关内容
		Dom4jUtil.initDocument(jbiPath);
		org.dom4j.Element service =  Dom4jUtil.JBI.getServices();
		org.dom4j.Element link = 
			(org.dom4j.Element)service.selectSingleNode("//*[local-name()='link'][@operation-name='"+ oldOper + "']");
		if (link == null) {
			org.dom4j.Element services = Dom4jUtil.JBI.getServices();
			Dom4jUtil.addNamespace(services, "sn", Namespaces.SESAME);
			
			org.dom4j.Element newLink = services.addElement("sn:link");
			newLink.addAttribute("service-unit", serviceUnit);
			newLink.addAttribute("operation-name", newOper);
			org.dom4j.Element operationNameEl = newLink.addElement("operation-name");
			operationNameEl.setText(refOpera);
			org.dom4j.Element interfaceNameEl = newLink.addElement("interface-name");
			interfaceNameEl.setText(refIntf);
			org.dom4j.Element serviceNameEl = newLink.addElement("service-name");
			serviceNameEl.setText(refSvc);
			org.dom4j.Element epNameEl = newLink.addElement("endpoint-name");
			epNameEl.setText("");
			Dom4jUtil.saveFile(jbiPath);
		} else {
				String value = Dom4jUtil.getAttributeValue(link, "operation-name");
				if (oldOper.equals(value)) {
					link.addAttribute("service-unit", serviceUnit);
					link.addAttribute("operation-name", newOper);
					
					org.dom4j.Element operationNameEl = Dom4jUtil.getElement(link, "operation-name");
					operationNameEl.setText(operationName);
					org.dom4j.Element interfaceNameEl = Dom4jUtil.getElement(link, "interface-name");
					interfaceNameEl.setText(refIntf);
					org.dom4j.Element serviceNameEl = Dom4jUtil.getElement(link, "service-name");
					serviceNameEl.setText(refSvc);
					org.dom4j.Element epNameEl = Dom4jUtil.getElement(link, "endpoint-name");
					epNameEl.setText("");
			}
			Dom4jUtil.saveFile(jbiPath);
		}
	}
	
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

@SuppressWarnings("unchecked")
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
	finally{}
}

@SuppressWarnings("unchecked")
public String removeBinding(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	request.setCharacterEncoding("utf-8");
	String unit =request.getParameter("unit");
	String bindingName =  request.getParameter("bindingName");

	File unitFolder = Configuration.getWorkspaceFile(unit);
	File wsdlPath = new File(unitFolder, "unit.wsdl");
	
	WSDLReader reader = WSDLUtil.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(wsdlPath, true));
	
	Service service;
	Map serviceMap = wsdlDef.getServices();
	if (!serviceMap.isEmpty()) {
		Iterator iter = serviceMap.entrySet().iterator();
		Map.Entry entry = (Map.Entry)iter.next();
		service = (Service)entry.getValue();

		Map portMap = new HashMap();
		portMap.putAll(service.getPorts());
		for (Iterator ports=portMap.entrySet().iterator(); ports.hasNext(); ) {
			Map.Entry portEntry = (Map.Entry)ports.next();
			Port port = (Port)portEntry.getValue();
			if (port.getBinding() != null && port.getBinding().getQName().getLocalPart().equals(bindingName)) 
			{
				service.removePort(port.getName());
			}
		}
	}

	QName bindingQName = new QName(wsdlDef.getTargetNamespace(), bindingName);
	
	wsdlDef.removeBinding(bindingQName);
	wsdlDef.removePortType(bindingQName);

	WSDLWriter writer = WSDLUtil.getWSDLWriter();
	OutputStream fileOutput = new FileOutputStream(wsdlPath);
	try {
		writer.writeWSDL(wsdlDef, fileOutput);
	} 
	finally {
		fileOutput.close();
	}
	
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

@SuppressWarnings("unchecked")
public String removeOpera(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("operas");
	String unit = request.getParameter("unit");
	try
	{
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File wsdlPath = new File(unitFolder, "unit.wsdl");
		File jbiPath = new File(unitFolder, "jbi.xml");
		Dom4jUtil.initDocument(wsdlPath);
		org.dom4j.Element root = Dom4jUtil.getRootEl();
		String tns = Dom4jUtil.getAttributeValue(root, "targetNamespace");
		org.dom4j.Element schema = Dom4jUtil.Types.getSchema();
		List includes = Dom4jUtil.Types.getIncludeList();
		
		String portTypeName = Dom4jUtil.PortType.getName();
		List operList = Dom4jUtil.PortType.getOperList();
		List bindingOperList = Dom4jUtil.Binding.getOperList();
		
		JSONArray operas = new JSONArray(data);
		for (int i=0,len=operas.length(); i<len; i++) {
			JSONObject operaObj = operas.getJSONObject(i);
			String opera = operaObj.getString("opera");
			
			File xsdfile = new File(unitFolder, opera+".xsd");
			if (xsdfile.exists()) xsdfile.delete();
			File xmlfile = new File(unitFolder, opera+".xml");
			if (xmlfile.exists()) xmlfile.delete();
			
			// 删除对应的schemaLocation
			if (includes != null && includes.size() > 0) {
				for (int k=0; k<includes.size(); k++) {
					org.dom4j.Element include = (org.dom4j.Element)includes.get(k);
					String schemaLocation = 
						Dom4jUtil.getAttributeValue(include, "schemaLocation");
					if (opera.equals(schemaLocation.replaceAll("\\.\\.\\/", "").replaceAll("\\.xsd", ""))) {
						include.getParent().remove(include);
						break;
					}
				}
			}
			
			//删除message中对应内容
			Dom4jUtil.Message.removeMessage(opera);
			
			for (int j=0; j<operList.size(); j++) {
				org.dom4j.Element operEl = (org.dom4j.Element) operList.get(j);
				org.dom4j.Element bindingOperEl = (org.dom4j.Element)bindingOperList.get(j);
				
				String operName = Dom4jUtil.getAttributeValue(operEl, "name");
				String bindingOperName = Dom4jUtil.getAttributeValue(bindingOperEl, "name"); 
				
				if (opera.equals(operName) && opera.equals(bindingOperName)) {
					operEl.getParent().remove(operEl);
					bindingOperEl.getParent().remove(bindingOperEl);
					break;
				}
			}
		}
		Dom4jUtil.saveFile(wsdlPath);
		
		Dom4jUtil.initDocument(jbiPath);
		List artLinkList = Dom4jUtil.JBI.getArtLinkList();
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
		Dom4jUtil.saveFile(jbiPath);
		
		response.setContentType("text/json; charset=utf-8");
		return "true";
	}
	finally{}
}

@SuppressWarnings("unchecked")
public String saveBinding(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	request.setCharacterEncoding("utf-8");
	String unit =request.getParameter("unit");
	String data =  request.getParameter("data");

	File unitFolder = Configuration.getWorkspaceFile(unit);
	File wsdlPath = new File(unitFolder, "unit.wsdl");
	JSONObject json = new JSONObject(data);
	JSONArray bindings = json.getJSONArray("bindings");
	
	WSDLReader reader = WSDLUtil.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(wsdlPath, true));
	
	Service service;
	Map serviceMap = wsdlDef.getServices();
	if (serviceMap.isEmpty()) {
		service = wsdlDef.createService();
		service.setQName(wsdlDef.getQName());
		wsdlDef.addService(service);
	}
	else {
		Iterator iter = serviceMap.entrySet().iterator();
		Map.Entry entry = (Map.Entry)iter.next();
		service = (Service)entry.getValue();
		if (json.optBoolean("saveAll")) {
			Set portNames = service.getPorts().keySet();
			String[] names = new String[portNames.size()];
			portNames.toArray(names);
			for (int i=0; i<names.length; i++) {
				service.removePort(names[i]);
			}

		}
	}
	
	for (int i=0,len=bindings.length(); i<len; i++) {
		JSONObject bindingObj = bindings.getJSONObject(i);
		QName bindingName = new QName(wsdlDef.getTargetNamespace(), bindingObj.getString("name"));
		wsdlDef.removeBinding(bindingName);
		Binding binding = wsdlDef.createBinding();
		binding.setUndefined(false);
		binding.setQName(bindingName);
		wsdlDef.addBinding(binding);
		
		wsdlDef.removePortType(bindingName);
		PortType portType = wsdlDef.createPortType();
		portType.setUndefined(false);
		portType.setQName(bindingName);
		wsdlDef.addPortType(portType);
		
		binding.setPortType(portType);
		
		JSONArray operations = bindingObj.getJSONArray("operations");
		for (int j=0,count=operations.length(); j<count; j++) {
			JSONObject operationObj = operations.getJSONObject(j);
			Operation operation = wsdlDef.createOperation();
			operation.setName(operationObj.getString("name"));
			operation.setUndefined(false);
			portType.addOperation(operation);

			BindingOperation bindingOperation = wsdlDef.createBindingOperation();
			bindingOperation.setName(operationObj.getString("name"));
			String description = operationObj.optString("description");
			org.w3c.dom.Element docuEl = createDocumentationEl(wsdlDef.getDocumentationElement(), description);
			bindingOperation.setDocumentationElement(docuEl);
			SOAPOperation soapOpera = new SOAPOperationImpl();
			soapOpera.setSoapActionURI(operationObj.optString("action"));
			bindingOperation.addExtensibilityElement(soapOpera);
			BindingInput bindingInput = wsdlDef.createBindingInput();
			SOAPBody inputBody = new SOAPBodyImpl();
			inputBody.setUse("literal");
			bindingInput.addExtensibilityElement(inputBody);
			bindingOperation.setBindingInput(bindingInput);
			BindingOutput bindingOutput = wsdlDef.createBindingOutput();
			SOAPBody outputBody = new SOAPBodyImpl();
			outputBody.setUse("literal");
			bindingOutput.addExtensibilityElement(outputBody);
			bindingOperation.setBindingOutput(bindingOutput);
			binding.addBindingOperation(bindingOperation);
		}
		String portName = bindingObj.getString("portName");
		service.removePort(portName);
		Port port = wsdlDef.createPort();
		port.setName(portName);
		port.setBinding(binding);
		SOAPAddress soapAddr = new SOAPAddressImpl();
		soapAddr.setLocationURI(bindingObj.getString("address"));
		soapAddr.setRequired(true);
		port.addExtensibilityElement(soapAddr);
		port.setExtensionAttribute(new QName(Namespaces.SESAME, "style"), "local");
		service.addPort(port);
	}

	WSDLWriter writer = WSDLUtil.getWSDLWriter();
	OutputStream fileOutput = new FileOutputStream(wsdlPath);
	try {
		writer.writeWSDL(wsdlDef, fileOutput);
	} 
	finally {
		fileOutput.close();
	}
	
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

@SuppressWarnings("unchecked")
public String saveParams(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	request.setCharacterEncoding("utf-8");
	String data = URLDecoder.decode((String)request.getParameter("data"), "UTF-8");
	String unit = request.getParameter("unit");
	File unitFolder = Configuration.getWorkspaceFile(unit);
	
	org.dom4j.Document inputDoc = Dom4jUtil.strToXml(data);
	inputDoc.setXMLEncoding("utf-8");
	
	// 输入参数xml
	org.dom4j.Element root = inputDoc.getRootElement();
	String inputComponent = root.attributeValue("component").replaceAll(".*/", "");
	
	org.dom4j.Element attributes = root.element("attributes");
	String inputDocumentation = attributes.attributeValue("documentation");
	String namespace = attributes.attributeValue("namespace");
	
	// 设置WSDL Defination元素各个参数
	File wsdlPath = new File(unitFolder, "unit.wsdl");
	Dom4jUtil.initDocument(wsdlPath);
	org.dom4j.Element wsdlDef = Dom4jUtil.getRootEl();
	
	Dom4jUtil.addNamespace(wsdlDef, "tns", namespace);
	Dom4jUtil.addNamespace(wsdlDef, "sn", Namespaces.SESAME);
	Dom4jUtil.addNamespace(wsdlDef, "xs", Namespaces.XSD);
	Dom4jUtil.addNamespace(wsdlDef, "soap", Namespaces.SOAP_NAMESPACE);
	Dom4jUtil.addNamespace(wsdlDef, "", Namespaces.WSDL1_NAMESPACE);
	Dom4jUtil.setAttributeValue(wsdlDef, "targetNamespace", namespace);
	
	// 修改对应的schema文件的命名空间
	org.dom4j.Element schema =  Dom4jUtil.Types.getSchema();
	List includes = schema.elements("include");
	if (includes != null && includes.size() > 0) {
		for (int i=0 ; i<includes.size(); i++) {
			org.dom4j.Element include = (org.dom4j.Element)includes.get(i);
			String schemaName = include.attributeValue("schemaLocation").replaceAll("\\.\\.\\/", "");
			File schemaFile =  new File(unitFolder, schemaName);
			
			if (schemaFile.exists()) {
				SAXBuilder saxbuilder = new SAXBuilder();
				Document doc = saxbuilder.build(schemaFile);
				Element rootEl = doc.getRootElement();
				rootEl.setAttribute("targetNamespace", namespace);
				Namespace xmlns_tns = rootEl.getNamespace("tns");
				if (xmlns_tns != null) rootEl.removeNamespaceDeclaration(xmlns_tns);
				xmlns_tns = Namespace.getNamespace("tns", namespace);
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
	
	// 设置documentation值
	Dom4jUtil.setDocumentation(inputDocumentation);
	
	// 设置Types中命名空间
	schema.addAttribute("targetNamespace", namespace);
	
	// 写入WSDL文件
	Dom4jUtil.saveFile(wsdlPath);
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

public String getServiceName(HttpServletRequest request, HttpServletResponse response)  throws Exception { 
	String unit = request.getParameter("unit");
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File file = new File(unitFolder, "unit.wsdl");
	WSDLReader reader = WSDLUtil.getWSDLReader();
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
}%>

<%
String operation = request.getParameter("operation");
WebServletResponse responseWrapper = new WebServletResponse(response);

if (operation != null)
{
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
			logger.error(t.getMessage(), t);
		responseWrapper.sendError(t.getMessage());
	}
	catch (Exception e) {
		logger.error(e.getMessage(), e);
		responseWrapper.sendError(e.getMessage());
	}
}
else {
	responseWrapper.sendError("["+request.getMethod()+"]拒绝执行，没有指定 operation 参数");
}

%>