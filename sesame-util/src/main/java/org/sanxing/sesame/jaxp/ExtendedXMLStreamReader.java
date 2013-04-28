package org.sanxing.sesame.jaxp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

public class ExtendedXMLStreamReader extends StreamReaderDelegate {
	private SimpleNamespaceContext context = new SimpleNamespaceContext();

	public ExtendedXMLStreamReader(XMLStreamReader delegate) {
		super(delegate);
	}

	public NamespaceContext getNamespaceContext() {
		return this.context;
	}

	public int nextTag() throws XMLStreamException {
		int eventType = next();
		while (((eventType == 4) && (isWhiteSpace()))
				|| ((eventType == 12) && (isWhiteSpace())) || (eventType == 6)
				|| (eventType == 3) || (eventType == 5)) {
			eventType = next();
		}
		if ((eventType != 1) && (eventType != 2)) {
			throw new XMLStreamException("expected start or end tag",
					getLocation());
		}
		return eventType;
	}

	public int next() throws XMLStreamException {
		int next = super.next();
		if (next == 1)
			this.context = new SimpleNamespaceContext(this.context,
					getNamespaces());
		else if (next == 2) {
			this.context = this.context.getParent();
		}
		return next;
	}

	private Map getNamespaces() {
		Map ns = new HashMap();
		for (int i = 0; i < getNamespaceCount(); ++i) {
			ns.put(getNamespacePrefix(i), getNamespaceURI(i));
		}
		return ns;
	}

	public static class SimpleNamespaceContext implements
			ExtendedNamespaceContext {
		private SimpleNamespaceContext parent;
		private Map namespaces;

		public SimpleNamespaceContext() {
			this.namespaces = new HashMap();
		}

		public SimpleNamespaceContext(SimpleNamespaceContext parent,
				Map namespaces) {
			this.parent = parent;
			this.namespaces = namespaces;
		}

		public SimpleNamespaceContext getParent() {
			return this.parent;
		}

		public Iterator getPrefixes() {
			Set prefixes = new HashSet();
			for (SimpleNamespaceContext context = this; context != null; context = context.parent) {
				prefixes.addAll(context.namespaces.keySet());
			}
			return prefixes.iterator();
		}

		public String getNamespaceURI(String prefix) {
			String uri = (String) this.namespaces.get(prefix);
			if ((uri == null) && (this.parent != null)) {
				uri = this.parent.getNamespaceURI(prefix);
			}
			return uri;
		}

		public String getPrefix(String namespaceURI) {
			for (SimpleNamespaceContext context = this; context != null; context = context.parent) {
				for (Iterator it = context.namespaces.keySet().iterator(); it
						.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					if (entry.getValue().equals(namespaceURI)) {
						return ((String) entry.getKey());
					}
				}
			}
			return null;
		}

		public Iterator getPrefixes(String namespaceURI) {
			Set prefixes = new HashSet();
			for (SimpleNamespaceContext context = this; context != null; context = context.parent) {
				for (Iterator it = context.namespaces.keySet().iterator(); it
						.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					if (entry.getValue().equals(namespaceURI)) {
						prefixes.add(entry.getKey());
					}
				}
			}
			return prefixes.iterator();
		}
	}
}