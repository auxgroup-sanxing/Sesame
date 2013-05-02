/**
 * @class Ext.ux.ProcessNodeUI
 * This class provides the default UI implementation for <b>basic</b> Process Nodes.
 * The ProcessNode UI implementation allows customizing the appearance of the process node.<br>
 * <p>
 * If you are customizing the Diagram's user interface, you
 * may need to extend this class, but you should never need to instantiate this class.<br>
 */
Ext.ux.ProcessNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
	//public
	imagePath :(function (){
		var scriptList=document.getElementsByTagName("script");
		var scriptEl=scriptList[scriptList.length-1];
		var href=scriptEl.getAttribute("src");
		var loc= href.substr(0, href.lastIndexOf('/')+1);
		var linkEl = document.createElement("link");
		linkEl.setAttribute('rel', 'stylesheet');
		linkEl.setAttribute('type', 'text/css');
		linkEl.setAttribute('href', loc+'resources/ProcessPanel.css');
		scriptEl.parentNode.appendChild(linkEl);
		return loc+'resources/images/';
	})(),
	// private
	getDDHandles : function(){
		var handles = [this.iconNode, this.textNode];
		if (this.indentNode.firstChild) handles.push(this.indentNode.firstChild);
		return handles;
	},

	// private
	getIndent : function(){
		return '<div class="hotspot" style="height:40px; width:32px; margin: 0px auto 7px auto;"></div>';
    },
	
    render : function(bulkRender){
        var n = this.node, a = n.attributes;
        var targetNode = n.parentNode ?
              n.parentNode.ui.getContainer() : n.ownerTree.innerCt.dom;

        if(!this.rendered){
            this.rendered = true;

            this.renderElements(n, a, targetNode, bulkRender);

            if(a.qtip){
                this.iconNode.setAttribute("title", a.qtip);
                this.textNode.setAttribute("title", a.qtip);
            }else if(a.qtipCfg){
                a.qtipCfg.target = Ext.id(this.textNode);
                Ext.QuickTips.register(a.qtipCfg);
            }
            this.initEvents();
            if(!this.node.expanded){
                this.updateExpandIcon();
            }
        }else{
            if(bulkRender === true) {
                targetNode.appendChild(this.wrap);
            }
        }
    },

	// private
    renderElements : function(n, a, targetNode, bulkRender){
        // add some indent caching, this helps performance when rendering a large tree
        this.indentMarkup = this.getIndent(); //n.parentNode ? n.parentNode.ui.getChildIndent() : '';

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node" style="text-align:center; background-color:white;">',
            '<div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" style="cursor:default; margin-right:auto; margin-left:auto; text-align:center;" unselectable="on">',
            '<div class="x-tree-node-indent processLineD" style="text-align:center; margin: 0px auto 0px auto;">',this.indentMarkup,"</div>",
            '<img src="', this.emptyIcon, '" style="display:none;" class="x-tree-ec-icon x-tree-elbow" />',
            '<div style="position:relative; padding: 0px 4px 0px 4px;" ',(a.iconCls ? " class="+a.iconCls : ""),'><img src="', a.icon || this.emptyIcon, '" style="display:inline; margin-left:auto; margin-right:auto;" class="',(a.icon ? "x-tree-node-inline-icon" : ""),'" unselectable="on" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; margin-left:3px; margin-top:0px;" src="' + this.imagePath + (a.checked ? 'complete.gif' : 'incomplete.gif') + '" />') : '', '</div>',
            '<a hidefocus="on" style="cursor:default; margin: 0px 4px 0px 4px;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            '<ul class="x-tree-node-ct" style="display:none;"></ul>',
            "</li>"].join('');

        var nel;
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
        this.iconNode = cs[2].childNodes[0];
        var index = 3;
        if(cb){
            this.checkbox = cs[2].childNodes[1];
            this.checkbox.checked = (a.checked==true);
            //index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
    },

    // private
    renderIndent : function(){
        if(this.rendered){
            var indent = "";
            var p = this.node.parentNode;
            if(p){
                indent = this.getIndent(); //p.ui.getChildIndent();
            }
            if(this.indentMarkup != indent){ // don't rerender if not required
                this.indentNode.innerHTML = indent;
                this.indentMarkup = indent;
            }
            this.updateExpandIcon();
        }
    },

    // private
    onClick : function(e){
		if (e.getTarget('a') || e.getTarget()==this.iconNode || e.getTarget()==this.checkbox){
			Ext.ux.ProcessNodeUI.superclass.onClick.apply(this, arguments);
		}
    },

    // private
    onContextMenu : function(e){
		if (e.getTarget('a') || e.getTarget()==this.iconNode){
			Ext.ux.ProcessNodeUI.superclass.onContextMenu.apply(this, arguments);
		}
    },

    // private
    onDblClick : function(e){
		if (e.getTarget('a') || e.getTarget()==this.iconNode){
			//Ext.ux.ProcessNodeUI.superclass.onDblClick.apply(this, arguments);
			
	        e.preventDefault();
	        if(this.disabled){
	            return;
	        }
	        if(this.fireEvent("beforedblclick", this.node, e) !== false){
	            if(!this.animating && this.node.isExpandable()){
	                this.node.toggle();
	            }
	            this.fireEvent("dblclick", this.node, e);
	        }
		}
    },

    // private
    onSelectedChange : function(state){
        if(state){
            this.focus();
            if (this.textNode) Ext.fly(this.textNode).addClass("x-tree-selected");
        }
		else{
            //this.blur();
            if (this.textNode) Ext.fly(this.textNode).removeClass("x-tree-selected");
        }
    },
    toggleCheck : function(value){
        var cb = this.checkbox;
        if(cb){
            cb.checked = (value === undefined ? !cb.checked : value);
            cb.src = this.imagePath + (cb.checked ? 'complete.gif' : 'incomplete.gif');
            this.onCheckChange();
        }
    },
    collapse : Ext.emptyFn,
    expand : Ext.emptyFn
});

