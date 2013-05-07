<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>表设计器</title>

<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />

<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<!-- include the locale file -->
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>

<script type="text/javascript" src="RestrictEdit.js"></script>
</head>
<body>
	<div id="grid-panel" style="width: 100%; height: 100%;">
		<div id="editor-grid"></div>
	</div>
	<script type="text/javascript">
		var edittingObject='<%=request.getParameter("object")%>';
		var app = top.Application;
    	if (window.top && app)
    	{
    		window.app = app;
			var el = document.createElement("SCRIPT");
			el.setAttribute("type", "text/javascript");
			el.setAttribute("src", "table_dsgn.js");
			document.body.appendChild(el);
		}
		else
		{
			document.location.href='../error404.jsp';
		}
    </script>
</body>
</html>
