<%@page language="java" contentType="text/html; charset=utf-8"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>服务视图</title>
<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<style type="text/css">
.item-wrap {
	font: 12px arial, tahoma, helvetica, sans-serif;
	background: url("../images/obj16/folder.gif") no-repeat 2px 3px;
	padding: 3px 7px 3px 20px !important;
}

.hilited {
	background: #EFEFEF url("../images/obj16/folder.gif") no-repeat 2px 3px;
	color: black;
}

.x-form-field-wrap .x-form-search-trigger {
	background-image: url("../images/search-trigger.gif");
	cursor: pointer;
}

.x-form-element {
	padding-left: 0px !important;
	position: relative;
}

.advance {
	background-image: url("../images/icons/advance.png") !important;
}

.innerImg {
	float: left;
	padding-left: 25px;
	margin-top: -4px;
}

.innerDiv {
	float: left;
	padding-left: 5px;
}
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/ux/SearchField.js"></script>
<script type="text/javascript" src="service_viewer.js"></script>
</head>

<body style="height: 100%">
	<script type="text/javascript">
if (window.top && window.top.Application) {
	var Application = window.top.Application;
}
</script>
</body>
</html>