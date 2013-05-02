document.oncontextmenu = function(e) {return false;};
Ext.BLANK_IMAGE_URL='../images/s.gif';
Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

var Viewer = function() {
	return {
		url: 'sharelib_ctrl.jsp',
		
		preparedInit: function() {
			Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
			Ext.QuickTips.init();
		},
		
		init: function() {
			var _this = this;
			_this.preparedInit();
			
			// 库文件查看面板
			var Item = Ext.data.Record.create([
				{name: 'displayName'},
				{name: 'fullName'},
				{name: 'size'},
				{name: 'lastModify'}
			]);
	            
			var jarFileDataStore = new Ext.data.Store({
				proxy: new Ext.data.HttpProxy({url: _this.url}),
				baseParams: {
					operation: 'loadJarFileList',
					path: path
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
							'<img src="../images/obj32/jar.png">'+
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
				columns: gridColumn,
				store: jarFileDataStore,
				style: 'margin-top: 10px;',
				viewConfig: {forceFit: true},
				hideMode: 'offsets'
			});
			
			var libPanel = new Ext.form.FormPanel({
				id: 'uploadPanel',
				labelWidth: 130,
			    frame: false,
			    bodyStyle:'padding:5px 5px 0; text-align:left; background-color:white;',
				autoScroll:true, 
				border: true,
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
						this.leftTopBarhandler(libPanel, jarFileDataStore);
					},
					scope: this
		        },{
					id: 'detail_icon',
		            text:'详细信息',
					cls: 'x-btn-text-icon',
					icon: '../images/obj16/outline_co.gif',
					handler: function() {
						this.leftTopBarhandler(libPanel, jarFileDataStore);
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
					afterLayout: function(formPanel){
						if (!formPanel.loaded) {
							jarFileDataStore.load();
							formPanel.loaded = true;
						}
					}
				}
			});
			
			var viewport = new Ext.Viewport({
				layout : 'fit',
				items: libPanel
			});
			
			jarView.on('selectionchange', function(view, selections) {
				var btnmap = libPanel.getTopToolbar().items.map;
				btnmap['remove_icon'].setDisabled(selections.length < 1);
			});
			
			jarGrid.getSelectionModel().on('rowselect', function(sm, rowIdx, r) {
	           var btnmap = libPanel.getTopToolbar().items.map;
			   var selections = jarGrid.getSelectionModel().getSelections();
			   btnmap['remove_icon'].setDisabled(selections.length < 1);
	        });
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
			var _this = this;
			var uploadDialog = new Ext.ux.UploadDialog.Dialog({
				title: '添加JAR文件',
				permitted_extensions: ['jar', 'Jar', 'JAR'],
				url: _this.url,
				base_params: {
					path: path
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
				url: _this.url,
				params: {
					operation: 'deletJarFiles',
					path: path,
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
	}
}();
Ext.onReady(Viewer.init, Viewer, true);