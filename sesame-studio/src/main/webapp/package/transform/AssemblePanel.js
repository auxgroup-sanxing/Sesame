Ext.ux.AssemblePanel = Ext.extend(Ext.Panel, {

title: '转换',

border: false, 

layout:'border', 

//环境变量
env: [],

//扩展转换函数
extension: [],

//创建XML重组面板
constructor : function(config){
	var _this = this;
	if ((typeof config)!='object') {
		config = {};
	}
	//目标XML树
	var targetTree = new Ext.tree.TreePanel({
		title: '目标',
		region: 'center',
		itemId: 'target-tree',
		border: false, autoScroll: true, animate: false,
		enableDD: true, hlDrop: false,
		//ddGroup: 'statenet-transform-dnd',
		root: new Ext.tree.AsyncTreeNode({
			text: '目标元素',
			id: 'ROOT',
			iconCls: 'icon-element',
			//uiProvider: Ext.extend(Ext.ux.ElementNodeUI, { getChildIndent: function(){return "";} }),
			allowDrag: false,
			expanded: false
		}),
		rootVisible: true,
		dropConfig: this.getDropConfig(),
		listeners: {
			append: function(){
				this.modified = true;
			},
			movenode: function(){
				this.modified = true;
			},
			remove: function(){
				//this.lastOverNode = null;
				this.modified = true;
			},
			textchange: function(){
				this.modified = true;
			},
			beforecollapsenode: function(node, deep, anim) {
				if (node.attributes.collapsible==false && node.firstChild) 
					return false;
			},
			nodedragover: function(e){
				if (e.target == e.target.getOwnerTree().getRootNode()) {
					return false;
				}
			},
			beforenodedrop: function(e){
				if (e.dropNode.getOwnerTree() == e.tree) {
				}
				else {
					e.dropStatus = true;
					var a = e.dropNode.attributes;
					var rootPath = e.dropNode.getOwnerTree().getRootNode().getPath('name');
					var node = new Ext.tree.AsyncTreeNode({
						text: e.dropNode.getPath('name').replace(rootPath+'/', ''),
						leaf: true,
						iconCls: a.iconCls,
						uiProvider: Ext.ux.ParamNodeUI
					});
					e.dropNode = node;
				}
			},
			nodedrop: function(e){
				if (e.point=='append') e.target.expand();
			}
		},
        tools: [{
        	id: 'refresh',
        	qtip: '刷新',
        	handler: function(e, toolEl, panel, tc){
	    		var root = panel.getRootNode();
	    		root.reload();
        	}
        }],
		keys:[{
			key:  Ext.EventObject.ENTER,
			fn: function(){ targetTree.editNode(); }
		},{
			key: Ext.EventObject.DELETE,
			fn: function(){ targetTree.removeNode(); }
		}],
		editNode: function() {
			var node = this.getSelectionModel().getSelectedNode();
    		if (node && node.attributes.uiProvider==Ext.ux.ParamNodeUI) {
    			targetTree.editor.triggerEdit(node);
    		}
    		else {
    			alert('不可编辑');
    		}
    	},
		removeNode: function() {
			var node = this.getSelectionModel().getSelectedNode();
    		if (node && node!=node.getOwnerTree().getRootNode()) {
    			if (node.attributes.uiProvider==Ext.ux.ElementNodeUI) {
    				while (node.firstChild) {
    					try {node.firstChild.remove(true); } catch(e) {}
    				}
    			}
    			else {
    				try {	node.remove(true);	} catch(e) {}
    			}
    		}
		}
	});
	//目标树编辑器
	targetTree.editor = new Ext.tree.TreeEditor(
		targetTree,
		{
			allowBlank:false,
			listeners: {
				focus: function(f) {
					var val = f.getValue();
					if (val && val.indexOf("'")==0) {
						f.selectText(1, val.length-1);
					}
					else {
						f.selectText();
					}
				}
			}
		},{
			editDelay: 100,
			cancelOnEsc: true,
			completeOnEnter: true,
			ignoreNoChange: true,
			listeners: {
				beforestartedit : function(treeEditor, boundEl, value) {
					var node = treeEditor.editNode;
					var oldValue = node.text;
					if (!node.isLeaf()) {
						return false;
					}
					else {
						var c = treeEditor.field;
						if (node.attributes.type) {
							if (node.attributes.type=='variable') {
								return false;
							}
							if (node.attributes.type=='string') {
								c.regex = /^'.*'$/;  c.regexText = '非法的字符串';
							}
							else if (node.attributes.type=='int') {
								c.regex = /^[0-9]{1,10}$/;  c.regexText = '非法的整数';
							}
							else if (node.attributes.type=='float') {
								c.regex = /^[0-9]{1,10}(\.[0-9]{1,10})?$/;  c.regexText = '非法的实数';
							}
							else {
								c.regex = null;
							}
						} 
						else {
							c.regex = null;
						}
					}

					if (!node.parentNode) {
						return false;
					}
					return true;
				},
				complete : function(editor, value, startValue){
				}
			}
	});
	//源XML树
	var sourceTree = new Ext.tree.TreePanel({
		region: 'east',
		title: '源变量',
		itemId: 'source-tree',
		border: false, containerScroll: true, autoScroll: true, 
		animate: false, trackMouseOver: false,
		width: 200, split: true,
		cls: 'rtl-tree',
		enableDrag: true,
		ddGroup: 'statenet-transform-dnd',
		loader: new Ext.tree.TreeLoader({
			dataUrl: 'diagram_ctrl.jsp',
			baseParams: {
				operation: 'getDomChildren',
				service: servicePath
			},
			listeners: {
				beforeload: function(loader, node){
					var a = node.attributes;
					
					loader.baseParams.message = a.name;
					if (a.schema) {
						loader.baseParams.schema = a.schema;
						loader.baseParams.message = a.ref;
					}
					else {
						delete loader.baseParams.schema;
					}
				},
				load: function(loader, node, response){
					node.attributes['namespace'] = response.getResponseHeader('namespace');
				},
				loadexception: function(loader, node, response){
					Viewer.showException(response);
				}
			},
			preloadChildren: true,
			clearOnLoad: false
		}),
		rootVisible: false,
		root: new Ext.tree.TreeNode({
			text: 'context',
			id: 'ROOT',
			name: 'context',
			iconCls: 'icon-element',
			allowDrag: false,
			expanded: false
		}),
        tools: [{
        	id: 'refresh',
        	qtip: '刷新',
        	handler: function(e, toolEl, panel, tc){
	    		var root = panel.getRootNode();
	    		root.eachChild(function(child){
	    			child.reload();
	    		});
        	}
        }]
	});

	var insertConst = function(item, e) {
		var node = targetTree.getSelectionModel().getSelectedNode();
		if (!node) return;
		
		var paramNode = new Ext.tree.AsyncTreeNode({
			text: item.itemId=='string' ? "''" : item.itemId=='int' ? '0' : '0.0',
			collapsible: false, 
			uiProvider: Ext.ux.ParamNodeUI,
			type: item.itemId,
			leaf: true
		});
		if (node.attributes.uiProvider==Ext.ux.ElementNodeUI) {
			node.appendChild(paramNode);
		}
		else if (node.attributes.uiProvider==Ext.ux.FuncNodeUI) {
			node.appendChild(paramNode);
		}
		else {
			node.parentNode.insertBefore(paramNode, node);
		}
	};

	var tbar = new Ext.Toolbar({
		disabled: true,
	    items: [{
	    	itemId: 'clear',
	    	tooltip: '清除',
			icon:"../images/tool16/clear_co.gif",
	    	handler: function(){
	    		targetTree.removeNode();
	    	}
	    },{
	    	itemId: 'edit',
	    	tooltip: '编辑',
			icon:"../images/tool16/rename.png",
	    	handler: function(){
	    		targetTree.editNode();
	    	}
	    },
	    '-',
	    {
	    	icon: '../images/obj16/function_obj.gif',
	    	text: 'concat',
	    	itemId: 'concat',
	    	tooltip: 'concat(string,string,...)',
	    	handler: function(item, e) {
				var btn = this.ownerCt.get('std-func');
				btn.handler(this, e);
	    	}
	    },{
	    	icon: '../images/obj16/function_obj.gif',
	    	text: 'substring',
	    	itemId: 'substring',
	    	tooltip: 'substring(string,start[,len]), 下标从1开始',
	    	handler: function(item, e) {
				var btn = this.ownerCt.get('std-func');
				btn.handler(this, e);
	    	}
	    },{
	    	itemId: 'std-func',
	    	text: '标准函数',
	    	xtype: 'splitbutton',
	    	pulldown: true,
	    	handler: function(item, e) {
	    		var node = targetTree.getSelectionModel().getSelectedNode();
	    		if (!node) return;

	    		if (item==this) {
	    			if (this.pulldown) {
		    			this.showMenu();
		    			return;
	    			}
	    		}
	    		else if (item.getXType()=='menuitem') {
	    			this.setText(item.itemId);
	    			this.setTooltip(item.text);
	    			this.pulldown = false;
	    		}
	    		
    			var funcNode = new Ext.tree.AsyncTreeNode({
    				iconCls: 'icon-any',
    				text: item==this ? this.text : item.itemId,
    				collapsible: false, 
    				uiProvider: Ext.ux.FuncNodeUI,
    				children: []
    			});
    			if (node.attributes.uiProvider==Ext.ux.ElementNodeUI) {
	    			node.insertBefore(funcNode, node.firstChild);
	    			while (funcNode.nextSibling) {
	    				var movedNode = funcNode.nextSibling.remove();
	    				funcNode.appendChild(movedNode);
	    				movedNode.renderIndent();
	    			}
    			}
    			else {
    				node.parentNode.insertBefore(funcNode, node);
    				funcNode.appendChild(node.remove());
    				if (node.rendered) node.ui.renderIndent();
    			}
    			funcNode.expand();
	    	},
	    	menu: new Ext.menu.Menu({
	    		items: [{
	    			itemId: 'replace',
	    			icon: '../images/obj16/function_obj.gif',
	    			text: '替换 replace(string,pattern,replace)'
	    		},{
	    			itemId: 'matches',
	    			icon: '../images/obj16/function_obj.gif',
	    			text: '匹配 matches(string,pattern)'
	    		},{
	    			itemId: 'contains',
	    			icon: '../images/obj16/function_obj.gif',
	    			text: '包含 contains(string1,string2)'
	    		},
	    		'-',
	    		{
	    			itemId: 'number',
	    			icon: '../images/obj16/function_obj.gif',
	    			text: '数值 number(arg)'
	    		},{
	    			itemId: 'round',
	    			icon: '../images/obj16/function_obj.gif',
	    			text: '数值 round(num)'
	    		},
	    		'-',
	    		{
	    			itemId: 'max',
	    			icon: '../images/obj16/function_obj.gif',
	    			text: '最大值 max((arg,arg,...))'
	    		},{
	    			itemId: 'min',
	    			icon: '../images/obj16/function_obj.gif',
	    			text: '最小值 min((arg,arg,...))'
	    		}],
	    		listeners: {
	    			click: function(menu, item, e) {
			    		var btn = tbar.get('std-func');
			    		btn.handler(item, e);
	    			}
	    		}
	    	})
	    },{
	    	itemId: 'ext-func',
	    	text: '扩展函数',
	    	xtype: 'splitbutton',
	    	pulldown: true,
	    	handler: function(item, e) {
	    		var node = targetTree.getSelectionModel().getSelectedNode();
	    		if (!node || item.disabled) return;

	    		if (item==this) {
	    			if (this.pulldown) {
		    			this.showMenu();
		    			return;
	    			}
	    		}
	    		else if (item.getXType()=='menuitem') {
	    			this.setText(item.parentMenu.prefix+":"+item.itemId);
	    			this.setTooltip(item.text);
	    			this.pulldown = false;
	    		}
	    		
    			var funcNode = new Ext.tree.AsyncTreeNode({
    				iconCls: 'icon-any',
    				text: item==this ? this.text : item.parentMenu.prefix+":"+item.itemId,
    				collapsible: false, 
    				uiProvider: Ext.ux.FuncNodeUI,
    				children: []
    			});
    			if (node.attributes.uiProvider==Ext.ux.ElementNodeUI) {
	    			node.insertBefore(funcNode, node.firstChild);
	    			while (funcNode.nextSibling) {
	    				var movedNode = funcNode.nextSibling.remove();
	    				funcNode.appendChild(movedNode);
	    				movedNode.renderIndent();
	    			}
    			}
    			else {
    				node.parentNode.insertBefore(funcNode, node);
    				funcNode.appendChild(node.remove());
    				if (node.rendered) node.ui.renderIndent();
    			}
    			funcNode.expand();
	    	},
	    	menu: new Ext.menu.Menu({
	    		enableScrolling: true,
	    		listeners: {
		    		beforerender: function(menu) {
		    			if (typeof(_this.extension)!='object' || _this.extension.length==0) {
				    		menu.addMenuItem({text: '(空)', disabled: true});
				    		return;
		    			}
		    			for (var i=0; i<_this.extension.length; i++) {
		    				var group = _this.extension[i];
			    			menu.addMenuItem({
			    				text: group.description+'('+group.prefix+')',
			    				icon: '../images/obj16/activity_category.gif',
			    				handler: function(item, e){ return false; },
			    				menu: new Ext.menu.Menu({
			    					prefix: group.prefix, 
			    					items: group.items,
			    					listeners: {
						    			click: function(menu, item, e) {
								    		var btn = tbar.get('ext-func');
								    		btn.handler(item, e);
						    			}
			    					}
			    				})
			    			});
		    			}
	    			}
	    			
	    		}
	    	})
	    },
	    '-',
	    {
			itemId: 'string',
			text: '字符串',
			handler: insertConst
		},{
			itemId: 'int',
			text: '整数',
			handler: insertConst
		},{
			itemId: 'float',
			text: '实数',
			handler: insertConst
	    },
	    '-',
	    {
	    	itemId: 'var',
	    	text: '变量',
	    	menu: new Ext.menu.Menu({
	    		enableScrolling: true,
	    		showSeparator: false,
	    		listeners: {
		    		beforerender: function(menu) {
		    			if (typeof(_this.env)!='object' || _this.env.length==0) {
				    		menu.addMenuItem({text: '(空)', disabled: true});
				    		return;
		    			}
		    			for (var i=0; i<_this.env.length; i++) {
		    				var itemId = '$'+_this.env[i];
			    			menu.addMenuItem({itemId: itemId, text: itemId, icon: '../images/obj16/envvar_obj.gif'});
		    			}
	    			},
	    			click: function(menu, item, e) {
			    		var node = targetTree.getSelectionModel().getSelectedNode();
			    		if (!node || item.disabled) return;
			    		
		    			var paramNode = new Ext.tree.AsyncTreeNode({
		    				text: item.itemId,
		    				collapsible: false, 
		    				uiProvider: Ext.ux.ParamNodeUI,
		    				type: 'variable',
		    				leaf: true
		    			});
		    			if (node.attributes.uiProvider==Ext.ux.ElementNodeUI) {
			    			node.insertBefore(paramNode, node.firstChild);
		    			}
		    			else {
		    				node.parentNode.insertBefore(paramNode, node);
		    			}
	    			}
	    		}
	    	})
	    },
	    '->',
	    {
	    	text: '重载',
	    	hidden: true,
	    	handler: function(){
	    		//_this.generateXSLT(targetTree.getRootNode());
	    	}
	    }]
	});
	
	targetTree.getSelectionModel().on("selectionchange", function(model, node){
		tbar.setDisabled(node==null || node.parentNode==null);
	});
	
	Ext.ux.AssemblePanel.superclass.constructor.call(this, Ext.apply(config, {items: [targetTree, sourceTree], tbar: tbar}));
},

getDropConfig : function(){
	return {
		ddGroup: 'statenet-transform-dnd',
		
		getDropPoint : function(e, n, dd){
	        var tn = n.node;
	        if(tn.isRoot){
	            return tn.allowChildren !== false ? "append" : false; // always append for root
	        }
	        var dragEl = n.ddel;
	        var l = Ext.lib.Dom.getX(dragEl), r = l + dragEl.offsetWidth;
	        var y = Ext.lib.Event.getPageX(e);
	        var noAppend = tn.allowChildren === false || tn.isLeaf();
	        if(this.appendOnly || tn.parentNode.allowChildren === false){
	            return noAppend ? false : "append";
	        }
	        var noBelow = false;
	        if(!this.allowParentInsert){
	            noBelow = tn.hasChildNodes() && tn.isExpanded();
	        }
	        
			if (n.node.attributes.uiProvider==Ext.ux.ElementNodeUI) {
				return "append";
			}

			var q = (r - l) / (noAppend ? 2 : 3);
	        if(y >= l && y < (l + q)){
	            return "above";
	        }else if(!noBelow && (noAppend || y >= r-q && y <= r)){
	            return "below";
	        }else{
	            return "append";
	        }
		},
		onNodeOver: function(n, dd, e, data){
			var pt = this.getDropPoint(e, n, dd);
			var node = n.node;
			
			// auto node expand check
			if (!this.expandProcId && pt == "append" && node.hasChildNodes() && !n.node.isExpanded()) {
				this.queueExpand(node);
			}
			else if (pt != "append") {
				this.cancelExpand();
			}
			
			// set the insert point style on the target node
			var returnCls = this.dropNotAllowed;
			if (this.isValidDropPoint(n, pt, dd, e, data)) {
				if (pt) {
					var el = n.ddel;
					var cls;
					if (pt == "above") {
						returnCls = n.node.isFirst() ? "x-tree-drop-ok-above" : "x-tree-drop-ok-between";
						cls = "x-tree-drag-insert-above";
					}
					else if (pt == "below") {
						returnCls = n.node.isLast() ? "x-tree-drop-ok-below" : "x-tree-drop-ok-between";
						cls = "x-tree-drag-insert-below";
					}
					else {
						returnCls = "x-tree-drop-ok-append";
						cls = "x-tree-drag-append";
					}
					
					if (this.lastInsertClass != cls) {
						Ext.fly(el).replaceClass(this.lastInsertClass, cls);
						this.lastInsertClass = cls;
					}
				}
			}
			return returnCls;
		},
		removeDropIndicators: function(n){
			if (n && n.ddel) {
				var el = n.ddel;
				Ext.fly(el).removeClass(["x-tree-drag-insert-above", "x-tree-drag-insert-below", "x-tree-drag-append"]);
				this.lastInsertClass = "_noclass";
			}
		}
	};
}

});



