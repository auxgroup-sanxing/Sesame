<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="com.sanxing.ads.*,com.sanxing.ads.utils.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="org.json.*"%>
<%@page import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.XPath"%>
<%!
	private final Logger logger = Logger.getLogger(this.getClass());
%>

<% 
request = new WebServletRequest(request);
String libPath = request.getParameter("lib");
String libName = libPath.replaceAll(".*?/", "");
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>共享库浏览</title>
<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<link rel="stylesheet" type="text/css" href="../package/upload/resources/UploadDialog.css"/>
<style type="text/css">
.item-wrap {
	text-align:center;
	float: left;
	cursor: pointer;
	margin-top: 4px;
	margin-left: 4px;
	padding:2px 7px!important;
	font: 13px Arial,Helvetica,"Nimbus Sans L",sans-serif;
}

.displayNameWrap {
	
}

.fullNameWrap, .sizeWarp, .timeWarp {
	display: none
}

.hilited {
	background: #EFEFEF; 
	color: black;
}
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="sharelib_viewer.js"></script>
<script type="text/javascript" src="../package/upload/UploadDialog.js"></script>
</head>

<body style="height: 100%">
<script type="text/javascript">
   var path = '<%=libPath%>';
   var name = '<%=libName%>';
</script>
</body>
</html>