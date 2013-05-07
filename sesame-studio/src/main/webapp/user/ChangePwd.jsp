<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<%@page import="org.json.*"%>
<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.auth.*"%>
<%@page import="com.sanxing.studio.utils.WebUtil"%>
<%@page import="java.security.*"%>
<%@page import="java.security.interfaces.*"%>
<%@page import="java.util.*"%>
<%@page import="javax.security.auth.*"%>
<%
	KeyPair keyPair = Application.getKeyPair();

	RSAPublicKey rsaPublicKey = (RSAPublicKey)keyPair.getPublic();

	String modulus = rsaPublicKey.getModulus().toString(16);
	String publicExp = rsaPublicKey.getPublicExponent().toString(16);
	
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
<title>更改帐户密码</title>
<style type="text/css">
TD {
	font-size: 12px;
	vertical-align: top;
}
</style>
<script type="text/javascript" src="../package/secure/md5.js"></script>
<script type="text/javascript" src="../package/secure/rsa/BigInt.js"></script>
<script type="text/javascript" src="../package/secure/rsa/Barrett.js"></script>
<script type="text/javascript" src="../package/secure/rsa/RSA.js"></script>

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

	function submitCheck(){
		var form = document.forms[0];
		if (form.newpwd.value!=form.confirmpwd.value)
		{
			window.alert("您两次输入的新密码不匹配，请重新输入");
			form.newpwd.value="";
			form.confirmpwd.value="";
			form.newpwd.focus();
			return;
		}
		if (form.currpwd && form.currpwd.value) 
			form.currpwd.value = encryptedString(keyPair, form.currpwd.value);
		if (form.newpwd && form.newpwd.value)
			form.newpwd.value = encryptedString(keyPair, form.newpwd.value);
		form.confirmpwd.disabled=true;
		form.submit();
	}
</script>
</head>
<body
	onload="var form = document.forms[0]; form.confirmpwd.disabled=false;">
	<%
	String userId = request.getParameter("userid");
	if (userId!=null)
	{
		userId = new String(userId.getBytes("iso8859-1"), "utf-8");
	}
	%>
	<p style="font-weight: bold"><%=currUser.equals(userId)?"更改您的密码":"更改 "+userId+" 的密码"%></p>
	<form name="form" action="../UserAction" method="post">
		<input type="hidden" name="action" value="changePwd"> <input
			type="hidden" name="userid" value="<%=userId%>">
		<table style="width: 100%">
			<%
		if (currUser.equals(userId))
		{
		%>
			<tr>
				<td align="left">输入您的当前密码:<br />
				<input type="password" name="currpwd" style="width: 240px;"><br /></td>
			</tr>
			<%
		}
		%>
			<tr>
				<td align="left">输入一个新密码:<br />
				<input type="password" name="newpwd" style="width: 240px;"><br /></td>
			</tr>
			<tr>
				<td align="left">再次输入新密码:<br />
				<input type="password" name="confirmpwd" style="width: 240px;"><br /></td>
			</tr>
			<tr>
				<td align="right"><input type="button" onclick="submitCheck()"
					value="更改密码"><input type="button" value="取消"
					onclick="window.history.back(-1)"></td>
			</tr>
		</table>
	</form>
</body>
</html>