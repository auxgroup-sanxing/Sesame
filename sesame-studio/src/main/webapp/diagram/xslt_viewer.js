Ext.BLANK_IMAGE_URL='../images/s.gif';

Ext.tree.ColumnTree = Ext.extend(Ext.tree.TreePanel, {
    lines:false,
    borderWidth: Ext.isBorderBox ? 0 : 2, // the combined left/right border for each cell
    cls:'x-column-tree',
    
    onRender : function(){
        Ext.tree.ColumnTree.superclass.onRender.apply(this, arguments);
        this.headers = this.body.createChild(
            {cls:'x-tree-headers'},this.innerCt.dom);

        var cols = this.columns, c;
        var totalWidth = 0;

        for(var i = 0, len = cols.length; i < len; i++){
             c = cols[i];
             totalWidth += c.width;
             this.headers.createChild({
                 cls:'x-tree-hd ' + (c.cls?c.cls+'-hd':''),
                 cn: {
                     cls:'x-tree-hd-text',
                     html: c.header
                 },
                 style:'width:'+(c.width-this.borderWidth)+'px;'
             });
        }
        this.headers.createChild({cls:'x-clear'});
        // prevent floats from wrapping when clipped
        this.headers.setWidth(totalWidth);
        this.innerCt.setWidth(totalWidth);
    }
});

Ext.tree.ColumnNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
    focus: Ext.emptyFn, // prevent odd scrolling behavior
    // private
    getDDHandles : function(){
        return  Ext.fly(this.wrap).query(".x-tree-col, .x-tree-col-text"); //[this.iconNode, this.textNode, this.elNode, this.elNode.nextSibling, this.wrap.lastChild];
    },
    // private
    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';

        var t = n.getOwnerTree();
        var cols = t.columns;
        var bw = t.borderWidth;
        var c = cols[0];

        var buf = [
             '<li class="x-tree-node"><div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf ', a.cls,'">',
                '<div class="x-tree-col" style="width:',c.width-bw,'px;">',
                    '<span class="x-tree-node-indent">',this.indentMarkup,"</span>",
                    '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow">',
                    '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on">',
                    '<a hidefocus="on" class="x-tree-node-anchor" href="',a.href ? a.href : "#",'" tabIndex="1" ',
                    a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '>',
                    '<span unselectable="on">', n.text || (c.renderer ? c.renderer(a[c.dataIndex], n, a) : a[c.dataIndex]),"</span></a>",
                "</div>"];
         for(var i = 1, len = cols.length; i < len; i++){
             c = cols[i];

             buf.push('<div class="x-tree-col ',(c.cls?c.cls:''),'" style="width:',c.width-bw,'px;">',
                        '<div class="x-tree-col-text">',(c.renderer ? c.renderer(a[c.dataIndex], n, a) : a[c.dataIndex]),"</div>",
                      "</div>");
         }
         buf.push(
            '<div class="x-clear"></div></div>',
            '<ul class="x-tree-node-ct" style="display:none;"></ul>',
            "</li>");

        if(bulkRender !== true && n.nextSibling && n.nextSibling.ui.getEl()){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin",
                                n.nextSibling.ui.getEl(), buf.join(""));
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf.join(""));
        }

        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.firstChild.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1];
        this.iconNode = cs[2];
        this.anchor = cs[3];
        this.textNode = cs[3].firstChild;
    }
});


