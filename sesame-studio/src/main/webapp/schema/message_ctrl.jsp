<%@page import="com.sanxing.sesame.test.UnitTest"%>
<%@page language="java" contentType="text/xml; charset=utf-8"%>
<%@page import="com.sanxing.studio.Configuration"%>
<%@page
	import="com.sanxing.studio.utils.*,com.sanxing.studio.team.svn.*,com.sanxing.studio.team.*,com.sanxing.studio.*"%>
<%@page import="java.io.*, java.util.*, java.util.regex.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.apache.ws.commons.schema.*"%>
<%@page import="org.apache.ws.commons.schema.utils.*"%>
<%@page import="org.dom4j.*, org.dom4j.io.*"%>
<%@page import="org.jaxen.*"%>
<%@page import="org.json.*"%>
<%@page import="org.xml.sax.InputSource"%>

<%!
private Logger logger = LoggerFactory.getLogger(this.getClass());
private File getWorkareaFile(String filePath) {
	return Configuration.getWorkspaceFile(filePath);
}

/**
* 获取 XML Schema 对象的描述信息
*/
private String getDocumentation(XmlSchemaDocumentation docu) {
	org.w3c.dom.NodeList  list = docu.getMarkup();
	if (list.getLength() > 0) {
		StringBuffer buf = new StringBuffer();
		for (int i=0, len=list.getLength(); i<len; i++) {
			org.w3c.dom.Node node = list.item(i);
			buf.append(node.getNodeValue()!=null ? node.getNodeValue() : "");
		}
		return buf.toString();
	} else {
		return "";
	}
}

@SuppressWarnings("unchecked")
private JSONObject getComplexParticle(XmlSchemaParticle particle) throws Exception {
	JSONObject result = new JSONObject();
	if (particle instanceof XmlSchemaSequence) {
		result.put("tag", "sequence");
		result.put("text", "序列");
	}
	else if (particle instanceof XmlSchemaChoice) {
		result.put("tag", "choice");
		result.put("text", "选择");
	}
	else if (particle instanceof XmlSchemaAll) {
		result.put("tag", "all");
		result.put("text", "全部");
	}
	result.put("allowDrag", false);
	result.put("expanded", true);
	JSONArray items = new JSONArray();
	result.put("children", items);
	XmlSchemaObjectCollection collection = ((XmlSchemaGroupBase)particle).getItems();
	for (Iterator<XmlSchemaElement> iter=collection.getIterator(); iter.hasNext(); ) {
		XmlSchemaElement element = iter.next();
		javax.xml.namespace.QName qname = element.getQName();

		JSONObject item = new JSONObject();
		item.put("tag", "element");
		item.put("name", qname.getLocalPart());
		item.put("allowDrag", false);
		String desc = "";
		if (element.getAnnotation() != null) {
			XmlSchemaObjectCollection annColl = element.getAnnotation().getItems();
			org.w3c.dom.Element formatEl = null;
			for (Iterator<?> it = annColl.getIterator(); it.hasNext(); ) {
				Object ann = it.next();
				if (ann instanceof XmlSchemaDocumentation) {
					desc = getDocumentation((XmlSchemaDocumentation)ann);
				}
			}
		}
		item.put("doc", desc);
		//如果Schema元素是复合类型，递归获取此元素的内容
		if (element.getSchemaType() instanceof XmlSchemaComplexType) {
			JSONArray children = new JSONArray();
			XmlSchemaComplexType complexType = (XmlSchemaComplexType)element.getSchemaType();
			children.put(this.getComplexParticle(complexType.getParticle()));
			item.put("children", children);
		}
		else {
			item.put("leaf", true);
		}
		items.put(item);		
	}
	
	return result;
}
/**
*获取schema中定义的类型， 结果放到 array 参数中
**/
@SuppressWarnings("unchecked")
private void getSchemaTypes(XmlSchema schema, String prefix, JSONArray array) throws JSONException {
	XmlSchemaObjectTable schemaTypes = schema.getSchemaTypes();
	for (Iterator<XmlSchemaType> iter = schemaTypes.getValues(); iter.hasNext(); ) {
		XmlSchemaType schemaType = iter.next();
		javax.xml.namespace.QName qname = schemaType.getQName();

		JSONObject item = new JSONObject();
		item.put("cls", "xsd-icon-"+(schemaType instanceof XmlSchemaSimpleType?"simpletype":"complextype"));
		item.put("type", qname.getLocalPart());
		if (prefix != null) {
			item.put("pre", prefix);
		}
		String desc = null;
		if (schemaType.getAnnotation() != null) {
			XmlSchemaObjectCollection annColl = schemaType.getAnnotation().getItems();
			org.w3c.dom.Element formatEl = null;
			for (Iterator<?> it = annColl.getIterator(); it.hasNext(); ) {
				Object ann = it.next();
				if (ann instanceof XmlSchemaDocumentation) {
					desc = getDocumentation((XmlSchemaDocumentation)ann);
				}
			}
		}
		item.put("desc", desc);
		array.put(item);
	}
}