/**
 * @class Ext.ux.BranchNodeUI
 * This class provides the default UI implementation for <b>Branch</b> Process Node.
 */
Ext.ux.BranchNodeUI = Ext.extend(Ext.ux.ProcessNodeUI, {
	icon: 'branch.gif',
    // private
	getPrefixCls: function(){
		var a = this.node.attributes,  cls = '';
		if (this.node.isFirst())
			cls = a.tlCls ? a.tlCls : 'processLineTL_branch';
		else if (this.node.isLast())
			cls = a.trCls ? a.trCls : 'processLineTR_branch';
		else
			cls = a.tmCls ? a.tmCls : 'processLineTM_branch';
		return cls;
	},
    // private
	getPostfixCls: function(){
		var a = this.node.attributes,  cls = '';
		if (this.node.isFirst())
			cls = a.blCls ? a.blCls : 'processLineBL';
		else if (this.node.isLast())
			cls = a.brCls ? a.brCls : 'processLineBR';
		else
			cls = a.bmCls ? a.bmCls : 'processLineBM';
		return cls;
	},
    // private
    onContextMenu : function(e){
		if (e.getTarget('a') || e.getTarget()==this.iconNode || e.getTarget()==this.checkbox){
			Ext.tree.TreeNodeUI.prototype.onContextMenu.apply(this, arguments);
		}
    },
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        var cb = typeof a.checked == 'boolean';
        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<td class="x-tree-node" style="text-align:center; vertical-align:top; padding-bottom:18px; background-color:transparent;">',
		    '<div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" style="cursor:default; background-color:white; text-align:center;" unselectable="on">',
            '<div class="x-tree-node-indent" style="text-align:center; height:8px; overflow:hidden;"></div>',
            '<img src="', this.emptyIcon, '" style="display:none;" class="x-tree-ec-icon x-tree-elbow" />',
            '<div style="position:relative; padding: 0px 4px 0px 4px;" ',(a.iconCls ? " class="+a.iconCls : ""),'>',
            '<img src="', a.icon || this.imagePath+this.icon, '" style="display:block; margin-left:auto; margin-right:auto;" class="x-tree-node-inline-icon"  unselectable="on" />',
            cb ? ('<img class="x-tree-node-cb" style="display:block; position:absolute; top:50%; left:50%; margin-top:-9px; margin-left:-9px;" src="' + this.emptyIcon + '" />') : '', 
            '</div>',
            '<a hidefocus="on" style="cursor:default; margin: 0px 4px 0px 4px;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            '<ul class="x-tree-node-ct"></ul>',
            "</td>"].join('');

        var nel;
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
        this.iconNode = cs[2].childNodes[0];
        if(cb){
            this.checkbox = cs[2].lastChild;
            this.toggleCheck(a.checked);
        }
        var index = 3;
        this.anchor = cs[index];
        this.textNode = this.anchor.firstChild;

		this.renderIndent();
	},
    // private
    renderIndent : function(){
        if(this.rendered){
			this.indentNode.className = this.getPrefixCls();
			this.wrap.className = 'x-tree-node '+this.getPostfixCls();
        }
    }
});

/**
 * @class Ext.ux.IfNodeUI
 * This class provides the default UI implementation for <b>if</b> Process Node.
 */
Ext.ux.IfNodeUI = Ext.extend(Ext.ux.BranchNodeUI, {
	icon: 'decision.gif',
    // private
	getPrefixCls: function(){
		var a = this.node.attributes,  cls = '';
		if (this.node.isFirst())
			cls = a.tlCls ? a.tlCls : 'processLineTL';
		else if (this.node.isLast())
			cls = a.trCls ? a.trCls : 'processLineTR';
		else
			cls = a.tmCls ? a.tmCls : 'processLineTM';
		return cls;
	},
    // private
	getPostfixCls: function(){
		var a = this.node.attributes,  cls = '';
		if (this.node.isFirst())
			cls = a.blCls ? a.blCls : 'processLineBL_decision';
		else if (this.node.isLast())
			cls = a.brCls ? a.brCls : 'processLineBR';
		else
			cls = a.bmCls ? a.bmCls : 'processLineBM_decision';
		return cls;
	},
	// private
    renderElements : function(n, a, targetNode, bulkRender){
		Ext.ux.IfNodeUI.superclass.renderElements.apply(this, arguments);
		if (Ext.isIE) {
			this.checkbox.style.display = 'none';
		}
	},
    // private
    renderIndent : function(){
        if(this.rendered){
			this.iconNode.parentNode.className = this.getPrefixCls();
			this.wrap.className = 'x-tree-node '+this.getPostfixCls();
        }
    },
    toggleCheck : function(value){
        var cb = this.checkbox;
        if(cb){
            cb.checked = (value === undefined ? !cb.checked : value);
            cb.src = cb.checked ? this.imagePath + 'XML_variable.gif' : this.emptyIcon;
            this.onCheckChange();
        }
    }
});

/**
 * @class Ext.ux.PathNodeUI
 * This class provides the default UI implementation for <b>path</b> Process Node.
 */
