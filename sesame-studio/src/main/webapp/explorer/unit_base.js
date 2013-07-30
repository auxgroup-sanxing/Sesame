/**
 * 所有服务单元界面的基础类
 * 
 * 功能: 
 *   1.提供基本面板的功能并且可重写
 *   2.各个子类提供Tab相关信息、界面参数和提交请求时的URL实例化生成面板
 *
 * 需求：
 *   1.发布服务 
 *     a.分为"向导"和"自定义路由"两种 
 *     b.各个组件有可能都有自己的个性化参数
 *     
 *   2.远程服务
 *     a.界面同"自定义界面" 
 *     b.操作列表tab中的"新建操作"和"属性"部分参数不需要
 *     c.各个组件有可能都有自己的个性化参数
 *     
 *   3.本地服务
 *     a.参数设置tab中界面形式与其他不同 但是名称类似
 *     b.操作列表tab中的"新建操作"和"属性"部分参数不需要
 *     c.各个组件有可能都有自己的个性化参数  
 */
document.oncontextmenu = function(e){return false;}

// 增加String类原生方法trim,去掉字符串两端的空格
String.prototype.trim = function() {return this.replace(/^\s+|\s+$/g,"");}

// 命名空间输入校验
var NCName = {
	regex: /^[A-Za-z_]+[\w_\-.]*$/,
	regexText: '必须以字母或下划线开头，不能包含冒号和空白字符'
};

// 目标服务参数输入校验
var QName = {
	regex: /^({.+})?[A-Za-z_]\w*/,
	regexText: "QName的格式为:{http://www.example.com/}portType"
};

var ImgPath = {
	lock: '../images/tool16/lock.png',
	unlock: '../images/tool16/unlock.png',
	disconnect: '../images/elcl16/launch_disconnect.gif'
};

// 端点参数地址输入校验
var URL = {
	regex: new RegExp("^(([a-zA-Z]+\w*)://)"
				+ "(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // ftp的user@
				+ "(([0-9]{1,3}\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
				+ "|" // 允许IP和DOMAIN（域名）
				+ "([0-9a-z_!~*'()-]+\.)*" // 域名- www.
				+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\." // 二级域名
				+ "[a-z]{2,6})" // first level domain- .com or .museum
				+ "(:[0-9]{1,4})?" // 端口- :80
				+ "((/?)|" // a slash isn't required if there is no file name
				+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$"),
	regexText: "非法的地址，地址示例: http://hostname:port/[file[?param=value]]"
};

/**
 * 服务单元基类构造函数及原型链
 */
var UnitBase = function(cfg){
	// 数据请求地址
	this.url = (!!cfg.url)? cfg.url : 'unit_ctrl.jsp';
	
	// 活动面板id
	this.activeTabId = active;
	
	/* "参数设置" 面板参数 */
	this.paramsLoaderCfg = Ext.apply({
		operation: "loadParams",
		unit: unit
	}, cfg.paramsLoaderCfg);
	
	this.paramsSaverCfg = Ext.apply({
		operation: "saveParams",
		unit: unit
	}, cfg.paramsSaverCfg);
	
	// 顶部
	this.paramTopPanel = Ext.apply({
		xtype: 'textfield',
		fieldLabel : '部署到绑定组件',
		grow : true,
		tag : "attribute",
		name : "component",
		labelStyle: 'text-align:right; font-weight:bold; padding-top: 4px;',
		allowBlank : false, 
		disabled: true,
		style: {
			'border': 'none',
			'color': 'black',
			'background-image': 'none',
			'padding-top':'0px',
			'font-weight':'bold'
		}, 
		value: componentDesc,
		listeners: {
			render: function(obj) {
				var linkHTML = '<span><img style="cursor:pointer;position:relative;top:3px;" src="../images/tool16/help_search.gif"/></span>';
				var inputEl = obj.el.dom.parentNode;
				var newEl = Ext.DomHelper.insertHtml('afterBegin', inputEl, linkHTML);
				newEl.onclick = function() {
					var title = componentDesc + '[' + component.replace(/.*?\//ig, '') + ']';
					var path = '../explorer/component_viewer.jsp?component=' + component;
					top.Application.createWindow(component, title, path);
				}
			}
		}
	}, cfg.paramTopPanel);
	
	// 中部表格
	this.gridReaderCfg = Ext.apply({
		fields: [{name: 'name'}, {name: 'location'}],
		root: 'list'
	}, cfg.gridReaderCfg);
	
	
	var gridColumn = [{
			header: '端点名称',
			dataIndex: 'name',
			width: 60
		}, {
			header: '地址',
			dataIndex: 'location',
			width: 140
		}
	];
	
	if (!!cfg.gridColumn && cfg.gridColumn.length > 0)
		Ext.each(cfg.gridColumn, function(item) {
			gridColumn.push(item);
		});
	this.gridColumnCfg = gridColumn;
	
	this.paramMidPanel = Ext.apply({
		xtype: 'grid',
		id: 'endpGrid',
		title: '端点',
		tag: "element",
		name: "endpoints",
		height: 100,
		border: true,
		style: 'margin-top: 10px;',
		store: new Ext.data.Store({
			reader: new Ext.data.ArrayReader(Ext.apply({}, this.gridReaderCfg))
		}),
		columns: [].concat(this.gridColumnCfg),
		viewConfig: {
			forceFit: true
		}
	}, cfg.paramMidPanel);
	
	// 底部
	this.paramBottomPanelItems = 
		(!!cfg.paramBottomPanelItems) ? cfg.paramBottomPanelItems: 
		[{
			fieldLabel: "服务名",
			tag: "attribute",
			xtype: "textfield",
			name: "service-name",
			allowBlank: false,
			regex: NCName.regex,
			regexText: NCName.regexText
		},{
			fieldLabel: "命名空间(只读)",
			tag: "attribute",
			xtype: "textfield",
			name: "namespace",
			regex: /^\S+$/,
			readOnly: true,
			regexText: '命名空间不能为空'
		},{
			fieldLabel: "描述",
			tag: "attribute",
			xtype: "textarea",
			allowBlank: false,
			name: "documentation"
		}];
	
	this.paramBottomPanel = Ext.apply({
		xtype: 'panel',
		title: '属性',
		tag: 'element',
		name: 'attributes',
		style: 'margin-top: 10px;',
		autoHeight: true,
		border : true,
		bodyStyle : 'padding:8px;',
		defaults : {anchor:"100%", stateful:false},
		layout : 'form',
		labelWidth : 130,
		items: [].concat(this.paramBottomPanelItems)
	}, cfg.paramBottomPanel);
	
	var paramsFieldsets = 
		[this.paramTopPanel, this.paramMidPanel, this.paramBottomPanel];
	if (!!cfg.paramsFieldsets && cfg.paramsFieldsets.length > 0) {
		if (!!cfg.insertIdx) {
			var preArray = paramsFieldsets.slice(0, cfg.insertIdx);
			var nextArray = paramsFieldsets.slice(cfg.insertIdx);
			Ext.each(cfg.paramsFieldsets, function(item){
				preArray.push(item);
			});
			paramsFieldsets = preArray.concat(nextArray);
		} else {
			Ext.each(cfg.paramsFieldsets, function(item){
				paramsFieldsets.push(item);
			});
		}
	}
		
	this.paramsFieldsets = paramsFieldsets;
	
	this.leftBaseCfg = Ext.apply({
		id: 'comm-form',
		title: '参数设置',
		animCollapse: false,
		autoScroll: true,
		border: false,
		collapsible: false,
		labelWidth: 140,
		bodyStyle: 'padding:5px 5px 0; text-align:left;',
		layout: 'form',
		defaults: {anchor: "-18",stateful: false},
		url: this.url,
		items: [].concat(this.paramsFieldsets),
		footerCssClass: 'x-window-bc',
		buttons: [{
			scope : this,
			itemId: 'saveBtn',
			disabled:(isVersioned == 'true' && unitLocked == 'false')? true:false,
			ref:'../save',
			text: '保存',
			handler: this.leftTabSaveHandler
		}, {
			scope : this,
			ref:'../reload',
			text: '刷新',
			listeners:{
				click:this.leftTabReloadHandler,
				scope:this
			}
		}],
		listeners: {
			activate: {
				fn: this.paramsLoader,
				scope: this
			},
			afterlayout: function(obj) {
				var x = obj.buttons[0].getPosition(false)[1];
				var y = obj.buttons[0].getPosition(false)[0];
				Ext.fly('tip').setStyle('top', x - 40);
				Ext.fly('tip').setStyle('left', y - 50);
				if (isVersioned == 'false' || unitLocked == 'true') {
					Ext.fly('tip').setStyle('display', 'none');
				} else {
					Ext.fly('tip').setStyle('display', 'block');
				}
			} 
		}
	}, cfg.leftBaseCfg);
	
	this.leftTabCfg = this.leftBaseCfg;
	
	// "操作列表"(绑定设置) 面板参数
	this.listReaderMeta = Ext.apply({
		root: 'items'
	}, cfg.listReaderMeta);
	
	var listReaderFields = [
		{name: 'interface'},
		{name: 'opera'},
		{name: 'desc'},
		{name: 'modifier'},
		{name: 'lastModified'}];
	if (!!cfg.listReaderFields && cfg.listReaderFields.length > 0)
		Ext.each(cfg.listReaderFields, function(item) {
			listReaderFields.push(item);
		});
	this.listReaderFields = listReaderFields;
	
	this.operaParamsCfg = Ext.apply({
		operation: 'loadOperations',
		unit: unit
	}, cfg.operaParamsCfg);
	
	var rightBaseColumns = [
		{header: "名称", id:'opera',width: 40, sortable: true, dataIndex: 'opera'},
        {header: "描述", width: 70, sortable: true, dataIndex: 'desc'},
        {header: "接口", width: 20, sortable: true,  hidden:true, dataIndex: 'interface'},
        {header: "修改时间", width: 50, sortable: true, dataIndex: 'lastModified'}
	];
	if (!!cfg.rightBaseColumns && cfg.rightBaseColumns.length > 0)
		Ext.each(cfg.rightBaseColumns, function(item) {
			rightBaseColumns.push(item);
		});
	this.rightBaseColumnsCfg = rightBaseColumns;
	
	// 右键菜单打开窗口参数
	var dialogIntfStoreCfg = [
	{
		name: 'opera',
		allowBlank: false,
		fieldLabel: '操作名称'
	}, {
		name: 'desc',
		fieldLabel: '描述',
		allowBlank: false
	}, {
		name: 'interface',
		itemId: 'interface',
		xtype: 'combo',
		fieldLabel: '接口',
		allowBlank: false,
		forceSelection: false,
		triggerAction: 'all',
		editable: true,
		valueField: 'name',
		displayField: 'label',
		store: new Ext.data.JsonStore({
			url: this.url,
			baseParams: {
				operation: 'getInterfaces',
				unit: unit
			},
			root: 'items',
			fields: [
				{name: 'label',type: 'string'}, 
				{name: 'name',type: 'string'}
			],
			listeners: {
				loadexception: {
					fn: function(proxy, obj, response){
						this.showException(response);
					}, 
					scope: this
				}
			}
		})
	}];
	if (!!cfg.dialogIntfStoreCfg && cfg.dialogIntfStoreCfg.length > 0)
		Ext.each(cfg.dialogIntfStoreCfg, function(item){
			dialogIntfStoreCfg.push(item);
		});
	this.dialogIntfStoreCfg = dialogIntfStoreCfg;
	
	this.dialogForm = Ext.apply({
		xtype: 'form',
		autoScroll: true,
		labelWidth: 90,
		border: false,
		bodyStyle: 'padding:10px;',
		defaultType: 'textfield',
		defaults: { anchor: '-18', stateful:false  },
		url: this.url,
		baseParams: {unit: unit},
		items: [].concat(this.dialogIntfStoreCfg)
	}, cfg.dialogForm);
	
	this.dialogCfg = Ext.apply({
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
		height: 300,
		minWidth: 300,
		footer: true,
		closable: true,
		closeAction: 'hide',
		plain: true,
		layout: 'fit',
		items: Ext.apply({}, this.dialogForm)
	}, cfg.dialogCfg);
	
	this.operaGridConfig = Ext.apply({
		header: true,
		title: '操作列表',
		itemId: 'operaGrid',
		autoScroll: true,
		height: 160,
		columnLines : true,
		headerStyle: {
			'border-bottom':'0px'
		},
		bodyStyle : {
			'border': '1px solid #99BBE8'
		},
		columns: [].concat(this.rightBaseColumnsCfg),
		viewConfig: { forceFit: true }
	}, cfg.operaGridConfig);
	
	this.rightBaseCfg = Ext.apply({
		title: '操作列表',
		id: 'service-panel',
		animCollapse: false,
		autoScroll: true,
		border: false,
		collapsible: false,
		enableHdMenu: false,
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
			ref:'../save',
			disabled:(isVersioned == 'true' && unitLocked == 'false')? true:false,
			handler: function() {
				var intfs = Ext.getCmp('service-panel').items.items;
				this.dataSaver(intfs);
			},
			scope: this
		}, {
			text: '刷新',
			ref:'../reload',
			scope: this,
			listeners:{
				click:this.dataLoader,
				scope:this
			}
		}]
	}, cfg.rightBaseCfg);
	
	this.rightTabCfg = this.rightBaseCfg;
};

