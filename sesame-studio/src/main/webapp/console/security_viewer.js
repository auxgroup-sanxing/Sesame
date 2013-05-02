document.oncontextmenu = function(e) {return false;};
Ext.BLANK_IMAGE_URL='../images/s.gif';

var Viewer = function() {
	return {
		url: 'security_ctrl.jsp',
		
		preparedInit: function(){
			Ext.Ajax.defaultHeaders = {
				"Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
			};
			Ext.QuickTips.init();
		},
		
		init: function(){
			var _this = this;
			_this.preparedInit();
			
			// keyStoreInfo面板
			var ksiPanel = new Ext.Panel({
				title: 'KeyStoreInfo',
				id: 'KeyStoreInfo',
				labelWidth: 100,
				frame: false,
				bodyStyle: 'padding:5px 5px 0;',
				autoScroll: true,
				border: false,
				style: Ext.isGecko ? 'margin: auto;' : '',
				defaults: {
					anchor: "-18",
					style: 'margin-bottom:5px;'
				},
				tbar: new Ext.Toolbar({
					width: 30,
					height: 30,
					items: ['->', {
						text: '新建KeyStore',
						cls: 'x-btn-text-icon',
						icon: '../images/elcl16/add_obj.gif',
						scope: this,
						handler: function(){
							ksiPanel.add(this.createKeyStore(null));
							ksiPanel.doLayout();
						}
					}]
				}),
				footerCssClass: 'x-window-bc',
				buttons: [{
					text: '保存',
					handler: function(){
						var keyStores = ksiPanel.items.items;
						if (keyStores.length > 0) {
							var valid = true;
							var data = [];
							Ext.each(keyStores, function(keyStore) {
								var form = keyStore.getForm();
								if (!form.isValid()) {
									valid = false;
									return;
								} else {
									var value = form.getValues(false);
									var name = value['name'];
									if (typeof name.sort == 'function' && typeof name.length == 'number') {
										value['oldName'] = name[0];
										value['name'] = name[1];
									} else {
										value['oldName'] = name;
									}
									data.push(value);
								}
							});
							
							if (valid) {
								_this.saveKeyStoreInfo(data, skpPanel);
							}
						}
					}
				}, {
					text: '重置',
					handler: function(){
						_this.loadKeyStoreInfo(ksiPanel, skpPanel);
					}
				}],
				listeners: {
					activate: function(ksiPanel){
						if (!ksiPanel.loaded) {
							_this.loadKeyStoreInfo(ksiPanel, skpPanel);
							ksiPanel.loaded = true;
						}
					}
				}
			});
			
			// serviceKeyProvider面板
			var skpPanel = new Ext.Panel({
				title: 'KeyProvider',
				id: 'KeyProvider',
				disabled: true,
				labelWidth: 100,
				frame: false,
				bodyStyle: 'padding:5px 5px 0;',
				autoScroll: true,
				border: false,
				style: Ext.isGecko ? 'margin: auto;' : '',
				defaults: {
					anchor: "-18",
					style: 'margin-bottom:5px;'
				},
				tbar: new Ext.Toolbar({
					width: 30,
					height: 30,
					items: ['->', {
						text: '新建KeyProvider',
						cls: 'x-btn-text-icon',
						icon: '../images/elcl16/add_obj.gif',
						scope: this,
						handler: function(){
							skpPanel.add(this.createKeyProvider(null));
							skpPanel.doLayout();
						}
					}]
				}),
				footerCssClass: 'x-window-bc',
				buttons: [{
					text: '保存',
					handler: function(){
						var keyProviders = skpPanel.items.items;
						if (keyProviders.length > 0) {
							var valid = true;
							var data = [];
							Ext.each(keyProviders, function(kp) {
								var form = kp.getForm();
								if (!form.isValid()) {
									valid = false;
									return;
								} else {
									var value = form.getValues(false);
									if (value['keyPass'] == null)
										value['keyPass'] = '';
									data.push(value);
								}
							});
							
							if (valid) {
								_this.saveSkpInfo(data);
							}
						}
					}
				}, {
					text: '重置',
					handler: function(){
						_this.loadSkpInfo(skpPanel);
					}
				}],
				listeners: {
					activate: function(c){
						if (!c.loaded) {
							_this.loadSkpInfo(c);
							c.loaded = true;
						}
					}
				}
			});
			
			var viewport = new Ext.Viewport({
				layout: 'fit',
				stateful: false,
				items: [new Ext.TabPanel({
					activeTab: 0,
					border: false,
					bodyStyle: 'text-align:left;',
					deferredRender: false,
					items: [ksiPanel, skpPanel]
				})]
			});
		},
		
		// 读取keyStore信息
		loadKeyStoreInfo: function(ksiPanel, skpPanel){
			var _this = this;
			Ext.getBody().mask('正在读取信息...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'GET',
				url: _this.url,
				params: {
					operation: 'getKeyStoreInfo'
				},
				callback: function(options, success, response) {
					Ext.getBody().unmask();
					if (success) {
						var result = Ext.decode(response.responseText);
						var items = result.items;
						
						if (items.length > 0) {
							ksiPanel.removeAll(true);
							Ext.each(items, function(item, index){
								ksiPanel.add(_this.createKeyStore(item));
								ksiPanel.doLayout();
							});
							skpPanel.enable();
						}
					} else {
						_this.showException(response);
					}
				}
			});
		},
		
		// 创建KeyStore面板
		createKeyStore: function(options) {
			var _this = this;

			var storeName = (!!options) ? options['name'] : '';
			var keystorePath = (!!options) ? options['keystorePath'] : '';
			var storePass = (!!options) ? options['storePass'] : '';
			var description = (!!options) ? options['description'] : '';
			
			var storePanel = new Ext.form.FormPanel({
				title: storeName,
				style: {'margin': '5 8 3 8'},
				height: 180,
				frame: true,
				closable: true,
				collapsible: true,
				defaults: {anchor: '100%'},
				labelWidth: 100,
				tools: [{
					id: 'minus',
					qtip: '删除该KeyStore',
					handler: function(e, toolEl, panel, tc){
						if (!window.confirm("确实要删除选中的项吗？")) 
							return;
						_this.deleteKeyStore(panel);	
					}
				}],
				items: [
				{
					name: 'name',
					itemId: 'hiddenName',
					xtype: 'hidden',
					allowBlank: false,
					hidden: true,
					value: storeName
				},
				{
					fieldLabel: '名称',
					name: 'name',
					xtype: 'textfield',
					allowBlank: false,
					value: storeName,
					listeners: {
						valid: function(field) {
							storePanel.setTitle(field.getValue());
						}
					}
				}, {
					fieldLabel: 'keystore路径',
					name: 'keystorePath',
					xtype: 'textfield',
					allowBlank: false,
					value: keystorePath
				}, {
					fieldLabel: 'keyStore密码',
					name: 'storePass',
					xtype: 'textfield',
					inputType: 'password',
					allowBlank: false,
					value: storePass
				}, {
					fieldLabel: '描述',
					name: 'description',
					xtype: 'textarea',
					allowBlank: true,
					value: description
				}]
			});
			
			return storePanel;
		},
		
		// 创建KeyProvider面板
		createKeyProvider: function(options) {
			var _this = this;
			function showField(field){
				field.enable();
				field.show();
				field.getEl().up('.x-form-item').setDisplayed(true);
				field.getEl().dom.parentNode.parentNode.setAttribute('class', 'x-form-item');
				field.getEl().dom.parentNode.firstChild.style.width = '100%';
			};
			
			function hideField(field){
				field.disable();
				//field.hide();
				field.getEl().up('.x-form-item').setDisplayed(false);
			};
			
			var providerName = (!!options) ? options['name'] : '';
			var alias = (!!options) ? options['alias'] : '';
			var keystoreName = (!!options) ? options['keystoreName'] : '';
			var paired = (!!options) ? options['paired'] : '';
			var keyPass = (!!options) ? options['keyPass'] : '';
			
			var providerPanel = new Ext.form.FormPanel({
				title: providerName,
				style: {'margin': '5 8 3 8'},
				height: 180,
				frame: true,
				closable: true,
				collapsible: true,
				defaults: {anchor: '100%'},
				labelWidth: 100,
				tools: [{
					id: 'minus',
					qtip: '删除该KeyProvider',
					handler: function(e, toolEl, panel, tc){
						if (!window.confirm("确实要删除选中的项吗？")) 
							return;
						_this.deleteSkp(panel);	
					}
				}],
				items: [
				{
					fieldLabel: 'KeyProvider名称',
					name: 'name',
					xtype: 'textfield',
					allowBlank: false,
					readOnly: !!providerName,
					value: providerName
				}, {
					fieldLabel: '别名',
					name: 'alias',
					xtype: 'textfield',
					allowBlank: false,
					value: alias
				}, {
					fieldLabel: 'KeyStore名称',
					name: 'keystoreName',
					xtype: 'combo',
					emptyText: '<请选择已设置的KeyStore>',
					allowBlank: false,
					value: keystoreName,
					triggerAction: 'all',
					valueField: 'keystore',
					displayField: 'keystore',
					store: new Ext.data.Store({
						url: _this.url,
						baseParams: {
							operation: 'getKeyStoreList'
						},
						reader: new Ext.data.JsonReader({
							root: 'items',
							fields: [{name: 'keystore'}]
						})
					})
				}, {
					fieldLabel: '密钥是否成对',
					xtype: 'radiogroup',
					name: 'radio-group',
					columns: [110,110],
					itemId: 'radio-group',
					value: paired,
					style: {'margin-top':'-4px!important'},
					items: [{
						boxLabel: '是',
						name: 'paired',
						width: 10,
						inputValue: 'true',
						checked: true
					}, {
						boxLabel: '否',
						name: 'paired',
						width: 10,
						inputValue: 'false'
					}],
					listeners: {
						change: function(obj) {
							var isPaired = obj.getValue().getGroupValue();
							if (isPaired == 'true') {
								showField(obj.ownerCt.get('keyPass'));
								providerPanel.get('keyPass').setValue(keyPass);
							} else {
								providerPanel.get('keyPass').setValue('');
								hideField(obj.ownerCt.get('keyPass'));
							}
						}
					}
				},{
					fieldLabel: '私钥密码:',
					labelSeparator: null,
					name: 'keyPass',
					itemId: 'keyPass',
					xtype: 'textfield',
					inputType: 'password',
					allowBlank: false,
					hideLabel: (paired!= '' && paired != 'true'),
					hidden: (paired!= '' && paired != 'true'),
					value: (paired == 'true') ? keyPass:''
				}]
			});
			
			return providerPanel;
		},
		
		// 保存keyStore信息
		saveKeyStoreInfo: function(data, skpPanel){
			var _this = this;		
			Ext.getBody().mask('正在保存信息...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST',
				url: _this.url,
				params: {
					operation: 'saveKeyStoreInfo',
					data: Ext.encode(data)
				},
				callback: function(options, success, response) {
					Ext.getBody().unmask();
					if (success) {
						Ext.getBody().mask('保存成功!', 'x-mask-loading');
						Ext.getBody().unmask.defer(1000, Ext.getBody());
						skpPanel.enable();
					} else {
						_this.showException(response);
					}
				}
			});
		},
		
		// 删除KeyStore
		deleteKeyStore: function(panel) {
			var _this = this;
			Ext.getBody().mask('正在删除...', 'x-mask-loading');
			var name = panel.get('hiddenName').getValue();
			Ext.Ajax.request({
				method: 'POST',
				url: _this.url,
				params: {
					operation: 'deleteKeyStore',
					name: name
				},
				callback: function(options, success, response) {
					Ext.getBody().unmask();
					if (success) {
						var ksiPanel = Ext.getCmp('KeyStoreInfo');
						ksiPanel.remove(panel);
						ksiPanel.doLayout();
						Ext.getBody().mask('删除成功!', 'x-mask-loading');
						Ext.getBody().unmask.defer(1000, Ext.getBody());
						
						var skpPanel = Ext.getCmp('KeyProvider');
						if (ksiPanel.items.items.length == 0) {
							skpPanel.disable();
						}
					} else {
						_this.showException(response);
					}
				}
			});
		},
		
		// 读取keyProvider信息
		loadSkpInfo: function(skpPanel){
			var _this = this;
			Ext.getBody().mask('正在读取信息...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'GET',
				url: _this.url,
				params: {
					operation: 'getKeyProviderInfo'
				},
				callback: function(options, success, response) {
					Ext.getBody().unmask();
					if (success) {
						var result = Ext.decode(response.responseText);
						var items = result.items;
						
						if (items.length > 0) {
							skpPanel.removeAll(true);
							Ext.each(items, function(item, index){
								skpPanel.add(_this.createKeyProvider(item));
								skpPanel.doLayout();
							});
						}
					} else {
						_this.showException(response);
					}
				}
			});
		},
		
		// 保存keyProvider信息
		saveSkpInfo: function(data){
			var _this = this;		
			Ext.getBody().mask('正在保存信息...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST',
				url: _this.url,
				params: {
					operation: 'saveKeyProviderInfo',
					data: Ext.encode(data)
				},
				callback: function(options, success, response) {
					Ext.getBody().unmask();
					if (success) {
						Ext.getBody().mask('保存成功!', 'x-mask-loading');
						Ext.getBody().unmask.defer(1000, Ext.getBody());
					} else {
						_this.showException(response);
					}
				}
			});
		},
		
		// 删除KeyProvider
		deleteSkp: function(panel) {
			var _this = this;
			Ext.getBody().mask('正在删除...', 'x-mask-loading');
			var name = (!!panel.get('name'))? panel.get('name').getValue():'';
			if (!name) {
				var skpPanel = Ext.getCmp('KeyProvider');
				skpPanel.remove(panel);
				skpPanel.doLayout();
				Ext.getBody().unmask();
				return;
			}
			
			Ext.Ajax.request({
				method: 'POST',
				url: _this.url,
				params: {
					operation: 'deleteSkp',
					name: name
				},
				callback: function(options, success, response) {
					Ext.getBody().unmask();
					if (success) {
						var skpPanel = Ext.getCmp('KeyProvider');
						skpPanel.remove(panel);
						skpPanel.doLayout();
						Ext.getBody().mask('删除成功!', 'x-mask-loading');
						Ext.getBody().unmask.defer(1000, Ext.getBody());
					} else {
						_this.showException(response);
					}
				}
			});
		},
		
		// 异常处理
		showException: function(response){
			var statusText = response.responseText;
			if (!statusText) {
				switch (response.status) {
					case -1:
						statusText = '通讯超时，事务终止';
						break;
					case 0:
						statusText = '通讯错误，连接服务器失败';
						break;
					default:
						alert(response.statusText + '(' + response.status + ')');
						return;				}
				statusText = '<table style="width:100%;">' +
				'<tr><td style="background: url(\'../images/top_bg.gif\') no-repeat; vertical-align:middle; font: bold 24px \'Times New Roman\', Verdana, Arial; height:54px; color:white;">Application Management Center</td></tr>' +
				'<tr><td><table align="center" border="0" cellpadding="0" cellspacing="8" width="100%"><tbody><tr>' +
				'<td style="text-align: center; width:50px;"><img src="../images/notice.png"/></td>' +
				'<td style="white-space: normal; overflow: hidden; font-size:12px;">' +
				statusText +
				'</td>' +
				'</tr></tbody></table></td></tr></table>';
			}
			
			Ext.MessageBox.show({
				title: '错误提示',
				msg: '<div style="width:100%; height:auto; background-color:white; overflow:auto;">' + statusText + '</div>',
				buttons: Ext.MessageBox.OK,
				width: 492
			});
		}
	};
}();

Ext.onReady(Viewer.init, Viewer, true);
