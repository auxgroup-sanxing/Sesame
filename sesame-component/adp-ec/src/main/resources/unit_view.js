String.prototype.trim = function() {return this.replace(/^\s+|\s+$/g,"");}

var UnitAdp = function() {
	UnitAdp.superclass.constructor.call(this, {});
};

Ext.extend(UnitAdp, UnitEngine, {
	init: function(){
		var _this = this;
		this.preparation();
		
		// "参数设置" 面板
		var leftTab = new Ext.form.FormPanel(_this.leftTabCfg);
		
		// "操作列表"(绑定设置) 面板
		var rightTab = new Ext.Panel(_this.rightTabCfg);
		
		// "创建工程"面板
		var fileTree = new Ext.tree.TreePanel({
            id: 'fileTree',
            split: true,
            region: 'west',
            title: '工程目录',
            xtype: 'treepanel',
            width: 240,
            autoScroll: true,
            split: true,
            border: false,
            loader: new Ext.tree.TreeLoader({
                dataUrl: _this.url,
                baseParams: {
                    operation: 'getProjectTree',
                    unit: unit
                },
                listeners: {
					beforeload: function(treeLoader, node) {
						var a = node.attributes;
						this.baseParams.path = node.getPath('name');
					},
					load: function(treeLoader, node) {
						var treeRoot = Ext.getCmp('fileTree').getRootNode();
						if (!!treeRoot) {
							if(treeRoot.childNodes.length > 0){
								treeRoot.setText('Adp_' + unit.replace(/.*\//ig, ''));
							} else {
								treeRoot.setText('');
							}
						}
					},
                    loadexception: function(loader, node, response){
						_this.showException(response);
                    }
                }
            }),
            root: new Ext.tree.AsyncTreeNode({
                id: 'ROOT',
                expanded: true
            }),
			tbar:[
				{
					id: 'create_icon',
		            text:'创建工程',
					cls: 'x-btn-text-icon',
					icon: '../images/icons/folder_add.png',
					handler: function() {
						this.createProject();
					},
					scope: this
		        },{
					id: 'report_icon',
		            text:'生成报告',
					cls: 'x-btn-text-icon',
					icon: '../images/icons/docs.gif',
					handler: function() {
						this.createReport();
					},
					scope: this
		        }, {
					id: 'svn_icon',
					text: 'SVN路径',
					cls: 'x-btn-text-icon',
					icon: '../images/tool16/svn_persp.gif',
					handler: function() {
						this.getSvnProp();
					},
					scope: this
				}
			],
            tools: [{
                id: 'refresh',
                qtip: '刷新',
                handler: function(e, toolEl, panel, tc){
                    var treeRoot = panel.getRootNode()
					treeRoot.reload(function(root) {
						if (!!root && root.childNodes.length > 0) {
							Ext.getCmp('fileTree').getTopToolbar().findById('report_icon').enable()
							if (isVersioned)
								Ext.getCmp('fileTree').getTopToolbar().findById('svn_icon').enable();
						} else {
							Ext.getCmp('fileTree').getTopToolbar().findById('report_icon').disable();
							Ext.getCmp('fileTree').getTopToolbar().findById('svn_icon').disable();
						}
					});
                }
            }],
            listeners: {
                click: function(node){
					if (!node.isLeaf()) {
						node.expand();
					// 展示文件
					} else {
						var path = node.getPath('name');
						Ext.Ajax.request({
							method: 'POST',
							url: _this.url,
							params: {
								operation: 'showFile',
								unit : unit,
								path : path
							},
							callback: function(options, success, response){
								if (success) {
									Ext.get('codeViewer').dom.src = "../explorer/adp_src_parent.jsp?srcPath=" + response.responseText;
								} else {
									_this.showException(response);
								}
							}
						});
					}	
				}
            }
        });
		
		var createProjectTab = new Ext.Panel({
			id: 'intf-form',
			layout: 'border',
			title: '工程展示',
			animCollapse: false,
			autoScroll: true,
			border: false,
			collapsible: false,
			labelWidth: 140,
			height: 220,
			bodyStyle: 'padding:5px 5px 0; text-align:left;',
			defaults: {anchor: "-18",stateful: false},
			items: [fileTree, {
				id: 'fileContentPanel',
				region: 'center',
				autoScroll: true,
				xtype: 'panel',
				header: false,
				border: false,
				deferredRender: false,
				items: {
					bodyCfg: {
						id: 'codeViewer',
						tag: 'iframe',
						frameborder: 'no',
						style: 'border:none;',
						height: '100%',
						width: '100%',
						src: ''
					}
				}
			}],
			listeners: {
				activate: function(){
					Ext.fly('tip').setStyle('display', 'none');
					var treeRoot = Ext.getCmp('fileTree').getRootNode();
					if (!!treeRoot) {
						if (!Ext.getCmp('fileTree').collapsed) {
							treeRoot.reload(function(root) {
								if (root.childNodes.length > 0) {
									Ext.getCmp('fileTree').getTopToolbar().findById('report_icon').enable()
									if (isVersioned)
										Ext.getCmp('fileTree').getTopToolbar().findById('svn_icon').enable();
								} else {
									Ext.Msg.alert('提示:', '目前没有已经创建的工程');
									Ext.getCmp('fileTree').getTopToolbar().findById('report_icon').disable();
									Ext.getCmp('fileTree').getTopToolbar().findById('svn_icon').disable();
								}
							});
						}
						_this.codeTransfer();
					} else {
						Ext.Msg.alert('提示:', '目前没有已经创建的工程');
						Ext.getCmp('fileTree').getTopToolbar().findById('report_icon').disable();
						Ext.getCmp('fileTree').getTopToolbar().findById('svn_icon').disable();
					}
				}
			}
		});
		
		// "同步实现"
		var syncImplTab = new Ext.form.FormPanel({
			id: 'impl-form',
			title: '同步实现',
			hidden: true,
			disabled: true,
			animCollapse: false,
			autoScroll: true,
			border: false,
			collapsible: false,
			labelWidth: 140,
			height: 220,
			bodyStyle: 'padding:5px 5px 0; text-align:left;',
			defaults: {anchor: "-18",stateful: false},
			url: this.url
		});
		
		// 主面板
		var tabPanel = new Ext.ux.InlineToolbarTabPanel({
			id:'mainPanel',
			activeTab: active,	// tabpanel的显示顺序 ,
			headerToolbar: true,
			toolbar: {items: [
				'->', 
				{
					width:30,
					text: (unitLocked == 'false')?'锁定': '解锁',
					id:'lock',
					disabled:(isVersioned == 'true')? false:true,
					icon: (unitLocked == 'false')? ImgPath.unlock : ImgPath.lock,
					cls: 'x-btn-text-icon',
					handler:function(){
						_this.unitLock(this);
					}
				},'-',{
					width:30,
					text: '部署',
					icon: '../images/tool16/webgroup_deploy.gif',
					cls: 'x-btn-text-icon',
					handler:function(){
						_this.unitDeploy(this);
					}
				},'-',{
					width:40,
					text: '返回',
					icon: '../images/tool16/undo_edit.gif',
					cls: 'x-btn-text-icon',
					handler: function() {
						_this.openProject();
					}
				}]
			},
			style : 'border-right:0px;',
			items: [leftTab, rightTab, {
						title: '业务常量',
						border: false, autoShow: true,
						xtype: 'panel',
						bodyCfg: {
					        tag: 'iframe',
					        style: 'overflow:auto; display:block;',
					        frameBorder: 0,
					        src: "../console/busconstant_viewer.jsp?unit=" + unitId
						}
					},createProjectTab, syncImplTab]
		});
		
		var viewport = new Ext.Viewport({
			stateId: 'unit_vi',
			stateful: false,
			layout: 'fit',
			items: tabPanel
		});
	},
	
	openProject: function() {
		var currentWinId = Application.getDesktop().getActiveWindow().id;
		Application.closeWindow(currentWinId);
		var name = unit.replace(/\/.*/ig, '');
		var desktop = Application.getDesktop();
		var win = desktop.getWindow('project_' + name);
		if (!win)
			win = desktop.createWindow({
                id: 'project_'+ name,
                title: projectDesc,
                width: 950,
				height: 500,
				iconCls: 'bogus',
				shim:false,
                animCollapse:false,
                constrainHeader:true,
                resizable: false,
                maximizable: false,
				listeners: {
                	render: function(sender){
						this.body.dom.contentWindow.onfocus = function(){ sender.toFront()};
						this.body.setVisible(true);
                	}
            	},
				bodyCfg: {
			        tag: 'iframe',
			        style: 'overflow:auto; display:none;',
			        frameBorder: 0,
			        src: '../explorer/project_viewer.jsp?project='+ name
				}
            });
		win.show();
	},
	
	// 创建工程
	createProject: function() {
		var _this = this;
		Ext.getBody().mask('正在创建工程...', 'x-mask-loading');
		
		Ext.Ajax.request({
			method: 'POST', 
			url: _this.url,
			params: {
				operation: 'createProject',
				component: component,
				unit : unit
			},
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (success) {
					Ext.getBody().mask('已经成功创建工程!', 'x-mask-loading');
					Ext.getBody().unmask.defer(1000, Ext.getBody());
					
					// 展示工程目录
					var treeRoot = Ext.getCmp('fileTree').getRootNode();
					if (!!treeRoot) {
						treeRoot.setText('Adp_' + unit.replace(/.*\//ig, ''));
						if (!Ext.getCmp('fileTree').collapsed) {
							treeRoot.reload();
							_this.codeTransfer();
						}
					}
				} else {
					_this.showException(response);
				}
			}
		});
	},
	
	// 代码转换
	codeTransfer : function() {
		var _this = this;
		
		// java文件转html格式
		Ext.Ajax.request({
			method: 'POST',
			url: _this.url,
			params: {
				operation: 'javaToHtml',
				unit: unit
			},
			callback: function(options, success, response) {
				if (!success){
					_this.showException(response);
				}
			}
		});
		
		Ext.getCmp('fileTree').getTopToolbar().findById('report_icon').enable();
		if (isVersioned)
			Ext.getCmp('fileTree').getTopToolbar().findById('svn_icon').disable();
	},
	
	// 生成报告
	createReport : function() {
		var _this = this;
		Ext.getBody().mask('正在生成报告...', 'x-mask-loading');
		
		Ext.Ajax.request({
			method: 'POST',
			url: _this.url,
			params: {
				operation: 'createReport',
				unit: unit,
				project: 'Adp_' + unit.replace(/.*\//ig, '')
			},
			callback: function(options, success, response) {
				Ext.getBody().unmask();
				if (!success){
					_this.showException(response);
				} else {
					var treeRoot = Ext.getCmp('fileTree').getRootNode();
					if (!!treeRoot) {
						if (!Ext.getCmp('fileTree').collapsed) {
							treeRoot.reload();
						}
					}
				}
			}
		});
	},
	
	// 获取SVN路径
	getSvnProp: function() {
		var _this = this;
		Ext.Ajax.request({
			method: 'GET',
			url: _this.url,
			params: {
				operation: 'getAdpSvnPath',
				unit: unit,
				project: 'Adp_' + unit.replace(/.*\//ig, '')
			},
			callback: function(options, success, response){
				if (!success){
					_this.showException(response);
				} else {
					Ext.Msg.show({
						title: '工程属性',
						msg: response.responseText,
						buttons: Ext.Msg.OK
					});
				}
			}
		});
	},
	
	// 重载父类创建接口方法
	// 添加接口
	addIntf : function() {
		var _this = this;
		var panel = new Ext.form.FormPanel({
			header: false,
			autoHeight: true,
			frame: true,
			border: false,
			style: {
				'padding': '10 5 5 5'
			},
			labelWidth: 115,
			defaults: {
				anchor: '100%',
				stateful: false
			},
			items: [{
				fieldLabel: '接口名称',
				xtype: 'textfield',
				itemId: 'interface',
				emptyText: '<请输入符合JAVA命名规范的接口名称>',
				regex: /[a-zA-Z]+/,
				regexText: '接口名称必须符合JAVA命名规范',
				allowBlank: false
			},{
				fieldLabel: '接口描述',
				xtype: 'textarea',
				itemId: 'intfDesc',
				allowBlank: false,
				listeners: {
					specialKey: function(obj, e){
						if (e.getKey() == Ext.EventObject.ENTER) 
							submitHandler();
					}
				}
			}]
		});
		
		function submitHandler(){
			var intf = panel.get('interface').getValue()
					   .replace(/\b\w+\b/g, function(word) {
	                       return word.substring(0,1).toUpperCase() + word.substring(1);
	                   });
			var desc = panel.get('intfDesc').getValue();
			var intfs = Ext.getCmp('service-panel').items.items;
			var hasPublished = false;
			Ext.each(intfs, function(item, index){
				var publishedIntf = item.title.replace(/接口(：|:)/ig, '').trim().toLowerCase();
				if (intf.toLowerCase() == publishedIntf) {
					hasPublished = true;
					return;
				}
			});
			
			if (!hasPublished) {
				if (!!(/^[A-Z]{1,2}[a-zA-Z]+$/).exec(intf)) {
					_this.createIntf(intf, desc);
					selectWin.close();
				} else {
					Ext.Msg.alert('提示', '接口名称必须符合JAVA命名规范!');
				}
			} else {
				Ext.Msg.alert('提示:', '该接口 "<b style="color:red">' + intf + '</b>" ' + '已经存在,请重新输入！');
			}
		}
		
		var selectWin = new Ext.Window({
			title : '新建接口',
			autoCreate: true,
			border : false,
	        resizable:false, constrain:true, 
			constrainHeader:true,
	        minimizable:false, maximizable:false,
	        stateful:false,  modal:true,
			defaultButton: 0,
	        width: 400, height: 180, 
			minWidth: 200,
	        footer: true,
	        closable: true, 
			closeAction: 'close',
			plain: true,
			layout: 'fit',
			items: panel,
			buttons: [{
		        text: '确定',
		        disabled: false,
				handler: function(){
					if (!panel.getForm().isValid())
						return;
					submitHandler();
				}
			},{
		        text: '取消',
				handler: function(){
					selectWin.close();
				}
			}]
		});
		selectWin.show();
	},
	
	// 重载父类添加操作方法
	// 右键菜单->"添加操作"选项
	dialogSubmit: function(dlg){
		var _this = this;
		var formPanel = dlg.getComponent(0);
		var form = formPanel.getForm();
		var values = form.getValues();
		var params = {};
		if (dlg.options.data) {
			params = {
				file: dlg.options.data['file'],
				'old-opera': dlg.options.data['opera'],
				'old-intf': dlg.options.data['interface'],
				'interface': form.items.get('interface').getValue(),
				component: component
			};
		}

		if (values['opera'].toLowerCase() == dlg.options.data['interface'].toLowerCase()){
			Ext.Msg.alert('提示', '操作名不能与接口名重复!');
			return;
		}
		
		if (!(/^[a-zA-Z]+$/).exec(values['opera'])) {
			Ext.Msg.alert('提示', '操作名必须符合JAVA方法命名规范!');
			return;
		}
		
		var operaValue = form.getValues(false)['opera'].trim();
		if (!operaValue) {
			Ext.Msg.alert('提示:', '操作名不允许为空!');
			return;
		} else {
			form.setValues(Ext.apply(form.getValues(false), {opera: operaValue}));
		}
		
		form.submit({
			clientValidation: true,
			params: Ext.apply(params, {
				operation: dlg.options.operation || 'createOpera'
			}),
			success: function(){
				var values = form.getValues();
				if (!values.file) 
					values.file = values.opera + ".wsdl";
				if (!values['interface'])
						values['interface'] = dlg.options.data['interface'];
				if (dlg.options.callback) dlg.options.callback(values); 
				dlg.hide();
			},
			failure: function(form, action){
				if (action.response) 
					_this.showException(action.response);
			}
		});
	}
});

var ADP = new UnitAdp();
Ext.onReady(ADP.init, ADP, true);