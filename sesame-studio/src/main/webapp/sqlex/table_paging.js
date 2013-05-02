Ext.BLANK_IMAGE_URL='../images/s.gif';
/*
 * 需要在页面上配置一个table对象。示例：
 */
var Browser = function(){
	var grid;
	
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
				var ClassRef, config={selectOnFocus:true};
				
				fields.push({name: column.dataIndex, type: column.type, dateFormat: column.dateFormat});
				column.editable = (!table.readonly && table.disabled.indexOf(column.dataIndex)==-1);
				switch(column.type)
				{
				case "string":
					ClassRef=Ext.form.TextField; 
					config.autoCreate={tag: "input", type: 'text', maxLength: column.size, autocomplete: "off"};
					break;
				case "int":
					config.allowDecimals=false;
					config.autoCreate={tag: "input", type: 'text', maxLength: 10, autocomplete: "off"};
				case "decimal":
					ClassRef=Ext.form.NumberField;
					column.align='right';
					if (Ext.type(column.restriction)=="object"){
						if (column.restriction.minValue) config.minValue=column.restriction.minValue;
						if (column.restriction.maxValue) config.maxValue=column.restriction.maxValue;
					}
					break;
				case "date":
					config.format='Y-m-d';
					ClassRef=Ext.form.DateField;
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
					column.editor = new Ed(new ClassRef(config))
				}
			});
			var cm = new Ext.grid.ColumnModel(table.columns);
			// by default columns are sortable
			cm.defaultSortable = true;
			
			var Item = Ext.data.Record.create(fields);
			// create the Data Store
			var ds = new Ext.data.Store({
			    proxy: new Ext.data.HttpProxy({url: '../TableOperate', method:'GET'}),
				baseParams: {action:"loadData", table:table.name},
			    reader: new Ext.data.JsonReader(	{ root: 'rows', totalProperty: 'totalCount', id: table.key }, Item)
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
			var save = function(){
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
				
				Ext.getBody().mask('正在保存信息...', 'x-mask-loading');
				var hide = Ext.getBody().unmask.createDelegate(Ext.getBody());
				Ext.Ajax.request({
					method:'POST', 
					url:'../TableOperate', 
					params: {action:"saveData", table:table.name, key:table.key, data:data},
					callback: function(options, success, response){
						hide(); 
						if (success)
						{
							grid.getTopToolbar().get('save').disable();
							deletedRows.splice(0, deletedRows.length);
							ds.each(function(record){if (record.newRecord) record.newRecord=false;});
							ds.commitChanges();
						}
						else
						{
							app.showException(response);
						}
					}
				});
		    };
			// create the editor grid
			grid = new Ext.grid.EditorGridPanel({
				border: false,
				columnLines: true,
			    ds: ds,
			    cm: cm,
				loadMask: true,
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
				    handler : function(){
						var cell = grid.getSelectionModel().getSelectedCell();
				        if (cell) grid.startEditing(cell[0], cell[1]);
				    }
				},
				'-',
				{
					id: 'save',
				    text: '保存',
					icon: '../images/icons/save_edit.gif',
					cls: 'x-btn-text-icon',
					disabled: true,
				    handler : save
				},
				{
					id: 'saveas',
					text: '导出',
				    tooltip: '保存到Excel文档',
					icon: '../images/icons/saveas_edit.gif',
					cls: 'x-btn-text-icon',
				    handler : function(){
						frame.src = frame.getAttribute('url');
				    }
				}],
				bbar:  new Ext.PagingToolbar({
					store: ds,
				    pageSize: 25,
				    beforePageText: "页",
					afterPageText: " / {0}",
				    displayInfo: true,
				    displayMsg: '正在显示记录 {0} - {1} 总记录数: {2}',
				    emptyMsg: "空记录"
				})
			});
			var viewport = new Ext.Viewport({
				stateId: 'table_paging',
			    layout: 'fit',
			    items: [grid]
			});
			
			grid.el.addKeyListener(Ext.EventObject.DELETE, removeRec);
			grid.el.addKeyListener(Ext.EventObject.INSERT, insertRec);
			grid.on('contextmenu', function(e){e.stopEvent();});
			
			// trigger the data store load
			ds.on('loadexception', function(proxy, obj, response, e){ if (e) alert('读取数据失败\n'+e.message); else app.showException(response); });
			var enableSave = function(){ grid.getTopToolbar().get('save').enable(); }
			ds.on('add', function(store, records, index){ Ext.each(records, function(record){record.newRecord=true}); enableSave();});
			ds.on('remove', function(store, record, index){ if (!record.newRecord) { deletedRows.push(record.data); enableSave();}});
			ds.on('update', function(store, record, operation){ if (operation==Ext.data.Record.EDIT) enableSave();});
			ds.load({params:{start:0, limit:25}, callback:function(records, options, success){}});
		}
		
	}
}();

Ext.onReady(Browser.init, Browser, true);