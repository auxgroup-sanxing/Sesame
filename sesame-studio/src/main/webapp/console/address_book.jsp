<%@page language="java" contentType="text/html; charset=utf-8"%>
<% 
String externalAddr = "";
if (request.getParameter("externalAddr") != null) {
	externalAddr = request.getParameter("externalAddr");
}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>设置面板</title>
<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<link rel="stylesheet" type="text/css"
	href="../package/ux/css/RowEditor.css" />
<link rel="stylesheet" type="text/css"
	href="../package/ux/grid-examples.css" />
<style type="text/css">
.x-grid3 .x-window-ml {
	padding-left: 0;
}

.x-grid3 .x-window-mr {
	padding-right: 0;
}

.x-grid3 .x-window-tl {
	padding-left: 0;
}

.x-grid3 .x-window-tr {
	padding-right: 0;
}

.x-grid3 .x-window-tc .x-window-header {
	height: 3px;
	padding: 0;
	overflow: hidden;
}

.x-grid3 .x-window-mc {
	border-width: 0;
	background: #cdd9e8;
}

.x-grid3 .x-window-bl {
	padding-left: 0;
}

.x-grid3 .x-window-br {
	padding-right: 0;
}

.x-grid3 .x-panel-btns {
	padding: 0;
}

.x-grid3 .x-panel-btns td.x-toolbar-cell {
	padding: 3px 3px 0;
}

.x-box-inner {
	zoom: 1;
}

.x-icon-addr {
	background-image: url('../images/obj16/new_addr.gif') !important;
}
</style>

<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/ux/RowEditor.js"></script>
<script type="text/javascript" src="../package/xml/xdom.js"></script>
</head>

<body style="height: 100%">
	<script type="text/javascript">
   	if (window.top && window.top.Application)	{
		Application = window.top.Application;
		debug = Application.debug;
	}

	var externalAddr = '<%=externalAddr%>';
</script>
	<script type="text/javascript" src="address_book.js"></script>
</body>
</html>