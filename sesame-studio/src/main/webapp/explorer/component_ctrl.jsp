<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>

<%@page import="org.jdom.xpath.XPath"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.sesame.transport.Protocols"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="java.util.jar.*"%>
<%@page import="java.util.zip.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.jdom.*, org.jdom.input.*, org.jdom.output.*"%>
<%@page import="org.json.*"%>

<%!
private static final String BINDING_FILE = "binding.xml";
private static final String FUNCTIONS_FILE = "xpath.ext";
private static final String TRANSFORM_FILE = "transform.ext";
private static final String BEANSHELL_FILE = "beanshell.ext";
private static final String STATUS_FILE = "status.xml";
private static final String PROPERTY_FILE = ".properties";


private void setChildText(Element parentEl, String childName, String text) {
	Element childEl = parentEl.getChild(childName, parentEl.getNamespace());
	if (childEl == null) {
		childEl = new Element(childName, parentEl.getNamespace());
		parentEl.addContent(childEl);
	}
	childEl.setText(text);
}
//获取指定元素的备注信息
private String getComment(Element element) {
	for (Iterator<?> iter = element.getContent().iterator(); iter.hasNext();) {
		Content content = (Content) iter.next();
		if (content instanceof Comment)
			return ((Comment) content).getText();
	}
	return null;
}

/**
*检查是否能够注册协议
*/
@SuppressWarnings("unchecked")
public String canRegister(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	response.setContentType("text/json; charset=utf-8");

	String component = request.getParameter("component");
	String name = request.getParameter("protocol");
	String[] schemes = Protocols.list();
	for (String scheme : schemes) {
		if (scheme.equals(name))
			return "false";
	}
	return "true";
}
//
public String getShareLibs(HttpServletRequest request, HttpServletResponse response) throws Exception {
	JSONArray items = new JSONArray();
	File libFolder = Application.getWarehouseFile("lib");

	File[] sharelibs = libFolder.listFiles();
	SAXBuilder builder = new SAXBuilder();
	Document document;
	for (File compEntry : sharelibs) {
		if (compEntry.isDirectory()) {
			if (compEntry.getName().startsWith(".")) continue;
			document = builder.build(new File(compEntry, "jbi.xml"));
		}
		else {
			JarFile compJar = new JarFile(compEntry);
			try {
				JarEntry entry = compJar.getJarEntry("jbi.xml");
				if (entry != null) {
					InputStream input = compJar.getInputStream(entry);
					document = builder.build(input);
				}
				else {
					document = new Document(new Element("jbi"));
				}
			}
			finally {
				compJar.close();
			}
		}
		Element rootEl = document.getRootElement();
		List list = rootEl.getChildren();
		if (list.size() > 0) {
			Element idenEl, firstEl = (Element)list.get(0);
			
			if ((idenEl = firstEl.getChild("identification", rootEl.getNamespace())) != null) {
				String name = idenEl.getChildText("name", rootEl.getNamespace());
				String description = idenEl.getChildText("description", rootEl.getNamespace());
				if (name != null) {
				JSONObject json = new JSONObject();
				json.put("description", description);
				json.put("name", name);
				json.put("version", idenEl.getAttribute("version"));
				items.put(json);
				}
			}
		}
	}
	
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("items", items);
	result.put("success", true);
	return String.valueOf(result);
}

/**
*获取XSLT扩展函数列表
*/
@SuppressWarnings("unchecked")
public String getConvExtension(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");

	JSONArray items = new JSONArray();
	File compBundle = Application.getWarehouseFile(component);
	if (!compBundle.exists()) {
		throw new Exception("找不到指定的组件包: "+compBundle.getCanonicalPath());
	}

	try {
		SAXBuilder builder = new SAXBuilder();
		Document document;
		if (compBundle.isDirectory()) {
			document = builder.build(new File(compBundle, TRANSFORM_FILE));
		}
		else {
			JarFile compJar = new JarFile(compBundle);
			try {
				JarEntry entry = compJar.getJarEntry(TRANSFORM_FILE);
				if (entry != null) {
					InputStream input = compJar.getInputStream(entry);
					document = builder.build(input);
				}
				else {
					document = new Document(new Element("extensions"));
				}
			}
			finally {
				compJar.close();
			}
		}
		Element rootEl = document.getRootElement();
		List<Element> list = rootEl.getChildren("class", rootEl.getNamespace());
		for (Element classEl : list) {
			JSONObject json = new JSONObject();
			json.put("id", classEl.getAttributeValue("id"));
			json.put("prefix", classEl.getAttributeValue("prefix"));
			json.put("class-name", classEl.getAttributeValue("class-name"));
			json.put("description", classEl.getAttributeValue("description"));
			json.put("collapsed", classEl.getAttributeValue("collapsed", "false").equals("true"));
			items.put(json);
			JSONArray array = new JSONArray();
			json.put("items", array);
			List<Element> funcs = classEl.getChildren("function", rootEl.getNamespace());
			for (Element funcEl : funcs) {
				JSONObject funcObj = new JSONObject();
				funcObj.put("func-name", funcEl.getAttributeValue("name"));
				funcObj.put("description", funcEl.getAttributeValue("description"));
				array.put(funcObj);
			}
		}
	}
	catch (FileNotFoundException e) {
		//dummy
	}
	
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("categories", items);
	result.put("success", true);
	return String.valueOf(result);
}

