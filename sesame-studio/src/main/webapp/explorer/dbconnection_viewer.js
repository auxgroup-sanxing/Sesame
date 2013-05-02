document.oncontextmenu = function(e) {return false;};
Ext.BLANK_IMAGE_URL='../images/s.gif';

var Viewer = function(){
	var dialog;
	
	return {
		init: function(){
			Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
			Ext.QuickTips.init();
			
			var Item = Ext.data.Record.create([
				{name: 'default'}, 
				{name: 'name'}, 
				{name: 'type'}, 
				{name: 'datasource'}, 
				{name: 'driver'}, 
				{name: 'url'},
				{name: 'user'},
				{name: 'password'}]);
			
			var ds = new Ext.data.Store({
				proxy: new Ext.data.HttpProxy({
					url: '../SQLExplorer'
				}),
				baseParams: {
					action: 'loadConnections'
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
					'<div id="{name}" class="item-wrap" style="cursor:pointer;padding:1px;white-space:nowrap;">{name} <span style="color:gray">{default}</span></div>' +
					'</tpl>'),
				multiSelect: true,
				store: ds,
				itemSelector: 'div.item-wrap',
				selectedClass: 'hilited',
				emptyText: '没有连接'
			});
			
			var connTypes = new Ext.data.SimpleStore({
				fields: ['value', 'label'],
				data: [['jndi', '应用服务器连接池'], ['jdbc', '私有连接']]
			});
			
			var nameField = new Ext.form.TextField({
				fieldLabel: '连接名称',
				name: 'name',
				width: 245,
				allowBlank: false
			});
			
			var append = function(){
				var p = new Item({
					name: '未命名',
					type: 'jndi'
				});
				ds.add(p);
			};
			
			var remove = function(){
				if (!window.confirm("确实要删除选中的连接吗？")) 
					return;
				var records = [];
				Ext.each(folderView.getSelectedIndexes(), function(index){
					records.push(ds.getAt(index));
				});
				Ext.each(records, function(record){
					ds.remove(record);
				});
			};
			
			var speedBar = new Ext.Toolbar({
				style : {'border-left' : 'none'}
			});
			speedBar.add({
				id: 'add_conn',
				cls: 'x-btn-text-icon',
				text: '添加',
				handler: append,
				icon: "sqlexplorer_viewer/images/add_obj.gif"
			}, {
				id: 'del_conn',
				cls: 'x-btn-text-icon',
				text: '删除',
				handler: remove,
				icon: "sqlexplorer_viewer/images/delete.gif",
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
					items: [{
						region: 'west',
						title: '连接列表',
						tbar: speedBar,
						autoScroll: true,
						width: 180,
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
						title: '连接属性',
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
						labelWidth: 80,
						buttonAlign: 'right',
						stateful: false,
						method: 'POST',
						url: '../SQLExplorer',
						defaults: {
							anchor: '-18',
							stateful: false
						},
						items: [nameField, {
							xtype: 'combo',
							fieldLabel: '连接类型',
							width: 200,
							name: 'type',
							hiddenName: 'type',
							store: connTypes,
							mode: 'local',
							triggerAction: 'all',
							editable: false,
							forceSelection: true,
							valueField: 'value',
							displayField: 'label',
							value: 'jndi',
							listeners: {
								'select': function(combo, record, index){
									var value = record.get('value');
									Ext.getCmp('pool').setVisible(value == 'jndi');
									Ext.getCmp('jdbc').setVisible(value == 'jdbc');
								}
							}
						}, {
							xtype: 'fieldset',
							id: 'pool',
							hidden: false,
							bodyStyle: Ext.isIE ? 'padding-top: 10px;' : '',
							layout: 'form',
							defaults: {
								anchor: '100%'
							},
							items: new Ext.form.TextField({
								fieldLabel: '数据源 JNDI',
								name: 'datasource',
								hidden: false
							})
						}, {
							xtype: 'fieldset',
							id: 'jdbc',
							hidden: true,
							bodyStyle: Ext.isIE ? 'padding-top: 10px;' : null,
							layout: 'form',
							defaults: {
								anchor: '100%'
							},
							items: [new Ext.form.TextField({
								fieldLabel: '驱动类',
								name: 'driver',
								width: 245,
								hidden: false
							}), new Ext.form.TextField({
								fieldLabel: '连接URL',
								name: 'url',
								width: 245,
								hidden: false
							}), new Ext.form.TextField({
								fieldLabel: '用户名',
								name: 'user',
								width: 245,
								hidden: false
							}), new Ext.form.TextField({
								fieldLabel: '密码',
								name: 'password',
								inputType: 'password',
								width: 245
							})]
						}],
						buttons: [{
							text: '设为默认',
							handler: function(){
								var selections = folderView.getSelectedIndexes();
								var rec = ds.getAt(selections[0]);
								rec.set('default', 'default');
								ds.each(function(record){
									if (record != rec && record.get('default') == 'default'){
										record.set('default', '');
									}
								});
							}
						}]
					}]
				});
				var formPanel = dialog.getComponent(1);
				var apply = formPanel.addButton('应用', function(){
					if (!formPanel.form.isValid()) 
						return;
					var selections = folderView.getSelectedIndexes();
					var rec = ds.getAt(selections[0]);
					formPanel.form.updateRecord(rec);
				});
				
				ds.on('loadexception', function(proxy, obj, response){
					Viewer.showException(response);
				});
				
				ds.load({
					callback: function(){
						;
					}
				});
				
				folderView.on('selectionchange', function(view, selections){
					btns['del_conn'].setDisabled(selections.length < 1);
					if (selections.length == 1) {
						formPanel.show();
						formPanel.body.dom.style.height = "auto";
						var index = folderView.indexOf(selections[0]);
						var rec = ds.getAt(index);
						formPanel.form.loadRecord(rec);
						nameField.setDisabled(rec.get('name') == 'default');
						var type = rec.get('type');
						Ext.getCmp('pool').setVisible(type == 'jndi');
						Ext.getCmp('jdbc').setVisible(type == 'jdbc');
					}
					else {
						formPanel.hide();
					}
				});
				
				var okButton = dialog.addButton('保存', function(){
					if (folderView.getSelectionCount() > 0) 
						apply.handler();
					
					var conns = [];
					ds.each(function(record){
						conns.push(record.data);
					});
					Ext.Ajax.request({
						method: 'POST',
						url: '../SQLExplorer',
						success: function(response, options){
							location.reload();
						},
						failure: function(response, options){
							Viewer.showException(response);
						},
						params: {
							action: "saveConnections",
							data: Ext.encode(conns)
						}
					});
				});
				
				dialog.addButton('重置', function(){
					location.reload();
				}, dialog);
				
				viewport = new Ext.Viewport({
					layout : 'fit',
					items : dialog
				});
				formPanel.hide();
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
