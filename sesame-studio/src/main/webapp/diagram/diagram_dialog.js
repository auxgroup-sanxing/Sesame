// 元素重名命规则
var ElementName = {
	regex: /^[A-Za-z_]+[\w_\-.]*$/,
	regexText: '必须以字母或下划线开头，不能包含冒号和空白字符'
};

var Dialog = function(){

var Saver = {
	namespaces :{
	},
	map : {
		'element' : function(node, parentEl){
			var a = node.attributes;
			var prefix = Saver.namespaces[a.qtip];
			var element = parentEl.ownerDocument.createElementNS(a.qtip, prefix ? prefix+':'+a.name : a.name, null);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
		},
		'func' : function(node, parentEl){
			var a = node.attributes;
			if (parentEl.nodeType==XDom.ATTRIBUTE_NODE) {
				var text = (node.previousSibling ? ',' : '') + a.text+'(';
				parentEl.nodeValue = parentEl.nodeValue+text;
				Saver.saveNodes(node, parentEl);
				parentEl.nodeValue = parentEl.nodeValue+")";
			}
			else {
				var ns = parentEl.ownerDocument.documentElement.namespaceURI;
				var element = parentEl.ownerDocument.createElementNS(ns, 'xsl:value-of');
				element.setAttribute("select", a.text+"(");
				parentEl.appendChild(element);
				Saver.saveNodes(node, element.attributes.getNamedItem('select'));
				element.setAttribute("select", element.getAttribute('select')+")");
			}
		},
		'param' : function(node, parentEl){
			var a = node.attributes;
			if (parentEl.nodeType==XDom.ATTRIBUTE_NODE) {
				var text = (node.previousSibling ? ',' : '') + a.text;
				parentEl.nodeValue = parentEl.nodeValue+text;
			}
			else if (!a.type || a.type=='variable') {
				var ns = parentEl.ownerDocument.documentElement.namespaceURI;
				var element = parentEl.ownerDocument.createElementNS(ns, 'xsl:value-of');
				element.setAttribute("select", a.text);
				parentEl.appendChild(element);
			}
			else {
				var text = a.text.replace(/^'|'$/g, '');
				var textNode = parentEl.ownerDocument.createTextNode(text);
				parentEl.appendChild(textNode);
			}
		}
	},
	
	saveNodes : function(parentNode, parentEl){
		var node = parentNode.firstChild;
		while (node) {
			var funcName;
			if (node.attributes.uiProvider==Ext.ux.ElementNodeUI) {
				funcName = 'element';
			}
			else if (node.attributes.uiProvider==Ext.ux.FuncNodeUI) {
				funcName = 'func';
			}
			else if (node.attributes.uiProvider==Ext.ux.ParamNodeUI) {
				funcName = 'param';
			}
			var func = this.map[funcName];
			if (func) func(node, parentEl);
			node = node.nextSibling;
		}
	}
};

var varList_tpl = new Ext.XTemplate(
	'<tpl for=".">',
	    '<div class="x-combo-list-item" id="{name}">',
	    '<div class="x-unselectable" unselectable="on" style="vertical-align:middle; line-height:16px;">',
		'<img style="width:16px; height:16px; vertical-align:middle;" class="{icon}" src="'+Ext.BLANK_IMAGE_URL+'" />',
	    '<span class="x-editable" style="vertical-align:middle;">{name}</span></div></div>',
	'</tpl>',
	'<div class="x-clear"></div>'
);


return {
// 配置帮助文档
showHelperWin: function(type, dialog) {
	var winWidth = 420;
	var winHeight = 430;
	
	if (!!Ext.fly('settingHelper'))
		return;
	
	// 窗口弹出时设置父对话框偏移量
	var currentX = dialog.getPosition(true)[0];
	var currentY = dialog.getPosition(true)[1];
	dialog.setPosition((currentX - 160 > 0)?currentX - 160:currentX , currentY);
	var win = new Ext.Window({
		title: '配置帮助文档',
		id: 'settingHelper',
		width: winWidth,
		height: winHeight,
		border: false,
		resizable : true,
		minimizable : true,
		maximizable : true,
		constrain:true, constrainHeader:true,
		closeAction: 'close',
		pageX: currentX + 300, pageY: currentY - 100,
		iconCls: 'xpathHelper',
		items: {
			bodyCfg: {
				tag: 'iframe',
				frameborder: 'no',
				style: 'border:none;',
				height: '100%',
				width: '100%',
				src: '../documents/action/' + type + 'Help.html'
			}
		},
		listeners: {
			close: function() {
				if (!!dialog && dialog.isVisible())
					dialog.setPosition(currentX, currentY);
			},
			activate: function() {
				Dialog.setBackMask();
			}
		}
	});
	win.show();
},
	
// XPath帮助文档
showXpathWin: function(dialog) {
	var winWidth = dialog.getWidth();
	var winHeight = 220;
	
	if (!!Ext.fly('xpathHelper'))
		return;
	
	// 窗口弹出时设置父对话框偏移量
	var currentX = dialog.getPosition(true)[0];
	var currentY = dialog.getPosition(true)[1];
	dialog.setPosition(currentX, currentY - 100);
	
	var win = new Ext.Window({
		title: 'XPath帮助文档',
		id: 'xpathHelper',
		width: winWidth,
		height: winHeight,
		border: false,
		resizable : true,
		minimizable : true,
		maximizable : true,
		constrain:true, constrainHeader:true,
		closeAction: 'close',
		pageX: currentX, pageY: currentY + 200,
		iconCls: 'xpathHelper',
		items: {
			bodyCfg: {
				tag: 'iframe',
				frameborder: 'no',
				style: 'border:none;',
				height: '100%',
				width: '100%',
				src: '../documents/xpath/index.html'
			}
		},
		listeners: {
			close: function() {
				if (!!dialog && dialog.isVisible())
					dialog.setPosition(currentX, currentY);
			},
			activate: function() {
				Dialog.setBackMask();
			}
		}
	});
	win.show();
}, 
	
// 窗口活动时再弹出窗口禁止遮罩	
setBackMask : function() {
	var backMask = Ext.query('.ext-el-mask')[0];
	backMask.style.zIndex = '3000';
	
	if (Ext.fly('xpathHelper') != null) {
		Ext.fly('xpathHelper').setStyle('z-index', '9999');
	}
	if (Ext.fly('settingHelper') != null)	
		Ext.fly('settingHelper').setStyle('z-index', '9999');
},


//追加DOM节点对话框
getAppendDialog : function(node){
	var _this = this;
	
	if (!this.appendDlg) {
		var titleText = '追加';
		var type = 'append';
		var array=_this.getVariableArray(node);
		array.unshift({name:'', icon:''});
		
	    var dlg = new Ext.Window({
	        autoCreate : true,
	        title: titleText,
	        resizable:true,  constrain:true,  constrainHeader:true,
	        minimizable: false,  maximizable: false,
	        modal: true, stateful: false,
	        buttonAlign: "right",	defaultButton: 0,
	        width: 450,  height: 285,
	        minHeight: 220, minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				id: 'appendform',
				xtype: 'form',
				autoScroll: true,
				height: 100,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false },
				items: [
					{
						fieldLabel: '变量',
				        name: 'to-var',
						xtype: 'combo',
						editable: false,
						allowBlank: false,
						forceSelection: false,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon', 'schema'],
						    data : _this.getVariableArray(node)
						}),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'name', displayField: 'name'
				    },{
						name: 'index',
						fieldLabel: '位置',
						xtype: 'numberfield',
						minValue: 0,
						emptyText: '',
						allowDigits: false
				    },{
						fieldLabel: '源变量',
				        name: 'var',
						xtype: 'combo',
						forceSelection: true, editable: false,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon', 'schema'],
						    data : array
						}),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'name', displayField: 'name',
						listeners: {
							select: function(sender, record, index){
								var formPanel = sender.ownerCt, value = sender.getValue();
								var form=formPanel.getForm();
								var field=form.findField('raw-value');
								field.setDisabled(value);
								field=form.findField('type');
								field.setDisabled(value);
								field=form.findField('xpath');
								field.setDisabled(!value);
								if (field.reloadTree) field.reloadTree();
							}
						}
					},{
						name: 'xpath',
						fieldLabel: 'XPath',
						xtype: 'treecombo',
						disabled: true
				    },{
						fieldLabel: '类型',
				        name: 'type',
						hiddenName: 'type',
						xtype: 'combo',
						allowBlank: false,
						editable: false,
						forceSelection: true,
						store: _this.getTypeStore(),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'value', displayField: 'label'
				    },{
						name: 'raw-value',
						xtype: 'textarea',
						fieldLabel: '值'
				    }]
				}
			],
			tools: [{
				id: 'help',
				qtip: '配置使用帮助',
				handler: function(e, toolEl, panel, tc){
					Dialog.showHelperWin(type, dlg);
				}
			}],
			buttons: [{
				text: 'XPath帮助',
				handler: function(){
					Dialog.showXpathWin(dlg);
				}
			}],
			listeners : {
				activate : function() {
					Dialog.setBackMask();
				},
				hide: function() {
					dlg.hide();
					Viewer.closeAction();
				}
			}
	    });
		
	    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:!node.attributes.lock}, function(){
			if (!dlg.node) return false;
			var a = dlg.node.attributes;
			var formPanel = dlg.getComponent(0);
			var form = formPanel.getForm();
			if (!form.isValid()) return false;
			
			var type = form.getValues()['type'];
			if (type == 'element') {
				var info = Viewer.validateXML(form.getValues()['raw-value']);
				if (!!info) {
					Ext.Msg.alert('提示:', info);
					return;
				}
			}
			Ext.apply(a, form.getValues());
			dlg.node.setText(a.toString());
			Viewer.getToolbar().items.get('save').enable();
			dlg.hide();
		});
	    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
			dlg.hide();
			Viewer.closeAction();
		});
	    
		dlg.render(Ext.getBody());
		var form = dlg.getComponent(0).getForm();
		form.findField('xpath').treeLoader = _this.getTreeLoader(form),
	    this.appendDlg = dlg;
	}
	else {
		if(node.attributes.lock){
			this.appendDlg.ok.enable();
		}else{
			this.appendDlg.ok.disable();
		}
		var formPanel = this.appendDlg.getComponent(0);
		var field = formPanel.getForm().findField('to-var');
		var array=_this.getVariableArray(node);
		field.store.loadData(array);
		field = formPanel.getForm().findField('var');
		array.unshift({name:'', icon:''});
		field.store.loadData(array);
	}
	this.appendDlg.node = node;
	var a = node.attributes;
	var form = this.appendDlg.getComponent(0).getForm();
	form.reset();
	form.setValues(a);
	var field = form.findField('var');
	field.fireEvent('select', field);
	
	this.appendDlg.center();
    return this.appendDlg;
},