public String getComponents(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	String type = request.getParameter("type");

	JSONArray items = new JSONArray();
	File compBundle = Application.getWarehouseFile(component);
	if (!compBundle.exists()) {
		throw new Exception("找不到指定的组件包: "+compBundle.getCanonicalPath());
	}

	File[] components = compBundle.getParentFile().listFiles();
	SAXBuilder builder = new SAXBuilder();
	Document document;
	for (File compEntry : components) {
		if (compEntry.getName().startsWith(".")) {
			continue;
		}
		
		if (compEntry.isDirectory()) {
			document = builder.build(new File(compEntry, "jbi.xml"));
		}
		else {
			JarFile compJar = new JarFile(compEntry);
			try {
				JarEntry entry = compJar.getJarEntry("jbi.xml");
				if (entry != null) {
					InputStream input = compJar.getInputStream(entry);
					document = builder.build(input);
				}
				else {
					document = new Document(new Element("jbi"));
				}
			}
			finally {
				compJar.close();
			}
		}
		Element rootEl = document.getRootElement();
		List<?> list = rootEl.getChildren();
		if (list.size() > 0) {
			Element idenEl, firstEl = (Element)list.get(0);
			if (type!=null && !firstEl.getAttributeValue("type", "").equals(type)) {
				continue;
			}
			if ((idenEl = firstEl.getChild("identification", rootEl.getNamespace())) != null) {
				String name = idenEl.getChildText("name", rootEl.getNamespace());
				String description = idenEl.getChildText("description", rootEl.getNamespace());
				if (name != null) {
				JSONObject json = new JSONObject();
				json.put("description", description);
				json.put("name", name);
				items.put(json);
				}
			}
		}
	}

	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("items", items);
	result.put("success", true);
	return String.valueOf(result);
}

@SuppressWarnings("unchecked")
public String loadErrorMap(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	String topic = request.getParameter("topic");

	JSONArray items = new JSONArray();
	File compBundle = Application.getWarehouseFile(component);
	if (!compBundle.exists()) {
		throw new Exception("找不到指定的组件包: "+compBundle.getCanonicalPath());
	}

	try {
		Namespace xmlns_xsl = Namespace.getNamespace("xsl", Namespaces.XSL);
		SAXBuilder builder = new SAXBuilder();
		Document document;
		if (compBundle.isDirectory()) {
			document = builder.build(new File(compBundle, topic+".fault"));
		}
		else {
			JarFile compJar = new JarFile(compBundle);
			try {
				JarEntry entry = compJar.getJarEntry(topic+".fault");
				if (entry != null) {
					InputStream input = compJar.getInputStream(entry);
					document = builder.build(input);
				}
				else {
					document = new Document(new Element("transform"));
				}
			}
			finally {
				compJar.close();
			}
		}
		Element rootEl = document.getRootElement();
		List<Element> list = rootEl.getChildren("template", rootEl.getNamespace());
		for (Element templateEl : list) {
			String match = templateEl.getAttributeValue("match");
			List<Element> cases = templateEl.getChildren("when", rootEl.getNamespace());
			for (Element whenEl : cases) {
				JSONObject json = new JSONObject();
				json.put("fault-spot", match);
				json.put("status", whenEl.getAttributeValue("status"));
				//json.put("status-text", getComment(whenEl));
				json.put("result", whenEl.getAttributeValue("fault-code"));
				json.put("description", whenEl.getAttributeValue("fault-reason"));
				items.put(json);
			}
		}
	}
	catch (FileNotFoundException e) {
		//dummy
	}

	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("items", items);
	result.put("success", true);
	return String.valueOf(result);
}

