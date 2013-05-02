document.oncontextmenu = function(e) {return false;};
Ext.BLANK_IMAGE_URL='../images/s.gif';
Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

var Viewer = function(){
	var dialog;
	
	return {
		init: function(){
			var _this = this;
			Ext.QuickTips.init();
			
			if (!dialog) {
				dialog = new Ext.form.FormPanel({
					id: 'comm-form',
					title: '参数设置',
					animCollapse: false,
					autoScroll: true,
					border: false,
					collapsible: false,
					labelWidth: 120,
					height: 120,
					bodyStyle: 'padding:5px 5px 0; text-align:left;',
					layout: 'form',
					defaults: {
						anchor: "-18",
						stateful: false
					},
					items: [{
						xtype: 'textfield',
						fieldLabel: 'JNDI名称',
						id: "jndi-name",
						name: "jndi-name",
						labelStyle: 'text-align:left;',
						allowBlank: false,
					},{
						xtype: 'textfield',
						fieldLabel: '消息类型',
						id: "type",
						name: "type",
						labelStyle: 'text-align:left;',
						allowBlank: false,
					},{
						xtype: 'numberfield',
						fieldLabel: '消息路由端口',
						id: "activemq-broker-port",
						name: "activemq-broker-port",
						labelStyle: 'text-align:left;',
						allowBlank: false,
						minText: '端口最小值为1024',
						maxText: '端口最大值为65534',
						nanText: '请输入1024~65534之间的端口号',
						minValue: 1024,
						maxValue: 65534
					}],
					footerCssClass: 'x-window-bc',
					buttons: [{
						scope: this,
						text: '保存',
						handler: function() {
							var data = dialog.getForm().getValues(true);
							
							Ext.getBody().mask('正在保存参数...', 'x-mask-loading');
							Ext.Ajax.request({
								method: 'POST',
								url: 'jms_ctrl.jsp',
								params: {
									operation: 'save',
									data: data
								},
								callback: function(options, success, response){
									if (success) {
										Ext.getBody().mask('保存成功', 'x-mask-loading');
										Ext.getBody().unmask.defer(1000, Ext.getBody());
									} else {
										Ext.getBody().unmask();
										_this.showException(response);
									}
								}
							});
						}
					}, {
						scope: this,
						text: '重载',
						handler: function() {
							location.reload();
						}
					}],
					listeners: {
						afterLayout: function() {
							Ext.Ajax.request({
								method: 'GET',
								url: 'jms_ctrl.jsp',
								params: {operation: 'load'},
								callback: function(options, success, response){
									if (success) {
										var data = Ext.decode(response.responseText);
										var items = data.items;
										if (items.length > 0)
										Ext.each(items, function(item){
											for (var key in item) {
												var value = item[key];
												dialog.getComponent(key).setValue(value);
											}
										});
									} else {
										_this.showException(response);
									}
								}
							});
						}
					}
				});
				
				viewport = new Ext.Viewport({
					layout : 'fit',
					items : dialog
				});
			}
		},
		
		showException : function(response){
			var statusText = response.responseText;
			if (!statusText)
			{
				switch (response.status) 
				{
					case -1:
						statusText = '通讯超时，事务终止';  break;
					case 0:
						statusText = '通讯错误，连接服务器失败';   break;
					default:
						alert (response.statusText+'('+response.status+')');  return;
				}
				statusText = '<table style="width:100%;">'+
					'<tr><td style="background: url(\'../images/top_bg.gif\') no-repeat; vertical-align:middle; font: bold 24px \'Times New Roman\', Verdana, Arial; height:54px; color:white;">Application Management Center</td></tr>'+
					'<tr><td><table align="center" border="0" cellpadding="0" cellspacing="8" width="100%"><tbody><tr>'+
			        '<td style="text-align: center; width:50px;"><img src="../images/notice.png"/></td>'+
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
	}
}();

Ext.onReady(Viewer.init, Viewer, true);