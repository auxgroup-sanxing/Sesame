/**
 * 组件需要构造各自的扩展元素实例
 * 扩展数据通过父对象UnitBase传递给该实例
 * 名称为extdata,类型为哈希表
 * 形式为extdata = {name : value}
 */

// SOAP类型数组
var soapTypes = [
	['SOAP头', 'SoapHeader'],
	['SOAP Action', 'SOAPActionheader'],
	['WSA Action', 'WS-Addressing'],
	['HTTP头', 'httpHeader'],
	['SOAP BODY', 'SOAPBody']
];

var soapStore = new Ext.data.ArrayStore({
	fields: ['name', 'value'],
	data: soapTypes
});

var showField = function(field) {		
	field.hideLabel = false;
	field.allowBlank = false;
	var lable = field.getEl().dom.parentNode.parentNode.firstChild;
	lable.style.display = 'block';
	field.getEl().dom.parentNode.setAttribute('class','');
	field.getEl().dom.parentNode.style.paddingLeft = '135px!important;';
	field.getEl().dom.parentNode.firstChild.style.width = '100%';
	field.show();
};

var hideField = function(field) {
	field.allowBlank = true;
	field.hideLabel = false;
	var lable = field.getEl().dom.parentNode.parentNode.firstChild;
	lable.style.display = 'none';
	field.hide();
};

// 类型选择时触发该方法(向导模式)
var selectTypeGuide = function(obj, record, index) {
	var extPanel = obj.ownerCt;
	var grid = extPanel.ownerCt.get('operaGrid');
	// 扩展参数
	var data = 
		(obj.extdata == 'undefined' || !obj.extdata) ? '' : obj.extdata;
	var xpath = (!data) ? '':data.xpath;
	var head = (!data) ? '':data.head;
	
	obj.setValue(soapTypes[index][0]);
	obj.hiddenField.value = soapTypes[index][1];
	
	switch (index) {
		case 0:
			var field = extPanel.get('xpath');
			field.setValue(xpath);
			grid.getColumnModel().setEditable(2, true);
			hideField(extPanel.get('head'));
			showField(field);
			break;
		case 3:
			var field = extPanel.get('head');
			field.setValue(head);
			hideField(extPanel.get('xpath'));
			grid.getColumnModel().setEditable(2, true);
			showField(field);
			break;
		case 4:
			hideField(extPanel.get('xpath'));
			hideField(extPanel.get('head'));
			grid.getColumnModel().setEditable(2, false);
			break;	
		default:
			hideField(extPanel.get('xpath'));
			hideField(extPanel.get('head'));
			grid.getColumnModel().setEditable(2, true);
			break;
	}
};

// 类型选择时触发该方法(自定义模式)
var selectTypeCustom = function(obj, record, index){
	var formPanel = Ext.getCmp('comm-form');
	switch (index) {
		case 0:
			var field = formPanel.findById('xpath');
			hideField(formPanel.findById('head'));
			showField(field);
			break;
		case 3:
			var field = formPanel.findById('head');
			hideField(formPanel.findById('xpath'));
			showField(field);
			break;
		default:
			hideField(formPanel.findById('xpath'));
			hideField(formPanel.findById('head'));
			break;
	}
	obj.hiddenField.value = soapTypes[index][1];
};

