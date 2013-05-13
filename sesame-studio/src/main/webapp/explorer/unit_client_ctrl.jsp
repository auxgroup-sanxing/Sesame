<%@page import="com.ibm.wsdl.BindingOutputImpl"%>
<%@page import="com.ibm.wsdl.BindingInputImpl"%>
<%@page import="com.ibm.wsdl.BindingOperationImpl"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="org.dom4j.DocumentHelper"%>
<%@page import="org.dom4j.QName"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page
	import="com.sanxing.studio.*,com.sanxing.studio.utils.*,com.sanxing.studio.team.svn.*,com.sanxing.studio.team.*"%>
<%@page import="com.ibm.wsdl.extensions.schema.*"%>
<%@page import="com.ibm.wsdl.extensions.soap.*"%>
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
<%@page import="javax.xml.parsers.*"%>
<%@page import="javax.wsdl.extensions.soap.*"%>
<%@page import="javax.wsdl.extensions.schema.Schema"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="com.sanxing.sesame.transport.Protocols"%>
<%@page import="com.sanxing.sesame.jaxp.*"%>
<%@page import="com.sanxing.studio.deploy.*"%>
<%@page import="com.sanxing.studio.IllegalNameException"%>
<%@page
	import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.*"%>
<%@page import="java.net.URL"%>
<%@page import="org.json.*"%>
<%@page import="java.net.*"%>
<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>
<%!private static final String BINDING_FILE = "binding.xml";

private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

private WSDLReader getWSDLReader() throws WSDLException {
	WSDLFactory factory = WSDLFactory.newInstance();
	WSDLReader reader = factory.newWSDLReader();
	reader.setFeature("javax.wsdl.verbose", false);
	reader.setFeature("javax.wsdl.importDocuments", false);
	return reader;
}