//命名空间对话框
showNamespaceDialog : function(node){
	var _this = this;
	
	var dlg = this.space;
	if (!this.space) {
		var type = 'clone';
	    dlg = new Ext.Window({
	        autoCreate : true,
	        title: '命名空间',
	        resizable:true,  constrain:true,  constrainHeader:true,
	        minimizable: false,  maximizable: false,
	        stateful: false, modal: true,
	        buttonAlign: "right",	defaultButton: 0,
	        width: 450,  height: 220,
	        minHeight: 300,	minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				region: 'north',
				xtype: 'form',
				autoScroll: true,
				height: 110,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false },
				items: [
					{
						fieldLabel: '操作',
						hiddenName: 'method',
						xtype: 'combo',
						allowBlank: false,
						editable: false, forceSelection: true,
						store:  [['set', '设置命名空间'], ['add', '添加声明'], ['remove','删除声明']],
						mode: 'local', triggerAction: 'all'
				    },{
						fieldLabel: '前缀',
						name: 'prefix',
						xtype: 'textfield',
						allowBlank: true,
						qtip: '无前缀'
				    },{
						fieldLabel: 'URI',
						name: 'uri',
						xtype: 'combo',
						allowBlank: true,
						editable: true, forceSelection: false,
						store:  ['http://schemas.xmlsoap.org/wsdl/soap/', 
							'http://schemas.xmlsoap.org/soap/envelope/', 
							'http://www.w3.org/2001/XMLSchema'
						],
						mode: 'local', triggerAction: 'all'
				    },{
						fieldLabel: '目标变量',
				        name: 'var',
						xtype: 'combo',
						allowBlank: false,
						editable: false,
						forceSelection: false,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon', 'schema'],
						    data : _this.getVariableArray(node)
						}),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'name', displayField: 'name',
						listeners: {
							select: function(sender, record, index){
								var form = sender.ownerCt.getForm(), value = sender.getValue();
								var field = form.findField('xpath');
								if (field.reloadTree) field.reloadTree();
							}
						}
				    },{
						name: 'xpath',
						allowBlank: true,
						xtype: 'treecombo',
						fieldLabel: 'xpath'
				    }]
				}],
				tools: [{
					id: 'help',
					qtip: '配置使用帮助',
					handler: function(e, toolEl, panel, tc){
						Dialog.showHelperWin(type, dlg);
					}
				}],
				buttons: [{
					text: 'XPath帮助',
					handler: function(){
						Dialog.showXpathWin(dlg);
					}
				},{
					text:Ext.Msg.buttonText.ok, 
					disabled:!node.attributes.lock, 
					handler: function(){
						if (!dlg.node) return false;
						var a = dlg.node.attributes;
						var formPanel = dlg.getComponent(0);
						var form = formPanel.getForm();
						if (!form.isValid()) return false;
			
						Ext.apply(a, form.getValues());
						dlg.node.setText(a.toString());
						Viewer.getToolbar().items.get('save').enable();
						dlg.hide();
					}
				},{
					text: Ext.Msg.buttonText.cancel, 
					handler: function(){
						dlg.hide();
						Viewer.closeAction();
					}
				}],
				listeners: {
					activate: function() {
						Dialog.setBackMask();
					},
					hide: function() {
						dlg.hide();
						Viewer.closeAction();
					}
				}
	    });
		
		dlg.render(Ext.getBody());
		var form = dlg.getComponent(0).getForm();
		form.findField('xpath').treeLoader = _this.getTreeLoader(form),
	    this.spaceDlg = dlg;
	}
	else {
		if(node.attributes.lock){
			dlg.ok.enable();
		}
		else{
			dlg.ok.disable();
		}
		var formPanel = dlg.getComponent(0);
		var field = formPanel.getForm().findField('var');
		field.store.loadData(_this.getVariableArray(node));
	}
	dlg.node = node;
	var a = node.attributes;
	var form = dlg.getComponent(0).getForm();
	form.reset();
	form.setValues(a);
	var field = form.findField('var');
	field.fireEvent('select', field);
	
	dlg.center();
	dlg.show();
    return dlg;
},

