Ext.BLANK_IMAGE_URL='../images/s.gif';

var Viewer = function(){
//变量
var mapingReader = new Ext.data.JsonReader({
    fields: [
       {name: 'fault-spot', allowBlank:false },
       {name: 'status', allowBlank:false },
       {name: 'result', allowBlank:false },
       {name: 'description'}
    ],
    root: 'items'
});

//转换函数
var convReader = new Ext.data.JsonReader({
	successProperty: 'success',
	idProperty: 'func-name',
    root: 'items',
    fields: [
       {name: 'func-name', allowBlank: false},
       {name: 'description'}
    ]
});

return {

	init : function(){
		var _this = this;

		Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
		//Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		Ext.QuickTips.init();
		
		var formPanel = new Ext.FormPanel({
			title: '基本属性',
		    labelWidth: 130,
		    url: 'binding_ctrl.jsp',
		    frame: false,
		    bodyStyle:'padding:5px 5px 0; text-align:left; background-color:white;',
			autoScroll:true, border: false,
			style: Ext.isGecko?'margin: auto;':'',
			layout: 'form',
			buttonAlign: 'right',
			defaults: {anchor:"-18", style:'margin-bottom:5px;'},
			items: [{
				xtype: 'panel',
				title: '组件标识',
				frame: true,
				layout: 'form',
				defaults: {anchor:"-18"},
				items: [{
		            fieldLabel : '组件名',
					name : 'name',
					xtype : 'textfield',
					allowBlank : false,
					readOnly : true
				},{
					fieldLabel : '描述',
					name : 'description',
					xtype : 'textfield',
					allowBlank : false
				},{
					fieldLabel : '组件类型',
					name : 'type',
					xtype: 'combo',
					width: 300,
					triggerAction: 'all',
					forceSelection:true, editable:false, disabled: true,
					store: [['binding-component', '绑定组件'], ['service-engine', '引擎组件']]
				}]
			},{
				xtype: 'editorgrid',
				title: '组件类',
				id: 'compGrid',
				autoHeight: true, boxMaxHeight: 200,
				frame: true, enableHdMenu: false,
				store: new Ext.data.Store({
					reader: new Ext.data.ArrayReader({
				        fields: [
				           {name: 'name'},
				           {name: 'url'}
				        ],
				        root: 'component-classpath'
			        })
				}),
                columns: [
		            {header: "类路径", width: 60, dataIndex: 'name', editor: new Ext.grid.GridEditor(new Ext.form.TextField({})) }
		        ],
				autoExpandColumn: 'url',
				viewConfig: {
					forceFit: true
				},
				tbar: [
				'类名称: &nbsp;',
				{
					xtype: 'combo',
					id: 'component-classname',
					width: 300,
					triggerAction: 'all',
					forceSelection:false, editable:true,
					store: ['com.sanxing.sesame.engine.component.ProcessEngine']
				}],
				tools: [{
					id: 'plus',
					qtip: '添加类路径',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						var row = cell ? cell[0]+1 : panel.store.getCount();
						panel.store.insert(row, new panel.store.recordType({}));
		            	panel.startEditing(row, 0);
					}
				},{
					id: 'minus',
					qtip: '删除类路径',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						if (!cell || !window.confirm("确实要删除选中的类路径吗？")) return;
						panel.store.removeAt(cell[0]);
					}
				}]
			},{
				xtype: 'editorgrid',
				title: '引导类',
				id: 'bootGrid',
				autoHeight: true, boxMaxHeight: 200,
				frame: true, enableHdMenu: false,
				style: 'margin-bottom:5px;',
				store: new Ext.data.Store({
					reader: new Ext.data.ArrayReader({
				        fields: [
				           {name: 'name'},
				           {name: 'url'}
				        ],
				        root: 'bootstrap-classpath'
			        })
				}),
                columns: [
		            {header: "类路径", width: 60, dataIndex: 'name', editor: new Ext.grid.GridEditor(new Ext.form.TextField({})) }
		        ],
				autoExpandColumn: 'url',
				viewConfig: {
					forceFit: true
				},
				tbar: [
				'类名称: &nbsp;',
				{
					xtype: 'combo',
					id: 'bootstrap-classname',
					width: 300,
					triggerAction: 'all',
					forceSelection:false, editable:true,
					store: ['com.sanxing.sesame.engine.component.BootstrapImpl']
				}],
				tools: [{
					id: 'plus',
					qtip: '添加类路径',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						var row = cell ? cell[0]+1 : panel.store.getCount();
						panel.store.insert(row, new panel.store.recordType({}));
		            	panel.startEditing(row, 0);
					}
				},{
					id: 'minus',
					qtip: '删除类路径',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						if (!cell || !window.confirm("确实要删除选中的类路径吗？")) return;
						panel.store.removeAt(cell[0]);
					}
				}]
			}],
			listeners: {
				activate: function(c){
					if (!c.loaded) {
						_this.loadAttrib(c);
						c.loaded = true;
					}
				}
			},
			footerCssClass: 'x-window-bc',
			buttons: [{
		        text: '保存',
		        disabled: properties.readOnly,
				handler: function(){
					if (formPanel.getForm().isValid())
						_this.saveAttrib(formPanel);
				}
			},{
		        text: '重置',
				handler: function(){
					_this.loadAttrib(formPanel);
				}
			}]
		});
	
		
		var funcPanel = new Ext.grid.EditorGridPanel({
			title: 'XPATH函数',
			columnLines: true,
			border: false, loadMask: true,
			store: new Ext.data.Store({
				//url: 'component_ctrl.jsp',
				proxy: new Ext.data.HttpProxy({
				    api: {
				        read : 'component_ctrl.jsp?operation=loadFunctions',
				        destroy: 'component_ctrl.jsp?operation=deleteFunctions'
				    }
				}),
				reader: new Ext.data.JsonReader({
					successProperty: 'success',
					totalProperty : "count",
					idProperty: 'func-name',
			        root: 'items',
			        fields: [
			           {name: 'prefix'},
			           {name: 'func-name'},
			           {name: 'class-name'},
			           {name: 'description'}
			        ]
		        }),
		        writer: new Ext.data.JsonWriter({
				    encode: true,
				    writeAllFields: true
				}),
				autoSave: false,
				baseParams: {	operation:'loadFunctions', component: component},
	            sortInfo:{field: 'func-name', direction: "ASC"},
				listeners: {
					loadexception: function(proxy, obj, response){ _this.showException(response); }
				}
			}),
            columns: [
	            {
	            	header: "前缀", width: 60, sortable: true, dataIndex: 'prefix', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            },
	            {
	            	header: "函数名称", width: 60, sortable: true, dataIndex: 'func-name', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            },
	            {
	            	header: "类名", width: 150, sortable: true, dataIndex: 'class-name', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            },
	            {	
	            	header: "功能描述", width: 150, sortable: true, dataIndex: 'description', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            }
	        ],
			viewConfig: {
				forceFit: true
			},
			listeners: {
				activate: function(c){
					if (!c.loaded) {
						c.store.load();
						c.loaded = true;
					}
				}
			},
			bbar: [{
				cls: 'x-btn-text-icon',
				icon: '../images/icons/add.gif',
				text: '添加',
				handler: function(button, e){
					var panel = funcPanel;
					panel.store.add(new panel.store.recordType({}));
	            	panel.startEditing(panel.store.getCount()-1, 0);
				}
			},{
				cls: 'x-btn-text-icon',
				icon: '../images/icons/delete.gif',
				text: '删除',
				handler: function(button, e){
					var panel = funcPanel;
					var cell = panel.getSelectionModel().getSelectedCell();
					if (!cell || !window.confirm("确实要删除选中的项吗？")) return;
					panel.store.removeAt(cell[0]);
				}
			},
			'->',
			{
				cls: 'x-btn-text-icon',
				icon: '../images/tool16/save_edit.gif',
				text: '保存',
				handler: function(button, e){
					funcPanel.stopEditing();
					_this.saveFunctions(funcPanel);
				}
			},{
				cls: 'x-btn-text-icon',
				icon: '../images/tool16/refresh.gif',
				text: '重载',
				handler: function(){
					var panel = funcPanel;
					panel.store.reload();
				}
			}]
		});
		
		var convPanel = new Ext.Panel({
			title: '转换函数',
		    bodyStyle: 'padding:5px 5px 0; text-align:left; background-color:white;',
			autoScroll: true, border: false,
			style: Ext.isGecko?'margin: auto;':'',
			layout: 'anchor',
			defaults: { frame: true, anchor: '-20', style:'margin-bottom:5px;' },
			listeners: {
				activate: function(c){
					if (!c.loaded) {
						_this.loadConvertion(c);
						c.loaded = true;
					}
				}
			},
			bbar: [{
				cls: 'x-btn-text-icon',
				icon: '../images/icons/add.gif',
				text: '添加类别',
				handler: function(button, e){
					var panel = convPanel;
					var uuid = new UUID().id;
					convPanel.add(_this.createConvGrid({ id: uuid, items: [] }));
					convPanel.doLayout();
				}
			},
			'->',
			{
				cls: 'x-btn-text-icon',
				icon: '../images/tool16/saveall_edit.gif',
				text: '保存',
				tooltip: '全部保存',
				handler: function(button, e){
					funcPanel.stopEditing();
					_this.saveConvertion(convPanel);
				}
			},{
				cls: 'x-btn-text-icon',
				icon: '../images/tool16/refresh.gif',
				text: '重载',
				handler: function(){
					_this.loadConvertion(convPanel);
				}
			}]
		});
		
		var mapPanel = new Ext.Panel({
			title: '故障映射',
		    bodyStyle: 'padding:5px 5px 0; text-align:left; background-color:white;',
			autoScroll: true, border: false,
			style: Ext.isGecko?'margin: auto;':'',
			layout: 'anchor',
			defaults: { frame: true, anchor: '-20', style:'margin-bottom:5px;' },
			listeners: {
				activate: function(c){
					if (!c.loaded) {
						_this.loadMapping(c);
						c.loaded = true;
					}
				}
			}
		});
		
		var addMenu = new Ext.menu.Menu({
			id: 'addMenu',
			items: [],
			listeners: {
				click: function(menu, item, e){
					if (!item.name) return;
					var panel = menu.activePanel;
					panel.store.add(new panel.store.recordType({'fault-spot': item.name}));
	            	panel.startEditing(panel.store.getCount()-1, 0);
				}
			}
		});
		
		var viewport = new Ext.Viewport({
			stateful: false,
			layout: 'fit',
			items: [
				new Ext.TabPanel({
					activeTab: 0,
					border: true,
					bodyStyle: 'text-align:left;',
					deferredRender: true,
					items: [formPanel, funcPanel, convPanel, mapPanel]
				})
			]
		});
		
	},

	createConvGrid: function(data) {
		var _this = this;
		var gridPanel = new Ext.grid.EditorGridPanel({
			itemId: data.id,
			title: data.description,
			collapsible: true, collapseFirst: false, collapsed: data.collapsed,
			columnLines: true, loadMask: true,
			autoHeight: true, boxMaxHeight: 200,
			store: new Ext.data.Store({
				//url: 'component_ctrl.jsp',
				data: data,
				reader: convReader,
				baseParams: {	operation:'loadFunctions', component: component},
	            sortInfo: {field: 'func-name', direction: "ASC"},
				listeners: {
					loadexception: function(proxy, obj, response){ _this.showException(response); }
				}
			}),
            columns: [
	            {
	            	header: "函数名称", width: 60, sortable: true, dataIndex: 'func-name', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            },
	            {
	            	header: "功能描述", width: 150, sortable: true, dataIndex: 'description', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            }
	        ],
			viewConfig: {
				forceFit: true
			},
			listeners: {
				render: function(c) {
					c.header.setStyle('cursor', 'default');
					c.header.on("dblclick", function(){ c.toggleCollapse(); });
				}
			},
			tbar: [
			'前缀: ',
			{
				xtype: 'textfield',
				itemId: 'prefix',
				width: 40,
				value: data.prefix
			},
			'&nbsp;类名称: ',
			{
				xtype: 'textfield',
				itemId: 'class-name',
				width: 200,
				value: data['class-name']
			},
			'&nbsp;类别描述: ',
			{
				xtype: 'textfield',
				itemId: 'description',
				width: 200,
				value: data.description,
				listeners: {
					change: function(field, newVal, oldVal){ gridPanel.setTitle(newVal); }
				}
			}],
			tools: [{
				id: 'plus',
				qtip: '添加函数',
				handler: function(e, toolEl, panel, tc){
					e.stopEvent();
					if (panel.collapsed) panel.expand();
					var cell = panel.getSelectionModel().getSelectedCell();
					var row = cell ? cell[0]+1 : panel.store.getCount();
					panel.store.insert(row, new panel.store.recordType({}));
	            	panel.startEditing(row, 0);
				}
			},{
				id: 'minus',
				qtip: '删除函数',
				handler: function(e, toolEl, panel, tc){
					e.stopEvent();
					if (panel.collapsed) panel.expand();
					var cell = panel.getSelectionModel().getSelectedCell();
					if (!cell || !window.confirm("确实要删除选中的函数吗？")) return;
					panel.stopEditing();
					panel.store.removeAt(cell[0]);
				}
			},{
				id: 'save',
				qtip: '保存',
				handler: function(e, toolEl, panel, tc){
					panel.stopEditing();
					_this.saveConvertion(panel);
				}
			},{
				id: 'close',
				qtip: '删除本组',
				handler: function(e, toolEl, panel, tc){
					if (!confirm("确认要删除本组所有函数吗？")) return;
					
					Ext.getBody().mask('正在删除扩展分组...', 'x-mask-loading');
					Ext.Ajax.request({
						method: 'POST', 
						url: 'component_ctrl.jsp',
						params: {operation:"removeConvertion", component:component, groupId: panel.itemId },
						callback: function(options, success, response){
							Ext.getBody().unmask();
							if (!success) {
								_this.showException(response);
							}
							else {
								panel.destroy();
							}
						}
					});
				}
			}]
		});
		
		return gridPanel;
	},
	
	createMappingGrid : function(options){
		var _this = this;
		var errorPanel = new Ext.grid.EditorGridPanel({
			title: options.title+'('+options.id+')',
			autoScroll: true, loadMask: true,
			id: options.id,
			collapsible: true, collapseFirst: false, collapsed: options.collapsed,
			columnLines: true, animCollapse: false,
			height: 240,
			border: false, enableHdMenu: false,
			store: new Ext.data.GroupingStore({
				proxy: new Ext.data.HttpProxy({
					url: 'component_ctrl.jsp'
				}),
				reader: mapingReader,
		        baseParams: {component: component, operation: 'loadErrorMap', topic: options.id},
	            sortInfo: {field: 'status', direction: "ASC"},
	            groupField: 'fault-spot',
				listeners: {
					add : function(store) {
						store.changed=true;
					},
					remove : function(store) {
						store.changed=true;
					},
					update : function(store) {
						store.changed=true;
					},
					loadexception: function(proxy, obj, response){ _this.showException(response); }
				}
			}),
            columns: [
	            {
	            	header: "状态码", width: 60, sortable: true, dataIndex: 'status', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({ allowBlank:false }))
	            },
	            {
	            	header: "响应代码", width: 60, sortable: true, dataIndex: 'result', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({ allowBlank:false }))
	            },
	            {
	            	header: "响应描述", width: 150, sortable: true, dataIndex: 'description', 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            },
	            {
	            	header: "故障点", width: 60, sortable: true, dataIndex: 'fault-spot', hidden:true, 
	            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
	            }
	        ],
	        view: new Ext.grid.GroupingView({
	            forceFit: true,
	            groupTextTpl: '{text} ({[values.rs.length]} 条映射)'
	        }),
			listeners: {
				render: function(c) {
					c.header.setStyle('cursor', 'default');
					c.header.on("dblclick", function(){ c.toggleCollapse(); });
				},
				expand: function(c){
					if (!c.loaded) {
						c.store.load();
						c.loaded = true;
					}
				}
			},
			tools: [{
				id: 'plus',
				qtip: '添加映射',
				handler: function(e, toolEl, panel, tc){
					if (panel.collapsed) panel.expand();
					var menu = Ext.menu.MenuMgr.get('addMenu');
					menu.activePanel = panel;
					menu.showAt(e.getXY());
				}
			},{
				id: 'minus',
				qtip: '删除映射',
				handler: function(e, toolEl, panel, tc){
					if (panel.collapsed) panel.expand();
					var cell = panel.getSelectionModel().getSelectedCell();
					if (!cell || !window.confirm("确实要删除选中的映射吗？")) return;
					panel.stopEditing();
					panel.store.removeAt(cell[0]);
				}
			},{
				id: 'save',
				qtip: '保存',
				handler: function(e, toolEl, panel, tc){
					panel.stopEditing();
					_this.saveMapping(panel);
				}
			},{
				id: 'refresh',
				qtip: '重载',
				handler: function(e, toolEl, panel, tc){
					panel.store.reload();
					panel.store.changed = false;
				}
			},{
				id: 'close',
				qtip: '清除',
				handler: function(e, toolEl, panel, tc){
					if (!confirm("确认要删除此组件所有状态映射吗？")) return;
					
					panel.bwrap.mask('正在清除映射...', 'x-mask-loading');
					Ext.Ajax.request({
						method: 'POST', 
						url: 'component_ctrl.jsp',
						params: {operation:"removeMapping", component:component, topic: panel.id },
						callback: function(options, success, response){
							panel.bwrap.unmask();
							if (!success) {
								_this.showException(response);
							}
							else {
								panel.store.removeAll();
							}
						}
					});
				}
			},{
				id: 'maximize',
				qtip: '放大',
				handler: function(e, toolEl, panel, tc){
					if (panel.collapsed) panel.expand();
					panel.setHeight(panel.getHeight() * 2);
				}
			}]
		});
		return errorPanel;
	},
	
	loadAttrib : function(formPanel){
		formPanel.getForm().setValues(properties);
		Ext.getCmp('component-classname').setValue(properties['component-classname']);
		Ext.getCmp('bootstrap-classname').setValue(properties['bootstrap-classname']);
		if (!!properties['component-classpath'])
			Ext.getCmp('compGrid').store.loadData(properties);
		if (!!properties['bootstrap-classpath'])	
			Ext.getCmp('bootGrid').store.loadData(properties);
	},
	
	loadConvertion : function(convPanel){
		var _this = this;
		convPanel.setIconClass('x-icon-loading');
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"getConvExtension", component:component },
			callback: function(options, success, response){
				convPanel.setIconClass('');
				if (!success) {
					_this.showException(response);
				}
				else {
					var result = Ext.decode(response.responseText);
					convPanel.suspendEvents();
					try {
						convPanel.removeAll(true);
						Ext.each(result.categories, function(rec){
							convPanel.add(_this.createConvGrid(rec));
						});
					}
					finally {
						convPanel.resumeEvents();
						convPanel.doLayout();
					}
				}
			}
		});
		
	},
		
	loadMapping : function(mapPanel){
		var _this = this;
		mapPanel.setIconClass('x-icon-loading');
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"getComponents", component:component, type:'binding-component' },
			callback: function(options, success, response){
				mapPanel.setIconClass('');
				if (!success) {
					_this.showException(response);
				}
				else {
					var result = Ext.decode(response.responseText);
					mapPanel.suspendEvents();
					try {
						mapPanel.removeAll(true);
						Ext.each(result.items, function(rec){
							mapPanel.add(_this.createMappingGrid({title: rec.description, id:rec.name, collapsed: false }));
						});
					}
					finally {
						mapPanel.resumeEvents();
						mapPanel.doLayout();
						mapPanel.items.each(function(grid){ grid.collapse(); });
					}
				}
			}
		});
		
		var menu = Ext.menu.MenuMgr.get('addMenu');
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"getComponents", component:component },
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (success) {
					var result = Ext.decode(response.responseText);
					menu.removeAll(true);
					menu.addMenuItem({
						text: '<b style="cursor:default;">选择故障点</b>',
						disabled: true
					});
					Ext.each(result.items, function(rec){
						menu.addMenuItem({
							text: rec.description,
							name: rec.name
						});
						
					});
				}
			}
		});
	},
		
	saveAttrib : function(formPanel){
		var _this = this;

		var v = formPanel.getForm().getValues();
		var properties = window.properties;
		properties.name = v.name;
		properties.description = v.description;
		properties['component-classname'] = Ext.getCmp('component-classname').el.dom.value;
		properties['bootstrap-classname'] = Ext.getCmp('bootstrap-classname').el.dom.value;
		
		properties['component-classpath'] = [];
		Ext.getCmp('compGrid').store.each(function(rec){
			properties['component-classpath'].push(rec.get('path'));
		});
		properties['bootstrap-classpath'] = [];
		Ext.getCmp('bootGrid').store.each(function(rec){
			properties['bootstrap-classpath'].push(rec.get('path'));
		});


		Ext.getBody().mask('正在保存组件...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"save", component:component, data: Ext.encode(properties) },
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				}
			}
		});
	},
	
	saveConvertion: function(panel){
		var _this = this;
		
		var getCategory = function(grid) {
			var toolbar = grid.getTopToolbar();
			var prefix = toolbar.items.get('prefix').getValue();
			var className = toolbar.items.get('class-name').getValue();
			var description = toolbar.items.get('description').getValue();
			if (prefix=='' || className=='' || description=='') {
				if (grid.collapsed) grid.expand();
				throw { message: '前缀、类名称、描述均不能为空' };
			}
			
			var category= { 
				collapsed: grid.collapsed,
				id: grid.itemId, prefix: prefix, 
				'class-name': className, description: description, 
				items: [] 
			};
			grid.store.each(function(rec){
				if (rec.isValid()) {
					category.items.push(rec.data); 
				}
				else {
					var row = grid.store.indexOf(rec);
					grid.getSelectionModel().select(row, 0);
					throw { message: '非法的函数名称' };
				}
			});
			return category;
		};
		
		var extension = { categories: [] };
		try {
			if (panel.store) {
				extension.categories.push(getCategory(panel));
			}
			else {
				panel.items.each(function(p) {
					extension.categories.push(getCategory(p));
				});
			}
		}
		catch (e) {
			alert(e.message);
			return;
		}
		
		if (extension.categories.length > 0) {
			panel.bwrap.mask('正在保存扩展函数...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'component_ctrl.jsp',
				params: {operation:"saveConvertion", component:component, data: Ext.encode(extension) },
				callback: function(options, success, response){
					panel.bwrap.unmask();
					if (!success) {
						_this.showException(response);
					}
					else {
					}
				}
			});
		}
	},
		
	saveFunctions: function(funcGrid){
		var _this = this;
		var store  = funcGrid.store;
		if (store.removed.length>0 || store.getModifiedRecords().length>0) {
			var array=[];
			store.each(function(rec){ if (rec.isValid()) array.push(rec.data); });
			funcGrid.bwrap.mask('正在保存扩展函数...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'component_ctrl.jsp',
				params: {operation:"saveFunctions", component:component, data: Ext.encode(array) },
				callback: function(options, success, response){
					funcGrid.bwrap.unmask();
					if (!success) {
						_this.showException(response);
					}
					else {
						store.commitChanges();
					}
				}
			});
		}
	},
		
	saveMapping: function(mappingGrid){
		var _this = this;
		var store  = mappingGrid.store;
		if (store.changed) {
			var array =[];
			
			for (var r=0,len=store.getCount(); r<len; r++){
				var rec = store.getAt(r);
				if (rec.isValid()) {
					array.push(rec.data);
				}
				else {
					mappingGrid.expand();
					mappingGrid.getSelectionModel().select(r, 0);
					alert("状态码和响应码不能为空");
					return false;
				}
			}
			
			mappingGrid.bwrap.mask('正在保存组件故障映射表...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'component_ctrl.jsp',
				params: {operation:"saveMapping", component:component, topic: mappingGrid.id, data: Ext.encode(array) },
				callback: function(options, success, response){
					mappingGrid.bwrap.unmask();
					if (!success) {
						_this.showException(response);
					}
					else {
						store.commitChanges();
						store.changed = false;
					}
				}
			});
		}
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
};

}();

Ext.onReady(Viewer.init, Viewer, true);

Ext.ux.GroupingView = Ext.extend(Ext.grid.GroupingView, {

    // private
    interceptMouse : function(e){
        var hd = e.getTarget('.x-grid-group-hd', this.mainBody);
       
        if (e.getTarget().nodeName=='INPUT') {
        	return;
        }
        if(hd){
            e.stopEvent();
            this.toggleGroup(hd.parentNode);
        }
    }
});