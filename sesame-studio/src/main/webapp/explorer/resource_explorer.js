Ext.BLANK_IMAGE_URL='../images/s.gif';

var Explorer = function(){

var Namespace = {
		regex: /^(([a-zA-Z][0-9a-zA-Z+\\-\\.]*:)?\/{0,2}[0-9a-zA-Z;\/?:@&=+$\\.\\-_!~*'()%]+)?(#[0-9a-zA-Z;\/?:@&=+$\\.\\-_!~*'()%]+)?$/, 
		regexText: '命名空间必须是合法的URI, 不能包含空白字符'
	};

//变量
var _this;
var viewport, resourceTree, tabPanel;

var saveAction;

var viewers = {
	'xsd' : '../schema/schema_viewer.jsp?schema='
};

var associations = {
	'css' : { type: 'text', syntax: 'css'},
	'doc' : { type: 'application'},
	'jar' : { type: 'application'},
	'java' : { type: 'text', syntax: 'java'},
	'js' : { type: 'text', syntax: 'javascript'},
	'sql' : { type: 'text', syntax: 'sql'},
	'txt' : { type: 'text', syntax: 'text'},
	'xml' : { type: 'text', syntax: 'html'},
	'xsd' : { type: 'text', syntax: 'html'},
	'xsl' : { type: 'text', syntax: 'xsl'},
	'wsdl' : { type: 'text', syntax: 'html'}
};

return {
	
init : function(){
	Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
	Ext.QuickTips.init();
	
	_this = this;

	var rootNode = new Ext.tree.AsyncTreeNode({
	    id: 'ROOT',
		name: ''
	});
	resourceTree = new Ext.tree.TreePanel({
		title: '系统资源',
		region: 'west', split: true,
		border: false, width: 260,
		style : {
			'border-right': '1px solid #99BBE8'
		},
		rootVisible: false, lines: false,
		animate: false, autoScroll: true, trackMouseOver: false,
		tbar: [],
		selModel: new Ext.tree.MultiSelectionModel(),
		loader: new Ext.tree.TreeLoader({
			dataUrl: 'resource_ctrl.jsp', 
			baseAttrs: {},
			baseParams: {operation:'getResourceFiles', filter:'^[^\\.]+(\\..*|.*)$'},
			uiProviders: {'resource':Ext.ux.ResourceNodeUI},
			listeners: {
				"beforeload": function(treeLoader, node) {
					this.baseParams.path = node.getPath('name');
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
				panel.getRootNode().reload();
			}
		}],
		listeners: {
			beforeappend: function(tree, parent, node){
				var a = node.attributes;
				var text = (a.desc ? a.desc : a.name) + (a.revision > -1 ? '&nbsp;<span style="color:olive;">'+a.revision+'</span>' : '');
				a.icon = _this.getOverlayIcon(node);
				node.setText(text);
			},
			click : function(node, e){
				//if (!node.isLeaf()) node.expand();
			},
			dblclick : function(node, e){
		        if(node.isLeaf()){
					var a = node.attributes;
		            var viewer = node.parentNode.attributes.viewer;
					var file = node.getPath('name').substring(2);
					_this.openFile(file);
		        }
				
			}
		},
		root: rootNode
	});
	 new Ext.tree.TreeSorter(resourceTree, {
	 	folderSort: true
	});
	
	tabPanel = new Ext.TabPanel({
		region: 'center',
		border: false,
		style : {
			'border-left': '1px solid #99BBE8'
		},
		listeners: {
			tabchange: function(sender, tab) {
				if (tab) {
					saveAction.setDisabled(tab.dirty==false);
				}
				else {
					saveAction.disable();
				}
			}
		}
	});
	
	viewport = new Ext.Viewport({
		stateId: 'resource_explorer',
		layout: 'border',
		items: [
			resourceTree, tabPanel
		]
	});
	
	var newFoldAction = new Ext.Action({
		text: '新建目录',
		icon:"../images/tool16/newfolder_wiz.gif",
		disabled: true,
		handler: function(){
		 	var item = this;
			
			var cNode = resourceTree.selModel.getSelectedNodes()[0];
		 	var a = cNode.attributes;
			var callback = function(values){
				cNode.expand(false, false, function(){
					var file = values['file-name'];
					var node = cNode.findChild("name", file);
					if (!node) {
						node = new Ext.tree.AsyncTreeNode({
							iconCls: 'x-icon-folder',
							text: file,
							name: file,
							status: values.status,
							allowRemove: true
						});
						cNode.appendChild(node);
					}
					node.select();
				});
			};
			_this.getFileDialog({
				title: this.text, 
				path: cNode.getPath('name').substring(2), 
				operation:'createFolder', 
				callback: callback
			});
		}
	});
	var newFileAction = new Ext.Action({
		text: '新建文件',
		icon:"../images/tool16/newfile_wiz.gif",
		disabled: true,
		handler: function(){
		 	var item = this;
			var cNode = resourceTree.selModel.getSelectedNodes()[0];
			if (cNode.isLeaf()) cNode = cNode.parentNode;
		 	var a = cNode.attributes;
			var callback = function(values){
				cNode.expand(false, false, function(){
					var file = values['file-name'];
					var node = cNode.findChild("name", file);
					if (!node) {
						node = new Ext.tree.AsyncTreeNode({
							iconCls: 'x-icon-'+_this.getExtension(file),
							text: file,
							name: file,
							status: values.status,
							allowRemove: true,
							leaf: true
						});
						cNode.appendChild(node);
					}
					node.select();
				});
			};
			_this.getFileDialog({
				title: this.text, 
				path: cNode.getPath('name').substring(2), 
				operation: 'createFile',
				callback: callback
			});
		}
	});
	var removeAction = new Ext.Action({
		text: '删除',
		itemId: 'remove',
		icon: '../images/icons/remove.gif',
		tooltip: '删除',
		disabled: true,
	    handler: function(){
			if (!window.confirm('确实要删除选中的项吗？'))  return false;

			var sels = resourceTree.getSelectionModel().getSelectedNodes();
			var files = [];
			for (var i = sels.length - 1; i >= 0; i--) {
				var n = sels[i];
				if (!n || !n.attributes.allowRemove)  continue;
				files.push(n.getPath('name'));
			}
			Ext.Ajax.request({
				method: 'POST', 
				url: 'resource_ctrl.jsp', 
				params: {operation:"remove", files: Ext.encode(files) },
				callback: function(options, success, response){
					if (success) {
						var result = Ext.decode(response.responseText);
						if (result.success==false) {
							alert(result.message);
							Ext.each(result.effected, function(file){
								resourceTree.selectPath(file, 'name', function(success, node){
									if (success) node.remove();
								});
							});
							return;
						}
						for (var i = sels.length - 1; i >= 0; i--) {
							var n = sels[i];
							if (!n || !n.attributes.allowRemove)  continue;
							n.remove();
						}
					}
					else {
						_this.showException(response);
					}
				}
			});
		}
	});
	
	var openAction = new Ext.Action({
		text: '打开',
		itemId: 'open-target',
		tooltip: '打开',
		icon: '../images/tool16/opentype.gif',
	    handler : function(){
			var sels = resourceTree.selModel.getSelectedNodes();
			if (sels.length < 1) return;
			var n =  sels[0], a = n.attributes;
			var file = n.getPath('name').substring(2);
			var extension = _this.getExtension(file);
			if (!n.isLeaf())
				n.expand();
			else {
				_this.openFile(file);
			}
		}
	});
	
	//保存操作
	saveAction = new Ext.Action({
		itemId: 'save',
		icon: '../images/tool16/save_edit.gif',
	    tooltip: '保存',
	    disabled: true,
	    handler : function(button, e){
	    	var panel = tabPanel.getActiveTab();
	    	var code = panel.get('file-editor').getCode();
	    	if (panel.dirty) {
	    		panel.bwrap.mask('保存中...');
				panel.getForm().submit({
					method: 'POST', 
					url: 'file_ctrl.jsp', 
					params: {operation:"save", file: panel.itemId, type: "system", content: code },
					success: function(form, action){
						resourceTree.selectPath('//'+panel.itemId, 'name', function(success, node){
							if (success) {
								var a = node.attributes;
								a.revision = action.result.revision;
								a.status = action.result.status;
								var text = (a.desc ? a.desc : a.name) + (a.revision > -1 ? '&nbsp;<span style="color:olive;">'+a.revision+'</span>' : '');
								node.getUI().iconNode.src = _this.getOverlayIcon(node);
								node.setText(text);
							}
						});
			    		panel.commit();
			    		button.disable();
	    				panel.bwrap.mask('保存成功');
						panel.bwrap.unmask.defer(2000, panel.bwrap);
					},
					failure: function(form, action){
						panel.bwrap.unmask();
						if (action.failureType == Ext.form.Action.CONNECT_FAILURE) 
							_this.showException(action.response); 
						else if (action.failureType == Ext.form.Action.SERVER_INVALID) 
							alert(action.result.message);
					}
				});
	    	}
	    	else {
	    		this.disable();
	    	}
	    }
	});
		
	resourceTree.selModel.on('selectionchange', function(){
		var sels = this.getSelectedNodes();
		if (sels.length<1) {
			removeAction.disable();
			newFoldAction.disable();
			newFileAction.disable();
			return;
		}
		var node = sels[0];
		var a = node.attributes;
		removeAction.setDisabled(!a.allowRemove);
		newFoldAction.setDisabled(node.isLeaf() && node.parentNode.getDepth()==0);
		newFileAction.enable();
	});
	
	var importMenu = new Ext.menu.Menu({
		id: 'import-menu',
		items :[{
			itemId: 'check-out',
			text: '从版本库获取',
     		handler: function(){
     			VersionDlg.getCheckoutDialog({
     				node: resourceTree.getRootNode(),
     				callback: function(result) {
						var cNode = resourceTree.getRootNode();
						var node = _this.createResourceNode(result);
						cNode.appendChild(node);
						node.select();
						
						var resources = top.Application.launcher.items.get("all-resources");
						resources.menu.add({
							id: result['name'],
							text: result['desc'],
							iconCls: 'bogus',
							handler: top.Application.resourcesMenu.createWindow,
					        scope: top.Application.resourcesMenu,
							resizable: false,
							maximizable: false
						});
     					
     				}
     			});
     		}
		}]
	});

    var ctxMenu = new Ext.menu.Menu({
     id: 'ctxmenu',
     items: [
     	openAction,
     	'-',
     	{
     		text: '新建',
     		handler: function(){ return false; },
     		menu: {
     			items: [newFoldAction, newFileAction]
     		}
     	},
		'-',
		{
			text: '上传文件',
			//icon:"../images/tool16/newfile_wiz.gif",
			handler: function(){
			 	var item = this;
				var cNode = this.parentMenu.node;
				if (cNode.isLeaf()) cNode = cNode.parentNode;
			 	var a = cNode.attributes;
				var callback = function(values){
					cNode.expand(false, false, function(){
						var file = values['file-name'];
						var node = cNode.findChild("name", file);
						if (!node) {
							node = new Ext.tree.AsyncTreeNode({
								iconCls: 'x-icon-'+_this.getExtension(file),
								text: file,
								name: file,
								allowRemove: true,
								leaf: true
							});
							cNode.appendChild(node);
						}
						node.select();
					});
				};
				_this.getUploadDialog({params:{path:cNode.getPath('name')}, callback:callback}).show();
			}
		},
		{
			id: 'download',
			text: '下载文件',
			handler: function(){
			 	var item = this;
				var node = this.parentMenu.node;
				alert('暂不支持下载');
				var callback = function(values){
				};
				_this.downloadFile({params:{path:node.getPath('name')}, callback:callback});
			}
		},
		'-',
		{
			itemId: 'info',
			text: '属性',
			handler: function(item, e){
				e.stopEvent();
				var node = this.parentMenu.node;
				if (!node) return true;
				
				var root = node.getOwnerTree().getRootNode().getPath('name');
				var path = node.getPath('name').substring(root.length+1);

				Ext.Ajax.request({
					method: 'POST', 
					url: 'resource_ctrl.jsp', 
					params: {operation: "info", path: path },
					callback: function(options, success, response) {
						if (success) {
							Ext.Msg.show({
								title: '属性',
								msg: response.responseText,
								buttons: Ext.Msg.OK
							});
						}
						else {
							_this.showException(response);
						}
					}
				});
			}
		},
		removeAction,
		{
			itemId: 'rename',
			text: '重命名',
			icon:"../images/icons/rename.png",
			handler: function(item, e){
				e.stopEvent();
				var node = this.parentMenu.node;
				if (!node) return true;

				var a = node.attributes;
				var newName = prompt("重命名 ", a.name);
				if (newName === null) {
					return;
				}
				else if (newName == a.name) {
					return;
				}
				else 	if (!newName.match(/^[A-Za-z_][\w\.]*$/)) {
					alert('名称必须以字母开头，并且不能包含空白字符');
					return false;
				}

				Ext.Ajax.request({
					method: 'POST', 
					url: 'resource_ctrl.jsp', 
					params: {operation:"rename", path: node.getPath('name'), newName: newName },
					callback: function(options, success, response) {
						if (success) {
							a.name =  newName;
							if (a.desc)
								node.setText(a.desc+"["+a.name+"]");
							else
								node.setText(a.name);
						}
						else {
							_this.showException(response);
						}
					}
				});
			}
		},
		'-',
		{
			itemId: 'scm',
			text: '版本控制',
     		handler: function(){ return false; },
     		menu: {
     			listeners : {
     				beforeshow:function(){
     					var a = ctxMenu.node.attributes;
     					var pa = ctxMenu.node.parentNode.attributes;
     					this.items.get('import').setDisabled(ctxMenu.node.getDepth()>1 || a.revision);
						this.items.get('svnUrl').setDisabled(!a.revision);
     					this.items.get('disconnect').setDisabled(ctxMenu.node.getDepth()>1 || !a.revision);
     					this.items.get('update').setDisabled(!a.revision);
     					this.items.get('commit').setDisabled(!a.revision && !pa.revision);
     					this.items.get('revert').setDisabled(!a.revision);
     					this.items.get('resolve').setVisible(a.status=='C');
     					this.items.get('cleanup').setDisabled(!a.revision);
     				}
     			},
     			items: [{
     				itemId: 'import',
				    text: '添加到版本库',
					icon: '../images/obj16/props_rep.gif',
				    handler: function(){
						if (!ctxMenu.node) return true;
						VersionDlg.getReposDialog({node: ctxMenu.node}).show();
				    }
     			},{
					itemId: 'svnUrl',
					text: 'SVN路径',
					icon: '../images/obj16/link.png',
					handler: function(){
						if (!ctxMenu.node) return true;
						alert(ctxMenu.node.attributes.reposUrl);
				    }
				},{
     				itemId: 'disconnect',
				    text: '与版本库断开连接',
					icon: '../images/elcl16/launch_disconnect.gif',
				    handler: function(){
						if (!ctxMenu.node) return true;
						var a = ctxMenu.node.attributes;
						
						Ext.Ajax.request({
							method: 'POST', 
							url: 'version_ctrl.jsp', 
							timeout: 90 * 1000,
							params: {operation:"disconnect", path: a.name },
							callback: function(options, success, response) {
								if (success) {
									delete a.revision;
									ctxMenu.node.getUI().iconNode.src=Ext.BLANK_IMAGE_URL;
									ctxMenu.node.setText(a.desc || a.name);
									ctxMenu.node.reload();
								}
								else {
									_this.showException(response);
								}
							}
						});
				    }
     			},
     			'-',
				{
					itemId: 'update',
					icon: '../images/tool16/update.gif',
					text: '更新',
					handler: function(){
						Ext.getBody().mask('正在更新...', 'x-mask-loading');
						Ext.Ajax.request({
							method: 'POST', 
							url: 'version_ctrl.jsp', 
							timeout: 3600 * 1000,
							params: {operation:"update", path: ctxMenu.node.getPath('name').substring(2) },
							callback: function(options, success, response) {
								if (success) {
									ctxMenu.node.reload();
									Ext.getBody().mask(response.responseText);
									Ext.getBody().unmask.defer(3000, Ext.getBody());
								}
								else {
									Ext.getBody().unmask();
									_this.showException(response);
								}
							}
						});
					}
				},{
					itemId: 'commit',
					icon: '../images/tool16/commit.gif',
					text: '提交',
					handler: function(){
						VersionDlg.getCommitDialog({
							path: ctxMenu.node.getPath('name').substring(2),
							callback: function(result) {
								var a = ctxMenu.node.attributes;
								a.revision = result.revision;
								var text = (a.desc || a.name) + '&nbsp;<span style="color:olive;">'+a.revision+'</span>';
								ctxMenu.node.getUI().iconNode.src='../images/ovr16/version_controlled.gif';
								ctxMenu.node.setText(text);
								if (ctxMenu.node.reload) {
									ctxMenu.node.reload();
								}
							}
						});
					}
				},{
					itemId: 'revert',
					icon: '../images/tool16/revert.gif',
					text: '还原',
					handler: function(){
						Ext.getBody().mask('正在还原...', 'x-mask-loading');
						Ext.Ajax.request({
							method: 'POST', 
							url: 'version_ctrl.jsp', 
							timeout: 90 * 1000,
							params: {operation:"revert", path: ctxMenu.node.getPath('name').substring(2) },
							callback: function(options, success, response) {
								if (success) {
									ctxMenu.node.reload();
									Ext.getBody().mask(response.responseText);
									Ext.getBody().unmask.defer(3000, Ext.getBody());
								}
								else {
									Ext.getBody().unmask();
									_this.showException(response);
								}
							}
						});
					}
				},{
					itemId: 'resolve',
					text: '解决冲突',
					handler: function(){
						var node = ctxMenu.node;
						VersionDlg.getResolveDialog({
							path: node.getPath('name').substring(2),
							callback: function(result) {
								if (node.parentNode.reload) {
									node.parentNode.reload();
								}
							}
						});
					}
				},{
     				itemId: 'cleanup',
				    text: '清理',
					//icon: '../images/elcl16/launch_disconnect.gif',
				    handler: function(){
						Ext.Ajax.request({
							method: 'POST', 
							url: 'version_ctrl.jsp', 
							timeout: 90 * 1000,
							params: {operation:"cleanup", path: ctxMenu.node.getPath('name').substring(2) },
							callback: function(options, success, response) {
								if (success) {
									alert('清理完成');
								}
								else {
									_this.showException(response);
								}
							}
						});
				    }
     			}]
     		}
		},
		'-',
		{
			itemId: 'refresh',
			text: '刷新',
			icon:"../images/tool16/refresh.gif",
			handler: function(){
				var node = this.parentMenu.node;
				if (node.isLeaf()) node = node.parentNode;
				node.reload();
				node.collapse(true, false);
				node.expand(false, false);
			}
		}]
	});
	
	resourceTree.on('contextmenu', function(node, e){
		node.select();
		ctxMenu.node = node;
		ctxMenu.showAt(e.getXY());
        var a = node.attributes;
		var rename = ctxMenu.items.get('rename');
		rename.setDisabled(a.allowRemove==false);
		var scm = ctxMenu.items.get('scm');
		scm.setDisabled(node.getDepth()==1);
		return false;
	});
	
	resourceTree.getTopToolbar().insertButton(0, [
		new Ext.Toolbar.Button({
			iconCls: "x-icon-newpro",
			tooltip: '新建',
			menu: {
				items: [
					newFoldAction,
					newFileAction
				]
			}
		}),
		new Ext.Toolbar.Button({
			icon: '../images/tool16/import_wiz.gif',
			tooltip: '导入',
			menu: importMenu
		}),
		'-',
		openAction,
		removeAction,
		'-',
		'->',
		saveAction
	]);
	resourceTree.doLayout(true, true);
},

createResourceNode : function(config){
	var node = new Ext.tree.AsyncTreeNode(Ext.apply(config, {
		iconCls: 'x-icon-folder',
		//uiProvider: Ext.ux.ResourceNodeUI,
		allowRemove: true,
		leaf: false
	}));
	return node;
},

downloadFile : function(options) {
	
},

showException : function(response){
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
	
},

getExtension : function(filename) {
	var index = filename.lastIndexOf('.');
	if (index > 0)
		return filename.substring(index+1);
	else
		return '';
},

getOverlayIcon: function(node) {
	var a = node.attributes;
	switch(a.status) {
	case ' ': 
		return '../images/ovr16/version_controlled.gif';
	case 'A':
		return '../images/ovr16/addition.gif';
	case 'C':
		return '../images/ovr16/conflicted_unresolved.gif';
	case 'D':
		return '../images/ovr16/deleted.gif';
	case 'I':
		return Ext.BLANK_IMAGE_URL;
	case 'M': 
		return '../images/ovr16/dirty_ov.gif';
	case '?':
		return '../images/ovr16/new_resource.gif';
	default:
		return Ext.BLANK_IMAGE_URL;
	}
},

getFileDialog : function(options){
	var _this = this;
	
	if (!this.fileDlg) {
	    var dlg = new Ext.Window({
			title: '新建文件',
	        autoCreate: true,
	        resizable:false,
	        constrain:true,
	        constrainHeader:true,
	        minimizable:false,
	        maximizable:false,
	        stateful:false,
	        modal:true,
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
				defaults: { anchor: '-18' },
				msgTarget: 'title',
				items: [
					{
						name: 'file-name',
						allowBlank: false,
						fieldLabel: '名称',
						regex: /^[^\/\\]+$/, 
						regexText:'非法的文件名，不能包含/,\\'
				    }]
				}
			],
			keys: {
				key: Ext.EventObject.ENTER,
				fn: function(){ 
					dlg.btnOK.handler();
				}
			},
			buttons: [{
				text: Ext.Msg.buttonText.ok, 
				ref: '../btnOK',
				handler: function(){
					var formPanel = dlg.getComponent(0);
					var form = formPanel.getForm();
		
					form.submit({
						url: 'resource_ctrl.jsp', clientValidation: true, 
						params: {operation: dlg.options.operation, path:dlg.options.path},
						success: function(form, action){
							if (dlg.options.callback) {
								dlg.options.callback(Ext.apply(form.getValues(), action.result)); 
							}
							dlg.hide(); 
						},
						failure: function(form, action){ if (action.response) _this.showException(action.response); }
					});
				}
			},{
				text: Ext.Msg.buttonText.cancel, 
				handler: function(){ dlg.hide(); }
			}]
	    });
		
		dlg.render(Ext.getBody());
	    this.fileDlg = dlg;
	}
	
	this.fileDlg.options = options;
	this.fileDlg.setTitle(options.title);
	this.fileDlg.show(null, function(){
		var formPanel = this.getComponent(0);
		var form = formPanel.getForm();
		form.reset();
		form.findField('file-name').focus();
	});
    return this.fileDlg;
},

openFile: function(file) {
	var _this = this;
	var pos = file.lastIndexOf('.');
	var ext = pos>-1 ? file.substring(pos+1) : "";
	var mime = associations[ext];
	if (mime) {
		if (mime.type != "text") {
			alert('不能打开此类型的文件');
			return;
		}
	}
	else {
		alert("不能识别的文件类型");
		return;
	}
	
	var panel = tabPanel.get(file);
	if (panel == null) {
		var idx=file.lastIndexOf('/');
		panel = new Ext.form.FormPanel({
			itemId: file,
			title: idx>-1 ? file.substring(idx+1) : file,
			tabTip: file,
			closable: true, 
			dirty: false,
			hideLabels: true, hidden: true, hideMode: 'offsets',
			commit: function() {
				this.dirty = false;
				this.setTitle(this.initialConfig.title);
			},
			items: [{
				itemId: 'file-editor',
				name: 'source',
				xtype: 'codepress',
				anchor: '100%, 100%',
				language : mime.syntax,
				listeners: {
					initialize: function(){
						panel.bwrap.mask("载入中...");
						Ext.Ajax.request({
							method: 'GET',
							url: 'file_ctrl.jsp',
							params: {operation: 'load', type: "system", file: file},
							callback: function(options, success, response) {
								panel.bwrap.unmask();
								if (success) {
									var code = response.responseText;
									this.setCode(code);
								}
								else {
									this.toggleReadOnly();
									_this.showException(response);
								}
							}.createDelegate(this)
						});
						
					},
					change: function(field, evt){
						if (!panel.dirty) {
							panel.dirty = true;
							panel.setTitle("*"+panel.initialConfig.title);
							saveAction.enable();
						}
					}
				}
			}],
			listeners: {
				beforeclose : function(panel) {
					if (panel.dirty && !confirm("放弃您所做的修改吗？点击[确定]关闭，点击[取消]返回编辑状态")) {
						return false;
					}
				}
			}
		});
		tabPanel.add(panel);
	}
	tabPanel.setActiveTab(panel);
},

getUploadDialog : function(options){
	var _this = this;
	
	if (!this.uploadDlg) {
	    var dlg = new Ext.Window({
			title: '上传文件',
	        autoCreate: true,
	        resizable:false,
	        constrain:true,  constrainHeader:true,
	        minimizable:false, maximizable:false,
	        stateful:false, modal:true,
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
				labelWidth: 75,
				fileUpload: true,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18' },
				msgTarget: 'title',
				items: [
					{
						name: 'file-name',
						allowBlank: false,
						fieldLabel: '文件名',
						xtype: 'filefield',
						emptyText: '请选择文件',
						buttonCfg: {
							text: '',
							iconCls: 'upload-icon'
						},
						listeners: {
							fileselected : function(sender, value){
								var form = this.ownerCt.getForm();
								form['file-name'] = value;
							}
						}
				    }]
				}
			]
	    });
		
	    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:false}, function(){ 
			var formPanel = dlg.getComponent(0);
			var form = formPanel.getForm();

			form.submit({url: 'resource_ctrl.jsp', clientValidation: true, 
				params: {operation: 'uploadFile', path:dlg.options.params.path},
				success: function(){ if (dlg.options.callback) dlg.options.callback({'file-name': form['file-name']}); dlg.hide(); },
				failure: function(form, action){ if (action.response) _this.showException(action.response); }
			});
		});
	    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){ dlg.hide(); });
	    
		dlg.render(Ext.getBody());
	    this.uploadDlg = dlg;
	}
	else {
	}
	this.uploadDlg.options = options;
	var formPanel = this.uploadDlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	if (options.params) {
		form.setValues(options.params);
	}
    return this.uploadDlg;
},

getToolbar : function(){
	var _this = this;
	
	var buttons = [
		'->',
		{
			id: 'reload',
		    text: '刷新',
			icon: '../images/tool16/refresh.gif',
			cls: 'x-btn-text-icon',
		    handler : function(){
				var root = resourceTree.getRootNode();
				root.reload();
				root.collapse();  root.expand();
				return;
			}
		}
	];
	return buttons;
}

};

}();

Ext.onReady(Explorer.init, Explorer, true);

Ext.ux.ResourceNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
    // private
    renderElements : function(n, a, targetNode, bulkRender) {
		Ext.ux.ResourceNodeUI.superclass.renderElements.apply(this, arguments);
		Ext.fly(this.wrap).addClass('x-resource');
        Ext.fly(this.ctNode).applyStyles({'border-width':'0px 1px 1px 1px', 'border-style':'solid', 'border-color':'gray'});
	}
});