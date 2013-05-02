<%@page import="com.sanxing.ads.Configuration"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.rmi.*"%>
<%@page import="com.sanxing.ads.utils.*"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="org.json.*" %>
<%@page import="org.jdom.*"%>
<%@page language="java" contentType="text/xml; charset=utf-8" pageEncoding="utf-8"%>

<%!

@SuppressWarnings("unchecked")
public String saveServers(HttpServletRequest request,HttpServletResponse response) throws Exception{
	String dataStr = request.getParameter("data");
	JSONArray dataArray = new JSONArray(dataStr);
	
	SAXBuilder builder = CommonUtil.newSAXBuilder();
	File clusterFile = Configuration.getClusterFile(getServletContext());
	Document cluster = builder.build(clusterFile);
	File serverFile = Configuration.getServerFile(getServletContext());
	Document server = builder.build(serverFile);
	
	// 先删除所有server
	cluster.getRootElement().removeChildren("server");
	
	for(int i=0; i<dataArray.length(); i++) {
		JSONObject jso = dataArray.getJSONObject(i);
		String serverName = jso.getString("server-name");
		String ip = jso.getString("IP");
		String jmsPort = jso.getString("jms-port");
		String jndiName = jso.getString("jndi-name");
		
		// 创建服务节点元素
		Element serverEl = new Element("server");
		serverEl = createNewServer(serverName, ip, jmsPort, jndiName, serverEl);
		
		// 取得数据源元素并添加
		Element datasourceEl =  
			(Element) org.jdom.xpath.XPath.selectSingleNode(server, "/server/jdbc/datasource[jndi-name='" + jndiName + "']");
		if (datasourceEl != null) {
			Element jdbc =  new Element("jdbc");
			jdbc.addContent((Element)datasourceEl.clone());
			serverEl.addContent(jdbc);
		}
		cluster.getRootElement().addContent(serverEl);
		Configuration.writeClusterFile(cluster, clusterFile);
	}
	
	return "true";
}

private Element getServerElement(Document cluster, String serverName) throws Exception {
	Element serverEl =
		(Element)org.jdom.xpath.XPath.selectSingleNode(cluster, "/managed-servers/server[server-name='" + serverName  + "']");
	return serverEl;
}

private Element createNewServer(String serverName ,String ip,String jmsPort,String jndiName,  Element serverEl) throws Exception {
	Element snEl = new Element("server-name");
	snEl.setText(serverName);
	
	Element ipEl = new Element("IP");
	ipEl.setText(ip);
	
	Element jmsEl =  new Element("jms");
	Element appEl = new Element("app-info");
	Element port = new Element("activemq-broker-port");
	port.setText(jmsPort);
	
	appEl.addContent(port);
	jmsEl.addContent(appEl);
	
	serverEl.addContent(snEl);
	serverEl.addContent(ipEl);
	serverEl.addContent(jmsEl);
	
	return serverEl;
}




@SuppressWarnings("unchecked")
public String loadDatasource(HttpServletRequest request, HttpServletResponse response) throws Exception{
	JSONObject result = new JSONObject();
	JSONArray items = new JSONArray();
	result.put("items",items);
	
	File DSFile = Configuration.getServerFile(getServletContext());
	SAXBuilder builder = new SAXBuilder();
	Document doc = builder.build(DSFile);
	Element root = doc.getRootElement();
	Element jdbc = root.getChild("jdbc");
	
	if (jdbc != null) {
		Iterator children = jdbc.getChildren("datasource").iterator();
		while(children.hasNext()){
			JSONObject item = new JSONObject();
			Element ds = (Element)children.next();
			Element jndi = ds.getChild("jndi-name");
			if(null != jndi){
				item.put("text",jndi.getTextTrim());
				item.put("value",jndi.getTextTrim());
			}
			
			Element appInfo = ds.getChild("app-info");
			if (appInfo != null) {
				List appChildren = appInfo.getChildren();
				if (appChildren != null && !appChildren.isEmpty())
					for (Iterator itr = appChildren.iterator(); itr.hasNext();) {
						Element appChild = (Element)itr.next();
						item.put(appChild.getName(), appChild.getText());
					}
			}
			items.put(item);
		}
	}
	
	response.setContentType("text/json;charset=utf-8");
	return result.toString();
}
%>

<%
	Logger logger = Logger.getLogger(this.getClass());
	String operation = request.getParameter("operation");
	WebServletResponse responseWrapper = new WebServletResponse(response);
	
	if (operation == null)
		operation = request.getMethod().toLowerCase();

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
