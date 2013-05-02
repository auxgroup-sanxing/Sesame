Ext.BLANK_IMAGE_URL='../images/s.gif';

var Editor = function(){
	
    return {
		init : function(){
			var _this = this;
			Ext.QuickTips.init();
			
			var sql = referer && referer.indexOf('table_')==0 ? 'SELECT * FROM '+referer.replace(/table_/, '') : '';
			
			var viewport = new Ext.Viewport({
				layout: 'border',
		        items: [{
					region: 'north',
					xtype: 'form',
					border: false,
					id: 'sql-form',
		            height: 160,
		            minSize: 30,
		            split: true,
					hideLabels: true,
					monitorValid:true,
				    method:'POST',
				    url:'../TableOperate',
				    baseParams: {action:'execSQL'},
					waitMsgTarget: true,
					timeout: 60,
					tbar: [
						{
						    id: "execute",
						    text: '执行',
							tooltip: '执行SQL语句',
							icon: '../images/icons/run_exc.gif',
							cls: 'x-btn-text-icon',
						    handler : function(){ Ext.getCmp('sql-form').form.submit({waitMsg:"正在执行..."});}
						},
						{
						    text: '清除',
							icon: '../images/icons/clear_co.gif',
							cls: 'x-btn-text-icon',
						    handler : function(){ 
						    	var f = Ext.getCmp('sql-form').form.findField("sql");
						    	f.setValue('');
						    	f.focus();
						    }
						},
						'-'
					],
				    items: [{
				    	xtype: 'textarea',
				        name: 'sql',
				        value: sql,
				        anchor: "100%, 100%",
				        allowBlank: false,
				        blankText: "请输入SQL语句然后点击执行"
				    }]
				},
			    {
			    	region: 'center',
			    	border: false,
			    	layout: 'card',
			    	id: 'result-card',
			    	activeItem: 0,
			    	items: [{
				    	xtype: 'panel',
				    	id: 'result-panel',
				    	autoScroll: true,
				    	bodyStyle: 'border-width: 1px 0 0 0; font-size: 12px; padding: 5px;',
				    	html: ''
					},{
				    	xtype: 'editorgrid',
				    	id: 'result-grid',
				    	bodyStyle: 'border-width: 1px 0 0 0;',
				        columns: [
				            new Ext.grid.RowNumberer()
				        ],
						tbar: new Ext.PagingToolbar({
							style: 'border-width: 1px 0 0;',
							store: null,
						    pageSize: 50,
							afterPageText: " / {0}",
						    displayInfo: true,
						    displayMsg: '正在显示记录 {0} - {1} 总记录数: {2}',
						    emptyMsg: "空记录"
						}),
				    	store: new Ext.data.JsonStore({}),
						selModel: new Ext.grid.CellSelectionModel(),
						loadMask: true
			    	}]
			    }]
			});

			var sqlPanel = Ext.getCmp('sql-form');
			var speedBar = sqlPanel.getTopToolbar();
			var btns = speedBar.items.map;
			
			var resultGrid = Ext.getCmp('result-grid');
			var resultPanel = Ext.getCmp('result-panel');
			resultGrid.getTopToolbar().insertButton(11, {
				id: 'save',
			    tooltip: '保存查询结果到Excel文档',
				icon: '../images/icons/saveas_edit.gif',
				cls: 'x-btn-icon',
			    handler : function(){
					//pageLayout.el.mask('正在保存信息...', 'x-mask-loading');
					//var hide = pageLayout.el.unmask.createDelegate(pageLayout.el);
					var frame = document.getElementById('download');
					//frame.onload = hide;
					frame.src = frame.getAttribute('url');
			    }
			});

			sqlPanel.on('actioncomplete', function(form, action){
				var sql = sqlPanel.form.getValues().sql;
				var count=10, shift=true;
				for (var i=1; i<=count; i++)
				{
					if (!btns['sql'+i]) 
					{
						var button = speedBar.addButton(new Ext.Button({
							id: 'sql'+i,
							icon:'../images/icons/history.gif', 
							cls:'x-btn-icon', 
							tooltip: sql,
							handler: function(){ sqlPanel.form.setValues({sql:this.tooltip}) }
						}));
						shift = false;
						break;
					}
				}
				if (shift)
				{
					for (var i=1; i<=count; i++)
					{
						var btn1 = btns['sql'+i];
						var btn2 = btns['sql'+(i+1)];
						if (btn1 && btn2) 
						{
							btn1.tooltip = btn2.tooltip;
							var btnEl = btn1.el.child("button:first");
							btnEl.dom[btn1.tooltipType] = btn1.tooltip;
						}
					}
					var btn = btns['sql'+(count)]; 
					if (btn) 
					{ 
						btn.tooltip = sql;
						var btnEl = btn.el.child("button:first");
						btnEl.dom[btn1.tooltipType] = btn1.tooltip;
					}
				}
				speedBar.doLayout();
				
				if (action.result.columns) {
					Ext.getCmp('result-card').getLayout().setActiveItem(resultGrid);
					_this.showGrid(resultGrid, action.result.columns, form.findField("sql").getValue());
				}
				else {
					console.debug(Ext.getCmp('result-card'));
					Ext.getCmp('result-card').getLayout().setActiveItem(resultPanel);
					resultPanel.body.dom.innerHTML = action.result.affected+" 项变动";
					alert("执行成功");
				}
			});
			sqlPanel.on('actionfailed', function(form, action){ top.Application.showException(action.response); });
			sqlPanel.on('clientvalidation', function(form, valid){btns.execute.setDisabled(!valid);})
		},
		
		showGrid: function(grid, columns, sql){
			function formatBoolean(value){
			    return value ? '是' : '否';  
			};
			// shorthand alias
			var fm = Ext.form, Ed = Ext.grid.GridEditor;
			
			var fields = [];
			Ext.each(columns, function(column){
				var ClassRef, config={readOnly: true};
				
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
					break;
				case "date":
					ClassRef= Ext.form.TextField; //Ext.form.DateField; 
					column.renderer=function(value){ return value ? value.dateFormat(column.dateFormat) : '';};
					break;
				case "boolean":
					ClassRef=Ext.form.Checkbox;  
					column.renderer=formatBoolean;
					column.align='center';
					break;
				default:
					ClassRef=Ext.form.TextField;
				}
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
					   readOnly: true,
					   lazyRender:true
			        }));
					var valueText={};
					Ext.each(column.restriction, function(elem){valueText[elem.value]=elem.text;});
					column.renderer=function(value){ if (value==null) return value; var text=column.valueText[value.toString()]; return text?text:value;};
					column.align='left';
				}
				else
				{
					column.editor = new Ed(new ClassRef(config))
				}
			});
			var cm = new Ext.grid.ColumnModel(columns);
			// by default columns are sortable
			cm.defaultSortable = true;
			
			var Item = Ext.data.Record.create(fields);
			
			// create the Data Store
			var ds = new Ext.data.Store({
			    proxy: new Ext.data.HttpProxy(new Ext.data.Connection({url: '../TableOperate', method:'GET', timeout:300000})),
				baseParams: {action:"loadData", sql:sql },
			    reader: new Ext.data.JsonReader({
			           root: 'rows', totalProperty:'totalCount'
			       }, Item)
			});
			ds.on('loadexception', function(proxy, obj, response, e){ 
				if (e) 
					alert('读取失败'+e.message); 
				else 
					app.showException(response); 
			});

			var frame = document.getElementById('download');
			frame.setAttribute("url", "../TableOperate?action=loadData&export=true&sql="+sql);
			
			ds.load({params:{start:0, limit:50}});
			grid.reconfigure(ds, cm);
			grid.getTopToolbar().bind(ds);
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
			dialog.body.createChild({html: statusText || '&#160;', style:'height:100%; background-color:#f9f9f9;'});
	        dialog.addButton('确定', dialog.hide, dialog);
			dialog.on('hide', function(){ dialog.destroy(true);});
			dialog.show();
		}
	}
}();
//TODO 字符串为SQL语句时的引号处理
Ext.onReady(Editor.init, Editor, true);