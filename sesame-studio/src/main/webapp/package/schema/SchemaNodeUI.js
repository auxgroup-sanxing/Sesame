/**
 * 自动载入样式表单
 * 
 */
var SchemaNodeUI = {

imagePath :(function (){
	var scriptList=document.getElementsByTagName("script");
	var scriptEl=scriptList[scriptList.length-1];
	var href=scriptEl.getAttribute("src");
	var loc= href.substr(0, href.lastIndexOf('/')+1);
	var linkEl = document.createElement("link");
	linkEl.setAttribute('rel', 'stylesheet');
	linkEl.setAttribute('type', 'text/css');
	linkEl.setAttribute('href', loc+'resources/SchemaPanel.css');
	scriptEl.parentNode.appendChild(linkEl);
	return loc+'resources/images/';
})()

}

/**
 * @class Ext.ux.SchemaNodeUI
 * This class provides the default UI implementation for <b>basic</b> Schema Nodes.
 * The ProcessNode UI implementation allows customizing the appearance of the schema node.<br>
 */
Ext.ux.SchemaNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
	collapse: Ext.emptyFn,
	expand: Ext.emptyFn,
	// private
	getDDHandles : function(){
		var handles = [this.iconNode, this.textNode];
		return handles;
	},
	// private
	getChildIndent : function(){
		return '';
    },
	// private
    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent(n) : '';

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node" style="background-color:white;">',
            '<div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" style="cursor:default;" unselectable="on">',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on" />',
            cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" style="cursor:default;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" />',
            '<span class="x-tree-node-indent" style="display:none;">',this.indentMarkup,"</span></div>",
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
        this.iconNode = cs[0];
        var index = 1;
        if(cb){
            this.checkbox = cs[index];
			// fix for IE6
			this.checkbox.defaultChecked = this.checkbox.checked;			
            index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
        this.ecNode = this.anchor.nextSibling;
        this.indentNode = this.ecNode.nextSibling;
		
		//this.wrap.className = 'x-tree-node '+this.indentMarkup;
    },
    // private
    renderIndent : function(){
        if(this.rendered){
            var indent = "";
            var p = this.node.parentNode;
            if(p){
                indent = p.ui.getChildIndent(this.node);
            }
            if(this.indentMarkup != indent){ // don't rerender if not required
                //this.indentNode.innerHTML = indent;
                this.wrap.className = 'x-tree-node '+indent;
				this.indentMarkup = indent;
            }
            this.updateExpandIcon();
        }
    },
    // private
    onClick : function(e){
		if (e.getTarget('a') || e.getTarget()== this.iconNode){
			Ext.ux.SchemaNodeUI.superclass.onClick.apply(this, arguments);
		}
    },
    // private
    onContextMenu : function(e){
		if (e.getTarget('a') || e.getTarget()==this.iconNode){
			Ext.ux.SchemaNodeUI.superclass.onContextMenu.apply(this, arguments);
		}
    },
    // private
    onDblClick : function(e){
		if (e.getTarget('a') || e.getTarget()== this.iconNode){
			var importTree = Ext.getCmp('import_tree');
			if (!!importTree) {
				var root = importTree.getRootNode();
				if (!!root) {
					Ext.each(root.childNodes, function(dataModel){
						dataModel.expand();
					});
				}
			}
			Ext.ux.SchemaNodeUI.superclass.onDblClick.apply(this, arguments);
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
    }
});

/**
 * @class Ext.ux.SchemaRefUI
 * This class provides the default UI implementation for <b>ref</b> schema Node.
 */
Ext.ux.SchemaRefUI = Ext.extend(Ext.ux.SchemaNodeUI, {
    // private
    renderElements : function(n, a, targetNode, bulkRender){
		Ext.ux.SchemaRefUI.superclass.renderElements.apply(this, arguments);
        this.ecNode.style.display = '';
        this.ecNode.title = '查找目标';
		this.ecNode.className = 'x-tree-ec-icon xsd-icon-searchref';
	},
    updateExpandIcon : function(){
        if(this.rendered){
            var ecc = "x-tree-ec-icon xsd-icon-searchref";
            if(this.ecc != ecc){
                this.ecNode.className = ecc;
                this.ecc = ecc;
            }
        }
    }
});

/**
 * @class Ext.ux.SchemaElementUI
 * This class provides the default UI implementation for <b>element</b> Schema Node.
 */
