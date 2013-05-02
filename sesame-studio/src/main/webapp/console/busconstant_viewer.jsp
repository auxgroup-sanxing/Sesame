<%@page language="java" contentType="text/html; charset=utf-8"%>
<% 
	String type = "APP";
	String unit = 
		request.getParameter("unit") != null ? request.getParameter("unit"):null;
	String operation = 
		request.getParameter("operation") != null ? request.getParameter("operation"):null;
		
	if (unit != null && operation != null) {
		type = "OPERATION";
	} else if (unit != null && operation == null) {
		type = "UNIT";
	}
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>业务常量配置</title>
<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="busconstant_viewer.js"></script>
</head>

<body style="height: 100%">
<script type="text/javascript">
   	if (window.top && window.top.Application)
		Application = window.top.Application;

	var type = '<%=type%>';
	var suName = '<%=unit%>';
	var operaName = '<%=operation%>';
</script>
</body>
</html>