<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="com.sanxing.ads.*,com.sanxing.ads.utils.*"%>
<%@page import="com.sanxing.ads.team.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.regex.*"%>
<%@page import="org.json.*"%>
<%@page import="org.jdom.*, org.jdom.input.*, org.jdom.output.*"%>
<%
request = new WebServletRequest(request);

String project = request.getParameter("project");
String projectDesc = request.getParameter("projectDesc");
String namespace = PrefsUtil.getNamespaceUri(project);

File location = Application.getWorkspaceFile(project);

boolean isVersioned = SCM.isVersioned(location);

out.clear();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>项目查看器</title>
<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<link rel="stylesheet" type="text/css" href="project_viewer/viewer.css">
<link rel="stylesheet" type="text/css" href="project_viewer/sview2.css">
<link rel="stylesheet" type="text/css" href="object_icon16.css" />

<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>

<style type="text/css">

html {
	overflow: hidden;
}

.x-res-group {
    padding-top:6px;
	-moz-user-select:none;
	-khtml-user-select:none;
}

.x-res-group-el {
    border-bottom: 2px solid #99bbe8;
    cursor:pointer;
    font-weight:bold;
}

.x-res-group .x-tree-selected {
	background-color: #ebf7d2;
}

.x-resource {
    float:left;
    width:300px;
    height:100px;
    margin:5px 5px 5px 10px;
    cursor:pointer;
    zoom:1;
}

.x-resource .x-tree-node-icon {
    width:120px;
    height:90px;
    margin:5px 0 0 5px;
    float:left;
}

.x-resource .x-tree-node-label {
    float:left;
    width:160px;
    margin-left:10px;
	overflow: hidden;
    text-overflow: ellipsis;
}

.x-resource .x-tree-node-label a {
    font-family:tahoma,arial,san-serif;
    color:#555;
    font-size:12px;
    font-weight:bold;
}

.x-resource .x-tree-node-label p {
    color:#777;
    white-space: normal;
}

.x-resource .x-tree-node-over {
	background: #ebf7d2 url('project_viewer/sample-over.gif') no-repeat;
}
.x-resource .x-tree-selected {
	background: #ebf7d2;
}

/* condensed view */
.condensed-view .x-resource {
    float:left;
    width:135px;
    height:114px;
    margin:5px 5px 5px 10px;
    cursor:pointer;
    zoom:1;
	text-align:center;
}
.condensed-view .x-resource .x-tree-node-icon {
    width:120px;
    height:90px;
    margin:5px 2px 0;
    float:none;
}

.condensed-view .x-resource .x-tree-node-label {
    float:none;
    width:100%;
    margin-left:0;
	overflow: hidden;
    text-overflow: ellipsis;
}

.condensed-view .x-resource .x-tree-node-label a {
    font-family:tahoma,arial,san-serif;
    color:#666;
    font-size:12px;
    font-weight:normal;
	text-align:center;
	margin:0;
	text-overflow: ellipsis;
}

.condensed-view .x-resource .x-tree-node-label p {
    display:none;
}

/* mini view */

.mini-view .x-resource {
    float:left;
    width:75px;
    height:69px;
    margin:5px 5px 5px 10px;
    cursor:pointer;
    zoom:1;
	text-align:center;
}
.mini-view .x-resource .x-tree-node-icon {
    width:60px;
    height:45px;
    margin:5px 2px 0;
    float:none;
}

.mini-view .x-resource .x-tree-node-label {
    float:none;
    width:100%;
    margin-left:0;
    overflow: hidden;
    text-overflow: ellipsis;
}

.mini-view .x-resource .x-tree-node-label a {
    font-family:tahoma,arial,san-serif;
    color:#666;
    font-size:10px;
    font-weight:normal;
	text-align:center;
	margin:0;
	white-space: nowrap;
}
.mini-view .x-resource .x-tree-node-label p {
    display:none;
}
.mini-view .x-resource dd.over {
	background:#ebf7d2;
}
.mini-view .x-resource dd.over h4 {
	color:#1860A8;
}

.upload-icon {
	background: url('../images/tool16/image_add.png') no-repeat 0 0 !important;
}

.upload-package {
	background: url('../images/tool16/package_add.png') no-repeat 0 0 !important;
}

/* 安装、部署对话框图标 */
.x-icon-com {
}
</style>
</head>

<body>
<script type="text/javascript" src="../package/upload/FileUploadField.js"></script>
<script type="text/javascript" src="project_viewer.js"></script>
<script type="text/javascript" src="project_dialog.js"></script>
<script type="text/javascript" src="version_dialog.js"></script>

<script type="text/javascript">
	document.oncontextmenu = function(e) { return false; };
	var project = "<%=project%>";
	var projectDesc = "<%=projectDesc%>";
	var targetNamespace = "<%=namespace%>";
	var isVersioned = <%=isVersioned%>;

</script>


<div id="viewport">

<div id="hd">
</div>

<div id="bd">

<!-- 
<span style="margin-top: 10px; font-size: 12px;"><a href="http://extjs.com/store/extjs">添加系统</a></span>
 -->
<div id="samples" unselectable="on" class="x-unselectable">
	<div id="samples-cb">
		<img src="project_viewer/s.gif" class="normal-view" title="平铺"> 
		<img src="project_viewer/s.gif" class="condensed-view" title="大图标">
		<img src="project_viewer/s.gif" class="mini-view" title="小图标">
	</div>
	
	<div id="sample-menu">
		<div id="sample-menu-inner"></div>
	</div>
	<div id="sample-box">
		<div id="sample-box-inner"></div>
	</div>
</div>

<div style="clear: both;"></div>
</div>
<!-- end bd -->

</div>
<!-- end viewport -->

</body>
</html>