Ext.ux.PathNodeUI = Ext.extend(Ext.ux.BranchNodeUI, {
    // private
	getPrefixCls: function(){
		var a = this.node.attributes,  cls = '';
		if (this.node.isLast())
			cls = a.trCls ? a.trCls : 'processLineTR';
		else
			cls = a.tmCls ? a.tmCls : 'processLineTM';
		return cls;
	},
    // private
	getPostfixCls: function(){
		var a = this.node.attributes,  cls = '';
		if (this.node.isLast())
			cls = a.brCls ? a.brCls : 'processLineBR';
		else
			cls = a.bmCls ? a.bmCls : 'processLineBM';
		return cls;
	},
    // private
    renderIndent : function(){
        if(this.rendered){
			this.iconNode.parentNode.className = this.getPrefixCls();
			this.wrap.className = 'x-tree-node '+this.getPostfixCls();
        }
    }
});

/**
 * @class Ext.ux.CompositeNodeUI
 * This class provides the default UI implementation for <b>Composite</b> Process Node.
 */
Ext.ux.CompositeNodeUI = Ext.extend(Ext.ux.ProcessNodeUI, {
    addClass : function(cls){
        if(this.indentWrap){
            Ext.fly(this.indentWrap).addClass(cls);
        }
    },
    removeClass : function(cls){
        if(this.indentWrap){
            Ext.fly(this.indentWrap).removeClass(cls);
        }
    },
    // private
    collapse : function(){
        this.updateExpandIcon();
		if (this.ctNode != undefined && !!this.ctNode)	// 增加检查,修正消息配置中"指示面板"打开时报错: this.ctNode is undefined	
        	this.ctNode.style.display = "none";
		Ext.get(this.frameNode).removeClass("processDottedBorder");
		Ext.get(this.iconWrap).removeClass("processLine_pathintf");
		this.ecNode.title = '展开';
    },
    // private
    expand : function(){
        this.updateExpandIcon();
		if (this.node.hasChildNodes()) {
			this.ctNode.style.display = "";
			Ext.get(this.frameNode).addClass("processDottedBorder");
			Ext.get(this.iconWrap).addClass("processLine_pathintf");
			this.ecNode.title = '折叠';
		}
    },
	// private
	getDDHandles : function(){
		var handles = [this.iconNode, this.iconNode.nextSibling, this.iconNode.previousSibling, this.textNode];
		if (this.indentNode.firstChild) handles.push(this.indentNode.firstChild);
		return handles;
	},
    // private
    getDDRepairXY : function(){
		return Ext.lib.Dom.getXY(this.iconNode.previousSibling);
    },
    // private
    onClick : function(e){
		if (e.getTarget('a') || e.getTarget('img')){
			Ext.tree.TreeNodeUI.prototype.onClick.apply(this, arguments);
		}
    },
    // private
    onContextMenu : function(e){
		if (e.getTarget('a') || e.getTarget('img')){
			Ext.tree.TreeNodeUI.prototype.onContextMenu.apply(this, arguments);
		}
    },
    // private
    onDblClick : function(e){
		if (e.getTarget('a') || e.getTarget('img')){
			Ext.tree.TreeNodeUI.prototype.onDblClick.apply(this, arguments);
		}
    },
    // private
    onDisableChange : function(node, state){
        this.disabled = state;
        if (this.checkbox) {
            this.checkbox.disabled = state;
        }        
        if(state){
	        if(this.elNode){
	            Ext.fly(this.elNode).addClass("x-tree-node-disabled");
	        }
        }else{
	        if(this.elNode){
            	Ext.fly(this.elNode).removeClass("x-tree-node-disabled");
	        }
        } 
    },
	// private
    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = this.getIndent(); //n.parentNode ? n.parentNode.ui.getChildIndent() : '';
		
		var decIcon = a.leftIcon ? a.leftIcon : a.rightIcon;
		var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li style="text-align:center; background-color:white;">',
            '<div><div class="x-tree-node-indent processLineD" style="text-align:center; margin: 0px auto 0px auto;">',this.indentMarkup,"</div></div>",
            '<table style="margin-right:auto; margin-left:auto;" class="" cellpadding="0" cellspacing="0"><tr>',
            '<td class="x-tree-node"><div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" style="cursor:default; margin-right:auto; margin-left:auto; text-align:center;" unselectable="on">',
            '<div style="background-color:white; position:relative; padding-left:18px; padding-right:18px;">',
            '<img src="', decIcon, '" style="visibility:',(a.leftIcon?'visible':'hidden'),';" />',
            '<img src="', a.icon || this.emptyIcon, '" style="margin-left:auto; margin-right:auto;" class="',(a.iconCls ? a.iconCls : ''),(a.icon ? ' x-tree-node-inline-icon' : ''),'" unselectable="on" />',
            '<img src="', decIcon, '" style="visibility:',(a.rightIcon?'visible':'hidden'),';" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; margin-left:3px; margin-top:0px;" src="' + this.imagePath + (a.checked ? 'complete.gif' : 'incomplete.gif') + '" />') : '', 
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="position:absolute; margin-left:3px; top:18px;" /></div>',
            '<a hidefocus="on" style="cursor:default; margin: 0px 4px 0px 4px;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div></td>",
            '<td><table style="" cellpadding="0" cellspacing="0"><tr class="x-tree-node-ct" style="display:none;"><td style="width:2px; vertical-align:top;" class="processLineBL">',
            '<div style="height:40px; background-color:white;" class="processLineTL"></div></td></tr></table></td>',
            '</tr></table>',
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = Ext.get(this.wrap).child('div.x-tree-node-el', true);
        this.ctNode = Ext.get(this.wrap).child('tr.x-tree-node-ct', true);
        var cs = this.elNode.childNodes;
        this.indentWrap = this.wrap.childNodes[0];
        this.indentNode = this.indentWrap.firstChild;
        this.frameNode = this.wrap.childNodes[1];
		this.iconWrap = cs[0];
        this.ecNode = this.iconWrap.lastChild;
        this.iconNode = this.iconWrap.childNodes[1];
        var index = 1;
        if(cb){
            this.checkbox = this.ecNode.previousSibling;
            this.checkbox.checked = (a.checked==true);
            index++;
        }
        this.anchor = this.iconWrap.nextSibling;
        this.textNode = this.anchor.firstChild;
    },

    updateExpandIcon : function(){
        if(this.rendered){
            var n = this.node, c1, c2;
            var cls = "process-path";
            var hasChild = n.hasChildNodes();
            if(hasChild){
                if(n.expanded){
                    cls += "-expanded";
                    c1 = "x-tree-node-collapsed";
                    c2 = "x-tree-node-expanded";
                }else{
                    cls += "-collapsed";
                    c1 = "x-tree-node-expanded";
                    c2 = "x-tree-node-collapsed";
                }
                if(this.wasLeaf){
                    this.removeClass("x-tree-node-leaf");
                    this.wasLeaf = false;
                }
                if(this.c1 != c1 || this.c2 != c2){
                    Ext.fly(this.elNode).replaceClass(c1, c2);
                    this.c1 = c1; this.c2 = c2;
                }
            }else{
                if(!this.wasLeaf){
                    Ext.fly(this.elNode).replaceClass("x-tree-node-expanded", "x-tree-node-leaf");
                    delete this.c1;
                    delete this.c2;
                    this.wasLeaf = true;
                }
            }
            var ecc = "x-tree-ec-icon "+cls;
            if(this.ecc != ecc){
                this.ecNode.className = ecc;
                this.ecc = ecc;
            }
        }
    }
});