var extPanelConfig = {
	header : false,
	itemId: 'extPanel',
	autoHeight : true, 
	border : true,
	layout: 'form',
	style : {'padding' : '0 0 0 5'},
	labelWidth: 115,
	defaults: {
		anchor: '100%',
		stateful: false
	},
	items: [{
		fieldLabel : 'SOAP类型',
		xtype : 'combo',
		name : 'type', 
		store : soapStore, 
		mode: 'local', 
		displayField : 'name',
		valueField: 'value',
		hiddenName : 'type',
		triggerAction: 'all', 
		forceSelection:true, 
		editable:false,
		allowBlank:false,
		listeners: {
			select: function(obj, record, index){
				selectTypeGuide(obj, record, index);
			} 
		}
	},{
		fieldLabel:'XPATH:',
		itemId : 'xpath', 	// 与unit.wsdl文件中sesa:binding属性名对应
		name:'xpath',
		hideLabel:true, hidden:true,
		xtype:'textfield'
	}, {
		fieldLabel: 'HTTP Head:',
		itemId:'head', 
		name: 'head',
		hideLabel: true,hidden: true,
		xtype: 'textfield'
	}, {
		xtype: 'fieldset',
		itemId: 'secFieldSet',
		titleCollapse : true,
		bodyStyle: Ext.isIE ? 'padding-top: 10px;' : null,
		layout: 'column',
		title : '安全设置',
		collapsed : true,
		collapsible : true,
		items: [{
			columnWidth : 0.4,
			itemId: 'checkBoxColumn',
			defaultType: 'checkbox',
			bodyStyle: {'padding-left':'30px'},
			defaults: {height: 28},
			items: [{
				boxLabel: '是否签名',
				itemId: 'signSwitch',
				name: 'signSwitch',
				tag: "attribute",
				readOnly : true,
				listeners: {
					check: function(obj) {
						var comboObj = obj.ownerCt.ownerCt.getComponent('comboColumn').getComponent('signingKeyProvider');
						if (obj.getValue() === true) {
							comboObj.enable();
						} else {
							comboObj.disable();
						}
					}
				}
			},{
				boxLabel: "是否加解密",
				itemId: 'encryptionSwitch',
				name: "encryptionSwitch",
				tag: "attribute",
				listeners: {
					check: function(obj) {
						var comboObj = obj.ownerCt.ownerCt.getComponent('comboColumn').getComponent('encryptionKeyProvider');
						if (obj.getValue() === true) {
							comboObj.enable();
						} else {
							comboObj.disable();
						}
					}
				}
			},{
				boxLabel: "是否验证签名",
				itemId: 'verifySwitch',
				name: "verifySwitch",
				tag: "attribute",
				listeners: {
					check: function(obj) {
						var comboObj = obj.ownerCt.ownerCt.getComponent('comboColumn').getComponent('verifyKeyProvider');
						if (obj.getValue() === true) {
							comboObj.enable();
						} else {
							comboObj.disable();
						}
					}
				}
			}]
		},{
			columnWidth : 0.6,
			itemId: 'comboColumn',
			defaultType: 'combo',
			bodyStyle: {'padding-right':'30px'},
			defaults: {
				width: 320,
				forceSelection: true,
				triggerAction: 'all',
				emptyText: '<请选择 KeyProvider>',
				cls : 'soapComboBox',
				allowBlank: false,
				store: new Ext.data.Store({
					url: '../console/security_ctrl.jsp',
					baseParams: {
						operation: 'getKeyProviderInfo'
					},
					reader: new Ext.data.JsonReader({
						root: 'items',
						fields: [{
							name: 'name'
						}]
					})
				}),
				valueField: 'name',
				displayField: 'name',
			},
			items: [{
				itemId: 'signingKeyProvider',
				name: 'signingKeyProvider',
				tag: "attribute",
				disabled: true
			},{
				itemId: 'encryptionKeyProvider',
				name: 'encryptionKeyProvider',
				tag: "attribute",
				disabled: true
			}, {
				itemId: 'verifyKeyProvider',
				name: 'verifyKeyProvider',
				tag: "attribute",
				disabled: true
			}]
		}]
	}],
	
	listeners: {
		afterlayout : function(obj) {	// 容器渲染后给内部元素赋值
			var data = (obj.extdata == 'undefined' || !obj.extdata) ? '' : obj.extdata;
			var xpath = (!data) ? '':data.xpath;
			var head = (!data) ? '':data.head;
			var value = (!data) ? '':data.type;
			
			// 安全设置赋值
			var signSwitch = (!data) ? 'off' : data.signSwitch;
			if (typeof signSwitch == 'undefined')
				signSwitch = 'off';
			
			var encryptionSwitch = (!data) ? 'off' : data.encryptionSwitch;
			if (typeof encryptionSwitch == 'off')
				encryptionSwitch = 'off';
				
			var verifySwitch = (!data) ? 'off' : data.verifySwitch;
			if (typeof verifySwitch == 'off')	
				verifySwitch = 'off';
				
			var fieldSet = obj.getComponent('secFieldSet');
			var checkboxes = fieldSet.getComponent('checkBoxColumn');
			var signSwitchObj = checkboxes.getComponent('signSwitch');
			var encryptionSwitchObj = checkboxes.getComponent('encryptionSwitch');
			var verifySwitchObj = checkboxes.getComponent('verifySwitch');
			
			var comboboxes = fieldSet.getComponent('comboColumn');
			var signingKP = comboboxes.getComponent('signingKeyProvider');
			var encryptionKP = comboboxes.getComponent('encryptionKeyProvider');
			var verifyKP = comboboxes.getComponent('verifyKeyProvider');
			
			
			var signingKeyProvider = (!data) ? '':data.signingKeyProvider;
			var encryptionKeyProvider = (!data) ? '':data.encryptionKeyProvider;
			var verifyKeyProvider = (!data) ? '':data.verifyKeyProvider;
			
			signSwitchObj.setValue(signSwitch);
			if (signSwitch == 'on') {
				signingKP.setValue(signingKeyProvider);
				signingKP.enable();
			}
			
			encryptionSwitchObj.setValue(encryptionSwitch);
			if (encryptionSwitch == 'on') {
				encryptionKP.setValue(encryptionKeyProvider);
				encryptionKP.enable();
			}
			
			verifySwitchObj.setValue(verifySwitch);
			if (verifySwitch == 'on') {
				verifyKP.setValue(verifyKeyProvider);
				verifyKP.enable();
			}
			
			
			// 操作列表赋值
			var grid = obj.ownerCt.get('operaGrid');
			Ext.each(soapTypes, function(type, index){
				if (value == type[1]) {
					obj.items.items[0].setValue(type[0]);
					obj.items.items[0].hiddenField.value = value;
					
					switch (index) {
						case 0:
							var field = obj.get('xpath');
							field.setValue(xpath);
							showField(field);
							break;
						case 3:
							var field = obj.get('head');
							field.setValue(head);
							showField(field);
							break;
						case 4:	// 选中soap body时,"操作列表"中的SOAP ACTION列不允许编辑
							grid.getColumnModel().setEditable(2, false);
							break; 	
						default:
							grid.getColumnModel().setEditable(2, true);
							break;
					}
				}
			});
		}
	}
};