/**
*获取XPath扩展函数列表
*/
@SuppressWarnings("unchecked")
public String loadFunctions(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");

	JSONArray items = new JSONArray();
	File compBundle = Application.getWarehouseFile(component);
	if (!compBundle.exists()) {
		throw new Exception("找不到指定的组件包: "+compBundle.getCanonicalPath());
	}

	try {
		SAXBuilder builder = new SAXBuilder();
		Document document;
		if (compBundle.isDirectory()) {
			document = builder.build(new File(compBundle, FUNCTIONS_FILE));
		}
		else {
			JarFile compJar = new JarFile(compBundle);
			try {
				JarEntry entry = compJar.getJarEntry(FUNCTIONS_FILE);
				if (entry != null) {
					InputStream input = compJar.getInputStream(entry);
					document = builder.build(input);
				}
				else {
					document = new Document(new Element("functions"));
				}
			}
			finally {
				compJar.close();
			}
		}
		Element rootEl = document.getRootElement();
		List<Element> list = rootEl.getChildren("function", rootEl.getNamespace());
		for (Element el : list) {
			JSONObject json = new JSONObject();
			json.put("prefix", el.getAttributeValue("prefix"));
			json.put("func-name", el.getAttributeValue("name"));
			json.put("class-name", el.getAttributeValue("class-name"));
			json.put("description", el.getAttributeValue("description"));
			items.put(json);
		}
	}
	catch (FileNotFoundException e) {
		//dummy
	}
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("items", items);
	result.put("success", true);
	return String.valueOf(result);
}

/**
*载入组件参数
*/
@SuppressWarnings("unchecked")
public String loadParams(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");

	JSONObject result = new JSONObject();
	File compBundle = Application.getWarehouseFile(component);
	if (!compBundle.exists()) {
		throw new Exception("找不到指定的组件包: "+compBundle.getCanonicalPath());
	}

	try {
		Properties properties = new Properties();
		if (compBundle.isDirectory()) {
			InputStream input = new FileInputStream(new File(compBundle, PROPERTY_FILE));
			try {
				properties.load(input);
			}
			finally {
				input.close();
			}
		}
		else {
			JarFile compJar = new JarFile(compBundle);
			try {
				JarEntry entry = compJar.getJarEntry(TRANSFORM_FILE);
				if (entry != null) {
					InputStream input = compJar.getInputStream(entry);
					properties.load(input);
				}
			}
			finally {
				compJar.close();
			}
		}
		for (Object key : properties.keySet()) {
			result.put((String)key, properties.get(key));
		}
	}
	catch (FileNotFoundException e) {
		//do nothing
	}
	
	response.setContentType("text/json; charset=utf-8");
	return String.valueOf(result);
}

/**
*获取状态代码列表
*/
@SuppressWarnings("unchecked")
public String loadStatus(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");

	JSONArray items = new JSONArray();
	Namespace artNS = Namespace.getNamespace("sn", Namespaces.SESAME);
	File compBundle = Application.getWarehouseFile(component);
	if (!compBundle.exists()) {
		throw new Exception("找不到指定的组件包: "+compBundle.getCanonicalPath());
	}

	try {
		SAXBuilder builder = new SAXBuilder();
		Document document;
		if (compBundle.isDirectory()) {
			document = builder.build(new File(compBundle, STATUS_FILE));
		}
		else {
			JarFile compJar = new JarFile(compBundle);
			try {
				JarEntry entry = compJar.getJarEntry(STATUS_FILE);
				if (entry != null) {
					InputStream input = compJar.getInputStream(entry);
					document = builder.build(input);
				}
				else {
					document = new Document(new Element("status-list"));
				}
			}
			finally {
				compJar.close();
			}
		}
		Element rootEl = document.getRootElement();
		List<Element> list = rootEl.getChildren("status", rootEl.getNamespace());
		for (Element el : list) {
			JSONObject json = new JSONObject();
			json.put("status", el.getAttributeValue("code"));
			json.put("text", el.getAttributeValue("text"));
			items.put(json);
		}
	}
	catch (FileNotFoundException e) {
		//dummy
	}
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("items", items);
	result.put("success", true);
	return String.valueOf(result);
}