Ext.ux.ElementNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
    // private
    expand : function(){
        this.updateExpandIcon();
        this.ctNode.style.display = "inline";
    },
    // private
    focus: Ext.emptyFn, // prevent odd scrolling behavior
    // private
    getDDHandles : function(){
        return  [this.iconNode, this.textNode, this.elNode];
    },
    // private
    getChildIndent : function(n) {
    	if (!n)
			return Ext.ux.ElementNodeUI.superclass.getChildIndent.apply(this, arguments);
    	else if (n.isFirst())
    		return ' = ';
    	else
    		return ' + ';
    },
    // private
    addClass : function(cls){
        if(this.elNode){
            Ext.fly(this.textNode).addClass(cls);
        }
    },
    // private
    removeClass : function(cls){
        if(this.elNode){
            Ext.fly(this.textNode).removeClass(cls);  
        }
    },
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';

        var cb = Ext.isBoolean(a.checked),
            nel,
            href = a.href ? a.href : Ext.isGecko ? "" : "#",
            buf = ['<li class="x-tree-node"><span ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<span class="x-tree-node-indent">',this.indentMarkup,"</span>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" />',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on" />',
            cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></span>",
            '<span class="x-tree-node-ct" style="display:none;"></span>',
            "</li>"].join('');

        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1];
        this.iconNode = cs[2];
        var index = 3;
        if(cb){
            this.checkbox = cs[3];
            // fix for IE6
            this.checkbox.defaultChecked = this.checkbox.checked;
            index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
    }
    
});


