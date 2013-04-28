package org.sanxing.sesame.jaxp;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UniversalNamespaceResolver implements NamespaceContext {
	private static final String DEFAULT_NS = "DEFAULT";
	private Map<String, String> prefix2Uri = new HashMap();
	private Map<String, String> uri2Prefix = new HashMap();

	public UniversalNamespaceResolver(Document document, boolean toplevelOnly) {
		examineNode(document.getFirstChild(), toplevelOnly);
		System.out.println("The list of the cached namespaces:");
		for (String key : this.prefix2Uri.keySet())
			System.out.println("prefix " + key + ": uri "
					+ ((String) this.prefix2Uri.get(key)));
	}

	private void examineNode(Node node, boolean attributesOnly) {
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); ++i) {
			Node attribute = attributes.item(i);
			storeAttribute((Attr) attribute);
		}

		if (!(attributesOnly)) {
			NodeList chields = node.getChildNodes();
			for (int i = 0; i < chields.getLength(); ++i) {
				Node chield = chields.item(i);
				if (chield.getNodeType() == 1)
					examineNode(chield, false);
			}
		}
	}

	private void storeAttribute(Attr attribute) {
		if ((attribute.getNamespaceURI() == null)
				|| (!(attribute.getNamespaceURI()
						.equals("http://www.w3.org/2000/xmlns/"))))
			return;
		if (attribute.getNodeName().equals("xmlns")) {
			putInCache("DEFAULT", attribute.getNodeValue());
		} else
			putInCache(attribute.getLocalName(), attribute.getNodeValue());
	}

	private void putInCache(String prefix, String uri) {
		this.prefix2Uri.put(prefix, uri);
		this.uri2Prefix.put(uri, prefix);
	}

	public String getNamespaceURI(String prefix) {
		if ((prefix == null) || (prefix.equals(""))) {
			return ((String) this.prefix2Uri.get("DEFAULT"));
		}

		return ((String) this.prefix2Uri.get(prefix));
	}

	public String getPrefix(String namespaceURI) {
		return ((String) this.uri2Prefix.get(namespaceURI));
	}

	public Iterator<String> getPrefixes(String namespaceURI) {
		List list = new ArrayList();
		for (Iterator iter = this.prefix2Uri.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (namespaceURI.equals(entry.getValue())) {
				list.add((String) entry.getKey());
			}
		}
		return list.iterator();
	}
}