/**
 * @class Ext.ux.EachUI
 * This class provides the UI implementation for <b>for-each</b> inner each Node.
 */
Ext.ux.EachUI = Ext.extend(Ext.ux.ProcessNodeUI, {
    // private
    onContextMenu : function(e){
		if (e.getTarget('a') || e.getTarget()==this.iconNode || e.getTarget()==this.checkbox){
			Ext.tree.TreeNodeUI.prototype.onContextMenu.apply(this, arguments);
		}
    },
	// private
    renderElements : function(n, a, targetNode, bulkRender){
		Ext.ux.WhileUI.superclass.renderElements.apply(this, arguments);
		
		if (Ext.isIE) {
			this.checkbox.style.display = 'none';
		}
		else {
			var style = this.checkbox.style;
			style.display = 'block';
			style.marginLeft = '1px';
			style.left = '50%';
			style.bottom = '-1px';
			this.toggleCheck(a.checked);
		}
	},
    toggleCheck : function(value){
        var cb = this.checkbox;
        if(cb){
            cb.checked = (value === undefined ? !cb.checked : value);
            cb.src = cb.checked ? this.imagePath + 'XML_variable.gif' : this.emptyIcon;
            this.onCheckChange();
        }
    }
});

/**
 * @class Ext.ux.WhileUI
 * This class provides the UI implementation for <b>do-while, while-do</b> inner while Node.
 */
Ext.ux.WhileUI = Ext.extend(Ext.ux.ProcessNodeUI, {
    // private
    onContextMenu : function(e){
		if (e.getTarget('a') || e.getTarget()==this.iconNode || e.getTarget()==this.checkbox){
			Ext.tree.TreeNodeUI.prototype.onContextMenu.apply(this, arguments);
		}
    },
	// private
    renderElements : function(n, a, targetNode, bulkRender){
		Ext.ux.WhileUI.superclass.renderElements.apply(this, arguments);
		
		if (Ext.isIE) {
			this.checkbox.style.display = 'none';
		}
		else {
			var style = this.checkbox.style;
			style.display = 'block';
			style.marginTop = '-8px';
			style.marginLeft = '-8px';
			style.top = '50%';
			style.left = '50%';
			this.toggleCheck(a.checked);
		}
	},
    toggleCheck : function(value){
        var cb = this.checkbox;
        if(cb){
            cb.checked = (value === undefined ? !cb.checked : value);
            cb.src = cb.checked ? this.imagePath + 'XML_variable.gif' : this.emptyIcon;
            this.onCheckChange();
        }
    }
});

/**
 * @class Ext.ux.GroupNodeUI
 * This class provides the default UI implementation for <b>Group</b> Process Node.
 */