/**
*获取指定组件所允许使用的通讯协议列表
**/
public String loadAllowable(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	File compBundle = Application.getWarehouseFile(component);

	JSONObject properties = new JSONObject();

	Element bindingEl;
	SAXBuilder builder = new SAXBuilder();
	if (compBundle.isDirectory()) {
		File bindingXml = new File(compBundle, BINDING_FILE);
		if (bindingXml.exists()) {
			bindingEl = builder.build(bindingXml).getRootElement();
		}
		else {
			bindingEl = new Element("binding");
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
			else {
				bindingEl = new Element("binding");
			}
		}
		finally {
			compJar.close();
		}
	}
	
	Namespace xmlns_art = bindingEl.getNamespace();
	JSONArray transports = new JSONArray();
	properties.put("schemes", transports);
	Element allowableEl = bindingEl.getChild("allowable", xmlns_art);
	if (allowableEl != null) {
		List<?> list = allowableEl.getChildren("protocol", xmlns_art);
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ){
			Element transportEl = (Element)iter.next();
			JSONArray json = new JSONArray();
			json.put(transportEl.getAttributeValue("name"));
			json.put(transportEl.getAttributeValue("impl", ""));
			json.put(transportEl.getTextTrim());
			transports.put(json);
		}
	}

	response.setContentType("text/json; charset=utf-8");
	return properties.toString();
}

/**
*装载编码解码设置
**/
public String loadCodec(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	File compBundle = Application.getWarehouseFile(component);
	
	response.setContentType("text/json; charset=utf-8");
	JSONObject properties = new JSONObject();
	if (!compBundle.exists()) {
		return properties.toString();
	}
	
	Element bindingEl;
	SAXBuilder builder = new SAXBuilder();
	if (compBundle.isDirectory()) {
		File bindingXml = new File(compBundle, BINDING_FILE);
		bindingEl = builder.build(bindingXml).getRootElement();
	}
	else {
		JarFile compJar = new JarFile(compBundle);
		try {
			//获取部署描述符
			JarEntry bindingXml = compJar.getJarEntry(BINDING_FILE);
			if (bindingXml != null) {
				InputStream input = compJar.getInputStream(bindingXml);
				bindingEl = builder.build(input).getRootElement();
			}
			else {
				bindingEl = new Element("binding");
			}
		}
		finally {
			compJar.close();
		}
	}
	Namespace xmlns_art = bindingEl.getNamespace();

	Element codecEl = bindingEl.getChild("codec", xmlns_art);
	if (codecEl != null) {
		JSONObject codecObj = new JSONObject();
		Element paramsEl = codecEl.getChild("params", xmlns_art);
		if (paramsEl != null) {
			JSONObject paramsObj = new JSONObject();
			List<?> list = paramsEl.getChildren();
			for (Iterator<?> iter=list.iterator(); iter.hasNext(); ){
				Element paramEl = (Element)iter.next();
				paramsObj.put(paramEl.getName(), paramEl.getTextTrim());
			}
			codecObj.put("params", paramsObj);
		}
		
		Element r2xEl = codecEl.getChild("raw2xml", xmlns_art);
		if (r2xEl != null) {
			JSONObject r2xObj = new JSONObject();
			r2xObj.put("decoder", r2xEl.getChildText("decoder", xmlns_art));
			Element handlersEl = r2xEl.getChild("raw-handlers", xmlns_art);
			if (handlersEl != null) {
				List<?> list = handlersEl.getChildren("handler", xmlns_art);
				for (Iterator<?> iter=list.iterator(); iter.hasNext(); ){
					Element handlerEl = (Element)iter.next();
					r2xObj.append("raw-handlers", handlerEl.getTextTrim());
				}
			}
			handlersEl = r2xEl.getChild("xml-handlers", xmlns_art);
			if (handlersEl != null) {
				List<?> list = handlersEl.getChildren("handler", xmlns_art);
				for (Iterator<?> iter=list.iterator(); iter.hasNext(); ){
					Element handlerEl = (Element)iter.next();
					r2xObj.append("xml-handlers", handlerEl.getTextTrim());
				}
			}
			codecObj.put("raw2xml", r2xObj);
		}
		Element x2rEl = codecEl.getChild("xml2raw", xmlns_art);
		if (x2rEl != null) {
			JSONObject x2rObj = new JSONObject();
			x2rObj.put("encoder", x2rEl.getChildText("encoder", xmlns_art));
			Element handlersEl = x2rEl.getChild("raw-handlers", xmlns_art);
			if (handlersEl != null) {
				List<?> list = handlersEl.getChildren("handler", xmlns_art);
				for (Iterator<?> iter=list.iterator(); iter.hasNext(); ){
					Element handlerEl = (Element)iter.next();
					x2rObj.append("raw-handlers", handlerEl.getTextTrim());
				}
			}
			handlersEl = x2rEl.getChild("xml-handlers", xmlns_art);
			if (handlersEl != null) {
				List<?> list = handlersEl.getChildren("handler", xmlns_art);
				for (Iterator<?> iter=list.iterator(); iter.hasNext(); ){
					Element handlerEl = (Element)iter.next();
					x2rObj.append("xml-handlers", handlerEl.getTextTrim());
				}
			}
			codecObj.put("xml2raw", x2rObj);
		}
		
		Element faultEl = codecEl.getChild("fault-handler", xmlns_art);
		if (faultEl != null) {
			codecObj.put("fault-handler", faultEl.getTextTrim());
		}
		
		properties.put("codec", codecObj);
	}
	
	return properties.toString();
}


