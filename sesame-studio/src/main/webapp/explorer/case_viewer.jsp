<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="org.json.*"%>
<%@page
	import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.XPath"%>

<%
String unit = request.getParameter("unit");
unit = unit!=null ? new String(unit.getBytes("iso-8859-1"), "utf-8") : null;

String oriented=null, endpoint=null, schema=null;

JSONArray items = new JSONArray();
JSONArray cases = new JSONArray();
try
{
	oriented = request.getParameter("oriented");
	endpoint = request.getParameter("endpoint");
	schema = request.getParameter("schema");
	File unitFolder = Configuration.getWorkspaceFile(unit);
	SAXBuilder builder = JdomUtil.newSAXBuilder();
	
	File epFolder = new File(unitFolder, "../../"+oriented+"/"+endpoint);
	File schemaFile = new File(epFolder, schema);
	if (schemaFile.exists()) {
		Element rootEl = builder.build(schemaFile).getRootElement();
		String condition = oriented.equals("client")?"@name='response'":"@name";
		XPath xpath = XPath.newInstance("xsd:element["+condition+"]");
		xpath.addNamespace("xsd", rootEl.getNamespace().getURI());
		List<?> list = xpath.selectNodes(rootEl);

		for (Iterator<?> iter = list.iterator(); iter.hasNext(); ) {
			Element el = (Element) iter.next();
			String name = el.getAttributeValue("name");
			JSONObject json = new JSONObject();
			json.put("tag", el.getName());
			json.put("name", name);
			json.put("stateful", false);
			SchemaUtil.generateUI(el, json);
			json.put("title", name.equals("request")?"请求消息":name.equals("response")?"响应消息":"故障消息");
			json.put("bodyStyle", "padding:5px");
			items.put(json);
		}
	}
	File caseFile = new File(unitFolder, schema.replace(".xsd", ".xml"));
	if (caseFile.exists()) {
		try {
			Element rootEl = builder.build(caseFile).getRootElement();
			List<?> list = rootEl.getChildren();
			for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
				Element el = (Element)iter.next();
				JSONObject json = new JSONObject();
				json.put("name", el.getAttributeValue("name"));
				json.put("documentation", el.getAttributeValue("documentation"));
				cases.put(json);
			}
		}
		catch (Exception e) {
		}
	}
	out.clear();
}
catch (Throwable e)
{
	WebUtil.sendError(response, e.getMessage());
	getServletContext().log(e.getMessage(), e);
}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

<title>外部系统</title>

<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<!--link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/xtheme-gray.css" /-->
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/source/locale/ext-lang-zh_CN.js"></script>

<script type="text/javascript" src="../package/xml/xdom.js"></script>

<style type="text/css">
.x-combo-list-inner {
	left: 0px !important;
	top: 0px !important;
	width: auto !important;
}
</style>
</head>

<body>
	<script type="text/javascript">
   	//if (window.top && window.top.Application)
	{
		//Application = window.top.Application;
		//debug = Application.debug;
		var el = document.createElement("SCRIPT");
		el.setAttribute("type", "text/javascript");
		el.setAttribute("src", "case_viewer.js");
		document.body.appendChild(el);
	}
	//else
	//{
	//	document.location.href='../error404.jsp';
	//}
	var unit = '<%=unit%>';
	var oriented = '<%=oriented%>';
	var endpoint = '<%=endpoint%>';
	var schema = '<%=schema%>';
	var properties = <%=items%>;
	var cases = <%=cases%>;
	
	function CreateRegex(item){
		if (item.regex) item.regex = new RegExp(item.regex);
		if (item.items) Ext.each(item.items, CreateRegex);
	}
	Ext.each(properties, CreateRegex);
	
</script>
</body>
</html>


