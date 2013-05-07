<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=utf-8"
	errorPage="../exception.jsp"%>
<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.action.*"%>
<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="java.io.File"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="org.jdom.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.jdom.xpath.XPath"%>

<%
	String roleId = request.getParameter("roleid");
	String roleName = request.getParameter("rolename");
	String desc = request.getParameter("desc");
	roleId = roleId!=null ? new String(roleId.getBytes("iso8859-1"), "utf-8") : "";
	roleName = roleName!=null ? new String(roleName.getBytes("iso8859-1"), "utf-8") : "";
	desc = desc!=null ? new String(desc.getBytes("iso8859-1"), "utf-8") : "";
	
	SAXBuilder builder = new SAXBuilder();
	File file = UserAction.getUserFile(getServletContext());
	File pluginFile = new File("."); //AuthorityFilter.getPluginFile(getServletContext());
	JSONObject accredit = new JSONObject();
	try
	{
		Document document = builder.build(file);
		Element root = document.getRootElement();
	    List list = XPath.selectNodes(root, "roles/role[@id='"+roleId+"']/accredit");
		for (Iterator iter=list.iterator(); iter.hasNext();)
		{
			Element accreditEl = (Element)iter.next();
			String name = accreditEl.getAttributeValue("type");
			JSONObject sub = new JSONObject();
			accredit.put(name, sub);
			List items = accreditEl.getChildren();
			JSONObject menu = new JSONObject();
			for (Iterator it=items.iterator(); it.hasNext();)
			{
				Element item= (Element)it.next();
				sub.put(item.getName(), item.getText());
			}
		}
	}
	catch(Exception e)
	{
		e.printStackTrace();
		WebUtil.sendError(response, e.getMessage());
		return;
	}
	out.clear();
%>
<html>
<head>
<title>更改角色权限</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<style type="text/css">
<!--
TD {
	font-size: 12px;
	vertical-align: middle;
}
-->
</style>

<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>

<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-core.js"></script>
<script type="text/javascript" src="../ext-Ajax/package/util.js"></script>
<script type="text/javascript" src="../ext-Ajax/package/date.js"></script>
<script type="text/javascript" src="../ext-Ajax/package/widget-core.js"></script>
<script type="text/javascript" src="../ext-Ajax/package/qtips/qtips.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/package/button/button.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/package/toolbar/toolbar.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/package/dragdrop/dragdrop.js"></script>
<script type="text/javascript" src="../ext-Ajax/package/splitbar.js"></script>
<script type="text/javascript" src="../ext-Ajax/package/data/data.js"></script>
<script type="text/javascript" src="../ext-Ajax/package/form/form.js"></script>
<script type="text/javascript" src="../ext-Ajax/package/tabs/tabs.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/package/layout/layout.js"></script>
<script type="text/javascript" src="../ext-Ajax/package/menu/menus.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/package/datepicker/datepicker.js"></script>
<script type="text/javascript" src="../ext-Ajax/package/tree/tree.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/build/widgets/View-min.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/build/widgets/LoadMask-min.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/build/state/State-min.js"></script>
<!-- include the locale file -->
<script type="text/javascript"
	src="../ext-Ajax/source/locale/ext-lang-zh_CN.js"></script>

