<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=utf-8"
	errorPage="../exception.jsp"%>
<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.studio.action.*"%>

<%@page import="java.io.*"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.json.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.jdom.*"%>
<%@page import="org.jdom.xpath.XPath"%>
<html>
<head>
<%
	JSONObject currUser = Authentication.getCurrentUser();
	if (currUser==null)
	{
		WebUtil.sendError(response, PublicConsts.SESSION_EXPIRES);
		return;
	}
	if (!currUser.optString("userlevel").equals("S")){
		WebUtil.sendError(response, "对不起，您没有角色管理权限");
		return;
	}

	File file = UserAction.getUserFile(getServletContext());
	SAXBuilder builder = new SAXBuilder();
	try
	{
		Document document = builder.build(file);
		Element root = document.getRootElement();
%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>角色管理</title>
<style type="text/css">
BODY {
	background-color: white;
}

TD {
	font-size: 12px;
	vertical-align: top;
}

.user {
	border-style: solid;
	border-width: 1px;
	border-color: white;
	cursor: pointer;
	width: 50%;
}

.userHilite {
	border-style: solid;
	border-width: 1px;
	border-color: blue;
}
</style>
<script type="text/javascript">
	
	function go(){}
</script>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-core.js"></script>
</head>
<body>
	<p>
		<b>角色管理</b>
	</p>
	<table style="margin: 0px 0px 5px 0px;">
		<tr>
			<td><img src="../images/icons/user_add.png"></td>
			<td><a onclick='href="RoleEdit.jsp"' href="goto:" target="_self">创建新角色</a></td>
		</tr>
	</table>
	<%
		JSONArray result = new JSONArray();
	    List list = XPath.selectNodes(root, "roles/role");

		for (Iterator iter=list.iterator(); iter.hasNext();)
		{
			Element roleEl = (Element)iter.next();
			JSONObject obj = new JSONObject();
			List attributes = roleEl.getAttributes();
			for (Iterator i= attributes.iterator(); i.hasNext();)
			{
				Attribute attr = (Attribute)i.next();
				obj.put(attr.getName(), attr.getValue());
			}
			result.put(obj);
		}
	%>
	<table style="width: 100%; border: 1px dotted gray;">
		<%
		for (int i=0,len=result.length(); i<len;)
		{
		%>
		<tr>
			<%
			for (int j=0; j<2; j++)
			{
				if (i >= len) break;
				JSONObject role = result.getJSONObject(i);
			%>
			<td class="user" title="修改角色信息"
				onmouseover="Ext.get(this).addClass('userHilite');"
				onmouseout="Ext.get(this).removeClass('userHilite');"
				onclick="window.open('RoleEdit.jsp?roleid=<%=role.getString("id")%>&rolename=<%=role.getString("name")%>&desc=<%=role.optString("desc")%>', '_self')">
				<table>
					<tr>
						<td><img src="<%="../images/icons/user.png"%>"></td>
						<td><b><%=role.getString("name") %></b> (<%=role.optString("id") %>)<br />
							<i><%=role.optString("desc")%></i></td>
					</tr>
				</table>
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