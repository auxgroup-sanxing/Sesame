Ext.BLANK_IMAGE_URL='../images/s.gif';

var Viewer = function(){
//变量
var layout, schemaTree;
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
			var documentation = (annEl=XDom.firstElement(el, 'annotation'))?((docuEl=XDom.firstElement(annEl, 'documentation'))?docuEl.firstChild.nodeValue:''):'';
			if (el.getAttribute('name')) {
				var node = Viewer.createComplextypeNode({
					text: el.getAttribute('name')+' : '+documentation,
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
			var documentation = Loader.loadDocumentation(el);
			if (el.getAttribute('ref'))
			{
				var node = Viewer.createElRefNode({
					ref: el.getAttribute('ref'), 
					documentation:documentation,
					minOccurs: el.getAttribute('minOccurs'),
					maxOccurs: el.getAttribute('maxOccurs')
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
			if (el.parentNode == el.ownerDocument.documentElement) {
				Loader.elements.appendChild(node);
			}
			else {
				parentNode.insertBefore(node, refNode);
			}
			Loader.loadNodes(el, node);
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
			};
			var node = Viewer.createImportNode(a);
			Loader.directives.appendChild(node);
		},
		'sequence' : function(el, parentNode, refNode){
			var node = Viewer.createStyleNode({tag:'sequence', minOccurs: el.getAttribute('minOccurs'), maxOccurs: el.getAttribute('maxOccurs')});
			parentNode.insertBefore(node, refNode);
			Loader.loadNodes(el, node);
		},
		'simpleType' : function(el, parentNode, refNode){
			var annEl,docuEl;
			var documentation = Loader.loadDocumentation(el);
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
				if (restriction.base == 'string' || restriction.base == 'hexBinary') {
					restriction.minLength = (minEl=Ext.query('minLength', restrictEl[0])).length>0 ? minEl[0].getAttribute('value') : null;
					restriction.maxLength = (minEl=Ext.query('maxLength', restrictEl[0])).length>0 ? minEl[0].getAttribute('value') : null;
				}
				else if (restriction.base != 'boolean') {
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
			if (a.nillable) element.setAttribute("nillable", a.nillable);
			if (a.documentation) Saver.saveDocumentation(a.documentation, element);
			parentEl.appendChild(element);
			var annoEl = XDom.firstElement(element, 'annotation');
			if (node.firstChild) {
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
	imagePath : SchemaNodeUI.imagePath,

	init : function(){
		var _this = this;

		Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		Ext.QuickTips.init();

		var propGrid = new Ext.grid.PropertyGrid({
			id: 'propGrid',
			border: false,
			closable: false,
			listeners: {
				columnresize : function(columnIndex, newSize){ this.stopEditing(); },
				resize : function(){ this.stopEditing(); },
				beforepropertychange : function(source, recordId, value, oldValue){
					switch (recordId) {
						case 'name':
							var node = schemaTree.selModel.getSelectedNodes()[0];
							if (node.parentNode && node.parentNode.findChild(recordId, value)) {
								alert(propGrid.propertyNames[recordId]+'不能重复');
								return false;
							}
							break;
					}
				},
				propertychange : function(source, recordId, value, oldValue){
					_this.setProperties(schemaTree.selModel.getSelectedNodes(), recordId, value);
				}
			},
			propertyNames : {
				'align': '对齐方式',
				'blank': '补齐字符',
				'compress': 'BCD 压缩',
				'endian': '字节序',
				'documentation': '描述',
				'head-align': '头对齐方式',
				'head-blank': '头填充符',
				'head-length': '头长度',
				'head-radix': '头长度进制',
				'number': '编号',
				'length': '长度',
				'limit': '界定符',
				'maxOccurs': '最大出现次数',
				'minOccurs': '最小出现次数',
				'name': '名称',
				'namespace': '命名空间',
				'nillable': '允许空值',
				'prefix': '前缀',
				'ref': '引用',
				'separator': '分隔符',
				'schemaLocation': 'Schema 位置',
				'targetNamespace': '目标命名空间',
				'variety': '类型'
			},
			customEditors: {
				"schemaLocation" : new Ext.grid.GridEditor(new Ext.form.TriggerField({
					triggerClass: 'xsd-icon-opentrigger',
					readOnly: true,
					onTriggerClick: function(e){
						var field = this;
						var callback = function(button, filename, record){
							if (button!='ok') return;
							var node = selModel.getSelectedNodes()[0];
							var a = node.attributes;
							var tns = schemaTree.getRootNode().attributes.targetNamespace;
							if (a.tag=='import' && tns==record.get('content')) {
								alert('导入的 Schema 目标命名空间和当前 Schema 命名空间一致，请使用包含');  
								return false;
							}
							if (a.tag=='include' && tns!=record.get('content')) {
								alert('包含的 Schema 目标命名空间和当前 Schema 命名空间不一致');  
								return false;
							}
							var loc = _this.getRelativePath(schema, filename);
							if (loc=='') {
								alert('不能引用 Schema 本身');  
								return false;
							}
							a.schemaLocation = loc;
							if (a.tag=='import') a.namespace = record.get('content');
							node.setText(a.toString());
							
							propGrid.stopEditing(true);
							propGrid.setSource(_this.getProperties([node]));
							topBtns.save.enable();
						}
						propGrid.stopEditing();
						_this.getOpenDialog(callback).show();
					}
				})),
				"variety" : new Ext.grid.GridEditor(new Ext.form.TriggerField({
					triggerClass: 'xsd-icon-commtrigger',
					readOnly: true,
					onTriggerClick: function(e){
						var node = selModel.getSelectedNodes()[0];
						_this.getVarietyDialog(node).show();
					}
				})),
				"compress": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:[['0','否'],['1','是']], triggerAction:'all', editable:false, forceSelection:true})),
				"length": new Ext.grid.GridEditor(new Ext.form.NumberField({allowBlank:false, allowDecimals:false, selectOnFocus:true})),
				"head-length": new Ext.grid.GridEditor(new Ext.form.NumberField({allowBlank:false, allowDecimals:false, selectOnFocus:true})),
				"align": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:[['L','L-左对齐'],['R','R-右对齐']], triggerAction:'all', editable:false, forceSelection:true})),
				"head-align": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:[['L','L-左对齐'],['R','R-右对齐']], triggerAction:'all', editable:false, forceSelection:true})),
				"nillable": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:[['true','是'],['false','否']], triggerAction:'all', editable:false, forceSelection:true})),
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
							var elemParentNode = schemaTree.getNodeById('ELEMENTS');
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
				"head-radix": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:['2','8','10','16'], triggerAction:'all', editable:true, forceSelection:true})),
				"maxOccurs": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:['0','1','unbounded'], triggerAction:'all', editable:true, forceSelection:false})),
				"minOccurs": new Ext.grid.GridEditor(new Ext.form.ComboBox({store:['0','1'], triggerAction:'all', forceSelection:false, selectOnFocus:true }))
			},
			source: {}
		});
		this.propertyGrid = propGrid;
		
		var schemaRoot = new Ext.tree.AsyncTreeNode({
			id: 'ROOT',
			text: 'Schema',
			tag: 'schema',
			targetNamespace: targetNamespace,
			iconCls: 'xsd-icon-schema',
			uiProvider: Ext.ux.SchemaBoxUI,
			expanded: true,
			allowDrag: false,
			allowDrop: false,
			children: []
		});

		var viewport = new Ext.Viewport({
			layout: 'border',
			stateId: 'schema_viewer',
			items:[
				{
					region: 'center',
					xtype: 'treepanel',
					id: 'schema_pannel',
					loader: new Ext.tree.TreeLoader({preloadChildren: true, clearOnLoad: false}),
					root: schemaRoot,
					rootVisible: true,
					lines: false,
					imagePath: this.imagePath,
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
					tbar: this.getToolbar()
				},
				{
					region: 'east',
					split: true,
					width: 200,
					minSize: 175,
					maxSize: 400,
					collapsible: true,
					margins: '0 0 0 0',
					layout: 'border',
					items: [
					  new Ext.tree.TreePanel({
					  	region: 'north',
						iconCls: 'xsd-palette-category',
						title: '指令',
						border: false,
						loader: new Ext.tree.TreeLoader(),
						id: 'standard-tree',
						rootVisible: false,
						lines: false,
						autoScroll: true,
						enableDrag: true,
						trackMouseOver: false,
						root: new Ext.tree.AsyncTreeNode({
						    text:'Standard',
						    children: this.getStandardActions()
						})
					  }),
					  {
					  	region: 'center',
						iconCls: 'xsd-palette-category',
						id: 'prop-panel',
						title: '属性',
						border: false,
						layout: 'card',
						activeItem: 0,
						defaults: {boder:false},
						items: [propGrid]
					}]
				}
			]
		});

		schemaTree = viewport.findById('schema_pannel');
		var topBtns = schemaTree.getTopToolbar().items.map;
		var selModel = schemaTree.getSelectionModel();
		
		schemaTree.on('nodedragover', function(e){
			var a = (e.point=='append') ? e.target.attributes : e.target.parentNode.attributes;
			if (e.dropNode.getOwnerTree() != e.tree)
			{
				if (a.id != 'DIRECTIVES') 
					return false;
			}
			switch (e.dropNode.attributes.tag)
			{
				case 'element':
					if (a.id!='ELEMENTS' && a.tag!='elements' && a.tag!='sequence' && a.tag!='choice'  && a.tag!='all')  
						e.cancel=true;
					break;
				case 'complexType':
					if (a.id!='TYPES')  e.cancel=true;
					break;
				default:
			}
		});
		schemaTree.on('startdrag', function(treePanel, node, e){
		});
		schemaTree.on('enddrag', function(treePanel, node, e){
		});
		schemaTree.on('beforenodedrop', function(e){
			if (e.dropNode.getOwnerTree() == e.tree) 
			{
				return true;
			}
			else
			{
				if (e.dropNode.attributes.create) {
					var a = e.dropNode.attributes;
					e.dropNode = e.dropNode.attributes.create();
				}
				else {
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
			var elNode = _this.createElementNode({name: prefix+index, type: 'string', maxOccurs: '', minOccurs: '', nillable:'', documentation:''});
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
		var setFormat_Handler = function(){
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
			propGrid.stopEditing(true);
			propGrid.setSource(_this.getProperties(selModel.getSelectedNodes()));
			topBtns.save.enable();
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
			Ext.apply(node.attributes, this.occurs);
			node.getUI().iconNode.src = _this.getOccursIcon(this.occurs.minOccurs, this.occurs.maxOccurs);
			propGrid.stopEditing(true);
			propGrid.setSource(_this.getProperties(selModel.getSelectedNodes()));
			topBtns.save.enable();
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
			var prefix='SimpleType', index=0;
			for (var i=0; i<65536; i++)
			{
				if (!node.findChild('name', prefix+i)){
					index=i;  break;
				}
			}
			var typeNode = _this.createSimpletypeNode({
				text: prefix+index+' : string', name: prefix+index, documentation:'',
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
					id: 'addElRef',
					text: '添加元素引用',
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
					handler: addStyle_Handler
				},
				{
					id: 'addAll',
					text: '添加 all',
					tag: 'all',
					handler: addStyle_Handler
				},
				'-',
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
					id: 'setFormat',
					text: '编码格式',
					handler: function(){return false;},
					menu: new Ext.menu.Menu({
						listeners: {
							beforeshow: function(){
								var a = _this.contextMenu.node.attributes;
								var item = this.items.get(a.kind || '-');
								if (item) item.setChecked(true, true);
							}
						},
						items: [
							new Ext.menu.CheckItem({
								id: '-',
								text: '无',
								group: 'format',
								handler: setFormat_Handler
							}),
							new Ext.menu.CheckItem({
								id: 'F',
								text: '定长',
								group: 'format',
								handler: setFormat_Handler
							}),
							new Ext.menu.CheckItem({
								id: 'S',
								text: '分隔符',
								group: 'format',
								handler: setFormat_Handler
							}),
							new Ext.menu.CheckItem({
								id: 'V',
								text: '变长',
								group: 'format',
								handler: setFormat_Handler
						})]
					})
				},
				{
					id: 'setStyle',
					text: '组合方式',
					handler: function(){return false;},
					menu: new Ext.menu.Menu({
						listeners: {
							beforeshow: function(){
								var a = _this.contextMenu.node.attributes;
								var item = this.items.get(a.tag);
								if (item) item.setChecked(true, true);
							}
						},
						items: [
							new Ext.menu.CheckItem({
								id: 'sequence',
								text: '序列',
								group: 'style',
								handler: setStyle_Handler
							}),
							new Ext.menu.CheckItem({
								id: 'choice',
								text: '选择',
								group: 'style',
								handler: setStyle_Handler
							}),
							new Ext.menu.CheckItem({
								id: 'all',
								text: '全部',
								group: 'style',
								handler: setStyle_Handler
						})]
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
					icon:"../images/elcl16/launch_restart.gif",
					handler: topBtns.reload.handler
				}
			]
		});
		schemaTree.on('contextmenu', function(node, e){
			if (!node.isSelected()) node.select();
			_this.contextMenu.node = node;
			
			var nodes = selModel.getSelectedNodes();
			var a = node.attributes;
			var allowRemove = node.attributes.allowRemove;
			var allowElement = nodes.length==1 && (a.id=='ELEMENTS' || a.tag=='complexType' || a.tag=='element' || a.tag=='sequence' || a.tag=='choice');
			var items = _this.contextMenu.items;
			items.get('addElement').setDisabled(!allowElement);
			items.get('addElRef').setDisabled(nodes.length!=1 || node.getDepth()<=2);
			items.get('addAny').setVisible(nodes.length<2 && (a.tag=='sequence' || a.tag=='choice'));
			items.get('addSeq').setVisible(nodes.length<2 && (a.tag=='complexType' || a.tag=='sequence' || a.tag=='choice'));
			items.get('addChoice').setVisible(nodes.length<2 && (a.tag=='complexType' || a.tag=='sequence' || a.tag=='choice'));
			items.get('addAll').setVisible(nodes.length<2 && (a.tag=='complexType'));
			items.get('addSeq').setDisabled(a.tag=='complexType' && node.childNodes.length>0);
			items.get('addChoice').setDisabled(a.tag=='complexType' && node.childNodes.length>0);
			items.get('addAll').setDisabled(a.tag=='complexType' && node.childNodes.length>0);
			items.get('addComplexType').setVisible(a.id=='TYPES');
			items.get('addSimpleType').setVisible(a.id=='TYPES');
			items.get('setType').setVisible(a.type);
			items.get('setFormat').setVisible(a.type);
			items.get('setStyle').setVisible(a.tag=='sequence' || a.tag=='choice' || a.tag=='all');
			items.get('setOccurs').setVisible(a.tag=='element' || a.tag=='sequence' || a.tag=='choice' || a.tag=='all');
			items.get('deleteItem').setDisabled(!allowRemove);
			
		    _this.contextMenu.showAt(e.getXY());
		});

		if (schemaTree.el != null && schemaTree.el != undefined)
			schemaTree.el.addKeyListener(Ext.EventObject.DELETE, topBtns.remove.handler, topBtns.remove);
			
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
				propGrid.stopEditing(true);
				propGrid.setSource({});
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
			propGrid.stopEditing(true);
			propGrid.setSource(_this.getProperties(sels));
		});

		schemaTree.loader.on('loadexception', function(loader, node, response){ _this.showException(response); });
		this.load(schemaRoot);
		
		window.onbeforeclose = function(e){
			var xmlHttp =getXMLHttp() ;
			if(xmlHttp == null)
				return true;
				
				
			if(topBtns.save.disabled){
				if(!topBtns.lock.disabled &&(Ext.getCmp('lock').text == '解锁')) {
					xmlHttp.open("GET", "schema_ctrl.jsp?operation=schemaUnlock&schema="+schema, false);
					try{ xmlHttp.send(null); }catch(e){}
					return true;
				}
			}else{
				if(confirm('放弃所做的修改吗？\n点击"确定"关闭，点击"取消"可以选择保存')){
					if(!topBtns.lock.disabled &&(Ext.getCmp('lock').text == '解锁')) {
						xmlHttp.open("GET", "schema_ctrl.jsp?operation=schemaUnlock&schema="+schema, false);
						try{ xmlHttp.send(null); }catch(e){}
						return true;
					}
				}else{
					return false;
				}
			}
			
			function getXMLHttp(){
					var xmlHttp;
					try {
						xmlHttp = new XMLHttpRequest();// Firefox, Opera 8.0+, Safari
					} catch (e) {
						// Internet Explorer
						try {
							xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
						} catch (e) {
							try {
								xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
							} catch (e) {
								alert("您的浏览器不支持AJAX！");
								xmlHttp = null;
							}
						}
					}
					return xmlHttp;
			}
		};
	},
	
	getProperties : function(nodes) {
		var maps = this.propertyGrid.propertyNames;
		var a = nodes[0].attributes;
		var attrs = {};
		for (var elem in a){
			if (maps[elem]) {
				attrs[elem] = a[elem];
			}
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
		for (var i=0,len=nodes.length; i<len; i++){
			var node = nodes[i], a = node.attributes;
			var oldValue = a[prop];
			a[prop] = value;
			schemaTree.getTopToolbar().items.get('save').enable();
			switch (prop){
				case 'documentation':
					node.setText(value);
					break; 
				case 'name': 
				case 'ref':
				case 'schemaLocation':
				case 'length':
					node.setText(a.toString());
					break;
				case 'minOccurs': 
				case 'maxOccurs':
					node.getUI().iconNode.src = this.getOccursIcon(a.minOccurs, a.maxOccurs);
					break;
				case 'targetNamespace':
					node.setText('Schema : '+a.targetNamespace)
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
			}
		}
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

					var sels = schemaTree.getSelectionModel().getSelectedNodes();
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
				icon: '../images/elcl16/launch_restart.gif',
				cls: 'x-btn-text-icon',
			    handler : function(){
					if (!schemaTree.getTopToolbar().items.get('save').disabled){
						if (!window.confirm('模型已经更改，确实要放弃当前所做的修改吗？'))  return false;
					} 
					_this.load();
				}
			},
			{
				id: 'save',
			    text: '保存',
				icon: '../images/tool16/save_edit.gif',
				cls: 'x-btn-text-icon',
				disabled: true,
				listeners : {
					click:function(){
						_this.save();
					},
					
					enable:function(button){
						var lockButton = schemaTree.getTopToolbar().items.get('lock');
						if(!lockButton.disabled)
							if(lockButton.getText() == '锁定')
								button.disable();
					}
			    }
			},'-',
			{
				//TODO
				id:'lock',
				text:'锁定',
				icon: '../images/tool16/lock.png',
				disabled:(isVersioned == 'true')? false:true,
				cls: 'x-btn-text-icon',
				handler:function(){
					_this.schemaLock(this)
				}
			}
		];
		return buttons;
	},

	schemaLock:function(button){
		var operation
		if(button.text == "锁定")
			operation = "schemaLock"
		else
			operation = "schemaUnlock";
		var _this = this;
		Ext.Ajax.request({
				method: 'POST',
				url: 'schema_ctrl.jsp',
				params: {
					operation: operation,
					schema: schema
				},
				callback: function(options, success, response){
					if (!success) {
						
						_this.showException(response);
					}else {
						if(button.text == "锁定"){
							button.setText('解锁');
							
							Viewer.load();
						}else{
							button.setText('锁定');
							schemaTree.getTopToolbar().items.get('save').disable();
						}
					}
				}
		});
	},
	
	
	getStandardActions : function(){
		return [{
			text: '导入',
			iconCls: 'xsd-icon-import',
			cls: 'xsd-node-hideIndent',
			create: this.createImportNode,
			leaf: true
		},
		{
			text: '包含',
			iconCls: 'xsd-icon-include',
			cls: 'xsd-node-hideIndent',
			create: this.createIncludeNode,
			leaf: true
		},
		{
			text: '重定义',
			iconCls: 'xsd-icon-redefine',
			cls: 'xsd-node-hideIndent',
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
				url: 'schema_ctrl.jsp',
				path:  schema.substr(0, schema.indexOf('/')+1)+'schema/',
				filter: '.*\\.xsd$',
				callback: callback
			});
			this.openDlg.on('exception', function(ex){
				Viewer.showException(ex);
			});
		}
		this.openDlg.callback = callback;
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
				    '<span class="x-editable" style="vertical-align:middle;">{type} <font color="blue">{desc}</font></span></div></div>',
				'</tpl>',
				'<div class="x-clear"></div>'
			);
			var a = schemaTree.getRootNode().attributes;
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
							xtype: 'combo',
							anchor: '100%',
					        name: 'first',
					        allowBlank: true,
							hideTrigger: true,
							minChars: 1,
							queryDelay: 500,
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
			url: 'schema_ctrl.jsp',
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
		var a = (typeof config)=='object' ? config : {documentation:''};
		a.toString=function(){
			return this.name + (this.documentation ? ' ' + this.documentation : '') + (this.type ? ' <b>'+this.type+(this.kind=='F'&&this.type=='string'?'['+this.length+']':'')+'</b>' : '');
		};
		if (!a.children) {
			a.children = [];
		}
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			icon: this.getOccursIcon(a.minOccurs, a.maxOccurs),
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
			return this.schemaLocation||"没有指定位置";
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
			overClass: 'processViewDropSite',
			
			getDropPoint: function(e, n, dd){
				var tn = n.node;
				if (tn.isRoot) {
					return false; // always not allowed for root
				}
				var dragEl = tn.getUI().indentNode;//n.ddel;
				var t = Ext.lib.Dom.getY(dragEl), b = t + dragEl.offsetHeight;
				var y = Ext.lib.Event.getPageY(e);
				var noAppend = tn.allowChildren === false || tn.isLeaf();
				if (this.appendOnly || tn.parentNode.allowChildren === false) {
					return noAppend ? false : "append";
				}
				var indent = tn.getUI().indentNode.offsetHeight;
				if (y > t && y < t + indent) {
					return "above";
				}
				else {
					return noAppend ? false : "append";
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
		var rootNode = schemaTree.getRootNode();
		schemaTree.suspendEvents();
		this.clear(rootNode);
		rootNode.setText('Schema');
		
		var directives = new Ext.tree.AsyncTreeNode({
			id: 'DIRECTIVES',
			text: "指令",
			iconCls: 'xsd-icon-directives', 
			expanded: true,
			uiProvider: Ext.ux.SchemaBoxUI,
			allowDrag: false,
			children: []
		});
		var elements = new Ext.tree.AsyncTreeNode({
			id: 'ELEMENTS',
			text: "元素",
			iconCls: 'xsd-icon-elements', 
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
		rootNode.appendChild([directives, elements, typesNode]);
		rootNode.expand();
		Ext.getBody().mask('编辑数据字典前，请锁定数据字典!', 'x-mask-loading');
		Ext.Ajax.request({
			method: 'GET', 
			url: 'schema_ctrl.jsp', 
			params: { operation:"load", schema: schema },
			callback: function(options, success, response){
				Ext.getBody().unmask.defer(2000, Ext.getBody());
				try {
					if (success) {
						var rootEl = response.responseXML.documentElement;
						if (rootEl==null || rootEl.tagName!='schema') {
							alert('非法的文档模式');
							return;
						}
						var a = rootNode.attributes;
						a.xmlns = rootEl.getAttribute('xmlns') || "http://www.w3.org/2001/XMLSchema";
						a.targetNamespace= rootEl.getAttribute('targetNamespace') || targetNamespace;
						a.documentation = Loader.loadDocumentation(rootEl);
						
						rootNode.setText('Schema : '+ a.targetNamespace);
						Loader.root = rootNode;
						Loader.directives = directives;
						Loader.elements = elements;
						Loader.types = typesNode;
						
						Loader.loadNodes(rootEl, rootNode);
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
		var rootNode = schemaTree.getRootNode();
		var a = rootNode.attributes;
		var xmlns = a.xmlns || "http://www.w3.org/2001/XMLSchema";
		var xmldoc = XDom.createDocument(xmlns);
		try
		{
			var rootEl = xmldoc.createElement('schema');
			xmldoc.appendChild(rootEl);
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
					if (!a.prefix) {
						node.select();
						throw {message:'导入的 Schema 命名空间没有指定前缀'};
					}
					el.setAttribute('namespace', a.namespace);
					el.setAttribute('prefix', a.prefix);
				}
			});
			var elements = rootNode.findChild('id', 'ELEMENTS');
			Saver.saveNodes(elements, rootEl);
			var types = rootNode.findChild('id', 'TYPES');
			Saver.saveNodes(types, rootEl);
			
			schemaTree.body.mask('正在保存模型...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'schema_ctrl.jsp',
				params: { operation:"save", schema:schema, xmlns: xmlns, data: XDom.innerXML(xmldoc) },
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