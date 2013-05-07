<%@page import="java.net.URLDecoder"%><%@page
	import="org.dom4j.DocumentHelper"%>
<%@page import="org.dom4j.QName"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
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
<%@page import="javax.xml.parsers.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="com.sanxing.sesame.transport.Protocols"%>
<%@page
	import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.*"%>
<%@page import="java.net.URL"%>
<%@page import="org.json.*"%>
<%@page import="java.net.*"%>
<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>
<%!private static final String BINDING_FILE = "binding.xml";

private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

@SuppressWarnings("unchecked")
private List getImportPortTypeOperas(List importList, String ptName, File unitFolder) throws Exception {
	List<?> operList = new ArrayList();
	WSDLReader reader = WSDLUtil.getWSDLReader();
	
	if (importList != null && !importList.isEmpty()) {
		for (Iterator itr = importList.iterator(); itr.hasNext();) {
			org.dom4j.Element  importEl = (org.dom4j.Element)itr.next();
			String importLocation = importEl.attributeValue("location");
			
			if (!"".equals(importLocation)) {
				if (importLocation.matches(".*?\\/intf/.*?" + ptName.replaceAll(".*?:", "") + "/public.wsdl")) {
					File intfWsdlFile = new File(unitFolder.getParentFile().getParent() + File.separator+ importLocation.replaceAll("\\..*?\\/", ""));
					if (intfWsdlFile.exists()) {
						Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfWsdlFile, true));
						Map ptMap = wsdlDef.getAllPortTypes();
						if (ptMap != null && !ptMap.isEmpty()) {
							Set<Map.Entry<?,?>> set = ptMap.entrySet();
							for (Iterator<?> iter=set.iterator(); iter.hasNext();) {
								Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
								PortType portType = (PortType)entry.getValue();
								operList = portType.getOperations();
							}
						}
					}
				}
			}
		}
	}
	return operList;
}

@SuppressWarnings("unchecked")
private String getImportPortTypeDesc(List importList, String ptName, File unitFolder) throws Exception {
	String desc = "";
	List<?> operList = new ArrayList();
	WSDLReader reader = WSDLUtil.getWSDLReader();
	
	if (importList != null && !importList.isEmpty()) {
		for (Iterator itr = importList.iterator(); itr.hasNext();) {
			org.dom4j.Element  importEl = (org.dom4j.Element)itr.next();
			String importLocation = importEl.attributeValue("location");
			
			if (!"".equals(importLocation)) {
				if (importLocation.matches(".*?\\/intf/.*?" + ptName.replaceAll(".*?:", "") + "/public.wsdl")) {
					File intfWsdlFile = new File(unitFolder.getParentFile().getParent() + File.separator+ importLocation.replaceAll("\\..*?\\/", ""));
					if (intfWsdlFile.exists()) {
						Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfWsdlFile, true));
						desc = wsdlDef.getDocumentationElement().getTextContent();
					}
				}
			}
		}
	}
	return desc;
}

