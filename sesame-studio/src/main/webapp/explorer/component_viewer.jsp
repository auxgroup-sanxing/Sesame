<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.sesame.transport.Protocols"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.json.*"%>
<%@page
	import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.XPath"%>
<%!
private final Logger logger = LoggerFactory.getLogger(this.getClass());

private String getDocumentation(Element element)
{
	Element annoEl = element.getChild("annotation", element.getNamespace());
	if (annoEl==null) return "";
	return annoEl.getChildText("documentation", element.getNamespace());
}

private void removeNamespace(Element element) {
	element.setNamespace(Namespace.NO_NAMESPACE);
	for (Iterator<?> iter=element.getDescendants(); iter.hasNext(); ) {
		Object obj = iter.next();
		if (obj instanceof Element) {
			((Element)obj).setNamespace(Namespace.NO_NAMESPACE);
		}
	}
}

%>

<%
request = new WebServletRequest(request);
String script = "component_viewer/common_v.js";
String component = request.getParameter("component");
File compBundle = Application.getWarehouseFile(component);

JSONObject properties = new JSONObject();

JSONArray protocols = new JSONArray();
String[] array = Protocols.getStandards();
for (String scheme : array) {
	protocols.put(scheme);
}
try
{
	SAXBuilder builder = JdomUtil.newSAXBuilder();
	Document document;
	if (compBundle.isFile()) {
		properties.put("readOnly", true);
		JarFile compJar = new JarFile(compBundle);
		try {
			//获取部署描述符
			JarEntry jbiXml = compJar.getJarEntry("jbi.xml");
			if (jbiXml != null) {
				InputStream input = compJar.getInputStream(jbiXml);
				document = builder.build(input);
			}
			else {
				document = new Document(new Element("jbi", Namespace.getNamespace(Namespaces.JBI)));
			}
		}
		finally {
			compJar.close();
		}
	}
	else {
		properties.put("readOnly", false);
		document = builder.build(new File(compBundle, "jbi.xml"));
	}
	
	Element rootEl = document.getRootElement();
	Namespace xmlns_jbi = rootEl.getNamespace();
	Namespace xmlns_art = Namespace.getNamespace("sn", Namespaces.SESAME);
	Element compEl = rootEl.getChild("component", rootEl.getNamespace());
	if (compEl != null) {
		String componentType = compEl.getAttributeValue("type", "");
	
		script = "component_viewer/view_script.jsp?component="+component+"&type="+componentType;
		properties.put("type", componentType);
		Element idenEl = compEl.getChild("identification", rootEl.getNamespace());
		properties.put("name", idenEl.getChildText("name", rootEl.getNamespace()));
		properties.put("description", idenEl.getChildText("description", rootEl.getNamespace()));
		
		String componentClass = compEl.getChildText("component-class-name", xmlns_jbi);
		if (componentClass!=null) {
			if (componentClass.equals("com.sanxing.sesame.binding.AdapterComponent"))
				script = "component_viewer/adapter_bc_v.js";
			else if (componentClass.endsWith("ProcessEngine"))
				script = "component_viewer/process_ec_v.js";
			else if (componentClass.equals("com.sanxing.sesame.binding.unionpay.UnionpayComponent"))
				script = "component_viewer/unionpay_bc_v.js";
			else if (componentClass.equals("com.sanxing.adp.ADPEngine"))
				script = "component_viewer/adp_ec_v.js";
		}
		properties.put("component-classname", componentClass);
		properties.put("component-classpath", new JSONArray());
		Element classpathEl = compEl.getChild("component-class-path", xmlns_jbi);
		if (classpathEl != null) {
			List<Element> list = classpathEl.getChildren("path-element", xmlns_jbi);
			for (Element pathEl : list) {
				properties.append("component-classpath", pathEl.getTextTrim());
			}
		}
		String bootstrapClass = compEl.getChildText("bootstrap-class-name", xmlns_jbi);
		properties.put("bootstrap-classname", bootstrapClass);
		properties.put("bootstrap-classpath", new JSONArray());
		classpathEl = compEl.getChild("bootstrap-class-path", xmlns_jbi);
		if (classpathEl != null) {
			List<Element> list = classpathEl.getChildren("path-element", xmlns_jbi);
			for (Element pathEl : list) {
				properties.append("bootstrap-classpath", pathEl.getTextTrim());
			}
		}
		properties.put("shared-library", new JSONArray());
		List<Element> list = compEl.getChildren("shared-library", xmlns_jbi);
		for (Element libEl : list) {
			JSONObject obj = new JSONObject();
			obj.put("lib-name", libEl.getTextNormalize());
			obj.put("version", libEl.getAttributeValue("version"));
			properties.append("shared-library", obj);
		}
		
	}
	
	out.clear();
}
catch (Exception e)
{
	WebUtil.sendError(response, e.getMessage());
	logger.error(e.getMessage(), e);
}

%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Expires" content="0">
<title>通讯绑定查看器</title>
<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/xtheme-gray.css" />
<style type="text/css">
.x-combo-list-inner {
	text-align: left;
}

.item-wrap {
	text-align: center;
	float: left;
	cursor: pointer;
	margin-top: 4px;
	margin-left: 4px;
	padding: 2px 7px !important;
	font: 13px Arial, Helvetica, "Nimbus Sans L", sans-serif;
}

.displayNameWrap {
	
}

.fullNameWrap,.sizeWarp,.timeWarp {
	display: none
}

.hilited {
	background: #EFEFEF;
	color: black;
}

.x-icon-loading {
	background-image:
		url('../ext-Ajax/resources/images/default/grid/loading.gif');
	background-position: center;
	background-repeat: no-repeat;
}
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
</head>

<body>

	<script type="text/javascript" src="../package/xml/xdom.js"></script>
	<script type="text/javascript" src="../package/uuid.js"></script>
	<script type="text/javascript" src="<%=script%>"></script>

	<script type="text/javascript">

	var component = "<%=component%>";
	var properties = <%=properties%>;
	var protocols = <%=protocols%>;
	
</script>

	<script type="text/javascript">
	<%if (!script.startsWith("http://")) {%>
		<%if(component.indexOf("adp-ec") != -1) {%>
			var scriptEl = document.createElement("SCRIPT");
			scriptEl.setAttribute("type", "text/javascript");
			scriptEl.setAttribute("src", "../package/upload/UploadDialog.js");
			document.body.appendChild(scriptEl);

			var linkEl = document.createElement("LINK");
			linkEl.setAttribute("rel", "stylesheet");
			linkEl.setAttribute("type", "text/css");
			linkEl.setAttribute("href", "../package/upload/resources/UploadDialog.css");
			document.getElementsByTagName("head")[0].appendChild(linkEl);
		<%}%>
	<%}%>
</script>

</body>
</html>