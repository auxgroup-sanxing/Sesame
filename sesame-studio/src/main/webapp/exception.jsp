<%@page language="java" contentType="text/html; charset=utf-8"
	isErrorPage="true"%>

<%
	System.out.println("exception .............................");
	String context = request.getContextPath();
	String msg = exception.getMessage();
	out.clear();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>应用管理中心</title>

<style type="text/css">
<!--
body,td,th {
	font-family: Tahoma, Verdana, Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-weight: normal;
	color: #000000;
	margin: 0;
	padding: 0;
}

.font {
	color: #FFFFFF;
	text-decoration: none;
	font: bolder 24px "Times New Roman";
}

a:link,a:visited {
	color: #0240a3;
}

#top {
	height: 51px;
	background-image: url("<%=context%>/images/top_bg.jpg");
	background-position: top right;
	background-repeat: no-repeat;
	margin-bottom: 40px;
	padding-top: 5px;
	padding-left: 10px;
}

.line {
	border-right: 1px solid #CCCCCC;
	border-bottom: 1px solid #CCCCCC;
	border-left: 1px solid #CCCCCC;
}
-->
</style>
</head>
<body leftmargin="0" topmargin="0">
	<div id="top">
		<table border="0" cellpadding="0" cellspacing="0" width="97%">
			<tbody>
				<tr>
					<td>&nbsp;</td>
				</tr>
			</tbody>
		</table>
	</div>
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
								<td width="30%"><img src="<%=context%>/images/notice.gif" /></td>
								<td width="70%"><br />
									<p><%=msg==null ? exception.toString() : msg%></p></td>
							</tr>
						</tbody>
					</table></td>
			</tr>
		</tbody>
	</table>
</body>
</html>