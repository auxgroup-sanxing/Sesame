Ext.BLANK_IMAGE_URL='../images/s.gif';

Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
// desktop configuration
Application = new Ext.app.App({
	init : function(){
		Ext.QuickTips.init();
		// 事件注册
		this.initEventListeners();
		
		this.serviceEditor = new Modules.ServiceModule();
		this.serviceEditor.app = this;
		this.logger = new Modules.Logger();
		this.logger.app = this;
	},
	
	initEventListeners: function() {
		// 鼠标右键
		this.addCtxMenu();
	},
	
	addCtxMenu: function() {
		var ctxMenu = new Ext.menu.Menu({
			items: [
			{
				id: 'wallpaperSetting',
				text: '主题设置',
				iconCls:'wallpaperSetting',
				handler: function(){
					this.showWallpaperView();
				},
				scope: this
			},'-', {
				id: 'showDesktop',
				text: '显示桌面',
				iconCls:'showDesktop',
				handler: function(){
					new Modules.ShowDesktop().showDesktop();
				},
				scope: this
			},'-', {
				id: 'logout',
				text: '注销',
				iconCls:'logout',
				handler: function(){
					if (window.confirm('您确定要退出系统吗?')==true) 
						window.location='../logout.jsp?action=relogin';
				}
			}]
		});
		
		Ext.getBody().on('contextmenu', function(e){
			ctxMenu.showAt(e.getXY());
			e.stopEvent();
		});
	},
	
	showWallpaperView: function(){
		if (!!Ext.fly('wallpaper'))
			return;
			
		var viewTpl = new Ext.XTemplate(
		 	'<tpl for=".">' +
	 			'<div class="itemWrap">' +
	 				'<img src="{path}"/>' +
	 			'</div>' +
		 	'</tpl>');
			
		var Item = [{name: 'path'}];
		var store = new Ext.data.Store({
			reader: new Ext.data.JsonReader({
				fields: Item,
				root: 'items'
			})
		});
		store.loadData({
			items: wallDatas.items
		});
		
		var wallpaperView = new Ext.DataView({
			id: 'wallpaperView',
			tpl: viewTpl,
			multiSelect: true,
			anchor: -10,
			store: store,
			itemSelector: 'div.itemWrap',
			selectedClass: 'hilited',
			loadingText: '正在加载列表...',
			emptyText: '<主题列表为空>',
			listeners: {
				click: function(item){
					var index = wallpaperView.getSelectedIndexes();
					var rec = store.getAt(index);
					var path = rec.data.path;
					Ext.getBody().setStyle('backgroundImage', 'url("' + path + '")');
				}
			}
		});
		
		var panel = new Ext.Panel({
			header: false,
			layout: 'fit',
			border: false,
			items: wallpaperView
		});
		
		var win = new Ext.Window({
			id: 'wallpaper',
			title: '主题设置',
			width: 680,
			height: 460,
			shim: false,
			animCollapse: false,
			constrainHeader: true,
			maximizable: false,
			resizable: false,
			layout: 'fit',
			items: panel,
			footerCssClass: 'x-window-bc',
			buttons: [
			{
				text: '使用默认主题',
				handler: function() {
					Ext.getBody().setStyle('backgroundImage', 'url("wallpapers/default.jpg")');
					win.close();
				}
			},{
				text: '关闭',
				handler: function() {
					win.close();
				}
			}]
		});
		win.show();
	},

	getModules : function(){
		return [
			new Modules.ShowDesktop(),
			new Modules.DeployWizard(),
            new Modules.ProjectExplorer(),
            this.projectsMenu = new Modules.ProjectsMenu(),
            new Modules.WareExplorer(),
			new Modules.DatabaseExplorer()
		];
	},

    // config for the start menu
    getStartConfig : function(){
    	var _this = this;
        return {
            title: '未登录',
            iconCls: 'user',
            toolItems: [{
                text:'控制面板',
                iconCls:'settings',
				handler: function(){ 
					this.createWindow('prefs-win', '控制面板', '../console/setting_viewer.jsp'); 
				},
                scope: this
            },
			{
                text:'查看日志',
                iconCls:'settings',
				handler: function(){ this.logger.createWindow(); },
                scope: this
            },
			'-',
			{
                text:'注销',
                iconCls:'logout',
                handler: function(){ 
					if (window.confirm('您确定要退出系统吗?')==true) window.location='../logout.jsp?action=relogin';
				},
                scope:this
            }],
            listeners: {
            	show: function(panel){
					Ext.Ajax.request({
						method: 'GET', 
						url: '../LoginAction', 
						timeout: 30 * 1000,
						callback: function(options, success, response) {
							if (success) {
								currentUser = Ext.decode(response.responseText);
								panel.setTitle(Ext.isEmpty(currentUser) ? '未登录' : currentUser.userid);
							}
							else {
								_this.showException(response);
							}
						}
					});
            	}
            }
        };
    },
	
	debug: function(obj, showMethods, sort){
		var div = Ext.DomHelper.append("statusDiv", {
			tag: 'div', html: '<b>'+obj+'</b>', style:'border-bottom:1px solid gray; margin:0px 3px 5px 3px;'
		});
		showMethods = (showMethods != false);
		sort = (sort != false);
		if (obj == null || obj.constructor == null) {
			return true;
		}
		var type = typeof(obj);
		if (type)
		{
			if (type == "string" || type == "number") {
				div.innerHTML = type+': '+obj;
				return true;
			}
			if (showMethods && !sort) 
			{
			}
			else 
			{
				var propNames = [];
				if (showMethods) {
					for (var prop in obj) {
						propNames.push(prop);
					}
				}
				else 
				{
					for (var prop in obj) {
						if (typeof obj[prop] != "function") 
						{
							propNames.push(prop);
						}
						else 
						{
							//Ext.DomHelper.append(div, {tag:"div", html: '<font color=blue>'+prop+'</font>: Method'});
						}
					}
				}
				if (sort) {
					propNames.sort();
				}
				Ext.each(propNames, function (prop) {
					Ext.DomHelper.append(div, {tag:"div", html: '<font color=blue>'+prop+'</font>: '+obj[prop]});
				});
				return true;
			}
		}
		for (elem in obj)
		{
			Ext.DomHelper.append(div, {tag:"div", html: '<font color=blue>'+elem+'</font>: '+obj[elem]});
		}
	},
	
    createWindow : function(id, title, url, icon){
        this.createWindowWithCfg(id, title, url, icon, 640, 480);
	},
	
	createWindowWithCfg: function(id, title, url, icon, width, height) {
		var desktop = this.getDesktop();
        var win = desktop.getWindow(id);
        if(!win){
            win = desktop.createWindow({
                id: id,
                title: title,
                width:width,
                height:height,
                iconCls: icon||'tabs',
                shim: true,
                animCollapse:false,
                border:false,
                constrainHeader:true,
                listeners: {
                	render: function(sender){
						this.body.dom.contentWindow.onfocus = function(){ sender.toFront()};
						this.body.setVisible(true);
                	},
            		beforeclose: function(sender){
            			var iframe = this.body;
            			if (iframe.dom.contentWindow.onbeforeclose) {
            				return iframe.dom.contentWindow.onbeforeclose();
            			}
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
	},
	
    closeWindow : function(id){
        var desktop = this.getDesktop();
        var win = desktop.getWindow(id);
        if(win){
            win.close();
        }
    },
	
    findWindow : function(id){
        var desktop = this.getDesktop();
        return desktop.getWindow(id);
    },

    /**
     * 打开流程引擎服务单元的一个操作
     * @param {} src
     */
    openProcess : function(src){
        var desktop = this.getDesktop();
        var win = desktop.getWindow(src.id);
        if(!win){
			win = desktop.createWindow({
                id: src.id,
                title: src.text,
                width:740,
                height:480,
                iconCls: 'settings',
                shim: true,
                animCollapse: false,
                constrainHeader: true,
				maximized: true,
				plain: true,
				layout: 'fit',
                listeners: {
                	afterrender: function(sender){
						this.doLayout();
                	},
            		beforeclose: function(sender){
						var tabPanel = this.getComponent(0);
						for (var i=0,len=tabPanel.items.length; i<len; i++) {
							var tab = tabPanel.items.get(i);
							if (tab && tab.rendered) {
		            			var iframe = tab.body;
		            			if (iframe && iframe.dom.contentWindow.onbeforeclose) {
		            				var r = iframe.dom.contentWindow.onbeforeclose();
								
		            				if (r==false) {
		            					tabPanel.setActiveTab(tab);
		            					return false;
		            				}
		            			}
	            			}
						}
            		}
            	},
				items: new Ext.TabPanel({
			        activeItem: src.activeItem || 0,
			        border: false,
					tabPosition: 'bottom',
					deferredRender: true,
					items: [
						{
							title: '消息',
							border: false,
							xtype: 'panel',
							bodyCfg: {
						        tag: 'iframe',
						        style: 'overflow:auto; display:block;',
						        frameBorder: 0,
						        scrolling: 'no',
						        src: '../schema/encoding_viewer.jsp?schema='+src.pathname+'.xsd' + (src.params||'')
							}
						},{
							title: '流程',
							border: false, autoShow: true,
							xtype: 'panel',
							bodyCfg: {
						        tag: 'iframe',
						        style: 'overflow:auto; display:block;',
						        frameBorder: 0,
						        src: "../diagram/diagram_viewer.jsp?service="+src.pathname+".xml"
							}
						}, {
							title: '业务常量',
							border: false, autoShow: true,
							xtype: 'panel',
							bodyCfg: {
						        tag: 'iframe',
						        style: 'overflow:auto; display:block;',
						        frameBorder: 0,
						        src: "../console/busconstant_viewer.jsp?unit=" + src.unitId + "&operation=" + src.text.split('-')[0]
							}
						}]
			    })
            });
        }
        win.show();
    },
	
	/**
	 * 打开公共接口
	 * @param var
	 * @param {} src 
	 */
	openPublicIntf: function(config) {
		var desktop = this.getDesktop();
        var win = desktop.getWindow(config.id);
        if(!win){
			win = desktop.createWindow({
                id: config.id,
                title: config.text,
                width:740,
                height:480,
                iconCls: 'settings',
                shim: true,
                animCollapse: false,
                constrainHeader: true,
				maximized: true,
				plain: true,
				layout: 'fit',
                listeners: {
                	afterrender: function(sender){
						this.doLayout();
                	},
            		beforeclose: function(sender){
						var tabPanel = this.getComponent(0);
						for (var i=0,len=tabPanel.items.length; i<len; i++) {
							var tab = tabPanel.items.get(i);
							if (tab && tab.rendered) {
		            			var iframe = tab.body;
		            			if (iframe && iframe.dom.contentWindow.onbeforeclose) {
		            				var r = iframe.dom.contentWindow.onbeforeclose();
								
		            				if (r==false) {
		            					tabPanel.setActiveTab(tab);
		            					return false;
		            				}
		            			}
	            			}
						}
            		}
            	},
				items: new Ext.TabPanel({
			        activeItem: 0,
			        border: false,
					tabPosition: 'bottom',
					deferredRender: true,
					items: [
						{
							title: '消息',
							border: false,
							xtype: 'panel',
							bodyCfg: {
						        tag: 'iframe',
						        style: 'overflow:auto; display:block;',
						        frameBorder: 0,
						        scrolling: 'no',
						        src: config.src
							}
						},
						{
							title: '流程',
							border: false, autoShow: true,
							xtype: 'panel',
							bodyCfg: {
						        tag: 'iframe',
						        style: 'overflow:auto; display:block;',
						        frameBorder: 0,
						        src: "../diagram/diagram_viewer.jsp?service=" + config.pathname + ".xml&isPublic=true" 
							}
						}]
			    })
            });
        }
        win.show();
	},
    
    /**
     * 打开窗口
     * @param {} id
     * @param {} title
     * @param {} url
     * @param {} icon
     * @param {} options
     */
	openWindow: function(id, title, url, icon, options) {
		var desktop = this.getDesktop();
        var win = desktop.getWindow(id);
        if(!win){
        	if (typeof(options) != 'object') {
        		options = { width: 600, height: 480 };
        	}
            win = desktop.createWindow(Ext.apply(options, {
                id: id,
                title: title,
                iconCls: icon||'tabs',
                shim: true,
                animCollapse:false,
                border:false,
                constrainHeader:true,
                listeners: {
                	render: function(sender){
						this.body.dom.contentWindow.onfocus = function(){ sender.toFront()};
						this.body.setVisible(true);
                	},
            		beforeclose: function(sender){
            			var iframe = this.body;
            			if (iframe.dom.contentWindow.onbeforeclose) {
            				return iframe.dom.contentWindow.onbeforeclose();
            			}
            		}
            	},
				bodyCfg: {
			        tag: 'iframe',
			        style: 'overflow:auto; display:none;',
			        frameBorder: 0,
			        src: url
				}
            }));
        }
        win.show();
	},
	
	showAccounts : function(){
		var desktop = this.getDesktop();
        var win = desktop.getWindow("accounts");
		if (!win) {
			win = desktop.createWindow({
				id: "accounts", 
				title: "用户帐户", 
                iconCls: 'group',
                shim: true, autoShow: false, autoScroll: true,
                constrainHeader: true,
                width: 680, height: 420,
				tbar: new Ext.Toolbar(),
				bodyCfg: {
			        tag: 'iframe',
			        frameBorder: 0,
			        border: false,
			        scrolling: 'no',
			        style: 'overflow: auto; display:none; border:0px; background-color: window;',
			        src: "../user/UserHome.jsp"
				},
				listeners: {
            		beforeclose: function(sender){
            			var iframe = this.body;
            			if (iframe.dom.contentWindow.onbeforeclose) {
            				return iframe.dom.contentWindow.onbeforeclose();
            			}
            		},
                	render: function(sender){
						this.body.dom.contentWindow.onfocus = function(){ sender.toFront()};
						this.body.setVisible(true);
                	},
					afterrender: function(_this){
						var btns = this.getTopToolbar().items.map;
						_this.body.dom.onload = function() {
							var src = this.src.replace(/\.\.\//g, '');
							var location = this.contentWindow.location.toString();
							var isHome = (location.indexOf(src) + src.length == location.length);
							btns.home.setDisabled(isHome);
							btns.back.setDisabled(isHome);
						};
					}
				}
			});
			var tb = win.getTopToolbar();
            tb.add(
				{
					id: 'back',
					text: '上一步',
	                cls: 'x-btn-text-icon',
					icon: '../images/icons/left-a.gif',
					disabled: true,
					handler: function(){ win.body.dom.contentWindow.history.back(-1);}
				},
				{
	                id: 'home',
					text: '主页',
	                cls: 'x-btn-text-icon',
					icon: '../images/icons/user_suit.png',
					disabled: true,
	                handler: function(){ win.body.dom.src="../user/UserHome.jsp";}
	            }
			);
			win.render();
		}
        win.show();
	},
		
	showException : function(response){
		var statusText = response.responseText;
		if (!statusText){
			switch (response.status) 
			{
				case -1:
					alert('通讯超时，事务终止');  return;
				case 0:
					alert('通讯错误，连接服务器失败');   return;
				default:
					alert (response.statusText+'('+response.status+')');  return;
			}
		}

		Ext.MessageBox.show({
			title: '错误提示',
			msg: '<div style="width:100%; height:auto; background-color:white; overflow:auto;">'+statusText+'</div>',
			buttons: Ext.MessageBox.OK,
			width: 492
		});
	}
});

/**
 * 模块
 */
Modules = {

ServiceModule : Ext.extend(Ext.app.Module, {
    id: 'service-win',
    init : function(){
        this.launcher = {
            text: '服务操作编辑器',
            iconCls:'settings',
            handler : this.createWindow,
            scope: this
        };
    },

    createWindow : function(src){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow(src.id);
		var params = (!src.params) ? '': src.params;
        if(!win){
			win = desktop.createWindow({
                id: src.id,
                title: src.text,
                width:740,
                height:480,
                iconCls: 'settings',
                shim: true,
                animCollapse:false,
                constrainHeader:true,
				maximized: true,
				plain: true,
				layout: 'fit',
                listeners: {
            		beforeclose: function(sender){
						var tabPanel = this.getComponent(0);
						for (var i=0,len=tabPanel.items.length; i<len; i++) {
							var tab = tabPanel.items.get(i);
							if (tab && tab.rendered) {
		            			var iframe = tab.body;
		            			if (iframe && iframe.dom.contentWindow.onbeforeclose) {
		            				var r = iframe.dom.contentWindow.onbeforeclose();
								
		            				if (r==false) {
		            					tabPanel.setActiveTab(tab);
		            					return false;
		            				}
		            			}
	            			}
						}
            		}
            	},
				items: new Ext.TabPanel({
			        activeItem: (!!src.activeItem)? src.activeItem : 0,
			        border:false,
					tabPosition: 'bottom',
					deferredRender: true,
					items: [
						{
							title: '消息',
							border: false,
							xtype: 'panel',
							bodyCfg: {
						        tag: 'iframe',
						        style: 'overflow:auto; display:block;',
						        frameBorder: 0,
						        scrolling: 'no',
						        src: '../schema/encoding_viewer.jsp?schema='+src.filePath+'.xsd' + params
							}
						},
						{
							title: '流程',
							border: false, autoShow: true,
							xtype: 'panel',
							bodyCfg: {
						        tag: 'iframe',
						        style: 'overflow:auto; display:block;',
						        frameBorder: 0,
						        src: "../diagram/diagram_viewer.jsp?service="+src.filePath+".xml"
							}
						}]
			    })
            });
        }
		win.doLayout(true);	// 加载后重新渲染一次窗体防止底部Tab页显示不出
        win.show();
    }
}),

// 日志
Logger : Ext.extend(Ext.app.Module, {
    id:'logger-win',
    init : function(){
        this.launcher = {
            text: '系统日志',
            iconCls:'icon-grid',
            handler : this.createWindow,
            scope: this
        }
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow(this.id);
		
        if(!win){
            win = desktop.createWindow({
                id: this.id,
                title:'系统日志',
                width:800,
                height:480,
                iconCls: 'icon-grid',
                shim:false,
                animCollapse:false,
                constrainHeader:true,
				autoScroll: true,
                tbar:[{
                    tooltip:'清除',
					cls: 'x-btn-icon',
                    icon:'../images/tool16/clear_co.gif',
					handler: function(){ Ext.get('statusDiv').update(''); }
                },{
                    tooltip:'滚屏锁定',
					cls: 'x-btn-icon',
                    icon:'../images/tool16/lock_co.gif',
                    enableToggle: true,
                    ref: '../scrollLock',
					handler: function(){  }
                }],
				bodyStyle: 'background-color:black;color:white;font:13px arial,sans-serif;',
                contentEl: 'statusDiv',
                scrollToBottom : function() {
					this.body.dom.scrollTop = this.body.dom.scrollHeight;
				},
				listeners: {
					'beforeclose': function(window){
						Ext.get('inactive-container').appendChild(Ext.get('statusDiv'));
					},
					'close': function(window){
						var provider = Ext.Direct.getProvider('logger-poll-provider');
						if (!!provider) {
							provider.disconnect();
						}
					}
				}

            });
        }
		win.show();
		
		var pollingProvider = new Ext.direct.LongPollingProvider({
			id: 'logger-poll-provider',
			url: '../ConsoleMonitor',
			interval: 5
		});
		
		Ext.Direct.addProvider(pollingProvider);
		Ext.Direct.window = win;
		Ext.Direct.purgeListeners();
		Ext.Direct.on('message', function(e){
			Ext.each(e.data, function(data){
				var statusEl = Ext.get('statusDiv');
				if (statusEl.dom.children.length > 5000) {
					statusEl.first().remove();
				}
				var divEl = document.createElement('div');
				divEl.innerHTML = "<pre>" + data + "</pre>";
				statusEl.appendChild(divEl);
				if (!Ext.Direct.window.scrollLock.pressed) {
					Ext.Direct.window.scrollToBottom();
				}
			});
		});
    }
}),

ProjectExplorer : Ext.extend(Ext.app.Module, {
	id: 'project-explorer',
	
    init : function(){
        this.launcher = {
			itemId: 'project-explorer',
            text: '项目浏览器',
            iconCls: 'tabs',
            handler: this.createWindow,
            scope: this
        };
    },
	
    createWindow : function(src){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow(this.id);
        if(!win){
            win = desktop.createWindow({
                id: this.id,
                title: '项目浏览器' || src.text,
                width: 900,
                height: 500,
				html : '<iframe frameBorder="0" scrolling="no" style="width:100%; height:100%; overflow:auto;" src="../explorer/project_explorer.jsp" />',
                iconCls: 'tabs',
                shim:false,
                animCollapse:false,
                constrainHeader:true
            });
        }
        win.show();
    }
}),

ProjectsMenu : Ext.extend(Ext.app.Module, {
    init : function(){
        var _this = this;
		Ext.each(projects, function(project){
			project.handler = _this.createWindow;
	        project.scope = _this;
			project.resizable = false;
			project.maximizable = false;
		});
		
        this.launcher = {
        	itemId: 'all-projects',
            text: '所有项目',
            iconCls: 'bogus',
            handler: function(){return false;},
            menu: {
                items: projects
            }
        }
    },

    createWindow : function(src){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('project_'+src.id);
        if(!win){
            win = desktop.createWindow({
                id: 'project_'+src.id,
                title: src.text,
                width: 950,
                height: 500,
                iconCls: 'bogus',
                shim:false,
                animCollapse:false,
                constrainHeader:true,
                resizable: src.resizable,
                maximizable: false,
                listeners: {
                	render: function(sender){
						this.body.dom.contentWindow.onfocus = function(){ sender.toFront()};
						this.body.setVisible(true);
                	}
            	},
            	tools: [{
            		id: 'refresh',
            		qtip: '刷新',
            		handler: function(e, toolEl, panel, tc){
            			panel.body.dom.contentWindow.location.reload();
            		}
            	}],
				bodyCfg: {
			        tag: 'iframe',
			        style: 'overflow:auto; display:none;',
			        frameBorder: 0,
			        src: '../explorer/project_viewer.jsp?project='+src.id + '&projectDesc=' + encodeURIComponent(src.text)
				}
            });
        }
        win.show();
    }
}),

DatabaseExplorer : Ext.extend(Ext.app.Module, {
	menus : [{
         id: 'setconnection',
		 iconCls: 'dbserver',
		 text: '数据库连接设置',
		 url : '../explorer/dbconnection_viewer.jsp'
    }],
				 
	init : function(){
        var _this = this;
		Ext.each(_this.menus, function(menu){
			menu.handler = _this.createWindow;
	        menu.scope = _this;
			menu.resizable = true;
			menu.maximizable = true;
		});
		
        this.launcher = {
            text: '数据库浏览器',
            iconCls: 'dbexpl',
            handler: function(){return false;},
            menu: {
                items: _this.menus
            }
        }
    },
	
	createWindow : function(src){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow("sql" + src.id);
        if(!win){
            win = desktop.createWindow({
                id: "sql" + src.id,
                title: src.text,
                width:880,
            	height:500,
                iconCls: src.iconCls,
                shim:false,
                animCollapse:false,
                constrainHeader:true,
                resizable: src.resizable,
                maximizable: src.maximizable,
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
			        src: src.url
				}
            });
        }
        win.show();
    }
}),

// 部署向导
DeployWizard : Ext.extend(Ext.app.Module, {
	id: 'deploy-wizard',
	
    init : function(){
        this.launcher = {
			itemId: 'deploy-wizard',
            text: '部署向导',
            iconCls: 'deploy',
            handler: this.createWindow,
            scope: this
        }
    },

    createWindow : function(src){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow(this.id);
        if(!win){
            win = desktop.createWindow({
                id: this.id,
                title: '部署向导' || src.text,
				plain: true,
				border: false,
                width: 640,
                height: 480,
				html : '<iframe frameBorder="0" scrolling="no" style="width:100%; height:100%; overflow:auto;" src="../explorer/deploy_wizard.jsp" />',
                iconCls: 'deploy',
                shim: false,
                animCollapse:false,
                constrainHeader:true
            });
        }
        win.show();
    }
}),

WareExplorer : Ext.extend(Ext.app.Module, {
	id: 'ware-explorer',
	
    init : function(){
        this.launcher = {
			itemId: 'ware-explorer',
            text: '公共资源库',
            iconCls: 'tabs',
            handler: this.createWindow,
            scope: this
        }
    },

    createWindow : function(src){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow(this.id);
        if(!win){
            win = desktop.createWindow({
                id: this.id,
                title: '公共资源库' || src.text,
                width: 950,
                height: 500,
                iconCls: 'tabs',
                shim:false,
                animCollapse:false,
                constrainHeader:true,
                maximizable: false,
                resizable: false,
                listeners: {
                	render: function(sender){
						this.body.dom.contentWindow.onfocus = function(){ sender.toFront()};
						this.body.setVisible(true);
                	}
            	},
            	tools: [{
            		id: 'refresh',
            		qtip: '刷新',
            		handler: function(e, toolEl, panel, tc){
            			panel.body.dom.contentWindow.location.reload();
            		}
            	}],
				bodyCfg: {
			        tag: 'iframe',
			        style: 'overflow:auto; display:none;',
			        frameBorder: 0,
			        scrolling: "no",
			        src: '../explorer/ware_explorer.jsp'
				}
            });
        }
        win.show();
    }
}),

// 返回桌面
ShowDesktop: Ext.extend(Ext.app.Module, {
	id: 'showDesktop',
	init : function(){
        this.launcher = {
			itemId: 'showDesktop',
            text: '显示桌面',
            iconCls: 'showDesktop',
            handler: this.showDesktop,
            scope: this
        }
    },
	
	showDesktop: function() {
		var openedWins = Ext.getCmp('TaskBarButtons').getAllBtns();
		if (openedWins.length > 0) {
			Ext.each(openedWins, function(obj) {
				obj.win.minimize();
			});
		}
	}
})
//end of modules
};