Ext.ux.GroupNodeUI = Ext.extend(Ext.ux.ProcessNodeUI, {
	icon: 'group_collapsed.gif',
	// private
    addClass : function(cls){
        if(this.indentWrap){
            Ext.fly(this.indentWrap).addClass(cls);
        }
    },
    removeClass : function(cls){
        if(this.indentWrap){
            Ext.fly(this.indentWrap).removeClass(cls);  
        }
    },
	// private
	appendDDGhost : function(ghostNode){
		var divEl = document.createElement('div');
		var iconEl = this.iconNode.cloneNode(true);
		iconEl.style.display = 'block';  iconEl.style.textalign = 'center';
		divEl.appendChild(iconEl);
		divEl.appendChild(this.anchor.cloneNode(true));
		ghostNode.style.textAlign = 'center';
		ghostNode.appendChild(divEl);
    },
    // private
    collapse : function(){
        this.updateExpandIcon();
        this.iconNode.style.display = "block";
		this.iconNode.appendChild(this.ecNode);
		this.ecNode.style.top=-4; this.ecNode.style.left=-4;
		this.ecNode.title='展开';
        this.frameNode.style.display = "none";
    },
    // private
    expand : function(){
        this.updateExpandIcon();
        this.iconNode.style.display = "none";
		this.eccNode.appendChild(this.ecNode);
		this.ecNode.style.top=0; this.ecNode.style.left=0;
		this.ecNode.title='折叠';
        this.frameNode.style.display = "";
    },
    // private
    getDDRepairXY : function(){
		var point = Ext.lib.Dom.getXY(this.iconNode);
		if (this.iconNode.style.display=='none') {
			point = Ext.lib.Dom.getXY(this.anchor);
			point[1] = point[1] - 32;
		}
        return point;
    },
    // private
    onClick : function(e){
		if (e.getTarget('a') || e.getTarget()==this.iconNode || e.getTarget('table')){
			Ext.tree.TreeNodeUI.prototype.onClick.apply(this, arguments);
		}
    },
    // private
    onContextMenu : function(e){
		if (e.getTarget('a') || e.getTarget()==this.iconNode || e.getTarget('table')){
			Ext.tree.TreeNodeUI.prototype.onContextMenu.apply(this, arguments);
		}
    },
    // private
    onSelectedChange : function(state){
        if(state){
			Ext.fly(this.frameNode).addClass("processSolidBorder");
        }
		else{
			Ext.fly(this.frameNode).removeClass("processSolidBorder");
        }
		Ext.ux.GroupNodeUI.superclass.onSelectedChange.apply(this, arguments);
    },
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = this.getIndent();

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node" style="text-align:center; margin: 0px 4px 0px 4px;">',
		    '<div ext:tree-node-id="',n.id,'" style="cursor:default; margin-right:auto; margin-left:auto; text-align:center;" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<div><div class="x-tree-node-indent processLineD" style="text-align:center;">',this.indentMarkup,"</div></div>",
            '<div style="width:32px; height:32px; background: url(', a.icon||this.imagePath+this.icon, ') no-repeat center; margin-left:auto; margin-right:auto; position:relative;" class="process-ec-container" unselectable="on">',
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:block; background-position:-4px -5px; position:relative; top:-5px; left:-5px; width:12px; height:12px;" title="展开" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; top:0px; right:-19px;" src="' + this.imagePath + (a.checked ? 'complete.gif' : 'incomplete.gif') + '" />') : '', 
			'</div>',
            '<table style="display:none; margin-right:auto; margin-left:auto;" class="processDottedBorder" cellpadding="0" cellspacing="0"><tr>',
            '<td style="width:16px;" class="process-ec-container"></td><td><ul class="x-tree-node-ct"></ul></td><td style="width:16px;"></td>',
            '</tr></table>',
            '<a hidefocus="on" style="cursor:default;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        var cs = this.elNode.childNodes;
        this.indentWrap = cs[0];
        this.indentNode = this.indentWrap.firstChild;
        this.ecNode = cs[1].childNodes[0];
        this.iconNode = cs[1];
        this.frameNode = cs[2];
        this.eccNode = Ext.get(this.frameNode).child('td.process-ec-container', true);
        this.ctNode = Ext.get(this.frameNode).child('ul.x-tree-node-ct', true);
        if(cb){
            this.checkbox = this.iconNode.lastChild;  //cs[index];
            this.checkbox.checked = (a.checked==true);
			this.checkbox.defaultChecked = this.checkbox.checked;			
        }
        var index = 3;
        this.anchor = this.frameNode.nextSibling;
        this.textNode = this.anchor.firstChild;
    }
});

/**
 * @class Ext.ux.DowhileUI
 * This class provides the default UI implementation for <b>Do While</b> Process Node.
 */
Ext.ux.DowhileUI = Ext.extend(Ext.ux.GroupNodeUI, {
	icon: 'do_while_collapsed.gif',
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        // add some indent caching, this helps performance when rendering a large tree
        this.indentMarkup = this.getIndent();

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node" style="text-align:center; margin: 0px 4px 0px 4px;">',
		    '<div ext:tree-node-id="',n.id,'" style="cursor:default; margin-right:auto; margin-left:auto; text-align:center;" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<div><div class="x-tree-node-indent processLineD" style="text-align:center;">',this.indentMarkup,"</div></div>",
            '<div style="width:32px; height:32px; background: url(', a.icon||this.imagePath+this.icon, ') no-repeat center; margin-left:auto; margin-right:auto; postion:relative;" class="process-ec-container" unselectable="on">',
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:block; background-position:-4px -5px; position:relative; top:-5px; left:-5px; width:12px; height:12px;" title="展开" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; top:0px; right:-19px;" src="' + this.imagePath + (a.checked ? 'complete.gif' : 'incomplete.gif') + '" />') : '', 
            '</div>',
            '<table style="display:none; margin: 0 auto 0 auto;" class="processDottedBorder" cellpadding="0" cellspacing="0">',
            '<tr><td style="width:20px; height:20px;"  class="process-ec-container"></td><td class="processLineTL_dowhile"></td><td class="processLineTR"></td></tr>',
            '<tr><td style="width:20px;"></td><td><ul class="x-tree-node-ct"></ul></td><td style="width:20px; vertical-align:bottom;" class="processLineV x-tree-node"><div style="height:40px; background-color:white; text-align:center;" class="processLineBR_dowhile"></div><div style="background-color:white; line-height:18px;">&nbsp;</div></td></tr>',
            '<tr><td style="width:20px; height:15px;"></td><td class="processLineV"></td><td></td></tr></table>',
            '<a hidefocus="on" style="cursor:default;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        //this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.childNodes;
        this.indentWrap = cs[0];
        this.indentNode = this.indentWrap.firstChild;
        this.ecNode = cs[1].childNodes[0];
        this.iconNode = cs[1];
        this.frameNode = cs[2];
        this.loopline = this.frameNode.childNodes[0].childNodes[1].childNodes[2].firstChild;
        this.eccNode = Ext.get(this.frameNode).child('td.process-ec-container', true);
        this.ctNode = Ext.get(this.frameNode).child('ul.x-tree-node-ct', true);
        var index = 3;
        if(cb){
            this.checkbox = this.iconNode.lastChild//cs[index];
            this.checkbox.checked = (a.checked==true);
			// fix for IE6
			this.checkbox.defaultChecked = this.checkbox.checked;			
            //index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
    }
});

