<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="java.util.*"%>
<%@page import="javax.security.auth.*" %>
<%@page import="org.json.*"%>
<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.auth.*"%>
<%
Subject subject = Authentication.getSubject();
Set<StudioPrincipal> principals;
if (subject != null && (principals=subject.getPrincipals(StudioPrincipal.class)).size()>0) {
	response.sendRedirect("desktop/index.jsp");
}
else {
	response.sendRedirect("login.jsp");
}
%>