//测试
public String getInputInterface(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String result = "";
	String intf = request.getParameter("intf");
	String schema = request.getParameter("schema");
	String operationName = schema.replaceAll(".*?\\/", "").replaceAll("\\.xsd$","");
	
	File unitFolder = Configuration.getWorkspaceFile(intf);
	UnitTest test = new UnitTest(unitFolder.getAbsolutePath(),operationName);
	result = test.generateXmlData();
	
	return result;
}

public String startTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String result = "";
	String xml = request.getParameter("xml");
	
	String intf = request.getParameter("intf");
	String schema = request.getParameter("schema");
	String operationName = schema.replaceAll(".*?\\/", "").replaceAll("\\.xsd$","");
	
	File unitFolder = Configuration.getWorkspaceFile(intf);
	UnitTest client = new UnitTest(unitFolder.getAbsolutePath(),operationName);
	result = client.sendRecv(xml);
	return result;
}

@SuppressWarnings("unchecked")
public String load(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String schemaPath = request.getParameter("schema");
	try {
		File schemaFile = Configuration.getWorkspaceFile(schemaPath);
		File unitFile = new File(schemaFile.getParentFile(), "public.wsdl");
		SAXReader reader = new SAXReader();
		Map map = new HashMap();
		map.put("wsdl", Namespaces.WSDL1_NAMESPACE);
		map.put("xs", Namespaces.XSD);
		reader.getDocumentFactory().setXPathNamespaceURIs(map);
		Document wsdl = reader.read(unitFile);
		Element definitionEl = wsdl.getRootElement();
		String operaName = schemaFile.getName().replaceFirst("\\.xsd$", "");
		String select = "wsdl:portType/wsdl:operation[@name='"+operaName+"']";
		org.dom4j.Element operaEl = (org.dom4j.Element)definitionEl.selectSingleNode(select);
		if (operaEl == null) return "<schema></schema>";
		
		
		Document document = reader.read(schemaFile);
		Element rootEl = document.getRootElement();
		//处理 import, 添加前缀
		List<?> list = rootEl.elements(new QName("import", rootEl.getNamespace()));
		reader.getDocumentFactory().getXPathNamespaceURIs();
		List<?> nsList = rootEl.additionalNamespaces();
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
			Element importEl=(Element)iter.next();
			for (Iterator<?> it=nsList.iterator(); it.hasNext(); ) {
				Namespace ns = (Namespace)it.next();
				if (ns.getURI().equals(importEl.attributeValue("namespace"))) {
					importEl.addAttribute("prefix", ns.getPrefix());
				}
			}
		}
		
		SimpleVariableContext variableContext = new SimpleVariableContext();
		org.dom4j.XPath partPath = reader.getDocumentFactory().createXPath("wsdl:message[@name=$name]/wsdl:part");
		partPath.setVariableContext(variableContext);
		org.dom4j.XPath elemPath = reader.getDocumentFactory().createXPath("xs:element[@name=$name]");
		elemPath.setVariableContext(variableContext);
		//处理全局元素，添加消息类别
		String targetNamespace = definitionEl.attributeValue("targetNamespace");
		list = operaEl.selectNodes("wsdl:*[@message]");
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
			Element channel = (Element)iter.next();
			
			String messageName = QName.get(channel.attributeValue("message", ""), targetNamespace).getName();
			variableContext.setVariableValue("name", messageName);
			Element partEl = (Element)partPath.selectSingleNode(definitionEl);
			if (partEl != null) {
				QName typeName = QName.get(partEl.attributeValue("element", ""), targetNamespace);
				variableContext.setVariableValue("name", typeName.getName());
				Element element = (Element)elemPath.selectSingleNode(rootEl);
				if (element != null) {
					element.addAttribute("channel", channel.getName());
				}
			}
		}
		//处理特殊字符
		list = document.selectNodes("//*/@separator|//*/@limit|//*/@blank");
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			String value = attr.getValue();
			String escaped = StringUtil.escape(value, false);
			attr.setValue(escaped);
		}
		
		return document.asXML();
	}
	catch (DocumentException e) {
		if (e.getNestedException() instanceof FileNotFoundException) {
			return "<schema></schema>";
		} else {
			throw e;
		}
	}
}
@SuppressWarnings("unchecked")
public String save(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String schemaPath = request.getParameter("schema");
	String data = request.getParameter("data");
	File schemaFile = Configuration.getWorkspaceFile(schemaPath);
	File unitFile = new File(schemaFile.getParentFile(), "public.wsdl");
	
	SAXReader reader = new SAXReader();
	Map map = new HashMap();
	map.put("wsdl", Namespaces.WSDL1_NAMESPACE);
	map.put("xs", Namespaces.XSD);
	reader.getDocumentFactory().setXPathNamespaceURIs(map);
	Document wsdl = reader.read(unitFile);
	Element definitionEl = wsdl.getRootElement();
	String targetNamespace = definitionEl.attributeValue("targetNamespace");

	String operaName = schemaFile.getName().replaceFirst("\\.xsd$", "");
	String select = "wsdl:portType/wsdl:operation[@name='"+operaName+"']";
	org.dom4j.Element operaEl = (org.dom4j.Element)definitionEl.selectSingleNode(select);
	if (operaEl == null) throw new Exception("操作不存在，可能已被删除");
	
	Document document = DocumentHelper.parseText(data); 
	Element rootEl = document.getRootElement();
	final Namespace xs=Namespace.get(Namespaces.XSD);
	rootEl.setQName(new QName(rootEl.getName(), xs));
	rootEl.addNamespace("tns", targetNamespace);
	rootEl.addAttribute("targetNamespace", targetNamespace);
	rootEl.accept(new VisitorSupport() {
		public void visit(Element el) {
			el.setQName(new QName(el.getName(), xs));
		}
	});
	//处理import前缀
	List<?> list = rootEl.elements(new QName("import", xs));
	for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
		Element el = (Element)iter.next();
		Attribute attr = el.attribute("prefix");
		String prefix = attr.getValue();
		el.remove(attr);
		rootEl.addNamespace(prefix, el.attributeValue("namespace"));
	}
	//处理全局元素，提取消息类别
	SimpleVariableContext variableContext = new SimpleVariableContext();
	org.dom4j.XPath msgPath = reader.getDocumentFactory().createXPath("wsdl:message[@name=$name]");
	msgPath.setVariableContext(variableContext);
	org.dom4j.XPath chnnPath = reader.getDocumentFactory().createXPath("wsdl:*[@message]");
	chnnPath.setVariableContext(variableContext);
	
	list = chnnPath.selectNodes(operaEl);
	for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
		Element el = (Element)iter.next();
		String messageName = QName.get(el.attributeValue("message"), targetNamespace).getName();
		variableContext.setVariableValue("name", messageName);
		Element messageEl = (Element)msgPath.selectSingleNode(definitionEl);
		if (messageEl != null) {
			definitionEl.remove(messageEl);
		}
		operaEl.remove(el);
	}
	list = rootEl.elements(new QName("element", xs));
	for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
		Element el = (Element)iter.next();
		Attribute attr = el.attribute("channel");
		String channel = attr.getValue();
		el.remove(attr);
		if (channel.length() == 0) continue;
		
		QName channelName = new QName(channel, definitionEl.getNamespace());
		Element channelEl = operaEl.addElement(channelName);
		channelEl.addAttribute("message", "tns:"+el.attributeValue("name"));
		if (channel.equals("fault")) {
			channelEl.addAttribute("name", el.attributeValue("name"));
		}
		String messageName = QName.get(channelEl.attributeValue("message"), targetNamespace).getName();
		variableContext.setVariableValue("name", messageName);
		Element messageEl = (Element)msgPath.selectSingleNode(definitionEl);
		if (messageEl == null) {
			messageEl = definitionEl.addElement(new QName("message", definitionEl.getNamespace()));
			messageEl.addAttribute("name", messageName);
		}
		else {
			messageEl.clearContent();
		}
		Element partEl = messageEl.addElement(new QName("part", definitionEl.getNamespace()));
		partEl.addAttribute("name", "parameters");
		partEl.addAttribute("element", "tns:"+el.attributeValue("name"));
	}
	//处理特殊字符
	list = document.selectNodes("//*/@separator|//*/@limit|//*/@blank");
	for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
		Attribute attr = (Attribute)iter.next();
		String value = attr.getValue();
		String normalized = StringUtil.normalize(value.toCharArray(), 0, value.length(), new char[value.length()]);
		attr.setValue(normalized);
	}
    
	OutputFormat format = OutputFormat.createPrettyPrint();
	format.setEncoding("utf-8");
	XMLWriter outputter = new XMLWriter(format);
	FileOutputStream outStream = new FileOutputStream(schemaFile);
	try {
		outputter.setOutputStream(outStream);
		outputter.write(document);
	}
	finally {
		outStream.close();
	}
	FileOutputStream output = new FileOutputStream(unitFile);
	try {
		outputter.setOutputStream(output);
		outputter.write(wsdl);
	}
	finally {
		output.close();
	}
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

