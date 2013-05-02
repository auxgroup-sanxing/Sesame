<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="com.sanxing.ads.*,com.sanxing.ads.utils.*,com.sanxing.ads.team.svn.*,com.sanxing.ads.team.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="org.json.*"%>
<%@page import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.XPath"%>
<%@page import="java.net.URLDecoder" %>

<%!
	private final Logger logger = Logger.getLogger(this.getClass());
%>

<%
request = new WebServletRequest(request);
String intf = request.getParameter("intf");
String intfId = request.getParameter("unitId");
String intfDesc = (request.getParameter("unitDesc") == null) ? "" : request.getParameter("unitDesc").replaceAll("[\n]+", "");
String projectDesc = (request.getParameter("projectDesc") == null) ? "" :request.getParameter("projectDesc");
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>公共接口面板</title>

<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/ux/InlineToolbarTabPanel.js"></script>
<script type="text/javascript" src="../package/xml/xdom.js"></script>
<style type="text/css">

</style>
</head>

<body>
<script type="text/javascript">
	var intf = '<%=intf%>';
	var intfId = '<%=intfId%>';
	var intfDesc = '<%=URLDecoder.decode(intfDesc, "UTF-8")%>';
	var projectDesc = '<%=URLDecoder.decode(projectDesc, "UTF-8")%>';
	
 	if (window.top && window.top.Application) {
		Application = window.top.Application;
	} else {
		location.href='../error404.jsp';
	}
 </script>	
 <script type="text/javascript" src="intf_viewer.js"></script>
 </body>
</html>