//服务单元保存并部署
public String deployserviceunit(HttpServletRequest request, HttpServletResponse response) throws Exception{
    String unit = request.getParameter("unit");
	File unitFolder = Configuration.getWorkspaceFile(unit);
	String unitname = unitFolder.getName();
	String serviceassemblyname =  unitFolder.getParentFile().getParentFile().getName();
	String supath = unitFolder.getAbsolutePath();
	String componentname = request.getParameter("compName");
	DeployServiceUnit dsu = new DeployServiceUnit();
	dsu.deployeServiceUnit(serviceassemblyname,unitname,componentname,supath);
	return "success";
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

@SuppressWarnings("unchecked")
public String getTransports(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String component = request.getParameter("component");
	
	try {
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File compBundle = Configuration.getWarehouseFile(component);

		Element bindingEl;
		JSONArray items = new JSONArray();
		if (compBundle.isDirectory()) {
			File bindingXml = new File(compBundle, BINDING_FILE);
			bindingEl = 
					(bindingXml.exists()) ? builder.build(bindingXml).getRootElement() : new Element("binding");
		} else {
			JarFile compJar = new JarFile(compBundle);
			try {
				JarEntry bindingXml = compJar.getJarEntry(BINDING_FILE);
				if (bindingXml != null) {
					InputStream input = compJar.getInputStream(bindingXml);
					bindingEl = builder.build(input).getRootElement();
				} else {
					bindingEl = new Element("binding");
				}
			} finally {
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
				json.put("transport", transport);
				items.put(json);
			}
		} else {
			String [] trans = Protocols.list();
			if (trans.length > 0)
			for (String tran : trans) {
				JSONObject json = new JSONObject();
				json.put("transport", tran);
				items.put(json);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	}finally{}
}

@SuppressWarnings("unchecked")
public String generateSchemaUI(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String transport = request.getParameter("transport");
	String bindingName = request.getParameter("bindingName");
	JSONObject rs = new JSONObject();
	JSONArray valueArray = new JSONArray();
	JSONObject json = new JSONObject();
	
	try {
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		
		Document document = null;
		URL schemaURL = Protocols.getContextSchema(transport, unit.indexOf("/client/") > -1 ? "remote" : "local");
		if (schemaURL != null) {
				document = builder.build(schemaURL);
			
				if (document != null) {
					org.jdom.Element rootEl = document.getRootElement();
					Element firstEl = rootEl.getChild("element", rootEl.getNamespace());
					if (firstEl != null) {
						json.put("tag", firstEl.getName());
						json.put("name", firstEl.getAttributeValue("name"));
						json.put("stateful", false);
						SchemaUtil.generateUI(firstEl, json);
					}
				}
				
				// 读取unit.wsdl文件sn:binding子元素
				org.dom4j.Namespace xmlns_art=org.dom4j.Namespace.get("sn", Namespaces.SESAME);
				File wsdlPath = new File(unitFolder, "unit.wsdl");
				Dom4jUtil.initDocument(wsdlPath);
				org.dom4j.Element root = Dom4jUtil.getRootEl();
				List bindingList = 
					root.selectNodes("//*[name()='binding'][@name=\"" + bindingName + "\"]");
				if (bindingList != null && !bindingList.isEmpty()) {
					org.dom4j.Element bindingEl = (org.dom4j.Element)bindingList.get(0);
					if (bindingEl != null) {
						org.dom4j.Element artBinding = (org.dom4j.Element) bindingEl.element(new QName("binding", xmlns_art));
						if (artBinding != null) {
							List children = artBinding.elements();
							if (children != null && !children.isEmpty()) {
								for (Iterator itr = children.iterator(); itr.hasNext();) {
									JSONObject valueJso = new JSONObject();
									org.dom4j.Element el = (org.dom4j.Element)itr.next();
									String name = el.getName();
									String value = el.getText();
									valueJso.put(name, value);
									valueArray.put(valueJso);
								}
							}
						}
					}
				}
		}
		
		rs.put("schema", json);
		rs.put("values", valueArray);
		
		response.setContentType("text/json; charset=utf-8");
		return rs.toString();
	} finally {}
}

@SuppressWarnings("unchecked")
public String getBindings(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String component = request.getParameter("component");
	try {
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File wsdlPath = new File(unitFolder, "unit.wsdl");
		Dom4jUtil.initDocument(wsdlPath);
		
		List bindingList = Dom4jUtil.Binding.getBindingList();
		JSONArray items = new JSONArray();
		if (bindingList != null && bindingList.size() > 0) {
			for (Iterator itr = bindingList.iterator(); itr.hasNext();) {
				org.dom4j.Element binding = (org.dom4j.Element)itr.next();
				JSONObject json = new JSONObject();
				json.put("bindingname", binding.attributeValue("name", ""));
				items.put(json);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	} finally {}
}

private JSONObject getJso(JSONArray items, String bindingType, String jsoName) throws Exception {
	JSONObject jso = null;
	if (items.length() > 0) {
		for (int i=0; i<items.length(); i++) {
			JSONObject tmp = items.getJSONObject(i);
			if (bindingType.equals(tmp.optString(jsoName))) {
				jso = tmp;
				break;
			}
		}
	}
	return jso;
}

//从 unit.wsdl 文件装载绑定信息
@SuppressWarnings("unchecked")
public String loadOperations(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try {
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		// unit.wsdl文件路径
		File wsdlPath = new File(unitFolder, "unit.wsdl");
		org.dom4j.Namespace xmlns_art=org.dom4j.Namespace.get("sn", Namespaces.SESAME);
		org.dom4j.Namespace xmlns_soap=org.dom4j.Namespace.get("soap", Namespaces.SOAP_NAMESPACE);
		Dom4jUtil.initDocument(wsdlPath);

		JSONArray items = new JSONArray();
		org.dom4j.Element serviceRoot =  Dom4jUtil.getRootEl();
		List bindingList = serviceRoot.selectNodes("//*[name()='binding']");
		
		List importIntfList = serviceRoot.selectNodes("//*[name()='import']");
		boolean isPublic = false;
		if (importIntfList != null && !importIntfList.isEmpty()) {
			isPublic = true;
		}
		
		if(bindingList!= null && !bindingList.isEmpty()) {
			for (Iterator itr = bindingList.iterator(); itr.hasNext();) {
				JSONObject bindingJso = new JSONObject();
				
				org.dom4j.Element bindingEl = (org.dom4j.Element)itr.next();
				
				String bindingName =  bindingEl.attributeValue("name", "");
				String bindingType = bindingEl.attributeValue("type");
				QName typeName = null;
				if (bindingType != null)
					typeName = QName.get(bindingType, "");
				
				org.dom4j.Element portType = null;
				org.dom4j.Namespace ns = null;
				if (typeName != null) {
					portType = 
						(org.dom4j.Element) serviceRoot.selectSingleNode("//*[name()='portType'][@name='" + typeName.getName()  + "']");
					ns = serviceRoot.getNamespaceForPrefix(typeName.getNamespacePrefix());
				}
				
				if (ns == null)
					continue;
				
				String namespace = ns.getURI();
				JSONArray bindingOpJsoArray = new JSONArray();
				
				// operation 节点
				List bindingOpList = bindingEl.selectNodes("//*[name()='binding'][@name='" + bindingName + "']/*[name()='operation']");
				if (bindingOpList != null && !bindingOpList.isEmpty()) {
					for (Iterator bindingOpitr = bindingOpList.iterator(); bindingOpitr.hasNext();) {
						JSONObject bindingOpJso = new JSONObject();
						org.dom4j.Element bindingOp = (org.dom4j.Element) bindingOpitr.next();
						String operaName = bindingOp.attributeValue("name", "");
						String xsdFile = unit + File.separator + operaName + ".xsd";
						bindingOpJso.put("opera", operaName);									//  获取操作的锁状态
						bindingOpJso.put("locked", isPublic ? "" : LockUtil.isOperaLocked(xsdFile));
						bindingOpJso.put("isPublic", isPublic);
						
						org.dom4j.Element portTypeOpera = null;
						if (portType != null)
							portTypeOpera = 
								(org.dom4j.Element) portType.selectSingleNode("//*[name()='operation'][@name='" + operaName  + "']");
						if (portTypeOpera != null)
							bindingOpJso.put("desc", portTypeOpera.elementText("documentation"));
						
						if (bindingOp.elements().size() > 0) {
							org.dom4j.Element bindingSoapOp = (org.dom4j.Element) bindingOp.elements().get(0);
							bindingOpJso.put("action", bindingSoapOp.attributeValue("soapAction", ""));
							bindingOpJsoArray.put(bindingOpJso);
						}
					}
				
					bindingJso.put("operations", bindingOpJsoArray);
				} else {
					bindingJso.put("operations", bindingOpJsoArray);
				}
				
				bindingJso.put("portTypeName", bindingType);
				bindingJso.put("bindingName", bindingName);
				
				// 端点参数
				List ports = Dom4jUtil.Service.getPortList();
				if (ports != null && !ports.isEmpty())
					for (Iterator itrtemp = ports.iterator();  itrtemp.hasNext();) {
						org.dom4j.Element port = (org.dom4j.Element)itrtemp.next();
						String name = port.attributeValue("name");
						String portBinding = port.attributeValue("binding").replaceAll(".*?:", "");
						if (portBinding.equals(bindingName)) {
							org.dom4j.Element soapAddr = Dom4jUtil.getElement(port, "address");
							bindingJso.put("epName", name);
							bindingJso.put("address", soapAddr.attributeValue("location"));
						}
					}
				
				// 扩展元素
				org.dom4j.Element artBinding = (org.dom4j.Element) bindingEl.element(new QName("binding", xmlns_art));
				if (artBinding != null) {
					List extAttrs = artBinding.attributes();
					if (extAttrs != null && !extAttrs.isEmpty()) {
						JSONObject extJso = new JSONObject();
						for (Iterator arrtItr = extAttrs.iterator(); arrtItr.hasNext();) {
							org.dom4j.Attribute extAttr = (org.dom4j.Attribute)arrtItr.next();
							extJso.put(extAttr.getName(), extAttr.getValue());
						}
						bindingJso.put("extdata", extJso);
					}
				}
				
				// soap:binding 节点transport元素值
				org.dom4j.Element soapBinding = (org.dom4j.Element)bindingEl.element(new QName("binding", xmlns_soap));
				if (soapBinding != null)
					bindingJso.put("transport", soapBinding.attributeValue("transport", ""));
				items.put(bindingJso);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	} finally {}
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

@SuppressWarnings("unchecked")
public String loadPortTypes(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	try {
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File unitFile = Configuration.getWorkspaceFile(unit + File.separator + "unit.wsdl");
		
		if (!unitFile.exists()) {
			WebUtil.sendError(response, "unit.wsdl文件不存在!");
			return "";
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		WSDLReader reader = this.getWSDLReader();
		reader.setFeature("javax.wsdl.importDocuments", true);
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(unitFile, true));
		
		JSONArray items = new JSONArray();
		Map ptMap = wsdlDef.getAllPortTypes();
		if (ptMap != null && !ptMap.isEmpty()) {
			Set<Map.Entry<?,?>> set = ptMap.entrySet();
			for (Iterator<?> iter=set.iterator(); iter.hasNext();) {
				Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
				PortType portType = (PortType)entry.getValue();
				
				// 判断是否为引用的公共接口
				boolean isPublic = false;
				String baseURI = (portType.getDocumentationElement() != null) ? portType.getDocumentationElement().getBaseURI() : "";
				if (baseURI.contains("/intf/"))
					isPublic = true;
				
				List<?> operList = portType.getOperations();
				for (Iterator<?> it = operList.iterator(); it.hasNext(); ) {
					Operation operation = (Operation)it.next();
					String opera = operation.getName();
					File file = new File(unitFolder, opera + ".xsd");
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
	} finally{}
}

@SuppressWarnings("unchecked")
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
		String newName = inputPortTypeEl.getAttribute("intfName");
		String oldName = inputPortTypeEl.getAttribute("oldIntfName");
		String desc = inputPortTypeEl.getAttribute("intfDesc");
		PortType ptEl = wsdlDef.getPortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), oldName));
		if (ptEl != null) {
			ptEl.setQName(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), newName));
			org.w3c.dom.Element ptDoc = ptEl.getDocumentationElement();
			if (ptDoc == null) {
				ptDoc = document.createElement("documentation");
				ptEl.setDocumentationElement(ptDoc);
			}
			ptDoc.setTextContent(desc);
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

@SuppressWarnings("unchecked")
public String deleteIntf(HttpServletRequest request, HttpServletResponse response)  throws Exception {
	String unit = request.getParameter("unit");
	String intfName = request.getParameter("interface");
	String bindingName =  null;
	
	try { 
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, "unit.wsdl");
		
		WSDLReader reader = this.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(file, true));
		
		
		// 删除对应的binding
		Map bindingMap = wsdlDef.getAllBindings();
		if (bindingMap != null && !bindingMap.isEmpty())
			for (Iterator itr = bindingMap.entrySet().iterator(); itr.hasNext();) {
				Map.Entry entry = (Map.Entry)itr.next();
				Binding binding = (Binding)entry.getValue();
				String name = binding.getQName().getLocalPart();
				String ptName = 
						(binding.getPortType() != null) ? binding.getPortType().getQName().getLocalPart() : "";
				if (intfName.equals(ptName))
					wsdlDef.removeBinding(binding.getQName());
				
				// 删除对应的service中的port
				Iterator serviceItr = wsdlDef.getAllServices().entrySet().iterator();
				if (serviceItr.hasNext()) {
					Map.Entry serviceEntry = (Map.Entry)serviceItr.next();
					Service service =  (Service) serviceEntry.getValue();
					Map portMap = service.getPorts();
					if (portMap != null && !portMap.isEmpty())
						for (Iterator portItr = portMap.entrySet().iterator(); portItr.hasNext();) {
							Map.Entry ptentry = (Map.Entry)portItr.next();
							Port port = (Port)ptentry.getValue();
							String portName = port.getName();
							javax.xml.namespace.QName attr  =  
								(javax.xml.namespace.QName)port.getExtensionAttribute(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), "binding"));
							if (attr != null) {
								String attrBinding = attr.getLocalPart();
								if (attrBinding.equals(name))
									service.removePort(portName);
							}
						}
				}
			}
		
		PortType portType = 
			wsdlDef.getPortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), intfName));
		if (portType != null) {
			// 删除对应的xsd文件
			List opList = portType.getOperations();
			if (opList != null && !opList.isEmpty()) {
				for (Iterator itr = opList.iterator(); itr.hasNext();) {
					Operation operation = (Operation)itr.next();
					if (operation != null) { 
						String opera = operation.getName();
						
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
										if (elem.getAttribute("schemaLocation").equals(opera + ".xsd")) {
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
		
		wsdlDef.removePortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), intfName));
		
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
public String getInterfaces(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try {
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, "unit.wsdl");
				
		JSONArray items = new JSONArray();
		WSDLReader reader = this.getWSDLReader();
		reader.setFeature("javax.wsdl.importDocuments", true);
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(file, true));
		
		Map portMap = wsdlDef.getAllPortTypes();
		if (portMap != null && !portMap.isEmpty()) {
			Set<Map.Entry<?,?>> set = portMap.entrySet();
			for (Iterator<?> iter=set.iterator(); iter.hasNext();) {
				Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
				PortType portType = (PortType)entry.getValue();
				JSONObject jso = new JSONObject();
				String name = portType.getQName().getLocalPart();
				String nsURI = portType.getQName().getNamespaceURI();
				jso.put("type", name);
				jso.put("isPublic", nsURI.indexOf("/intf/") != -1 ? true:false);
				items.put(jso);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	} finally {}
}

@SuppressWarnings("unchecked")
public String getBindingTypes(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String component = request.getParameter("component");
	JSONArray items = new JSONArray();
	
	try {
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File dirPath = new File(unitFolder.getParentFile(), component);
		if (dirPath.exists() && dirPath.isDirectory()) {
			File[] allFiles = dirPath.listFiles();
			for (File file : allFiles) {
				if (file.getName().endsWith(".binding")) {				
					JSONObject jso = new JSONObject();
					String type = file.getName().replaceAll("\\.binding", "");
					jso.put("type", type);
					items.put(jso);
				}
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("count", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	}finally {}	
}

@SuppressWarnings("unchecked")
public String addBinding(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	String portTypeName = request.getParameter("type");
	
	try {
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, "unit.wsdl");
		WSDLReader reader = this.getWSDLReader();
		Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(file, true));
		PortType portType = wsdlDef.getPortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), portTypeName));
		
		String bindingName = "";
		Map bindingMap = wsdlDef.getAllBindings();
		if (bindingMap != null && !bindingMap.isEmpty()) {
			int bindingCount = bindingMap.entrySet().size();
			bindingName = portTypeName + "-binding" + (bindingCount + 1);
		} else {
			bindingName = portTypeName + "-binding";
		}
		
		Binding binding = wsdlDef.createBinding();
		binding.setPortType(portType);
		binding.setQName(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), bindingName));
		
		if (portType != null) {
			List opList = portType.getOperations();
			if (opList != null && !opList.isEmpty())
				for (Iterator opItr = opList.iterator(); opItr.hasNext();) {
					Operation operation = (Operation)opItr.next();
					String operaName = operation.getName();
					
					BindingOperation bindingOp = new BindingOperationImpl();
					bindingOp.setName(operaName);
					
					SOAPOperation soapOp = new SOAPOperationImpl();
					soapOp.setSoapActionURI("");
					soapOp.setStyle("document");
					bindingOp.addExtensibilityElement(soapOp);
					
					SOAPBody soapBody = new SOAPBodyImpl();
					soapBody.setUse("literal");
					
					BindingInput input = new BindingInputImpl();
					input.addExtensibilityElement(soapBody);
					bindingOp.setBindingInput(input);
					
					BindingOutput output = new BindingOutputImpl();
					output.addExtensibilityElement(soapBody);
					bindingOp.setBindingOutput(output);
					binding.addBindingOperation(bindingOp);
				}
		}
		binding.setUndefined(false);
		wsdlDef.addBinding(binding);
		
		WSDLWriter writer = WSDLUtil.getWSDLWriter();
		OutputStream fileStream = new FileOutputStream(file);
		try {
			writer.writeWSDL(wsdlDef, fileStream);
		} finally {
			fileStream.close();
		}
		
		response.setContentType("text/plain; charset=utf-8");
		return "true";
	} finally {}
}

