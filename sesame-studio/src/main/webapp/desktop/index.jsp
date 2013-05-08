<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page import="org.json.*"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="javax.security.auth.*"%>
<%@page
	import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.XPath"%>
<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.auth.*"%>
<%@page import="com.sanxing.studio.utils.*"%>

<%
JSONObject currUser = null;

Subject subject = Authentication.getSubject();
Set principals;
if (subject != null && (principals=subject.getPrincipals(StudioPrincipal.class)).size()>0) {
	StudioPrincipal principal = (StudioPrincipal)principals.iterator().next();
	
	currUser = new JSONObject();
	currUser.put("userid", principal.getName());
	currUser.put("fullname", principal.getFullname());
	currUser.put("userlevel", principal.getLevel());
}

JSONArray projects = new JSONArray();
String path = Application.getWorkspaceRoot().getAbsolutePath();
File dir = new File(path);
File[] files=dir.listFiles();
if (files != null && files.length > 0) {
	for(int i=0; i<files.length; i++)
	{
		File file = files[i];
	    if(file.isDirectory() && !file.isHidden())
		{
			JSONObject project = new JSONObject();
			project.put("text", file.getName());
			project.put("iconCls", "bogus");
			project.put("id", file.getName());
			projects.put(project);
			File propFile = new File(file, "jbi.xml");
			if (propFile.exists()) {
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(propFile);
				Element rootEl = doc.getRootElement();
				Namespace ns = rootEl.getNamespace();
				Element saEl, idenEl;
				if ((saEl=rootEl.getChild("service-assembly", ns))!=null && (idenEl=saEl.getChild("identification", ns))!=null) {
					String name = idenEl.getChildText("name", ns);
					String desc = idenEl.getChildText("description", ns);
					project.put("text", desc!=null ? desc : (name!=null ? name : file.getName()));
					Namespace tns = JdomUtil.getAdditionalNamespace(rootEl, "tns");
					project.put("qtip", tns!=null ? tns.getURI() : "");
				}
			}
		}
	}
}

JSONObject wallDatas = new JSONObject();
JSONArray wallpapersArray = new JSONArray();
String wallDirPath = pageContext.getServletContext().getRealPath("desktop/wallpapers");
File walldir = new File(wallDirPath);
File[] walls = walldir.listFiles();
for (File imgFile : walls) {
	if (!imgFile.getName().endsWith(".db")) {
		JSONObject jso = new JSONObject();
		jso.put("path", "wallpapers/" + imgFile.getName());
		wallpapersArray.put(jso);
	}
}
wallDatas.put("items", wallpapersArray);
out.clear();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache" />
<meta http-equiv="Expires" content="0" />
<title>Application Development Studio</title>

<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<link rel="stylesheet" type="text/css" href="css/desktop.css" />

<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="" src="../package/ux/LongPollingProvider.js"></script>

<!-- DESKTOP -->
<script type="text/javascript" src="js/StartMenu.js"></script>
<script type="text/javascript" src="js/TaskBar.js"></script>
<script type="text/javascript" src="js/Desktop.js"></script>
<script type="text/javascript" src="js/App.js"></script>
<script type="text/javascript" src="js/Module.js"></script>
<script type="text/javascript" src="application.js"></script>

<script type="text/javascript">
	var currentUser = <%=currUser%>;
	var projects = <%=projects%>;
	var wallDatas = <%=wallDatas%>;
	document.oncontextmenu = function(e) { return false; };
</script>

<style type="text/css">
.noneFloat {
	float: none !important;
}

.itemWrap {
	float: left;
	cursor: pointer;
	margin: 8px;
}

.itemWrap img {
	width: 140px;
	height: 110px;
	-moz-border-radius: 3px;
}

.itemWrap {
	float: left;
	cursor: pointer;
	cursor: pointer;
	padding: 3px;
	-moz-border-radius: 3px;
}

.hilited {
	background: #FFC401;
	color: highlighttext;
}
</style>

</head>
<body scroll="no">

	<div id="x-desktop" style="cursor: default;">

		<dl unselectable="on" id="x-shortcuts">

			<dt unselectable="on" id="logger-win-shortcut"
				style="float: right; padding: 0 20px; clear: right;">
				<a href="#"><img src="images/guake.png" /><br /> <span>系统日志</span>
				</a>
			</dt>

			<dt unselectable="on" id="project-explorer-shortcut"
				class="noneFloat">
				<a href="#"> <img src="images/project_explorer.png" /><br /> <span>项目浏览器</span>
				</a>
			</dt>

			<dt unselectable="on" id="ware-explorer-shortcut" class="noneFloat">
				<a href="#"> <img src="images/warehouse_explorer.png" /><br /> <span>公共资源库</span>
				</a>
			</dt>

			<dt unselectable="on" id="deploy-wizard-shortcut" class="noneFloat">
				<a href="#"> <img src="images/publish.png" /><br /> <span>部署向导</span>
				</a>
			</dt>

			<dt unselectable="on" id="prefs-win-shortcut" class="noneFloat">
				<a href="#"> <img src="images/control_panel.png" /><br /> <span>控制面板</span>
				</a>
			</dt>

			<!-- 
         <dt id="express-msgr-shortcut">
            <a href="#"><img src="images/s.gif" /><br/>
            <span>即时通讯</span></a>
        </dt>
         -->
		</dl>
	</div>

	<div id="ux-taskbar">
		<div id="ux-taskbar-start"></div>
		<div id="ux-taskbar-panel-wrap">
			<div id="ux-quickstart-panel"></div>
			<div id="ux-taskbuttons-panel"></div>
		</div>
		<div class="x-clear"></div>
	</div>

	<div id="inactive-container" style="display: none;">
		<div id="statusDiv" unselectable="off"
			style="-moz-user-select: -moz-all;"
			onselectstart="javascript:return true;"></div>
	</div>
</body>
</html>
