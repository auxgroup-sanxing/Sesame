Ext.BLANK_IMAGE_URL='../images/s.gif';

// 给树节点添加mouseover事件
Ext.tree.TreeNodeUI.prototype.onOver = function(e){
        this.addClass('x-tree-node-over');
		if (this.node.isLeaf()) {
			this.fireEvent("mouseover", this.node, e);
		}
};

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
	{name:'server', title:'代理服务单元', qtip:'对外发布', icon:'net-service.gif', filter: '^(.*)\\.(zip)$', path:'../explorer/unit_viewer.jsp?script=unit_v_binding.js&unit=', cls:'active'}, 
	{name:'engine', title:'引擎服务单元', qtip:'系统内部提供', icon:'engine-su.gif', filter: '^(.*)\\.(zip)$', path:'../explorer/unit_viewer.jsp?script=unit_v_engine.js&unit='}, 
   	{name:'client', title:'远程服务单元', qtip:'外部主机提供', icon:'switch.gif', filter: '^(.*)\\.(zip)$', path:'../explorer/unit_viewer.jsp?script=unit_v_binding.js&unit='}, 
	{name:'intf', title:'公共接口', icon:'intf.png', path: '../explorer/intf_viewer.jsp?intf='},
	{name:'schema', title:'数据模型', icon:'application_xsd.gif', path:'../schema/schema_viewer.jsp?schema='}, 
	{name:'team', title:'项目成员', icon:'user.gif', filter: '^[^default]$', path:''}, 
	{name:'tests', title:'测试', icon:'feeds.gif', path:'../explorer/test_viewer.jsp?unit='}, 
	{name:'docs', title:'项目文档', icon:'docs.jpg', path:''}, 
	{name:'conf', title:'项目配置与管理', icon:'', path:''}];

