Ext.BLANK_IMAGE_URL='../images/s.gif';

var Viewer = function(){
//变量
var layout;
var schemes = {};
var index = 1;

return {

	init : function(){
		var _this = this;

		Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
		//Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		Ext.QuickTips.init();
		
		var textEditor = new Ext.grid.GridEditor(new Ext.form.TextField({
			
		}));

		var formPanel = new Ext.form.FormPanel({
			title: '基本属性',
		    labelWidth: 130,
		    url: 'binding_ctrl.jsp',
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
				height: 160,
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
				height: 140,
				frame: true, enableHdMenu: false,
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
		
		//-----------------------------------------------------------------------------------------------
		var codecPanel = new Ext.Panel({
			title: '编码/解码',
			autoScroll: true,
			bodyStyle: 'padding:5px;',
			layout: 'anchor',
			defaults: {anchor: '-18', style: 'margin-bottom:5px;'},
			items: [{
				xtype: 'form',
				title: '参数',
				itemId: 'param-panel',
				frame: true,
				labelWidth: 90,
				defaults: {anchor:"100%"},
				items: [{
					xtype: 'fieldset',
					//title: '交易码',
					autoHeight: true,
					border: true,
					layout: 'column',
					items: [{
						xtype: 'panel',
						columnWidth: 0.3,
						autoHeight: true,
						border: false,
						style: 'margin-right:5px;',
						labelWidth: 78,
						defaults: {anchor: '100%'},
						defaultType: 'textfield',
		                layout: 'form',
		                items: [{
				            fieldLabel : '交易码起始位',
							name : 'tx-start',
							xtype : 'numberfield',
							allowDecimals: false
						}]
					},{
						xtype: 'panel',
						columnWidth: 0.2,
						autoHeight: true,
						border: false,
						style: 'margin-right:5px;',
						labelWidth: 50,
						defaults: {anchor: '100%'},
						defaultType: 'textfield',
		                layout: 'form',
						items: [{
				            fieldLabel : '终止位',
							name : 'tx-end',
							xtype : 'numberfield',
							allowDecimals: false
						}]
					},{
						xtype: 'panel',
						columnWidth: 0.5,
						autoHeight: true,
						border: false,
						style: 'margin-left:5px; border-left-width:1px',
						labelWidth: 80,
						defaults: {anchor: '100%'},
						defaultType: 'textfield',
		                layout: 'form',
		                items: [{
				            fieldLabel : '&nbsp;&nbsp;交易码XPath',
							name : 'tx-code',
							xtype : 'textfield'
						}]
					}]
				},{
					xtype: 'panel',
					autoHeight: true,
					border: false,
					layout: 'column',
					items: [{
						xtype: 'panel',
						columnWidth: 0.5,
						autoHeight: true,
						border: false,
						style: 'margin-right:5px;',
						labelWidth: 90,
						defaults: {anchor: '100%'},
						defaultType: 'textfield',
		                layout: 'form',
		                items: [{
				            fieldLabel : '状态码XPath',
							name : 'status',
							xtype : 'textfield'
		                }]
					},{
						xtype: 'panel',
						columnWidth: 0.5,
						autoHeight: true,
						border: false,
						style: 'margin-left:5px;',
						labelWidth: 80,
						defaults: {anchor: '100%'},
						defaultType: 'textfield',
		                layout: 'form',
		                items: [{
				            fieldLabel : '&nbsp;&nbsp;成功状态码',
							name : 'success-code',
							xtype : 'textfield'
		                }]
					}]
				},{
					fieldLabel : '状态信息XPath',
					name : 'status-text',
					xtype : 'textfield'
				}]
			}],
			listeners: {
				activate: function(c){
					if (!c.loaded) {
						_this.loadCodec(c);
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
					if (formPanel.getForm().isValid())
						_this.saveCodec(codecPanel);
				}
			},{
		        text: '重置',
				handler: function(){
					_this.loadCodec(codecPanel);
				}
			}]
		});
		
		var statPanel = new Ext.form.FormPanel({
			title: '响应代码',
		    labelWidth: 130,
		    url: 'binding_ctrl.jsp',
		    frame: false,
		    bodyStyle:'padding:5px 5px 0; text-align:left; background-color:white;',
			autoScroll:true, border: false,
			style: Ext.isGecko?'margin: auto;':'',
			layout: 'fit',
			defaults: {anchor:"-18, -10"},
			items: [{
				xtype: 'editorgrid',
				title: '状态代码表',
				itemId: 'statGrid',
				margins: {top:5, right:5, bottom:5, left:5},
				columnLines: true,
				height: 100,
				frame: true, loadMask: true,
				store: new Ext.data.Store({
					proxy: new Ext.data.HttpProxy({
					    api: {
					        read : 'component_ctrl.jsp?operation=loadStatus',
					        create : 'component_ctrl.jsp?operation=insertStatus',
					        update: 'component_ctrl.jsp?operation=updateStatus',
					        destroy: 'component_ctrl.jsp?operation=deleteStatus'
					    }
					}),
					reader: new Ext.data.JsonReader({
						successProperty: 'success',
						totalProperty : "count",
						id : 'status',
				        root: 'items',
				        fields: [
				           {name: 'status', allowBlank:false},
				           {name: 'text'}
				        ]
			        }),
			        writer: new Ext.data.JsonWriter({
					    encode: true,
					    writeAllFields: true
					}),
					autoSave: false,
					baseParams: {component: component},
					sortInfo: {
						field : 'status',
						direction : 'ASC' 
					},
					listeners : {
						exception : function(proxy, type, action, options, response, arg) {
				            if (type === 'remote') {
				                Ext.Msg.show({
				                    title: 'REMOTE EXCEPTION',
				                    msg: response.message,
				                    icon: Ext.MessageBox.ERROR,
				                    buttons: Ext.Msg.OK
				                });
				            }
				            else {
								_this.showException(response);
				            }
				        },
				        write : function(store, action, result, response, rs) {
				        	statPanel.getComponent('statGrid').save.setDisabled(store.getModifiedRecords().length==0);
				        }
					}
				}),
                columns: [
		            {
		            	header: "状态码", width: 60, sortable: true, dataIndex: 'status', 
		            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({allowBlank:false}))
		            },
		            {
		            	header: "状态信息", width: 150, sortable: true, dataIndex: 'text', 
		            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
		            }
		        ],
				viewConfig: {
					forceFit: true
				},
				tools: [{
					id: 'plus',
					handler: function(e, toolEl, panel, tc){
						panel.store.add(new panel.store.recordType({}));
		            	panel.startEditing(panel.store.getCount()-1, 0);
					}
				},{
					id: 'minus',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						if (!cell || !window.confirm("确实要删除选中的项吗？")) return;
						panel.store.removeAt(cell[0]);
					}
				}]
			}],
			listeners: {
				activate: function(c){
					if (!c.loaded) {
						c.getComponent('statGrid').store.load();
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
					var store  = statPanel.getComponent('statGrid').store;
					if (store.removed.length>0 || store.getModifiedRecords().length>0) {
						var array=[];
						store.each(function(rec){ if (rec.isValid()) array.push(rec.data); });
						Ext.getBody().mask('正在保存组件状态代码表...', 'x-mask-loading');
						Ext.Ajax.request({
							method: 'POST', 
							url: 'component_ctrl.jsp',
							params: {operation:"saveStatus", component:component, data: Ext.encode(array) },
							callback: function(options, success, response){
								Ext.getBody().unmask();
								if (!success) {
									_this.showException(response);
								}
								else {
									store.commitChanges();
								}
							}
						});
					}
				}
			},{
		        text: '载入',
				handler: function(){
					statPanel.getComponent('statGrid').store.load();
				}
			}]
		});
		
		var portPanel = new Ext.Panel({
			title: '传输端点',
			autoScroll: true,
			bodyStyle: 'background-color:transparent;',
			layout: 'border',
			items: [{
				region: 'center',
				xtype: 'panel',
				title: '',
				id: 'portPanel',
				autoHeight : true,
				border: false,
				autoScroll: true, frame: false,
				layout: 'anchor',
				margins: {top:5, right:5, bottom:5, left:5}
			}],
			listeners: {
				activate: function(c){
					if (!c.loaded) {
						_this.loadPort(c);
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
					if (formPanel.getForm().isValid())
						_this.savePort(portPanel);
				}
			},{
		        text: '重置',
				handler: function(){
					_this.loadPort(portPanel);
				}
			}]
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
					items: [formPanel, portPanel, codecPanel, statPanel]
				})
			]
		});
	},
	
	createPortPanel : function(options){
		var formpanel = new Ext.form.FormPanel({
			title: options.location,
			autoHeight: true,
			frame: true,
			closable: true,
			collapsible: true,
			defaults: {anchor:'100%'}
		});
		
		var fieldset=null;
		var loc = options.location || "";
		var scheme = loc.substring(0, loc.indexOf("://"));
		
		var properties = schemes[scheme];
		if (properties && properties.items) {
			properties.itemId = 'params-set';
			properties.title = '';
			properties.scheme = scheme;
			fieldset = Ext.ComponentMgr.create(properties, 'fieldset');
			formpanel.add(fieldset);
			formpanel.doLayout();
		}
		
		if (fieldset!=null && options.params) {
			var loadItems = function(parentEl, items) {
				items.each(function(item){
					if (item.isFormField) {
						if (item.tag == 'attribute') {
							if (!item.readOnly) item.setValue(parentEl.getAttribute(item.name));
						}
						else {
							var el = XDom.firstElement(parentEl, item.name);
							if (el && !item.readOnly) item.setValue(el.text||el.textContent);
						}
					}
					else {
						var el = XDom.firstElement(parentEl, item.name);
						while (el && el.getAttribute('used')) {
							el = XDom.nextElement(el, item.name);
						}
						if (el) {
							loadItems(el, item.items);
							el.setAttribute('used', true);
						}
					}
				});
			};
			var doc = XDom.parseXml(options.params);
			loadItems(doc.documentElement, fieldset.items);
			
		}
		return formpanel;
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
	
	loadPort : function(portPanel){
		var _this = this;
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"loadPort", component:component },
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				}
				else {
					var result = Ext.decode(response.responseText);
					schemes = result.schemes;
					var createRegex=function(item){
						if (item.regex) item.regex = new RegExp(item.regex);
						if (item.items) Ext.each(item.items, createRegex);
					}
					for (var a in schemes) {
						Ext.each(schemes[a].items, createRegex);
					}
					
					var portPanel = Ext.getCmp('portPanel');
					portPanel.removeAll(true);
					Ext.each(result.locations, function(rec){
						portPanel.add(_this.createPortPanel({location: rec[1], params: rec[2]}));
						portPanel.doLayout();
					});
				}
			}
		});
	},
		
	loadCodec : function(codecPanel){
		var _this = this;

		var load = function(codec) {
			var c = codec;
			var encodePanel = Ext.getCmp('encodeForm');
			var f = encodePanel.form.findField('encoder');
			f.setValue(c.xml2raw ? c.xml2raw['encoder'] || '' : '');
			var g = encodePanel.getComponent('raw-handlers');
			g.store.loadData(c.xml2raw ? c.xml2raw['raw-handlers'] || [] : []);
			var g = encodePanel.getComponent('xml-handlers');
			g.store.loadData(c.xml2raw ? c.xml2raw['xml-handlers'] || [] : []);
			
			var decodePanel = Ext.getCmp('decodeForm');
			var f = decodePanel.form.findField('decoder');
			f.setValue(c.raw2xml ? c.raw2xml['decoder'] || '' : '');
			var g = decodePanel.getComponent('raw-handlers');
			g.store.loadData(c.raw2xml ? c.raw2xml['raw-handlers'] || [] : []);
			var g = decodePanel.getComponent('xml-handlers');
			g.store.loadData(c.raw2xml ? c.raw2xml['xml-handlers'] || [] : []);
		};
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"loadCodec", component:component },
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				}
				else {
					var result = Ext.decode(response.responseText);
					var params = result.codec.params;
					codecPanel.getComponent('param-panel').form.setValues(params);
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
	
	saveCodec : function(codecPanel){
		var _this = this;

		var properties = {};
		var c = properties.codec = {};
		var paramPanel = codecPanel.getComponent('param-panel');
		c.params = paramPanel.form.getValues();

		Ext.getBody().mask('正在保存组件编码/解码设置...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"saveBinding", component:component, data: Ext.encode(properties) },
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				}
			}
		});
	},
	
	savePort : function(portPanel){
		var _this = this;
		var properties = {transports: [], locations: []};
		
		var saveItems = function(parentEl, items){
			var doc = parentEl.ownerDocument;
			items.each(function(item){
				if (item.isFormField) {
					if (item.tag == 'attribute') {
						parentEl.setAttribute(item.name, item.getValue());
					}
					else {
						var el = doc.createElement(item.name);
						parentEl.appendChild(el);
						var value = (item.isXType('combo') && !item.forceSelection ? item.getRawValue() : item.getValue());
						el.appendChild(doc.createTextNode(value));
					}
				}
				else {
					var el = doc.createElement(item.name);
					parentEl.appendChild(el);
					if (item.items) saveItems(el, item.items);
				}
			});
		};
		
		var coll = Ext.getCmp('portPanel').items;
		var url = Ext.getCmp('portPanel').getComponent(0).title;

		for (var i=0,count=coll.getCount(); i<count; i++){
			var panel = coll.itemAt(i);
			if (!panel.getForm().isValid()) {
				panel.expand();
				return false;
			}
			var params = '';
			var fieldset = panel.getComponent('params-set');
			if (fieldset!=null) {
				var xmldoc = XDom.createDocument();
				var rootEl = xmldoc.createElement('params');
				xmldoc.appendChild(rootEl);
				saveItems(rootEl, fieldset.items);
				params = XDom.innerXML(xmldoc);
			}
			properties.locations.push(['', url, params]);
		}

		Ext.getBody().mask('正在保存组件绑定设置...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'POST',
			url: 'component_ctrl.jsp',
			params: {operation:"saveBinding", component:component, data: Ext.encode(properties) },
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