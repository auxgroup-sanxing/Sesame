document.oncontextmenu = function(e) {return false;};
Ext.BLANK_IMAGE_URL='../images/s.gif';
Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

Ext.onReady(function(){
	var dataArray = [
        ['数据源设置', 'dataSource', 'dbserver', 'setting_viewer/images/datasource.png', '../console/datasource_viewer.jsp', 680, 505],
        ['JMS设置', 'jms', '', 'setting_viewer/images/JmsSetting.png', '../console/jms_viewer.jsp', 460, 260],
		['安全设置', 'sec', '', 'setting_viewer/images/SecuritySetting.png', '../console/security_viewer.jsp'],
		['集群管理', 'comclust', '', 'setting_viewer/images/cluster.png', '../console/cluster_viewer.jsp', 800, 500],
		['地址簿', 'address-book', '', 'setting_viewer/images/addressbook.png', '../console/address_book.jsp', 700, 480],
		['用户帐户', 'accounts', '', 'setting_viewer/images/users.png', '../user/UserHome.jsp'],
		['服务视图', 'serviceViewer', 'settings', 'setting_viewer/images/service_view.png', '../console/service_viewer.jsp'],
		['锁视图', 'lockViewer', '', 'setting_viewer/images/lock.png', '../console/locks_viewer.jsp',700,480],
		//['交易监控', 'trasactionViewer', '', 'setting_viewer/images/activity_monitor.png', '../console/transaction_viewer.jsp', 800, 500],
		['业务常量', 'busConstantViewer', '', 'setting_viewer/images/busconstant.png', '../console/busconstant_viewer.jsp'],
		['资源管理', 'sysResourceViewer', '', 'setting_viewer/images/resource.png', '../explorer/resource_explorer.jsp']
    ];
	
	var store = new Ext.data.ArrayStore({
        fields: ['name', 'dataIndex', 'iconCls', 'imgUrl', 'url'],
        idIndex: 1
    });
	store.loadData(dataArray, false);
	
    var tpl = new Ext.XTemplate(
		'<tpl for=".">',
            '<div class="thumb-wrap" id="{dataIndex}">',
		    '<div class="thumb"><img src="{imgUrl}" title="{name}"></div>',
		    '<span class="x-editable"><b>{name}</b></span></div>',
        '</tpl>',
        '<div class="x-clear"></div>'
	);
	
	var dataView = new Ext.DataView({
        store: store,
        tpl: tpl,
        multiSelect: true,
        overClass:'x-view-over',
        itemSelector:'div.thumb-wrap',
        
        listeners: {
			contextmenu: function(dataView, idx, node, e){
				var index = dataView.getSelectedIndexes();
				if (index.length <= 0) 
					return false;
				else {
					ctxMenu.showAt(e.getXY());
					e.stopEvent();
				}
			},
			
			dblclick : function(dataView, index, node, e) {
				var record = dataView.store.getAt(index);
				var data = record.data;
				
				var id = data.dataIndex;
				var url = data.url;
				var iconCls = data.iconCls;
				var title = node.textContent;
				var width = dataArray[index][5];
				var height = dataArray[index][6];
				if (id == 'accounts') {
					top.Application.showAccounts();
				}
				else {
					openEditor(id, url, title, iconCls, width, height);
				}
			}
        }
    });
	
	// 打开具体设置面板
	var openEditor = function(id, url, title, iconCls, width, height) {
		var desktop = top.Application.getDesktop();
        var win = desktop.getWindow(id);
        if(!win){
            win = desktop.createWindow({
                id: id,
                title: title,
                width:(!!width)?width: 680,
            	height:(!!height)?height: 420,
                iconCls: iconCls,
                shim:false,
                animCollapse:false,
                constrainHeader:true,
                resizable: true,
                maximizable: true,
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
			        src: url
				}
            });
        }
        win.show();
	};
	
	var ctxMenu = new Ext.menu.Menu({
		items: [{
			id: 'shortcutLink',
			text: '发送到桌面快捷方式',
			iconCls:'shortcutLink',
			handler: function(){
				var index = dataView.getSelectedIndexes();
				var rec = store.getAt(index);
				var data = rec.data;
				
				var id = data.dataIndex;
				var url = data.url;
				var iconCls = data.iconCls;
				var title = data.name;
				var width = dataArray[index][5];
				var height = dataArray[index][6];
				
				var elId = id + '-shortcut';
				var imgUrl = '../console/' + data.imgUrl;
				
				var innerHtmlStr = 
						'<a href="#">'+
							'<img style="float:right;" src="../images/obj16/shortcutink.png" />' +
							'<img width="48px" height="48px" src="' + imgUrl + '" /><br>' + 
							'<span>'+ title +'</span>' + 
						'</a>';
				
				// 生成桌面快捷方式
				var shortCuts = top.document.getElementById('x-shortcuts');
				
				var dtEls = top.document.getElementsByTagName('dt');
				var exist = false;
				Ext.each(dtEls, function(dtEl) {
					var id = dtEl.getAttribute('ID');
					if(id == elId) {
						exist = true;
					}
				});			
				if (!!exist) {
					alert('该快捷方式已经存在!');
					return;
				}
				
				var childrenLen = shortCuts.children.length;				
				var lastChild = shortCuts.children[childrenLen-1];
				
				var newEl = top.document.createElement('dt');
				newEl.setAttribute('ID', elId);
				newEl.setAttribute('unselectable', 'on');
				newEl.setAttribute('CLASS', 'noneFloat');
				newEl.innerHTML = innerHtmlStr;
				shortCuts.appendChild(newEl);
				
				var newModule = Ext.extend(Ext.app.Module, {
					id: id,
				    init : function(){
						this.launcher = {
							itemId: id,
				            text: title,
				            iconCls: iconCls,
				            handler: this.createWindow,
				            scope: this
				        }
				    },
					createWindow: function() {
						var app = Application || top.Application;
				        var desktop = Application.getDesktop();
        				var win = desktop.getWindow(id);
				        if(!win){
				            win = desktop.createWindow({
				                id: id, title: title,
								plain: true, border: false,
				                width:(!!width)?width: 680,
            					height:(!!height)?height: 420,
				                iconCls: iconCls,
				                shim: false, animCollapse:false, constrainHeader:true,
								html : '<iframe frameBorder="0" scrolling="no" style="width:100%;height:100%;overflow:auto;" src="'+ url +'"/>',
				            });
				        }
				        win.show();
					}
				});
 				Application.modules.push(new newModule());
			}
		}]
	});

    var panel = new Ext.Panel({
        id:'images-view',
        frame : true,
		hideCollapseTool : true,
        autoWidth : true,
        layout : 'fit',
        title : '双击打开子选项',
        items: dataView
    });
	
	viewport = new Ext.Viewport({
		layout : 'fit',
		items : panel
	});
});