Ext.ux.FuncNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
    focus: Ext.emptyFn, // prevent odd scrolling behavior
    // private
    getDDHandles : function(){
        return  [this.iconNode, this.textNode, this.elNode];
    },
    // private
    getChildIndent : function(n) {
    	if (!n)
    		return '';
    	else if (n.isFirst())
    		return '';
    	else
    		return ' , ';
    },
    // private
    addClass : function(cls){
        if(this.elNode){
            Ext.fly(this.textNode).addClass(cls);
        }
    },
    // private
    removeClass : function(cls){
        if(this.elNode){
            Ext.fly(this.textNode).removeClass(cls);  
        }
    },
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent(n) : '';

        var cb = Ext.isBoolean(a.checked),
            nel,
            href = a.href ? a.href : Ext.isGecko ? "" : "#",
            buf = ['<span class="x-tree-node"><span ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<span class="x-tree-node-indent">',this.indentMarkup,"</span>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:none;" />',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on" />',
            cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></span>",
            '(<span class="x-tree-node-ct" style="display:none;"></span>)',
            "</span>"].join('');

        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[2];
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1];
        this.iconNode = cs[2];
        var index = 3;
        if(cb){
            this.checkbox = cs[3];
            // fix for IE6
            this.checkbox.defaultChecked = this.checkbox.checked;
            index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
    },
    // private
    renderIndent : function(){
        if(this.rendered){
            var indent = "",
                p = this.node.parentNode;
            if(p){
                indent = p.ui.getChildIndent(this.node);
            }
            if(this.indentMarkup != indent){ // don't rerender if not required
                this.indentNode.innerHTML = indent;
                this.indentMarkup = indent;
            }
            this.updateExpandIcon();
        }
    }
    
});

