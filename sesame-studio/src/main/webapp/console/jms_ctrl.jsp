<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>
<%! 
private final Logger logger = LoggerFactory.getLogger(this.getClass());

@SuppressWarnings("unchecked")
public String load(HttpServletRequest request, HttpServletResponse response) throws Exception {
	JSONArray items = new JSONArray();
	org.jdom.Element el = Configuration.getJMS();
	if (el != null) {
		List children = el.getChildren();
		if (children != null && !children.isEmpty())
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				JSONObject jso = new JSONObject();
				org.jdom.Element element = (org.jdom.Element)itr.next();
				jso.put(element.getName(), element.getText());
				items.put(jso);
			}
	}
	
	JSONObject rs = new JSONObject();
	rs.put("items", items);
	rs.put("totalCount", items.length());
	
	response.setContentType("text/json; charset=utf-8");
	return rs.toString();
}

public String save(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String data = request.getParameter("data");
	String[] dataArray = data.split("&");
	JSONArray items = new JSONArray();
	
	if (dataArray.length > 0)
	for (String tmp : dataArray) {
		JSONObject jso = new JSONObject();
		String key = tmp.split("=")[0];
		String value = tmp.split("=")[1];
		jso.put(key, value);
		items.put(jso);
	}
	
	Configuration.setJMS(items);
	response.setContentType("text/plain; charset=utf-8");
	return "true";
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