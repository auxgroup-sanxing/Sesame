<%@page
	import="com.sanxing.studio.*,com.sanxing.studio.utils.*,com.sanxing.studio.team.svn.*,com.sanxing.studio.team.*"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="javax.xml.namespace.QName"%>
<%@page import="javax.xml.xpath.*"%>
<%@page import="javax.wsdl.*"%>
<%@page import="javax.wsdl.xml.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.apache.ws.commons.schema.*"%>
<%@page import="org.dom4j.*, org.dom4j.io.*"%>
<%@page import="org.json.*"%>
<%@page import="org.w3c.dom.NodeList"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="javax.xml.transform.stream.StreamSource"%>

<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>

<%!
private Logger logger = LoggerFactory.getLogger(this.getClass());

/*
private XPathFactory xfactory = XPathFactory.newInstance();

private org.w3c.dom.Element getFormat(XmlSchemaAppInfo appInfo)
{
	NodeList  list = appInfo.getMarkup();
	if (list.getLength() > 0) {
		Node parent =list.item(0).getParentNode();
		for (int i=0,len=list.getLength(); i<len; i++) {
			Node node = list.item(i);
			if (node.getNodeName().equals("format")) {
				return (org.w3c.dom.Element)node;
			}
		}
		//XPath xpath = xfactory.newXPath();
		//xfactory.
		//String expression = "format";
		//Node formatNode = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
		//if (formatNode != null) {
		//	System.out.println(formatNode.getAttributes());
		//}
	}
	return null;
}
*/

/**
* 获取 XML Schema 对象的描述信息
*/
private String getDocumentation(XmlSchemaDocumentation docu)
{
	NodeList  list = docu.getMarkup();
	if (list.getLength() > 0) {
		StringBuffer buf = new StringBuffer();
		for (int i=0, len=list.getLength(); i<len; i++) {
			org.w3c.dom.Node node = list.item(i);
			buf.append(node.getNodeValue()!=null ? node.getNodeValue() : "");
		}
		return buf.toString();
	}
	else {
		return null;
	}
}

private String getJbiDescription(Document jbiDoc) {
	try {
		Element rootEl = jbiDoc.getRootElement();
		List list = rootEl.elements();
		if (list.size() > 0) {
			Element idenEl, firstEl = (Element)list.get(0);
			org.dom4j.QName qname = new org.dom4j.QName("identification", rootEl.getNamespace());
			if ((idenEl = firstEl.element(qname)) != null) {
				return idenEl.elementText(new org.dom4j.QName("description", rootEl.getNamespace()));
			}
		}
	}
	catch (Exception e) {
	}
	return null;
}

private String getTextContent(org.w3c.dom.Element element)
{
	org.w3c.dom.NodeList  list = element.getChildNodes();
	if (list.getLength() > 0) {
		StringBuffer buf = new StringBuffer();
		for (int i=0, len=list.getLength(); i<len; i++) {
			org.w3c.dom.Node node = list.item(i);
			buf.append(node.getNodeValue()!=null ? node.getNodeValue() : "");
		}
		return buf.toString();
	}
	else {
		return "";
	}
}

//返回Operation的 input/output 元素名称
@SuppressWarnings("unchecked")
private QName getOperationParameter(Definition definition, String operationName, String channel) {
	Map<QName, PortType> portTypes = definition.getAllPortTypes();
	Iterator<Map.Entry<QName, PortType>> entries = portTypes.entrySet().iterator();
	while (entries.hasNext()) {
		Map.Entry<QName, PortType> entry = entries.next();
		Operation operation = entry.getValue().getOperation(operationName, null, null);
		if (operation != null) {
			Message message = null;
			if (channel.equals("request")) {
				message = operation.getInput()!=null ? operation.getInput().getMessage() : null;
			}
			else if (channel.equals("response")) {
				message = operation.getOutput()!=null ? operation.getOutput().getMessage() : null;
			}
			else {
				Fault fault = operation.getFault(channel);
				if (fault != null) message = fault.getMessage();
			}
			if (message != null) {
			    javax.wsdl.Part part = message.getPart("parameters");
				if (part != null) {
					return part.getElementName();
				}
			}
		}
	}
	return null;
}

