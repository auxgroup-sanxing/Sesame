Ext.BLANK_IMAGE_URL='../images/s.gif';

var Viewer = function(){
//变量
var flowTree;
var lastAction = null;

var Loader = {
	map : {
		'append' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'type': el.getAttribute('type'),
				'to-var': el.getAttribute('to-var'),
				'var': el.getAttribute('var'),
				index: el.getAttribute('index')
			};
			var rawEl=null, xpathEl=null;
			if (rawEl=XDom.firstElement(el, 'raw-value')) {
				var cdata = rawEl.firstChild;
				if (cdata) a['raw-value'] = cdata.nodeValue;
			}
			if (xpathEl=XDom.firstElement(el, 'xpath')){
				var cdata = xpathEl.firstChild;
				if (cdata) a['xpath'] = cdata.nodeValue;
			}
			var node = Viewer.createDomAppendNode(a);
			parentNode.insertBefore(node, refNode);
		},
		'assign' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'type': el.getAttribute('type'),
				'to-var': el.getAttribute('to-var'),
				'var': el.getAttribute('var'),
				'clone': el.getAttribute('clone')
			};
			var rawEl=null, xpathEl=null, commentEl=null;
			if (rawEl=XDom.firstElement(el, 'raw-value')){
				var cdata = rawEl.firstChild;
				if (cdata) a['raw-value'] = cdata.nodeValue;
			}
			if (xpathEl=XDom.firstElement(el, 'xpath')){
				var cdata = xpathEl.firstChild;
				if (cdata) a['xpath'] = cdata.nodeValue;
			}
			if (commentEl=XDom.firstElement(el, 'comment')){
				var cdata = commentEl.firstChild;
				if (cdata) a['comment'] = cdata.nodeValue;
			}
			var node = Viewer.createDeclareNode(a);
			parentNode.insertBefore(node, refNode);
		},
		'branch' : function(el, parentNode, refNode){
			var a = {}, node;
			if (el == XDom.firstElement(el.parentNode)) {
				node = parentNode.firstChild;
			}
			else if (el == XDom.lastElement(el.parentNode)) {
				node = parentNode.lastChild;
			}
			else {
				node = new Ext.tree.AsyncTreeNode({
					text: "分支",
					icon: Viewer.imagePath + 'branch.gif',
					uiProvider: Ext.ux.BranchNodeUI,
					tag: 'branch',
					allowDrag: false,
					allowEdit: true,
					allowRemove: true,
					expanded: true,
					children: [{
						cls: 'processNodeLabelHidden',
						uiProvider: Ext.ux.ProcessNodeUI,
						leaf: true,
						allowRemove: true,
						allowDrag: false
					}]
				});
				parentNode.insertBefore(node, refNode);
			}
			Loader.loadNodes(el, node, node.lastChild);
		},
		'break' : function(el, parentNode, refNode){
			parentNode.insertBefore(Viewer.createBreakNode(el.getAttribute('id')), refNode);
		},
		'delete' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'var': el.getAttribute('var')
			};
			var xpathEl;
			if (xpathEl=XDom.firstElement(el, 'xpath')) {
				var cdata = xpathEl.firstChild;
				if (cdata) a['xpath'] = cdata.nodeValue;
			}
			//a.text = '删除 '+a['var']+'['+a['xpath']+']';
			var node = Viewer.createDomDeleteNode(a);
			parentNode.insertBefore(node, refNode);
		},
		'callout' : function(el, parentNode, refNode){
			var a = {
				text: el.getAttribute('text'), 
				'mode': el.getAttribute('mode'),
				'actionId': el.getAttribute('id'),
				'use-var': el.getAttribute('use-var'), 
				'to-var': el.getAttribute('to-var'),
				'is-emulator': el.getAttribute('is-emulator'),
				'fault-type': el.getAttribute('fault-type'),
				'emulator-fault-text': el.getAttribute('emulator-fault-text'),
				'emulator-text': el.getAttribute('emulator-text'),
				'fault-code': el.getAttribute('fault-code')
			};
			var addrEl=XDom.firstElement(el, 'address');
			if (addrEl) {
				a['ref'] = addrEl.getAttribute('ref');
				var serviceEl=XDom.firstElement(addrEl, 'service-name');
				a['service-name'] = serviceEl&&serviceEl.firstChild ? serviceEl.firstChild.nodeValue : '';
				var intfEl=XDom.firstElement(addrEl, 'interface-name');
				a['interface-name'] = intfEl&&intfEl.firstChild ? intfEl.firstChild.nodeValue : '';
				var operEl=XDom.firstElement(addrEl, 'operation-name');
				a['operation-name'] = operEl&&operEl.firstChild ? operEl.firstChild.nodeValue : '';
			}
			var xsltEl=XDom.firstElement(el, 'xslt');
			if (xsltEl) {
				var cdata = xsltEl.firstChild;
				if (cdata) a.xslt = cdata.nodeValue;
			}
			
			var node = Viewer.createCallout(a);
			parentNode.insertBefore(node, refNode);
			
			var timeoutEl = XDom.firstElement(el, 'onTimeout');
			if (timeoutEl) {
				var exNode = Viewer.createTimeoutNode({
					'actionId': timeoutEl.getAttribute('id'),
					'index': timeoutEl.getAttribute('index'),
					'instant': timeoutEl.getAttribute('instant') || 'true',
					'timeout': timeoutEl.getAttribute('timeout')
				});
				node.appendChild(exNode);
				Loader.loadNodes(timeoutEl, exNode, exNode.lastChild);
			}
			var exceptEl = XDom.firstElement(el, 'onException');
			for (; exceptEl !== null; exceptEl=XDom.nextElement(exceptEl, 'onException')) {
				var exceptionKey = exceptEl.getAttribute('exception-key') || '';
				var instant = exceptEl.getAttribute('instant') || 'true';
				var index = exceptEl.getAttribute('index') || '0';
				var flowId = exceptEl.getAttribute('id');
				var exNode = Viewer.createExceptionNode({
					'exception-key': exceptionKey,
					'instant': instant,
					'actionId': flowId,
					'index': index
				});
				node.insertBefore(exNode);
				Loader.loadNodes(exceptEl, exNode, exNode.lastChild);
			}
			node.collapse();
			if (el.getAttribute('expanded')=='true') node.expand();
		},
		'decision' : function(el, parentNode, refNode){
			var node = Viewer.createDecisionNode({'actionId': el.getAttribute('id')});
			parentNode.insertBefore(node, refNode);
			el.getAttribute('expanded')=='true' ? node.expand() : node.collapse();
			
			Loader.loadNodes(el, node, node.lastChild);
		},
		'if' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id')
			};
			
			var ifNode, xpathEl, thenEl, commentEl;
			
			if (xpathEl=XDom.firstElement(el, 'xpath')) {
				var cdata = xpathEl.firstChild;
				if (cdata) a.xpath = cdata.nodeValue;
			}
			if (commentEl=XDom.firstElement(el, 'comment')){
				var cdata = commentEl.firstChild;
				if (cdata) a['comment'] = cdata.nodeValue;
			}

			if (el == el.parentNode.firstChild) {
				ifNode = parentNode.firstChild;
				Ext.apply(ifNode.attributes, a);
				ifNode.setText(Viewer.ellipsis(a.xpath, 20));
				ifNode.getUI().toggleCheck(!!a['xpath']);
			}
			else {
				a.toString = function(){
					return this.xpath ? Ext.util.Format.ellipsis(this.xpath, 20) : "条件";
				};
				ifNode = new Ext.tree.AsyncTreeNode(Ext.apply(a, {
					text: a.toString(), 
					uiProvider: Ext.ux.IfNodeUI,
					qtip: "条件",
					tag: 'if',
					allowDrag: false,
					allowEdit: true,
					allowRemove: true,
					expanded: true,
					checked: a['var']!=null && a['xpath']!=null,
					children: [{
						cls: 'processNodeLabelHidden',
						uiProvider: Ext.ux.ProcessNodeUI,
						leaf: true, 
						allowRemove: true,
						allowDrag: false
					}]
				}));
				parentNode.insertBefore(ifNode, refNode);
			}
			if (thenEl = XDom.firstElement(el, 'then')) {
				Loader.loadNodes(thenEl, ifNode, ifNode.lastChild);
			}
		},
		'default' : function(el, parentNode, refNode){
			var node= parentNode.lastChild;
			Loader.loadNodes(el, node, node.lastChild);
		},
		'do-while' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'var': el.getAttribute('var')
			};
			var xpathEl, doEl, node;
			if (xpathEl=XDom.firstElement(el, 'xpath')) {
				var cdata = xpathEl.firstChild;
				if (cdata) a.xpath = cdata.nodeValue;
			}
			var node = Viewer.createDowhileNode(a);
			parentNode.insertBefore(node, refNode);
			node.lastChild.setText(a['var']+'['+Viewer.ellipsis(a.xpath, 20)+']');
			if (xpathEl) {
				node.lastChild.attributes.actionId = xpathEl.getAttribute('id');
			}
			el.getAttribute('expanded')=='true' ? node.expand() : node.collapse();
			if (doEl = XDom.firstElement(el, 'do')) {
				Loader.loadNodes(doEl, node, node.lastChild);
			}
		},
		'finish' : function(el, parentNode, refNode){
			parentNode.insertBefore(Viewer.createFinishNode(el.getAttribute('id')), refNode);
		},
		'for-each' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'var': el.getAttribute('var'),
				'as': el.getAttribute('as')
			};
			var xpathEl, actionEl, node;
			if (xpathEl=XDom.firstElement(el, 'xpath')) {
				var cdata = xpathEl.firstChild;
				if (cdata) a.xpath = cdata.nodeValue;
			}
			a.text = a['as']+' : '+a['var']+'['+Viewer.ellipsis(a.xpath, 20)+']';
			var node = Viewer.createForeachNode(a);
			parentNode.insertBefore(node, refNode);
			node.firstChild.setText(a.text);

			el.getAttribute('expanded')=='true' ? node.expand() : node.collapse();
			if (actionEl = XDom.firstElement(el, 'actions')) {
				Loader.loadNodes(actionEl, node, node.lastChild);
			}
		},
		'group' : function(el, parentNode, refNode){
			var node = Viewer.createGroupNode({actionId: el.getAttribute('id'), text: el.getAttribute('text')});
			parentNode.insertBefore(node, refNode);
			el.getAttribute('expanded')=='true' ? node.expand() : node.collapse();
			Loader.loadNodes(el, node, node.lastChild);
		},
		'kill-time' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'wait': el.getAttribute('wait')
			};
			parentNode.insertBefore(Viewer.createSleepNode(a), refNode);
		},
		'log' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id')
			};
			for(var i=0; i<el.attributes.length; i++){
				var attr = el.attributes[i];
				a[attr.name] = attr.value;
			}
			parentNode.insertBefore(Viewer.createLoggerNode(a), refNode);
		},
		'namespace' : function(el, parentNode, refNode){
			var xpathEl=XDom.firstElement(el, 'xpath');
			var xpath = xpathEl ? xpathEl.firstChild.nodeValue : '';
			var a = {
				'actionId': el.getAttribute('id'),
				xpath: xpath
			};
			for(var i=0; i<el.attributes.length; i++){
				var attr = el.attributes[i];
				a[attr.name] = attr.value;
			}
			parentNode.insertBefore(Viewer.createNamespaceNode(a), refNode);
		},
		'fork' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'join': el.getAttribute('join')
			};
			var node = Viewer.createParallelNode(a);
			parentNode.insertBefore(node, refNode);
			el.getAttribute('expanded')=='true' ? node.expand() : node.collapse();
			Loader.loadNodes(el, node, node.lastChild);
		},
		'perform' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'func': el.getAttribute('function')
			};
			parentNode.insertBefore(Viewer.createPerformNode(a), refNode);
		},
		'rename' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'var': el.getAttribute('var'),
				'xpath': el.getAttribute('xpath'),
				'new-name': el.getAttribute('new-name')
			};
			var node = Viewer.createDomRenameNode(a);
			parentNode.insertBefore(node, refNode);
		},
		'sql' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'dsn': el.getAttribute('dsn'),
				'var': el.getAttribute('var'),
				'to-var': el.getAttribute('to-var')
			};
			var cdata = el.firstChild;
			if (cdata) a.sql = cdata.nodeValue;
			parentNode.insertBefore(Viewer.createSQLNode(a), refNode);
		},
		'throw' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'exception-key': el.getAttribute('exception-key'),
				'message': el.getAttribute('message')
			};
			parentNode.insertBefore(Viewer.createThrowNode(a), refNode);
		},
		'transform' : function(el, parentNode, refNode){
			var a = {'actionId': el.getAttribute('id')};
			for(var i=0; i<el.attributes.length; i++){
				var attr = el.attributes[i];
				a[attr.name] = attr.value;
			}
			var cdata = el.firstChild;
			if (cdata) a.xslt = cdata.nodeValue;
			parentNode.insertBefore(Viewer.createTransformNode(a), refNode);
		},
		'transaction' : function(el, parentNode, refNode){
			var a = {
				actionId: el.getAttribute('id'),
				dsn: el.getAttribute('dsn'),
				'tx-option': el.getAttribute('tx-option')
			};
			var node = Viewer.createXactNode(a);
			parentNode.insertBefore(node, refNode);
			el.getAttribute('expanded')=='true' ? node.expand() : node.collapse();
			Loader.loadNodes(el, node, node.lastChild);
		},
		'try-catch' : function(el, parentNode, refNode){
			var node = Viewer.createTrycatchNode({'actionId': el.getAttribute('id')});
			parentNode.insertBefore(node, refNode);
			el.getAttribute('expanded')=='true' ? node.expand() : node.collapse();
			
			Loader.loadNodes(el, node, node.lastChild);
		},
		'try' : function(el, parentNode, refNode){
			var tryNode = parentNode.firstChild;
			tryNode.attributes['actionId'] = el.getAttribute('id');
			Loader.loadNodes(el, tryNode, tryNode.lastChild);
		},
		'catch' : function(el, parentNode, refNode){
			//var node= parentNode.lastChild;
			var a = {
				'actionId': el.getAttribute('id'),
				'exception-key': el.getAttribute('exception-key'),
				'throw': el.getAttribute('throw')
			};
			
			var node, xpathEl, thenEl;
			
			if (el == el.parentNode.lastChild) {
				node = parentNode.lastChild;
				Ext.apply(node.attributes, a);
				node.setText(node.attributes.toString());
			}
			else {
				a.toString = function(){
					return 'catch('+(this['exception-key'] ? Ext.util.Format.ellipsis(this['exception-key'], 20)+')' : '*' )+")";
				};
				node = new Ext.tree.AsyncTreeNode(Ext.apply(a, {
					text: a.toString(), 
					icon: Viewer.imagePath+'on_exception.gif', 
					uiProvider: Ext.ux.IfNodeUI,
					qtip: "catch",
					tag: 'catch',
					allowDrag: false,
					allowEdit: true,
					allowRemove: true,
					expanded: true,
					children: [{
						cls: 'processNodeLabelHidden',
						uiProvider: Ext.ux.ProcessNodeUI,
						leaf: true, 
						allowRemove: true,
						allowDrag: false
					}]
				}));
				parentNode.insertBefore(node, refNode);
			}
			Loader.loadNodes(el, node, node.lastChild);
		},
		'while' : function(el, parentNode, refNode){
			var a = {
				'actionId': el.getAttribute('id'),
				'var': el.getAttribute('var')
			};
			var xpathEl, doEl, node;
			if (xpathEl=XDom.firstElement(el, 'xpath')) {
				var cdata = xpathEl.firstChild;
				if (cdata) a.xpath = cdata.nodeValue;
			}
			var node = Viewer.createWhiledoNode(a);
			parentNode.insertBefore(node, refNode);
			node.firstChild.setText(a['var']+'['+Viewer.ellipsis(a.xpath, 20)+']');
			if (xpathEl) {
				node.firstChild.attributes.actionId = xpathEl.getAttribute('id');
			}
			el.getAttribute('expanded')=='true' ? node.expand() : node.collapse();
			if (doEl = XDom.firstElement(el, 'do')) {
				Loader.loadNodes(doEl, node, node.lastChild);
			}
		},
		'scription' : function(el, parentNode, refNode) {
			var a = {
				'actionId': el.getAttribute('id'),
				'scriptDesc': el.getAttribute('desc')
			};
			var cdata = el.firstChild;
			if (cdata) a.scriptCode = cdata.nodeValue;
			var node = Viewer.createScriptNode(a);
			parentNode.insertBefore(node, refNode);
		}
	},
	
	loadNodes : function(parentEl, parentNode, refNode){
		var el = parentEl.firstChild;
		while (el) {
			var func = this.map[el.nodeName];
			if (func) func(el, parentNode, refNode);
			el = el.nextSibling;
		}
	},
	loadNameSpaces : function(parentEl, parentNode, refNode) {
			var ns = Ext.getCmp('namespace-grid');
			ns.store.removeAll();
			Ext.each(parentEl.attributes, function(item) {
				var PersonRecord = Ext.data.Record.create([
					{
						name : 'prefix'
					}, {
						name : 'nsUri'
					}
				]);
				var rec = new PersonRecord({
						prefix : item.localName,
						nsUri : item.nodeValue
					});
				ns.store.add(rec);
			});
		}
};

