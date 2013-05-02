<%@page language="java" contentType="text/html; charset=utf-8"%>
<%
out.clear();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>部署向导</title>
<link rel="stylesheet" type="text/css" href="../ext-Ajax/resources/css/ext-all.css" />

<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript" src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>

<style type="text/css">
.x-tab-strip-spacer {
	display: none;	
}

ul.x-tab-strip li {
	margin-left: 0px;
}

.comboSpace {
	padding-top: 15px!important;
}

.tipInfo {
	font-weight: normal!important;
}

.item-wrap {
	font-size: 12px;
	float: left;
	width: 260px;
	padding-bottom: 8px!important;
}

.itemNameWrap {}

.checkboxWrap {
	float: left;
}

.descWrap {
	float: left;
	padding-left: 2px; 
}

#libView, #compView, #transView {
	font-size: 12px;
}

.fieldClass {
	margin-top:4px;
}

.pathClass {
	display: none;
}
</style>
</head>

<body style="height: 100%">
<script type="text/javascript" src="deploy_wizard.js"></script>

<script type="text/javascript">
   	if (window.top && window.top.Application)
   	{
		Application = window.top.Application;
		debug = Application.debug;
	}

</script>

</body>
</html>