//对外调用对话框
getInvokeDialog : function(node){
	var initial = '<?xml version="1.0" encoding="utf-8"?>\n'+
		'<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">\n'+
		'<!-- 在此填写 -->\n'+
		'<xsl:template match="/">\n'+
		'</xsl:template>\n'+
		'</xsl:transform>';
	
	var _this = this;
	var type = 'callout';
	var dlg = this.invokeDialog; 
	var transformPanel = new Ext.ux.AssemblePanel({
		title: '重组', 
		env: window.env, 
		extension: window.xslt_ext
	});
	var targetTree = transformPanel.getComponent('target-tree');
	var sourceTree = transformPanel.getComponent('source-tree');
	
	dlg = new Ext.Window({
		title: '对外调用',
		resizable: true,	constrain: true,	constrainHeader: true,
		minimizable: false,	maximizable: false,
		stateful: false,	modal: true,
		buttonAlign: "right",	defaultButton: 0,
		width: 640, height: 460,	minHeight: 240,	minWidth: 350,
		footer: true,
		bodyBorder: false,
		closable: true, closeAction: 'hide',
		sourceTree: sourceTree,
		targetTree: targetTree,
		layout: 'fit',
		items: [{
			region: 'center',
			xtype: 'tabpanel',
			itemId: 'tabs',
			border: false,
			style: 'border-width: 1px 0 0 0;',
			tabPosition: 'bottom',
			activeItem: 0,
			deferredRender: true,
			items: [{
				xtype: 'form',
				title: '参数',
				itemId: 'params',
				bodyStyle: 'padding: 10px;',
				defaults: {
					xtype: 'textfield',
					anchor: '100%'
				},
				items: [{
					name: 'service-name',
					disabled: true,
					fieldLabel: '服务名'
				}, {
					name: 'interface-name',
					disabled: true,
					fieldLabel: '接口名'
				}, {
					name: 'operation-name',
					disabled: true,
					fieldLabel: '操作名'
				}, {
					fieldLabel: '调用方式',
					name: 'mode',
					hiddenName: 'mode',
					xtype: 'combo',
					editable: false,
					forceSelection: true,
					mode: 'local',
					triggerAction: 'all',
					allowBlank: false,
					store: [
						['direct','直接调用(在同一线程中执行)'], 
						['wait','阻塞等待(切换线程执行调用，流程执行线程等待调用完成继续执行)'], 
						['async', '异步调用(切换线程执行调用，流程执行线程结束，调用完成通知流程继续执行)']
					]
				}, {
					fieldLabel: '请求变量',
					name: 'use-var',
					xtype: 'combo',
					forceSelection: false,
					editable: true,
					allowBlank: false,
					tpl: varList_tpl,
					store: new Ext.data.JsonStore({
						fields: ['name', 'icon'],
						data: _this.getVariableArray(node, 'element')
					}),
					mode: 'local',
					triggerAction: 'all',
					valueField: 'name',
					displayField: 'name',
					listeners: {
						change: function(field, newVal, oldVal){
							targetTree.setTitle('目标: ' + newVal);
							if (targetTree.rendered)
								targetTree.getRootNode().reload();
						}
					}
				}, {
					fieldLabel: '响应变量',
					name: 'to-var',
					xtype: 'combo',
					forceSelection: false,
					editable: true,
					allowBlank: false,
					tpl: varList_tpl,
					store: new Ext.data.JsonStore({
						fields: ['name', 'icon'],
						data: _this.getVariableArray(node, 'element')
					}),
					mode: 'local',
					triggerAction: 'all',
					valueField: 'name',
					displayField: 'name',
					listeners: {}
				},{
					xtype: 'fieldset',
					title: '模拟应答选项',
					autoHeight: true,
					autoScroll: false,
					animCollapse: false,
					collapsible: true,
					collapseFirst: false,
					items: [{
						fieldLabel: '模拟应答',
						xtype: 'radiogroup',
						columns: [90,110,110],
						vertical: true,
						itemId: 'radio-group',
						style: {'margin-top':'-4px!important'},
						name: 'radio-group',
						items: [
							{
								boxLabel: '不使用',
								name: 'is-emulator',
								width: 10,
								inputValue: 0,
								checked: true
							}, {
								boxLabel: '正确应答',
								name: 'is-emulator',
								width: 10,
								inputValue: 1
							}, {
								boxLabel: '错误应答',
								name: 'is-emulator',
								width: 10,
								inputValue: 2
							}],
							listeners: {
								change: function(obj){
									var calloutAddr = node.attributes.ref;
									var wsdlPath = calloutAddr.replace(/[\w\.]+\.xsd/ig, 'unit.wsdl');
									
									function showCombox(field) {
										var parentNode = field.getEl().dom.parentNode.parentNode.parentNode;
										field.getEl().dom.style.width = '460px';
										parentNode.setAttribute('class', 'x-form-item');
										if (!field.getValue() || field.getValue().toString() == 'undefined')
											field.setValue('');
										field.show();
									}
									
									function hideCombox(field) {
										var parentNode = field.getEl().dom.parentNode.parentNode.parentNode;
										parentNode.setAttribute('class', 'x-form-item x-hide-label');
										field.hide();
									}
									
									function showField(field){
										field.hideLabel = false;
										field.allowBlank = false;
										var lable = field.getEl().dom.parentNode.parentNode.firstChild;
										lable.style.display = 'block';
										field.getEl().dom.parentNode.setAttribute('class', '');
										field.getEl().dom.parentNode.style.paddingLeft = '135px!important;';
										field.getEl().dom.parentNode.firstChild.style.width = '100%';
										if (!field.getValue() || field.getValue().toString() == 'undefined')
											field.setValue('');
										field.show();
									};
									
									function hideField(field){
										field.allowBlank = true;
										field.hideLabel = false;
										var lable = field.getEl().dom.parentNode.parentNode.firstChild;
										lable.style.display = 'none';
										field.hide();
									};
									
									var value = (!!obj.getValue()) ? obj.getValue().inputValue : 0;
									var faultList = obj.ownerCt.get('fault-type');
									var textObj = obj.ownerCt.get('emulator-text');
									var faulTextObj = obj.ownerCt.get('emulator-fault-text');
									var faultObj = obj.ownerCt.get('fault-code');
									
									
									if (value != null) 
										switch (value) {
											case 0:
												hideCombox(faultList);
												hideField(textObj);
												hideField(faulTextObj);
												hideField(faultObj);
												break;
											case 1:
												// 生成模拟应答报文
												if (!textObj.getValue() || textObj.getValue().toString() == 'undefined') {
													Ext.Ajax.request({
														method: 'POST',
														url: 'diagram_ctrl.jsp',
														params: {
															operation: 'generateEmulatorXml',
															servicePath: servicePath,
															schemaPath: calloutAddr,
															wsdlPath: wsdlPath,
															operaName: node.attributes['operation-name']
														},
														callback: function(options, success, response){
															var data;
															if (success) 
																data = response.responseText.replace(/(\r\n)$/ig, '');
															textObj.setValue(data);
															
															hideCombox(faultList);
															showField(textObj);
															hideField(faulTextObj);
															hideField(faultObj);
														}
													});
												} else {
													hideCombox(faultList);
													showField(textObj);
													hideField(faulTextObj);
													hideField(faultObj);
												}
												break;
											case 2:
												showCombox(faultList);
												showField(faulTextObj);
												hideField(textObj);
												showField(faultObj);
												break;
											default:
												break;
										}
								}
							}
						},{
							fieldLabel: '错误类型:',
							hiddenName: 'fault-type',
							itemId: 'fault-type',
							xtype: 'combo',
							hidden: true,
							hideLabel: true,
							forceSelection: false,
							editable: false,
							allowBlank: true,
							emptyText: '<请选择错误消息类型>',
							store: new Ext.data.Store({
								url: 'diagram_ctrl.jsp',
								baseParams: {
									operation: 'getFaultList',
									servicePath: servicePath,
									wsdlPath: node.attributes.ref.replace(/[\w\.]+\.xsd/ig, 'unit.wsdl'),
									operaName: node.attributes['operation-name']
								},
								reader: new Ext.data.JsonReader({
									root: 'items',
									fields: [{name: 'faultname'}]
								})
							}),
							triggerAction: 'all',
							valueField: 'faultname',
							displayField: 'faultname',
							listeners: {
								select: function(obj) {
									var faultName = this.getValue();
									var textObj = obj.ownerCt.get('emulator-fault-text');
									var calloutAddr = node.attributes.ref;
									var wsdlPath = calloutAddr.replace(/[\w\.]+\.xsd/ig, 'unit.wsdl');
									// 生成错误模拟应答报文
									Ext.Ajax.request({
										method: 'POST',
										url: 'diagram_ctrl.jsp',
										params: {
											operation: 'generateFaultEmulatorXml',
											servicePath: servicePath,
											schemaPath: calloutAddr,
											faultName: faultName
										},
										callback: function(options, success, response){
											var data;
											if (success)
												data = response.responseText.replace(/(\r\n)$/ig, '');
											textObj.setValue(data);
										}
									});
								},
								
								expand: function() {
				                    this.list.setWidth(Math.round(this.getEl().dom.style.width.replace(/px/ig, '')) + 15 + 'px');
				                }
							}
						}, {
							fieldLabel: '模拟应答报文:',
							hidden: true,
							hideLabel: true,
							height: 80,
							xtype: 'textarea',
							itemId: 'emulator-text',
							name: 'emulator-text'
						}, {
							fieldLabel: '模拟应答报文:',
							hidden: true,
							height: 45,
							hideLabel: true,
							xtype: 'textarea',
							itemId: 'emulator-fault-text',
							name: 'emulator-fault-text'
						},{
							fieldLabel: '错误代码:',
							hidden: true,
							hideLabel: true,
							xtype: 'textfield',
							itemId: 'fault-code',
							name: 'fault-code'
						}]
				}]
			}, transformPanel, {
				xtype: 'form',
				title: 'XSLT',
				itemId: 'xslt',
				border: false,
				items: [{
					hideLabel: true,
					xtype: 'textarea',
					anchor: '100%, 100%',
					border: false,
					name: 'xslt_text',
					allowBlank: true
				}],
				tbar: [{
					text: '示例',
					handler: function(){
						dlg.get('tabs').get('xslt').getForm().findField('xslt_text').setValue(initial);
					}
				}],
				listeners: {
					activate: function(panel){
						if (!targetTree.modified) 
							return;
						var match = sourceTree.getRootNode().getPath('name');
						panel.getForm().findField('xslt_text').setValue(_this.generateXSLT(targetTree.getRootNode(), match));
						targetTree.modified = false;
					}
				}
			}]
		}],
		buttons: [{
					text: 'XPath帮助',
					handler: function(){
						Dialog.showXpathWin(dlg);
					}
				},{
					text: Ext.Msg.buttonText.ok,
					disabled:!node.attributes.lock,
					handler: function(){
						if (!dlg.node) 
							return false;
						var a = dlg.node.attributes;
						var tabs = dlg.getComponent('tabs');
						var form = tabs.get('params').getForm();
						if (!form.isValid()) 
							return false;
					
						function validateXML(txt){
							var rs;
							if (window.ActiveXObject) { // IE
								var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
								xmlDoc.async = "false";
								xmlDoc.loadXML(txt);
								
								if (xmlDoc.parseError.errorCode != 0) {
									txt = "错误代码: " + xmlDoc.parseError.errorCode + "\n";
									txt = txt + "原因: " + xmlDoc.parseError.reason;
									txt = txt + "提示: " + xmlDoc.parseError.line;
									rs = txt;
								}
							}
							else 
								if (document.implementation.createDocument) { // Mozilla, Firefox, Opera, etc.
									var parser = new DOMParser();
									var xmlDoc = parser.parseFromString(txt, "text/xml");
									
									if (xmlDoc.documentElement.nodeName == "parsererror") {
										rs = xmlDoc.documentElement.childNodes[0].nodeValue;
										rs = rs.replace(/位置.*?\.xml/ig, '').replace(/：$/ig, '。');
										rs = Ext.util.Format.htmlEncode(rs);
									}
								}
							return rs;
						};
				
						// XSLT验证
						var xsltForm = tabs.get('xslt').getForm();
						if (!!xsltForm.findField('xslt_text').el) {
							var xsltValue = xsltForm.findField('xslt_text').el.dom.value;
							if (!!xsltValue) {
								var info = Viewer.validateXML(xsltValue);
								if (!!info) {
									tabs.activate('xslt');
									Ext.Msg.alert('XSLT验证错误:', info);
									return;
								}
							}
						}
						
						var formValue = form.getValues();
						
						// 保存时格式化xml内容
						var info;
						if (formValue['is-emulator']=='2' && !!formValue['emulator-fault-text']) {
							info = validateXML(formValue['emulator-fault-text']);
							formValue['emulator-fault-text'] = formValue['emulator-fault-text'].replace(/\n/ig, '');
						}
						
						if (!!info) {
							Ext.Msg.alert('提示:', info);
							return;
						}
						
						if (formValue['is-emulator']=='1' && !!formValue['emulator-text']) {
							info = validateXML(formValue['emulator-text']);
							formValue['emulator-text'] = formValue['emulator-text'].replace(/\n/ig, '');
						}
						
						if (!!info) {
							Ext.Msg.alert('提示:', info);
							return;
						}
						
						Ext.apply(a, formValue);
						
						var xsltPanel = tabs.getComponent('xslt');
						var xsltField = xsltPanel.getForm().findField('xslt_text');
						
						if (targetTree.modified) {
							var match = sourceTree.getRootNode().getPath('name');
							xsltField.setValue(_this.generateXSLT(targetTree.getRootNode(), match));
						}
						a.xslt = xsltField.getValue();
						
						dlg.hide();
						Viewer.getToolbar().items.get('save').enable();
					}
		}, {
			text: Ext.Msg.buttonText.cancel,
			handler: function(){
				dlg.close();
			}
		}],
		tools: [{
					id: 'help',
					qtip: '配置使用帮助',
					handler: function(e, toolEl, panel, tc){
						Dialog.showHelperWin(type, dlg);
					}
				}],
				listeners: {
					activate: function() {
						Dialog.setBackMask();
					},
					hide: function() {
						dlg.hide();
						Viewer.closeAction();
					}
				}
	});
	
	dlg.render(Ext.getBody());
	
	targetTree.loader = new Ext.tree.TreeLoader({
		dataUrl: 'diagram_ctrl.jsp',
		baseParams: {
			operation: 'getDomChildren',
			service: servicePath
		},
		baseAttrs: {
			uiProvider: Ext.ux.ElementNodeUI,
			allowDrag: false
		},
		listeners: {
			beforeload: function(loader, node){
				var a = node.attributes;
				var form = dlg.getComponent('tabs').get('params').getForm();
				var field = form.findField('use-var');
				if (!field.getValue()) 
					return false;
				var index = field.store.find('name', new RegExp("^" + field.getValue() + "$"));
				
				if (index > -1) {
					var rec = field.store.getAt(index);
					var schema = rec.get('schema');
					if (schema) {
						loader.baseParams.message = 'request';
						loader.baseParams.schema = schema;
					}
					else {
						loader.baseParams.message = field.getValue();
						delete loader.baseParams.schema;
					}
				}
				else {
					loader.baseParams.message = 'request';
					loader.baseParams.schema = dlg.node.attributes.ref;
				}
			},
			load: function(loader, node, response){
				node.attributes['name'] = response.getResponseHeader('elementName');
				node.attributes['namespace'] = response.getResponseHeader('namespace');
				node.setText(response.getResponseHeader('elementName'));
				var textNode = node.getUI().textNode;
            	if (textNode.setAttributeNS){
					textNode.setAttributeNS("ext", "qtip", node.attributes['namespace']);
            	}
				else {
					textNode.setAttribute("ext:qtip", node.attributes['namespace']);
				}
				if (typeof(XML)=='function') {
					var xsltPanel = dlg.getComponent('tabs').getComponent('xslt');
					var xsltField = xsltPanel.getForm().findField('xslt_text');
					var xsl = new XML(xsltField.getValue());
					_this.reappearXSLT(node, xsl);
				}
				node.getOwnerTree().modified = false;
			},
			loadexception: function(loader, node, response){
				Viewer.showException(response);
			}
		},
		preloadChildren: true,
		clearOnLoad: false
	});
	
	this.invokeDialog = dlg;
		
	dlg.node = node;
	var a = node.attributes;	
			
	var formPanel = dlg.getComponent(0).getComponent('params');
	var form = formPanel.getForm();
	var field = form.findField('use-var');
	form.reset();
	form.setValues(a);
	dlg.targetTree.setTitle('目标: '+a['use-var']);
	if (dlg.targetTree.rendered) {
		field.fireEvent('change', field, field.getValue());
	}
	
	var sourceRoot = dlg.sourceTree.getRootNode();
    while(sourceRoot.firstChild){
        sourceRoot.removeChild(sourceRoot.firstChild).destroy();
    }
	var array = _this.getVariableArray(node, 'element');
	for (var i=0; i<array.length; i++) {
		sourceRoot.appendChild(new Ext.tree.AsyncTreeNode({
			text: array[i].name,
			name: array[i].name,
			iconCls: array[i].icon,
			schema: array[i].schema,
			ref: 'response'
		}));
	}
	
	
	// radiogroup初始化
	var emulatorObj = formPanel.getForm().findField('radio-group');
	if (a['is-emulator'] != null && 
		a['is-emulator'] != 'undefined' && 
		a['is-emulator'] != '')
		emulatorObj.setValue(a['is-emulator']);
	else
		emulatorObj.setValue(0);
		
	var xsltPanel = dlg.getComponent(0).getComponent('xslt');
	var xsltField = xsltPanel.getForm().findField('xslt_text');
	
	xsltField.setValue(a.xslt || "");
	
	dlg.setTitle(node.text);
	
	var ok = dlg.buttons[1];
	if(node.attributes.lock){
		ok.enable();
	}else{
		ok.disable();
	}
	this.invokeDialog.center();
	return dlg;
},

