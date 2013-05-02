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
	
		var unitName = unit.substring(unit.lastIndexOf('/')+1);
		
		var items = [
			{fieldLabel:"案例名称",tag:"attribute",xtype:"textfield",name:"name", allowBlank:false, readOnly:false},
			{fieldLabel:"描述",tag:"attribute",xtype:"textfield",name:"documentation"}
		];
		
		var tabs = [];
		for (var i=0; i<cases.length; i++) {
			var c = cases[i];
			var tab = _this.createTab(c.name, '✿'+c.documentation, items.concat(properties));
			tabs.push(tab);
			_this.load(tab);
		}
		if (tabs.length==0) tabs.push(_this.createTab(null, null, items.concat(properties)));
		
		var viewport = new Ext.Viewport({
			stateful: false,
			layout: 'border',
			items: [{
				id: 'card-panel',
				activeItem: 0,
				region: 'center',
				xtype: 'tabpanel',
				enableTabScroll: true,
				stateful: false,
				items: tabs,
				listeners: {
					beforeremove: function(sender, tab){
						if (this.items.length==1) return false;
						
						if (!confirm('确实要删除 '+tab.title+' 吗？')) {
							return false;
						}
						
						Ext.Ajax.request({
							method: 'POST', 
							url: 'test_ctrl.jsp',
							params: {operation:"removeCase", unit: unit, schema: schema, casename: tab.getForm().getValues(false).name },
							callback: function(options, success, response){
								if (!success) _this.showException(response);
							}
						});
					}
				}
			},{
				region: 'south',
				xtype: 'panel',
				autoHeight: true,
				stateful: false,
				bbar: [
				    {
					    id: 'save-btn',
					    text: '保存',
						cls: 'x-btn-text-icon',
						icon: '../images/tool16/save_edit.gif',
						handler: function(){
							var formPanel = Ext.getCmp('card-panel').getActiveTab();
							if (formPanel && formPanel.getForm().isValid())
								_this.save(formPanel);
						}
					},{
					    id: 'load-btn',
					    text: '重载',
						cls: 'x-btn-text-icon',
						icon: '../images/tool16/undo_edit.gif',
						handler: function(){
							var formPanel  = Ext.getCmp('card-panel').getActiveTab();
							if (formPanel)  _this.load(formPanel);
						}
					},
					'-',
					{
					    id: 'create-btn',
					    text: '新建案例',
						cls: 'x-btn-text-icon',
						icon: '../images/icons/new_wiz.gif',
						handler: function(){
							var formPanel  = _this.createTab(null, null, items.concat(properties));
							var tabFolder = Ext.getCmp('card-panel');
							tabFolder.add(formPanel);
							tabFolder.setActiveTab(formPanel);
						}
					},
					{
					    id: 'send-btn',
					    text: '发送',
						cls: 'x-btn-text-icon',
						icon: '../images/elcl16/mail_send.png',
						hidden: oriented!='server',
						handler: function(){
							var formPanel = Ext.getCmp('card-panel').getActiveTab();
							if (formPanel && formPanel.getForm().isValid())
								_this.send(formPanel);
						}
					}
				]
			}]
		});
		var tabFolder = Ext.getCmp('card-panel');
        var ctxMenu = new Ext.menu.Menu([{
            id: 'move-first',
            text: '移到最前',
            handler : function(){
        		var ctxItem = ctxMenu.popItem;
        		//tabFolder.remove(ctxItem, false);
        		tabFolder.insert(0, ctxItem);
            }
        },{
            id: 'move-last',
            text: '移到最后',
            handler : function(){
        		var ctxItem = ctxMenu.popItem;
        		//tabFolder.remove(ctxItem, false);
        		tabFolder.add(ctxItem);
            }
        }]);
		tabFolder.on('contextmenu', function(ts, item, e){
			ctxMenu.popItem = item;
			ctxMenu.showAt(e.getPoint());
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

		//formPanel.body.mask('正在装载测试案例数据...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET', 
			url: 'test_ctrl.jsp', 
			params: {operation : "loadCase", unit : unit, schema: schema, casename: formPanel.title },
			callback: function(options, success, response){
				//formPanel.body.unmask();
				if (success) {
					var rootEl = response.responseXML.documentElement;
					formPanel.schema = rootEl.getAttribute('schema');
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

	loadItems : function(parentEl, items) {
		var _this = this;
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
					_this.loadItems(el, item.items);
					el.setAttribute('used', true);
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
			formPanel.body.mask('正在保存测试案例...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'test_ctrl.jsp',
				params: {operation:"saveCase", unit: unit, schema: schema, data: XDom.innerXML(xmldoc) },
				callback: function(options, success, response){
					formPanel.body.unmask();
					if (success)
						formPanel.setTitle(formPanel.getForm().getValues(false).name);
					else
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

	saveItems : function(parentEl, items){
		var _this = this;
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
				if (item.items) _this.saveItems(el, item.items);
			}
		});
	},

	send : function(formPanel){
		var _this = this;
		var xmldoc = XDom.createDocument();
		try
		{
			var rootEl = xmldoc.createElement('unit');
			xmldoc.appendChild(rootEl);
			_this.saveItems(rootEl, formPanel.items);
			formPanel.body.mask('正在发送测试数据...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'test_ctrl.jsp',
				params: {operation:"sendCase", unit: unit, schema: schema, data: XDom.innerXML(xmldoc) },
				callback: function(options, success, response){
					formPanel.body.unmask();
					if (success) {
						var resEl = response.responseXML.documentElement;
						var panel = formPanel.find('name', 'response')[0];
						if (panel) _this.loadItems(resEl, panel.items);
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
	
	createTab : function(title, tip, items){
		var _this = this;
		return new Ext.form.FormPanel({
			xtype: 'form',
			title: title || '新案例',
			tabTip: tip,
			animCollapse: false, collapsible: false, autoScroll: true,
			border: false, closable: true,
			height: 220,
		    labelWidth: 130,
		    url: 'test_ctrl.jsp',
		    bodyStyle: 'padding:5px 5px 0; text-align:left;',
			layout: 'form', stateful:false,
			defaults: {anchor:"-20", stateful:false},
			items: items,
			listeners: {
			}
		});
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
		Application.createWindow(unit+'/'+opera, opera+"-"+desc+'仿真', "../schema/case_viewer.jsp?schema="+
			oriented+'/'+endpoint+'/'+opera+'.xsd&unit='+unit, 'tabs');
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
