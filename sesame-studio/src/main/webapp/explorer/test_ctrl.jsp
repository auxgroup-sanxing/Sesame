<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.studio.emu.*"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.sql.*"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page
	import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.*"%>
<%@page import="org.json.*"%>

<%!

private Element createTarget(String serviceName, String intfName, String operaName){
   	Element targetEl = new Element("target");
   	targetEl.addContent(new Element("service-name").setText(serviceName));
   	targetEl.addContent(new Element("interface-name").setText(intfName));
   	targetEl.addContent(new Element("operation-name").setText(operaName));
   	return targetEl;
}

public String load(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		Element unitEl = new Element("unit");
		File unitFolder = Configuration.getWorkspaceFile(unit);
		Document document = builder.build(new File(unitFolder, "META-INF/unit.xml"));
		Element rootEl = document.getRootElement();
		String oriented = rootEl.getAttributeValue("oriented");
		String endpoint = rootEl.getAttributeValue("endpoint");
		Element attrEl = new Element("attributes");
		unitEl.addContent(attrEl);
		List<?> list = rootEl.getAttributes();
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			iter.remove();
			attrEl.setAttribute(attr.detach());
		}
		attrEl.addContent(new Element("name").setText(unitFolder.getName()));
		Element docuEl = rootEl.getChild("documentation");
		if (docuEl != null)
			attrEl.addContent(docuEl.detach());
			
		File epFolder = new File(unitFolder, "../../"+oriented+"/"+endpoint);
		rootEl = builder.build(new File(epFolder, "META-INF/unit.xml")).getRootElement();
		Element prefEl=rootEl.getChild("properties");
		if (prefEl != null) {
			unitEl.addContent(prefEl.detach());
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

public String loadCase(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String caseName = request.getParameter("casename");
	String unit = request.getParameter("unit");
	String schema = request.getParameter("schema");
	try
	{
		Element rootEl = new Element("unit");

		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, schema.replace(".xsd", ".xml"));
		try {
			Document doc = builder.build(file);
			Element root = doc.getRootElement();
			Element el = (Element) XPath.selectSingleNode(root, "*[@name='"+caseName+"']");
			System.out.println(el);
			if (el != null) {
				rootEl.addContent(el.detach());
				List<?> attributes = el.getAttributes();
				for (Iterator<?> iter=attributes.iterator(); iter.hasNext(); ) {
					Attribute attr = (Attribute)iter.next();
					iter.remove();
					rootEl.setAttribute(attr.detach());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		XMLOutputter outputter = JdomUtil.getPrettyOutputter(response.getCharacterEncoding());
		return outputter.outputString(new Document(rootEl));
	}
	finally
	{
	}
}

public String getInterfaces(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		Document document = builder.build(new File(unitFolder, "META-INF/unit.xml"));
		Element rootEl = document.getRootElement();
		JSONArray items = new JSONArray();
		List<?> list=rootEl.getChildren("interface");
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
			Element intfEl = (Element)iter.next();
			JSONObject item = new JSONObject();
			String name = intfEl.getAttributeValue("name");
			String desc = intfEl.getAttributeValue("desc", name);
			item.put("name", name);
			item.put("label", desc);
			items.put(item);
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

public String getAvailServices(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
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
			Document document = builder.build(new File(folder, "META-INF/unit.xml"));
			Element rootEl = document.getRootElement();
			String service = rootEl.getAttributeValue("service-name");
			String namespace = rootEl.getAttributeValue("namespace");
			File[] files = folders[i].listFiles();
			for (int j=0; j<files.length; j++) {
				File file = files[j];
				if (!file.isFile()) continue;
				document = builder.build(file);
				rootEl = document.getRootElement();
				Namespace ns = rootEl.getNamespace();
				Element portEl, operEl;
				if ((portEl=rootEl.getChild("portType", ns))!=null && (operEl=portEl.getChild("operation", ns)) != null) {
					JSONObject item = new JSONObject();
					String label = operEl.getChildText("documentation", ns);
					String opera = operEl.getAttributeValue("name");
					item.put("service", "{"+namespace+"}"+service);
					item.put("intf", "{"+namespace+"}"+portEl.getAttributeValue("name"));
					item.put("opera", "{}"+opera);
					item.put("name", folder.getName()+"/"+file.getName());
					item.put("label", label!=null ? "["+opera+"]"+label : opera);
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

public String getCurrentState(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	JSONObject result = new JSONObject();
	result.put("state", BindingUnitManager.getInstance().getCurrentState(unit));
	result.put("success", true);
	response.setContentType("text/json; charset=utf-8");
	return result.toString();
}

//从 unit.xml 文件装载服务的操作列表，供绑定服务单元使用
public String loadOperations(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Document document = builder.build(new File(unitFolder, "META-INF/unit.xml"));
		Element rootEl = document.getRootElement();
		String oriented = rootEl.getAttributeValue("oriented");
		String endpoint = rootEl.getAttributeValue("endpoint");
		
		File epFolder = new File(unitFolder, "../../"+oriented+"/"+endpoint);
		rootEl = builder.build(new File(epFolder, "META-INF/unit.xml")).getRootElement();
		JSONArray items = new JSONArray();
		List<?> list=rootEl.getChildren("interface");
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
			Element intfEl = (Element)iter.next();
			List<?> operList = intfEl.getChildren("operation");
			for (Iterator<?> it=operList.iterator(); it.hasNext(); ) {
				Element operEl = (Element)it.next();
				String opera = operEl.getAttributeValue("name");
				File file = new File(unitFolder, opera+".xsd");
				JSONObject item = new JSONObject();
				item.put("interface", intfEl.getAttributeValue("name"));
				item.put("opera", opera);
				item.put("desc", operEl.getAttributeValue("desc"));
				item.put("ref", operEl.getAttributeValue("ref"));
				Element targetEl=operEl.getChild("target");
				if (targetEl != null) {
					item.put("ref-svc", targetEl.getChildText("service-name"));
					item.put("ref-intf", targetEl.getChildText("interface-name"));
					item.put("ref-opera", targetEl.getChildText("operation-name"));
				}
				if (file.exists()) item.put("lastModified", dateFormat.format(new java.util.Date(file.lastModified()))  );
				items.put(item);
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

//从 wsdl 文件装载服务的操作列表，引擎服务单元使用
public String loadOperaFiles(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	try
	{
		SAXBuilder builder = new SAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONArray items = new JSONArray();
		
		File[] files = unitFolder.listFiles();
		if (files != null) {
			
			for (int i=0; i<files.length; i++) {
				File file = files[i];
				if (!file.isFile()) continue;
			
				Document document = builder.build(file);
				Element rootEl = document.getRootElement();
				Namespace ns = rootEl.getNamespace();
			
				List<?> list=rootEl.getChildren("portType", ns);
				for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
					Element intfEl = (Element)iter.next();
					List<?> operList = intfEl.getChildren("operation", ns);
					for (Iterator<?> it=operList.iterator(); it.hasNext(); ) {
						Element operaEl = (Element)it.next();
						String opera = operaEl.getAttributeValue("name");
						JSONObject item = new JSONObject();
						item.put("interface", intfEl.getAttributeValue("name"));
						item.put("opera", opera);
						item.put("desc", operaEl.getChildText("documentation", ns));
						item.put("file", file.getName());
						item.put("lastModified", dateFormat.format(new java.util.Date(file.lastModified()))  );
						item.put("readOnly", !file.canWrite());
						items.put(item);
					}
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

public String modifyBinding(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");

	SAXBuilder builder = JdomUtil.newSAXBuilder();
	File unitFolder = Configuration.getWorkspaceFile(unit);
	File file = new File(unitFolder, "META-INF/unit.xml");
	Document doc = builder.build(file);
	Element rootEl = doc.getRootElement();

	String intf = request.getParameter("old-intf");
	String oper = request.getParameter("old-opera");
	Element serviceEl = (Element)XPath.selectSingleNode(rootEl, "interface[@name='"+intf+"']/operation[@name='"+oper+"']");
	if (serviceEl == null) {
		WebUtil.sendError(response, "操作不存在，可能已被删除");
		return "";
	}
	else {
		serviceEl.setAttribute("name", request.getParameter("opera"));
	   	serviceEl.setAttribute("desc", request.getParameter("desc"));
	   	serviceEl.setAttribute("ref", request.getParameter("ref"));
	  	Element targetEl = serviceEl.getChild("target");
	  	if (targetEl != null) targetEl.detach();
	  	targetEl = this.createTarget(request.getParameter("ref-svc"), request.getParameter("ref-intf"), 
	  		request.getParameter("ref-opera"));
	   	serviceEl.addContent(targetEl);
	   	String newIntf = request.getParameter("interface");
		if (!intf.equals(newIntf)) {
			Element intfEl = (Element)XPath.selectSingleNode(rootEl, "interface[@name='"+newIntf+"']");
			if (intfEl == null) {
				rootEl.addContent(intfEl=new Element("interface"));
				intfEl.setAttribute("name", newIntf);
			}
	   		intfEl.addContent(serviceEl.detach());
		}
	}
	XMLOutputter outputter = new XMLOutputter();
	outputter.setFormat(Format.getPrettyFormat().setEncoding("utf-8").setIndent("  "));
	OutputStream fileStream = new FileOutputStream(file);
	try {
	   	outputter.output(doc, fileStream);
	}
	finally {
	   	fileStream.close();
	}
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

//引擎服务单元 修改 wsdl 文件内容
public String modifyFile(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unitName = request.getParameter("unit");
	String fileName = request.getParameter("file");

	SAXBuilder builder = JdomUtil.newSAXBuilder();
	File unitFolder = Configuration.getWorkspaceFile(unitName);
	File file = new File(unitFolder, fileName);
	Document doc = builder.build(file);
	Element rootEl = doc.getRootElement();
	Namespace ns = rootEl.getNamespace();
	String intf = request.getParameter("old-intf");
	String oper = request.getParameter("old-opera");
	XPath xpath = XPath.newInstance("wsdl:portType[@name='"+intf+"']/wsdl:operation[@name='"+oper+"']");
	xpath.addNamespace("wsdl", ns.getURI());
	Element operaEl = (Element)xpath.selectSingleNode(rootEl);
	if (operaEl == null) {
		WebUtil.sendError(response, "操作不存在，可能已被删除");
		return "";
	}
	else {
		operaEl.setAttribute("name", request.getParameter("opera"));
		Element docuEl = operaEl.getChild("documentation", ns);
		if (docuEl==null) operaEl.addContent(0, docuEl=new Element("documentation", ns));
		docuEl.setText(request.getParameter("desc"));
		
	   	String newIntf = request.getParameter("interface");
		if (!intf.equals(newIntf)) {
			if (operaEl.getParentElement().getChildren("operation", ns).size() == 1) {
				operaEl.getParentElement().setAttribute("name", newIntf);
			}
			else {
				xpath = XPath.newInstance("wsdl:portType[@name='"+intf+"']");
				xpath.addNamespace("wsdl", ns.getURI());
				Element intfEl = (Element)xpath.selectSingleNode(rootEl);
				if (intfEl == null) {
					rootEl.addContent(intfEl=new Element("portType", rootEl.getNamespace()));
					intfEl.setAttribute("name", newIntf);
				}
		   		intfEl.addContent(operaEl.detach());
	   		}
		}
	}
	XMLOutputter outputter = new XMLOutputter();
	outputter.setFormat(Format.getPrettyFormat().setEncoding("utf-8").setIndent("  "));
	OutputStream fileStream = new FileOutputStream(file);
	try {
	   	outputter.output(doc, fileStream);
	}
	finally {
	   	fileStream.close();
	}
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

public String removeBinding(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("operas");
	String unit = request.getParameter("unit");
	try
	{
		SAXBuilder builder = JdomUtil.newSAXBuilder();
		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, "META-INF/unit.xml");
		Document doc = builder.build(file);
		Element rootEl = doc.getRootElement();
	
		JSONArray operas = new JSONArray(data);
		for (int i=0,len=operas.length(); i<len; i++) {
			JSONObject operaObj = operas.getJSONObject(i);
			String intf = operaObj.getString("interface");
			String opera = operaObj.getString("opera");
			String xpath = "interface[@name='"+intf+"']/operation[@name='"+opera+"']";
			Element serviceEl = (Element)XPath.selectSingleNode(rootEl, xpath);
			if (serviceEl != null) {
				File xsdfile = new File(unitFolder, opera+".xsd");
				if (xsdfile.exists()) xsdfile.delete();
				Element intfEl = serviceEl.getParentElement();
				if (intfEl.getChildren().size()==1) 
					intfEl.detach();
				else
					serviceEl.detach();
			}
		}
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat().setEncoding("utf-8").setIndent("  "));
 		OutputStream fileStream = new FileOutputStream(file);
		try {
			outputter.output(doc, fileStream);
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

public String save(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("data");
	String unit = request.getParameter("unit");
	try
	{
		SAXBuilder builder = new SAXBuilder();
		Reader reader= new StringReader(data);
		Document document = builder.build(reader);
		Element unitEl = document.getRootElement();

		File unitFolder = Configuration.getWorkspaceFile(unit);
		File unitFile = new File(unitFolder, "META-INF/unit.xml");
		Document doc = builder.build(unitFile);
		Element root = doc.getRootElement();
		Element attrEl = unitEl.getChild("attributes");
		List<?> list = attrEl.getAttributes();
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			iter.remove();
			root.setAttribute(attr.detach());
		}
		Element docuEl = attrEl.getChild("documentation");
		Element documentation = root.getChild("documentation");
		if (documentation != null) 
			documentation.setText(docuEl.getText());
		else
			root.addContent(0, docuEl.detach());
		
		Element propsEl = unitEl.getChild("properties");
		Element properties = root.getChild("properties");
		if (propsEl != null) {
			if (properties != null)
				properties.setContent(propsEl.removeContent());
			else
				root.addContent(1, propsEl.detach());
		}
		XMLOutputter outputter = JdomUtil.getPrettyOutputter();
		OutputStream fileStream = new FileOutputStream(unitFile);
		try {
			outputter.output(doc, fileStream);
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

public String removeCase(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("data");
	String unit = request.getParameter("unit");
	String schema = request.getParameter("schema");

	SAXBuilder builder = new SAXBuilder();

	File unitFolder = Configuration.getWorkspaceFile(unit);
	File file = new File(unitFolder, schema.replace(".xsd", ".xml"));
	Document doc = builder.build(file);
	Element root = doc.getRootElement();
	Element el = (Element) XPath.selectSingleNode(root, "*[@name='"+request.getParameter("casename")+"']");
	if (el != null) {
		el.detach();
	}
	
	XMLOutputter outputter = JdomUtil.getPrettyOutputter();
	OutputStream fileStream = new FileOutputStream(file);
	try {
		outputter.output(doc, fileStream);
	}
	finally {
		fileStream.close();
	}
	response.setContentType("text/json; charset=utf-8");
	return "true";
}

public String saveCase(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("data");
	String unit = request.getParameter("unit");
	String schema = request.getParameter("schema");
	try
	{
		SAXBuilder builder = new SAXBuilder();
		Reader reader= new StringReader(data);
		Document document = builder.build(reader);
		Element rootEl = document.getRootElement();
		List<?> attributes = rootEl.getAttributes();

		File unitFolder = Configuration.getWorkspaceFile(unit);
		File file = new File(unitFolder, schema.replace(".xsd", ".xml"));
		Document doc;
		Element messageEl = (Element) XPath.selectSingleNode(rootEl, "*[1]");
		try {
			doc = builder.build(file);
			Element root = doc.getRootElement();
			Element el = (Element) XPath.selectSingleNode(root, "*[@name='"+rootEl.getAttributeValue("name")+"']");
			if (el != null) {
				el.setContent(messageEl.removeContent());
				messageEl = el;
			}
			else {
				root.addContent(messageEl.detach());
			}
		}
		catch (Exception e) {
			doc = document;
			messageEl = (Element) XPath.selectSingleNode(rootEl, "*[1]");
		}
		for (Iterator<?> iter=attributes.iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			iter.remove();
			messageEl.setAttribute(attr.detach());
		}
		
		XMLOutputter outputter = JdomUtil.getPrettyOutputter();
		OutputStream fileStream = new FileOutputStream(file);
		try {
			outputter.output(doc, fileStream);
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

public String sendCase(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("data");
	String unit = request.getParameter("unit");
	String schema = request.getParameter("schema");
	try
	{
		SAXBuilder builder = new SAXBuilder();
		Reader reader= new StringReader(data);
		Document document = builder.build(reader);
		Element rootEl = document.getRootElement();
		Element messageEl = (Element) XPath.selectSingleNode(rootEl, "*[1]");
		List<?> attributes = rootEl.getAttributes();

		String unitRootPath = Configuration.getWorkspaceFile(unit).getAbsolutePath();
		String code = schema.replace(".xsd", "");
		
		BindingUnitManager manager = BindingUnitManager.getInstance();
		BindingUnit unitObj = manager.getUnit(unit);
		Element result = new Element("response");
		if (unitObj instanceof Client) {
			result = ((Client)unitObj).send(code, messageEl, 30*1000);
		}
		else {
			throw new Exception("模拟器尚未初始化");
		}
		//manager.init(unit, unitRootPath, properties);

		response.setContentType("text/xml; charset=utf-8");
		XMLOutputter outputter = new XMLOutputter();
		return outputter.outputString(result);
	}
	finally
	{
	}
}

public String start(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String data = request.getParameter("data");
	String unit = request.getParameter("unit");
	String autorun = request.getParameter("autorun");
	try
	{
		SAXBuilder builder = new SAXBuilder();
		Reader reader= new StringReader(data);
		Document document = builder.build(reader);
		Element unitEl = document.getRootElement();

		String unitRootPath = Configuration.getWorkspaceFile(unit).getAbsolutePath();
		
		//Element propertiesEl = unitEl.getChild("properties");
		Map<String, Object> properties = new Hashtable<String, Object>();
		List<?> list = XPath.selectNodes(unitEl, "properties/*/*");
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ){
		    Element el = (Element)iter.next();
		    properties.put(el.getName(), el.getText());
		}
		Element attributesEl = unitEl.getChild("attributes");
		properties.put("emulator", attributesEl.getAttributeValue("emulator"));
		properties.put("selector", attributesEl.getAttributeValue("selector"));
		BindingUnitManager manager = BindingUnitManager.getInstance();
		manager.init(unit, unitRootPath, properties);
		if (autorun.equals("true")) {
			manager.start(unit);
		}
		response.setContentType("text/json; charset=utf-8");
		return "true";
	}
	finally
	{
	}
}

public String stop(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String unit = request.getParameter("unit");
	
	BindingUnitManager manager = BindingUnitManager.getInstance();
	manager.shutDown(unit);
	return "true";
}
%>

<%
Logger logger = LoggerFactory.getLogger(this.getClass());
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