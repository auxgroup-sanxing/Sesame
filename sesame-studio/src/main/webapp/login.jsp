<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.utils.*"%>

<html>
<head>
<title>Sesame Development Studio</title>

<META http-equiv="Content-Type" content="text/html; charset=utf-8" />
<META http-equiv="Pragma" content="no-cache" />
<META http-equiv="Cache-Control" content="no-cache" />
<META http-equiv="Expires" content="0" />

<link rel="stylesheet" type="text/css"
	href="ext-Ajax/resources/css/ext-all.css" />
<link rel="stylesheet" type="text/css"
	href="ext-Ajax/resources/css/xtheme-gray.css" />

<style type="text/css">
</style>

<script type="text/javascript" src="ext-Ajax/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="ext-Ajax/ext-all.js"></script>
<script type="text/javascript"
	src="ext-Ajax/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="package/secure/md5.js"></script>
<script type="text/javascript">

	Ext.BLANK_IMAGE_URL="ext-Ajax/resources/images/default/s.gif";

	var contextPath = '<%=request.getContextPath()%>';
	var realm = 'Sesame Studio';
	var nonce = '<%=session.getId().toLowerCase()%>';
	var qop = 'auth';
	var nc = '00000001';
	var cnonce = hex_md5(new Date().getTime()+'').substring(16);

	function showException(response){
		var statusText = response.responseText;
		if (!statusText)
		{
			switch (response.status) 
			{
				case -1:
					alert('通讯超时，事务终止');  return;
				case 0:
					alert('通讯错误，连接服务器失败');   return;
				default:
					alert (response.statusText+'('+response.status+')');  return;
			}
			statusText = '<table style="width:100%;">'+
				'<tr><td style="background: url(\'images/top_bg.gif\') no-repeat; vertical-align:middle; font: bold 24px \'Times New Roman\', Verdana, Arial; height:54px; color:white;">Application Management Center</td></tr>'+
				'<tr><td><table align="center" border="0" cellpadding="0" cellspacing="8" width="100%"><tbody><tr>'+
		        '<td style="text-align: center; width:50px;"><img src="images/notice.gif"/></td>'+
		        '<td style="white-space: normal; overflow: hidden; font-size:12px;">'+statusText+'</td>'+
			    '</tr></tbody></table></td></tr></table>';
		}

		Ext.MessageBox.show({
			title: '错误提示',
			msg: '<div style="width:100%; height:auto; background-color:white; overflow:auto;">'+statusText+'</div>',
			buttons: Ext.MessageBox.OK,
			width: 492
		});
	}

	Ext.onReady(function(){
		Ext.QuickTips.init();
		
        var dlg = new Ext.Window({
            title: '登录',
			width: 400,
            shim:false,
            animCollapse: false,
            autoCreate: true,
            closable: false,
            resizable: false,
            maximizable: false,
            listeners: {
        		activate: function(sender) {
        			this.getComponent(0).getForm().findField('username').focus(true, 200);
        		},
            	render: function(sender){
					this.body.setVisible(true);
					//this.alignTo(Ext.getBody(), 'br', [-490, -260]);
            	}
        	},
        	layout: 'fit',
			items: [{
		        xtype: 'form',
		        autoHeight: true,
			    labelWidth: 80,
			    border: false,
			    bodyStyle:'padding:10px; text-align:left;',
				buttonAlign: 'right',
				defaults: {anchor:"-18"},
				layout: 'form',
				items: [{
		            fieldLabel : '用  户',
					name : 'username',
					xtype : 'textfield',
					allowBlank : false
				},{
					fieldLabel : '密  码',
					name : 'passwd',
					xtype : 'textfield',
					inputType: 'password'
				}]
			}],
			keys: {
				key: Ext.EventObject.ENTER,
				fn: function(){ 
					dlg.btnLogin.handler();
				}
			},
			buttons: [{
				ref: '../btnLogin',
		        text: '登录',
				handler: function(){
					var form = dlg.getComponent(0).getForm();
					if (!form.isValid()) return;
					
					var values = form.getValues();
					var username = values['username'];
					var passwd= values['passwd'];
					var ha1 = hex_md5(username+':'+realm+':'+passwd);
					var ha2 = hex_md5('POST:'+contextPath+'/LoginAction');
					var digist = hex_md5(ha1+':'+nonce+':'+nc+':'+cnonce+':'+qop+':'+ha2);
					var auth = 'Digest username="'+username+'", realm="'+realm+'", nonce="'+nonce+'", uri="'+
						contextPath+'/LoginAction", response="'+digist+'", qop=auth, nc='+nc+', cnonce="'+cnonce+'"';
					Ext.Ajax.request({
						method: 'POST', 
						url: "LoginAction", 
						headers : { Authorization : auth },
						success: function(response, options){ 
							window.location.replace("desktop/index.jsp");
						},
						failure: function(response, options){
							nonce = response.getResponseHeader('nonce');
							showException(response); 
						}
					});
				
				}
			},{
		        text: '重置',
				handler: function(){
					var form = dlg.getComponent(0).getForm();
					form.reset();
				}
			}]
        });
		dlg.show();
	});

</script>
</head>

<body
	style="background: #ffffff url('images/studio.png') top center no-repeat;">

</body>
</html>