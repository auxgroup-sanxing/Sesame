<%@page language="java" contentType="text/html; charset=utf-8"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>集群管理</title>
<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<style type="text/css">
.x-btn-text {
    font-size: 12px !important;
}
.x-layout-panel-center p {
    margin:5px;
}
.x-menu-item {
	font-size: 12px;
}
.icon-folder {
	background-image:url(resources/icons/closedFolder.gif);
}
.item-wrap {
	float: left;
	cursor: pointer;
	margin-top: 4px;
	margin-left: 4px;
	font: 13px Arial,Helvetica,"Nimbus Sans L",sans-serif;
	cursor:pointer;
	padding:10px;
	white-space:nowrap;
	border: #99BBE8 solid 1px;
	-moz-border-radius: 3px;
}
.hilited {
	background: #D5E2F2; 
	color: highlighttext;
}

.x-tree-node-leaf.run{
	background-image:url(../images/icons/run_exc.gif) !important
}
.run{
	background-image:url(../images/icons/run_exc.gif) !important
}
.x-tree-node-leaf.stop{
	background-image:url(../images/icons/finish.gif) !important
}

.serverWrap {
	float:left;
	margin-left:40px;
	width: 30px;
	height: 30px;
}

.x-icon-server {
	background-image:url(../images/obj16/server.gif) !important;
	background-position: center !important;
	background-repeat: no-repeat !important;
}

.x-icon-com {
	background-image:url(../images/obj16/component.gif) !important;
	background-position: center !important;
	background-repeat: no-repeat !important;
}

.x-icon-sa {
	background-image:url(../images/obj16/theme_category.gif) !important;
	background-position: center !important;
	background-repeat: no-repeat !important;
}

.x-icon-su {
	background-image:url(../images/obj16/wsdl_file.gif) !important;
	background-position: center !important;
	background-repeat: no-repeat !important;
}

.x-icon-endpoint {
	background-image:url(../images/obj16/port.gif) !important;
	background-position: center !important;
	background-repeat: no-repeat !important;
}

</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../ext-Ajax/adapter/jquery/jquery.min.js"></script>
<!-- <script type="text/javascript" src="../ext-Ajax/adapter/highcharts/ext-highcharts-adapter.js"></script> -->
<script type="" src ="../package/ux/LongPollingProvider.js"></script>
<script type="" src ="../package/chart/highcharts.js"></script>
<script type="text/javascript" src="cluster_viewer.js"></script>
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