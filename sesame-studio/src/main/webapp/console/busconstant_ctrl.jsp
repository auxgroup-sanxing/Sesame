<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="org.jdom.Element"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.jdom.xpath.*" %>
<%@page import="com.sanxing.studio.utils.CommonUtil"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.jdom.Document"%>
<%@page import="com.sanxing.sesame.component.params.AppParameters"%>
<%@page import="com.sanxing.sesame.component.params.Parameter"%>
<%@page language="java" contentType="text/xml; charset=utf-8" pageEncoding="utf-8"%>
<%! 
private final Logger logger = LoggerFactory.getLogger(this.getClass());

public String loadAppParameters(HttpServletRequest request, HttpServletResponse response) throws Exception {
	JSONArray items = new JSONArray();
	
	AppParameters appParams = AppParameters.getInstance();
	List<String> paramsList = appParams.getAppParamKeys();
	
	if (paramsList != null && !paramsList.isEmpty()) 
	for (Iterator<String> itr = paramsList.iterator(); itr.hasNext();) {
		JSONObject jso = new JSONObject();
		String paramName = (String)itr.next();
		Parameter param = appParams.getAppParamter(paramName);
		jso.put("name", param.getName());
		jso.put("value", param.getValue());
		jso.put("type", param.getType());
		jso.put("comment", param.getComment());
		items.put(jso);
	}
	
	JSONObject rs = new JSONObject();
	rs.put("items", items);
	rs.put("count", items.length());
	
	response.setContentType("text/json; charset=utf-8");
	return rs.toString();
}

public String loadSuParameters(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String suName = request.getParameter("suName");
	JSONArray items = new JSONArray();
	AppParameters appParams = AppParameters.getInstance();
	List<String> paramsList = appParams.getSuParamKeys(suName);
	
	if (paramsList != null && !paramsList.isEmpty())
		for (Iterator<String> itr = paramsList.iterator(); itr.hasNext();) {
			JSONObject jso = new JSONObject();
			String paramName = (String)itr.next();
			Parameter param = appParams.getSUParamter(suName, paramName);
			jso.put("name", param.getName());
			jso.put("value", param.getValue());
			jso.put("type", param.getType());
			jso.put("comment", param.getComment());
			items.put(jso);
		}
	
	JSONObject rs = new JSONObject();
	rs.put("items", items);
	rs.put("count", items.length());
	
	response.setContentType("text/json; charset=utf-8");
	return rs.toString();
}

public String loadOperaParameters(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String suName = request.getParameter("suName");
	String operaName = request.getParameter("operaName");
	JSONArray items = new JSONArray();
	AppParameters appParams = AppParameters.getInstance();
	List<String> paramsList = appParams.getOperationParamKeys(suName, operaName);
	if (paramsList != null && !paramsList.isEmpty()) 
		for (Iterator<String> itr = paramsList.iterator(); itr.hasNext();) {
			JSONObject jso = new JSONObject();
			String paramName = (String)itr.next();
			Parameter param = appParams.getOperationParamter(suName, operaName, paramName);
			jso.put("name", param.getName());
			jso.put("value", param.getValue());
			jso.put("type", param.getType());
			jso.put("comment", param.getComment());
			items.put(jso);
		}
	
	JSONObject rs = new JSONObject();
	rs.put("items", items);
	rs.put("count", items.length());
	
	response.setContentType("text/json; charset=utf-8");
	return rs.toString();
}


public String saveAppParameters(HttpServletRequest request, HttpServletResponse response) throws Exception {
	AppParameters appParams = AppParameters.getInstance();
	String dataStr = request.getParameter("data");
	if (!"".equals(dataStr)) {
		JSONArray data = new JSONArray(dataStr);
		for (int i=0; i< data.length(); i++) {
			JSONObject jso = data.getJSONObject(i);
			String name = jso.optString("name");
			String value = jso.optString("value");
			String comment = jso.optString("comment");
			
			String type = jso.optString("type");
			appParams.setParameter(name, value, Parameter.PARAMTYPE.valueOf(type), comment);
		}
	}
	
	appParams.store();
	response.setContentType("text/plain; charset=utf-8");
	return "success";
}

public String saveSuParameters(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String suName = request.getParameter("suName");
	
	AppParameters appParams = AppParameters.getInstance();
	String dataStr = request.getParameter("data");
	if (!"".equals(dataStr)) {
		JSONArray data = new JSONArray(dataStr);
		for (int i=0; i< data.length(); i++) {
			JSONObject jso = data.getJSONObject(i);
			String name = jso.optString("name");
			String value = jso.optString("value");
			String comment = jso.optString("comment");
			
			String type = jso.optString("type");
			appParams.setParameter(suName, name, value, Parameter.PARAMTYPE.valueOf(type), comment);
		}
	}
	
	appParams.store();
	response.setContentType("text/plain; charset=utf-8");
	return "success";
}

public String saveOperaParameters(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String suName = request.getParameter("suName");
	String operaName = request.getParameter("operaName");
	
	AppParameters appParams = AppParameters.getInstance();
	String dataStr = request.getParameter("data");
	if (!"".equals(dataStr)) {
		JSONArray data = new JSONArray(dataStr);
		for (int i=0; i< data.length(); i++) {
			JSONObject jso = data.getJSONObject(i);
			String name = jso.optString("name");
			String value = jso.optString("value");
			String comment = jso.optString("comment");
			
			String type = jso.optString("type");
			appParams.setParameter(suName, operaName, name, value, Parameter.PARAMTYPE.valueOf(type), comment);
		}
	}
	
	appParams.store();
	response.setContentType("text/plain; charset=utf-8");
	return "success";
}

public String removeAppParameter(HttpServletRequest request, HttpServletResponse response) throws Exception {
	AppParameters appParams = AppParameters.getInstance();
	String paramName = request.getParameter("name");
	appParams.remove(paramName);
	appParams.store();
	response.setContentType("text/plain; charset=utf-8");
	return "success";
}

public String removeSuParameter(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String suName = request.getParameter("suName");
	
	AppParameters appParams = AppParameters.getInstance();
	String paramName = request.getParameter("name");
	appParams.remove(suName, paramName);
	appParams.store();
	response.setContentType("text/plain; charset=utf-8");
	return "success";
}

public String removeOperaParameter(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String suName = request.getParameter("suName");
	String operaName = request.getParameter("operaName");
	
	AppParameters appParams = AppParameters.getInstance();
	String paramName = request.getParameter("name");
	appParams.remove(suName, operaName, paramName);
	appParams.store();
	response.setContentType("text/plain; charset=utf-8");
	return "success";
}
%>

<%
String operation = request.getParameter("operation");
WebServletResponse responseWrapper = new WebServletResponse(response);

if (operation != null) {
	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[]{HttpServletRequest.class, HttpServletResponse.class});
		String result = (String)method.invoke(this, new Object[]{new WebServletRequest(request), responseWrapper});
		out.clear();
		out.println(result);
	} catch (NoSuchMethodException e) {
		responseWrapper.sendError("["+request.getMethod()+"]找不到相应的方法来处理指定的 operation: "+operation);
	} catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		if (!(t instanceof FileNotFoundException))
			logger.error("", t);
		responseWrapper.sendError(t.getMessage());
	} catch (Exception e) {
		logger.error("", e);
		responseWrapper.sendError(e.getMessage());
	}
} else {
	responseWrapper.sendError("["+request.getMethod()+"]拒绝执行，没有指定 operation 参数");
}
%>