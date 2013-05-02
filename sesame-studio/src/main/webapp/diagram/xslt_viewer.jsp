<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@page import="java.util.*"%>
<%
String schema = request.getParameter("schema");
schema = schema!=null ? new String(schema.getBytes("iso8859-1"), "utf-8") : null;

out.clear();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1 plus MathML 2.0 plus SVG 1.1/EN" "http://www.w3.org/2002/04/xhtml-math-svg/xhtml-math-svg.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>转换编辑器</title>
<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />
<link rel="stylesheet" type="text/css" href="../package/schema/resources/SchemaPanel.css" />
<style type="text/css">
html, body {
font: normal 12px arial, verdana;
	margin:0;
	padding:0;
	border: 0 none;
	overflow: auto;
	cursor:default; 
	width:100%; 
	height:100%;
}

.x-view-item {
    padding: 2px; 
}

.x-view-selected {
    background: #DFEDFF; 
    border: 1px solid #6593cf; 
    padding: 1px; 
}

.x-form-item {
	white-space: nowrap;
}

.x-form-field-wrap {
	left: 0px !important;
	top: 0px !important;
}

.x-form-check-wrap {
	left: 0px !important;
	top: 0px !important;
	height: 22px !important;
	width: 100% !important;
}

.x-form-text {
	height: 22px !important;
}

.x-column-inner {
	height: 100%;
}

.x-restrict-tool {
	
}

.x-combo-list-inner {
	left: 0px !important;
	top: 0px !important;
	width: auto !important;
}

.x-label {
	height: auto !important;
	width: auto !important;
}

.x-data-view {
	background-color:white; 
	border:1px solid #B5B8C8; 
	cursor:default;
	width: auto !important;
	height: 150px !important;
	overflow:auto; 
}

.x-column-tree .x-tree-node {
    zoom:1;
}
.x-column-tree .x-tree-node-el {
    /*border-bottom:1px solid #eee; borders? */
    zoom:1;
}
.x-column-tree .x-tree-selected {
    background: #d9e8fb;
}
.x-column-tree  .x-tree-node a {
    line-height:18px;
    vertical-align:middle;
}
.x-column-tree  .x-tree-node a span{
	
}
.x-column-tree  .x-tree-node .x-tree-selected a span{
	background:transparent;
	color:#000;
}
.x-tree-col {
    float:left;
    overflow:hidden;
    padding:0 1px;
    zoom:1;
}

.x-tree-col-text, .x-tree-hd-text {
    overflow:hidden;
    -o-text-overflow: ellipsis;
	text-overflow: ellipsis;
    padding:3px 3px 3px 5px;
    white-space: nowrap;
    font:normal 12px arial, tahoma, helvetica, sans-serif;
}

.x-tree-headers {
    background: #f9f9f9 url(../ext-Ajax/resources/images/default/grid/grid3-hrow.gif) repeat-x 0 bottom;
	cursor:default;
    zoom:1;
}

.x-tree-hd {
    float:left;
    overflow:hidden;
    border-left:1px solid #eee;
    border-right:1px solid #d0d0d0;
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

.xslt-icon-none {
	background-image: none !important;
}

.xslt-line-horz {
	background-image: url('images/horz_line.gif');
	background-position: center;
	background-repeat: repeat-x;
}

.xslt-line-hleft {
	background-image: url('images/horz_line.gif');
	background-position: center left;
	background-repeat: no-repeat;
}

.xslt-line-hright {
	background-image: url('images/horz_line.gif');
	background-position: center right;
	background-repeat: no-repeat;
}

.xslt-tree .x-tree-selected {
	background-color: yellow;
}

</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>

<script type="text/javascript" src="../ext-Ajax/source/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/schema/SchemaNodeUI.js"></script>
<script type="text/javascript" src="../package/xml/xdom.js"></script>
<script type="text/javascript" src="../package/dialog/dialog.js"></script>
<script type="text/javascript" src="../package/chart/canvas.js"></script>

<script type="text/javascript" src="xslt_viewer.js"></script>
<script type="text/javascript">
	document.oncontextmenu = function(e) { return false; };
	var schema = '<%=schema%>';
	
	var debug = function(obj, showMethods, sort){
		var div = Ext.DomHelper.append("status", {tag: 'div', html: '<b>'+obj+'</b>', style:'border-bottom:1px solid gray;'});
		showMethods = (showMethods != false);
		sort = (sort != false);
		if (obj == null || obj.constructor == null) {
			return true;
		}
		var type = typeof(obj);
		if (type)
		{
			if (type == "string" || type == "number") {
				div.innerHTML = type+': '+obj;
				return true;
			}
			if (showMethods && !sort) 
			{
			}
			else 
			{
				var propNames = [];
				if (showMethods) {
					for (var prop in obj) {
						propNames.push(prop);
					}
				}
				else 
				{
					for (var prop in obj) {
						if (typeof obj[prop] != "function") 
						{
							propNames.push(prop);
						}
						else 
						{
							//Ext.DomHelper.append(div, {tag:"div", html: '<font color=blue>'+prop+'</font>: Method'});
						}
					}
				}
				if (sort) {
					propNames.sort();
				}
				Ext.each(propNames, function (prop) {
					Ext.DomHelper.append(div, {tag:"div", html: '<font color=blue>'+prop+'</font>: '+obj[prop]});
				});
				return true;
			}
		}
		for (elem in obj)
		{
			Ext.DomHelper.append(div, {tag:"div", html: '<font color=blue>'+elem+'</font>: '+obj[elem]});
		}
	};

</script>
</head>
<body>

</body>
</html>
