<%@page language="java" contentType="text/html; charset=utf-8"
	isErrorPage="true"%>

<%
	String context = request.getContextPath();
	out.clear();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>应用开发工作室</title>

<style type="text/css">
<!--
body,td,th {
	font-family: Tahoma, Verdana, Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-weight: normal;
	color: #000000;
	margin: 0px;
	padding: 0px;
}

.font {
	color: #FFFFFF;
	text-decoration: none;
	font: bolder 24px "Times New Roman";
}

a:link,a:visited {
	color: #0240a3;
}

.line {
	border-right: 1px solid #CCCCCC;
	border-bottom: 1px solid #CCCCCC;
	border-left: 1px solid #CCCCCC;
}
-->
</style>
<script type="text/javascript">
	//top.location.replace('<%=context%>/login.jsp');
</script>
</head>
<body style="vertical-align: middle; padding: 30px;">
	<table align="center" bgcolor="#f9f9f9" border="0" cellpadding="0"
		cellspacing="0" width="460">
		<tbody>
			<tr>
				<td background="<%=context%>/images/top_bg.gif" height="54"><table
						border="0" cellpadding="0" cellspacing="8" width="97%">
						<tbody>
							<tr>
								<td><font size="5" color="white" face="Times New Roman"><b>Application
											Development Studio</b></font></td>
							</tr>
						</tbody>
					</table></td>
			</tr>
			<tr>
				<td class="line"><table align="center" border="0"
						cellpadding="0" cellspacing="8" width="100%">
						<tbody>
							<tr>
								<td align="center"><img
									src="<%=context%>/images/notice.gif" /></td>
								<td style="font-size: 12px;">
									<p>
										<big><b>通过认证才能访问</b></big>
									</p>
									<p>
										您尚未登录系统，也可能会话已过期，请点击<a href="<%=context%>/login.jsp"
											target="_top">登录</a>
									</p>
								</td>
							</tr>
						</tbody>
					</table></td>
			</tr>
		</tbody>
	</table>
</body>
</html>