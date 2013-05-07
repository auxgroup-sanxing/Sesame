<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.sesame.transport.*"%>
<%@page import="com.sanxing.sesame.address.*"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.net.*"%>
<%@page import="javax.management.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="org.json.*"%>
<%@page import="org.dom4j.*, org.dom4j.io.*"%>

<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>


<%!
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String getSchemeParams(String scheme, String style) throws Exception {

		URL url = Protocols.getSchema(scheme, style);
		if (url == null) {
			return null;
		}
		org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
		org.jdom.Element rootEl = builder.build(url).getRootElement();
		org.jdom.Element firstEl = rootEl.getChild("element", rootEl.getNamespace());
		if (firstEl != null) {
			JSONObject json = new JSONObject();
			json.put("tag", firstEl.getName());
			json.put("name", firstEl.getAttributeValue("name"));
			json.put("stateful", false);
			SchemaUtil.generateUI(firstEl, json);
			return json.toString();
		}
		return null;
	}
	
	public String affect(HttpServletRequest request, HttpServletResponse response) throws Exception {
		AddressBook.init();
		
		JSONObject result = new JSONObject();
		result.put("success", true);
		result.put("message", "地址簿载入成功");
		return result.toString();
	}
	
	public String getLocations(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONArray result = new JSONArray();
		File bookFile = Configuration.getAddressBookFile();
		if (!bookFile.exists() && System.getProperty("SESAME_HOME")==null) {
			throw new FileNotFoundException("地址簿文件不存在: "+bookFile + ", 测试环境请使用-D参数设置SESAME_HOME");
		}
		
		SAXReader reader = new SAXReader();
		Document document = reader.read(bookFile);
		List<?> locations = document.getRootElement().elements("location");
		for (Object location : locations) {
			Element locationEl = (Element)location;
			URI uri = new URI(locationEl.attributeValue("url", ""));
			JSONObject data = new JSONObject();
			data.put("name", locationEl.attributeValue("name"));
			data.put("scheme", uri.getScheme());
			data.put("uri", uri.getSchemeSpecificPart());
			data.put("style", locationEl.attributeValue("style"));
			result.put(data);
		}
		
		return result.toString();
	}
	
	public String getSchemes(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONArray result = new JSONArray();
		String[] schemes = Protocols.list();
		for (String scheme : schemes) {
			//URI uri = new URI(locationEl.attributeValue("url"));
			JSONObject data = new JSONObject();
			data.put("scheme", scheme);
			data.put("label", scheme);
			result.put(data);
		}
		return result.toString();
	}
	
	public String loadConfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String name = request.getParameter("name");
		File bookFile = Configuration.getAddressBookFile();
		SAXReader reader = new SAXReader();
		Document document = reader.read(bookFile);
		Element locationEl = (Element)document.getRootElement().selectSingleNode("location[@name='"+name+"']");
		if (locationEl == null) {
			locationEl = DocumentHelper.createElement("location");
		}
		response.setContentType("text/xml; charset=utf-8");
		return locationEl.asXML();
	}
	
	public String loadProperties(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String scheme = request.getParameter("scheme");
		String style = request.getParameter("style");
		return this.getSchemeParams(scheme, style);
	}
	
	public String removeLocation(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String name = request.getParameter("name");
		File bookFile = Configuration.getAddressBookFile();
		SAXReader reader = new SAXReader();
		Document document = reader.read(bookFile);
		Element rootEl = document.getRootElement();
		Element locationEl = (Element)rootEl.selectSingleNode("location[@name='"+name+"']");
		if (locationEl != null) {
			locationEl.detach();
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("utf-8");
			XMLWriter outputter = new XMLWriter(format);
			FileOutputStream outStream = new FileOutputStream(bookFile);
			try {
				outputter.setOutputStream(outStream);
				outputter.write(document);
			}
			finally {
				outStream.close();
			}
		}
		return "{success: true}";
	}
	
	public String saveConfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String name = request.getParameter("name");
		String data = request.getParameter("data");
		File bookFile = Configuration.getAddressBookFile();
		SAXReader reader = new SAXReader();
		Document document = reader.read(bookFile);
		Element locationEl = (Element)document.getRootElement().selectSingleNode("location[@name='"+name+"']");
		if (locationEl != null) {
			Document doc = DocumentHelper.parseText(data);
			Element rootEl = doc.getRootElement();
			locationEl.clearContent();
			locationEl.appendContent(rootEl);
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("utf-8");
			XMLWriter outputter = new XMLWriter(format);
			FileOutputStream outStream = new FileOutputStream(bookFile);
			try {
				outputter.setOutputStream(outStream);
				outputter.write(document);
			}
			finally {
				outStream.close();
			}
		}
		else {
			throw new IllegalNameException("没有找到地址 - "+name);
		}
		MBeanServer mbeanServer = (MBeanServer)getServletContext().getAttribute("MBeanServer");
		if ((mbeanServer != null) ){
			URI uripath = new URI(locationEl.attributeValue("url"));
			String style = locationEl.attributeValue("style");
			org.w3c.dom.Element locationDocEle = (org.w3c.dom.Element)Dom4jUtil.parseDOM4JElement2DOMElement(locationEl);   
			Location location = new Location(uripath, locationDocEle, style);
			AddressBook.add(name,location);
		}
		return "{success: true}";
	}
	
	public String saveLocation(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String oldName = request.getParameter("old-name");
		String name = request.getParameter("name");
		String scheme = request.getParameter("scheme");
		String uri = request.getParameter("uri");
		String style = request.getParameter("style");
		
		File bookFile = Configuration.getAddressBookFile();
		SAXReader reader = new SAXReader();
		Document document = reader.read(bookFile);
		Element rootEl = document.getRootElement();
		Element locationEl = (Element)rootEl.selectSingleNode("location[@name='"+name+"']");
		
		if (oldName==null || oldName.length()==0) {
			if (locationEl != null) {
				throw new IllegalNameException("地址名称已存在 - "+name);
			}
		}
		else {
			locationEl = (Element)rootEl.selectSingleNode("location[@name='"+oldName+"']");
		}
		if (locationEl == null) {
			locationEl = DocumentHelper.createElement("location");
			rootEl.add(locationEl);
		}
		locationEl.addAttribute("name", name);
		locationEl.addAttribute("url", scheme+":"+uri);
		locationEl.addAttribute("style", style);
		
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		XMLWriter outputter = new XMLWriter(format);
		FileOutputStream outStream = new FileOutputStream(bookFile);
		try {
			outputter.setOutputStream(outStream);
			outputter.write(document);
		}
		finally {
			outStream.close();
		}
		
		MBeanServer mbeanServer = (MBeanServer)getServletContext().getAttribute("MBeanServer");
		if ((mbeanServer != null) ){
			URI uripath = new URI(scheme+":"+uri);
			org.w3c.dom.Element locationDocEle = (org.w3c.dom.Element)Dom4jUtil.parseDOM4JElement2DOMElement(locationEl);   
			Location location = new Location(uripath, locationDocEle, style);
			AddressBook.add(name,location);
		}
		return "true";
		
	}%>

<%
	Logger logger = LoggerFactory.getLogger(this.getClass());
	String operation = request.getParameter("operation");
	WebServletResponse responseWrapper = new WebServletResponse(response);
	
	try {
		response.setContentType("text/json; charset=utf-8");
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[]{HttpServletRequest.class, HttpServletResponse.class});
		String result = (String)method.invoke(this, new Object[]{new WebServletRequest(request), responseWrapper});
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
		logger.error("", e);
		throw new ServletException(e.getMessage(), e);
	}
%>