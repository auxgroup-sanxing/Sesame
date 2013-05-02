<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%
String obj = request.getParameter("object");
out.clear();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache" />
<meta http-equiv="Expires" content="0" />
<title>SQL编辑器</title>

<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>

</head>
<body>
	<iframe id="download" style="display: none;"></iframe>
    <script type="text/javascript">
    	var referer = '<%=obj%>';
		var app = top.Application;
    	if (top && app){
			window.app = app;
			var el = document.createElement("SCRIPT");
			el.setAttribute("type", "text/javascript");
			el.setAttribute("src", "sql_edit.js");
			document.body.appendChild(el);
		}else{
			document.location.href='../error404.jsp';
		}
	</script>
</body>
</html>
