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
					store: ['com.sanxing.sesame.binding.AdapterComponent']
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
				autoHeight: true, 
				collapsible: true, collapseFirst: false, 
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
					store: ['com.sanxing.sesame.binding.BootstrapImpl']
				}],
				listeners: {
					render: function(c) {
						c.header.setStyle('cursor', 'default');
						c.header.on("dblclick", function(){ c.toggleCollapse(); });
					},
					expand: function(c){
						c.setHeight(140);
					}
				},
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
				collapsible: true, collapseFirst: false,
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
				listeners: {
					render: function(c) {
						c.header.setStyle('cursor', 'default');
						c.header.on("dblclick", function(){ c.toggleCollapse(); });
					}
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
		
		var protPanel = new Ext.Panel({
			title: '传输协议',
			autoScroll: true,
			bodyStyle: 'background-color:transparent; padding:5px 5px 5px 5px',
			layout: 'fit',
			items: [{
				xtype: 'editorgrid',
				title: '允许协议',
				id: 'protGrid',
				columnLines: true,
				height: 120,
				frame: true,
				margins: {top:5, right:5, bottom:5, left:5},
				store: new Ext.data.Store({
					reader: new Ext.data.ArrayReader({
				        fields: [
				           {name: 'scheme', allowBlank: false },
				           {name: 'class-name'},
				           {name: 'description'}
				        ],
				        root: 'schemes'
			        })
				}),
                columns: [
		            {
		            	header: "协议名称", width: 60, sortable: true, dataIndex: 'scheme', 
		            	editor: new Ext.grid.GridEditor(new Ext.form.ComboBox({
		            		store : protocols,
		            		triggerAction: 'all'
		            	}))
		            },
		            {
		            	header: "实现类", width: 100, sortable: true, dataIndex: 'class-name', 
		            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
		            },
		            {
		            	header: "描述", width: 150, sortable: true, dataIndex: 'description', 
		            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
		            }
		        ],
				viewConfig: {
					forceFit: true
				},
				listeners: {
					beforeedit: function(e){
						if (e.field=='class-name' && protocols.indexOf(e.record.get('scheme')) > -1) {
							alert('警告！平台已经注册此协议的实现类');
						}
					}
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
						_this.saveProtocols(protPanel);
				}
			},{
		        text: '重置',
				handler: function(){
					_this.loadPort(protPanel);
				}
			}]
		});
		
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
				            fieldLabel : '应答码XPath',
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
				            fieldLabel : '&nbsp;&nbsp;成功应答码',
							name : 'success-code',
							xtype : 'textfield'
		                }]
					}]
				},{
					fieldLabel : '应答信息XPath',
					name : 'status-text',
					xtype : 'textfield'
				}]
			},{
				xtype: 'panel',
				autoHeight: true,
				border: false,
				layout: 'column',
				items: [{
					xtype: 'form',
					title: '编码',
					id: 'encodeForm',
					columnWidth: .5,
					autoHeight: true,
					frame: true,
					style: 'margin-right:5px;',
					labelWidth: 70,
					defaults: {anchor: '100%'},
					defaultType: 'textfield',
	                layout: 'form',
	                items: [
					{
						fieldLabel: '&nbsp;编码器',
						name: 'encoder',
						labelStyle: 'font-weight:bold;',
						xtype: 'combo',
						triggerAction:'all',
						forceSelection:false, editable:true,
						store: ['com.sanxing.sesame.codec.impl.EncodeFSV', 'com.sanxing.sesame.codec.impl.EncodeXML']
			        }]
				},{
					xtype: 'form',
					title: '解码',
					id: 'decodeForm',
					columnWidth: .5,
					autoHeight: true,
					frame: true,
					style: 'margin-left:5px;',
					labelWidth: 70,
					defaults: {anchor: '100%'},
					defaultType: 'textfield',
	                layout: 'form',
	                items: [
					{
						fieldLabel: '&nbsp;解码器',
						name: 'decoder',
						labelStyle: 'font-weight:bold;',
						xtype: 'combo',
						triggerAction:'all',
						forceSelection:false, editable:true,
						store: ['com.sanxing.sesame.codec.impl.DecodeFSV', 'com.sanxing.sesame.codec.impl.DecodeXML']
			        }]
				}]
			},{
				xtype: 'panel',
				autoHeight: true,
				border: false,
				layout: 'fit',
				items: [{
					xtype: 'form',
					title: '错误码',
					id: 'faultForm',
					autoHeight: true,
					frame: true,
					style: 'margin-right:5px;',
					labelWidth: 100,
					defaults: {anchor: '100%'},
					defaultType: 'textfield',
	                layout: 'form',
	                items: [
					{
						fieldLabel: '错误码处理器',
						name: 'fault-handler'
			        }]
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

		var viewport = new Ext.Viewport({
			stateful: false,
			layout: 'fit',
			items: [
				new Ext.TabPanel({
					activeTab: 0,
					border: true,
					bodyStyle: 'text-align:left;',
					deferredRender: true,
					items: [formPanel, protPanel, codecPanel, statPanel]
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
			defaults: {anchor:'100%'},
			items: [{
				fieldLabel : '地址',
				xtype : 'textfield',
				name : 'location',
				itemId : 'location',
				allowBlank : false,
				value: options.location,
				listeners: {
					focus: function(field){
						var schemes = [];
						Ext.getCmp('protGrid').store.each(function(rec){
							schemes.push(rec.get('scheme'));
						});
						var regs = "^(("+schemes.join('|')+")://)"
										+ "(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // ftp的user@
										+ "(([0-9]{1,3}\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
										+ "|" // 允许IP和DOMAIN（域名）
										+ "([0-9a-z_!~*'()-]+\.)*" // 域名- www.
										+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\." // 二级域名
										+ "[a-z]{2,6})" // first level domain- .com or .museum
										+ "(:[0-9]{1,4})?" // 端口- :80
										+ "((/?)|" // a slash isn't required if there is no file name
										+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
						field.regex = new RegExp(regs);
						field.regexText = "非法的地址，地址示例: "+schemes.join('|')+"://hostname:port/[file[?params]]";
					},
					valid: function(field){
						formpanel.setTitle(field.getValue());
						var loc = field.getValue();
						var scheme = loc.substring(0, loc.indexOf("://"));
						var fieldset = formpanel.getComponent('params-set');
						if (fieldset==null || fieldset.scheme!=scheme) {
							formpanel.remove(fieldset, true);
							var properties = schemes[scheme];
						
							if (properties && properties.items) {
								properties.itemId = 'params-set';
								properties.title = '';
								properties.scheme = scheme;
								fieldset = Ext.ComponentMgr.create(properties, 'fieldset');
								formpanel.add(fieldset);
								formpanel.doLayout();
							}
						}
					}
				}
			}],
			tools: [{
				id: 'close',
				qtip: '删除',
				handler: function(e, toolEl, panel, tc){
					panel.ownerCt.remove(panel, true);
				}
			}]
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
		if (!!properties['shared-library'])	
			Ext.getCmp('sharelibGrid').store.loadData(properties);
	},
	
	loadPort : function(portPanel){
		var _this = this;
		portPanel.bwrap.mask("正在载入...");
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"loadAllowable", component:component },
			callback: function(options, success, response){
				portPanel.bwrap.unmask();
				if (!success) {
					_this.showException(response);
				}
				else {
					var result = Ext.decode(response.responseText);
					
					Ext.getCmp('protGrid').store.loadData(result);
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
			
			var decodePanel = Ext.getCmp('decodeForm');
			var f = decodePanel.form.findField('decoder');
			f.setValue(c.raw2xml ? c.raw2xml['decoder'] || '' : '');
		};
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"loadCodec", component:component },
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				} else {
					var result = Ext.decode(response.responseText);
					var params = result.codec.params;
					codecPanel.getComponent('param-panel').form.setValues(params);
					
					var faultHandler = result.codec['fault-handler'];
					Ext.getCmp('faultForm').form.findField('fault-handler').setValue(faultHandler);
					load(result.codec);
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
	
	saveCodec : function(codecPanel){
		var _this = this;

		var properties = {};
		var c = properties.codec = {xml2raw:{'xml-handlers':[], 'raw-handlers':[]}, raw2xml: {'raw-handlers':[], 'xml-handlers':[]}};
		var paramPanel = codecPanel.getComponent('param-panel');
		c.params = paramPanel.form.getValues();
		var encodeForm = Ext.getCmp('encodeForm');
		var f = encodeForm.form.findField('encoder');
		c.xml2raw['encoder'] = f.getValue();
		
		var decodeForm = Ext.getCmp('decodeForm');
		var f = decodeForm.form.findField('decoder');
		c.raw2xml['decoder'] = f.getValue();
		
		// 错误码处理器
		var faultForm = Ext.getCmp('faultForm');
		var g = faultForm.form.findField('fault-handler');
		c['fault-handler'] = g.getValue();
		
		codecPanel.bwrap.mask('正在保存组件编码/解码设置...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"saveBinding", component:component, data: Ext.encode(properties) },
			callback: function(options, success, response){
				codecPanel.bwrap.unmask();
				if (!success) {
					_this.showException(response);
				}
			}
		});
	},
	
	saveProtocols : function(portPanel){
		var _this = this;
		var properties = {protocols: [], locations: []};
		
		Ext.getCmp('protGrid').store.each(function(rec){
			properties.protocols.push([rec.get('scheme'), rec.get('class-name'), rec.get('description')]);
		});


		portPanel.bwrap.mask('正在保存组件协议设置...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"saveBinding", component:component, data: Ext.encode(properties) },
			callback: function(options, success, response){
				portPanel.bwrap.unmask();
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