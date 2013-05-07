<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>更改帐户名称</title>
<style type="text/css">
TD {
	font-size: 12px;
	vertical-align: top;
}
</style>

<script type="text/javascript">
	function submitCheck(){
		if (form.userid.value=='')
		{
			form.userid.focus();
			return;
		}
		if (form.fullname.value=='')
		{
			form.fullname.value=form.userid.value;
		}
		form.submit();
	}
</script>
</head>
<body>
	<%
	String userId = request.getParameter("userid");
	String fullName = request.getParameter("fullname");
	if (userId!=null)
	{
		userId = new String(userId.getBytes("iso8859-1"), "utf-8");
	}
	if (fullName!=null)
	{
		fullName = new String(fullName.getBytes("iso8859-1"), "utf-8");
	}
	%>
	<p style="font-weight: bold;">
		为
		<%=userId%>
		输入一个新名称
	</p>
	<form name="form" action="../UserAction" method="post">
		<input type="hidden" name="action" value="changeName"> <input
			type="hidden" name="oldid" value="<%=userId%>">
		<table style="width: 100%">
			<tr>
				<td align="left">键入一个新标识<br />
				<input type="text" name="userid" style="width: 240px;"
					value="<%=userId%>"><br /></td>
			</tr>
			<tr>
				<td align="left">键入一个新名称<br />
				<input type="text" name="fullname" style="width: 240px;"
					value="<%=fullName%>"><br /></td>
			</tr>
			<tr>
				<td align="right"><input type="button" onclick="submitCheck()"
					value="更改"><input type="button" value="取消"
					onclick="window.history.back(-1)"></td>
			</tr>
		</table>
	</form>
</body>
</html>