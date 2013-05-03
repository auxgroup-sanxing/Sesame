<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="com.sanxing.studio.utils.WebServletRequest"%>
<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>选择帐户类型</title>
<style type="text/css">
	TD { font-size: 12px; vertical-align: top; }
</style>
<script type="text/javascript">
	var sAdminDscr="<br/>&nbsp;&nbsp;使用系统管理员帐户，您可以:<ul><li>创建、更改和删除帐户</li><li>系统设置的更改</li><li>访问所有资源</li></ul>";
	var sRestrictDscr="<br/>&nbsp;&nbsp;使用一个普通的帐户，您可以:<ul><li>更改或删除您的密码</li><li>更改您的图片和其它设置</li><li>访问您自己创建的资源或别人共享的资源</li></ul>";
</script>
</head>
<body>
	<%
	request = new WebServletRequest(request);
	request.setCharacterEncoding("utf-8");
	
	String userId = request.getParameter("userid");
	String fullName = request.getParameter("fullname");
	String password = request.getParameter("password");
	%>
	<p style="font-weight: bold">选择帐户类型</p>
	<form name="form" action="../UserAction" method="post">
	<input type="hidden" name="userid" value="<%=userId%>">
	<input type="hidden" name="fullname" value="<%=fullName%>">
	<input type="hidden" name="password" value="<%=password%>">
	<table style="width:100%">
		<tr><td align="left">
			<table><tr>
				<td><input type="radio" name="kind" id="radio1" value="S" checked="checked" onclick="document.getElementById('Description').innerHTML=sAdminDscr"><label for="radio1">系统管理员</label></td>
				<td><input type="radio" name="kind" id="radio2" value="C" onclick="document.getElementById('Description').innerHTML=sRestrictDscr"><label for="radio2">普通帐户</label></td>
			</tr></table>
		</td></tr>
		<tr><td id="Description">
		</td></tr>
		<tr><td align="right">
			<input type="button" onclick="window.history.back(-1)" value="上一步">
			<input type="submit" value="创建帐户">
			<input type="button" value="取消" onclick="window.open('UserHome.jsp', '_self')">
		</td></tr>
	</table>
	</form>
	<script type="text/javascript">
		var oDescription=document.getElementById("Description");
		oDescription.innerHTML=sAdminDscr;
	</script>
</body>
</html>