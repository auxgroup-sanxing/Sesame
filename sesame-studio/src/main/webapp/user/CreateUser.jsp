<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%@page import="org.json.*"%>
<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.utils.WebUtil"%>
<%@page import="java.security.*" %>
<%@page import="java.security.interfaces.*" %>
<%
	KeyPair keyPair = Application.getKeyPair();

	RSAPublicKey rsaPublicKey = (RSAPublicKey)keyPair.getPublic();

	String modulus = rsaPublicKey.getModulus().toString(16);
	String publicExp = rsaPublicKey.getPublicExponent().toString(16);
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>创建帐户</title>
<style type="text/css">
	TD { font-size: 12px; vertical-align: top; }
</style>
<script type="text/javascript" src="../package/secure/md5.js"></script>
<script type="text/javascript" src="../package/secure/rsa/BigInt.js"></script>
<script type="text/javascript" src="../package/secure/rsa/Barrett.js"></script>
<script type="text/javascript" src="../package/secure/rsa/RSA.js"></script>

<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>

<script type="text/javascript">
	var publicExp = "<%=publicExp%>";
	var modulus = "<%=modulus%>";
	<%
	if (Application.KEY_SIZE==512) {
	%>
	//keysize=512,
	setMaxDigits(76);
	<%
	} 
	else if (Application.KEY_SIZE==1024) {
	%>
	//keysize=1024,
	setMaxDigits(130);
	<%
	}
	%>
	var keyPair = new RSAKeyPair(publicExp, "", modulus);


	Ext.onReady(function(){
		var form = document.forms[0];
		form.password1.disabled=false;
		form.password2.disabled=false;
	});
	function submitCheck(){
		var form = document.forms[0];
		if (form.userid.value=='')
		{
			form.userid.focus();
			return;
		}
		if (form.fullname.value=='')
		{
			form.fullname.value=form.userid.value;
		}
		if (form.password2.value!=form.password1.value)
		{
			alert('两次输入密码不一致');  return;
		}
		form.password.value = encryptedString(keyPair, form.password1.value);
		//form.password.value=form.password1.value ? hex_md5(form.password1.value) : '';
		form.password1.disabled=true;
		form.password2.disabled=true;
		form.action="SelectKind.jsp";
		form.submit();
	}
</script>
</head>
<body>
	<p style="font-weight: bold;">为新帐户起名</p>
	<form name="form" enctype="application/x-www-form-urlencoded" method="post">
	<input type="hidden" name="password" />
	<table style="width:100%">
		<tr><td align="left">键入新帐户标识<br/><input type="text" name="userid" style="width:240px;" /><br/></td></tr>
		<tr><td align="left">键入新帐户名称<br/><input type="text" name="fullname" style="width:240px;" /><br/></td></tr>
		<tr><td align="left">键入新帐户密码<br/><input type="password" name="password1" style="width:240px;" /><br/></td></tr>
		<tr><td align="left">再次输入帐户密码<br/><input type="password" name="password2" style="width:240px;" /><br/></td></tr>
		<tr><td align="right"><input type="button" onclick="submitCheck()" value="下一步"><input type="button" value="取消" onclick="window.history.back(-1);"></td></tr>
	</table>
	</form>
</body>
</html>