var Viewer = function(){
//变量
var layout, xsltTree, sourceTree, targetTree;

var Loader = {
	map : {
		'all' : function(el, parentNode, refNode){
			var node = new Ext.tree.AsyncTreeNode({text:'所有', iconCls: 'xsd-icon-all', tag:'all', children:[]});
			parentNode.insertBefore(node, refNode);
			Loader.loadNodes(el, node);
		},
		'choice' : function(el, parentNode, refNode){
			var node = new Ext.tree.AsyncTreeNode({text:'选择', iconCls: 'xsd-icon-choice', 
				tag:'choice', minOccurs: el.getAttribute('minOccurs'), maxOccurs: el.getAttribute('maxOccurs'),
				children:[]
			});
			parentNode.insertBefore(node, refNode);
			Loader.loadNodes(el, node);
		},
		'complexType' : function(el, parentNode, refNode){
			Loader.loadNodes(el, parentNode);
		},
		'element' : function(el, parentNode, refNode){
			var documentation = Loader.loadDocumentation(el);
			var node = new Ext.tree.AsyncTreeNode({
				uiProvider: Ext.tree.ColumnNodeUI,
				text: el.getAttribute('name')+' '+documentation,
				func: '',
				value: ' ',
				iconCls: 'xsd-icon-element',
				tag: 'element',
				name: el.getAttribute('name'),
				type: el.getAttribute('type'),
				documentation: documentation,
				minOccurs: el.getAttribute('minOccurs'),
				maxOccurs: el.getAttribute('maxOccurs'),
				expanded: el.getAttribute('type')?true:false,
				children:[]
			});
			parentNode.insertBefore(node, refNode);
			Loader.loadNodes(el, node);
			//if (node.childNodes.length==0) node.expand();
		},
		'group' : function(el, parentNode, refNode){
			var node = Viewer.createGroupNode();
			parentNode.insertBefore(node, refNode);
			el.getAttribute('expanded')=='true' ? node.expand() : node.collapse();
			Loader.loadNodes(el, node);
		},
		'sequence' : function(el, parentNode, refNode){
			var node = new Ext.tree.AsyncTreeNode({text:'序列', iconCls: 'xsd-icon-sequence',
				tag:'sequence', minOccurs: el.getAttribute('minOccurs'), maxOccurs: el.getAttribute('maxOccurs'),
				children:[]
			});
			parentNode.insertBefore(node, refNode);
			Loader.loadNodes(el, node);
		}
	},
	
	loadDocumentation : function(el){
		var annEl,docuEl;
		return (annEl=XDom.firstElement(el, 'annotation'))?((docuEl=XDom.firstElement(annEl, 'documentation'))?docuEl.text || docuEl.textContent:''):'';
	},
	loadNodes : function(parentEl, parentNode, refNode){
		var el = parentEl.firstChild;
		while (el) {
			var func = this.map[el.nodeName];
			if (func) func(el, parentNode, refNode);
			el = el.nextSibling;
		}
	}
};

var Saver = {
	map : {
		'all' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
		},
		'choice' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			if (a.minOccurs) element.setAttribute("minOccurs", a.minOccurs);
			if (a.maxOccurs) element.setAttribute("maxOccurs", a.maxOccurs);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
		},
		'complexType' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			if (a.name) element.setAttribute("name", a.name);
			if (a.documentation) Saver.saveDocumentation(a.documentation, element);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
		},
		'element' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			if (a.ref) {
				element.setAttribute("ref", a.ref);
			}
			else {
				element.setAttribute("name", a.name);
				if (a.type) element.setAttribute("type", a.type);
			}
			if (a.minOccurs) element.setAttribute("minOccurs", a.minOccurs);
			if (a.maxOccurs) element.setAttribute("maxOccurs", a.maxOccurs);
			if (a.documentation) Saver.saveDocumentation(a.documentation, element);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
		},
		'group' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
		},
		'sequence' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			if (a.minOccurs) element.setAttribute("minOccurs", a.minOccurs);
			if (a.maxOccurs) element.setAttribute("maxOccurs", a.maxOccurs);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
		},
		'simpleType' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			parentEl.appendChild(element);
			element.setAttribute("name", a.name);
			if (a.documentation) Saver.saveDocumentation(a.documentation, element);
			if (a.restriction) {
				var restr = a.restriction;
				var restriction = parentEl.ownerDocument.createElement('restriction');
				element.appendChild(restriction);
				restriction.setAttribute('base', restr.base);
				if (restr.base=='string' || restr.base=='hexBinary'){
					if (restr.minLength) {
						var minEl = parentEl.ownerDocument.createElement('minLength');  restriction.appendChild(minEl);
						minEl.setAttribute('value', restr.minLength);
					}
					if (restr.maxLength) {
						var maxEl = parentEl.ownerDocument.createElement('maxLength');  restriction.appendChild(maxEl);
						maxEl.setAttribute('value', restr.maxLength);
					}
				}
				else if (restr.base!='boolean'){
					if (restr.minInclusive) {
						var minEl = parentEl.ownerDocument.createElement('minInclusive');  restriction.appendChild(minEl);
						minEl.setAttribute('value', restr.minInclusive);
					}
					if (restr.maxInclusive) {
						var maxEl = parentEl.ownerDocument.createElement('maxInclusive');  restriction.appendChild(maxEl);
						maxEl.setAttribute('value', restr.maxInclusive);
					}
				}
				for (var i=0; i<restr.enumerations.length; i++) {
					var item = restr.enumerations[i];  
					var enumEl = parentEl.ownerDocument.createElement('enumeration');   restriction.appendChild(enumEl);
					enumEl.setAttribute('value', item[0]);
				}
				for (var i=0; i<restr.patterns.length; i++) {
					var item = restr.patterns[i];
					var patternEl = parentEl.ownerDocument.createElement('pattern');  restriction.appendChild(patternEl);
					patternEl.setAttribute('value', item[0]);
				}
			}
			else if (a.list) {
				var list = parentEl.ownerDocument.createElement('list');
				element.appendChild(list);
				list.setAttribute('itemType', a.base);
				for (var m in a.list) {
					list.setAttribute(m, a.list[m]);
				}
			}
			else if (a.union) {
				var union = parentEl.ownerDocument.createElement('union');
				element.appendChild(union);
				union.setAttribute('memberTypes', a.union.memberTypes);
			}
		}
	},
	saveDocumentation : function(documentation, parentEl)
	{
		var annotation = parentEl.ownerDocument.createElement('annotation');
		parentEl.appendChild(annotation);
		var docuEl = parentEl.ownerDocument.createElement('documentation');
		annotation.appendChild(docuEl);
		docuEl.appendChild(parentEl.ownerDocument.createTextNode(documentation));
	},
	saveNodes : function(parentNode, parentEl){
		var node = parentNode.firstChild;
		while (node) {
			var func = this.map[node.attributes.tag];
			if (func) func(node, parentEl);
			node = node.nextSibling;
		}
	}
};

