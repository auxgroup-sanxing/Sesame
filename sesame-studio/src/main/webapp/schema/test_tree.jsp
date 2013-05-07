<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page import="java.io.File"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%
	out.clear();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>模式编辑</title>
<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<link rel="stylesheet" type="text/css"
	href="../package/schema/resources/SchemaPanel.css" />

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
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../package/schema/SchemaNodeUI.js"></script>

<script type="text/javascript">
	var debug = window.top && window.top.Worbench ? window.top.Workbench.debug : function(){ alert('Not supported!');};
	
	Ext.BLANK_IMAGE_URL='../images/s.gif';
	Ext.onReady(function(){
		
    	var viewDiv = Ext.DomHelper.append(document.body, {tag:"div", id:"viewDiv", style:"width:600px; height:400px;" });
		
		//treeLoader.on('loadexception', function(loader, node, response){ Workbench.showException(response); });
		var er_view = new Ext.tree.TreePanel({
			el: 'viewDiv',
			loader: new Ext.tree.TreeLoader({preloadChildren: true, clearOnLoad: false}), //treeLoader,
			autoScroll: true,
	        animate: false,
	        bodyStyle: 'padding:5px; background-color:white;',
			containerScroll: true,
			lines: false,
			rootVisible: true,
	        trackMouseOver: false
	    });

	    var root = new Ext.tree.AsyncTreeNode({
			id: 'SCHEMA',
			text:'Schema',
			iconCls: 'xsd-icon-schema',
			expanded: true,
			uiProvider: Ext.ux.SchemaBoxUI,
			//icon: "../images/icons/plugin.gif",
			children: [],
			allowAppend: true
		});
		er_view.setRootNode(root);
	    er_view.render();

		var directives=new Ext.tree.AsyncTreeNode({
			id: 'DIRECTIVES',
			text: "指示",
			iconCls: 'xsd-icon-directives', 
			expanded: true,
			uiProvider: Ext.ux.SchemaBoxUI,
			leaf: false,
			children: []
		});
		root.appendChild(directives);
		
		directives.appendChild(new Ext.tree.AsyncTreeNode({
			text: "abc.xsd {http://www.asodiw.xml.xsd}",
			iconCls: 'xsd-icon-import', 
			children: [],
			uiProvider: Ext.ux.SchemaNodeUI,
			leaf: true
		}));
		
		var elements=new Ext.tree.AsyncTreeNode({
			id: 'ELEMENTS',
			text: "元素",
			iconCls: 'xsd-icon-elements', 
			expanded: true,
			uiProvider: Ext.ux.SchemaBoxUI,
			leaf: false,
			children: []
		});
		root.appendChild(elements);
		
		var types=new Ext.tree.AsyncTreeNode({
			id: 'TYPES',
			text: "类型",
			iconCls: 'xsd-icon-types', 
			expanded: true,
			uiProvider: Ext.ux.SchemaBoxUI,
			leaf: false,
			children: []
		});
		root.appendChild(types);
		//Ext.get(types.ui.wrap).applyStyles({float:'right', width:'40%'});

		var node=new Ext.tree.AsyncTreeNode({
			text: "name : string",
			iconCls:'xsd-icon-element', 
			children: [],
			uiProvider: Ext.ux.SchemaNodeUI,
			expanded: true, 
			allowModify:true
		});
		elements.appendChild(node);
		var node1=new Ext.tree.AsyncTreeNode({
			text: "address : AddressType",
			qtip: '真好啊',
			uiProvider: Ext.ux.SchemaNodeUI,
			iconCls: 'xsd-icon-element', 
			children: [],
			leaf: false
		});
		elements.appendChild(node1);
/*		
		child=new Ext.tree.AsyncTreeNode({
			text: "并行", 
			icon:'../images/icons/thread_view.gif', 
			isLeaf: true, allowModify:true, allowDelete:true
		});
		loop.appendChild(child);
		
		var branch1 = child.appendChild(new Ext.tree.AsyncTreeNode({
			text: "任务2", 
			annotation: '触发', 
			icon:'../images/icons/thread_view.gif', 
		}));
		var branch2 = child.appendChild(new Ext.tree.AsyncTreeNode({
			text: "任务3", 
			icon:'../images/icons/thread_view.gif', 
		}));
		var branch3 = child.appendChild(new Ext.tree.AsyncTreeNode({
			text: "分支4", 
			icon:'../images/icons/thread_view.gif', 
		}));
		child5 = branch3.appendChild(new Ext.tree.AsyncTreeNode({
			text: "任务5", 
			icon:'../images/icons/thread_view.gif', 
			isLeaf: true, allowModify:true, allowDelete:true
		}));
		branch1.appendChild(new Ext.tree.AsyncTreeNode({
			text: "任务11", 
			icon:'../images/icons/thread_view.gif', 
			isLeaf: true, allowModify:true, allowDelete:true
		}));
		branch2.appendChild(new Ext.tree.AsyncTreeNode({
			text: "任务12", 
			icon:'../images/icons/thread_view.gif', 
			isLeaf: true, allowModify:true, allowDelete:true
		}));
		branch3.appendChild(new Ext.tree.AsyncTreeNode({
			text: "任务13", 
			icon:'../images/icons/thread_view.gif', 
			isLeaf: true, allowModify:true, allowDelete:true
		}));
		loop.appendChild(new Ext.tree.AsyncTreeNode({
			text: "任务7", 
			icon:'../images/icons/thread_view.gif', 
			isLeaf: true, allowModify:true, allowDelete:true
		}));
*/
	});
</script>
</head>
<body>
</body>
</html>