@SuppressWarnings("unchecked")
public String syncBinding(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	
	String data = request.getParameter("data");
	JSONObject jso = new JSONObject(data);
	JSONArray items = jso.getJSONArray("items");
	for (int i=0; i<items.length(); i++) {
		JSONObject item = items.getJSONObject(i);
		String intf = item.optString("intf").replaceAll(".*?:", "");
		String bindingName = item.optString("bindingName");
		synchronizeBinding(unit, intf, bindingName);
	}
	
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

private void synchronizeBinding(String unit, String intf, String bindingName) throws Exception {
	File unitFolder = Configuration.getWorkspaceFile(unit);
	
	// server unit.wsdl文件路径
	File file = new File(unitFolder, "unit.wsdl");
	WSDLReader reader = this.getWSDLReader();
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(file, true));
	
	PortType portType = wsdlDef.getPortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), intf));
	Binding binding = wsdlDef.getBinding(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), bindingName));
	if (portType != null) {
		List opList = portType.getOperations();
		if (opList != null && !opList.isEmpty())
		for (Iterator opItr = opList.iterator(); opItr.hasNext();) {
			Operation operation = (Operation)opItr.next();
			String operaName = operation.getName();
			
			// 同步操作
			if (binding != null) {
				BindingOperation bindingOp = binding.getBindingOperation(operaName, null, null);
				if (bindingOp == null) {
					bindingOp = new BindingOperationImpl();
					bindingOp.setName(operaName);
					
					SOAPOperation soapOp = new SOAPOperationImpl();
					soapOp.setSoapActionURI("");
					soapOp.setStyle("document");
					bindingOp.addExtensibilityElement(soapOp);
					
					SOAPBody soapBody = new SOAPBodyImpl();
					soapBody.setUse("literal");
					
					BindingInput input = new BindingInputImpl();
					input.addExtensibilityElement(soapBody);
					bindingOp.setBindingInput(input);
					
					BindingOutput output = new BindingOutputImpl();
					output.addExtensibilityElement(soapBody);
					bindingOp.setBindingOutput(output);
					binding.addBindingOperation(bindingOp);
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
}

@SuppressWarnings("unchecked")
public String deleteBinding(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	String intf = request.getParameter("intf");
	String bindingName = request.getParameter("bindingName");
	String prefix = intf.replaceAll(":.*", "");
	String typeName = intf.replaceAll(".*?:", "");
	
	try {
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		
		// server unit.wsdl文件路径
		File wsdlPath = new File(unitFolder, "unit.wsdl");
			
		Dom4jUtil.initDocument(wsdlPath);
		org.dom4j.Element root = Dom4jUtil.getRootEl();
		org.dom4j.Element bindingEl =
			(org.dom4j.Element) root.selectSingleNode("//*[name()='binding'][@name='" + bindingName + "']");
	
		if (bindingEl != null) {
			bindingEl.getParent().remove(bindingEl);
		}
		
		org.dom4j.Element serviceEl = Dom4jUtil.Service.getService();
		if (serviceEl != null) {
			Iterator ports =  serviceEl.elementIterator();
			while(ports.hasNext()) {
				org.dom4j.Element portEl = (org.dom4j.Element)ports.next();
				String portBinding = portEl.attributeValue("binding").replaceAll(".*?:", "");
				if (portBinding.equals(bindingName))			
					portEl.getParent().remove(portEl);
			}
		}
		
		Dom4jUtil.saveFile(wsdlPath);
		response.setContentType("text/plain; charset=utf-8");
		return "true";
	} finally {}
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
	
	List inputEndpoints = 
		root.selectNodes("//endpoints/endpoint");
	
	// 写入WSDL文件
	Dom4jUtil.saveFile(wsdlPath);
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

@SuppressWarnings("unchecked")
public String saveBindings(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	request.setCharacterEncoding("utf-8");
	String data = URLDecoder.decode((String)request.getParameter("data"), "UTF-8");
	String unit = request.getParameter("unit");
	String component = request.getParameter("component");
	
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File wsdlPath = new File(unitFolder, "unit.wsdl");
	
	org.dom4j.Namespace xmlns_art=org.dom4j.Namespace.get("sn", Namespaces.SESAME);
	
	Dom4jUtil.initDocument(wsdlPath);
	org.dom4j.Element root = Dom4jUtil.getRootEl();
	
	org.dom4j.Document inputDoc = Dom4jUtil.strToXml(data);
	inputDoc.setXMLEncoding("utf-8");
	
	// 输入参数xml
	org.dom4j.Element inputRoot = inputDoc.getRootElement();
	List inputBindingList = inputRoot.selectNodes("//binding");
	if (inputBindingList != null && !inputBindingList.isEmpty()) {
		// 先删除service下所有port
		List servicePorts = Dom4jUtil.Service.getPortList();
		org.dom4j.Element el = Dom4jUtil.Service.getService();
		el.clearContent();
		
		for (Iterator itr = inputBindingList.iterator(); itr.hasNext();) {
			org.dom4j.Element inputBinding = (org.dom4j.Element)itr.next();
			String intf = inputBinding.attributeValue("interface", "");
			String oldName = inputBinding.attributeValue("oname", "");
			String newName = inputBinding.attributeValue("name", "");
			
			// WSDL文件中对应的binding节点 
			org.dom4j.Element binding = 
				(org.dom4j.Element )root.selectSingleNode("//*[name()='binding'][@name='" + oldName + "'][@type='" + intf + "']");
			if (binding != null) {
				binding.addAttribute("name", newName);
				// 扩展元素赋值
				QName qname = new QName("binding", xmlns_art);
				
				org.dom4j.Element artBinding = (org.dom4j.Element) binding.element(qname);
				if (artBinding == null) {
					artBinding = DocumentHelper.createElement(qname);
					binding.elements().add(0, artBinding);
					artBinding.addAttribute("component-name", component);
				}
				
				// 组件附加参数
				Iterator extraItr = inputBinding.elementIterator("extdata");
				if (extraItr.hasNext()) {
					org.dom4j.Element inputExtEl = (org.dom4j.Element)extraItr.next();
					if (inputExtEl != null) {
						List extAttributes = inputExtEl.attributes();
						if (extAttributes != null && !extAttributes.isEmpty()) {
							for (Iterator attrItr = extAttributes.iterator(); attrItr.hasNext();) {
								org.dom4j.Attribute tmpAttr= (org.dom4j.Attribute)attrItr.next();
								artBinding.addAttribute(tmpAttr.getName(), tmpAttr.getValue());
							}
						} 
					}
				}
				
				// 协议附加参数
				Iterator propItr = inputBinding.elementIterator("properties");
				if (propItr.hasNext()) {
					org.dom4j.Element propExtEl = (org.dom4j.Element)propItr.next();
					if(propExtEl != null) {
						List extChildren = propExtEl.elements();
						if (extChildren != null && !extChildren.isEmpty()) {
							List artChildren = artBinding.elements();
							for (Iterator artItr = artChildren.iterator(); artItr.hasNext();) {
								org.dom4j.Element artEl= (org.dom4j.Element)artItr.next();
								artBinding.remove(artEl);
							}
							
							for (Iterator attrItr = extChildren.iterator(); attrItr.hasNext();) {
								org.dom4j.Element inputExtraChildEl= (org.dom4j.Element)attrItr.next();
								org.dom4j.Element newEl = DocumentHelper.createElement(inputExtraChildEl.getName());
								newEl.setText(inputExtraChildEl.getText());
								artBinding.add(newEl);
							}
						}
					}
				}
				
				// WSDL文件中对应的service port节点
				List ports = Dom4jUtil.Service.getPortList();
				if (ports != null && !ports.isEmpty())
					for (Iterator portItr = ports.iterator(); portItr.hasNext();) {
						org.dom4j.Element port = (org.dom4j.Element) portItr.next();
						if (port != null) {
							String portBinding = port.attributeValue("binding", "");
							if (oldName.equals(portBinding.replaceAll(".*?:", "")))
								port.addAttribute("binding", "tns:" + newName);
						}
					}
				
				// 设置Service中Port元素属性
				String endPointName = inputBinding.attributeValue("epName");
				String address = inputBinding.attributeValue("address");
				
				if (!"".equals(endPointName) && !"".equals(address)) {
					org.dom4j.Element child = Dom4jUtil.createEl(el, "port");
					child.addAttribute("name", endPointName);
					child.addAttribute("binding", "tns:" + newName);
					child.addAttribute(new QName("style", org.dom4j.Namespace.get("sn", Namespaces.SESAME)), 
							unit.indexOf("/client/") > -1 ? "remote" : "local");
					
					org.dom4j.Element addressEl = Dom4jUtil.createEl(child, "soap:address");
					addressEl.addAttribute("location", address);
				}
				
				// operation赋值
				Iterator inputOperaItr = inputBinding.elementIterator("operation");
				while(inputOperaItr.hasNext()) {
						org.dom4j.Element inputOpera = (org.dom4j.Element)inputOperaItr.next();
						String inputOperaName = inputOpera.attributeValue("name", "");
						String inputOperaAction = inputOpera.attributeValue("action", "");
						Iterator operaItr= binding.elementIterator("operation");
						while(operaItr.hasNext()) {
							org.dom4j.Element operation = (org.dom4j.Element)operaItr.next();
							String name = operation.attributeValue("name");
							
							if (name.equals(inputOperaName)) {
								org.dom4j.Element soapOpera = 
									(org.dom4j.Element)operation.elements().get(0);
								soapOpera.addAttribute("soapAction", inputOperaAction);
							}
						}
					}
			}
		}
	} else {
		// 删除绑定
		List bindingList = Dom4jUtil.Binding.getBindingList();
		if (bindingList !=null &&!bindingList.isEmpty())
			for (Iterator bindingItr = bindingList.iterator(); bindingItr.hasNext();) {
				org.dom4j.Element binding = (org.dom4j.Element) bindingItr.next();
				root.remove(binding);
			}
	}
	
	// 写入WSDL文件
	Dom4jUtil.saveFile(wsdlPath);
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

public String createOpera(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
		String intf = request.getParameter("interface");
		String oper = request.getParameter("opera").trim();
		String component = request.getParameter("component");
		
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, "unit.wsdl");
		
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
		File schemaFile = new File(unitFolder, oper+".xsd");
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
		
		// 同步绑定设置
		synchronizeBinding(unit, intf, intf + "-binding");
		
		response.setContentType("text/json; charset=utf-8");
		return "true";
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

	File unitFolder = Configuration.getWorkspaceFile(unit);
	File unitFile = new File(unitFolder, "unit.wsdl");
	
	// 从SVN改名
	File projectFolder = unitFolder.getParentFile().getParentFile();
	ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
	
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
	
	PortType portType = wsdlDef.getPortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), intf));
	Operation opera = null;
	if (portType==null || (opera=portType.getOperation(oper, null, null))==null) {
		throw new IllegalNameException("操作不存在，可能已被删除");
	} 
	else {
		// 修改portType中的operation
		portType.setUndefined(false);
		opera.setUndefined(false);
		opera.setName(request.getParameter("opera"));
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
	if (srcFile.exists())  {
		LockUtil.renameFromSvn(sync, srcFile, destFile);
		srcFile.renameTo(destFile);
	}
	
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

@SuppressWarnings("unchecked")
public String removeOpera(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("operas");
	JSONArray operas = new JSONArray(data);
	String unit = request.getParameter("unit");
	String component = request.getParameter("component");
	try
	{
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File unitFile = new File(unitFolder, "unit.wsdl");
		
		// 从SVN删除文件
		File projectFolder = unitFolder.getParentFile().getParentFile();
		ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
		
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
			
			PortType portType = wsdlDef.getPortType(new javax.xml.namespace.QName(wsdlDef.getTargetNamespace(), intf));
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