//删除一组自定义转换函数(XSLT)
public String removeConvertion(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	String groupId = request.getParameter("groupId");

	File compBundle = Application.getWarehouseFile(component);
	if (compBundle.isDirectory()) {
		File xsltExt = new File(compBundle, TRANSFORM_FILE);
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(xsltExt);
		Element rootEl = document.getRootElement();
		
		String path = groupId==null || groupId.length()==0 ? "class[not(@id)]" : "class[@id='"+groupId+"']";
		Element groupEl = (Element)XPath.selectSingleNode(rootEl, path);
		if (groupEl != null) {
			groupEl.detach();
		}

		OutputStream output = new FileOutputStream(xsltExt);
		try {
			JdomUtil.getPrettyOutputter("utf-8").output(document, output);
		}
		finally {
			output.close();
		}
	}
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("success", true);
	return String.valueOf(result);
}

public String removeMapping(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	String topic = request.getParameter("topic");

	File compBundle = Application.getWarehouseFile(component);
	if (compBundle.isDirectory()) {
		File topicFile = new File(compBundle, topic+".fault");
		if (topicFile.exists()) {
			topicFile.delete();
		}
	}
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("success", true);
	return String.valueOf(result);
}

//保存组件基本属性
public String save(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("data");
	String component = request.getParameter("component");
	JSONObject properties = new JSONObject(data);
	File compEntry = Application.getWarehouseFile(component);
	if (compEntry.isDirectory()) {
		File jbiXml = new File(compEntry, "jbi.xml");
		SAXBuilder builder = new SAXBuilder();
		Element rootEl = builder.build(jbiXml).getRootElement();
		
		Element compEl = rootEl.getChild("component", rootEl.getNamespace());
		Element identEl = compEl.getChild("identification", rootEl.getNamespace());
		
		setChildText(identEl, "name", compEntry.getName());
		setChildText(identEl, "description", properties.getString("description"));
		
		setChildText(compEl, "component-class-name", properties.getString("component-classname"));
		Element compClassPathEl = compEl.getChild("component-class-path", rootEl.getNamespace());
		compClassPathEl.removeContent();
		JSONArray compCP = properties.getJSONArray("component-classpath");
		for (int i=0,len=compCP.length(); i<len; i++) {
			Element pathEl = new Element("path-element", rootEl.getNamespace());
			pathEl.setText(compCP.getString(i));
			compClassPathEl.addContent(pathEl);
		}
		
		setChildText(compEl, "bootstrap-class-name", properties.getString("bootstrap-classname"));
		Element bootClassPathEl = compEl.getChild("bootstrap-class-path", rootEl.getNamespace());
		bootClassPathEl.removeContent();
		JSONArray bootCP = properties.getJSONArray("bootstrap-classpath");
		for (int i=0,len=bootCP.length(); i<len; i++) {
			Element pathEl = new Element("path-element", rootEl.getNamespace());
			pathEl.setText(bootCP.getString(i));
			bootClassPathEl.addContent(pathEl);
		}
		compEl.removeChildren("shared-library", rootEl.getNamespace());
		JSONArray sharedLibs = properties.optJSONArray("shared-library");
		if (sharedLibs != null) {
			for (int i=0,len=sharedLibs.length(); i<len; i++) {
				Element libEl = new Element("shared-library", rootEl.getNamespace());
				JSONArray array = sharedLibs.getJSONArray(i);
				libEl.setText(array.getString(0));
				libEl.setAttribute("version", array.getString(1));
				compEl.addContent(libEl);
			}
		}

		OutputStream output = new FileOutputStream(jbiXml);
		try {
			JdomUtil.getPrettyOutputter("utf-8").output(rootEl.getDocument(), output);
		}
		finally {
			output.close();
		}
	}

	return "true";
}

