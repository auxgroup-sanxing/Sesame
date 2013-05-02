<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>删除帐户</title>
<style type="text/css">
	TD { font-size: 12px; vertical-align: top; }
</style>

</head>
<body>
	<%
	String userId = request.getParameter("userid");
	if (userId!=null)
	{
		userId = new String(userId.getBytes("iso8859-1"), "utf-8");
	}
	%>
	<p style="font-weight: bold">确认删除帐户 <%=userId%></p>
	<form name="form" action="../UserAction" method="post">
	<input type="hidden" name="action" value="removeUser">
	<input type="hidden" name="userid" value="<%=userId%>">
	<table style="width:100%">
		<tr><td align="left">您确实要删除该帐户吗？<br/></td></tr>
		<tr><td align="right">
			<input type="submit" value="删除">
			<input type="button" value="取消" onclick="window.history.back(-1);">
		</td></tr>
	</table>
	</form>
</body>
</html>