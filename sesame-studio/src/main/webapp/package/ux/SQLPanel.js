/**
 * SQL 浏览器
 * @class Ext.ux.SQLPanel
 * @extends Ext.tree.TreePanel
 */
Ext.ux.SQLPanel = Ext.extend(Ext.tree.TreePanel, {
	constructor: function(config) {
		var treeLoader = new Ext.tree.TreeLoader({dataUrl:'../SQLExplorer', baseParams: {action:'getChildren'}});
		var toolbar = new Ext.Toolbar({
			style : 'border-left:none;border-top:none;padding:2px 0px;'
		});
		Ext.ux.SQLPanel.superclass.constructor.call(this, Ext.apply(config, {loader: treeLoader, lines: true, containerScroll: true, tbar:toolbar}));
		this.contextMenu=null;
		var _this = this;
		
		var appendNode = function(){
	        var selection = _this.getSelectionModel();
			var n = selection.getSelectedNode();
	        if(n && n.attributes.allowAppend){
				if (n.id=='TABLE')	{
					_this.createTable(n);
				}
				else if (n.id.indexOf("columns_")==0)	{
					var id = n.parentNode.id;
					openEditor('alter_table', '设计器 - '+n.parentNode.text, '../sqlex/table_dsgn.jsp?object='+id);
				}
				else if (n.id.indexOf("indices_")==0) {
					_this.createIndex(n);
				}
	        }
		};
		
		var modifyNode = function(){
			var n = _this.selModel.getSelectedNode();
	        if(n && n.attributes.allowModify){
				var id = n.id;
				if (id.indexOf("table_")==0 || id.indexOf("view_")== 0){
					openEditor('alter_table', '设计器 - '+n.text, '../sqlex/table_dsgn.jsp?object='+id);
				} else if (n.attributes.meta == "INDEX") {
					_this.setIndexAttr(n);
				}
	        }
		};
		
		var removeNode = function(){
	        var selection = _this.getSelectionModel();
			var n = selection.getSelectedNode();
	        if(n && n.attributes.allowDelete){

				var fn = function(button){
					if (button=='cancel') return;
					var attr = {};
					Ext.apply(attr, n.attributes);
					delete attr.loader;
					Ext.Ajax.request({
						method:'POST', 
						url: _this.loader.dataUrl, 
						success: function(response, options){ selection.selectPrevious(); n.parentNode.removeChild(n); },
						failure: function(response, options){ _this.showException(response); },
						params: {action:"remove", drop: button, data: Ext.encode(attr)}
					});
				};
				
				Ext.MessageBox.show({
					title:'确认',
					msg: '"'+n.text+'" 将要被删除，同时从数据库中删除该对象吗? <br />点击[保留]只删除元信息',
					buttons: { no: '保留', cancel: '取消'},
					defaultButton: 1,
					fn: fn,
					icon: Ext.MessageBox.QUESTION
				});
	        }
		};
	
		var browseTable = function(){
			var n = _this.selModel.getSelectedNode();
	        if(n && n.attributes.allowBrowse){
				var id = n.id.replace(/[a-z]+_/, "");
				if (n.id.indexOf("table_")==0)
					openEditor('browse_'+id, '浏览 - '+n.text, '../sqlex/table_brow.jsp?table='+id);
	        }
		};
		var setProperty = function(){
			var n = _this.selModel.getSelectedNode();
	        if(n && n.attributes.allowSet){
				if (n.id=="ROOT"){
					Ext.Ajax.request({
						method:'GET', 
						url: _this.loader.dataUrl, 
						callback: function(options, success, response){ if (success) alert(response.responseText); else _this.showException(response); },
						params: {action:"getDBInfo"}
					});
				} else if (n.id.indexOf("table_")==0) {
					_this.setTableAttr(n);
				}
	        }
		};
		
		var refresh = function(node){
			node.reload();
			node.collapse(true, false);
			node.expand();
		};
		
		var alter = function(){
			if (!confirm("如果表存在将会修改表的结构，否则执行创建操作\n确定要执行此操作吗 ? ")) return;
			var n = _this.selModel.getSelectedNode();
			_this.el.mask('正在更改数据库信息...', 'x-mask-loading');
			var hide = _this.el.unmask.createDelegate(_this.el);
			Ext.Ajax.request({
				method: 'POST', 
				url: _this.loader.dataUrl, 
				callback: function(options, success, response){
					hide();
					if (success) {
					}
					else {
						_this.showException(response);
					}
				},
				params: {action:"alter", object: n.id}
			});
		};
		
		function singleReverse(){
			var n = _this.selModel.getSelectedNode();
			_this.el.mask('正在读取数据库信息...', 'x-mask-loading');
			var hide = _this.el.unmask.createDelegate(_this.el);
			Ext.Ajax.request({
				method:'POST', 
				url: _this.loader.dataUrl, 
				callback: function(options, success, response){
					hide();
					if (success) {
						refresh(n);
					}
					else {
						alert(response.statusText);
					}
				},
				params: {action:"singleReverse",tableName:n.id}
			});
		};
		
		this.contextMenu = new Ext.menu.Menu({
		     id: 'organMenu',
		     items: [
		         {
		             id: 'append',
					 icon:"../sqlex/resources/icons/add_obj.gif",
					 text: '添加',
		             handler: appendNode
		         },
		         {
				     id: 'remove',
					 icon:"../sqlex/resources/icons/delete.gif",
		             text: '删除',
		             handler: removeNode
		         },
		         new Ext.menu.Item({
					 id: 'modify',
					 icon : '../sqlex/resources/icons/tasks_tsk.gif',
		             text: '设计',
		             handler: modifyNode
		         }),
		         new Ext.menu.Item({
				 	id: 'rename',
					icon : '../sqlex/resources/icons/prop_ps.gif',
		            text: '属性',
		            handler: setProperty
		         }),
		         '-',
		     	 {
		             id: 'reverse',
					 text: '获取表结构',
		             handler: singleReverse
		         },
		     	 {
		             id: 'alter',
					 text: '更新表结构',
		             handler: alter
		         },
		         {
				 	id: 'browse',
		            text: '浏览',
		            handler: browseTable
		         },
		         {
				 	id: 'sql_editor',
					icon : '../sqlex/resources/icons/table.gif',
		            text: 'SQL编辑器',
		            handler: function(){
					 	var node = _this.selModel.getSelectedNode();
		             	var query = node ? '?object='+node.id: '';
		             	openEditor('sql_editor', 'SQL编辑器', '../sqlex/sql_edit.jsp'+query); 
		            }
		         },
				 /*
		         '-',
		         {
				 	id: 'connection',
					icon : '../sqlex/resources/icons/example.gif',
		            text: '连接设置',
		            handler: function(){ Workbench.setConnection(); }
		         },
		         */
		         '-',
		         {
				 	id: 'refresh',
		            text: '刷新',
					icon:"../sqlex/resources/icons/refresh.gif",
		            handler: function() {
					 	var node = _this.selModel.getSelectedNode();
						if (!node.isLeaf())
							refresh(node?node:_this.getRootNode());
					}
		         }
		     ]
		});
		
		// 打开编辑器
		var openEditor = function(name, title, url) {
			var panel = Viewer.getCenterRegion().get(name);
			if (!!panel) {
				panel.setTitle(title);
				panel.tabTip = title;
				if (url != panel.body.dom.src) 
					panel.body.dom.src = url;
			} else {
				var panel = new Ext.Panel({
					id: name,
					title: title,
					tabTip: title,
					closable: true,
					tag: 'center',
					bodyCfg: {
						tag: 'iframe',
						frameBorder: 0,
						cls: 'x-panel-body',
						style: 'overflow:auto;border:none;',
						src: url
					},
					listeners : {
						close : function() {
							// 关闭时隐藏面板
							if(Viewer.getCenterRegion().items.length == 1)
								Ext.fly(Viewer.getCenterTabPanel().getEl().dom.firstChild.firstChild).setStyle('visibility', 'hidden');
						}
					}
				});
				Viewer.getCenterRegion().add(panel);
			}
			
			Ext.fly(Viewer.getCenterTabPanel().getEl().dom.firstChild.firstChild).setStyle('visibility', 'visible');
			Viewer.getCenterRegion().setActiveTab(panel);
			Viewer.getCenterRegion().doLayout();
		};
		
		var prepareCtx=function(node, e){
		    node.select();
		    this.contextMenu.get('remove').setDisabled(!node.attributes.allowDelete);
			this.contextMenu.get('append').setDisabled(!node.attributes.allowAppend);
			this.contextMenu.get('modify').setDisabled(!node.attributes.allowModify);
			this.contextMenu.get('rename').setVisible(node.attributes.allowSet);
			this.contextMenu.get('browse').setVisible(node.attributes.allowBrowse);
			this.contextMenu.get('reverse').setVisible(node.attributes.allowReverse);
			this.contextMenu.get('alter').setVisible(node.attributes.allowAlter);
			
			if (node.childNodes.length == 0)
				this.contextMenu.get('refresh').setDisabled(true);
			else
				this.contextMenu.get('refresh').setDisabled(false);	
		    this.contextMenu.showAt(e.getXY());
		};
	
		var syncronize = function(){
			_this.getSyncDialog().show();
		};

		toolbar.add(
		{
			cls:'x-btn-text-icon', text:'添加', id:'add', handler:appendNode, icon:"../sqlex/resources/icons/add_obj.gif", disabled:true
		},
		{
			cls:'x-btn-text-icon', text:'修改', id:'modi', handler:modifyNode, icon:"../sqlex/resources/icons/prop_ps.gif", disabled:true
		},
		{
			cls:'x-btn-text-icon', text:'删除', id:'del', handler: removeNode, icon:"../sqlex/resources/icons/delete.gif", disabled:true
		},
		'-',
		{
			cls:'x-btn-text-icon', text:'同步', handler:syncronize, icon:"../sqlex/resources/icons/synced.gif"
		});
		
		treeLoader.on('loadexception', function(loader, node, response){
			_this.showException(response);
		});
		
	    var root = new Ext.tree.AsyncTreeNode({
			text:'数据库',
			isLeaf: false,
			id: 'ROOT',
			icon: "../sqlex/resources/icons/schema.gif",
			allowSet:true
		});
		
		this.setRootNode(root);
	    this.on('expand', function(node){
	        if(node.id == "TABLE"){
				node.sort(function(node1, node2){ return node1.text==node2.text? 0 : (node1.text>=node2.text?1:-1); });
	        }
	    });
		this.on('contextmenu', prepareCtx, _this);
	},
	
	render: function(container, position) {
		Ext.ux.SQLPanel.superclass.render.apply(this, arguments);
		var _this = this;
		
		var ctx = this.contextMenu;
		this.el.addKeyListener(Ext.EventObject.DELETE, ctx.get('remove').handler);

		var selModel = this.getSelectionModel();
		selModel.on('selectionchange', function(){
			var btns = _this.getTopToolbar().items.map;
		    var node = selModel.getSelectedNode();
		    if (!node) {
		        btns.del.disable();
		        btns.modi.disable();
		        btns.add.disable();
		        return;
		    }
		    var a = node.attributes;
		    btns.add.setDisabled(!a.allowAppend);
		    btns.del.setDisabled(!a.allowDelete);
		    btns.modi.setDisabled(!a.allowModify);
		}, this);
	},
	
	createTable: function(node){
		var _this=this;
		var dialog=this.getTableDialog();
		dialog.form.baseParams={action:'appendChild', node:node.id};
		dialog.form.on('actioncomplete', function(){ 
			_this.appendChild(node, dialog.form.getValues()); dialog.hide();
		});
		dialog.setTitle('创建表');
		dialog.show();
	},
	
	setTableAttr: function(node){
		var dialog = this.getTableDialog();
		dialog.form.baseParams={action:'update', node:node.id};
		dialog.form.on('actioncomplete', function(form, action){
			if (action.type=='submit') {
				var values = dialog.form.getValues();
				node.id="table_"+values.name;
				node.setText(values.name+':'+values.remarks); 
				dialog.hide();
			}
		});
		dialog.setTitle('表属性 - '+node.text);
		dialog.show();
		dialog.form.load({ params:{action: 'loadTable', table: node.id.replace(/table_/, '')} });
	},
	
	getSyncDialog: function(){
		var _this = this;
		if (_this.syncDialog) return _this.syncDialog;
		
        var dialog = new Ext.Window({
        	title: '同步',
			autoCreate: true,
			closeAction: 'hide',
            width:320,
			resizable:false,
            shadow:true,
			modal:true,
            minWidth:320,
            minHeight:120,
            autoHeight: true,
            layout: "fit",
            items: {
            	xtype: 'form',
			    labelWidth: 120,
			    autoHeight: true,
			    border: false,
			    method:'POST',
				monitorValid:true,
			    url:'../SQLExplorer',
			    baseParams: { action: 'syncronize'},
			    bodyStyle: 'padding: 5px;',
			    defaults: {anchor: '100%'},
            	items: [
	            	{
	            		xtype: 'combo',
	            		fieldLabel: '同步方式',
	            		name: 'direction',
	            		hiddenName: 'direction',
	            		triggerAction: 'all',
	            		value: 'bidi',
	            		store: [['bidi','双向'],['db','更新数据库'],['meta','更新元数据']]
	            	},
				    new Ext.form.Checkbox({
				        fieldLabel: '同步表结构',
				        name: 'table',
				        checked: true
				    }),
				    new Ext.form.Checkbox({
				        fieldLabel: '同步视图',
				        name: 'view'
				    }),
				    new Ext.form.Checkbox({
				        fieldLabel: '同步存储过程',
				        name: 'procedure'
				    })
            	],
            	bbar: [
					'执行此操作可能改变数据库物理结构，请注意备份！'
				]		
            }
        });
        //dialog.body.addKeyListener(27, dialog.close, dialog);
		dialog.form = dialog.getComponent(0).form;
        var okButton=dialog.addButton('同步', function(){
        	if (dialog.form.isValid()) {
	        	dialog.form.submit({waitMsg:'正在同步...'});
        	}
        });
        dialog.addButton('取消', function(){ dialog.hide(); });
		dialog.form.on('actioncomplete', function(form, action){ dialog.hide(); });
		dialog.form.on('actionfailed', function(form, action){ _this.showException(action.response); });
		dialog.form.on('clientvalidation', function(form, valid){okButton.setDisabled(!valid);})
		_this.syncDialog = dialog;
		return dialog;
	},
	
	getTableDialog: function(){
        var dialog = new Ext.Window({
			autoCreate: true, 
            width:420, //height:300,
            autoHeight: true,
			resizable:false,
            shadow:true,
			modal:true,
            minWidth:320,
            minHeight:150,
            layout: "fit",
            items: {
            	xtype: 'form',
			    labelWidth: 75,
			    height: 350,
			    border: false,
			    method:'POST',
				monitorValid:true,
			    url:'../SQLExplorer',
			    bodyStyle: 'padding: 5px;',
			    defaults: {anchor: '-18'},
            	items: [
				    new Ext.form.TextField({
				        fieldLabel: '表名称',
				        name: 'name',
						regex: /[A-Za-z_]\w*/, 
						regexText:'表名称必须以字母或下划线开头',
				        allowBlank: false
				    }),
				    new Ext.form.TextField({
				        fieldLabel: '备注',
				        name: 'remarks',
				        allowBlank: false
				    }),
				    {
				    	xtype: 'fieldset',
				        title: '记录验证',
				        layout: 'form',
					    items: {
					    	xtype: 'textarea',
					        height: 200,
					    	anchor: '100%',
					        hideLabel: true,
					        name: 'validator',
					        allowBlank: true
					    },
					    bbar: [{
					    	text: '清除',
					    	handler: function(){
					    		var field = this.ownerCt.ownerCt.getComponent(0);
					    		field.setValue('');
					    	}
					    },
					    {
					    	text: '示例',
					    	handler: function(){
					    		var field = this.ownerCt.ownerCt.getComponent(0);
					    		field.setValue('function(rec){ if(!rec.acct) { alert("账号不能为空!"); return false; }; return true; }');
					    	}
					    }]
				    }
            	]
            }
        });
		dialog.form = dialog.getComponent(0).form;
        var okButton=dialog.addButton('确定', function(){if (dialog.form.isValid()) dialog.form.submit();});
        dialog.addButton('取消', dialog.close, dialog);
		dialog.form.on('actionfailed', function(form, action){ _this.showException(action.response); });
		dialog.form.on('clientvalidation', function(form, valid){okButton.setDisabled(!valid);})
		return dialog;
	},
	
	appendChild: function(parentNode, obj){
		if (!parentNode.loaded) return;
		if (parentNode.id=='TABLE')
		{
			var node=new Ext.tree.AsyncTreeNode({
				text:obj.name+':'+obj.remarks, id:'table_'+obj.name, icon:'../sqlex/resources/icons/table.gif', isLeaf: false,
				allowModify:true, allowDelete:true, allowSet:true,allowReverse:true, allowAlter:true
			});
			parentNode.appendChild(node);
		}
		else if (parentNode.id.indexOf('indices_')==0)
		{
			var node=new Ext.tree.AsyncTreeNode({
				text:obj.name, meta:'INDEX', name:obj.name, table:parentNode.id.replace(/[a-z]+_/, ''),
				icon:'../sqlex/resources/icons/index.gif', isLeaf: true,
				allowModify:true, allowDelete:true, allowSet:true,allowReverse:true
			});
			parentNode.appendChild(node);
		}
	},
	
	createIndex: function(node){
		var _this=this;
		var tableName = node.id.replace(/[a-z]+_/, "");
		var dialog=this.getIndexDialog(tableName);
		dialog.form.baseParams={action:'appendChild', node:node.id};
		dialog.form.on('actioncomplete', function(){ _this.appendChild(node, dialog.form.getValues()); dialog.hide();});
		dialog.setTitle('创建索引 - '+node.parentNode.text);
		dialog.show();
	},
	
	setIndexAttr: function(node){
		var _this=this;
		var dialog=this.getIndexDialog(node.attributes.table, node.attributes.name);
		dialog.form.baseParams={action:'update', node:'index_'+node.attributes.name};
		dialog.form.on('actioncomplete', function(){ 
			var values = dialog.form.getValues();
			node.attributes.name=values.name;
			node.attributes.type=values.type;
			node.setText(values.name); 
			dialog.hide();
		});
		dialog.setTitle('索引属性 - '+node.text);
		dialog.show();
		dialog.form.setValues(node.attributes);
	},

	getIndexDialog: function(tableName, indexName){
		var indexTypes = new Ext.data.SimpleStore({
		    fields: ['value', 'label'],
		    data : [['primary', '主索引'],['unique', '唯一索引'], ['common', '普通索引']]
		});
		var sortTypes = new Ext.data.SimpleStore({
		    fields: ['value', 'label'],
		    data : [['ASC', '升序'],['DESC', '降序']]
		});

	    function formatBoolean(value){
	        return '<input type="checkbox" '+(value?'checked="true"':'')+' onclick="this.checked=!this.checked;" style="width:14px;height:14px;"/>';
	    };
		var fm = Ext.form, Ed = Ext.grid.GridEditor;
		var cm = new Ext.grid.ColumnModel([{
	           header: "",
	           dataIndex: 'is_checked',
	           width: 30,
	           renderer: formatBoolean,
	           editor: new Ed(new fm.Checkbox())
	        },
			{
	           header: "列名",
	           dataIndex: 'column_name',
	           width: 100
	        },
			{
	           header: "描述",
	           dataIndex: 'remarks',
	           width: 90
	        },
			{
	           header: "排序",
	           dataIndex: 'sort',
	           width: 58,
	           editor: new Ed(new Ext.form.ComboBox({
	               typeAhead: true,
	               triggerAction: 'all',
				   store: sortTypes,
				   mode: 'local',
				   valueField: 'value',
				   displayField: 'label',
				   editable: false,
	               lazyRender: true
	            }))
	        }
		]);
	
	    var Column = Ext.data.Record.create([
           {name: 'is_checked', type: 'bool'},
           {name: 'column_name', type: 'string'},
           {name: 'remarks', type: 'string'},
           {name: 'sort', type: 'string'}
        ]);
	    var ds = new Ext.data.Store({
	        proxy: new Ext.data.HttpProxy({url: '../SQLExplorer?action=loadIndex&table='+tableName+(indexName?"&index="+indexName:"")}),
	        reader: new Ext.data.XmlReader({
	               record: 'column'
	           }, Column)
	    });

	    var grid = new Ext.grid.EditorGridPanel({
	    	region: 'center',
	        ds: ds,
	        cm: cm,
	        clicksToEdit: 1,
	        listeners: {
				beforeedit: function(e){
					switch (e.field)
					{
					case 'is_checked':
						break;
					case 'sort':
						e.cancel=!e.record.get('is_checked');
						break;
					default:
						e.cancel=true;
					}
				}
	        }
	    });
		
		var dialog = new Ext.Window({
			autoCreate: true, 
            width:320,
            height:320,
			resizable:false,
            shadow:true,
			modal:true,
            minWidth:320,
            minHeight:120,
            proxyDrag: true,
            layout: 'border',
			items: [{
				region: 'north',
				xtype: 'form',
			    labelWidth: 75,
			    bodyStyle: 'padding: 5px;',
			    autoHeight: true,
			    method:'POST',
				monitorValid:true,
			    url:'../SQLExplorer',
			    items: [
				    new Ext.form.TextField({
				        fieldLabel: '索引名称',
				        name: 'name',
				        width: 200,
						regex: /[A-Za-z_]\w*/, 
						regexText:'索引名称必须以字母开头',
				        allowBlank:false
				    }),
					new Ext.form.ComboBox({
				        fieldLabel: '索引类型',
				        name: 'type',
						hiddenName: 'type', 
						store: indexTypes, mode:'local', 
						triggerAction: 'all', 
						editable: false, 
						forceSelection: true,
						valueField:'value',displayField:'label',
						value: 'common',
						width:200
					}),
				    new Ext.form.TextField({
				        name: 'table',
				        width: 200,
						value: tableName,
						hidden: true
				    }),
				    new Ext.form.TextField({
				        name: 'data',
				        width: 200,
						hidden: true
				    })
			    ]
			},
			grid
			]
        });
		dialog.form = dialog.getComponent(0).form;
		dialog.grid = grid;
		ds.load();
		
        var okButton=dialog.addButton('确定', function(){
			if (dialog.form.isValid()) 
			{
				var records= new Array();
				ds.each(function(record){ if (record.get('is_checked')) records.push(record.data); });
				if (records.length==0) { alert('请至少选择一个字段作为索引字段'); return; }
				dialog.form.setValues({data:Ext.encode(records)});
				dialog.form.submit();
			}
		});
        dialog.addButton('取消', dialog.close, dialog);
		dialog.form.on('actionfailed', function(form, action){ _this.showException(action.response); });
		dialog.form.on('clientvalidation', function(form, valid){okButton.setDisabled(!valid);})
		return dialog;
	},
	
    destroy: function(){
		Ext.destroy(this.contextMenu);
		Ext.ux.SQLPanel.superclass.destroy.call(this); 
		this.fireEvent("destroy", this);
    },
	
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
});