<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" errorPage="../exception.jsp"%>
<%@page import="com.sanxing.studio.utils.CommonUtil"%>
<%@page import="com.sanxing.studio.action.SQLExplorer"%>
<%@page import="java.io.File"%>
<%@page import="java.util.*"%>
<%@page import="org.jdom.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.jdom.xpath.XPath"%>
<%@page import="org.json.*"%>
<%
	String script = request.getParameter("script");
	String tableName = request.getParameter("table");
	String key = request.getParameter("key");
	String readonly = request.getParameter("readonly");
	String cols = request.getParameter("columns");
	String except = request.getParameter("except");
	
	if (script == null) script="table_paging.js";

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
	
    for (int i=0, len=columns.length(); i<len; i++)
	{
		JSONObject column = columns.getJSONObject(i);
		String columnName = column.getString("dataIndex");
		if (cols!=null && (cols+",").indexOf(columnName+",") < 0) {
			column.put("hidden", true);
		}
		else if (except!=null && (except+",").indexOf(columnName+",") > -1) {
			column.put("hidden", true);
		}
	}
    table.put("name", tableName);
    table.put("readonly", readonly!=null && readonly.equals("true"));
    table.put("disabled", new JSONArray());
	if (key!=null) table.put("key", key);
	out.clear();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>表维护 - <%=tableName %></title>

<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/source/locale/ext-lang-zh_CN.js"></script>

</head>
<body>
    <script type="text/javascript">
   	    var app = top.Application;
    	if (window.top && app)
    	{
    		var table = <%=table%>;
    		try {
    			eval("var validator="+table.validator);
    			if (validator) {
    				table.validator=validator;
    			}
    			else {
    				table.validator=null;
    			}
    		}
    		catch(e) {
    			table.validator=null;
    			alert("读取表校验失败");
    		}
    		
			var el = document.createElement("SCRIPT");
			el.setAttribute("type", "text/javascript");
			el.setAttribute("src", "<%=script%>");
			document.body.appendChild(el);
		}
		else
		{
			document.location.href='../error404.jsp';
		}
    </script>
</body>
</html>
