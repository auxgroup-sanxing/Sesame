<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=utf-8"
	errorPage="../exception.jsp"%>
<%@page import="com.sanxing.studio.action.*"%>
<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="java.io.File"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="org.jdom.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.jdom.xpath.XPath"%>

<%
	String userId = request.getParameter("userid");
	SAXBuilder builder = new SAXBuilder();
	File file = UserAction.getUserFile(getServletContext());
	JSONArray roleArray = new JSONArray();
	try
	{
		if (userId==null) throw new MessageException("没有指定要设置角色的帐户");
		userId = new String(userId.getBytes("iso8859-1"), "utf-8");

		Document document = builder.build(file);
		Element root = document.getRootElement();
	    Element userEl = (Element)XPath.selectSingleNode(root, "user[@userid='"+userId+"']");
		if (userEl==null) throw new MessageException("指定帐户不存在");

	    List roles = XPath.selectNodes(document, "/users/roles/role");
		for (Iterator iter=roles.iterator(); iter.hasNext();)
		{
			Element roleEl = (Element)iter.next();
			List attributes = roleEl.getAttributes();
			JSONObject role = new JSONObject();
			for (Iterator at=attributes.iterator(); at.hasNext();)
			{
				Attribute attr= (Attribute)at.next();
				role.put(attr.getName(), attr.getValue());
			}
			String roleId = roleEl.getAttributeValue("id");
			role.put("checked", userEl!=null && XPath.selectSingleNode(userEl, "role[@id='"+roleId+"']")!=null);
			
			roleArray.put(role);
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
<title>更改用户角色</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<style type="text/css">
<!--
TD {
	font-size: 12px;
	vertical-align: top;
}
-->
</style>

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
	Ext.BLANK_IMAGE_URL='../images/s.gif';
	var workbench = window.top && window.top.Workbench ? window.top.Workbench : null;
	
	var userId='<%=userId%>';
	var roleArray=<%=roleArray%>;
	Ext.onReady(function(){
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
	    layout.beginUpdate();
	    layout.add('center', new Ext.ContentPanel(funcEl, {title:'角色', fitToFrame:true, autoScroll:true}));
	    layout.endUpdate();

	    var tree = new Ext.tree.TreePanel(funcEl.createChild({tag:'div', id:Ext.id()}),
	    	{
				rootVisible: false,
				lines: false,
				containerScroll: true,
				autoScroll:true,
		        animate:false
	    	}
	    );
	    var root = new Ext.tree.TreeNode({
			text:'角色',
			isLeaf: false,
			id: 'ROOT',
			allowSet:true
		});
		tree.setRootNode(root);
		if (Ext.type(window.roleArray)=="array")
		{
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
			Ext.each(roleArray, function(role){
				var roleNode = new Ext.tree.TreeNode({
					id: role.id, text: role.name, 
					checked: role.checked, 
					icon:'../images/icons/user.png'
				});
				roleNode.on('checkchange', checkChange);
				root.appendChild(roleNode);
			});
		}
	    tree.render();
	    
		Ext.get('btnSubmit').on('click', function(){
			var form = document.forms[0];
			var roles = [];
			Ext.each(tree.getChecked(), function(node){
				var url=node.attributes.url;
				roles.push(node.id);
			});
			//var added="", removed="";
			form.roles.value=Ext.encode(roles);
			form.submit();
		});
	});
-->
</script>
</head>
<body style="margin-top: 5px;">
	<p style="font-weight: bold;"><%="更改 "+userId+" 的角色"%></p>
	<form name="form" action="../UserAction" method="post">
		<input type="hidden" name="action" value="changeRole"> <input
			type="hidden" name="userid" value="<%=userId%>"> <input
			type="hidden" name="roles" value="">
		<table style="width: 100%;">
			<tr>
				<td align="left">
					<div id="layout" style="font-size: 14px; height: 350px;"></div>
				</td>
			</tr>
			<tr>
				<td align="right"><input type="button" id="btnSubmit"
					value="更改角色" disabled="disabled"> <input type="button"
					value="取消" onclick="window.history.back(-1)"></td>
			</tr>
		</table>
	</form>
</body>
</html>