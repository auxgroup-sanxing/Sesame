Ext.BLANK_IMAGE_URL='../images/s.gif';

var canClose = true;

Ext.ux.MessageNodeUI = Ext.extend(Ext.ux.SchemaBoxUI, {
    // private
    renderElements : function(n, a, targetNode, bulkRender) {
        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li style="margin-bottom:5px; margin-right:5px; float:left;">',
            '<table style="" class="xsd-complex-deactive x-tree-node" cellpadding="0" cellspacing="0"><tr><td class="" style="padding: 2px; border-bottom: gray solid 1px;">',
		    '<div ext:tree-node-id="',n.id,'" style="cursor:default;" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<span class="x-tree-node-indent" style="display:none;"></span>',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on" />',
			cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" style="cursor:default; font-weight:bold" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" style="display:none;" />',
            '</div></td></tr>',
            '<tr><td><ul class="x-tree-node-ct" style="margin:5px;"></ul></td></tr>',
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
    }
});

var Viewer = function(){
//变量
var layout, schemaTree, importTree;

var Loader = {
	map : {
		'all' : function(el, parentNode, refNode){
			var node = Viewer.createStyleNode({tag:'all'});
			parentNode.insertBefore(node, refNode);
			Loader.loadNodes(el, node);
		},
		'choice' : function(el, parentNode, refNode){
			var node = Viewer.createStyleNode({tag:'choice', minOccurs: el.getAttribute('minOccurs'), maxOccurs: el.getAttribute('maxOccurs') });
			parentNode.insertBefore(node, refNode);
			Loader.loadNodes(el, node);
		},
		'complexType' : function(el, parentNode, refNode){
			var annEl,docuEl;
			var documentation = Loader.getDocumentation(el);
			if (el.getAttribute('name')) {
				var node = Viewer.createComplextypeNode({
					name: el.getAttribute('name'),
					documentation: documentation,
					allowChildren: true,
					allowEdit: true,
					allowRemove: true,
					children: []
				});
				Loader.types.appendChild(node);
				Loader.loadNodes(el, node);
			}
			else {
				Loader.loadNodes(el, parentNode);
			}
		},
		'element' : function(el, parentNode, refNode){
			var documentation = Loader.getDocumentation(el);
			if (el.parentNode.localName=='schema'|| el.parentNode.nodeName=='schema'|| el.parentNode.nodeName=='xs:schema') {
				Loader.map['message'](el, parentNode, refNode);
				return;
			}
			else if (el.getAttribute('ref')) {
				var node = Viewer.createElRefNode({
					ref: el.getAttribute('ref'), 
					documentation:documentation,
					minOccurs: el.getAttribute('minOccurs') || '',
					maxOccurs: el.getAttribute('maxOccurs') || ''
				});
				parentNode.insertBefore(node, refNode);
				return;
			}
			var annEl,appEl;
			var cfg = {};
			if ((annEl=XDom.firstElement(el, 'annotation')) && (appEl=XDom.firstElement(annEl, 'appinfo'))) {
				var formatEl = XDom.firstElement(appEl, 'format');
				if (formatEl != null) {
					cfg.kind = formatEl.getAttribute('kind');
					cfg.number = formatEl.getAttribute('id') || '';
					cfg.compress = formatEl.getAttribute('compress') || '0';
					//var cfgEl;
					switch (cfg.kind) {
					case 'F':
						cfg.align = formatEl.getAttribute('align') || 'L';
						cfg.blank = formatEl.getAttribute('blank') || '';
						cfg.length = formatEl.getAttribute('length') || '10';
						cfg.endian = formatEl.getAttribute('endian') || 'big';
						break;
					case 'S':
						cfg.separator = formatEl.getAttribute('separator') || ',';
						cfg.limit = formatEl.getAttribute('limit') || '';
						break;
					case 'V':
						cfgEl=XDom.firstElement(formatEl, 'head');
						cfg['head-align'] =  cfgEl ? cfgEl.getAttribute('align'): 'L';
						cfg['head-blank'] = cfgEl ? cfgEl.getAttribute('blank'): '';
						cfg['head-length'] = cfgEl ? cfgEl.getAttribute('length'): '2';
						cfg['head-radix'] = cfgEl ? cfgEl.getAttribute('radix'): '10';
						cfg['head-compress'] = cfgEl ? (cfgEl.getAttribute('compress')||'0') : '0';
						break;
					}
				}
				var occuEl = XDom.firstElement(appEl, 'occurs');
				if (occuEl != null) {
					cfg['occurs-ref'] = occuEl.getAttribute('ref') || '';
					cfg['occurs-style'] = occuEl.getAttribute('style') || '';
				}
			}
			var node = Viewer.createElementNode(Ext.apply(cfg, {
				name: el.getAttribute('name'),
				type: el.getAttribute('type'),
				documentation: documentation,
				minOccurs: el.getAttribute('minOccurs') || '',
				maxOccurs: el.getAttribute('maxOccurs') || '',
				nillable: el.getAttribute('nillable') || '',
				expanded: false,
				children:[]
			}));
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
		'import' : function(el, parentNode, refNode){
			var a = {
				schemaLocation: el.getAttribute('schemaLocation'), 
				namespace: el.getAttribute('namespace'),
				prefix: el.getAttribute('prefix') || ''
			}
			var node = Viewer.createImportNode(a);
			Loader.directives.appendChild(node);
		},
		'include' : function(el, parentNode, refNode){
			var a = {
				schemaLocation: el.getAttribute('schemaLocation')
			};
			var node = Viewer.createIncludeNode(a);
			Loader.directives.appendChild(node);
		},
		'message' : function(el, parentNode, refNode){
			var channel = el.getAttribute('channel');
			var text = '';
			if (channel=='input'){
				text = '请求';
			}
			else if (channel=='output'){
				text = '响应';
			}
			else if (channel=='fault'){
				text = '故障消息';
			}
			else {
				text = name;
			}
			var node = Viewer.createMessageNode({
				channel: el.getAttribute('channel'),
				name: el.getAttribute('name'), 
				text: text
			});
			Loader.messages.appendChild(node);
			var compEl,seqEl;
			if ((compEl=XDom.firstElement(el, 'complexType')) && (seqEl=XDom.firstElement(compEl, 'sequence'))) {
				Loader.loadNodes(seqEl, node);
			}
		},
		'sequence' : function(el, parentNode, refNode){
			var node = Viewer.createStyleNode({tag:'sequence', minOccurs: el.getAttribute('minOccurs'), maxOccurs: el.getAttribute('maxOccurs')});
			parentNode.insertBefore(node, refNode);
			Loader.loadNodes(el, node);
		},
		'simpleType' : function(el, parentNode, refNode){
			var annEl,docuEl;
			var documentation = Loader.getDocumentation(el);
			var restriction, list, union;
			var restrictEl, listEl, unionEl, patternEl, minEl, maxEl;
			if ((restrictEl=Ext.query('restriction', el)).length>0) {
				restriction = { base: restrictEl[0].getAttribute('base'), enumerations: [], patterns: [] };
				Ext.each(Ext.query('enumeration', restrictEl[0]), function(el){
					restriction.enumerations.push([el.getAttribute('value')]);
				});
				Ext.each(Ext.query('pattern', restrictEl[0]), function(el){
					restriction.patterns.push([el.getAttribute('value')]);
				});
				restriction. spectial = restriction.patterns.length>0 ? 'patterns' : 'enumerations';
				if (restriction.base == 'xs:string' || restriction.base == 'xs:hexBinary') {
					restriction.minLength = (minEl=Ext.query('minLength', restrictEl[0])).length>0 ? minEl[0].getAttribute('value') : null;
					restriction.maxLength = (minEl=Ext.query('maxLength', restrictEl[0])).length>0 ? minEl[0].getAttribute('value') : null;
				}
				else if (restriction.base != 'xs:boolean') {
					restriction.minInclusive = (minEl=Ext.query('minInclusive', restrictEl[0])).length>0 ? minEl[0].getAttribute('value') : null;
					restriction.maxInclusive = (maxEl=Ext.query('maxInclusive', restrictEl[0])).length>0 ? maxEl[0].getAttribute('value') : null;
				}
			}
			else if ((listEl = Ext.query('list', el)).length > 0) {
				list = { itemType: listEl[0].getAttribute('itemType') };
			}
			else if ((unionEl = Ext.query('union', el)).length > 0) {
				union = { memberTypes: unionEl[0].getAttribute('memberTypes') };
			}
			var node = Viewer.createSimpletypeNode({
				restriction: restriction,
				list: list,
				union: union,
				name: el.getAttribute('name'),
				documentation: documentation,
				allowChildren: true,
				allowEdit: true,
				allowRemove: true,
				children: []
			});
			Loader.types.appendChild(node);
		},
		'types' : function(el, parentNode, refNode){
			Loader.loadNodes(el, Loader.types);
		}
	},
	getDocumentation : function(element){
		var annEl, docuEl;
		if (annEl=XDom.firstElement(element, 'annotation')||XDom.firstElement(element, 'xs:annotation'))
		{
			if (docuEl=XDom.firstElement(annEl, 'documentation')||XDom.firstElement(annEl, 'xs:documentation'))
				return docuEl.textContent||docuEl.text;
		}
		return '';
	},
	loadNodes : function(parentEl, parentNode, refNode){
		var el = parentEl.firstChild;
		while (el) {
			var func = this.map[el.localName||el.nodeName.substring(el.nodeName.indexOf(':')+1)];
			if (func) func(el, parentNode, refNode);// else alert(el.nodeName);
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
			if (a.nillable) element.setAttribute("nillable", a.nillable);
			if (a.documentation) Saver.saveDocumentation(a.documentation, element);
			parentEl.appendChild(element);
			var annoEl = XDom.firstElement(element, 'annotation');
			if (node.firstChild) {
				if (a['occurs-ref'])
				{
					if (!annoEl) {
						var annoEl = parentEl.ownerDocument.createElement("annotation");
						element.appendChild(annoEl);
					}
					var appEl = parentEl.ownerDocument.createElement('appinfo');
					annoEl.appendChild(appEl);
					var occuEl = parentEl.ownerDocument.createElement("occurs");
					appEl.appendChild(occuEl);
					occuEl.setAttribute('ref', a['occurs-ref']);
					occuEl.setAttribute('style', a['occurs-style']);
				}
				var complexEl = parentEl.ownerDocument.createElement("complexType");
				element.appendChild(complexEl);
				Saver.saveNodes(node, complexEl);
			}
			else {
				if (!a.kind)	return;
				if (!annoEl) {
					var annoEl = parentEl.ownerDocument.createElement("annotation");
					element.appendChild(annoEl);
				}
				var appEl = parentEl.ownerDocument.createElement('appinfo');
				annoEl.appendChild(appEl);
				var formatEl = parentEl.ownerDocument.createElement('format');
				appEl.appendChild(formatEl);
				formatEl.setAttribute('kind', a.kind);
				formatEl.setAttribute('id', a.number);
				formatEl.setAttribute('compress', a.compress);
				switch (a.kind)
				{
				case 'F':
					formatEl.setAttribute('align', a.align);
					formatEl.setAttribute('blank', a.blank);
					formatEl.setAttribute('length', a.length);
					formatEl.setAttribute('endian', a.endian);
					break;
				case 'S':
					formatEl.setAttribute('separator', a.separator);
					formatEl.setAttribute('limit', a.limit);
					break;
				case 'V':
					var headEl = parentEl.ownerDocument.createElement("head");
					formatEl.appendChild(headEl);
					headEl.setAttribute('align', a['head-align']);
					headEl.setAttribute('blank', a['head-blank']);
					headEl.setAttribute('length', a['head-length']);
					headEl.setAttribute('radix', a['head-radix']);
					headEl.setAttribute('compress', a['head-compress']);
					break;
				}
			}
		},
		'group' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
		},
		'message' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('element');
			element.setAttribute("name", a.name);
			element.setAttribute("channel", a.channel);
			parentEl.appendChild(element);
			var complexEl = parentEl.ownerDocument.createElement('complexType');
			element.appendChild(complexEl);
			var seqEl = parentEl.ownerDocument.createElement('sequence');
			complexEl.appendChild(seqEl);
			Saver.saveNodes(node, seqEl);
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
				if (restr.base=='xs:string' || restr.base=='xs:hexBinary'){
					if (restr.minLength) {
						var minEl = parentEl.ownerDocument.createElement('minLength');  restriction.appendChild(minEl);
						minEl.setAttribute('value', restr.minLength);
					}
					if (restr.maxLength) {
						var maxEl = parentEl.ownerDocument.createElement('maxLength');  restriction.appendChild(maxEl);
						maxEl.setAttribute('value', restr.maxLength);
					}
				}
				else if (restr.base!='xs:boolean'){
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
	imagePath : SchemaNodeUI.imagePath,
	propertyGrid: null,

	init : function(){
		var _this = this;
		
		if (isVersioned == 'false')
			isLocked = 'true';

		Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		Ext.QuickTips.init();

		this.propertyGrid = new Ext.grid.PropertyGrid({
			id: 'propGrid',
			border: false,
			closable: false,
			propertyNames: {
				'align': '对齐方式',
				'blank': '补齐字符',
				'compress': 'BCD 压缩',
				'endian': '字节序',
				'documentation': '描述',
				'head-align': '头对齐方式',
				'head-blank': '头填充符',
				'head-length': '头长度',
				'head-radix': '头长度进制',
				'nillable': '允许空值',
				'number': '编号',
				'length': '长度',
				'limit': '界定符',
				'maxOccurs': '最大出现次数',
				'minOccurs': '最小出现次数',
				'name': '名称',
				'namespace': '命名空间',
				'prefix': '前缀',
				'ref': '引用',
				'separator': '分隔符',
				'schemaLocation': 'Schema 位置',
				'targetNamespace': '目标命名空间',
				'variety': '类型'
			},
			listeners: {
				columnresize : function(columnIndex, newSize){ this.stopEditing(); },
				resize : function(){ this.stopEditing(); },
				beforepropertychange : function(source, recordId, value, oldValue){
					if (recordId=='name') {
						var selected = schemaTree.selModel.getSelectedNodes()[0];
						var sibling = selected.nextSibling;
						while (sibling!=null) {
							if (sibling.attributes.name==value) {
								alert('同级元素不能重名');
								return false;
							}
							sibling = sibling.nextSibling;
						}
						var sibling = selected.previousSibling;
						while (sibling!=null) {
							if (sibling.attributes.name==value) {
								alert('同级元素不能重名');
								return false;
							}
							sibling = sibling.previousSibling;
						}
					}
				},
				propertychange : function(source, recordId, value, oldValue){
					_this.setProperties(schemaTree.selModel.getSelectedNodes(), recordId, value);
				},
				beforeedit:function(e){
					if (isLocked != 'true')
						return false;
					if (e.record.id=='name' && schemaTree.selModel.getSelectedNodes().length!=1) {
						return false;
					}
				}
			},
			customEditors: {
				"schemaLocation": new Ext.grid.GridEditor(new Ext.form.TextField({
					triggerClass: 'xsd-icon-opentrigger',
					readOnly: true,
					onTriggerClick: function(e){
						var field = this;
						var callback = function(button, filename, record){
							if (button!='ok') return;
							var node = selModel.getSelectedNodes()[0];
						};
						_this.getOpenDialog(callback).show();
					}
				})),
				"variety": new Ext.grid.GridEditor(new Ext.form.TriggerField({
					triggerClass: 'xsd-icon-commtrigger',
					readOnly: true,
					onTriggerClick: function(e){
						var node = selModel.getSelectedNodes()[0];
						//var a = node.attributes;
						_this.getVarietyDialog(node).show();
					}
				})),
				"compress": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:[['0','否'],['1','是']], triggerAction:'all', editable:false, forceSelection:true})),
				"length": new Ext.grid.GridEditor(new Ext.form.NumberField({allowBlank:false, allowDecimals:false, selectOnFocus:true})),
				"head-length": new Ext.grid.GridEditor(new Ext.form.NumberField({allowBlank:false, allowDecimals:false, selectOnFocus:true})),
				"align": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:[['L','L-左对齐'],['R','R-右对齐']], triggerAction:'all', editable:false, forceSelection:true})),
				"head-align": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:[['L','L-左对齐'],['R','R-右对齐']], triggerAction:'all', editable:false, forceSelection:true})),
				"nillable":  new Ext.grid.GridEditor(new Ext.form.ComboBox({store:[['true','是'],['false','否']], triggerAction:'all', editable:false, forceSelection:true})),
				"name": new Ext.grid.GridEditor(new Ext.form.TextField({allowBlank:false, regex:/^[A-Za-z_]\w*$/, regexText:'非法的名称', selectOnFocus:true })),
				"namespace": new Ext.grid.GridEditor(new Ext.form.TextField({readOnly:true, selectOnFocus:true})),
				"number": new Ext.grid.GridEditor(new Ext.form.NumberField({allowDecimals:false, selectOnFocus:true})),
				"ref": new Ext.grid.GridEditor(new Ext.form.ComboBox({
					triggerAction: 'all',
					forceSelection: true,
					autoShow: true,
					store: [], 
					listeners:{
						beforequery : function(queryEvent){
							var selected = schemaTree.getSelectionModel().getSelectedNodes()[0];
							var elements = [];
							var elemParentNode = schemaTree.getNodeById('MESSAGES');
							elemParentNode.eachChild(function(node){
								var a = node.attributes;
								if (a.tag=='element' && !a.ref && !selected.isAncestor(node)) 
									elements.push(a.name);
							});
							this.store.removeAll();
							this.store.loadData(elements, true);
						}
					}
				})),
				"separator": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:[',',';','|','/','\\r\\n'], triggerAction:'all', editable:true, forceSelection:false})),
				"endian": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:[['big','big-大尾字节序'],['little','little-小尾字节序']], triggerAction:'all', editable:false, forceSelection:true})),
				"head-radix": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:['2','8','10','16'], triggerAction:'all', editable:true, forceSelection:true}))
			},
			source: {}
		});
		this.propertyGrid.store.sortInfo = null;
			
		var schemaRoot = new Ext.tree.AsyncTreeNode({
			id: 'ROOT',
			text: 'Schema',
			tag: 'schema',
			targetNamespace: targetNamespace,
			prefix: '',
			iconCls: 'xsd-icon-schema',
			expanded: true,
			allowDrag: false,
			allowDrop: false,
			children: []
		});

		var viewport = new Ext.Viewport({
			layout: 'border',
			stateId: 'message_viewer',
			items:[
				{
					region: 'center',
					xtype: 'treepanel',
					id: 'schema_pannel',
					loader: new Ext.tree.TreeLoader({preloadChildren: true, clearOnLoad: false}),
					root: schemaRoot,
					rootVisible: false,
					lines: false,
					containerScroll: true,
					animate: false,
					autoScroll: true,
					bodyStyle: 'padding:5px; background-color:white;',
					enableDD: true,
					dragConfig: this.getDragConfig(),
					//dropConfig: this.getDropConfig(),
					hlDrop: false,
					selModel: new Ext.tree.MultiSelectionModel(),
					trackMouseOver: false,
					tbar: this.getToolbar(),
					keys: {
						key: Ext.EventObject.DELETE,
						fn: function(){ 
							//btns.remove.handler, topBtns.remove 
						}
					},
					listeners: {
						beforeexpandnode: function(node){
							if (!node.attributes.ref) return true;
							
							var array = node.attributes.ref.split(':');
							var prefix = array[0];
							var name = array[1];
							var founded = null;
							
							var directives = schemaTree.getRootNode().findChild('id', 'DIRECTIVES');
							if (prefix=='tns') {
								for (var child=directives.firstChild; child!=null; child=n.nextSibling) {
									if (child.attributes.tag=='include') {
										var n = importTree.getRootNode().findChild('id', importNode.attributes.schemaLocation);
										if (n) {
											alert(n.text);
										}
									}
								}
							}
							else {
								var importNode = directives.findChild('prefix', prefix);
								if (importNode) {
									var n = importTree.getRootNode().findChild('id', importNode.attributes.schemaLocation);
									if (n) {
										if (n.isLoaded()) {
											var founded = n.findChild('name', name);
											if (founded) {
												founded.select();
											}
											else {
												alert('没找到引用的元素');
											}
										}
										else {
											n.reload(function(){ 
												var founded = n.findChild('name', name);
												if (founded) {
													founded.select();
												}
												else {
													alert('没找到引用的元素');
												}
											});
										}
									}
								}
							}
							
							return false;
						}
					}
					
				},
				{
					region: 'east',
					split: true,
					width: 220,
					minSize: 180,
					maxSize: 400,
					collapsible: true,
					margins: '0 0 0 0',
					layout: 'border',
					items: [{
					  	region: 'center',
						xtype: 'treepanel',
						iconCls: 'xsd-palette-category',
						title: '引入的类型与元素',
						border: false,
						loader: new Ext.tree.TreeLoader({
							requestMethod: 'GET', 
							dataUrl: 'encoding_ctrl.jsp', 
							baseParams: {operation:'getSchemaET', schema:schema},
							listeners: {
								'beforeload': function(loader, node, callback){
									if (node.parentNode==node.getOwnerTree().getRootNode()) {
										this.baseParams.operation = 'getSchemaET';
										delete this.baseParams.path;
										delete this.baseParams.tag;
										delete this.baseParams.name;
									}
									else {
										var schemaNode = node.parentNode;
										while(schemaNode.getDepth()>1) schemaNode=schemaNode.parentNode;
										var a = node.attributes;
										this.baseParams.operation = 'getStructure';
										this.baseParams.path = schemaNode.attributes.id;
										this.baseParams.tag = a.tag; 
										this.baseParams.name = a.name; 
									}
								},
								'loadexception': function(loader, node, response){ _this.showException(response); } 
							}
						}),
						id: 'import_tree',
						rootVisible: false,
						lines: false,
						animate: false,
						autoScroll: true,
						enableDrag: true,
						trackMouseOver: false,
						tools: [{
							id: 'search',
							qtip: '查找',
							keyword: null,
							handler: function(e, toolEl, panel, tc){
								var treeFilter = importTree.treeFilter;
								var str = prompt("请输入关键字", tc.keyword);
								if (typeof(str)=='string') {
									var node = importTree.getSelectionModel().getSelectedNode();
									while (node && node.getDepth()>1) node=node.parentNode;
									var regex=new RegExp(str, 'i');
									treeFilter.filter(regex, 'text', node);
									tc.keyword = str;
									this.qtip = '关键字: "'+str+'"';
								}
							}
						},{
							id: 'refresh',
							qtip: '刷新',
							on: {
								click: function(){
									var node = importTree.getSelectionModel().getSelectedNode();
									if (node && node.reload) node.reload();
								}
							}
						}],
						root: new Ext.tree.AsyncTreeNode({
						    text: 'Standard',
						    children: []
						})
					  },
					  {
					  	region: 'south',
						iconCls: 'xsd-palette-category',
						id: 'prop-panel',
						title: '属性',
						border: false,
						split: true,
						height: 200,
						layout: 'card',
						activeItem: 0,
						defaults: {boder:false},
						items: [this.propertyGrid]
					}]
				}
			]
		});

		schemaTree = viewport.findById('schema_pannel');
		importTree = viewport.findById('import_tree');
		importTree.treeSorter = new Ext.tree.TreeSorter(importTree, {
			caseSensitive: true,
			folderSort: true,
			dir: "asc"
		});
		importTree.treeFilter = new Ext.tree.TreeFilter(importTree, {
			autoClear: true,
			clearBlank: true
		});
		
		importTree.on('append', function(tree, parent, node, index){
			var a= node.attributes;
			if (a.tag){
				a.iconCls = 'xsd-icon-'+a.tag.toLowerCase();
			}
			if (typeof(a.text)=="undefined"){
				node.setText(a.name+' '+a.doc);
			}
		});
		
		var topBtns = schemaTree.getTopToolbar().items.map;
		var selModel = schemaTree.getSelectionModel();
		
		schemaTree.on('nodedragover', function(e){
			var a = (e.point=='append') ? e.target.attributes : e.target.parentNode.attributes;
			if (e.dropNode.getOwnerTree() != e.tree)
			{
				if (isLocked != 'true')
					return false;
				if (a.tag != 'message' && a.tag != 'sequence' && a.tag != 'choice' && a.tag != 'all') {
					e.cancel = true;
					return false;
				}
				return true;
			}
			switch (e.dropNode.attributes.tag)
			{
				case 'element':
					if (a.tag!='message' && a.tag!='sequence' && a.tag!='choice'  && a.tag!='all')  
						e.cancel=true;
					break;
				case 'complexType':
					if (a.id!='TYPES' && a.tag!='message' && a.tag!='sequence')  
						e.cancel=true;
					break;
				default:
			}
		});
		schemaTree.on('startdrag', function(treePanel, node, e){
		});
		schemaTree.on('enddrag', function(treePanel, node, e){
		});
		schemaTree.on('beforenodedrop', function(e) {
			if (e.dropNode.getOwnerTree() == e.tree) {
				var a = e.dropNode.attributes;
				if (a.tag=='complexType'||a.tag=='simpleType') {
					e.dropNode = _this.createElementNode({
						name:a.name, type:'tns:'+a.name, maxOccurs: '', minOccurs: '', nillable: '', documentation: a.documentation
					});
					return true;
				} else {
					var a = e.dropNode.attributes;
					var srcNodeText = a.text
					var dropNode = e.target;
					if (dropNode.childNodes.length > 0) {
						var duplicated = false;
						Ext.each(dropNode.childNodes, function(child) {
							if(a.text == child.text) {
								duplicated = true;
								return;
							}
						});
						
						if (!!duplicated) {
							alert('元素不允许重复!');
							return false;
						}
					} else {
						if(a.text == dropNode.text) {
							alert('元素不允许重复!');
							return false;
						}
					}
										
				}
			}
			else {
				var p = (e.point=='append') ? e.target.attributes : e.target.parentNode.attributes;
				var a = e.dropNode.attributes;
				var directives = schemaRoot.findChild('id', 'DIRECTIVES');
				var direNode = directives.findChild('schemaLocation', e.dropNode.parentNode.id);
				var prefix = direNode ? (direNode.attributes.tag=='import'?direNode.attributes.prefix:'tns')+':' : '';
				
				if (prefix==':') {
					alert('导入的 Schema 命名空间还没有指定前缀');
					return false;
				}
				switch (a.tag)
				{
					case 'complexType':
					case 'simpleType':
						e.dropNode = _this.createElementNode({name:a.name, type:prefix+a.name, maxOccurs:'', minOccurs:'', nillable: '', documentation:a.doc});
						break;
					case 'element':
						e.dropNode = _this.createElRefNode({ref:prefix+a.name, documentation:a.doc});
						break;
					default:
						alert('没有找到组件的构建器');
						return false;
				}
			}
		});
		schemaTree.on('append', function(tree, parent, node, index){
			topBtns.save.enable();
		});
		schemaTree.on('insert', function(tree, parent, node, ref){
			topBtns.save.enable();
		});
		schemaTree.on('movenode', function(tree, oldParent, newParent, index){
			topBtns.save.enable();
		});
		schemaTree.on('remove', function(tree, parent, node){
			topBtns.save.enable();
		});
		
		var addStyle_Handler = function(){
			var styleNode = _this.createStyleNode({tag: this.tag});
			var node = selModel.getSelectedNodes()[0];
			node.appendChild(styleNode);
			node.expand();
			styleNode.getUI().iconNode.title = this.tag=='sequence'?'序列':this.tag=='choice'?'选择':this.tag=='all'?'全部':null;
		};
		var addAny_Handler = function(){
			var anyNode = _this.createAnyNode();
			var node = selModel.getSelectedNodes()[0];
			node.appendChild(anyNode);
			node.expand();
		};
		var addElement_Handler = function(){
			var node = selModel.getSelectedNodes()[0];
			var ref=null, a=node.attributes;
			if (a.tag=='complexType'){
				node = node.firstChild ? node.firstChild : node.appendChild(_this.createStyleNode({tag: 'sequence'}));
			}
			else if (a.tag=='element' || a.tag=='any'){
				if (a.allowExpand) {
					node = node.firstChild ? node.firstChild : node.appendChild(_this.createStyleNode({
						tag: 'sequence'
					}));
				}
				else {
					ref = node;
					node = node.parentNode;
				}
			}
			var prefix='Element', index=0;
			for (var i=0; i<65536; i++)
			{
				if (!node.findChild('name', prefix+i)){
					index=i;  break;
				}
			}
			var elNode = _this.createElementNode({name: prefix+index, type: 'string', maxOccurs:'', minOccurs:'', nillable:'', documentation:'' });
			node.insertBefore(elNode, ref);
			node.expand(false, false);
			//elNode.ensureVisible();
			elNode.select();
		};
		var copyElement_Handler = function(){
			Application.selectedNodes = selModel.getSelectedNodes();
			Application.canPaste = true;
			Application.isCut = false;
		};
		var cutElement_Handler = function(){
			Application.selectedNodes =  selModel.getSelectedNodes();
			Application.canPaste = true;
			Application.isCut = true;
		};
		var pasteElement_Handler = function(){
			var node = selModel.getSelectedNodes()[0];
			var ref=null, a=node.attributes;
			if (a.tag=='complexType'){
				node = node.firstChild ? node.firstChild : node.appendChild(_this.createStyleNode({tag: 'sequence'}));
			}
			else if (a.tag=='element' || a.tag=='any'){
				if (a.allowExpand) {
					node = node.firstChild ? node.firstChild : node.appendChild(_this.createStyleNode({
						tag: 'sequence'
					}));
				}
				else {
					ref = node;
					node = node.parentNode;
				}
			}
			if (Application.selectedNodes) {
				var nodes = Application.selectedNodes;
				for (var i=nodes.length-1; i>=0; i--) {
					var elNode = null;
					if (Application.isCut) {
						elNode = nodes[i];
					}
					else {
						var a = nodes[i].attributes;
						if (a.iconCls == 'xsd-icon-ref') {
							elNode = _this.createElRefNode({ref:a.ref, maxOccurs:a.maxOccurs, minOccurs:a.minOccurs, documentation:a.documentation});
						}
						else {
							elNode = _this.createElementNode({name:a.name, type:a.type, maxOccurs:a.maxOccurs, minOccurs:a.minOccurs, nillable:a.nillable, documentation:a.documentation });
						}
					}
					node.insertBefore(elNode, ref);
					node.expand(false, false);
					ref = elNode;
				}
				if (Application.isCut) {
					Application.selectedNodes = null;
					Application.canPaste = false;
					Application.isCut = false;
				}
			}
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
			var elNode = _this.createElRefNode({ref:'NewElement', maxOccurs:'', minOccurs:'', documentation:''});
			node.insertBefore(elNode, ref);
			node.expand();
			elNode.select();
		};
		var addImport_Handler = function(item, e){
			var node = selModel.getSelectedNodes()[0];
			var rootTns = node.parentNode.attributes.targetNamespace;
			
			var a = node.attributes;
			var callback = function(button, filename, record){
				if (button!='ok') return;
				var loc = _this.getRelativePath(schema, filename);
				var ns = record.get('content'), directive='';
				if (ns != rootTns) {
					node.appendChild(_this.createImportNode({
						schemaLocation: loc,
						namespace: ns,
						prefix: ''
					}));
					directive = 'import';
				} else {
					node.appendChild(_this.createIncludeNode({
						schemaLocation: loc
					}));
					directive = 'include';
				}
				importTree.getRootNode().appendChild(new Ext.tree.AsyncTreeNode({
					id:loc, text:loc, iconCls: 'xsd-icon-'+directive
				}));
			};
			_this.getOpenDialog(callback).show();
		};
		var addMsg_Handler = function(){
			var opera = schema.substring(schema.lastIndexOf('/')+1, schema.lastIndexOf('.'));
			var msgNode = _this.createMessageNode({text:this.text, channel:this.id});
			var node = selModel.getSelectedNodes()[0];
			if (this.id=='input') {
				node.insertBefore(msgNode, node.findChild('channel', 'output'));
				msgNode.attributes.name = opera;
			}
			else if (this.id=='output') {
				node.insertBefore(msgNode, node.findChild('channel', 'fault'));
				msgNode.attributes.name = opera+'Response';
			}
			else {
				node.appendChild(msgNode);
				var index=0, faultNode=null;
				while ((faultNode=node.findChild('name', opera+'Fault'+index)) != null) {
					index++;
				}
				msgNode.attributes.name = opera+'Fault'+index;
			}
			node.expand();
		};
		var setStyle_Handler = function(){
			var item = this;
			Ext.each(selModel.getSelectedNodes(), function(node){
				var a = node.attributes;
				a.kind = item.id;
				a.number = a.number || '';
				a.compress = a.compress || '0';
				switch (item.id){
				case 'F':
					delete a['separator'];
					delete a['limit'];
					delete a['head-align'];
					delete a['head-blank'];
					delete a['head-length'];
					delete a['head-radix'];
					a['align'] = a['align'] || 'L';
					a['blank'] = a['blank'] || ' ';
					a['length'] = a['length'] || '10';
					a['endian'] = a['endian'] || 'big';
					break;
				case 'S':
					delete a['align'];
					delete a['blank'];
					delete a['length'];
					delete a['head-align'];
					delete a['head-blank'];
					delete a['head-length'];
					delete a['head-radix'];
					a['separator'] = a['separator'] || ',';
					a['limit'] = a['limit'] || '';
					break;
				case 'V':
					delete a['align'];
					delete a['blank'];
					delete a['length'];
					delete a['separator'];
					delete a['limit'];
					a['head-align'] = a['head-align'] || 'L';
					a['head-blank'] = a['head-blank'] || '';
					a['head-length'] = a['head-length'] || '2';
					a['head-radix'] = a['head-radix'] || '10';
					a['head-compress'] = a['head-compress'] || '0';
					break;
				default:
					delete a.kind;
					delete a.number;
					delete a.compress;
					delete a['align'];
					delete a['blank'];
					delete a['length'];
					delete a['endian'];
					delete a['separator'];
					delete a['limit'];
					delete a['head-align'];
					delete a['head-blank'];
					delete a['head-length'];
					delete a['head-radix'];
				}
				node.setText(a.toString());
			});
			
			_this.propertyGrid.stopEditing(true);
			_this.propertyGrid.setSource(_this.getProperties(selModel.getSelectedNodes()));
			topBtns.save.enable();
		};
		var setType_Handler = function(item, e){
			var callback = function(record){
				var pre = record.get('pre');
				var type = (pre ? pre+':' : '') + record.get('type');
				Ext.each(selModel.getSelectedNodes(), function(node){
					var a = node.attributes;
					if (a.tag!='element') return;
					node.attributes.type = type;
					node.setText(a.toString());
					while(node.firstChild) node.firstChild.remove();
					//node.ui.setExpandable(false);
				});
				topBtns.save.enable();
			}
			var dlg = _this.getTypeDialog(callback);
			dlg.show();
		};
		var setAnonyType_Handler = function() {
			var node = selModel.getSelectedNodes()[0];
			var a = node.attributes;
			if (a.type) delete a.type;
			node.setText(a.toString());
			if (!node.firstChild) {
				node.appendChild(_this.createStyleNode({tag: 'sequence'}));
			}
			node.ui.setExpandable(true);
			topBtns.save.enable();
		};
		var setOccurs_Handler = function(){
			var node = selModel.getSelectedNodes()[0];
			var callback = function(record, style){
				node.attributes['occurs-ref'] = record.get('xpath');
				node.attributes['occurs-style'] = style;
				topBtns.save.enable();
			}
			_this.getOccursDialog({params:node.attributes, callback:callback}).show();
		};
		var addCompType_Handler = function(){
			var node = selModel.getSelectedNodes()[0];
			var prefix='ComplexType', index=0;
			for (var i=0; i<65536; i++)
			{
				if (!node.findChild('name', prefix+i)){
					index=i;  break;
				}
			}
			var typeNode = _this.createComplextypeNode({text: prefix+index, name: prefix+index, documentation:''});
			node.appendChild(typeNode);
			node.expand();
			typeNode.select();
		};
		var addSimpType_Handler = function(){
			var node = selModel.getSelectedNodes()[0];
			var prefix='NewSimpleType', index=0;
			for (var i=0; i<65536; i++)
			{
				if (!node.findChild('name', prefix+i)){
					index=i;  break;
				}
			}
			var typeNode = _this.createSimpletypeNode({
				text: prefix+index+' string', name: prefix+index, documentation:'',
				restriction: {base: 'string'}
			});
			node.appendChild(typeNode);
			node.expand();
			typeNode.select();
		};

		this.contextMenu = new Ext.menu.Menu({
			id: 'schemaMenu',
			enableScrolling: false,
			items: [
				{
					id: 'deleteItem',
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
				{
					id: 'copyElement',
					text: '复制元素',
					handler: copyElement_Handler
				},
				{
					id: 'cutElement',
					text: '剪切元素',
					disabled: true,
					handler: cutElement_Handler
				},
				{
					id: 'pasteElement',
					text: '粘帖元素',
					disabled: true,
					handler: pasteElement_Handler
				},
				{
					id: 'addElRef',
					text: '添加元素引用',
					hidden: true,
					handler: addElRef_Handler
				},
				{
					id: 'addAny',
					text: '添加 any',
					handler: addAny_Handler
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
					disabled: true,
					handler: addStyle_Handler
				},
				{
					id: 'addAll',
					text: '添加 all',
					tag: 'all',
					disabled: true,
					handler: addStyle_Handler
				},
				'-',
				{
					id: 'addImport',
					text: '引入模型..',
					handler: addImport_Handler
				},
				{
					id: 'addMsg',
					text: '添加消息',
					icon:"../images/icons/email_add.png",
					tag: 'message',
					handler: function(){return false;},
					menu: new Ext.menu.Menu({
						items: [
							{
								id: 'input',
								text: '请求',
								handler: addMsg_Handler
							},
							{
								id: 'output',
								text: '响应',
								handler: addMsg_Handler
							},
							{
								id: 'fault',
								text: '故障消息',
								handler: addMsg_Handler
						}]
					})
				},
				{
					id: 'addComplexType',
					text: '添加复合类型',
					handler: addCompType_Handler
				},
				{
					id: 'addSimpleType',
					text: '添加简单类型',
					handler: addSimpType_Handler
				},
				{
					id: 'setType',
					text: '数据类型',
					handler: function(){return false;},
					menu: new Ext.menu.Menu({
						items: [
							{
								id: 'browse',
								text: '浏览...',
								handler: setType_Handler
							},
							{
								id: 'newComplex',
								text: '复合类型',
								handler: setAnonyType_Handler
						}]
					})
				},
				{
					id: 'setStyle',
					text: '编码格式',
					handler: function(){return false;},
					menu: new Ext.menu.Menu({
						listeners: {
							beforeshow: function(){
								var a = _this.contextMenu.node.attributes;
								var item = this.items.get(a.kind || '-');
								if (item) item.setChecked(true, false);
							}
						},
						items: [
							new Ext.menu.CheckItem({
								id: '-',
								text: '无',
								group: 'style',
								handler: setStyle_Handler
							}),
							new Ext.menu.CheckItem({
								id: 'F',
								text: '定长',
								group: 'style',
								handler: setStyle_Handler
							}),
							new Ext.menu.CheckItem({
								id: 'S',
								text: '分隔符',
								group: 'style',
								handler: setStyle_Handler
							}),
							new Ext.menu.CheckItem({
								id: 'V',
								text: '变长',
								group: 'style',
								handler: setStyle_Handler
						})]
					})
				},
				{
					id: 'setOccurs',
					text: '出现次数',
					handler: setOccurs_Handler
				},
				'-',
				{
					id: 'refresh',
					text: '刷新',
					icon:"../images/elcl16/launch_restart.gif",
					handler: topBtns.reload.handler
				}
			]
		});
		schemaTree.on('contextmenu', function(node, e){
			var lock = isLocked == 'true';
			
			if (!node.isSelected()) node.select();
			_this.contextMenu.node = node;
			
			var nodes = selModel.getSelectedNodes();
			var a = node.attributes;
			var allowRemove = node.attributes.allowRemove;
			var allowElement = nodes.length==1 && node.getPath().indexOf('DIRECTIVES')<0 && (a.tag && a.tag!='simpleType');
			var elementVisible = true;
			var items = _this.contextMenu.items;
			var item = items.get('addMsg');
			if (a.id=='MESSAGES'){
				if(lock){
					item.show();
					var subItems=item.menu.items;
					subItems.get('input').setDisabled(node.findChild('channel', 'input'));
					subItems.get('output').setDisabled(node.findChild('channel', 'output'));
				}else{
					item.hide();
				}
			}
			else {
				item.hide();
			}
			items.get('addElement').setDisabled(!allowElement || !lock);
			items.get('cutElement').setDisabled(!lock);
			items.get('pasteElement').setDisabled(!allowElement || !lock || !Application.canPaste);
			items.get('addElRef').setDisabled(!allowElement || !lock);
			items.get('addAny').setVisible((nodes.length<2 && (a.tag=='sequence' || a.tag=='choice')) && lock);
			items.get('addSeq').setVisible((nodes.length<2 && (a.tag=='complexType' || a.tag=='sequence' || a.tag=='choice')) && lock);
			items.get('addChoice').setVisible((nodes.length<2 && (a.tag=='complexType' || a.tag=='sequence' || a.tag=='choice')) && lock);
			items.get('addAll').setVisible((nodes.length<2 && (a.tag=='complexType')) && lock);
			items.get('addSeq').setDisabled((node.childNodes.length>0 || a.tag!='complexType' & a.tag!='element') || !lock);
			items.get('addComplexType').setVisible(a.id=='TYPES' && lock);
			items.get('addSimpleType').setVisible(a.id=='TYPES' && lock);
			items.get('addImport').setDisabled(a.id!='DIRECTIVES' || !lock);
			items.get('setType').setVisible((a.tag=='element' && !a.ref) && lock);
			items.get('setType').menu.items.get('newComplex').setDisabled(!a.type || !lock);
			items.get('setStyle').setVisible((a.tag=='element' && !a.ref) && lock);
			items.get('setOccurs').setVisible((a.tag=='element' && node.attributes.allowExpand) && lock);
			items.get('deleteItem').setDisabled(!allowRemove || !lock);
			
		    _this.contextMenu.showAt(e.getXY());
		});

		schemaTree.on('dblclick', function(node, e){
			if (node.attributes.allowEdit) {
				switch (node.attributes.tag)
				{
				case 'simpleType':
					_this.getVarietyDialog(node).show();
					break;
				}
				
			}
		});

		selModel.on('selectionchange', function(){
			var sels = selModel.getSelectedNodes();
			if (sels.length<1) {
				topBtns.remove.disable();
				_this.propertyGrid.setSource({});
				return;
			}
			var node = sels[0];
			var a = node.attributes, allowRemove=false;
			switch (a.name)
			{
				case 'branch':
					allowRemove = node.parentNode.childNodes.length > 2;
					break;
				case 'default':
					allowRemove = false;
					break;
				case 'if':
					allowRemove = node.nextSibling.attributes.name==a.name || node.previousSibling!=null;
					break;
				default:
					allowRemove = node.attributes.allowRemove;
			}
			topBtns.remove.setDisabled(!allowRemove);
			_this.propertyGrid.stopEditing(true);
			_this.propertyGrid.setSource(_this.getProperties(sels));
		});
		schemaTree.loader.on('loadexception', function(loader, node, response){ _this.showException(response); });
		this.load(schemaRoot);
		
		window.onbeforeclose = function(e){
			if (!topBtns.save.disabled) {
				if (isLocked == 'true') {
					canClose = confirm('放弃所做的修改吗？\n点击"确定"关闭，点击"取消"可以选择保存');
					if (!canClose) {
						return false;
					}
				}
				else {
					return true;
				}
			} else {
				return true;
			}
		};
		schemaTree.resumeEvents();
	},
	
	getProperties : function(nodes) {
		var maps = this.propertyGrid.propertyNames;
		var a = nodes[0].attributes;
		var attrs = {};
		for (var elem in a){
			if (maps[elem]) attrs[elem]=a[elem];
		}
		for (var i=1,len=nodes.length; i<len; i++)
		{
			var a = nodes[i].attributes;
			for (var elem in attrs)
			{
				if (typeof a[elem]=='undefined'){
					delete attrs[elem];
				}
			}
		}
		return attrs;
	},
	
	setProperties : function(nodes, prop, value){
		for (var i=0,len=nodes.length; i<len; i++)
		{
			var node = nodes[i], a = node.attributes;
			var oldValue = a[prop];
			a[prop]=value;
			schemaTree.getTopToolbar().items.get('save').enable();
			switch (prop)
			{
				case 'documentation': 
				case 'name': 
				case 'ref':
				case 'schemaLocation':
				case 'length':
					node.setText(a.toString());
					break;
				case 'prefix':
					if (oldValue) schemaTree.getRootNode().cascade(function(child){
						var p=child.attributes, m=false;
						if (p.type) {
							p.type = p.type.replace(oldValue + ':', value + ':');
							m=true;
						}
						if (p.element) {
							p.element = p.element.replace(oldValue + ':', value + ':');
							m=true;
						}
						if (p.ref) {
							p.ref = p.ref.replace(oldValue + ':', value + ':');
							m=true;
						}
						if (m) child.setText(p.toString());
						return true;
					});
					break;
				case 'targetNamespace':
					node.setText('Schema : '+a.targetNamespace);
					break;
			}
		}
	},
	
	getToolbar : function(){
		var _this = this;
		var buttons = [
			{
				id: 'remove',
				text: '删除',
				icon: '../images/tool16/delete_edit.gif',
				cls: 'x-btn-text-icon',
				disabled: true,
			    handler: function(){
					if (this.disabled)  return false;
					if (!window.confirm('确实要删除选中的项吗？'))  return false;

					var sels = schemaTree.getSelectionModel().getSelectedNodes();
					for (var i = sels.length - 1; i >= 0; i--) {
						var n = sels[i];
						if (!n || !n.attributes.allowRemove)  continue;
						if (n.attributes.tag=='import' || n.attributes.tag=='include') {
							var node = importTree.getRootNode().findChild('id', n.attributes.schemaLocation);
							if (node) node.remove();
						}
						n.remove();
					}
				}
			},'-',{
				id: 'reload',
			    text: '刷新',
				icon: '../images/elcl16/launch_restart.gif',
				cls: 'x-btn-text-icon',
			    handler : function(){
					if (!schemaTree.getTopToolbar().items.get('save').disabled){
						if (!window.confirm('模型已经更改，确实要放弃当前所做的修改吗？'))  return false;
					} 
					_this.load();
				}
			},{
				id: 'save',
			    text: '保存',
				icon: '../images/tool16/save_edit.gif',
				cls: 'x-btn-text-icon',
				disabled: isLocked != 'true',
				listeners:{
					click:function(){
						_this.save();
					},
					enable:function(button){
						if (isLocked != 'true')
							button.disable();
					}
				}
			},{
			    text: '向上',
				icon: '../images/tool16/up.gif',
				cls: 'x-btn-text-icon',
			    handler : function(){
					_this.openUnit();
			    }
			},'-',{
				width:40,
				text: '测试',
				icon: '../images/elcl16/launch_run.gif',
				handler:function(){
					Ext.Ajax.request({
						method: 'POST',
						url: 'encoding_ctrl.jsp',
						params: {
							operation: 'getInputInterface',
							unit:unit,
							schema:schema
						},
						callback: function(options, success, response){
							var inputData;
							if (success) 
								inputData = response.responseText.replace(/(\r\n)$/ig, '');
							_this.testing(inputData);
						}
					});
				}
			},'->',{
				id: 'open-target',
			    text: '目标操作',
				hidden: !ref || ref == 'null',
				tooltip: "打开 " + ref,
				icon: '../images/icons/thread_view.gif',
				cls: 'x-btn-text-icon',
			    handler : function(){
					if (!ref) return;
					var file = schema.substring(0, schema.indexOf('/')+1)+'engine/'+ref;
					Application.serviceEditor.createWindow({id:file, text: ref.replace(/\..*$/, ''), filePath:file});
				}
			}
		];
		return buttons;
	},

	testing:function(inputData){
		if(Ext.getCmp("testWin"))
			return;
		// 输入XML验证
		function validateXML(txt){
			var rs;
			if (window.ActiveXObject) {	// IE
				var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
				xmlDoc.async = "false";
				xmlDoc.loadXML(txt);
				
				if (xmlDoc.parseError.errorCode != 0) {
					txt = "错误代码: " + xmlDoc.parseError.errorCode + "\n";
					txt = txt + "原因: " + xmlDoc.parseError.reason;
					txt = txt + "提示: " + xmlDoc.parseError.line;
					rs = txt;
				}
			}else if (document.implementation.createDocument) {	// Mozilla, Firefox, Opera, etc.
				var parser = new DOMParser();
				var xmlDoc = parser.parseFromString(txt, "text/xml");
				
				if (xmlDoc.documentElement.nodeName == "parsererror") {
					rs = xmlDoc.documentElement.childNodes[0].nodeValue;
					rs = rs.replace(/位置.*?\.xml/ig, '').replace(/：$/ig, '。');
					rs = Ext.util.Format.htmlEncode(rs);
				}
			}
			return rs;
		}
		var startTest = (!!inputData) ? function(){
			outputForm.getForm().findField('outputArea').setValue("");
			var inputPanel = Ext.getCmp("input");
			if(!!inputForm.getForm().isValid()){
			
				var inputXML = Ext.get("inputArea").getValue();
				var info = validateXML(inputXML);
				if (!!info) {
					Ext.Msg.alert('提示:', info);
					return;
				}
				inputForm.getTopToolbar().findById('start').disable();
				
				
				Ext.Ajax.request({
					method: 'POST',
					url:'encoding_ctrl.jsp',
					params:{
						operation:'startTest',
						xml: inputXML,
						unit:unit,
						schema:schema
					},
					callback:function(options,success,response){
						inputForm.getTopToolbar().findById('start').enable();
						if(success){
							var result = response.responseText;
							outputForm.getForm().findField('outputArea').setValue(result);
						}
					}
				});
			}
		} :  function(){
			outputForm.getForm().findField('outputArea').setValue("");
			outputForm.getTopToolbar().findById('start').disable();
			
			Ext.Ajax.request({
				method: 'POST',
				url:'encoding_ctrl.jsp',
				params:{
					operation:'startTest',
					xml: '<request/>',
					unit:unit,
					schema:schema
				},
				callback:function(options,success,response){
					outputForm.getTopToolbar().findById('start').enable();
					if(success){
						var result = response.responseText;
						outputForm.getForm().findField('outputArea').setValue(result);
					}
				}
			});
		};
		var inputForm = new Ext.FormPanel({
			region:'north',
			id:'input',
			resiable:false,
			stateful: false,
			modal: true,
			hideLabels: true,
			monitorValid: true,
			frame: false,
			border: true,
			height: 140,
            minSize: 50,
			bodyStyle : {'border': 'none'},
            split: true,
            tbar:[{
            	id: 'start',
            	text: '开始',
            	icon: '../images/tool16/launch_run.gif',
				cls: 'x-btn-text-icon',
            	handler:startTest
            		
            }],
            items:[{
            	xtype:'textarea',
            	value: (!!inputData) ? inputData : '',
            	id:'inputArea',
            	name:'inputArea',
            	anchor: '100%, 100%',
				emptyText : '<请输入初始XML然后点击开始>',
		        allowBlank: false,
		        blankText: '请输入XML',
		        style: {'width':'100%', 'height':'100%', 'border':'1 1 1 1px'},
				preventScrollbars: false
            	
            }]
			
		});
		
		var outputForm = (!!inputData) ? new Ext.FormPanel({
			region:'center',
			id:'output',
			resiable:false,
			stateful: false,
			modal: true,
			hideLabels: true,
			monitorValid: true,
			frame: false,
			border: true,
			bodyStyle : {'border': 'none'},
            split: true,
            items:[{
            	xtype:'textarea',
            	id:'outputArea',
            	name:'outputArea',
            	anchor: '100%, 100%',
		        style: {'width':'100%', 'height':'100%', 'border':'1 1 1 1px'},
				preventScrollbars: false
            	
            }]
			
		}) : new Ext.FormPanel({
			region:'center',
			id:'output',
			resiable:false,
			stateful: false,
			modal: true,
			hideLabels: true,
			monitorValid: true,
			frame: false,
			border: true,
			bodyStyle : {'border': 'none'},
            tbar:[{
            	id: 'start',
            	text: '开始',
            	icon: '../images/tool16/launch_run.gif',
				cls: 'x-btn-text-icon',
            	handler:startTest
            		
            }],
            items:[{
            	xtype:'textarea',
            	id:'outputArea',
            	name:'outputArea',
            	anchor: '100%, 100%',
		        style: {'width':'100%', 'height':'100%', 'border':'1 1 1 1px'},
				preventScrollbars: false
            	
            }]
			
		});
		var dbgWinWidth = 430;
		var screenWidth = window.screen.availWidth;
		var pageX = screenWidth - dbgWinWidth - 20;
		var win = new Ext.Window({
			title:'服务单元测试',
			layout:'border',
			id:'testWin',
			width: dbgWinWidth,
			height: 320,
			border: false,
			pageX: 100,
			pageY: 100,
			iconCls: 'settings',
			items:(!!inputData) ? [inputForm,outputForm] : [outputForm]
		});
		
		win.show();
	},
	
	
	getVarietyDialog : function(node){
		var _this = this;
		
		if (!this.varietyDlg) {
			var titleText = '设置类型';
			
			var typeStore = new Ext.data.SimpleStore({
			    fields: ['type'],
			    data : [['xs:boolean'], ['xs:date'], ['xs:dateTime'], ['xs:double'], ['xs:float'], ['xs:hexBinary'], ['xs:int'], ['xs:string'], ['xs:time'], ['xs:anySimpleType']]
			});
			
			var restrictView = new Ext.DataView({
				store: new Ext.data.SimpleStore({
				    fields: ['item'],
				    data: []
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
										formPanel.getComponent('lengthRectrict').setVisible(value=='xs:string' || value=='xs:hexBinary');
										formPanel.getComponent('scopeRectrict').setVisible(value!='xs:string' && value!='xs:hexBinary' && value!='xs:boolean');
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
				schemaTree.getTopToolbar().items.get('save').enable();
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
	
	getOpenDialog : function(callback){
		if (!this.openDlg) {
			this.openDlg = new Dialog.OpenDialog({
				title: '导入 Schema',
				width: 500,
				height: 280,
				forceSelection: true,
				closeAction: 'hide', 
				url: 'schema_ctrl.jsp',
				path:  schema.substr(0, schema.indexOf('/')+1)+'schema/',
				filter: '.*\\.xsd$',
				callback: callback
			});
			this.openDlg.on('exception', function(ex){
				Viewer.showException(ex);
			});
		}
		else {
			this.openDlg.callback = callback;
			//this.openDlg.refresh();
		}
		return this.openDlg;
	},
	
	getOccursDialog : function(options){
		var n = schemaTree.selModel.getSelectedNodes()[0];
		var elements=[];
		var p=n, path='';
		while (p = p.parentNode) {
			if (!p.attributes.tag) break;
			
			var node=p.firstChild;
			while (node && node!=n && !n.isAncestor(node)) {
				var a = node.attributes;
				elements.push({
					cls: 'xsd-icon-element',
					name: a.name ? a.name : a.ref,
					xpath: a.name ? path+a.name : path+a.ref,
					desc: a.documentation
				});
				node = node.nextSibling;
			};
			if (p.attributes.tag=='element') path+= '../';
		}
		
		if (!this.occursDialog){
			var titleText = '设置出现次数';
			
			var tpl = new Ext.XTemplate(
				'<tpl for=".">',
				    '<div class="thumb-wrap x-view-item" id="{name}">',
				    '<div class="thumb x-unselectable" unselectable="on" style="vertical-align:middle; line-height:18px;">',
					'<img style="width:16px; height:16px; vertical-align:middle;" class="{cls}" src="'+Ext.BLANK_IMAGE_URL+'" />',
				    '<span class="x-editable" style="vertical-align:middle;">{name} <font color="navy">{desc}</font></span></div></div>',
				'</tpl>',
				'<div class="x-clear"></div>'
			);
			var elemStore = new Ext.data.JsonStore({
				root: 'items',
				fields: [
					{name:'cls', type: 'string'},
					{name:'name', type:'string'},
					{name:'xpath', type:'string'},
					{name:'desc', type:'string'}
				]
			});
			var dataView = new Ext.DataView({
				store: elemStore,
				tpl: tpl,
				style: 'height:100%; overflow:auto;',
				autoHeight: false,
				multiSelect: false,
				singleSelect: true,
				overClass: 'x-view-over',
				itemSelector: 'div.thumb-wrap',
				emptyText: '没有可选元素',
				listeners: {
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
		        width: 350, height: 420, minHeight: 200,
				margins:'0 5 0 5',
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
							xtype: 'combo',
							anchor: '100%',
					        name: 'first',
					        allowBlank:true,
							hideTrigger: true,
							minChars: 1,
							mode: 'local',
							store: [],
							listeners: {
								beforequery: function(e){
									try { elemStore.filter('name',  new RegExp(e.query.replace('*', '.*'), 'i')); } catch(e){}
								}
							}
					    }]
					},
					{
						id: 'styleForm',
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
							fieldLabel: '重复方式',
							xtype: 'combo',
							anchor: '100%',
					        name: 'style',
							hiddenName: 'style',
							editable: false,
							mode: 'local',
							triggerAction: 'all',
							store: [['cycle','循环重复'], ['separate','单域重复']],
							listeners: {
							}
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
				if (dlg.options.callback) {
					var form = dlg.getComponent('styleForm').getForm();
					dlg.options.callback(dataView.getSelectedRecords()[0], form.getValues().style);
				}
				dlg.hide();
			});
		    dlg.cancel = dlg.addButton(Ext.Msg.buttonText.cancel, function(){ dlg.hide(); });
		    dlg.render(document.body);
			dlg.elementView= dataView;
		    this.occursDialog = dlg;
		}
		else {
		}
		
		var view = this.occursDialog.elementView;
		view.store.loadData({items:elements});
		view.select(view.store.findExact('xpath', options.params['occurs-ref']));
		this.occursDialog.options = options;
		var form = this.occursDialog.getComponent('styleForm').getForm();
		form.setValues({count:options.params['occurs-count'], style:options.params['occurs-style']});
        return this.occursDialog;
	},
	
	getTypeDialog : function(callback){
		var selNode = schemaTree.selModel.getSelectedNodes()[0];
		var internalTypes = [];
		var typesNode = schemaTree.getNodeById("TYPES");
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
				    '<span class="x-editable" style="vertical-align:middle;">{type} <font color="maroon">{desc}</font></span></div></div>',
				'</tpl>',
				'<div class="x-clear"></div>'
			);
			var a = schemaTree.getRootNode().attributes;
			var typeStore = this.getTypeStore(a.xmlns);
			var dataView = new Ext.DataView({
				store: typeStore,
				tpl: tpl,
				style: 'height:100%; overflow:auto;',
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
						var directives = schemaTree.getRootNode().findChild('id', 'DIRECTIVES');
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
		        constrain:true, constrainHeader:true,border: false,
		        minimizable: false, maximizable: false, resizable:true,
		        stateful: false, modal: true, shim:true,
		        buttonAlign: "right",	defaultButton: 0,
		        width: 350, height: 420, minHeight: 200,
		        footer: true,
		        closable: true,
		        closeAction: 'hide',
				bodyStyle: 'border: none;background-color:#CCD8E7',
				layout: 'border',
				items: [
					{
						region: 'north',
						xtype: 'form',
						labelAlign: 'top',
						bodyStyle: 'background-color:#CCD8E7;',
					    split: false,
					    collapsible: false,
					    margins:'5 0 0 0',
						autoHeight: true,
						border: false,
						defaultType: 'textfield',
						items: [{
							fieldLabel: '查找 ( * 匹配任意字符)',
							xtype: 'combo',
							anchor: '100%',
					        name: 'first',
					        allowBlank:true,
							hideTrigger: true,
							minChars: 1,
							queryDelay: 300,
							store: [],
							listeners: {
								beforequery: function(e){
									try { typeStore.filter('type',  new RegExp(e.query.replace('*', '.*'), 'i')); } catch(e){}
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
						bodyStyle: 'background-color: #CCD8E7;',
						items: [{
							fieldLabel: '声明位置',
							anchor: '100%',
					        name: 'location',
					        disabled: true
					    }]
					},
					{
						region: 'center',
						autoScroll: true,
						anchor: -18,
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
			url: 'encoding_ctrl.jsp',
			baseParams: {	operation:'loadSchemaTypes', schema:schema, xmlns:xmlns },
			root: 'items',
			fields: [
				{name:'cls', type: 'string'},
				{name:'type', type:'string'},
				{name:'desc', type:'string'},
				{name:'pre', type:'string'}
			]
		});
		store.on('loadexception', function(proxy, obj, response){ Viewer.showException(response); });
		store.on('load', function(sender, records, options){
			if (options.add) sender.sort("type", "ASC");
		});
		store.on('beforeload', function(sender){
			var directives = schemaTree.getRootNode().findChild('id', 'DIRECTIVES');
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
		var a = (typeof config)=='object' ? config : {documentation:'', kind:'F', length:10, align:'L', blank:''};
		a.toString=function(){
			return this.name + (this.documentation ? ' ' + this.documentation : '') + (this.type ? ' <b>'+this.type+(this.kind=='F'&&this.type=='string'?'['+this.length+']':'')+'</b>' : '');
		};
		if (!a.children) {
			a.children = [];
		}
		var node = new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			icon: Viewer.getOccursIcon(a.minOccurs, a.maxOccurs),
			iconCls: 'xsd-icon-element', 
			uiProvider: Ext.ux.SchemaComplextypeUI,
			tag: 'element',
			text: a.toString(),
			allowExpand: a.type?false:true,
			allowDrag: true,
			allowChildren: true,
			allowEdit: true,
			allowRemove: true,
			leaf: false
		}));
		return node;
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
			expanded: false,
			allowDrag: true,
			allowChildren: true,
			allowEdit: true,
			allowRemove: true,
			children: []
		}));
	},

	createImportNode : function(config){
		var a = (typeof config)=='object' ? config : {schemaLocation:'', namespace:'', prefix:'' };
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
	
	createIncludeNode : function(config){
		var a = (typeof config)=='object' ? config : {schemaLocation:''};
		a.toString=function(){
			return this.schemaLocation||"没有指定位置";
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
	
	createPartNode : function(config){
		var a = (typeof config)=='object' ? config : {name:'Part', type:'xs:string', documentation:''};
		a.toString=function(){
			return this.name + (this.documentation ? ' ' + this.documentation : '') + (this.element?' ->'+this.element:' <b>' + this.type + '</b>');
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			iconCls: 'xsd-icon-part', 
			uiProvider: Ext.ux.SchemaNodeUI,
			tag: 'part',
			text: a.toString(),
			allowDrag: true,
			allowRemove: true,
			leaf: true
		}));
	},
	
	createRedefineNode : function(config){
		var a = (typeof config)=='object' ? config : {schemaLocation:''};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			text: a.schemaLocation || "没有指定位置",
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
		a.toString = function(){
			return this.name+(this.documentation?' '+this.documentation:'');
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

	createMessageNode : function(config){
		var a = (typeof config)=='object' ? config : {documentation:''};
		a.toString = function(){
			return this.channel=='input' ? '请求' : this.channel=='output' ? '响应' : this.channel=='fault' ? '故障消息' : this.name;
		};
		if (!a.children) {
			a.children = [];  a.expanded = true;
		}
		return new Ext.tree.TreeNode(Ext.apply(a, {
			iconCls: 'xsd-icon-notations', 
			uiProvider: Ext.ux.MessageNodeUI,
			tag: 'message',
			text: a.text || a.toString(),
			allowDrag: false,
			allowRemove: true,
			listeners: {
				beforecollapse: function(){ this.expanded=false; return false;}
			}
		}));
	},
	
	createSimpletypeNode : function(config){
		var a = (typeof config)=='object' ? config : {name:'', documentation:''};
		a.toString = function(){
			return this.name+(this.documentation?'-'+this.documentation:'')+' : '+Viewer.getTypeDesc(this);
		};
		if (!a.children) {
			a.children = [];
		}
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			iconCls: 'xsd-icon-simpletype', 
			uiProvider: Ext.ux.SchemaNodeUI,
			tag: 'simpleType',
			text: a.text || a.toString(),
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
			return '<b>'+a.restriction.base+'</b>';
		}
		if (a.list) {
			return '<b>['+a.list.itemType+']</b>';
		}
		if (a.union) {
			return '<b>{'+a.union.memberTypes+'}</b>';
		}
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

	getDragConfig : function(){
		return {
		    afterRepair : function(){
		        this.dragging = false;
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
		var rootNode = schemaTree.getRootNode();
		var importRoot = importTree.getRootNode();
		schemaTree.suspendEvents();
		this.clear(rootNode);
		this.clear(importRoot);
		
		var directives = new Ext.tree.AsyncTreeNode({
			id: 'DIRECTIVES',
			text: "指示",
			iconCls: 'xsd-icon-directives', 
			expanded: true,
			uiProvider: Ext.ux.SchemaBoxUI,
			allowDrag: false,
			children: []
		});
		var messages = new Ext.tree.AsyncTreeNode({
			id: 'MESSAGES',
			text: "消息",
			iconCls: 'xsd-icon-groups', 
			expanded: true,
			uiProvider: Ext.ux.SchemaBoxUI,
			allowDrag: false,
			children: []
		});
		var typesNode = new Ext.tree.AsyncTreeNode({
			id: 'TYPES',
			text: "类型",
			iconCls: 'xsd-icon-types', 
			expanded: true,
			uiProvider: Ext.ux.SchemaBoxUI,
			allowDrag: false,
			children: []
		});
		rootNode.appendChild([directives, typesNode, messages]);
		rootNode.expand();
		
		Ext.getBody().mask('正在装载模型...', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET', 
			url: 'encoding_ctrl.jsp', 
			params: {operation:"load", schema: schema },
			callback: function(options, success, response){
				Ext.getBody().unmask();
				try {
					if (success) {
						var rootEl = response.responseXML.documentElement;
						if (rootEl==null || rootEl.tagName!='schema') {
							alert('非法的文档模式');
							return;
						}
						
						var a = rootNode.attributes;
						a.xmlns = rootEl.getAttribute('xmlns:xs');
						a.targetNamespace= rootEl.getAttribute('targetNamespace')||targetNamespace;
						if (!a.xmlns) a.xmlns = "http://www.w3.org/2001/XMLSchema";
						if (!a.targetNamespace) a.targetNamespace = "http://framework.sesame.org/server/";
						
						Loader.root = rootNode;
						Loader.directives = directives;
						Loader.messages = messages;
						Loader.types = typesNode;
						Loader.loadNodes(rootEl, rootNode);
						directives.eachChild(function(node){
							var child = new Ext.tree.AsyncTreeNode({
								id: node.attributes.schemaLocation,
								text: node.attributes.schemaLocation,
								iconCls: node.attributes.iconCls,
								allowDrag: false
							});
							importRoot.appendChild(child);
						});
					}
					else {
						_this.showException(response);
					}
				}
				finally {
					schemaTree.resumeEvents();
					schemaTree.getTopToolbar().items.get('save').disable();
				}
			}
		});
	},
	
	save : function(){
		var _this = this;
		var xmldoc = XDom.createDocument();
		try
		{
			var rootNode = schemaTree.getRootNode();
			var a = rootNode.attributes;

			var rootEl = xmldoc.createElement('schema');
			xmldoc.appendChild(rootEl);
			
			rootEl.setAttribute('elementFormDefault', a.elementFormDefault||"qualified");
			rootEl.setAttribute('attributeFormDefault', a.attributeFormDefault||"unqualified");

			var directives = rootNode.findChild('id', 'DIRECTIVES');
			directives.eachChild(function(node){
				var a = node.attributes;
				var el = xmldoc.createElement(a.tag);
				rootEl.appendChild(el);
				el.setAttribute('schemaLocation', a.schemaLocation);
				if (a.tag=='import')
				{
					if (!a.prefix) {
						node.select();
						throw {message:'导入的 Schema 命名空间没有指定前缀'};
					}
					el.setAttribute('namespace', a.namespace);
					el.setAttribute('prefix', a.prefix);
				}
			});
			var types = rootNode.findChild('id', 'TYPES');
			Saver.saveNodes(types, rootEl);
			var messages = rootNode.findChild('id', 'MESSAGES');
			Saver.saveNodes(messages, rootEl);
			
			schemaTree.body.mask('正在保存模型...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'encoding_ctrl.jsp',
				params: {operation:"save", schema: schema, data: XDom.innerXML(xmldoc) },
				callback: function(options, success, response){
					schemaTree.body.unmask(); 
					if (success)
						schemaTree.getTopToolbar().items.get('save').disable();
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
	
	// 查看对应的源操作
	openUnit : function() {
		var title = decodeURIComponent(unitDesc) ? decodeURIComponent(unitDesc)+'['+ unitId + ']' : unitId;
		var viewer = '../explorer/unit_viewer.jsp?unit=';

		var currWin = top.Application.getDesktop().getActiveWindow();
		top.Application.openWindow(unit, title, viewer + unit + '&activeTab=1' + '&unitId=' + unitId);
		currWin.close();
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