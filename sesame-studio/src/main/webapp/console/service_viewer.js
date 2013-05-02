document.oncontextmenu = function(e) {return false;};
Ext.BLANK_IMAGE_URL='../images/s.gif';

// 服务单元分类
var unitCatalog = [
	{name: 'server', zhName:'代理服务', imgSrc: '../images/tool16/opentype.gif', viewer: '../explorer/unit_viewer.jsp?script=unit_v_binding.js&unit='},
	{name: 'engine', zhName:'本地服务', imgSrc: '../images/icons/thread_view.gif', viewer: '../explorer/unit_viewer.jsp?script=unit_v_engine.js&unit='},
	{name: 'client', zhName:'远程服务', imgSrc: '../images/icons/monitor_view.gif', viewer: '../explorer/unit_viewer.jsp?script=unit_v_binding.js&unit='}
];

// 查询结果类型分类
var typeCatalog = [
	{name: 'project', zhName: '项目'},
	{name: 'service', zhName: '服务'},
	{name: 'interface', zhName: '接口'},
	{name: 'operation', zhName: '操作'},
	{name: 'massage', zhName: '消息'},
	{name: 'element', zhName: '元素'},
];

var Viewer = function(){
	return {
		url: 'service_ctrl.jsp',
		
		searchCondition: null,	// 过滤条件
		
		preparedInit: function(){
			Ext.Ajax.defaultHeaders = {
				"Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
			};
			Ext.QuickTips.init();
			this.searchCondition = null;
		},
		
		init: function() {
			var _this = this;
			_this.preparedInit();
			
			// 项目列表面板
			var Item = Ext.data.Record.create([
				{name: 'project-name'},
				{name: 'project-desc'}
			]);
				
			var projectDs = new Ext.data.Store({
				proxy: new Ext.data.HttpProxy({
					url: _this.url
				}),
				baseParams: {
					operation: 'getAllProjects'
				},
				reader: new Ext.data.JsonReader({
					root: 'items',
					id: 'name'
				}, Item)
			});
			
			projectDs.load();
				
			var viewTpl = new Ext.XTemplate(
				'<tpl for=".">' +
				'<div id="{project-desc}" class="item-wrap" style="cursor:pointer;padding:1px;white-space:nowrap;">{project-desc}</div>' +
				'</tpl>');
				
			var projectsView = new Ext.DataView({
				id: 'projectsView',
	            tpl: viewTpl,
	            multiSelect: true,
	            store: projectDs,
	            itemSelector: 'div.item-wrap',
	            selectedClass: 'hilited',
				loadingText: '正在加载列表...',
	            emptyText: '<列表为空>'
	        });
			
			var projectsPanel = new Ext.Panel({
				title: '项目列表',
				region: 'west',
				split: true,
				autoScroll: true,
				width: 185,
				headerStyle: {
					'padding': '5px',
					'height': 'auto',
					'border-top': 'none',
					'border-left': 'none'
				},
				bodyStyle: {
					'border': 'none',
					'border-right': '1px solid #99BBE8'
				},
				items: projectsView
			});
			
			// 搜索结果面板
			function renderService(value, cellmeta, record, rowIndex, columnIndex, store){
				var imgSrc;
				Ext.each(unitCatalog, function(unit) {
					if (value == unit['zhName'])
						imgSrc = unit['imgSrc'];
				});
				var str = (!!imgSrc)? '<div><img class="innerImg" src="' + imgSrc + '"/><div class="innerDiv">' + value + '</div></div>' : value;
				return str;
			};
			
			var gridColumn = [
				{ 
					header: '服务类型',
				  	dataIndex: 'serviceType',
					align: 'center',
					width: 140,
					fixed: true,
					renderer: renderService
				}, {
					header: '服务单元',
					dataIndex: 'serviceDesc',
					align: 'center',
					width: 140
				},{ 
					header: '名称',
				  	dataIndex: 'name',
					align: 'center',
				  	width: 90
				},{ 
					header: '描述',
				  	dataIndex: 'desc',
					align: 'center',
				  	width: 90
				},{ 
					header: '结果类型',
				  	dataIndex: 'type',
					align: 'center',
				  	width: 60,
					fixed: true
				},{ 
				  	dataIndex: 'impLocation',
					sortable: false,
					hidden: true
				},{ 
				  	dataIndex: 'unitName',
					sortable: false,
					hidden: true
				},{ 
				  	dataIndex: 'projectName',
					sortable: false,
					hidden: true
				},{ 
				  	dataIndex: 'operaName',
					sortable: false,
					hidden: true
				},{ 
				  	dataIndex: 'operaDesc',
					sortable: false,
					hidden: true
				},{
					dataIndex: 'component',
					sortable: false,
					hidden: true
				},{
					dataIndex: 'projectDesc',
					sortable: false,
					hidden: true
				}
			];
			
			var searchGrid = new Ext.grid.GridPanel({
				id: 'searchGrid',
				title: '查询结果',
				stripeRows: true,
				modal: true,
				border: false,
				columnLines: true,
				frame: false,
				autoHeight: true,
				hidden: true,
				store: new Ext.data.Store({
					reader: new Ext.data.JsonReader({
						fields: [
							{name: 'type'},
							{name: 'name'},
							{name: 'desc'},
							{name: 'serviceType'},
							{name: 'serviceDesc'},
							{name: 'unitName'},
							{name: 'projectName'},
							{name: 'operaName'},
							{name: 'operaDesc'},
							{name: 'impLocation'},
							{name: 'component'},
							{name: 'projectDesc'}
						],
						root: 'items'
					})
				}),
				columns: gridColumn,
				viewConfig: {
					forceFit: true
				}
			});
			
			// 搜索结果表格行提示信息
			searchGrid.on('render', function(grid) {
				var view = grid.getView();
				searchGrid.tip = new Ext.ToolTip({
			        target: searchGrid.getView().mainBody,
			        delegate: '.x-grid3-row',
			        trackMouse: true,
			        renderTo: document.body,
			        listeners: {
			            beforeshow: function updateTipBody(tip) {  
			                tip.body.dom.innerHTML = "双击查看详细视图";  
			            }
			        }
			    });
			});
			
			// 双击打开具体信息
			searchGrid.on('rowdblclick', function(grid, rowIndex, columnIndex, e) {
				var record = grid.getStore().getAt(rowIndex);
				var serviceType = record.data['serviceType'];
				var name = record.data['name'].replace(/\{.*?\}/ig, ''); 
				var desc = record.data['desc'];
				var type = record.data['type'];
				var porjectName = record.data['projectName'];
				var unitName = record.data['unitName'];
				var unitDesc = record.data['serviceDesc'];
				unitDesc = encodeURIComponent(unitDesc);
				
				var impLocation = record.data['impLocation'];
				var operaName = record.data['operaName'];
				var operaDesc = record.data['operaDesc'];
				var component = record.data['component'];
				var importComp = record.data['importComp'];
				var projectDesc = record.data['projectDesc'];
				projectDesc = encodeURIComponent(projectDesc);
				var path, viewer;
				
				Ext.each(unitCatalog, function(unit) {
					if (serviceType == unit['zhName']) {
						viewer = unit['viewer'];
						path = porjectName + '/' + unit['name'] + '/' + unitName;
					}
				});
				
				switch(type) {
					case '项目':
						var desktop = Application.getDesktop();
						var win = desktop.getWindow('project_' + name);
						if (!win)
							win = desktop.createWindow({
				                id: 'project_'+ name,
				                title: desc,
				                width: 950,
                				height: 500,
								iconCls: 'bogus',
								shim:false,
				                animCollapse:false,
				                constrainHeader:true,
				                resizable: false,
				                maximizable: false,
								listeners: {
				                	render: function(sender){
										this.body.dom.contentWindow.onfocus = function(){ sender.toFront()};
										this.body.setVisible(true);
				                	}
				            	},
								bodyCfg: {
							        tag: 'iframe',
							        style: 'overflow:auto; display:none;',
							        frameBorder: 0,
							        src: '../explorer/project_viewer.jsp?project='+ name + '&projectDesc=' + projectDesc
								}
				            });
						win.show();
						break;
					case '服务':
						var title = desc ? desc+'['+ unitName + ']' : unitName;
						Application.createWindow(path, title, viewer + path + '&unitId=' + unitName + '&unitDesc=' + unitDesc + '&projectDesc=' + projectDesc);
						break;
					case '接口':		// 打开后显示第二个TAB
						var title = desc ? desc+'['+ name + ']' : name;
						Application.createWindow(path, title, viewer + path + '&activeTab=1' + '&unitId=' + unitName + '&unitDesc=' + unitDesc + '&projectDesc=' + projectDesc);
						break;
					default:
						var param = 
							(!!impLocation) ? porjectName + '/' + impLocation + name : path + '/' + name;
						var extra = '&unit=' + path + '&unitId=' + unitName + '&unitDesc=' + unitDesc + '&projectDesc=' + projectDesc;
						
						if (!impLocation) {
							if (component.indexOf('process') == -1) {
								Application.createWindow(param, name + '-' + desc, '../schema/encoding_viewer.jsp?schema=' + param + '.xsd&ref=' + extra, 'settings');
							}else {
								Application.serviceEditor.createWindow({
									id: param,
									text: name + '-' + desc,
									filePath: param,
									params: extra
								});
							}
						}
						break;
				};
			});
			
			// 服务查询面板
			var searchPanel = new Ext.Panel({
				region: 'center',
				id: 'searchPanel',
				header: false,
				autoScroll: true,
				layout: 'fit',
				bodyStyle: {
					'border-right': 'none',
					'border-bottom': 'none'
				},
				items: searchGrid,
				tbar: new Ext.Toolbar({
					height: 38,
					style: {
						'padding-top': '6px',
						'border-top':'none',
						'border-right':'none'
					},
					items: [
						'查询: ', ' ',
						new Ext.ux.SearchField({
							allowBlank: true,
							width: 320,
							emptyText: '<请输入要查询的关键词>',
							onTrigger2Click: function() {
								var datas = [];
								Ext.each(projectsView.getSelectedIndexes(), function(index){
									datas.push(projectsView.store.getAt(index).data);
								});
								
								var searchCondition = this.getValue();
								_this.search(searchCondition, datas);
							}
						}), {
							cls: 'x-btn-icon',
							icon: '../images/icons/advance.png',
							handler: function() {
								_this.showAdvance();
							}
						}, '-', '重建索引:', ' ',
						{
							cls: 'x-btn-icon',
							icon: '../images/icons/index.png',
							style: {
								'margin-top':'4px'
							},
							handler: function(){
								if (projectsView.getSelectedIndexes().length > 0) {
									var datas = [];
									Ext.each(projectsView.getSelectedIndexes(), function(index){
										datas.push(projectsView.store.getAt(index).data);
									});
									this.createIndex(datas);
								} else {
									Ext.Msg.alert('提示: ', '请先在项目列表中选择要创建索引的项目!');
								}
							},
							scope: this
						}
					]
				})
			});
			
			var serverPanel = new Ext.Panel({
				region: 'center',
				header: false,
				autoScroll: true,
				layout: 'border',
				bodyStyle: {
					'padding': '5px',
					'border': 'none'
				},
				items: searchPanel
			});
			
			// 主面板
			var mainPanel = new Ext.Panel({
				autoCreate: true,
				width: Ext.getBody().getWidth(),
				height: Ext.getBody().getHeight(),
				resizable: false,
				stateful: false,
				modal: true,
				frame: false,
				bodyStyle: {
					'border': 'none'
				},
				hideCollapseTool: true,
				header: false,
				layout: 'border',
				items: [projectsPanel, serverPanel]
			});
			
			var viewport = new Ext.Viewport({
				layout: 'fit',
				items: mainPanel
			});
		},
		
		// 搜索
		search : function(value, projects) {
			var _this = this;
			
			if (!value || value == 'null') {
				Ext.Msg.alert('提示:', '请先输入要搜索的关键词！');
				return;
			}
			
			var filter = (!!_this.searchCondition) ? '[' + Ext.encode(_this.searchCondition) + ']': 'null';
			
			Ext.getBody().mask('正在搜索...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST',
				url: _this.url,
				params: {
					operation: 'searchRecords',
					projects: Ext.encode(projects),
					filter: filter,
					value: Ext.encode(value)
				},
				callback: function(options, success, response) {
					Ext.getBody().unmask();
					if (!!success) {
						var rs = Ext.decode(response.responseText);
						// 处理返回结果
						var grid = Ext.getCmp('searchGrid');
						grid.store.removeAll();
						if (rs.items.length > 0) {
							Ext.each(rs.items, function(item){
								Ext.each(typeCatalog, function(catalog){
									if (item['type'] == catalog['name'])
										item['type'] = catalog['zhName'];
								});
								
								Ext.each(unitCatalog, function(unit) {
									if (item['serviceType'] == unit['name'])
										item['serviceType'] = unit['zhName'];
								});
								
								var rec = new Ext.data.Record(item);
								grid.store.add(rec);
							});
							grid.setVisible(true);
						} else {
							grid.setVisible(false);
							Ext.Msg.alert('提示:', '没有符合条件的记录！');
						}
					} else {
						_this.showException(response);
					}
				}
			});
		},
		
		// 创建索引
		createIndex : function(datas) {
			var _this = this;
			Ext.getBody().mask('正在创建索引...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST',
				url: _this.url,
				params: {
					operation: 'createIndex',
					data: Ext.encode(datas)
				},
				callback: function(options, success, response) {
					Ext.getBody().unmask();
					if (!!success) {
						Ext.getBody().mask('创建成功!', 'x-mask-loading');
						Ext.getBody().unmask.defer(1000, Ext.getBody());
					} else {
						_this.showException(response);
					}
				}
			});
		},
		
		// 高级选项
		showAdvance: function() {
			var _this = this;
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
					columns: [130, 130, 50],
					vertical: true,
					itemId: 'checkbox-group',
					style: { 'margin-top': '-4px!important'},
					name: 'checkbox-group',
					items: [
					{
						boxLabel: '项目',
						itemId: 'project',
						name: 'project',
						checked: (!!_this.searchCondition)?_this.searchCondition['project']:true,
						labelStyle: 'padding-top: 0px;'
					},{
						boxLabel: '服务',
						itemId: 'service',
						name: 'service',
						checked: (!!_this.searchCondition)?_this.searchCondition['service']:true,
						labelStyle: 'padding-top: 0px;'
					},{
						boxLabel: '接口',
						itemId: 'interface',
						name: 'interface',
						checked: (!!_this.searchCondition)?_this.searchCondition['portType']:true,
						labelStyle: 'padding-top: 0px;'
					},{
						boxLabel: '操作',
						itemId: 'operation',
						name: 'operation',
						checked: (!!_this.searchCondition)?_this.searchCondition['operation']:true,
						labelStyle: 'padding-top: 0px;'
					},{
						boxLabel: '消息',
						itemId: 'message',
						name: 'message',
						checked: (!!_this.searchCondition)?_this.searchCondition['message']:true,
						labelStyle: 'padding-top: 0px;'
					},{
						boxLabel: '元素',
						itemId: 'element',
						name: 'element',
						checked: (!!_this.searchCondition)?_this.searchCondition['element']:true,
						labelStyle: 'padding-top: 0px;'
					}]
				}]
			});
			
			var advanceWin = new Ext.Window({
				title: '自定义搜索条件',
				iconCls: 'advance',
				id: 'advanceWin',
				autoCreate: true,
				border : false,
		        resizable:false,
				constrain:true,
				constrainHeader:true,
		        minimizable:false, maximizable:false,
		        stateful:false,  modal:true,
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
						var selected = {};
						Ext.each(form.getForm().findField('checkbox-group').items.items, function(checkbox){
							if (!!checkbox.checked) {
								selected[checkbox.name] = checkbox.checked;
							}
						});
						_this.searchCondition = selected;
						advanceWin.close();
					}
				},{
			        text: '取消',
					handler: function(){
						this.searchCondition = null;
						advanceWin.close();
					}
				}]
			});
			
			advanceWin.show();
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

Ext.onReady(Viewer.init, Viewer, true);