private void schema2Nodes(XmlSchemaCollection collection, XmlSchemaType type, JSONArray array) 
	throws JSONException, XPathExpressionException
{
	if (type instanceof XmlSchemaComplexType) {
		XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
		if (complexType.getParticle() instanceof XmlSchemaSequence) {
			XmlSchemaSequence xsdSequence = (XmlSchemaSequence)complexType.getParticle();
			XmlSchemaObjectCollection coll = xsdSequence.getItems();
			for (Iterator<?> iter = coll.getIterator(); iter.hasNext(); ) {
				Object item = iter.next();
				if (item instanceof XmlSchemaElement) {
					String name = null;
					String desc = null;
					XmlSchemaElement elem = (XmlSchemaElement) item;
					if (elem.getRefName() != null) {
						XmlSchemaObjectCollection annColl = elem.getAnnotation().getItems();
						for (Iterator<?> it = annColl.getIterator(); it.hasNext(); ) {
							Object ann = it.next();
							if (ann instanceof XmlSchemaDocumentation) {
								desc = getDocumentation((XmlSchemaDocumentation)ann);
							}
						}
						XmlSchemaElement refEl = collection.getElementByQName(elem.getRefName());
						if (refEl != null) elem = refEl;
					}
					if (elem.getAnnotation() != null) {
						XmlSchemaObjectCollection annColl = elem.getAnnotation().getItems();
						org.w3c.dom.Element formatEl = null;
						for (Iterator<?> it = annColl.getIterator(); it.hasNext(); ) {
							Object ann = it.next();
							if (ann instanceof XmlSchemaDocumentation) {
								desc = getDocumentation((XmlSchemaDocumentation)ann);
							}
							else if (ann instanceof XmlSchemaAppInfo) {
								//formatEl = getFormat((XmlSchemaAppInfo)ann);
							}
						}
					}
					JSONObject node = new JSONObject();
					name = elem.getName();
					node.put("name", name);
					node.put("text", name+(desc!=null?" "+desc:""));
					node.put("prefix", elem.getQName().getPrefix());
					node.put("qtip", elem.getQName().getNamespaceURI());
					node.put("children", new JSONArray());
					array.put(node);
					XmlSchemaType childType = elem.getSchemaType();
					if (childType instanceof XmlSchemaComplexType) {
						node.put("iconCls", "icon-element");
						schema2Nodes(collection, childType, node.getJSONArray("children"));
					}
					else if (childType instanceof XmlSchemaSimpleType) {
						XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType)childType;
						QName typeName = elem.getSchemaTypeName();
						if (typeName == null) typeName = simpleType.getBaseSchemaTypeName();
						node.put("iconCls", "icon-"+(typeName!=null ? typeName.getLocalPart() : "unknown"));
						node.put("collapsible", false);
						node.put("expanded", true);
					}
				}
			}
			
		}
		else if (complexType.getParticle() instanceof XmlSchemaChoice) {
			
		}
		else if (complexType.getParticle() instanceof XmlSchemaAll) {
			
		}
	}
}

public String load(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String servicePath = request.getParameter("service");

	File file = Configuration.getWorkspaceFile(servicePath);
    StringBuffer buffer = new StringBuffer();
	if (file.exists()) {
		InputStream input = new FileInputStream(file);
		try {
		    String line;
	        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
	        while ((line = reader.readLine()) != null) {
	            buffer.append(line);
	            buffer.append("\n");
	        }
		}
        finally {
        	input.close();
        }
	}
	else {
		buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		buffer.append("<process/>\n");
	}
	
	return buffer.toString();
}

@SuppressWarnings("unchecked")
public String loadDSNs(HttpServletRequest request, HttpServletResponse response) throws Exception {
	List list = Configuration.getDataSources();
	JSONArray items = new JSONArray();
	
	if (list != null && !list.isEmpty()) {
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			JSONObject obj = new JSONObject();
			org.jdom.Element datasource = (org.jdom.Element) iter.next();
			JSONObject item = new JSONObject();
			item.put("name", datasource.getChildText("jndi-name"));
			items.put(item);
		}
	}

	JSONObject result = new JSONObject();
	result.put("items", items);
	return result.toString();
}