//从 unit.wsdl 文件装载绑定信息
@SuppressWarnings("unchecked")
public String loadOperations(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try {
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		// server unit.wsdl文件路径
		File wsdlPath = new File(unitFolder, "unit.wsdl");

		org.dom4j.Namespace xmlns_art=org.dom4j.Namespace.get("sn", Namespaces.SESAME);
		org.dom4j.Namespace xmlns_soap=org.dom4j.Namespace.get("soap", Namespaces.SOAP_NAMESPACE);
		Dom4jUtil.initDocument(wsdlPath);

		JSONArray items = new JSONArray();
		
		// engine WSDL路径集合
		Set enginPathSet = new HashSet();
		
		org.dom4j.Element serviceRoot =  Dom4jUtil.getRootEl();
		List bindingList = serviceRoot.selectNodes("//*[name()='binding']");
		
		if(bindingList!= null && !bindingList.isEmpty()) {
			for (Iterator itr = bindingList.iterator(); itr.hasNext();) {
				JSONObject bindingJso = new JSONObject();
				
				org.dom4j.Element bindingEl = (org.dom4j.Element)itr.next();
				
				String bindingName =  bindingEl.attributeValue("name", "");
				String bindingType = bindingEl.attributeValue("type");
				QName typeName = QName.get(bindingType, "");

				org.dom4j.Namespace ns = serviceRoot.getNamespaceForPrefix(typeName.getNamespacePrefix());
				if (ns == null)
					continue;
				
				String namespace = ns.getURI();
				// engine WSDL路径
				List importList = 
					serviceRoot.selectNodes("//*[name()='import'][@namespace='" + namespace + "']");
				
				JSONArray bindingOpJsoArray = new JSONArray();
				if (importList ==null || importList.isEmpty())
					continue;
				
				// operation 节点
				List bindingOpList = bindingEl.selectNodes("//*[name()='binding'][@name='" + bindingName + "']/*[name()='operation']");
				if (bindingOpList != null && !bindingOpList.isEmpty()) {
					for (Iterator importItr = importList.iterator(); importItr.hasNext();) {
						org.dom4j.Element importEl = (org.dom4j.Element) importItr.next();
						enginPathSet.add(importEl.attributeValue("location"));
					}
					
					for (Iterator bindingOpitr = bindingOpList.iterator(); bindingOpitr.hasNext();) {
						JSONObject bindingOpJso = new JSONObject();
						org.dom4j.Element bindingOp = (org.dom4j.Element) bindingOpitr.next();
						bindingOpJso.put("opera", bindingOp.attributeValue("name", ""));
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
		
		// 从对应的engine WSDL文件中读取portType 完善结果
		if (items.length() > 0)
			if (enginPathSet != null && !enginPathSet.isEmpty())
				for (Iterator itr = enginPathSet.iterator(); itr.hasNext();) {
					String enginePath = (String) itr.next();
					File engineFile = new File(unitFolder, enginePath);
					Dom4jUtil.initDocument(engineFile);
					
					org.dom4j.Element engineRoot = Dom4jUtil.getRootEl();
					List importIntfList = engineRoot.selectNodes("//*[name()='import']");
					
					for (int j=0; j<items.length(); j++) {
						JSONObject jso = items.getJSONObject(j);
						String ptName = jso.optString("portTypeName");
						QName typeName = QName.get(ptName, "");
						
						org.dom4j.Element portType = 
							(org.dom4j.Element) engineRoot.selectSingleNode("//*[name()='portType'][@name='" + typeName.getName()  + "']");
						
						// 读取engine服务单元中引用的公共接口
						List importOperaList = this.getImportPortTypeOperas(importIntfList, ptName, unitFolder);
						String importPtDesc = this.getImportPortTypeDesc(importIntfList, ptName, unitFolder);
						
						if (portType == null && importOperaList.size() == 0) {
							items.put(j, "");
						} else if (portType != null && importOperaList.size() == 0){
							JSONArray operaArray = jso.optJSONArray("operations");
							if (operaArray != null && operaArray.length() > 0)
								for (int k=0; k<operaArray.length(); k++) {
									JSONObject operaJso = operaArray.getJSONObject(k);
									String operaName = operaJso.optString("opera");
									// 获取操作锁状态
									String operaFile = 	engineFile.getParentFile().getCanonicalPath() + File.separator + operaName + ".xsd";					
									operaJso.put("locked", LockUtil.isOperaLocked(operaFile));		
									operaJso.put("isPublic", false);
									org.dom4j.Element portTypeOpera = 
										(org.dom4j.Element) portType.selectSingleNode("//*[name()='operation'][@name='" + operaName  + "']");
									if (portTypeOpera == null) {
										operaArray.put(k,"");
									} else {
										operaJso.put("desc", portTypeOpera.elementText("documentation"));
									}
								}
						} else {
							JSONArray operaArray = jso.optJSONArray("operations");
							if (operaArray != null && operaArray.length() > 0)
								for (int k=0; k<operaArray.length(); k++) {
									JSONObject operaJso = operaArray.getJSONObject(k);
									String operaName = operaJso.optString("opera");
									// 获取操作锁状态
									operaJso.put("locked", "");	
									operaJso.put("isPublic", true);
									
									for (Iterator opItr = importOperaList.iterator(); opItr.hasNext();) {
										Operation operation = (Operation)opItr.next();
										if (operaName.equals(operation.getName())) {
											operaJso.put("desc", operation.getDocumentationElement().getTextContent());
											operaJso.put("intfDesc", importPtDesc);
										}
									}
								}
						}
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
public String getInterfaces(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try {
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File wsdlPath = new File(unitFolder, "unit.wsdl");
		WSDLReader reader = WSDLUtil.getWSDLReader();
				
		JSONArray items = new JSONArray();		
		Dom4jUtil.initDocument(wsdlPath);
		org.dom4j.Element serviceRoot = Dom4jUtil.getRootEl();
		List importList = serviceRoot.selectNodes("//*[name()='import']");
		// engine WSDL路径集合
		Set enginPathSet = new HashSet();
		if (importList != null && !importList.isEmpty())
			for (Iterator itr = importList.iterator(); itr.hasNext();) {
				org.dom4j.Element importEl = (org.dom4j.Element)itr.next();
				String enginePath = importEl.attributeValue("location");
 				enginPathSet.add(enginePath);
			}
		
		if (enginPathSet != null && !enginPathSet.isEmpty())
			for (Iterator itr = enginPathSet.iterator(); itr.hasNext();) {
				String enginePath = (String)itr.next();
				File engineFile = new File(unitFolder, enginePath);
				Dom4jUtil.initDocument(engineFile);
				org.dom4j.Element engineRoot = (org.dom4j.Element) Dom4jUtil.getRootEl();
				List portTypes = engineRoot.selectNodes("//*[name()='portType']");
				if (portTypes !=null && !portTypes.isEmpty()) {
					for (Iterator ptItr = portTypes.iterator(); ptItr.hasNext();) {
						JSONObject jso = new JSONObject();
						org.dom4j.Element portType = (org.dom4j.Element) ptItr.next();
						String name = portType.attributeValue("name");
						jso.put("type", name);
						jso.put("isPublic", false);
						items.put(jso);
					}
				}
				
				// 引用的公共接口
				List importIntfList = engineRoot.selectNodes("//*[name()='import']");
				if (importIntfList != null && !importIntfList.isEmpty()) {
					for (Iterator importItr = importIntfList.iterator(); importItr.hasNext();) {
						JSONObject jso = new JSONObject();
						org.dom4j.Element importEl = (org.dom4j.Element) importItr.next();
						String importLocation = importEl.attributeValue("location");
						if (!"".equals(importLocation)) {
							File intfWsdlFile = new File(unitFolder, importLocation);
							
							if (intfWsdlFile.exists()) {
								Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfWsdlFile, true));
								Map<?,?> portTypeMap = wsdlDef.getAllPortTypes();
								for (Iterator<?> iter=portTypeMap.entrySet().iterator(); iter.hasNext(); ) {
									Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
									PortType portType = (PortType)entry.getValue();
									jso.put("type", portType.getQName().getLocalPart());
									jso.put("isPublic", true);
								}
							}
						}
						items.put(jso);
					}
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
		File dirPath = new File(unitFolder.getParentFile(), component + "/META-INF/");
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
public String addPublicBinding(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String bindingType = request.getParameter("type");
	WSDLReader reader = WSDLUtil.getWSDLReader();
	
	SAXBuilder builder = new SAXBuilder();
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File wsdlPath = new File(unitFolder, "unit.wsdl");
			
	Dom4jUtil.initDocument(wsdlPath);
	org.dom4j.Element serviceRoot = Dom4jUtil.getRootEl();
	List importList = serviceRoot.selectNodes("//*[name()='import']");
	
	// engine WSDL路径集合
	Set enginPathSet = new HashSet();
	String prefix = "";
	if (importList !=null && !importList.isEmpty())
		for (Iterator itr = importList.iterator(); itr.hasNext();) {
			org.dom4j.Element importEl = (org.dom4j.Element)itr.next();
			String enginePath = importEl.attributeValue("location");
			String namespace = importEl.attributeValue("namespace");
			
			org.dom4j.Namespace ns = serviceRoot.getNamespaceForURI(namespace);
				prefix = ns.getPrefix();
				enginPathSet.add(enginePath);
		}
	
	Set operaSet = new HashSet();
	if (enginPathSet != null && !enginPathSet.isEmpty())		
		for (Iterator itr = enginPathSet.iterator(); itr.hasNext();) {
			String enginePath = (String)itr.next();
			File engineFile = new File(unitFolder, enginePath);
			Dom4jUtil.initDocument(engineFile);
			org.dom4j.Element engineRoot = (org.dom4j.Element) Dom4jUtil.getRootEl();
			
			List importIntfList = engineRoot.selectNodes("//*[name()='import']");
			if (importIntfList != null && !importIntfList.isEmpty()) {
				for (Iterator importItr = importIntfList.iterator(); importItr.hasNext();) {
					JSONObject jso = new JSONObject();
					org.dom4j.Element importEl = (org.dom4j.Element) importItr.next();
					String importLocation = importEl.attributeValue("location");
					if (!"".equals(importLocation)) {
						File intfWsdlFile = new File(unitFolder, importLocation);
						if (intfWsdlFile.exists()) {
							if (importLocation.matches(".*?\\/intf/.*?" + bindingType + "/public.wsdl")) {
								Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(intfWsdlFile, true));
								Map<?,?> portTypeMap = wsdlDef.getAllPortTypes();
								for (Iterator<?> iter=portTypeMap.entrySet().iterator(); iter.hasNext(); ) {
									Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
									PortType portType = (PortType)entry.getValue();
									for (Iterator<?> it=portType.getOperations().iterator(); it.hasNext(); ) {
										Operation opera = (Operation)it.next();
										operaSet.add(opera.getName());
									}
								}
							}
						}
					}
				}
			}
		}
		
	
	if (operaSet != null && !operaSet.isEmpty()) {
		Dom4jUtil.initDocument(wsdlPath);
		serviceRoot = Dom4jUtil.getRootEl();
		
		String bindingName = "";
		List bindingList = serviceRoot.selectNodes("//*[name()='binding'][@type='" + prefix + ":" +  bindingType + "']");
		if (bindingList !=null && !bindingList.isEmpty()) 
			bindingName = bindingType + "-binding" + (bindingList.size() + 1);
		else
			bindingName = bindingType + "-binding";
			
		org.dom4j.Element bindingEl = serviceRoot.addElement("binding");
		bindingEl.addAttribute("name", bindingName);
		bindingEl.addAttribute("type", prefix + ":" +  bindingType);
		
		org.dom4j.Element soapBinding = bindingEl.addElement("soap:binding");
		soapBinding.addAttribute("transport" ,"http://schemas.xmlsoap.org/soap/http");
		soapBinding.addAttribute("style", "document");
		
		for (Iterator itr = operaSet.iterator(); itr.hasNext();) {
			String operaName = (String) itr.next();
			org.dom4j.Element operaEl = bindingEl.addElement("operation");
			operaEl.addAttribute("name", operaName);
			org.dom4j.Element soapOperaEl = operaEl.addElement("soap:operation");
			soapOperaEl.addAttribute("soapAction", "");
			soapOperaEl.addAttribute("style", "document");
			org.dom4j.Element inputEl = operaEl.addElement("input");
			inputEl.addElement("soap:body").addAttribute("use", "literal");
			org.dom4j.Element outputEl = operaEl.addElement("output");
			outputEl.addElement("soap:body").addAttribute("use", "literal");
		}
		
		Dom4jUtil.saveFile(wsdlPath);
	} else {
		// 操作列表为空则创建空binding
		Dom4jUtil.initDocument(wsdlPath);
		serviceRoot = Dom4jUtil.getRootEl();
		
		String bindingName = bindingType + "-binding1";
		org.dom4j.Element bindingEl = serviceRoot.addElement("binding");
		bindingEl.addAttribute("name", bindingName);
		bindingEl.addAttribute("type", prefix + ":" +  bindingType);
		
		org.dom4j.Element soapBinding = bindingEl.addElement("soap:binding");
		soapBinding.addAttribute("transport" ,"http://schemas.xmlsoap.org/soap/http");
		soapBinding.addAttribute("style", "document");
		
		Dom4jUtil.saveFile(wsdlPath);
	}
	response.setContentType("text/plain; charset=utf-8");
	return "true";
}

@SuppressWarnings("unchecked")
public String addBinding(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String unit = request.getParameter("unit");
	String bindingType = request.getParameter("type");
	
	SAXBuilder builder = new SAXBuilder();
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File wsdlPath = new File(unitFolder, "unit.wsdl");
			
	Dom4jUtil.initDocument(wsdlPath);
	org.dom4j.Element serviceRoot = Dom4jUtil.getRootEl();
	List importList = serviceRoot.selectNodes("//*[name()='import']");
	
	// engine WSDL路径集合
	Set enginPathSet = new HashSet();
	String prefix = "";
	if (importList !=null && !importList.isEmpty())
		for (Iterator itr = importList.iterator(); itr.hasNext();) {
			org.dom4j.Element importEl = (org.dom4j.Element)itr.next();
			String enginePath = importEl.attributeValue("location");
			String namespace = importEl.attributeValue("namespace");
			
			org.dom4j.Namespace ns = serviceRoot.getNamespaceForURI(namespace);
				prefix = ns.getPrefix();
				enginPathSet.add(enginePath);
		}
	
	Set operaSet = new HashSet();
	if (enginPathSet != null && !enginPathSet.isEmpty())		
		for (Iterator itr = enginPathSet.iterator(); itr.hasNext();) {
			String enginePath = (String)itr.next();
			File engineFile = new File(unitFolder, enginePath);
			Dom4jUtil.initDocument(engineFile);
			org.dom4j.Element engineRoot = (org.dom4j.Element) Dom4jUtil.getRootEl();
			
			org.dom4j.Element portType =
				(org.dom4j.Element)engineRoot.selectSingleNode("//*[name()='portType'][@name='" + bindingType.replaceAll(".*?:", "") + "']");
			if (portType != null) {
				String name = portType.attributeValue("name", "");
				List portOpList = portType.selectNodes("//*[name()='portType'][@name='" + name + "']/*[name()='operation']");
				if (portOpList !=null && !portOpList.isEmpty())
					for (Iterator portOpItr = portOpList.iterator(); portOpItr.hasNext();) {
						org.dom4j.Element opertaion = (org.dom4j.Element) portOpItr.next();
						if (opertaion != null)
							operaSet.add(opertaion.attributeValue("name"));
					}
			}
		}
	
	if (operaSet != null && !operaSet.isEmpty()) {
		Dom4jUtil.initDocument(wsdlPath);
		serviceRoot = Dom4jUtil.getRootEl();
		
		String bindingName = "";
		List bindingList = serviceRoot.selectNodes("//*[name()='binding'][@type='" + prefix + ":" +  bindingType + "']");
		if (bindingList !=null && !bindingList.isEmpty()) 
			bindingName = bindingType + "-binding" + (bindingList.size() + 1);
		else
			bindingName = bindingType + "-binding";
			
		org.dom4j.Element bindingEl = serviceRoot.addElement("binding");
		bindingEl.addAttribute("name", bindingName);
		bindingEl.addAttribute("type", prefix + ":" +  bindingType);
		
		org.dom4j.Element soapBinding = bindingEl.addElement("soap:binding");
		soapBinding.addAttribute("transport" ,"http://schemas.xmlsoap.org/soap/http");
		soapBinding.addAttribute("style", "document");
		
		for (Iterator itr = operaSet.iterator(); itr.hasNext();) {
			String operaName = (String) itr.next();
			org.dom4j.Element operaEl = bindingEl.addElement("operation");
			operaEl.addAttribute("name", operaName);
			org.dom4j.Element soapOperaEl = operaEl.addElement("soap:operation");
			soapOperaEl.addAttribute("soapAction", "");
			soapOperaEl.addAttribute("style", "document");
			org.dom4j.Element inputEl = operaEl.addElement("input");
			inputEl.addElement("soap:body").addAttribute("use", "literal");
			org.dom4j.Element outputEl = operaEl.addElement("output");
			outputEl.addElement("soap:body").addAttribute("use", "literal");
		}
		
		Dom4jUtil.saveFile(wsdlPath);
	} else {
		// 操作列表为空则创建空binding
		Dom4jUtil.initDocument(wsdlPath);
		serviceRoot = Dom4jUtil.getRootEl();
		
		String bindingName = bindingType + "-binding1";
		org.dom4j.Element bindingEl = serviceRoot.addElement("binding");
		bindingEl.addAttribute("name", bindingName);
		bindingEl.addAttribute("type", prefix + ":" +  bindingType);
		
		org.dom4j.Element soapBinding = bindingEl.addElement("soap:binding");
		soapBinding.addAttribute("transport" ,"http://schemas.xmlsoap.org/soap/http");
		soapBinding.addAttribute("style", "document");
		
		Dom4jUtil.saveFile(wsdlPath);
	}
	response.setContentType("text/plain; charset=utf-8");
	return "true";
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
		String intf = item.optString("intf");
		String bindingName = item.optString("bindingName");
		QName typeName = QName.get(intf, "");
		
		synchronizeBinding(unit, intf, bindingName, typeName);
	}
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

private void synchronizeBinding(String unit, String intf, String bindingName, QName typeName) throws Exception {
	try {
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		
		// server unit.wsdl文件路径
		File wsdlPath = new File(unitFolder, "unit.wsdl");
			
		// 找到前缀对应的命名空间
		Dom4jUtil.initDocument(wsdlPath);
		org.dom4j.Element serviceRoot = Dom4jUtil.getRootEl();
		org.dom4j.Namespace nspace = serviceRoot.getNamespaceForPrefix(typeName.getNamespacePrefix());
		String namespace = nspace != null ? nspace.getURI() : "";
		
		Set engineOpSet = new HashSet();
		// engine unti.wsdl文件路径
		List importList = serviceRoot.selectNodes("//*[name()='import']");
		if (importList !=null && !importList.isEmpty())
			for (Iterator importItr = importList.iterator(); importItr.hasNext();) {
				org.dom4j.Element importEl = (org.dom4j.Element)importItr.next();
				String ns = importEl.attributeValue("namespace", "");
				if (ns.equals(namespace)) {
					String enginePath = importEl.attributeValue("location", "");
					File engineFile = new File(unitFolder, enginePath);
					if (engineFile.exists())
						Dom4jUtil.initDocument(engineFile);
						org.dom4j.Element engineRoot = Dom4jUtil.getRootEl();
						org.dom4j.Element portType = 
							(org.dom4j.Element)engineRoot.selectSingleNode("//*[name()='portType'][@name='" + typeName.getName() + "']");
						if (portType != null) {
							List engineOperaList = 
								portType.selectNodes("//*[name()='portType'][@name='" + typeName.getName() + "']/*[name()='operation']");
							for (Iterator itr = engineOperaList.iterator(); itr.hasNext();) {
								engineOpSet.add(((org.dom4j.Element)itr.next()).attributeValue("name"));
							}
						}
				}
			}
		
		if(engineOpSet !=null && !engineOpSet.isEmpty()) {
			Dom4jUtil.initDocument(wsdlPath);
			serviceRoot = Dom4jUtil.getRootEl();
			List bindingList = serviceRoot.selectNodes("//*[name()='binding'][@type='" + intf  + "']");
			
			for (Iterator itr= engineOpSet.iterator(); itr.hasNext();) {
				String engineOpName = (String)itr.next();
				
				if (bindingList !=null && !bindingList.isEmpty())
				for (Iterator bItr = bindingList.iterator(); bItr.hasNext();) {
					org.dom4j.Element serviceBinding = (org.dom4j.Element) bItr.next();
					String opName = serviceBinding.attributeValue("name", "");
					
					List bindingOperaList = serviceBinding.selectNodes("//*[name()='binding'][@name='" + opName + "']/*[name()='operation']");
					if (bindingOperaList !=null && !bindingOperaList.isEmpty())
						for (Iterator operaItr = bindingOperaList.iterator(); operaItr.hasNext();) {
							org.dom4j.Element bindingOpera = (org.dom4j.Element)operaItr.next();
							String name = bindingOpera.attributeValue("name");
							if (!engineOpSet.contains(name))
								bindingOpera.getParent().remove(bindingOpera);
						}
					
					org.dom4j.Element bindingOp = 
						(org.dom4j.Element) serviceBinding.selectSingleNode("//*[name()='operation'][@name='" + engineOpName  + "']");
					
					// 添加operation
					if (bindingOp == null) {
						bindingOp = serviceBinding.addElement("operation");
						bindingOp.addAttribute("name", engineOpName);
						
						org.dom4j.Element newSoapOpera = bindingOp.addElement("soap:operation");
						newSoapOpera.addAttribute("soapAction", "");
						newSoapOpera.addAttribute("style", "document");
						org.dom4j.Element inputEl = bindingOp.addElement("input");
						inputEl.addElement("soap:body").addAttribute("use", "literal");
						org.dom4j.Element outputEl = bindingOp.addElement("output");
						outputEl.addElement("soap:body").addAttribute("use", "literal");
					}
				}
			}
			Dom4jUtil.saveFile(wsdlPath);
		}
	} finally {}
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
		
		// 删除port
		List portList = Dom4jUtil.Service.getPortList();
		if (portList != null && !portList.isEmpty())
			for (Iterator portItr = portList.iterator(); portItr.hasNext();) {
				org.dom4j.Element port = (org.dom4j.Element) portItr.next();
				Dom4jUtil.Service.getService().remove(port);
			}
	}
	
	// 写入WSDL文件
	Dom4jUtil.saveFile(wsdlPath);
	response.setContentType("text/json; charset=utf-8");
	return "true";
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
			logger.error("", t);
		responseWrapper.sendError(t.getMessage());
	}
	catch (Exception e) {
		logger.error("", e);
		responseWrapper.sendError(e.getMessage());
	}
}
else {
	responseWrapper.sendError("["+request.getMethod()+"]拒绝执行，没有指定 operation 参数");
}

%>