showConditionDialog : function(node){
	var _this = this;
	var type = 'decision';
	if(node.attributes.tag == 'while-meta' && node.attributes.iconCls == 'processLineTL_whiledo'){
			type = 'whiledo';
	} else if (node.attributes.tag == 'while-meta' && node.attributes.iconCls == 'processLineBL_dowhile'){
			type = 'dowhile';
	}
	if (!this.conditionDlg) {
		var titleText = '条件表达式';
	    var dlg = new Ext.Window({
	        autoCreate : true,
	        title: titleText,
	        resizable:true, constrain:true, constrainHeader:true,
	        minimizable: false, maximizable: false,
	        stateful: false,  modal: true,
	        buttonAlign: "right",	defaultButton: 0,
	        width: 460, height: 320,
	        minHeight: 220, minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				id: 'conditionform',
				region: 'north',
				xtype: 'form',
				autoScroll: true,
				labelWidth: 75, labelAlign: 'top',
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false },
				items: [
					{
						fieldLabel: '表达式',
						name: 'xpath',
						allowBlank: false,
						xtype: 'textarea'
				    },{
						fieldLabel: '可选变量',
				        name: 'var',
						emptyText: '<在此选择变量插入表达式>',
						xtype: 'combo',
						editable: false, forceSelection: true,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon', 'schema'],
						    data : _this.getVariableArray(node)
						}),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'name', displayField: 'name',
						listeners: {
							select: function(sender, record, index){
								var form = sender.ownerCt.getForm(), value = sender.getValue();
								var field = form.findField('xpath');
								
								if (!!value) {
									var currentValue = field.getValue();
									if (currentValue == undefined) value = '';
									else if (!!currentValue && currentValue.indexOf(value) != -1)
										currentValue = '';
									field.setValue(currentValue + ' $' + value);
								}
							}
						}
				    },{
						fieldLabel: '说明',
				    	name: 'comment',
						xtype: 'textarea'
				    }]
				}],
				tools: [{
					id: 'help',
					qtip: '配置使用帮助',
					handler: function(e, toolEl, panel, tc){
						Dialog.showHelperWin(type, dlg);
					}
				}],
				buttons: [{
					text: 'XPath帮助',
					ref: '../help',
					handler: function(){
						Dialog.showXpathWin(dlg);
					}
				},{
					text: Ext.Msg.buttonText.ok, 
					disabled:!node.attributes.lock, 
					ref: '../ok',
					handler: function(){
						if (!dlg.node) return false;
						var a = dlg.node.attributes;
						var condPanel = dlg.getComponent(0);
						var form = condPanel.getForm();
						if (!form.isValid()) return false;
			
						Ext.apply(a, form.getValues());
						dlg.node.setText(a.toString());
						dlg.node.getUI().toggleCheck(a['var']!='' && a['xpath']!='');
						Viewer.getToolbar().items.get('save').enable();
						dlg.hide();
					}
				},{
					text: Ext.Msg.buttonText.cancel,
					handler: function(){
						dlg.hide();
						Viewer.closeAction();
					}
				}],
				listeners: {
					activate: function() {
						Dialog.setBackMask();
					},
					hide: function() {
						dlg.hide();
						Viewer.closeAction();
					}
				}
	    });
		
		dlg.render(Ext.getBody());
	    this.conditionDlg = dlg;
	}
	else {
		this.conditionDlg.ok.setDisabled(!node.attributes.lock);
		var formPanel = this.conditionDlg.getComponent(0);
		var field = formPanel.getForm().findField('var');
		field.store.loadData(_this.getVariableArray(node));
	}
	this.conditionDlg.node = node;
	var a = node.attributes;
	var formPanel = this.conditionDlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	
	this.conditionDlg.center();
	this.conditionDlg.show();
    return this.conditionDlg;
},

//变量赋值对话框
showAssignDialog : function(node){
	var _this = this;
	
	if (!this.declareDlg) {
		var titleText = '赋值';
		var type = 'assign';
		var array=_this.getVariableArray(node);
		array.unshift({name:'', icon:''});
	    var dlg = new Ext.Window({
	        autoCreate : true,
	        iconCls: 'palette_declare_var',
	        title: titleText,
	        constrain:true,  constrainHeader:true,
	        minimizable: false,  maximizable: false,  modal: true,
	        resizable:true, stateful: false,
	        buttonAlign: "right",
			defaultButton: 0,
	        width: 450, height: 300,
	        minHeight: 200, minWidth: 300,
	        closable: true,
	        closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				xtype: 'form',
				autoScroll: true,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false },
				items: [
					{
						fieldLabel: '变量名称',
						name: 'to-var',
						allowBlank: false
				    },{
						fieldLabel: '源变量',
				        name: 'var',
						xtype: 'combo',
						forceSelection: true, editable: false,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon', 'schema'],
						    data : array
						}),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'name', displayField: 'name',
						listeners: {
							select: function(sender, record, index){
								var formPanel = sender.ownerCt, value = sender.getValue();
								var form=formPanel.getForm();
								var field=form.findField('raw-value');
								field.setDisabled(value);
								field=form.findField('type');
								field.setDisabled(value);
								field=form.findField('clone');
								field.setDisabled(!value);
								field=form.findField('xpath');
								field.setDisabled(!value);
								if (field.reloadTree) field.reloadTree();
							}
						}
					},{
						name: 'xpath',
						xtype: 'treecombo',
						fieldLabel: 'XPath',
						disabled: true
				    },{
						xtype: 'combo',
						fieldLabel: '克隆',
						name: 'clone',
						hiddenName: 'clone',
						forceSelection: true, editable: false,
						store: ['true', 'false'],
						mode: 'local', triggerAction: 'all',
						disabled: true
				    },{
						fieldLabel: '类型',
				        name: 'type',
						hiddenName: 'type',
						xtype: 'combo',
						allowBlank: false,
						forceSelection: true, editable: false,
						store: _this.getTypeStore(),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'value', displayField: 'label'
				    },{
						name: 'raw-value',
						xtype: 'textarea',
						fieldLabel: '值'
				    }]
				}
			],
			tools: [{
				id: 'help',
				qtip: '配置使用帮助',
				handler: function(e, toolEl, panel, tc){
					Dialog.showHelperWin(type, dlg);
				}
			}],
			buttons: [{
				text: 'XPath帮助',
				handler: function(){
					Dialog.showXpathWin(dlg);
				}
			}],
			listeners : {
				activate : function() {
					Dialog.setBackMask();
				},
				hide: function() {
					dlg.hide();
					Viewer.closeAction();
				}
			}
	    });
		
	    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:!node.attributes.lock}, function(){ 
			if (!dlg.node) return false;
			var a = dlg.node.attributes;
			var formPanel = dlg.getComponent(0);
			var form = formPanel.getForm();		
			if (!form.isValid()) return false;
			
			// element需要进行xml验证
			if (form.getValues()['type'] == 'element') {
				var info = Viewer.validateXML(form.getValues()['raw-value']);
				if (!!info) {
					Ext.Msg.alert('提示:', info);
					return;
				}
			}

			Ext.apply(a, form.getValues());
			dlg.node.setText(a.toString());
			Viewer.getToolbar().items.get('save').enable();
			dlg.hide();
		});
	    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
			dlg.hide();
			Viewer.closeAction();
		});
	    
		dlg.render(Ext.getBody());
		var form = dlg.getComponent(0).getForm();
		form.findField('xpath').treeLoader = _this.getTreeLoader(form),
	    this.declareDlg = dlg;
	}
	else {
		if(node.attributes.lock){
			this.declareDlg.ok.enable();
		}else{
			this.declareDlg.ok.disable();
		}
		
		var form = this.declareDlg.getComponent(0).getForm();
		var field = form.findField('var');
		var array=_this.getVariableArray(node);
		array.unshift({name:'', icon:''});
		field.store.loadData(array);
	}
	this.declareDlg.node = node;
	var a = node.attributes;
	var formPanel = this.declareDlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	var field = formPanel.getForm().findField('var');
	field.fireEvent('select', field);
	
	this.declareDlg.center();
	this.declareDlg.show();
    return this.declareDlg;
},

//删除子元素对话框
getDeleteDialog : function(node){
	var _this = this;
	
	if (!this.deleteDlg) {
		var titleText = '删除';
		var type = 'delete';
	    var dlg = new Ext.Window({
	        autoCreate : true,
	        title: titleText,
	        resizable:true, constrain:true, constrainHeader:true,
	        minimizable: false, maximizable: false,
	        stateful: false, modal: true,
	        buttonAlign: "right",	defaultButton: 0,
	        width: 450, height: 150,
	        minHeight: 300,	minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				xtype: 'form',
				autoScroll: true,
				height: 90,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false },
				items: [
					{
						fieldLabel: '变量',
				        name: 'var',
						xtype: 'combo',
						allowBlank: false,
						forceSelection: false, editable: false,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon', 'schema'],
						    data : _this.getVariableArray(node, 'element')
						}),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'name', displayField: 'name',
						listeners: {
							select: function(sender, record, index){
								var form = sender.ownerCt.getForm(), value = sender.getValue();
								var field=form.findField('xpath');
								if (field.reloadTree) field.reloadTree();
							}
						}
				    },{
						name: 'xpath',
						allowBlank: false,
						xtype: 'treecombo',
						fieldLabel: 'xpath'
				    }]
				}],
				tools: [{
					id: 'help',
					qtip: '配置使用帮助',
					handler: function(e, toolEl, panel, tc){
						Dialog.showHelperWin(type, dlg);
					}
				}],
				buttons: [{
					text: 'XPath帮助',
					handler: function(){
						Dialog.showXpathWin(dlg);
					}
				}],
				listeners: {
					activate: function() {
						Dialog.setBackMask();
					},
					hide: function() {
						dlg.hide();
						Viewer.closeAction();
					}
				}
	    });
		
	    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:!node.attributes.lock}, function(){ 
			if (!dlg.node) return false;
			var a = dlg.node.attributes;
			var condPanel = dlg.getComponent(0);
			var form = condPanel.getForm();
			if (!form.isValid()) return false;

			Ext.apply(a, form.getValues());
			dlg.node.setText(a.toString());
			Viewer.getToolbar().items.get('save').enable();
			dlg.hide();
		});
	    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
			dlg.hide(); 
			Viewer.closeAction();
		});
	    
		dlg.render(Ext.getBody());
		var form = dlg.getComponent(0).getForm();
		form.findField('xpath').treeLoader = _this.getTreeLoader(form),
	    this.deleteDlg = dlg;
	}
	else {
		if(node.attributes.lock){
			this.deleteDlg.ok.enable();
		}else{
			this.deleteDlg.ok.disable();
		}
		var formPanel = this.deleteDlg.getComponent(0);
		var field = formPanel.getForm().findField('var');
		field.store.loadData(_this.getVariableArray(node, 'element'));
	}
	this.deleteDlg.node = node;
	var a = node.attributes;
	var formPanel = this.deleteDlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	var field = form.findField('var');
	field.fireEvent('select', field);
	
	this.deleteDlg.center();
    return this.deleteDlg;
},

// 脚本对话框
getScriptDialog: function(node) {
	var _this = this;
	if (!this.scriptDlg) {
		var titleText = '脚本';
		var type = 'scription';
		
		var dlg = new Ext.Window({
	        autoCreate : true,
	        title: titleText,
	        resizable:true, constrain:true, constrainHeader:true,
	        minimizable: false, maximizable: false,
	        stateful: false, modal: true,
	        buttonAlign: "right",	defaultButton: 0,
	        width: 580, height: 450,
	        minHeight: 550,	minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				xtype: 'form',
				id: 'scriptForm',
				autoScroll: true,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaults: { anchor: '-18', stateful: false},
				defaultType: 'textfield',
				items: [{
					name: 'scriptDesc',
					fieldLabel: '脚本描述',
					allowBlank: false
				},{
					name: 'scriptCode',
					xtype: 'textarea',
					style: 'line-height:1.5;font-family:Arial,sans-serif;font-size:14px;',
					fieldLabel: '脚本代码',
					height: 320,
					autoScroll: true,
					allowBlank: false
				}]
			}],
			tools: [{
				id: 'help',
				qtip: '配置使用帮助',
				handler: function(e, toolEl, panel, tc){
					Dialog.showHelperWin(type, dlg);
				}
			}],
			listeners: {
				activate: function() {
					Dialog.setBackMask();
				},
				hide: function() {
					dlg.hide();
					Viewer.closeAction();
				}
			}
		});
		
		dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:!node.attributes.lock}, function(){ 
			if (!dlg.node) return false;
			var a = dlg.node.attributes;
			var condPanel = dlg.getComponent(0);
			var form = condPanel.getForm();
			if (!form.isValid()) return false;

			Ext.apply(a, form.getValues());
			dlg.node.setText(a.toString());
			Viewer.getToolbar().items.get('save').enable();
			dlg.hide();
		});
	    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
			dlg.hide(); 
			Viewer.closeAction();
		});
	    
		dlg.render(Ext.getBody());
	    this.scriptDlg = dlg;
	} else {
		if(node.attributes.lock){
			this.scriptDlg.ok.enable();
		}else{
			this.scriptDlg.ok.disable();
		}
		// TODO
	}
	
	this.scriptDlg.node = node;
	var a = node.attributes;
	var formPanel = this.scriptDlg.getComponent('scriptForm');
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	
	// 窗口居中显示
	this.scriptDlg.center();
    return this.scriptDlg;
},

