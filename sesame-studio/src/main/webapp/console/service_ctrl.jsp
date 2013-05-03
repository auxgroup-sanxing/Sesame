
<%@page import="java.net.URLDecoder"%><%@page import="com.sanxing.studio.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.rmi.*"%>
<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.json.*" %>
<%@page import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.XPath"%>
<%@page import="com.sanxing.sesame.core.keymanager.*"%>
<%@page import="javax.wsdl.*"%>
<%@page import="javax.wsdl.factory.*"%>
<%@page import="javax.wsdl.xml.*"%>
<%@page import=" com.sanxing.studio.search.*"%>
<%@page language="java" contentType="text/xml; charset=utf-8" pageEncoding="utf-8"%>

<%!
private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

private Element getJBIRootElement(File jbiFile) {
	Element rootEl = null;
	try {
		if (jbiFile != null && jbiFile.exists()) {
			SAXBuilder builder = JdomUtil.newSAXBuilder();
			Document doc = builder.build(jbiFile);
			rootEl = doc.getRootElement();
		}
	} catch(Exception e) {
		e.printStackTrace();
		return null;
	}
	return rootEl;
}

private Definition getWSDLReader(File wsdlFile) throws WSDLException {
	if (wsdlFile == null || !wsdlFile.exists())
		return null;
	
	WSDLFactory factory = WSDLFactory.newInstance();
	WSDLReader reader = factory.newWSDLReader();
	reader.setFeature("javax.wsdl.verbose", false);
	reader.setFeature("javax.wsdl.importDocuments", false);
	Definition wsdlDef = reader.readWSDL(new WSDLLocatorImpl(wsdlFile, true));
	return wsdlDef;
}

// 获取所有项目列表
public String getAllProjects(HttpServletRequest request,HttpServletResponse response) throws Exception{
	JSONArray items = new JSONArray();
	JSONObject result = new JSONObject();
	
	File workarea = Configuration.getWorkspaceRoot();
	if(workarea.exists()) {
		File[] fileList = workarea.listFiles();
		if (fileList != null && fileList.length > 0) {
			for(File file : fileList) {
				if (file.isDirectory()) {
					Element root = getJBIRootElement(new File(file, "jbi.xml"));
					if (root != null) {
						Namespace ns = root.getNamespace();
						Element serviceEl = root.getChild("service-assembly", ns);
						Element idEl = serviceEl.getChild("identification", ns);
						String desc =  idEl.getChildText("description", ns);
						
						JSONObject jso = new JSONObject();
						jso.put("project-name", file.getName());
						jso.put("project-desc", desc);
						items.put(jso);
					}
				}
			}
		}
	}
	
	response.setContentType("text/json; charset=utf-8");
	result.put("items", items);
	return result.toString();
}

// 获取所有服务列表
public String getAllServices(HttpServletRequest request,HttpServletResponse response) throws Exception{ 
	String projectName = request.getParameter("projectName");
	File projectFolder = Configuration.getWorkspaceFile(projectName);
	
	JSONArray items = new JSONArray();
	
	if (projectFolder.exists()) {
		File[] list = projectFolder.listFiles();
		if (list != null && list.length > 0) {
			for (File file : list) {
				String name = file.getName();
				File unitFolder = null;
				File[] unitList = null;
				if (file.isDirectory()) {
					if ("server".equals(name) || "client".equals(name) || "engine".equals(name)) {
						unitFolder = new File(projectFolder, name);
						unitList = unitFolder.listFiles();
						if (unitList != null && unitList.length > 0) {
							for(File unitFile : unitList) {
								JSONObject jso = new JSONObject();
								Definition definition = getWSDLReader(new File(unitFile, "unit.wsdl"));
								String desc = getTextContent(definition.getDocumentationElement());
								
								jso.put("project-name", projectName);
								jso.put("service-type", name);
								jso.put("service-name", unitFile.getName());
								jso.put("service-desc", desc);
								items.put(jso);
							} 
						}
					}
				}
			}
		}
	}
	
	JSONObject result = new JSONObject();
	result.put("items", items);
	response.setContentType("text/json; charset=utf-8");
	return result.toString();
}

// 重建索引
public String createIndex(HttpServletRequest request,HttpServletResponse response) throws Exception{ 
	String data = request.getParameter("data");
	JSONArray projectsArray = new JSONArray(data);
	
	File stutdio = Configuration.getWorkspaceRoot().getParentFile();
	File indexDir = new File(stutdio, "temp");
	if (!indexDir.exists())
		indexDir.mkdir();
	
	// 取得searchurtil实例
	SearcherUtil su = SearcherUtil.getInstance(indexDir.getAbsolutePath());
	su.init();
	
	JSONObject jso = (projectsArray.length() > 0) ? projectsArray.getJSONObject(0) : null;
	String projectName = (jso != null) ? jso.getString("project-name") : null;
	File projectFile = (projectName != null) ? Configuration.getWorkspaceFile(projectName) : Configuration.getWorkspaceRoot();
	
	if (projectFile != null && projectFile.exists() && projectFile.isDirectory()) {
		su.buildIndexes(projectFile.getAbsolutePath());
		su.closeIndexs();
	}
	
	response.setContentType("text/plain;charset=utf-8");
	return "true";
}