Ext.ux.ParamNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
    focus: Ext.emptyFn, // prevent odd scrolling behavior
    // private
    getDDHandles : function(){
        return  [this.iconNode, this.textNode, this.elNode];
    },
    // private
    addClass : function(cls){
        if(this.elNode){
            Ext.fly(this.textNode).addClass(cls);
        }
    },
    // private
    removeClass : function(cls){
        if(this.elNode){
            Ext.fly(this.textNode).removeClass(cls);  
        }
    },
    
    // private
    onClick : function(e){
		Ext.ux.ParamNodeUI.superclass.onClick.apply(this, arguments);
    },
    
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent(n) : '';

        var cb = Ext.isBoolean(a.checked),
            nel,
            href = a.href ? a.href : Ext.isGecko ? "" : "#",
            buf = ['<span class="x-tree-node"><span ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<span class="x-tree-node-indent">',this.indentMarkup,"</span>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:none;" />',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" style="display:none;" unselectable="on" />',
            cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></span>",
            '<span class="x-tree-node-ct" style="display:none;"></span>',
            "</span>"].join('');

        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1];
        this.iconNode = cs[2];
        var index = 3;
        if(cb){
            this.checkbox = cs[3];
            // fix for IE6
            this.checkbox.defaultChecked = this.checkbox.checked;
            index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
    },
    // private
    renderIndent : function(){
        if(this.rendered){
            var indent = "",
                p = this.node.parentNode;
            if(p){
                indent = p.ui.getChildIndent(this.node);
            }
            if(this.indentMarkup != indent){ // don't rerender if not required
                this.indentNode.innerHTML = indent;
                this.indentMarkup = indent;
            }
            this.updateExpandIcon();
        }
    }
    
});

