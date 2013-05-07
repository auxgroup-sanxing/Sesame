<%@page language="java" contentType="text/html; charset=utf-8"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>数据库编辑器</title>
<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<style type="text/css">
.x-btn-text {
	font-size: 12px !important;
}

.x-layout-panel-center p {
	margin: 5px;
}

.x-menu-item {
	font-size: 12px;
}
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/ux/SQLPanel.js"></script>
<script type="text/javascript" src="dbmanager_viewer.js"></script>
</head>

<body style="height: 100%">
	<script type="text/javascript">
   	if (window.top && window.top.Application)	{
		Application = window.top.Application;
		debug = Application.debug;
	}
</script>
</body>
</html>