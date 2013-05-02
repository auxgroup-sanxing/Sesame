Ext.BLANK_IMAGE_URL='../images/s.gif';

var NCName = {
		regex: /^[A-Za-z_]+[\w_\-.]*$/,
		regexText: '必须以字母或下划线开头，不能包含冒号和空白字符'
	};
var Identifier = {
		regex: /^[A-Za-z_]\w*$/,
		regexText:'必须以字母或下划线开头，后面可以有数字'
	};
var Namespace = {
		regex: /^(([a-zA-Z][0-9a-zA-Z+\\-\\.]*:)?\/{0,2}[0-9a-zA-Z;\/?:@&=+$\\.\\-_!~*'()%]+)?(#[0-9a-zA-Z;\/?:@&=+$\\.\\-_!~*'()%]+)?$/, 
		regexText: '命名空间必须是合法的URI, 不能包含空白字符'
	};

var Viewer = function(){
//变量
var layout, resourceTree;

var catalog = [
	{name:'components', title:'组件', icon:'../images/icons/component.gif', path:'../explorer/component_viewer.jsp?component=', cls:'active', installable:true}, 
	{name:'lib', title:'共享类库', icon:'../images/icons/java_jar.gif', installable:true, path:'../explorer/sharelib_viewer.jsp?lib='}, 
	{name:'transport', title:'传输端子', filter: '^.*\\.jar$', icon:'../images/icons/connection.gif', installable:true, path:'../explorer/transport_viewer.jsp?transport='}];

return {
	
	init : function(){
		Ext.Ajax.defaultHeaders = { 
			"Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8",
			"region" : 'warehouse'
		};
		Ext.QuickTips.init();
		Ext.Ajax.timeout = 900000;
		
		var _this = this;
	
		var ct = Ext.get('sample-box-inner');
		
		var rootNode = new Ext.tree.AsyncTreeNode({
		    id: 'ROOT',
		    name: '',
			children:[]
		});
		
		resourceTree = new Ext.tree.TreePanel({
			border: false,
			id: 'resource-ct',
			rootVisible: false,
			lines: false,
			loader: new Ext.tree.TreeLoader({
				requestMethod: 'GET', 
				dataUrl: 'ware_ctrl.jsp', 
				baseAttrs: {},
				baseParams: {operation:'getResources'},
				uiProviders: {'resource':Ext.ux.ResourceNodeUI},
				listeners: {
					'beforeload': function(treeLoader, node) {
						var a = node.attributes;
						this.baseParams.group = a.name;
						this.baseAttrs.icon = a.icon;
						if (a.filter)
							this.baseParams.filter = a.filter;
						else
							delete this.baseParams.filter;
					},
					'loadexception': function(loader, node, response){ _this.showException(response); }
				}
			}),
			listeners: {
				beforeappend: function(tree, parent, node){
					if (parent != tree.getRootNode()) {
						var a = node.attributes;
						node.setText(_this.getNodeText(a));
					}
				},
				click : function(node, e){
					if (!node.isLeaf()) node.expand();
				},
				dblclick : function(node, e){
			        if (node.isLeaf()){
						var a = node.attributes;
			            var viewer = node.parentNode.attributes.viewer;
			            var file = node.getPath('name').substring(2);
						var title = a.desc ? a.desc+'['+a.name+']' : a.name;
						top.Application.createWindow(file, title, viewer+file);
			        }
				}
			},
			animate: false,
			autoScroll: true,
			trackMouseOver: true,
			renderTo: ct,
			root: rootNode
		});

	    for(var i = 0, c; c = catalog[i]; i++) {
	        c.id = 'group-' + i;
	        var cNode = new Ext.tree.AsyncTreeNode({
				id: c.id,
				text: c.title,
				qtip: c.qtip,
				uiProvider: Ext.ux.ResourceGroupUI,
				cls: 'x-res-group-el',
				name: c.name,
				filter: c.filter,
				icon: c.icon,
				installable: c.installable,
				viewer: c.path
			});
			rootNode.appendChild(cNode);
	    }
		
		Ext.get('samples-cb').on('click', function(e){
			var img = e.getTarget('img', 2);
			if (img){
				Ext.getDom('samples').className = img.className;
			}
		});
		
		var addMenu = new Ext.menu.Menu({
			id: 'addMenu',
			items: [{
				catalog: 'components',
				text: '组件',
				handler: function(){
				 	var item = this;
					var cNode = rootNode.findChild("name", this.catalog);
					if (!cNode) return;
				 	var a = cNode.attributes;
					var callback = function(values){
						cNode.expand(false, false, function(){
							var node = cNode.findChild("name", values['file']);
							if (!node) {
								node = _this.createResourceNode({
									icon: a.icon,
									text: values['file'].replace(/\.jar$/, ''),
									path: a.path,
									name: values['file'],
									checked: values['checked'],
									desc: values['desc']
								});
								cNode.appendChild(node);
							}
							node.select();
						});
					};
					var dlg = Dialog.getComponentDialog({
						title: '添加组件', params: {component:item.catalog}, 
						operation: 'uploadCom',
						callback: callback
					});
					dlg.show();
				}
			},{
				catalog: 'lib',
				text: '共享库',
				handler: function(){
				 	var item = this;
					var cNode = rootNode.findChild("name", this.catalog);
					if (!cNode) return;
				 	var a = cNode.attributes;
					var callback = function(values){
						cNode.expand(false, false, function(){
							var node = cNode.findChild("name", values['file']);
							if (!node) {
								node = _this.createResourceNode({
									icon: a.icon,
									text: values['file'].replace(/\.jar$/, ''),
									path: a.path,
									checked: values['checked'],
									name: values['file'],
									desc: values['desc']
								});
								cNode.appendChild(node);
							}
							node.select();
						});
					};
					var dlg = Dialog.getComponentDialog({
						title: '添加共享库', params: {component:item.catalog}, 
						operation: 'uploadLib',
						callback: callback
					});
					dlg.show();
				}
			},{
				catalog: 'transport',
				text: '传输库',
				handler: function(){
				 	var item = this;
					var cNode = rootNode.findChild("name", this.catalog);
					if (!cNode) return;
				 	var a = cNode.attributes;
					var callback = function(values){
						cNode.expand(false, false, function(){
							var node = cNode.findChild("name", values['file']);
							if (!node) {
								node = _this.createResourceNode({
									icon: a.icon,
									text: values['file'],
									path: a.path,
									checked: values['checked'],
									name: values['file'],
									desc: values['desc']
								});
								cNode.appendChild(node);
							}
							node.select();
						});
					};
					var dlg = Dialog.getComponentDialog({
						title:'添加传输库', params: {component:item.catalog}, 
						operation: 'uploadTransport',
						callback: callback
					});
					dlg.show();
				}
			}]
		});

		var createMenu = new Ext.menu.Menu({
			id: 'createMenu',
			items: [{
				catalog: 'lib',
				text: '共享类库',
				handler: function(){
				 	var item = this;
					var cNode = rootNode.findChild("name", this.catalog);
					if (!cNode) return;
				 	var a = cNode.attributes;
					var callback = function(values){
						cNode.expand(false, false, function(){
							var node = cNode.findChild("name", values['file']);
							if (!node) {
								node = _this.createResourceNode({
									icon: a.icon,
									text: values['file'],
									path: a.path,
									checked: values['checked'],
									name: values['file'],
									desc: values['desc']
								});
								cNode.appendChild(node);
							}
							node.select();
						});
					};
					Dialog.getPluginDialog({
						title: item.text, params: {component:item.catalog}, 
						callback:callback
					}).show();
				}
			}]
		});
		
		var exportMenu = new Ext.menu.Menu({
			id: 'exportMenu',
			items: [{
				text: '项目',
				handler: function(){
				}
			},
			{
				text: '服务单元',
				handler: function(){
				}
			}]
		});
			
		var syncMenu = new Ext.menu.Menu({
			id: 'syncMenu',
			items: [
				{
	 				itemId: 'import',
				    text: '添加到版本库',
					icon: '../images/obj16/props_rep.gif',
				    handler: function(){
						_this.getReposDialog().show();
				    }
	 			},{
	 				itemId: 'disconnect',
					disabled: !isVersioned,
				    text: '与版本库断开连接',
					icon: '../images/elcl16/launch_disconnect.gif',
				    handler: function(){
						Ext.Ajax.request({
							method: 'POST', 
							url: 'version_ctrl.jsp', 
							timeout: 90 * 1000,
							params: {operation:"disconnect"},
							callback: function(options, success, response) {
								if (success) {
									isVersioned = false;
									alert('断开成功');
								} else {
									_this.showException(response);
								}
							}
						});
				    }
	 			},
	 			'-',
	 			{
					text: '同步',
					itemId: 'synchronize',
					disabled: !isVersioned,
					hidden: true,
				    handler: function(){
				    	Ext.MessageBox.progress("版本同步", "正在与版本库同步...");
						Ext.Ajax.request({
							method: 'POST', 
							url: 'version_ctrl.jsp',
							timeout: 180 * 1000,
							params: {operation:"synchronize"},
							callback: function(options, success, response) {
								if (success) {
									Ext.MessageBox.updateProgress(1, '100%', response.responseText);
									setTimeout(function(){Ext.MessageBox.hide();}, 5000);
								}
								else {
									_this.showException(response);
								}
							}
						});
				    }
				},{
					icon: '../images/tool16/update.gif',
					text: '更新',
					itemId: 'update',
					disabled: !isVersioned,
					handler: function(){
						Ext.getBody().mask('正在更新...', 'x-mask-loading');
						Ext.Ajax.request({
							method: 'POST', 
							url: 'version_ctrl.jsp', 
							timeout: 90 * 1000,
							params: {
								operation: "update", path: ''
							},
							callback: function(options, success, response) {
								if (success) {
									var result = Ext.decode(response.responseText);
									Ext.getBody().mask(result.message);
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
					disabled: !isVersioned,
					handler: function(){
						VersionDlg.getCommitDialog({
							path: '',
							callback: function(result) {
								alert('提交成功, 新版本号: '+result['head-rev']);
							}
						});
					}
				},{
	 				itemId: 'cleanup',
				    text: '清理',
				    handler: function(){
						Ext.Ajax.request({
							method: 'POST', 
							url: 'version_ctrl.jsp', 
							timeout: 90 * 1000,
							params: {operation:"cleanup", path: '' },
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
	 			}],
				listeners : {
     				beforeshow:function(){
     					this.items.get('import').setDisabled(isVersioned);
     					this.items.get('disconnect').setDisabled(!isVersioned);
						this.items.get('synchronize').setDisabled(!isVersioned);
						this.items.get('update').setDisabled(!isVersioned);
						this.items.get('commit').setDisabled(!isVersioned);
						this.items.get('cleanup').setDisabled(!isVersioned);
     				}
     			}
		});
		
		var ctxMenu = new Ext.menu.Menu({
			id: 'ctxmenu',
			enableScrolling: false,
		    items: [{
				text: '导入',
				handler: function(){return false;},
				menu: addMenu
			},
			{
				text: '导出',
				hidden: true,
				handler: function(){return false;}
			},
			{
				text: '创建',
				handler: function(){return false;},
				menu: createMenu
			},
			{
				itemId: 'install',
				text: '安装',
				handler: function(){
					var node = this.parentMenu.node;
					if (!node) return true;
					
					var selected = [];
					var a = node.attributes;
					if (node.isLeaf()) {
						var path = node.getPath('name').substring(2);
						if (path.indexOf('transport/')==0) {
							alert('传输端子不能单独安装');
							return;
						}
						
						if (confirm("确实要安装 " + a.name + " 吗？")) {
							selected.push(path);
						}
					}
					else {
						if (node.childNodes.length==0) {
							return;
						}
						if (confirm("确实要安装所有的 " + node.text + " 吗？")) {
							for (var child=node.firstChild; child!=null; child=child.nextSibling) {
								selected.push(child.getPath('name').substring(2));
							}
						}
					}
					
					if (selected.length <= 0) {
						return;
					}

					if (node.childNodes.length > 0) {
						Ext.each(node.childNodes, function(child){
							child.getUI().checkbox.src = child.getUI().LOAD_IMG_URL;
						});
					}
					else {
						node.getUI().checkbox.src = node.getUI().LOAD_IMG_URL;
					}
					Ext.Ajax.request({
						method: 'POST',
						url: 'install_ctrl.jsp', 
						params: {operation: (a.name=="transport" ? "installTransport": "install"), selected: Ext.encode(selected) },
						callback: function(options, success, response){
							if (success) {
								if (node.childNodes.length > 0) {
									Ext.each(node.childNodes, function(child){
										child.getUI().toggleCheck(true);
										child.getUI().checkbox.src = child.getUI().LAMP_IMG_URL;
									});
								}
								else {
									node.getUI().toggleCheck(true);
									node.getUI().checkbox.src = node.getUI().LAMP_IMG_URL;
								}
								Ext.MessageBox.show({
									title: '安装完毕',
									msg: '<div style="width:100%; height:auto; background-color:white; overflow:auto; padding:10px;">' +
										response.responseText +'</div>',
									buttons: Ext.MessageBox.OK,
									width: 492
								});
								
								setTimeout(function(){Ext.MessageBox.hide();}, 8*1000);
							}
							else {
								if (node.childNodes.length > 0) {
									Ext.each(node.childNodes, function(child){
										child.getUI().checkbox.src = child.getUI().checkbox.src=child.attributes.checked?
											child.getUI().LAMP_IMG_URL:
											Ext.BLANK_IMAGE_URL;
									});
								}
								else {
									node.getUI().checkbox.src = node.getUI().checkbox.src=node.attributes.checked?
										node.getUI().LAMP_IMG_URL :
										Ext.BLANK_IMAGE_URL;
								}
								_this.showException(response);
							}
						}
					});
				}
			},
			{
				itemId: 'uninstall',
				text: '卸载',
				handler: function(){
					var node = this.parentMenu.node;
					if (!node) return true;
					
					var selected = [];
					var a = node.attributes;
					if (node.isLeaf()) {
						var path = node.getPath('name').substring(2);
						if (path.indexOf('transport/')==0) {
							alert('传输端子不能单独卸载');
							return;
						}
						
						if (confirm("确实要安装 " + a.name + " 吗？")) {
							selected.push(path);
						}
					}
					else {
						if (node.childNodes.length==0) {
							return;
						}
						if (confirm("确实要卸载所有的 " + node.text + " 吗？")) {
							for (var child=node.firstChild; child!=null; child=child.nextSibling) {
								selected.push(child.getPath('name').substring(2));
							}
						}
					}
					
					if (selected.length <= 0) {
						return;
					}

					if (node.childNodes.length > 0) {
						Ext.each(node.childNodes, function(child){
							child.getUI().checkbox.src = child.getUI().LOAD_IMG_URL;
						});
					}
					else {
						node.getUI().checkbox.src = node.getUI().LOAD_IMG_URL;
					}
					Ext.Ajax.request({
						method: 'POST',
						url: 'install_ctrl.jsp', 
						params: {operation: (a.name=="transport" ? "uninstallTransport": "uninstall"), selected: Ext.encode(selected) },
						callback: function(options, success, response){
							if (success) {
								if (node.childNodes.length > 0) {
									Ext.each(node.childNodes, function(child){
										child.getUI().toggleCheck(false);
										child.getUI().checkbox.src = Ext.BLANK_IMAGE_URL;
									});
								}
								else {
									node.getUI().toggleCheck(false);
									node.getUI().checkbox.src = Ext.BLANK_IMAGE_URL;
								}
								Ext.MessageBox.show({
									title: '卸载',
									msg: '<div style="width:100%; height:auto; background-color:white; overflow:auto; padding:10px;">' +
										response.responseText +'</div>',
									buttons: Ext.MessageBox.OK,
									width: 492
								});
								
								setTimeout(function(){Ext.MessageBox.hide();}, 8*1000);
							}
							else {
								if (node.childNodes.length > 0) {
									Ext.each(node.childNodes, function(child){
										child.getUI().checkbox.src = child.getUI().checkbox.src=child.attributes.checked?
											child.getUI().LAMP_IMG_URL:
											Ext.BLANK_IMAGE_URL;
									});
								}
								else {
									node.getUI().checkbox.src = node.getUI().checkbox.src=node.attributes.checked?
										node.getUI().LAMP_IMG_URL :
										Ext.BLANK_IMAGE_URL;
								}
								_this.showException(response);
							}
						}
					});
				}
			},
			'-',
			{
				id: 'remove',
				text: '删除',
				icon:"../images/tool16/delete_edit.gif",
				handler: function(item, e){
					var node = this.parentMenu.node;
					if (!node) return true;
					
					var path = node.getPath('name').substring(2);
					if (node.isLeaf()) {
						var a = node.attributes;
						if (confirm("确实要删除 " + a.name + " 吗？")) {
							Ext.Ajax.request({
								method: 'POST', 
								url: 'ware_ctrl.jsp', 
								params: {operation: "remove", file: path },
								callback: function(options, success, response){
									if (success) {
										top.Application.closeWindow(path);
										node.remove();
									}
									else {
										_this.showException(response);
									}
								}
							});
						}
					}
					else {
						alert("不能删除所有的 " + node.text + "");
					}
				}
			},
			{
				id: 'rename',
				text: '重命名',
				icon:"../images/icons/rename.png",
				handler: function(item, e){
					var node = this.parentMenu.node;
					if (!node) return true;

					var a = node.attributes;
					var newName = prompt("重命名 ", a.text);
					if (newName === null) {
						return;
					}
					else if (newName == a.text) {
						return;
					}
					else 	if (!newName.match(/^[A-Za-z_]\w*$/)) {
						alert('名称必须以字母或下划线开头，并且不能包含只能包含字母和数字');
						return;
					}
	
					var path = node.getPath('name').substring(2);
					Ext.Ajax.request({
						method: 'POST', 
						url: 'ware_ctrl.jsp', 
						params: {operation: "rename", file: path, newName: newName },
						callback: function(options, success, response) {
							if (success) {
								top.Application.closeWindow(node.getPath('name').substring(2));
								var idx = a.name.lastIndexOf('.');
								var ext = idx>-1 ? a.name.substring(idx) : '';
								a.name = newName + ext;
								node.setText(newName);
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
				disabled: !isVersioned,
				handler: function(){
					var node = this.parentMenu.node;
					if (!node) {
						return true;
					}
					
					var a = node.attributes;
					Ext.getBody().mask('正在更新...', 'x-mask-loading');
					Ext.Ajax.request({
						method: 'POST',
						url: 'version_ctrl.jsp',
						timeout: 90 * 1000,
						params: {
							operation: "update",
							path: node.getPath('name').substring(2)
						},
						callback: function(options, success, response){
							if (success) {
								var result = Ext.decode(response.responseText);
								if (!node.isLeaf()) {
								}
								else if (result.revision != a.revision) {
									a.revision = result.revision;
									a.author = result.author;
									a.status = result.status;
									node.setText(_this.getNodeText(a));
								}
								Ext.getBody().mask(result.message);
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
				disabled: !isVersioned,
				handler: function(){
					var node = this.parentMenu.node;
					if (!node) return true;
					
					VersionDlg.getCommitDialog({
						path: node.getPath('name').substring(2),
						callback: function(result) {
							var a = node.attributes;
							if (node.isLeaf() && !a.revision) {
								a.revision = result.revision;
								a.author = result.author;
								a.url = result.url;
								node.setText(_this.getNodeText(a));
							}
							else {
								Ext.getBody().mask('提交成功, 新版本号: '+result['head-rev']);
								Ext.getBody().unmask.defer(3000, Ext.getBody());
								if (node.reload) {
									node.reload();
								}
							}
						}
					});
				}
			},{
				itemId: 'revert',
				icon: '../images/tool16/revert.gif',
				text: '还原',
				disabled: !isVersioned,
				handler: function(){
					var node = this.parentMenu.node;
					if (!node || !confirm('您所做的更改将会被丢弃，确实要还原到修改之前的版本吗？')) {
						return true;
					}
					Ext.getBody().mask('正在还原...', 'x-mask-loading');
					Ext.Ajax.request({
						method: 'POST', 
						url: 'version_ctrl.jsp', 
						timeout: 90 * 1000,
						params: {operation:"revert", path: node.getPath('name').substring(2) },
						callback: function(options, success, response) {
							if (success) {
								if (node.reload) node.reload();
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
			},
			'-',
			{
				itemId: 'info',
				text: '属性',
				handler: function(item, e){
					e.stopEvent();
					var node = this.parentMenu.node;
					if (!node) return true;

					Ext.Ajax.request({
						method: 'POST', 
						url: 'ware_ctrl.jsp', 
						params: {operation:"info", path: node.getPath('name').substring(2) },
						callback: function(options, success, response) {
							if (success) {
								Ext.Msg.show({
									title: '属性',
									msg: response.responseText,
									buttons: Ext.Msg.OK
								});
							} else {
								_this.showException(response);
							}
						}
					});
				}
			},{
				id: 'refresh',
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
			var pos = e.getXY();
            if(!ctxMenu.el){
                ctxMenu.render();
            }
			if ((pos[1] + ctxMenu.getHeight()) > Ext.getBody().getHeight(true)) {
				ctxMenu.defaultAlign = 'bl-tl?';
			}
			else {
				ctxMenu.defaultAlign = 'tl-bl?';
			}
			ctxMenu.node = node;
	        var a = node.attributes;
			var rename = ctxMenu.items.get('rename');
			rename.setDisabled(!node.isLeaf() || node.parentNode.attributes.name=='component');
			ctxMenu.items.get('remove').enable();
			ctxMenu.items.get('install').setDisabled(!(a.installable || node.parentNode.attributes.installable));
			ctxMenu.items.get('uninstall').setDisabled(!a.checked);
			ctxMenu.items.get('info').enable();
			ctxMenu.items.get('commit').show();
			ctxMenu.items.get('update').show();
			ctxMenu.items.get('refresh').setVisible(!node.isLeaf());
			
			ctxMenu.items.get('update').setDisabled(!isVersioned || node.isLeaf() && !a.author);
			ctxMenu.items.get('commit').setDisabled(!isVersioned);
			
			ctxMenu.items.get('install').setText(a.checked ? '重新安装' : '安装');
			ctxMenu.showAt(pos);
		});

		resourceTree.on('containercontextmenu', function(tree, e){
			var pos = e.getXY();
            if(!ctxMenu.el){
                ctxMenu.render();
            }
			if ((pos[1] + ctxMenu.getHeight()) > Ext.getBody().getHeight(true)) {
				ctxMenu.defaultAlign = 'bl-tl?';
			}
			else {
				ctxMenu.defaultAlign = 'tl-bl?';
			}
			ctxMenu.node = null;
			ctxMenu.items.get('info').disable();
			ctxMenu.items.get('rename').disable();
			ctxMenu.items.get('remove').disable();
			ctxMenu.items.get('install').disable();
			ctxMenu.items.get('commit').hide();
			ctxMenu.items.get('refresh').hide();
			ctxMenu.showAt(pos);
		});
		var toolbar = new Ext.Toolbar({style:'padding-top: 5px;' });
		toolbar.addButton(
 			[{
			    text: '导入',
				cls: 'x-btn-text-icon',
				icon: '../images/tool16/import_wiz.gif',
			    menu: addMenu
			},
 			{
			    text: '创建',
				cls: 'x-btn-text-icon',
				icon: '../images/tool16/create.gif',
			    menu: createMenu
			}
		]);
		toolbar.addSeparator();
		toolbar.addButton([
			{
			    text: '导出',
			    //tooltip: '导出组件包',
			    hidden: true,
				cls: 'x-btn-text',
			    menu : exportMenu
			},{
				text: '刷新',
				hidden: true,
				handler: function(){
					location.reload();
				}
			}
		]);
		toolbar.addFill();
		toolbar.addButton([{
			xtype: 'button',
		    text: '版本控制',
		    tooltip: '与版本库同步',
			cls: 'x-btn-text-icon',
			icon: '../images/elcl16/synced.gif',
		    menu: syncMenu
		}]);
		toolbar.render('hd');
	},
	
	createResourceNode : function(config){
		var node = new Ext.tree.AsyncTreeNode(Ext.apply(config, {
			uiProvider: Ext.ux.ResourceNodeUI,
			children: [],
			leaf: true
		}));
		return node
	},
	
	getNodeText: function(a) {
		var text = a.name + (a.revision > -1 ? '<br/><span style="color:olive; font-weight:normal;">版本: '+
			a.revision+', '+a.author+'</span>' : '');
		return text;
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
						}catch (e) {
							alert(e.message);
							return;
						}
						button.disable();
						
						Ext.Ajax.request({
							method: 'POST', 
							url: 'version_ctrl.jsp', 
							timeout: 90 * 1000,
							params: Ext.apply(params, {operation:"put"}),//, path: a.name }),
							callback: function(options, success, response) {
								button.enable();
								if (success) {
									isVersioned = true;
									dlg.hide();
								} else {
									_this.showException(response);
								}
							}
						});
					}
				},{
					text: '完成',
					hidden: true
				},{
					text: Ext.Msg.buttonText.cancel, 
					handler: function(){ dlg.hide(); }
				}]
			});
			
			dlg.render(Ext.getBody());
			this.reposDlg = dlg;
		}
		
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
				}else {
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
					fieldLabel: '密码',
					xtype: 'hidden'
				}],
				tools: [{
					id: 'down',
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
	
	showException : function(response){
		var statusText = response.responseText;
		if (!statusText){
			switch (response.status) 
			{
				case -1:
					alert('通讯超时，事务终止');  return;
				case 0:
					alert('通讯错误，连接服务器失败');   return;
				default:
					alert ('错误('+response.status+')');  return;
			}
			return;
		}

		Ext.MessageBox.show({
			title: '错误提示',
			msg: '<div style="width:100%; height:auto; background-color:white; overflow:auto;">'+statusText+'</div>',
			buttons: Ext.MessageBox.OK,
			width: 492
		});
		
	}


};

}();

Ext.onReady(Viewer.init, Viewer, true);

Ext.ux.ResourceGroupUI = Ext.extend(Ext.tree.TreeNodeUI, {
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';

        var cb = typeof a.checked == 'boolean';
        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node x-res-group"><div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<span class="x-tree-node-indent">',this.indentMarkup,"</span>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" />',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" style="display:none;" unselectable="on" />',
            cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on" style="color:#3764a0">',n.text,"</span></a></div>",
            '<ul class="x-tree-node-ct" style="display:none;"></ul><div style="clear:left"></div>',
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1];
        this.iconNode = cs[2];
        var index = 3;
        if(cb){
            this.checkbox = cs[3];
			// fix for IE6
			this.checkbox.defaultChecked = this.checkbox.checked;			
            index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
    }
});

Ext.ux.ResourceNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
	LAMP_IMG_URL: '../images/elcl16/smartmode_co.gif',
	LOAD_IMG_URL: '../ext-Ajax/resources/images/default/grid/loading.gif',
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';

        var cb = typeof a.checked == 'boolean';
		
        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node x-resource"><div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" style="height:100%;" unselectable="on">',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on" />',
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:none;" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; margin-left:-20px; margin-top:5px;" src="' + (a.checked ? this.LAMP_IMG_URL : Ext.BLANK_IMAGE_URL) + '" />') : '',
            '<div class="x-tree-node-label"><a hidefocus="on" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a>",
            '<p class="x-tree-node-indent">',a.desc,"</p></div></div>",
            '<ul class="x-tree-node-ct" style="display:none;"></ul>',
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.childNodes;
        this.iconNode = cs[0];
        this.ecNode = cs[1];
        var index = 2;
        if(cb){
            this.checkbox = cs[index];
			// fix for IE6
			this.checkbox.defaultChecked = this.checkbox.checked;			
            index++;
        }
        this.anchor = cs[index].firstChild;
        this.textNode = this.anchor.firstChild;
        this.indentNode = this.anchor.nextSibling;
		
		this.iconNode.title = a.desc;
    }
});