//保存组件绑定信息
public String saveBinding(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	String data = request.getParameter("data");
	JSONObject properties = new JSONObject(data);
	File compEntry = Application.getWarehouseFile(component);
	if (compEntry.isDirectory()) {
		File bindingXml = new File(compEntry, BINDING_FILE);
		SAXBuilder builder = new SAXBuilder();
		Element bindingEl;
		if (bindingXml.exists()) {
			bindingEl = builder.build(bindingXml).getRootElement();
		}
		else {
			bindingEl = new Element("binding");
			new Document(bindingEl);
		}
		Namespace xml_art = Namespace.NO_NAMESPACE;
		if (properties.has("protocols")) {
			Element allowableEl = bindingEl.getChild("allowable", xml_art);
			if (allowableEl == null) {
				bindingEl.addContent(allowableEl=new Element("allowable", xml_art));
			}
			else {
				allowableEl.removeContent();
			}
			int index = 0;
			//处理transport
			JSONArray protocols = properties.getJSONArray("protocols");
			for(int i = 0; i<protocols.length(); i++){
				JSONArray array = protocols.getJSONArray(i);
				Element protocol = new Element("protocol", xml_art);
				protocol.setAttribute("name", array.getString(0));
				if (array.getString(1).length() > 0) {
					protocol.setAttribute("impl", array.getString(1));
				}
				protocol.setText(array.getString(2));
				allowableEl.addContent(index++, protocol);
			}
		}
		//处理编码解码
		if (properties.has("codec")) {
			JSONObject codec = properties.getJSONObject("codec");
			bindingEl.removeChildren("codec", xml_art);
			Element codecEl = new Element("codec", xml_art);
			bindingEl.addContent(codecEl);
			
			JSONObject paramsObj = codec.getJSONObject("params");
			Element paramsEl = new Element("params", xml_art);
			codecEl.addContent(paramsEl);
			for (Iterator<?> iter=paramsObj.keys(); iter.hasNext(); ) {
				String key = (String)iter.next();
				setChildText(paramsEl, key, paramsObj.getString(key));
			}

			if (codec.has("raw2xml")) {
				JSONObject raw2xml = codec.getJSONObject("raw2xml");
				Element r2xEl = new Element("raw2xml", xml_art);
				codecEl.addContent(r2xEl);
				JSONArray rawHandlers = raw2xml.getJSONArray("raw-handlers");
				Element rHandlers = new Element("raw-handlers", xml_art);
				r2xEl.addContent(rHandlers);
				for (int i=0, len=rawHandlers.length(); i<len; i++) {
					rHandlers.addContent(new Element("handler", xml_art).setText(rawHandlers.getString(i)));
				}
				setChildText(r2xEl, "decoder", raw2xml.getString("decoder"));
				JSONArray xmlHandlers = raw2xml.getJSONArray("xml-handlers");
				Element xHandlers = new Element("xml-handlers", xml_art);
				r2xEl.addContent(xHandlers);
				for (int i=0, len=xmlHandlers.length(); i<len; i++) {
					xHandlers.addContent(new Element("handler", xml_art).setText(xmlHandlers.getString(i)));
				}
			}
	
			if (codec.has("xml2raw")) {
				JSONObject xml2raw = codec.getJSONObject("xml2raw");
				Element x2rEl = new Element("xml2raw", xml_art);
				codecEl.addContent(x2rEl);
				JSONArray xmlHandlers = xml2raw.getJSONArray("xml-handlers");
				Element xHandlers = new Element("xml-handlers", xml_art);
				x2rEl.addContent(xHandlers);
				for (int i=0, len=xmlHandlers.length(); i<len; i++) {
					xHandlers.addContent(new Element("handler", xml_art).setText(xmlHandlers.getString(i)));
				}
				setChildText(x2rEl, "encoder", xml2raw.getString("encoder"));
				Element rHandlers = new Element("raw-handlers", xml_art);
				x2rEl.addContent(rHandlers);
				JSONArray rawHandlers = xml2raw.getJSONArray("raw-handlers");
				for (int i=0, len=rawHandlers.length(); i<len; i++) {
					rHandlers.addContent(new Element("handler", xml_art).setText(rawHandlers.getString(i)));
				}
			}
			
			if (codec.has("fault-handler")) {
				String value = codec.getString("fault-handler");
				Element faultEl = new Element("fault-handler", xml_art);
				codecEl.addContent(faultEl);
				faultEl.setText(value);
			}
		}
		
		//System.out.println( JdomUtil.getPrettyOutputter("utf-8").outputString(rootEl) );
		OutputStream output = new FileOutputStream(bindingXml);
		try {
			JdomUtil.getPrettyOutputter("utf-8").output(bindingEl.getDocument(), output);
		}
		finally {
			output.close();
		}
	}

	response.setContentType("text/plain; charset=utf-8");
	return "true";
}

