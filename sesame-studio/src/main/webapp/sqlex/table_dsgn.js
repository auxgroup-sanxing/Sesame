Ext.BLANK_IMAGE_URL='../images/s.gif';

Ext.onReady(function(){
    Ext.QuickTips.init();
	
    function formatBoolean(value){
        return value ? '是' : '否';  
    };
    function formatRestrict(value){
        if (!value) return '';
		try
		{
			var obj = Ext.decode(value);
			if (Ext.type(obj)=="array")
			{
				var list = new Array();
				Ext.each(obj, function(elem){ list.push(elem.value+'-'+elem.text); });
				return list.join(', ');
			}
			else
			{
				return String.format('最小值:{0}, 最大值:{1}', obj.minValue, obj.maxValue);
			}
		}
		catch(e)
		{
			return '';
		}
    };
    // shorthand alias
    var fm = Ext.form, Ed = Ext.grid.GridEditor;

	var dataType = new Ext.data.SimpleStore({
	    fields: ['value', 'label'],
	    data: [['char', 'char-字符型'],['int', 'int-整型'], ['decimal','decimal-浮点型'], ['varchar', 'varchar-变长字符型'], 
					['date','date-日期型'], ['time','time-时间型'], ['boolean','boolean-逻辑型'], 
					['text','text-文本'], ['blob','blob-二进制块'], ['serial','serial-自增']
				]
	});
	
    // the column model has information about grid columns
    var cm = new Ext.grid.ColumnModel([{
		   id: "column_name",
		   header: "列名",
		   dataIndex: 'column_name',
		   width: 140,
		   editor: new Ed(new fm.TextField({
		       allowBlank: false,
			   regex: /[A-Za-z_]\w*/,
			   regexText: '列名称必须由字母或下划线开头'
		   }))
		},
		{
		   header: "描述",
		   id: 'remarks_col',
		   dataIndex: 'remarks',
		   width: 160,
		   editor: new Ed(new fm.TextField({
		       allowBlank: false
		   }))
		},
		{
		   id: "type_name",
		   header: "数据类型",
		   dataIndex: 'type_name',
		   width: 140,
		   editor: new Ed(new Ext.form.ComboBox({
		       typeAhead: true,
		       triggerAction: 'all',
			   store: dataType,
			   mode: 'local',
			   valueField:'value',
			   displayField:'label',
			   editable: false,
		       lazyRender:true
		    }))
		},
		{
		   header: "长度",
		   dataIndex: 'column_size',
		   width: 50,
		   align: 'right',
		   editor: new Ed(new fm.NumberField({
		       allowBlank: false,
		       allowNegative: false,
		       allowDecimals: false,
			   maxValue: 65536
		   }))
		},
		{
		   header: "小数位数",
		   dataIndex: 'decimal_digits',
		   width: 55,
		   align: 'right',
		   editor: new Ed(new fm.NumberField({
		       allowBlank: true,
		       allowNegative: false,
		       allowDecimals: false,
		       maxValue: 10
		    }))
		},
		{
		   header: "允许空",
		   dataIndex: 'is_nullable',
		   width: 55,
		   align: 'center',
		   renderer: formatBoolean,
		   editor: new Ed(new fm.Checkbox())
		},
		{
		   header: "约束",
		   id: 'restriction_col',
		   dataIndex: 'restriction',
		   width: 160,
		   renderer: formatRestrict,
		   editor: new Ed(new fm.RestrictEdit({ editable: false}))
		},
		{
		   header: "默认值",
		   dataIndex: 'column_def',
		   width: 80,
		   editor: new Ed(new fm.TextField())
		}
	]);
    cm.defaultSortable = false;

    var Column = Ext.data.Record.create([
           {name: 'column_name', type: 'string'},
           {name: 'type_name', type: 'string'},
           {name: 'column_size', type: 'int'},
           {name: 'decimal_digits', type: 'int'},
           {name: 'is_nullable', type: 'bool'},
           {name: 'column_def', type: 'string'},
           {name: 'restriction', type: 'string'},
           {name: 'remarks', type: 'string'}
      ]);

    // create the Data Store
    var ds = new Ext.data.Store({
        // load using HTTP
        proxy: new Ext.data.HttpProxy({url: '../SQLExplorer?action=loadColumns&object='+edittingObject}),
        // the return will be XML
        reader: new Ext.data.XmlReader({
               // records will have a "column" tag
               record: 'column'
           }, Column)
    });

    // create the editor grid
    var grid = new Ext.grid.EditorGridPanel({
    	border: false,
    	columnLines: true,
        ds: ds,
        cm: cm,
        autoExpandColumn: 'restriction_col',
		tbar: [
		{
	        text: '添加',
			tooltip: '添加列',
			icon: '../images/icons/add.gif',
			cls: 'x-btn-text-icon',
	        handler : function(){
	            var rec = new Column({
	                column_name: '',
	                type_name: '',
	                column_size: '',
	                decimal_digits: '0',
	                is_nullable: true,
	                column_def: '',
	                restriction: '',
	                remarks: ''
	            });
	            grid.stopEditing();
	            ds.add(rec);
	            grid.startEditing(ds.getCount()-1 , 0);
	        }
	    },
		{
	        text: '插入',
			cls: 'x-btn-text',
	        handler : function(){
				var cell = grid.getSelectionModel().getSelectedCell();
				var row = cell?cell[0]:0;
	            var rec = new Column({
	                column_name: '',
	                type_name: '',
	                column_size: '',
	                decimal_digits: '0',
	                is_nullable: true,
	                column_def: '',
	                restriction: '',
	                remarks: ''
	            });
	            grid.stopEditing();
	            ds.insert(row, rec);
	            grid.startEditing(row, 0);
	        }
	    },
		{
	        id: 'remove',
	        text: '删除',
			icon: '../images/icons/delete.gif',
			cls: 'x-btn-text-icon',
	        handler:  function(){
		        grid.stopEditing();
				var cell = grid.getSelectionModel().getSelectedCell();
				if (!cell || !window.confirm("确实要删除选中的项吗？")) return;
				ds.remove(ds.getAt(cell[0]));
			}
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
	        id: 'save',
			text: '保存',
			icon: '../images/icons/save_edit.gif',
			cls: 'x-btn-text-icon',
			disabled: true,
	        handler : function(){
	        	var _this = this;
				var records= new Array();
				try
				{
					var index=0;
					ds.each(function(record){
						var colName=record.get("column_name");
						if (colName=="")
						{
							grid.selModel.select(index, cm.getIndexById("column_name"));
							throw {message:"列名不能为空"}
						}
						if (index>0)
						{
							Ext.each(ds.getRange(0, index-1), function(rec){
								if (rec.get("column_name")==colName)
								{
									grid.selModel.select(index, cm.getIndexById("column_name"));
									throw {message:"列名必须唯一，前面已经有一个 "+colName};
								}
							});
						}
						if (record.get("type_name")=="") 
						{
							grid.selModel.select(index, cm.getIndexById("type_name"));
							throw {message:"数据类型不能为空"}
						}
						records.push(record.data);
						index++;
					});
				}
				catch(e)
				{
					alert(e.message);  return;
				}
				var data = Ext.encode(records);
				
				Ext.getBody().mask('正在保存信息...', 'x-mask-loading');
				var hide = Ext.getBody().unmask.createDelegate(Ext.getBody());
				Ext.Ajax.request({
					method:'POST', 
					url:'../SQLExplorer', 
					params: {action:"saveColumns", object: edittingObject, data: data},
					callback: function(options, success, response){
						hide(); 
						if (!success){
							alert(response.statusText);
						}
						else{
							_this.disable();  ds.commitChanges();
						}
					}
				});
	        }
		}],
		listeners : {
			afterrender: function(component) {
				var h = this.getTopToolbar().get('remove').handler;
				this.el.addKeyListener(Ext.EventObject.DELETE, h);
			},
			contextmenu: function(e){
				e.stopEvent();
			},
			beforeedit: function(e){
				switch (e.field)
				{
				case 'column_size':
					switch (e.record.get('type_name'))
					{
					case 'int':
					case 'text':
						e.cancel=true;
						break;
					}
					break;
				case 'decimal_digits':
					if (e.record.get('type_name')!='decimal') e.cancel=true;
					break;
				default:
				}
			}
		}
    });

	var viewport = new Ext.Viewport({
        layout: 'fit',
		items: [grid]
	});

    // trigger the data store load
	ds.on('loadexception', function(proxy, obj, response){ top.Application.showException(response); });
	var enableSave = function(){ grid.getTopToolbar().get('save').enable(); }
	ds.on('add', enableSave);
	ds.on('remove', enableSave);
	ds.on('update', function(store, record, operation){ if (operation==Ext.data.Record.EDIT) enableSave();});
    ds.load();
    
});