Ext.ux.SchemaElementUI = Ext.extend(Ext.ux.SchemaNodeUI, {
    // private
    collapse : function(){
        this.updateExpandIcon();
		//this.arrowNode.style.display = "none";
        this.ctNode.style.display = "none";
		this.ecNode.title = '展开';
    },
    // private
    expand : function(){
        this.updateExpandIcon();
		//Ext.get(this.ecNode).removeClass('process-path-collapsed');
		if (this.node.hasChildNodes()) {
			//Ext.get(this.ecNode).addClass('process-path-expanded');
			this.ctNode.style.display = "";
			this.ecNode.title = '折叠';
		}
    },
	// private
    renderElements : function(n, a, targetNode, bulkRender){
		Ext.ux.SchemaElementUI.superclass.renderElements.apply(this, arguments);
		this.ctNode.style.paddingLeft='16px';
    }
});

/**
 * @class Ext.ux.SchemaStyleUI
 * This class provides the default UI implementation for <b>sequence, choice, all</b> schema Node.
 */
Ext.ux.SchemaStyleUI = Ext.extend(Ext.ux.SchemaNodeUI, {
    // private
    onSelectedChange : function(state){
        if(state){
            this.focus();
			Ext.get(this.iconNode).addClass("x-tree-selected");
        }
		else{
			//this.iconNode.style.borderColor='gray';
			Ext.get(this.iconNode).removeClass("x-tree-selected");
        }
    },
    // private
    render : function(bulkRender){
        var n = this.node, a = n.attributes;
        var targetNode = n.parentNode ? 
              n.parentNode.ui.getContainer() : n.ownerTree.innerCt.dom;
        
        if(!this.rendered){
            this.rendered = true;

            this.renderElements(n, a, targetNode, bulkRender);

            if(a.qtip){
               if(this.iconNode.setAttributeNS){
                   this.iconNode.setAttributeNS("ext", "qtip", a.qtip);
                   if(a.qtipTitle){
                       this.iconNode.setAttributeNS("ext", "qtitle", a.qtipTitle);
                   }
               }else{
                   this.iconNode.setAttribute("ext:qtip", a.qtip);
                   if(a.qtipTitle){
                       this.iconNode.setAttribute("ext:qtitle", a.qtipTitle);
                   }
               } 
            }else if(a.qtipCfg){
                a.qtipCfg.target = Ext.id(this.iconNode);
                Ext.QuickTips.register(a.qtipCfg);
            }
            this.initEvents();
            if(!this.node.expanded){
                this.updateExpandIcon(true);
            }
        }else{
            if(bulkRender === true) {
                targetNode.appendChild(this.wrap);
            }
        }
    },
	// private
    renderElements : function(n, a, targetNode, bulkRender){
        //this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent(n) : '';
		
		var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li style="background-color:white;"><table cellpadding="0" cellspacing="0"><tr>',
            '<td class="x-tree-node" style="vertical-align:middle; padding-right:1px; border-right:1px solid #808000;">',
            '<div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" style="background-color:white; cursor:default;" unselectable="on">',
            '<span class="x-tree-node-indent" style="display:none"></span>',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on" />',
            cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" style="cursor:default; display:none;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:none;" /></div></td>',
            '<td style="padding-left:1px;"><ul class="x-tree-node-ct"></ul></td>',
            '</tr></table>',
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = Ext.get(this.wrap).child('div.x-tree-node-el', true);
        this.ctNode = Ext.get(this.wrap).child('ul.x-tree-node-ct', true);
        this.frameNode = this.wrap.childNodes[0];
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.iconNode = cs[1];
        var index = 2;
        if(cb){
            this.checkbox = cs[index];
			// fix for IE6
			this.checkbox.defaultChecked = this.checkbox.checked;			
            index++;
        }
        this.anchor = cs[index];
        this.textNode = this.anchor.firstChild;
        this.ecNode = this.anchor.nextSibling;
		
		//this.wrap.className = this.indentMarkup;
		this.iconNode.title = (a.tag=='sequence'?'序列':a.tag=='choice'?'选择':a.tag=='all'?'全部':'');
    }
});
/**
 * @class Ext.ux.SchemaBoxUI
 * This class provides the default UI implementation for <b>Group</b> schema Node.
 */