var Saver = {
	count : 0,
	map : {
		'assign' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			element.setAttribute('to-var', a['to-var']);
			element.setAttribute('type', a['type']);
			if (a['var']) {
				element.setAttribute('clone', a['clone']);
				element.setAttribute('var', a['var']);
				var xpathEl = parentEl.ownerDocument.createElement('xpath');
				element.appendChild(xpathEl);
				var text = parentEl.ownerDocument.createCDATASection(a['xpath']);
				xpathEl.appendChild(text);
			}
			else {
				var rawEl = parentEl.ownerDocument.createElement('raw-value');
				element.appendChild(rawEl);
				var cdata = parentEl.ownerDocument.createCDATASection(a['raw-value']);
				rawEl.appendChild(cdata);
			}
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'branch' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'break' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'callout' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			element.setAttribute("text", a.text);
			element.setAttribute("use-var", a['use-var']);
			element.setAttribute("to-var", a['to-var']);
			element.setAttribute("is-emulator", a['is-emulator']);
			element.setAttribute("emulator-text", a['emulator-text']);
			element.setAttribute("fault-type", a['fault-type']);
			element.setAttribute("emulator-fault-text", a['emulator-fault-text']);
			element.setAttribute("fault-code", a['fault-code']);
			element.setAttribute("mode", a['mode']);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			var addressEl = parentEl.ownerDocument.createElement('address');
			element.appendChild(addressEl);
			addressEl.setAttribute('ref', a['ref']||'');
			var serviceEl = parentEl.ownerDocument.createElement('service-name');
			serviceEl.appendChild(parentEl.ownerDocument.createTextNode(a['service-name']));
			addressEl.appendChild(serviceEl);
			var interfaceEl = parentEl.ownerDocument.createElement('interface-name');
			interfaceEl.appendChild(parentEl.ownerDocument.createTextNode(a['interface-name']));
			addressEl.appendChild(interfaceEl);
			var operationEl = parentEl.ownerDocument.createElement('operation-name');
			operationEl.appendChild(parentEl.ownerDocument.createTextNode(a['operation-name']));
			addressEl.appendChild(operationEl);
			
			if (a['xslt']) {
				var xsltEl = parentEl.ownerDocument.createElement('xslt');
				element.appendChild(xsltEl);
				var cdata = parentEl.ownerDocument.createCDATASection(a['xslt']);
				xsltEl.appendChild(cdata);
			}

			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'catch' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('catch');
			element.setAttribute('id', Saver.count++);
			element.setAttribute("exception-key", a['exception-key'] || '');
			element.setAttribute("throw", a['throw'] || 'false');
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'decision' : function(node, parentEl){
			var element = parentEl.ownerDocument.createElement('decision');
			element.setAttribute('id', Saver.count++);
			parentEl.appendChild(element);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			try {
				Saver.saveNodes(node, element);
			}
			catch (e) {
				node.getUI().toggleCheck(false);
				throw e;
			}
			node.attributes.actionId=element.getAttribute('id');
		},
		'dom-append' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('append');
			element.setAttribute('id', Saver.count++);
			element.setAttribute('to-var', a['to-var']);
			element.setAttribute('index', a['index']);
			if (a['var']) {
				element.setAttribute('var', a['var']);
				var xpathEl = parentEl.ownerDocument.createElement('xpath');
				element.appendChild(xpathEl);
				var text = parentEl.ownerDocument.createCDATASection(a['xpath']);
				xpathEl.appendChild(text);
			}
			else {
				element.setAttribute('type', a['type']);
				var rawEl = parentEl.ownerDocument.createElement('raw-value');
				element.appendChild(rawEl);
				var cdata = parentEl.ownerDocument.createCDATASection(a['raw-value']);
				rawEl.appendChild(cdata);
			}
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'dom-delete' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('delete');
			element.setAttribute('id', Saver.count++);
			element.setAttribute('var', a['var']);
			var xpathEl = parentEl.ownerDocument.createElement('xpath');
			xpathEl.appendChild(parentEl.ownerDocument.createCDATASection(a.xpath));
			element.appendChild(xpathEl);
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'dom-rename' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('rename');
			element.setAttribute('id', Saver.count++);
			element.setAttribute('var', a['var']);
			if (a.xpath) element.setAttribute('xpath', a['xpath']);
			element.setAttribute('new-name', a['new-name']);
			//var xpathEl = parentEl.ownerDocument.createElement('xpath');
			//xpathEl.appendChild(parentEl.ownerDocument.createCDATASection(a.xpath));
			//element.appendChild(xpathEl);
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'if' : function(node, parentEl){
			var element = parentEl.ownerDocument.createElement('if');
			element.setAttribute('id', Saver.count++);
			parentEl.appendChild(element);
			var a = node.attributes;
			if (!a.xpath) {
				node.select();
				throw {message:'没有指定条件表达式'};
			}
			var xpathEl = parentEl.ownerDocument.createElement('xpath');
			var cdata = parentEl.ownerDocument.createCDATASection(a['xpath']);
			xpathEl.appendChild(cdata);
			var commentEl = parentEl.ownerDocument.createElement('comment');
			var cdata = parentEl.ownerDocument.createCDATASection(a['comment']);
			commentEl.appendChild(cdata);
			var thenEl = parentEl.ownerDocument.createElement('then');
			element.appendChild(xpathEl);
			element.appendChild(commentEl);
			element.appendChild(thenEl);
			Saver.saveNodes(node, thenEl);
			node.attributes.actionId=element.getAttribute('id');
		},
		'default' : function(node, parentEl){
			var element = parentEl.ownerDocument.createElement('default');
			element.setAttribute('id', Saver.count++);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'do-while' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('do-while');
			element.setAttribute('id', Saver.count++);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			parentEl.appendChild(element);
			var a = node.lastChild.attributes;
			if (!a.xpath) {
				node.select();
				throw {message:'没有指定条件表达式'};
			}
			var xpathEl = parentEl.ownerDocument.createElement('xpath');
			var cdata = parentEl.ownerDocument.createCDATASection(a['xpath']);
			xpathEl.appendChild(cdata);
			var commentEl = parentEl.ownerDocument.createElement('comment');
			var cdata = parentEl.ownerDocument.createCDATASection(a['comment']);
			commentEl.appendChild(cdata);
			var doEl = parentEl.ownerDocument.createElement('do');
			element.appendChild(xpathEl);
			element.appendChild(commentEl);
			element.appendChild(doEl);
			Saver.saveNodes(node, doEl);
			xpathEl.setAttribute('id', Saver.count++);
			node.lastChild.attributes.actionId=xpathEl.getAttribute('id');
			node.attributes.actionId=element.getAttribute('id');
		},
		'finish' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'for-each' : function(node, parentEl){
			var element = parentEl.ownerDocument.createElement(node.attributes.tag);
			parentEl.appendChild(element);
			element.setAttribute('id', Saver.count++);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			var a = node.firstChild.attributes;
			element.setAttribute("var", a['var']);
			element.setAttribute("as", a['as']);
			var xpathEl = parentEl.ownerDocument.createElement('xpath');
			var cdata = parentEl.ownerDocument.createCDATASection(a['xpath']);
			xpathEl.appendChild(cdata);
			var doEl = parentEl.ownerDocument.createElement('actions');
			element.appendChild(xpathEl);
			element.appendChild(doEl);
			Saver.saveNodes(node, doEl);
			node.attributes.actionId=element.getAttribute('id');
		},
		'group' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('group');
			element.setAttribute('id', Saver.count++);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			element.setAttribute("text", a.text);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'log' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			if (a['name']) element.setAttribute('name', a['name']);
			element.setAttribute('var', a['var']);
			element.setAttribute('level', a['level']);
			if (a['msg']) element.setAttribute('msg', a['msg']);
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'namespace' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			element.setAttribute('method', a['method']);
			element.setAttribute('prefix', a['prefix']);
			element.setAttribute('uri', a['uri']);
			element.setAttribute('var', a['var']);
			var xpathEl = parentEl.ownerDocument.createElement('xpath');
			xpathEl.appendChild(parentEl.ownerDocument.createCDATASection(a.xpath));
			element.appendChild(xpathEl);
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'onException' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('onException');
			element.setAttribute('id', Saver.count++);
			element.setAttribute("exception-key", a['exception-key'] || '');
			element.setAttribute("instant", a['instant'] || 'true');
			element.setAttribute("index", a['index'] || '0');
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'onTimeout' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('onTimeout');
			element.setAttribute('id', Saver.count++);
			element.setAttribute('index', a.index||'0');
			element.setAttribute("instant", a['instant'] || 'true');
			element.setAttribute('timeout', a.timeout||'30');
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'parallel' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('fork');
			element.setAttribute('id', Saver.count++);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			element.setAttribute("join", a.join);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'perform' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			element.setAttribute("function", 'axp');
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'sleep' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('kill-time');
			element.setAttribute('id', Saver.count++);
			element.setAttribute('wait', a['wait']);
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'sql' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			element.setAttribute('dsn', a['dsn']);
			element.setAttribute("var", a['var']);
			element.setAttribute("to-var", a['to-var']);
			var cdata = parentEl.ownerDocument.createCDATASection(a['sql']);
			element.appendChild(cdata);
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'throw' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('throw');
			element.setAttribute('id', Saver.count++);
			element.setAttribute('exception-key', a['exception-key']);
			element.setAttribute('message', a['message']);
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'transform' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			element.setAttribute("var", a['var']);
			element.setAttribute("to-var", a['to-var']);
			var cdata = parentEl.ownerDocument.createCDATASection(a['xslt']);
			element.appendChild(cdata);
			parentEl.appendChild(element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'transaction' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			element.setAttribute("tx-option", a['tx-option']);
			element.setAttribute("dsn", a.dsn);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'try' : function(node, parentEl){
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement('try');
			element.setAttribute('id', Saver.count++);
			parentEl.appendChild(element);
			Saver.saveNodes(node, element);
			node.attributes.actionId=element.getAttribute('id');
		},
		'try-catch' : function(node, parentEl){
			var element = parentEl.ownerDocument.createElement('try-catch');
			element.setAttribute('id', Saver.count++);
			parentEl.appendChild(element);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			try {
				Saver.saveNodes(node, element);
			}
			catch (e) {
				node.getUI().toggleCheck(false);
				throw e;
			}
			node.attributes.actionId=element.getAttribute('id');
		},
		'while' : function(node, parentEl) {
			var element = parentEl.ownerDocument.createElement(node.attributes.tag);
			element.setAttribute('id', Saver.count++);
			parentEl.appendChild(element);
			element.setAttribute("expanded", node.isExpanded()?'true':'false');
			var a = node.firstChild.attributes;
			if (!a.xpath) {
				node.select();
				throw {message:'没有指定条件表达式'};
			}
			var xpathEl = parentEl.ownerDocument.createElement('xpath');
			var cdata = parentEl.ownerDocument.createCDATASection(a['xpath']);
			xpathEl.appendChild(cdata);
			var commentEl = parentEl.ownerDocument.createElement('comment');
			var cdata = parentEl.ownerDocument.createCDATASection(a['comment']);
			commentEl.appendChild(cdata);
			var doEl = parentEl.ownerDocument.createElement('do');
			element.appendChild(xpathEl);
			element.appendChild(commentEl);
			element.appendChild(doEl);
			Saver.saveNodes(node, doEl);
			xpathEl.setAttribute('id', Saver.count++);
			node.firstChild.attributes.actionId=xpathEl.getAttribute('id');
			node.attributes.actionId=element.getAttribute('id');
		},
		'scription' : function(node, parentEl) {
			var a = node.attributes;
			var element = parentEl.ownerDocument.createElement(a.tag);
			element.setAttribute('id', Saver.count++);
			element.setAttribute('desc', a['scriptDesc']);
			var cdata = parentEl.ownerDocument.createCDATASection(a['scriptCode']);
			element.appendChild(cdata);
			parentEl.appendChild(element);
			node.attributes.actionId = element.getAttribute('id');
		}
	},
	
	reset : function(){
		this.count = 0;
	},
	saveNodes : function(parentNode, parentEl){
		var node = parentNode.firstChild;
		while (node) {
			var func = this.map[node.attributes.tag];
			if (func) func(node, parentEl);
			node.getUI().toggleCheck(true);
			node = node.nextSibling;
		}
	}
};

var varList_tpl = new Ext.XTemplate(
	'<tpl for=".">',
	    '<div class="x-combo-list-item" id="{name}">',
	    '<div class="x-unselectable" unselectable="on" style="vertical-align:middle; line-height:16px;">',
		'<img style="width:16px; height:16px; vertical-align:middle;" class="{icon}" src="'+Ext.BLANK_IMAGE_URL+'" />',
	    '<span class="x-editable" style="vertical-align:middle;">{name}</span></div></div>',
	'</tpl>',
	'<div class="x-clear"></div>'
);

return {
	imagePath : Ext.ux.ProcessNodeUI.prototype.imagePath,

	ellipsis: Ext.util.Format.ellipsis,
	htmlEncode: Ext.util.Format.htmlEncode,
	
	
	init : function(){
		var _this = this;
		if (isVersioned == 'false')
			isLocked = 'true';

		if (typeof(XML) != 'undefined') {
			// E4X设置
			XML.prettyPrinting = true;
			XML.ignoreWhitespace = true;
			XML.prettyIndent = 4;
		}
		
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		Ext.Ajax.defaultHeaders = { "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8" };
		Ext.QuickTips.init();
		
		var flowRoot = new Ext.tree.AsyncTreeNode({
			text: '流程根节点',
			id: 'ROOT',
			children: [
				{
					actionId: "start", 
					cls: 'processNodeIndentHidden',
					icon: this.imagePath+"start_stateless.gif", 
					text:"开始", 
					uiProvider: Ext.ux.ProcessNodeUI, 
					allowDrag:false, allowDrop:false, 
					leaf:true
				},{
					actionId: "finish",
					text: "结束",
					icon: this.imagePath+'finish.gif', 
					uiProvider: Ext.ux.ProcessNodeUI,
					allowDrag: false,
					allowDrop: false, 
					allowChildren: false,
					leaf: true
				}
			]
		});
		var viewport = new Ext.Viewport({
			autoShow: true,
			boder: false,
			layout: 'border',
			stateId: 'diagram_viewer',
			id:'diagram_viewer',
			items:[
				{
					region: 'east',
					title: '操作面板',
					split: true,
					width: 200,
					minSize: 175,
					maxSize: 400,
					collapsible: true,
					margins: '0 0 0 0',
					layout: 'accordion',
					layoutConfig: {
						animate:true,hideCollapseTool: true
					},
					items: [{
						iconCls: 'palette_activity_category',
						title: '常用',
						border: false,
						xtype: 'treepanel',
						loader: new Ext.tree.TreeLoader(),
						id: 'standard-tree',
						rootVisible: false,
						lines: false,
						autoScroll: true,
						enableDrag: true,
						ddGroup: 'process',
						trackMouseOver: false,
						root: new Ext.tree.AsyncTreeNode({
						    text:'Standard',
						    children: this.getStandardActions()
						})
					},{
						iconCls: 'palette_activity_namespaces',
						title: '命名空间',
						xtype: 'editorgrid',
						id: 'namespace-grid',
						border: false,
						stripeRows: true,
						columnLines: true,
						frame: false,
						clicksToEdit: 2,
						viewConfig: {forceFit: true},
						store: new Ext.data.Store({
							reader: new Ext.data.JsonReader(
								{
									totalProperty: 'count',
									root: 'items'
								},
								[{name: 'prefix'},
								 {name: 'nsUri'}]
							)
						}),
						columns: [
							{
								header: '前缀',
								id: 'prefix',
								dataIndex: 'prefix',
								editor: new Ext.form.TextField({
									allowBlank: false
								})
							},
			        		{
								header: '命名空间',
								id: 'nsUri',
								dataIndex: 'nsUri',
								editor: new Ext.form.TextField({
									allowBlank: false
								})
							}
						],
						tools: [
						{
							id: 'plus',
							qtip: '添加选定的命名空间',
							handler: function(e, toolEl, panel, tc){
								panel.store.add(new panel.store.recordType({}));
								panel.startEditing(panel.store.getCount() - 1, 0);
								flowTree.getTopToolbar().items.get('save').enable();
							}
						},{
							id: 'minus',
							qtip: '删除选定的命名空间',
							handler: function(e, toolEl, panel, tc){
								var cell = panel.getSelectionModel().getSelectedCell();
								if (!cell || !window.confirm("确实要删除选中的项吗？")) 
									return;
								panel.store.removeAt(cell[0]);
								flowTree.getTopToolbar().items.get('save').enable();
							}
						}],
						listeners: {
							validateedit: function(obj) {
								if (obj.field == 'prefix') {
									var editedValue = obj.value;
									var store = obj.grid.getStore();
									store.each(function(rec){
										var prefix = rec.get('prefix');
										if (editedValue == prefix) {
											alert('前缀值不允许重复！');
											obj.cancel = true;
										}
									});
								}
							},
							// 修改完毕"保存"按钮可点
							afteredit: function() {
								flowTree.getTopToolbar().items.get('save').enable();
							}
						}
					},{
						id: 'service-tree',
						iconCls: 'palette_activity_host',
						title: '服务',
						xtype: 'treepanel',
						border: false,
						autoScroll: true,	// 服务名和服务列表过长时显示滚动条
						tools: [{
							id: 'refresh',
							qtip: '刷新',
							handler: function(e, toolEl, panel, tc){
								var tree = panel;
								var node = tree.selModel.getSelectedNode() || tree.root;
								if (node.isLeaf()) return;
								tree.body.mask('正在刷新', 'x-mask-loading');
								node.reload();
								node.collapse(true, false);
								setTimeout(function(){ // mimic a server call
									tree.body.unmask();
									node.expand(false, false);
								}, 500);
							}
						}],
						loader: new Ext.tree.TreeLoader({
							dataUrl:'diagram_ctrl.jsp', 
							baseParams:{service:servicePath, operation:'getServices'},
							listeners: {
								beforeload: function(loader, node) {
									loader.baseParams.path = node.getPath('name').substring(2);
								},
								loadexception: function(loader, node, response){ _this.showException(response); } 
							}
						}),
						rootVisible: false,	lines: false, animate: false, containerScroll: true,
						enableDrag: true,
						ddGroup: 'process',
						trackMouseOver: false,
						root: new Ext.tree.AsyncTreeNode({
						    text: 'Services',
							name: '',
							id: 'ROOT'
						}),
						listeners: {
							'append': function(tree, parent, node, index){
								if (node.attributes.allowDrag!=false) {
									var g = parent.parentNode.attributes;
									var a = parent.attributes;
									var o = node.attributes.options;
									o['service-name'] = g['service-name'] ? '{'+g['namespace']+'}'+g['service-name'] : '';
									o['interface-name'] = '{'+g['namespace']+'}'+a['name'];
									node.attributes.create = _this.createCallout;
								}
							}
							//beforecollapse: function(){ return false; }
						}
					}]
				},
				{
					region: 'center',
					xtype: 'treepanel',
					id: 'flow-pannel',
					loader: new Ext.tree.TreeLoader({preloadChildren: false, clearOnLoad: false}),
					root: flowRoot,
					rootVisible: false,
					lines: false,
					imagePath: this.imagePath,
					containerScroll: true,
					animate: false,
					autoScroll: true,
					bodyCssClass: 'edit-mode',
					bodyStyle: 'padding:5px; background-color:white;',
					enableDD: true,
					dragConfig: this.getDragConfig(),
					dropConfig: this.getDropConfig(),
					hlDrop: false,
					selModel: new Ext.tree.MultiSelectionModel(),
					trackMouseOver: false,
					tbar: this.getButtonArray(),
					keys: [{
						key: Ext.EventObject.DELETE, 
						fn: function(){ viewport.findById('flow-pannel').getTopToolbar().items.get('remove').handler(); }
					},{
						key: Ext.EventObject.ENTER, 
						fn: function(){ viewport.findById('flow-pannel').getTopToolbar().items.get('prop').handler(); }
					}]
				}
			]
		});

		flowTree = viewport.findById('flow-pannel');
		
		var topBtns = flowTree.getTopToolbar().items.map;
		var selModel = flowTree.getSelectionModel();
		
		flowTree.on('nodedragover', function(e){
			if (e.dropNode.getOwnerTree() != e.tree){
				if (isLocked != 'true')
					return false;
			}

			if (e.point=='above') {
				if (e.target == e.dropNode.nextSibling) {
					e.cancel = true;  return;
				}
				if(!!e.target.previousSibling && e.target.previousSibling.attributes.tag == 'finish')  {	// 之前的节点如果是结束则不能添加
					e.cancel = true;  return;
				}
				if (e.dropNode.attributes.tag == 'finish'){
					switch (e.target.parentNode.attributes.tag)
					{
						case 'catch':
						case 'default':
						case 'if':
						case 'onException':
						case 'onTimeout':
							break;
						default:
							e.cancel = true;  return;
					}
				} 
				else if (e.dropNode.attributes.tag == 'break'){
					var allowDrop=false;
					var parent=e.target, a;
					while (parent=parent.parentNode)
					{
						a = parent.attributes;
						if (a.tag == 'while' || a.tag == 'do-while' || a.tag == 'for-each'){
							allowDrop=true;  break;
						}
					}
					e.cancel = !allowDrop;
				}
			} 
			else {
				e.cancel = true;
			}
		});
		
		var hideSpotCls = 'processViewHideHotspot';
		flowTree.on('startdrag', function(treePanel, node, e){
			Ext.get(node.getUI().wrap).addClass(hideSpotCls);
			if (node.nextSibling) Ext.get(node.nextSibling.getUI().indentNode).addClass(hideSpotCls);
		});
		flowTree.on('enddrag', function(treePanel, node, e){
			Ext.get(node.getUI().wrap).removeClass(hideSpotCls);
			if (node.nextSibling) Ext.get(node.nextSibling.getUI().indentNode).removeClass(hideSpotCls);
		});

		flowTree.on('beforenodedrop', function(e){
			if (e.dropNode.getOwnerTree() == e.tree) {
				if (e.dropNode.nextSibling) 
					Ext.get(e.dropNode.nextSibling.getUI().indentNode).removeClass(hideSpotCls);
				return true;
			}
			else {
				if (e.dropNode.attributes.create) {
					var a = e.dropNode.attributes;
					var config = {
						text: a.text
					};
					if (a.options) {
						if (a.options['service-name'] != undefined) {
							var text = a.options['service-name'].replace(/\{.*?\}/ig, '');
							if (text == 'undefined')
								a.options['service-name'] = '';
						}
						Ext.apply(config, a.options);
					}
					e.dropNode = e.dropNode.attributes.create(config);
				} 
				else {
					alert('没有找到组件的构造器');
					return false;
				}
			}
		});
		
		flowTree.on('append', function(tree, parent, node, index){
			flowTree.body.addClass('edit-mode');
			topBtns.save.enable();
		});
		flowTree.on('insert', function(tree, parent, node, ref){
			flowTree.body.addClass('edit-mode');
			topBtns.save.enable();
		});
		flowTree.on('movenode', function(tree, oldParent, newParent, index){
			flowTree.body.addClass('edit-mode');
			topBtns.save.enable();
		});
		flowTree.on('remove', function(tree, parent, node){
			flowTree.body.addClass('edit-mode');
			topBtns.save.enable();
		});
		
		var addcatch_Handler = function(){
			var exceptNode = _this.createExceptionNode();
			var node = selModel.getSelectedNodes()[0];
			node.collapse();
			node.appendChild(exceptNode);
			node.expand();
		};
		var addtimeout_Handler = function(){
			var timeoutNode = _this.createTimeoutNode({index:0, timeout:30});
			var node = selModel.getSelectedNodes()[0];
			node.collapse();
			node.appendChild(timeoutNode);
			node.expand();
		};
		var addxact_Handler = function(){
			var sels = selModel.getSelectedNodes();
			var xact = _this.createXactNode();
			var firstNode = sels[0];
			firstNode.parentNode.insertBefore(xact, firstNode);
			for (var i=sels.length-1; i>=0; i--) {
				xact.insertBefore(sels[i], xact.firstChild);
			}
		};
		var addgroup_Handler = function(){
			var sels = selModel.getSelectedNodes();
			var group = _this.createGroupNode();
			var firstNode = sels[0];
			firstNode.parentNode.insertBefore(group, firstNode);
			for (var i=sels.length-1; i>=0; i--) {
				group.insertBefore(sels[i], group.firstChild);
			}
		};
		
		var callout_Handler = function() {
			var sels = selModel.getSelectedNodes();
			var node = selModel.getSelectedNodes()[0];
			var attributes = node.attributes;
			
			var ref = attributes['ref'];
			var opera = attributes['operation-name'];
			var desc = node.text;
			
			Ext.Ajax.request({
				method: 'POST',
				url: 'diagram_ctrl.jsp',
				params: {
					operation : "getDeclaration",
					ref : ref
				},
				
				callback : function(options, success, response) {
					if (!success) {
						_this.showException(response);
					} 
					else {
						var declaration = Ext.util.JSON.decode(response.responseText);
						if (declaration.type=='process') {
							var path = ref.replace(/.xsd$/ig, '');
							top.Application.openProcess({
								id: path,
								text: desc,
								pathname: path,
								activeItem: 1
							});
						} 
						else {
							top.Application.openWindow(
								ref,
								opera + '-' + desc,
								'../schema/encoding_viewer.jsp?schema='+ ref , 
								'settings');
						}
					}
				}
			});
		}
		
		this.contextMenu = new Ext.menu.Menu({
			id: 'flowMenu',
			enableScrolling: false,
			items: [
				{
					id: 'expand',
					text: '展开',
					handler: function(){
						var sels = selModel.getSelectedNodes();
						if (sels.length>0) sels[0].expand();
					}
				},
				new Ext.menu.Item({
					id: 'prop',
					text: '属性',
					handler: topBtns.prop.handler
				}),
				{
					id: 'remove',
					icon:"../images/tool16/delete_edit.gif",
					text: '删除',
					handler: topBtns.remove.handler
				},
				{
					id: 'view',
					text: '查看',
					handler: callout_Handler
				},
				'-',
				{
					id: 'addgroup',
					text: '添加组',
					handler: addgroup_Handler
				},
				{
					id: 'addxact',
					text: '添加事务',
					handler: addxact_Handler
				},
				'-',
				{
					id: 'addcatch',
					text: '添加回滚处理',
					handler: addcatch_Handler
				},
				{
					id: 'addtimeout',
					text: '添加超时处理',
					handler: addtimeout_Handler
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
		flowTree.on('contextmenu', function(node, e){
		    if (!node.isSelected()) node.select();
			
			var nodes = selModel.getSelectedNodes();
			var a = node.attributes;
			var allowRemove=false;
			var innerCallOut = false;
			switch (a.tag)
			{
				case 'branch':
				case 'if':
					allowRemove = node.parentNode.childNodes.length > 2;
					break;
				case 'default':
					allowRemove = false;
					break;
				case 'callout':		// callout添加异常后再添加callout,则该callout禁止添加异常
					if (node.parentNode.attributes.tag == 'onException') {
						innerCallOut = true;
					}
					allowRemove = node.attributes.allowRemove;
					break;	
				default:
					allowRemove = node.attributes.allowRemove;
			}
			var items = _this.contextMenu.items;
			
			var lock = isLocked == 'true' ;
			
			items.get('expand').setVisible((!node.isLeaf() && !node.isExpanded()) && lock);
			items.get('addcatch').setVisible(nodes.length<2 && lock);
			items.get('addcatch').setDisabled(!a.allowCatch || !lock || innerCallOut);
			items.get('addtimeout').setVisible(nodes.length<2 && lock);
			items.get('addtimeout').setDisabled((!a.allowTimeout || node.findChild('tag', 'onTimeout')!=null) || !lock);
			items.get('prop').setDisabled((!a.allowEdit || selModel.getSelectedNodes().length!=1));
			items.get('remove').setDisabled(!allowRemove || !lock);
			items.get('view').setVisible(a.tag == 'callout');
			if(lock){
				items.get('addgroup').enable();
				items.get('addxact').enable();
			}else{
				items.get('addgroup').disable();
				items.get('addxact').disable();
			}
			
			for (var i=0,len=nodes.length; i<len; i++) {
				var n = nodes[i], at = n.attributes;
				if (n.parentNode != node.parentNode || at.actionId == 'start' || at.actionId == 'finish' || n.ui.wrap.nodeName!='LI') {
					items.get('addgroup').disable();
					items.get('addxact').disable();
					break;
				}
			}
			
		    _this.contextMenu.showAt(e.getXY());
		});

		flowTree.on('dblclick', function(node, e){
			if (node.attributes.allowEdit && (!node.hasChildNodes() || node.attributes.tag=='if' || node.attributes.tag=='onException')) {
				_this.openAction(node);
			}
		});

		selModel.on('selectionchange', function(){
			var sels = selModel.getSelectedNodes();
			if (sels.length<1) {
				topBtns.remove.disable();
				topBtns.prop.disable();
				return;
			}
			var node = sels[0];
			var a = node.attributes, allowRemove=false;
			switch (a.tag)
			{
				case 'branch':
					allowRemove = node.parentNode.childNodes.length > 2;
					break;
				case 'default':
					allowRemove = false;
					break;
				case 'if':
					allowRemove = node.nextSibling.attributes.tag==a.tag || node.previousSibling!=null;
					break;
				default:
					allowRemove = node.attributes.allowRemove;
			}
			topBtns.prop.setDisabled(!a.allowEdit);
			topBtns.remove.setDisabled(!allowRemove);
		});

		window.setTimeout(function(){ _this.load(flowRoot); }, 500);
				
		window.onbeforeclose = function(e){
			if (!topBtns.save.disabled) {
				if (isLocked == 'true') {
					beforeCloseFlg = confirm('放弃所做的修改吗？\n点击"确定"关闭，点击"取消"可以选择保存');
					if (!beforeCloseFlg) {
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
	},
	
	cloneNode: function(node){
		var _this = this;
		var atts = node.attributes;
		atts.id = Ext.id();
		var clonedNode = new Ext.tree.TreeNode(Ext.apply({}, atts));
		clonedNode.text = node.text;
		
		for (var i = 0; i < node.childNodes.length; i++) {
			clonedNode.appendChild(_this.cloneNode(node.childNodes[i]));
		}
		return clonedNode;
	},
	
	getButtonArray: function(){
		var _this = this;
		
		var buttons = [
			{
				itemId: 'prop',
				text: '属性',
				icon: '../images/elcl16/prop_ps.gif',
				cls: 'x-btn-text-icon',
				handler : function(){
					var sels = flowTree.getSelectionModel().getSelectedNodes();
					if (sels.length<1 || !sels[0].attributes.allowEdit)
						return false;
					_this.openAction(sels[0]);
			    }
			},
			{
				itemId: 'remove',
				text: '删除',
				icon: '../images/tool16/delete_edit.gif',
				cls: 'x-btn-text-icon',
				disabled: true,
			    handler: function(){
					if (this.disabled)  
						return false;
					if (!window.confirm('确实要删除选中的项吗？'))  
						return false;
					
					var sels = flowTree.getSelectionModel().getSelectedNodes();
					for (var i = sels.length - 1; i >= 0; i--) {
						var n = sels[i];
						if (!n || !n.attributes.allowRemove)  
							continue;
						if ((n.attributes.tag=='if' || n.attributes.tag=='branch') && n.parentNode.childNodes.length<3)  
							continue;
							
						// 删除"组"和"事务"时需要用户确认:保留子节点或整体删除							
						if (n.attributes.tag == 'transaction' ||
							n.attributes.tag == 'group') {
								var text = n.text;
								Ext.Msg.show({
									title: '删除' + text,
									msg: '选择"是"删除' + text + '及其所有被包含内容,选择"否"只删除'+ text,
									buttons: Ext.Msg.YESNO,
									fn: function(btn) {
										if(btn == 'no') {
											Ext.each(n.childNodes, function(child, idx){
												n.parentNode.insertBefore(_this.cloneNode(child), n);
											});
										}
										n.remove();
									},
									icon: Ext.MessageBox.INFO
								});
						} else {
							n.remove();
						}
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
					if (!flowTree.getTopToolbar().items.get('save').disabled){
						if (!window.confirm('流程已经更改，确实要放弃当前所做的修改吗？'))  return false;
					} 
					_this.load();
				}
			},
			{
				id: 'save',
			    text: '保存',
				icon: '../images/tool16/save_edit.gif',
				cls: 'x-btn-text-icon',
				disabled: isLocked != 'true',
			    listeners : {
					click:function(){
						_this.save();
					},
					enable:function(button){
						if (isLocked != 'true')
							button.disable();
					}
			    }
			},
			'-',
			{
				id: 'stepdebug',
			    text: '调试',
				icon: '../images/tool16/launch_debug.gif',
				cls: 'x-btn-text-icon',
				disabled: false,
			    handler : function(){
					if(!Ext.getCmp('save').disabled) {
						Ext.Msg.alert('提示:', '请先保存流程再进行单步调试！');
						return;
					}
					
					if (!Ext.get('debugWin')) {
						flowTree.body.addClass('debug-mode');
						// 生成测试xml数据
						if (!isPublic) {
							Ext.Ajax.request({
								method: 'POST',
								url: 'diagram_ctrl.jsp',
								params: {
									operation: 'generateXml',
									schemaPath: servicePath.replace(/\.xml/ig, '\.xsd'),
									wsdlPath: servicePath.replace(/\w+\.xml/ig, 'unit.wsdl'),
									operaName: servicePath.replace(/\.xml/ig, '').replace(/.*\//ig, '')
								},
								callback: function(options, success, response){
									var debugData;
									if (success) 
										debugData = response.responseText.replace(/(\r\n)$/ig, '');
									_this.stepDebug(debugData).show();
								}
							});
						} else {
							var win = Application.getDesktop().getActiveWindow();
							var schemaPath;
							if (!!win)
								schemaPath = win.id + '.xsd';
	
							Ext.Ajax.request({
								method: 'POST',
								url: 'diagram_ctrl.jsp',
								params: {
									operation: 'generateXml',
									schemaPath: schemaPath,
									wsdlPath: servicePath.replace(/\w+\.xml/ig, 'unit.wsdl'),
									operaName: servicePath.replace(/\.xml/ig, '').replace(/.*\//ig, '')
								},
								callback: function(options, success, response){
									var debugData;
									if (success) 
										debugData = response.responseText.replace(/(\r\n)$/ig, '');
									_this.stepDebug(debugData).show();
								}
							});
						}	
					}
			    }
			}
		];
		return buttons;
	},
	
	getStandardActions : function(){
		return [/*{
			text: '响应',
			iconCls: 'palette_client_send',
			cls: 'palette_hideIndent',
			create: this.createClientRspNode,
			leaf: true
		},*/
		{
			text: '赋值',
			iconCls: 'palette_declare_var',
			cls: 'palette_hideIndent',
			create: this.createDeclareNode,
			leaf: true
		},{
			text: '命名空间',
			iconCls: 'palette_ns',
			cls: 'palette_hideIndent',
			create: this.createNamespaceNode,
			options: { method: 'set' },
			leaf: true
		},{
			text: '添加元素',
			iconCls: 'palette_dom_add',
			cls: 'palette_hideIndent',
			create: this.createDomAppendNode,
			leaf: true
		},{
			text: '删除元素',
			iconCls: 'palette_dom_del',
			cls: 'palette_hideIndent',
			create: this.createDomDeleteNode,
			leaf: true
		},{
			// TODO
			text: '脚本',
			iconCls: 'palette_script_add',
			cls: 'palette_hideIndent',
			create: this.createScriptNode,
			leaf: true
		},{
			text: '重命名',
			iconCls: 'palette_dom_ren',
			cls: 'palette_hideIndent',
			create: this.createDomRenameNode,
			leaf: true
		},{
			text: '休眠',
			iconCls: 'palette_sleep',
			cls: 'palette_hideIndent',
			create: this.createSleepNode,
			options: { wait: 30000 },
			leaf: true
		},{
			text: '日志',
			iconCls: 'palette_perform',
			cls: 'palette_hideIndent',
			create: this.createLoggerNode,
			options: { level: 'info' },
			leaf: true
		},{
			text: '数据库操作',
			iconCls: 'palette_sql',
			cls: 'palette_hideIndent',
			create: this.createSQLNode,
			level: 'debug',
			leaf: true
		},{
			text: '转换',
			iconCls: 'palette_transform',
			cls: 'palette_hideIndent',
			create: this.createTransformNode,
			leaf: true
		},{
			text: '决策',
			iconCls: 'palette_decision',
			cls: 'palette_hideIndent',
			create: this.createDecisionNode,
			leaf: true
		},{
			text: '当型循环',
			iconCls: 'palette_while_do',
			cls: 'palette_hideIndent',
			create: this.createWhiledoNode,
			leaf: true
		},{
			text: '直到型循环',
			iconCls: 'palette_do_while',
			cls: 'palette_hideIndent',
			create: this.createDowhileNode,
			leaf: true
		},{
			text: '遍历',
			iconCls: 'palette_for_each',
			cls: 'palette_hideIndent',
			create: this.createForeachNode,
			leaf: true
		},{
			text: '跳出',
			iconCls: 'palette_break',
			cls: 'palette_hideIndent',
			tag: 'break',
			create: this.createBreakNode,
			leaf: true
		},{
			text: '并行',
			iconCls: 'palette_parallel',
			cls: 'palette_hideIndent',
			create: this.createParallelNode,
			leaf: true
		},{
			text: '异常捕捉',
			iconCls: 'palette_try-catch',
			cls: 'palette_hideIndent',
			create: this.createTrycatchNode,
			leaf: true
		},{
			text: '抛出异常',
			iconCls: 'palette_throw',
			cls: 'palette_hideIndent',
			create: this.createThrowNode,
			options: { 'exception-key': '' },
			leaf: true
		},{
			text: '结束',
			iconCls: 'palette_finish',
			cls: 'palette_hideIndent',
			tag: 'finish',
			create: this.createFinishNode,
			leaf: true
		}];
	},
	
	getToolbar: function(){
		return flowTree.getTopToolbar();
	},
	
	createDomAppendNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		a.toString = function() {
			return this['to-var']+'<b style="color:maroon;"> += </b>'+Viewer.ellipsis(this['var']?this['var']+'['+this['xpath']+']':
				Viewer.htmlEncode(this['raw-value']), 20);
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: '添加子元素',
			icon: Viewer.imagePath+'dom_add.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			text: a.toString(),
			tag: 'dom-append',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			checked: false,
			leaf: true,
			children: []
		}));
	},
	
	createDomDeleteNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		a.toString = function() {
			return '<font color="maroon">delete</font> '+this['var']+'['+Viewer.ellipsis(this['xpath'], 20)+']';
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: '删除子元素',
			icon: Viewer.imagePath+'dom_remove.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			text: a.toString(),
			tag: 'dom-delete',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			leaf: true
		}));
	},
	
	createScriptNode: function(config) {
		// TODO
		var a = (typeof config)=='object' ? config : {};
		a.toString = function() {
			return this['scriptDesc'] || '脚本';
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: '脚本',
			icon: Viewer.imagePath+'script_add.png', 
			uiProvider: Ext.ux.ProcessNodeUI,
			text: a.toString(),
			tag: 'scription',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			leaf: true
		}));
	},
	
	createDomRenameNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		a.toString = function() {
			return '<font color="maroon">rename</font> '+this['var']+(this['xpath']?'['+this['xpath']+']':'') +
				'->'+this['new-name'];
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: '设置DOM元素名称',
			icon: Viewer.imagePath+'dom_rename.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			text: a.toString(),
			tag: 'dom-rename',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			checked: false,
			leaf: true,
			children: []
		}));
	},
	
	createBreakNode : function(actionId){
		return new Ext.tree.AsyncTreeNode({
			text: "跳出循环",
			icon: Viewer.imagePath+'break.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			tag: 'break',
			actionId: actionId,
			allowDrag: true,
			allowRemove: true,
			leaf: true,
			children: []
		});
	},
	
	createCallout : function(config){
		var a = (typeof config)=='object' ? config : {};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			icon: Viewer.imagePath+'control_send_return.gif', 
			rightIcon: Viewer.imagePath+'control.gif', 
			uiProvider: Ext.ux.CompositeNodeUI,
			tag: 'callout',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			allowCatch: true,
			allowTimeout: true,
			expanded: true,
			checked: false,
			leaf: false,
			children: []
		}));
	},
	
	createClientRspNode : function(){
		return new Ext.tree.AsyncTreeNode({
			text: "响应",
			icon: Viewer.imagePath+'client_response.gif', 
			leftIcon: Viewer.imagePath+'client.gif', 
			uiProvider: Ext.ux.CompositeNodeUI,
			tag: 'clientResponse',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			allowCatch: true,
			leaf: true
		});
	},

	createNamespaceNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		a.toString = function() {
			return '<font color="maroon">'+(this['method']=='set' ? '' : this['method']+"&nbsp;")+
				'xmlns'+(this['prefix'] ? ':'+this['prefix'] : '') + '</font>="'+Viewer.ellipsis(this['uri'], 10)+'", '+
				this['var']+'['+Viewer.ellipsis(this['xpath'], 10)+']';
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: '命名空间',
			icon: '../images/obj32/namespace.png', 
			uiProvider: Ext.ux.ProcessNodeUI,
			text: a.toString(),
			tag: 'namespace',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			leaf: true
		}));
	},

	createDecisionNode : function(config){
		var node = new Ext.tree.AsyncTreeNode({
			text: "决策", 
			uiProvider: Ext.ux.DecisionNodeUI,
			tag: 'decision',
			actionId: config ? config.actionId : null,
			expanded: true, 
			allowRemove: true,
			checked: false,
			children: [{
				text: "条件", 	qtip: "条件",
				uiProvider: Ext.ux.IfNodeUI,
				tag: 'if',
				allowDrag: false,
				allowEdit: true,
				allowRemove: true,
				expanded: true,
				checked: false,
				toString: function() {
					return Viewer.ellipsis(this['xpath'], 20);
				},
				children: [{
					cls: 'processNodeLabelHidden',
					uiProvider: Ext.ux.ProcessNodeUI,
					leaf: true, 
					allowChildren: false, 
					allowDrag: false
				}]
			},
			{
				text: "缺省", 
				icon: Viewer.imagePath+'decision_default.gif', 
				uiProvider: Ext.ux.IfNodeUI,
				trCls: 'processLineTR',
				tag: 'default',
				allowDrag: false,
				expanded: true,
				children: [{
					cls: 'processNodeLabelHidden',
					uiProvider: Ext.ux.ProcessNodeUI,
					leaf: true, 
					allowChildren: false, 
					allowDrag: false
				}]
			}]
		});	
		//node.ui.addBranch = Viewer.onDecisionAdd;
		return node;
	},

	createDeclareNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		a.toString = function() {
			return this['to-var']+'<b style="color:maroon;"> = </b>'+Viewer.ellipsis(this['var']?this['var']+'['+this['xpath']+']':
				Viewer.htmlEncode(this['raw-value']), 20);
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: '赋值',
			icon: Viewer.imagePath+'store.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			text: a.toString(),
			tag: 'assign',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			checked: false,
			leaf: true,
			children: []
		}));
	},
	
	createDowhileNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		return new Ext.tree.AsyncTreeNode({
			text: "直到型循环", 
			uiProvider: Ext.ux.DowhileUI,
			tag: 'do-while',
			actionId: a.actionId,
			allowRemove: true,
			expanded: true,
			checked: false,
			children: [Ext.apply(a, {
				text: "条件", 
				icon: Viewer.imagePath+'do_while.gif',
				iconCls: 'processLineBL_dowhile',
				uiProvider: Ext.ux.WhileUI,
				toString: function() {
					return Viewer.ellipsis(this['xpath'], 20);
				},
				qtip: "条件",
				tag: 'while-meta',
				leaf: true, 
				checked: a['var']!=null && a['xpath']!=null,
				allowChildren: false, 
				allowEdit: true,
				allowDrag: false,
				allowDrop: false
			})]
		});
	},
	
	createExceptionNode: function(config){
		var a = (typeof config)=='object' ? config : { index: 0 };
		a.toString = function() { return '回滚处理('+this.index+')'; };
		
		var node = new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: "回滚处理",
			icon: Viewer.imagePath+'on_exception.gif', 
			uiProvider: Ext.ux.PathNodeUI,
			text: a.toString(), 
			tag: 'onException',
			allowDrag: false,
			allowEdit: true,
			allowRemove: true,
			expanded: true,
			children: []
		}));
		node.appendChild(new Ext.tree.AsyncTreeNode({
				cls: 'processNodeLabelHidden',
				uiProvider: Ext.ux.ProcessNodeUI,
				leaf: true, 
				allowChildren: false, 
				allowDrag: false
			}));
		return node;
	},
	
	createFinishNode : function(id){
		return new Ext.tree.AsyncTreeNode({
			text: "结束",
			icon: Viewer.imagePath+'finish.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			tag: 'finish',
			actionId: id,
			allowDrag: true,
			allowRemove: true,
			children: [],
			leaf: true
		});
	},
	
	createForeachNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		return new Ext.tree.AsyncTreeNode({
			text: "遍历", 
			icon: Viewer.imagePath+'for_each_collapsed.gif',
			uiProvider: Ext.ux.LoopNodeUI,
			tag: 'for-each',
			expanded: true,
			allowRemove: true,
			checked: false,
			children: [Ext.apply(a, {
				text: a.text || "集合",
				qtip: "集合", 
				cls: 'processNodeIndentHidden',
				icon: Viewer.imagePath+'for_each.gif',
				iconCls: 'processLineTL_whiledo',
				uiProvider: Ext.ux.EachUI,
				toString: function() {
					return this['as']+' : '+this['var']+'['+Viewer.ellipsis(this['xpath'], 20)+']';
				},
				tag: 'for-meta',
				leaf: true, 
				checked: a['as']!=null && a['var']!=null && a['xpath']!=null,
				allowEdit: true,
				allowDrag: false,
				allowDrop: false
			}),
			{
				cls: 'processNodeLabelHidden',
				uiProvider: Ext.ux.ProcessNodeUI,
				leaf: true, 
				allowDrag: false,
				allowDrop: false
			}]
		});
	},
	
	createGroupNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		return new Ext.tree.AsyncTreeNode({
			qtip: "分组", 
			text: a.text || "分组", 
			uiProvider: Ext.ux.GroupNodeUI,
			tag: 'group',
			actionId: a.actionId,
			allowEdit: true,
			allowRemove: true,
			expanded: true,
			checked: false,
			children: [{
				cls: 'processNodeLabelHidden',
				uiProvider: Ext.ux.ProcessNodeUI,
				leaf: true, 
				allowChildren: false, 
				allowDrag: false
			}]
		});
	},
	
	createLoggerNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		a.toString = function() {
			return Viewer.ellipsis(this['msg'] || '日志', 20);
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: '日志',
			icon: Viewer.imagePath+'perform.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			text: a.toString(),
			tag: 'log',
			level: a.level || 'debug',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			checked: false,
			leaf: true,
			children: []
		}));
	},
	
	createPerformNode : function(a){
		return new Ext.tree.AsyncTreeNode({
			text: "执行",
			qtip: '执行',
			icon: Viewer.imagePath+'perform.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			tag: 'perform',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			checked: false,
			leaf: true,
			children: []
		});
	},
	
	createParallelNode : function(){
		var node = new Ext.tree.AsyncTreeNode({
			text: "并行", 
			//icon: Viewer.imagePath+'parallel_collapsed.gif', 
			uiProvider: Ext.ux.ParallelNodeUI,
			tag: 'parallel',
			join: 'and',
			expanded: true, 
			allowRemove: true,
			children: [{
				text: "分支", 
				icon: Viewer.imagePath+'branch.gif', 
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
			},
			{
				text: "分支", 
				icon: Viewer.imagePath+'branch.gif', 
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
			}]
		});
		//node.ui.addBranch = Viewer.onParallelAdd;
		return node;
	},
	
	createSleepNode : function(config){
		var a = (typeof config)=='object' ? config : { wait: 30000 };
		a.toString = function(){
			return '休眠: '+this.wait+' 毫秒';
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: '休眠',
			icon: Viewer.imagePath+'sleep.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			text: a.toString(),
			tag: 'sleep',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			leaf: true,
			children: []
		}));
	},
	
	createSQLNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		a.toString = function() {
			return Viewer.ellipsis(this['sql'] || 'SQL', 20) + (this['var'] ? '<font color="maroon">%'+this['var']+'%</font>' : '');
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: 'SQL语句',
			icon: Viewer.imagePath+'sql32.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			text: a.toString(),
			tag: 'sql',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			checked: false,
			leaf: true,
			children: []
		}));
	},
	
	createThrowNode : function(config){
		var a = (typeof config)=='object' ? config : { 'exception-key': '' };
		a.toString = function(){
			return '<font color="maroon">throw</font>&nbsp;'+this['exception-key']+"|"+this.message;
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			qtip: '抛出异常',
			icon: '../images/obj32/critical_msg.png', 
			uiProvider: Ext.ux.ProcessNodeUI,
			text: a.toString(),
			tag: 'throw',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			checked: false,
			leaf: true,
			children: []
		}));
	},
	
	createTimeoutNode: function(config){
		var a = (typeof config)=='object' ? config : {};
		a.toString = function() {
			return this.timeout ? '超时'+this.timeout+'秒' : '超时处理';
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			text: a.text || a.toString(), 
			icon: Viewer.imagePath+'on_timeout.gif', 
			qtip: '超时处理',
			uiProvider: Ext.ux.PathNodeUI,
			tag: 'onTimeout',
			allowDrag: false,
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
		}));
	},
	
	createTransformNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		a.toString = function(){
			return 'context<font color="maroon"> -&gt; </font>'+this['to-var'];
		};
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			text: a.text || a.toString(),
			qtip: "转换",
			icon: Viewer.imagePath+'transform.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			tag: 'transform',
			allowDrag: true,
			allowEdit: true,
			allowRemove: true,
			leaf: true, checked: false,
			children: []
		}));
	},
	
	createTrycatchNode : function(config){
		var node = new Ext.tree.AsyncTreeNode({
			text: "异常捕捉", 
			uiProvider: Ext.ux.TryCatchUI,
			tag: 'try-catch',
			actionId: config ? config.actionId : null,
			expanded: true, 
			allowRemove: true,
			children: [{
				text: "try", 	qtip: "try",
				icon: Viewer.imagePath+'on_message.gif', 
				uiProvider: Ext.ux.IfNodeUI,
				tag: 'try',
				allowDrag: false,
				allowEdit: false,
				allowRemove: false,
				expanded: true,
				toString: function() {
					return 'try';
				},
				children: [{
					cls: 'processNodeLabelHidden',
					uiProvider: Ext.ux.ProcessNodeUI,
					leaf: true, 
					allowChildren: false, 
					allowDrag: false
				}]
			},
			{
				text: "catch", 
				icon: Viewer.imagePath+'on_exception.gif', 
				uiProvider: Ext.ux.IfNodeUI,
				trCls: 'processLineTR',
				tag: 'catch',
				allowDrag: false,
				allowEdit: true,
				expanded: true,
				toString: function(){
					return this['exception-key'] ? 'catch('+Ext.util.Format.ellipsis(this['exception-key'], 20)+')' : "catch(*)";
				},
				children: [{
					cls: 'processNodeLabelHidden',
					uiProvider: Ext.ux.ProcessNodeUI,
					leaf: true, 
					allowChildren: false, 
					allowDrag: false
				}]
			}]
		});	
		//node.ui.addBranch = Viewer.onDecisionAdd;
		return node;
	},

	createWhiledoNode : function(config){
		var a = (typeof config)=='object' ? config : {};
		
		return new Ext.tree.AsyncTreeNode({
			text: "当型循环", 
			//icon: Viewer.imagePath+'while_do_collapsed.gif',
			uiProvider: Ext.ux.LoopNodeUI,
			tag: 'while',
			actionId: a.actionId,
			allowRemove: true,
			expanded: true,
			checked: false,
			children: [Ext.apply(a, {
				text: "条件", 
				icon: Viewer.imagePath+'while_do.gif',
				iconCls: 'processLineTL_whiledo',
				cls: 'processNodeIndentHidden',
				uiProvider: Ext.ux.WhileUI,
				toString: function() {
					return Viewer.ellipsis(this['xpath'], 20);
				},
				qtip: "条件",
				tag: 'while-meta',
				leaf: true, 
				checked: a['var']!=null && a['xpath']!=null,
				allowEdit: true,
				allowDrag: false,
				allowDrop: false
			}),
			{
				cls: 'processNodeLabelHidden',
				uiProvider: Ext.ux.ProcessNodeUI,
				leaf: true, 
				allowDrag: false,
				allowDrop: false
			}]
		});
	},
	
	createXactNode : function(config){
		var a = (typeof config)=='object' ? config : { 'tx-option': 'require-new'};
		a.toString = function(){ return "事务"; };
		return new Ext.tree.AsyncTreeNode(Ext.apply(a, {
			text: "事务", 
			uiProvider: Ext.ux.XactNodeUI,
			tag: 'transaction',
			allowEdit: true, 
			allowRemove: true,
			expanded: true,
			checked: false,
			children: [{
				cls: 'processNodeLabelHidden',
				uiProvider: Ext.ux.ProcessNodeUI,
				leaf: true, 
				allowChildren: false, 
				allowDrag: false
			}]
		}));
	},

	getDragConfig : function(){
		return {
			ddGroup: 'process',
			
		    afterRepair : function(){
		        this.dragging = false;
		    }
		};
	},
	
	getDropConfig : function(){
		return {
			overClass: 'processViewDropSite',
			ddGroup: 'process',
			
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
			},
			notifyEnter: function(dd, e, data){
				if (this.overClass) {
					this.el.addClass(this.overClass);
				}
				return this.dropNotAllowed;
			},
			notifyOut: function(dd, e, data){
				if (this.lastOverNode) {
					this.onNodeOut(this.lastOverNode, dd, e, data);
					this.lastOverNode = null;
				}
				if (this.overClass) {
					this.el.removeClass(this.overClass);
				}
			},
			notifyDrop: function(dd, e, data){
				if (this.lastOverNode) {
					this.onNodeOut(this.lastOverNode, dd, e, data);
					this.lastOverNode = null;
				}
				if (this.overClass) {
					this.el.removeClass(this.overClass);
				}
				var n = this.getTargetFromEvent(e);
				return n ? this.onNodeDrop(n, dd, e, data) : this.onContainerDrop(dd, e, data);
			},
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
						var el = node.ui.indentNode; //n.ddel;
						var cls;
						if (pt == "above") {
							returnCls = n.node.isFirst() ? "x-tree-drop-ok-above" : "x-tree-drop-ok-between";
							cls = "processViewDropTarget"; //"x-tree-drag-insert-above";
						}
						else 
							if (pt == "below") {
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
					var el = n.node.ui.indentNode; //n.ddel;
					Ext.fly(el).removeClass(["processViewDropTarget", "x-tree-drag-insert-above", "x-tree-drag-insert-below", "x-tree-drag-append"]);
					this.lastInsertClass = "_noclass";
				}
			}
		};
	},
	
	clear : function(rootNode){
		var first=rootNode.firstChild, node=null;
		if (!first) return;
		
		while (node=first.nextSibling)
		{
			if (node.isLast())
				break;
			else
				node.remove();
		};
	},

	load : function(){
		var _this = this;
		var rootNode = flowTree.getRootNode();

		flowTree.suspendEvents();
		this.clear(rootNode);
		var loadingNode = new Ext.tree.AsyncTreeNode({
			text: "正在装载...",
			icon: this.imagePath+'loading.gif', 
			uiProvider: Ext.ux.ProcessNodeUI,
			allowDrag: false,
			allowDrop: false,
			leaf: true,
			children: []
		});
		rootNode.insertBefore(loadingNode, rootNode.lastChild);
		
		
		Ext.Ajax.request({
			method: 'POST', 
			url: 'diagram_ctrl.jsp', 
			params: {operation:"load", service: servicePath },
			callback: function(options, success, response){
				loadingNode.remove();
				try {
					if (success) {
						if (response.responseXML.documentElement.tagName != 'process') {
							alert('非法的流程描述脚本');
							return;
						}
						Loader.loadNodes(response.responseXML.documentElement, rootNode, rootNode.lastChild);
						Loader.loadNameSpaces(response.responseXML.documentElement, rootNode, rootNode.lastChild);
					}
					else {
						_this.showException(response);
					}
				}
				finally {
					flowTree.resumeEvents();
					flowTree.getTopToolbar().items.get('save').disable();
				}
			}
		});
	},

	save : function(){
		var _this = this;
		var xmldoc = XDom.createDocument();
		try
		{
			var ns = Ext.getCmp('namespace-grid');
			var rootEl = xmldoc.createElement('process');
			ns.store.each(function(rec){
				var prefix = rec.get('prefix');
				var nameURI = rec.get('nsUri');
				rootEl.setAttribute('xmlns:' + prefix,nameURI);
			})
			xmldoc.appendChild(rootEl);
			var rootNode = flowTree.getRootNode();
			flowTree.body.removeClass('edit-mode');
			Saver.reset();
			Saver.saveNodes(rootNode, rootEl);
			
			Ext.getBody().mask('正在保存流程...', 'x-mask-loading');
			Ext.Ajax.request({
				method: 'POST', 
				url: 'diagram_ctrl.jsp', 
				params: {operation:"save", service: servicePath, data: XDom.innerXML(xmldoc) },
				callback: function(options, success, response){
					Ext.getBody().unmask(); 
					if (success)
						flowTree.getTopToolbar().items.get('save').disable(); 
					else
						_this.showException(response);
				}
			});
		}
		catch(e){
			alert(e.message);
		} finally {
			delete xmldoc;
		}
	},
	
	// XML验证
	validateXML : function(txt){
		var rs;
		if(txt == undefined || !txt){
		return '';
		} 
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
	},
	
	/*单步调试*/
	stepDebug : function(data) {
		var _this = this;
		
		// 给节点增加调试标志
		function toggleFlag(node) {
			node.getUI().addClass('action-hilited');
		}
		
		function getFlagNode(node){
			var flagNode;
			if (node.isLeaf()) {
				var nodeUI = node.getUI();
				if (nodeUI.elNode.getAttribute('class').indexOf('debugFlag') != -1) {
					flagNode = node;
					return flagNode;
				}
			} else if (!!node.childNodes && node.childNodes.length > 0) {
					for (var i = 0; i < node.childNodes.length; i++) {
						var child = node.childNodes[i];
						flagNode = getFlagNode(child);
						if (!!flagNode)
							break;
					}
				}
			return flagNode;
		};
		
		function findActionNode(parent, actionId) {
			var actNode = parent.findChild('actionId', actionId);
			if (actNode != null) {
				return actNode;
			} 
			
			for (var i = 0; i < parent.childNodes.length; i++) {
				actNode = findActionNode(parent.childNodes[i], actionId);
				if (actNode != null)	break;
			}
			return actNode;
		};
		
		// 开始调试
		function startDebug() {
			xmlOutput.store.removeAll();
			var toolbar = xmlinput.getTopToolbar();
			if (!!xmlinput.getForm().isValid()) {
				// xml验证
				var xml = Ext.get('xmlInputArea').getValue();
				var info = _this.validateXML(xml);
				if (!!info) {
					Ext.Msg.alert('提示:', info);
					return;
				}
				
				// 工具栏显示
				toolbar.findById('start').disable();
				
				var rootNode = flowTree.getRootNode();
				rootNode.expand(true, false, Ext.emptyFn);	// 展开所有子节点
				
				rootNode.cascade(function(node){
					node.getUI().removeClass('action-hilited');
					var loopline = node.getUI().loopline;
					if (loopline) {
						loopline.innerHTML = '&nbsp;';
						delete node.attributes.count;
						Ext.fly(node.getUI().frameNode).removeClass('line-hilited');
					}
				});
				lastAction = null;
				
				var suName = servicePath.replace(/.*?engine\//ig, '').split('/')[0];
				var operaName = servicePath.replace(/.*?engine\//ig, '').split('/')[1].replace(/\..*/ig, '');

				Ext.Ajax.request({
					method: 'POST',
					url: 'debug_ctrl.jsp',
					timeout: 3600 * 1000,
					params: {
						operation: 'startDebug',
						flowXml: servicePath,
						suName: suName,
						operaName: operaName, 
						data: encodeURIComponent(xml)
					},
					callback: function(options, success, response) {
						// 工具栏显示
						if (success) {
							toolbar.findById('step').enable();
							toolbar.findById('resume').enable();
							toolbar.findById('terminate').enable();
							var result = Ext.decode(response.responseText);
							
							if (result.context) {
								xmlOutput.store.removeAll();
								Ext.each(result.context.items, function(item){
									var tmp = Ext.util.Format.htmlEncode(item.value);
									item.value = tmp;
									var rec = new Ext.data.Record(item);
									xmlOutput.store.add(rec);
								});
							}
							stepIn(result.actionId);
						}
						else {
							_this.showException(response);
						}
					}
				});
			}
		};
		
		// 单步行进
		function stepIn(actionId){
			var rootNode = flowTree.getRootNode();
			if (lastAction != null && lastAction.nextSibling==lastAction.parentNode.lastChild) {
				var a = lastAction.parentNode.attributes;
				switch (a.tag) {
					case 'if':
					case 'default':
						Ext.fly(lastAction.parentNode.getUI().wrap).addClass('line-hilited');
					case 'group':
						lastAction.nextSibling.getUI().addClass("action-hilited");
						break;
				}
			}
			// 获取下一流程节点
			var actNode =  findActionNode(rootNode, actionId); 
			if (actNode != null) {
				if (actNode.isLeaf()) {
					actNode.ensureVisible();
				}
				toggleFlag(actNode);	// 增加调试标志
				
				
				if (actNode.isFirst()) {
					var a = actNode.parentNode.attributes;
					switch (a.tag) {
					case 'while':
					case 'do-while':
						if (typeof(a.count)=='number') {
							a.count++;
						}
						else if (a.tag=='do-while') {
							a.count = 0;
						}
						else {
							a.count = 1;
						}
						if (a.count > 0) {
							actNode.parentNode.getUI().loopline.innerHTML = '<b style="background-color:white;">'+a.count+'</b>';
						}
						if (a.count == 1) {
							Ext.fly(actNode.parentNode.getUI().frameNode).addClass('line-hilited');
						}
						
						var nextNode = actNode;
						while (nextNode=nextNode.nextSibling) {
							nextNode.getUI().removeClass("action-hilited");
						}
						break;
					}
				}
				lastAction = actNode;
			}
		};
		
		// 执行到流程结束
		function resume() {
			Ext.Ajax.request({
				method: 'POST',
				url: 'debug_ctrl.jsp',
				params: {operation: 'resume'},
				callback: function(){
					
				}
			});
			
			var toolbar = xmlinput.getTopToolbar();
			toolbar.findById('start').enable();
			toolbar.findById('step').disable();
			toolbar.findById('resume').disable();
			toolbar.findById('terminate').disable();
		};
		
		// 终止流程调试
		function terminate() {
			Ext.Ajax.request({
				method: 'POST',
				url: 'debug_ctrl.jsp',
				params: {operation: 'terminate'},
				callback: function(){
					
				}
			});
			
			var toolbar = xmlinput.getTopToolbar();
			toolbar.findById('start').enable();
			toolbar.findById('step').disable();
			toolbar.findById('resume').disable();
			toolbar.findById('terminate').disable();
		};
		
		var debugButtons = [{
			id: 'start',
		    text: '开始',
			icon: '../images/tool16/launch_run.gif',
			cls: 'x-btn-text-icon',
			disabled: false,
		    handler : startDebug
		},'-',{
			id: 'step',
		    text: '单步执行',
			icon: '../images/tool16/stepinto_co.gif',
			cls: 'x-btn-text-icon',
			disabled: true,
		    handler: function(){
				// 开始时禁止"步进"按钮
				xmlinput.getTopToolbar().findById('step').disable();
				var data = Ext.get('xmlInputArea').getValue();
				
				// 此处获取下一流程的flowID
				var rootNode = flowTree.getRootNode();
				Ext.Ajax.request({
					method: 'POST',
					url: 'debug_ctrl.jsp',
					timeout: 3600 * 1000,
					params: {
						operation: 'stepDebug',
						flowXml: servicePath,
						data: encodeURIComponent(data)
					},
					callback: function(options, success, response){
						if (success) {
							var rs = Ext.util.JSON.decode(response.responseText);
							if (rs.flowId == 'exception') {
								var msg = '';
								if (!!rs['exception'])
									msg = rs['exception'];
								
								Ext.Msg.show({
									title: '流程错误',
									maxWidth: 300,
									msg: msg,
									icon: Ext.MessageBox.ERROR
								});
								
								terminate();
								return;
							}
							
							var flowId = rs.flowId;
							
							stepIn(flowId);
							
							if (!!rs.items && rs.items.length > 0) {
								xmlOutput.store.removeAll();
								Ext.each(rs.items, function(item){
									var tmp = Ext.util.Format.htmlEncode(item.value);
									item.value = tmp;
									var rec = new Ext.data.Record(item);
									xmlOutput.store.add(rec);
								});
							}
							// 请求返回后允许"步进"按钮
							xmlinput.getTopToolbar().findById('step').enable();
							
							if (rs.done==true) {
								resume();
								Ext.Msg.alert('提示:', '流程执行结束！');
								return;
							}
						} 
						else {
							Ext.Msg.alert('错误:', response.responseText);
						}
					}
				});
			}
		},{
			id: 'resume',
		    text: '连续执行',
			icon: '../images/elcl16/resume_co.gif',
			cls: 'x-btn-text-icon',
			disabled: true,
		    handler : resume
		},'-',{
			id: 'terminate',
		    text: '终止执行',
			icon: '../images/tool16/launch_stop.gif',
			cls: 'x-btn-text-icon',
			disabled: true,
		    handler : terminate
		}];
		
		var	xmlinput = new Ext.form.FormPanel({
			region: 'north',
			id: 'xmlInput',
			resizable: false,
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
			tbar: debugButtons,
			items: [{
		    	xtype: 'textarea',
				value: (!!data) ? data : '',
				id: 'xmlInputArea',
		        name: 'xmlInputArea',
				anchor: '100%, 100%',
				emptyText : '<请输入初始XML然后点击开始>',
		        allowBlank: false,
		        blankText: '请输入XML',
				style: {'width':'100%', 'height':'100%', 'border':'1 1 1 1px'},
				preventScrollbars: false
		    }]
		});
		
		var xmlOutput = new Ext.grid.GridPanel({
			region: 'center',
			id: 'xmlOutput',
			border: true,
			resizable: false,
			hideLabels: true,
			stateful: false,
			stripeRows: true,
			modal: true,
			columnLines: true,
			frame: false,
			store: new Ext.data.Store({
				reader: new Ext.data.JsonReader(
					{
						totalProperty: 'count',
						root: 'items'
					},
					[{name: 'key'},
					 {name: 'value'}]
				)
			}),
			columns: [
				{header: '变量名', id: 'var', width: 50, dataIndex: 'key'},
        		{header: '值', id:'varValue', dataIndex: 'value'}
			],
			viewConfig: {forceFit: true, singleSelect: true}
		});
		
		xmlOutput.on('cellclick', function(grid, rowIndex, columnIndex, e) {
			var record = grid.getStore().getAt(rowIndex);
			var value = record.get('value');
			
			// 详细内容格式化
			alert(Ext.util.Format.htmlDecode(value));			
		});
		
		// 获取屏幕宽度
		var dbgWinWidth = 430;
		var screenWidth = window.screen.availWidth;
		var pageX = screenWidth - dbgWinWidth - 20;
		
		var debugWin = new Ext.Window({
			title: '流程调试',
			layout : 'border',
			id: 'debugWin',
			width: dbgWinWidth,
			height: 320,
			border: false,
			pageX: pageX,
			pageY: 100,
			iconCls: 'settings',
			items : [xmlinput, xmlOutput],
			listeners: {
				close: function(window) {
					terminate();
					flowTree.getRootNode().cascade(function(node){
						node.getUI().removeClass('action-hilited');
						var loopline = node.getUI().loopline;
						if (loopline) {
							loopline.innerHTML = '&nbsp;';
							Ext.fly(node.getUI().frameNode).removeClass('line-hilited');
						}
					});
					flowTree.body.removeClass('debug-mode');
				}
			}
		});
		return debugWin;
	},
	
	onDecisionAdd : function(e){
		e.stopEvent();
		var branch = new Ext.tree.AsyncTreeNode({
			text: "条件", 
			uiProvider: Ext.ux.IfNodeUI,
			tag: 'if',
			allowDrag: false,
			allowEdit: true,
			allowRemove: true,
			expanded: true,
			checked: false,
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
	},
	
	openAction : function(node){
		// Action对话框打开时展开命名空间面板
		var view = Ext.getCmp('diagram_viewer');
		view.getLayout().east.panel.items.get('namespace-grid').expand();		
		
		//lock the flow-tree
		var topBtns = flowTree.getTopToolbar().items.map;
		var lock = isLocked == 'true';
		node.attributes.lock = lock;
		switch (node.attributes.tag)
		{
			case 'assign':
				Dialog.showAssignDialog(node);				break;
			case 'callout':
				Dialog.getInvokeDialog(node).show();		break;
			case 'catch':
				Dialog.showCatchDialog(node);				break;
			case 'dom-append':
				Dialog.getAppendDialog(node).show();		break;
			case 'dom-delete':
				Dialog.getDeleteDialog(node).show();		break;
			case 'scription':
				Dialog.getScriptDialog(node).show(); 		break;	
			case 'dom-rename':
				Dialog.getRenameDialog(node).show();		break;
			case 'log':
				Dialog.getLoggerDialog(node).show();		break;
			case 'namespace':
				Dialog.showNamespaceDialog(node);			break;
			case 'onException':
				Dialog.showRollbackDialog(node);			break;
			case 'onTimeout':
				Dialog.showTimeoutDialog(node);				break;
			case 'for-meta':
				Dialog.getForeachDialog(node).show();		break;
			case 'if':
			case 'while-meta':
				Dialog.showConditionDialog(node);			break;
			case 'sleep':
				Dialog.getSleepDialog(node).show();				break;
			case 'sql':
				Dialog.getSQLDialog(node).show();				break;
			case 'throw':
				Dialog.showThrowDialog(node);				break;
			case 'transaction':
				Dialog.getXactDialog(node).show();				break;
			case 'transform':
				Dialog.getTransfDialog(node).show();				break;
			case 'group':
				var text = prompt("请输入标题", node.attributes.text);
				if (text) {
					node.setText(text);
					this.getToolbar().items.get('save').enable();
				}
				break;
		}
	},
	
	closeAction: function() {
		var view = Ext.getCmp('diagram_viewer');
		view.getLayout().east.panel.items.get('standard-tree').expand();
		var xpathDoc = Ext.getCmp('xpathHelper');
		if (!!xpathDoc) xpathDoc.close();
		var helpDoc = Ext.getCmp('settingHelper');
		if (!!helpDoc) helpDoc.close();
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
};

}();

Ext.onReady(Viewer.init, Viewer, true);