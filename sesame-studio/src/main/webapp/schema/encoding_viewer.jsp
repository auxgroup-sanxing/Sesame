<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page import="java.util.*,  java.io.*"%>
<%@page
	import="com.sanxing.studio.*,com.sanxing.studio.utils.*,com.sanxing.studio.team.svn.*,com.sanxing.studio.team.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="org.json.*"%>
<%!
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
%>
<%
request = new WebServletRequest(request);
String schema = request.getParameter("schema");
String ref = request.getParameter("ref");
String project = schema.substring(0, schema.indexOf('/'));
String unit = schema.substring(0, schema.lastIndexOf('/'));
String unitId = request.getParameter("unitId");
String unitDesc = request.getParameter("unitDesc") == null ? "" : request.getParameter("unitDesc");
String projectDesc = request.getParameter("projectDesc") == null ? "" : request.getParameter("projectDesc");
String isVersioned = "true";
String isLocked = "false";
try
{
	File xsdFile = Configuration.getWorkspaceFile(schema);
	File unitFolder = xsdFile.getParentFile();
	
	unitId = unitFolder.getName();
	
	JSONObject user = Authentication.getCurrentUser();
	String userName = user.optString("userid");
	
	ThreeWaySynchronizer sync = SCM.getSynchronizer(unitFolder.getParentFile().getParentFile());
	if(null == sync || (!sync.isVersioned(xsdFile))){
		isVersioned = "false";
	} else {
		File wsdlFile = new File(unitFolder, "unit.wsdl");
		if (sync != null && sync.isVersioned(wsdlFile) && sync.isVersioned(xsdFile)) {
			Map<String, ?> props = sync.info(wsdlFile);
			String lock = (String)props.get("lock");
			
			if (lock == null) {
				props = sync.info(xsdFile);
				lock = (String) props.get("lock");
				if (lock != null) {
					if (userName.equals((String) props.get("lock.owner")))
						isLocked = "true";
				}
			} else {	
				if (userName.equals((String) props.get("lock.owner")))
					isLocked = "true";
			}
		}
		/*
		Map props = sync.info(xsdFile);
		String lock = (String)props.get("lock");
		if (lock != null) {
			JSONObject user = Authentication.getCurrentUser();
			String userName = user.optString("userid");
			if(userName.equals((String)props.get("lock.owner")))
				isLocked = "true";
		}
		*/
	}
	out.clear();

}
catch (Exception e)
{
	new WebServletResponse(response).sendError(e.getMessage());
	logger.debug(e.getMessage(), e);
}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<title>消息编辑器</title>
<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<style type="text/css">
html,body {
	font: normal 12px arial, verdana;
	margin: 0;
	padding: 0;
	border: 0 none;
	overflow: auto;
	cursor: default;
	width: 100%;
	height: 100%;
}

.x-view-item {
	padding: 2px;
	white-space: nowrap;
	text-overflow: ellipsis;
	-o-text-overflow: ellipsis;
	overflow: hidden;
}

.x-view-selected {
	background: #DFEDFF;
	border: 1px solid #6593cf;
	padding: 1px;
}

.x-form-item {
	white-space: nowrap;
}

.x-form-field-wrap {
	left: 0px !important;
	top: 0px !important;
}

.x-form-check-wrap {
	left: 0px !important;
	top: 0px !important;
	height: 22px !important;
	width: 100% !important;
}

.x-form-text {
	height: 22px !important;
}

.x-column-inner {
	height: 100%;
}

.x-restrict-tool {
	
}

.x-combo-list-inner {
	left: 0px !important;
	top: 0px !important;
	width: auto !important;
}

.x-label {
	height: auto !important;
	width: auto !important;
}

.x-data-view {
	background-color: white;
	border: 1px solid #B5B8C8;
	cursor: default;
	width: auto !important;
	height: 150px !important;
	overflow: auto;
}

.settings {
	background-image: url("../images/elcl16/cog_go.png") !important;
}
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>

<script type="text/javascript" src="../package/schema/SchemaNodeUI.js"></script>
<script type="text/javascript" src="../package/dialog/dialog.js"></script>
<script type="text/javascript" src="../package/xml/xdom.js"></script>

<script type="text/javascript" src="encoding_viewer.js"></script>
</head>
<body>
	<script type="text/javascript">
	//document.oncontextmenu = function(e) { return false; };
	var isVersioned = '<%=isVersioned%>';
	var isLocked = '<%=isLocked%>';
	var schema = '<%=schema%>';
	var project = '<%=project%>';
	var ref = '<%=ref%>';
	var unit = '<%=unit%>';
	var unitId = '<%=unitId%>';
	var unitDesc = '<%=URLDecoder.decode(unitDesc, "UTF-8")%>';
	var projectDesc = '<%=URLDecoder.decode(projectDesc, "UTF-8")%>';
	var targetNamespace = '';
	
   	if (window.top && window.top.Application)
   	{
		Application = window.top.Application;
		debug = Application.debug;
	}
</script>
</body>
</html>
