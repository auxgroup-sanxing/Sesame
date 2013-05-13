Ext.BLANK_IMAGE_URL='../images/s.gif';

var Book = function(){

//参数设置Form缓存
var cache = {};

var NCName = {
	regex: /^[A-Za-z_]+[\w_\-.]*$/,
	regexText: '必须以字母或下划线开头，不能包含冒号和空白字符'
};

return {

	init: function(){
		var _this = this;

		Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
		Ext.QuickTips.init();
		
		var Address = Ext.data.Record.create([{
	        name: 'name',
	        type: 'string'
	    }, {
	        name: 'scheme',
	        type: 'string'
	    }, {
	        name: 'uri',
	        type: 'string'
	    }, {
	        name: 'style',
	        type: 'string'
	    }]);
		
	    var addressBook = new Ext.data.GroupingStore({
	        reader: new Ext.data.JsonReader({fields: Address}),
	        url: "address_ctrl.jsp",
	        baseParams: {operation:'getLocations' },
	        sortInfo: {field: 'name', direction: 'ASC'},
	        listeners: {
				exception : function(proxy, type, action, options, response, arg){
					if (type=='response') {
						_this.showException(response);
					}
					else {
						alert(response['error-message']);
					}
				}
			}
	    });
	    
	    var editor = new Ext.ux.grid.RowEditor({
	        saveText: '保存',
	        cancelText: '取消',
	        clicksToEdit: 2,
	        listeners: {
	        	'afteredit' : function(editor, changes, rec, rowIndex) {
	        		var oldName = typeof(rec.modified['name'])=='undefined' ? rec.get('name') : rec.modified['name'];
	        		
					Ext.Ajax.request({
						method: 'POST', 
						url: 'address_ctrl.jsp', 
						params: Ext.apply({operation : "saveLocation", 'old-name': oldName }, rec.data),
						callback: function(options, success, response){
							if (success) {
								addressBook.reload();
							}
							else {
								_this.showException(response);
							}
						}
					});
	        	}
	        }
	    });
		
		var dataGrid = new Ext.grid.GridPanel({
			region:'center',
			itemId: 'book-panel',
			xtype: 'grid',
			border: false,
	        view: new Ext.grid.GroupingView({
	        	forceFit: true,
	            markDirty: false
	        }),
			store: addressBook,
	        columns: [
              new Ext.grid.RowNumberer(),
              {
                  id: 'name',
                  header: '地址名称',
                  dataIndex: 'name',
                  width: 120,
                  sortable: true,
                  editor: {
                      xtype: 'textfield',
                      allowBlank: false
                  }
              },{
                  header: '协议',
                  dataIndex: 'scheme',
                  width: 100,
                  sortable: true,
                  editor: {
					xtype: 'combo',
					allowBlank: false,
					forceSelection: true, editable: false,
					store: new Ext.data.JsonStore({
						url: 'address_ctrl.jsp',
						baseParams: {operation:'getSchemes'},
						fields: [{name:'scheme', type: 'string'}, {name:'label', type: 'string'}]
					}),
					valueField: 'scheme', displayField: 'label',
					triggerAction: 'all'
                  }
              },{
                  header: '地址',
                  dataIndex: 'uri',
                  width: 200
              },{
                  header: '类别',
                  dataIndex: 'style',
                  align: 'center',
                  width: 100,
                  editor: {
					xtype: 'combo',
					fieldLabel: '地址类型',
					allowBlank: false,
					forceSelection: true, editable: false,
					store: [['local','本地地址'], ['remote','远程地址']],
					triggerAction: 'all'
                  }
            }],
			listeners: {
				rowdblclick : function(grid, rowIndex, e){
					grid.getTopToolbar().get('prop').handler(rowIndex);
				}
			},
			tbar: [{
				itemId: 'plus',
				icon: '../images/tool16/abook_add.gif',
				text: '添加',
				cls: 'x-btn-text-icon',
				handler: function(e){
					var panel = viewport.get('book-panel');
					var rec = new Address({name:''});
					var callback = function(rec){
						rec.commit();
						addressBook.add([rec]);
						panel.getSelectionModel().selectRow(addressBook.getCount()-1);
					};
					_this.getEditDialog({record: rec, callback: callback }).show();
					
				}
			},{
				itemId: 'minus',
				icon: '../images/icons/delete.gif',
				text: '删除',
				cls: 'x-btn-text-icon',
				handler: function(e){
					var panel = viewport.get('book-panel');
					var rec = panel.getSelectionModel().getSelected();
					if (!rec) return;
					if (!confirm("确定要删除选中的地址吗？")) return;
					
					Ext.Ajax.request({
						method: 'POST', 
						url: 'address_ctrl.jsp', 
						params: {operation : "removeLocation", name: rec.get('name') },
						callback: function(options, success, response){
							if (success) {
								addressBook.remove(rec);
								viewport.get('conf').getLayout().setActiveItem('blank');
								viewport.get('conf').setTitle('参数设置');
							}
							else {
								_this.showException(response);
							}
						}
					});
				}
			},{
				itemId: 'edit',
				icon: '../images/icons/edit.gif',
				text: '修改',
				cls: 'x-btn-text-icon',
				handler: function(e){
					var panel = viewport.get('book-panel');
					var rec = panel.getSelectionModel().getSelected();
					if (!rec) return;
					
					var callback = function(rec){
						rec.commit();
						var formPanel = viewport.get('conf').getLayout().activeItem;
						if (formPanel!=null && formPanel.record==rec) {
							viewport.get('conf').setTitle(rec.get('name')+' - 参数设置');
						}
					};
					_this.getEditDialog({record: rec, callback:callback }).show();
				}
			},
			'-',
			{
				itemId: 'prop',
				cls: 'x-btn-text-icon',
				icon: '../images/elcl16/templateprop_co.gif',
				text: '参数设置',
				handler : function(rowIndex){
					var panel = viewport.get('book-panel');
					// var rec = panel.getSelectionModel().getSelected();
					var rec = panel.getStore().getAt(rowIndex);
					if (!rec) return;

					var confPanel = viewport.get('conf');
					confPanel.setTitle(rec.get('name')+' - 参数');
					if (confPanel.collapsed) {
						confPanel.expand();
					}
								
					var panel = confPanel.get(rec.get('scheme')+'-'+rec.get('style'));
					if (panel != null) {
						if (panel.getXType()=='form')
						panel.getForm().reset();
						_this.loadConfig(panel, rec);
						
						confPanel.getLayout().setActiveItem(panel);
						return;
					}
					
					Ext.Ajax.request({
						method: 'GET', 
						url: 'address_ctrl.jsp', 
						params: {operation : "loadProperties", scheme: rec.get('scheme'), style: rec.get('style') },
						callback: function(options, success, response){
							if (success) {
								var result = Ext.decode(response.responseText);
								if (result==null) {
									var panel = confPanel.add({
										itemId: rec.get('scheme')+'-'+rec.get('style'),
										xtype: 'panel',
										border: false, bodyStyle: 'padding: 10px 20px 10px 10px;',
										html: '<div style="align:center">无参数可设置</div>'
									});
									confPanel.getLayout().setActiveItem(panel);
									return;
								}
								
								var createRegex=function(item){
									if (item.regex) item.regex = new RegExp(item.regex);
									if (item.items) Ext.each(item.items, createRegex);
								};
								createRegex(result);
								
								Ext.apply(result, {
									itemId: rec.get('scheme')+'-'+rec.get('style'),
									xtype: 'form',
									title: null,
									autoScroll: true, autoHeight: false,
									border: false, bodyStyle: 'padding: 10px 20px 10px 10px;',
									stateful:  false
								});
								var formPanel = confPanel.add(result);
								confPanel.getLayout().setActiveItem(formPanel);
								_this.loadConfig(formPanel, rec);
							}
							else {
								_this.showException(response);
							}
						}
					});
				}
			},
			'-',
			{
				icon:"../images/tool16/refresh.gif",
				text: '刷新',
				cls: 'x-btn-text-icon',
				handler: function(e){
					addressBook.reload();
					viewport.get('conf').getLayout().setActiveItem('blank');
					viewport.get('conf').setTitle('参数设置');
				}
			},
			'->', 
			{
				icon:"../images/elcl16/launch_profile.gif",
				text: '提交变更',
				tooltip: '使地址簿的变更在运行环境中生效',
				cls: 'x-btn-text-icon',
				handler: function(e){
					viewport.el.mask("提交变更...");
					Ext.Ajax.request({
						method: 'POST', 
						url: 'address_ctrl.jsp',
						params: {operation:"affect" },
						callback: function(options, success, response){
							if (success) {
								viewport.el.mask("提交成功");
								viewport.el.unmask.defer(1000, viewport.el);
							}
							else {
								viewport.el.unmask();
								_this.showException(response);
							}
						}
					});
				}
			}]
		});
		
		dataGrid.on('render', function(grid) {
			var view = grid.getView();
			dataGrid.tip = new Ext.ToolTip({
		        target: dataGrid.getView().mainBody,
		        delegate: '.x-grid3-row',
		        trackMouse: true,
		        renderTo: document.body,
		        listeners: {
		            beforeshow: function updateTipBody(tip) {
		                tip.body.dom.innerHTML = "双击该行显示详细参数";  
		            }
		        }
		    });
		});
		
		var viewport = new Ext.Viewport({
			stateful: false,
			layout: 'border',
			items: [dataGrid,
			{
				region: 'south',
				itemId: 'conf',
				title: '参数',
				xtype: 'panel',
				height: 200,
				border: false, split: true, collapsible: true, collapseFirst: false,
				layout: 'card', activeItem: 0,
				items: [{
					itemId : 'blank',
					border: false
				}],
				tools: [{
					id: 'save',
					qtip: '保存',
					handler: function(e, toolEl, panel, tc){
						var activePanel = panel.getLayout().activeItem;
						if (!activePanel || activePanel.getXType()!='form') return;
			    		if (!activePanel.getForm().isValid()) return;

						var r = activePanel.record;
			    		_this.saveConfig(activePanel, r.get('name'));
					}
				}]
			}]
		});
		
		addressBook.load({
			callback: function(rec, options, success) {
				if (success) {
					if (!!externalAddr) {
						var rowIndex = dataGrid.store.find('name', externalAddr, 0, true, false);
						if (rowIndex != -1) {
							var gridEl = dataGrid.getView().getRow(rowIndex);
							dataGrid.getSelectionModel().selectRow(rowIndex);
							dataGrid.fireEvent('rowdblclick', dataGrid, rowIndex);	
						}
					}
				}
			}
		});
	},
	
	loadConfig: function(formPanel, r){
		var _this = this;
		formPanel.record = r;
		
		Ext.Ajax.request({
			method: 'POST', 
			url: 'address_ctrl.jsp',
			params: {operation:"loadConfig", name: r.get('name') },
			callback: function(options, success, response){
				if (success) {
					_this.loadItems(response.responseXML.documentElement, formPanel.items);
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
	
	saveConfig : function(formPanel, locationName){
		var _this = this;
		var xmldoc = XDom.createDocument();
		try
		{
			var rootEl = xmldoc.createElement('params');
			xmldoc.appendChild(rootEl);
			_this.saveItems(rootEl, formPanel.items);
		
			formPanel.bwrap.mask("正在保存...");
			Ext.Ajax.request({
				method: 'POST', 
				url: 'address_ctrl.jsp',
				params: {operation:"saveConfig", name: locationName, data: XDom.innerXML(xmldoc) },
				callback: function(options, success, response){
					if (success) {
						formPanel.bwrap.mask("保存成功");
						formPanel.bwrap.unmask.defer(1000, formPanel.bwrap);
					}
					else {
						formPanel.bwrap.unmask();
						_this.showException(response);
					}
				}
			});
		}
		catch (e) {
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

	getEditDialog : function(options){
		var _this = this;
		
		var dlg = this.editDlg;
		if (!dlg) {
			
		    dlg = new Ext.Window({
				title: '编辑地址',
		        autoCreate: true,
		        resizable: true, constrain:true, constrainHeader:true,
		        minimizable:false, maximizable:false, stateful:false, modal:true,
		        buttonAlign: "right", defaultButton: 0,
		        width: 500, height: 210, minWidth: 300,
		        footer: true, plain: false,
		        closeAction: 'hide',
				layout: 'fit',
				items: [{
					itemId: 'form',
					xtype: 'form',
					autoScroll: true, autoHeight: false,
					border: false,
					bodyStyle: 'padding: 10px 20px 10px 10px;',
					stateful:  false,
					items: [{
	                      fieldLabel: '地址名称',
	                      name: 'name',
	                      sortable: true,
	                      xtype: 'textfield',
	                      allowBlank: false
                  	},{
	                    fieldLabel: '协议',
	                    name: 'scheme',
	                    sortable: true,
						xtype: 'combo',
						allowBlank: false,
						forceSelection: true, editable: false,
						store: new Ext.data.JsonStore({
							url: 'address_ctrl.jsp',
							baseParams: {operation:'getSchemes'},
							fields: [{name:'scheme', type: 'string'}, {name:'label', type: 'string'}]
						}),
						valueField: 'scheme', displayField: 'label',
						triggerAction: 'all'
                  	},{
                      	fieldLabel: '地址',
                      	name: 'uri',
                        xtype: 'textfield',
                        allowBlank: false,
						regex: new RegExp("^(//)?"
										+ "(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // ftp的user@
										+ "(([0-9]{1,3}\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
										+ "|" // 允许IP和DOMAIN（域名）
										+ "([0-9a-z_!~*'()-]+\.)*" // 域名- www.
										+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\." // 二级域名
										+ "[a-z]{2,6})" // first level domain- .com or .museum
										+ "(:[0-9]{1,5})?" // 端口- :80
										+ "((/?)|" // a slash isn't required if there is no file name
										+ "(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)$"),
						regexText: "非法的地址，地址示例: //hostname:port/[file[?params]]"
                  	},{
	                    name: 'style',
						xtype: 'combo',
						fieldLabel: '地址类型',
						allowBlank: false,
						forceSelection: true, editable: false,
						store: [['local','本地地址'], ['remote','远程地址']],
						triggerAction: 'all'
	                }]
				}],
			    buttons: [{
			    	text: Ext.MessageBox.buttonText.ok, 
			    	handler: function(){
			    		var formPanel = dlg.getComponent(0);
			    		if (!formPanel.getForm().isValid()) return;

						var rec = dlg.options.record;
			    		formPanel.getForm().updateRecord(rec);
			    		
		        		var oldName = typeof(rec.modified['name'])=='undefined' ? rec.get('name') : rec.modified['name'];
		        		
						Ext.Ajax.request({
							method: 'POST', 
							url: 'address_ctrl.jsp', 
							params: Ext.apply({operation : "saveLocation", 'old-name': oldName }, rec.data),
							callback: function(options, success, response){
								if (success) {
			    					dlg.hide();
			    					dlg.options.callback(rec);
									//addressBook.reload();
								}
								else {
									_this.showException(response);
								}
							}
						});
			    	}
			    },{
			    	text: Ext.MessageBox.buttonText.cancel, 
			    	handler: function(){ dlg.hide(); }
			    }]
		    });
		    
			dlg.render(Ext.getBody());
		    this.editDlg = dlg;
		}
		
		dlg.options = options;
		var formPanel = dlg.getComponent(0);
		formPanel.getForm().reset();
		formPanel.getForm().loadRecord(options.record);

        return dlg;
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
			title: '系统提示',
			msg: '<div style="width:100%; height:auto; background-color:white; overflow:auto;">'+statusText+'</div>',
			buttons: Ext.MessageBox.OK,
			width: 492
		});
	}
}
}();

Ext.onReady(Book.init, Book, true);