@SuppressWarnings("unchecked")
public String getDomChildren(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String nodeid = request.getParameter("node");
	String processPath = request.getParameter("service");
	String message = request.getParameter("message");
	String schema = request.getParameter("schema");

	response.setContentType("text/json; charset=utf-8");
	
	File serviceFile = Configuration.getWorkspaceFile(processPath);
	JSONArray items = new JSONArray();
	WSDLReader reader = WSDLUtil.getWSDLReader();

	if (schema != null && schema.endsWith(".xsd")) {
		File schemaFile = Application.getWorkspaceFile(schema);
		File unitFile = new File(schemaFile.getParentFile(), "unit.wsdl");
		String operaName = schemaFile.getName().replaceFirst("\\.xsd$", "");
		Definition wsdl = reader.readWSDL(unitFile.toURI().toString());
		
		QName param = getOperationParameter(wsdl, operaName, message);
		if (param == null) return items.toString();

		response.addHeader("elementName", param.getLocalPart());
		response.addHeader("namespace", param.getNamespaceURI());
		XmlSchemaCollection schemaColl = new XmlSchemaCollection();
		InputSource input = new InputSource(schemaFile.getCanonicalPath());

		XmlSchema xmlSchema = schemaColl.read(input, null);
		XmlSchemaElement messageEl = xmlSchema.getElementByName(param);
		if (messageEl != null) {
			schema2Nodes(schemaColl, messageEl.getSchemaType(), items);
		}
	}
	else {
		if (schema != null) processPath = schema;
		File schemaFile = new File(serviceFile.getParentFile(), serviceFile.getName().replaceFirst("\\.xml$", ".xsd"));
		File unitFile = new File(schemaFile.getParentFile(), "unit.wsdl");
		String operaName = schemaFile.getName().replaceFirst("\\.xsd$", "");
		Definition wsdl = reader.readWSDL(unitFile.toURI().toString());
		
		QName param = getOperationParameter(wsdl, operaName, message);
		if (param == null) return items.toString();
		
		response.addHeader("elementName", param.getLocalPart());
		response.addHeader("namespace", param.getNamespaceURI());
		
		XmlSchemaCollection schemaColl = new XmlSchemaCollection();
		InputSource input = new InputSource(schemaFile.getCanonicalPath());
		XmlSchema xmlSchema = schemaColl.read(input, null);

		XmlSchemaElement messageEl = xmlSchema.getElementByName(param);
		if (messageEl != null) {
			schema2Nodes(schemaColl, messageEl.getSchemaType(), items);
		}
	}
	
	return items.toString();
}