return {
	init : function(){
		Ext.Ajax.defaultHeaders = {
			"Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8",
			"region" : 'workspace'
		};
		Ext.QuickTips.init();
		
		var _this = this;
	
		var menu = Ext.get('sample-menu-inner');
		var ct = Ext.get('sample-box-inner');
		
		var rootNode = new Ext.tree.AsyncTreeNode({
		    id: 'ROOT',
		    name: project,
			children:[]
		});
		
		resourceTree = new Ext.tree.TreePanel({
			border: false,
			id: 'resource-ct',
			rootVisible: false,
			lines: false,
			loader: new Ext.tree.TreeLoader({
				requestMethod: 'GET', 
				dataUrl: 'project_ctrl.jsp', 
				baseAttrs: {},
				baseParams: {operation:'getResources', project:project},
				uiProviders: {'resource':Ext.ux.ResourceNodeUI},
				listeners: {
					"beforeload": function(treeLoader, node) {
						var a = node.attributes;
						this.baseParams.group = a.name;
						this.baseAttrs.icon = 'project_viewer/'+a.icon;
						if (a.filter)
							this.baseParams.filter = a.filter;
						else
							delete this.baseParams.filter;
					},
					'load' : function(treeLoader, node, response) {
						var rs = Ext.decode(response.responseText);
						if (rs.length > 0)
							rs.sort(function(var1, var2) {
								return var1['file'] > var2['file']? 1:var1['file'] == var2['file'] ? 0 : -1;
							});
					},
					'loadexception': function(loader, node, response){ _this.showException(response); }
				}
			}),
			listeners: {
				beforeappend: function(tree, parent, node){
					if (parent != tree.getRootNode()) {
						var a = node.attributes;
						a.checkIcon = _this.getOverlayIcon(a.status);
						node.setText(_this.getNodeText(a));
					}
				},
				click : function(node, e){
					if (!node.isLeaf()) {
						node.expand();
					} 
					else {
						var a = node.attributes;
			            var viewer = node.parentNode.attributes.viewer;
						var file = node.getPath('name');
						var currentWin = top.Application.findWindow(file);
						if (!!currentWin) {
							currentWin.show();
						}
					}
				},
				dblclick : function(node, e){
			        if(node.isLeaf()){
						var a = node.attributes;
			            var viewer = node.parentNode.attributes.viewer;
			            var file = node.getPath('name').substring(1);
						var title = a.desc ? a.desc + '['+ a.name +']' : a.name;
						top.Application.createWindow(file, title, viewer + file + '&unitId=' + a.name + '&unitDesc=' + escape(encodeURIComponent(a.desc)) + '&projectDesc=' + escape(encodeURIComponent(projectDesc)));
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
				viewer: c.path
			});
			rootNode.appendChild(cNode);
	    }
		
		var tpl2 = new Ext.XTemplate(
	        '<tpl for="."><a href="#{id}" hidefocus="on" class="{cls}" id="a4{id}"><img src="project_viewer/s.gif" class="{iconCls}">{title}</a></tpl>'
	    );
	    tpl2.overwrite(menu, catalog);
		
		function calcScrollPosition(){
			var found = false, last;
			
			rootNode. eachChild(function(node){
				var el = Ext.fly(node.ui.elNode);
				last = node;
				if(el.getOffsetsTo(ct)[1] > -5){
					activate(node.id)
					found = true;
					return false;
				}
			});
			if(!found){
				activate(last.id);
			}
		}
		
		var bound;
		function bindScroll(){
			ct.on('scroll', calcScrollPosition, ct, {buffer:250});
			bound = true;
		}
		function unbindScroll(){
			ct.un('scroll', calcScrollPosition, ct);
			bound = false;
		}
		function activate(id){
			Ext.get('a4' + id).radioClass('active');
		}
		
		menu.on('click', function(e, t){
			e.stopEvent();
			if((t = e.getTarget('a', 2)) && bound){
				var id = t.href.split('#')[1];
				var node = rootNode.findChild("id", id);
				node.expand(false, false);
				var top = node.ui.elNode.offsetTop;
				Ext.get(t).radioClass('active');
				unbindScroll();
				ct.scrollTo('top', top, {callback:bindScroll});
			}
		});
		
		Ext.get('samples-cb').on('click', function(e){
			var img = e.getTarget('img', 2);
			if(img){
				Ext.getDom('samples').className = img.className;
				calcScrollPosition.defer(10);
			}
		});
		
		bindScroll();
		
		var addMenu = new Ext.menu.Menu({
			id: 'addMenu',
			items: [{
				catalog: 'server',
				text: '代理服务',
				icon:"../images/icons/computer_add.png",
				handler: function(){
				 	var item = this;
					var cNode = rootNode.findChild("name", this.catalog);
					if (!cNode) return;
				 	var a = cNode.attributes;
					var callback = function(values){
						cNode.expand(false, false, function(){
							var node = cNode.findChild("name", values['unit-name']);
							if (!node) {
								node = _this.createResourceNode({
									icon: a.icon,
									text: values['unit-name'],
									name: values['unit-name'],
									desc: values['desc']
								});
								cNode.appendChild(node);
							}
							node.select();
						});
					};
					Dialog.getUnitDialog({title:'添加代理服务单元', params: {type:item.catalog}, callback:callback}).show();
				}
			},
			{
				catalog: 'engine',
				text: '本地服务',
				icon:"../images/obj16/elements_obj.gif",
				handler: function(){
				 	var item = this;
					var cNode = rootNode.findChild("name", this.catalog);
					if (!cNode) return;
				 	var a = cNode.attributes;
					var callback = function(values){
						cNode.expand(false, false, function(){
							var node = cNode.findChild("name", values['unit-name']);
							if (!node) {
								node = _this.createResourceNode({
									icon: a.icon,
									text: values['unit-name'],
									name: values['unit-name'],
									desc: values['desc']
								});
								cNode.appendChild(node);
							}
							node.select();
						});
					};
					Dialog.getUnitDialog({title:'添加本地服务单元', params: {type:item.catalog}, callback:callback}).show();
				}
			},
			{
				catalog: 'client',
				text: '远程服务',
				icon:"../images/elcl16/external_browser.gif",
				handler: function(){
				 	var item = this;
					var cNode = rootNode.findChild("name", this.catalog);
					if (!cNode) return;
				 	var a = cNode.attributes;
					var callback = function(values){
						cNode.expand(false, false, function(){
							var node = cNode.findChild("name", values['unit-name']);
							if (!node) {
								node = _this.createResourceNode({
									icon: a.icon,
									text: values['unit-name'],
									name: values['unit-name'],
									desc: values['desc']
								});
								cNode.appendChild(node);
							}
							node.select();
						});
					};
					Dialog.getUnitDialog({title:'添加远程服务单元', params: {type:item.catalog}, callback:callback}).show();
				}
			},
			'-',
			{
				catalog: 'docs',
				text: '文档',
				handler: function(){
				 	var item = this;
					var cNode = rootNode.findChild("name", this.catalog);
					if (!cNode) return;
				 	var a = cNode.attributes;
					var callback = function(values){
						cNode.expand(false, false, function(){
							var file = project + '/' + item.catalog + '/' + values['file'];//TODO:file
							var node = cNode.findChild("file", file);
							if (!node) {
								node = _this.createResourceNode({
									icon: a.icon,
									text: values['file'],
									path: a.path,
									file: file,
									desc: values['desc']
								});
								cNode.appendChild(node);
							}
							node.select();
						});
					};
					Dialog.getPluginDialog({title:'添加文档', params: {component:item.catalog}, callback:callback}).show();
				}
			}]
		});

		var createMenu = new Ext.menu.Menu({
			id: 'createMenu',
			items: [
				{
					catalog: 'server',
					text: '代理服务',
					icon:"../images/icons/computer_add.png",
					handler: function(){
					 	var item = this;
						var cNode = rootNode.findChild("name", this.catalog);
						if (!cNode) return;
					 	var a = cNode.attributes;
						var callback = function(values){
							cNode.expand(false, false, function(){
								var node = cNode.findChild("name", values['unit-name']);
								if (!node) {
									node = _this.createResourceNode({
										icon: a.icon,
										text: values['unit-name'],
										name: values['unit-name'],
										desc: values['desc']
									});
									cNode.appendChild(node);
								}
								node.select();
							});
						};
						Dialog.getServerDialog({callback:callback}).show();
					}
				},
				{
					catalog: 'engine',
					text: '本地服务',
					icon:"../images/obj16/elements_obj.gif",
					handler: function(){
					 	var item = this;
						var cNode = rootNode.findChild("name", this.catalog);
						if (!cNode) return;
					 	var a = cNode.attributes;
						var callback = function(values){
							cNode.expand(false, false, function(){
								var node = cNode.findChild("name", values['unit-name']);
								if (!node) {
									node = _this.createResourceNode({
										icon: a.icon,
										text: values['unit-name'],
										name: values['unit-name'],
										desc: values['desc']
									});
									cNode.appendChild(node);
								}
								node.select();
							});
						};
						Dialog.getLocalDialog({callback:callback}).show();
					}
				},
				{
					catalog: 'client',
					text: '远程服务',
					icon:"../images/elcl16/external_browser.gif",
					handler: function(){
					 	var item = this;
						var cNode = rootNode.findChild("name", this.catalog);
						if (!cNode) return;
					 	var a = cNode.attributes;
						var callback = function(values){
							cNode.expand(false, false, function(){
								var node = cNode.findChild("name", values['unit-name']);
								if (!node) {
									node = _this.createResourceNode({
										icon: a.icon,
										text: values['unit-name'],
										name: values['unit-name'],
										desc: values['desc']
									});
									cNode.appendChild(node);
								}
								node.select();
							});
						};
						Dialog.getClientDialog({callback:callback}).show();
					}
				},
				'-',
				{
					catalog: 'intf',
					text: '公共接口',
					icon: '../images/obj16/interface.gif',
					handler: function() {
						var item = this;
						var cNode = rootNode.findChild("name", this.catalog);
						if (!cNode) return;
					 	var a = cNode.attributes;
						var callback = function(values){
							cNode.expand(false, false, function(){
								var node = cNode.findChild("name", values['intf-name']);
								if (!node) {
									node = _this.createResourceNode({
										icon: a.icon,
										text: values['intf-name'],
										path: a.path,
										name: values['intf-name'],
										desc: values['desc']
									});
									cNode.appendChild(node);
								}
								node.select();
							});
						};
						Dialog.getIntfDialog({callback:callback}).show();
					}	
				},
				'-',
				{
					catalog: 'schema',
					text: '数据模型',
					icon:"../images/obj16/schema_file.gif",
					handler: function(){
					 	var item = this;
						var cNode = rootNode.findChild("name", this.catalog);
						if (!cNode) return;
					 	var a = cNode.attributes;
						var callback = function(values){
							cNode.expand(false, false, function(){
								var node = cNode.findChild("name", values['schema-name'] + '.xsd');
								if (!node) {
									node = _this.createResourceNode({
										icon: a.icon,
										text: values['schema-name'],
										path: a.path,
										name: values['schema-name'] + '.xsd',
										desc: values['desc']
									});
									cNode.appendChild(node);
								}
								node.select();
							});
						};
						Dialog.getSchemaDialog({callback:callback}).show();
					}
				},
				'-',
				{
					catalog: 'tests',
					text: '测试单元',
					handler: function(){
					 	var item = this;
						var cNode = rootNode.findChild("name", this.catalog);
						if (!cNode) return;
					 	var a = cNode.attributes;
						var callback = function(values){
							cNode.expand(false, false, function(){
								var file = project + '/' + item.catalog + '/' + values['name'];
								var node = cNode.findChild("name", values['name']);
								if (!node) {
									node = _this.createResourceNode({
										icon: a.icon,
										text: values['name'],
										path: a.path,
										name: values['name'],
										desc: values['desc']
									});
									cNode.appendChild(node);
								}
								node.select();
							});
						};
						_this.getTestDialog({callback:callback}).show();
					}
			}]
		});
		
		var exportMenu = new Ext.menu.Menu({
			id: 'exportMenu',
			items: [
				{
					text: '项目',
					handler: function(){
					}
				},
				{
					text: '服务单元',
					handler: function(){
					}
				}
			]
		});
		
		var syncMenu = new Ext.menu.Menu({
			id: 'syncMenu',
			items: [{
				itemId: 'update',
				icon: '../images/tool16/update.gif',
				text: '更新',
				handler: function(){
					Ext.getBody().mask('正在更新...', 'x-mask-loading');
					Ext.Ajax.request({
						method: 'POST', 
						url: 'version_ctrl.jsp', 
						timeout: 90 * 1000,
						params: {operation:"update", path: project },
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
				text: '提交',
				icon: '../images/tool16/commit.gif',
				handler: function(){
					VersionDlg.getCommitDialog({
						path: project,
						callback: function(result) {
							alert('提交成功, 新版本号: '+result['head-rev']);
						}
					});
				}
			},{
				itemId: 'revert',
				icon: '../images/tool16/revert.gif',
				text: '还原',
				handler: function(){
					if (!confirm('您所做的所有更改将全部被丢弃，确实要执行还原吗？')) {
						return true;
					}
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
 				itemId: 'cleanup',
			    text: '清理',
			    handler: function(){
					Ext.Ajax.request({
						method: 'POST', 
						url: 'version_ctrl.jsp', 
						timeout: 90 * 1000,
						params: {operation:"cleanup", path: project },
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
				text: '创建',
				handler: function(){return false;},
				menu: createMenu
			},
			'-',
			{
				id: 'remove',
				text: '删除',
				icon:"../images/tool16/delete_edit.gif",
				handler: function(item, e){
					var node = this.parentMenu.node;
					if (!node) return true;
					
					var a = node.attributes
					if (node.isLeaf()) {
						if (a.icon.indexOf('intf') != -1) {
							alert('注意: 删除公共接口后,已经引入该接口的服务单元不会被修改!');
						}
						if (confirm("确实要删除 " + a.name + " 吗？")) {
							Ext.Ajax.request({
								method: 'POST', 
								url: 'project_ctrl.jsp', 
								params: {operation: "remove", file: node.getPath('name').substring(1) },
								callback: function(options, success, response){
									if (success) {
										top.Application.closeWindow(a.file);
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
						alert("不能删除所有的 " + node.text +"");
					}
				}
			},
			{
				id: 'rename',
				text: '重命名',
				icon:"../images/icons/rename.png",
				handler: function(item, e){
					e.stopEvent();
					var node = this.parentMenu.node;
					if (!node) return true;

					var a = node.attributes;
					if (a.icon.indexOf('intf') != -1) {
						alert('注意: 修改该公共接口后,已经引入该接口的服务单元不会被修改!');
					}
					var pathname = node.getPath('name').substring(1);
					var newName = prompt("重命名 ", a.name);
					if (newName === null) {
						return;
					}
					else if (newName == a.text) {
						return;
					}
					else 	if (!newName.match(/^[A-Za-z_]\w*$/)) {
						alert('名称必须以字母或下划线开头，并且不能包含只能包含字母和数字');
						return false;
					}
	
					Ext.Ajax.request({
						method: 'POST', 
						url: 'project_ctrl.jsp', 
						params: {operation:"rename", file: pathname, newName: newName },
						callback: function(options, success, response) {
							if (success) {
								top.Application.closeWindow(pathname);
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
					if (!node) return true;
					
					var a = node.attributes;
					if (node.childNodes.length > 0) {
						Ext.getBody().mask('正在更新...', 'x-mask-loading');
					}
					else {
						node.getUI().checkbox.src = node.getUI().LOAD_IMG_URL;
					}
					Ext.Ajax.request({
						method: 'POST', 
						url: 'version_ctrl.jsp', 
						timeout: 90 * 1000,
						params: {operation:"update", path: node.getPath('name').substring(1) },
						callback: function(options, success, response) {
							if (success) {
								var result = Ext.decode(response.responseText);
								if (!node.isLeaf()) {
									
								}
								else if (result.revision != a.revision) {
									a.revision = result.revision;
									a.status = result.status;
									a.author = result.author;
									a.checkIcon = _this.getOverlayIcon(a.status);
									node.getUI().toggleCheck(a.status!=null);
									node.getUI().checkbox.src = a.checkIcon;
									
									node.setText(_this.getNodeText());
								}
								else {
									node.getUI().checkbox.src = a.checkIcon;
								}
								Ext.getBody().mask(result.message);
								Ext.getBody().unmask.defer(3000, Ext.getBody());
							}
							else {
								node.getUI().checkbox.src = a.checkIcon;
								Ext.getBody().unmask();
								_this.showException(response);
							}
						}
					});
				}
			},{
				itemId: 'commit',
				text: '提交',
				icon:"../images/tool16/commit.gif",
				disabled: !isVersioned,
				handler: function(){
					var node = this.parentMenu.node;
					if (!node) return true;
					
					VersionDlg.getCommitDialog({
						path: node.getPath('name').substring(1),
						callback: function(result) {
							if (node.isLeaf()) {
								var a = node.attributes;
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
				handler: function(item, e){
					if (!confirm('您所做的所有更改将被丢弃，确实要执行还原吗？')) {
						return true;
					}
					Ext.getBody().mask('正在还原...', 'x-mask-loading');
					Ext.Ajax.request({
						method: 'POST', 
						url: 'version_ctrl.jsp', 
						timeout: 90 * 1000,
						params: {operation:"revert", path: item.parentMenu.node.getPath('name').substring(1) },
						callback: function(options, success, response) {
							if (success) {
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

					var a = node.attributes;
	
					Ext.Ajax.request({
						method: 'POST', 
						url: 'project_ctrl.jsp', 
						params: {
							operation: "info",
							path: node.getPath('name').substring(1),
							project: project
						},
						callback: function(options, success, response) {
							if (success) {
								var props = response.responseText;
								Ext.Msg.show({
									title: '属性',
									msg: props,
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
			{
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
			ctxMenu.items.get('info').enable();
			ctxMenu.items.get('commit').show();
			ctxMenu.items.get('revert').show();
			ctxMenu.items.get('update').show();
			ctxMenu.items.get('refresh').setVisible(!node.isLeaf());
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
			ctxMenu.items.get('commit').hide();
			ctxMenu.items.get('update').hide();
			ctxMenu.items.get('revert').hide();
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
			    text: '安装共享库',
				cls: 'x-btn-text',
				hidden: true,
			    handler: function(){
					Dialog.getInstallDialog({title:'安装共享库', load: 'getLibs', submit: 'installLibs'}).show();
			    }
			},{
			    text: '部署服务',
			    tooltip: '打包项目并部署到运行环境',
				cls: 'x-btn-text',
			    handler: function(){
				 	var items = [];
					items.push(rootNode.findChild("name", "client"));
					items.push(rootNode.findChild("name", "server"));
					items.push(rootNode.findChild("name", "engine"));
					Dialog.getDeployDialog({title:'项目部署', items: items, params: {} }).show();
			    }
			}
		]);
		toolbar.addSeparator();
		toolbar.addButton([
			{
			    text: '导出',
			    //tooltip: '导出部署包',
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
			//xtype: 'splitbutton',
		    text: '版本同步',
		    tooltip: '与版本库同步',
			cls: 'x-btn-text-icon',
			icon: '../images/elcl16/synced.gif',
			disabled: !isVersioned,
		    handler: function(){
		    	//Ext.getBody().mask("正在与版本库同步...", 'x-mask-loading');
		    	return;
		    	Ext.MessageBox.progress("版本同步", "正在与版本库同步...");
				Ext.Ajax.request({
					method: 'POST', 
					url: 'version_ctrl.jsp', 
					timeout: 180 * 1000,
					params: {operation:"synchronize", path: project },
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
		    },
		    menu: syncMenu
		}]);
		toolbar.render('hd');
	},
	
	createResourceNode : function(config){
		var node = new Ext.tree.AsyncTreeNode(Ext.apply(config, {
			icon: 'project_viewer/'+config.icon,
			uiProvider: Ext.ux.ResourceNodeUI,
			children: [],
			leaf: true
		}));
		return node
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

	getTestDialog : function(options){
		var _this = this;
		
		if (!this.testDlg) {
		    var dlg = new Ext.Window({
				title: '创建仿真单元',
		        autoCreate: true,
		        resizable:false, constrain:true, constrainHeader:true,
		        minimizable:false, maximizable:false,
		        stateful:false,
		        modal:true,
		        buttonAlign: "right",
				defaultButton: 0,
		        width: 450, height: 225,	 minWidth: 300,
		        footer: true,
		        closable: true, closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: [{
					xtype: 'form',
					id: 'test-form',
					autoScroll: true,
					labelWidth: 80,
					border: false,
					bodyStyle: 'padding:10px;',
					defaultType: 'textfield',
					defaults: { anchor: '-18' },
					msgTarget: 'title',
					items: [
						{
							xtype: 'radiogroup',
							fieldLabel: '仿真类型',
							items: [
							    {boxLabel: '客户端', name: 'oriented', inputValue: 'server', checked: true},
							    {boxLabel: '服务器', name: 'oriented', inputValue: 'client', 
							    	listeners: {
								    	check: function(sender, checked){
							    			var form = Ext.getCmp('test-form').getForm();
								    		var field=form.findField('endpoint');
											field.clearValue();
											field.store.baseParams['oriented'] = checked ? 'client' : 'server';
											field.store.reload();
										}
									}
							    }
							],
					    	listeners: {
								change: function(){	alert('change'); }
							}
						},{
							name: 'name',
							allowBlank: false,
							fieldLabel: '名称',
							regex: Identifier.regex,
							regexText: Identifier.regexText
					    },{
							name: 'desc',
							fieldLabel: '描述'
					    },{
							name: 'endpoint',
							hiddenName: 'endpoint',
							xtype: 'combo',
							fieldLabel: '服务单元',
							allowBlank: false,
							forceSelection: true,
							store: _this.getUnitStore(),
							triggerAction: 'all', editable: false,
							valueField: 'name', displayField: 'label',
							listeners: {
							}
					    },{
							name: 'emulator',
							hiddenName: 'emulator',
							xtype: 'combo',
							fieldLabel: '仿真插件',
							allowBlank: false,
							forceSelection: true,
							store: _this.getEmulatorStore(),
							triggerAction: 'all', editable: false,
							valueField: 'name', displayField: 'label',
							listeners: {
							}
					    }]
					}
				]
		    });
			
		    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:false}, function(){ 
				var formPanel = dlg.getComponent(0);
				var form = formPanel.getForm();

				form.submit({url: 'project_ctrl.jsp', clientValidation: true, 
					params: {operation:'createTestUnit', project:project},
					success: function(){ if (dlg.options.callback) dlg.options.callback(form.getValues()); dlg.hide(); },
					failure: function(form, action){ if (action.response) _this.showException(action.response); }
				});
			});
		    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){ dlg.hide(); });
		    
			dlg.render(Ext.getBody());
		    this.testDlg = dlg;
		}
		else {
			var formPanel = this.testDlg.getComponent(0);
			var form = formPanel.getForm();
			var field = form.findField('endpoint')
			field.store.baseParams['oriented'] = 'server';
			field.store.reload();
			form.reset();
		}
		this.testDlg.options = options;
        return this.testDlg;
	},
	
	getEmulatorStore : function(){
		var _this = this;
		var store = new Ext.data.JsonStore({
			url: 'project_ctrl.jsp',
			baseParams: {	operation:'loadEmulators', project: project},
			root: 'items',
			fields: [
				{name:'label', type:'string'},
				{name:'name', type:'string'}
			],
			listeners: {
				'loadexception': function(proxy, obj, response){ _this.showException(response); }
			}
		});
		return store;
	},
	
	getNodeText: function(a) {
		var text = a.name + (a.revision > -1 ? '<br/><span style="color:olive; font-weight:normal;">版本: '+
			a.revision+', '+a.author+'</span>' : '');
		return text;
	},
	
	getOverlayIcon: function(status) {
		switch(status) {
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
	
	getUnitStore : function(){
		var _this = this;
		var unitStore = new Ext.data.JsonStore({
			url: 'project_ctrl.jsp',
			baseParams: {	operation:'loadUnits', oriented:'server', project: project},
			root: 'items',
			fields: [
				{name:'label', type:'string'},
				{name:'name', type:'string'}
			],
			listeners: {
				'loadexception': function(proxy, obj, response){ _this.showException(response); }
			}
		});
		return unitStore;
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
	REPO_IMG_URL: '../images/obj16/repository_rep.gif',
	LOAD_IMG_URL: '../ext-Ajax/resources/images/default/grid/loading.gif',
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node x-resource"><div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" style="height:100%;" unselectable="on">',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on" />',
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:none;" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; margin-left:-20px; margin-top:5px;" src="' + (a.checked ? a.checkIcon : Ext.BLANK_IMAGE_URL) + '" />') : '',
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