/**
 * @class Ext.ux.LoopNodeUI
 * This class provides the default UI implementation for <b>loop</b> Process Node.
 */
Ext.ux.LoopNodeUI = Ext.extend(Ext.ux.GroupNodeUI, {
	icon: 'while_do_collapsed.gif',
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        // add some indent caching, this helps performance when rendering a large tree
        this.indentMarkup = this.getIndent();

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node" style="text-align:center; margin: 0px 4px 0px 4px;">',
		    '<div ext:tree-node-id="',n.id,'" style="cursor:default; margin-right:auto; margin-left:auto; text-align:center;" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<div><div class="x-tree-node-indent processLineD" style="text-align:center;">',this.indentMarkup,"</div></div>",
            '<div style="width:32px; height:32px; background: url(', a.icon||this.imagePath+this.icon, ') no-repeat center; margin-left:auto; margin-right:auto; position:relative;" class="process-ec-container" unselectable="on">',
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:block; background-position:-4px -5px; position:relative; top:-5px; left:-5px; width:12px; height:12px;" title="展开" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; top:0px; right:-19px;" src="' + this.imagePath + (a.checked ? 'complete.gif' : 'incomplete.gif') + '" />') : '', 
            '</div>',
            '<table style="display:none; margin-right:auto; margin-left:auto;" class="processDottedBorder" cellpadding="0" cellspacing="0">',
            '<tr><td style="width:20px;" class="process-ec-container"></td><td><ul class="x-tree-node-ct"></ul></td><td style="width:20px; vertical-align:top;" class="processLineV x-tree-node"><div style="height:32px; background-color:white;" class="processLineTR"></div><div style="line-height:18px; text-align:center;">&nbsp;</div></td></tr>',
            '<tr><td style="width:20px; height:20px;"></td><td style="min-width:80px;" class="processLineBL_whiledo"></td><td class="processLineBR"></td></tr></table>',
            '<a hidefocus="on" style="cursor:default;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        var cs = this.elNode.childNodes;
        this.indentWrap = cs[0];
        this.indentNode = this.indentWrap.firstChild;
        this.ecNode = cs[1].childNodes[0];
        this.iconNode = cs[1];
        this.frameNode = cs[2];
        this.loopline = this.frameNode.childNodes[0].childNodes[0].childNodes[2].lastChild;
        this.eccNode = Ext.get(this.frameNode).child('td.process-ec-container', true);
        this.ctNode = Ext.get(this.frameNode).child('ul.x-tree-node-ct', true);
        var index = 3;
        if(cb){
            this.checkbox = this.iconNode.lastChild; //cs[index];
            this.checkbox.checked = (a.checked==true);
			this.checkbox.defaultChecked = this.checkbox.checked;			
            //index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
    }
});

/**
 * @class Ext.ux.DecisionNodeUI
 * This class provides the default UI implementation for <b>decision</b> Process Node.
 */
Ext.ux.DecisionNodeUI = Ext.extend(Ext.ux.GroupNodeUI, {
	icon: 'decision_collapsed.gif',
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        // add some indent caching, this helps performance when rendering a large tree
        this.indentMarkup = this.getIndent();

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node" style="text-align:center; margin: 0px 4px 0px 4px;">',
		    '<div ext:tree-node-id="',n.id,'" style="cursor:default; margin-right:auto; margin-left:auto; text-align:center;" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<div><div class="x-tree-node-indent processLineD" style="text-align:center;">',this.indentMarkup,"</div></div>",
            '<div style="width:32px; height:32px; background: url(', a.icon||this.imagePath+this.icon, ') no-repeat center; margin-left:auto; margin-right:auto; position:relative;" class="process-ec-container" unselectable="on">',
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:block; background-position:-4px -5px; position:relative; top:-5px; left:-5px; width:12px; height:12px;" title="展开" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; top:0px; right:-19px;" src="' + this.imagePath + (a.checked ? 'complete.gif' : 'incomplete.gif') + '" />') : '', 
            '</div>',
            '<table style="display:none; margin-right:auto; margin-left:auto;" class="processDottedBorder" cellpadding="0" cellspacing="0">',
            '<tr><td style="width:16px;" class="process-ec-container"></td><td><table style="" cellpadding="0" cellspacing="0"><tr class="x-tree-node-ct"></tr></table></td>',
            '<td style="width:16px; vertical-align:top;"><div class="process_AddButton" title="添加条件分支"></div></td></tr></table>',
            '<a hidefocus="on" style="cursor:default;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        var cs = this.elNode.childNodes;
        this.indentWrap = cs[0];
        this.indentNode = this.indentWrap.firstChild;
        this.ecNode = cs[1].childNodes[0];
        this.iconNode = cs[1];
        this.frameNode = cs[2];
        this.eccNode = Ext.get(this.frameNode).child('td.process-ec-container', true);
        this.ctNode = Ext.get(this.frameNode).child('tr.x-tree-node-ct', true);
        var index = 3;
        if(cb){
            this.checkbox = this.iconNode.lastChild;  //cs[index];
            this.checkbox.checked = (a.checked==true);
			this.checkbox.defaultChecked = this.checkbox.checked;			
            //index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
		
        this.addButton = Ext.get(this.frameNode).child('div.process_AddButton');
		this.addButton.addClassOnOver('process_AddButton_hover');
		this.addButton.on('click', function(e){ this.addBranch(e); }, this);
    },

	// public
	addBranch: function(e){
		e.stopEvent();
		var branch = new Ext.tree.AsyncTreeNode({
			text: "条件", 
			icon: this.imagePath+'decision.gif', 
			uiProvider: Ext.ux.IfNodeUI,
			tag: 'if',
			allowDrag: false,
			allowEdit: true,
			allowRemove: true,
			expanded: true,
			checked: false,
			toString: function(){
				return this['var'] ? this['var']+'['+Ext.util.Format.ellipsis(this.xpath, 20)+']' : "条件";
			},
			children: [{
				cls: 'processNodeLabelHidden',
				uiProvider: Ext.ux.ProcessNodeUI,
				leaf: true, 
				allowRemove: true,
				allowDrag: false
			}]
		});
		this.node.insertBefore(branch, this.node.lastChild);
		branch.select();
	}
});

