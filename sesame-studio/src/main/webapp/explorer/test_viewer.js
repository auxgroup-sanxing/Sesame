Ext.BLANK_IMAGE_URL='../images/s.gif';


var Viewer = function(){
//变量
var layout;

var NCName = {
	regex: /^[A-Za-z_]+[\w_\-.]*$/,
	regexText: '必须以字母或下划线开头，不能包含冒号和空白字符'
};

return {

	init: function(){
		var _this = this;

		Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
		//Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		Ext.QuickTips.init();
		var xg = Ext.grid;
	
		// shared reader
		var reader = new Ext.data.JsonReader({
			totalProperty: "count", 
			id: 'opera',
			root: "items"
		},[
		   {name: 'interface'},
		   {name: 'opera'},
		   {name: 'desc'},
		   {name: 'ref'},
		   {name: 'ref-svc'},
		   {name: 'ref-intf'},
		   {name: 'ref-opera'},
		   {name: 'modifier'},
		   {name: 'lastModified'}
		]);
		
		var unitName = unit.substring(unit.lastIndexOf('/')+1);
		
		var items = [{
			xtype: 'fieldset',
			layout: "form", 
			tag: "element", name:"attributes",
			title: '属性',
			autoHeight:true, border:true,
			defaults:{anchor:"100%", stateful:false},
			items: [
				{fieldLabel:"单元名称", tag:"element", xtype:"textfield", name:"name", value: unitName, allowBlank:false, readOnly:true},
				{fieldLabel:"描述", tag:"element", xtype:"textfield", name:"documentation"},
				//{fieldLabel:"服务名",tag:"attribute",xtype:"textfield",name:"service-name", allowBlank:false, regex: NCName.regex, regexText: NCName.regexText},
				{fieldLabel:"仿真类", tag:"attribute", xtype:"textfield", name:"emulator", allowBlank:false},
				{fieldLabel:"案例选择规则", tag:"attribute", xtype:"combo", name:"selector", forceSelection:true, triggerAction:"all", allowBlank:false, editable:false, store:[["first()","第一个"],["random()","随机"],["last()","最后一个"]]}
			]
		}];
		
		var viewport = new Ext.Viewport({
			stateId: 'test_unit',
			stateful: false,
			layout: 'fit',
			items: [{
				id: 'card-panel',
				activeItem: 'logger-panel',
				layout: 'card',
				stateful: false,
				bbar: [
			        {
						id: 'start-btn',
						text: '启动',
						cls: 'x-btn-text-icon',
						icon: '../images/elcl16/launch_run.gif',
						disabled: true,
						handler: function(){
			        		if (oriented=='server') {
					        	Ext.Msg.show({
									title:'自动发送',
									msg: '启动仿真机后，是否自动发送测试案例？',
									buttons: Ext.Msg.YESNOCANCEL,
									fn: function(btn){ if (btn!='cancel') _this.start(Ext.getCmp('comm-form'), btn=='yes'); },
									icon: Ext.MessageBox.QUESTION
								});
			        		}
			        		else {
			        			_this.start(Ext.getCmp('comm-form'), true);
			        		}
						}
			        },
			        {
						id: 'stop-btn',
						text: '停止',
						cls: 'x-btn-text-icon',
						icon: '../images/elcl16/launch_stop.gif',
						disabled: true,
						handler: function(){
			        		_this.stop(Ext.getCmp('comm-form'));
						}
			        },
			        '-',
				    '->',
				    {
					    id: 'card-left',
					    text: '&laquo;参数设置',
						handler: function(){
							var l = Ext.getCmp('card-panel').getLayout(); l.setActiveItem('comm-form');
							this.disable();  Ext.getCmp('card-center').enable(); Ext.getCmp('card-right').enable();
						}
					},{
					    id: 'card-center',
					    text: '操作列表',
						handler: function(){
							var l = Ext.getCmp('card-panel').getLayout(); l.setActiveItem('service-grid');
							this.disable();  Ext.getCmp('card-left').enable(); Ext.getCmp('card-right').enable();
						}
					},{
					    id: 'card-right',
					    text: '通讯日志&raquo;',
					    disabled: true,
						handler: function(){
							var l = Ext.getCmp('card-panel').getLayout(); l.setActiveItem('logger-panel');
							//var formPanel  = Ext.getCmp('comm-form');
							this.disable();  Ext.getCmp('card-left').enable(); Ext.getCmp('card-center').enable();
						}
					}
				],
				items: [{
					xtype: 'form',
					id: 'comm-form',
					title: '参数设置',
					animCollapse: false,
					autoScroll: true,
					border: false,
					collapsible: false,
					height: 220,
				    labelWidth: 130,
				    url: 'test_ctrl.jsp',
				    bodyStyle: 'padding:5px 5px 0; text-align:left;',
					layout: 'form', stateful:false,
					defaults: {anchor:"-20", stateful:false},
					items: items.concat(properties),
					buttonAlign: 'left',
					tbar: [{
				        text: '保存',
				        hidden: true,
						handler: function(){
							var formPanel = Ext.getCmp('comm-form');
							if (formPanel.getForm().isValid())
								_this.save(formPanel);
						}
					},{
				        text: '重载',
						handler: function(){
							var formPanel = Ext.getCmp('comm-form');
							_this.load(formPanel);
						}
					}]
				},{
			        title: '操作列表',
					xtype: 'grid',
					id: 'service-grid',
					border: false,
					enableHdMenu: false,
			        //iconCls: 'icon-grid'
			        store: new Ext.data.GroupingStore({
			            reader: reader,
						url: 'test_ctrl.jsp',
						baseParams: {	operation:'loadOperations', unit: unit},
			            sortInfo:{field: 'opera', direction: "ASC"},
			            groupField:'interface',
						listeners: {
							loadexception: function(proxy, obj, response){ _this.showException(response); }
						}
			        }),
			
			        columns: [
			            {header: "名称", id:'opera',width: 40, sortable: true, dataIndex: 'opera'},
			            {header: "注释", width: 70, sortable: true, dataIndex: 'desc'},
			            {header: "修改者", width: 20, sortable: true, dataIndex: 'modifier'},
			            {header: "", width: 20, sortable: true,  hidden:true, dataIndex: 'interface'},
			            {header: "修改时间", width: 20, sortable: true, dataIndex: 'lastModified'}
			        ],
			
			        view: new Ext.grid.GroupingView({
			            forceFit:true,
			            groupTextTpl: '{text} ({[values.rs.length]} 个)'
			        }),
			
			        frame: false,
			        width: '100%',
			        height: '100%'
			    },{
			        title: '日志',
					xtype: 'grid',
					id: 'logger-panel',
					border: false,
					enableHdMenu: false,
			        store: new Ext.data.Store({
			            reader: reader,
						url: 'test_ctrl.jsp',
						baseParams: {	operation:'loadLog', unit: unit},
			            sortInfo:{field: 'time', direction: "ASC"},
						listeners: {
							loadexception: function(proxy, obj, response){ _this.showException(response); }
						}
			        }),
			
			        columns: [
			            {header: "时间", width: 80, sortable: true, dataIndex: 'time'},
			            {header: "操作名", width: 60, sortable: true, dataIndex: 'opera'},
			            {header: "对方地址", width: 80, sortable: true, dataIndex: 'addr'},
			            {header: "内容", width: 220, sortable: false, dataIndex: 'content'}
			        ],
			        viewConfig: { forceFit: true },
			        frame: false,
			        width: '100%',
			        height: '100%'
			    }]
			}]
		});	
		
		this.getCurrentState();
		
		var formPanel  = Ext.getCmp('comm-form');
		_this.load(formPanel);
		
		var serviceGrid = Ext.getCmp('service-grid');
		serviceGrid.store.load();
		
	    var ctxMenu = new Ext.menu.Menu({
	     id: 'ctxmenu',
	     items: [
			{
				id: 'open',
				text: '打开',
				handler: function(){
				 	var item = this;
					Ext.each(ctxMenu.selections, function(rec){
						_this.openOpera(rec.get('opera'), rec.get('desc'), rec.get('ref'));
					});
				}
			},
			{
				id: 'property',
				text: '属性',
				handler: function(){
				 	var item = this;
					var rec = ctxMenu.selections[0];
					var callback = function(values){
						//rec.set('opera', values['opera']);
						//rec.set('desc', values['desc']);
						//rec.set('interface', values['interface']);
						serviceGrid.store.reload();
					};
					_this.getBindingDialog({title:'操作属性', operation:'modifyBinding', data: rec.data, callback:callback}).show();
				}
			},
			'-',
			{
				id: 'refresh',
				text: '刷新',
				icon:"../images/tool16/refresh.gif",
				handler: function(){ serviceGrid.store.reload(); }
			}]
		});
		serviceGrid.on("contextmenu", function(e){
			var sels = serviceGrid.selModel.getSelections();
			ctxMenu.selections = sels;
			ctxMenu.items.get('property').setDisabled(sels.length!=1);
			ctxMenu.items.get('open').setDisabled(sels.length<1);
			ctxMenu.showAt(e.getXY());
			e.stopEvent();
		});
		serviceGrid.on("rowdblclick", function(sender, rowIndex, e){
			var rec = serviceGrid.store.getAt(rowIndex);
			_this.openOpera(rec.get('opera'), rec.get('desc'), rec.get('ref'));
		});
	},
	
	getCurrentState : function(){
		var _this = this;
		Ext.Ajax.request({
			method: 'GET', 
			url: 'test_ctrl.jsp', 
			params: {operation : "getCurrentState", unit : unit },
			callback: function(options, success, response){
				if (success) {
					var result = Ext.decode(response.responseText);
					Ext.getCmp('start-btn').setDisabled(result.state==1);
					Ext.getCmp('stop-btn').setDisabled(result.state==0);
				}
				else {
					_this.showException(response);
				}
			}
		});
	},
	
	load : function(formPanel){
		var _this = this;
		
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

		formPanel.body.mask('正在装载组件参数...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET', 
			url: 'test_ctrl.jsp', 
			params: {operation : "load", unit : unit },
			callback: function(options, success, response){
				formPanel.body.unmask();
				if (success) {
					var rootEl = response.responseXML.documentElement;
					var attribEl = XDom.firstElement(rootEl, 'attributes');
					if (attribEl) attribEl.setAttribute('selector', 'last()');
					var docEl = XDom.firstElement(rootEl, 'documentation');
					if (docEl) formPanel.documentation = docEl.text || docEl.textContent;
					loadItems(rootEl, formPanel.items);
				}
				else {
					_this.showException(response);
				}
			}
		});
	},

	save : function(formPanel){
		var _this = this;
		var xmldoc = XDom.createDocument();
		try
		{
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
			
			var rootEl = xmldoc.createElement('unit');
			xmldoc.appendChild(rootEl);
			saveItems(rootEl, formPanel.items);
			formPanel.body.mask('正在保存参数...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'test_ctrl.jsp',
				params: {operation:"save", unit: unit, data: XDom.innerXML(xmldoc) },
				callback: function(options, success, response){
					formPanel.body.unmask();
					if (!success)
						_this.showException(response);
				}
			});
		}
		catch (e)
		{
			alert(e.message);
		}
		finally
		{
			delete xmldoc;
		}
	},
	
	start : function(formPanel, autorun){
		var _this = this;
		var xmldoc = XDom.createDocument();
		try
		{
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
							var value = (item.hiddenName ? formPanel.getForm().getValues()[item.hiddenName] : item.getValue());
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
			
			var rootEl = xmldoc.createElement('unit');
			xmldoc.appendChild(rootEl);
			saveItems(rootEl, formPanel.items);
			formPanel.body.mask('正在启动...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'test_ctrl.jsp',
				params: {operation:"start", unit: unit, data: XDom.innerXML(xmldoc), autorun: autorun },
				callback: function(options, success, response){
					formPanel.body.unmask();
					if (success) {
						Ext.getCmp('start-btn').disable();
						Ext.getCmp('stop-btn').enable();
					}
					else {
						_this.showException(response);
					}
				}
			});
		}
		catch (e)
		{
			alert(e.message);
		}
		finally
		{
			delete xmldoc;
		}
	},

	stop : function(formPanel){
		var _this = this;
		try
		{
			formPanel.body.mask('正在停止...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'test_ctrl.jsp',
				params: {operation:"stop", unit: unit },
				callback: function(options, success, response){
					formPanel.body.unmask();
					if (success) {
						Ext.getCmp('stop-btn').disable();
						Ext.getCmp('start-btn').enable();
					}
					else {
						_this.showException(response);
					}
				}
			});
		}
		catch (e)
		{
			alert(e.message);
		}
		finally
		{
			delete xmldoc;
		}
	},

	getBindingDialog : function(options){
		var _this = this;
		
		if (!this.serviceDlg) {
			var svcStore = new Ext.data.JsonStore({
				url: 'test_ctrl.jsp',
				baseParams: {	operation:'getAvailServices', unit: unit},
				root: 'items',
				fields: [
					{name:'label', type:'string'},
					{name:'service', type:'string'},
					{name:'intf', type:'string'},
					{name:'opera', type:'string'},
					{name:'name', type:'string'}
				],
				listeners: {
					loadexception: function(proxy, obj, response){ _this.showException(response); }
				}
			});
			
		    var dlg = new Ext.Window({
				title: '新建操作',
		        autoCreate: true,
		        resizable:false,
		        constrain:true,
		        constrainHeader:true,
		        minimizable:false,
		        maximizable:false,
		        stateful:false,
		        modal:true,
		        buttonAlign: "right",
				defaultButton: 0,
		        width: 450,
		        height: 270,
				minWidth: 300,
		        footer: true,
		        closable: true,
		        closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: [{
					xtype: 'form',
					autoScroll: true,
					labelWidth: 90,
					border: false,
					bodyStyle: 'padding:10px;',
					defaultType: 'textfield',
					defaults: { anchor: '-18', stateful:false  },
					url: 'test_ctrl.jsp',
					baseParams: {unit: unit},
					items: [
						{
							name: 'opera',
							allowBlank: false,
							fieldLabel: '操作名称'
					    },{
							name: 'desc',
							fieldLabel: '描述'
					    },{
							name: 'interface',
							xtype: 'combo',
							fieldLabel: '接口',
							allowBlank: false,
							forceSelection: false,
							triggerAction: 'all', editable: true,
							valueField: 'name', displayField: 'label',
							store: new Ext.data.JsonStore({
								url: 'test_ctrl.jsp',
								baseParams: {	operation:'getInterfaces', unit: unit},
								root: 'items',
								fields: [
									{name:'label', type:'string'},
									{name:'name', type:'string'}
								],
								listeners: {
									loadexception: function(proxy, obj, response){ _this.showException(response); }
								}
							})
					    },{
							name: 'ref',
							hiddenName: 'ref',
							xtype: 'combo',
							fieldLabel: '目标服务',
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
								regex: /^{.*}[A-Za-z_]\w*/, 
								regexText:'服务名的格式为:{http://www.example.com/}serviceName'
						    },{
								fieldLabel: '目标服务接口',
								name: 'ref-intf',
								regex: /^{.*}[A-Za-z_]\w*/, 
								regexText:'接口名的格式为:{http://www.example.com/}portType'
						    },{
								fieldLabel: '目标操作',
								name: 'ref-opera',
								regex: /^{.*}[A-Za-z_]\w*/, 
								regexText:'操作名的格式为:{}operation'
						    }]
						}]
					}]
		    });
			
		    dlg.cancel = dlg.addButton('关闭', function(){ dlg.hide(); });
		    
			dlg.render(Ext.getBody());
		    this.serviceDlg = dlg;
		}
		this.serviceDlg.options = options;
		var formPanel = this.serviceDlg.getComponent(0);
		var form = formPanel.getForm();
		var store = form.findField('ref').store;
		form.reset();
		store.reload({callback:function(){
			var target = formPanel.getComponent('target-group');
			if (options.data) {
				form.setValues(options.data);
				var value = options.data.ref;
				target.setVisible(!value);
			}
			else {
				target.setVisible(true);
			}
		}});
		this.serviceDlg.setTitle(options.title || '创建操作');
		
        return this.serviceDlg;
	},
	
	openOpera : function(opera, desc, ref){
		Application.createWindow(unit+'/'+opera, opera+"-"+desc+'仿真', "../explorer/case_viewer.jsp?oriented="+
			oriented+'&endpoint='+endpoint+'&schema='+opera+'.xsd&unit='+unit, 'tabs');
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
