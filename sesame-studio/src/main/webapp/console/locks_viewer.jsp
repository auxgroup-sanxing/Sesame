<%@page language="java" contentType="text/html; charset=utf-8"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>集群管理</title>
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

.icon-folder {
	background-image: url(resources/icons/closedFolder.gif);
}

.item-wrap {
	text-align: center;
	float: left;
	cursor: pointer;
	margin-top: 4px;
	margin-left: 4px;
	padding: 2px 12px 2px 7px !important;
	font: 13px Arial, Helvetica, "Nimbus Sans L", sans-serif;
}

.hilited {
	background: highlight;
	color: highlighttext;
}
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all-debug.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="locks_viewer.js"></script>
</head>

<body style="height: 100%">
	<script type="text/javascript">
   	if (window.top && window.top.Application)	{
		Application = window.top.Application;
		debug = Application.debug;
	}
	var test = function(){
			alert('te');
		};
</script>
</body>
</html>