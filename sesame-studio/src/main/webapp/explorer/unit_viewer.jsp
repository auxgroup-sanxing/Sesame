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
String unit = request.getParameter("unit");
String component = null;
String compName = null;
String componentDesc = null;
String engineDesc = null;
String engineCmp = "";
String script = "";
String activeTab = request.getParameter("activeTab");
if (activeTab == null)
	activeTab = "0";

String unitId = request.getParameter("unitId");
String unitDesc = (request.getParameter("unitDesc") == null) ? "" : request.getParameter("unitDesc").replaceAll("[\n]+", "");
String projectDesc = (request.getParameter("projectDesc") == null) ? "" :request.getParameter("projectDesc");

String unitType = "";
String isVersioned = "true";
String unitLocked = "false";

String importLocation = "";
JSONObject properties = null;
try {
	JSONObject user = Authentication.getCurrentUser();
	String userName = user.optString("userid");
	
	File unitFolder = Configuration.getWorkspaceFile(unit);
	
	//判断WSDL是否在版本库中 
	File wsdl = new File(unitFolder, "unit.wsdl");
	File projectFolder = unitFolder.getParentFile().getParentFile();
	ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
	//如果该项目或该服务单元的WSDL不在版本控制中，直接返回。
	if(null == sync || (!sync.isVersioned(wsdl))){
		isVersioned = "false";
	} else {
		Map<String, ?> props = sync.info(wsdl);
		String lock = (String)props.get("lock");
		if (lock != null) {
			if (userName.equals((String) props.get("lock.owner")))
				unitLocked = "true";
		}
	}
	
	SAXBuilder builder = JdomUtil.newSAXBuilder();
	Document doc = builder.build(new File(unitFolder, "jbi.xml"));
	Element rootEl = doc.getRootElement();
	Namespace comNS = rootEl.getNamespace("comp");
	component = comNS!=null ? comNS.getURI() : null;
	
	if (!"".equals(component)) {
		Document compDoc = builder.build(new File(Configuration.getWarehouseFile(component), "/jbi.xml"));
		if (compDoc != null) {
			Element root = compDoc.getRootElement();
			Namespace ns = root.getNamespace();
			Element componentEl = root.getChild("component", ns);
			if (componentEl != null) {
				Element identify = componentEl.getChild("identification", ns);
				if (identify != null) {
					String compDesc = identify.getChildText("description",ns);
					compName = identify.getChildText("name",ns);
					componentDesc = ("".equals(compDesc)) ? compName : compDesc;
				}
			}
		}
	}
	
	if (component != null) {
		File compBundle = Configuration.getWarehouseFile(component);
		if (!compBundle.exists()) {
			throw new Exception("找不到指定的组件包: "+compBundle.getCanonicalPath());
		}
		
		File extendJs = new File(compBundle, "unit_view.js");
		if (extendJs.exists()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(extendJs), "utf-8"));
			try {
				StringBuffer buffer = new StringBuffer();
				for (String line; (line=reader.readLine())!=null; ) {
					buffer.append(line);
					buffer.append("\n");
				}
				script = buffer.toString();
			}
			finally {
				reader.close();
			}
		}
		Document document = null;
		if (compBundle.isDirectory()) {
			File jbiXml = new File(compBundle, "jbi.xml");
			rootEl = builder.build(jbiXml).getRootElement();
			Element compEl = rootEl.getChild("component", rootEl.getNamespace());
			if (compEl != null) {
				if (compEl.getAttributeValue("type").equals("binding-component")) {
					File wsdlFile = new File(unitFolder, "unit.wsdl");
					Element wsdlRootEl = builder.build(wsdlFile).getRootElement();
					Element importEl = wsdlRootEl.getChild("import", wsdlRootEl.getNamespace());
					
					if (importEl != null && unit.indexOf("/client/") == -1) {
						String location = importEl.getAttributeValue("location");
						File importFile = new File(unitFolder, location);
						importLocation = 
								projectFolder.getName() + "/engine/" + importFile.getParentFile().getName();
						File jbiPath = new File(importFile.getParentFile(), "/jbi.xml");
						if (jbiPath.exists()) {
							Element root = builder.build(jbiPath).getRootElement();
							engineCmp = root.getNamespace("comp").getURI();
							File engineWSDL = new File(unitFolder, location);
							Element engineRootEl = builder.build(engineWSDL).getRootElement();
							engineDesc = engineRootEl.getChildText("documentation", engineRootEl.getNamespace());
						}
						unitType = "serverGuide";
					} else {
						if (unit.indexOf("/client/") != -1) {
								unitType = "client";	
						} else {
								unitType = "serverCustom";
						}
					}
				} else {
						unitType = "engine";
				}
			}
			
			File unitXsd = new File(compBundle, "binding.xsd");
			if (unitXsd.exists()) 
				document = builder.build(unitXsd);
		} else {
			JarFile compJar = new JarFile(compBundle);
			try {
				JarEntry entry = compJar.getJarEntry("binding.xsd");
				if (entry != null) {
					InputStream input = compJar.getInputStream(entry);
					document = builder.build(input);
				}
			} 
			finally {
				compJar.close();
			}
		}
		
		if (document != null) {
			rootEl = document.getRootElement();
			Element firstEl = rootEl.getChild("element", rootEl.getNamespace());
			if (firstEl != null && script.length()==0) {	// binding.js存在时不使用properties
				JSONObject json = new JSONObject();
				json.put("tag", firstEl.getName());
				json.put("name", firstEl.getAttributeValue("name"));
				json.put("stateful", false);
				SchemaUtil.generateUI(firstEl, json);
				properties = json;
			}
		}
		
	}
	out.clear();
}
catch (Exception e) {
	throw new ServletException(e.getMessage(), e);
}

