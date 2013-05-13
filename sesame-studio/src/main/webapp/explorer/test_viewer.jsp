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

String oriented=null, endpoint=null, emulator=null;

JSONArray items = new JSONArray();
try
{
	File unitFolder = Configuration.getWorkspaceFile(unit);
	SAXBuilder builder = JdomUtil.newSAXBuilder();
	Document doc = builder.build(new File(unitFolder, "unit.xml"));
	Element rootEl = doc.getRootElement();
	oriented = rootEl.getAttributeValue("oriented");
	endpoint = rootEl.getAttributeValue("endpoint");
	
	File epFolder = new File(unitFolder, "../../"+oriented+"/"+endpoint);
	rootEl = builder.build(new File(epFolder, "unit.xml")).getRootElement();
	Element propertiesEl = rootEl.getChild("properties");
	String component;
	if (propertiesEl!=null && (component=propertiesEl.getAttributeValue("component"))!=null) {
		File compFile = new File(unitFolder.getParent(), component);
		if (!compFile.exists()) {
			throw new Exception("找不到指定的组件包: "+compFile.getCanonicalPath());
		}
		JarFile compJar = new JarFile(compFile);
		JarEntry entry = compJar.getJarEntry("unit.xsd");
		try {
			if (entry != null) {
				InputStream input = compJar.getInputStream(entry);
				Document xsd = builder.build(input);
				rootEl = xsd.getRootElement();
				Element firstEl = rootEl.getChild("element", rootEl.getNamespace());
				if (firstEl != null) {
					JSONObject json = new JSONObject();
					json.put("tag", firstEl.getName());
					json.put("name", firstEl.getAttributeValue("name"));
					json.put("stateful", false);
					SchemaUtil.generateUI(firstEl, json);
					json.put("title", oriented.equals("client")?" 客户端参数":"服务端参数");
					items.put(json);
				}
			}
		}
		finally {
			compJar.close();
		}
	}
	out.clear();
}
catch (Exception e)
{
	WebUtil.sendError(response, e.getMessage());
	e.printStackTrace();
}
//获取项目的(目标命名空间)
//String project =  unit.substring(0, unit.indexOf("/"));
//String targetUri = OptionUtil.getNamespaceUri(project);

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
   	if (window.top && window.top.Application)
	{
		Application = window.top.Application;
		debug = Application.debug;
		var el = document.createElement("SCRIPT");
		el.setAttribute("type", "text/javascript");
		el.setAttribute("src", "test_viewer.js");
		document.body.appendChild(el);
	}
	else
	{
		document.location.href='../error404.jsp';
	}
	var unit = '<%=unit%>';
	var oriented = '<%=oriented%>';
	var endpoint = '<%=endpoint%>';
	var properties = <%=items%>;
	
	function CreateRegex(item){
		if (item.regex) item.regex = new RegExp(item.regex);
		if (item.items) Ext.each(item.items, CreateRegex);
	}
	Ext.each(properties, CreateRegex);
	
</script>
</body>
</html>


