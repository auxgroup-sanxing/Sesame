Ext.BLANK_IMAGE_URL='../images/s.gif';
document.oncontextmenu = function(e){
	return false;
};

var Viewer = function(){
//变量
var layout;

return {

	init : function(){
		var _this = this;

		Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
		Ext.QuickTips.init();
		
		// 属性面板
		var formPanel = new Ext.form.FormPanel({
			title: '基本属性',
		    labelWidth: 130,
		    frame: false,
		    bodyStyle:'padding:5px 5px 0; text-align:left; background-color:white;',
			autoScroll: true, border: false,
			style: Ext.isGecko?'margin: auto;':'',
			layout: 'form',
			defaults: {anchor:"-18", style:'margin-bottom:5px;'},
			items: [{
				xtype: 'panel',
				title: '组件标识',
				frame: true,
				layout: 'form',
				defaults: {anchor:"-18"},
				items: [{
		            fieldLabel : '组件名',
					name : 'name',
					xtype : 'textfield',
					allowBlank : false,
					readOnly : true
				},{
					fieldLabel : '描述',
					name : 'description',
					xtype : 'textfield',
					allowBlank : false
				},{
					fieldLabel : '组件类型',
					name : 'type',
					xtype: 'combo',
					width: 300,
					triggerAction: 'all',
					forceSelection:true, editable:false, disabled:true,
					store: [['binding-component', '绑定组件'], ['service-engine', '引擎组件']]
				}]
			},{
				xtype: 'editorgrid',
				title: '组件类',
				id: 'compGrid',
				autoHeight: true,
				frame: true, enableHdMenu: false,
				store: new Ext.data.Store({
					reader: new Ext.data.ArrayReader({
				        fields: [
				           {name: 'path'}
				        ],
				        root: 'component-classpath'
			        })
				}),
                columns: [
		            {header: "类路径", width: 60, dataIndex: 'path', editor: new Ext.grid.GridEditor(new Ext.form.TextField({})) }
		        ],
				autoExpandColumn: 'url',
				viewConfig: {
					forceFit: true
				},
				tbar: [
				'类名称: &nbsp;',
				{
					xtype: 'combo',
					id: 'component-classname',
					width: 300,
					triggerAction: 'all',
					forceSelection:false, editable:true,
					store: ['com.sanxing.sesame.binding.AdapterComponent']
				}],
				tools: [{
					id: 'plus',
					qtip: '添加类路径',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						var row = cell ? cell[0]+1 : panel.store.getCount();
						panel.store.insert(row, new panel.store.recordType({}));
		            	panel.startEditing(row, 0);
					}
				},{
					id: 'minus',
					qtip: '删除类路径',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						if (!cell || !window.confirm("确实要删除选中的类路径吗？")) return;
						panel.store.removeAt(cell[0]);
					}
				}]
			},{
				xtype: 'editorgrid',
				title: '引导类',
				id: 'bootGrid',
				autoHeight: true,
				frame: true, enableHdMenu: false,
				style: 'margin-bottom:5px;',
				store: new Ext.data.Store({
					reader: new Ext.data.ArrayReader({
				        fields: [
				           {name: 'path'}
				        ],
				        root: 'bootstrap-classpath'
			        })
				}),
                columns: [
		            {header: "类路径", width: 60, dataIndex: 'path', editor: new Ext.grid.GridEditor(new Ext.form.TextField({})) }
		        ],
				autoExpandColumn: 'url',
				viewConfig: {
					forceFit: true
				},
				tbar: [
				'类名称: &nbsp;',
				{
					xtype: 'combo',
					id: 'bootstrap-classname',
					width: 300,
					triggerAction: 'all',
					forceSelection:false, editable:true,
					store: ['com.sanxing.sesame.binding.BootstrapImpl']
				}],
				tools: [{
					id: 'plus',
					qtip: '添加类路径',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						var row = cell ? cell[0]+1 : panel.store.getCount();
						panel.store.insert(row, new panel.store.recordType({}));
		            	panel.startEditing(row, 0);
					}
				},{
					id: 'minus',
					qtip: '删除类路径',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						if (!cell || !window.confirm("确实要删除选中的类路径吗？")) return;
						panel.store.removeAt(cell[0]);
					}
				}]
			},{
				xtype: 'editorgrid',
				title: '共享类库',
				id: 'sharelibGrid',
				autoHeight: true,
				collapsible: true, collapseFirst: false,
				frame: true, enableHdMenu: false,
				style: 'margin-bottom:5px;',
				store: new Ext.data.Store({
					reader: new Ext.data.JsonReader({
				        fields: [
				           {name: 'lib-name', type: "string" }, {name: "version"}
				        ],
				        root: 'shared-library'
			        })
				}),
                columns: [
		            {
		            	header: "类库名称", width: 60, dataIndex: 'lib-name', 
		            	editor: new Ext.grid.GridEditor(new Ext.form.ComboBox({
							triggerAction: 'all',
							forceSelection:true, editable:false, 
							valueField: 'name', displayField: 'description',
						    store: new Ext.data.JsonStore({
						        url: "component_ctrl.jsp",
						    	root: 'items', 
						    	fields: [{name:'name', type:'string'}, {name:'description', type:'string'}, {name:'version'}],
						        baseParams: {operation:'getShareLibs' },
						        sortInfo: {field: 'description', direction: 'ASC'},
						        listeners: {
						        	'loadexception': function(proxy, obj, response){ _this.showException(response); },
									'select': function(sender, record, index){  }
						        }
						    })
		            	})) 
		            },
		            {
		            	header: "版本", width: 60, dataIndex: 'version', 
		            	editor: new Ext.grid.GridEditor(new Ext.form.TextField({}))
		            }
		        ],
				viewConfig: {
					forceFit: true
				},
				listeners: {
					render: function(c) {
						c.header.setStyle('cursor', 'default');
						c.header.on("dblclick", function(){ c.toggleCollapse(); });
					}
				},
				tools: [{
					id: 'plus',
					qtip: '添加共享库',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						var row = cell ? cell[0]+1 : panel.store.getCount();
						panel.store.insert(row, new panel.store.recordType({}));
		            	panel.startEditing(row, 0);
					}
				},{
					id: 'minus',
					qtip: '删除共享库',
					handler: function(e, toolEl, panel, tc){
						var cell = panel.getSelectionModel().getSelectedCell();
						if (!cell || !window.confirm("确实要删除选中的共享类库吗？")) return;
						panel.store.removeAt(cell[0]);
					}
				}]
			}],
			footerCssClass: 'x-window-bc',
			buttons: [{
		        text: '保存',
		        disabled: properties.readOnly,
				handler: function(){
					if (formPanel.getForm().isValid())
						_this.saveAttr(formPanel);
				}
			},{
		        text: '重置',
				handler: function(){
					_this.loadAttrib(formPanel);
				}
			}],
			listeners: {
				activate: function(c){
					if (!c.loaded) {
						_this.loadAttrib(c);
						c.loaded = true;
					}
				}
			}
		});
		
		// 文件上传面板
		var Item = Ext.data.Record.create([
			{name: 'displayName'},
			{name: 'fullName'},
			{name: 'size'},
			{name: 'lastModify'}
		]);
            
		var jarFileDataStore = new Ext.data.Store({
			proxy: new Ext.data.HttpProxy({url: 'adp_ctrl.jsp'}),
			baseParams: {
				operation: 'loadJarFileList',
				component: component
			},
			reader: new Ext.data.JsonReader({
				root: 'items',
				totalProperty: 'totalCount',
				id: 'name'
			}, Item),
			// 重写排序函数支持对文件大小的排序
			sortData: function(f, direction) {
				direction = direction || 'ASC';
				var st = this.fields.get(f).sortType;
				
				function getSize(value){
					var unit = value.replace(/\d+/ig, '');
					var size = parseInt(value.replace(new RegExp(unit, 'ig'), ''));
					switch (unit) {
						case 'B':
							return size;
						case 'KB':
							return size * 1024;
						case 'MB':
							return size * 1024 * 1024;
						default:
							return value;
					}
				};
				
				var fn = function(r1, r2, store){
					var v1 = st(r1.data[f]), v2 = st(r2.data[f]);
					var v1 = getSize(v1), v2 = getSize(v2);
					return v1 > v2 ? 1 : (v1 < v2 ? -1 : 0);
				};
				
				this.data.sort(direction, fn);
				if (this.snapshot && this.snapshot != this.data) {
					this.snapshot.sort(direction, fn);
				}
			}
		});
		
		var viewTpl = new Ext.XTemplate(
			'<tpl for=".">' +
		        '<div class="item-wrap" style="cursor:pointer;padding:1px;white-space:nowrap;">' +
					'<div>' +
						'<img src="../images/icons/jar.png">'+
						'<div class="fullNameWrap" >{fullName}</div>' +
						'<div class="sizeWarp" >{size}</div>' +
						'<div class="timeWarp" >{lastModify}</div>' +
					'</div>'+
					'<div class="displayNameWrap">{displayName}</div>' +
				'</div>' +
	        '</tpl>'
		);
		
		// 缩略图模式
		var jarView = new Ext.DataView({
			id: 'jarView',
            tpl: viewTpl,
            multiSelect: true,
            store: jarFileDataStore,
            itemSelector: 'div.item-wrap',
            selectedClass: 'hilited',
			loadingText: '正在加载列表...',
            emptyText: '<列表为空>',
			hideMode: 'offsets',
			hidden: true
        });
		
		// 表格(详细信息)模式
		var gridColumn = [
			{header: '名称', dataIndex: 'fullName', sortable: true},
			{header: '大小', dataIndex: 'size', width: 40, sortable: true},
			{header: '修改日期', dataIndex: 'lastModify', width: 60, sortable: true, sortType: 'asDate'}
		];
		
		var jarGrid = new Ext.grid.GridPanel({
			id: 'jarGrid',
			autoHeight: true,
			deferRowRender: true,
			stripeRows: true,
			columnLines: true,
			header: false,
			frame: false,
			columns: gridColumn,
			store: jarFileDataStore,
			style: 'margin-top: 10px;',
			viewConfig: {forceFit: true},
			hideMode: 'offsets'
		});
		
		var uploadPanel = new Ext.form.FormPanel({
			id: 'uploadPanel',
			title: '开发库文件管理',
			labelWidth: 130,
		    frame: false,
		    bodyStyle:'padding:5px 5px 0; text-align:left; background-color:white;',
			autoScroll:true, border: false,
			style: Ext.isGecko?'margin: auto;':'',
			layout: 'form',
			defaults: {anchor:"-18", style:'margin-bottom:5px;'},
			items: [jarView, jarGrid],
			tbar:[
			{
				id: 'shortcut_icon',
	            text:'缩略图',
				cls: 'x-btn-text-icon',
				icon: '../images/obj16/picture.png',
				handler: function() {
					this.leftTopBarhandler(uploadPanel, jarFileDataStore);
				},
				scope: this
	        },{
				id: 'detail_icon',
	            text:'详细信息',
				cls: 'x-btn-text-icon',
				icon: '../images/obj16/outline_co.gif',
				handler: function() {
					this.leftTopBarhandler(uploadPanel, jarFileDataStore);
				},
				scope: this
	        },'->', {
				id: 'add_icon',
	            text:'添加',
				cls: 'x-btn-text-icon',
				icon: '../images/tool16/newfile_wiz.gif',
				handler: function() {
					this.uploadJarFile(jarFileDataStore);
				},
				scope: this
	        },{
				id: 'remove_icon',
	            text:'删除',
				cls: 'x-btn-text-icon',
				disabled: true,
				icon: '../images/icons/remove.gif',
				handler: function() {
					this.removeJarFile(jarView, jarGrid, jarFileDataStore);
				},
				scope: this
	        },{
				id: 'refresh_icon',
	            text:'刷新',
				cls: 'x-btn-text-icon',
				icon: '../images/tool16/refresh.gif',
				handler: function() {
					jarFileDataStore.reload();
				}
	        }],
			listeners: {
				activate: function(formPanel){
					if (!formPanel.loaded) {
						jarFileDataStore.load();
						formPanel.loaded = true;
					}
				}
			}
		});
		
		var viewport = new Ext.Viewport({
			stateful: false,
			layout: 'fit',
			items: [
				new Ext.TabPanel({
					activeTab: 0,
					border: true,
					bodyStyle: 'text-align:left;',
					deferredRender: false,
					items: [formPanel, uploadPanel]
				})
			]
		});
		
		// 事件注册
		// 选中文件后将"删除按钮"置为可点击状态
		jarView.on('selectionchange', function(view, selections) {
			var btnmap = uploadPanel.getTopToolbar().items.map;
			btnmap['remove_icon'].setDisabled(selections.length < 1);
		});
		
		jarGrid.getSelectionModel().on('rowselect', function(sm, rowIdx, r) {
           var btnmap = uploadPanel.getTopToolbar().items.map;
		   var selections = jarGrid.getSelectionModel().getSelections();
		   btnmap['remove_icon'].setDisabled(selections.length < 1);
        });
	},
	
	saveAttr : function(formPanel){
		var _this = this;

		var v = formPanel.getForm().getValues();
		var properties = window.properties;
		properties.name = v.name;
		properties.description = v.description;
		properties['component-classname'] = Ext.getCmp('component-classname').el.dom.value;
		properties['bootstrap-classname'] = Ext.getCmp('bootstrap-classname').el.dom.value;
		
		properties['component-classpath'] = [];
		Ext.getCmp('compGrid').store.each(function(rec){
			properties['component-classpath'].push(rec.get('path'));
		});
		properties['bootstrap-classpath'] = [];
		Ext.getCmp('bootGrid').store.each(function(rec){
			properties['bootstrap-classpath'].push(rec.get('path'));
		});
		properties['shared-library'] = [];
		Ext.getCmp('sharelibGrid').store.each(function(rec){
			properties['shared-library'].push([rec.get('lib-name'), rec.get('version')]);
		});

		Ext.getBody().mask('正在保存组件...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'POST', 
			url: 'component_ctrl.jsp',
			params: {operation:"save", component:component, data: Ext.encode(properties) },
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (!success) {
					_this.showException(response);
				}
			}
		});
	},
	
	loadAttrib : function(formPanel){
		formPanel.getForm().setValues(properties);
		Ext.getCmp('component-classname').setValue(properties['component-classname']);
		Ext.getCmp('bootstrap-classname').setValue(properties['bootstrap-classname']);
		if (!!properties['component-classpath'])
			Ext.getCmp('compGrid').store.loadData(properties);
		if (!!properties['bootstrap-classpath'])	
			Ext.getCmp('bootGrid').store.loadData(properties);
	},
	
	leftTopBarhandler: function(uploadPanel, jarFileDataStore) {
		jarFileDataStore.reload();
		uploadPanel.getTopToolbar().items.map['remove_icon'].setDisabled(true);
		if (Ext.getCmp('jarView').isVisible()) {
			Ext.getCmp('jarView').setVisible(false);
			Ext.getCmp('jarGrid').setVisible(true);
		}else if (Ext.getCmp('jarGrid').isVisible()) {
			Ext.getCmp('jarView').setVisible(true);
			Ext.getCmp('jarGrid').setVisible(false);
		}
	},
	
	uploadJarFile: function(jarFileDataStore) {
		var uploadDialog = new Ext.ux.UploadDialog.Dialog({
			title: 'JAR文件上传',
			permitted_extensions: ['jar', 'Jar', 'JAR'],
			url: 'adp_ctrl.jsp',
			base_params: {
				component: component
			}
		});
		
		uploadDialog.show();
		uploadDialog.on( 'uploadsuccess', function() {
			jarFileDataStore.reload();
		});
		
		uploadDialog.on('uploadcomplete' , function(){
			window.setTimeout(function(){
				uploadDialog.hide();
			}, 1500);
		});
	},
	
	removeJarFile: function(view, grid, ds) {
		var _this = this;
		if (!window.confirm("确实要删除选中的文件吗？")) 
            return;
		
        var records = [];
		if (view.isVisible()) {
			Ext.each(view.getSelectedIndexes(), function(index){
				records.push(ds.getAt(index));
			});
		} else if (grid.isVisible()) {
			Ext.each(grid.getSelectionModel().getSelections(), function(record){
				records.push(record);
			});
		}
		
		var datas = [];
        Ext.each(records, function(record){
			datas.push(record.data);
            ds.remove(record);
        });
		
		Ext.getBody().mask('正在删除文件...', 'x-mask-loading');
        Ext.Ajax.request({
			method: 'POST',
			url: 'adp_ctrl.jsp',
			params: {
				operation: 'deletJarFiles',
				component: component,
				data: Ext.encode(datas)
			},
			callback: function(options, success, response){
				Ext.getBody().unmask();
				if (success) {
					Ext.getBody().mask('成功删除!', 'x-mask-loading');
					Ext.getBody().unmask.defer(1000, Ext.getBody());
					Ext.getCmp('uploadPanel').getTopToolbar().items.map['remove_icon'].setDisabled(true);
				} else {
					_this.showException(response);
				}
			}
		});
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