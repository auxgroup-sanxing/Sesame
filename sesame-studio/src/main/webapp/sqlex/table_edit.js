Ext.BLANK_IMAGE_URL='../images/s.gif';
/*
 * 需要在页面上配置一个table对象。示例：
 */
var Browser = function(){

var logicOps = [['AND','与'],['OR','或'],['NOT','非']];
var comparisonOps = [['>','>'],['<','<'],['=','='],['!=','!='],['LIKE','LIKE']];
	
var viewport;

return {
	init : function(){
		var _this = this;
		//Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		Ext.QuickTips.init();
		
		var deletedRows = [];
		function formatBoolean(value){
		    return value ? '是' : '否';  
		};
		function formatDate(value){
				if(value){
					if(value.length == 8)
						return value;
					else
				    	return value.dateFormat('Ymd');
				}else{
					return '';
				}
		};
		// shorthand alias
		var fm = Ext.form, Ed = Ext.grid.GridEditor;
		
		var fields = [];
		Ext.each(table.columns, function(column){
			var ClassRef, config={};
			
			fields.push({name: column.dataIndex, type: column.type=='date1'?'date':column.type, dateFormat: column.dateFormat});
			column.editable = false; //(!table.readonly && table.disabledCols.indexOf(column.dataIndex)==-1);
			switch(column.type)
			{
			case "date":
			case "date1":
				config.format='Ymd';
				column.renderer=formatDate;
				break;
			case "bool":
				column.renderer=formatBoolean;
				column.align='center';
				break;
			}
			if (Ext.type(column.restriction)=="array")
			{
				var valueText={};
				Ext.each(column.restriction, function(elem){valueText[elem.value]=elem.text;});
				column.renderer=function(value){ if (value==null) return value; var text=valueText[value.toString()]; return text?text:value;};
				column.align='left';
			}
		});
		var cm = new Ext.grid.ColumnModel(table.columns);
		// by default columns are sortable
		cm.defaultSortable = true;
		
		var Item = Ext.data.Record.create(fields);
		// create the Data Store
		var ds = new Ext.data.Store({
		    proxy: new Ext.data.HttpProxy({url: '../TableOperate', method:'GET'}),
			baseParams: {action:"loadData", table:table.name,orderby:table.orderby ,condition:table.condition},
		    reader: new Ext.data.JsonReader(	{ root: 'rows', totalProperty: 'totalCount', id: table.key }, Item),
		    listeners: {
				loadexception: function(proxy, obj, response, e){ 
					if (e) alert('读取数据失败\n'+e.message); else top.Application.showException(response); 
				}
		    }
		});
		
		var frame = document.getElementById('download');
		if (!frame) 
		{
			frame = document.createElement('iframe');
			frame.setAttribute('id', 'download');
			frame.setAttribute('style', 'display:none;');
			frame.setAttribute('url', '../TableOperate?action=loadData&export=true&table='+table.name);
			document.body.appendChild(frame);
		}
		var insertRec = function(){
			var rec = new Item();
			Ext.each(table.columns, function(column){
				if (column.def)rec.set(column.dataIndex, column.def);
			});
			var callback=function(rec){
		        ds.insert(0, rec);
			}
			_this.getEditDialog({title:'添加', action: 'insert', record: rec, callback:callback}).show();
		};
		var modifyRec = function(){
	    	var rec = grid.getSelectionModel().getSelected();
			if (rec) {
				var callback = function(rec){
					rec.commit(true);
				};
				_this.getEditDialog({title:'修改', action: 'update', record: rec, callback:callback}).show();
			}
	    };
		var removeRec = function(){
			var r = grid.getSelectionModel().getSelected();
			if (!r || !window.confirm("确实要删除选中的记录吗？")) return;
			if(table.accredit_grade&&table.accredit_grade.length == 1)
				_this.getAccreditDialog(removeDetail,"delete");
			else
				removeDetail();
		};
		var removeDetail = function(accredit_userid){
			var r = grid.getSelectionModel().getSelected();
			for (var i=0,len=table.columns.length; i<len; i++) {
						var col=table.columns[i];
						if (!col.autoInc && r.data[col.dataIndex]) {
							
							if(col.type == 'date1'){
								if(r.data[col.dataIndex].length != 8)
									r.data[col.dataIndex] = r.data[col.dataIndex].dateFormat("Ymd");
							}
						}
				}
					
				var records = [Ext.apply({"~":"delete"}, r.data)];
				//r.reject();
				Ext.getBody().mask('正在删除...', 'x-mask-loading');
				var hide = Ext.getBody().unmask.createDelegate(Ext.getBody());
				Ext.Ajax.request({
					method:'POST', 
					url:'../TableOperate', 
					params: {action:"saveData", table:table.name, key:table.key, data:Ext.encode(records),accredit_grade:table.accredit_grade,accredit_userid:accredit_userid},
					callback: function(options, success, response){
						hide(); 
						if (success) {
							ds.remove(r);
						}
						else {
							app.showException(response);
						}
					}
				});
		}
		// create the editor grid
		var grid = new Ext.grid.GridPanel({
		    ds: ds,
		    cm: cm,
			selModel: new Ext.grid.RowSelectionModel(),
			border: false,
		    columnLines: true,
			loadMask: true,
			stateful: false,
			listeners: {
				contextmenu: function(e){e.stopEvent();},
				dblclick: modifyRec
			},
			tbar: [{
			    text: '添加',
				tooltip: '添加记录',
				icon: '../images/icons/add.gif',
				cls: 'x-btn-text-icon',
				disabled: table.readonly,
			    handler : insertRec
			},
			{
			    text: '删除',
				icon: '../images/icons/delete.gif',
				cls: 'x-btn-text-icon',
				disabled: table.readonly,
			    handler : removeRec
			},
			{
			    text: '编辑',
				icon: '../images/icons/edit.gif',
				cls: 'x-btn-text-icon',
				disabled: table.readonly,
			    handler : modifyRec
			},
			'-',
			{
				text: '查询',
				handler: function(){
					_this.getFilterDialog(ds).show();
				}
			},
			'-',
			{
				text: '显示全部',
			    tooltip: '清除查询条件,显示全部数据',
			    handler : function(){
			    	_this.refresh(ds);
			    }
			},'-',
			{
				id: 'saveas',
				text: '导出',
			    tooltip: '保存到Excel文档',
			    hidden: true,
				icon: '../images/icons/saveas_edit.gif',
				cls: 'x-btn-text-icon',
			    handler : function(){
					frame.src = frame.getAttribute('url');
			    }
			}],
			bbar: new Ext.PagingToolbar({
				store: ds,
			    pageSize: 25,
			    beforePageText: "页",
				afterPageText: " / {0}",
			    displayInfo: true,
			    displayMsg: '正在显示记录 {0} - {1} 总记录数: {2}',
			    emptyMsg: "空记录"
			})
		});
		viewport = new Ext.Viewport({
			stateId: 'table_edit',
		    layout: 'fit',
		    items: [grid]
		});
		
		grid.el.addKeyListener(Ext.EventObject.DELETE, removeRec);
		grid.el.addKeyListener(Ext.EventObject.INSERT, insertRec);
		// trigger the data store load
		ds.load({params:{start:0, limit:25}, callback:function(records, options, success){}});
	},
	refresh:function(ds){
	
		ds.removeAll();
		ds.baseParams.condition = "";
		ds.load({params:{start:0, limit:25}, callback:function(records, options, success){}});
	},
	getFilterDialog : function(ds){
		
		var _this = this;
		
		if (!this.filterDlg) {
			var titleText = '查询条件';
			
			var opCombo = new Ext.form.ComboBox({
				typeAhead:true,
				editable: false,
				disabled: true,
				width: 60,
				triggerAction:'all',
				store: new Ext.data.Store({
					data: logicOps,
					reader: new Ext.data.ArrayReader({}, [
						{name:'value'}, {name:'text'}
					])
				}),
				mode:'local',
				displayField:'text',
				valueField:'value',
				listClass:'x-combo-list-small'
			});

			var dlg = new Ext.Window({
		        autoCreate : true,
		        title: titleText,
		        resizable:true, constrain:true, constrainHeader:true,
				minimizable: false, maximizable: false,
		        stateful: false, modal: true,
		        buttonAlign: "right",	defaultButton: 0,
		        width: 550, height: 380,
		        minHeight: 300, minWidth: 300,
		        footer: true,
		        closable: true, closeAction: 'hide',
				plain: true,
				layout: 'border',
				items: [{
					region: 'center',
					xtype: 'treepanel',
					lines: false,
					autoScroll: true,
					loader: new Ext.tree.TreeLoader({}),
					rootVisible: false,
					root: _this.createLogicExpr('OR'),
					tbar: new Ext.Toolbar(),
					contextMenu: new Ext.menu.Menu({
						items:[{
							id:'add_logic',
							text:'逻辑表达式'
						},{
							id:'add_comparison',
							text:'比较表达式'
						},{
							id:'add_operand',
							text:'操作数'
						}],
						listeners:{
							itemclick:function(item){
								var node = item.parentMenu.contextNode;
								if (!node) node = dlg.getComponent(0).getRootNode();
								
								switch(item.id){
									case 'add_logic':
										var a = node.attributes;
										if (typeof(a.op)=='string' && a.op.toUpperCase()=='NOT' && node.childNodes.length>0) {
											alert('非运算不能添加多于一个操作数');
											return;
										}
										var logicOr = _this.createLogicExpr('OR', true);
										var equal = _this.createComparison('=', _this.createOperand('field1', 'string'), _this.createOperand("''"));
										var like = _this.createComparison('LIKE', _this.createOperand('field2', 'string'), _this.createOperand("'abc%'"));
										logicOr.appendChild(equal);
										logicOr.appendChild(like);
										node.appendChild(logicOr);
										break;
									case 'add_operand':
										if (node.childNodes.length>1) {
											alert('不能添加多于两个操作数');
											return;
										}
										node.appendChild(_this.createOperand("undefined"));
										break;
									case 'add_comparison':
										var a = node.attributes;
										if (typeof(a.op)=='string' && a.op.toUpperCase()=='NOT' && node.childNodes.length>0) {
											alert('非运算不能添加多于一个操作数');
											return;
										}
										var equal = _this.createComparison("=", _this.createOperand("field", "string"), _this.createOperand("undefined"));
										node.appendChild(equal);
								}
							}
						}
					}),
					selModel: new Ext.tree.DefaultSelectionModel({
						listeners: {
							selectionchange: function(sm, node){
								var tbar = dlg.getComponent(0).getTopToolbar();
								if (node == null) {
									node = dlg.getComponent(0).getRootNode();
									tbar.get('remove_node').disable();
								}
								else {
									tbar.get('remove_node').enable();
								}
								var c = node.getOwnerTree().contextMenu;
								c.contextNode = node;
								if (node && node.attributes.op) {
									opCombo.enable();
									opCombo.store.loadData(node.attributes.uiProvider==Ext.ux.ComparisonUI ? comparisonOps : logicOps);
									opCombo.setValue(node.attributes.op);
								}
								else {
									opCombo.setValue('');
									opCombo.disable();
								}
							}
						}
					}),
					listeners: {
						contextmenu: function(node, e){
							e.preventDefault();
							node.select();
							var a = node.attributes;
							var c = node.getOwnerTree().contextMenu;
							c.items.get('add_logic').setDisabled(a.uiProvider!=Ext.ux.LogicUI);
							c.items.get('add_comparison').setDisabled(a.uiProvider!=Ext.ux.LogicUI);
							c.items.get('add_operand').setDisabled(a.uiProvider!=Ext.ux.ComparisonUI);
							c.contextNode = node;
							c.showAt(e.getXY());
						}
					}
				},{
					region: 'east',
					xtype: 'treepanel',
					autoScroll: true,
					split: true,
					width: 150,
					loader: new Ext.tree.TreeLoader({ url: '../TableOperate'}),
					rootVisible: false,
					root: new Ext.tree.TreeNode({
						text: '字段',
						id: 'FIELDS'
					}),
					listeners: {
						dblclick: function(node, e){
							var a = node.attributes;
							var v = a.type=='string' ? "''" : "NULL";
							var root = dlg.getComponent(0).getRootNode();
							root.appendChild(_this.createComparison('=', _this.createOperand(a.field, a.type), _this.createOperand(v)));
						}
					}
				}]
		    });
			
		    var exprTree = dlg.getComponent(0);
		    var fieldsTree = dlg.getComponent(1);

		    var toolbar = exprTree.getTopToolbar();
		    toolbar.add(
		    	{
			    	text: '添加',
			    	menu: exprTree.contextMenu,
			    	handler: function(){
						var c = exprTree.contextMenu;
						var node = c.contextNode || exprTree.getRootNode();
						var a = node.attributes;
						c.items.get('add_logic').setDisabled(a.uiProvider!=Ext.ux.LogicUI);
						c.items.get('add_comparison').setDisabled(a.uiProvider!=Ext.ux.LogicUI);
						c.items.get('add_operand').setDisabled(a.uiProvider!=Ext.ux.ComparisonUI);
			    	}
			    },{
					id:'remove_node',
					text: '删除',
					handler: function(){
						var node = exprTree.getSelectionModel().getSelectedNode();
						if(node && node.parentNode){
							if(node.isLeaf() && node.parentNode.childNodes.length == 1)
								node.parentNode.remove();
							else
								node.remove();
						}
					}
				},
				'-',
				'操作符:',
				opCombo
			);
			opCombo.on("select", function(combo,record,index){
				var node = exprTree.contextMenu.contextNode;
				if (node) {
					node.attributes.op = combo.getValue();
					node.setText(combo.getValue());
				}
			});
			
		    var fields = [];
		    Ext.each(table.columns, function(column) {
		    	fields.push([column.dataIndex, column.header,column.type]);
		    	fieldsTree.getRootNode().appendChild(new Ext.tree.TreeNode({
		    		text: column.header,
		    		field: column.dataIndex,
		    		type: column.type,
		    		leaf: true
		    	}));
		    });
			var comboBox = new Ext.form.ComboBox({
			  typeAhead:true,
			  editable: false,
			  triggerAction:'all',
			  selectOnFocus: true,
			  store: new Ext.data.Store({
				data: fields,
				reader: new Ext.data.ArrayReader({}, [
					{name:'value'}, {name:'text'},{name:'type'}
				])
			  }),
			  mode:'local',
			  displayField:'text',
			  valueField:'value',
			  listeners: {
			    specialkey: function(field, e){
			        if (e.getKey() == e.ENTER) {
			            field.beforeBlur();
			        }
			    },
			    select:function(combo,record,index){
			    	combo.type = record.get("type");
				}
			  }
			});
			var treeEditor = new Ext.tree.TreeEditor(
				exprTree,
				{
					allowBlank:false,
					selectOnFocus:true
				},{
					editDelay: 10,
					cancelOnEsc: true,
					completeOnEnter: true,
					ignoreNoChange: true,
					field: comboBox,
					listeners: {
						beforestartedit : function(treeEditor, boundEl, value) {
							var node = treeEditor.editNode;
							var oldValue = node.text;
							if (!node.isLeaf()) {
								return false;
							}
							else {
								var c = treeEditor.field;
								if (node.attributes.type) {
									c.regex = null;
									c.setEditable(false);
									c.store.loadData(fields);
								} 
								else {
									var n = node.previousSibling || node.nextSibling;
									if (n) {
										if (n.attributes.type=='string') {
											c.regex = /^'.*'$/;  c.regexText = '非法的字符串';
										}
										else {
											c.regex = null;
										}
									}
									else {
										c.regex = null;
									}
									c.setEditable(true);
									c.store.removeAll();
								}
							}

							if (!node.parentNode) {
								return false;
							}
							return true;
						},
						complete : function(editor, value, startValue){
							var node = editor.editNode;
							var attri = node.attributes;
							if(attri.type){
								attri.type = editor.field.type;
							}
							//alert("type is:"+node.attributes.type);
						}
					}
			});
			
		    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:false}, function(){ 
		    	
				var  root = dlg.getComponent(0).getRootNode();
		    	var sql = _this.createSQL(root);
		    	ds.baseParams.condition = sql;
		    	ds.removeAll();
		    	ds.load({params:{start:0, limit:25}, callback:function(records, options, success){}});
		    	dlg.hide();
		    	var	root= _this.createLogicExpr('OR');
		    	dlg.getComponent(0).setRootNode(root);
		    	
		    	return;
		    	
				/*if (!dlg.node) return false;
				var a = dlg.node.attributes;
				var condPanel = dlg.getComponent(0);
				var form = condPanel.getForm();
				if (!form.isValid()) return false;

				Ext.apply(a, form.getValues());
				dlg.node.setText(a.toString());
				flowTree.getTopToolbar().items.get('save').enable();
				dlg.hide();*/
			});
		    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){ 
		    	dlg.hide(); 
		    	var	root= _this.createLogicExpr('OR');
		    	dlg.getComponent(0).setRootNode(root);
		    });
		    
			dlg.render(Ext.getBody());
		    this.filterDlg = dlg;
		}
		else {
			
		}
