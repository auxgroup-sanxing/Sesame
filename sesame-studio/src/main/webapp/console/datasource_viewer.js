//document.oncontextmenu = function(e) {return false;};
Ext.BLANK_IMAGE_URL='../images/s.gif';

var descArray = [
	'BTM(Bitronix Transaction Manager)是一个开源的事务管理器,它完全实现了JTA 1.0.1B标准,可以很好的支持JDBC和JMS两种资源.', 
	'ATM(Statenet Transaction Manager)是statenet平台内部提供的事务管理器,相比 BTM,目前只支持单数据源的JDBC资源,且数据源的JNDI名称必须为atm-atasource.',
	''
];

var Viewer = function(){
	var dialog;
	var nanText = '请输入数字！';
	
	return {
		atmSelected : false,
		
		init: function(){
			var _this = this;
			Ext.QuickTips.init();
			
			var Item = Ext.data.Record.create([
				{name: 'jndi-name'}, 
				{name: 'transaction'},
				{name: 'driver-class'},
				{name: 'url'}, 
				{name: 'username'}, 
				{name: 'password'},
				{name: 'max-idle'},
				{name: 'max-wait'},
				{name: 'max-active'},
				{name: 'initial-size'}]);
			
			var ds = new Ext.data.Store({
				proxy: new Ext.data.HttpProxy({
					url: '../SQLExplorer'
				}),
				baseParams: {
					action: 'loadDataSources'
				},
				reader: new Ext.data.JsonReader({
					root: 'items',
					totalProperty: 'totalCount',
					id: 'name'
				}, Item)
			});
			
			var folderView = new Ext.DataView({
				tpl: new Ext.XTemplate(
					'<tpl for=".">' +
					'<div id="{jndi-name}" class="item-wrap" style="cursor:pointer;padding:1px;white-space:nowrap;">{jndi-name}</div>' +
					'</tpl>'),
				multiSelect: true,
				store: ds,
				itemSelector: 'div.item-wrap',
				selectedClass: 'hilited',
				emptyText: '无数据源列表'
			});
			
			var nameField = new Ext.form.TextField({
				fieldLabel: 'JNDI名称',
				name: 'jndi-name',
				width: 245,
				allowBlank: false
			});
			
			var tsField = new Ext.form.ComboBox({
				fieldLabel: '使用事务管理器',
				name: 'transaction',
				hiddenName: 'transaction',
				allowBlank: false,
				forceSelection: true,
				store: new Ext.data.ArrayStore({
		           fields: ['text', 'value'],
		           data : [['使用', 'true'], ['不使用', 'false']]
			    }),
				triggerAction: 'all', editable: false,
				mode: 'local',
				displayField: 'text',
				valueField: 'value',
				listeners: {
					select: function(sender, rec) {
						if (_this.atmSelected == true) {
							sender.previousSibling().setValue('atm-datasource');
							sender.previousSibling().setReadOnly(true);		
						} else {
							sender.previousSibling().setReadOnly(false);
						}
					}
				}
			});
			
			var tsManagerField = new Ext.form.ComboBox({
				fieldLabel: '事务管理器',
				name: 'transaction-manager',
				hiddenName: 'transaction-manager',
				allowBlank: false,
				forceSelection: true,
				store: new Ext.data.ArrayStore({
		           fields: ['text', 'value'],
		           data : [['不使用', ''], ['Bitronix事务管理器', 'BTM'], ['Statenet事务管理器', 'ATM']]
			    }),
				triggerAction: 'all', editable: false,
				mode: 'local',
				displayField: 'text',
				valueField: 'value',
				listeners: {
					select: function(sender, rec) {
						var value = sender.getValue();
						var desc = sender.nextSibling();
						switch(value) {
							case 'BTM':
								desc.setValue(descArray[0]);
								break;
							case 'ATM':
								desc.setValue(descArray[1]);
								break;
							default:
								desc.setValue(descArray[2]);
								break;
						}
						_this.atmSelected = (value == 'ATM') ? true:false;
					}
				}
			});
			
			var append = function(){
				var p = new Item({
					'jndi-name': '未命名'
				});
				p.set('transaction', '');
				p.set('driver-class', '');
				p.set('url', '');
				p.set('username', ''); 
				p.set('password', '');
				p.set('max-idle', '');
				p.set('max-wait', '');
				p.set('max-active', '');
				p.set('initial-size', '');
				ds.add(p);
			};
			
			var remove = function(){
				if (!window.confirm("确实要删除选中的数据源吗？")) 
					return;
				var records = [];
				Ext.each(folderView.getSelectedIndexes(), function(index){
					records.push(ds.getAt(index));
				});

				var dels = [];		
				Ext.each(records, function(record){
					dels.push(record.data);
					ds.remove(record);
				});
		
				Ext.Ajax.request({
					method: 'post',
					url: 'datasource_ctrl.jsp',
					params: {
						operation: 'abolishDataSource',
						data: Ext.encode(dels)
					},
					callback: function(options, success, response) {
						if (!success)
							Viewer.showException(response);
						saveSetting();	
					}
				});
			};
			
			var publish = function() {
				var records = [];
				Ext.each(folderView.getSelectedIndexes(), function(index){
					records.push(ds.getAt(index));
				});
				publishDataSource(records);
			};
			
			var speedBar = new Ext.Toolbar({
				style : {'border-left' : 'none'}
			});
			speedBar.add({
				id: 'add_conn',
				cls: 'x-btn-text-icon',
				text: '添加',
				handler: append,
				icon: "../images/elcl16/add_obj.gif"
			}, {
				id: 'del_conn',
				cls: 'x-btn-text-icon',
				text: '删除',
				handler: remove,
				icon: "../images/icons/remove.gif",
				disabled: true
			}, {
				id: 'publish_ds',
				cls: 'x-btn-text-icon',
				text: '发布',
				handler: publish,
				icon: "../images/elcl16/synced.gif",
				disabled: true
			});
			var btns = speedBar.items.map;
		
			if (!dialog) {
				dialog = new Ext.Panel({
					autoCreate: true,
					width: Ext.getBody().getWidth(),
					height: Ext.getBody().getHeight(),
					resizable: false,
					stateful: false,
					modal: true,
					frame: false,
					bodyStyle: {
						'border': 'none',
						'border-bottom': '1px solid #99BBE8'
					},
					hideCollapseTool: true,
					header: false,
					layout: 'border',
					items: [
					{
						region: 'north',
						title: '事务管理器',
						xtype: 'form',
						header: false,
						modal: true,
						height: 70,
						defaults: {anchor: '100%', labelWidth: 100},
						bodyStyle: {
							'padding': '5px',
							'height': 'auto',
							'border-top': 'none',
							'border-left': 'none',
							'border-right': 'none',
							'background-color': '#DFE8F6'
						},
						items: [tsManagerField,{
							fieldLabel: '说明',
							xtype: 'textarea',
							name: 'discription',
							style: {color: 'gray'},
							height: 35,
							readOnly: true
						}]
					},{
						region: 'west',
						title: '数据源列表',
						tbar: speedBar,
						autoScroll: true,
						width: 175,
						headerStyle: {
							'padding': '5px',
							'height': 'auto',
							'border-top': 'none',
							'border-left': 'none'
						},
						bodyStyle: {
							'border': 'none',
							'border-right': '1px solid #99BBE8'
						},
						items: folderView
					}, {
						region: 'center',
						xtype: 'form',
						hidden: true, // form设置为不可见,查看具体信息时显示
						title: '数据源属性',
						autoScroll: true,
						headerStyle: {
							'padding': '5px',
							'height': 'auto',
							'border': 'none',
							'border-bottom': '1px solid #99BBE8'
						},
						bodyStyle: {
							'padding': '5px',
							'height': 'auto',
							'border-left': 'none',
							'border-right': 'none'
						},
						labelWidth: 100,
						buttonAlign: 'right',
						stateful: false,
						method: 'POST',
						url: '../SQLExplorer',
						defaults: {
							anchor: '-18',
							stateful: false
						},
						items: [nameField,tsField,
						{
							xtype: 'fieldset',
							id: 'jdbc',
							hidden: true,
							bodyStyle: Ext.isIE ? 'padding-top: 10px;' : null,
							layout: 'form',
							defaults: {anchor: '100%'},
							items: [
								new Ext.form.TextField({
									fieldLabel: '驱动类',
									name: 'driver-class',
									width: 245,
									hidden: false
								}), new Ext.form.TextField({
									fieldLabel: '连接URL',
									name: 'url',
									width: 245,
									hidden: false
								}), new Ext.form.TextField({
									fieldLabel: '用户名',
									name: 'username',
									width: 245,
									hidden: false
								}), new Ext.form.TextField({
									fieldLabel: '密码',
									name: 'password',
									inputType: 'password',
									width: 245
								}), new Ext.form.NumberField({
									fieldLabel : '最大空闲连接',
									name: 'max-idle',
									allowNegative : false,
									nanText: nanText,
									width: 245,
									hidden: false
								}),new Ext.form.NumberField({
									fieldLabel : '最大等待连接时间',
									name: 'max-wait',
									width: 245,
									allowNegative : false,
									nanText: nanText,
									hidden: false
								}),new Ext.form.NumberField({
									fieldLabel : '最大活动连接数',
									name: 'max-active',
									width: 245,
									allowNegative : false,
									nanText: nanText,
									hidden: false
								}),new Ext.form.NumberField({
									fieldLabel : '初始化连接池数量',
									name: 'initial-size',
									width: 245,
									allowNegative : false,
									nanText: nanText,
									hidden: false
								})
							]
						}]
					}]
				});
				
				var formPanel = dialog.getComponent(2);
				var apply = formPanel.addButton('应用', function(){
					var isConflict = false;
					var hasChnChar = false;
					if (!formPanel.form.isValid()) 
						return;
					var selections = folderView.getSelectedIndexes();
					var rec = ds.getAt(selections[0]);
					
					if (isConflict) {
						Ext.Msg.alert('提示:', '已经存在同名数据源,请重新配置!');
						return;
					} else {
						formPanel.form.updateRecord(rec);
					}
				});
				
				ds.on('loadexception', function(proxy, obj, response){
					Viewer.showException(response);
				});
				
				// 加载数据
				ds.load({
					callback: function(records, options, success){
						if (success) {
							Ext.Ajax.request({
								method: 'POST',
								url: 'datasource_ctrl.jsp',
								params: {
									operation: 'getTransactionManager'
								},
								callback: function(options, success, response){
									if (success) {
										var tsManager = response.responseText.replace(/[\r\n]+/ig, '');
										var tsmObj = dialog.layout.north.items[0];
										tsmObj.setValue(tsManager);
										tsmObj.fireEvent('select', tsmObj);
									}
									else {
										_this.showException(response);
									}
								}
							});
						}
					}
				});
				
				folderView.on('selectionchange', function(view, selections){
					btns['del_conn'].setDisabled(selections.length < 1);
					btns['publish_ds'].setDisabled(selections.length < 1);
					if (selections.length == 1) {
						formPanel.show();
						formPanel.body.dom.style.height = "auto";
						var index = folderView.indexOf(selections[0]);
						var rec = ds.getAt(index);
						formPanel.form.loadRecord(rec);
						Ext.getCmp('jdbc').setVisible(true);
					} else {
						formPanel.hide();
					}
				});
				
				var saveSetting = function() {
					if (folderView.getSelectionCount() > 0) 
						apply.handler();
						
					// 使用的事务管理器					
					var tsm = dialog.layout.north.items[0].getValue();
					
					var datas = [];
					ds.each(function(record){
						datas.push(record.data);
					});
					
					Ext.Ajax.request({
						method: 'POST',
						url: '../SQLExplorer',
						success: function(response, options){
							ds.load();
						},
						failure: function(response, options){
							Viewer.showException(response);
						},
						params: {
							action: "saveDataSources",
							data: Ext.encode(datas),
							manager: tsm
						}
					});
				};
				
				// 发布数据源到指定受管服务器
				var publishDataSource = function(records) {
					var datas = [];
					Ext.each(records, function(record) {
						datas.push(record.data);
					});
					
					// 取得受管服务器列表
					Ext.Ajax.request({
						method: 'POST',
						url: 'datasource_ctrl.jsp',
						params: {
							operation: 'getServerList',
							data: (datas.length == 1) ? Ext.encode(datas) : ''
						},
						callback : function(options, success, response) {
							if (success) {
								var dsData = Ext.encode(datas);
								var serverList = Ext.decode(response.responseText);
								if (serverList.count > 0)
									showServerWin(serverList.items, dsData)
								else 
									Ext.Msg.alert('提示:', '受管服务器列表为空！');	
							} else {
								Viewer.showException(response);
							}
						}
					});
				};
				
				var showServerWin = function(serverList, dsData) {
					var items = [];
					Ext.each(serverList, function(server, index) {
						var checkbox = new Ext.form.Checkbox({
							boxLabel: server['name'],
							itemId: 'server' + index,
							name: server['name'],
							checked: server['published'],
							labelStyle: 'padding-top: 0px;'
						});
						items.push(checkbox);
					});
					
					var form = new Ext.form.FormPanel({
						header: false,
						height: 220,
						frame : false,
						border: true,
						autoScroll: true,
						layout: 'form',
						bodyStyle: {
							'padding': '5 8 5 8',
							'background-color': '#DFE8F6'
						},
						defaults: {anchor: '100%',stateful: false},
						items: [{
							hideLabel: true,
							xtype: 'checkboxgroup',
							columns: 4,
							vertical: true,
							itemId: 'checkbox-group',
							style: { 'margin-top': '-4px!important'},
							name: 'checkbox-group',
							items: items
						}]
					});
					
					var serversWin = new Ext.Window({
						title : '服务器列表',
						iconCls: 'settings',
						autoCreate: true,
						border : false,
				        resizable:false,
						constrain:true,
						constrainHeader:true,
				        minimizable:false, maximizable:false,
				        stateful:false,  modal:true,
						defaultButton: 0,
				        width: 400, height: 240, 
						minWidth: 200,
				        footer: true,
				        closable: true, 
						closeAction: 'close',
						plain: true,
						layout: 'fit',
						items: form,
						buttons: [{
					        text: '确定',
					        disabled: false,
							handler: function(){
								var selectedServer = [];
								Ext.each(form.getForm().findField('checkbox-group').items.items, function(checkbox){
									var data = {
										name: checkbox.name,
										publish: checkbox.checked
									};
									selectedServer.push(data);
								});
								if (selectedServer.length > 0) {
									savePublished(selectedServer, dsData, serversWin);
								}
							}
						},{
					        text: '取消',
							handler: function(){
								serversWin.close();
							}
						}]
					});
					serversWin.show();
				};
				
				var savePublished = function(selectedServer, dsData, win) {
					Ext.getBody().mask('正在发布到服务器...', 'x-mask-loading');
					Ext.Ajax.request({
						method: 'post',
						url: 'datasource_ctrl.jsp',
						params: {
							operation: 'publishDataSource',
							servers: Ext.encode(selectedServer),
							data: dsData
						},
						callback: function(options, success, response) {
							Ext.getBody().unmask();
							if (success) {
								Ext.getBody().mask('发布成功', 'x-mask-loading');
								Ext.getBody().unmask.defer(1000, Ext.getBody());
								win.close();
							} else {
								Viewer.showException(response);
							}
						}
					});
				};
				
				var okButton = dialog.addButton('保存', function(){
					saveSetting();
				});
				
				dialog.addButton('重置', function(){
					location.reload();
				}, dialog);
				
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