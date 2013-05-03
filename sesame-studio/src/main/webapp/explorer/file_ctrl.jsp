<%@page language="java" contentType="text/xml; charset=utf-8" pageEncoding="utf-8"%>

<%@page import="java.io.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.util.*,java.util.jar.*,java.util.regex.*"%>

<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.studio.team.*"%>

<%@page import="org.json.*"%>

<%!
private String getProject(String path) {
	int n = path.indexOf("/");
	return n > 0 ? path.substring(0, n) : path;
}

public String load(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String filepath = request.getParameter("file");

	File file = Configuration.getWorkspaceFile(filepath);
    StringBuffer buffer = new StringBuffer();
	if (file.exists()) {
		if (file.length() > 1024 * 1024) {
			throw new IllegalDataException("载入失败，文件太大");
		}
		else {
			String agent = request.getHeader("User-Agent");
			String separator="\n";
			if (agent.contains("Windows")) {
				separator = "\r\n";
			}
			else if (agent.contains("Mac")) {
				separator = "\r";
			}
			
			InputStream input = new FileInputStream(file);
			try {
			    String line;
		        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
		        while ((line = reader.readLine()) != null) {
		            buffer.append(line);
		            buffer.append(separator);
		        }
			}
	        finally {
	        	input.close();
	        }
		}
	}
	else {
		throw new FileNotFoundException("文件没找到: "+file);
	}
	
	return buffer.toString();
}


public String save(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String filepath = request.getParameter("file");

	File file = Configuration.getWorkspaceFile(filepath);
    StringBuffer buffer = new StringBuffer();

	if (!file.getParentFile().exists()) {
		throw new FileNotFoundException("目录不存在: "+file.getParent());
	}
	
	String agent = request.getHeader("User-Agent");
	String separator="\n";
	if (agent.contains("Windows")) {
		separator = "\r\n";
	}
	else if (agent.contains("Mac")) {
		separator = "\r";
	}
	
	String content = request.getParameter("content");
	
	OutputStream output = new FileOutputStream(file);
	try {
	    //String line;
        //BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        output.write(content.getBytes());
	}
    finally {
		output.close();
    }
	
	String project = this.getProject(filepath);
	ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(project);
	
	JSONObject result = new JSONObject();
	if (synchronizer != null) {
		Map prop = synchronizer.status(file);
		result.put("revision", prop.get("revision"));
		result.put("author", prop.get("author"));
		result.put("status", prop.get("status"));
	}
	result.put("success", true);
	return result.toString();
}

public String post(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	
	return "true";
}
%>

<%
	String operation = request.getParameter("operation");

	if (operation == null) {
		operation = request.getMethod().toLowerCase();
	}
	
	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[] { HttpServletRequest.class,
				HttpServletResponse.class });
		String result = (String) method.invoke(this, new Object[] { new WebServletRequest(request),
				new WebServletResponse(response) });
		if (result != null) {
			out.clear();
			out.println(result);
		}
	}
	catch (NoSuchMethodException e) {
		throw new ServletException("[" + request.getMethod() + "]找不到相应的方法来处理指定的 operation: " + operation);
	}
	catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		throw new ServletException(t.getMessage(), t);
	}
	catch (Exception e) {
		throw new ServletException(e.getMessage(), e);
	}
%>