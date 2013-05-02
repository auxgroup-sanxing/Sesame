var Dialog = function(){

return {

	//创建远程服务单元对话框
	getClientDialog: function(options){
		var _this = this;
		
		if (!this.connectDlg) {
			var dlg = new Ext.Window({
				title: '创建远程服务单元',
				autoCreate: true,
				resizable: false,
				constrain: true,
				constrainHeader: true,
				minimizable: false,
				maximizable: false,
				stateful: false,
				modal: true,
				buttonAlign: "right",
				defaultButton: 0,
				width: 450,
				height: 285,
				minWidth: 300,
				footer: true,
				closable: true,
				closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: [{
					xtype: 'form',
					autoScroll: true,
					labelWidth: 75,
					border: false,
					bodyStyle: 'padding:10px;',
					defaultType: 'textfield',
					defaults: {
						anchor: '-18'
					},
					items: [{
						name: 'unit-name',
						allowBlank: false,
						fieldLabel: '单元名称',
						regex: Identifier.regex,
						regexText: Identifier.regexText,
						listeners: {
							change: function(field, newValue, oldValue){
								var form = field.ownerCt.getForm();
								var f = form.findField('namespace');
								if (f.getValue() == '') {
									var s = targetNamespace.charAt(targetNamespace.length - 1) == '/' ? '' : '/';
									f.setValue(targetNamespace + s + newValue);
								}
							}
						}
					}, {
						name: 'desc',
						fieldLabel: '描述',
						xtype: 'textarea',
						allowBlank: false
					}, {
						name: 'namespace',
						fieldLabel: '命名空间',
						allowBlank: false,
						regex: Namespace.regex,
						regexText: Namespace.regexText
					}, {
						name: 'service-name',
						fieldLabel: '服务名',
						allowBlank: false,
						regex: NCName.regex,
						regexText: NCName.regexText
					}, {
						name: 'component-name',
						xtype: 'hidden',
						fieldLabel: '组件名',
						regex: NCName.regex,
						regexText: NCName.regexText
					}, {
						name: 'component',
						hiddenName: 'component',
						xtype: 'combo',
						fieldLabel: '组件',
						allowBlank: false,
						forceSelection: true,
						store: _this.getComponentStore('binding-component', 'provider'),
						triggerAction: 'all',
						editable: false,
						valueField: 'component',
						displayField: 'label',
						listeners: {
							select: function(sender, record, index){
								var form = sender.ownerCt.getForm();
								var field = form.findField('component-name');
								field.setValue(record.get('component-name'));
								
								var hiddenField = form.findField('initialWSDL');
								if (!!hiddenField)
								// 选择soap-cc组件的时候显示导入WSDL文件文本框
								if (field.getValue() == 'soap-cc') {
									hiddenField.getItemCt().dom.style.display = 'block';
								} else {
									hiddenField.getItemCt().dom.style.display = 'none';
								}
							}
						}
					}, {
						name: 'initialWSDL',
						itemId: 'initialWSDL',
						fieldLabel: '导入WSDL',
						regex: /.*\.wsdl$/,
						regexText: '必须以.wsdl结尾',
						allowBlank: true,
						emptyText: '请输入WSDL文件的地址,必须以.wsdl结尾'
					}],
					listeners: {
						afterlayout: function(form) {
							form.find('name', 'initialWSDL')[0].getItemCt().dom.style.display = 'none';
						}
					}
				}]
			});
			
			dlg.ok = dlg.addButton({
				text: Ext.Msg.buttonText.ok,
				disabled: false
			}, function(){
				var formPanel = dlg.getComponent(0);
				var form = formPanel.getForm();
				
				form.submit({
					url: 'project_ctrl.jsp',
					clientValidation: true,
					params: {
						operation: 'createClient',
						project: project
					},
					success: function(){
						if (dlg.options.callback) 
							dlg.options.callback(form.getValues());
						dlg.hide();
					},
					failure: function(form, action){
						if (action.response) 
							Viewer.showException(action.response);
					}
				});
			});
			dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
				dlg.hide();
			});
			
			dlg.render(Ext.getBody());
			this.connectDlg = dlg;
		}
		this.connectDlg.options = options;
		var formPanel = this.connectDlg.getComponent(0);
		var form = formPanel.getForm();
		form.findField('component').store.reload();
		form.reset();
		if (options.params) {
			form.setValues(options.params);
		}
		return this.connectDlg;
	},
	
	
	getComponentDialog: function(options){
		var _this = this;
		
		if (!this.compDlg) {
			var dlg = new Ext.Window({
				title: '添加组件',
				autoCreate: true,
				resizable: false,
				constrain: true,
				constrainHeader: true,
				minimizable: false,
				maximizable: false,
				stateful: false,
				modal: true,
				buttonAlign: "right",
				defaultButton: 0,
				width: 450,
				height: 120,
				minWidth: 300,
				footer: true,
				closable: true,
				closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: [{
					xtype: 'form',
					autoScroll: true,
					labelWidth: 85,
					border: false,
					bodyStyle: 'padding:10px;',
					fileUpload: true,
					defaultType: 'textfield',
					defaults: {
						anchor: '-18'
					},
					items: [{
						name: 'comp-jar',
						fieldLabel: '类库',
						xtype: 'filefield',
						allowBlank: false,
						emptyText: '选择类库，必须是 .jar 文件',
						buttonCfg: {
							text: '',
							iconCls: 'upload-package'
						},
						listeners: {
							fileselected: function(sender, value){
								var form = this.ownerCt.getForm();
								form.findField('file').setValue(this.getValue());
							}
						}
					}, {
						xtype: 'hidden',
						name: 'file'
					}, {
						xtype: 'hidden',
						name: 'component'
					}]
				}]
			});
			
			dlg.ok = dlg.addButton({
				text: Ext.Msg.buttonText.ok,
				disabled: false
			}, function(){
				var formPanel = dlg.getComponent(0);
				var form = formPanel.getForm();
				
				form.submit({
					url: 'project_ctrl.jsp',
					clientValidation: true,
					params: {
						project: project,
						operation: dlg.options.operation
					},
					success: function(form, action){
						if (dlg.options.callback) {
							dlg.options.callback(action.result);//form.getValues());
							dlg.hide();
						}
					},
					failure: function(form, action){
						if (action.response) 
							alert(action.result.error);
					}
				});
			});
			dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
				dlg.hide();
			});
			
			dlg.render(Ext.getBody());
			this.compDlg = dlg;
		}
		else {
		}
		this.compDlg.options = options;
		var formPanel = this.compDlg.getComponent(0);
		var form = formPanel.getForm();
		//form.reset();
		if (options.params) {
			form.setValues(options.params);
		}
		this.compDlg.setTitle(options.title);
		return this.compDlg;
	},
	
	//获取上传服务单元的对话框
	getUnitDialog: function(options){
		var _this = this;
		
		if (!this.unitDlg) {
			var dlg = new Ext.Window({
				title: '添加服务单元',
				autoCreate: true,
				resizable: false,
				constrain: true,
				constrainHeader: true,
				minimizable: false,
				maximizable: false,
				stateful: false,
				modal: true,
				buttonAlign: "right",
				defaultButton: 0,
				width: 450,
				height: 160,
				minWidth: 300,
				footer: true,
				closable: true,
				closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: [{
					xtype: 'form',
					autoScroll: true,
					labelWidth: 85,
					border: false,
					bodyStyle: 'padding:10px;',
					fileUpload: true,
					defaultType: 'textfield',
					defaults: {
						anchor: '-18'
					},
					items: [{
						xtype: 'textfield',
						fieldLabel: '单元名称',
						name: 'serviceUnit',
						regex: Identifier.regex,
						regexText: Identifier.regexText
					}, {
						name: 'file',
						fieldLabel: '压缩包',
						xtype: 'filefield',
						allowBlank: false,
						emptyText: '选择文件，必须是 .zip 文件',
						buttonCfg: {
							text: '',
							iconCls: 'upload-package'
						}
					}, {
						xtype: 'hidden',
						name: 'type'
					}]
				}],
				buttons: [{
					text: Ext.Msg.buttonText.ok,
					disabled: false,
					handler: function(){
						var formPanel = dlg.getComponent(0);
						var form = formPanel.getForm();
						
						form.submit({
							url: 'project_ctrl.jsp',
							clientValidation: true,
							params: {
								project: project,
								operation: "uploadUnit"
							},
							success: function(form, action){
								if (dlg.options.callback) {
									dlg.options.callback(form.getValues());
									dlg.hide();
								}
							},
							failure: function(form, action){
								if (action.response) 
									alert(action.result.error);
							}
						});
					}
				}, {
					text: Ext.Msg.buttonText.cancel,
					handler: function(){
						dlg.hide();
					}
				}]
			});
			
			this.unitDlg = dlg;
		}
		
		this.unitDlg.options = options;
		var formPanel = this.unitDlg.getComponent(0);
		var form = formPanel.getForm();
		//form.reset();
		if (options.params) {
			form.setValues(options.params);
		}
		this.unitDlg.setTitle(options.title);
		return this.unitDlg;
	},
	
	getSchemaDialog: function(options){
		var _this = this;
		
		if (!this.schemaDlg) {
			var dlg = new Ext.Window({
				title: '创建报文模型',
				autoCreate: true,
				resizable: false,
				constrain: true,
				constrainHeader: true,
				minimizable: false,
				maximizable: false,
				stateful: false,
				modal: true,
				buttonAlign: "right",
				defaultButton: 0,
				width: 450,
				height: 150,
				minWidth: 300,
				footer: true,
				closable: true,
				closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: [{
					xtype: 'form',
					autoScroll: true,
					labelWidth: 75,
					border: false,
					bodyStyle: 'padding:10px;',
					defaultType: 'textfield',
					defaults: {
						anchor: '-18'
					},
					items: [{
						name: 'schema-name',
						allowBlank: false,
						fieldLabel: '名称'
					}, {
						name: 'desc',
						fieldLabel: '描述',
						allowBlank: false
					}]
				}]
			});
			
			dlg.ok = dlg.addButton({
				text: Ext.Msg.buttonText.ok,
				disabled: false
			}, function(){
				var formPanel = dlg.getComponent(0);
				var form = formPanel.getForm();
				
				form.submit({
					url: 'project_ctrl.jsp',
					clientValidation: true,
					params: {
						operation: 'createSchema',
						project: project
					},
					success: function(){
						if (dlg.options.callback) 
							dlg.options.callback(form.getValues());
						dlg.hide();
					},
					failure: function(form, action){
						if (action.response) 
							Viewer.showException(action.response);
					}
				});
			});
			dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
				dlg.hide();
			});
			
			dlg.render(Ext.getBody());
			this.schemaDlg = dlg;
		}
		else {
		}
		this.schemaDlg.options = options;
		var formPanel = this.schemaDlg.getComponent(0);
		var form = formPanel.getForm();
		form.reset();
		if (options.params) {
			form.setValues(options.params);
		}
		return this.schemaDlg;
	},
	
	// 创建公共接口对话框
	getIntfDialog: function(options) {
		var _this = this;
		if (!this.intfDlg) {
			var dlg = new Ext.Window({
				title: '创建公共接口',
				autoCreate: true, resizable: false,
				constrain: true, constrainHeader: true,
				minimizable: false, maximizable: false,
				stateful: false, modal: true,
				buttonAlign: "right",
				defaultButton: 0,
				width: 450, height: 210, minWidth: 300,
				footer: true, closable: true, closeAction: 'hide',
				plain: true, layout: 'fit',
				items: [{
					xtype: 'form',
					autoScroll: true,
					labelWidth: 75,
					border: false,
					bodyStyle: 'padding:10px;',
					defaultType: 'textfield',
					defaults: {anchor: '-18'},
					items: [{
						name: 'intf-name',
						allowBlank: false,
						fieldLabel: '接口名称',
						regex: Identifier.regex,
						regexText: Identifier.regexText,
						listeners: {
							change: function(field, newValue, oldValue){
								var form = field.ownerCt.getForm();
								var f = form.findField('namespace');
								if (f.getValue() == '') {
									var s = targetNamespace.charAt(targetNamespace.length - 1) == '/' ? '' : '/';
									f.setValue(targetNamespace + '/intf' + s + newValue);
								}
							}
						}
					}, {
						name: 'namespace',
						fieldLabel: '命名空间',
						allowBlank: false,
						regex: Namespace.regex,
						regexText: Namespace.regexText
					}, {
						name: 'desc',
						xtype: 'textarea',
						fieldLabel: '描述',
						allowBlank: false
					}]
				}]
			});
			
			dlg.ok = dlg.addButton({
				text: Ext.Msg.buttonText.ok,
				disabled: false
			}, function(){
				var formPanel = dlg.getComponent(0);
				var form = formPanel.getForm();
				
				form.submit({
					url: 'project_ctrl.jsp',
					clientValidation: true,
					params: {
						operation: 'createPubIntf',
						project: project
					},
					success: function(){
						if (dlg.options.callback) 
							dlg.options.callback(form.getValues());
						dlg.hide();
					},
					failure: function(form, action){
						if (action.response) 
							Viewer.showException(action.response);
					}
				});
			});
			dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
				dlg.hide();
			});
			
			dlg.render(Ext.getBody());
			this.intfDlg = dlg;
		}
		
		this.intfDlg.options = options;
		var formPanel = this.intfDlg.getComponent(0);
		var form = formPanel.getForm();
		form.reset();
		if (options.params) {
			form.setValues(options.params);
		}
		return this.intfDlg;
	},
	
	//部署对话框
	getDeployDialog: function(options){
		var _this = this;
		
		if (!this.deployDlg) {
			var dlg = new Ext.Window({
				title: '生成部署包',
				autoCreate: true, resizable: false, constrain: true,
				constrainHeader: true, minimizable: false, maximizable: false,
				stateful: false, modal: true,
				buttonAlign: "right", defaultButton: 0,
				width: 450, height: 380, minWidth: 300,
				closable: true, closeAction: 'hide',
				plain: false, footer: true,
				layout: 'border',
				items: [{
					itemId: 'dir-tree',
					xtype: 'treepanel',
					region: 'center',
					border: true,
					style: "padding:10px;",
					loader: new Ext.tree.TreeLoader({
						requestMethod: 'GET',
						dataUrl: 'install_ctrl.jsp',
						baseParams: {
							operation: 'getUnits'
						},
						listeners: {
							beforeload: function(loader, node){
								loader.baseParams.path = node.getPath('name').substring(1);
								loader.baseParams.checked = node.attributes.checked;
								loader.baseParams.revision = dlg.get('prefs').getForm().getValues()['revision'];
							},
							loadexception: function(loader, node, response){
								Viewer.showException(response);
							}
						}
					}),
					listeners: {
						checkchange: function(node, checked){
							node.getOwnerTree().suspendEvents();
							node.bubble(function(n){
								if (n == node && node.id == "PROJECT_ROOT") 
									return;
								if (n.ui.isChecked() == checked) 
									return;
								if (!checked) {
									n.ui.toggleCheck(checked);
									n.attributes.checked = checked;
									return;
								}
								var allChecked = true;
								n.eachChild(function(child){
									if (!child.attributes.checked) {
										allChecked = false;
										return false;
									}
								});
								if (allChecked) {
									n.ui.toggleCheck(checked);
									n.attributes.checked = checked;
								}
							});
							
							node.cascade(function(n){
								if (n.ui.isChecked() != checked) {
									n.ui.toggleCheck(checked);
									n.attributes.checked = checked;
								}
							});
							node.getOwnerTree().resumeEvents();
						}
					},
					rootVisible: false,
					lines: false,
					animate: false,
					autoScroll: true,
					trackMouseOver: false,
					root: new Ext.tree.AsyncTreeNode({
						text: '根',
						name: project,
						id: 'PROJECT_ROOT',
						expanded: true,
						children: []
					})
				}, {
					itemId: 'prefs',
					xtype: 'form',
					region: 'north',
					//height: 100,
					url: 'install_ctrl.jsp',
					autoHeight: true,
					style: 'padding: 10px 10px 0px 10px;',
					bodyStyle: 'background-color: transparent;',
					labelWidth: 85,
					border: false,
					defaultType: 'textfield',
					defaults: {
						anchor: '100%'
					},
					items: [{
						xtype: 'hidden',
						fieldLabel: '目标地址',
						name: 'target',
						allowBlank: true
					},{
						xtype: 'combo',
						fieldLabel: '版本',
						name: 'revision', hiddenName: 'revision',
						allowBlank: false,
						forceSelection: false, editable: true,
						store: _this.getRevisionStore(),
						triggerAction: 'all', mode: 'local',
						valueField: 'rev',
						displayField: 'label',
						listeners: {
							change: function(field, newValue, oldValue){
								dlg.refreshTree(dlg.options.items);
							}
						}
					},{
						title: '选项',
						xtype: 'fieldset',
						layout: "form",
						name: "options",
						labelWidth: 100,
						autoHeight: true,
						border: true,
						defaults: {
							anchor: "100%",
							stateful: false
						},
						items: [{
							xtype: 'checkbox',
							fieldLabel: '压缩文件内容',
							name: 'compress',
							checked: true
						}, {
							xtype: 'checkbox',
							fieldLabel: '完成后提供下载',
							name: 'download',
							checked: true
						}]
					}]
				}],
				buttons: [{
					text: Ext.Msg.buttonText.ok,
					disabled: false,
					handler: function(){
						var treePanel = dlg.getComponent(0);
						var selected = {};
						Ext.each(treePanel.getChecked(), function(node){
							if (node.firstChild != null) 
								return;
							selected[node.getPath('name')] = node.isLeaf() ? 'zip' : 'folder';
						});
						
						var formPanel = dlg.getComponent('prefs');
						var form = formPanel.getForm();
						var field = form.findField('revision');
						var regex = /^[0-9]{1,19}$/g;
						if (field.store.findExact('rev', field.getValue())==-1 && !regex.test(field.getValue())) {
							field.markInvalid("输入的版本号必须为整数");
							return;
						}
						
						dlg.bwrap.mask('正在打包...', 'x-mask-loading');
						form.submit({
							clientValidation: true,
							timeout: 90 * 1000,
							params: {
								project: project,
								operation: "exportSA",
								selected: Ext.encode(selected)
							},
							success: function(form, action){
								dlg.bwrap.unmask();
								dlg.hide();
								if (form.getValues().download == "on") {
									Ext.MessageBox.show({
										title: '部署完成',
										msg: '<div style="width:auto; height:auto; background-color:white; overflow:auto; padding:10px;">' +
											action.result.html +'</div>',
										buttons: Ext.MessageBox.OK,
										width: 492
									});
									setTimeout(function(){Ext.MessageBox.hide();}, 8*1000);
								}
							},
							failure: function(form, action){
								dlg.bwrap.unmask();
								if (action.response) 
									Viewer.showException(action.response);
							}
						});
					}
				},{
					text: Ext.Msg.buttonText.cancel, 
					handler: function(){ dlg.hide(); }
				}]
			});
			
			dlg.render(Ext.getBody());
			
			dlg.refreshTree = function(items) {
				var treePanel = dlg.get('dir-tree');
				treePanel.suspendEvents();
				try {
					var rootNode = treePanel.getRootNode();
					while (rootNode.firstChild != null) 
						rootNode.firstChild.remove();
					for (var i = 0; i < items.length; i++) {
						var a = items[i].attributes;
						rootNode.appendChild(new Ext.tree.AsyncTreeNode({
							text: (a.desc || a.text),
							name: a.name,
							checked: true
						}));
					}
				}
				finally {
					treePanel.resumeEvents();
				}
			}

			this.deployDlg = dlg;
		}
		else {
		}
		this.deployDlg.options = options;
		
		if (options.items) {
			this.deployDlg.refreshTree(options.items);
		}

		var formPanel = this.deployDlg.getComponent('prefs');
		var form = formPanel.getForm();
		form.load({
			params: {
				project: project,
				operation: "loadDeployPrefs"
			},
			success: function(form, action){
				var field = form.findField('revision');
				field.store.loadData(action.result);
				field.setValue(action.result.data[field.getName()]);
				if (field.store.getCount() < 2) {
					field.setEditable(false);
				}
			},
			failure: function(form, action){
				if (action.response) 
					Viewer.showException(action.response);
			}
		});
		return this.deployDlg;
	},
	
	//组件安装对话框
	getInstallDialog: function(options){
		var _this = this;
		
		if (!this.installDlg) {
			var dlg = new Ext.Window({
				options: options,
				title: '安装组件',
				autoCreate: true,
				resizable: false,
				constrain: true,	constrainHeader: true,
				minimizable: false,	maximizable: false,
				stateful: false, modal: true,
				buttonAlign: "right",	defaultButton: 0,
				width: 450,	height: 380,	minWidth: 300,
				closable: true, closeAction: 'hide',
				plain: true, footer: true,	
				layout: 'border',
				items: [{
					xtype: 'treepanel',
					region: 'center',
					border: true,
					style: "padding:10px;",
					loader: new Ext.tree.TreeLoader({
						requestMethod: 'GET',
						dataUrl: 'install_ctrl.jsp',
						baseParams: { project: project },
						listeners: {
							beforeload: function(loader, node){
								loader.baseParams.operation = dlg.options.load;
								loader.baseParams.checked = true; //node.attributes.checked;
							},
							loadexception: function(loader, node, response){
								Viewer.showException(response);
							}
						}
					}),
					listeners: {
						checkchange: function(node, checked){
						}
					},
					rootVisible: false,
					lines: true,
					animate: false,
					autoScroll: true,
					trackMouseOver: false,
					root: new Ext.tree.AsyncTreeNode({
						text: '根',
						id: 'ROOT',
						expanded: true
					})
				}, {
					xtype: 'form',
					region: 'south',
					url: 'install_ctrl.jsp',
					autoHeight: true,
					style: 'padding:0px 10px 0px 10px;',
					bodyStyle: 'background-color: transparent;',
					labelWidth: 85,
					border: false,
					defaultType: 'textfield',
					defaults: {
						anchor: '100%'
					},
					items: [{
						xtype: 'textfield',
						fieldLabel: '目标地址',
						name: 'target',
						allowBlank: false
					}, {
						title: '选项',
						xtype: 'fieldset',
						layout: "form",
						name: "options",
						labelWidth: 100,
						autoHeight: true,
						border: true,
						defaults: {
							anchor: "100%",
							stateful: false
						},
						items: [{
							xtype: 'checkbox',
							fieldLabel: '压缩文件内容',
							name: 'compress',
							checked: true
						}, {
							xtype: 'checkbox',
							fieldLabel: '完成后提供下载',
							name: 'download',
							checked: true
						}]
					}]
				}]
			});
			
			dlg.ok = dlg.addButton({
				text: Ext.Msg.buttonText.ok,
				disabled: false
			}, function(){
				var treePanel = dlg.getComponent(0);
				var selected = {};
				Ext.each(treePanel.getChecked(), function(node){
					if (node.firstChild != null) 
						return;
					selected[node.getPath('name')] = node.isLeaf() ? 'zip' : 'folder';
				});
				
				var formPanel = dlg.getComponent(1);
				var form = formPanel.getForm();
				
				form.submit({
					clientValidation: true,
					params: {
						project: project,
						operation: dlg.options.submit,
						selected: Ext.encode(selected)
					},
					success: function(form, action){
						dlg.hide();
						if (form.getValues().download == "on") {
							Ext.MessageBox.show({
								title: '安装完毕',
								msg: '<div style="width:100%; height:auto; background-color:white; overflow:auto; padding:10px;">' +
								action.result.html +
								'</div>',
								buttons: Ext.MessageBox.OK,
								width: 492
							});
						}
					},
					failure: function(form, action){
						if (action.response) 
							Viewer.showException(action.response);
					}
				});
			});
			dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
				dlg.hide();
			});
			
			dlg.render(Ext.getBody());
			this.installDlg = dlg;
		}
		this.installDlg.options = options;
		var treePanel = this.installDlg.getComponent(0);
		var formPanel = this.installDlg.getComponent(1);
		var form = formPanel.getForm();
		
		treePanel.getRootNode().reload();
		
		form.load({
			params: {
				project: project,
				operation: "loadInstallPrefs"
			},
			failure: function(form, action){
				if (action.response) 
					Viewer.showException(action.response);
			}
		});
		this.installDlg.setTitle(options.title);
		return this.installDlg;
	},
	
	
	getLocalDialog: function(options){
		var _this = this;
		
		if (!this.serviceDlg) {
			var dlg = new Ext.Window({
				title: '创建本地服务单元',
				autoCreate: true,
				resizable: false,
				constrain: true,
				constrainHeader: true,
				minimizable: false,
				maximizable: false,
				stateful: false,
				modal: true,
				buttonAlign: "right",
				defaultButton: 0,
				width: 450,
				height: 230,
				minWidth: 300,
				footer: true,
				closable: true,
				closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: [{
					xtype: 'form',
					autoScroll: true,
					labelWidth: 75,
					border: false,
					bodyStyle: 'padding:10px;',
					defaultType: 'textfield',
					defaults: {
						anchor: '-18'
					},
					msgTarget: 'title',
					items: [{
						name: 'unit-name',
						allowBlank: false,
						fieldLabel: '单元名称',
						regex: Identifier.regex,
						regexText: Identifier.regexText,
						listeners: {
							change: function(field, newValue, oldValue){
								var form = field.ownerCt.getForm();
								var f = form.findField('namespace');
								if (f.getValue() == '') {
									var s = targetNamespace.charAt(targetNamespace.length - 1) == '/' ? '' : '/';
									f.setValue(targetNamespace + s + newValue);
								}
							}
						}
					}, {
						name: 'namespace',
						fieldLabel: '命名空间',
						allowBlank: false,
						regex: Namespace.regex,
						regexText: Namespace.regexText
					}, {
						name: 'component-name',
						xtype: 'hidden',
						fieldLabel: '组件名',
						regex: NCName.regex,
						regexText: NCName.regexText
					}, {
						name: 'component',
						hiddenName: 'component',
						xtype: 'combo',
						fieldLabel: '组件',
						allowBlank: false,
						forceSelection: true,
						store: _this.getComponentStore('service-engine', ''),
						triggerAction: 'all',
						editable: false,
						valueField: 'component',
						displayField: 'label',
						listeners: {
							select: function(sender, record, index){
								var form = sender.ownerCt.getForm();
								var field = form.findField('component-name');
								field.setValue(record.get('component-name'));
							}
						}
					}, {
						name: 'desc',
						xtype: 'textarea',
						fieldLabel: '描述',
						allowBlank: false
					}]
				}]
			});
			
			dlg.ok = dlg.addButton({
				text: Ext.Msg.buttonText.ok,
				disabled: false
			}, function(){
				var formPanel = dlg.getComponent(0);
				var form = formPanel.getForm();
				
				form.submit({
					url: 'project_ctrl.jsp',
					clientValidation: true,
					params: {
						operation: 'createInternalUnit',
						project: project
					},
					success: function(){
						if (dlg.options.callback) 
							dlg.options.callback(form.getValues());
						dlg.hide();
					},
					failure: function(form, action){
						if (action.response) 
							Viewer.showException(action.response);
					}
				});
			});
			dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
				dlg.hide();
			});
			
			dlg.render(Ext.getBody());
			this.serviceDlg = dlg;
		}
		
		this.serviceDlg.options = options;
		var formPanel = this.serviceDlg.getComponent(0);
		var form = formPanel.getForm();
		form.findField('component').store.reload();
		form.reset();
		if (options.params) {
			form.setValues(options.params);
		}
		else {
		}
		return this.serviceDlg;
	},
	
	getServerDialog: function(options){
		var _this = this;		
		if (!this.serverDlg) {
			var dlg = new Ext.Window({
				title: '创建发布服务',
				autoCreate: true,
				resizable: false,
				constrain: true,
				constrainHeader: true,
				minimizable: false,
				maximizable: false,
				stateful: false,
				modal: true,
				buttonAlign: "right",
				defaultButton: 0,
				width: 460,
				height: 300,
				minWidth: 300,
				footer: true,
				closable: true,
				closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: [{
					xtype: 'form',
					autoScroll: true,
					labelWidth: 85,
					border: false,
					bodyStyle: 'padding:10px;',
					defaultType: 'textfield',
					defaults: {
						anchor: '-20'
					},
					items: [{
						name: 'service-name',
						fieldLabel: '服务名',
						allowBlank: false,
						regex: NCName.regex,
						regexText: NCName.regexText,
						listeners: {
							change: function(field, newValue, oldValue){
								var form = field.ownerCt.getForm();
								var f = form.findField('unit-name');
								var ns = form.findField('namespace');
								if (f.getValue() == '') {
									f.setValue(newValue);
									var s = targetNamespace.charAt(targetNamespace.length - 1) == '/' ? '' : '/';
									ns.setValue(targetNamespace + s + newValue + '_unit');
								}
							}
						}
					}, {
						name: 'component',
						hiddenName: 'component',
						xtype: 'combo',
						fieldLabel: '发布服务组件',
						allowBlank: false,
						forceSelection: true,
						store: _this.getComponentStore('binding-component', 'consumer'),
						triggerAction: 'all',
						editable: false,
						valueField: 'component',
						displayField: 'label',
						listeners: {
							select: function(sender, record, index){
								var form = sender.ownerCt.getForm();
								var field = form.findField('component-name');
								field.setValue(record.get('component-name'));
							}
						}
					}, {
						name: 'engine-services',
						hiddenName: 'engine-services',
						xtype: 'combo',
						emptyText: '<不选择该选项则需要自定义路由信息>',
						fieldLabel: '引擎服务',
						valueField: 'engine-services',
						displayField: 'label',
						triggerAction: 'all',
						editable: true,
						allowBlank: true,
						store: _this.getServiceStore('binding-component'),
						listeners: {
							select: function(sender, record, index) {
								var services = record.get('engine-services');
								if (services.split('#').length > 0) {
									if (services.split('#')[2] == 'undefined' ||
									!services.split('#')[2]) {
										alert('该引擎服务中没有任何接口!');
										sender.setValue('');
										sender.hiddenField.value = '';
									}	
								} else {
									alert('该引擎服务中没有任何接口!');
									sender.setValue('');
									sender.hiddenField.value = '';
								}		
							}
						}
					}, {
						name: 'unit-name',
						allowBlank: false,
						fieldLabel: '单元名称',
						regex: Identifier.regex,
						regexText: Identifier.regexText,
						listeners: {
							change: function(field, newValue, oldValue){
								var form = field.ownerCt.getForm();
								var f = form.findField('namespace');
								if (f.getValue() == '') {
									var s = targetNamespace.charAt(targetNamespace.length - 1) == '/' ? '' : '/';
									f.setValue(targetNamespace + s + newValue);
								}
							}
						}
					}, {
						name: 'namespace',
						fieldLabel: '命名空间',
						allowBlank: false,
						regex: Namespace.regex,
						regexText: Namespace.regexText
					}, {
						name: 'component-name',
						xtype: 'hidden',
						fieldLabel: '组件名',
						regex: NCName.regex,
						regexText: NCName.regexText
					}, {
						name: 'desc',
						xtype : 'textarea',
						fieldLabel: '描述',
						allowBlank: false
					}]
				}]
			});
			
			dlg.ok = dlg.addButton({
				text: Ext.Msg.buttonText.ok,
				disabled: false
			}, function(){
				var formPanel = dlg.getComponent(0);
				var form = formPanel.getForm();
	
				form.submit({
					url: 'project_ctrl.jsp',
					clientValidation: true,
					params: {
						operation: 'createProxyUnit',
						project: project
					},
					success: function(){
						if (dlg.options.callback) 
							dlg.options.callback(form.getValues());
						dlg.hide();
					},
					failure: function(form, action){
						if (action.response) 
							Viewer.showException(action.response);
					}
				});
			});
			dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
				dlg.hide();
			});
			
			dlg.render(Ext.getBody());
			this.serverDlg = dlg;
		}
		this.serverDlg.options = options;
		var formPanel = this.serverDlg.getComponent(0);
		var form = formPanel.getForm();
		form.findField('component').store.reload();
		form.findField('engine-services').store.reload();
		form.reset();
		if (options.params) {
			form.setValues(options.params);
		}
		return this.serverDlg;
	},
	
	// 添加共享类库对话框
	getPluginDialog : function(options) {
		var _this = this;
		
		if (!this.pluginDlg) {
			var dlg = new Ext.Window({
				title: '创建共享类库',
				autoCreate: true,
				resizable: false,
				constrain: true,
				constrainHeader: true,
				minimizable: false,
				maximizable: false,
				stateful: false,
				modal: true,
				buttonAlign: "right",
				defaultButton: 0,
				width: 450,
				height: 180,
				minWidth: 300,
				footer: true,
				closable: true,
				closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: [{
					xtype: 'form',
					autoScroll: true,
					labelWidth: 75,
					border: false,
					bodyStyle: 'padding:10px;',
					defaultType: 'textfield',
					defaults: {
						anchor: '-18'
					},
					items: [
					{
						name: 'file',
						fieldLabel: '库名称'
					}, {
						name: 'desc',
						fieldLabel: '描述',
						allowBlank: true
					}, {
						name: 'version',
						fieldLabel: '版本号',
						allowBlank: true,
						emptyText: '1.0'
					}]
				}]
			});
			
			dlg.ok = dlg.addButton({
				text: Ext.Msg.buttonText.ok,
				disabled: false
			}, function(){
				var formPanel = dlg.getComponent(0);
				var form = formPanel.getForm();
				
				form.submit({
					url: 'project_ctrl.jsp',
					clientValidation: true,
					params: {
						operation: 'createPlugin',
						project: project
					},
					success: function(){
						if (dlg.options.callback) 
							dlg.options.callback(form.getValues());
						dlg.hide();
					},
					failure: function(form, action){
						if (action.response) 
							Viewer.showException(action.response);
					}
				});
			});
			dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
				dlg.hide();
			});
			
			dlg.render(Ext.getBody());
			this.pluginDlg = dlg;
		}
		this.pluginDlg.options = options;
		var formPanel = this.pluginDlg.getComponent(0);
		var form = formPanel.getForm();
		form.reset();
		if (options.params) {
			form.setValues(options.params);
		}
		return this.pluginDlg;
	},
	
	getComponentStore: function(type, role){
		var _this = this;
		var compStore = new Ext.data.JsonStore({
			url: 'project_ctrl.jsp',
			baseParams: {
				operation: 'loadComponents',
				type: type,
				role: role,
				project: project
			},
			root: 'items',
			fields: [{
				name: 'label',
				type: 'string'
			}, {
				name: 'component-name',
				type: 'string'
			}, {
				name: 'component',
				type: 'string'
			}],
			listeners: {
				'loadexception': function(proxy, obj, response){
					Viewer.showException(response);
				}
			}
		});
		return compStore;
	},
	
	getRevisionStore: function(type){
		var _this = this;
		var revStore = new Ext.data.JsonStore({
			url: 'project_ctrl.jsp',
			baseParams: {
				operation: 'getRevisions',
				type: type,
				project: project
			},
			root: 'revisions',
			fields: [{
				name: 'label',
				type: 'string'
			},{
				name: 'rev',
				type: 'string'
			}],
			listeners: {
				'loadexception': function(proxy, obj, response){
					Viewer.showException(response);
				}
			}
		});
		return revStore;
	},
	
	getServiceStore: function(type){
		// 取得引擎服务列表
		var _this = this;
		var serviceStore = new Ext.data.JsonStore({
			url: 'project_ctrl.jsp',
			baseParams: {
				operation: 'loadEngineServices',
				type: type,
				project: project
			},
			root: 'items',
			fields: [{
				name: 'label',
				type: 'string'
			}, {
				name: 'service-name',
				type: 'string'
			}, {
				name: 'engine-services',
				type: 'string'
			}],
			listeners: {
				'loadexception': function(proxy, obj, response){
					Viewer.showException(response);
				}
			}
		});
		return serviceStore;
	}
};

}();