//日志对话框
getLoggerDialog : function(node){
	var _this = this;
	
	if (!this.loggerDlg) {
		var titleText = '日志';
		var type = 'log';
		
	    var dlg = new Ext.Window({
	        autoCreate : true,
	        title: titleText,
	        resizable:true, constrain:true, constrainHeader:true,
	        minimizable: false, maximizable: false,
			stateful: false, modal: true,
	        buttonAlign: "right", defaultButton: 0,
	        width: 450, height: 205,
	        minHeight: 200,	minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				id: 'loggerform',
				xtype: 'form',
				autoScroll: true,
				height: 100,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false},
				items: [
					{
						fieldLabel: '名称',
						name: 'name',
						xtype: 'hidden'
				    },{
						fieldLabel: '变量',
				        name: 'var',
						xtype: 'combo',
						emptyText: '<选择变量后编辑提示信息>',
						allowBlank: true,
						forceSelection: false, editable: false,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon'],
						    data : _this.getVariableArray(node)
						}),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'name', displayField: 'name',
						listeners: {
							select: function(sender, record, index){
								var form = sender.ownerCt.getForm(), value = sender.getValue();
								var field = form.findField('msg');
								
								if (!!value) {
									var currentValue = field.getValue();
									if (currentValue == undefined) value = '';
									else if (!!currentValue && currentValue.indexOf(value) != -1)
										currentValue = '';
									field.setValue(currentValue+' {$' + value + '}');
								}
							}
						}
				    },{
						fieldLabel: '日志级别',
						hiddenName: 'level',
						xtype: 'combo',
						allowBlank: false,
						forceSelection: true, editable: false,
						store: [['debug','调试'], ['warn','警告'], ['info','提示'], ['error','错误'], ['fatal','重大错误']],
						mode: 'local',
						triggerAction: 'all'
				    },{
						name: 'msg',
						xtype: 'textarea',
						fieldLabel: '提示信息'
				    }]
				}],
				tools: [{
					id: 'help',
					qtip: '配置使用帮助',
					handler: function(e, toolEl, panel, tc){
						Dialog.showHelperWin(type, dlg);
					}
				}],
				buttons: [{
					text: 'XPath帮助',
					handler: function(){
						Dialog.showXpathWin(dlg);
					}
				}],
				listeners: {
					activate: function() {
						Dialog.setBackMask();
					},
					hide: function() {
						dlg.hide();
						Viewer.closeAction();
					}
				}
	    });
		
	    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:!node.attributes.lock}, function(){ 
			if (!dlg.node) return false;
			var a = dlg.node.attributes;
			var formPanel = dlg.getComponent('loggerform');
			var form = formPanel.getForm();
			if (!form.isValid()) return false;

			Ext.apply(a, form.getValues());
			dlg.node.setText(a.toString());
			Viewer.getToolbar().items.get('save').enable();
			dlg.hide();
		});
	    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
			dlg.hide();
			Viewer.closeAction();
		});
	    
		dlg.render(Ext.getBody());
	    this.loggerDlg = dlg;
	}
	else {
		if(node.attributes.lock){
			this.loggerDlg.ok.enable();
		}else{
			this.loggerDlg.ok.disable();
		}
		var formPanel = this.loggerDlg.getComponent(0);
		var field = formPanel.getForm().findField('var');
		field.store.loadData(_this.getVariableArray(node));
	}
	this.loggerDlg.node = node;
	var a = node.attributes;
	var formPanel = this.loggerDlg.getComponent('loggerform');
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	
	// 窗口居中显示
	this.loggerDlg.center();
    return this.loggerDlg;
},

//SQL对话框
getSQLDialog : function(node){
	var _this = this;
	var type = 'db';
	var dlg = this.sqlDlg;
	if (!dlg) {
		
	    dlg = new Ext.Window({
	        autoCreate : true,
	        title: 'SQL 操作',
	        resizable:true, constrain:true, constrainHeader:true,
	        minimizable: false, maximizable: false,
			stateful: false, modal: true,
	        buttonAlign: "right", defaultButton: 0,
	        width: 450, height: 300,
	        minHeight: 200,	minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				xtype: 'form',
				autoScroll: true,
				height: 100,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false},
				items: [
					{
						fieldLabel: '数据源',
						hiddenName: 'dsn',
						xtype: 'combo',
						allowBlank: false,
						editable: true, forceSelection: false,
						store: this.getDSNStore(),
						mode: 'remote',	triggerAction: 'all',
						valueField: 'name', displayField: 'name'
				    },{
						fieldLabel: 'SQL语句',
						xtype: 'textarea',
						name: 'sql',
						allowBlank: false,
						height: 100
				    },{
						fieldLabel: '参数变量',
				        name: 'var',
						xtype: 'combo',
						allowBlank: true,
						editable: false,
						forceSelection: false,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon'],
						    data : _this.getVariableArray(node)
						}),
						mode: 'local',	triggerAction: 'all',
						valueField: 'name', displayField: 'name'
				    },{
						fieldLabel: '输出变量',
				        name: 'to-var',
						xtype: 'combo',
						allowBlank: false,
						editable: true,
						forceSelection: false,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon'],
						    data : _this.getVariableArray(node)
						}),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'name', displayField: 'name'
				    }]
				}
			],
			buttons: [{
					text: 'XPath帮助',
					handler: function(){
						Dialog.showXpathWin(dlg);
					}
				},{
		    	text: Ext.Msg.buttonText.ok, 
		    	disabled:!node.attributes.lock,
		    	handler: function(){
					if (!dlg.node) return false;
					var a = dlg.node.attributes;
					var formPanel = dlg.getComponent(0);
					var form = formPanel.getForm();
					if (!form.isValid()) return false;
		
					Ext.apply(a, form.getValues());
					dlg.node.setText(a.toString());
					Viewer.getToolbar().items.get('save').enable();
					dlg.hide();
				}
			},{
				text: Ext.Msg.buttonText.cancel,
				handler: function(){ dlg.hide(); }
			}],
				tools: [{
					id: 'help',
					qtip: '配置使用帮助',
					handler: function(e, toolEl, panel, tc){
						Dialog.showHelperWin(type, dlg);
					}
				}],
				listeners: {
					activate: function() {
						Dialog.setBackMask();
					},
					hide: function() {
						dlg.hide();
						Viewer.closeAction();
					}
				}
	    });
		
		dlg.render(Ext.getBody());
	    this.sqlDlg = dlg;
	}
	else {
		var ok = dlg.buttons[1];
		if(node.attributes.lock){
			ok.enable();
		}else{
			ok.disable();
		}
		var formPanel = dlg.getComponent(0);
		var field = formPanel.getForm().findField('var');
		field.store.loadData(_this.getVariableArray(node));
		field = formPanel.getForm().findField('to-var');
		field.store.loadData(_this.getVariableArray(node));
	}
	dlg.node = node;
	var a = node.attributes;
	var form = dlg.getComponent(0).getForm();
	form.reset();
	form.setValues(a);
	
	var inXact = false;
	for (var p=node.parentNode; p!=null; p=p.parentNode) {
		if (p.attributes.tag == 'transaction') {
			inXact=true;    form.findField('dsn').setValue(node.attributes.dsn);
			break;
		}
	}
	form.findField('dsn').setDisabled(false);
	if (inXact==false) {
		form.findField('dsn').store.reload();
	}
	
	this.sqlDlg.center();
    return dlg;
},

//异常对话框
showCatchDialog : function(node){
	var _this = this;
	var type = 'onexception';
	var dlg = this.catchDlg;
	if (!dlg) {
		
	    dlg = new Ext.Window({
	        autoCreate : true,
	        title: '异常捕捉',
	        resizable:true, constrain:true, constrainHeader:true,
	        minimizable: false, maximizable: false,
			stateful: false, modal: true,
	        buttonAlign: "right", defaultButton: 0,
	        width: 450, height: 180,
	        minHeight: 200,	minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				xtype: 'form',
				autoScroll: true,
				height: 100,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false},
				items: [
					{
						fieldLabel: '异常代码',
						name: 'exception-key',
						xtype: 'textfield',
						value: '',
						readOnly: false
				    },{
						fieldLabel: '是否抛出',
						hiddenName: 'throw',
						xtype: 'combo',
						allowBlank: false,
						editable: false,
						forceSelection: true,
						store: [['false','不抛出'], ['true','重新抛出']],
						mode: 'local',
						triggerAction: 'all'
				    }]
				}
			],
			tools: [{
					id: 'help',
					qtip: '配置使用帮助',
					handler: function(e, toolEl, panel, tc){
						Dialog.showHelperWin(type, dlg);
					}
				}],
			buttons: [{
		    	text: Ext.Msg.buttonText.ok, 
		    	disabled:!node.attributes.lock,
		    	handler: function(){
					if (!dlg.node) return false;
					var a = dlg.node.attributes;
					var formPanel = dlg.getComponent(0);
					var form = formPanel.getForm();
					if (!form.isValid()) return false;
		
					Ext.apply(a, form.getValues());
					dlg.node.setText(a.toString());
					Viewer.getToolbar().items.get('save').enable();
					dlg.hide();
		    	}
			},{
				text: Ext.Msg.buttonText.cancel, 
				handler: function(){ dlg.hide(); }
			}],
				listeners : {
					activate : function() {
						Dialog.setBackMask();
					},
					hide: function() {
						dlg.hide();
						Viewer.closeAction();
					}
				}
	    });
	    
		dlg.render(Ext.getBody());
	    this.catchDlg = dlg;
	}
	
	var ok = dlg.buttons[0];
	if(node.attributes.lock){
		ok.enable();
	}else{
		ok.disable();
	}
	dlg.node = node;
	var a = node.attributes;
	var formPanel = dlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	
	this.catchDlg.center();
    this.catchDlg.show();
},