@SuppressWarnings("unchecked")
public String getSchemaET(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String schemaLoc = request.getParameter("node");
	String schemaPath = request.getParameter("schema");
	File schemaFile = this.getWorkareaFile(schemaPath);
	File file = new File(schemaFile.getParent()+"/"+schemaLoc);

    JSONArray items = new JSONArray();
	
	XmlSchemaCollection schemaColl = new XmlSchemaCollection();
    XmlSchema schema = schemaColl.read(new InputSource(file.getAbsolutePath()), null);
    
	XmlSchemaObjectTable elements = schema.getElements();
	for (Iterator<XmlSchemaElement> iter = elements.getValues(); iter.hasNext(); ) {
		XmlSchemaElement element = iter.next();
		javax.xml.namespace.QName qname = element.getQName();

		JSONObject item = new JSONObject();
		item.put("tag", "element");
		item.put("name", qname.getLocalPart());
		String desc = "";
		if (element.getAnnotation() != null) {
			XmlSchemaObjectCollection annColl = element.getAnnotation().getItems();
			org.w3c.dom.Element formatEl = null;
			for (Iterator<?> it = annColl.getIterator(); it.hasNext(); ) {
				Object ann = it.next();
				if (ann instanceof XmlSchemaDocumentation) {
					desc = getDocumentation((XmlSchemaDocumentation)ann);
				}
			}
		}
		
		item.put("doc", desc);
		item.put("leaf", element.getSchemaType() instanceof XmlSchemaSimpleType);
		items.put(item);
	}
	XmlSchemaObjectTable schemaTypes = schema.getSchemaTypes();
	for (Iterator<XmlSchemaType> iter = schemaTypes.getValues(); iter.hasNext(); ) {
		XmlSchemaType schemaType = iter.next();
		javax.xml.namespace.QName qname = schemaType.getQName();

		JSONObject item = new JSONObject();
		item.put("tag", (schemaType instanceof XmlSchemaSimpleType?"simpleType":"complexType"));
		item.put("name", qname.getLocalPart());
		String desc = "";
		if (schemaType.getAnnotation() != null) {
			XmlSchemaObjectCollection annColl = schemaType.getAnnotation().getItems();
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
		item.put("doc", desc);
		item.put("leaf", schemaType instanceof XmlSchemaSimpleType);
		items.put(item);
	}
	
	response.setContentType("text/json; charset=utf-8");
	return items.toString();
}

//获取Schema元素或者类型的结构
@SuppressWarnings("unchecked")
public String getStructure(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String schemaLoc = request.getParameter("path");
	String schemaPath = request.getParameter("schema");
	String tag = request.getParameter("tag");
	String name = request.getParameter("name");
	File schemaFile = this.getWorkareaFile(schemaPath);
	File file = new File(schemaFile.getParent()+"/"+schemaLoc);

    JSONArray items = new JSONArray();
	
	XmlSchemaCollection schemaColl = new XmlSchemaCollection();
    XmlSchema schema = schemaColl.read(new InputSource(file.getAbsolutePath()), null);
    
    if (tag == null) {
    	
    }
    else if (tag.equals("element")) {
		XmlSchemaObjectTable elements = schema.getElements();
		XmlSchemaElement globalEl = schema.getElementByName(name);
		
		if (globalEl.getSchemaType() instanceof XmlSchemaComplexType) {
			XmlSchemaComplexType complexType = (XmlSchemaComplexType)globalEl.getSchemaType();
			items.put(this.getComplexParticle(complexType.getParticle()));
		}
    } else {
		XmlSchemaObjectTable schemaTypes = schema.getSchemaTypes();
		for (Iterator<XmlSchemaType> iter = schemaTypes.getValues(); iter.hasNext(); ) {
			XmlSchemaType schemaType = iter.next();
			javax.xml.namespace.QName qname = schemaType.getQName();
	
			JSONObject item = new JSONObject();
			item.put("tag", (schemaType instanceof XmlSchemaSimpleType?"simpleType":"complexType"));
			item.put("name", qname.getLocalPart());
			String desc = "";
			if (schemaType.getAnnotation() != null) {
				XmlSchemaObjectCollection annColl = schemaType.getAnnotation().getItems();
				org.w3c.dom.Element formatEl = null;
				for (Iterator<?> it = annColl.getIterator(); it.hasNext(); ) {
					Object ann = it.next();
					if (ann instanceof XmlSchemaDocumentation) {
						desc = getDocumentation((XmlSchemaDocumentation)ann);
					}
				}
			}
			item.put("doc", desc);
			item.put("leaf", true);
			items.put(item);
		}
    }
	response.setContentType("text/json; charset=utf-8");
	return items.toString();
}

public String loadSchemaTypes(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String schemaPath = request.getParameter("schema");
	File schemaFile = this.getWorkareaFile(schemaPath);
	String xmlns = request.getParameter("xmlns");
	String location = request.getParameter("location");

	XmlSchemaCollection schemaColl = new XmlSchemaCollection();

	JSONArray items = new JSONArray();
	XmlSchema[] schemas = schemaColl.getXmlSchemas();
	for (XmlSchema schema : schemas) {
		getSchemaTypes(schema, null, items);
	}
	
	JSONObject loc = new JSONObject(location!=null ? location: "{}");
	for (Iterator<?> keys=loc.keys(); keys.hasNext(); ) {
		String pref = (String)keys.next();
		String path = loc.getString(pref);
        	
		File file = new File(path);
        if (!file.isAbsolute()) {
			file = new File(schemaFile.getParent()+"/"+path);
		}
        if (!file.exists()) {
			break;
        }
        XmlSchema schema = schemaColl.read(new InputSource(file.getAbsolutePath()), null);
		getSchemaTypes(schema, pref, items);
	}
	
	JSONObject result = new JSONObject();
	result.put("items", items);
	response.setContentType("text/json; charset=utf-8");
	return result.toString();
}

public String listFiles(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String path = request.getParameter("path");
	final String regex = request.getParameter("filter");
	Pattern pattern = regex!=null && regex.length()>0 ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE) : null;
	JSONArray items = new JSONArray();
	File folder = this.getWorkareaFile(path);
	File[] list = folder.listFiles();
	if (list != null) {
		SAXReader builder = new SAXReader();
		for (int i=0,len=list.length; i<len; i++)
		{
			File file = list[i];
			if (file.isFile() && pattern!=null && !pattern.matcher(file.getName()).matches()) {
				continue;
			}
			JSONObject item = new JSONObject();
			item.put("cls", file.isDirectory()?"x-dialog-folder":"x-dialog-file");
			item.put("name", file.getName());
			if (file.isFile()) {
				Document document = builder.read(file);
				Element rootEl = document.getRootElement();
				item.put("content", rootEl.attributeValue("targetNamespace"));
			}
        	items.put(item);
        }
	}
	JSONObject data = new JSONObject();
	data.put("items", items);
	response.setContentType("text/json; charset=utf-8");
	return data.toString();
}

%>

<%
String operation = request.getParameter("operation");
if (operation != null) {
	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[]{HttpServletRequest.class, HttpServletResponse.class});
		String result = (String)method.invoke(this, new Object[]{new WebServletRequest(request), response});
		out.clear();
		out.println(result);
	} catch (NoSuchMethodException e) {
		WebUtil.sendError(response, "["+request.getMethod()+"]找不到相应的方法来处理指定的 operation: "+operation);
	} catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		logger.error(t.getMessage(), t);
		WebUtil.sendError(response, t.getMessage());
	} catch (Exception e) {
		logger.error(e.getMessage(), e);
		WebUtil.sendError(response, e.getMessage());
	}
} else {
	WebUtil.sendError(response, "["+request.getMethod()+"]拒绝执行，没有指定 operation 参数");
}

%>