/**
 * 扩展元素 名称必须为Ext.ux.extendPanel
 * 继承自Panel,构造函数的参数通过父类UnitBase传递
 * @param {Object} config
 */
Ext.ux.extendPanel = function(config){
	extPanelConfig.extdata = config.extdata;
	Ext.ux.extendPanel.superclass.constructor.call(this, extPanelConfig);
}
Ext.extend(Ext.ux.extendPanel, Ext.Panel, {});

/**
 * 构造函数参数
 */
var UnitSoap;
if (unitType == 'serverGuide') {
	UnitSoap = function(){
		UnitSoap.superclass.constructor.call(this, {});
	};
	Ext.extend(UnitSoap, UnitServerGuide, {
		createBinding: function(options){
			var _this = this;
			var bindingName = (!options) ? '' : options.bindingName;
			var operations = (!options) ? '' : options.operations;
			var intf = (!options) ? '' : options.portTypeName;
			var endPoint = (!options) ? '' : options.epName;
			var address = (!options) ? '' : options.address;
			
			var extdata = (!options) ? '' : options.extdata;
			var transport = (!extdata) ? '' : extdata.transport;
			var bindingType = (!extdata) ? '' : extdata.bindingType;
			
			// 扩展元素Panel
			var operaExtPanel = {};
			if (!!Ext.ux.extendPanel) 
				operaExtPanel = new Ext.ux.extendPanel({
					extdata: extdata
				});
			
			var isClient = unitType == 'client';
				
			// 按钮列
			function renderOpera(value, cellmeta, record, rowIndex, columnIndex, store) {
				if (!isClient) {
					var str = "<img class='innerImg' src='../images/elcl16/external_browser.gif'>";
				} else {
				 var isLocked = record.json['locked'];
				 var str = "<img class='innerImg' src='../images/elcl16/launch_disconnect.gif'>";
				 if (isLocked == 'true')
				 	str = "<img class='innerImg' src='" + ImgPath.lock + "'/>";
				 if (isLocked == 'false')
				 	str = "<img class='innerImg' src='" + ImgPath.unlock + "'/>";
				}
				return str;
			};
			
			var btnColumnCfg = {
				header: false,
				dataIndex: 'lock',
				menuDisabled: true,
				sortable: false,
				width: 25,
				fixed: true,
				align: 'center',
				renderer: renderOpera
			};
			
			var config = {
				title: '操作列表',
				itemId: 'operaGrid',
				autoScroll: true,
				height: 130,
				columnLines: true,
				bodyStyle: {
					'border': '1px solid #99BBE8',
					'border-top': 'none'
				},
				columns: [
				{header: "名称",	dataIndex: 'opera',width: 40}, 
				{header: "描述",	dataIndex: 'desc',width: 40}, 
				{
					header: "SOAP Action",
					dataIndex: 'action',
					editor: new Ext.form.TextField({
						allowBlank: false
					})
				}],
				viewConfig: {forceFit: true}
			};
			var reader = _this.operaGridReaderConfig;
			
			if (config.columns.length < 4 && isVersioned == 'true') {
				config.columns.push(btnColumnCfg);
			}
			
			// 操作列表表格
			config.store = new Ext.data.Store({
				reader: new Ext.data.JsonReader(reader)
			});
			var operaGridPanel = new Ext.grid.EditorGridPanel(config);
			operaGridPanel.store.loadData({
				operations: operations
			});
			
			operaGridPanel.on('render', function(grid) {
				var store = grid.getStore();
				var view = grid.getView();
				operaGridPanel.tip = new Ext.ToolTip({
			        target: view.mainBody,
			        delegate: !isClient ?'.x-grid3-row' : '.x-grid3-cell',
			        trackMouse: true,
			        renderTo: document.body,
					mouseOffset: [-70, 5],
			        listeners: {
			            beforeshow: function(tip) {
							if (!isClient) {
								tip.body.dom.innerHTML = '双击查看该操作';
							} else {
								var cellText = tip.anchorTarget.textContent;
								var rowIndex = view.findRowIndex(tip.triggerElement);
								var record = store.getAt(rowIndex);
								var rs = true;
								if (!!cellText) {
									Ext.each(record.fields.keys, function(key){
										if (record.get(key) == cellText) {
											switch (key) {
												case 'opera':
												case 'desc':
													tip.body.dom.innerHTML = "双击查看该操作";
													break;
												default:
													rs = false;
													break;
											}
										}
									});
								} else {
									var innerHTML = tip.anchorTarget.innerHTML;
									if (innerHTML.indexOf('launch_disconnect.gif') != -1) {
										tip.body.dom.innerHTML = '操作未同步至SVN';
									} else {
										if (innerHTML.indexOf('unlock.png') != -1) {
											tip.body.dom.innerHTML = '锁定操作';
										} else {
											tip.body.dom.innerHTML = '解锁操作';
										}
									}
								}
								return rs;
							}
			            }
			        }
			    });
			});
			
			// 单击按钮锁定操作
			operaGridPanel.on('cellclick', function (grid, rowIndex, columnIndex, e) {
				var img = e.getTarget('.innerImg');
				if (img) {
					if (!isClient) {
						operaGridPanel.fireEvent('celldblclick', grid, rowIndex, columnIndex, e);
						return;
					}
				
					var record = grid.getStore().getAt(rowIndex);
					var importUnit = unit.replace(/\/.*/ig, '/engine/'+intf).replace(/:.*/ig, '');
					var data = unit + '/' + record.get('opera');
					if (!!engineCmp)
						data = importUnit + '/' + record.get('opera');
					
					_this.operaLockToggle(grid, rowIndex, columnIndex, data);
				}
			});
			
			// 双击单元格打开操作
			operaGridPanel.on('celldblclick', function(grid, rowIndex, columnIndex, e) {
				function openOpera(portType, opera, desc) {
					var importUnit = unit.replace(/\/.*/ig, '/engine/'+intf).replace(/:.*/ig, '');
					if (!!engineCmp){
						if (engineCmp.indexOf('process') == -1) {
							Application.createWindow(importUnit + '/' + opera, opera + '-' + desc, '../schema/encoding_viewer.jsp?schema=' + importUnit + '/' + opera + '.xsd' + '&unit=' + unit + '&unitId=' + unitId + '&unitDesc=' + encodeURIComponent(unitDesc) + '&ref=' + '&projectDesc=' + encodeURIComponent(projectDesc), 'settings');
						} else {
							Application.serviceEditor.createWindow({
								id: importUnit + '/' + opera,
								text: opera + '-' + desc,
								filePath: importUnit + '/' + opera,
								params: '&unit=' + unit + '&unitId=' + unitId + '&unitDesc=' + encodeURIComponent(unitDesc) + '&projectDesc=' + encodeURIComponent(projectDesc)
							});
						}
					} else {
						Application.createWindow(unit + '/' + opera, opera + '-' + desc, '../schema/encoding_viewer.jsp?schema=' + unit + '/' + opera + '.xsd' + '&unit=' + unit + '&unitId=' + unitId + '&unitDesc=' + encodeURIComponent(unitDesc) + '&ref=' + '&projectDesc=' + encodeURIComponent(projectDesc), 'settings');
					}
				};
				
				var record = grid.getStore().getAt(rowIndex);  // Get the Record
				var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
				var ptName = intf.replace(/:.*/ig, '');
				if (!!isClient) {
					if (fieldName != 'action' && fieldName != 'lock') 
						openOpera(ptName, record.get('opera'), record.get('desc'));
				} else {
					if (fieldName != 'action') 
						openOpera(ptName, record.get('opera'), record.get('desc'));
				}
			});
			
			var formpanel = new Ext.form.FormPanel({
				title: bindingName,
				style: {
					'margin': '5 8 3 8'
				},
				height: _this.panelHeight,
				frame: true,
				closable: true,
				collapsible: true,
				defaults: {
					anchor: '-18'
				},
				labelWidth: 120,
				layout: 'form',
				items: [{
					xtype: 'field',
					hideLabel: true,
					hidden: true,
					itemId: 'interface',
					name: 'interface',
					value: intf
				}, {
					xtype: 'field',
					hideLabel: true,
					hidden: true,
					itemId: 'oBindingName',
					name: 'oBindingName',
					value: bindingName
				}, {
					fieldLabel: '绑定名称',
					labelStyle: 'padding : 3 0 0 5',
					xtype: 'textfield',
					name: 'bindingName',
					allowBlank: false,
					value: bindingName,
					listeners: {
						blur: function(field){
							if (!bindingName) {
								var value = field.getValue().toLowerCase();
								var hasSame = false;
								var sameCount = 1;
								var parentPanel = Ext.getCmp('binding-panel');
								var items = parentPanel.items.items;
								Ext.each(items, function(item, index){
									if (!!item.title) 
										if (value == item.title.toLowerCase()) {
											hasSame = true;
											sameCount++;
											return;
										}
								});
								
								if (hasSame && sameCount > 2) {
									Ext.Msg.alert('提示:', '已经存在相同的绑定名称' + '"' + value + '"' + ',请重新输入！', function(){
										field.focus(true, true);
										field.reset();
									});
								}
							}
						},
						valid: function(field){
							formpanel.setTitle(field.getValue());
						}
					}
				}, {
					fieldLabel: '传输协议',
					labelStyle: 'padding : 3 0 0 5',
					name: 'transport',
					itemId: 'transport',
					xtype: 'combo',
					forceSelection: false,
					editable: false,
					allowBlank: true,
					emptyText: '<请选择传输协议类型>',
					store: new Ext.data.Store({
						url: _this.url,
						baseParams: {
							operation: 'getTransports',
							unit: unit,
							component: component
						},
						reader: new Ext.data.JsonReader({
							root: 'items',
							fields: [{
								name: 'transport'
							}]
						})
					}),
					triggerAction: 'all',
					valueField: 'transport',
					displayField: 'transport',
					listeners: {
						select: function(obj){
							var transport = this.getValue();
							if (!!transport) {
								Ext.Ajax.request({
									method: 'POST',
									url: _this.url,
									params: {
										operation: 'generateSchemaUI',
										bindingName: bindingName,
										transport: transport,
										unit: unit
									},
									callback: function(options, success, response){
										var propCmp = formpanel.getComponent('properties');
										if (!!propCmp) {
											propCmp.destroy();
										}
										
										// 生成面板
										var data;
										if (success) {
											data = Ext.decode(response.responseText);
											var schema = data.schema;
											var values = data.values;
											if (!!schema) {
												if (!!schema.items && schema.items.length > 0) {
													_this.createExtaPanel(formpanel, schema, values);
													formpanel.doLayout();
													
													var bindingPanel = Ext.getCmp('binding-panel');
													bindingPanel.doLayout();
												}
											}
										}
									}
								});
							}
						}
					}
				}, operaExtPanel, {
					xtype: 'fieldset',
					itemId: 'enpointFieldSet',
					style: {
						'margin': '0 0 10 5'
					},
					titleCollapse: true,
					bodyStyle: Ext.isIE ? 'padding-top: 10px;' : null,
					layout: 'column',
					title: '端点设置',
					collapsed: true,
					collapsible: true,
					items: [{
						columnWidth: 0.5,
						itemId: 'epColumn',
						layout: 'form',
						defaults: {
							anchor: '100%'
						},
						items: [{
							fieldLabel: '端点名称',
							xtype: 'textfield',
							cls: 'column',
							itemId: 'epName',
							name: 'epName',
							tag: 'attribute',
							value: endPoint
						}]
					}, {
						columnWidth: 0.4,
						itemId: 'addrColumn',
						layout: 'form',
						labelWidth: 60,
						defaults: {anchor: '100%'},
						items: [{
							fieldLabel: '地址',
							xtype: 'combo',
							itemId: 'address',
							name: 'address',
							tag: 'attribute',
							emptyText: '<请选择地址>',
							store: new Ext.data.Store({
								url: _this.url,
								baseParams: {
									operation: 'getLocations',
									unit: unit
								},
								reader: new Ext.data.JsonReader({
									root: 'items',
									fields: [{
										name: 'name'
									}, {
										name: 'url'
									}]
								}),
								listeners: {
									beforeload: function(store, options) {
										options.params.transportType = formpanel.find('itemId', 'transport')[0].getValue();
									}
								}
							}),
							valueField: 'url',
							displayField: 'url',
							allowBlank: true,
							forceSelection: false,
							editable: true,
							triggerAction: 'all',
							value: address,
							listeners: {
								expand: function() {
									this.store.load();
								}
							}
						}]
					},{
						columnWidth: 0.1,
						itemId: 'btnColumn',
						layout: 'fit',
						items: [{
							id: 'address-book-btn',
							xtype: 'button',
							html: '<div ext:qtip="查看地址簿" class="address-book-btn"><div><img src="../images/obj16/bkmrk_nav.gif"></div></div>',
							listeners: {
								click: function(){
									var addr = this.ownerCt.ownerCt.items.get('addrColumn').items.items[0].value;
									
									var desktop = top.Application.getDesktop();
							        var win = desktop.getWindow('address-book');
							        if(!win){
							            win = desktop.createWindow({
							                id: 'address-book',
							                title: '地址簿',
							                width:600,
							            	height:480,
							                iconCls: '',
							                shim:false,
							                animCollapse:false,
							                constrainHeader:true,
							                resizable: true,
							                maximizable: true,
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
										       src: '../console/address_book.jsp?externalAddr=' + addr
											}
							            });
							        }
							        win.show();
								}
							}
						}]
					}]
				}, operaGridPanel],
				tools: [{
					id: 'refresh',
					qtip: '同步接口',
					handler: function(e, toolEl, panel, tc){
						var intf = panel.get('interface').getValue();
						var bindingName = panel.get('oBindingName').getValue();
						_this.syncBinding(intf, bindingName);
					}
				}, {
					id: 'close',
					qtip: '删除绑定',
					handler: function(e, toolEl, panel, tc){
						if (!confirm('确实要删除该绑定吗?')) 
							return;
						var intf = panel.get('interface').getValue();
						var bindingName = panel.get('oBindingName').getValue();
						_this.deleteBinding(intf, bindingName);
						panel.destroy();
						Ext.getCmp('binding-panel').doLayout();
					}
				}]
			});
			
			// 传输协议赋值
			var transObj = formpanel.getComponent('transport');
			transObj.setValue(transport);
			transObj.fireEvent('select', transObj);
			return formpanel;
		},
		
		saveBindings: function(bindings, processTip, successTip){
			var _this = this;
			var xmldoc = XDom.createDocument();
			var processMsg = (processTip == 'undefiend' || !processTip) ? '正在保存绑定信息...' : processTip;
			var successMsg = (successTip == 'undefiend' || !successTip) ? '保存成功' : successTip;
			
			function setBindingAttrs(bindingEl, params){
				bindingEl.setAttribute('interface', decodeURIComponent(params['interface']));
				bindingEl.setAttribute('oname', decodeURIComponent(params['oBindingName']));
				bindingEl.setAttribute('name', decodeURIComponent(params['bindingName']));
				bindingEl.setAttribute('epName', decodeURIComponent(params['epName']));
				bindingEl.setAttribute('address', decodeURIComponent(params['address']));
				return bindingEl;
			};
			
			function setExtAttrs(extEl, params){
				for (var key in params) {
					if (key == 'interface' ||
					key == 'oBindingName' ||
					key == 'bindingName' ||
					key == 'epName' || key == 'address')
						continue;
					if (key == 'transport') {
						if (params[key].indexOf('传输协议') != -1)
							params[key] = '';
					}
					extEl.setAttribute(key, decodeURIComponent(params[key]));
				}
				return extEl;
			};
			
			function setExtChildren(transExtEl, extEl, properties) {
				var items = properties.items.items;
				if (!!items && items.length > 0)
				Ext.each(items, function(item) {
					if (item.name.indexOf('column') != -1) {
						var columnItems = item.items.items;
						Ext.each(columnItems, function(childItem) {
							var child = xmldoc.createElement(childItem.name);
							child.textContent = childItem.getValue();
							transExtEl.appendChild(child);
							extEl.removeAttribute(childItem.name);
						});		
					} else {
						var child = xmldoc.createElement(item.name);
						child.textContent = item.getValue();
						transExtEl.appendChild(child);
						extEl.removeAttribute(item.name);
					}
				});
				return transExtEl;
			};
			
			try {
				var rootEl = xmldoc.createElement('bindings');
				xmldoc.appendChild(rootEl);
				
				var valid = true;
				Ext.each(bindings, function(binding){
					var form = binding.getForm();
					if (!form.isValid()) {
						valid = false;
						return;
					}
					
					var params = form.getValues(false);
					
					// 端点设置
					params['epName'] = 
						binding.getComponent('enpointFieldSet').getComponent('epColumn').getComponent('epName').getValue();
					params['address'] = 
						binding.getComponent('enpointFieldSet').getComponent('addrColumn').getComponent('address').getValue();
					
					// 安全设置部分
					if (!params['signSwitch'])
						params['signSwitch'] = 'off';
					if (!params['encryptionSwitch'])
						params['encryptionSwitch'] = 'off';
					if (!params['verifySwitch'])
						params['verifySwitch'] = 'off';
					if (!params['signingKeyProvider'])	
						params['signingKeyProvider'] = '';
					if (!params['encryptionKeyProvider'])	
						params['encryptionKeyProvider'] = '';
					if (!params['verifyKeyProvider'])	
						params['verifyKeyProvider'] = '';		
						
					var bindingEl = xmldoc.createElement('binding');
					bindingEl = setBindingAttrs(bindingEl, params);
					
					// 组件附加参数
					var extEl = xmldoc.createElement('extdata');
					extEl = setExtAttrs(extEl, params);
					bindingEl.appendChild(extEl);
					
					// 协议附加参数
					var properties = binding.getComponent('properties');
					if (!!properties) {
						var transExtEl = xmldoc.createElement('properties');
						transExtEl = setExtChildren(transExtEl, extEl, properties);
						bindingEl.appendChild(transExtEl);
					}
					
					// operation节点
					var store = binding.get('operaGrid').store;
					store.each(function(record){
						var operaEl = xmldoc.createElement('operation');
						operaEl.setAttribute('name', record.get('opera'));
						operaEl.setAttribute('desc', record.get('desc'));
						operaEl.setAttribute('action', record.get('action'));
						bindingEl.appendChild(operaEl);
					});
					rootEl.appendChild(bindingEl);
				});
				
				if (valid) {
					Ext.getBody().mask(processMsg, 'x-mask-loading');
					Ext.Ajax.request({
						method: 'POST',
						url: _this.url,
						params: {
							operation: "saveBindings",
							unit: unit,
							component: component,
							data: encodeURIComponent(XDom.innerXML(xmldoc))
						},
						callback: function(options, success, response){
							if (!success) {
								Ext.getBody().unmask();
								_this.showException(response);
							}
							else {
								Ext.getBody().mask(successMsg, 'x-mask-loading');
								Ext.getBody().unmask.defer(500, Ext.getBody());
							}
						}
					});
				}
			} catch (e) {
				alert(e.message);
			} finally {
				delete xmldoc;
			}
		}
	});
} else if (unitType == 'serverCustom') {
		UnitSoap = function(){
			var listReaderFields = [{
				name: 'action'
			}];
			var rightBaseColumnsCfg = [{
				header: "SOAP Action",
				dataIndex: 'action',
				hidden: true
			}];
			var dialogIntfStoreCfg = [{
				id: 'soapAction',
				name: 'action',
				allowBlank: true,
				fieldLabel: 'SOAP Action'
			}];
			
			var config = {
				paramMidPanel: {
					height: 90
				},
				paramsFieldsets: [{
					xtype: 'panel',
					title: '参数',
					id: 'params',
					style: "margin-top: 10px;",
					tag: "element",
					name: "params",
					autoHeight: true,
					border: true,
					bodyStyle: 'padding: 5px;',
					defaults: {
						anchor: "100%",
						stateful: false
					},
					layout: "form",
					labelWidth: 130,
					items: [{
						fieldLabel: "SOAP类型",
						xtype: "combo",
						name: "type",
						store: soapStore,
						mode: 'local',
						id: 'soapType',
						displayField : 'name',
						valueField: 'value',
						hiddenName : 'type',
						triggerAction: 'all',
						forceSelection: true,
						editable: false,
						allowBlank: false,
						listeners: {
							select: selectTypeCustom
						}
					}, {
						fieldLabel: "XPATH:",
						id: 'xpath',
						name: "xpath",
						hideLabel: true,
						hidden: true,
						tag: "attribute",
						xtype: "textfield"
					}, {
						fieldLabel: "HTTP Head:",
						id: 'head',
						name: "head",
						hideLabel: true,
						hidden: true,
						tag: "attribute",
						xtype: "textfield"
					}]
				}]
			};
			
			var cfg = {
				config: config,
				dialogCfg: {
					height: 300
				},
				listReaderFields: listReaderFields,
				rightBaseColumnsCfg: rightBaseColumnsCfg,
				dialogIntfStoreCfg: dialogIntfStoreCfg
			};
			UnitSoap.superclass.constructor.call(this, cfg);
		};
		Ext.extend(UnitSoap, UnitServerCustom, {
			gridLoader: function(rootEl, container){
				var endpointsEl = rootEl.getElementsByTagName('endpoints')[0];
				var epList = endpointsEl.getElementsByTagName('endpoint');
				var store = Ext.getCmp('endpGrid').store;
				var array = [];
				for (var i = 0, len = epList.length; i < len; i++) {
					var ep = epList[i];
					array.push([ep.getAttribute('name'), ep.getAttribute('location')]);
				}
				
				if (array.length > 0) {
					container.get('endpGrid').setTitle('端点');
				} else {
					container.get('endpGrid').setTitle('端点(<span style="color:red">请点击"+"添加端点</span>)');
				}
				store.loadData({
					list: array
				});
				
				// soap参数面板
				var soapParams = Ext.getCmp('params');
				// 输入参数
				var paramsEl = rootEl.getElementsByTagName('params')[0];
				var attributes = (paramsEl != 'undefined' && !!paramsEl) ? paramsEl.attributes : [];
				
				if (attributes.length > 0) 
					Ext.each(attributes, function(attr, index){
						if (attr.name != 'used') {
							var name = attr.name;
							var value = paramsEl.getAttribute(attr.name);
							if (!!value) {
								// 下拉菜单赋值
								Ext.each(soapTypes, function(type, index){
									if (value == type[1]) {
										soapParams.getComponent(0).setValue(type[0]);
										soapParams.getComponent(0).hiddenField.value = value;
										soapComboxMapValue = value;
										
										switch (index) {
											case 0:
												var field = formPanel.findById('xpath');
												showField(field, value);
												break;
											case 3:
												var field = formPanel.findById('head');
												showField(field, value);
												break;
											default:
												break;
										}
									}
								});
								return;
							}
						}
					});
			}
		});
} else if (unitType == 'client') {
	UnitSoap = function(){
		UnitSoap.superclass.constructor.call(this, {});
	};
	Ext.extend(UnitSoap, UnitClient, {
		operationGridConfig: {
			title: '操作列表',
			itemId: 'operaGrid',
			autoScroll: true,
			height: 130,
			columnLines: true,
			bodyStyle: {
				'border': '1px solid #99BBE8',
				'border-top': 'none'
			},
			columns: [{
				header: "名称",
				dataIndex: 'opera',
				width: 40
			}, {
				header: "描述",
				dataIndex: 'desc',
				width: 40
			}, {
				header: "SOAP Action",
				dataIndex: 'action',
				editor: new Ext.form.TextField({
					allowBlank: false
				})
			}],
			viewConfig: {
				forceFit: true
			}
		},
		
		saveBindings: function(bindings, processTip, successTip){
			var _this = this;
			var xmldoc = XDom.createDocument();
			var processMsg = (processTip == 'undefiend' || !processTip) ? '正在保存绑定信息...' : processTip;
			var successMsg = (successTip == 'undefiend' || !successTip) ? '保存成功' : successTip;
			
			function setBindingAttrs(bindingEl, params){
				bindingEl.setAttribute('interface', decodeURIComponent(params['interface']));
				bindingEl.setAttribute('oname', decodeURIComponent(params['oBindingName']));
				bindingEl.setAttribute('name', decodeURIComponent(params['bindingName']));
				bindingEl.setAttribute('epName', decodeURIComponent(params['epName']));
				bindingEl.setAttribute('address', decodeURIComponent(params['address']));
				return bindingEl;
			};
			
			function setExtAttrs(extEl, params){
				for (var key in params) {
					if (key == 'interface' ||
					key == 'oBindingName' ||
					key == 'bindingName' ||
					key == 'epName' || key == 'address')
						continue;
					if (key == 'transport') {
						if (params[key].indexOf('传输协议') != -1)
							params[key] = '';
					}
					extEl.setAttribute(key, decodeURIComponent(params[key]));
				}
				return extEl;
			};
			
			function setExtChildren(extEl, properties){
				var items = properties.items.items;
				if (!!items && items.length > 0) 
					Ext.each(items, function(item){
						var child = xmldoc.createElement(item.name);
						child.textContent = item.getValue();
						extEl.appendChild(child);
					});
				return extEl;
			};
			
			try {
				var rootEl = xmldoc.createElement('bindings');
				xmldoc.appendChild(rootEl);
				
				var valid = true;
				Ext.each(bindings, function(binding){
					var form = binding.getForm();
					if (!form.isValid()) {
						valid = false;
						return;
					}
					
					var params = form.getValues(false);
					// 端点设置
					if (!params['epName'])
						params['epName'] = '';
					if (!params['address'])	
						params['address'] = '';
						
					// 安全设置部分
					if (!params['signSwitch'])
						params['signSwitch'] = 'off';
					if (!params['encryptionSwitch'])
						params['encryptionSwitch'] = 'off';
					if (!params['verifySwitch'])
						params['verifySwitch'] = 'off';
					if (!params['signingKeyProvider'])	
						params['signingKeyProvider'] = '';
					if (!params['encryptionKeyProvider'])	
						params['encryptionKeyProvider'] = '';
					if (!params['verifyKeyProvider'])	
						params['verifyKeyProvider'] = '';		
						
					var bindingEl = xmldoc.createElement('binding');
					bindingEl = setBindingAttrs(bindingEl, params);
					
					var extEl = xmldoc.createElement('extdata');
					var properties = binding.getComponent('properties');
					if (!!properties) 
						extEl = setExtChildren(extEl, properties);
					else 
						extEl = setExtAttrs(extEl, params);
					bindingEl.appendChild(extEl);
					
					// operation节点
					var store = binding.get('operaGrid').store;
					store.each(function(record){
						var operaEl = xmldoc.createElement('operation');
						operaEl.setAttribute('name', record.get('opera'));
						operaEl.setAttribute('desc', record.get('desc'));
						operaEl.setAttribute('action', record.get('action'));
						bindingEl.appendChild(operaEl);
					});
					rootEl.appendChild(bindingEl);
				});
				
				if (valid) {
					Ext.getBody().mask(processMsg, 'x-mask-loading');
					Ext.Ajax.request({
						method: 'POST',
						url: _this.url,
						params: {
							operation: "saveBindings",
							unit: unit,
							component: component,
							data: encodeURIComponent(XDom.innerXML(xmldoc))
						},
						callback: function(options, success, response){
							if (!success) {
								Ext.getBody().unmask();
								_this.showException(response);
							}
							else {
								Ext.getBody().mask(successMsg, 'x-mask-loading');
								Ext.getBody().unmask.defer(500, Ext.getBody());
							}
						}
					});
				}
			} catch (e) {
				alert(e.message);
			} finally {
				delete xmldoc;
			}
		}
	});
}

var Soap = new UnitSoap();
Ext.onReady(Soap.init, Soap, true);