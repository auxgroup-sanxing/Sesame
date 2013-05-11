
Ext.BLANK_IMAGE_URL='../images/s.gif';
var Viewer = function(){

return {
	
	 getCollectionView: function(viewConfig){
	 	var _this = this;
	 	
		function createView(view){
			var fields = [];
			Ext.each(view.columns, function(column){
				var ClassRef, config={};
				fields.push({name: column.dataIndex, type: column.type, dateFormat: column.dateFormat});
			});
			var cm = new Ext.grid.ColumnModel(view.columns);
			cm.defaultSortable = false;
			
			var Item = Ext.data.Record.create(fields);
			var ds = new Ext.data.Store({
			    //proxy: new Ext.data.HttpProxy({url: '../TableOperate', method:'GET'}),
			    reader: new Ext.data.JsonReader(	{ root: 'rows', totalProperty: 'totalCount', idProperty:'tradeType' }, Item)
			});
			
			return {
				xtype: 'panel',
				border: false,
				layout: 'border',
				items: [{
					region: 'center',
					height: 480,
					xtype: 'grid',
					itemId: "system",
					//icon: '../images/icons/loadingx.gif',
					title: "分析视图",
					floatable: false, collapsible: true, autoScroll: true,
					border: false, closable: false,
				    cm: cm,
				    store : ds,
			        view: new Ext.grid.GridView({
			            forceFit: view.forceFit
			        })
			       
				}]
			};
		};
		
		var win = new Ext.Window({
			title: '采样分析',
	        autoCreate: true,   resizable:true,  
	        constrain:true, constrainHeader:true,
	        minimizable:false,  maximizable:false,
	        stateful:false,
	        modal:true,
	        buttonAlign: "right",
			defaultButton: 0,
			y: 0,
			width: 640,
			height: 400,
			footer: true,
			closable: true,
			//closeAction: 'hide',
			plain: true,
			stateful: false,
			layout: 'fit',
			items: [createView(viewConfig)],
			stop:function(){
				if(win.sampling){
					win.sampling = false;
					Ext.Ajax.request({
						waitMsg: 'Please Wait....',
						url:'console_ctrl.jsp',
						params:{
							operation:"endCollection",
							objectName: viewConfig.objectName
						},
						success:function(response){
							Ext.Msg.alert("提示", "采样已停止");
						},
						failure:function(response){
							_this.showException(response);
						}
					});
				}
			},
			listeners:{
				beforeClose:function(){
					win.stop();
				}
			},
			tbar: [
				'-',
				{
					text:'停止采样',
					handler:function(){
						win.stop();
					}
				},
				'-'
			],
			interval: 1000 * 5,
			sampling: true,
		    run : function(){
		        var _this = this;
		    	return (function() {
		    		if(!_this.sampling || !_this.isVisible()) {
		    			return;
		    		}
		    		
					Ext.Ajax.request({
						url: viewConfig.url,
						timeout: 70000,
						params:{
							//operation: "polling"
						},
						success:function(response){
							_this.onMessage(Ext.decode(response.responseText));
							setTimeout(_this.run(), 1000 * 5);
						},
						failure:function(response){
							setTimeout(_this.run(), 1000 * 10);
						}
					});
		    	});
		    },
	    	onMessage : function(e){
		    	//var data = {totalCount: 1, id:'tradeType' };
	    		var grid = this.getComponent(0).getComponent("system");
		    	Ext.each(e.data, function(element){
		    	
		    		if (grid && grid.store) {
		    			var reqRec  = grid.store.getById(element.tradeType);
		    			if(reqRec){
		    				for (var p in element) {
		    					reqRec.set(p, element[p]);
		    				}
		    			}
		    			else {
			    			var rec = new grid.store.recordType(element, element.tradeType);
			    			grid.store.add(rec);
		    			}
		    		}
		    	});
		    }
		});
		setTimeout(win.run(), 1000 * 3);
		/*
		Ext.Direct.addProvider(
	        {
	            type:'longpolling',
	            url: viewConfig.url,
	            interval: 10000
	        }
	    );
	    Ext.Direct.on('message', function(e){
	    	//var data = {totalCount: 1, id:'tradeType' };
	    	Ext.each(e.data, function(element){
	    	
	    		var grid = Ext.getCmp("system");
	    		if (grid) {
	    			var reqRec  = grid.store.getById(element.tradeType);
	    			if(reqRec){
	    				for (var p in element) {
	    					reqRec.set(p, element[p]);
	    				}
	    			}
	    			else{
		    			var rec = new grid.store.recordType(element, element.tradeType);
		    			grid.store.add(rec);
	    			}
	    		}
	    	});
	    });
	    */
		return win;
	},
    
	init : function() {
		var _this = this;
		
		Ext.QuickTips.init();
		var propGrid = new Ext.grid.PropertyGrid({
			region: 'center',
		    autoHeight: true,
		    border: false,
			propertyNames: {
				'arch': '架构',
				'currentState': '当前状态',
				'description': '描述',
				'free': '可用',
				'length': '长度',
				'max': '峰值',
				'name': '名称',
				'total': '总数',
				'title': '标题',
				'vendor': '生产商',
				'version': '版本'
			},
			listeners: {
				columnresize : function(columnIndex, newSize){ this.stopEditing(); },
				resize : function(){ this.stopEditing(); },
				beforeedit : function(e){
					e.cancel = true;
				}
			},
			tbar:[]
		});

		function do_invoke(userData){
			Ext.Ajax.request({
				waitMsg: 'Please Wait....',
				url:'console_ctrl.jsp',
				params:{
					operation:"invoke",
					record:Ext.encode(userData)
				},
				success:function(response){
					Ext.Msg.alert("成功调用方法");
				},
				failure:function(response){
					_this.showException(response);
				}
			});
		}
		
		function invoke(b){
			if(b.userData.parameters.length >0){
				_this.invokeWith(b.userData, do_invoke);
			}
			else{
				do_invoke(b.userData);
			}
			
		}
	
		
		var viewport = new Ext.Viewport({
		    layout: 'border',
		    
		    items: [{
		    	id: 'westPanel',
		        region: 'west',
		        collapsible: true,
		        title: '资源树',
		        xtype: 'treepanel',
		        width: 200,
		        autoScroll: true,
		        split: true,
		        border: false,
		        loader: new Ext.tree.TreeLoader({
		        	dataUrl:'console_ctrl.jsp',
		        	baseParams: {operation:'refreshAll'},
					listeners: {
						loadexception: function(loader, node, response){ _this.showException(response); } 
					}
	        	}),
		        root: new Ext.tree.AsyncTreeNode({
		        	id: 'ROOT',
		            expanded: true,
		            text: '树'
		        }),
		        tools: [{
		        	id: 'refresh',
		        	qtip: '刷新',
		        	handler:function(e, toolEl, panel, tc){
		        		panel.getRootNode().reload();
		        	}
		        }],
		        contextMenu:new Ext.menu.Menu({
		        	items:[{
		        		id:'refresh',
		        		text:'刷新'
		        	},{
		        		id:'collection',
		        		text:'采集该节点数据'
		        	},{
		        		id:'collectionAll',
		        		text:'采集该节点同组数据'
		        	}],
		        	listeners:{
		        		itemclick:function(item){
		        			var node = item.parentMenu.contextNode;
		        			switch(item.id){
		        				case 'refresh':
		        					node.reload();
									node.collapse(true, false);
									node.expand(false, false);
		        					break;
		        				case 'collection':
		        				
		        					Ext.Ajax.request({
										waitMsg: 'Please Wait....',
										url:'console_ctrl.jsp',
										params:{
											operation:"startCollection",
											objectName:node.attributes.qtip,
											id:'system'
										},
										success:function(response){
											var view = Ext.decode(response.responseText);
											view.objectName = node.attributes.qtip;
											view.url="console_ctrl.jsp?operation=pollMessage&model=system&id="+node.attributes.qtip;
											_this.getCollectionView(view).show();
										},
										failure:function(response){
											_this.showException(response);
										}
									});
		        					break;
		        				case 'collectionAll':
		        					
		        					Ext.Ajax.request({
										waitMsg: 'Please Wait....',
										url:'console_ctrl.jsp',
										params:{
											operation:"startCollection",
											objectName:node.attributes.qtip,
											id:'group'
										},
										success:function(response){
											var view = Ext.decode(response.responseText);
											view.objectName = node.attributes.qtip;
											view.url="console_ctrl.jsp?operation=pollMessage&model=group&id="+node.attributes.qtip;
											_this.getCollectionView(view).show();
										},
										failure:function(response){
											_this.showException(response);
										}
									});
		        					
		        			}
		        		}
		        	}
		        
		        }),
		       // rootVisible: false,
		
		        listeners: {
		        	contextMenu:function(node,e){
		        			var c = node.getOwnerTree().contextMenu;
		        			if(node.attributes.qtip){
			        			var type = node.attributes.qtip.split(',').slice(-1)[0];
			        			c.get('collection').setVisible(type=='Type=Collector');
			        			c.get('collectionAll').setVisible(type=='Type=Collector');
		        			}else{
		        				c.get('collectionAll').setVisible(false);
		        				c.get('collection').setVisible(false);
		        			}	
							e.preventDefault();
							node.select();
							var c = node.getOwnerTree().contextMenu;
							c.contextNode = node;
							c.showAt(e.getXY());
						},
		            click: function(n) {
		            	var centerPanel = Ext.getCmp("centerPanel");
		            	if (n.leaf){
		            		Ext.Ajax.request({
								waitMsg: 'Please Wait....',
								url:'console_ctrl.jsp',
								params:{
									operation:"refreshMBean",
									objectName:n.attributes.qtip
								},
								success:function(response){
									var views = response.responseText;
									
									var attributes = Ext.decode(views);
									centerPanel.setDisabled(false);
					            	propGrid.setSource({});
				            		//var attributes = n.attributes;
				            		if(attributes){
			            				propGrid.setSource(attributes.attris);
			            				//grid.store.loadData(eachDemain);
			            				var operations = attributes.ops;
			            				var tb = propGrid.getTopToolbar();
			            				if(tb){
				            				tb.removeAll();
				            				if(operations.length > 0){
					            				tb.add('-');
					            				Ext.each(operations,function(op){
					            					op.objectName=n.attributes.qtip;
					            					tb.add({
					            						text:op.op_name,
					            						handler:invoke,
					            						userData:op
						            					},
						            					{xtype: 'tbspacer', width: 5},'-'	
					            					)
					            					
					            				})
					            				tb.doLayout();
				            				}
			            				}
				            				
					            	}
									
								},
								failure:function(response){
									_this.showException(response);
								}
							});
		            	}
		            	else {
		            		propGrid.setSource({});
		            		propGrid.getTopToolbar().removeAll();
		            	}
		            }
		        }
		    }, {
		        region: 'center',
		        id: 'centerPanel',
		        xtype: 'panel',
		        title: '详细信息',
		        border: false, 
		        deferredRender:false, 
		        activeItem: 0,
		        layout: 'card',
		        items:[
		        	propGrid
		        ]
		    }]
		});
		viewport.show();
		
		//监听MBeanServer变化
		window.listen = function(){
			
			Ext.Ajax.request({
				url:'console_ctrl.jsp',
				timeout: 70000,
				params:{
					operation:"listenNotification"
				},
				success:function(response){
					//alert("成功调用listen方法");
					setTimeout(listen, 1000 * 5);
				},
				failure:function(response){
					setTimeout(listen, 1000 * 30);
				}
			});
			
		}
		setTimeout(listen, 1000);
		
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
					//config.emptyText='boolean';
					config.editable=false;
					config.forceSelection=true;
					config.selectOnFocus=true;
					config.triggerAction = 'all';
					config.store =new Ext.data.SimpleStore({
								fields : ['Value',
										'Name'],
								data : [[1, true],
										[2, false]]
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
};

}();

Ext.onReady(Viewer.init, Viewer, true);