public String getServices(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String nodeid = request.getParameter("node");
	String nodePath = request.getParameter("path");
	String service = request.getParameter("service");
	File serviceFile = Configuration.getWorkspaceFile(service);

	WSDLReader reader = WSDLUtil.getWSDLReader();

	JSONArray result = new JSONArray();
	if (nodeid.equals("ROOT")) {
		File engineFolder = serviceFile.getParentFile().getParentFile();
		File projectFolder = engineFolder.getParentFile();
		File[] entries = engineFolder.listFiles();
		if (entries != null) {
		    for (File entry : entries) {
				if (!entry.isDirectory() || entry.getName().startsWith(".")) continue;
				File file  = new File(entry, "unit.wsdl");
				Definition wsdlDef = reader.readWSDL(file.toURI().toString());

				org.w3c.dom.Element docuEl = wsdlDef.getDocumentationElement();
				String desc = docuEl != null ? getTextContent(docuEl) : null; 
				Iterator<Service> services = wsdlDef.getServices().values().iterator();
				String serviceName = services.hasNext() ? services.next().getQName().getLocalPart() : null;
				JSONObject item = new JSONObject();
				item.put("icon", "../images/obj16/wsdl_file.gif");
				item.put("text", desc!=null ? desc : entry.getName());
				item.put("name", projectFolder.getName()+'/'+engineFolder.getName()+'/'+entry.getName());
				item.put("namespace", wsdlDef.getTargetNamespace());
				item.put("service-name", serviceName);
				item.put("allowDrag", false);
				result.put(item);
			}
		}
		File connFolder = new File(projectFolder, "client");
		entries = connFolder.listFiles();
		if (entries != null) {
		    for (File entry : entries) {
				if (!entry.isDirectory() || entry.getName().startsWith(".")) continue;
				File file  = new File(entry, "unit.wsdl");
				Definition wsdlDef = reader.readWSDL(file.toURI().toString());
				org.w3c.dom.Element docuEl = wsdlDef.getDocumentationElement();
				String desc = docuEl != null ? getTextContent(docuEl) : null; 
				Iterator<Service> services = wsdlDef.getServices().values().iterator();
				String serviceName = services.hasNext() ? services.next().getQName().getLocalPart() : null;

				JSONObject item = new JSONObject();
				item.put("icon", "../images/obj16/wsdl_file.gif");
				item.put("text", desc!=null ? desc : entry.getName());
				item.put("name", projectFolder.getName()+"/client/"+entry.getName());
				item.put("namespace", wsdlDef.getTargetNamespace());
				item.put("service-name", serviceName);
				item.put("allowDrag", false);
				result.put(item);
			}
		}

		File propertyFile = new File(projectFolder, "project.properties");
		Properties properties = new Properties();
		if (propertyFile.exists()) {
			InputStream input = new FileInputStream(propertyFile);
			properties.load(input);
			input.close();
		}
		
		JSONArray references = new JSONArray(properties.getProperty("project.dependency", "[]"));
		SAXReader saxReader = new SAXReader();
		for (int i=0,count=references.length(); i<count; i++) {
			projectFolder = Application.getWorkspaceFile(references.getString(i));
			Document doc = saxReader.read(new File(projectFolder, "jbi.xml"));
			String projectDesc = getJbiDescription(doc);
			JSONObject project = new JSONObject();
			JSONArray children = new JSONArray();
			project.put("icon", "../images/obj16/prj_obj.gif");
			project.put("text", projectDesc+"("+projectFolder.getName()+")");
			project.put("name", projectFolder.getName());
			project.put("allowDrag", false);
			project.put("children", children);
			result.put(project);
			
			engineFolder = new File(projectFolder, "engine");
			entries = engineFolder.listFiles();
			if (entries != null) {
			    for (File entry : entries) {
					if (!entry.isDirectory() || entry.getName().startsWith(".")) continue;
					File file  = new File(entry, "unit.wsdl");
					Definition wsdlDef = reader.readWSDL(file.toURI().toString());

					org.w3c.dom.Element docuEl = wsdlDef.getDocumentationElement();
					String desc = docuEl != null ? getTextContent(docuEl) : null; 
					Iterator<Service> services = wsdlDef.getServices().values().iterator();
					String serviceName = services.hasNext() ? services.next().getQName().getLocalPart() : null;
					JSONObject item = new JSONObject();
					item.put("icon", "../images/obj16/wsdl_file.gif");
					item.put("text", desc!=null ? desc : entry.getName());
					item.put("name", engineFolder.getName()+'/'+entry.getName());
					item.put("namespace", wsdlDef.getTargetNamespace());
					item.put("service-name", serviceName);
					item.put("allowDrag", false);
					children.put(item);
				}
			}
			connFolder = new File(projectFolder, "client");
			entries = connFolder.listFiles();
			if (entries != null) {
			    for (File entry : entries) {
					if (!entry.isDirectory() || entry.getName().startsWith(".")) continue;
					File file  = new File(entry, "unit.wsdl");
					Definition wsdlDef = reader.readWSDL(file.toURI().toString());
					org.w3c.dom.Element docuEl = wsdlDef.getDocumentationElement();
					String desc = docuEl != null ? getTextContent(docuEl) : null; 
					Iterator<Service> services = wsdlDef.getServices().values().iterator();
					String serviceName = services.hasNext() ? services.next().getQName().getLocalPart() : null;
	
					JSONObject item = new JSONObject();
					item.put("icon", "../images/obj16/wsdl_file.gif");
					item.put("text", desc!=null ? desc : entry.getName());
					item.put("name", connFolder.getName()+"/"+entry.getName());
					item.put("namespace", wsdlDef.getTargetNamespace());
					item.put("service-name", serviceName);
					item.put("allowDrag", false);
					children.put(item);
				}
			}
		}
	}
	else {
		File entry = Application.getWorkspaceFile(nodePath);
		if (entry.isDirectory()) {
			File file = new File(entry, "unit.wsdl");
			Definition wsdlDef = reader.readWSDL(file.toURI().toString());

			Collection<PortType> portTypes = wsdlDef.getPortTypes().values();
			for (PortType portType : portTypes) {
				String intfName = portType.getQName().getLocalPart();
				
				org.w3c.dom.Element docuEl = portType.getDocumentationElement();
				String desc = docuEl != null ? getTextContent(docuEl) : intfName; 
				JSONObject item = new JSONObject();
				item.put("name", intfName);
				item.put("text", desc!=null && desc.length()>0 ? desc : intfName);
				item.put("allowDrag", false);
				result.put(item);

				JSONArray children = new JSONArray();
				item.put("children", children);
				List<Operation> list=portType.getOperations();
				for (Operation operation : list) {
					String operaName = operation.getName();
					docuEl = operation.getDocumentationElement();
					desc = docuEl != null ? getTextContent(docuEl) : ""; 
					JSONObject child = new JSONObject();
					child.put("iconCls", "palette_control_send_recv");
					child.put("text", operaName+"-"+desc);
					child.put("leaf", true);
				
					JSONObject options = new JSONObject();
					child.put("options", options);
					options.put("ref", (nodePath+"/"+operaName+".xsd"));
					options.put("operation-name", operaName);
					children.put(child);
				}
			}
		}
	}
	
	response.setContentType("text/json; charset=utf-8");
	return result.toString();
}