UnitBase.prototype = {
	preparation: function() {
		Ext.Ajax.defaultHeaders = {
			"Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
		};
		Ext.QuickTips.init();
	},
	
	// 初始化方法
	init: function(){
		var _this = this;
		this.preparation();
		
		// "参数设置" 面板
		var leftTab = new Ext.form.FormPanel(_this.leftTabCfg);
		
		// "操作列表"(绑定设置) 面板
		var rightTab = new Ext.Panel(Ext.apply({}, _this.rightTabCfg));
		
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
					width:30,
					text: '返回',
					icon: '../images/tool16/undo_edit.gif',
					cls: 'x-btn-text-icon',
					handler: function() {
						_this.openProject();
					}
				}
			]},
			style : 'border-right:0px;',
			items: [leftTab, rightTab]
		});
		
		var viewport = new Ext.Viewport({
			stateId: 'unit_vi',
			stateful: false,
			layout: 'fit',
			items: tabPanel
		});
	},

    //服务单元保存并部署
	unitDeploy : function(button) {
		var operation = "deployserviceunit";
		var _this = this;
		Ext.Ajax.request({
			method : 'POST',
			url : 'unit_client_ctrl.jsp',
			params : {
				operation : operation,
				compName : compName,
				unit : unit
			},
			callback : function(options, success, response) {
				if (!success) {
					_this.showException(response);
				} else {
					Ext.Msg.alert('提示', '服务单元部署完成');
				}
			}
		});
	},
	
	// 操作锁定/解锁
	operaLockToggle: function(grid, rowIndex, columnIndex, operaWinId) {
		if (unitLocked == 'true') {
			Ext.Msg.alert('提示:', '服务单元已锁定,请直接双击打开该操作进行编辑!');
			return;
		}
		
		var _this = this;
		var imgEl = Ext.get(grid.getView().getCell(rowIndex, columnIndex)).select('.innerImg').elements[0];
		var operation, msg;
		if (imgEl.src.indexOf('launch_disconnect.gif') != -1) {
			Ext.Msg.alert('提示', '操作未同步至SVN!');
			return;
		} else if(imgEl.src.indexOf('unlock.png') != -1) {
			operation = 'operationLock';
			msg = '正在锁定操作...';
		} else {
			operation = 'operationUnlock';
			msg = '正在解锁操作...';
		}
		
		Ext.getBody().mask(msg, 'x-mask-loading');
		Ext.Ajax.request({
			method: 'POST',
			url: '../schema/encoding_ctrl.jsp',
			params: {
				operation: operation,
				schema: operaWinId + '.xsd'
			},
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				} else {
					var isLocked = 'false';
					if (operation == 'operationLock') {
						imgEl.src = imgEl.src.replace(/\/unlock.png/ig, '/lock.png');
						isLocked = 'true';
					} else {
						imgEl.src = imgEl.src.replace(/\/lock.png/ig, '/unlock.png');
					}
					
					// 通知已打开操作/流程窗口改变锁定状态
					// operaWinId为打开相应操作窗口ID
					var win = Application.findWindow(operaWinId);
					if (!!win) {
						var openedPanel = win.items.items[0];
						var tabPanels = openedPanel.items.items;
						Ext.each(tabPanels, function(tab) {
							var body = tab.body;
							if (!!body && !!body.dom) {
								var dom = body.dom;
								if (dom.nodeName == 'IFRAME') {
									dom.contentWindow.isLocked = isLocked;
									dom.contentWindow.Viewer.load();
								}
							}
						});
					}						
				}
			}
		});
	},
	
	// 服务单元锁定 /解锁
	unitLock: function(button){
		var _this = this;
		var operation = (button.text == '锁定') ? 'unitLock' : 'unitUnlock';
		var msg = (button.text == '锁定') ? '正在锁定服务单元...' : '正在解锁服务单元...';
		var _this = this;
		
		Ext.getBody().mask(msg, 'x-mask-loading');
		Ext.Ajax.request({
				method: 'POST',                                 
				url: 'unit_ctrl.jsp',
				timeout: 180 * 1000,
				params: {
					operation: operation,
					unit: unit
				},
				callback: function(options, success, response){
					Ext.getBody().unmask();
					if (!success) {
						_this.showException(response);              
					} else {
						var mainPanel = Ext.getCmp("mainPanel");
						if (button.text == "锁定") {
							Ext.each(mainPanel.items.keys, function(key){
								var panel = Ext.getCmp(key);
								if (!!panel.save)
									panel.save.setDisabled(false); 
							});
							button.setText('解锁');
							button.setIcon(ImgPath.lock);
							unitLocked = 'true';
							Ext.fly('tip').setStyle('display', 'none');
							_this.opendOperaLockToggle('true'); 
						} else {
							Ext.each(mainPanel.items.keys, function(key){
								var panel = Ext.getCmp(key);
								if (!!panel.save)
									panel.save.setDisabled(true);
							});
							_this.opendOperaLockToggle('false');
							button.setText('锁定');
							button.setIcon(ImgPath.unlock);
							unitLocked = 'false';
							Ext.fly('tip').setStyle('display', 'block'); 
						}
						
						// 重新加载页面
						var activeTab = mainPanel.getActiveTab();
						if(activeTab.reload)
							activeTab.reload.fireEvent('click');                                     
					}                                             
				}
			});                                               
	},
	
	// 实时改变已打开操作的锁状态
	opendOperaLockToggle: function(isLocked) {
		var currentWinId = Application.getDesktop().getActiveWindow().id;		
		if (unitType == 'serverGuide')
			currentWinId = importLocation;	// 有引用的情况
			
		var allBtns = Application.getDesktop().taskbar.tbPanel.getAllBtns();
		if (allBtns.length > 0) {
			Ext.each(allBtns, function(btn) {
				var win = btn.win;
				var id = win.id;
				if (id != currentWinId && id.indexOf(currentWinId) > -1) {
					var openedPanel = win.items.items[0];
					var tabPanels = openedPanel.items.items;
					Ext.each(tabPanels, function(tab) {
						var body = tab.body;
						if (!!body && !!body.dom) {
							var dom = body.dom;
							if (dom.nodeName == 'IFRAME') {
								dom.contentWindow.isLocked = isLocked;
								dom.contentWindow.Viewer.load();
							}
						}
					});
				}
			});
		}
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
			        src: '../explorer/project_viewer.jsp?project='+ name + '&projectDesc=' + escape(encodeURIComponent(projectDesc))
				}
            });
		win.show();
	},
	
	// "参数设置"面板->"保存"方法
	leftTabSaveHandler : function(){
		var container = Ext.getCmp('comm-form');
		var fields = container.findBy(function(c){
			return !!c.setValue && !!c.getValue && !!c.markInvalid && !!c.clearInvalid;
		});
		var valid = true;
		Ext.each(fields, function(f){
			if (!f.validate())
				valid = false;
		});
		
		if (valid)
			this.paramsSaver(container);
	},
	
	// "参数设置"面板->"刷新"方法
	leftTabReloadHandler : function(){
		var container = Ext.getCmp('comm-form');		
		this.paramsLoader(container);
	},
	
	// "参数设置"面板数据读取
	paramsLoader : function(container){
		var _this = this;
		var c = container.form.findField('component');
		Ext.getBody().mask('正在装载参数...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET',
			url: _this.url,
			params: _this.paramsLoaderCfg,
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (success) {
					// 其它参数赋值
					var rootEl = response.responseXML.documentElement;
					container.data = rootEl;
					_this.itemsLoader(rootEl, container.items);
				
					// 端点Grid赋值
					_this.gridLoader(rootEl, container);
					
					// 顶部panel赋值
					if (c && !c.getValue()) 
						c.setValue(componentDesc);
				} else {
					_this.showException(response);
				}
			}
		});
	},
	
	// "参数设置"面板数据保存
	paramsSaver : function(container) {
		var _this = this;
		var xmldoc = XDom.createDocument();
		try {
			var rootEl = xmldoc.createElement('unit');
			xmldoc.appendChild(rootEl);
			_this.itemsSaver(rootEl, container.items);
			
			// endpoints节点	
			var endpoints = rootEl.getElementsByTagName('endpoints');
			var warning = false;
			if (!!endpoints) {
				var endpointsEl = endpoints[0];
				warning = _this.gridSaver(xmldoc, endpointsEl);
			}
			
			if (!!warning) {
				Ext.Msg.alert('提示:', '请将<b style="color:red">端点</b>中参数填写完整！');
				return;
			}
		
			Ext.getBody().mask('正在保存参数...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST',
				url: _this.url,
				params: Ext.apply(_this.paramsSaverCfg, {
					data: encodeURIComponent(XDom.innerXML(xmldoc)),
					unit: unit
				}),
				callback: function(options, success, response){
					if (!success) {
						Ext.getBody().unmask();
						_this.showException(response);
					} else {
						container.data = rootEl;
						container.form.findField('component').setValue(componentDesc);
						Ext.getBody().mask('保存成功', 'x-mask-loading');
						Ext.getBody().unmask.defer(1500, Ext.getBody());
					}
				}
			});
		} catch (e) {
			alert('paramsSaver exception: ' + e.message);
		}
		finally {
			delete xmldoc;
		}
	},
	
	// 读取xml
	itemsLoader : function(parentEl, items) {
		var _this = this;
		if (!items) 
			return;
		items.each(function(item){
			if (item.isFormField) {
				if (item.tag == 'attribute') {
					if (item.name!='name') 
						item.setValue(parentEl.getAttribute(item.name));
				} else {
					var el = XDom.firstElement(parentEl, item.name);
					if (el && !item.readOnly) 
						item.setValue(el.text||el.textContent);
				}
			} else {
				var el = XDom.firstElement(parentEl, item.name);
				while (el && el.getAttribute('used')) {
					el = XDom.nextElement(el, item.name);
				}
				if (el) {
					_this.itemsLoader(el, item.items);
					el.setAttribute('used', true);
				}
			}
		});
	},
	
	// 创建xml
	itemsSaver: function(parentEl, items){
		var _this = this;
		var doc = parentEl.ownerDocument;
		items.each(function(item){
			if (item.isFormField) {
				if (item.tag == 'attribute') {
					if (item.name == 'component')
						item.setValue(component);
						
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
				if (item.items) 
					_this.itemsSaver(el, item.items);
			}
		});
	},
	
	// GridPanel赋值
	gridLoader: function(rootEl, container) {
		var endpointsEl = rootEl.getElementsByTagName('endpoints')[0];
		var epList = endpointsEl.getElementsByTagName('endpoint');
		var store = Ext.getCmp('endpGrid').store;
		var array = [];
		for (var i=0,len=epList.length; i<len; i++) {
			var ep = epList[i];
			array.push([
				ep.getAttribute('name'),
				ep.getAttribute('location')
			]);
		}
		
		if (array.length > 0) {
			container.get('endpGrid').setTitle('端点');
		} else {
			container.get('endpGrid').setTitle('端点 (<span style="color:#666666">请在"绑定设置"页面中配置端点参数</span>)');
		}
		store.loadData({list: array});
	},
	
	// GridPanel保存
	gridSaver : function(xmldoc, endpointsEl){
		var store = Ext.getCmp('endpGrid').store;
		if (!!store)
			store.each(function(r){
				if (!!r) {
					var endpEl = xmldoc.createElement('endpoint');
					if (!!r.get('name')) 
						endpEl.setAttribute('name', r.get('name'));
					if (!!r.get('location')) 
						endpEl.setAttribute('location', r.get('location'));
					endpointsEl.appendChild(endpEl);
				}
			});
		return false;	
	},
	
	// "操作列表"(绑定设置)面板数据读取
	dataLoader: function(){
		var _this = this;
		Ext.getBody().mask('正在读取操作列表...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET',
			url: _this.url,
			params: _this.operaParamsCfg,
			callback : function(options, success, response) {
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				} else {
					var result = Ext.decode(response.responseText);
					var items = result.items;
					
					// 将返回结果分组
					var rs = {};
					if (!!items && items.length > 0)
					Ext.each(items, function(item, index){
						var item = item;
						var intf = item['interface'];
						var opera = item['opera'];
						
						if (!rs[intf]) {
							var group = [item];
							rs[intf] = group;
						} else {
							rs[intf].push(item);
						}
					});
					
					var groupItems = [];
					Ext.each(rs, function(intfGroup) {
						for (var key in intfGroup) {
							var temp = {};
							temp.items = intfGroup[key];
							groupItems.push(temp);
						}
					});
					
					// 创建操作列表面板
					var servicePanel = Ext.getCmp('service-panel');
					servicePanel.removeAll(true);
					if (groupItems.length > 0) {
						Ext.each(groupItems, function(item){
							var panel = _this.createOperaPanel(item);
							var isImport = item.items[0]['isPublic'];
							var grid = panel.getComponent('operaGrid');
							isImport ? _this.addImportCtxMenuListener(grid, item) : _this.addCtxMenuListener(grid, item);
							servicePanel.add(panel);
							servicePanel.doLayout();
						});
					}
					servicePanel.doLayout();
				}
			}
		});
	},
	
	// "操作列表"(绑定设置)面板数据保存
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
				portTypeEl.setAttribute('intfName', params['intfName']);
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
					unit: unit,
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
		}catch(e) {
			alert(e.message);
		} finally {
			delete xmldoc;
		}
	},
	
	// 创建默认接口面板(无操作列表):默认接口名称为服务名称
	createDefaultIntf : function() {
		var _this = this;
		Ext.Ajax.request({
			url: _this.url,
			method: 'POST',
			params: {
				operation: 'getServiceName',
				unit: unit
			},
			callback: function(options, success, response){
				if (!success) {
					_this.showException(response);
				} else {
					var intf = response.responseText.replace(/\r\n/ig, '');
					if (!intf)
						return;
					_this.createIntf(intf, '', false);
				}
			}
		});
	},
	
	// 操作列表表格右键菜单事件注册
	addCtxMenuListener: function(operaGridPanel, item){
		var _this = this;
		var intf = item.items[0]['interface'];
		var ctxMenu = new Ext.menu.Menu({
			items: [{
				text: '添加操作',
				disabled: isVersioned == 'true' && unitLocked == 'false',
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
			ctxMenu.items.get('remove').setDisabled((isVersioned == 'true' && unitLocked == 'false') || sels.length < 1);
			ctxMenu.items.get('open').setDisabled(sels.length < 1);
			ctxMenu.showAt(e.getXY());
			e.stopEvent();
		});
		
		operaGridPanel.on("rowdblclick", function(sender, rowIndex, e){
			var rec = operaGridPanel.store.getAt(rowIndex);
			_this.gridClickfn(rec);
		});
		
		// 单击按钮锁定操作
		operaGridPanel.on('cellclick', function (grid, rowIndex, columnIndex, e) {
			var img = e.getTarget('.innerImg');
			if (img) {
				var record = grid.getStore().getAt(rowIndex);
				var data = unit + '/' + record.get('opera');
				_this.operaLockToggle(grid, rowIndex, columnIndex, data);
			}
		});
	},
	
	// 操作列表表格右键菜单事件注册(针对引入的公共接口)
	addImportCtxMenuListener: function(operaGridPanel, item) {
		var _this = this;
		var intf = item.items[0]['interface'];
		var ctxMenu = new Ext.menu.Menu({
			items: [{
				itemId: 'open',
				text: '打开',
				handler: function(){
					this.openHandler(ctxMenu);
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
			ctxMenu.items.get('open').setDisabled(sels.length < 1);
			ctxMenu.showAt(e.getXY());
			e.stopEvent();
		});
		
		operaGridPanel.on("rowdblclick", function(sender, rowIndex, e){
			var rec = operaGridPanel.store.getAt(rowIndex);
			_this.gridClickfn(rec);
		});
	},

	createOperaPanel: function(options) {
		var _this = this;
		var isImport = options.items[0]['isPublic'];
		var intf = options.items[0]['interface'];
		var intfDesc = options.items[0]['intfDesc'];
		var title = isImport? '公共接口:' + intf : '接口: ' + intf;
		
		function renderOpera(value, cellmeta, record, rowIndex, columnIndex, store) {
			var isLocked = record.json['locked'];
			var str = "<img class='innerImg' src='../images/elcl16/launch_disconnect.gif'>";
			if (isLocked == 'true')
				str = "<img class='innerImg' src='" + ImgPath.lock + "'/>";
			if (isLocked == 'false')
				str = "<img class='innerImg' src='" + ImgPath.unlock + "'/>";
			return str;
		};

		var btnColumnCfg = [{
			header: false,
			dataIndex: 'lock',
			menuDisabled: true,
			sortable: false,
			width: 25,
			fixed: true,
			align: 'center',
			renderer: renderOpera
		}];

		if(this.operaGridConfig.columns.length < 5 && isVersioned == 'true') {
			this.operaGridConfig.columns = btnColumnCfg.concat(this.operaGridConfig.columns);
		}
		
		// 操作列表表格
		this.operaGridConfig.store = new Ext.data.Store({
			reader: new Ext.data.JsonReader(
				Ext.apply({},this.listReaderMeta), 
				[].concat(this.listReaderFields))
		});
		var operaGridPanel = 
			new Ext.grid.GridPanel(Ext.apply({},this.operaGridConfig));
				
		operaGridPanel.store.loadData({
			items: options.items
		});
		
		var formpanel = new Ext.form.FormPanel({
			title: title,
			style: {'margin': '5 8 3 8'},
			autoHeight: true,
			frame: true,
			closable: true,
			collapsible: true,
			defaults: {anchor: '100%'},
			labelWidth: 120,
			layout : 'form',
			items: [{
				xtype: 'field',
				hideLabel: true,
				hidden: true,
				name: 'intfName',
				value: intf
			},{
				fieldLabel: '接口描述',
				labelStyle: 'padding : 3 0 0 5',
				xtype: 'textfield',
				name: 'intfDesc',
				value: intfDesc,
				disabled: isImport,
				allowBlank: true
			}, operaGridPanel],
			tools: [
				{
				id: 'close',
				qtip: '删除接口',
				handler: function(e, toolEl, panel, tc){
					if (!confirm('确实要删除该接口吗?'))
						return;
					var intfName = intf;
					_this.deleteIntf(intfName, isImport);
					panel.destroy();
					Ext.getCmp('service-panel').doLayout();
				}
			}]
		});
		
		return formpanel;
	},
	
	// 添加引入的公共接口
	createImportOperaPanel: function(options, operas) {
		var _this = this;
		var _this = this;
		var intf = options.items[0]['interface'];
		var intfDesc = options.items[0]['intfDesc'];
		var title = '公共接口: ' + intf;
		
		this.operaGridConfig.store = new Ext.data.Store({
			reader: new Ext.data.JsonReader(
				Ext.apply({},this.listReaderMeta), 
				[].concat(this.listReaderFields))
		});
		var operaGridPanel = 
			new Ext.grid.GridPanel(Ext.apply({},this.operaGridConfig));
		operaGridPanel.store.loadData({
			items: operas.items
		});
		
		var formpanel = new Ext.form.FormPanel({
			title: title,
			style: {'margin': '5 8 3 8'},
			autoHeight: true,
			frame: true,
			closable: true,
			collapsible: true,
			defaults: {anchor: '100%'},
			labelWidth: 120,
			layout : 'form',
			items: [{
				xtype: 'field',
				hideLabel: true,
				hidden: true,
				name: 'intfName',
				value: intf
			},{
				fieldLabel: '接口描述',
				labelStyle: 'padding : 3 0 0 5',
				xtype: 'textfield',
				name: 'intfDesc',
				value: intfDesc,
				disabled: true,
				allowBlank: true
			}, operaGridPanel],
			tools: [{
				id: 'close',
				qtip: '删除接口',
				handler: function(e, toolEl, panel, tc){
					if (!confirm('确实要删除该接口吗?'))
						return;
					var intfName = intf;
					_this.deleteIntf(intfName);
					panel.destroy();
					Ext.getCmp('service-panel').doLayout();
				}
			}]
		});
		
		return formpanel;
	},
	
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
			var intf = panel.get('interface').getValue();
			var desc = panel.get('intfDesc').getValue();
			var intfs = Ext.getCmp('service-panel').items.items;
			var hasPublished = false;
			Ext.each(intfs, function(item, index){
				var publishedIntf = item.title.replace(/接口(：|:)/ig, '').trim();
				if (intf == publishedIntf) {
					hasPublished = true;
					return;
				}
			});
			
			if (!hasPublished) {
				_this.createIntf(intf, desc, false);
				selectWin.close();
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
	
	// 导入公共接口
	importIntf: function() {
		var _this = this;
		var panel = new Ext.form.FormPanel({
			header: false, autoHeight: true,
			frame: true, border: false,
			style: {'padding': '10 5 5 5'},
			labelWidth: 115,
			defaults: {
				anchor: '100%',
				stateful: false
			},
			items: [
			{
				fieldLabel: ' 公共接口',
				xtype: 'combo',
				itemId: 'publicIntfList',
				store: new Ext.data.Store({
					url: 'intf_ctrl.jsp',
					baseParams: {
						operation: 'getPublicIntfs',
						unit: unit
					},
					reader: new Ext.data.JsonReader({
						root: 'items',
						fields: [{name: 'intf'}, {name: 'desc'}]
					}),
					listeners: {
						load: function() {
							var count = this.getTotalCount();
							if (count == 0) 
								Ext.Msg.show({
								   title:'提示',
								   msg: '没有已配置的公共接口！',
								   icon: Ext.MessageBox.INFO
								});
						}
					}
				}),
				valueField: 'intf', displayField: 'intf',
				triggerAction: 'all', forceSelection: true,
				editable: false, allowBlank: false,
				listeners: {
					select: function(combo, record, index) {
						var desc = record.get('desc');
						var descField = 
							this.ownerCt.getComponent('intfDesc');
						descField.setValue(desc);	
					}
				}
			},{
				fieldLabel: '接口描述',
				xtype: 'textarea',
				itemId: 'intfDesc',
				disabled: true,
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
			var importIntf = panel.get('publicIntfList').getValue();
			var desc = panel.get('intfDesc').getValue();
			var intfs = Ext.getCmp('service-panel').items.items;
			var hasPublished = false;
			Ext.each(intfs, function(item, index){
				var publishedIntf = item.title.replace(/.*接口(：|:)/ig, '').trim();
				if (importIntf == publishedIntf) {
					hasPublished = true;
					return;
				}
			});
			
			if (!hasPublished) {
				Ext.Ajax.request({
					method: 'POST',
					url: 'intf_ctrl.jsp',
					params: {
						operation: 'addPublicIntf',
						unit: unit,
						intf: importIntf
					},
					callback: function(options, success, response) {
						if (!success) {
							_this.showException(response);
						} else {
							var operas = Ext.decode(response.responseText);
							_this.createIntf(importIntf, desc, true, operas);
						}
					}
				});
				selectWin.close();
			} else {
				Ext.Msg.alert('提示:', '该接口 "<b style="color:red">' + importIntf + '</b>" ' + '已经存在,请选择其他接口！');
			}
		}
		
		var selectWin = new Ext.Window({
			title : '导入公共接口',
			autoCreate: true, border : false,
	        resizable:false, constrain:true, 
			constrainHeader:true, minimizable:false, maximizable:false,
	        stateful:false,  modal:true,
	        width: 400, height: 180, minWidth: 200,
	        footer: true,  closable: true, 
			closeAction: 'close',plain: true, layout: 'fit',
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
	
	// 创建接口
	createIntf : function(intf, desc, isImport, operas) {
		var _this = this;
		
		var servicePanel = Ext.getCmp('service-panel');
		var item = [{'interface':intf, 'intfDesc': desc}];
		var items = {items : item};
		var panel = (!isImport)?_this.createOperaPanel(items) : _this.createImportOperaPanel(items, operas);
		var grid = panel.getComponent('operaGrid');
		
		if (!!isImport) {
			_this.addImportCtxMenuListener(grid, items)
		} else {
			grid.store.removeAll();
			_this.addCtxMenuListener(grid, items);
		}
			
		servicePanel.add(panel);
		servicePanel.doLayout();
	},
	
	// 删除接口及其操作
	deleteIntf : function(intfName, isImport) {
		var _this = this;
		var importIntf = isImport || false;
		Ext.getBody().mask('正在读取操作列表...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET',
			url: _this.url,
			params: {
				operation: 'deleteIntf',
				unit: unit,
				component: component,
				isImport: importIntf,
				'interface': intfName
			},
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				}
			}
		});
	},
	
	gridClickfn: function(rec) {
		var _this = this;
		_this.openOpera(rec.get('opera'), rec.get('desc'), '');
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
	
	openOpera : function(opera, desc, ref){
		Application.createWindow(
			unit + '/' + opera,
			opera + '-' + desc,
			'../schema/encoding_viewer.jsp?schema='+ unit + '/' + opera + '.xsd' + '&unit=' + unit + '&unitId=' + unitId+ '&unitDesc=' + escape(encodeURIComponent(unitDesc)) + '&ref=' + ref + '&projectDesc=' + escape(encodeURIComponent(projectDesc)), 
			'settings');
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
					unit: unit,
					component:component,
					operas: Ext.encode(operas)
				},
				callback: function(options, success, response){
					if (success) {
						Ext.each(ctxMenu.selections, function(rec){
							Application.closeWindow(unit + '/' + rec.get('opera'));
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
		rec.set('interface', values['interface']);
	},
	
	// 右键菜单打开窗口处理
	getDialog : function(options){
		var _this = this;
	    var dlg = new Ext.Window(_this.dialogCfg);
	    dlg.ok = dlg.addButton(
			{
				text: Ext.Msg.buttonText.ok,
				disabled: isVersioned == 'true' && unitLocked == 'false'
			},
			function(){
				_this.dialogSubmit(dlg);
			}
		);
	    dlg.cancel = 
			dlg.addButton(Ext.Msg.buttonText.cancel, function(){
				dlg.hide();
			});
	    
		dlg.render(Ext.getBody());
	    this.serviceDlg = dlg;
		
		this.serviceDlg.options = options;
		var formPanel = this.serviceDlg.getComponent(0);	
		var form = formPanel.getForm();
		
		// "接口"选项赋值并且置为 不允许编辑状态
		form.items.get('interface').setValue(options.data['interface']);
		form.items.get('interface').disable();
		
		this.afterSubmit(form, formPanel, options);
		this.serviceDlg.setTitle(options.title || '创建操作');
        return this.serviceDlg;
	},
	
	dialogSubmit: function(dlg){
		var _this = this;
		var formPanel = dlg.getComponent(0);
		var form = formPanel.getForm();
		var params = {};
		if (dlg.options.data) {
			params = {
				'old-opera': dlg.options.data['opera'],
				'old-intf': dlg.options.data['interface'],
				'interface': dlg.options.data['interface']
			};
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
				if (dlg.options.callback) {
					var values = form.getValues();
					if (!values['interface'])
						values['interface'] = dlg.options.data['interface'];
					dlg.options.callback(values);
				}
				dlg.hide();
			},
			failure: function(form, action){
				if (action.response) 
					_this.showException(action.response);
			}
		});
	},
	
	afterSubmit: function(form, formPanel, options){
		var store = form.findField('route').store;
		form.reset();
		store.reload({
			callback: function(){
				var target = formPanel.getComponent('target-group');
				if (options.data) {
					form.setValues(options.data);
					var value = options.data.ref;
					target.setVisible(!value);
				}
				else {
					target.setVisible(true);
				}
			}
		});
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
};

/**
 * 代理服务类——自定义模式
 */ 
var UnitServerCustom = function(cfg){
	var svcStore = new Ext.data.JsonStore({
		url: 'unit_custom_ctrl.jsp',
		baseParams: {
			operation: 'getAvailServices',
			unit: unit
		},
		root: 'items',
		fields: [
			{name:'label', type:'string'},
			{name:'service', type:'string'},
			{name:'intf', type:'string'},
			{name:'opera', type:'string'},
			{name:'name', type:'string'}
		],
		listeners: {
			loadexception: {
				fn: function(proxy, obj, response){
					this.showException(response);
				},
				scope: this
			}
		}
	});
	
	var listReaderFields = [
		{name: 'route'},
	   	{name: 'ref-svc'},
	   	{name: 'ref-intf'},
	   	{name: 'ref-opera'}
	];
	if (!!cfg.listReaderFields && cfg.listReaderFields.length > 0)
		Ext.each(cfg.listReaderFields, function(item) {
			listReaderFields.push(item);
		});
	this.listReaderFields = listReaderFields;
	
	var rightBaseColumnsCfg = [{
		header: "路由",
		width: 70,
		sortable: true,
		dataIndex: 'route'
	}];
	if (!!cfg.rightBaseColumnsCfg && cfg.rightBaseColumnsCfg.length > 0)
		Ext.each(cfg.rightBaseColumnsCfg, function(item) {
			rightBaseColumnsCfg.push(item);
		});
	this.rightBaseColumnsCfg = rightBaseColumnsCfg;
	
	var dialogIntfStoreCfg = [{
		name: 'route',
		hiddenName: 'route',
		xtype: 'combo',
		fieldLabel: '路由',
		allowBlank: false,
		forceSelection: true,
		store: svcStore,
		triggerAction: 'all', editable: false,
		valueField: 'name', displayField: 'label',
		listeners: {
			select: function(sender, rec){
				var form = sender.ownerCt.getForm();
				var value = sender.getValue();
				var field = form.findField('ref-svc');
				field.setValue(rec.get('service')? rec.get('service'):'');
				field = form.findField('ref-intf');
				field.setValue(rec.get('intf')? rec.get('intf'):'');
				field = form.findField('ref-opera');
				field.setValue(rec.get('opera')? rec.get('opera'):'');
				var target = sender.ownerCt.getComponent('target-group');
				target.setVisible(!value);
			}
		}
    },{
		id: 'target-group',
		xtype: 'panel',
		layout: 'form',
		autoHeight: true,
		border: false,
		defaultType: 'textfield',
		defaults: { anchor: '100%' },
		items:[{
			fieldLabel: '目标服务名称',
			name: 'ref-svc',
			regex: QName.regex, 
			regexText:'服务名的格式为:{http://www.example.com/}serviceName'
	    },{
			fieldLabel: '目标服务接口',
			name: 'ref-intf',
			regex: QName.regex, 
			regexText:'接口名的格式为:{http://www.example.com/}portType'
	    },{
			fieldLabel: '目标操作',
			name: 'ref-opera',
			regex: QName.regex, 
			regexText:'操作名的格式为: operation'
	    }]
	}];
	if (!!cfg.dialogIntfStoreCfg && cfg.dialogIntfStoreCfg.length > 0)
		Ext.each(cfg.dialogIntfStoreCfg, function(item) {
			dialogIntfStoreCfg.push(item);
		});
	this.dialogIntfStoreCfg = dialogIntfStoreCfg;
	
	this.dialogCfg = Ext.apply({height: 270}, cfg.dialogCfg);
	var config = Ext.apply({
		url : 'unit_custom_ctrl.jsp',
		paramsLoaderCfg: {
			operation: 'load',
			unit: unit
		},
		dialogCfg: this.dialogCfg,
		listReaderFields : this.listReaderFields,
		paramsSaverCfg: {operation: 'saveParams'},
		rightBaseColumnsCfg: this.rightBaseColumnsCfg,
		dialogIntfStoreCfg: this.dialogIntfStoreCfg
	}, cfg.config);
	
	UnitServerCustom.superclass.constructor.call(this, config);
};
Ext.extend(UnitServerCustom, UnitBase, {
	gridClickfn: function(rec) {
		var _this = this;
		_this.openOpera(rec.get('opera'), rec.get('desc'), rec.get('route'));
	}
});

/**
 * 代理服务——向导模式
 */ 
var UnitServerGuide = function(cfg){
	this.gridReaderCfg = {
		fields: [{name: 'name'}, {name: 'location'}, {name: 'binding'}],
		root: 'list'
	};
	
	this.gridColumn = [{
		header: "绑定",
		dataIndex: "binding",
		width: 60
	}];
	
	var paramsFieldsets = [{
		xtype: 'textfield',
		fieldLabel : '引擎服务',
		grow : true,
		name : "engine",
		labelStyle: 'text-align:right; font-weight:bold;',
		allowBlank : false, 
		disabled: true,
		style: {
			'border': 'none',
			'color': 'black',
			'background-image': 'none',
			'padding-top':'0px',
			'font-weight':'bold'
		}, 
		value: engineDesc
	}];
	
	var config = Ext.apply({
		url: 'unit_guide_ctrl.jsp',
		gridReaderCfg: Ext.apply({},this.gridReaderCfg),
		gridColumn: [].concat(this.gridColumn),
		paramsFieldsets: paramsFieldsets,
		insertIdx: 1,
		paramsSaverCfg: {
			operation: 'saveParams'
		}
	}, cfg.config);
	
	UnitServerGuide.superclass.constructor.call(this, config);
}
Ext.extend(UnitServerGuide, UnitBase, {
	init: function() {
		this.preparation();
		var _this = this;
		
		var rightTabCfg = {
			title: '绑定设置',
			id: 'binding-panel',
			animCollapse: false,
			autoScroll: true,
			border: false,
			collapsible: false,
			defaults: {anchor: '100%'},
			listeners: {
				activate: {
					fn: function(c){
						this.loadBindings();
					},
					scope: this
				}
			},
			footerCssClass: 'x-window-bc',
			buttonAlign: 'right',
			fbar: [
			{
				text: '添加绑定',
				cls: 'x-btn-text-icon',
				icon: '../images/elcl16/add_obj.gif',
				scope: this,
				handler: function(){this.selectBindingType();}
			},{
				text: '同步接口',
				cls: 'x-btn-text-icon',
				icon: '../images/elcl16/synced.gif',	// heliang
				handler: function(){_this.syncBinding(this);}
			},{
				text: '保存',
				ref:'../save',
				disabled: (isVersioned == 'true' && unitLocked == 'false')? true:false,
				handler: function(){
					var bindings = Ext.getCmp('binding-panel').items.items;
					this.saveBindings(bindings);
				},
				scope: this
			}, {
				text: '刷新',
				ref:'../reload',
				scope: this,
				listeners:{
					click:this.loadBindings,
					scope:this
				}
			}]
		};
		
		// "参数设置" 面板
		var leftTab = new Ext.form.FormPanel(Ext.apply({},_this.leftTabCfg));
		
		// "操作列表"(绑定设置) 面板
		var rightTab =  new Ext.Panel(Ext.apply({},rightTabCfg));
		
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
					width:30,
					text: '返回',
					icon: '../images/tool16/undo_edit.gif',
					cls: 'x-btn-text-icon',
					handler: function() {
						_this.openProject();
					}
				}
			]},
			style : 'border-right:0px;',
			items: (engineDesc == 'null')? leftTab:[leftTab, rightTab]
		});
		
		var viewport = new Ext.Viewport({
			stateId: 'unit_vi',
			stateful: false,
			layout: 'fit',
			items: tabPanel
		});
		
		// 引用的引擎服务不存在时给出提示
		if (engineDesc == 'null')
			Ext.Msg.show({
			   title:'提示:',
			   msg: '该代理服务单元所引用的<b>引擎服务</b>已更改或不存在,请检查!',
			   buttons: Ext.Msg.OK,
			   fn: function() {
			   		var currentWinId = Application.getDesktop().getActiveWindow().id;
					Application.closeWindow(currentWinId);
			   }
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
			        src: '../explorer/project_viewer.jsp?project='+ name + '&projectDesc=' + escape(encodeURIComponent(projectDesc))
				}
            });
		win.show();
	},
	
	gridLoader: function(rootEl, container) {
		var endpointsEl = rootEl.getElementsByTagName('endpoints')[0];
		var epList = endpointsEl.getElementsByTagName('endpoint');
		var store = Ext.getCmp('endpGrid').store;
		var array = [];
		for (var i=0,len=epList.length; i<len; i++) {
			var ep = epList[i];
			array.push([
				ep.getAttribute('name'),
				ep.getAttribute('location'),
				ep.getAttribute('binding')
			]);
		}
		
		if (array.length > 0) {
			container.get('endpGrid').setTitle('端点');
		} else {
			container.get('endpGrid').setTitle('端点 (<span style="color:#666666">请在"绑定设置"页面中配置端点参数</span>)');
		}
		store.loadData({list: array});
	},
	
	gridSaver: function(xmldoc, endpointsEl) {
		var store = Ext.getCmp('endpGrid').store;
		var warning = false;
		if (!!store)
			store.each(function(r){
				var endpEl = xmldoc.createElement('endpoint');
				if (r.get('binding') == 'undefined' || !r.get('binding')) {
					warning = true;
					return;
				}
				endpEl.setAttribute('name', r.get('name'));
				endpEl.setAttribute('location', r.get('location'));
				endpEl.setAttribute('binding', r.get('binding'));
				endpointsEl.appendChild(endpEl);
			});
		return warning;	
	},
	
	// 绑定类型选项面板
	selectBindingType : function() {
		var _this = this;
		var panel = new Ext.Panel({
			header: false,
			autoHeight: true,
			frame : true,
			border: false,
			layout: 'form',
			style: {'padding': '10 5 5 5'},
			labelWidth: 115,
			defaults: {anchor: '100%',stateful: false},
			items: [
			{
				fieldLabel: '服务接口',
				xtype: 'combo',
				itemId: 'interface',
				store: new Ext.data.Store({
					url: _this.url,
					baseParams: {
						operation: 'getInterfaces',
						unit: unit
					},
					reader: new Ext.data.JsonReader({
						root: 'items',
						fields: [{name: 'type'}, {name: 'isPublic'}]
					}),
					listeners: {
						load: function() {
							var count = this.getTotalCount();
							if (count == 0) 
								Ext.Msg.show({
								   title:'提示',
								   msg: '没有已配置的接口或接口操作列表为空！',
								   icon: Ext.MessageBox.INFO
								});
						},
						select: function(combo, record, index) {
							var flag = record.get('isPublic');
							var hiddenField = 
								this.ownerCt.getComponent('flag');
							hiddenField.setValue(flag);	
						}
					}
				}),
				valueField: 'type',
				displayField: 'type',
				triggerAction: 'all',
				forceSelection: true,
				editable: false,
				allowBlank: false
			}]
		});
		
		var selectWin = new Ext.Window({
			title : '绑定设置',
			autoCreate: true,
			border : false,
	        resizable:false, constrain:true, 
			constrainHeader:true,
	        minimizable:false, maximizable:false,
	        stateful:false,  modal:true,
			defaultButton: 0,
	        width: 400, height: 140, 
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
					var intfObj = panel.get('interface');
					var intf = intfObj.getValue();
					if (!intf) return;
					
					var store = intfObj.getStore();
					store.each(function(rec){
						if(rec.get('type') == intf) {
							// 一个接口对应多个绑定
							_this.addBinding(intf, rec.get('isPublic'));
							selectWin.close();
							return;
						}
					});
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
	
	// 添加绑定
	addBinding : function(intf, isImport) {
		var _this = this;
		Ext.getBody().mask('正在添加...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET',
			url: _this.url,
			params: {
				operation : !isImport ? "addBinding":"addPublicBinding",
				unit : unit,
				type : intf
			},
			callback : function(options, success, response) {
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				} else {
					_this.loadBindings();
				}
			}
		});
	},
	
	// 读取绑定信息
	loadBindings : function() {
		var _this = this;
		Ext.getBody().mask('正在装载参数...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET',
			url: _this.url,
			params: {
				operation : 'loadOperations',
				unit : unit
			},
			
			callback : function(options, success, response) {
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				} else {
					var result = Ext.decode(response.responseText);
					
					// 读取返回结果中的绑定列表
					var items = result.items;
					
					var bindingPanel = Ext.getCmp('binding-panel');
					bindingPanel.removeAll(true);
					
					// 循环读取binding list生成panel
					Ext.each(items, function(item, index){
						bindingPanel.add(_this.createBinding(item));
						bindingPanel.doLayout();
					});
					bindingPanel.doLayout();
				}
			}
		});
	},
	
	operaGridReaderConfig: {
		root: 'operations',
		fields: [{name: 'opera'}, {name: 'desc'}, {name: 'action'}]
	},
	
	// 创建绑定Panel
	createBinding : function(options) {
		var _this = this;
		var bindingName = (!options)? '' : options.bindingName;
		var operations = (!options)? '' : options.operations;
		var intf = (!options)? '' : options.portTypeName;
		var endPoint = (!options)? '' : options.epName;
		var address = (!options)? '' : options.address;
		
		var extdata = (!options)? '' : options.extdata;
		var transport = (!extdata)? '' : extdata.transport;
		var bindingType = (!extdata)? '' : extdata.bindingType;
		
		// 扩展元素Panel
		var operaExtPanel = {};
		if (!!Ext.ux.extendPanel)
			operaExtPanel = new Ext.ux.extendPanel({
				extdata : extdata
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
			
		var btnColumnCfg = [{
			header: false,
			dataIndex: 'lock',
			menuDisabled: true,
			sortable: false,
			width: 25,
			fixed: true,
			align: 'center',
			renderer: renderOpera
		}];

		var config = {
			title: '操作列表',
			itemId: 'operaGrid',
			height: 150,
			columnLines : true,
			bodyStyle : {
				'border': '1px solid #99BBE8',
				'border-top' : 'none'
			},
			columns: [
			{header: "名称",dataIndex: 'opera',width: 120},
			{header: "描述",	dataIndex: 'desc',width: 200},
			{
			 	header: "交易码",
				dataIndex: 'action',
				editor: new Ext.form.TextField()
			}],
			viewConfig: { forceFit: true }
		};
				
		var reader = _this.operaGridReaderConfig;
		
		if(config.columns.length < 4 && isVersioned == 'true') {
			config.columns = btnColumnCfg.concat(config.columns);
		}
		
		// 操作列表表格
		config.store = new Ext.data.Store({
			reader: new Ext.data.JsonReader(reader)
		});
		
		var operaGridPanel = new Ext.grid.EditorGridPanel(Ext.apply({},config));
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
		
		// 图标单击事件
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
			function openOpera(opera, desc) {
				var importUnit = unit.replace(/\/.*/ig, '/engine/'+intf).replace(/:.*/ig, '');
				if (!!engineCmp){
					if (engineCmp.indexOf('process') == -1) {
						top.Application.createWindow(importUnit + '/' + opera, opera + '-' + desc, '../schema/encoding_viewer.jsp?schema=' + importUnit + '/' + opera + '.xsd' + '&unit=' + unit + '&unitId=' + unitId + '&unitDesc=' + escape(encodeURIComponent(unitDesc)) + '&ref=' + '&projectDesc=' + escape(encodeURIComponent(projectDesc)), 'settings');
					} else {
						top.Application.openProcess({
							id: importUnit + '/' + opera,
							text: opera + '-' + desc,
							pathname: importUnit + '/' + opera,
							unitId: unitId,
							params: '&unit=' + unit + '&unitId=' + unitId + '&unitDesc=' + escape(encodeURIComponent(unitDesc)) + '&projectDesc=' + escape(encodeURIComponent(projectDesc))
						});
					}
				} else {
					Application.createWindow(unit + '/' + opera, opera + '-' + desc, '../schema/encoding_viewer.jsp?schema=' + unit + '/' + opera + '.xsd' + '&unit=' + unit + '&unitId=' + unitId + '&unitDesc=' + escape(encodeURIComponent(unitDesc)) + '&ref=' + '&projectDesc=' + escape(encodeURIComponent(projectDesc)), 'settings');
				}
			};
			
			var record = grid.getStore().getAt(rowIndex);  // Get the Record
			var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
			var isImport = record.json['isPublic'];
			
			// 引用公共接口需要打开对应的操作模型
			if (!!isImport && fieldName != 'action') {
				var project = unit.replace(/\/.*/ig, '/');
				var intfId = options['portTypeName'].replace(/.*?:/ig, '');
				var pubIntf = project + 'intf/' + intfId;
				var intfDesc = record.json['intfDesc'];
				var opera = record.get('opera');
				var desc = record.get('desc');
				
				if (!!engineCmp && engineCmp.indexOf('process') != -1) {
					var importUnit = unit.replace(/\/.*/ig, '/engine/'+intf).replace(/:.*/ig, '');
					top.Application.openPublicIntf({
						id: pubIntf + '/' + opera, 
						text: opera + '-' + desc,
						src: '../schema/message_viewer.jsp?schema=' + pubIntf + '/' 
							+ opera + '.xsd' + '&unit=' + pubIntf + '&unitId=' + intfId 
							+ '&unitDesc=' + escape(encodeURIComponent(intfDesc)) 
							+ '&ref=' + '&projectDesc=' + escape(encodeURIComponent(projectDesc))
							+ '&isOriginalPublic=false',
						pathname: importUnit + '/' + opera
					});
				}else {
					Application.createWindow(
						intf + '/' + opera, opera + '-' + desc, 
						'../schema/message_viewer.jsp?schema=' + pubIntf + '/' 
						+ opera + '.xsd' + '&unit=' + pubIntf + '&unitId=' + intfId 
						+ '&unitDesc=' + escape(encodeURIComponent(intfDesc)) 
						+ '&ref=' + '&projectDesc=' + escape(encodeURIComponent(projectDesc))
						+ '&isOriginalPublic=false', 
						'settings');
				}
			} else {
				if (!!isClient) {
					if (fieldName != 'action' && fieldName != 'lock') 
						openOpera(record.get('opera'), record.get('desc'));
				}
				else {
					if (fieldName != 'action') 
						openOpera(record.get('opera'), record.get('desc'));
				}
			}
		});
		
		var formpanel = new Ext.form.FormPanel({
			title: bindingName,
			style: {'margin': '5 8 3 8'},
			autoHeight: true,
			frame: true,
			closable: true,
			collapsible: true,
			defaults: {
				anchor: '-18'
			},
			labelWidth: 120,
			layout : 'form',
			items: [
			{xtype: 'field',hideLabel:true,hidden: true,itemId:'interface', name:'interface',value: intf},
			{xtype: 'field',hideLabel:true,hidden: true,itemId:'oBindingName', name:'oBindingName',value: bindingName},	
			{
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
			},{	
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
				height: 60,
				itemId: 'enpointFieldSet',
				style: {'margin' : '0 0 10 5'},
				titleCollapse: true,
				bodyStyle: Ext.isIE ? 'padding-top: 10px;' : null,
				layout: 'column',
				title: '端点设置',
				collapsed: false,
				collapsible: true,
				items: [{
					columnWidth: 0.5,
					itemId: 'epColumn',
					layout: 'form',
					defaults: {anchor: '100%'},
					items: [{
						fieldLabel: '端点名称',
						xtype: 'textfield',
						cls: 'column',
						itemId: 'epName',
						name: 'epName',
						tag: 'attribute',
						value : endPoint
					}]
				},{
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
								fields: [{name: 'name'}, {name: 'url'}]
							}),
							listeners: {
								beforeload: function(store, options) {
									var transType = formpanel.find('itemId', 'transport')[0].getValue();
									if (!transType) {
										Ext.Msg.alert('提示:', '请先选择传输协议!');
										return false;
									} else {
										options.params.transportType = transType;
										return true;
									}
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
							beforequery: function(obj){
								delete obj.combo.lastQuery;
							},
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
	
	// 生成附加属性面板并赋值
	createExtaPanel: function(formpanel, schema, values) {
		var items = schema.items;
		if (!!items && items.length > 0) {
			Ext.each(items, function(item, index){
				var name = item.name;
				if (name.indexOf('column') != -1) {
					var columnItems = item.items;
					Ext.each(columnItems, function(columnItem){				
						var fieldName = columnItem.name;
						var value = '';
						if (!!values && values.length > 0) {
							Ext.each(values, function(obj, index){
								if (!!obj[fieldName]) 
									value = obj[fieldName];
							});
						} else {	// 如果wsdl文件中没有值则使用schema中设置的默认值
							value = columnItem.value;
						}
						columnItem.value = value;
						columnItem.labelStyle = 'padding: 0 0 0 5';
					});
				}
			});
			schema.itemId = 'properties';
			var panel = new Ext.Panel(schema);
			formpanel.insert(4, panel);
		}
	},
	
	// 删除绑定信息
	deleteBinding : function(intf, bindingName) {
		var _this = this;
		Ext.getBody().mask('正在删除...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET',
			url: _this.url,
			params: {
				operation : "deleteBinding",
				unit : unit,
				intf : intf,
				bindingName : bindingName
			},
			
			callback : function(options, success, response) {
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				} else {
					Ext.getBody().mask('删除成功', 'x-mask-loading');
					Ext.getBody().unmask.defer(500, Ext.getBody());
				} 	
			}
		});
	},
	
	// 同步绑定信息
	syncBinding: function(btnObj) {
		var _this = this;
		var mainPanel = btnObj.ownerCt.ownerCt;
		
		var data = {items: []};
		Ext.each(mainPanel.items.items, function(panel) {
			var item = {};
			item.intf = panel.get('interface').getValue();
			item.bindingName = panel.get('oBindingName').getValue();
			data.items.push(item);
		});
		
		if (data.items.length <= 0) {
			alert('目前没有添加任何绑定,无法同步!');
			return;
		}
		
		Ext.getBody().mask('正在同步...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'POST',
			url: _this.url,
			timeout: 3600 * 1000,
			params: {
				operation : "syncBinding",
				unit : unit,
				data: Ext.encode(data)
			},
			
			callback : function(options, success, response) {
				Ext.getBody().unmask();
				if (!!success) {
					_this.loadBindings();
				} else {
					_this.showException(response);
				}
			}
		});
	},
	
	// 保存绑定信息
	saveBindings : function(bindings, processTip, successTip) {
		var _this = this;
		var xmldoc = XDom.createDocument();
		var processMsg = 
			(processTip == 'undefiend' || !processTip)?'正在保存绑定信息...':processTip;
		var successMsg = 
			(successTip == 'undefiend' || !successTip)?'保存成功':successTip;
		
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
					
				var bindingEl = xmldoc.createElement('binding');
				bindingEl = setBindingAttrs(bindingEl, params);

				// 组件附加参数
				var extEl = xmldoc.createElement('extdata');
				extEl = setExtAttrs(extEl, params);
				bindingEl.appendChild(extEl);
				
				// 协议附加参数
				var properties = binding.getComponent('properties');
				if (!!properties && properties.isVisible()) {
					var transExtEl = xmldoc.createElement('properties');
					transExtEl = setExtChildren(transExtEl, extEl, properties);
					bindingEl.appendChild(transExtEl);
				}
				
				// operation节点
				var store = binding.get('operaGrid').store;
				store.each( function(record){
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
						component: component.replace(/.*\//ig, ''),
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
		} catch(e) {
			alert(e.message);
		} finally {
			delete xmldoc;
		}
	}
});

/**
 * 远程服务类 继承"代理服务-向导模式"
 */ 
var UnitClient = function(cfg){
	var config = Ext.apply({
		config: {
			url: 'unit_client_ctrl.jsp',
			dialogCfg: {height: 180},
			paramsFieldsets: null
		}
	});
	UnitClient.superclass.constructor.call(this, config);
}
Ext.extend(UnitClient, UnitServerGuide, {
	init: function() {
		this.preparation();
		var _this = this;
		
		// 接口设置Tab
		var portTypeCfg = {
			title: '接口设置',
			id: 'service-panel',
			animCollapse: false,
			animCollapse: false,
			autoScroll: true,
			border: false,
			collapsible: false,
			defaults: {anchor: '100%'},
			listeners: {
				activate: {	// 点击面板时重新加载数据
					fn: function(c){
						this.loadInterfaces();
					},
					scope: this
				}
			},
			footerCssClass: 'x-window-bc',
			buttonAlign: 'right',
			fbar: [
			{
				text: '新建接口',
				cls: 'x-btn-text-icon',
				icon: '../images/elcl16/add_obj.gif',
				scope: this,
				handler: function(){this.addIntf();}
			}, {
				text: '保存',
				ref:'../save',
				disabled: (isVersioned == 'true' && unitLocked == 'false')? true:false,
				handler: function(){
					var intfs = Ext.getCmp('service-panel').items.items;
					this.dataSaver(intfs);
				},
				scope: this
			},{
				text: '刷新',
				ref:'../reload',
				scope: this,
				listeners:{
					click:this.loadInterfaces,
					scope:this
				}
			}]
		};
		
		// 绑定设置Tab
		var rightTabCfg = {
			title: '绑定设置',
			id: 'binding-panel',
			animCollapse: false,
			autoScroll: true,
			border: false,
			collapsible: false,
			defaults: {anchor: '100%'},
			listeners: {
				activate: {
					fn: function(c){
						this.loadBindings();
					},
					scope: this
				}
			},
			footerCssClass: 'x-window-bc',
			buttonAlign: 'right',
			fbar: [
			{
				text: '添加绑定',
				cls: 'x-btn-text-icon',
				icon: '../images/elcl16/add_obj.gif',
				scope: this,
				handler: function(){this.selectBindingType();}
			},{
				text: '同步接口',
				cls: 'x-btn-text-icon',
				icon: '../images/elcl16/synced.gif',	// heliang
				handler: function(){
					_this.syncBinding(this);
				}
			},{
				text: '保存',
				ref:'../save',
				disabled: (isVersioned == 'true' && unitLocked == 'false')? true:false,
				handler: function(){
					var bindings = Ext.getCmp('binding-panel').items.items;
					this.saveBindings(bindings);
				},
				scope: this
			}, {
				text: '刷新',
				ref:'../reload',
				scope: this,
				listeners:{
					click:this.loadBindings,
					scope:this
				}
			}]
		};
		
		// "参数设置" 面板
		var leftTab = new Ext.form.FormPanel(Ext.apply({},_this.leftTabCfg));
		
		// "接口设置"面板
		var intfTab = new Ext.Panel(portTypeCfg);
		
		// "操作列表"(绑定设置) 面板
		var rightTab =  new Ext.Panel(rightTabCfg);
		
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
					width:30,
					text: '返回',
					icon: '../images/tool16/undo_edit.gif',
					cls: 'x-btn-text-icon',
					handler: function() {
						_this.openProject();
					}
				}
			]},
			style : 'border-right:0px;',
			items: [leftTab, intfTab, rightTab]
		});
		
		var viewport = new Ext.Viewport({
			stateId: 'unit_vi',
			stateful: false,
			layout: 'fit',
			items: tabPanel
		});
	},
	
	afterSubmit: function(form, formPanel, options){
		form.reset();
		if (options.data) {
			form.setValues(options.data);
		}
	},
	
	loadInterfaces: function() {
		var _this = this;
		Ext.getBody().mask('正在读取接口列表...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET',
			url: _this.url,
			params: {
				operation : 'loadPortTypes',
				unit : unit
			},
			callback : function(options, success, response) {
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				} else {
					var result = Ext.decode(response.responseText);
					var items = result.items;
					
					// 将返回结果分组
					var rs = {};
					if (!!items && items.length > 0)
					Ext.each(items, function(item, index){
						var item = item;
						var intf = item['interface'];
						var opera = item['opera'];
						
						if (!rs[intf]) {
							var group = [item];
							rs[intf] = group;
						} else {
							rs[intf].push(item);
						}
					});
					
					
					var groupItems = [];
					Ext.each(rs, function(intfGroup) {
						for (var key in intfGroup) {
							var temp = {};
							temp.items = intfGroup[key];
							groupItems.push(temp);
						}
					});
					
					// 创建操作列表面板
					var servicePanel = Ext.getCmp('service-panel');
					servicePanel.removeAll(true);
					if (groupItems.length > 0) {
						Ext.each(groupItems, function(item){
							var panel = _this.createOperaPanel(item);
							var isImport = item.items[0]['isPublic'];
							var grid = panel.getComponent('operaGrid');
							isImport ? _this.addImportCtxMenuListener(grid, item) : _this.addCtxMenuListener(grid, item);
							servicePanel.add(panel);
						});
					}
					servicePanel.doLayout();
				}
			}
		});
	},
	
	// Overwrite
	createOperaPanel: function(options) {
		var _this = this;
		
		var isImport = options.items[0]['isPublic'];
		var intf = options.items[0]['interface'];
		var intfDesc = options.items[0]['intfDesc'];
		var title = isImport? '公共接口:' + intf : intf;
		
		function renderOpera(value, cellmeta, record, rowIndex, columnIndex, store) {
			var isLocked = record.json['locked'];
			var str = "<img class=''innerImg' src='../images/elcl16/launch_disconnect.gif'>";
			if (isLocked == 'true')
				str = "<img class='innerImg' src='../images/tool16/lock.png'/>";
			if (isLocked == 'false')
				str = "<img class='innerImg' src='../images/tool16/unlock.png'/>";
			return str;
		};

		var btnColumnCfg = [{
			header: false,
			dataIndex: 'lock',
			menuDisabled: true,
			sortable: false,
			width: 25,
			fixed: true,
			align: 'center',
			renderer: renderOpera
		}];

		if(this.operaGridConfig.columns.length < 5 && isVersioned == 'true') {
			this.operaGridConfig.columns = btnColumnCfg.concat(this.operaGridConfig.columns);
		}
		
		// 操作列表表格
		this.operaGridConfig.store = new Ext.data.Store({
			reader: new Ext.data.JsonReader(
				this.listReaderMeta, 
				this.listReaderFields)
		});
		var operaGridPanel = 
			new Ext.grid.GridPanel(this.operaGridConfig);
				
		operaGridPanel.store.loadData({
			items: options.items
		});
		
		var formpanel = new Ext.form.FormPanel({
			title: title,
			style: {'margin': '5 8 3 8'},
			autoHeight: true,
			frame: true,
			closable: true,
			collapsible: true,
			defaults: {anchor: '100%'},
			labelWidth: 120,
			layout : 'form',
			items: [{
				xtype: 'field',
				hideLabel: true,
				hidden: true,
				itemId: 'oIntfName',
				name: 'oIntfName',
				value: intf
			},{
				fieldLabel: '接口名称',
				labelStyle: 'padding : 0 0 0 5',
				name: 'intfName',
				xtype: 'textfield',
				value: intf,
				disabled: isImport,
				allowBlank: false,
				listeners: {
					valid: function(field){
						formpanel.setTitle(field.getValue());
					}
				}
			},{
				fieldLabel: '接口描述',
				labelStyle: 'padding : 0 0 0 5',
				xtype: 'textfield',
				name: 'intfDesc',
				value: intfDesc,
				disabled: isImport,
				allowBlank: true
			}, operaGridPanel],
			tools: [
				{
				id: 'close',
				qtip: '删除接口',
				handler: function(e, toolEl, panel, tc){
					if (!confirm('确实要删除该接口吗?'))
						return;
					var intfName = panel.title.replace(/接口：/ig, '');
					_this.deleteIntf(intfName);
					panel.destroy();
					Ext.getCmp('service-panel').doLayout();
				}
			}]
		});
		
		return formpanel;
	},
	
	// "操作列表"(接口设置)数据保存
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
				portTypeEl.setAttribute('intfName', params['intfName']);
				portTypeEl.setAttribute('oldIntfName', params['oIntfName']);
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
					unit: unit,
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
		}catch(e) {
			alert(e.message);
		} finally {
			delete xmldoc;
		}
	},
	
	gridClickfn: function(rec) {
		var _this = this;
		if (!!rec.json['readOnly'] && !!rec.json['isPublic']) {
			var project = unit.replace(/\/.*/ig, '/');
			var intfId = rec.get('interface');
			var intf = project + 'intf/' + intfId;
			var intfDesc = rec.json['intfDesc'];
			var opera = rec.get('opera');
			var desc = rec.get('desc');
			Application.createWindow(
			intf + '/' + opera, 
				opera + '-' + desc, '../schema/message_viewer.jsp?schema=' + intf + '/' 
				+ opera + '.xsd' + '&unit=' + intf + '&unitId=' + intfId 
				+ '&unitDesc=' + escape(encodeURIComponent(intfDesc)) + '&ref=' 
				+ '&projectDesc=' + escape(encodeURIComponent(projectDesc)) 
				+ '&isOriginalPublic=false', 
			'settings');
				
		} else {
			_this.openOpera(rec.get('opera'), rec.get('desc'), '');
		}
	}
});

/** 
 * 本地服务类
 */
var UnitEngine = function(cfg){
	var bottomItems = [{
		fieldLabel: "命名空间(只读)",
		tag: "attribute",
		xtype: "textfield",
		name: "namespace",
		regex: /^\S+$/,
		readOnly: true,
		regexText: '命名空间不能为空'
	},{
		fieldLabel: "描述",
		tag: "attribute",
		xtype: "textarea",
		allowBlank: false,
		name: "documentation"
	}];
	if (!!cfg.bottomItems && cfg.bottomItems.length > 0)
		Ext.each(cfg.bottomItems, function(item) {
			bottomItems.push(item);
		});
	this.bottomItems = bottomItems;
	
	var listReaderFields = [
		{name: 'file'},
		{name: 'readOnly', type:'bool'}
	];
	if (!!cfg.listReaderFields && cfg.listReaderFields.length > 0)
		Ext.each(cfg.listReaderFields, function(item) {
			listReaderFields.push(item);
		});
	this.listReaderFields = listReaderFields;
	
	var config = Ext.apply({
		url: 'unit_ctrl.jsp',
		dialogCfg: {height: 180},
		paramsLoaderCfg: {
			operation: 'load',
			unit: unit
		},
		paramsSaverCfg: {
			operation: "saveInternal"
		},
		paramTopPanel: {
			fieldLabel: "引擎组件"
		},
		listReaderFields: [].concat(this.listReaderFields),
		paramMidPanel : {hidden : true},
		paramBottomPanelItems: [].concat(this.bottomItems),
		paramBottomPanel: {
			xtype: 'fieldset',
			autoHeight:true,
			border:true,
			defaults:{anchor:"100%", stateful:false},
			layout: "form",
			labelWidth: 130
		},
		rightBaseCfg: {
			fbar: [
			{
				text: '新建接口',
				cls: 'x-btn-text-icon',
				icon: '../images/elcl16/add_obj.gif',
				handler: function(){
					this.addIntf();
				},
				scope: this
			},{
				text: '导入接口',
				cls: 'x-btn-text-icon',
				icon: '../images/obj16/newint_wiz.gif',
				handler: function(){
					this.importIntf();
				},
				scope: this
			},{
				text: '保存',
				ref:'../save',
				disabled:(isVersioned == 'true' && unitLocked == 'false')? true:false,
				handler: function() {
					var intfs = Ext.getCmp('service-panel').items.items;
					this.dataSaver(intfs);
				},
				scope: this
			}, {
				text: '刷新',
				ref:'../reload',
				scope: this,
				listeners:{
					click:this.dataLoader,
					scope:this
				}
			}]
		}
	}, cfg.config);
	UnitEngine.superclass.constructor.call(this, config);
};
Ext.extend(UnitEngine, UnitBase, {
	init: function(){
		var _this = this;
		this.preparation();
		
		// "参数设置" 面板
		var leftTab = new Ext.form.FormPanel(_this.leftTabCfg);
		
		// "操作列表"(绑定设置) 面板
		var rightTab = new Ext.Panel(Ext.apply({}, _this.rightTabCfg));
		
		// "业务常量"设置面板
		var constantTab = new Ext.Panel();
		
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
					width:30,
					text: '返回',
					icon: '../images/tool16/undo_edit.gif',
					cls: 'x-btn-text-icon',
					handler: function() {
						_this.openProject();
					}
				}
			]},
			style : 'border-right:0px;',
			items: [leftTab, rightTab, 
					{
						title: '业务常量',
						hidden: compName == 'process-ec' || compName == 'adp-ec',
						border: false, autoShow: true,
						xtype: 'panel',
						bodyCfg: {
					        tag: 'iframe',
					        style: 'overflow:auto; display:block;',
					        frameBorder: 0,
					        src: "../console/busconstant_viewer.jsp?unit=" + unitId
						}
					}]
		});
		
		var viewport = new Ext.Viewport({
			stateId: 'unit_vi',
			stateful: false,
			layout: 'fit',
			items: tabPanel
		});
	},
	
	gridSaver: Ext.emptyFn,
	gridClickfn: function(rec) {
		var _this = this;
		if (rec.get('readOnly') && !rec.json['isPublic']) {
			return;
		} else if (rec.get('readOnly') && rec.json['isPublic'] && component.indexOf('process') != -1){
			var project = unit.replace(/\/.*/ig, '/');
			var intfId = rec.get('interface');
			var intf = project + 'intf/' + intfId;
			var intfDesc = rec.json['intfDesc'];
			var opera = rec.get('opera');
			var desc = rec.get('desc');
			
			if (component.indexOf('process') != -1) {
				top.Application.openPublicIntf({
					id: intf + '/' + opera, 
					text: opera + '-' + desc,
					src: '../schema/message_viewer.jsp?schema=' + intf + '/' 
						+ opera + '.xsd' + '&unit=' + intf + '&unitId=' + intfId 
						+ '&unitDesc=' + escape(encodeURIComponent(intfDesc)) 
						+ '&ref=' + '&projectDesc=' + escape(encodeURIComponent(projectDesc))
						+ '&isOriginalPublic=false',
					pathname: unit + '/' + opera
				});
			}else {
				Application.createWindow(
				intf + '/' + opera, opera + '-' + desc,
				'../schema/message_viewer.jsp?schema=' + intf + '/' 
				+ opera + '.xsd' + '&unit=' + intf + '&unitId=' + intfId 
				+ '&unitDesc=' + escape(encodeURIComponent(intfDesc)) 
				+ '&ref=' + '&projectDesc=' + escape(encodeURIComponent(projectDesc))
				+ '&isOriginalPublic=false', 
				'settings');
			}
		} else if (component.indexOf('process') == -1) {
			var opera = rec.get('opera');
			var desc = rec.get('desc');
			var ref = '';
			Application.createWindow(
				unit + '/' + opera,
				opera + '-' + desc,
				'../schema/encoding_viewer.jsp?schema='+ unit + '/' + opera + '.xsd&ref=' + ref + 
				'&unit=' + unit + '&unitId=' + unitId+ '&unitDesc=' + escape(encodeURIComponent(unitDesc)) + '&projectDesc=' + escape(encodeURIComponent(projectDesc)),
				'settings');
		} else {
			_this.openOpera(rec.get('opera'), rec.get('desc'), '');
		}
	},
	openOpera: function(opera, desc, file) {
		top.Application.openProcess({
			id: unit + '/' + opera,
			text: opera + '-' + desc,
			pathname: unit + '/' + opera,
			unitId: unitId,
			params: '&unit=' + unit + '&unitId=' + unitId+ '&unitDesc=' + escape(encodeURIComponent(unitDesc)) + '&projectDesc=' + escape(encodeURIComponent(projectDesc))
		});
	},
	afterSubmit: function(form, formPanel, options){
		form.reset();
		form.setValues(options.data);
	},
	dialogSubmit: function(dlg) {
		var _this = this;
		var formPanel = dlg.getComponent(0);
		var form = formPanel.getForm();
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
		
		var operaValue = form.getValues(false)['opera'].trim();
		if (!operaValue) {
			Ext.Msg.alert('提示:', '操作名不允许为空!');
			return;
		} else {
			form.setValues(Ext.apply(form.getValues(false), {opera: operaValue}));
		}

		form.submit({clientValidation: true, 
			params: Ext.apply(params, {
				operation: dlg.options.operation || 'createOpera'
			}),
			success: function(){ 
				var values = form.getValues();
				if (!values.file) 
					values.file = values.opera + '.wsdl';
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

// 服务单元工厂,根据页面传递的参数创建相应的对象
var UnitFactory = {
	newInstance: function(unitType){
		if (!!extendScript)
			return;
		var UnitObject = this.createUnit(unitType);
		if (!!UnitObject) {
			Ext.onReady(UnitObject.init, UnitObject, true);
		}
	},
	
	createUnit: function(unitType){
		var UnitObject;
		switch (unitType) {
			case 'serverCustom': // 代理服务(对外发布)——自定义模式
				UnitObject = new UnitServerCustom({});
				break;
			case 'serverGuide': // 代理服务(对外发布)——向导模式
				UnitObject = new UnitServerGuide({});
				break;
			case 'client': // 远程服务(外部主机)
				UnitObject = new UnitClient({});
				break;
			case 'engine': // 本地服务(内部服务)
				UnitObject = new UnitEngine({});
				break;
			default:
				break;
		}
		return UnitObject;
	}
};

UnitFactory.newInstance(unitType);