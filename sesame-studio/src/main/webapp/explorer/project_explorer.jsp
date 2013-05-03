<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.regex.*"%>
<%@page import="org.json.*"%>
<%@page import="org.jdom.*, org.jdom.input.*, org.jdom.output.*"%>
<%

out.clear();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>项目查看器</title>
<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<link rel="stylesheet" type="text/css" href="object_icon16.css" />

<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>

<style type="text/css">
li.x-project > .x-tree-node-el {
	background-image: url(../images/head_bg_deactive.gif);
	border: 1px solid gray;
	margin-top: 1px;
}

li.x-project > .x-tree-selected {
	background-image: url(../images/head_bg_active.gif);
}

.repos-icon {
	background-image: url('../images/obj16/repo_rep.gif') !important;
}

.upload-icon {
	background-image: url('../images/tool16/image_add.png') !important;
}
</style>
</head>

<body style="height: 100%">
<script type="text/javascript" src="../package/upload/FileUploadField.js"></script>
<script type="text/javascript" src="../package/codepress/codepress.js"></script>
<script type="text/javascript" src="../package/codepress/Ext.ux.codepress.js"></script>
<script type="text/javascript" src="project_explorer.js"></script>
<script type="text/javascript" src="version_dialog.js"></script>

<script type="text/javascript">
   	if (window.top && window.top.Application)
   	{
		Application = window.top.Application;
		debug = Application.debug;
	}

</script>

</body>
</html>