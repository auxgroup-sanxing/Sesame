<%@page language="java" contentType="text/html; charset=utf-8"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>交易监控</title>
<link rel="stylesheet" type="text/css"
	href="../ext-Ajax/resources/css/ext-all.css" />
<link rel="stylesheet" type="text/css"
	href="../package/ux/css/Spinner.css" />
<style type="text/css">
.advance {
	background-image: url("../images/icons/advance.png") !important;
}

.innerImg {
	background: no-repeat;
	margin-left: -5px;
	margin-top: -3px;
	cursor: pointer;
}

.mutilWinTextField {
	margin-bottom: 6px !important;
}

.transactionCodeS {
	margin-left: -61px;
	position: fixed;
	width: 445px !important;
}

.x-date-bottom {
	font-size: 12px;
}
</style>
<script type="text/javascript" src="../ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="../ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="../package/ux/SearchField.js"></script>
<script type="text/javascript" src="../package/ux/Spinner.js"></script>
<script type="text/javascript" src="../package/ux/SpinnerField.js"></script>
<script type="text/javascript" src="../package/ux/DateTimeField.js"></script>
<script type="text/javascript"
	src="../package/ux/LongPollingProvider.js"></script>
<script type="text/javascript" src="transaction_viewer.js"></script>
</head>

<body style="height: 100%">
	<script type="text/javascript">
if (window.top && window.top.Application) {
	var Application = window.top.Application;
}
</script>
</body>
</html>