return {
	imagePath : '../package/schema/resources/images/',

	init : function(){
		var _this = this;

		Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		Ext.QuickTips.init();
			
		var viewport = new Ext.Viewport({
			layout: 'border',
			stateId: 'xslt_viewer',
			items:[
				{
					region:'south',
					html: '<div id="status"  style="background-color:yellow;"></div>',
					autoScroll: true,
				    split: true,
				    height: 100,
				    minSize: 100,
				    maxSize: 200,
				    collapsible: true,
				    title:'信息',
				    margins:'0 0 0 0'
				},
				{
					region: 'center',
					//xtype: 'treepanel',
					id: 'transform_pannel',
					//loader: new Ext.tree.TreeLoader({clearOnLoad: false}),
					rootVisible: false,
					lines: false,
					//imagePath: this.imagePath,
					containerScroll: true,
					animate: false,
					autoScroll: false,
					cls: 'xslt-tree',
					//bodyStyle: 'background-color:white;',
					//enableDD: true,
					enableDrop: true,
					//dragConfig: this.getDragConfig(),
					//dropConfig: this.getDropConfig(),
					hlDrop: false,
					//selModel: new Ext.tree.MultiSelectionModel(),
					trackMouseOver: false,
					/*tbar: this.getToolbar(),
					root: new Ext.tree.AsyncTreeNode({
						id: 'ROOT',
						text: '转换表',
						tag: 'transform',
						expanded: true,
						allowDrag: false,
						allowDrop: false,
						children: []
					})*/
					layout: 'fit',
					items:{
						xtype: 'examplecanvas'
					}
				},
				{
					region: 'east',
					split: true,
					width: 200,
					minSize: 175,	//maxSize: 400,
					collapsible: false,
					margins: '0 0 0 0',
					xtype: 'treepanel',
					iconCls: 'xsd-palette-category',
					title: '源文档',
					cls: 'rtl-tree',
					animate: false,
					border: true,
					loader: new Ext.tree.TreeLoader(),
					id: 'source-tree',
					rootVisible: true,
					lines: false,
					autoScroll: true,
					enableDrag: true,
					trackMouseOver: false,
					root: new Ext.tree.AsyncTreeNode({
					    text: 'Context',
						iconCls: 'xsd-icon-element',
					    children: this.getStandardActions()
					})
				},
				new Ext.tree.ColumnTree({
					region: 'west',
					split: true,
					iconCls: 'xsd-palette-category',
					//title: '目标文档',
					width: 200,
					minSize: 175,
					margins: '0 0 0 0',
					xtype: 'treepanel',
					iconCls: 'xsd-palette-category',
					animate: false,
					border: true,
					loader: new Ext.tree.TreeLoader({
						clearOnLoad: false,
						uiProviders: { 'col': Ext.tree.ColumnNodeUI}
					}),
					id: 'target-tree',
					rootVisible: true,
					lines: true,
					autoScroll: true,
					enableDrop: true,
					dropConfig: this.getDropConfig(),
					hlDrop: false,
					trackMouseOver: false,
					tbar: this.getToolbar(),
					columns:[{
					    header:'目标消息',
					    width:350,
					    dataIndex:'text'
					},{
					    header:'函数',
					    width:120,
					    dataIndex:'func',
						renderer: null
					},{
					    header:'值',
					    width:220,
					    dataIndex:'value'
					}],
					root: new Ext.tree.AsyncTreeNode({
					    text: 'Context',
						iconCls: 'xsd-icon-element',
						uiProvider: Ext.tree.ColumnNodeUI,
					    children: []
					})
				})
			]
		});
		
		xsltTree = viewport.findById('transform_pannel');
		targetTree = viewport.findById('target-tree');
		sourceTree = viewport.findById('source-tree');
		var topBtns = targetTree.getTopToolbar().items.map;
		var selModel = xsltTree.selModel;
		//var rootNode = xsltTree.getRootNode();

		targetTree.on('nodedragover', function(e){
			//return e.point=='append';
			//var a = (e.point=='append') ? e.target.attributes : e.target.parentNode.attributes;
		});
		targetTree.on('beforenodedrop', function(e){
			if (e.dropNode.getOwnerTree() == e.tree) 
			{
				return true;
			}
			else if (e.dropNode.getOwnerTree() == sourceTree) 
			{
				e.cancel = true;
				e.dropStatus = true;

				var a = e.dropNode.attributes;
				var elNode = e.target.ui.elNode;
				
				//alert(elNode.childNodes[2].firstChild.innerHTML);
				elNode.childNodes[2].firstChild.innerHTML= a.text;
				e.target.attributes.value = a.text;
/*
				var node = new Ext.tree.AsyncTreeNode({
					tag: 'template',
					iconCls: 'xslt-icon-none',
					cls: 'xslt-line-horz',
					uiProvider: Ext.ux.MappingUI,
					expanded: true,
					children: []
				});
				xsltTree.root.appendChild(node);
*/
			}
		});
		xsltTree.on('append', function(tree, parent, node, index){
			topBtns.save.enable();
		});
		xsltTree.on('insert', function(tree, parent, node, ref){
			topBtns.save.enable();
		});
		xsltTree.on('movenode', function(tree, oldParent, newParent, index){
			topBtns.save.enable();
		});
		xsltTree.on('remove', function(tree, parent, node){
			topBtns.save.enable();
		});
		
		var addStyle_Handler = function(){
			var styleNode = _this.createStyleNode({tag: this.tag});
			var node = selModel.getSelectedNodes()[0];
			node.appendChild(styleNode);
			node.expand();
			styleNode.getUI().iconNode.title = this.tag=='sequence'?'序列':this.tag=='choice'?'选择':this.tag=='all'?'全部':null;
		};
		var addElement_Handler = function(){
			var node = selModel.getSelectedNodes()[0];
			var ref=null, a=node.attributes;
			if (a.tag=='complexType'){
				node = node.firstChild ? node.firstChild : node.appendChild(_this.createStyleNode({tag: 'sequence'}));
			}
			else if (a.tag=='element' || a.tag=='any'){
				ref = node;
				node = node.parentNode;
			}
			var prefix='NewElement', index=0;
			for (var i=0; i<65536; i++)
			{
				if (!node.findChild('name', prefix+i)){
					index=i;  break;
				}
			}
			var elNode = _this.createElementNode({name: prefix+index, type: 'string', documentation:''});
			node.insertBefore(elNode, ref);
			node.expand();
			elNode.select();
		};
		var addElRef_Handler = function(){
			var node = selModel.getSelectedNodes()[0];
			var ref=null, a=node.attributes;
			if (a.tag=='complexType'){
				node = node.firstChild ? node.firstChild : node.appendChild(_this.createStyleNode({tag: 'sequence'}));
			}
			else if (a.tag=='element' || a.tag=='any'){
				ref = node;
				node = node.parentNode;
			}
			var elNode = _this.createElRefNode({ref:'NewElement', documentation:''});
			node.insertBefore(elNode, ref);
			node.expand();
			elNode.select();
		};
		var setStyle_Handler = function(){
			var node = selModel.getSelectedNodes()[0];
			node.attributes.tag = this.id;
			node.getUI().iconNode.title = this.id=='sequence'?'序列':this.id=='choice'?'选择':this.id=='all'?'全部':null;
			node.getUI().iconNode.className = 'x-tree-node-icon xsd-icon-'+this.id;
			topBtns.save.enable();
		};
		var setType_Handler = function(item, e){
			var node = selModel.getSelectedNodes()[0];
			var a = node.attributes;
			var callback = function(record){
				var pre = record.get('pre');
				node.attributes.type = (pre ? pre+':' : '')+record.get('type');
				node.setText(a.name+(a.documentation?'.'+a.documentation:'')+' <b>'+node.attributes.type+'</b>');
				while(node.firstChild) node.firstChild.remove();
				topBtns.save.enable();
			}
			var dlg = _this.getTypeDialog(callback);
			dlg.show();
		};
		var setOccurs_Handler = function(){
			var node = selModel.getSelectedNodes()[0];
			Ext.apply(node.attributes, this.occurs);
			node.getUI().iconNode.src = _this.getOccursIcon(this.occurs.minOccurs, this.occurs.maxOccurs);
			topBtns.save.enable();
		};
		
		this.contextMenu = new Ext.menu.Menu({
			id: 'schemaMenu',
			items: [
				{
					id: 'remove',
					icon:"../images/icons/remove.gif",
					text: '删除',
					handler: topBtns.remove.handler
				},
				'-',
				{
					id: 'addElement',
					text: '添加元素',
					handler: addElement_Handler
				},
				'-',
				{
					id: 'addSeq',
					text: '添加序列',
					tag: 'sequence',
					handler: addStyle_Handler
				},
				{
					id: 'addChoice',
					text: '添加选择',
					tag: 'choice',
					handler: addStyle_Handler
				},
				{
					id: 'setType',
					text: '函数',
					handler: function(){return false;},
					menu: new Ext.menu.Menu({
						items: [
							{
								id: 'newComplex',
								text: 'trim()',
								handler: function(){
									var node = _this.contextMenu.node;
									//node.ui.iconNode.className='x-tree-node-icon xsd-icon-element';
									node.ui.elNode.childNodes[1].firstChild.innerHTML = this.text;
								}
							},
							{
								id: 'browse',
								text: '浏览...',
								handler: setType_Handler
						}]
					})
				},
				{
					id: 'setOccurs',
					text: '出现次数',
					handler: function(){return false;},
					menu: new Ext.menu.Menu({
						items: [
							{
								id: 'setRequired',
								text: '1..1(必须)',
								occurs: {},
								handler: setOccurs_Handler
							},
							{
								id: 'setOptional',
								text: '0..1(可选)',
								occurs: {minOccurs:'0', maxOccurs:'1'},
								handler: setOccurs_Handler
							},
							{
								id: 'set0orMore',
								text: '0..*(零或多次)',
								occurs: {minOccurs:'0', maxOccurs:'unbounded'},
								handler: setOccurs_Handler
							},
							{
								id: 'set1orMore',
								text: '1..*(一或多次)',
								occurs: {minOccurs:'1', maxOccurs:'unbounded'},
								handler: setOccurs_Handler
						}]
					})
				},
				'-',
				{
					id: 'refresh',
					text: '刷新',
					icon:"../images/icons/launch_restart.gif",
					handler: topBtns.reload.handler
				}
			]
		});
		targetTree.on('contextmenu', function(node, e){
			if (!node.isSelected()) node.select();
			_this.contextMenu.node = node;
			
			var nodes = selModel.getSelectedNodes();
			var a = node.attributes;
			var allowRemove = node.attributes.allowRemove;
			var allowElement = nodes.length==1 && (a.id=='ELEMENTS' || a.tag && a.tag!='simpleType');
			var items = _this.contextMenu.items;
			items.get('addElement').setDisabled(!allowElement);
			items.get('addSeq').setVisible(nodes.length<2 && (a.tag=='complexType' || a.tag=='sequence' || a.tag=='choice'));
			items.get('addChoice').setVisible(nodes.length<2 && (a.tag=='complexType' || a.tag=='sequence' || a.tag=='choice'));
			items.get('addSeq').setDisabled(a.tag=='complexType' && node.childNodes.length>0);
			items.get('addChoice').setDisabled(a.tag=='complexType' && node.childNodes.length>0);
			//items.get('setType').setVisible(a.type);
			items.get('setOccurs').setVisible(a.tag=='element' || a.tag=='sequence' || a.tag=='choice' || a.tag=='all');
			items.get('remove').setDisabled(!allowRemove);
			
		    _this.contextMenu.showAt(e.getXY());
		});

		targetTree.el.addKeyListener(Ext.EventObject.DELETE, topBtns.remove.handler, topBtns.remove);
		targetTree.on('dblclick', function(node, e){
			if (node.attributes.allowEdit) {
				switch (node.attributes.tag)
				{
				case 'simpleType':
					_this.getVarietyDialog(node).show();
					break;
				}
				
			}
		});

		//xsltTree.loader.on('loadexception', function(loader, node, response){ _this.showException(response); });
		this.load();
	},
	
	getToolbar : function(){
		var _this = this;
		
		var buttons = [
			{
				id: 'remove',
				text: '删除',
				icon: '../images/icons/remove.gif',
				cls: 'x-btn-text-icon',
				disabled: true,
			    handler: function(){
					if (this.disabled)  return false;
					if (!window.confirm('确实要删除选中的项吗？'))  return false;

					var sels = xsltTree.getSelectionModel().getSelectedNodes();
					for (var i = sels.length - 1; i >= 0; i--) {
						var n = sels[i];
						if (!n || !n.attributes.allowRemove)  continue;
						var a = n.parentNode.attributes;
						if (n.attributes.tag=='complexType' && a.tag=='element'){
							a.type="string";
							n.parentNode.setText(a.toString());
						}
						n.remove();
					}
				}
			},
			'-',
			{
				id: 'reload',
			    text: '刷新',
				icon: '../images/icons/launch_restart.gif',
				cls: 'x-btn-text-icon',
			    handler : function(){
					if (!xsltTree.getTopToolbar().items.get('save').disabled){
						if (!window.confirm('模型已经更改，确实要放弃当前所做的修改吗？'))  return false;
					} 
					_this.load();
				}
			},
			{
				id: 'save',
			    text: '保存',
				icon: '../images/icons/save_edit.gif',
				cls: 'x-btn-text-icon',
				disabled: true,
			    handler : function(){
					_this.save();
			    }
			}
		];
		return buttons;
	},

	getStandardActions : function(){
		return [{
			text: '导入',
			iconCls: 'xsd-icon-import',
			//cls: 'xsd-node-hideIndent',
			create: this.createImportNode,
			leaf: true
		},
		{
			text: '包含',
			iconCls: 'xsd-icon-include',
			//cls: 'xsd-node-hideIndent',
			create: this.createIncludeNode,
			children:[{
				text: 'abc',
				iconCls: 'xsd-icon-element',
				leaf: true
			},{
				text: 'def',
				iconCls: 'xsd-icon-element',
				leaf: true
			}]
		},
		{
			text: '重定义',
			iconCls: 'xsd-icon-redefine',
			//cls: 'xsd-node-hideIndent',
			create: this.createRedefineNode,
			leaf: true
		}];
	},

	getOpenDialog : function(callback){
		if (!this.openDlg) {
			this.openDlg = new Dialog.OpenDialog({
				title: '导入 Schema',
				width: 500,
				height: 280,
				forceSelection: true,
				closeAction: 'hide', 
				url: 'xslt_ctrl.jsp',
				path:  schema.substr(0, schema.indexOf('/')+1)+'schemas/',
				filter: '.*\\.xsd$',
				callback: callback
			});
			this.openDlg.on('exception', function(ex){
				Viewer.showException(ex);
			});
		}
		else {
			this.openDlg.callback = callback;
		}
		return this.openDlg;
	},

	getVarietyDialog : function(node){
		var _this = this;
		
		if (!this.varietyDlg) {
			var titleText = '设置类型';
			
			var typeStore = new Ext.data.SimpleStore({
			    fields: ['type'],
			    data : [['boolean'], ['date'], ['dateTime'], ['double'], ['float'], ['hexBinary'], ['int'], ['string'], ['time'], ['anySimpleType']]
			});
			
			var restrictView = new Ext.DataView({
				store: new Ext.data.SimpleStore({
				    fields: ['item'],
				    data : []
				}),
				tpl: new Ext.XTemplate('<tpl for=".">',
				    '<div class="thumb-wrap x-view-item" id="{item}">',
				    '<div class="thumb x-unselectable" unselectable="on" style="line-height:18px;">',
					'<img style="width:16px; height:16px; vertical-align:middle;" class="xsd-icon-item" src="'+Ext.BLANK_IMAGE_URL+'" />',
				    '<span class="x-editable" style="vertical-align:middle;">{item}</span></div></div>',
				'</tpl>',
				'<div class="x-clear"></div>'),
				multiSelect: false,
				singleSelect: true,
				cls: 'x-data-view',
				overClass: 'x-view-over',
				itemSelector: 'div.thumb-wrap',
				listeners: {
					containerclick: function(sender, e){ e.stopEvent(); },
					selectionchange: function(sender, selections){
						Ext.getCmp('btnDelete').setDisabled(selections.length<1);
						Ext.getCmp('btnEdit').setDisabled(selections.length<1);
					}
				}
			});
			
		    var dlg = new Ext.Window({
		        autoCreate : true,
		        title: titleText,
		        resizable:true,
		        constrain:true,
		        constrainHeader:true,
		        minimizable: false,
		        maximizable: false,
		        stateful: false,
		        modal: true,
		        buttonAlign: "right",
				defaultButton: 0,
		        width: 450,
		        height: 460,
		        minHeight: 300,
				minWidth: 300,
		        footer: true,
		        closable: true,
		        closeAction: 'hide',
				plain: true,
				layout: 'fit',
				items: new Ext.TabPanel({
					activeItem: 0,
					border: false,
					layoutOnTabChange: true,
					items: [{
						id: 'restriction',
						xtype: 'form',
						autoScroll: true,
					    title: '原子类型',
						labelWidth: 75,
						border: false,
						bodyStyle: 'padding:10px;',
						defaultType: 'textfield',
						defaults: { anchor: '-18' },
						items: [
							{
								fieldLabel: '基本类型',
						        name: 'base',
								xtype: 'combo',
								allowBlank: false,
								forceSelection: true,
								store: typeStore,
								mode: 'local',
								triggerAction: 'all',
								valueField: 'type', displayField: 'type',
								listeners: {
									valid: function(sender){
										var formPanel = sender.ownerCt, value = sender.getValue();
										formPanel.getComponent('lengthRectrict').setVisible(value=='string' || value=='hexBinary');
										formPanel.getComponent('scopeRectrict').setVisible(value!='string' && value!='hexBinary' && value!='boolean');
									}
								}
						    },{
								xtype: 'fieldset',
								id: 'lengthRectrict',
								title: '长度约束',
								collapsible: false,
								autoHeight: true,
								defaults: { anchor: '100%' },
								defaultType: 'textfield',
								items:[{
										xtype:'numberfield',
										allowDecimals: false,
								        fieldLabel: '最小长度',
								        name: 'minLength'
								    },{
								        xtype:'numberfield',
										allowDecimals: false,
										fieldLabel: '最大长度',
								        name: 'maxLength'
								    }]
						    },{
								xtype: 'fieldset',
								id: 'scopeRectrict',
								title: '范围约束',
								collapsible: false,
								autoHeight: true,
								defaults: { anchor: '100%' },
								defaultType: 'textfield',
								items:[{
										xtype:'numberfield',
								        fieldLabel: '最小值',
								        name: 'minInclusive'
								    },{
								        xtype:'numberfield',
										fieldLabel: '最大值',
								        name: 'maxInclusive'
								    }]
							},{
								xtype:'fieldset',
								title: '特殊约束',
								collapsible: false,
								defaults: {border: false},
								style: 'height: 180px;',
								layout: 'column',
								items:[{
										columnWidth: 0.3,
										autoHeight: true,
										border: false,
										cls: 'x-restrict-tool',
										style: 'height:100%;', 
										defaults: { anchor: '100%' },
										items: [{
											xtype: 'label',
											cls: 'x-label',
											text: '限制方式:'
										},{
											xtype: 'radio',
											boxLabel: '枚举',
									        name: 'spectial',
									        value: 'enumerations',
											id: 'enumerations-id',
											listeners: { 
												check: function(){
													var a = dlg.node.attributes;
													if (this.getValue())
														restrictView.store.loadData(a.restriction.enumerations);
												}
											}
										},{
											xtype: 'radio',
									        hideLabel: true,
											boxLabel: '模式',
									        name: 'spectial',
									        value: 'patterns',
											id: 'patterns-id',
											listeners: { 
												check: function(){
													var a = dlg.node.attributes;
													if (this.getValue())
														restrictView.store.loadData(a.restriction.patterns);
													else
														restrictView.store.removeAll();
												}
											}
										},{
											xtype: 'button',
											text: '添加',
											handler: function(){
												var info = (this.ownerCt.getComponent('patterns-id').getValue()?'正则表达式':'枚举值');
												var value = window.prompt("请输入"+info, "");
												if (value != null)
													restrictView.store.loadData([[value]], true);
												else
													restrictView.store.removeAll();
											}
										},{
											xtype: 'button',
											id: 'btnDelete',
											disabled: true,
											text: '删除',
											handler: function(){
												restrictView.store.remove(restrictView.getSelectedRecords()[0])
											}
										},{
											xtype: 'button',
											id: 'btnEdit',
											disabled: true,
											text: '编辑',
											handler: function(){
												var info = (this.ownerCt.getComponent('patterns-id').getValue()?'正则表达式':'枚举值');
												var rec = restrictView.getSelectedRecords()[0];
												var value = window.prompt("请输入"+info, rec.get('item'));
												if (value != null)	rec.set('item', value);
											}
										}]
								    },
									{
										columnWidth: 0.7,
										autoHeight: true,
										//style: 'height:auto;',
										items: restrictView
									}
								]
							}]
						},
						{
							id: 'list',
							title: '列表',
							autoScroll: true,
							border: false,
							xtype: 'form',
							bodyStyle: 'padding:10px;',
							labelWidth: 75,
							items: new Ext.form.ComboBox({
								fieldLabel: '列表项类型',
						        name: 'itemType',
								anchor: '100%',
						        allowBlank: false,
								forceSelection: true,
								store: typeStore, 
								mode: 'local',
								triggerAction: 'all',
								valueField: 'type', displayField: 'type'
						    })
						},
						{
							id: 'union',
							title: '联合类型',
							border: false,
							xtype: 'form',
							bodyStyle: 'padding:10px;',
							labelWidth: 75,
							items: [{
									xtype:'textfield',
							        fieldLabel: '成员类型',
									anchor: '100%',
							        allowBlank: false,
							        name: 'memberTypes'
							    },
								{
									xtype: 'combo',
									fieldLabel: '可选类型',
							        name: 'memberType',
									anchor: '100%',
									forceSelection: true,
									editable: false,
									store: typeStore, 
									mode: 'local',
									triggerAction: 'all',
									valueField: 'type', displayField: 'type',
									listeners: {
										select: function(sender){
											var formPanel = sender.ownerCt, value = sender.getValue();
											var field = formPanel.getForm().findField('memberTypes');
											field.setValue(field.getValue()+(field.getValue()?' ':'')+value);
										}
									}
						    }]
						}
					]
				})
		    });
			
		    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:false}, function(){ 
				if (!dlg.node) return false;
				var a = dlg.node.attributes;
				var tabPanel = dlg.getComponent(0);
				var tab = tabPanel.getActiveTab(), form = tab.getForm();
				if (!form.isValid()) return false;
				switch (tab.getId())
				{
					case 'restriction':
						a.restriction = Ext.apply({enumerations:[], patterns:[]}, form.getValues());
						a.restriction.spectial = form.findField('spectial').getValue() ? 'enumerations' : 'patterns';
						var array = a.restriction[a.restriction.spectial];
						restrictView.store.each(function(r){
							array.push([r.get('item')]);
						});
						a.list = undefined;  a.union = undefined;
						break;
					case 'list':
						a.list = form.getValues();
						a.restriction = undefined;  a.union = undefined;
						break;
					case 'union':
						a.union = form.getValues();
						a.restriction = undefined;  a.list = undefined;
						break;
				}
				dlg.node.setText(a.name+(a.documentation?'.'+a.documentation:'')+' : '+_this.getTypeDesc(a));
				xsltTree.getTopToolbar().items.get('save').enable();
				dlg.hide();
			});
		    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){ dlg.hide(); });
		    
			dlg.typeStore = typeStore;
			dlg.render(Ext.getBody());
		    this.varietyDlg = dlg;
		}
		else {
			//this.varietyDlg.typeStore.load({add:true});
		}
		this.varietyDlg.node = node;
		var a = node.attributes;
		var tabPanel = this.varietyDlg.getComponent(0);
		tabPanel.setActiveTab(a.list ? 'list' : a.union ? 'union' : 'restriction');
		var restrictForm = tabPanel.getComponent('restriction').getForm();
		restrictForm.setValues(a.restriction);
		if (a.restriction) {
			var radio = restrictForm.findField(a.restriction.spectial + '-id');
			radio ? radio.setValue(true) : '';
		}
		var listForm = tabPanel.getComponent('list').getForm();
		listForm.setValues(a.list);
		var unionForm = tabPanel.getComponent('union').getForm();
		unionForm.setValues(a.union);
        return this.varietyDlg;
	},
	
	getRelativePath : function(base, dest)
	{
		var baseArray = base.split('/');
		var destArray = dest.split('/');
		var i = 0;
		while (i<baseArray.length && i<destArray.length && baseArray[i]==destArray[i]) {
			i++;
		}
		var result = '', index = i;
		while (i<baseArray.length-1) {
			result += '../';
			i++;
		}
		while (index<destArray.length) {
			result += destArray[index]+(index<destArray.length-1?'/':'');
			index++;
		}
		return result;
	},

	getTypeDialog : function(callback){
		var selNode = xsltTree.selModel.getSelectedNodes()[0];
		var internalTypes = [];
		var typesNode = xsltTree.getNodeById("TYPES");
		typesNode.eachChild(function(node){
			if (selNode.isAncestor(node)) return true;
			var a = node.attributes;
			internalTypes.push({
				cls:a.tag=='complexType'?'xsd-icon-complextype':'xsd-icon-simpletype', 
				type: a.name, desc: a.documentation,
				pre: 'tns'
			});
		});
		
		if (!this.typeDlg){
			var titleText = '设置类型';
			
			var tpl = new Ext.XTemplate(
				'<tpl for=".">',
				    '<div class="thumb-wrap x-view-item" id="{type}">',
				    '<div class="thumb x-unselectable" unselectable="on" style="vertical-align:middle; line-height:18px;">',
					'<img style="width:16px; height:16px; vertical-align:middle;" class="{cls}" src="'+Ext.BLANK_IMAGE_URL+'" />',
				    '<span class="x-editable" style="vertical-align:middle;">{type} <font color="blue">{desc}</font></span></div></div>',
				'</tpl>',
				'<div class="x-clear"></div>'
			);
			var a = xsltTree.getRootNode().attributes;
			var typeStore = this.getTypeStore(a.xmlns);
			var dataView = new Ext.DataView({
				store: typeStore,
				tpl: tpl,
				style: 'overflow:auto;',
				autoHeight: false,
				multiSelect: false,
				singleSelect: true,
				overClass: 'x-view-over',
				itemSelector: 'div.thumb-wrap',
				emptyText: '没有可选类型',
				listeners: {
					containerclick: function(sender, e){
						var form = dlg.getComponent('detailForm').getForm();
						form.reset();
					},
					beforeselect: function(sender, node, selections){
						var form = dlg.getComponent('detailForm').getForm();
						var record =  sender.getRecord(node);
						var directives = xsltTree.getRootNode().findChild('id', 'DIRECTIVES');
						var impNode = directives.findChild('prefix', record.get('pre'));
						form.setValues({location: impNode?impNode.attributes.schemaLocation:''});
					},
					selectionchange: function(sender, selections){
						dlg.ok.setDisabled(selections.length<1);
					}
				}
			});
			
		    var dlg = new Ext.Window({
		        autoCreate : true,
		        title:titleText,
		        constrain:true, constrainHeader:true,
		        minimizable: false, maximizable: false, resizable:true,
		        stateful: false, modal: true, shim:true,
		        buttonAlign: "right",	defaultButton: 0,
		        width: 350, height: 450, minHeight: 200,
		        footer: true,
		        closable: true,
		        closeAction: 'hide',
				layout: 'border',
				items: [
					{
						region: 'north',
						xtype: 'form',
						labelAlign: 'top',
						bodyStyle: 'background-color: transparent;',
					    split: false,
					    collapsible: false,
					    margins:'5 0 0 0',
						autoHeight: true,
						border: false,
						defaultType: 'textfield',
						items: [{
							fieldLabel: '查找 ( * 匹配任意字符)',
							anchor: '100%',
					        name: 'first',
					        allowBlank:true,
							listeners: {
								valid: function(sender){
									try { typeStore.filter('type',  new RegExp(sender.getValue().replace('*', '.*'), 'i')); } catch(e){}
								}
							}
					    }]
					},
					{
						id: 'detailForm',
						region: 'south',
						xtype: 'form',
						labelAlign: 'top',
					    split: false,
					    collapsible: false,
					    margins:'5 0 0 0',
						autoHeight: true,
						border: false,
						defaultType: 'textfield',
						bodyStyle: 'background-color: transparent;',
						items: [{
							fieldLabel: '声明位置',
							anchor: '100%',
					        name: 'location',
					        disabled: true
					    }]
					},
					{
						region: 'center',
						layout: 'fit',
						items: dataView
					}
				]
		    });
		    dlg.ok = dlg.addButton({text:Ext.Msg.buttonText.ok, disabled:true}, function(){ 
				if (dlg.callback) dlg.callback(dataView.getSelectedRecords()[0]);
				dlg.hide();
			});
		    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){ dlg.hide(); });
		    dlg.render(document.body);
			dlg.typeStore = typeStore;
			typeStore.loadData({items:internalTypes});
			typeStore.load({add:true});
		    this.typeDlg = dlg;
		}
		else {
			this.typeDlg.typeStore.loadData({items:internalTypes});
			this.typeDlg.typeStore.load({add:true});
		}
		this.typeDlg.callback = callback;
        return this.typeDlg;
	},
	
	getTypeStore: function(xmlns){
		var store = new Ext.data.JsonStore({
			url: 'xslt_ctrl.jsp',
			baseParams: {	operation:'loadSchemaTypes', schema:schema, xmlns:xmlns},
			root: 'items',
			fields: [
				{name:'cls', type: 'string'},
				{name:'type', type:'string'},
				{name:'pre', type:'string'},
				{name:'desc', type:'string'}
			]
		});
		store.on('loadexception', function(proxy, obj, response){ Viewer.showException(response); });
		store.on('load', function(sender, records, options){
			if (options.add) sender.sort("type", "ASC");
		});
		store.on('beforeload', function(sender){
			var directives = xsltTree.getRootNode().findChild('id', 'DIRECTIVES');
			var location = {};
			directives.eachChild(function(child){
				var a=child.attributes;
				if (a.prefix) location[a.prefix] = a.schemaLocation;
			});
			this.baseParams['location']=Ext.encode(location);  
		});
		return store;
	},
	
	createElementNode : function(config){
		var a = (typeof config)=='object' ? config : {documentation:''};
		a.toString=function(){
			return this.name + (this.documentation ? ' ' + this.documentation : '') + (this.type ? ' <b>' + this.type + '</b>' : '');
		};
		if (!a.children) {
			a.children = [];  a.expanded = true;
		}
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			icon: this.getOccursIcon(a.minOccurs, a.maxOccurs),
			iconCls: 'xsd-icon-element', 
			uiProvider: Ext.ux.SchemaElementUI,
			tag: 'element',
			text: a.toString(),
			allowDrag: true,
			allowChildren: true,
			allowEdit: true,
			allowRemove: true,
			leaf: false
		}));
	},

	createElRefNode : function(config){
		var a = (typeof config)=='object' ? config : {documentation:''};
		a.toString=function(){
			return this.ref + (this.documentation ? ' ' + this.documentation : '');
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			icon: this.getOccursIcon(a.minOccurs, a.maxOccurs),
			iconCls: 'xsd-icon-ref', 
			uiProvider: Ext.ux.SchemaRefUI,
			tag: 'element',
			text: a.toString(),
			expandable: true,
			expanded: true,
			allowDrag: true,
			allowChildren: true,
			allowEdit: true,
			allowRemove: true,
			children: []
		}));
	},

	createImportNode : function(config){
		var a = (typeof config)=='object' ? config : {schemaLocation:'', namespace:'', prefix:''};
		a.toString=function(){
			return this.schemaLocation?this.schemaLocation+' {'+this.namespace+'}':"没有指定位置";
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			text: a.toString(),
			tag: 'import',
			iconCls: 'xsd-icon-import', 
			uiProvider: Ext.ux.SchemaNodeUI,
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			leaf: true
		}));
	},
	//创建元素出现样式节点：sequence, choice, all
	createStyleNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			icon: this.getOccursIcon(a.minOccurs, a.maxOccurs),
			iconCls: 'xsd-icon-'+a.tag, 
			uiProvider: Ext.ux.SchemaStyleUI,
			expanded: true,
			allowDrag: false,
			allowChildren: true,
			allowRemove: true,
			children: []
		}));
	},
	
	createGroupNode : function(){
		return new Ext.tree.AsyncTreeNode({
			text: "分组", 
			icon: Viewer.imagePath+'group_collapsed.gif',
			uiProvider: Ext.ux.GroupNodeUI,
			name: 'group',
			allowEdit: true,
			allowRemove: true,
			expanded: true,
			children: [{
				cls: 'processNodeLabelHidden',
				uiProvider: Ext.ux.ProcessNodeUI,
				leaf: true, 
				allowChildren: false, 
				allowDrag: false
			}]
		});
	},
	
	createIncludeNode : function(config){
		var a = (typeof config)=='object' ? config : {schemaLocation:''};
		a.toString=function(){
			return this.schemaLocation?this.schemaLocation+' {'+this.namespace+'}':"没有指定位置";
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			text: a.toString(),
			tag: 'include',
			qtip: '包含',
			iconCls: 'xsd-icon-include', 
			uiProvider: Ext.ux.SchemaNodeUI,
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			leaf: true
		}));
	},
	
	createAnyNode : function(){
		return new Ext.tree.AsyncTreeNode({
			text: "任何内容", 
			iconCls: 'xsd-icon-any', 
			uiProvider: Ext.ux.SchemaNodeUI,
			tag: 'any',
			allowDrag: true,
			allowRemove: true,
			leaf: true
		});
	},
	
	createRedefineNode : function(config){
		var a = (typeof config)=='object' ? config : {schemaLocation:''};
		a.toString=function(){
			return this.schemaLocation?this.schemaLocation+' {'+this.namespace+'}':"没有指定位置";
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			text: a.toString(),
			tag: 'redefine',
			iconCls: 'xsd-icon-redefine', 
			uiProvider: Ext.ux.SchemaNodeUI,
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			leaf: true
		}));
	},
	
	createComplextypeNode : function(config){
		var a = (typeof config)=='object' ? config : {documentation:''};
		a.toString=function(){
			return this.name + (this.documentation ? ' ' + this.documentation : '');
		};
		if (!a.children) {
			a.children = [];  a.expanded = true;
		}
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			iconCls: 'xsd-icon-complextype', 
			uiProvider: Ext.ux.SchemaComplextypeUI,
			tag: 'complexType',
			text: a.text || a.toString(),
			allowDrag: true,
			allowChildren: true,
			allowEdit: true,
			allowRemove: true,
			leaf: false
		}));
	},

	createSimpletypeNode : function(config){
		var a = (typeof config)=='object' ? config : {documentation:''};
		if (!a.children) {
			a.children = [];
		}
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			iconCls: 'xsd-icon-simpletype', 
			uiProvider: Ext.ux.SchemaNodeUI,
			tag: 'simpleType',
			text: a.text || a.name+(a.documentation?'.'+a.documentation:'')+' : '+this.getTypeDesc(a),
			variety: a.restriction?'原子类型':a.list?'列表':a.union?'联合类型':'',
			expanded: true,
			allowDrag: true,
			allowChildren: true,
			allowEdit: true,
			allowRemove: true,
			leaf: false
		}));
	},

	getTypeDesc: function(a) {
		if (a.restriction) {
			return '<b>'+a.restriction.base+'</b>'
		}
		if (a.list) {
			return '<b>['+a.list.itemType+']</b>'
		}
		if (a.union) {
			return '<b>{'+a.union.memberTypes+'}</b>'
		}
	},

	getDragConfig : function(){
		return {
		    afterRepair : function(){
		        this.dragging = false;
		    }
		};
	},
	
	getDropConfig : function(){
		return {
			//overClass: 'processViewDropSite',
			
			onNodeOver: function(n, dd, e, data){
				var pt = this.getDropPoint(e, n, dd);
				var node = n.node;
				
				// auto node expand check
				if (!this.expandProcId && pt == "append" && node.hasChildNodes() && !n.node.isExpanded()) {
					this.queueExpand(node);
				}
				else 
					if (pt != "append") {
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
						else 
							if (pt == "below") {
								returnCls = "x-tree-drop-ok-append";  //n.node.isLast() ? "x-tree-drop-ok-below" : "x-tree-drop-ok-between";
								cls = "x-tree-drag-insert-below";
							}
							else {
								returnCls = "x-tree-drop-ok-append";
								cls = "x-tree-drag-append";
							}
						cls = "x-tree-drag-append";
						
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
	},

	getOccursIcon : function(minOccurs, maxOccurs){
		if (minOccurs=='1' && maxOccurs=='1')
			return this.imagePath+'XSDOccurrenceOne.gif';
		else if (minOccurs=='0' && maxOccurs=='1')
			return this.imagePath+'XSDOccurrenceZeroToOne.gif';
		else if (minOccurs=='0' && maxOccurs=='unbounded')
			return this.imagePath+'XSDOccurrenceZeroToUnbounded.gif';
		else if (minOccurs=='1' && maxOccurs=='unbounded')
			return this.imagePath+'XSDOccurrenceOneToUnbounded.gif';
		else
			return Ext.BLANK_IMAGE_URL;
	},
	
	clear : function(rootNode){
		var node=null;
		while (node=rootNode.lastChild)
		{
			node.remove();
		};
	},

	load : function(){
		var _this = this;
		var rootNode = targetTree.getRootNode();
		targetTree.suspendEvents();
		this.clear(rootNode);
		rootNode.setText('Document');
		rootNode.expand();
		
		targetTree.body.mask('正在装载文档模型...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET', 
			url: 'xslt_ctrl.jsp', 
			params: { operation:"load", schema: schema },
			callback: function(options, success, response){
				targetTree.body.unmask();
				try {
					if (success) {
						var rootEl = response.responseXML.documentElement;
						var a = rootNode.attributes;
						//a.prefix = 'tns';
						a.documentation = Loader.loadDocumentation(rootEl);
						
						rootNode.setText(rootEl.getAttribute('name'));
						Loader.root = rootNode;
						
						Loader.loadNodes(rootEl, rootNode);
					}
					else {
						_this.showException(response);
					}
				}
				finally {
					targetTree.resumeEvents();
					targetTree.getTopToolbar().items.get('save').disable();
				}
			}
		});
	},

	save : function(){
		var _this = this;
		var rootNode = xsltTree.getRootNode();
		var a = rootNode.attributes;
		var xmlns = a.xmlns || "http://www.w3.org/2001/XMLSchema";
		var xmldoc = XDom.createDocument(xmlns);
		try
		{
			var rootEl = xmldoc.createElement('schema');
			xmldoc.appendChild(rootEl);
			//if (console) console.debug(xmldoc);
			rootEl.setAttribute('targetNamespace', a.targetNamespace);
			rootEl.setAttribute('elementFormDefault', a.elementFormDefault?a.elementFormDefault:"qualified");
			rootEl.setAttribute('attributeFormDefault', a.attributeFormDefault?a.attributeFormDefault:"unqualified");
			Saver.saveDocumentation(a.documentation, rootEl);
			
			var directives = rootNode.findChild('id', 'DIRECTIVES');
			directives.eachChild(function(node){
				var a = node.attributes;
				var el = xmldoc.createElement(a.tag);
				rootEl.appendChild(el);
				el.setAttribute('schemaLocation', a.schemaLocation);
				if (a.tag=='import')
				{
					el.setAttribute('namespace', a.namespace);
					el.setAttribute('prefix', a.prefix);
				}
			});
			var elements = rootNode.findChild('id', 'ELEMENTS');
			Saver.saveNodes(elements, rootEl);
			var types = rootNode.findChild('id', 'TYPES');
			Saver.saveNodes(types, rootEl);
			
			xsltTree.body.mask('正在保存模型...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'xslt_ctrl.jsp',
				params: { operation:"save", schema:schema, xmlns: xmlns, data: XDom.innerXML(xmldoc) },
				callback: function(options, success, response){
					xsltTree.body.unmask();
					if (success)
						xsltTree.getTopToolbar().items.get('save').disable();
					else
						_this.showException(response);
				}
			});
		}
		catch (e)
		{
			alert(e.message);
		}
		finally
		{
			delete xmldoc;
		}
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