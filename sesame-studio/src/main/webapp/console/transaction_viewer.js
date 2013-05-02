document.oncontextmenu = function(e){return false;}
Ext.BLANK_IMAGE_URL='../images/s.gif';

var Viewer = function(){
	return {
		url: 'transaction_ctrl.jsp',
		pollInterval: 10,		// 监控页面消息推送时间间隔(单位:ms)
		maxRows: 25,			// 监控页面最大显示行数
		fuzzySearchCfg: {		// 模糊查询对象
			channel: true, serviceName: true,
			operationName: true, transactionCode: true,
			content: true
		},	
		exactSearchCfg: null, 	// 精确查询对象
		
		preparedInit: function(){
			Ext.Ajax.timeout = 3600 * 1000;
			Ext.QuickTips.init();
		},
		
		init: function(){
			var _this = this;
			_this.preparedInit();
			
			// 列定义
			function renderState(val) {
				var imgPath = '../ext-Ajax/resources/images/default/window/';
				var container = '<span><img src="#" style="padding-left:15px;width:13px;height:13px;"/></span>'+ 
								'<span style="margin:0 0 2px 3px"></span>';
				switch(val) {
					case '0':
					    imgPath += 'icon-yes.gif';
						return container.replace(/#/ig, imgPath);
						break;
					case '1':
					    imgPath += 'icon-unusual.png';
						return container.replace(/#/ig, imgPath);
						break;
					case '2':
					    imgPath += 'icon-warning.gif';
						return container.replace(/#/ig, imgPath);
						break;
					case '9':
						imgPath += 'hourglass.png';
						return container.replace(/#/ig, imgPath);
						break;	
					default: 
						break;
				}
			}
			
			var columns = [
				{ 
					header: '<center>交易状态</center>',
				  	dataIndex: 'STATE',
					align: 'left',
					fixed: true,
					width: 60,
					renderer: renderState
				}, {
					header: '<center>流水号</center>',
					dataIndex: 'SERIALNUMBER',
					align: 'center',
					sortable: false
				},{ 
					header: '<center>服务名</center>',
				  	dataIndex: 'SERVICENAME',
					align: 'center',
					sortable: false
				},{ 
					header: '<center>操作名</center>',
				  	dataIndex: 'OPERATIONNAME',
					align: 'center',
					sortable: false
				},{ 
					header: '<center>交易码</center>',
				  	dataIndex: 'TRANSACTIONCODE',
					align: 'center',
					sortable: false
				},{ 
					header: '<center>渠道</center>',
					dataIndex: 'CHANNEL',
					align: 'center',
					sortable: false
				},{ 
					header: '<center>开始时间</center>',
				  	dataIndex: 'STARTTIME',
					align: 'center',
					sortable: false
				},{ 
				  	header: '<center>更新时间</center>',
				  	dataIndex: 'UPDATETIME',
					align: 'center',
					sortable: false
				}
			];
			
			var fields = [
				{name : 'STATE', type : 'string'},
				{name : 'SERIALNUMBER', type : 'string'},	
				{name : 'SERVICENAME', type : 'string'},	
				{name : 'OPERATIONNAME', type : 'string'},	
				{name : 'TRANSACTIONCODE', type : 'string'},	
				{name : 'CHANNEL', type : 'string'},	
				{name : 'STARTTIME', type : 'string'},	
				{name : 'UPDATETIME', type : 'string'}
			];
			
			var cm = new Ext.grid.ColumnModel({
				align : 'center',
				columns : columns
			});
			
			var reader = new Ext.data.JsonReader({
		   		root : 'data',
				totalProperty : 'total',
				fields : fields
			});
			
			var dataStore = new Ext.data.Store({
			   autoDestroy: true,
			   sortInfo : {field : 'SERIALNUMBER', direction : 'DESC'},
			   proxy: new Ext.data.HttpProxy({
			   		url: _this.url + '?operation=getTransactionInfo'
			   }),
			   reader : reader
		    });
			
			var dataGrid = new Ext.grid.GridPanel({
				hideBorders : true,
				border: false,
				split : true,
				store: dataStore,
				cm: cm,
				stripeRows : true,
				columnLines : true,
				viewConfig: {forceFit: true}
			});
			dataGrid.store.removeAll();	// 先把数据清零
			
			// 交易监控面板
			var monitorPanel = new Ext.Panel({
				id: 'transactionMonitor',
				title: '实时交易信息',
				autoScroll: true,
				layout: 'fit',
				items : dataGrid,
				tbar: new Ext.Toolbar({
					style: {
						'padding-top': '6px',
						'border-top':'none',
						'border-right':'none'
					},
					items: [
						{
							text:'清屏',
							cls: 'x-btn-text-icon',
		                    icon:'../images/icons/clear_co.gif',
							handler: function() {
								dataGrid.store.removeAll();
							}
						}, '-', {
							cls: 'x-btn-icon',
							icon: '../images/icons/advance.png',
							handler: function() {
								_this.showAdvance();
							}
						}, '模糊查询:', ' ',
						new Ext.ux.SearchField({
							id: 'searchField',
							allowBlank: true,
							width: 320,
							emptyText: '<请输入要查询的关键词>',
							onTrigger2Click: function() {
								var keyword = this.getValue();
								// 查询条件
								var count = 0;
								for (var param in _this.fuzzySearchCfg) {
									if(_this.fuzzySearchCfg[param] == true)
										count++;
								}
								if (count == 0) {
									Ext.Msg.alert('提示:', '请先设置查询类型!');
									return;
								}
								if (!!keyword) {
									if (!!Ext.getCmp('detailPanel')) 
										Ext.getCmp('detailPanel').destroy();
									_this.queryTrans(keyword, columns, fields);
								} else {
									Ext.Msg.alert('提示:', '查询关键字不允许为空!');
								}
							}
						}), '-', {
							text: '精确查询',
							cls: 'x-btn-text-icon',
							icon: '../images/icons/advance_setting.png',
							handler: function() {
								_this.showMultiSearchWin(columns, fields);
							}
						}
					]
				}),
				listeners: {
					// 重新设置查询栏宽度,修正"精确查询"被遮盖问题
					afterLayout: function(){
						var searchbar = this.getTopToolbar().get('searchField');
						Ext.get(searchbar.el.dom.parentNode).setWidth('100%');
					}
				}
			});
			
			var tabPanel = new Ext.TabPanel({
				id: 'tabpanel',
				activeTab: 0,
				border: false,
				frame: false,
				items: monitorPanel
			});
			
			var viewport = new Ext.Viewport({
                layout: 'fit',
                items: tabPanel
            });
			
			// 消息推送(轮循)
			Ext.Direct.addProvider({
				id : 'trans-poll-provider',
		        type:'longpolling',
		        url: _this.url + '?operation=getTransactionInfo',
		        interval: _this.pollInterval,
				listeners: {
					// 接收消息
					data: function(provider, e) {	
						var dataObj = e.data;
						if (!dataObj || dataObj['SERIALNUMBER'] == undefined || !dataObj['SERIALNUMBER']) {
							return;
						} else {
							var data = new Ext.data.Record(dataObj);
							dataGrid.store.insert(0, data);
							
							// 现有数据量如果多于设置的数量就先删除一部分数据
							while (dataGrid.store.getCount() > _this.maxRows) {
								dataGrid.store.removeAt(dataGrid.store.getCount() - 1);
							}
						}
					}
				}
		    });
		},
		
		// 高级选项
		showAdvance: function() {
			var _this = this;
			
			var mainPanel = Ext.getCmp('transactionMonitor');
			var form = new Ext.form.FormPanel({
				id: 'advanceForm',
				header: false,
				frame : false,
				border: true,
				autoScroll: true,
				layout: 'form',
				bodyStyle: {
					'padding': '5 8 5 8',
					'background-color': '#DFE8F6'
				},
				defaults: {anchor: '100%', stateful: true},
				items: [{
					hideLabel: true,
					xtype: 'checkboxgroup',
					columns: [100, 100, 80],
					vertical: true,
					itemId: 'checkbox-group',
					style: { 'margin-top': '-4px!important'},
					name: 'checkbox-group',
					items: [
					{
						boxLabel: '交易渠道',
						itemId: 'channel', name: 'channel',
						checked: _this.fuzzySearchCfg['channel'],
						labelStyle: 'padding-top: 0px;'
					},{
						boxLabel: '服务名',
						itemId: 'serviceName', name: 'serviceName',
						checked: _this.fuzzySearchCfg['serviceName'],
						labelStyle: 'padding-top: 0px;'
					},{
						boxLabel: '操作名',
						itemId: 'operationName', name: 'operationName',
						checked: _this.fuzzySearchCfg['operationName'],
						labelStyle: 'padding-top: 0px;'
					},{
						boxLabel: '交易码',
						itemId: 'transactionCode', name: 'transactionCode',
						checked: _this.fuzzySearchCfg['transactionCode'],
						labelStyle: 'padding-top: 0px;'
					},{
						boxLabel: '报文',
						itemId: 'content', name: 'content',
						checked: _this.fuzzySearchCfg['content'],
						labelStyle: 'padding-top: 0px;'
					}]
				}]
			});
			
			var advanceWin = new Ext.Window({
				title: '设置查询类型',
				iconCls: 'advance',
				id: 'advanceWin',
				autoCreate: true, border : false,
		        resizable:false, constrain:true,
				constrainHeader:true,
		        minimizable:false, maximizable:false,
		        stateful:false,  modal:false,
				defaultButton: 0,
		        width: 350, height: 200, 
				minWidth: 200,
		        footer: true,
		        closable: true, 
				closeAction: 'close',
				plain: true,
				layout: 'fit',
				items: form,
				buttons: [{
			        text: '确定',
			        disabled: false,
					handler: function(){
						Ext.each(form.getForm().findField('checkbox-group').items.items, function(checkbox){
							_this.fuzzySearchCfg[checkbox.itemId] = checkbox.checked;
						});
						advanceWin.close();
					}
				},{
			        text: '取消',
					handler: function(){
						advanceWin.close();
					}
				}]
			});
			advanceWin.show();
		},
		
		// 交易日志模糊查询
		queryTrans: function(keyword, columns, fields) {
			var _this = this;
			
			var newColumns = [];
			Ext.each(columns, function(column) {
				if(column.dataIndex.indexOf('TIME') == -1)
					newColumns.push(column);
			});
			var newFields = [];
			Ext.each(fields, function(field) {
				if(field.name.indexOf('TIME') == -1)
					newFields.push(field);
			});
			
			// 按钮列
			function renderMsg(value, cellmeta, record, rowIndex, columnIndex, store) {
				var str = "<img class='innerImg' src='../images/icons/thread_view.gif'/>";
	         	return str;
			};
			
			var btnColumnCfg = {
				header: false,
				dataIndex: 'opera',
				menuDisabled: true,
				sortable: false,
				width: 30,
				fixed: true,
				tooltip: '详细交易数据',
				align: 'center',
				renderer: renderMsg
			};
			
			if (newColumns.length < 7)
				newColumns.push(btnColumnCfg);
			
			var cm = new Ext.grid.ColumnModel({
				align : 'center',
				columns : newColumns
			});
			
			var reader = new Ext.data.JsonReader({
		   		root : 'data',
				totalProperty : 'total',
				fields : newFields
			});
			
			var dataStore = new Ext.data.Store({
			   autoDestroy: true,
			   sortInfo : {field : 'SERIALNUMBER', direction : 'ASC'},
			   proxy: new Ext.data.HttpProxy({
			   		url: _this.url + '?operation=queryTrans&keyword=' + keyword + '&types=' + Ext.encode(_this.fuzzySearchCfg)
			   }),
			   reader : reader
		    });
			
			var dataGrid = new Ext.grid.EditorGridPanel({
				hideBorders : true,
				border: false,
				split : true,
				store: dataStore,
				cm: cm,
				loadMask : { msg : '正在查询数据...' },
				stripeRows : true,
				columnLines : true,
				viewConfig: {forceFit: true},
				listeners : {
					cellclick : function(grid, rowIndex, columnIndex, e) {
					    function open(fieldName){
							if (fieldName == 'opera') {
								var snumber = record.get('SERIALNUMBER');
								var state = record.get('STATE');
								_this.queryTransMsg(snumber, state);
							}
						}
						
						var record = grid.getStore().getAt(rowIndex);  // Get the Record
					    var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
					    
						var img = e.getTarget('.innerImg');
						if (!!img)open(fieldName);
					}
				},
				bbar: new Ext.PagingToolbar({
		            pageSize: 25,
		            store: dataStore,
		            displayInfo: true,
		            emptyMsg: "未找到匹配记录"
		        })
			});
			
			// 行提示信息
			dataGrid.on('render', function(grid) {
				var view = grid.getView();
				dataGrid.tip = new Ext.ToolTip({
			        target: dataGrid.getView().mainBody,
			        delegate: '.x-grid3-row',
			        trackMouse: true,
			        renderTo: document.body,
			        listeners: {
			            beforeshow: function updateTipBody(tip) {
			                tip.body.dom.innerHTML = "点击右侧图标查看详细视图";  
			            }
			        }
			    });
			});
			
			var queryPanel = new Ext.Panel({
				id: 'detailPanel',
				title: '查询结果',
				autoScroll: true,
				closable: true,
				layout: 'fit',
				items : dataGrid
			});
			
			var mainTabPanel = Ext.getCmp('tabpanel');
			mainTabPanel.add(queryPanel);
			mainTabPanel.setActiveTab(queryPanel);
			dataStore.reload({
				params: {start: 0, limit: 25}
			});
		},
		
		// 精确查询
		showMultiSearchWin: function(columns, cmFields) {
			var _this = this;
			var fields = [].concat(cmFields);
			var form = new Ext.form.FormPanel({
				id: 'multiForm',
				header: false, frame : true,
				border: true, autoScroll: true,
				layout: 'column',
				bodyStyle: {
					'padding': '5 8 5 8',
					'background-color': '#DFE8F6'
				},
				defaults: {anchor: '-10', stateful: true},
				items: [{
					columnWidth : .18,
					itemId: 'checkBoxColumn',
					name: 'checkBoxColumn',
					defaultType: 'checkbox',
					readOnly : true,
					defaults: {
						height: 28,
						listeners: {
							check: function(obj){
								var itemId = obj.getItemId();
								var fromObj = obj.ownerCt.ownerCt.getComponent('inputColumnS').getComponent(itemId + 'S');
								var toObj = obj.ownerCt.ownerCt.getComponent('inputColumnE').getComponent(itemId + 'E');
								var operS = obj.ownerCt.ownerCt.getComponent('operaS').getComponent(itemId + 'OperaS');
								var operE = obj.ownerCt.ownerCt.getComponent('operaE').getComponent(itemId + 'OperaE');
								if (obj.getValue() === true) {
									if (fromObj)fromObj.enable();
									if (toObj) toObj.enable();
									if (operS) operS.enable();
									if (operE) operE.enable();
								} else {
									if (fromObj) fromObj.disable();
									if (toObj) toObj.disable();
									if (operS) operS.disable();
									if (operE) operE.disable();
								}
							}
						}
					},
					items: [
					{
						boxLabel: '流水号',
						itemId: 'serialNumber', 
						name: 'serialNumber'
					},{
						boxLabel: '交易开始时间',
						itemId: 'startTime',
						name: 'startTime'
					},{
						boxLabel: '交易更新时间',
						itemId: 'updateTime',
						name: 'updateTime'
					},{
						boxLabel: '交易码',
						itemId: 'transactionCode',
						name: 'transactionCode'
					}]
				},{
					columnWidth : .11,
					itemId: 'operaS',
					name: 'operaS',
					defaultType: 'combo',
					defaults: {
						width: 60,
						cls : 'mutilWinTextField',
						hideMode: 'visibility',
						forceSelection: true,
						store: new Ext.data.ArrayStore({
				           fields: ['text', 'value'],
				           data : [['=', '='], ['<>', '<>'], ['>', '>'], ['>=', '>=']]
					    }),
						triggerAction: 'all', editable: false,
						mode: 'local',
						displayField: 'text',
						valueField: 'value',
						emptyText: '运算符',
						listeners: {
							select: function(obj) {
								var itemId = obj.getItemId().replace(/OperaS/ig, '');
								var opera = obj.getValue();
								var toObj = obj.ownerCt.ownerCt.getComponent('inputColumnE').getComponent(itemId + 'E');
								var operE = obj.ownerCt.ownerCt.getComponent('operaE').getComponent(itemId + 'OperaE');
								if (opera == '=') {
									if (toObj) {
										toObj.setValue('');
										toObj.hide();
									}
									if (operE) {
										operE.setValue('');
										operE.hide();
									}
								} else {
									if (toObj) toObj.show();
									if (operE) operE.show();
								}
							}
						}
					},
					items: [{
						itemId: 'serialNumberOperaS',
						name: 'serialNumberOperaS',
						disabled: true
					},{
						itemId: 'startTimeOperaS',
						name: 'startTimeOperaS',
						disabled: true
					},{
						itemId: 'updateTimeOperaS',
						name: 'updateTimeOperaS',
						disabled: true
					},{
						hidden: true,
						disabled: true
					}]
				},{
					columnWidth : .3,
					itemId: 'inputColumnS',
					name: 'inputColumnS',
					defaultType: 'textfield',
					defaults: {
						width: 155,
						cls : 'mutilWinTextField',
						hideMode: 'visibility'
					},
					items: [{
						itemId: 'serialNumberS',
						name: 'serialNumberS',
						emptyText: '<请输入起始值>',
						disabled: true
					},{
						itemId: 'startTimeS',
						name: 'startTimeS',
						xtype: 'datetimefield',
						emptyText: '<请输入起始时间>',
						disabled: true
					},{
						itemId: 'updateTimeS',
						name: 'updateTimeS',
						xtype: 'datetimefield',
						emptyText: '<请输入起始时间>',
						disabled: true
					},{
						itemId: 'transactionCodeS',
						name: 'transactionCodeS',
						emptyText: '<请输入需要查询的值>',
						disabled: true,
						cls : 'transactionCodeS'
					}]
				},{
					columnWidth : .11,
					itemId: 'operaE',
					name: 'operaE',
					defaultType: 'combo',
					defaults: {
						width: 60,
						cls : 'mutilWinTextField',
						hideMode: 'visibility',
						forceSelection: true,
						store: new Ext.data.ArrayStore({
				           fields: ['text', 'value'],
				           data : [['=', '='], ['<>', '<>'],['<', '<'],['<=', '<=']]
					    }),
						triggerAction: 'all', editable: false,
						mode: 'local',
						displayField: 'text',
						valueField: 'value',
						emptyText: '运算符',
						listeners: {
							select: function(obj) {
								var itemId = obj.getItemId().replace(/OperaE/ig, '');
								var opera = obj.getValue();
								var fromObj = obj.ownerCt.ownerCt.getComponent('inputColumnS').getComponent(itemId + 'S');
								var operS = obj.ownerCt.ownerCt.getComponent('operaS').getComponent(itemId + 'OperaS');
								if (opera == '=') {
									if (fromObj) {
										fromObj.setValue('');
										fromObj.hide();
									}
									if (operS) {
										operS.setValue('');
										operS.hide();
									}
								} else {
									if (fromObj) fromObj.show();
									if (operS) operS.show();
								}
							}
						}
					},
					items: [{
						itemId: 'serialNumberOperaE',
						name: 'serialNumberOperaE',
						disabled: true
					},{
						itemId: 'startTimeOperaE',
						name: 'startTimeOperaE',
						disabled: true
					},{
						itemId: 'updateTimeOperaE',
						name: 'updateTimeOperaE',
						disabled: true
					},{
						hidden: true,
						disabled: true
					}]
				},{
					columnWidth : .3,
					itemId: 'inputColumnE',
					name: 'inputColumnE',
					defaultType: 'textfield',
					defaults: {
						width: 155,
						cls : 'mutilWinTextField',
						hideMode: 'visibility'
					},
					items: [{
						itemId: 'serialNumberE',
						name: 'serialNumberE',
						emptyText: '<请输入结束值>',
						disabled: true
					},{
						itemId: 'startTimeE',
						name: 'startTimeE',
						xtype: 'datetimefield',
						emptyText: '<请输入结束时间>',
						disabled: true
					},{
						itemId: 'updateTimeE',
						name: 'updateTimeE',
						xtype: 'datetimefield',
						emptyText: '<请输入结束时间>',
						disabled: true
					},{
						hidden: true,
						disabled: true
					}]
				}],
				listeners: {
					afterLayout: function(obj) {
						obj.getForm().setValues(_this.exactSearchCfg);
					}
				}
			});
			
			var advanceWin = new Ext.Window({
				title: '设置查询条件',
				iconCls: 'advance',
				id: 'multiSearchWin',
				autoCreate: true, border : false,
		        resizable:false, constrain:true, constrainHeader:true,
		        minimizable:false, maximizable:false,
		        stateful:false,  modal:false,
		        width: 600, height: 200,
		        footer: true, closable: true, 
				closeAction: 'close', plain: true,
				layout: 'fit', items: form,
				buttons: [{
			        text: '查询',
			        disabled: false,
					handler: function(){
						var formValueStr = form.getForm().getValues(true);
						var formValueObj = form.getForm().getValues(false);
						var submitValues = form.getForm().getValues();
						
						// 删除拥有emptyText值的属性防止无效值提交
						for (var param in submitValues) {
							 if (form.form.findField(param) &&
							 		form.form.findField(param).emptyText == submitValues[param]) {
							 	delete formValueObj[param];
							 }
						}
						
						function invalid(chk, id) {
							if (chk == undefined || !chk) {
								return true;
							} else if (chk == 'on') {
								var startValue = formValueObj[id + 'S'];
								var startOp = formValueObj[id + 'OperaS'];
								var endValue = formValueObj[id + 'E'];
								var endop = formValueObj[id + 'OperaE'];
								
								if (id == 'transactionCode')
									return (startValue != undefined && startValue != null);
								
								return ((startOp != undefined && startOp != null) && (startValue != undefined && startValue != null) ||
										(endop != undefined && endop != null) && (endValue != undefined && endValue != null));
							} else {
								return true;
							}
						}
						
						// 流水号
						var hasSNValue = 
							invalid(formValueObj['serialNumber'], 'serialNumber');
							
						// 交易码
						var hasTRCValue = 
							invalid(formValueObj['transactionCode'], 'transactionCode');
						
						// 开始时间
						var hasSTValue = 
							invalid(formValueObj['startTime'], 'startTime');
						
						// 更新时间
						var hasUTValue = 
							invalid(formValueObj['updateTime'], 'updateTime');
						
						if (!!formValueStr) {
							if (!!hasSNValue && !!hasTRCValue && !!hasSTValue && !!hasUTValue) {
								_this.exactSearchCfg = formValueObj;
							} else {
								Ext.Msg.alert('提示: ', '请输入完整查询条件!');
								return;
							}
						} else {
							Ext.Msg.alert('提示: ', '请输入查询条件!');
							return;
						}

						advanceWin.close();
						_this.multiSearch(columns, fields);
					}
				},{
			        text: '取消',
					handler: function(){
						advanceWin.close();
					}
				}]
			});
			advanceWin.show();
		},
		
		// 多条件查询
		multiSearch : function(columns, fields) {
			if (!!Ext.getCmp('detailPanel')) Ext.getCmp('detailPanel').destroy();
			var _this = this;
			
			// 按钮列
			function renderMsg(value, cellmeta, record, rowIndex, columnIndex, store) {
				var str = "<img class='innerImg' src='../images/icons/thread_view.gif'/>";
	         	return str;
			};
			
			var btnColumnCfg = {
				header: false,
				dataIndex: 'opera',
				menuDisabled: true,
				sortable: false,
				width: 30,
				fixed: true,
				tooltip: '详细交易数据',
				align: 'center',
				renderer: renderMsg
			};
			
			var newColumns = [].concat(columns);
			if (newColumns.length < 9) {
				newColumns.push(btnColumnCfg);
			}
			
			var cm = new Ext.grid.ColumnModel({
				align : 'center',
				columns : newColumns
			});
			
			var reader = new Ext.data.JsonReader({
		   		root : 'data',
				totalProperty : 'total',
				fields : fields
			});
			
			// 格式化搜索条件
			var searchArray = [];
			for (var param in _this.exactSearchCfg) {
				if (_this.exactSearchCfg[param] == 'on') {
					var opS = _this.exactSearchCfg[param + 'OperaS'];
					var valueS = _this.exactSearchCfg[param + 'S'];
					var opE = _this.exactSearchCfg[param + 'OperaE'];
					var valueE = _this.exactSearchCfg[param + 'E'];
					
					if (param == 'transactionCode') {
						var temp = {name: param, op: '=', value: valueS};
						searchArray.push(temp);
					} else {
						if (opS != undefined) {
							var temp = { name: param, op: opS, value: valueS};
							searchArray.push(temp);
						}
						
						if (opE != undefined) {
							var temp = {name: param, op: opE, value: valueE};
							searchArray.push(temp);
						}
					}
				}
			}

			var dataStore = new Ext.data.Store({
			   autoDestroy: true,
			   sortInfo : {field : 'SERIALNUMBER', direction : 'ASC'},
			   proxy: new Ext.data.HttpProxy({
			   		url: _this.url + '?operation=inquiryTrans&condition=' + Ext.encode(searchArray)
			   }),
			   reader : reader
		    });
			
			var dataGrid = new Ext.grid.EditorGridPanel({
				hideBorders : true,
				border: false,
				split : true,
				store: dataStore,
				cm: cm,
				loadMask : { msg : '正在查询数据...' },
				stripeRows : true,
				columnLines : true,
				viewConfig: {forceFit: true},
				listeners : {
					cellclick : function(grid, rowIndex, columnIndex, e) {
					    function open(fieldName){
							if (fieldName == 'opera') {
								var snumber = record.get('SERIALNUMBER');
								var state = record.get('STATE');
								_this.queryTransMsg(snumber, state);
							}
						}
						
						var record = grid.getStore().getAt(rowIndex);  // Get the Record
					    var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
						var img = e.getTarget('.innerImg');
						if (!!img) open(fieldName);
					}
				},
				bbar: new Ext.PagingToolbar({
		            pageSize: 25,
		            store: dataStore,
		            displayInfo: true,
		            emptyMsg: "未找到匹配记录"
		        }) 
			});
			
			// 行提示信息
			dataGrid.on('render', function(grid) {
				var view = grid.getView();
				dataGrid.tip = new Ext.ToolTip({
			        target: dataGrid.getView().mainBody,
			        delegate: '.x-grid3-row',
			        trackMouse: true,
			        renderTo: document.body,
			        listeners: {
			            beforeshow: function updateTipBody(tip) {
			                tip.body.dom.innerHTML = "点击右侧图标查看详细视图";  
			            }
			        }
			    });
			});
			
			var queryPanel = new Ext.Panel({
				id: 'detailPanel',
				title: '查询结果',
				autoScroll: true,
				closable: true,
				layout: 'fit',
				items : dataGrid
			});
			
			var mainTabPanel = Ext.getCmp('tabpanel');
			mainTabPanel.add(queryPanel);
			mainTabPanel.setActiveTab(queryPanel);
			dataStore.reload({
				params: {start: 0, limit: 25}
			});
		},
		
		// 查询详细信息
		queryTransMsg: function(snumber, state) {
			var _this = this;
			
			var dataStore = new Ext.data.Store({
			   autoDestroy: true,
			   proxy: new Ext.data.HttpProxy({
			   		url: _this.url + '?operation=queryMsg&snumber=' + snumber + '&state=' + state
			   }),
			   reader: new Ext.data.JsonReader({
			   	 root: 'items',
			  	 totalProperty: 'count',
				 fields: [{name: 'startTime'},{name: 'updateTime'},
				 		  {name: 'stage'}, {name: 'value'}]
			   })
		    });
			
			function renderValue(value, metadata, record){
				metadata.attr = 'style="white-space:normal;"';
				value = Ext.util.Format.htmlEncode(value);
				value = value.replace(/#+/ig, '&nbsp;&nbsp;&nbsp;&nbsp;').replace(/[\r\n]+/g, '<br>');
				return value;
			}
			
			var columns = [
			{
				header: '<center>开始时间</center>',
				id: 'startTime', dataIndex: 'startTime',
				align: 'center', fixed: true, width: 150
			}, {
				header: '<center>更新时间</center>',
				id: 'updateTime', dataIndex: 'updateTime',
				align: 'center', fixed: true, width: 150
			},{
				header: '<center>交易阶段</center>',
				id: 'var', fixed: true,
				align: 'center', width: 140,
				dataIndex: 'stage'
			},{
				header: '<center>详细信息</center>',
				id: 'varValue', dataIndex: 'value',
				renderer: renderValue
			}];
			
			var xmlOutput = new Ext.grid.GridPanel({
				id: 'xmlOutput',
				border: true, hideLabels: true, stateful: false, autoScroll: true,
				stripeRows: true, columnLines: true, frame: false,
				store: dataStore,
				columns: columns,
				viewConfig: {forceFit: true, singleSelect: true},
				bbar: new Ext.PagingToolbar({
		            pageSize: 5,
		            store: dataStore, // dataStore,
		            displayInfo: true,
		            emptyMsg: "未找到匹配记录"
		        })
			});
			xmlOutput.store.removeAll();
			
			var msgWin = new Ext.Window({
				title: '交易日志详细信息',
				layout : 'fit', id: 'msgWin', 
				border: false, resizable : true,
				maximizable: true, minimizable : true,
				constrain : false, modal : true,
				width: 660, height: 350, 
				items : xmlOutput
			});
			msgWin.show();
			
			dataStore.reload({
				params: {start: 0, limit: 5}
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
}();

Ext.onReady(Viewer.init, Viewer, true);