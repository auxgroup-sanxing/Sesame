var Viewer = function() {
	return {
		preparation: function(){
			Ext.Ajax.defaultHeaders = {
				"Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
			};
			Ext.QuickTips.init();
		},
		
		url: 'install_ctrl.jsp',
		
		init: function(){
			var _this = this;
			var Item = [{name: 'desc'},{name: 'checked'}, {name: 'path'}];
			var compStore = new Ext.data.Store({
				reader: new Ext.data.JsonReader({fields: Item, root: 'comps'})
			});
			var libStore = new Ext.data.Store({
				reader: new Ext.data.JsonReader({fields: Item, root: 'libs'})
			});
			var transStore = new Ext.data.Store({
				reader: new Ext.data.JsonReader({fields: Item, root: 'trans'})
			});
			var viewTpl = new Ext.XTemplate(
				'<tpl for=".">' +
			        '<div class="item-wrap" style="cursor:pointer;padding:1px;white-space:nowrap;">' +
						'<div class="itemNameWrap">' +
							'<tpl if="checked">' +
								'<div class="checkboxWrap"><input type="checkbox" class="resourceCheckBox" name="resourceCheckBox" checked></div>' +
							'</tpl>'+
							'<tpl if="!checked">' +
								'<div class="checkboxWrap"><input type="checkbox" class="resourceCheckBox" name="resourceCheckBox"></div>' +
							'</tpl>'+
							'<div class="descWrap">{desc}</div>' +
							'<div class="pathClass">{path}</div>' + 
						'</div>' +
					'</div>' +
		        '</tpl>'
			);
			var resourceStore = [compStore, libStore, transStore];
			
			/** 服务部署 **/
			var unitTab = new Ext.Panel({
				id: 'unitTab',
				title: '服务部署',
				bodyStyle: {'background-color':'#DFE8F6'},
				items: [
					{
						title: '项目列表 <span class="tipInfo">(选择需要部署的服务单元,点击"下一步"进入资源配置列表)</span>',
						xtype: 'fieldset',
						anchor: '-5',
						style: 'margin:10 0 0 2',
						layout: 'fit',
						name: 'detailInfo',
						itemId: 'detailInfo',
						height: 360,
						border: false,
						items: [{
							itemId: 'dir-tree',
							xtype: 'treepanel',
							border: true,
							bodyStyle: {
								'-moz-border-radius': '5px'	// 边框圆角效果只在firefox下有效
							},	
							loader: new Ext.tree.TreeLoader({
								dataUrl: _this.url,
								baseParams: {
									operation: 'getProjects'
								},
								listeners: {
									beforeload: function(loader, node){
										loader.baseParams.path = node.getPath('name').substring(1);
										loader.baseParams.checked = node.attributes.checked;
										loader.baseParams.depth = node.getDepth();
									},
									loadexception: function(loader, node, response){
										_this.showException(response);
									}
								}
							}),
							listeners: {
								checkchange: function(node, checked){
									_this.selectChange(node, checked);
								}
							},
							rootVisible: false,
							lines: false,
							animate: false,
							autoScroll: true,
							trackMouseOver: false,
							root: new Ext.tree.AsyncTreeNode({
								id: 'project_root',
								expanded: true
							})
						}]
					}
				],
				footerCssClass: 'x-window-bc',
				buttons: [{
					scope : this,
					text: '下一步',
					handler: function(){
						this.gotoUnitResource(unitTab, resourceStore);
					}
				}, {
					scope : this,
					text: '取消',
					handler: this.cancelDepoly
				}],
				listeners: {
					activate: function(container){
						if (!container.loaded) {
							var resourceTree = unitTab.get('detailInfo').get('dir-tree');
							var treeRoot = resourceTree.getRootNode();
							if (!!treeRoot) {
								Ext.getBody().mask('正在加载数据', 'x-mask-loading');
								treeRoot.reload(function(root){
									Ext.getBody().unmask();
								});
							}
							container.loaded = true;
						}
					}
				}
			});
			
			/** 服务资源 **/
			// 视图模板
			var compView = new Ext.DataView({
				id: 'compView',
	            tpl: viewTpl,
	            multiSelect: false,
				anchor: -10,
	            store: compStore,
	            itemSelector: 'div.item-wrap',
	            selectedClass: 'hilited',
				loadingText: '正在加载列表...',
	            emptyText: '<列表为空>'
	        });
			
			var libView = new Ext.DataView({
				id: 'libView',
	            tpl: viewTpl,
				anchor: -10,
	            multiSelect: false,
	            store: libStore,
	            itemSelector: 'div.item-wrap',
	            selectedClass: 'hilited',
				loadingText: '正在加载列表...',
	            emptyText: '<列表为空>'
	        });
			
			var transView = new Ext.DataView({
				id: 'transView',
	            tpl: viewTpl,
				anchor: -10,
	            multiSelect: false,
	            store: transStore,
	            itemSelector: 'div.item-wrap',
	            selectedClass: 'hilited',
				loadingText: '正在加载列表...',
	            emptyText: '<列表为空>'
	        });
			
			var resourceTab = new Ext.form.FormPanel({
				id: 'resourceTab',
				title: '服务资源',
				bodyStyle: {'background-color':'#DFE8F6'},
				items: [{
					title: '组件列表 ',
					xtype: 'fieldset',
					anchor: '-15',
					style: 'margin:10 0 0 15;-moz-border-radius:5px;overflow-y:auto;',
					layout: 'fit',
					name: 'compInfo',
					itemId: 'compInfo',
					height: 118,
					border: true,
					items: compView
				},{
					title: '共享库 ',
					xtype: 'fieldset',
					autoScroll: true,
					anchor: '-15',
					style: 'margin:5 0 0 15;-moz-border-radius:5px;overflow-y:auto;',
					layout: 'fit',
					name: 'libInfo',
					itemId: 'libInfo',
					height: 118,
					border: true,
					items: libView
				},{
					title: '传输端子 ',
					xtype: 'fieldset',
					autoScroll: true,
					anchor: '-15',
					style: 'margin:5 0 0 15;-moz-border-radius:5px;overflow-y:auto;',
					layout: 'fit',
					name: 'transInfo',
					itemId: 'transInfo',
					height: 118,
					border: true,
					items: transView
				}],
				footerCssClass: 'x-window-bc',
				buttons: [
				{
					scope : this,
					text: '上一步',
					handler: this.gotoProjects
				},
				{
					scope : this,
					text: '完成',
					handler: function(){
						this.finishDeploy(unitTab, resourceTab);
					}
				}, {
					scope : this,
					text: '取消',
					handler: this.cancelDepoly
				}]
			});
			
			// 部署向导
			var wizardTabPanel = new Ext.TabPanel({
				id: 'wizardTabPanel',
				activeTab: 0,
				plain: true,
				items: [unitTab, resourceTab]
			});
			
			var viewport = new Ext.Viewport({
				stateful: false,
				layout: 'fit',
				items: wizardTabPanel
			});
			
			wizardTabPanel.hideTabStripItem(1);
		},
		
		// 进入服务部署步骤
		gotoProjects : function() {
			var wizardTabPanel = Ext.getCmp('wizardTabPanel');
			if (!!wizardTabPanel) {
				wizardTabPanel.hideTabStripItem(1);
				wizardTabPanel.unhideTabStripItem(0);
				wizardTabPanel.setActiveTab(0);
			}
		},
		
		// 进入服务资源选择步骤
		gotoUnitResource: function(unitPanel, resourceStore) {
			var _this = this;
			var data = [];
			// 根据选择的项目和服务单元决定资源的checkbox是否被选中
			if (!!unitPanel) {
				var resourceTree = unitPanel.get('detailInfo').get('dir-tree');
				if (!!resourceTree)
					var selectedNodes = resourceTree.getChecked();
					if(selectedNodes.length > 0) {
						Ext.each(selectedNodes, function(node) {
							var info = {};
							info.depth = node.getDepth();
							info.name = node.attributes.name;
							info.path = node.getPath('name').substring(1);
							data.push(info);
						});
					}
					
				if (data.length == 0)
					data = '';
				
				Ext.getBody().mask('正在加载数据...', 'x-mask-loading');
				Ext.Ajax.request({
					method: 'GET',
					url: _this.url,
					params: {
						operation: 'loadWarehouse',
						data: Ext.encode(data)
					},
					callback: function(options, success, response){
						Ext.getBody().unmask();
						if (!success) {
							_this.showException(response);
						} else {
							var items = Ext.decode(response.responseText);
							resourceStore[0].loadData({comps: items.comps});
							resourceStore[1].loadData({libs: items.libs});
							resourceStore[2].loadData({trans: items.trans});
						}
					}
				});	
			}
			
			var wizardTabPanel = Ext.getCmp('wizardTabPanel')
			if (!!wizardTabPanel) {
				wizardTabPanel.hideTabStripItem(0);
				wizardTabPanel.unhideTabStripItem(1);
				wizardTabPanel.setActiveTab(1);
			}
		},
		
		// 完成部署
		finishDeploy: function(unitPanel, resourceTab) {
			var _this = this;
			var values = {
				compress: 'on',
				report: 'on',
				dowload: 'on'
			};
			
			// 服务单元信息
			var unitData = [];
			var resourceTree = unitPanel.get('detailInfo').get('dir-tree');
			if (!!resourceTree) {
				var selectedNodes = resourceTree.getChecked();
				if (selectedNodes.length > 0) {
					Ext.each(selectedNodes, function(node){
						var info = {};
						info.depth = node.getDepth();
						info.name = node.attributes.name;
						info.path = node.getPath('name').substring(1);
						unitData.push(info);
					});
				}
			}
				
			if (unitData.length == 0)
				unitData = '';


			// 资源文件信息
			var resourceData = {};
			var compCheckboxes = Ext.fly('compView').select('input.resourceCheckBox').elements;
			var libCheckboxes = Ext.fly('libView').select('input.resourceCheckBox').elements;
			var transCheckboxes = Ext.fly('transView').select('input.resourceCheckBox').elements;
			
			var compValue = [];		
			if (compCheckboxes.length > 0) {
				Ext.each(compCheckboxes, function(checkbox) {
					if (checkbox.checked == true) {
						var info = {};
						var path = Ext.fly(checkbox).up('div.itemNameWrap').query('div:last-child')[0].innerHTML;
						info.path = path;
						compValue.push(info);
					}
				});
			}
			resourceData.comp = compValue;
			
			var libValue = [];
			if (libCheckboxes.length > 0) {
				Ext.each(libCheckboxes, function(checkbox) {
					if (checkbox.checked == true) {
						var info = {};
						var path = Ext.fly(checkbox).up('div.itemNameWrap').query('div:last-child')[0].innerHTML;
						info.path = path;
						libValue.push(info);
					}
				});
			}
			resourceData.lib = libValue;
			
			var transValue = [];
			if (transCheckboxes.length > 0) {
				Ext.each(transCheckboxes, function(checkbox) {
					if (checkbox.checked == true) {
						var info = {};
						var path = Ext.fly(checkbox).up('div.itemNameWrap').query('div:last-child')[0].innerHTML;
						info.path = path;
						transValue.push(info);
					}
				});
			}
			resourceData.trans = transValue;

			Ext.getBody().mask('正在生成部署文件...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST',
				timeout: 90 * 1000,
				url: _this.url,
				params: {
					operation: 'finishDeploy',
					unitData: Ext.encode(unitData),
					resourceData: Ext.encode(resourceData),
					deployData: Ext.encode(values)
				},
				callback : function(options, success, response) {
					Ext.getBody().unmask();
					if (!!success) {
						var rs = Ext.decode(response.responseText);
						if (!!rs.html) {
							Ext.MessageBox.show({
								title: '部署完成',
								msg: '<div style="width:auto; height:auto; background-color:white; overflow:auto; padding:10px;">' +
								rs.html +
								'</div>',
								buttons: Ext.MessageBox.OK,
								width: 492,
								fn: function() {
									_this.cancelDepoly();
								}
							});
						} else {
							Ext.MessageBox.show({
								title: '提示:',
								msg: '部署过程中出现错误!',
								icon: Ext.MessageBox.WARNING,
								buttons: Ext.MessageBox.OK,
								fn: function() {
									_this.cancelDepoly();
								}
							});
						}
					} else {
						_this.showException(response);
					}
				}
			});
		},
		
		// 服务部署 checkbox事件相应
		selectChange: function(node, checked){
			node.getOwnerTree().suspendEvents();
			node.bubble(function(n){
				if (n == node && node.id == 'project_root') 
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
		},
		
		// 取消部署
		cancelDepoly : function() {
			 var win = Application.desktop.getWindow('deploy-wizard');
			 if (!!win)
			 	win.close();
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
						return;
				}
				
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
	}
}();

Ext.onReady(Viewer.init, Viewer, true);