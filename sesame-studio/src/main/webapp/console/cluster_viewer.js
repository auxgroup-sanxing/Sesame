document.oncontextmenu = function(e) {return false;};
Ext.BLANK_IMAGE_URL = '../images/s.gif';

var nameMapping = [
{
	'name': 'start',
	'displayname': '开始'
},{
	'name': 'stop',
	'displayname': '停止'
},{
	'name': 'shutDown',
	'displayname': '关闭'
},{
	'name': 'loadWSDL',
	'displayname': '查看接口'
},{
	'name': 'getDescriptor',
	'displayname': '查看描述'
}];

var Viewer = function(){
	var server_name;
	return {
		chartList : [],		// 监控图表对象列表
		serverList : [],	// 已启动server列表
		operaURL: 'clusterCtrl.jsp',
		mbeanURL: 'consoleCtrl.jsp',
		
		prepare: function() {
			Ext.QuickTips.init();
			// 监控图表使用本地时间
			Highcharts.setOptions({ global: {useUTC: false} });
			this.serverList = [];
			this.chartList = [];
		},
		
		init: function(){
			var _this = this;
			_this.prepare();
			
			// 发送请求取得所有server状态
			_this.loadServers();
		},
		
		loadServers: function() {
			var _this = this;
			
			var Item = Ext.data.Record.create([
				{name: 'server-name'},
				{name: 'IP'},
				{name: 'admin'},
				{name: 'jms-port'},
				{name: 'jndi-name'},
				{name: 'started'},	// 受管服务器启动/停止状态
				{name: 'sysInfo'}
			]);
			
			var ds = new Ext.data.Store({
                proxy: new Ext.data.HttpProxy({
                    url: _this.mbeanURL
                }),
                baseParams: {
                    operation: 'loadServers'
                },
                reader: new Ext.data.JsonReader({
                    root: 'items',
                    totalProperty: 'totalCount',
                    id: 'name'
                }, Item)
            });
			
			
			// 数据源
			var comboSt = new Ext.data.Store({
				url: _this.operaURL,
				baseParams: {
					operation: 'loadDatasource'
				},
				reader: new Ext.data.JsonReader({
					root: 'items',
					id: 'name',
					fields: [
						{name: 'value'}, 
						{name: 'text'},
						{name: 'driver-class'},
						{name: 'url'}, 
						{name: 'username'}, 
						{name: 'password'},
						{name: 'max-idle'},
						{name: 'max-wait'},
						{name: 'max-active'},
						{name: 'initial-size'}
					]
				})
			});
			
			var serverView = new Ext.DataView({
				id: 'serverView',
				tpl: new Ext.XTemplate(
				'<tpl for=".">' +
					'<div id="{server-name}" class="item-wrap">' +
						'<div style="display:block;height:30px;width:172px;">' +
							'<tpl if="started">' +
								'<div class="serverWrap" style="background:url(setting_viewer/images/server_running.png) no-repeat 0 0 scroll;"></div>'+
							'</tpl>' +
							'<tpl if="!started">' +
								'<div class="serverWrap" style="background:url(setting_viewer/images/server_stopped.png) no-repeat 0 0 scroll;"></div>'+
							'</tpl>' +
							'<div style="float:left;padding:5 0 0 20;"><b>{server-name}</b></div>' +
						'</div>' +
						'<div style="display:block;margin-left:1px;">' + 
							'<div id="{server-name}_cpu" style="float:left;"><img src="../images/s.gif"/></div>' + 
							'<div id="{server-name}_memory" style="float:left"><img src="../images/s.gif"/></div>' + 
						'</div>' +		
					'</div>' +
				'</tpl>'),
				multiSelect: true,
				store: ds,
				itemSelector: 'div.item-wrap',
				selectedClass: 'hilited',
				loadingText: '正在加载服务器列表...',
				emptyText: '<列表为空>',
				contextMenu: new Ext.menu.Menu({
					items: [{
						id: 'start',
						text: '启动'
					}, {
						id: 'stop',
						text: '停止'
					}, {
						id: 'prop',
						text: '属性'
					}, {
						id: 'detail',
						text: '查看详细'
					}],
					listeners: {
						itemclick: function(item){
							var servername = item.parentMenu.servername;
							var op = item.id;
							
							var index = serverView.getSelectedIndexes();
							var rec = ds.getAt(index);
							
							switch(op){
								case 'prop':
									append({
										winConfig: {title: '属性编辑器'},
										data: rec
									});
									break;
								case 'detail':
									var serverName = rec.data['server-name'];
									var isStarted = rec.data['started'];
									var data = rec.data.sysInfo;
									_this.openDetailPage(serverName, isStarted, data);
									break;
								default: 	
									Ext.Ajax.request({
										waitMsg: '正在启动...',
										url: _this.mbeanURL,
										params: {
											operation: 'invokeServer',
											servername: item.parentMenu.servername,
											op: item.id
										},
										success: function(response){
											Ext.Msg.alert('提示：', '成功调用方法!');
										},
										failure: function(response){
											_this.showException(response);
										}
									});
									break;
							}
						}
					}
				}),
				listeners: {
					contextMenu: function(dataview, index, el, e){
						e.preventDefault();
						dataview.select(el.id);
						var c = dataview.contextMenu;
						c.servername = el.id;
						c.showAt(e.getXY());
					}
				}
			});
			
			// 工具栏
			var toolBar = new Ext.Toolbar({
                style: {
                    'border-left': 'none',
					'border-right': 'none'
                }
            });
            toolBar.add({
                id: 'add_conn',
                cls: 'x-btn-text-icon',
                text: '添加',
                handler: function(){
					append({});
				},
                icon: "../explorer/sqlexplorer_viewer/images/add_obj.gif"
            }, {
                id: 'del_conn',
                cls: 'x-btn-text-icon',
                text: '删除',
                handler: remove,
                icon: "../explorer/sqlexplorer_viewer/images/delete.gif",
                disabled: true
            },{
				id: 'prop_conn',
                cls: 'x-btn-text-icon',
                text: '属性',
                handler: function(){
					var index = serverView.getSelectedIndexes();
					var rec = ds.getAt(index);
					append({
						winConfig: {
							title: '属性编辑器'
						},
						data: rec
					})
				},
                icon: "../explorer/sqlexplorer_viewer/images/example.gif",
                disabled: true
			}, {
				id: 'detail_conn',
				cls: 'x-btn-text-icon',
				text: '查看详细',
				handler: function() {
					var index = serverView.getSelectedIndexes();
					var rec = ds.getAt(index);
					var serverName = rec.data['server-name'];
					var isStarted = rec.data['started'];
					var data = rec.data.sysInfo;
					_this.openDetailPage(serverName, isStarted, data);		
				},
				icon: "../explorer/sqlexplorer_viewer/images/view.gif",
                disabled: true
			});
			
			// 添加或修改托管服务器
            var append = function(config){
                var servernameField = new Ext.form.TextField({
                    fieldLabel: '服务器名称',
                    name: 'server-name',
                    width: '100%',
                    allowBlank: false
                });
                
                var newPropsFormPanel = new Ext.FormPanel({
                    id: 'newnorth',
                    split: true,
                    autoScroll: true,
                    buttonAlign: 'right',
                    stateful: false,
					bodyStyle: {
						'border': 'none',
						'padding':'8 5 0 10'
					},
                    defaults: {
                        anchor: '-18',
                        stateful: false
                    },
                    items: [servernameField, {
                        xtype: 'fieldset',
                        id: 'cluster',
                        bodyStyle: Ext.isIE ? 'padding-top: 10px;' : null,
                        layout: 'form',
                        defaults: {anchor: '100%'},
                        items: [new Ext.form.TextField({
                            fieldLabel: '服务器地址',
                            name: 'IP',
                            allowBlank: false,
                            width: 200
                        }), new Ext.form.NumberField({
                            fieldLabel: 'JMS端口',
                            name: 'jms-port',
                            allowBlank: false,
                            width: 200
                        }), new Ext.form.ComboBox({
                            store: comboSt,
                            fieldLabel: '数据源',
                            forceSelection: 'true',
							itemId: 'jndi-name',
                            name: 'jndi-name',
							editable: true,
                            emptyText: '<请选择数据源>',
                            valueField: 'value',
                            displayField: 'text',
							triggerAction: 'all',
                            listeners: {
								expand: function() {
									this.store.reload();
								},
								select: function(obj, record){
									var value = obj.getValue();
									
									var form = Ext.getCmp('newcenter').getForm();
									var data = record.data;
									
									if (data.value == value) {
										form.findField('jndi-name-detail').setValue(value);
										for (var key in data) {
											var field = form.findField(key);
											if (!!field)
												field.setValue(data[key]);
										}
									}
									Ext.getCmp("newcenter").setVisible(true);
								},
								specialkey: function(field, e) {
									if (e.getKey() == e.BACKSPACE)
										Ext.getCmp("newcenter").setVisible(false);
								}
                            }
                        })]
                    }]
                });
				
                var newDs_formPanel = new Ext.FormPanel({
                    id: 'newcenter',
                    title: '数据源属性',
                    hidden: true,
                    autoScroll: true,
                    border: false,
                    buttonAlign: 'right',
                    stateful: false,
					headerStyle: {'border-top': '1px solid #99BBE8'},
					bodyStyle: {
						'border': 'none',
						'padding': '5 5 5 5'
					},
                    items: [{
                        layout: 'column',
						border: false,
                        defaults: {anchor: '100%'},
                        items: [{
                            columnwidth: .45,
							defaults: {
								width: 180, 
								readOnly : true,
								disabled: true
							},
							bodyStyle: {'padding-right':'8px'},
                            layout: 'form',
                            border: false,
                            items: [new Ext.form.TextField({
                                fieldLabel: 'JNDI名称',
								itemId: 'jndi-name-detail',
                                name: 'jndi-name'
                            }), new Ext.form.TextField({
                                fieldLabel: '连接URL',
								itemId: 'url',
                                name: 'url'
                            }), new Ext.form.TextField({
                                fieldLabel: '密码',
								itemId: 'password',
                                name: 'password',
                                inputType: 'password'
                            }), new Ext.form.NumberField({
                                fieldLabel: '最大空闲连接',
								itemId: 'max-idle',
                                name: 'max-idle',
                                allowNegative: false,
                                nanText: '请输入数字!'
                            }), new Ext.form.NumberField({
                                fieldLabel: '初始化连接池数量',
                                name: 'initial-size',
								itemId: 'initial-size',
                                allowNegative: false,
                                nanText: '请输入数字!'
                            })]
                        }, {
                            columnwidth: .45,
                            layout: 'form',
                            border: false,
							defaults: {
								width: 180, 
								readOnly : true,
								disabled: true
							},
                            items: [new Ext.form.TextField({
                                fieldLabel: '驱动类',
                                name: 'driver-class',
								itemId: 'driver-class'
                            }), new Ext.form.TextField({
                                fieldLabel: '用户名',
                                name: 'username',
								itemId: 'username'
                            }), new Ext.form.NumberField({
                                fieldLabel: '最大等待连接时间',
                                name: 'max-wait',
								itemId: 'max-wait',
                                allowNegative: false,
                                nanText: '请输入数字!'
                            }), new Ext.form.NumberField({
                                fieldLabel: '最大活动连接数',
                                name: 'max-active',
								itemId: 'max-active',
                                allowNegative: false,
                                nanText: '请输入数字!'
                            })]
                        }]
                    }]
                });
				
                var ds_win = new Ext.Window(Ext.apply({
                    title: '新建服务器',
                    autoCreate: true,
					frame: true,
                    resizable: true,
                    minimizable: false,
                    maximizable: false,
                    stateful: false,
                    modal: false,
                    buttonAlign: "right",
					closeAction: 'close',
                    width: 640,
                    height: 400,
                    closable: true,
					bodyStyle: {
						'background-color': '#FFFFFF'
					},
                    items: new Ext.Panel({
						border: false,
						items: [newPropsFormPanel, newDs_formPanel]
					}),
					footerCssClass: 'x-window-bc',
					buttons: [{
						text: '保存',
						handler: function() {
							if(newPropsFormPanel.getForm().isValid()){
								var values = 
									newPropsFormPanel.getForm().getValues(false);
								if (!!config.data) {
									folderView.store.each(function(record){
										if (record.data['server-name'] == config.data.data['server-name']) 
											folderView.store.remove(record);
									});
								}
								
								var rec = new Ext.data.Record(values);
								folderView.store.add(rec);
								saveSetting();
								ds_win.close();
							}else{
								Ext.Msg.alert("请确认要提交数据的完整性!");
							}
						}
					}, {
						text: '取消',
						handler: function() {
							ds_win.close();
						}
					}]
                }, config.winConfig || {} ));
				
				if (!!config.data) {
					var rec = config.data;
					newPropsFormPanel.getForm().loadRecord(rec);
					
					comboSt.reload({
						callback: function(records){
							Ext.each(records, function(record){
								if (record.data.value = rec.data['jndi-name']) {
									var dsCombobox = newPropsFormPanel.getForm().findField('jndi-name');
									dsCombobox.fireEvent('select', dsCombobox, record);
								}
							});
						}
					});
				}
                ds_win.show();
            };
			
			var remove = function(){
                if (!window.confirm("确实要删除选中的服务器吗？")) 
                    return;
                var records = [];
                Ext.each(folderView.getSelectedIndexes(), function(index){
                    records.push(ds.getAt(index));
                });
                Ext.each(records, function(record){
                    ds.remove(record);
                });
                saveSetting();
            };
			
			var saveSetting = function(){
                var datas = [];
                ds.each(function(record){
                    datas.push(record.data);
                });
				
				// 保存至 cluster.xml
                Ext.Ajax.request({
                    method: 'POST',
                    url: _this.operaURL,
                    params: {
                        operation: 'saveServers',
                        data: Ext.encode(datas)
                    },
                    callback: function(response, option, success) {
                        if(success)
                            ds.load();
                        else
                            _this.showException(response);
                    }
                });
            };
			
			var win;
            function promptWin(value){
            	if(!win){
            		win = new Ext.Window({
            			title:'操作返回信息',
            			width:640,
            			height:400,
            			border:false,
            			closable:false,
            			layout:'fit',
            			items:new Ext.FormPanel({
            				 id:'form',
            				 bodyStyle:'padding:5px 5px 0',
            				 layout:'fit',
							 items:[
							 	{
							 		xtype:'textarea',
							 		id:'area',
							 		readOnly:true,
							 		value:value
							 	}							 
							 ]
            			}),
            			buttons:[{
            				text:'关闭',
            				handler:function(){
            					win.hide();
            				}
            			}]
            		});
            	}else{
	            	var form = Ext.getCmp("form");
	            	var area = form.findById('area');
	            	area.setValue(value);
            	}
            	win.show();
            };
			
			ds.on('loadexception', function(proxy, obj, response){
                _this.showException(response);
            });
			
            ds.load({
				callback: function(records, options, success){
					if(success && records.length > 0) {
						Ext.each(records, function(record, index){
							var id = record.get('server-name');
							var started = record.get('started');
							var sysInfo = record.get('sysInfo');	// CPU和内存数据
							
							var cpuId = id + '_cpu';
							var cpuData = (!!sysInfo)? parseInt(sysInfo['cpu']):0;
							
							var memoryId = id + '_memory';
							var memoryData = (!!sysInfo)? parseInt(sysInfo['memory']):0;
							
							// 生成监控图表
							var cpuChart,memoryChart;
							if (!!Ext.fly(cpuId))
								cpuChart = 
									_this.createMonitor(cpuId, cpuData, 
										{text: 'CPU利用率', margin:[35, 0, 5, 30], height:100, width:120, showLabel: ''});
							if (!!Ext.fly(memoryId))
								memoryChart = _this.createMonitor(memoryId, memoryData, 
										{text: '内存占用率', margin:[35, 0, 5, 0], height:100, width:80, showLabel: 'none'});
							
							// 保存chart对象	
							if (started == true) {
								var chat = {
									'server': id,
									'cpu': cpuChart,
									'memory': memoryChart
								};
								_this.serverList.push({server: id});
								_this.chartList.push(chat);
							}
						});
						
						// 消息推送(每3秒轮循)
						Ext.Direct.addProvider({
							id: 'poll-provider',
							type: 'longpolling',
							url: _this.mbeanURL + '?operation=getSysInfo&servers=' + Ext.encode(_this.serverList),
							interval: 1500
						});
						
						Ext.Direct.on('message', function(e){
							function setChart(seriesId, chart, data) {
								var series = chart.get(seriesId);
								series.data[0].dataLabel.destroy();
								series.setData([data], true);
							};
							
							Ext.each(e.data, function(dataObj){
								var server = dataObj['name'];
								var cpu = parseInt(dataObj['cpu']);
								var memory = parseInt(dataObj['memory']);
								
								if(_this.chartList.length > 0)
									Ext.each(_this.chartList, function(chartObj) {
										var id = chartObj['server'];
										if(server == id) {
											var cpuChart = chartObj['cpu'];
											setChart(id + '_cpu_data', cpuChart, cpu);
											
											var memoryChart = chartObj['memory'];
											setChart(id + '_memory_data', memoryChart, memory);
										}
									});
							});
						});
					}
				}
			});
			
			// 服务器列表显示面板
			var serverPanel = new Ext.Panel({
				title: '服务器列表',
				autoScroll: true,
				resizable: true,
                stateful: false,
                split: true,
                modal: true,
                frame: false,
                border: false,
                header: false,
				tbar: toolBar,
				items: serverView,
				listeners: {
					activate: function() {
						Viewer.startPolling('poll-provider');
					},
					deactivate : function() {
						Viewer.stopPolling('poll-provider');
					}
				}
			});
			
			// 选择服务器列表后删除和属性button可见
			var btns = toolBar.items.map;
			serverView.on('selectionchange', function(view, selections){
				btns['del_conn'].setDisabled(selections.length < 1);
				btns['prop_conn'].setDisabled(!(selections.length == 1));
				btns['detail_conn'].setDisabled(!(selections.length == 1));
			});
			function invoke(b){
                if (b.userData.parameters.length > 0) {
                    _this.invokeWith(b.userData, do_invoke);
                } else {
                    //do_invoke(b.userData);
                	do_invoke(b);
                }
            };
            function do_invoke(button){
            	var userData = button.userData;
                Ext.Ajax.request({
                    waitMsg: '正在提交请求....',
                    url: 'consoleCtrl.jsp',
                    params: {
                        operation: "invoke",
                        record: Ext.encode(userData)
                    },
                    success: function(response){
                    	var returnType = userData.returnType
                    	var op_name = userData.op_name;
                    	if(returnType != 'void'){
                    		var xmlDoc = response.responseXML.documentElement;
	                    	var result = xmlDoc.getElementsByTagName("task-result")[0];
	                    	if(result){
	                    		 var state = result.childNodes[0].nodeValue;
	                    		 if(state == 'SUCCESS'){
	                    		 	Ext.Msg.alert('提示:','成功调用方法!');
	                    		 	var treeNode = button.selectedNode;
	                    		 	if(op_name == 'shutDown'){
	                    		 		treeNode.getUI().getIconEl().src = Ext.BLANK_IMAGE_URL;
	                    		 	}
	                    		 	else if(op_name == 'stop'){
	                    		 		treeNode.getUI().getIconEl().src = "../images/ovr16/state_stopped.gif";
	                    		 	}
	                    		 	else if(op_name == 'start'){
	                    		 		treeNode.getUI().getIconEl().src = "../images/ovr16/running_ovr.gif";
	                    		 	}
	                    		 	if(treeNode.attributes.type == 'ServiceUnitAdaptor'){
                    		 			treeNode.reload();
                    		 			var endpoint = treeNode.parentNode.parentNode.findChild("key", "Type=Endpoint");
                    		 			//var endpoint = mbeanTree.getNodeById("Endpoint_"+treeNode.attributes.servername);
                    		 			if (endpoint && endpoint.reload)
                    		 				endpoint.reload();
	                    		 	}
	                    		 	treeNode.fireEvent('click',treeNode);
	                    		 	
	                    		 }else{
	                    		 	Ext.Msg.alert('提示:','方法调用失败!');
	                    		 }
	                    	}else{
	                    		promptWin(response.responseText); 
	                    	}
                    	}
                    	else{
                    		Ext.Msg.alert('提示:','成功调用方法!');
                    		var treeNode = button.selectedNode;
                		 	if(op_name == 'shutDown'){
                		 		treeNode.getUI().getIconEl().src = Ext.BLANK_IMAGE_URL;
                		 	}
                		 	else if(op_name == 'stop'){
                		 		treeNode.getUI().getIconEl().src = "../images/ovr16/state_stopped.gif";
                		 	}
                		 	else if(op_name == 'start'){
                		 		treeNode.getUI().getIconEl().src = "../images/ovr16/running_ovr.gif";
                		 	}
                    		button.selectedNode.fireEvent('click',button.selectedNode);
                    	}
                    	
                    	
                    },
                    failure: function(response){
                        Viewer.showException(response);
                    }
                });
            };
            var mbeanTree = new Ext.tree.TreePanel({
                id: 'center-center-west',
                split: true,
                region: 'west',
                title: 'MBean',
                xtype: 'treepanel',
                rootVisible:false,
                width: 250,
                autoScroll: true,
                split: true,
                border: false,
                loader: new Ext.tree.TreeLoader({
                    dataUrl: _this.mbeanURL,
                    baseParams: {
                        operation: 'getServerMBean'
                    },
                    listeners: {
                        loadexception: function(loader, node, response){
							Viewer.showException(response);
                        },
                        beforeload:function(loader,node){
                        	loader.baseParams.path = node.getPath('key').substring(2);
                        	loader.baseParams.type = node.attributes.type;
                        }
                    }
                }),
                root: new Ext.tree.AsyncTreeNode({
                    id: 'ROOT',
                    key: '',
                    expanded: true,
                    text: 'MBean'
                }),
                tools: [{
                    id: 'refresh',
                    qtip: '刷新',
                    handler: function(e, toolEl, panel, tc){
                        panel.getRootNode().reload();
                    }
                }],
                contextMenu: new Ext.menu.Menu({
                    items: [{
                        itemId: 'refresh',
                        text: '刷新',
                        handler: function(item){
                            var node = item.parentMenu.contextNode;
                            if (node.reload) node.reload();
                        }
                    }]
                }),
                listeners: {
                	beforeappend: function(tree, parent, node){
						var a = node.attributes;
						if (a.iconCls) {
							a.icon = _this.getOverlayIcon(node);
						}
                	},
                    contextMenu: function(node, e){
                        node.select();
                        var c = node.getOwnerTree().contextMenu;
                        c.contextNode = node;
                        c.showAt(e.getXY());
                    },
                    click: function(n){
                        var centerPanel = Ext.getCmp("detail-panel");
                        if(n.attributes.name){
                            Ext.Ajax.request({
                                url: 'consoleCtrl.jsp',
                                params: {
                                    operation: "refreshMBean",
                                    objectName: n.attributes.name
                                },
                                success: function(response){
                                    var views = response.responseText;
                                    
                                    var attributes = Ext.decode(views);
                                    centerPanel.setDisabled(false);
                                    propGrid.setSource({});
                                    if (attributes) {
                                        propGrid.setSource(attributes.attris);
                                        var operations = attributes.ops;
                                        var tb = propGrid.getTopToolbar();
                                        if (tb) {
                                            tb.removeAll();
                                            if (operations.length > 0) {
                                                tb.add('-');
                                                Ext.each(operations, function(op){
                                                	Ext.each(nameMapping,function(map){
                                                		if(map.name == op.op_name){
                                                			op.objectName = n.attributes.name;
		                                                    tb.add({
		                                                        text: map.displayname,
		                                                        handler: invoke,
		                                                        userData: op,
		                                                        selectedNode:n
		                                                    }, {
		                                                        xtype: 'tbspacer',
		                                                        width: 5
		                                                    }, '-')
                                                			return false;
                                                		}
                                                	});
                                                });
                                                tb.doLayout();
                                            }
                                        }
                                    }
                                },
                                failure: function(response){
                                    Viewer.showException(response);
                                }
                            });
                        } 
                        else {
                            propGrid.setSource({});
                            var tb = propGrid.getTopToolbar();
							tb.removeAll();
                        }
                    }
                }
            });
			
			// 资源表格
			var propGrid = new Ext.grid.PropertyGrid({
                region: 'center',
                autoHeight: true,
				header: false,
                border: false,
                autoScroll: true,
                propertyNames: {
                    'arch': '架构',
                    'componentName': '组件名',
                    'componentType': '组件类别',
                    'currentState': '当前状态',
                    'description': '描述',
                    'endpointName': '端点名',
                    'exchangeThrottling': '节流阀',
                    'free': '可用',
                    'interfaces': '接口',
                    'inboundQueueCapacity': '队列深度',
                    'length': '长度',
                    'max': '峰值',
                    'name': '名称',
                    'remoteContainers': '远程容器',
                    'serviceAssembly': '服务集合',
                    'serviceName': '服务名',
                    'serviceUnits': '服务单元',
                    'throttlingInterval': '节流间隔',
                    'throttlingTimeout': '节流超时',
                    'total': '总数',
                    'title': '标题',
                    'vendor': '生产商',
                    'version': '版本'
                },
                listeners: {
                    beforeedit: function(e){
                        e.cancel = true;
                    }
                },
                tbar:[]
            });
			
			// MBean树面板
			var mbeanPanel = new Ext.Panel({
				title: '集群资源',
				layout: 'border',
				resizable: true,
                stateful: false,
                split: true,
                modal: true,
                frame: false,
                border: false,
                header: false,
				items: [
					mbeanTree, {
                    region: 'center',
                    id: 'detail-panel',
                    autoScroll: true,
                    xtype: 'panel',
                    title: '详细信息',
                    border: false,
                    deferredRender: false,
                    items: propGrid
                }]
			});
			
			// TabPanel主面板
			var tabPanel = new Ext.TabPanel({
				id: 'tabpanel',
				activeTab: 0,
				border: false,
				frame: false,
				items: [serverPanel, mbeanPanel]
			});
			
			var viewport = new Ext.Viewport({
                layout: 'fit',
                items: tabPanel
            });
		},
		
		// CPU监控和内存监控
		createMonitor: function(containerID, data, config) {
			var chart;
			var color = (containerID.indexOf('memory') != -1) ? ['#02923B']:['#4572A7'];
			var options = {
				title: {
					text: config['text'],
					style: {font: 'normal 12px Verdana, sans-serif'}
				},
				chart: {
					margin: config['margin'],
					height: config['height'],
					width: config['width'],
					color: color
				},
				yAxis: {
					min: 0,max: 100,gridLineWidth: 1,tickPixelInterval: 25,
					labels: {style: {display: config['showLabel']}}
				}
			};
			
			chart = HighChart.createColumnChart(containerID, data, options);
			return chart;
		},
		
		// 监控历史数据折线图
		createHisChart: function(containerID, data, config) {
			var options = {
				title: {
					text: config['text'],
					style: {
						font: 'normal 12px Verdana, sans-serif',
						margin: '10px 200px 0 0'
					}
				},
				chart: {
					margin: config['margin'],
					height: config['height'],
					width: config['width']
				}
			};
			chart = HighChart.createLineChart(containerID, data, options);
			return chart;
		},
		
		createThreadPoolChart: function(containerID, data, config) {
			var chart;
			var color = ['#4572A7'];
			var options = {
				title: {
					text: config['text'],
					style: {font: 'normal 12px Verdana, sans-serif'}
				},
				plotOptions: {
					column: {
						shadow: false,
						borderWidth: 0
					}
				},
				chart: {
					inverted: config['inverted'],
					margin: config['margin'],
					height: config['height'],
					width: config['width'],
					color: color
				},
				xAxis: {
					categories: ['activeCount:', 'completedTaskCount:', 
								 'corePoolSize:', 'largestPoolSize:', 
								 'poolSize:', 'taskCount:'],
					tickPixelInterval: 0, lineWidth: 0, tickLength: 0,
					labels: {style: {fontSize: '12px'}}
				},
				yAxis: {
					min: 0, max: 11, gridLineWidth: 0
				},
				series: {
					dataLabels: {
						enabled: true, rotation: 0, color: '#000',
						align: 'center', x: 5, y: 0,
						style: {font: 'normal 11px Verdana, sans-serif'},
						formatter: function(){
							return Highcharts.numberFormat(this.y, 0);
						}
					}
				}
			};
			
			chart = HighChart.createColumnChart(containerID, data, options);
			return chart;
		},
		
		getOverlayIcon: function(node) {
			var a = node.attributes;
			switch(a.state) {
			case 'Started': 
				return '../images/ovr16/running_ovr.gif';
			case 'Stopped':
				return '../images/ovr16/state_stopped.gif';
			default:
				return Ext.BLANK_IMAGE_URL;
			}
		},

		//带参数的方法调用
		invokeWith : function(op, doInvoke){
			var items = [{
				layout:'form',
				columnWidth:'0.5',
				border:false,
				items:[]
			},{
				layout:'form',
				columnWidth:'0.5',
				border:false,
				items:[]
			}];
			var i=0; n=items.length;
			var col;
			var parameters = op.parameters;
			Ext.each(parameters,function(element){
				col = items[i%n];
				var ClassRef;
				var config={fieldLabel:element.param_name,name:element.param_name,allowBlank:false,anchor:'95%'}
				switch(element.param_type){
					case "java.lang.String":
						config.xtype='textfield';
						config.emptyText='string';
						break;
					case "int":
					case "long":
					case "double":
						config.xtype='numberfield';
						config.emptyText='0';
						break;
					case "boolean":
						config.xtype='combo';
						config.editable=false;
						config.forceSelection=true;
						config.selectOnFocus=true;
						config.triggerAction = 'all';
						config.store =new Ext.data.SimpleStore({
							fields : ['Value','Name'],
							data : [[1, true],[2, false]]
						});
						config.mode = 'local';
						config.valueField= 'Value';
						config.displayField= 'Name';
						break;
					default:
						config.xtype='textfield';
						config.emptyText='string';
				}
				col.items.push(config);
				i++;
			});
			
			
			var win = new Ext.Window({
				title:'方法参数输入',
				autoCreate: true,   resizable:true,  
		        constrain:true, constrainHeader:true,
		        minimizable:false,  maximizable:false,
		        stateful:false,
		        modal:true,
		        buttonAlign: "right",
				closable : true,
				width:500,
	            autoHeight: true,
	            footer: true,
	            plain: true,
	            layout:'fit',
	            y: 100,
	            items:{
	            	id:'param_form',
	            	xtype:'form',
	            	layout:'column',
	            	border:false,
	            	items:items
	            },
	            buttons:[{
	            	text:'调用',
	            	handler:function(){
	            			var form = Ext.getCmp("param_form").getForm();
	            			if(form.isValid()){
		            			var key_value = form.getValues();
		            			Ext.each(op.parameters,function(element){
		            				element.param_value=key_value[element.param_name];
		            			})
		            			doInvoke(op);
		            			win.close();
	            			}else{
	            				Ext.Msg.alert("form valid error!");
	            			}
	            		}
	            },{
	            	text:'取消',
	            	handler:function(){win.close();}
	            }]
			});
			win.show();
		},
		
		// 打开详细监控信息页
		openDetailPage : function(serverName, isStarted, data) {
			var _this = this;
			
			// 节点服务器停止时无法查看
			if (!isStarted) {
				Ext.Msg.alert('提示', '受管服务器未启动,无法查看详细信息')
				return;
			}
			
			var pollingProvider = new Ext.direct.PollingProvider({
				id: serverName + '-poll-provider',
				priority: 1,
				type: 'longpolling',
				url: _this.mbeanURL + '?operation=getSysInfo&servers=' + Ext.encode([{server: serverName}]),
				interval: 1200
			});
			Ext.Direct.addProvider(pollingProvider);
			
			// CPU内存详细监控数据
			var cmItem = new Ext.form.FieldSet({
				title: 'CPU和内存状态',
				style: 'margin:5 8 0 8;-moz-border-radius:5px;overflow-y:auto;',
				html: '<div style="float:left">' + 
						'<div id="' + serverName + '_cpu_detail"><img src="../images/s.gif"/></div>' + 
						'<div id="' + serverName + '_memory_detail" style="margin-top:10px;"><img src="../images/s.gif"/></div>' + 
					  '</div>' +
					  '<div style="float:left">' +
					  	'<div id="' + serverName + '_history"><img src="../images/s.gif"/></div>' + 
					  '</div>',
				height: 275
			});
			
			// 线程池监控数据
			var threadPools = data['threadPool'];
			var html = '';
			Ext.each(threadPools, function(poolInfo) {
				var poolId = poolInfo['poolId'];
				var innerHTML = '<div style="float:left;font-size:12px;">' +
								'<div style="margin-bottom:3px;">线程池ID: ' + poolId + '</div>' +
								'<div id="' + poolId + '_pool"><img src="../images/s.gif"/></div>' +
								'</div>';
				html += innerHTML;
			});		
					
			var threadPoolItem = new Ext.form.FieldSet({
				title: '线程池状态',
				style: 'margin:8 8 0 8;-moz-border-radius:5px;overflow-y:auto;',
				height: 150,
				html: html
			});
			
			// 详细监控TabPanel
			var detailMonitor = new Ext.Panel({
				id: serverName + 'DetailMonitor',
				title: serverName + '详细监控信息',
				closable: true,
				items: [cmItem, threadPoolItem],
				listeners: {
					activate: function(){
						_this.startPolling(serverName + '-poll-provider');
					},
					deactivate : function() {
						_this.stopPolling(serverName + '-poll-provider');
					},
					close: function() {
						_this.stopPolling(serverName + '-poll-provider');		
					}
				}
			});
			
			var mainTabPanel = Ext.getCmp('tabpanel');
			mainTabPanel.add(detailMonitor);
			mainTabPanel.setActiveTab(detailMonitor);
			
			// 初始化请求数据
			Ext.Ajax.request({
				method: 'GET',
				url: _this.mbeanURL,
				params: {
                    operation: "getSysInfo",
                    servers: Ext.encode([{server: serverName}])
                },
				callback: function(options, success, response) {
					if (!success) {
						_this.showException(response);
						return;
					}
			
					var rs = Ext.decode(response.responseText);
					if (!!rs && !!rs.data) {
						// 请求数据后生成图表
						var data = rs.data[0];
						var cpu = parseInt(data['cpu']);
						var memory = parseInt(data['memory']);
						var threadPools = data['threadPool'];
						
						var cpuChart = _this.createMonitor(serverName + '_cpu_detail', cpu, {
							text: 'CPU利用率',margin: [35, 0, 5, 30],
							height: 110, width: 160, showLabel: ''
						});
						
						var memoryChart = _this.createMonitor(serverName + '_memory_detail', memory, {
							text: '内存占用率',margin: [35, 0, 5, 30],
							height: 110,width: 160,showLabel: ''
						});
						
						// 历史数据图表
						var historyChart = _this.createHisChart(serverName + '_history', data, {
							text: 'CPU和内存使用率历史记录', margin: [],
							height: 300, width: 580, showLabel: ''
						});
						
						// 线程池监控
						var threadPoolCharts = [];
						if (threadPools.length > 0) {
							Ext.each(threadPools, function(data){
								var poolId = data['poolId'];
								var realData = [data['activeCount'], data['completedTaskCount'], data['corePoolSize'], data['largestPoolSize'], data['poolSize'], data['taskCount']];
								var chart = _this.createThreadPoolChart(poolId + '_pool', realData, {
									text: '',
									margin: [0, 0, 0, 135],
									height: 100,
									width: 220,
									inverted: true
								});
								threadPoolCharts.push(chart);
							});
						}
						
						pollingProvider.on('data', function(provider, e) {
							// 监控大图 
							function setColChart(seriesId, chart, data) {
								var series = chart.get(seriesId);
								series.data[0].dataLabel.destroy();
								series.setData([data], true);
							};
							
							var dataObj = e.data[0];
							var server = dataObj['name'];
							var cpu = parseInt(dataObj['cpu']);
							var memory = parseInt(dataObj['memory']);
							var threadPools = dataObj['threadPool'];
							
							setColChart(serverName + '_cpu_detail_data', cpuChart, cpu);
							setColChart(serverName + '_memory_detail_data', memoryChart, memory);
							
							// 历史数据
							var seriesCPU = historyChart.get('cpuHistory');
							var seriesMEM = historyChart.get('memHistory');
							
							var x = (new Date()).getTime();
							if (!!seriesCPU) {
								if (!!seriesCPU.data[0].dataLabel && seriesCPU.data[0].dataLabel != undefined)
									seriesCPU.data[0].dataLabel.destroy();
								seriesCPU.addPoint([x, cpu], true, true);
							}
							
							if (!!seriesMEM) {
								if (!!seriesMEM.data[0].dataLabel && seriesMEM.data[0].dataLabel != undefined)
									seriesMEM.data[0].dataLabel.destroy();
								seriesMEM.addPoint([x, memory], true, true);
							}
							
							// 线程池
							if (threadPools.length > 0) {
								Ext.each(threadPools, function(data, index){
									var poolId = data['poolId'];
									var chartData = [data['activeCount'], data['completedTaskCount'], data['corePoolSize'], data['largestPoolSize'], data['poolSize'], data['taskCount']];
									if (threadPoolCharts.length > 0) {
										var chart = threadPoolCharts[index];
										var series = chart.get(poolId + '_pool_data');
										Ext.each(series.data, function(d){
											d.dataLabel.destroy();
										});
										series.setData(chartData, true);
									}
								});
							}
						});
					}
				}
			});
		},
		
		// 激活Provider
		startPolling: function(providerID) {
			var provider = Ext.Direct.getProvider(providerID);
			if (!!provider && !provider.isConnected())
				provider.connect();
		},
		
		// 停止Provider
		stopPolling: function(providerID) {
			var provider = Ext.Direct.getProvider(providerID);
			if (!!provider && !!provider.isConnected())
				provider.disconnect();
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
						return;				}
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


// 图表类定义
var HighChart = {
	// 柱状图
	createColumnChart: function(containerID, data, options) {
		var chart = new Highcharts.Chart({
			plotOptions: options.plotOptions,
			chart: {
				renderTo: Ext.fly(containerID).dom,
				inverted: options.chart.inverted,
				defaultSeriesType: 'column',
				margin: options.chart.margin,
				height: options.chart.height,
				width: options.chart.width,
				events: {
					click: function(event) {
						// 单击时改变元素背景
						var parentContainerID = containerID.replace(/_.*/ig, '');
						var servers = 
							Ext.fly(parentContainerID).dom.parentNode.childNodes;
						Ext.each(servers, function(server) {
							Ext.fly(server).removeClass('hilited');
						});	
						Ext.fly(parentContainerID).addClass('hilited');
						
						// 被禁止的工具栏按钮可用
						var toolBar = Ext.getCmp('tabpanel').get(0).getTopToolbar();
						var btns = toolBar.items.map;
						btns['del_conn'].enable();
						btns['prop_conn'].enable();
						btns['detail_conn'].enable();
					},
					
					dblclick: function(event) {
						var store = Ext.getCmp('serverView').getStore();						
						var serverName = containerID.replace(/_.*/ig, '');
						store.data.each(function(item){
							var server = item.data['server-name'];
							var isStarted = item.data['started'];
							var data = item.data.sysInfo;
							if (server == serverName)
								Viewer.openDetailPage(serverName, isStarted, data);	// 双击打开详细页面展示
						});
					}
				}
			},
			title: options.title,
			xAxis: Ext.apply({tickPixelInterval: 0}, (options.xAxis != undefined)? options.xAxis: {}),
			yAxis: options.yAxis,
			exporting: {enabled: false},
			legend: {enabled: false},
			tooltip: {enabled: false},
			series: [{
				id: containerID + '_data',
				data: (Object.prototype.toString.apply(data) === '[object Array]') ? data : [data],
				dataLabels: Ext.apply({
					enabled: true,
					rotation: 0,
					color: '#264390',
					align: 'center',
					x: 0,y: -2,
					style: {font: 'normal 10.5px Verdana, sans-serif'},
					formatter: function(){
						return Highcharts.numberFormat(this.y, 0) + '%';
					}
				}, (options.series != undefined)? options.series.dataLabels : {})
			}],
			colors: options.chart.color
		});
		return chart;
	},
	
	// 折线图
	createLineChart: function(containerID, data, options) {
		var cpu = parseInt(data['cpu']);
		var memory = parseInt(data['memory']);
		
		var chart = new Highcharts.Chart({
			chart: {
				renderTo: Ext.fly(containerID).dom,
				defaultSeriesType: 'line',
				margin: options.chart.margin,
				height: options.chart.height,
				width: options.chart.width
			},
			title: options.title,
			xAxis: {
				type: 'datetime',
				tickPixelInterval: 130
			},
			yAxis: {
				title: {text: ''},
				min: 0, max: 100,
				gridLineWidth: 1,
				tickPixelInterval: 25,
				labels: {style: {display: ''}}
			},
			tooltip: {
				formatter: function() {
		        return '<b>'+ this.series.name +':  </b>'+
		        	   '<b style="color:blue;">'+ Highcharts.numberFormat(this.y, 0) + '%' +'</b><br/>'+
					   '<span style="font-size:11px;">'+ Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) +'</span><br/>';
				}
			},
			plotOptions: {
				line: { dataLabels: {enabled: true} }
			},
			legend: {
				enabled: true,
				align: 'right',
        		verticalAlign: 'top',
        		x:-60, y:5
			},
			exporting: {enabled: false},
			series: [{
				id: 'cpuHistory',
				name: 'CPU',
				data: (function() {
					var data = [];
					var	time = (new Date()).getTime();
					for (var i = -12; i <= 0; i++) {
						data.push({
							x: time + i * 1000,
							y: cpu
						});
					}
					return data;
				})(),
				dataLabels: {
					formatter: function() {
						return Highcharts.numberFormat(this.y, 0) + '%';
					}
				}
			},{
				id: 'memHistory',
				name: '内存',
				data: (function() {
					var data = [];
					var	time = (new Date()).getTime();
					for (var i = -12; i <= 0; i++) {
						data.push({
							x: time + i * 1000,
							y: memory
						});
					}
					return data;
				})(),
				dataLabels: {
					formatter: function() {
						return Highcharts.numberFormat(this.y, 0) + '%';
					}
				}
			}],
			colors: ['#4572A7','#02923B']
		});
		return chart;
	}
}

Ext.onReady(Viewer.init, Viewer, true);