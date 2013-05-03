<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@page import="java.util.*, java.io.*"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*,com.sanxing.studio.team.svn.*,com.sanxing.studio.team.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%!
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
%>
<%
String schema = request.getParameter("schema");
schema = schema!=null ? new String(schema.getBytes("iso8859-1"), "utf-8") : null;
String project = schema.substring(0, schema.indexOf('/'));
String tns = "";
String isVersioned = "true";
try {
	tns = PrefsUtil.getNamespaceUri(project);
	
	File xsdFile = new File(Configuration.getWorkspaceRoot(),schema);
	File projectFolder = xsdFile.getParentFile().getParentFile();
	ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
	if((null == sync) ||(!sync.isVersioned(xsdFile))){
		isVersioned = "false";
	}
	out.clear();
}
catch (Exception e) {
	new WebServletResponse(response).sendError(e.getMessage());
	logger.debug(e.getMessage(), e);
}

%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>模型编辑器</title>
<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<style type="text/css">
html, body {
font: normal 12px arial, verdana;
	margin:0;
	padding:0;
	border: 0 none;
	overflow: auto;
	cursor:default; 
	width:100%; 
	height:100%;
}

.x-view-item {
    padding: 2px; 
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
	background-color:white; 
	border:1px solid #B5B8C8; 
	cursor:default;
	width: auto !important;
	height: 150px !important;
	overflow:auto; 
}
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>

<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/schema/SchemaNodeUI.js"></script>
<script type="text/javascript" src="../package/xml/xdom.js"></script>
<script type="text/javascript" src="../package/dialog/dialog.js"></script>

<script type="text/javascript" src="schema_viewer.js"></script>
<script type="text/javascript">
	document.oncontextmenu = function(e) { return false; };
	var schema = '<%=schema%>';
	var isVersioned = '<%=isVersioned%>';
	var targetNamespace = '<%=tns%>';
	
	var debug = function(obj, showMethods, sort){
		var div = Ext.DomHelper.append("status", {tag: 'div', html: '<b>'+obj+'</b>', style:'border-bottom:1px solid gray;'});
		showMethods = (showMethods != false);
		sort = (sort != false);
		if (obj == null || obj.constructor == null) {
			return true;
		}
		var type = typeof(obj);
		if (type)
		{
			if (type == "string" || type == "number") {
				div.innerHTML = type+': '+obj;
				return true;
			}
			if (showMethods && !sort) 
			{
			}
			else 
			{
				var propNames = [];
				if (showMethods) {
					for (var prop in obj) {
						propNames.push(prop);
					}
				}
				else 
				{
					for (var prop in obj) {
						if (typeof obj[prop] != "function") 
						{
							propNames.push(prop);
						}
						else 
						{
							//Ext.DomHelper.append(div, {tag:"div", html: '<font color=blue>'+prop+'</font>: Method'});
						}
					}
				}
				if (sort) {
					propNames.sort();
				}
				Ext.each(propNames, function (prop) {
					Ext.DomHelper.append(div, {tag:"div", html: '<font color=blue>'+prop+'</font>: '+obj[prop]});
				});
				return true;
			}
		}
		for (elem in obj)
		{
			Ext.DomHelper.append(div, {tag:"div", html: '<font color=blue>'+elem+'</font>: '+obj[elem]});
		}
	};

</script>
</head>
<body>
</body>
</html>
