/*!
 * Ext JS Library 3.0.3
 * Copyright(c) 2006-2009 Ext JS, LLC
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
Ext.Desktop = function(app){
	this.taskbar = new Ext.ux.TaskBar(app);
	var taskbar = this.taskbar;
	
	var desktopEl = Ext.get('x-desktop');
    var taskbarEl = Ext.get('ux-taskbar');
    var shortcuts = Ext.get('x-shortcuts');

    var windows = new Ext.WindowGroup();
    var activeWindow;
	
	this.getActiveWindow = function() {
		return activeWindow;
	}
		
    function minimizeWin(win){
        win.minimized = true;
        win.hide();
    }

    function markActive(win){
        if(activeWindow && activeWindow != win){
            markInactive(activeWindow);
        }
        taskbar.setActiveButton(win.taskButton);
        activeWindow = win;
        Ext.fly(win.taskButton.el).addClass('active-win');
        win.minimized = false;
    }

    function markInactive(win){
        if(win == activeWindow){
            activeWindow = null;
            Ext.fly(win.taskButton.el).removeClass('active-win');
        }
    }

    function removeWin(win){
    	taskbar.removeTaskButton(win.taskButton);
        layout();
    }

    function layout(){
        desktopEl.setHeight(Ext.lib.Dom.getViewHeight()-taskbarEl.getHeight());
    }
    Ext.EventManager.onWindowResize(layout);

    this.layout = layout;

    this.createWindow = function(config, cls){
    	var win = new (cls||Ext.Window)(
            Ext.applyIf(config||{}, {
                manager: windows,
                minimizable: true,
                maximizable: true
            })
        );
        win.render(desktopEl);
        win.taskButton = taskbar.addTaskButton(win);

        win.cmenu = new Ext.menu.Menu({
            items: []
        });

        win.animateTarget = win.taskButton.el;
        
        win.on({
        	'activate': {
        		fn: markActive
        	},
        	'beforeshow': {
        		fn: markActive
        	},
        	'deactivate': {
        		fn: markInactive
        	},
        	'minimize': {
        		fn: minimizeWin
        	},
        	'close': {
        		fn: removeWin
        	}
        });
        
        layout();
        return win;
    };

    this.getManager = function(){
        return windows;
    };

    this.getWindow = function(id){
        return windows.get(id);
    }
    
    this.getWinWidth = function(){
		var width = Ext.lib.Dom.getViewWidth();
		return width < 200 ? 200 : width;
	}
		
	this.getWinHeight = function(){
		var height = (Ext.lib.Dom.getViewHeight()-taskbarEl.getHeight());
		return height < 100 ? 100 : height;
	}
		
	this.getWinX = function(width){
		return (Ext.lib.Dom.getViewWidth() - width) / 2
	}
		
	this.getWinY = function(height){
		return (Ext.lib.Dom.getViewHeight()-taskbarEl.getHeight() - height) / 2;
	}

    layout();

    if(shortcuts){
		var dragElement = null;
		var mouseMove = false;
		var mouseY,mouseX,objY,objX;
		shortcuts.on('mousedown', function(e, t) {
			if (t = e.getTarget('dt', shortcuts)) {
				e.stopEvent();
				dragElement = document.getElementById(t.id);
				dragElement.style.position = 'relative';
				mouseX = 
					parseInt(e.browserEvent.clientX) + (document.documentElement.scrollLeft || document.body.scrollLeft);
				mouseY = 
					parseInt(e.browserEvent.clientY) + (document.documentElement.scrollTop || document.body.scrollTop);
					
				//记录元素的当前坐标
				objY = parseInt(getNodeStyle(dragElement, 'top'));
				objX = parseInt(getNodeStyle(dragElement, 'left'));
				mouseMove = false;
			}
		});
		
		function getNodeStyle(node, styleName){
			var realStyle = null;
			if(node.currentStyle){
				realStyle = node.currentStyle[styleName];
			}else if(window.getComputedStyle){
				realStyle = window.getComputedStyle(node,null)[styleName];
			}
			
			return realStyle;
		};
		
		shortcuts.on('click', function(e, t){
			if (!!mouseMove)
				return;
            if(t = e.getTarget('dt', shortcuts)){
                e.stopEvent();
				var moduleID = t.id.replace('-shortcut', '');
                var module = app.getModule(moduleID);
                if (module) {
					module.createWindow();
				} else {
					var startConfig	= app.getStartConfig();
					switch (moduleID) {
						case 'prefs-win':
							startConfig.toolItems[0].handler.call(app);
							break;
						case 'logger-win':
							startConfig.toolItems[1].handler.call(app);
							break;	
						default:
							break;
					}
				}
            }
        });
		
		shortcuts.on('mousemove', function(e, t) {
			move(e, t);
		});
		
		Ext.fly(document).on('mousemove', function(e, t) {
			move(e, document);
		});
		
		function move(e, t) {
			e.stopEvent();
			if (dragElement) {
				var x, y;
				//y等于鼠标当前y - 记录的鼠标y + 元素y,后面的x一样
				y = parseInt(e.browserEvent.clientY) 
					+ (document.documentElement.scrollTop || document.body.scrollTop) 
					- mouseY + objY;
					
				x = parseInt(e.browserEvent.clientX) 
					+ (document.documentElement.scrollLeft || document.body.scrollLeft) 
					- mouseX + objX;
					
				//更新元素的实际显示
				dragElement.style.top = y + 'px';
				dragElement.style.left = x + 'px';
				
				//更新鼠标坐标和元素坐标的记录
				objY = y;
				objX = x;
				mouseX = parseInt(e.browserEvent.clientX) 
						+ (document.documentElement.scrollLeft || document.body.scrollLeft);
				mouseY = parseInt(e.browserEvent.clientY) 
						+ (document.documentElement.scrollTop || document.body.scrollTop);
				mouseMove = true;	
			}
		}
		
		shortcuts.on('mouseup', function(e, t) {
			dragElement = null;
		});
		
		var ctxMenu = new Ext.menu.Menu({
			items: [{
				id: 'delShortCut',
				text: '删除快捷方式',
				handler: function(){
					var el = ctxMenu.selection;
					el.parentNode.removeChild(el);
					el = null;
				}
			}]
		});
		
		shortcuts.on('contextmenu', function(e, t){
            if(t = e.getTarget('dt', shortcuts)){
				e.stopEvent();
				if (t.innerHTML.indexOf('shortcutink.png') != -1) {
					ctxMenu.selection = t;
					ctxMenu.showAt(e.getXY());
				} else {
					return false;
				}
            }
        });
    }
};