/**
 * @class Ext.ux.ParallelNodeUI
 * This class provides the default UI implementation for <b>parallel</b> Process Node.
 */
Ext.ux.ParallelNodeUI = Ext.extend(Ext.ux.GroupNodeUI, {
	icon: 'parallel_collapsed.gif',
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        // add some indent caching, this helps performance when rendering a large tree
        this.indentMarkup = this.getIndent();

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node" style="text-align:center; margin: 0px 4px 0px 4px;">',
		    '<div ext:tree-node-id="',n.id,'" style="cursor:default; margin-right:auto; margin-left:auto; text-align:center;" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<div class="x-tree-node-indent processLineD" style="text-align:center;">',this.indentMarkup,"</div>",
            '<div style="width:32px; height:32px; background: url(', a.icon||this.imagePath+this.icon, ') no-repeat center; margin-left:auto; margin-right:auto; position:relative;" class="process-ec-container" unselectable="on">',
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:block; background-position:-4px -5px; position:relative; top:-5px; left:-5px; width:12px; height:12px;" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; top:0px; right:-19px;" src="' + this.imagePath + (a.checked ? 'complete.gif' : 'incomplete.gif') + '" />') : '', 
            '</div>',
            '<table style="display:none; margin-right:auto; margin-left:auto;" class="processDottedBorder" cellpadding="0" cellspacing="0">',
            '<tr><td style="width:16px;" class="process-ec-container"></td><td style="text-align:center;"><div class="processParallelHeader"></div></td><td style="vertical-align:top"><div class="process_AddButton" title="添加分支"></div></td></tr>',
            '<tr><td style="width:16px;">&nbsp;</td><td><table cellpadding="0" cellspacing="0"><tbody><tr class="x-tree-node-ct"></tr></tbody></table></td><td style="width:16px;"></td></tr></table>',
            '<a hidefocus="on" style="cursor:default;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1].childNodes[0];
        this.iconNode = cs[1];
        this.frameNode = cs[2];
        this.eccNode = Ext.get(this.frameNode).child('td.process-ec-container', true);
        this.ctNode = Ext.get(this.frameNode).child('tr.x-tree-node-ct', true);
        var index = 3;
        if(cb){
            this.checkbox = this.iconNode.lastChild; //cs[index];
            this.checkbox.checked = (a.checked==true);
			this.checkbox.defaultChecked = this.checkbox.checked;			
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
		
        this.addButton = Ext.get(this.frameNode).child('div.process_AddButton');
		this.addButton.addClassOnOver('process_AddButton_hover');
		this.addButton.on('click', function(e){ this.addBranch(e); }, this);
		this.setJoin(a.join);
    },

	//public
	addBranch: function(e){
		e.stopEvent();
		var branch = new Ext.tree.AsyncTreeNode({
			text: "分支",
			icon: this.imagePath + 'branch.gif',
			uiProvider: Ext.ux.BranchNodeUI,
			tag: 'branch',
			allowDrag: false,
			allowRemove: true,
			expanded: true,
			children: [{
				cls: 'processNodeLabelHidden',
				uiProvider: Ext.ux.ProcessNodeUI,
				leaf: true,
				allowDrag: false
			}]
		});
		this.node.appendChild(branch);
		branch.select();
	},
	//public
	setJoin: function(join){
		var cls = join=='or' ? 'processParallelOR' : 'processParallelAND';
		this.ctNode.parentNode.parentNode.className = cls;
	}
});

/**
 * @class Ext.ux.TryCatchUI
 * This class provides the default UI implementation for <b>try-catch</b> Process Node.
 */
