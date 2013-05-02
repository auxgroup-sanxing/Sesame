var XDom = {

ELEMENT_NODE : 1,
ATTRIBUTE_NODE : 2,
TEXT_NODE : 3,
CDATA_SECTION_NODE : 4,
ENTITY_REFERENCE_NODE : 5,
ENTITY_NODE : 6,
PROCESSING_INSTRUCTION_NODE : 7,
COMMENT_NODE : 8,
DOCUMENT_NODE : 9,
DOCUMENT_TYPE_NODE : 10,
DOCUMENT_FRAGMENT_NODE : 11,
NOTATION_NODE : 12,

createDocument : function (ns, rootName) {
	var doc = null;
	var _document = window.document;
	if (typeof ActiveXObject != 'undefined') {
		var prefixes = ["MSXML2", "Microsoft", "MSXML", "MSXML3", "MSXML4", "MSXML5"];
		for (var i = 0; i < prefixes.length; i++) {
			try {
				doc = new ActiveXObject(prefixes[i] + ".XMLDOM");
			}
			catch (e) {
			}
			if (doc) {
				break;
			}
		}
	}
	else {
		if ((_document.implementation) && (_document.implementation.createDocument)) {
			doc = _document.implementation.createDocument(ns||"", rootName||"", null);
		}
	}
	return doc;
},

innerXML : function (node) {
	if (node.innerXML) {
		return node.innerXML;
	}
	else {
		if (node.xml) {
			return node.xml;
		} else {
			if (typeof XMLSerializer != "undefined") {
				return (new XMLSerializer()).serializeToString(node);
			}
		}
	}
},

firstElement : function (parentNode, tagName) {
	var node = parentNode.firstChild;
	while (node && node.nodeType != this.ELEMENT_NODE) {
		node = node.nextSibling;
	}
	if (tagName && node && node.tagName && node.tagName.toLowerCase() != tagName.toLowerCase()) {
		node = this.nextElement(node, tagName);
	}
	return node;
},

insertAfter : function (node, ref, force) {
	var pn = ref.parentNode;
	if (ref == pn.lastChild) {
		if ((force != true) && (node === ref)) {
			return false;
		}
		pn.appendChild(node);
	}
	else {
		return this.insertBefore(node, ref.nextSibling, force);
	}
	return true;
},

insertBefore : function (node, ref, force) {
	if ((force != true) && (node === ref || node.nextSibling === ref)) {
		return false;
	}
	var parent = ref.parentNode;
	parent.insertBefore(node, ref);
	return true;
},

lastElement : function (parentNode, tagName) {
	var node = parentNode.lastChild;
	while (node && node.nodeType != this.ELEMENT_NODE) {
		node = node.previousSibling;
	}
	if (tagName && node && node.tagName && node.tagName.toLowerCase() != tagName.toLowerCase()) {
		node = this.prevElement(node, tagName);
	}
	return node;
},

nextElement : function (node, tagName) {
	if (!node) {
		return null;
	}
	do {
		node = node.nextSibling;
	} while (node && node.nodeType != this.ELEMENT_NODE);
	if (node && tagName && tagName.toLowerCase() != node.tagName.toLowerCase()) {
		return this.nextElement(node, tagName);
	}
	return node;
},

prevElement : function (node, tagName) {
	if (!node) {
		return null;
	}
	if (tagName) {
		tagName = tagName.toLowerCase();
	}
	do {
		node = node.previousSibling;
	} while (node && node.nodeType != this.ELEMENT_NODE);
	if (node && tagName && tagName.toLowerCase() != node.tagName.toLowerCase()) {
		return this.prevElement(node, tagName);
	}
	return node;
},

selectSingleNode: function(doc, expr){
	if(doc.selectSingleNode) {
		return doc.selectSingleNode(expr);
	}
	else {
		var result=doc.evaluate(expr,doc,null,XPathResult.ANY_TYPE,null);
		return result.iterateNext();
	}
},

parseXml:function(xml) {
	if (typeof ActiveXObject != 'undefined') {
		var result=this.createDocument();
		result.async="false";
		result.loadXML(xml);
		return result;
	}
	else {
		var parser=new DOMParser();
		return parser.parseFromString(xml,"text/xml");
	}
}

};