//		this.filterDlg.node = node;
//		var a = node.attributes;
//		var formPanel = this.filterDlg.getComponent(0);
		
        return this.filterDlg;
	},
	getAccreditDialog:function(_function,operation){
		var status = "false";
		function isFormValid() {
			return (form_userid.isValid() && form_password.isValid());
		}
		
		
		var form_userid = new Ext.form.TextField({
			id : 'form_userid',
			fieldLabel : '用户标识',
			//maxLength : 8,
			allowBlank : false,
			anchor : '95%'
		});
		var form_password = new Ext.form.TextField({
			id : 'form_password',
			fieldLabel : '密码',
			//maxLength : 5,
			//allowBlank : false,
			inputType:"password",
			anchor : '95%'
		});
		var createForm = new Ext.FormPanel({
			//labelAlign : 'left',
			//height : 80,
			//frame:true,
			bodyStyle : 'padding:5px 5px 0',
			items : [{
				layout : 'column',
				border : false,
				// heigth:200,
				items : [{
					labelAlign : 'right',
					columnWidth : 0.5,
					layout : 'form',
					border : false,
					heigth : 200,
					items : [form_userid]
				}, {
					labelAlign : 'right',
					columnWidth : 0.5,
					layout : 'form',
					heigth : 200,
					border : false,
					items : [form_password]
				}]
			}]
		});
		var createWindow = new Ext.Window({
			id : 'setProperties_window',
			title : '授权',
			closable : true,
			width:500,
            height:200,
			plain : true,
			layout : 'fit',
			
			items : [createForm],
			buttons : [{
				text : '授权',
				handler : create
			}, {
				text : '取消',
				handler : function() {
					createWindow.close();
					// grid_store.removeAll();
				}
			}]
		});
		createWindow.show();
		function create() {
			if (isFormValid()) {
				Ext.Ajax.request({
					method: 'POST', 
					url: "../LoginAction", 
					params : {
						operation : operation,
						_accredit_grade:table.accredit_grade,
						userid : form_userid.getValue(),
						passwd : form_password.getValue()? hex_md5(form_password.getValue()) : ""
					},
					success : function(response) {
						if(response.responseText == "true"){
							status = "true";
							createWindow.close();
							_function(form_userid.getValue());
						}else{
							Ext.MessageBox.alert("授权失败");
							createForm.getForm().reset();
							status = "false";
						}
							
					},
					failure : function(response) {
						top.Application.showException(response);
					}
				});
			} else {
				Ext.MessageBox.alert('Your Form is not valid');
			}
		}
		return status;
	},
	getEditDialog: function(options){
		var _this = this;
		
		if (!this.editDlg) {
			var items = [{
				layout: 'form',
				columnWidth: 0.5,
				style: 'margin:5px;',
				border: false,
				defaults: {anchor:"100%"},
				items: []
			},{
				layout: 'form',
				columnWidth: 0.49,
				style: 'margin:5px;',
				border: false,
				defaults: {anchor:"100%"},
				items: []
			}];
			var i=0, n=items.length;  //Math.floor(table.columns.length/2);
			var col;
			Ext.each(table.columns, function(column){
				if (column.hidden) return
				col = items[i%n];
				var ClassRef; 
				var config={fieldLabel:column.header, name:column.dataIndex, tabIndex: i+1, selectOnFocus:true};
				
				switch(column.type)
				{
				case "string":
					config.autoCreate={tag: "input", type: 'text', maxLength: column.size, autocomplete: "off"};
					config.xtype='textfield'; 
					break;
				case "int":
					config.allowDecimals=false;
					config.autoCreate={tag: "input", type: 'text', maxLength: 10, autocomplete: "off"};
				case "decimal":
					config.xtype='numberfield';
					if (Ext.type(column.restriction)=="object"){
						if (column.restriction.minValue) config.minValue=column.restriction.minValue;
						if (column.restriction.maxValue) config.maxValue=column.restriction.maxValue;
					}
					break;
				case "date":
				case "date1":
					config.format="Ymd";
					config.xtype='datefield'; 
					break;
				case "boolean":
					config.xtype='checkbox';  
					break;
				default:
					config.xtype='textfield';
				}
				config.readOnly = column.autoInc;
				config.allowBlank = config.readOnly || column.allowBlank;
				config.emptyText = config.readOnly ? "自动生成" : null;
				if (column.hidden) {
					config.xtype = 'hidden';
					config.disabled = true;
				}
				else if (Ext.type(column.restriction)=="array") {
					var itemStore = new Ext.data.Store({
						reader: new Ext.data.JsonReader(
							{ root: "rows" },
							Ext.data.Record.create([{name:'value'}, {name:'text'}])
						),
						data: { "rows":column.restriction}
					});
					delete config.maxLength;
					Ext.apply(config, {
						xtype : 'combo',
						hiddenName: column.dataIndex,
						allowBlank: column.allowBlank,
						typeAhead: true,
						triggerAction: 'all',
						store: itemStore,
						mode: 'local',
						valueField: 'value',
						displayField: 'text',
						editable: false,
						lazyRender:true
			        });
				}
				col.items.push(config);
				i++;
			});
			
			
			
			
		    var dlg = new Ext.Window({
				title: '新建操作',
				autoScroll:true,
		        autoCreate: true,   resizable:true,  
		        constrain:true, constrainHeader:true,
		        minimizable:false,  maximizable:false,
		        stateful:false,
		        modal:true,
		        buttonAlign: "right",
				defaultButton: 0,
				y: 0,
				width: 640,
				minWidth: 400,
				maxHeight: viewport.getHeight(),
				autoHeight: true,
				footer: true,
				closable: true,
				closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: {
					xtype: 'form',
					layout: 'column',
					autoScroll: true,
					border: false,
					stateful: false,
					items: items
				},
				keys: [{
			        key: '\r\n',
			        fn: function(){ dlg.ok.handler(); }
			    }]
		    });
			
		    dlg.ok = dlg.addButton('确定', function(){
				var form = dlg.getComponent(0).getForm();
				if (!form.isValid()) return;
				if (table.validator && !table.validator(form.getValues())) return;
				
				if(table.accredit_grade&&table.accredit_grade.length == 1)
					_this.getAccreditDialog(add_editGrid,options.action);
				else
					add_editGrid();
					
		    });
		    dlg.cancel = dlg.addButton('取消', function(){ dlg.hide(); });
		    
			dlg.render(Ext.getBody());
		    this.editDlg = dlg;
		}
		var add_editGrid= function(accredit_userid){
			var form = dlg.getComponent(0).getForm();
			var records = [];
				var record = dlg.options.record;
				form.updateRecord(record);
				
				var values = {};
				var modified = Ext.encode(record.modified);
				for (var i=0,len=table.columns.length; i<len; i++) {
							var col=table.columns[i];
							if (!col.autoInc && record.get(col.dataIndex)) {
								if(col.type == 'date1'){
									values[col.dataIndex] = record.get(col.dataIndex).dateFormat("Ymd");
									record.set(col.dataIndex,values[col.dataIndex]);
								}else{
									values[col.dataIndex] = record.get(col.dataIndex);
								}
							}
							if(dlg.options.action=='update'){
								var dataIndex = col.dataIndex;
								if (!col.autoInc && record.modified[dataIndex]) {
									if(col.type == 'date1'){
										if(record.modified[dataIndex].length != 8)
											record.modified[dataIndex] = record.modified[dataIndex].dateFormat("Ymd");
									}
								}
							}
					}
					if (dlg.options.action=='update') {
						var count = 0;
						for (var attr in record.modified) { count++; }
						if (count > 0) {
							records.push(Ext.apply({"~":"update"}, record.data, {"@":record.modified}));
						}
					}
					else {
						records.push(Ext.apply({"~":"insert"}, values));
				}
				Ext.Ajax.request({
					method:'POST', 
					url:'../TableOperate', 
					params: {action:"saveData", table:table.name, key:table.key, data:Ext.encode(records),accredit_grade:table.accredit_grade,accredit_userid:accredit_userid},
					callback: function(options, success, response){
						if (success) {
							var result = Ext.decode(response.responseText);
							if (result.generated && result.generated[0]) {
								var g = result.generated[0]
									for (var i=0,len=table.columns.length; i<len; i++) {
										var col=table.columns[i];
										if (col.autoInc) {
											record.set(col.dataIndex, g);
										}
									}
							}
							record.commit();
							if (dlg.options.callback) dlg.options.callback(record);
							dlg.hide();
						}
						else	{
							app.showException(response);
						}
					}
				});
		};
		this.editDlg.options = options;
		var formPanel = this.editDlg.getComponent(0);
		var form = formPanel.getForm();
		form.reset();
		form.loadRecord(options.record);
		if (typeof(table.disabled)=='string') {
			var cols = table.disabled.split(',');
			for (var i=0; i<cols.length; i++) {
				var f = form.findField(cols[i]);
				if (f) f.setDisabled(options.action=='update');
			}
		}
		if(typeof(table.key) == 'string'){
				var cols = table.key.split(',');
				
				for (var i=0; i<cols.length; i++) {
					var f = form.findField(cols[i]);
					if (f) f.setDisabled(options.action=='update');
				}
				
			}
		this.editDlg.setTitle(options.title || '新建记录');
		
        return this.editDlg;
	},
	
	createComparison: function(op, operand1, operand2){
		var node = new Ext.tree.TreeNode({
			text: op,
			op: op,
			uiProvider: Ext.ux.ComparisonUI,
			expanded: true
		});
		if (operand1) node.appendChild(operand1);
		if (operand2) node.appendChild(operand2);
		return node;
	},

	createLogicExpr: function(op, bracketed){
		return new Ext.tree.TreeNode({
			text: op,
			op: op,
			bracketed: bracketed,
			uiProvider: Ext.ux.LogicUI,
			expanded: true
		});
	},

	createOperand: function(operand, type){
		return new Ext.tree.TreeNode({
			text: operand,
			type: type,
			leaf: true,
			uiProvider: Ext.ux.OperandUI
		});
	},
	
	createSQL: function (node){
//		var sql = "";
//		
//		
//		root.eachChild(iteratorChildren);
//		
//		if(sql.length > 0){
//			sql = " select * from "+table.name+" where " + sql;
//		}else{
//			sql = " select * from "+table.name+" ";
//		}
//		return sql;
		
		function generateExpr(node){
			
			if(node.isLeaf()) {
				return node.text;
			}
			else {
				var result = "";
				node.eachChild(function(child){
					result += " "+node.attributes.op+" "+generateExpr(child);
				});
				var r = new RegExp('^\\ '+node.attributes.op+'\\ ');
				result = result.replace(r, '');
				if(node.attributes.uiProvider == Ext.ux.LogicUI && node.parentNode){
					result = "("+result+")";
				}
				return result;
			}
		}
		return generateExpr(node);
	}
	
}

}();

Ext.onReady(Browser.init, Browser, true);