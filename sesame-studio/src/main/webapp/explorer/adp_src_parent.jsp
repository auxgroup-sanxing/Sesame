<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.io.*"%>
<%
	String srcPath = request.getParameter("srcPath");
	File file = new File(srcPath);
	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	String content = null;
	try {
		StringBuffer buffer = new StringBuffer();
		for (String line; (line=reader.readLine())!=null; ) {
			buffer.append(line);
			buffer.append("\n");
		}
		content = buffer.toString();
	}
	finally {
		reader.close();
	}
%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK REL="STYLESHEET" TYPE="text/css"
	HREF="../desktop/css/stylesheet.css" TITLE="Style">
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
</HEAD>
<BODY>
	<%=content %>
	<script type="text/javascript">
	var srcPath = "<%=srcPath%>";
	var links = document.getElementsByTagName("A");
	
	if (links.length > 0 ) {
		for (var i=0; i<links.length; i++) {
			var link = links[i];
			link.onclick = function() {
				var forword = this.href.replace(/.*\//ig, '');
				this.href = "file:///" + srcPath.replace(/\/?[a-z0-9]+\.java\.html/ig, '/' + forword);
			};
		}
	}
</script>
</BODY>
</HTML>


