<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="java.io.File"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="javax.security.auth.*" %>
<%@page import="org.json.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.jdom.*"%>
<%@page import="org.jdom.xpath.XPath"%>

<%@page import="com.sanxing.studio.*" %>
<%@page import="com.sanxing.studio.auth.*" %>
<%@page import="com.sanxing.studio.utils.*" %>
<%@page import="com.sanxing.studio.action.UserAction"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用户管理</title>
<style type="text/css">
	TD { font-size: 12px; vertical-align: top; }
	.user { }
	.user .userLogo { height: 48px; width: 48px; }
</style>
<script type="text/javascript">
	document.oncontextmenu = function(e) { return false; };
	function run(url)
	{
		if (url)	{ window.open(url, '_self'); }
	}
</script>
</head>
<body>
	<%
	String currUser = "";
	Subject subject = Authentication.getSubject();
	Set<StudioPrincipal> principals;
	if (subject != null && (principals=subject.getPrincipals(StudioPrincipal.class)).size()>0) {
		StudioPrincipal principal = principals.iterator().next();
		currUser = principal.getName();
	}

	String userId = request.getParameter("userid");
	if (userId != null)
	{
		userId = new String(userId.getBytes("iso8859-1"), "utf-8");
	}
	File file = UserAction.getUserFile(getServletContext());
	SAXBuilder builder = new SAXBuilder();
	JSONObject user = new JSONObject();
	boolean role_play = false;
	try
	{
		Document document = builder.build(file);
		Element root = document.getRootElement();
		role_play = root.getAttributeValue("role-play", "false").equals("true");
	    Element userEl = (Element)XPath.selectSingleNode(root, "user[@userid='"+userId+"']");
		if (userEl!=null)
		{
			List list = userEl.getAttributes();
			for (Iterator iter= list.iterator(); iter.hasNext();)
			{
				Attribute attr = (Attribute)iter.next();
				user.put(attr.getName(), attr.getValue());
			}
		}
	}
	catch(Exception e)
	{
		//e.printStackTrace();
		WebUtil.sendError(response, e.getMessage());
	}
	%>
	<p style="font-weight: bold"><%=currUser.equals(userId) ? "更改我的帐户": "更改帐户 "+user.optString("userid") %></p>
	<table style="width:100%"><tr>
		<td>
		<table>
			<%
			if (currUser.equals(userId))
			{
			%>
			<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='href="ChangePwd.jsp?userid=<%=userId%>"' href="goto:" target="_self">更改我的密码</a></td></tr>
			<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='href="ChangeLogo.jsp?userid=<%=userId%>"' href="goto:" target="_self">更改我的图片</a></td></tr>
			<%
			}
			else
			{
			%>
			<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='href="ChangeName.jsp?userid=<%=userId%>&fullname=<%=user.getString("fullname")%>"' href="#" target="_self">更改名称</a></td></tr>
			<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='href="ChangePwd.jsp?userid=<%=userId%>"' href="goto:" target="_self">更改密码</a></td></tr>
			<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='href="ChangeLogo.jsp?userid=<%=userId%>"' href="goto:" target="_self">更改图片</a></td></tr>
			<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='href="ChangeKind.jsp?userid=<%=userId%>&kind=<%=user.getString("userlevel")%>"' href="#" target="_self">更改账户类型</a></td></tr>
				<%
				if (role_play && !user.getString("userlevel").equals("S"))
				{
				%>
				<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='this.href="ChangeRole.jsp?userid=<%=userId%>"' href="goto:" target="_self">更改角色</a></td></tr>
				<%
				}
				%>
			<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='this.href="RemoveUser.jsp?userid=<%=userId%>"' href="goto:" target="_self">删除帐户</a></td></tr>
			<%
			}
			%>
		</table>
		</td>
		<td>
			<table class="user"><tr>
				<td><img src="<%="../images/logo/"+(user.has("userdsr")?user.getString("userdsr"):"user.gif")%>" class="userLogo"></td>
				<td>
					<b><%=user.getString("userid") %></b><br/>
					<%=user.optString("fullname") %><br/>
					<%=user.optString("userlevel").equals("S") ? "系统管理员":"普通帐户" %>
				</td>
			</tr></table>
		</td>
	</tr></table>
</body>
</html>