Ext.ux.SchemaBoxUI = Ext.extend(Ext.ux.SchemaNodeUI, {
    // private
    collapse : function(){
        this.updateExpandIcon();
		this.ecNode.title='展开';
		this.ctNode.style.display='none';
    },
    // private
    expand : function(){
        this.updateExpandIcon();
		this.ecNode.title='折叠';
		this.ctNode.style.display='';
    },
	// private
	getDDHandles : function(){
		var handles = [this.iconNode, this.textNode, this.frameNode];
		return handles;
	},
    // private
    getDDRepairXY : function(){
		return Ext.lib.Dom.getXY(this.frameNode);
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
    onDblClick : function(e){
		if (e.getTarget('a') || e.getTarget()==this.iconNode || e.getTarget('td')){
			Ext.tree.TreeNodeUI.prototype.onDblClick.apply(this, arguments);
		}
    },
    // private
    onSelectedChange : function(state){
        if(state){
            this.focus();
			Ext.get(this.frameNode).addClass("xsd-box-active");
			Ext.get(this.elNode.parentNode).addClass("xsd-boxheader-active");
        }
		else{
			Ext.get(this.frameNode).removeClass("xsd-box-active");
			Ext.get(this.elNode.parentNode).removeClass("xsd-boxheader-active");
        }
    },
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li style="margin-bottom:5px;">',
            '<table style="width:100%;" class="xsd-box-deactive x-tree-node" cellpadding="0" cellspacing="0"><tr>',
		    '<td class="xsd-boxheader"><div ext:tree-node-id="',n.id,'" style="cursor:default;" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="float:right; display:inline;" title="展开" />',
            '<span class="x-tree-node-indent" style="display:none;"></span>',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on" />',
			cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" style="cursor:default;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a>",
            '</div></td></tr>',
            '<tr><td><ul class="x-tree-node-ct" style="display:none; margin:5px;"></ul></td></tr>',
            '</table>',
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.frameNode = this.wrap.firstChild;
        this.elNode = Ext.get(this.wrap).child('div.x-tree-node-el', true);
        var cs = this.elNode.childNodes;
        this.ecNode = cs[0];
        this.indentNode = cs[1];
        this.iconNode = cs[2];
        this.ctNode = Ext.get(this.frameNode).child('ul.x-tree-node-ct', true);
        var index = 3;
        if(cb){
            this.checkbox = cs[index];
			// fix for IE6
			this.checkbox.defaultChecked = this.checkbox.checked;			
            index++;
        }
        this.anchor = cs[index];
        this.textNode = this.anchor.firstChild;
    },
    renderIndent : Ext.emptyFn
});

/**
 * @class Ext.ux.SchemaComplextypeUI
 * This class provides the default UI implementation for <b>ComplexType</b> Schema Node.
 */
Ext.ux.SchemaComplextypeUI = Ext.extend(Ext.ux.SchemaBoxUI, {
    // private
    collapse : function(){
        this.updateExpandIcon();
		this.ecNode.title='展开';
		this.ctNode.style.display='none';
		Ext.fly(this.frameNode).removeClass('xsd-complex-expanded');
		Ext.fly(this.elNode.parentNode).removeClass('xsd-complex-title');
    },
    // private
    expand : function(){
        this.updateExpandIcon();
		this.ecNode.title='折叠';
		if (this.node.attributes.allowExpand!=false) {
			this.ctNode.style.display = '';
			Ext.fly(this.frameNode).addClass('xsd-complex-expanded');
			Ext.fly(this.elNode.parentNode).addClass('xsd-complex-title');
		}
    },
    // private
    onSelectedChange : function(state){
        if(state){
            this.focus();
			Ext.get(this.elNode.parentNode).addClass("x-tree-selected");
        }
		else{
			Ext.get(this.elNode.parentNode).removeClass("x-tree-selected");
        }
    },
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li style="">',
            '<table class="x-tree-node" cellpadding="0" cellspacing="0"><tr><td>',
		    '<div ext:tree-node-id="',n.id,'" style="cursor:default;" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<span class="x-tree-node-indent" style="display:none;"></span>',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on" />',
			cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" style="cursor:default;" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="position:relative; right: 0px;" />',
            '</div></td></tr>',
            '<tr><td><ul class="x-tree-node-ct" style="margin:5px; display:none;"></ul></td></tr>',
            '</table>',
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.frameNode = this.wrap.firstChild;
        this.elNode = Ext.get(this.wrap).child('div.x-tree-node-el', true);
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.iconNode = cs[1];
        this.ctNode = Ext.get(this.frameNode).child('ul.x-tree-node-ct', true);
        var index = 2;
        if (cb){
            this.checkbox = cs[index];
			// fix for IE6
			this.checkbox.defaultChecked = this.checkbox.checked;
            index++;
        }
        this.anchor = cs[index];
        this.textNode = this.anchor.firstChild;
        this.ecNode = this.anchor.nextSibling;
		this.ecNode.style.display = a.allowExpand==false ? 'none': '';
    },
	setExpandable : function(expandable){
		this.node.attributes.allowExpand = expandable==true;
		this.node.collapse();
		this.node.ui.ecNode.style.display= expandable==true?'':'none';
		this.node.expand();
	}
});

