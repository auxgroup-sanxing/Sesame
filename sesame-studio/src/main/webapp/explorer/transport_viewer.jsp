<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="com.sanxing.ads.*,com.sanxing.ads.utils.*"%>
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

String transport = request.getParameter("transport");

String unitType = "";

JSONArray schemes = new JSONArray();
try
{
	File transportFile = Application.getWarehouseFile(transport);
	
	
	Document document = null;
	JarFile compJar = new JarFile(transportFile);
	try {
		JarEntry entry = compJar.getJarEntry("META-INF/schemes");
		if (entry != null) {
			InputStream input = compJar.getInputStream(entry);
			Properties properties = new Properties();
			properties.load(input);
			
			Set<Entry<Object,Object>> set = properties.entrySet();
			for (Entry<Object,Object> e : set) {
				JSONObject json = new JSONObject();
				json.put("scheme", String.valueOf(e.getKey()));
				json.put("class", String.valueOf(e.getValue()));
				schemes.put(json);
			}
		}
	} 
	finally {
		compJar.close();
	}
	
		
	out.clear();
}
catch (Exception e)
{
	new WebServletResponse(response).sendError(e.getMessage());
	logger.debug(e.getMessage(), e);
}

%>



<%@page import="java.util.Map.Entry"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

<title>外部系统</title>

<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<!--link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/xtheme-gray.css" /-->
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/xml/xdom.js"></script>

<style type="text/css">
.x-combo-list-inner {
	left: 0px !important;
	top: 0px !important;
	width: auto !important;
}
</style>
</head>

<body>
<script type="text/javascript">
//document.oncontextmenu = function(e) { return false; };

var schemes = <%=schemes%>;


  	if (window.top && window.top.Application)
{
	Application = window.top.Application;
} 
else {
	location.href='../error404.jsp';
}

Ext.onReady(function(){

	var store = new Ext.data.JsonStore({
		fields: [{name:"scheme"}, {name:"class"}]
	});
	store.loadData(schemes);

	var viewport = new Ext.Viewport({
		layout: 'fit',
		items: {
			xtype: 'grid',
			columnLines: true,
            columns: [
	            {
	            	header: "协议名称", width: 60, sortable: true, dataIndex: 'scheme', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            },
	            {
	            	header: "传输端子类名", width: 150, sortable: true, dataIndex: 'class', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            }
	        ],
			viewConfig: {
				forceFit: true
			},
			
			store: store
		} 
	});

	
});
</script>
</body>
</html>


