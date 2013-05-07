<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="org.jdom.Element"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.jdom.xpath.*"%>
<%@page import="com.sanxing.studio.utils.CommonUtil"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.jdom.Document"%>
<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>
<%! 
private final Logger logger = LoggerFactory.getLogger(this.getClass());

@SuppressWarnings("unchecked")
public String getServerList(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String dataStr = request.getParameter("data");
	JSONArray datas = (!"".equals(dataStr)) ? new JSONArray(dataStr) : null;
	List<String> dataSources = new ArrayList<String>();
	if (datas != null) {
		for (int i = 0, len = datas.length(); i < len; i++) {
			JSONObject data = datas.getJSONObject(i);
			dataSources.add(data.getString("jndi-name"));
		}
	}
	
	JSONArray items = new JSONArray();
	List serverList = Configuration.getClusters();
	if (serverList != null && !serverList.isEmpty()) {
		for (Iterator itr = serverList.iterator(); itr.hasNext();) {
			boolean hasPublished = false;
			JSONObject jso = new JSONObject();
			org.jdom.Element serverEl = (org.jdom.Element)itr.next();
			
			Element jdbcEl = serverEl.getChild("jdbc");
			if (jdbcEl != null) {
				List dsList = jdbcEl.getChildren("datasource");
				if (dsList != null && !dsList.isEmpty()) {
					for (Iterator iterator = dsList.iterator(); iterator.hasNext();) {
						Element dsEl = (Element)iterator.next();
						String jndiName = dsEl.getChildText("jndi-name");
						if (dataSources.contains(jndiName)) {
							hasPublished = true;
						}
					}
				}
			}
			
			String value = serverEl.getChildText("server-name");
			jso.put("name", value);
			jso.put("published", hasPublished);
			items.put(jso);
		}
	}
	
	JSONObject rs = new JSONObject();
	rs.put("items", items);
	rs.put("count", items.length());
	
	response.setContentType("text/json; charset=utf-8");
	return rs.toString();
}

@SuppressWarnings("unchecked")
public String publishDataSource(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String servresStr = request.getParameter("servers");
	JSONArray servers = new JSONArray(servresStr);
	String dataStr = request.getParameter("data");
	
	JSONArray datas = new JSONArray(dataStr);
	List dataSources = new ArrayList();
	for (int i = 0, len = datas.length(); i < len; i++) {
		JSONObject data = datas.getJSONObject(i);
		Element datasource = new Element("datasource");
		
		Element jndiName = new Element("jndi-name");
		jndiName.setText(data.getString("jndi-name"));
		
		Element appInfo = new Element("app-info");
		for (Iterator keys = data.keys(); keys.hasNext();) {
			String key = (String) keys.next();
			if (!key.equals("jndi-name")) {
				Element child = new Element(key);
				child.setText(data.getString(key));
				appInfo.addContent(child);
			}
		}
		datasource.addContent(jndiName);
		datasource.addContent(appInfo);
		dataSources.add(datasource);
	}
	
	Configuration.publishDataSource(servers, dataSources);
	
	response.setContentType("text/plain; charset=utf-8");
	return "true";
}

public String abolishDataSource(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String dataStr = request.getParameter("data");
	JSONArray datas = new JSONArray(dataStr);
	List<String> dataSources = new ArrayList<String>();
	
	for (int i = 0, len = datas.length(); i < len; i++) {
		JSONObject data = datas.getJSONObject(i);
		dataSources.add(data.getString("jndi-name"));
	}
	
	Configuration.abolishDataSource(dataSources);
	
	response.setContentType("text/plain; charset=utf-8");
	return "true";
}

public String getTransactionManager(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String tsm = "";
		SAXBuilder builder = CommonUtil.newSAXBuilder();
		Document server = builder.build(Configuration.getServerFile());
		if (server != null) {
			Element root = server.getRootElement();
			if (root != null) {
				Element tsmanagerElement = root.getChild("transaction-manager");
				if (tsmanagerElement != null)
					tsm = tsmanagerElement.getText();
			}
		}
		return tsm;
}
%>

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