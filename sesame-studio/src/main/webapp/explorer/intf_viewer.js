document.oncontextmenu = function(e){return false;}
Ext.BLANK_IMAGE_URL='../images/s.gif';

var Viewer = function(){
	return {
		url: 'intf_ctrl.jsp',
		preparation: function(){
			Ext.Ajax.defaultHeaders = {
				"Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
			};
			Ext.QuickTips.init();
		},
		
		// 初始化接口面板
		initIntfTab: function() {
			var tab = new Ext.Panel({
				title: '接口设置',
				id: 'intf-panel',
				animCollapse: false, autoScroll: true,
				border: false, collapsible: false, enableHdMenu: false,
				defaults: {anchor: '100%'},
				frame: false,
				listeners: {
					activate: {
						fn: function(container){
							if (!container.loaded) {
								this.dataLoader();
								container.loaded = true;
							}
						},
						scope: this
					}
				},
				footerCssClass: 'x-window-bc',
				buttonAlign: 'right',
				fbar: [{
					text: '保存',
					ref: '../save',
					handler: function(){
						var intfs = Ext.getCmp('intf-panel').items.items;
						this.dataSaver(intfs);
					},
					scope: this
				}, {
					text: '刷新',
					ref: '../reload',
					scope: this,
					listeners: {
						click: this.dataLoader,
						scope: this
					}
				}]
			});
			return tab;
		},
		
		// 加载接口数据
		dataLoader: function(){
			var _this = this;
			var intfTabPanel = Ext.getCmp('intf-panel');
			var intfs = intfTabPanel.items.items;
			
			// 没有任何接口时才读取文件
			Ext.getBody().mask('正在读取接口设置...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST',
				url: _this.url,
				params: {
					operation: 'loadData',
					intfPath: intf
				},
				callback: function(options, success, response){
					Ext.getBody().unmask();
					if (!success) {
						_this.showException(response);
					} else {
						var result = Ext.decode(response.responseText);
						var items = result.items;
						
						// 将返回结果分组
						if (!!items && items.length > 0) {
							var rs = {};
							Ext.each(items, function(item, index){
								var item = item;
								var intf = item['interface'];
								var opera = item['opera'];
								
								if (!rs[intf]) {
									var group = [item];
									rs[intf] = group;
								}
								else {
									rs[intf].push(item);
								}
							});
							
							var groupItems = [];
							Ext.each(rs, function(intfGroup){
								for (var key in intfGroup) {
									var temp = {};
									temp.items = intfGroup[key];
									groupItems.push(temp);
								}
							});
							
							intfTabPanel.removeAll(true);
							if (groupItems.length > 0) {
								Ext.each(groupItems, function(item){
									var panel = _this.createOperaPanel(item);
									var grid = panel.getComponent('operaGrid');
									_this.addCtxMenuListener(grid, item);
									intfTabPanel.add(panel);
									intfTabPanel.doLayout();
								});
							}
							intfTabPanel.doLayout();
						} else { // 没有定义操作列表时只生成接口面板
							var intfs = intfTabPanel.items.items;
							if (intfs.length == 0) {
								_this.createIntf();
							}
						}
					}
				}
			});
		},
		
		// [接口设置]数据保存
		dataSaver: function(intfs) {
			var _this = this;
			var xmldoc = XDom.createDocument();
			try {
				var rootEl = xmldoc.createElement('portTypes');
				xmldoc.appendChild(rootEl);
				
				var isValid = true;
				Ext.each(intfs, function(intf){
					var form = intf.getForm();
					// 操作列表
					var store = intf.get('operaGrid').store;
					if(store.getCount() == 0)
						isValid = false;
						
					var params = form.getValues(false);
					var portTypeEl = xmldoc.createElement('portType');
					portTypeEl.setAttribute('intf', intf);
					portTypeEl.setAttribute('intfDesc', params['intfDesc']);
					rootEl.appendChild(portTypeEl);
				});
				
				if (!isValid){
					Ext.Msg.show({
					   title:'提示',
					   msg: '操作列表不允许为空！',
					   icon: Ext.MessageBox.WARNING
					});
					return;
				}
				
				Ext.getBody().mask('正在保存数据...', 'x-mask-loading');
				Ext.Ajax.request({
					method: 'POST',
					url: _this.url,
					params: {
						operation: "savePortTypes",
						intf: intf,
						data: encodeURIComponent(XDom.innerXML(xmldoc))
					},
					callback: function(options, success, response){
						if (!success) {
							Ext.getBody().unmask();
							_this.showException(response);
						}else {
							Ext.getBody().mask('接口数据保存成功', 'x-mask-loading');
							Ext.getBody().unmask.defer(500, Ext.getBody());
						}
					}
				});
			} catch(e) {
				alert(e.message);
			} finally {
				delete xmldoc;
			}
		},
		
		// 创建接口
		createIntf : function() {
			var _this = this;
			var servicePanel = Ext.getCmp('intf-panel');
			var item = [{'interface':intf, 'intfDesc': intfDesc}];
			var items = {items : item};
			var panel = _this.createOperaPanel(items);
			var grid = panel.getComponent('operaGrid');
			grid.store.removeAll();	
			_this.addCtxMenuListener(grid, items);
			servicePanel.add(panel);
			servicePanel.doLayout();
		},
		
		// 创建操作面板
		createOperaPanel: function(options){
			var _this = this;
			var intf = options.items[0]['interface'].replace(/.*?\//ig, '');
			var intfDesc = options.items[0]['intfDesc'];
			var title = '接口名称: ' + intf;
			
			// 操作列表表格
			var columns = [
				{header: "名称", id:'opera',width: 40, sortable: true, dataIndex: 'opera'},
		        {header: "描述", width: 70, sortable: true, dataIndex: 'desc'},
		        {header: "修改时间", width: 50, sortable: true, dataIndex: 'lastModified'}
			];
			
			var store = new Ext.data.Store({
				reader: new Ext.data.JsonReader(
					{root: 'items'},
					[{name: 'opera'},
					 {name: 'desc'},
					 {name: 'modifier'},
					 {name: 'lastModified'}]
				)
			});
			var operaGridPanel = new Ext.grid.GridPanel({
				header: true,
				title: '操作列表',
				itemId: 'operaGrid',
				autoScroll: true,
				height: 300,
				columnLines: true,
				headerStyle: {'border-bottom': '0px'},
				bodyStyle: {'border': '1px solid #99BBE8'},
				columns: columns,
				viewConfig: {forceFit: true},
				store: store
			});
			
			operaGridPanel.store.loadData({
				items: options.items
			});
			
			operaGridPanel.on('render', function(grid){
				var store = grid.getStore();
				var view = grid.getView();
				
				operaGridPanel.tip = new Ext.ToolTip({
					target: view.mainBody,
					delegate: '.x-grid3-row',
					trackMouse: true,
					renderTo: document.body,
					mouseOffset: [-70, 5],
					listeners: {
						beforeshow: function(tip){
							tip.body.dom.innerHTML = '双击查看该操作';
						}
					}
				});
			});
			
			var formpanel = new Ext.form.FormPanel({
				title: title,
				style: {'margin': '5 8 3 8'},
				frame: true,
				closable: true,
				collapsible: true,
				defaults: {anchor: '100%'},
				labelWidth: 120,
				items: [
					{
						fieldLabel: '描述',
						labelStyle: 'padding : 3 0 0 5',
						xtype: 'textfield',
						name: 'intfDesc',
						value: intfDesc,
						allowBlank: true
					}, 
					operaGridPanel
				]
			});
			return formpanel;
		},
		
		// 操作列表表格右键菜单事件注册
		addCtxMenuListener: function(operaGridPanel, item){
			var _this = this;
			var intf = item.items[0]['interface'];
			var ctxMenu = new Ext.menu.Menu({
				items: [{
					text: '添加操作',
					handler: function(item){
						this.addHandler(intf, operaGridPanel);
					},
					scope: this
				}, {
					itemId: 'open',
					text: '打开',
					handler: function(){
						this.openHandler(ctxMenu);
					},
					scope: this
				}, {
					itemId: 'property',
					text: '属性',
					handler: function(){
						this.propertyHandler(operaGridPanel, ctxMenu);
					},
					scope: this
				}, {
					itemId: 'remove',
					text: '删除',
					icon: '../images/icons/remove.gif',
					handler: function(){
						this.removeHandler(operaGridPanel, ctxMenu);
					},
					scope: this
				}, '-', {
					itemId: 'refresh',
					text: '刷新',
					icon: "../images/tool16/refresh.gif",
					handler: function(){
						delete ctxMenu;
						this.dataLoader();
					},
					scope: this
				}]
			});
			
			operaGridPanel.on("contextmenu", function(e){
				var sels = operaGridPanel.selModel.getSelections();
				ctxMenu.selections = sels;
				ctxMenu.items.get('property').setDisabled(sels.length != 1);
				ctxMenu.items.get('remove').setDisabled(sels.length < 1);
				ctxMenu.items.get('open').setDisabled(sels.length < 1);
				ctxMenu.showAt(e.getXY());
				e.stopEvent();
			});
			
			operaGridPanel.on("rowdblclick", function(sender, rowIndex, e){
				var rec = operaGridPanel.store.getAt(rowIndex);
				_this.gridClickfn(rec);
			});
		},
		
		// 右键菜单->"添加操作"选项
		addHandler: function(intf, servicePanel) {
			var _this = this;
			var callback = function(values){
				_this.addHandlerCallback(servicePanel, values);
			};
			_this.getDialog({
				data: {'interface': intf},
				callback: callback
			}).show();
		},
		
		// 右键菜单->"删除"选项
		removeHandler: function(servicePanel, ctxMenu) {
			var _this = this;
			if (!ctxMenu) 
				return false;
			
			if (confirm('确实要删除选中的 ' + ctxMenu.selections.length + ' 项吗?')) {
				var operas = [];
				Ext.each(ctxMenu.selections, function(rec){operas.push(rec.data);});
				Ext.Ajax.request({
					method: 'POST',
					url: _this.url,
					params: {
						operation: "removeOpera",
						intf: intf,
						operas: Ext.encode(operas)
					},
					callback: function(options, success, response){
						if (success) {
							Ext.each(ctxMenu.selections, function(rec){
								Application.closeWindow(intf + '/' + rec.get('opera'));
								servicePanel.store.remove(rec);
							});
						} else {
							_this.showException(response);
						}
					}
				});
			}
		},
		
		// 右键菜单->"属性"选项
		propertyHandler: function(servicePanel, ctxMenu){
			var _this = this;
			if (!ctxMenu) 
				return false;
				
			var rec = ctxMenu.selections[0];
			var callback = function(values){
				_this.propertyCallback(servicePanel, rec, values)
			};
			_this.getDialog({
				title: '操作属性',
				operation: 'modifyOpera',
				data: rec.data,
				callback: callback
			}).show();
		},
		
		propertyCallback: function(servicePanel, rec, values) {
			rec.set('opera', values['opera']);
			rec.set('desc', values['desc']);
		},
		
		// 新建操作对话框
		getDialog: function(options){
			var _this = this;
			
			var operaForm = new Ext.form.FormPanel({
				xtype: 'form',
				autoScroll: true,
				labelWidth: 90,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: {
					anchor: '-18',
					stateful: false,
					allowBlank: false
				},
				url: _this.url,
				baseParams: { intf: intf },
				items: [{name: 'opera',fieldLabel: '操作名称'}, 
						{name: 'desc', fieldLabel: '描述'}]
			});
			
			var dlg = new Ext.Window({
				title: '新建操作',
				autoCreate: true,
				resizable: false,
				constrain: true,
				constrainHeader: true,
				minimizable: false,
				maximizable: false,
				stateful: false,
				modal: true,
				buttonAlign: 'right',
				defaultButton: 0,
				width: 450,
				height: 150,
				minWidth: 150,
				footer: true,
				closable: true,
				closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: operaForm
			});
			
			dlg.ok = dlg.addButton({
				text: Ext.Msg.buttonText.ok
			}, function(){
				_this.dialogSubmit(dlg);
			});
			dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
				dlg.hide();
			});
			
			dlg.render(Ext.getBody());
			this.serviceDlg = dlg;
			
			this.serviceDlg.options = options;
			var formPanel = this.serviceDlg.getComponent(0);
			var form = formPanel.getForm();
			
			this.afterSubmit(form, formPanel, options);
			this.serviceDlg.setTitle(options.title || '创建操作');
			return this.serviceDlg;
		},
		
		afterSubmit: function(form, formPanel, options){
			form.reset();
			form.setValues(options.data);
		},
		
		// [新建/修改]操作
		dialogSubmit: function(dlg){
			var _this = this;
			var formPanel = dlg.getComponent(0);
			var form = formPanel.getForm();
			var params = {};
			if (dlg.options.data) {
				params = {
					'old-opera': dlg.options.data['opera']
				};
			}
			
			var operaValue = form.getValues(false)['opera'].trim();
			if (!operaValue) {
				Ext.Msg.alert('提示:', '操作名不允许为空!');
				return;
			} else {
				form.setValues(Ext.apply(form.getValues(false), {
					opera: operaValue
				}));
			}
			
			form.submit({
				clientValidation: true,
				params: Ext.apply(params, {
					operation: dlg.options.operation || 'createOpera'
				}),
				success: function(){
					var values = form.getValues();
					if (dlg.options.callback) 
						dlg.options.callback(values);
					dlg.hide();
				},
				failure: function(form, action){
					if (action.response) 
						_this.showException(action.response);
				}
			});
		},
		
		addHandlerCallback: function(servicePanel, values) {
			servicePanel.store.loadData({count:1, items:[values]}, true);
		},
		
		// 右键菜单->"打开"选项
		openHandler: function(ctxMenu) {
			var _this = this;
			if (!ctxMenu) 
				return false;
			Ext.each(ctxMenu.selections, function(rec){
				_this.gridClickfn(rec);
			});
		},
		
		gridClickfn: function(rec){
			var _this = this;
			_this.openOpera(rec.get('opera'), rec.get('desc'), '');
		},
		
		openOpera : function(opera, desc, ref){
			Application.createWindow(
				intf + '/' + opera,
				opera + '-' + desc,
				'../schema/message_viewer.jsp?schema='+ intf + '/' 
				+ opera + '.xsd' + '&unit=' + intf + '&unitId=' + intfId
				+ '&unitDesc=' + escape(encodeURIComponent(intfDesc)) + '&ref=' + ref 
				+ '&projectDesc=' + escape(encodeURIComponent(projectDesc))
				+ '&isOriginalPublic=true', 
				'settings');
		},
		
		init: function(){
			var _this = this;
			_this.preparation();
			var intfTab = _this.initIntfTab();
			var tabPanel = new Ext.ux.InlineToolbarTabPanel({
				id: 'mainIntfPanel',
				activeTab: 0,
				headerToolbar: true,
				toolbar: {
					items: ['->', {
						width: 30,
						text: '返回',
						icon: '../images/tool16/undo_edit.gif',
						cls: 'x-btn-text-icon',
						handler: function(){
							_this.openProject();
						}
					}]
				},
				style: 'border-right:0px;',
				items: intfTab
			});
			
			var viewport = new Ext.Viewport({
				stateful: false,
				layout: 'fit',
				items: tabPanel
			});
		},
		
		openProject: function(){
			var currentWinId = Application.getDesktop().getActiveWindow().id;
			Application.closeWindow(currentWinId);
			var name = intf.replace(/\/.*/ig, '');
			var desktop = Application.getDesktop();
			var win = desktop.getWindow('project_' + name);
			if (!win) 
				win = desktop.createWindow({
					id: 'project_' + name,
					title: decodeURIComponent(projectDesc),
					width: 950,
					height: 500,
					iconCls: 'bogus',
					shim: false,
					animCollapse: false,
					constrainHeader: true,
					resizable: false,
					maximizable: false,
					listeners: {
						render: function(sender){
							this.body.dom.contentWindow.onfocus = function(){
								sender.toFront()
							};
							this.body.setVisible(true);
						}
					},
					bodyCfg: {
						tag: 'iframe',
						style: 'overflow:auto; display:none;',
						frameBorder: 0,
						src: '../explorer/project_viewer.jsp?project=' + name + '&projectDesc=' + encodeURIComponent(projectDesc)
					}
				});
			win.show();
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
						return;			}
				
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
