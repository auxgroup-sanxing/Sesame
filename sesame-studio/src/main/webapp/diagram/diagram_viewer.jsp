<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page import="java.util.*,  java.io.*"%>
<%@page
	import="com.sanxing.studio.*,com.sanxing.studio.utils.*,com.sanxing.studio.team.svn.*,com.sanxing.studio.team.*"%>
<%@page import="com.sanxing.sesame.engine.ExecutionEnv"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.dom4j.*, org.dom4j.io.*"%>
<%@page import="org.json.*"%>
<%!
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
%>
<%
	String service = request.getParameter("service");
	boolean isPublic = request.getParameter("isPublic") == null ? false : true;
	
	service = service != null ? new String(service.getBytes("iso8859-1"), "utf-8") : null;
	
	JSONArray envList = new JSONArray();
	
	JSONArray xpathExt = new JSONArray();
	JSONArray xsltExt = new JSONArray();
	
	String isVersioned = "true";
	String isLocked = "false";
	
	try {
		JSONObject user = Authentication.getCurrentUser();
		String userName = user.optString("userid");
		
		String xsdFileName = service.replaceFirst("\\.xml$",".xsd");
		File xsdFile = Configuration.getWorkspaceFile(xsdFileName);
		File unitFolder = xsdFile.getParentFile();
		ThreeWaySynchronizer sync = SCM.getSynchronizer(unitFolder.getParentFile().getParentFile());
		
		if(null == sync || (!sync.isVersioned(xsdFile))){
			isVersioned = "false";
		} else {
			File wsdlFile = new File(unitFolder, "unit.wsdl");
			if (sync != null && sync.isVersioned(wsdlFile) && sync.isVersioned(xsdFile)) {
				Map<String, ?> props = sync.info(wsdlFile);
				String lock = (String)props.get("lock");
				
				if (lock == null) {
					props = sync.info(xsdFile);
					lock = (String) props.get("lock");
					if (lock != null) {
						if (userName.equals((String) props.get("lock.owner")))
							isLocked = "true";
					}
				} else {	
					if (userName.equals((String) props.get("lock.owner")))
						isLocked = "true";
				}
			}
		}
		
		SAXReader reader = new SAXReader();
		Document jbiDoc = reader.read(new File(unitFolder, "jbi.xml"));
		Element rootEl = jbiDoc.getRootElement();
		Namespace xmlns_comp = rootEl.getNamespaceForPrefix("comp");
		String component = xmlns_comp!=null ? xmlns_comp.getURI() : null;
		
		if (component!=null && component.length()>0) {
			File compBundle = Configuration.getWarehouseFile(component);
			
			Document xpath_doc = reader.read(new File(compBundle, "xpath.ext"));
			
			Document xslt_doc = reader.read(new File(compBundle, "transform.ext"));
			rootEl = xslt_doc.getRootElement();
			List list = rootEl.elements("class");
			for (Iterator iter=list.iterator(); iter.hasNext(); ) {
				Element classEl = (Element)iter.next();
				JSONObject classObj = new JSONObject();
				classObj.put("prefix", classEl.attributeValue("prefix"));
				classObj.put("description", classEl.attributeValue("description"));
				JSONArray items = new JSONArray();
				classObj.put("items", items);
				xsltExt.put(classObj);
				List funcs = classEl.elements("function");
				for (Iterator funcIt=funcs.iterator(); funcIt.hasNext(); ) {
					Element funcEl = (Element)funcIt.next();
					JSONObject funcObj = new JSONObject();
					funcObj.put("itemId", funcEl.attributeValue("name"));
					funcObj.put("text", funcEl.attributeValue("description"));
					funcObj.put("icon", "../images/obj16/function_obj.gif");
					items.put(funcObj);
				}
			}
		}
		
		Map env = ExecutionEnv.export();
		for (Iterator iter=env.keySet().iterator(); iter.hasNext(); ) {
			 String name = (String)iter.next();
			 envList.put(name);
		}
		
		out.clear();
	}
	catch (Exception e){
		logger.debug(e.getMessage(), e);
		throw new ServletException(e.getMessage(), e.getCause()!=null ? e.getCause() : e);
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>流程编辑器</title>
<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<style type="text/css">
html,body {
	font: normal 12px arial, verdana;
	margin: 0;
	padding: 0;
	border: 0 none;
	overflow: auto;
	cursor: default;
	width: 100%;
	height: 100%;
}

.hilited {
	background: highlight;
	color: highlighttext;
}

.x-form-text {
	height: 22px !important;
}

.x-combo-list-inner {
	left: 0px !important;
	top: 0px !important;
	width: auto !important;
}

.rtl-tree {
	direction: rtl;
}

.rtl-tree .x-tree-elbow {
	background-image: url('images/elbow.gif');
}

.rtl-tree .x-tree-elbow-line {
	background-image: url('images/elbow-line.gif');
}

.rtl-tree .x-tree-elbow-minus {
	background-image: url('images/elbow-minus.gif');
}

.rtl-tree .x-tree-elbow-plus {
	background-image: url('images/elbow-plus.gif');
}

.rtl-tree .x-tree-elbow-end {
	background-image: url('images/elbow-end.gif');
}

.rtl-tree .x-tree-elbow-end-minus {
	background-image: url('images/elbow-end-minus.gif');
}

.rtl-tree .x-tree-elbow-end-plus {
	background-image: url('images/elbow-end-plus.gif');
}

.icon-attribute {
	background-image: url("../images/icons/attribute.gif");
}

.icon-envvar {
	background-image: url("../images/obj16/envvar_obj.gif") !important;
}

.icon-global {
	background-image: url("../images/obj16/global_var_obj.gif") !important;
}

.icon-element {
	background-image: url("../images/obj16/element.gif") !important;
}

.icon-string,.icon-text {
	background-image: url("../images/icons/string.gif") !important;
}

.icon-number,.icon-cdata,.icon-boolean,.icon-int,.icon-integer,.icon-double,.icon-float,.icon-decimal,.icon-any
	{
	background-image: url("../images/icons/field_public_obj.gif") !important;
}

.palette_activity_category {
	background-image: url('../images/obj16/action.gif') !important;
	background-position: center;
	background-repeat: no-repeat;
}

.palette_ns {
	background-image: url('../images/obj16/namespace.png') !important;
}

.palette_throw {
	background-image: url('../images/elcl16/critical.png') !important;
}

.palette_try-catch {
	background-image: url('../images/obj16/filenav_nav.gif') !important;
}

.settings {
	background-image: url( ../images/tool16/launch_debug.gif) !important;
}

.xpathHelper {
	background-image: url( ../images/tool16/help_search.gif) !important;
}

.action-hilited .processLineD {
	background-image: url("images/down_array.gif") !important;
}

.action-hilited .processLineTL_whiledo {
	background-image: url('images/top_left_whiledo.gif');
	background-position: center top;
	background-repeat: no-repeat;
}

.line-hilited {
	
}

.line-hilited>tbody>tr>td.processLineV {
	background-image: url('images/vert_line.gif');
	background-position: center;
	background-repeat: repeat-y;
}

.line-hilited>tbody>tr>td.processLineTR {
	background-image: url('images/top_right.gif');
	background-position: center;
	background-repeat: no-repeat;
}

.line-hilited>tbody>tr>td>div.processLineTR {
	background-image: url('images/top_right.gif');
	background-position: center;
	background-repeat: no-repeat;
}

.line-hilited>tbody>tr>td.processLineTL_dowhile {
	background-image: url('images/top_left_dowhile.gif');
	background-position: top;
	background-repeat: no-repeat;
}

.line-hilited>tbody>tr>td>ul>li>div>div.processLineBL_dowhile {
	background-image: url('images/horz_line.gif');
	background-position: center;
	background-repeat: no-repeat;
}

.line-hilited>tbody>tr>td.processLineBL_whiledo {
	background-image: url('images/bottom_left_whiledo.gif');
	background-position: bottom;
	background-repeat: no-repeat;
}

.line-hilited>tbody>tr>td.processLineBR {
	background-image: url('images/bottom_right.gif');
	background-position: bottom;
	background-repeat: no-repeat;
}

.line-hilited>tbody>tr>td>div.processLineBR_dowhile {
	background-image: url('images/bottom_right_dowhile.gif');
	background-position: center;
	background-repeat: no-repeat;
}

td.line-hilited : first-child {
	background-image: url('images/bottom_left_decision.gif');
	background-position: bottom;
	background-repeat: no-repeat;
}

td.line-hilited : last-child {
	background-image: url('images/bottom_right.gif') !important;
	background-position: bottom;
	background-repeat: no-repeat;
}

.debug-mode .x-tree-node-cb {
	display: none;
}

.edit-mode .x-tree-node-cb {
	display: none;
}
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>

<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/process/ProcessNodeUI.js"></script>
<script type="text/javascript"
	src="../package/transform/AssemblePanel.js"></script>
<script type="text/javascript" src="../package/xml/xdom.js"></script>

<script type="text/javascript" src="diagram_viewer.js"></script>
<script type="text/javascript" src="diagram_dialog.js"></script>
<script type="text/javascript">
//document.oncontextmenu = function(e) { return false; };
var servicePath = '<%=service%>';
var isVersioned = '<%=isVersioned%>';
var isLocked = '<%=isLocked%>';
var isPublic = <%=isPublic%>;

var env = <%=envList%>;
var xpath_ext = <%=xpathExt%>;
var xslt_ext = <%=xsltExt%>;

if (window.top && window.top.Application)
{
	Application = window.top.Application;
	debug = Application.debug;
}

</script>

<script type="text/javascript">
Ext.TreeCombo = Ext.extend(Ext.form.ComboBox, {
	mode: 'local',
	store: [],
	
	initList: function() {
		if (this.list != null) {
			return;
		}
		
		this.list = new Ext.tree.TreePanel({
			floating: true,
			animate: false, lines: false,
			containerScroll: true,
			height: 200,
			root: new Ext.tree.AsyncTreeNode({
				text: '.',
				name: '.'
			}),
			loader: this.treeLoader,
			listeners: {
				click: this.onNodeClick,
				scope: this
			},
			alignTo: function(el, pos) {
				this.setPagePosition(this.el.getAlignToXY(el, pos));
			},
			setZIndex: function(zindex){
				
			}
		});
	},

	expand: function() {
		if (!this.list.rendered) {
			this.list.render(document.body);
			this.list.setWidth(this.el.getWidth());
			this.innerList = this.list.body;
			this.list.hide();
		}
		this.el.focus();
		Ext.TreeCombo.superclass.expand.apply(this, arguments);
	},

	doQuery: function(q, forceAll) {
		this.expand();
	},

    collapseIf : function(e){
        if(!e.within(this.wrap) && !e.within(this.list.el)){
            this.collapse();
        }
    },

	onNodeClick: function(node, e) {
		var value = node.getPath('name').substring(1);
		var idx = value.indexOf('/');
		this.setRawValue(idx==-1 ? '.' : value.substring(idx+1));
		if (this.hiddenField) {
			this.hiddenField.value = value;
		}
		this.collapse();
	},
	
	reloadTree: function() {
		if (!this.list) return;
		var node = this.list.getRootNode();
		node.collapse();
		node.reload();
	}
});
Ext.reg('treecombo', Ext.TreeCombo);

</script>
</head>
<body>
</body>
</html>
