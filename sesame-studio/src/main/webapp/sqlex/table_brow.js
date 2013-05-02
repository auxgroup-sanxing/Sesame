Ext.BLANK_IMAGE_URL='../images/s.gif';

var Browser = function(){
    return {
		init : function(){
			Ext.QuickTips.init();
			
			var deletedRows = [];
			function formatBoolean(value){
			    return value ? '是' : '否';  
			};
			function formatDate(value){
			    return value ? value.dateFormat('Y-m-d') : '';
			};
			// shorthand alias
			var fm = Ext.form, Ed = Ext.grid.GridEditor;
			
			var fields = [];
			Ext.each(table.columns, function(column){
				var ClassRef, config={};
				
				fields.push({name: column.dataIndex, type: column.type, dateFormat: column.dateFormat});
				switch(column.type)
				{
				case "string":
					ClassRef=Ext.form.TextField; 
					break;
				case "int":
					config.allowDecimals=false;
				case "decimal":
					ClassRef=Ext.form.NumberField;
					column.align='right';
					if (Ext.type(column.restriction)=="object"){
						if (column.restriction.minValue) config.minValue=column.restriction.minValue;
						if (column.restriction.maxValue) config.maxValue=column.restriction.maxValue;
					}
					break;
				case "date":
					ClassRef=Ext.form.DateField;
					config.format=column.dateFormat;
					column.renderer=formatDate;
					break;
				case "bool":
					ClassRef=Ext.form.Checkbox;  
					column.renderer=formatBoolean;
					column.align='center';
					break;
				default:
					ClassRef=Ext.form.TextField;
				}
				config.allowBlank = column.allowBlank;
				if (Ext.type(column.restriction)=="array")
				{
					var itemStore = new Ext.data.Store({
						reader: new Ext.data.JsonReader(
							{ root: "rows" },
							Ext.data.Record.create([{name:'value'}, {name:'text'}])
						),
						data: { "rows":column.restriction}
					});
					column.editor = new Ed(new Ext.form.ComboBox({
					   typeAhead: true,
					   triggerAction: 'all',
					   store: itemStore,
					   mode: 'local',
					   valueField: 'value',
					   displayField: 'text',
					   editable: false,
					   lazyRender:true
			        }));
					var valueText={};
					Ext.each(column.restriction, function(elem){valueText[elem.value]=elem.text;});
					column.renderer=function(value){ if (value==null) return value; var text=valueText[value.toString()]; return text?text:value;};
					column.align='left';
				}
				else
				{
					var field = new ClassRef(config);
					//if (field instanceof Ext.form.DateField) alert(field.format);
					column.editor = new Ed(field)
				}
			});
			var cm = new Ext.grid.ColumnModel(table.columns);
			// by default columns are sortable
			cm.defaultSortable = true;
			
			var Item = Ext.data.Record.create(fields);
			
			// create the Data Store
			var ds = new Ext.data.Store({
			    proxy: new Ext.data.HttpProxy({url: '../TableBrowser'}),
				baseParams: {action:"loadData", table: table.name},
			    reader: new Ext.data.JsonReader({
			           root: 'rows'
			       }, Item)
			});
			
			// create the editor grid
			var grid = new Ext.grid.EditorGridPanel({
				region: 'center',
			    ds: ds,
			    cm: cm,
				selModel: new Ext.grid.CellSelectionModel(),
			    enableColLock:false,
				loadMask: true
			});

			var viewport = Ext.Viewport({
				layout: 'border',
			    items : grid
			});
			
			var insertRec = function(){
		        var data={};
				Ext.each(fields, function(field){ data[field.name]=null; });
				var p = new Item(data);
		        grid.stopEditing();
		        ds.insert(0, p);
		        grid.startEditing(0, 0);
			};
			var removeRec = function(){
		        grid.stopEditing();
				var cell = grid.getSelectionModel().getSelectedCell();
				if (!cell || !window.confirm("确实要删除选中的记录吗？")) return;
				var record = ds.getAt(cell[0]);
				record.reject(); 
				ds.remove(record);
			};
			gridPanel.el.addKeyListener(Ext.EventObject.DELETE, removeRec);
			gridPanel.el.addKeyListener(Ext.EventObject.INSERT, insertRec);
			
			var gridHead = grid.getView().getHeaderPanel(true);
			var tb = new Ext.Toolbar(gridHead, [
			{
			    text: '添加',
				tooltip: '添加记录',
				icon: '../images/icons/add.gif',
				cls: 'x-btn-text-icon',
			    handler : insertRec
			},
			{
			    text: '删除',
				icon: '../images/icons/delete.gif',
				cls: 'x-btn-text-icon',
			    handler : removeRec
			},
			{
			    text: '编辑',
				icon: '../images/icons/edit.gif',
				cls: 'x-btn-text-icon',
			    handler : function(){
					var cell = grid.getSelectionModel().getSelectedCell();
			        if (cell) grid.startEditing(cell[0], cell[1]);
			    }
			},
			'-',
			{
				id: 'reload',
			    text: '刷新',
				icon: '../images/icons/refresh.gif',
				cls: 'x-btn-text-icon',
			    handler : function(){
					ds.reload();
					tb.items.get('save').disable(); 
				}
			},
			{
				id: 'save',
			    text: '保存',
				icon: '../images/icons/save_edit.gif',
				cls: 'x-btn-text-icon',
				disabled: true,
			    handler : function(){
					var records= new Array();
					var validate= function(record){
						var i=-1;
						Ext.each(table.columns, function(column){
							i++;
							if (column.allowBlank==false && record.get(column.dataIndex)==null)
							{
								grid.selModel.select(ds.indexOf(record), i);
								throw  {message: column.header+" 不能为空"}; 
							}
						});
					}
					try
					{
						ds.each(function(record){ 
							if(record.newRecord) 
							{
								validate(record);
								records.push(Ext.apply({"~":"insert"}, record.data));
							}
							else if (record.dirty)
							{
								validate(record);
								records.push(Ext.apply({"~":"update"}, record.data, {"@":record.modified}));
							}
						});
					}
					catch(e)
					{
						alert(e.message);  return;
					}
					Ext.each(deletedRows, function(data){
						records.push(Ext.apply({"~":"delete"}, data));
					});
					var data = Ext.encode(records);
					
					layout.el.mask('正在保存信息...', 'x-mask-loading');
					var hide = layout.el.unmask.createDelegate(layout.el);
					Ext.Ajax.request({
						method:'POST', 
						url:'../TableBrowser', 
						params: {action:"saveData", table: table.name, data:data},
						callback: function(options, success, response){
							hide(); 
							if (success)
							{
								tb.items.get('save').disable(); 
								deletedRows.splice(0, deletedRows.length);
								ds.each(function(record){if (record.newRecord) record.newRecord=false;});
								ds.commitChanges();
							}
							else
							{
								Browser.showException(response);
							}
						}
					});
			    }
			}
			]);
			// trigger the data store load
			ds.on('loadexception', function(proxy, obj, response, e){  if (e) alert('读取数据失败\n'+e.message); else Browser.showException(response); });
			var enableSave = function(){ tb.items.get('save').enable(); }
			ds.on('add', function(store, records, index){ Ext.each(records, function(record){record.newRecord=true}); enableSave();});
			ds.on('remove', function(store, record, index){ if (!record.newRecord) { deletedRows.push(record.data); enableSave();}});
			ds.on('update', function(store, record, operation){ if (operation==Ext.data.Record.EDIT) enableSave();});
			ds.load();
		},
		
		showException: function(response){
			var statusText = response.responseText;
			if (!statusText)
			{
				switch (response.status) 
				{
				case 0:
					alert('通讯失败，服务器可能已经停止运行'); 
					 return;
				default:
					alert (response.statusText+'('+response.status+')');  
					return;
				}
			}
	        var dialog = new Ext.BasicDialog(Ext.id(), {
				autoCreate: true, 
				title: '提示信息',
	            width:485,
	            height:260,
				resizable:false,
	            shadow:true,
				modal:true,
	            minWidth:320,
	            minHeight:120,
				buttonAlign:"center",
	            proxyDrag: true
	        });
			dialog.body.createChild({html: statusText || '&#160;'});
	        dialog.addButton('确定', dialog.hide, dialog);
			dialog.on('hide', function(){ dialog.destroy(true);});
			dialog.show();
		}
	}
}();

Ext.onReady(Browser.init, Browser, true);