@SuppressWarnings("unchecked")
public String getDeclaration(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String ref = request.getParameter("ref");
	File file = Configuration.getWorkspaceFile(ref.replaceFirst("\\.xsd$", ".xml"));
	
	JSONObject result = new JSONObject();
	if (file.exists()) {
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Element root = builder.build(file).getRootElement();
		if (root.getChildren().size() > 0) {
			result.put("type", root.getName());
		}
		else {
			result.put("type", "");
		}
	}
	
	response.setContentType("text/json; charset=utf-8");
	return result.toString();
}

public String save(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String path = request.getParameter("service");
	File file = Configuration.getWorkspaceFile(path);
	String data = request.getParameter("data");

	OutputStream output = new FileOutputStream(file);
	try {
	     /*
		SAXBuilder builder = new SAXBuilder();
		Reader reader= new StringReader(data);
		Document doc = builder.build(reader);
		//flowEl.setName("flow");
		//flowEl.addNamespaceDeclaration(Namespace.NO_NAMESPACE);
	       XMLOutputter outputter = new XMLOutputter();
	       outputter.setFormat(Format.getPrettyFormat().setEncoding("utf-8").setIndent("  "));
	       outputter.output(document, outStream);
		*/
		Writer writer = new OutputStreamWriter(output, "utf-8");
		writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		writer.write(data);
		writer.flush();
	}
	finally {
		output.close();
	}
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

public String getNamespace(HttpServletRequest request, HttpServletResponse response) throws Exception{
	
	String schemaPath = (String)request.getParameter("schemaPath");
	String wsdlPath = (String)request.getParameter("wsdlPath");
	String operaName = (String)request.getParameter("operaName");
	String ns = "";
	
	// 读取input message name
	String rootName = "";
	File wsdlFile = Configuration.getWorkspaceFile(wsdlPath).getCanonicalFile();
	if (wsdlFile.exists()) {
		Definition defination = WSDLUtil.getWSDLReader().readWSDL(wsdlFile.toURI().toString());
		Map ptMap = defination.getAllPortTypes();
		if (ptMap != null) {
			for (Iterator itr = ptMap.entrySet().iterator(); itr.hasNext();) {
				if (!"".equals(rootName)) break;
				
				Map.Entry entry = (Map.Entry)itr.next();
				PortType portType = (PortType)entry.getValue();
				List opList = portType.getOperations();
				if (opList != null && !opList.isEmpty())
				for (Iterator iter = opList.iterator(); iter.hasNext();) {
					Operation operation = (Operation)iter.next();
					if (operaName.equals(operation.getName())) {
						Input input = operation.getInput();
						if (input != null) {
							Message message = input.getMessage();
							rootName = (message != null) ? message.getPart("parameters").getElementName().getLocalPart():"";
							break;
						}
					}
				}
			}
		}
	}
	
	if ("".equals(rootName))
		rootName = operaName;
	
	// 转换schema
	File schemaFile = Configuration.getWorkspaceFile(schemaPath).getCanonicalFile();
	if (schemaFile.exists()) {
		InputStream is = schemaFile.toURI().toURL().openStream();
		try {
			XmlSchemaCollection schemaCol = new XmlSchemaCollection();
			StreamSource source = new StreamSource(is);
			source.setSystemId(schemaFile.toURI().toString());
			XmlSchema schema = schemaCol.read(source, null);
			
			XmlSchemaElement schemaEl = schema.getElementByName(rootName);
			if(schemaEl == null){
				throw new Exception("schemaElement ["+rootName+"] doesn't exist in the schema!!");
			}
			XmlSchemaType xsdType = schemaEl.getSchemaType();
			ns = schemaEl.getQName().getNamespaceURI();
		} catch(Exception e){
			ns = "";
		} finally {
			is.close();
		}
	}
	String path = request.getParameter("service");
	File file = Configuration.getWorkspaceFile(path);
	if(file.exists()){
	   ns=""; 
	}
	
    return ns;
}

// 根据shema生成单步调试输入内容
@SuppressWarnings("unchecked")
public String  generateXml(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String schemaPath = (String)request.getParameter("schemaPath");
	String wsdlPath = (String)request.getParameter("wsdlPath");
	String operaName = (String)request.getParameter("operaName");
	String xml = "";
	
	String xmlPath = wsdlPath.replaceFirst("unit.wsdl",operaName+".xml");
	File xmlFile = Configuration.getWorkspaceFile(xmlPath).getCanonicalFile();
	SAXBuilder builder = new SAXBuilder();
	org.jdom.Document newdoc = builder.build(xmlFile);
	
	// 读取input message name
	String rootName = "";
	File wsdlFile = Configuration.getWorkspaceFile(wsdlPath).getCanonicalFile();
	if (wsdlFile.exists()) {
		Definition defination = WSDLUtil.getWSDLReader().readWSDL(wsdlFile.toURI().toString());
		Map ptMap = defination.getAllPortTypes();
		if (ptMap != null) {
			for (Iterator itr = ptMap.entrySet().iterator(); itr.hasNext();) {
				if (!"".equals(rootName)) break;
				
				Map.Entry entry = (Map.Entry)itr.next();
				PortType portType = (PortType)entry.getValue();
				List opList = portType.getOperations();
				if (opList != null && !opList.isEmpty())
				for (Iterator iter = opList.iterator(); iter.hasNext();) {
					Operation operation = (Operation)iter.next();
					if (operaName.equals(operation.getName())) {
						Input input = operation.getInput();
						if (input != null) {
							Message message = input.getMessage();
							if(message.getPart("parameters") == null || message.getPart("parameters").getElementName() == null)
								throw new Exception("error message in unit.wsdl!!!");
							rootName = (message != null) ? message.getPart("parameters").getElementName().getLocalPart():"";
							break;
						}
					}
				}
			}
		}
	}
	
	if ("".equals(rootName))
		rootName = operaName;
	
	// 转换schema
	File schemaFile = Configuration.getWorkspaceFile(schemaPath).getCanonicalFile();
	if (schemaFile.exists()) {
		InputStream is = schemaFile.toURI().toURL().openStream();
		try {
			XmlSchemaCollection schemaCol = new XmlSchemaCollection();
			StreamSource source = new StreamSource(is);
			source.setSystemId(schemaFile.toURI().toString());
			XmlSchema schema = schemaCol.read(source, null);

			// request:操作名  response: 操作名 + respones
			org.jdom.Element xmlEl = SchemaUtil.schema2Xml(schema, rootName, newdoc.getRootElement());
			org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
			format.setEncoding(response.getCharacterEncoding());
			format.setIndent("  ");
			org.jdom.output.XMLOutputter outter = new org.jdom.output.XMLOutputter(format);
			xml = outter.outputString(xmlEl);
		} catch(Exception e){
			xml = "";
		} finally {
			is.close();
		}
	}
	
	response.setContentType("text/plain; charset=utf-8");
	return xml;
}

//	根据shema生成callout模拟数据
@SuppressWarnings("unchecked")
public String  generateEmulatorXml(HttpServletRequest request, HttpServletResponse response) throws Exception { 
	String schemaPath = (String)request.getParameter("schemaPath");
	String wsdlPath = (String)request.getParameter("wsdlPath");
	String operaName = (String)request.getParameter("operaName");
	String servicePath = (String)request.getParameter("servicePath");
	String xml = "";
	// 读取input message name
	String rootName = "";
	File path = Configuration.getWorkspaceFile(servicePath).getParentFile();
	SAXBuilder builder = new SAXBuilder();
	org.jdom.Document newdoc = builder.build(Configuration.getWorkspaceFile(servicePath));
	File wsdlFile = new File(path, wsdlPath);//.getCanonicalFile();
	
	if(!wsdlFile.exists())
		// 如果unit.wsdl是绝对路径
		wsdlFile = Configuration.getWorkspaceFile(wsdlPath);
	
	if (wsdlFile.exists()) {
		Definition defination = WSDLUtil.getWSDLReader().readWSDL(wsdlFile.toURI().toString());
		Map ptMap = defination.getAllPortTypes();
		if (ptMap != null) {
			for (Iterator itr = ptMap.entrySet().iterator(); itr.hasNext();) {
				if (!"".equals(rootName)) break;
				
				Map.Entry entry = (Map.Entry)itr.next();
				PortType portType = (PortType)entry.getValue();
				List opList = portType.getOperations();
				if (opList != null && !opList.isEmpty())
				for (Iterator iter = opList.iterator(); iter.hasNext();) {
					Operation operation = (Operation)iter.next();
					if (operaName.equals(operation.getName())) {
						Output output = operation.getOutput();
						if (output != null) {
							Message message = output.getMessage();
							rootName = (message != null) ? message.getPart("parameters").getElementName().getLocalPart():"";
							break;
						}
					}
				}
			}
		}
	}
	
	if ("".equals(rootName))
		rootName = operaName + "Response";
	
	// 转换schema
	File schemaFile = new File(path, schemaPath).getCanonicalFile();
	
	if(!schemaFile.exists())
		// schemaPath是绝对路径
		schemaFile = Configuration.getWorkspaceFile(schemaPath).getCanonicalFile();
	
	if (schemaFile.exists()) {
		InputStream is = schemaFile.toURI().toURL().openStream();
		try {
			XmlSchemaCollection schemaCol = new XmlSchemaCollection();
			StreamSource source = new StreamSource(is);
			source.setSystemId(schemaFile.toURI().toString());
			XmlSchema schema = schemaCol.read(source, null);

			// request:操作名  response: 操作名 + respones
			org.jdom.Element xmlEl = SchemaUtil.schema2Xml(schema, rootName, newdoc.getRootElement());
			org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
			format.setEncoding(response.getCharacterEncoding());
			format.setIndent("  ");
			org.jdom.output.XMLOutputter outter = new org.jdom.output.XMLOutputter(format);
			xml = outter.outputString(xmlEl);
		} catch(Exception e){
			xml = "";
		} finally {
			is.close();
		}
	}
	
	response.setContentType("text/plain; charset=utf-8");
	return xml;
}

//	选择错误应答后生成错误应答名称列表
@SuppressWarnings("unchecked")
public String  getFaultList(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String wsdlPath = (String)request.getParameter("wsdlPath");
	String operaName = (String)request.getParameter("operaName");
	String servicePath = (String)request.getParameter("servicePath");
	JSONArray items = new JSONArray();
	JSONObject item = new JSONObject();
	
	File path = Configuration.getWorkspaceFile(servicePath).getParentFile();
	File wsdlFile = new File(path, wsdlPath).getCanonicalFile();
	if (wsdlFile.exists()) {
		Definition defination = WSDLUtil.getWSDLReader().readWSDL(wsdlFile.toURI().toString());
		Map ptMap = defination.getAllPortTypes();
		if (ptMap != null) {
			for (Iterator itr = ptMap.entrySet().iterator(); itr.hasNext();) {
				Map.Entry entry = (Map.Entry)itr.next();
				PortType portType = (PortType)entry.getValue();
				List opList = portType.getOperations();
				if (opList != null && !opList.isEmpty())
				for (Iterator iter = opList.iterator(); iter.hasNext();) {
					Operation operation = (Operation)iter.next();
					if (operaName.equals(operation.getName())) {
						Map falutMap = operation.getFaults();
						if (falutMap != null && !falutMap.isEmpty()) {
							for (Iterator itror = falutMap.entrySet().iterator(); itror.hasNext();) {
								JSONObject ftJSO = new JSONObject();
								Map.Entry ftentry = (Map.Entry)itror.next();
								Fault fault = (Fault)ftentry.getValue();
								String ftName = fault.getMessage().getPart("parameters").getElementName().getLocalPart();
								ftJSO.put("faultname", ftName);
								items.put(ftJSO);
							}
						}
					}
				}
			}
		}
	}
	
	item.put("items", items);
	response.setContentType("text/json; charset=utf-8");
	return item.toString();
}

// 选择错误应答名称列表后生成错误应答模拟数据
@SuppressWarnings("unchecked")
public String  generateFaultEmulatorXml(HttpServletRequest request, HttpServletResponse response) throws Exception { 
	String schemaPath = (String)request.getParameter("schemaPath");
	String faultName = (String)request.getParameter("faultName");
	String servicePath = (String)request.getParameter("servicePath");
	String xml = "";
	
	String rootName = faultName;
	File path = Configuration.getWorkspaceFile(servicePath).getParentFile();
	SAXBuilder builder = new SAXBuilder();
	org.jdom.Document newdoc = builder.build(Configuration.getWorkspaceFile(servicePath));
	// 转换schema
	File schemaFile = new File(path, schemaPath).getCanonicalFile();
	if (schemaFile.exists()) {
		InputStream is = schemaFile.toURI().toURL().openStream();
		try {
			XmlSchemaCollection schemaCol = new XmlSchemaCollection();
			StreamSource source = new StreamSource(is);
			source.setSystemId(schemaFile.toURI().toString());
			XmlSchema schema = schemaCol.read(source, null);

			// request:操作名  response: 操作名 + respones 
			org.jdom.Element xmlEl = SchemaUtil.schema2Xml(schema, rootName,newdoc.getRootElement());
			org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
			format.setEncoding(response.getCharacterEncoding());
			format.setIndent("  ");
			org.jdom.output.XMLOutputter outter = new org.jdom.output.XMLOutputter(format);
			xml = outter.outputString(xmlEl);
		} catch(Exception e){
			xml = "";
		} finally {
			is.close();
		}
	}
	
	response.setContentType("text/plain; charset=utf-8");
	return xml;
}%>

<%
	String operation = request.getParameter("operation");
	
	WebServletResponse responseWrapper = new WebServletResponse(response);
	
	if (operation == null) operation = request.getMethod().toLowerCase();

	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[]{HttpServletRequest.class, HttpServletResponse.class});
		String result = (String)method.invoke(this, new Object[]{new WebServletRequest(request), responseWrapper});
		out.clear();
		out.println(result);
	}
	catch (NoSuchMethodException e) {
		throw new ServletException("[" + request.getMethod() + "]找不到相应的方法来处理指定的 operation: " + operation);
	}
	catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		throw new ServletException(t.getMessage());
	}
	catch (Exception e) {
		throw new ServletException(e.getMessage());
	}

%>