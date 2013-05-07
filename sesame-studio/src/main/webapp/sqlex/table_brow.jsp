<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8" errorPage="../exception.jsp"%>
<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.studio.action.SQLExplorer"%>
<%@page import="org.json.*"%>
<%
	String tableName = request.getParameter("table");
	JSONObject table = SQLExplorer.getTableMeta(getServletContext(), tableName);
	if (table == null) {
		String msg = "没有取到表的元信息";
		throw new Exception(msg);
	}
	JSONArray columns = table.getJSONArray("columns");
	if (columns.length()==0) 
	{
		String msg = "没有取到表字段";
		throw new Exception(msg);
	}
    table.put("name", tableName);
    table.put("disabled", new JSONArray());
	out.clear();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>表浏览器</title>

<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>

</head>
<body>
	<script type="text/javascript">
    var explorer = top.Application.getDesktop().getWindow('sqlsqlex');
		if (top && explorer){
			window.Viewer = explorer;
			var table = <%=table%>;
			var el = document.createElement("SCRIPT");
			el.setAttribute("type", "text/javascript");
			el.setAttribute("src", "table_paging.js");
			document.body.appendChild(el);
		}
		else
		{
			document.location.href='../error404.jsp';
		}
    </script>
</body>
</html>
