<%@page language="java" contentType="text/javascript; charset=utf-8"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.sesame.transport.Protocols"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.json.*"%>
<%@page import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.XPath"%>
<%!
private final Logger logger = LoggerFactory.getLogger(this.getClass());

%>

<%
request = new WebServletRequest(request);
String script = "component_viewer/common_v.js";
String component = request.getParameter("component");
String type = request.getParameter("type");
File compBundle = Application.getWarehouseFile(component);

try
{
	BufferedReader reader = null;
	if (compBundle.isFile()) {
		JarFile compJar = new JarFile(compBundle);
		try {
			//获取部署描述符
			JarEntry jbiXml = compJar.getJarEntry("component.js");
			if (jbiXml != null) {
				InputStream input = compJar.getInputStream(jbiXml);
				reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
				out.clear();
				for (String line; (line=reader.readLine())!=null; ) {
					out.println(line);
				}
				return;
			}
			else {
			}
		}
		finally {
			compJar.close();
		}
	}
	else {
		File file = new File(compBundle, "component.js");
		if (file.exists()) {
			FileInputStream input = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			try {
				out.clear();
				for (String line; (line=reader.readLine())!=null; ) {
					out.println(line);
				}
				return;
			}
			finally {
				reader.close();
			}
		}
	}
	
	String path = "explorer/component_viewer/"+
		(type!=null && type.equals("binding-component") ? "custom_bc_v.js" : "common_v.js");
	File file = new File(getServletContext().getRealPath(path));
	if (file.exists()) {
		FileInputStream input = new FileInputStream(file);
		reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
		try {
			out.clear();
			for (String line; (line=reader.readLine())!=null; ) {
				out.println(line);
			}
		}
		finally {
			reader.close();
		}
	}
}
catch (Exception e)
{
	WebUtil.sendError(response, e.getMessage());
	logger.error(e.getMessage(), e);
}
%>