<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=utf-8" errorPage="../exception.jsp"%>
<%@page import="java.sql.*"%>
<%@page import="com.sanxing.ads.*"%>
<%@page import="com.sanxing.ads.utils.*"%>
<%@page import="com.sanxing.ads.action.UserAction"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="javax.security.auth.*" %>

<%@page import="org.json.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.jdom.*"%>
<%@page import="org.jdom.xpath.XPath"%>
<%@page import="com.sanxing.ads.*" %>
<%@page import="com.sanxing.ads.auth.*" %>
<%@page import="com.sanxing.ads.utils.*" %>
<html>
<head>
<%
	Subject subject = Authentication.getSubject();
	Set<StudioPrincipal> principals;
	if (subject != null && (principals=subject.getPrincipals(StudioPrincipal.class)).size()>0) {
		StudioPrincipal principal = principals.iterator().next();
	
		if (!principal.getLevel().equals("S"))
		{
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/user/UserMgr.jsp?userid="+
					URLEncoder.encode(principal.getName(), "UTF-8"));
			dispatcher.forward(request, response);
			//response.sendRedirect("UserMgr.jsp?userid="+URLEncoder.encode(currUser.getString("userid"), "UTF-8"));
			return;
		}
	}
	
	File file = UserAction.getUserFile(getServletContext());
	SAXBuilder builder = new SAXBuilder();
	try
	{
		Document document = builder.build(file);
		Element root = document.getRootElement();
%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用户首页</title>
<style type="text/css">
	BODY { background-color: window; }
	TD { font-size: 12px; vertical-align: top; }
	.user { border-style:solid;  border-width:1px; border-color: window; cursor: pointer; width: 50%;}
	.user .userLogo { height: 48px; width: 48px; }
	.userHilite { border-style:solid;  border-width: 1px; border-color: blue; }
</style>
<script type="text/javascript">
	document.oncontextmenu = function(e) { return false; };
	function go(){}
</script>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
</head>
<body>
	<%
		if (request.getQueryString()==null)
		{
	%>
	<p><b>选择一项操作</b></p>
	<table>
		<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='href="UserHome.jsp?action=selectAccount"' href="goto:" target="_self">更改帐户</a></td></tr>
		<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='href="CreateUser.jsp"' href="goto:" target="_self">创建新帐户</a></td></tr>
		<%if (root.getAttributeValue("role-play", "false").equals("true")) {%>
		<tr><td><img src="../images/icons/right_a.gif"></td><td><a onclick='href="RoleManager.jsp"' href="goto:" target="_self">角色管理</a></td></tr>
		<%}%>
	</table>
	<%
		}
	%>
	<p><b>选择要更改的帐户</b></p>
	<%
		JSONArray result = new JSONArray();
	    List list = XPath.selectNodes(root, "user");

		for (Iterator iter=list.iterator(); iter.hasNext();)
		{
			Element userEl = (Element)iter.next();
			JSONObject obj = new JSONObject();
			List attributes = userEl.getAttributes();
			for (Iterator i= attributes.iterator(); i.hasNext();)
			{
				Attribute attr = (Attribute)i.next();
				obj.put(attr.getName(), attr.getValue());
			}
			result.put(obj);
		}
	%>
	<table style="width:100%">
	<%
		for (int i=0,len=result.length(); i<len;)
		{
		%>
			<tr>
			<%
			for (int j=0; j<2; j++)
			{
				if (i >= len) break;
				JSONObject user = result.getJSONObject(i);
			%>
			<td  class="user" title="修改帐户信息" onclick="window.open('UserMgr.jsp?userid=<%=user.getString("userid")%>', '_self')" 
				onmouseover="Ext.get(this).addClass('userHilite');" onmouseout="Ext.get(this).removeClass('userHilite');">
				<table><tr>
					<td><img src="<%="../images/logo/"+(user.has("userdsr")?user.getString("userdsr"):"user.gif")%>" class="userLogo"></td>
					<td>
						<b><%=user.getString("userid") %></b><br/>
						<%=user.optString("fullname") %><br/>
						<%=user.optString("userlevel").equals("S") ? "系统管理员":"普通帐户" %>
					</td>
				</tr></table>
			</td>
			<%
			i++;
			}
			%>
			</tr>
		<%
		}
	}
	catch(Exception e)
	{
		getServletContext().log("", e);
		throw e;
	}
	%>
	</table>
</body>
</html>