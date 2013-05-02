<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%@page import="org.json.*"%>
<%@page import="com.sanxing.ads.*"%>
<%@page import="com.sanxing.ads.auth.*" %>
<%@page import="com.sanxing.ads.utils.*"%>
<%@page import="java.security.*" %>
<%@page import="java.security.interfaces.*" %>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.io.File"%>
<%@page import="java.util.*" %>
<%@page import="javax.security.auth.*" %>
<%
	String currUser = "";
	Subject subject = Authentication.getSubject();
	Set<StudioPrincipal> principals;
	if (subject != null && (principals=subject.getPrincipals(StudioPrincipal.class)).size()>0) {
		StudioPrincipal principal = principals.iterator().next();
		currUser = principal.getName();
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>更改图片</title>
<style type="text/css">
	TD { font-size: 12px; vertical-align: top; }
	.userLogo { border: 2px solid window; cursor:pointer; height: 48px; width: 48px; }
	.hiliteLogo { border: 2px solid highlight; }
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>

<script type="text/javascript">
	var selectedLogo=null;
	
	function submitCheck(){
		var form = document.form;
		if (form.userdsr.value=="")
		{
			window.alert("请选择图片");
			return false;
		}
		form.submit();
	}
	
	function select(sender)
	{
		if (!sender) return;
		var form = document.form;
		form.userdsr.value=sender.title; 
		form.btnSubmit.disabled='';
		if (selectedLogo) Ext.get(selectedLogo).removeClass("hiliteLogo");
		selectedLogo = sender;
		Ext.get(selectedLogo).addClass("hiliteLogo");
	}
</script>
</head>
<body>
	<%
	String userId = request.getParameter("userid");
	if (userId!=null)
	{
		userId = new String(userId.getBytes("iso8859-1"), "utf-8");
	}
	%>
	<p style="font-weight: bold;"><%=currUser.equals(userId)?"为您的帐户挑选图像":"更改 "+userId+" 的图像"%></p>
	<form name="form" action="../UserAction" method="post">
	<input type="hidden" name="action" value="changeLogo">
	<input type="hidden" name="userid" value="<%=userId%>">
	<input type="hidden" name="userdsr" value="">
	<table style="width:100%">
		<tr><td align="left">选择图像:<br/>
		<div style="height: 150px; background-color:window; border: 1px solid gray; overflow: auto; padding: 2px;">
		<%
        try
		{
			String path = pageContext.getServletContext().getRealPath("images/logo");
			File dir = new File(path);
	        File[] files=dir.listFiles();
	        for(int i=0;i<files.length;i++)
			{
				File file = files[i];
	        	if(file.isFile())
				{
		%>
					<a href="javascript:select()"><img src="../images/logo/<%=file.getName()%>" title="<%=file.getName()%>" class="userLogo"  
						onclick="select(this)" onmouseover="Ext.get(this).addClass('hiliteLogo');" onmouseout="if (this!=selectedLogo) Ext.get(this).removeClass('hiliteLogo');"></a>
		<%
				}
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			WebUtil.sendError(response, e.getMessage());
		}
		%>
		</div>
		</td></tr>
		<tr><td align="right">
			<input type="button" onclick="submitCheck()" name="btnSubmit" value="更改图片" disabled="disabled">
			<input type="button" value="取消" onclick="window.history.back(-1)">
		</td></tr>
	</table>
	</form>
</body>
</html>