<%@page language="java" contentType="text/html; charset=utf-8" errorPage="../exception.jsp"%>
<%@page import="com.sanxing.ads.utils.*"%>
<%@page import="com.sanxing.ads.action.*"%>
<%@page import="java.io.File"%>
<%@page import="java.util.*"%>
<%@page import="org.jdom.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.jdom.xpath.XPath"%>
<%@page import="org.json.*"%>
<%
	String tableName = request.getParameter("table");
	String key = request.getParameter("key");
	String readonly = request.getParameter("readonly");
	String cols = request.getParameter("columns");
	String except = request.getParameter("except");
	
	String orderby = request.getParameter("orderby");
	String condition = request.getParameter("condition");
	String accredit_grade = request.getParameter("level");
	System.out.println("in jsp:"+accredit_grade);
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
    table.put("disabled", request.getParameter("disabled"));
	if (key!=null&&key.length()>0) table.put("key", key);
	if (accredit_grade!=null&&accredit_grade.length()==1) table.put("accredit_grade", accredit_grade.toUpperCase());
	
	if(null != orderby  && orderby.length() > 0) table.put("orderby",new String(orderby.getBytes("iso-8859-1"),"utf-8"));
	if(null != condition && condition.length() > 0) table.put("condition",new String(condition.getBytes("iso-8859-1"),"utf-8"));
	out.clear();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>表维护 - <%=tableName %></title>

<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/expr/expression.js"></script>
<script type="text/javascript" src="../package/secure/md5.js"></script>
<script type="text/javascript" src="table_edit.js"></script>

<script type="text/javascript">
	window.app = window.top.Application;
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
</script>
</head>
<body>
</body>
</html>