//保存组件的自定义转换函数(XSLT)
public String saveConvertion(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	String data = request.getParameter("data");

	JSONObject json = new JSONObject(data);
	File compBundle = Application.getWarehouseFile(component);
	if (compBundle.isDirectory()) {
		File xsltExt = new File(compBundle, TRANSFORM_FILE);
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(xsltExt);
		Element rootEl = document.getRootElement();
		XPath xpath = XPath.newInstance("class[@id=$id]");
		JSONArray categories = json.getJSONArray("categories");
		for (int i=0, len=categories.length(); i<len; i++) {
			JSONObject category = categories.getJSONObject(i);
			Element groupEl = null;  
			String groupId;
			if (category.has("id")) {
				groupId = category.getString("id");
				xpath.setVariable("id", groupId);
				groupEl = (Element)xpath.selectSingleNode(rootEl);
			}
			else {
				groupId = Thread.currentThread().getId() + "-" + System.currentTimeMillis();
			}
			if (groupEl == null) {
				groupEl = new Element("class");
				groupEl.setAttribute("id", groupId);
				rootEl.addContent(groupEl);
			}
			else {
				groupEl.removeContent();
			}
			groupEl.setAttribute("collapsed", category.optString("collapsed", "false"));
			groupEl.setAttribute("prefix", category.getString("prefix"));
			groupEl.setAttribute("class-name", category.getString("class-name"));
			groupEl.setAttribute("description", category.optString("description"));
			
			JSONArray items = category.getJSONArray("items");
			for (int j=0, l=items.length(); j<l; j++) {
				JSONObject func = items.getJSONObject(j);
				Element el = new Element("function");
				el.setAttribute("name", func.getString("func-name"));
				el.setAttribute("description", func.optString("description"));
				groupEl.addContent(el);
			}
		}

		OutputStream output = new FileOutputStream(xsltExt);
		try {
			JdomUtil.getPrettyOutputter("utf-8").output(document, output);
		}
		finally {
			output.close();
		}
	}
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("success", true);
	return String.valueOf(result);
}
//保存组件的自定义函数
public String saveFunctions(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	String data = request.getParameter("data");

	JSONArray items = new JSONArray(data);
	File compBundle = Application.getWarehouseFile(component);
	if (compBundle.isDirectory()) {
		File xpathExt = new File(compBundle, FUNCTIONS_FILE);
		Element rootEl = new Element("functions");
		
		for (int i=0, len=items.length(); i<len; i++) {
			JSONObject json = items.getJSONObject(i);
			Element el = new Element("function");
			el.setAttribute("prefix", json.getString("prefix"));
			el.setAttribute("name", json.getString("func-name"));
			el.setAttribute("class-name", json.getString("class-name"));
			el.setAttribute("description", json.optString("description"));
			rootEl.addContent(el);
		}

		OutputStream output = new FileOutputStream(xpathExt);
		try {
			JdomUtil.getPrettyOutputter("utf-8").output(new Document(rootEl), output);
		}
		finally {
			output.close();
		}
	}
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("success", true);
	return String.valueOf(result);
}
//保存流程引擎组件的状态映射表
public String saveMapping(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	String topic = request.getParameter("topic");
	String data = request.getParameter("data");

	JSONArray items = new JSONArray(data);
	File compBundle = Application.getWarehouseFile(component);
	if (compBundle.isDirectory()) {
		File xslt = new File(compBundle, topic+".fault");
		Namespace xmlns_xsl = Namespace.getNamespace("xsl", Namespaces.XSL);
		Element rootEl = new Element("mapping");
		XPath xpath = XPath.newInstance("template[@match=$group]");
		Element chooseEl = null;
		for (int i=0, len=items.length(); i<len; i++) {
			JSONObject json = items.getJSONObject(i);
			String group = json.getString("fault-spot");
			xpath.setVariable("group", group);
			Element templateEl = (Element)xpath.selectSingleNode(rootEl);
			if (templateEl == null) {
				templateEl = new Element("template");
				templateEl.setAttribute("match", group);
				rootEl.addContent(templateEl);
			}
			
			JSONArray datas = json.optJSONArray("items");
			if (datas.length() > 0) {
				for (int j = 0; j<datas.length(); j++) {
					JSONObject whenData = datas.getJSONObject(j);
					Element whenEl = new Element("when");
					whenEl.setAttribute("status", whenData.optString("status"));
					whenEl.setAttribute("fault-code", whenData.optString("result"));
					whenEl.setAttribute("fault-reason", whenData.optString("description"));
					templateEl.addContent(whenEl);
				}
			}
		}

		Document doc = new Document(rootEl);

		OutputStream output = new FileOutputStream(xslt);
		try {
			JdomUtil.getPrettyOutputter("utf-8").output(doc, output);
		}
		finally {
			output.close();
		}
	}
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("success", true);
	return String.valueOf(result);
}


