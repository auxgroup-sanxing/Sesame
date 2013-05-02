// document.oncontextmenu = function(e) {return false;};
Ext.BLANK_IMAGE_URL='../images/s.gif';
Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

/**
 * 业务常量设置 
 */
var Viewer = function(){
	return {
		url: 'busconstant_ctrl.jsp',
		title: null, 
		loadMethod: null,
		saveMethod: null, 
		removeMethod: null,
		
		prepareInit: function() {
			var _this = this;
			switch(type) {
				case 'APP':
					_this.title = '全局业务常量配置';
					_this.loadMethod = 'loadAppParameters';
					_this.saveMethod = 'saveAppParameters';
					_this.removeMethod = 'removeAppParameter';
					break;
				case 'UNIT':
					_this.title = '服务单元业务常量配置';
					_this.loadMethod = 'loadSuParameters';
					_this.saveMethod = 'saveSuParameters';
					_this.removeMethod = 'removeSuParameter';
					break;
				case 'OPERATION':
					_this.title = '操作业务常量配置';
					_this.loadMethod = 'loadOperaParameters';
					_this.saveMethod = 'saveOperaParameters';
					_this.removeMethod = 'removeOperaParameter';
					break;	
			}
		},
		
		init: function() {
			var _this = this;
			Ext.QuickTips.init();
			_this.prepareInit();
			
			var store = new Ext.data.Store({
				proxy: new Ext.data.HttpProxy({
				    url: _this.url
				}),
				baseParams: {
					operation: _this.loadMethod,
					suName: suName,
					operaName: operaName
				},
				reader: new Ext.data.JsonReader({
					successProperty: 'success',
					totalProperty : "count",
					idProperty: 'name',
			        root: 'items',
			        fields: [
			           {name: 'name'}, 
					   {name: 'value'},
					   {name: 'type'}, 
					   {name: 'comment'}
			        ]
		        }),
		        writer: new Ext.data.JsonWriter({
				    encode: true,
				    writeAllFields: true
				}),
				autoSave: false,
	            sortInfo: {
					field: 'name',
					direction: "ASC"
				},
				listeners: {
					loadexception: function(proxy, obj, response){
						_this.showException(response);
					}
				}
			});
			store.load();
			
			var columns = [{
	            	header: "名称", sortable: true, dataIndex: 'name', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            },{
	            	header: "类型", sortable: true, dataIndex: 'type', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            },{
	            	header: "值", sortable: true, dataIndex: 'value', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            },{
	            	header: "描述", sortable: true, dataIndex: 'comment', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            }];
			
			var settingPanel = new Ext.grid.EditorGridPanel({
				title: _this.title,
				columnLines: true,
				border: false,
				loadMask: true,
				store: store,
				columns: columns,
				viewConfig: {forceFit: true},
				bbar: [{
					cls: 'x-btn-text-icon',
					icon: '../images/tool16/add.gif',
					text: '添加',
					handler: function(button, e){
						var panel = settingPanel;
						panel.store.add(new panel.store.recordType({}));
						panel.startEditing(panel.store.getCount() - 1, 0);
					}
				}, {
					cls: 'x-btn-text-icon',
					icon: '../images/tool16/delete.gif',
					text: '删除',
					handler: function(button, e){
						_this.remove(settingPanel);
					}
				}, '->', {
					cls: 'x-btn-text-icon',
					icon: '../images/tool16/save_edit.gif',
					text: '保存',
					handler: function(button, e){
						settingPanel.stopEditing();
						_this.save(settingPanel);
					}
				}, {
					cls: 'x-btn-text-icon',
					icon: '../images/tool16/refresh.gif',
					text: '刷新',
					handler: function(){
						var panel = settingPanel;
						panel.store.reload();
					}
				}],
				listeners: {
					render: function(grid) {
						var store = grid.getStore();
						var view = grid.getView();
						
						grid.tip = new Ext.ToolTip({
							target: view.mainBody,
							delegate: '.x-grid3-row',
							trackMouse: true,
							renderTo: document.body,
							mouseOffset: [0, 0],
							listeners: {
								beforeshow: function(tip){
									tip.body.dom.innerHTML = '双击该单元格编辑参数';
								}
							}
						});
					}
				}
			});
			
			// 根据参数类型改变默认值类型
			settingPanel.getColumnModel().getCellEditor = function(colIndex, rowIndex){
				var field = this.getDataIndex(colIndex);
				if(field == 'type') {
					return new Ext.grid.GridEditor(new Ext.form.ComboBox({
						store: new Ext.data.ArrayStore({
							fields: ['name', 'value'],
							data: [
							['整型', 'PARAM_TYPE_INT'],
							['布尔', 'PARAM_TYPE_BOOLEAN'],
							['字符串', 'PARAM_TYPE_STRING'],
							['双精度', 'PARAM_TYPE_DOUBLE']]
						}),
						triggerAction: 'all',
						forceSelection:true,
						editable:false,
						displayField: 'name',
						valueField: 'value',
						mode: 'local',
						allowBlank: false
					}));
				} else {
					return new Ext.grid.GridEditor(new Ext.form.TextField({
						allowBlank: false
					}));
				}
			};
			
			viewport = new Ext.Viewport({
				layout : 'fit',
				items : settingPanel
			});
		},
		
		save: function(grid){
			var _this = this;
			var store = grid.store;
			
			var array = [];
			store.each(function(rec){
				if (rec.isValid()) 
					array.push(rec.data);
			});
			
			grid.bwrap.mask('正在保存...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST',
				url: _this.url,
				params: {
					operation: _this.saveMethod,
					data: Ext.encode(array),
					suName: suName,
					operaName: operaName
				},
				callback: function(options, success, response){
					grid.bwrap.unmask();
					if (!success) {
						_this.showException(response);
					}
					else {
						store.commitChanges();
					}
				}
			});
		},
		
		remove: function(panel) {
			var _this = this;
			var cell = panel.getSelectionModel().getSelectedCell();
			if (!cell || !window.confirm("确实要删除选中的项吗？")) 
				return;
				
			var name = panel.store.getAt(cell[0]).data.name;
			
			panel.bwrap.mask('正在删除...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST',
				url: _this.url,
				params: {
					operation: _this.removeMethod,
					name: name,
					suName: suName,
					operaName: operaName
				},
				callback: function(options, success, response){
					panel.bwrap.unmask();
					if (!success) {
						_this.showException(response);
					} else {
						panel.store.removeAt(cell[0]);
					}
				}
			});
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