// 搜索
@SuppressWarnings("unchecked")
public String searchRecords(HttpServletRequest request,HttpServletResponse response) throws Exception{ 
	JSONArray items = new JSONArray();
	JSONObject result = new JSONObject();
	
	String projects = request.getParameter("projects");
	JSONArray projectsArray = new JSONArray(projects);
	String filter = request.getParameter("filter");
	StringBuffer buffer = new StringBuffer();
	if (filter != null && !"null".equals(filter) && !"[{}]".equals(filter)) {
		JSONArray filterArray = new JSONArray(filter);
		for (int i=0; i<filterArray.length(); i++) {
			JSONObject jso = filterArray.getJSONObject(i);
			Iterator itr = jso.keys();
			while(itr.hasNext()) {
				String searchName = (String)itr.next();
				buffer.append(searchName + "#");
			}
		}
	}
	String queryStr = request.getParameter("value");
	queryStr = URLDecoder.decode(queryStr.trim(), "UTF-8");
	
	File stutdio = Configuration.getWorkspaceRoot().getParentFile();
	File indexDir = new File(stutdio, "temp");
	if (!indexDir.exists())
		indexDir.mkdir();
	
	SearcherUtil su = SearcherUtil.getInstance(indexDir.getAbsolutePath());
	JSONObject jso = (projectsArray.length() > 0) ? projectsArray.getJSONObject(0) : null;
	String projectName = (jso != null) ? jso.getString("project-name") : null;
	File projectFile = (projectName != null) ? Configuration.getWorkspaceFile(projectName) : Configuration.getWorkspaceRoot();
	
	if (projectFile != null && projectFile.exists() && projectFile.isDirectory()) {
		String[] filters = null;
		if (!"".equals(buffer.toString()))
			filters = buffer.toString().split("#");
		
		Set<Record> records = su.search(queryStr, 500, filters);
		
		if (records != null && !records.isEmpty()) {
		  for (Record record : records) {
			  	String type = record.getType();
	            String name = record.getNameColumn().getValue();
	            String desc = (record.getDescriptionColumn() != null) ? record.getDescriptionColumn().getValue(): "";
	            String impLocation = (record.getColumnByName(SearcherNames.WSDL_IMPORT_LOCATION) != null) ? record.getColumnByName(SearcherNames.WSDL_IMPORT_LOCATION).getValue() : "";
	            impLocation = impLocation.replaceAll("#", "").replaceAll("\\.\\./", "").replaceAll("unit.wsdl.*", "");
	            String serviceType = (record.getColumnByName(SearcherNames.SERVICE_TYPE) != null) ? record.getColumnByName(SearcherNames.SERVICE_TYPE).getValue() : "";
	            String unitName = (record.getColumnByName(SearcherNames.SERVICE_UNIT) != null) ? record.getColumnByName(SearcherNames.SERVICE_UNIT).getValue() : "";
	            String project = record.getColumnByName(SearcherNames.PROJECT_INDEX_NAME).getValue();
	            String opreationName = (record.getColumnByName(SearcherNames.OPERATION_INDEX_NAME) != null) ? record.getColumnByName(SearcherNames.OPERATION_INDEX_NAME).getValue(): "";
	            String opreationDesc = (record.getColumnByName(SearcherNames.OPERATION_INDEX_DESC) != null) ? record.getColumnByName(SearcherNames.OPERATION_INDEX_DESC).getValue(): "";
	            String serviceDesc = (record.getColumnByName(SearcherNames.SERVICE_INDEX_DESC) != null) ?  record.getColumnByName(SearcherNames.SERVICE_INDEX_DESC).getValue() : "";
	            String component = (record.getColumnByName(SearcherNames.COMPONENT) != null) ?  record.getColumnByName(SearcherNames.COMPONENT).getValue() : "";
	            String projectDesc = (record.getColumnByName(SearcherNames.PROJECT_INDEX_DESC) != null) ?  record.getColumnByName(SearcherNames.PROJECT_INDEX_DESC).getValue() : "";
	            
	            JSONObject rsJso = new JSONObject();
	            rsJso.put("type", type);
	            rsJso.put("name", name);
	            rsJso.put("desc", desc);
	            rsJso.put("serviceType", serviceType);
	            rsJso.put("serviceDesc", serviceDesc);
	            rsJso.put("projectName", project);
	            rsJso.put("unitName", unitName);
	            rsJso.put("operaName", opreationName);
	            rsJso.put("operaDesc", opreationDesc);
	            rsJso.put("impLocation", impLocation);
	            rsJso.put("component", component);
	            rsJso.put("projectDesc", projectDesc);
	            items.put(rsJso);
	        }
		}
	}
	
	response.setContentType("text/json; charset=utf-8");
	result.put("totalCount", items.length());
	result.put("items", items);
	return result.toString();
}
%>

<%
	Logger logger = LoggerFactory.getLogger(this.getClass());
	String operation = request.getParameter("operation");
	WebServletResponse responseWrapper = new WebServletResponse(response);
	
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
		responseWrapper.sendError(t.getMessage());
	}
	catch (Exception e) {
		logger.error("", e);
		responseWrapper.sendError(e.getMessage());
	}
%>