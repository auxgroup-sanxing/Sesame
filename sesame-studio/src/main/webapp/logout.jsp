<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="com.sanxing.studio.PublicConsts"%>
<%@page import="javax.security.auth.login.*" %>
<% 
HttpSession sess = request.getSession();
if (sess != null) {
	LoginContext loginCtx = (LoginContext)sess.getAttribute(PublicConsts.LOGIN_CONTEXT);
	if (loginCtx != null) {
		loginCtx.logout();
	}
	sess.invalidate();
}

String action = request.getParameter("action");
if (action!=null && action.equals("relogin"))
{
	response.sendRedirect("login.jsp");
}
else
{
%>
<HTML>
<HEAD>
<TITLE>Application Development Studio</TITLE>
<script type="text/javascript">
window.opener = null;
window.close();
</script>
</HEAD>
<BODY>
</BODY>
</HTML>
<%
}
%>