<script type="text/javascript">
<!--
	if (window.top && window.top.Workbench)
	{
		window.workbench = window.top.Workbench;
	}
	else
	{
		document.location.href='../error404.jsp';
	}
	
	function deleteRole(anchor){
		if (!window.confirm("确实要删除角色吗？")) return;
		var form = document.forms[0];
		form.elements['action'].value = "removeRole";
		form.submit();
	}
	
	var roleId='<%=roleId%>';
	var accredit=<%=accredit%>;
	
	Ext.BLANK_IMAGE_URL='../images/s.gif';
	Ext.onReady(function(){
		<%if (!roleId.equals("")) {%>
		var form = document.forms[0];
		form.elements['roleid'].setAttribute("readonly", "readonly");
		<%}%>
		var layout = new Ext.BorderLayout("layout", {
	        center: {
	            titlebar: false,
				fitToFrame: true,
	            autoScroll: false,
	            alwaysShowTabs: true,
	            closeOnTab: true
	        }
		});
		var funcEl = layout.getEl().createChild({tag:'div', id:Ext.id()});
		var viewEl = layout.getEl().createChild({tag:'div', id:Ext.id()});
		var editEl = layout.getEl().createChild({tag:'div', id:Ext.id()});
	    layout.beginUpdate();
	    layout.add('center', new Ext.ContentPanel(funcEl, {title:'功能授权', fitToFrame:true, autoScroll:true}));
	    layout.add('center', new Ext.ContentPanel(viewEl, {title:'视图授权', fitToFrame:true, autoScroll:true}));
	    layout.add('center', new Ext.ContentPanel(editEl, {title:'编辑授权', fitToFrame:true, autoScroll:true}));
	    layout.endUpdate();
	    layout.showPanel(funcEl.id);

		var checkChange = function(node, checked) {
			Ext.get('btnSubmit').dom.disabled='';
			if (checked)
			{
				node.bubble(function(node){
					if (node.id!="ROOT" && node.ui.isChecked()==false) {
						node.attributes.checked=true;
						node.ui.toggleCheck(true);
					}
					return true;
				});
			}
			else
			{
				node.cascade(function(node){
					if(node.ui.isChecked()==true) {
						node.attributes.checked=false;
						node.ui.toggleCheck(false);
					}
					return true;
				});
			}
		};

	    var menuTree = new Ext.tree.TreePanel(funcEl.createChild({tag:'div', id:Ext.id()}),
	    	{
				rootVisible: false,
				lines: true,
				containerScroll: true,
				autoScroll:true,
		        animate:false
	    	}
	    );
	    var menuRoot = new Ext.tree.TreeNode({
			text:'菜单',
			isLeaf: false,
			id: 'ROOT',
			allowSet:true
		});
		menuTree.setRootNode(menuRoot);
		if (top.pluginContext)
		{
			var menuAccr = accredit.menu;
			var iterate = function(item, parentNode){ 
				if (item=="-" || item.menu==undefined && item.accredit!="true") return;
				var node = new Ext.tree.TreeNode({
					id: item.id, text: item.text, url:item.url, 
					checked: menuAccr!=undefined && menuAccr[item.id]!=undefined //item.checked
				});
				node.on('checkchange', checkChange);
				parentNode.appendChild(node);
				if (item.menu)
				{
					node.attributes.icon = '../images/icons/menu.gif';
					var items = item.menu.items;
					for (var i=0,len=items.length; i<len; i++)
					{
						var subItem = items[i];
						iterate(subItem, node);
					};
				}
			};
			Ext.each(top.pluginContext.menus, function(item){
				iterate(item, menuRoot);
			});
		}
	    menuTree.render();
	    
	    var viewTree = new Ext.tree.TreePanel(viewEl.createChild({tag:'div', id:Ext.id()}),
	    	{
				rootVisible: false,
				lines: true,
				containerScroll: true,
				autoScroll:true,
		        animate:false
	    	}
	    );
	    var viewRoot = new Ext.tree.TreeNode({
			text:'视图',
			isLeaf: false,
			id: 'ROOT',
			allowSet:true
		});
		viewTree.setRootNode(viewRoot);
		if (top.pluginContext)
		{
			var viewAccr = accredit.view;
			Ext.each(top.pluginContext.views, function(item){
				if (item.accredit!="true") return;
				var node = new Ext.tree.TreeNode({
					id: item.id, text: item.title, url:item.url, 
					checked: viewAccr!=undefined && viewAccr[item.id]!=undefined,
					icon: '../images/icons/defaultview_misc.gif'
				});
				node.on('checkchange', checkChange);
				viewRoot.appendChild(node);
			});
		}
	    viewTree.render();
	    
	    var editTree = new Ext.tree.TreePanel(editEl.createChild({tag:'div', id:Ext.id()}),
	    	{
				rootVisible: false,
				lines: true,
				containerScroll: true,
				autoScroll:true,
		        animate:false
	    	}
	    );
	    var editRoot = new Ext.tree.TreeNode({
			text:'编辑器',
			isLeaf: false,
			id: 'ROOT',
			allowSet:true
		});
		editTree.setRootNode(editRoot);
		if (top.pluginContext)
		{
			var editAccr = accredit.edit;
			Ext.each(top.pluginContext.editors, function(item){
				if (item.accredit!="true") return;
				var node = new Ext.tree.TreeNode({
					id: item.id, text: item.title, url:item.url, 
					checked: editAccr!=undefined && editAccr[item.id]!=undefined,
					icon: '../images/icons/toolbar.gif'
				});
				node.on('checkchange', checkChange);
				editRoot.appendChild(node);
			});
		}
	    editTree.render();

		Ext.get('btnSubmit').on('click', function(){
			var form = document.forms[0];
			if (form.roleid.value=="")
			{
				alert("角色标识不能为空");  form.roleid.focus();
				return;
			}
			if (form.rolename.value=="")
			{
				alert("角色名称不能为空");  form.rolename.focus();
				return;
			}

			var accredit = {menu:{}, view:{}, edit:{}};
			Ext.each(menuTree.getChecked(), function(node){
				var url=node.attributes.url;
				accredit.menu[node.id] = url?url:'';
			});
			Ext.each(viewTree.getChecked(), function(node){
				var url=node.attributes.url;
				accredit.view[node.id] = url?url:'';
			});
			Ext.each(editTree.getChecked(), function(node){
				var url=node.attributes.url;
				accredit.edit[node.id] = url?url:'';
			});
			form.elements['action'].value = '<%=roleId.equals("")?"createRole":"modifyRole"%>';
			form.accredit.value=Ext.encode(accredit);
			form.submit();
		});
	});