//超时对话框
showTimeoutDialog : function(node){
	var _this = this;
	var dlg = this.timeoutDlg;
	if (!dlg) {
		
	    dlg = new Ext.Window({
	        autoCreate : true,
	        title: '超时',
	        resizable:true, constrain:true, constrainHeader:true,
	        minimizable: false, maximizable: false,
			stateful: false, modal: true,
	        buttonAlign: "right", defaultButton: 0,
	        width: 450, height: 180,
	        minHeight: 200,	minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				xtype: 'form',
				autoScroll: true,
				height: 100,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false},
				items: [
					{
						fieldLabel: '超时时间(秒)',
						name: 'timeout',
						xtype: 'numberfield',
						value: '30',
						allowBlank: false,
						allowDecimals: false
				    },{
						fieldLabel: '序号',
						name: 'index',
						xtype: 'numberfield',
						allowBlank: false,
						allowDecimals: false
				    },{
						fieldLabel: '执行方式',
						hiddenName: 'instant',
						xtype: 'combo',
						allowBlank: false,
						editable: false,
						forceSelection: true,
						store: [['true','立即执行(执行出错时记录日志)'], ['false','异步执行(记录日志由冲正组件执行)']],
						mode: 'local',
						triggerAction: 'all'
				    }]
				}
			],
			tools: [{
					id: 'help',
					qtip: '配置使用帮助',
					handler: function(e, toolEl, panel, tc){
						Dialog.showHelperWin(type, dlg);
					}
				}],
			buttons: [{
		    	text: Ext.Msg.buttonText.ok, 
		    	disabled:!node.attributes.lock,
		    	handler: function(){
					if (!dlg.node) return false;
					var a = dlg.node.attributes;
					var formPanel = dlg.getComponent(0);
					var form = formPanel.getForm();
					if (!form.isValid()) return false;
		
					Ext.apply(a, form.getValues());
					dlg.node.setText(a.toString());
					Viewer.getToolbar().items.get('save').enable();
					dlg.hide();
		    	}
			},{
				text: Ext.Msg.buttonText.cancel, 
				handler: function(){ dlg.hide(); }
			}],
				listeners : {
					activate : function() {
						Dialog.setBackMask();
					},
					hide: function() {
						dlg.hide();
						Viewer.closeAction();
					}
				}
	    });
	    
		dlg.render(Ext.getBody());
	    this.timeoutDlg = dlg;
	}
	
	var ok = dlg.buttons[0];
	if(node.attributes.lock){
		ok.enable();
	}else{
		ok.disable();
	}
	dlg.node = node;
	var a = node.attributes;
	var formPanel = dlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	
	this.timeoutDlg.center();
    this.timeoutDlg.show();
},

//回滚对话框
showRollbackDialog : function(node){
	var _this = this;
	var type = 'onexception';
	var dlg = this.exceptionDlg;
	if (!dlg) {
		
	    dlg = new Ext.Window({
	        autoCreate : true,
	        title: '回滚处理',
	        resizable:true, constrain:true, constrainHeader:true,
	        minimizable: false, maximizable: false,
			stateful: false, modal: true,
	        buttonAlign: "right", defaultButton: 0,
	        width: 450, height: 180,
	        minHeight: 200,	minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				xtype: 'form',
				autoScroll: true,
				height: 100,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false},
				items: [
					{
						fieldLabel: '异常代码',
						name: 'exception-key',
						xtype: 'textfield',
						value: '',
						readOnly: false
				    },{
						fieldLabel: '序号',
						name: 'index',
						xtype: 'numberfield',
						allowBlank: false,
						allowDecimals: false
				    },{
						fieldLabel: '执行方式',
						hiddenName: 'instant',
						xtype: 'combo',
						allowBlank: false,
						editable: false,
						forceSelection: true,
						store: [['true','立即执行(执行出错时记录日志)'], ['false','异步执行(记录日志由冲正组件执行)']],
						mode: 'local',
						triggerAction: 'all'
				    }]
				}
			],
			tools: [{
					id: 'help',
					qtip: '配置使用帮助',
					handler: function(e, toolEl, panel, tc){
						Dialog.showHelperWin(type, dlg);
					}
				}],
			buttons: [{
		    	text: Ext.Msg.buttonText.ok, 
		    	disabled:!node.attributes.lock,
		    	handler: function(){
					if (!dlg.node) return false;
					var a = dlg.node.attributes;
					var formPanel = dlg.getComponent(0);
					var form = formPanel.getForm();
					if (!form.isValid()) return false;
		
					Ext.apply(a, form.getValues());
					dlg.node.setText(a.toString());
					Viewer.getToolbar().items.get('save').enable();
					dlg.hide();
		    	}
			},{
				text: Ext.Msg.buttonText.cancel, 
				handler: function(){ dlg.hide(); }
			}],
				listeners : {
					activate : function() {
						Dialog.setBackMask();
					},
					hide: function() {
						dlg.hide();
						Viewer.closeAction();
					}
				}
	    });
	    
		dlg.render(Ext.getBody());
	    this.exceptionDlg = dlg;
	}
	
	var ok = dlg.buttons[0];
	if(node.attributes.lock){
		ok.enable();
	}else{
		ok.disable();
	}
	dlg.node = node;
	var a = node.attributes;
	var formPanel = dlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	
	this.exceptionDlg.center();
	this.exceptionDlg.show();
    return dlg;
},

getForeachDialog : function(node){
	var _this = this;
	
	if (!this.foreachDlg) {
		var titleText = '遍历';
		var type = 'foreach';
	    var dlg = new Ext.Window({
	        autoCreate : true,
	        title: titleText,
	        resizable:true, constrain:true, constrainHeader:true,
			minimizable: false, maximizable: false,
	        stateful: false, modal: true,
	        buttonAlign: "right",	defaultButton: 0,
	        width: 450, height: 180,
	        minHeight: 300, minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				xtype: 'form',
				autoScroll: true,
				height: 110,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18' , stateful: false},
				items: [
					{
						name: 'as',
						allowBlank: false,
						fieldLabel: '循环变量'
				    },{
						fieldLabel: '遍历对象',
				        name: 'var',
						xtype: 'combo',
						editable: false,
						allowBlank: false,
						forceSelection: false,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon', 'schema'],
						    data : _this.getVariableArray(node)
						}),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'name', displayField: 'name',
						listeners: {
							select: function(sender, record, index){
								var form = sender.ownerCt.getForm();
								var field=form.findField('xpath');
								if (field.reloadTree) field.reloadTree();
							}
						}
				    },{
						name: 'xpath',
						allowBlank: false,
						xtype: 'treecombo',
						fieldLabel: 'XPath'
				    }]
				}],
				tools: [{
					id: 'help',
					qtip: '配置使用帮助',
					handler: function(e, toolEl, panel, tc){
						Dialog.showHelperWin(type, dlg);
					}
				}],
				buttons: [{
					text: 'XPath帮助',
					handler: function(){
						Dialog.showXpathWin(dlg);
					}
				}],
				listeners : {
					activate : function() {
						Dialog.setBackMask();
					},
					hide: function() {
						dlg.hide();
						Viewer.closeAction();
					}
				}
	    });
		
	    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:!node.attributes.lock}, function(){ 
			if (!dlg.node) return false;
			var a = dlg.node.attributes;
			var condPanel = dlg.getComponent(0);
			var form = condPanel.getForm();
			if (!form.isValid()) return false;

			Ext.apply(a, form.getValues());
			dlg.node.setText(a.toString());
			Viewer.getToolbar().items.get('save').enable();
			dlg.hide();
		});
	    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
			dlg.hide();
			Viewer.closeAction();
		});
	    
		dlg.render(Ext.getBody());
		var form = dlg.getComponent(0).getForm();
		form.findField('xpath').treeLoader = _this.getTreeLoader(form),
	    this.foreachDlg = dlg;
	}
	else {
		if(node.attributes.lock){
			this.foreachDlg.ok.enable();
		}else{
			this.foreachDlg.ok.disable();
		}
		var formPanel = this.foreachDlg.getComponent(0);
		var field = formPanel.getForm().findField('var');
		field.store.loadData(_this.getVariableArray(node));
	}
	this.foreachDlg.node = node;
	var a = node.attributes;
	var formPanel = this.foreachDlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	var field = form.findField('var');
	field.fireEvent('select', field);
	
	this.foreachDlg.center();
    return this.foreachDlg;
},

getRenameDialog : function(node){
	var _this = this;
	
	if (!this.renameDlg) {
		var titleText = '元素更名';
		var type = 'rename';
	    var dlg = new Ext.Window({
	        autoCreate : true,
	        title: titleText,
	        resizable:false, constrain:true, constrainHeader:true,
	        minimizable: false, maximizable: false,
	        stateful: false, modal: true,
	        buttonAlign: "right", defaultButton: 0,
	        width: 450, height: 180,
			minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				xtype: 'form',
				autoScroll: true,
				height: 130,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18' , stateful: false},
				items: [
					{
						fieldLabel: '变量',
				        name: 'var',
						xtype: 'combo',
						allowBlank: false,
						forceSelection: false,
						editable: false,
						tpl: varList_tpl,
						store:  new Ext.data.JsonStore({
						    fields: ['name', 'icon'],
						    data : _this.getVariableArray(node)
						}),
						mode: 'local',
						triggerAction: 'all',
						valueField: 'name', displayField: 'name',
						listeners: {
							select: function(sender, record, index){
								var form = sender.ownerCt.getForm(), value = sender.getValue();
								var field=form.findField('xpath');
								if (field.reloadTree) field.reloadTree();
							}
						}
				    },{
						name: 'xpath',
						xtype: 'treecombo',
						fieldLabel: 'xpath'
				    },{
						name: 'new-name',
						allowBlank: false,
						fieldLabel: '新名称',
						regex: ElementName.regex
				    }]
				}
			],
			tools: [{
				id: 'help',
				qtip: '配置使用帮助',
				handler: function(e, toolEl, panel, tc){
					Dialog.showHelperWin(type, dlg);
				}
			}],
			buttons: [{
				text: 'XPath帮助',
				handler: function(){
					Dialog.showXpathWin(dlg);
				}
			}],
			listeners : {
				activate : function() {
					Dialog.setBackMask();
				},
				hide: function() {
					dlg.hide();
					Viewer.closeAction();
				}
			}
	    });
		
	    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:!node.attributes.lock}, function(){ 
			if (!dlg.node) return false;
			var a = dlg.node.attributes;
			var formPanel = dlg.getComponent(0);
			var form = formPanel.getForm();
			if (!form.isValid()) return false;

			Ext.apply(a, form.getValues());
			dlg.node.setText(a.toString());
			Viewer.getToolbar().items.get('save').enable();
			dlg.hide();
		});
	    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
			dlg.hide();
			Viewer.closeAction();
		});
	    
		dlg.render(Ext.getBody());
		
		var form = dlg.getComponent(0).getForm();
		form.findField('xpath').treeLoader = _this.getTreeLoader(form),
	    this.renameDlg = dlg;
	}
	else {
		if(node.attributes.lock){
			this.renameDlg.ok.enable();
		}else{
			this.renameDlg.ok.disable();
		}
		var formPanel = this.renameDlg.getComponent(0);
		var field = formPanel.getForm().findField('var');
		field.store.loadData(_this.getVariableArray(node));
	}
	this.renameDlg.node = node;
	var a = node.attributes;
	var formPanel = this.renameDlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	var field = form.findField('var');
	field.fireEvent('select', field);
	
    return this.renameDlg;
},