%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

<title>外部系统</title>

<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<!-- <link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/xtheme-gray.css" /> -->
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/xml/xdom.js"></script>
<script type="text/javascript" src="../package/upload/FileUploadField.js"></script>
<script type="text/javascript" src="../package/ux/InlineToolbarTabPanel.js"></script>

<style type="text/css">
.x-combo-list-inner {
	left: 0px !important;
	top: 0px !important;
	width: auto !important;
}

.x-icon-loading {
	background-image: url('../ext-Ajax/resources/images/default/grid/loading.gif');
	background-position: center;
	background-repeat: no-repeat;
}

.x-grid3-col-3{
	padding: 0 3px 0 3px!important;
}

.innerImg{
	background:no-repeat;
	cursor: pointer;
}

.x-tab-panel-header {
	border-right: 0px!important;
}

.soapComboBox {
	margin-bottom: 5px!important;
}

.column {
	width: 160px!important;
	margin-left: -60px!important;
}

.locationList {
	margin-left: -60px!important;
}

.address-book-btn {
	height: 25px;
	padding: 5px 0 0 8px;
}

.x-grid3-cell-selected {
	background-color: #FFFFFF !important;
}

/* 提示框 */
#tip {
	position: absolute;
	z-index: 9999;
	display: none;
}

.rc_box1,.rc_box2,.rc_box3 { 
	display:inline-block; 
	*display:inline; 
	*zoom:1; 
	position:relative; 
	border-style:solid; 
	border-color:#99BBE8;
}

.rc_box2,.rc_box3 {
	border-width:0 1px; 
	*left:-2px; 
	background-color:#DFE8F6;
}

.rc_box1 { border-width:1px; line-height:1.5;}
.rc_box2 { margin:0 -2px;}
.rc_box3 { 
	margin:1px -2px; 
	padding:4px 6px;
	font: 12px tahoma,arial,helvetica,sans-serif;
	color: #15428B;
	background: url("../images/unit_tip_bg.gif") repeat scroll 0 -48px transparent;
}

.ov1,.ov2 {
	position:absolute; 
	left:50%; 
	overflow:hidden; 
	width:0; 
	height:0;
	border-left:10px dotted transparent; 
	border-right:6px dotted transparent;
	border-top:15px solid transparent;
}

.ov1 { top:26px; border-top-color:#99BBE8;}
.ov2 { top:25px; border-top-color:#DFE8F6;}

.x-grid3-header-offset{
	width:auto;
}
</style>
</head>

<body>
<script type="text/javascript">
	var isVersioned = '<%=isVersioned%>';
	var unitLocked = '<%=unitLocked%>';
	var unit = '<%=unit%>';
   	var component = '<%=component%>';
   	var extendScript = <%=script.length()>0%>;
   	var compName = '<%=compName%>';
   	var componentDesc = '<%=componentDesc%>';
   	var engineDesc = '<%=engineDesc%>';

	var unitType = '<%=unitType%>';
	var engineCmp = '<%=engineCmp%>';
   	
	var properties = <%=properties%>;
	var importLocation = '<%=importLocation%>';
	
	if (properties) {
		var createRegex = function(item){
			if (item.regex) 
				item.regex = new RegExp(item.regex);
			if (item.items) 
				Ext.each(item.items, createRegex);
		}
		Ext.each(properties.items, createRegex);
	}
	var active = <%=activeTab%>;
	var unitId = '<%=unitId%>';
	var unitDesc = '<%=URLDecoder.decode(unitDesc, "UTF-8")%>';
	var projectDesc = '<%=URLDecoder.decode(projectDesc, "UTF-8")%>';
	
   	if (window.top && window.top.Application)
	{
		Application = window.top.Application;
	} else {
		location.href='../error404.jsp';
	}

</script>
<script type="text/javascript" src="unit_base.js"></script>
<script type="text/javascript; e4x=1">
	<%=script%>
</script>

<!-- 锁定提示  -->
<div id="tip" class="rc_box1">
    <div class="rc_box2">
        <div class="rc_box3">
            	<b>编辑服务前，请先锁定服务!</b>
        </div>
    </div>
    <div class="ov1"></div>
    <div class="ov2"></div>
</div>
</body>
</html>