-->
</script>
</head>
<body style="margin-top: 5px;">
	<p style="font-weight: bold;"><%=roleName.equals("")?"创建新角色":"更改角色 "+roleName+""%></p>
	<table style="margin: 5px 0px 5px 0px;">
		<%if (!roleId.equals("")) {%>
		<tr>
			<td><img src="../images/icons/user_delete.png"></td>
			<td><a onclick='deleteRole(this)' href="#" target="_self">删除角色</a></td>
		</tr>
		<%}%>
	</table>
	<form name="form" action="../UserAction" method="post">
		<input type="hidden" name="action" value="changeRole"> <input
			type="hidden" name="accredit" value="">

		<table style="margin: 0px 0px 5px 0px; width: 100%;">
			<tr>
				<td>角色标识:</td>
				<td><input type="text" name="roleid" value="<%=roleId%>"
					onchange="Ext.get('btnSubmit').dom.disabled='';"></td>
				<td rowspan="2">角色描述:</td>
				<td rowspan="2"><textarea style="width: 100%;" name="desc"
						onchange="Ext.get('btnSubmit').dom.disabled='';"><%=desc%></textarea></td>
			</tr>
			<tr>
				<td>角色名称:</td>
				<td><input type="text" name="rolename" value="<%=roleName%>"
					onchange="Ext.get('btnSubmit').dom.disabled='';"></td>
			</tr>
		</table>
	</form>
	<table style="width: 100%;">
		<tr>
			<td align="left">
				<div id="layout" style="font-size: 14px; height: 320px;"></div>
			</td>
		</tr>
		<tr>
			<td align="right"><input type="button" id="btnSubmit" value="提交"
				disabled="disabled"> <input type="button" value="取消"
				onclick="window.history.back(-1);"></td>
		</tr>
	</table>
</body>
</html>