showThrowDialog : function(node){
	var _this = this;
	
    var dlg = new Ext.Window({
        autoCreate : true,
        title: '抛出异常',
        resizable:false,
        constrain:true,
        constrainHeader:true,
        minimizable: false,
        maximizable: false,
        stateful: false,
        modal: true,
        buttonAlign: "right",
		defaultButton: 0,
        width: 450,
        height: 140,
        minHeight: 140,
		minWidth: 300,
        footer: true,
        closable: true,
		plain: true,
		layout: 'fit',
		items: [{
			xtype: 'form',
			autoScroll: true,
			height: 110,
			labelWidth: 100,
			border: false,
			bodyStyle: 'padding:5px;',
			defaultType: 'textfield',
			defaults: { anchor: '-18' , stateful: false},
			items: [{
				fieldLabel: '异常代码',
				name: 'exception-key',
				xtype: 'textfield',
				allowBlank: false
		    },{
				fieldLabel: '异常信息',
				name: 'message',
				xtype: 'textfield'
		    }]
		}],
		tools: [{
			id: 'help',
			qtip: '配置使用帮助',
			handler: function(e, toolEl, panel, tc){
				Dialog.showHelperWin('throw', dlg);
			}
		}],
		buttons: [{
			text: Ext.Msg.buttonText.ok, 
			disabled:!node.attributes.lock, 
			handler: function(){
				if (!dlg.node) return false;
				var a = dlg.node.attributes;
				var formPanel = dlg.getComponent(0);
				var form = formPanel.getForm();
				if (!form.isValid()) return false;
	
				Ext.apply(a, form.getValues());
				dlg.node.setText(a.toString());
				Viewer.getToolbar().items.get('save').enable();
				dlg.close();
			}
		},{
			text: Ext.Msg.buttonText.cancel, 
			handler: function(){
				dlg.close();
				//Viewer.closeAction();
			}
		}],
		listeners: {
			activate: function() {
				Dialog.setBackMask();
			},
			hide: function() {
				Viewer.closeAction();
			}
		}
    });
	
	dlg.render(Ext.getBody());
	dlg.node = node;
	var a = node.attributes;
	var formPanel = dlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	
	dlg.show();
    return dlg;
},

getSleepDialog : function(node){
	var _this = this;
	
	if (!this.sleepDlg) {
		var titleText = '休眠';
		var type = 'sleep';
	    var dlg = new Ext.Window({
	        autoCreate : true,
	        title: titleText,
	        resizable:false,
	        constrain:true,
	        constrainHeader:true,
	        minimizable: false,
	        maximizable: false,
	        stateful: false,
	        modal: true,
	        buttonAlign: "right",
			defaultButton: 0,
	        width: 450,
	        height: 130,
	        minHeight: 140,
			minWidth: 300,
	        footer: true,
	        closable: true,
	        closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				id: 'sleepform',
				xtype: 'form',
				autoScroll: true,
				height: 110,
				labelWidth: 100,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18' , stateful: false},
				items: [
					{
						name: 'wait',
						xtype: 'numberfield',
						emptyText: '<请输入睡眠时间,最大值不超过30分钟(1800000)>',
						allowDigits: false,
						fieldLabel: '休眠时间(毫秒)',
						maxValue: 1800000,
						maxText: '已超出最大休眠时间设定,请重新输入'
				    }]
				}
			],
			tools: [{
				id: 'help',
				qtip: '配置使用帮助',
				handler: function(e, toolEl, panel, tc){
					Dialog.showHelperWin(type, dlg);
				}
			}],
			listeners: {
				activate: function() {
					Dialog.setBackMask();
				},
				hide: function() {
					dlg.hide();
					Viewer.closeAction();
				}
			}
	    });
		
	    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:!node.attributes.lock}, function(){ 
			if (!dlg.node) return false;
			var a = dlg.node.attributes;
			var formPanel = dlg.getComponent(0);
			var form = formPanel.getForm();
			if (!form.isValid()) return false;

			Ext.apply(a, form.getValues());
			dlg.node.setText(a.toString());
			Viewer.getToolbar().items.get('save').enable();
			dlg.hide();
		});
	    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){
			dlg.hide();
			Viewer.closeAction();
		});
	    
		dlg.render(Ext.getBody());
	    this.sleepDlg = dlg;
	}
	else {
		if(node.attributes.lock){
			this.sleepDlg.ok.enable();
		}else{
			this.sleepDlg.ok.disable();
		}
	}
	this.sleepDlg.node = node;
	var a = node.attributes;
	var formPanel = this.sleepDlg.getComponent(0);
	var form = formPanel.getForm();
	form.reset();
	form.setValues(a);
	
    return this.sleepDlg;
},

//XSLT转换对话框
getTransfDialog : function(node){
	var initial = '<?xml version="1.0" encoding="utf-8"?>\n'+
		'<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">\n'+
		'<!-- 在此填写 -->\n'+
		'<xsl:template match="/">\n'+
		'</xsl:template>\n'+
		'</xsl:transform>';
	
	var _this = this;
	var type = 'transform';
	if (!this.transfDialog) {
		var transformPanel = new Ext.ux.AssemblePanel({
			env: window.env, 
			extension: window.xslt_ext
		});
		var targetTree = transformPanel.getComponent('target-tree');
		var sourceTree = transformPanel.getComponent('source-tree');

		var dlg = new Ext.Window({
	        title: '转换',
	        resizable:true,  constrain:true, constrainHeader:true,
	        minimizable: false,  maximizable: false,
	        stateful: false,  modal: true,
	        buttonAlign: "right",
			defaultButton: 0,
	        width: 640,  height: 450,
	        minHeight: 240, minWidth: 350,
	        footer: true, bodyBorder: false,
	        closable: true, closeAction: 'hide',
	        sourceTree: sourceTree,
	        targetTree: targetTree,
			layout: 'border',
			items: [{
				region: 'north',
				itemId: 'params',
				xtype: 'form',
				autoHeight: true,
				height: 100,
				labelWidth: 75,
				border: false,
				bodyStyle: 'padding:5px;',
				layout: 'column',
				items: [
					{
						xtype: 'panel',
						columnWidth: 0.5,
						layout: 'form',
						labelWidth: 75,
						border: false,
						bodyStyle: "padding-left:5px;",
						defaults: { anchor: '100%', stateful: false},
					    items: [{
							fieldLabel: '目标变量',
					        name: 'to-var',
							xtype: 'combo',
							allowBlank: false,
							forceSelection: false,
							tpl: varList_tpl,
							store:  new Ext.data.JsonStore({
							    fields: ['name', 'icon'],
							    data : []
							}),
							mode: 'local',
							triggerAction: 'all',
							valueField: 'name', displayField: 'name',
							listeners: {
								change : function(field, newVal, oldVal){ 
									targetTree.setTitle('目标: '+newVal);
									targetTree.getRootNode().reload();
								}
							}
					    }]
					}]
				},{
					region: 'center',
					xtype: 'tabpanel',
					itemId: 'tabs',
					border: false,
					style: 'border-width: 1px 0 0 0;',
					tabPosition: 'bottom',
			        activeItem: 1,
			        deferredRender: false,
					items: [{
						itemId: 'xslt',
						xtype: 'form',
						title: 'XSLT',
						border: false,
					    items: [{
							hideLabel: true,
							xtype: 'textarea',
							anchor: '100%, 100%',
							border: false,
					        name: 'xslt_text',
							allowBlank: false
					    }],
					    tbar: [{
					    	text: '示例',
					    	handler: function(){
					    		this.ownerCt.ownerCt.findField('xslt_text').setValue(initial);
					    	}
					    }],
					    listeners: {
					    	activate: function(panel) {
					    		if (!targetTree.modified) return;
					    		var match = sourceTree.getRootNode().getPath('name');
					    		Ext.getCmp('xslt_text').setValue(_this.generateXSLT(targetTree.getRootNode(), match));
					    		targetTree.modified = false;
					    	}
					    }
					},
					transformPanel
					/*,{
						xtype: 'panel',
						title: '映射',
						border: false, hidden: true,
					    tbar: [],
						html: '暂不支持'
					}*/]
				}
			],
			buttons: [{
				text: 'XPath帮助',
				handler: function(){
					Dialog.showXpathWin(dlg);
				}
			},
			{
				text: Ext.Msg.buttonText.ok, 
				disabled:!node.attributes.lock,
				handler: function(){ 
					if (!dlg.node) return false;
					var a = dlg.node.attributes;
					var formPanel = dlg.getComponent('params');
					var form = formPanel.getForm();
					if (!form.isValid()) return false;
					Ext.apply(a, form.getValues());
		
					var xsltPanel = dlg.getComponent('tabs').getComponent('xslt');
					var xsltField = xsltPanel.getForm().findField('xslt_text');
					var info = Viewer.validateXML(xsltField.getValue());
					if (!!info) {
						Ext.Msg.alert('提示:', info);
						return;
					}
					
					if (targetTree.modified) {
			    		var match = sourceTree.getRootNode().getPath('name');
			    		xsltField.setValue(_this.generateXSLT(targetTree.getRootNode(), match));
					}
					a.xslt = xsltField.getValue();
					
					dlg.node.setText(a.toString());
					dlg.hide();
					Viewer.getToolbar().items.get('save').enable();
				}
			},{
				text: Ext.Msg.buttonText.cancel, 
				handler: function(){
					dlg.hide();
				}
			}],
			tools: [{
				id: 'help',
				qtip: '配置使用帮助',
				handler: function(e, toolEl, panel, tc){
					Dialog.showHelperWin(type, dlg);
				}
			}],
			listeners: {
				activate: function() {
					//Dialog.setBackMask();
				},
				hide: function() {
					dlg.hide();
					Viewer.closeAction();
				}
			}
	    });
		
		dlg.render(Ext.getBody());
		
		targetTree.loader = new Ext.tree.TreeLoader({
			dataUrl: 'diagram_ctrl.jsp', 
			baseParams: {operation: 'getDomChildren', service: servicePath},
			baseAttrs: { uiProvider: Ext.ux.ElementNodeUI, allowDrag: false },
			listeners: {
				beforeload: function(loader, node) {
					var a = node.attributes;
					var form = dlg.getComponent(0).getForm();
					var field = form.findField('to-var');
					if (!field.getValue()) return false;
					var index = field.store.find('name', new RegExp("^"+field.getValue()+"$"));
					
					if (index > -1) {
						var rec = field.store.getAt(index);
						var schema = rec.get('schema');
						if (schema) {
							loader.baseParams.message = 'request';
							loader.baseParams.schema = schema;
						}
						else {
							loader.baseParams.message = field.getValue();
							delete loader.baseParams.schema;
						}
					}
					else {
						loader.baseParams.message = field.getValue();
						delete loader.baseParams.schema;
					}
				},
				load: function(loader, node, response) {
					node.attributes['name'] = response.getResponseHeader('elementName');
					node.attributes['namespace'] = response.getResponseHeader('namespace');
					node.setText(response.getResponseHeader('elementName'));
					
					if (typeof(XML)=='function') {
						var xsltPanel = dlg.getComponent('tabs').getComponent("xslt");
						var xsltField = xsltPanel.getForm().findField('xslt_text');
						var xsl = new XML(xsltField.getValue());
						_this.reappearXSLT(node, xsl);
					}
					node.getOwnerTree().modified = false;
				},
				loadexception: function(loader, node, response){
					Viewer.showException(response); 
				}
			},
			preloadChildren: true, 
			clearOnLoad: false
		});
		
	    this.transfDialog = dlg;
	}
	else {
		//TODO
		var ok = this.transfDialog.buttons[1];
		if(node.attributes.lock){
			ok.enable();
		}else{
			ok.disable();
		}
	}
	this.transfDialog.node = node;
	var a = node.attributes;
	var formPanel = this.transfDialog.getComponent('params');
	var form = formPanel.getForm();
	var field = form.findField('to-var');
	var array = _this.getVariableArray(node);
	array.push({name:'response', icon:'icon-global'});
	array.push({name:'fault', icon:'icon-global'});
	field.store.loadData(array);
	form.reset();
	form.setValues(a);
	field.fireEvent('change', field, a['to-var']);
	
	var sourceRoot = this.transfDialog.sourceTree.getRootNode();
    while(sourceRoot.firstChild){
        sourceRoot.removeChild(sourceRoot.firstChild).destroy();
    }
	var array = _this.getVariableArray(node, 'element');
	for (var i=0; i<array.length; i++) {
		sourceRoot.appendChild(new Ext.tree.AsyncTreeNode({
			text: array[i].name,
			name: array[i].name,
			iconCls: array[i].icon,
			schema: array[i].schema,
			ref: 'response'
		}));
	}
	
	var xsltPanel = this.transfDialog.getComponent(1).getComponent(0);
	var xsltField = xsltPanel.getForm().findField('xslt_text');
	
	xsltField.setValue(a.xslt || initial);
	
	
    return this.transfDialog;
},

