//common_v.js
Ext.BLANK_IMAGE_URL='../images/s.gif';

var Viewer = function(){

var layout;

return {

	init : function(){
		var _this = this;

		Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
		//Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		Ext.QuickTips.init();
		
		var formPanel = new Ext.form.FormPanel({
			title: '基本属性',
		    labelWidth: 130,
		    frame: false,
		    bodyStyle:'padding:5px 5px 0; text-align:left; background-color:white;',
			autoScroll:true, border: false,
			style: Ext.isGecko?'margin: auto;':'',
			layout: 'form',
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
					forceSelection:true, editable:false, disabled:true,
					store: [['binding-component', '绑定组件'], ['service-engine', '引擎组件']]
				}]
			},{
				xtype: 'editorgrid',
				title: '组件类',
				id: 'compGrid',
				autoHeight: true,
				frame: true, enableHdMenu: false,
				store: new Ext.data.Store({
					reader: new Ext.data.ArrayReader({
				        fields: [
				           {name: 'path'}
				        ],
				        root: 'component-classpath'
			        })
				}),
                columns: [
		            {header: "类路径", width: 60, dataIndex: 'path', editor: new Ext.grid.GridEditor(new Ext.form.TextField({})) }
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
					store: ['com.sanxing.statenet.binding.AdapterComponent']
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
				autoHeight: 140,
				frame: true, enableHdMenu: false, collapsible: true, 
				style: 'margin-bottom:5px;',
				store: new Ext.data.Store({
					reader: new Ext.data.ArrayReader({
				        fields: [
				           {name: 'path'}
				        ],
				        root: 'bootstrap-classpath'
			        })
				}),
                columns: [
		            {header: "类路径", width: 60, dataIndex: 'path', editor: new Ext.grid.GridEditor(new Ext.form.TextField({})) }
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
					store: ['com.sanxing.statenet.binding.BootstrapImpl']
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
				title: '共享类库',
				id: 'sharelibGrid',
				autoHeight: true,
				frame: true, enableHdMenu: false,
				style: 'margin-bottom:5px;',
				store: new Ext.data.Store({
					reader: new Ext.data.JsonReader({
				        fields: [
				           {name: 'lib-name', type: "string" }, {name: "version"}
				        ],
				        root: 'shared-library'
			        })
				}),
                columns: [
		            {
		            	header: "类库名称", width: 60, dataIndex: 'lib-name', 
		            	editor: new Ext.grid.GridEditor(new Ext.form.ComboBox({
							triggerAction: 'all',
							forceSelection:true, editable:false, 
							valueField: 'name', displayField: 'description',
						    store: new Ext.data.JsonStore({
						        url: "component_ctrl.jsp",
						    	root: 'items', 
						    	fields: [{name:'name', type:'string'}, {name:'description', type:'string'}, {name:'version'}],
						        baseParams: {operation:'getShareLibs' },
						        sortInfo: {field: 'description', direction: 'ASC'},
						        listeners: {
						        	'loadexception': function(proxy, obj, response){ _this.showException(response); },
									'select': function(sender, record, index){  }
						        }
						    })
		            	})) 
		            },
		            {
		            	header: "版本", width: 60, dataIndex: 'version', 
		            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
		            }
		        ],
				viewConfig: {
					forceFit: true
				},
				tools: [{
					id: 'plus',
					qtip: '添加共享库',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						var row = cell ? cell[0]+1 : panel.store.getCount();
						panel.store.insert(row, new panel.store.recordType({}));
		            	panel.startEditing(row, 0);
					}
				},{
					id: 'minus',
					qtip: '删除共享库',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						if (!cell || !window.confirm("确实要删除选中的共享类库吗？")) return;
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
			buttonAlign: 'right',
			buttons: [{
		        text: '保存',
		        disabled: properties.readOnly,
				handler: function(){
					if (!formPanel.getForm().isValid()) return;
					_this.saveAttr(formPanel);
				}
			},{
		        text: '重置',
				handler: function(){
					_this.loadAttrib(formPanel);
				}
			}]
		});
		
		var paramPanel = new Ext.form.FormPanel({
			title: '运行参数',
		    labelWidth: 130,
		    frame: false,
		    bodyStyle:'padding:5px 5px 0; text-align:left; background-color:white;',
			autoScroll:true, border: false,
			style: Ext.isGecko?'margin: auto;':'',
			layout: 'fit',
			items: [{
				xtype: 'propertygrid',
				title: '参数表',
				itemId: 'params',
				frame: true,
				margins: {top:5, right:5, bottom:5, left:5},
				tools: [{
					id: 'plus',
					qtip: '添加参数',
					handler: function(e, toolEl, panel, tc){
						if (panel.collapsed) panel.expand();
						var name = prompt("请输入属性名");
						if (!name) return;
						var values = panel.getSource();
						values[name]='';
						panel.setSource(values);
					}
				},{
					id: 'minus',
					qtip: '删除参数',
					handler: function(e, toolEl, panel, tc){
						if (panel.collapsed) panel.expand();
						var cell = panel.getSelectionModel().getSelectedCell();
						if (!cell || !window.confirm("确实要删除选中的参数吗？")) return;
						panel.stopEditing();
						panel.store.removeAt(cell[0]);
					}
				}],
				propertyNames: {
				},
				source: {}
			}],
			listeners: {
				activate: function(c){
					if (!c.loaded) {
						_this.loadParams(c);
						c.loaded = true;
					}
				}
			},
			footerCssClass: 'x-window-bc',
			buttonAlign: 'right',
			buttons: [{
		        text: '保存',
				ref : '../saveButton',
		        disabled: properties.readOnly,
				handler: function(){
					_this.saveParams(paramPanel);
				}
			},{
		        text: '载入',
				handler: function(){
					_this.loadParams(paramPanel);
				}
			}]
		});
		
		var loadAttrib = function(){
			formPanel.getForm().setValues(properties);
			Ext.getCmp('component-classname').setValue(properties['component-classname']);
			Ext.getCmp('bootstrap-classname').setValue(properties['bootstrap-classname']);
			Ext.getCmp('compGrid').store.loadData(properties);
			Ext.getCmp('bootGrid').store.loadData(properties);
			if (!!properties['shared-library'])	
				Ext.getCmp('sharelibGrid').store.loadData(properties);
		};
		
		var viewport = new Ext.Viewport({
			stateful: false,
			layout: 'fit',
			items: [
				new Ext.TabPanel({
					activeTab: 0,
					border: true,
					bodyStyle: 'text-align:left;',
					deferredRender: false,
					items: [formPanel, paramPanel]
				})
			]
		});
		loadAttrib();
	},
	
	loadAttrib : function(formPanel){
		formPanel.getForm().setValues(properties);
		Ext.getCmp('component-classname').setValue(properties['component-classname']);
		Ext.getCmp('bootstrap-classname').setValue(properties['bootstrap-classname']);
		if (!!properties['component-classpath'])
			Ext.getCmp('compGrid').store.loadData(properties);
		if (!!properties['bootstrap-classpath'])	
			Ext.getCmp('bootGrid').store.loadData(properties);
		if (!!properties['shared-library'])	
			Ext.getCmp('sharelibGrid').store.loadData(properties);
	},
		
	loadParams : function(paramPanel){
		var _this = this;

		paramPanel.setIconClass('x-icon-loading');
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"loadParams", component:component },
			callback: function(options, success, response){
				paramPanel.setIconClass('');
				if (!success) {
					_this.showException(response);
				}
				else {
					var result = Ext.decode(response.responseText);
					paramPanel.get('params').setSource(result);
				}
			}
		});
	},
		
	saveAttr : function(formPanel){
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
		properties['shared-library'] = [];
		Ext.getCmp('sharelibGrid').store.each(function(rec){
			properties['shared-library'].push([rec.get('lib-name'), rec.get('version')]);
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
	
	saveParams : function(paramPanel){
		var _this = this;

		var params = paramPanel.get('params').getSource();
		Ext.getBody().mask('正在保存组件参数...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"saveParams", component:component, data: Ext.encode(params) },
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
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
};

}();

Ext.onReady(Viewer.init, Viewer, true);