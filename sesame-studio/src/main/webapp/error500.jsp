<%@page language="java" contentType="text/html; charset=utf-8" isErrorPage="true"%>
<%
	String context = request.getContextPath();

	String msg = (String)request.getAttribute("javax.servlet.error.message");
	//logger.debug(msg);
	out.clear();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>应用开发工作室</title>

<style type="text/css">

body, td, th {
  font-family: Tahoma, Verdana, Arial, Helvetica, sans-serif;
  font-size: 12px;
  font-weight: normal;
  color: #000000;
  margin: 0;
  padding: 0;
}

.line {
	border-right: 1px solid #CCCCCC;
	border-bottom: 1px solid #CCCCCC;
	border-left: 1px solid #CCCCCC;
	overflow: hidden;
}

</style>
</head>
<body style="vertical-align: middle; padding: 30px; 0px; 0px; 0px;  font-family: Tahoma, Verdana, Arial, Helvetica, sans-serif; font-weight: normal;">
<table align="center" bgcolor="#f9f9f9" border="0" cellpadding="0" cellspacing="0" style="width:460px; overflow: hidden;"><tbody>
  <tr><td style="background-image: url('<%=context%>/images/top_bg.gif'); background-repeat: no-repeat; height:54px;">
      <table border="0" cellpadding="0" cellspacing="8" width="97%">
      <tbody><tr>
        <td><font size="5" color="white" face="Times New Roman"><b>Application Development Studio</b></font></td>
      </tr></tbody>
      </table>
  </td></tr>
  <tr>
    <td class="line"><table align="center" border="0" cellpadding="0" cellspacing="8" width="100%">
      <tbody><tr>
        <td align="center"><img src="<%=context%>/images/notice.gif"></img></td>
        <td style="width:380px; word-wrap:break-word; word-break:break-all; overflow:hidden; font-size: 12px;">
        	<%=new String(msg.getBytes("utf-8"), response.getCharacterEncoding())%>
		</td>
	  </tr></tbody>
	</table></td>
  </tr>
</tbody></table>
</body>
</html>