//事务对话框
getXactDialog : function(node){
	var _this = this;
	
	var dlg = this.xactDlg;
	if (!dlg) {
		
	    dlg = new Ext.Window({
	        autoCreate : true,
	        title: '事务',
	        resizable:true, constrain:true, constrainHeader:true,
	        minimizable: false, maximizable: false,
			stateful: false, modal: true,
	        buttonAlign: "right", defaultButton: 0,
	        width: 450, height: 150,
	        minHeight: 200,	minWidth: 300,
	        footer: true,
	        closable: true, closeAction: 'hide',
			plain: true,
			layout: 'fit',
			items: [{
				xtype: 'form',
				autoScroll: true,
				height: 100,
				labelWidth: 120,
				border: false,
				bodyStyle: 'padding:10px;',
				defaultType: 'textfield',
				defaults: { anchor: '-18', stateful: false},
				items: [
					{
						fieldLabel: '事务加入方式',
				        hiddenName: 'tx-option',
						xtype: 'combo',
						allowBlank: false, editable: false, forceSelection: true,
						store: [['require-new', '启动新事务'],['require', '加入当前事务']],
						mode: 'local',
						triggerAction: 'all'
				    }]
				}
			],
			buttons: [{
		    	text: Ext.Msg.buttonText.ok, 
		    	disabled:!node.attributes.lock,
		    	handler: function(){
					if (!dlg.node) return false;
					var a = dlg.node.attributes;
					var formPanel = dlg.getComponent(0);
					var form = formPanel.getForm();
					if (!form.isValid()) return false;
		
					Ext.apply(a, form.getValues());
					dlg.node.setText(a.toString());
					Viewer.getToolbar().items.get('save').enable();
					dlg.hide();
				}
			},{
				text: Ext.Msg.buttonText.cancel,
				handler: function(){ dlg.hide(); }
			}]
	    });
		
		dlg.render(Ext.getBody());
	    this.xactDlg = dlg;
	}
	else {
		var ok = dlg.buttons[1];
		if(node.attributes.lock){
			ok.enable();
		}else{
			ok.disable();
		}
		var formPanel = dlg.getComponent(0);
		var field = formPanel.getForm().findField('dsn');
		if (!!field)
			field.store.reload();
	}
	dlg.node = node;
	var a = node.attributes;
	var form = dlg.getComponent(0).getForm();
	form.reset();
	form.setValues(a);
	
    return dlg;
},

//DOM树装载器
getTreeLoader: function(form, varField) {
	var _this = this;
	var loader = new Ext.tree.TreeLoader({
		dataUrl: 'diagram_ctrl.jsp', 
		baseParams: {operation: 'getDomChildren', service: servicePath},
		listeners: {
			beforeload: function(loader, node) {
				var a = node.attributes;
				var field = form.findField(varField || 'var');
				if (!field.getValue()) return false;
				
				var index = field.store.find('name', new RegExp("^"+field.getValue()+"$"));
				if (index > -1) {
					var rec = field.store.getAt(index);
					var schema = rec.get('schema');
					if (schema) {
						loader.baseParams.message = 'response';
						loader.baseParams.schema = schema;
					}
					else {
						loader.baseParams.message = field.getValue();
						delete loader.baseParams.schema;
					}
				}
				else {
					return false;
				}
			},
			load: function(loader, node, response) {
				node.attributes.name = response.getResponseHeader('elementName');
				if (node.attributes.name) {
					node.setText(node.attributes.name);
				}
				else {
					form.getValues()[varField];
				}
			},
			loadexception: function(loader, node, response){
				var field = this.findField('xpath');
				if (field && field.collapse) field.collapse();
				Viewer.showException(response); 
			},
			scope: form
		}
	});
	return loader;
},

getDSNStore: function(){
	return new Ext.data.JsonStore({
		url: 'diagram_ctrl.jsp',
		baseParams: {	operation:'loadDSNs'},
		root: 'items',
		fields: [
			{name:'name', type:'string'}
		],
		listeners: {
			'loadexception': function(proxy, obj, response){ Viewer.showException(response); }
		}
	});
},

getTypeStore: function(){
	var typeStore = new Ext.data.SimpleStore({
	    fields: ['value', 'label'],
	    data : [['element','ELEMENT(元素)'], ['text','TEXT(文本)'], ['cdata','CDATA'], ['attribute','ATTRIBUTE(属性)'], ['namespace','NAMESPACE(命名空间)'], ['list','LIST(列表)'], ['boolean','BOOLEAN(逻辑型)'], ['string','STRING(字符串)'], ['number','NUMBER(数值)']]
	});
	return typeStore;
},

//获取变量数组
getVariableArray: function(node, type){
	var items=[], p=node;
	if (type==null || type=='env') {
		for (var i=0; i<env.length; i++) {
			items.push({name: env[i], icon:'icon-envvar'});
		}
	}
	
	items.push({name:'request', icon:'icon-global'});
	
	while((p=node.previousSibling) || (p=node.parentNode)) {
		var a = p.attributes;
		switch(a.tag){
			case 'assign':
			case 'clone':
				if (type) {
					if (a['type']==type) {
						for (var i=0; i<items.length; i++) {
							var item=items[i];
							if(item.name==a['to-var']){
							 	items.remove(item);
							}
						}
						items.push({name: a['to-var'], icon: 'icon-'+type});
					}
				}
				else {
					for (var i=0; i<items.length; i++) {
						var item=items[i];
						if(item.name==a['to-var']){
						 	items.remove(item);
						}
					}
					items.push({name: a['to-var'], icon: 'icon-'+type});
				}
				break;
			case 'sql':
				for (var i=0; i<items.length; i++) {
					var item=items[i];
					if(item.name==a['to-var']){
					 	items.remove(item);
					}
				}
				items.push({name: a['to-var'], icon: 'icon-element', schema:''}); 
				break;
			case 'callout':
				for (var i=0; i<items.length; i++) {
					var item=items[i];
					if(item.name==a['to-var']){
					 	items.remove(item);
					}
				}
				items.push({name: a['to-var'], icon: 'icon-element', schema: a['ref']});
				break;
			case 'transform':
				for (var i=0; i<items.length; i++) {
					var item=items[i];
					if(item.name==a['to-var']){
					 	items.remove(item);
					}
				}
				items.push({name: a['to-var'], icon: 'icon-element', schema: a['ref']});
				break;
			case 'for-meta':
				for (var i=0; i<items.length; i++) {
					var item=items[i];
					if(item.name==a['as']){
					 	items.remove(item);
					}
				}
				items.push({name: a['as'], icon: 'icon-any'});
				break;
		}
		node = p;
	}
	return items;
},

generateXSLT: function(root, match){
	var xmlns_xsl = "http://www.w3.org/1999/XSL/Transform";
	var xmldoc = XDom.createDocument(xmlns_xsl);
	try {
		root.getOwnerTree().body.mask('正在生成XSLT...', 'x-mask-loading');
		
		var rootEl = xmldoc.createElementNS(xmlns_xsl, 'xsl:transform', null);
		rootEl.setAttribute('version', "1.0");
		xmldoc.appendChild(rootEl);
		var templateEl = xmldoc.createElementNS(xmlns_xsl, 'xsl:template', null);
		templateEl.setAttribute("match", match);
		rootEl.appendChild(templateEl);
		var docuEl = xmldoc.createElementNS(root.attributes['namespace'], root.attributes.name, null);
		
		var n=1;
		root.cascade(function(node){
			var uri = node.attributes['qtip'];
			
			if (uri && uri != root.attributes['namespace']) {
				var prefix = 'ns' + n++;
				docuEl.setAttribute('xmlns:'+prefix, node.attributes.qtip);
				Saver.namespaces[node.attributes.qtip] = prefix;
			}
		});
		templateEl.appendChild(docuEl);

		Saver.saveNodes(root, docuEl);
		
		var xmltext = XDom.innerXML(xmldoc);
		if (typeof(XML)=='function') {
			xmltext = "" + new XML(xmltext.replace(/^<\?xml\s+version\s*=\s*(["'])[^\1]+\1[^?]*\?>/, "")).toXMLString();
		}
		if (typeof(console)=='object') {
			console.debug(xmltext);
		}
		return xmltext;
	}
	finally {
		root.getOwnerTree().body.unmask(); 
		Ext.destroy(xmldoc);
	}
},

/**
 * @param TreeNode root XML Convert tree root node
 * @param XML xsl
 */
reappearXSLT: function(root, xsl){
	var xslns = new Namespace("xsl", "http://www.w3.org/1999/XSL/Transform");
	var tarns = new Namespace("tns", root.attributes['namespace']);
	var templateEl = xsl.child(new QName(xslns, 'template'));
	if (templateEl) {
		var rootEl = templateEl.child(new QName(tarns, root.attributes['name']))
		if (rootEl == null) {
			return;
		}
		
		var reappear = function(node, element) {
			var list = element.children();
			if (list.length() > 0) {
				for (var i in list) {
					var child = list[i];
					if (child.nodeKind()=='comment' || child.nodeKind()=='processing-instruction') {
						
					}
					else if (child.nodeKind()=='text') {
						var paramNode = new Ext.tree.AsyncTreeNode({
							text: child.toString(),
							leaf: true,
							uiProvider: Ext.ux.ParamNodeUI
						});
						node.appendChild(paramNode);
					}
					else if (child.namespace()==xslns.uri && child.localName()=='value-of') {
						var paramNode = new Ext.tree.AsyncTreeNode({
							text: child.attribute('select'),
							leaf: true,
							//iconCls: a.iconCls,
							uiProvider: Ext.ux.ParamNodeUI
						});
						node.appendChild(paramNode);
					}
					
					var childNode = node.findChild('name', child.localName());
					if (childNode != null) {
						reappear(childNode, child);
					}
				}
			}
		};
		
		reappear(root, rootEl);
	}
}


};
}();