Ext.ux.TryCatchUI = Ext.extend(Ext.ux.GroupNodeUI, {
	icon: 'group_collapsed.gif',
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        // add some indent caching, this helps performance when rendering a large tree
        this.indentMarkup = this.getIndent();

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node" style="text-align:center; margin: 0px 4px 0px 4px;">',
		    '<div ext:tree-node-id="',n.id,'" style="cursor:default; margin-right:auto; margin-left:auto; text-align:center;" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<div><div class="x-tree-node-indent processLineD" style="text-align:center;">',this.indentMarkup,"</div></div>",
            '<div style="width:32px; height:32px; background: url(', a.icon||this.imagePath+this.icon, ') no-repeat center; margin-left:auto; margin-right:auto; position:relative;" class="process-ec-container" unselectable="on">',
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:block; background-position:-4px -5px; position:relative; top:-5px; left:-5px; width:12px; height:12px;" title="展开" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; top:0px; right:-19px;" src="' + this.imagePath + (a.checked ? 'complete.gif' : 'incomplete.gif') + '" />') : '', 
            '</div>',
            '<table style="display:none; margin-right:auto; margin-left:auto;" class="processDottedBorder" cellpadding="0" cellspacing="0">',
            '<tr><td style="width:16px;" class="process-ec-container"></td><td><table style="" cellpadding="0" cellspacing="0"><tr class="x-tree-node-ct"></tr></table></td>',
            '<td style="width:16px; vertical-align:top;"><div class="process_AddButton" title="添加异常处理"></div></td></tr></table>',
            '<a hidefocus="on" style="cursor:default;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        var cs = this.elNode.childNodes;
        this.indentWrap = cs[0];
        this.indentNode = this.indentWrap.firstChild;
        this.ecNode = cs[1].childNodes[0];
        this.iconNode = cs[1];
        this.frameNode = cs[2];
        this.eccNode = Ext.get(this.frameNode).child('td.process-ec-container', true);
        this.ctNode = Ext.get(this.frameNode).child('tr.x-tree-node-ct', true);
        var index = 3;
        if(cb){
            this.checkbox = this.iconNode.lastChild;  //cs[index];
            this.checkbox.checked = (a.checked==true);
			this.checkbox.defaultChecked = this.checkbox.checked;			
            //index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
		
        this.addButton = Ext.get(this.frameNode).child('div.process_AddButton');
		this.addButton.addClassOnOver('process_AddButton_hover');
		this.addButton.on('click', function(e){ this.addBranch(e); }, this);
    },

	// public
	addBranch: function(e){
		e.stopEvent();
		var branch = new Ext.tree.AsyncTreeNode({
			text: "catch", 
			icon: this.imagePath+'on_exception.gif', 
			uiProvider: Ext.ux.IfNodeUI,
			tag: 'catch',
			allowDrag: false,
			allowEdit: true,
			allowRemove: true,
			expanded: true,
			toString: function(){
				return 'catch('+(this['exception-key'] ? Ext.util.Format.ellipsis(this['exception-key'], 20)+')' : '*')+")";
			},
			children: [{
				cls: 'processNodeLabelHidden',
				uiProvider: Ext.ux.ProcessNodeUI,
				leaf: true, 
				allowRemove: true,
				allowDrag: false
			}]
		});
		this.node.insertBefore(branch, this.node.lastChild);
		branch.select();
	}
});

/**
 * @class Ext.ux.XactNodeUI
 * This class provides the default UI implementation for <b>transaction</b> Process Node.
 */
Ext.ux.XactNodeUI = Ext.extend(Ext.ux.GroupNodeUI, {
	icon: 'xact_collapsed.gif',
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        // add some indent caching, this helps performance when rendering a large tree
        this.indentMarkup = this.getIndent();

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node" style="text-align:center; margin: 0px 4px 0px 4px;">',
		    '<div ext:tree-node-id="',n.id,'" style="cursor:default; margin-right:auto; margin-left:auto; text-align:center;" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<div><div class="x-tree-node-indent processLineD" style="text-align:center;">',this.indentMarkup,"</div></div>",
            '<div style="width:32px; height:32px; background: url(', a.icon||this.imagePath+this.icon, ') no-repeat center; margin-left:auto; margin-right:auto; position:relative;" class="process-ec-container" unselectable="on">',
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:block; background-position:-4px -5px; position:relative; top:-5px; left:-5px; width:10px; height:10px;" />',
            cb ? ('<img class="x-tree-node-cb" style="position:absolute; top:0px; right:-19px;" src="' + this.imagePath + (a.checked ? 'complete.gif' : 'incomplete.gif') + '" />') : '', 
            '</div>',
            '<table style="display:none; margin-right:auto; margin-left:auto;" class="processDottedBorder" cellpadding="0" cellspacing="0"><tbody>',
            '<tr><td class="process-ec-container processLineV" colspan=3></td></tr>',
            '<tr><td class="process_xact_top_left">&nbsp;</td><td class="process_xact_top_fill"><div class="process_xact_top_middle"></div></td><td class="process_xact_top_right">&nbsp;</td></tr>',
            '<tr><td>&nbsp;</td><td><ul class="x-tree-node-ct"></ul></td><td>&nbsp;</td></tr>',
            '<tr><td class="process_xact_bottom_left">&nbsp;</td><td class="process_xact_bottom_fill"><div class="process_xact_bottom_middle"></div></td><td class="process_xact_bottom_right">&nbsp;</td></tr>',
			'</tbody></table>',
            '<a hidefocus="on" style="cursor:default;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        //this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.childNodes;
        this.indentWrap = cs[0];
        this.indentNode = this.indentWrap.firstChild;
        this.ecNode = cs[1].childNodes[0];
        this.iconNode = cs[1];
        this.frameNode = cs[2];
        this.eccNode = Ext.get(this.frameNode).child('td.process-ec-container', true);
        this.ctNode = Ext.get(this.frameNode).child('ul.x-tree-node-ct', true);
        var index = 3;
        if(cb){
            this.checkbox = this.iconNode.lastChild;  //cs[index];
            this.checkbox.checked = (a.checked==true);
			this.checkbox.defaultChecked = this.checkbox.checked;			
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
    }
});