/**
*载入组件参数
*/
public String saveParams(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	String data = request.getParameter("data");

	File compBundle = Application.getWarehouseFile(component);
	if (compBundle.isDirectory()) {
		JSONObject params = new JSONObject(data);
		Properties properties = new Properties();
		
		for (Iterator keys=params.keys(); keys.hasNext(); ) {
			String key = (String)keys.next();
			properties.setProperty(key, params.getString(key));
		}
		
		OutputStream output = new FileOutputStream(new File(compBundle, PROPERTY_FILE));
		try {
			properties.store(output, "");
		}
		finally {
			output.close();
		}
	}
	
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

//保存组件的状态列表
public String saveStatus(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String component = request.getParameter("component");
	String data = request.getParameter("data");

	JSONArray items = new JSONArray(data);
	File compBundle = Application.getWarehouseFile(component);
	if (compBundle.isDirectory()) {
		File statusXml = new File(compBundle, STATUS_FILE);
		Element rootEl = new Element("status-list");
		
		for (int i=0, len=items.length(); i<len; i++) {
			JSONObject json = items.getJSONObject(i);
			Element el = new Element("status");
			el.setAttribute("code", json.getString("status"));
			el.setAttribute("text", json.getString("text"));
			rootEl.addContent(el);
		}

		OutputStream output = new FileOutputStream(statusXml);
		try {
			JdomUtil.getPrettyOutputter("utf-8").output(new Document(rootEl), output);
		}
		finally {
			output.close();
		}
	}
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("success", true);
	return String.valueOf(result);
}

// 保存流程脚本扩展文件
public String saveScriptionPkg(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String component = request.getParameter("component");
	String data = request.getParameter("data");

	JSONArray items = new JSONArray(data);
	File compBundle = Application.getWarehouseFile(component);
	if (compBundle.isDirectory()) {
		File beanshellExt = new File(compBundle, BEANSHELL_FILE);
		
		Element rootEl = new Element("extensions");
		for (int i=0, len=items.length(); i<len; i++) {
			JSONObject json = items.getJSONObject(i);
			Element el = new Element("func-package");
			el.setText(json.getString("func-package"));
			rootEl.addContent(el);
		}

		OutputStream output = new FileOutputStream(beanshellExt);
		try {
			JdomUtil.getPrettyOutputter("utf-8").output(new Document(rootEl), output);
		}
		finally {
			output.close();
		}
	}
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("success", true);
	return result.toString();
}

// 读取流程脚本扩展文件
public String loadScriptionPkg(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String component = request.getParameter("component");

	JSONArray items = new JSONArray();
	File compBundle = Application.getWarehouseFile(component);
	if (!compBundle.exists()) {
		throw new Exception("找不到指定的组件包: "+compBundle.getCanonicalPath());
	}
	
	File beanshellFile = new File(compBundle, BEANSHELL_FILE);
	if (beanshellFile.exists()) {
		SAXBuilder builder = new SAXBuilder();
		Document document;
		if (compBundle.isDirectory()) {
			document = builder.build(new File(compBundle, BEANSHELL_FILE));
			Element rootEl = document.getRootElement();
			
			List list = rootEl.getChildren();
			if (list.size() > 0) {
				for (Iterator itr = list.iterator(); itr.hasNext();) {
					Element funcEl = (Element)itr.next();
					JSONObject json = new JSONObject();
					json.put("func-package", funcEl.getTextTrim());
					items.put(json);
				}
			}
		}
	}
		
	response.setContentType("text/json; charset=utf-8");
	JSONObject result = new JSONObject();
	result.put("items", items);
	result.put("success", true);
	
	return result.toString();
}
%>

<%
Logger logger = LoggerFactory.getLogger(this.getClass());
String operation = request.getParameter("operation");
WebServletResponse responseWrapper = new WebServletResponse(response);

	if (operation == null) operation = request.getMethod();
	
	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[]{HttpServletRequest.class, HttpServletResponse.class});
		String result = (String)method.invoke(this, new Object[]{new WebServletRequest(request), responseWrapper});
		out.clear();
		out.println(result);
	}
	catch (NoSuchMethodException e) {
		responseWrapper.sendError("["+request.getMethod()+"]找不到处理方法, operation:"+operation);
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
%>