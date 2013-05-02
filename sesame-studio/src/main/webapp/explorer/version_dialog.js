var VersionDlg = function(){

var statusText = {
	'?': '新增',
	'A': '添加',
	'C': '冲突',
	'D': '删除',
	'M': '修改'
};

return {

//检出对话框
getCheckoutDialog: function(options){
	var _this = this;
	
	var dlg = this.checkoutDlg;
	if (typeof(dlg)=='undefined') {
		//版本库列表
		var repositoryPanel = _this.getRepositoryPanel();
		//版本库目录浏览面板
		var browsePanel = new Ext.tree.TreePanel({
			getRepositoryURL: function() {
				var node = this.getSelectionModel().getSelectedNode();
				if (node == null) {
					throw {message: '请选择目录'};
				}
				return this.getRootNode().text+node.getPath('name').substring(1);
			},
			reload : function(){
				this.getRootNode().reload();
			},
			itemId: 'browsePanel',
			title: '选择要检出的文件夹',
			xtype: 'treepanel',
			autoScroll: true,
			border: false,
			rootVisible: true,
			root: new Ext.tree.AsyncTreeNode({
				id: 'ROOT',
				name: '',
				text: 'none',
				iconCls: "x-icon-repo"
			}),
			loader: new Ext.tree.TreeLoader({
				dataUrl: 'version_ctrl.jsp', 
				baseAttrs: {},
				baseParams: {operation:'getFolders' },
				listeners: {
					'beforeload': function(loader, node){
						this.baseParams.repositoryURL = node.getOwnerTree().getRootNode().text;
						this.baseParams.path = node.getPath('name').substring(2);
					},
					'loadexception': function(loader, node, response){
						_this.showException(response); 
					}
				}
			}),
			tools: [{
				id: 'refresh',
				qtip: '刷新',
				handler: function(e, toolEl, panel, tc){
					var node = panel.getSelectionModel().getSelectedNode() || panel.getRootNode();
					node.reload();
				}
			}],
			listeners: {
				beforeappend: function(tree, parent, node){
					var a = node.attributes;
					var text = a.name + '&nbsp;<span style="color:olive;">'+a.revision+'</span>';
					node.setText(text);
				}
			}
		});
		var checkoutPanel = new Ext.form.FormPanel({
			itemId: 'checkoutPanel',
			title: '检出',
			height: 'auto', hidden: true,
			url: 'version_ctrl.jsp',
			autoScroll: true,
			labelWidth: 120,
			border: false,
			bodyStyle: 'padding:10px;',
			defaults: {
				anchor: '-20'
			},
			items: [{
				xtype: 'textfield',
				name: 'url',
				fieldLabel: '版本库URL',
				readOnly: true
			},{
				xtype: 'fieldset',
				title: '版本',
				layout: 'column',
				items: [{
					columnWidth: 0.5,
					xtype: 'panel',
					border: false,
					layout: 'form',
					defaults: {anchor: '100%'},
					items: {
						xtype: 'checkbox',
						name: 'head',
						checked: true,
						fieldLabel: '取出最新版本',
						listeners: {
							check : function(checkbox, checked){
								checkoutPanel.getForm().findField('version').setDisabled(checked);
							}
						}
					}
				},{
					columnWidth: 0.5,
					xtype: 'panel',
					border: false,
					layout: 'form',
					defaults: {anchor: '100%'},
					items: {
						xtype: 'numberfield',
						name: 'version',
						fieldLabel: '取出指定版本',
						allowDecimals: false,
						disabled: true
					}
				}]
			}]
		
		});
		
		var dlg = new Ext.Window({
			iconCls: 'repos-icon',
			title: '从版本库检出项目',
			autoCreate: true, resizable: false, constrain: true, constrainHeader: true,
			minimizable: false, maximizable: false,
			stateful: false, modal: true,
			buttonAlign: "right",	defaultButton: 0,
			width: 480, height: 400, minWidth: 300,
			closable: true, closeAction: 'hide',
			footer: true, plain: true,
			layout: 'card',
			activeItem: 0,
			items: [repositoryPanel, browsePanel, checkoutPanel],
			buttons: [{
				ref: '../btn-prev',
				text: '&laquo;上一步',
				disabled: true, 
				handler: function(button, e){
					var l = dlg.getLayout();
					var index = dlg.items.indexOf(l.activeItem);
					l.setActiveItem(--index);
					dlg['btn-prev'].setDisabled(index==0);
					dlg['btn-next'].setDisabled(index==dlg.items.length-1);
					dlg['btn-finish'].setDisabled(index<dlg.items.length-1);
				}
			},{
				ref: '../btn-next',
				text: '下一步&raquo;',
				disabled: false, 
				handler: function(button, e){
					var l = dlg.getLayout();
					try {
						if (l.activeItem==repositoryPanel) {
							var url = repositoryPanel.getRepositoryURL();
							browsePanel.getRootNode().setText(url);
							browsePanel.getRootNode().reload();
							l.setActiveItem(browsePanel);
						}
						else if (l.activeItem==browsePanel) {
							var url = browsePanel.getRepositoryURL();
							checkoutPanel.getForm().setValues({url:url});
							l.setActiveItem(checkoutPanel);
						}
					}
					catch (e) {
						alert(e.message);
						return;
					}
					var index = dlg.items.indexOf(l.activeItem);
					//l.setActiveItem(++index);
					dlg['btn-prev'].setDisabled(index==0);
					dlg['btn-next'].setDisabled(index==dlg.items.length-1);
					dlg['btn-finish'].setDisabled(index<dlg.items.length-1);
				}
			},{
				ref: '../btn-finish',
				text: '完成',
				disabled: true,
				handler: function(button, e){
					try {
						var val = checkoutPanel.getForm().getValues();
						var revision = val.head ? 0 : val.version;
						dlg.bwrap.mask('正在获取...', 'x-mask-loading');
						Ext.Ajax.request({
							method: 'POST', 
							url: 'project_ctrl.jsp', 
							timeout: 3600 * 1000,
							params: {operation:"checkout", url: val.url, revision: revision },
							callback: function(options, success, response) {
								dlg.bwrap.unmask();
								if (success) {
									var result = Ext.decode(response.responseText);
									dlg.options.callback(result);
									dlg.hide();
								}
								else {
									_this.showException(response);
								}
							}
						});
					}
					catch (e) {
						alert(e.message);
					}
				}
			},{
				text: Ext.Msg.buttonText.cancel, 
				handler: function(){ dlg.hide(); }
			}]
		});
		
		dlg.render(Ext.getBody());
		this.checkoutDlg = dlg;
	}
	dlg.options = options;
	
	
	var panel = dlg.getComponent(0);
	panel.reload();
	dlg.getLayout().setActiveItem(panel);
	dlg['btn-prev'].disable();
	dlg['btn-next'].enable();
	dlg['btn-finish'].disable();
	
	dlg.show();
	return dlg;
},

//提交对话框
getCommitDialog: function(options){
	var _this = this;
	var dlg = this.commitDlg;
	if (Ext.isEmpty(dlg)) {
		var checksm;
		dlg = new Ext.Window({
			title: '提交',
			autoCreate: true, resizable: true, constrain: true,
			constrainHeader: true, minimizable: false, maximizable: false,
			stateful: false, modal: true,
			buttonAlign: "right", defaultButton: 0,
			width: 560, height: 380, minWidth: 300,
			closable: true, closeAction: 'hide',
			plain: false, footer: true,
			layout: 'border',
			items: [{
				itemId: 'commit',
				xtype: 'form',
				region: 'north',
				url: 'version_ctrl.jsp',
				autoHeight: true,
				style: 'padding: 5px 5px 0px 5px;',
				bodyStyle: 'background-color: transparent;',
				labelAlign: 'top',
				border: false,
				defaultType: 'textfield',
				defaults: {
					anchor: '100%'
				},
				items: [{
					xtype: 'textarea',
					fieldLabel: '注释',
					name: 'comment',
					allowBlank: true
				},{
					xtype: 'combo',
					fieldLabel: '历史注释',
					name: 'his',
					forceSelection: true, editable: false,
					store: new Ext.data.JsonStore({
						url: 'version_ctrl.jsp',
						baseParams: {
							operation: 'getComments'
						},
						root: 'items',
						fields: [{
							name: 'comment',
							type: 'string'
						}]
					}),
					triggerAction: 'all', mode: 'remote',
					valueField: 'comment', displayField: 'comment',
					listeners: {
						select: function(combo, record, index){
							var field = this.ownerCt.getForm().findField('comment');
							field.setValue(record.get('comment'));
						}
					}
				}]
			},{
				region: 'center',
				itemId: 'changes',
				xtype: 'grid',
				border: false, 
				sm: checksm=new Ext.grid.CheckboxSelectionModel({
					checkOnly: true,
					listeners: {
						selectionchange: function(sender) {
							dlg.btnOK.setDisabled(this.getCount() < 1);
						}
					}
				}),
				cm: new Ext.grid.ColumnModel([
					checksm,
					{
						header: '资源', dataIndex: 'resource', width: 300,
						renderer: function(value, meta, record, row, col, store) {
							var icon= 'change.gif';
							switch (record.get('status')) {
								case '?':
								case 'A':
									icon = 'addition.gif';  break;
								case 'C':
									icon = 'conflicted_unresolved.gif';  break;
								case 'D':
									icon = 'deletion.gif';  break;
							}
							var cls = record.get('iconCls');
							
							meta.attr= 'style="padding:0px; line-height: 16px; vertical-align:middle;"';
							return '<img style="width:16px; height:16px;" src="../images/ovr16/'+icon+
								'" class="'+cls+'" />&nbsp;'+value;
						}
					},{
						header: '状态', dataIndex: 'status', width: 50,
						renderer: function(value, meta, record, row, col, store) {
							return statusText[value] || value;
						}
					},{
						header: '属性', dataIndex: 'prop', width: 40
					}
				]),
				store: new Ext.data.JsonStore({
					url: 'version_ctrl.jsp',
					baseParams: {operation: 'getChangeList', exclusion: '[]' },
					root: 'items',
					fields: [
						{name: 'resource',type: 'string'}, 
						{name: 'status',type: 'string'},
						{name: 'prop',type: 'string'},
						{name: 'iconCls',type: 'string'}
					],
					sortInfo: {
						field: 'resource',
						direction: 'ASC'
					},
					listeners: {
						loadexception: {
							fn: function(proxy, obj, response){
								this.showException(response);
							}, 
							scope: this
						}
					}
				}),
				viewConfig: {
					forceFit: true
				}
			}],
			buttons: [{
				text: Ext.Msg.buttonText.ok,
				ref: '../btnOK',
				disabled: true,
				handler: function(){
					var gridPanel = dlg.getComponent('changes');
					var selModel = gridPanel.getSelectionModel();
					var selected = [];
					selModel.each(function(record){
						selected.push(record.data);
					});
					
					var formPanel = dlg.getComponent('commit');
					var form = formPanel.getForm();
					
					dlg.bwrap.mask('正在提交...', 'x-mask-loading');
					Ext.Ajax.request({
						method: 'POST', 
						url: 'version_ctrl.jsp', 
						timeout: 3600 * 1000,
						form: form.getEl().dom,
						params: {
							operation: "commit",
							path: dlg.options.path,
							selected: Ext.encode(selected)
						},
						callback: function(options, success, response) {
							dlg.bwrap.unmask();
							if (success) {
								var result = Ext.decode(response.responseText);
								dlg.options.callback(result);
								dlg.hide();
							}
							else {
								_this.showException(response);
							}
						}
					});
				}
			},{
				text: Ext.Msg.buttonText.cancel, 
				handler: function(){ dlg.hide(); }
			}]
		});
		
		dlg.render(Ext.getBody());
		
		this.commitDlg = dlg;
	}

	dlg.options = options;
	var changePanel = dlg.get('changes');
	changePanel.store.load({
		params: { path: options.path },
		callback: function(array, options, success){
			if (success==false) {
				return;
			}
			if (array.length>0) {
				changePanel.getSelectionModel().selectAll();
				dlg.show();
			}
			else {
				alert('自上次提交之后没有任何变动');
			}
		}
	});
	var formPanel = dlg.get('commit');
	var form = formPanel.getForm();
	var field = form.findField('his');
	field.store.reload();
	return dlg;
},

getReposDialog: function(options){
	var _this = this;
	
	var dlg = this.reposDlg;
	if (typeof(dlg)=='undefined') {
		var repositoryPanel = _this.getRepositoryPanel();
		var dlg = new Ext.Window({
			iconCls: 'repos-icon',
			title: '版本控制',
			autoCreate: true, resizable: false, constrain: true, constrainHeader: true,
			minimizable: false, maximizable: false,
			stateful: false, modal: true,
			buttonAlign: "right",	defaultButton: 0,
			width: 450, height: 400, minWidth: 300,
			closable: true, closeAction: 'hide',
			footer: true, plain: true,
			layout: 'card',
			activeItem: 0,
			items: [repositoryPanel],
			buttons: [{
				text: Ext.Msg.buttonText.ok,
				disabled: false, 
				handler: function(button, e){
					var params = {};
					try {
						params.url = repositoryPanel.getRepositoryURL();
					}
					catch (e) {
						alert(e.message);
						return;
					}
					var a = dlg.node.attributes;
					
					button.disable();
					Ext.Ajax.request({
						method: 'POST', 
						url: 'version_ctrl.jsp', 
						timeout: 3600 * 1000,
						params: Ext.apply(params, {operation:"put", path: a.name }),
						callback: function(options, success, response) {
							button.enable();
							if (success) {
								var result = Ext.decode(response.responseText);
								a.revision = result.revision;
								dlg.node.getUI().iconNode.src='../images/ovr16/version_controlled.gif';
								var text = (a.desc || a.name) + '&nbsp;<span style="color:olive;">'+a.revision+'</span>';
								dlg.node.setText(text);
								dlg.hide();
							}
							else {
								_this.showException(response);
							}
						}
					});
				}
			},{
				text: '完成',
				hidden: true,
				handler: function(){
				}
			},{
				text: Ext.Msg.buttonText.cancel, 
				handler: function(){ dlg.hide(); }
			}]
		});
		
		dlg.render(Ext.getBody());
		this.reposDlg = dlg;
	}
	dlg.node = options.node;
	
	var panel = dlg.getComponent(0);
	panel.reload();
	return dlg;
},

getRepositoryPanel : function(){
	var _this = this;
	var panel = new Ext.Panel({
		getRepositoryURL: function() {
			var formPanel = this.get('createPanel');
			var params;
			if (formPanel.isVisible()) {
				var form = formPanel.getForm();
				if (!form.isValid()) throw {message:'输入值非法'};
				
				return form.getValues().url;
			}
			else {
				var node = this.get('listPanel').getSelectionModel().getSelectedNode();
				if (node == null) {
					throw {message: '请选择版本库'};
				}
				return node.text;
			}
			
		},
		reload : function(){
			 this.get('listPanel').getRootNode().reload();
		},
		border: false,
		itemId: 'repos-panel',
		layout: 'border',
		items: [{
			region: 'south',
			itemId: 'createPanel',
			title: '创建新版本库',
			height: 'auto', hidden: true,
			xtype: 'form',
			url: 'version_ctrl.jsp',
			autoScroll: true,
			labelWidth: 75,
			border: false,
			bodyStyle: 'padding:10px;',
			defaultType: 'textfield',
			defaults: {
				anchor: '-20'
			},
			items: [{
				name: 'url',
				allowBlank: false,
				fieldLabel: '版本库地址'
			},{
				name: 'username',
				fieldLabel: '用户名',
				xtype: 'hidden'
			},{
				name: 'passwd',
				//inputType: 'password',
				fieldLabel: '密码',
				xtype: 'hidden'
			}],
			tools: [{
				id: 'down',
				qtip: '隐藏',
				handler: function(e, toolEl, panel, tc){
					panel.hide();
					panel.ownerCt.doLayout(true, false);
				}
			}],
			listeners: {
				actionfailed : function(form, action) {
					if (action.response) _this.showException(action.response);
				}
			}
		},{
			region: 'center',
			itemId: 'listPanel',
			title: '已有版本库',
			xtype: 'treepanel',
			border: false,
			rootVisible: false,
			root: new Ext.tree.AsyncTreeNode({
				id: 'ROOT',
				text: 'none'
			}),
			loader: new Ext.tree.TreeLoader({
				dataUrl: 'version_ctrl.jsp', 
				baseAttrs: {},
				baseParams: {operation:'getSCMPrefs' },
				//uiProviders: {'project':Ext.ux.ProjectNodeUI},
				listeners: {
					'loadexception': function(loader, node, response){ _this.showException(response); }
				}
			}),
			tools: [{
				id: 'refresh',
				qtip: '刷新',
				handler: function(e, toolEl, panel, tc){
					panel.getRootNode().reload();
				}
			},{
				id: 'plus',
				qtip: '添加',
				handler: function(e, toolEl, panel, tc){
					panel.ownerCt.get('createPanel').show();
					panel.ownerCt.doLayout(true, false);
				}
			},{
				id: 'minus',
				qtip: '删除',
				handler: function(e, toolEl, panel, tc){
					var node = panel.getSelectionModel().getSelectedNode();
					if (node) {
						if (!confirm("确实要删除选中的URL吗？")) {
							return;
						}
						Ext.Ajax.request({
							method: 'POST', 
							url: 'version_ctrl.jsp', 
							params: {operation: "removeRepository", url: node.text },
							callback: function(options, success, response) {
								if (success) {
									node.remove();
								}
								else {
									_this.showException(response);
								}
							}
						});
					}
				}
			}]
		}]
	});
	
	return panel;
},

//解决冲突对话框
getResolveDialog: function(options){
	var _this = this;
	var dlg = this.resolveDlg;
	if (Ext.isEmpty(dlg)) {
		var checksm;
		dlg = new Ext.Window({
			title: '解决冲突',
			autoCreate: true, resizable: false, constrain: true,
			constrainHeader: true, minimizable: false, maximizable: false,
			stateful: false, modal: true,
			buttonAlign: "right", defaultButton: 0,
			width: 300, height: 240, 
			closable: true, closeAction: 'hide',
			plain: false, footer: true,
			layout: 'fit',
			items: [{
				itemId: 'form',
				xtype: 'form',
				region: 'north',
				url: 'version_ctrl.jsp',
				autoHeight: true,
				style: 'padding: 5px 5px 0px 5px;',
				bodyStyle: 'background-color: transparent;',
				labelAlign: 'top',
				border: false, hideLabel: true,
				defaultType: 'textfield',
				defaults: {
					anchor: '100%'
				},
				items: [{
					xtype: 'radiogroup',
					fieldLabel: '解决方式',
					columns: 1,
					items: [{
						boxLabel: '已手工合并版本',
						checked: true,
						name: 'choice',
						inputValue: 'merged'
					},{
						boxLabel: '使用我的版本',
						name: 'choice',
						inputValue: 'mine'
					},{
						boxLabel: '使用基础版本',
						name: 'choice',
						inputValue: 'base'
					},{
						boxLabel: '使用版本库中最新版本',
						name: 'choice',
						inputValue: 'incoming'
					}]
				}]
			}],
			buttons: [{
				text: Ext.Msg.buttonText.ok,
				handler: function(){
					var formPanel = dlg.getComponent('form');
					var form = formPanel.getForm();
					
					dlg.bwrap.mask('正在解决冲突...', 'x-mask-loading');
					form.submit({
						timeout: 1800 * 1000,
						params: {
							operation: "resolve",
							path: dlg.options.path
						},
						success: function(form, action){
							dlg.bwrap.unmask();
							dlg.hide();
							if (dlg.options.callback) {
								dlg.options.callback(action.result);
							}
						},
						failure: function(form, action){
							dlg.bwrap.unmask();
							if (action.response) 
								_this.showException(action.response);
						}
					});
				}
			},{
				text: Ext.Msg.buttonText.cancel, 
				handler: function(){ dlg.hide(); }
			}]
		});
		
		dlg.render(Ext.getBody());
		this.resolveDlg = dlg;
	}
	else {
	}

	dlg.options = options;
	dlg.show()
	return dlg;
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

showException : function(response){
	var statusText = response.responseText;
	if (!statusText) {
		switch (response.status) {
			case -1:
				alert('通讯超时，事务终止');  return;
			case 0:
				alert('通讯错误，连接服务器失败');   return;
			case 200: 
				return;
			default:
				alert (response.statusText+'('+response.status+')');  return;
		}
		statusText = '<table style="width:100%;">'+
			'<tr><td style="background: url(\'../images/top_bg.gif\') no-repeat; vertical-align:middle; font: bold 24px \'Times New Roman\', Verdana, Arial; height:54px; color:white;">Application Management Center</td></tr>'+
			'<tr><td><table align="center" border="0" cellpadding="0" cellspacing="8" width="100%"><tbody><tr>'+
	        '<td style="text-align: center; width:50px;"><img src="../images/notice.png"></img></td>'+
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
/*end of method*/

};

}();