package org.sanxing.sesame.jaxp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;

public class NamespaceContextImpl implements NamespaceContext {
	private Map<String, String> namespaces;

	public NamespaceContextImpl() {
		this.namespaces = new LinkedHashMap();
	}

	public NamespaceContextImpl(Map<String, String> namespaces) {
		this.namespaces = new LinkedHashMap(namespaces);
	}

	public Map<String, String> getNamespaces() {
		return this.namespaces;
	}

	public void setNamespaces(Map<String, String> namespaces) {
		this.namespaces.clear();
		if (namespaces != null)
			this.namespaces.putAll(namespaces);
	}

	public String getNamespaceURI(String prefix) {
		if (prefix == null)
			throw new IllegalArgumentException("prefix argument was null");
		if (prefix.equals("xml"))
			return "http://www.w3.org/XML/1998/namespace";
		if (prefix.equals("xmlns"))
			return "http://www.w3.org/2000/xmlns/";
		if (this.namespaces.containsKey(prefix)) {
			String uri = (String) this.namespaces.get(prefix);
			if (uri.length() == 0) {
				return null;
			}
			return uri;
		}

		return null;
	}

	public String getPrefix(String nsURI) {
		if (nsURI == null)
			throw new IllegalArgumentException("nsURI was null");
		if (nsURI.length() == 0)
			throw new IllegalArgumentException("nsURI was empty");
		if (nsURI.equals("http://www.w3.org/XML/1998/namespace"))
			return "xml";
		if (nsURI.equals("http://www.w3.org/2000/xmlns/")) {
			return "xmlns";
		}
		Iterator iter = this.namespaces.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String uri = (String) entry.getValue();
			if (uri.equals(nsURI)) {
				return ((String) entry.getKey());
			}
		}
		if (nsURI.length() == 0) {
			return "";
		}
		return null;
	}

	public Iterator<String> getPrefixes(String nsURI) {
		if (nsURI == null)
			throw new IllegalArgumentException("nsURI was null");
		if (nsURI.length() == 0)
			throw new IllegalArgumentException("nsURI was empty");
		if (nsURI.equals("http://www.w3.org/XML/1998/namespace"))
			return Collections.singleton("xml").iterator();
		if (nsURI.equals("http://www.w3.org/2000/xmlns/")) {
			return Collections.singleton("xmlns").iterator();
		}
		Set prefixes = null;
		for (Map.Entry entry : this.namespaces.entrySet()) {
			String uri = (String) entry.getValue();
			if (uri.equals(nsURI)) {
				if (prefixes == null) {
					prefixes = new HashSet();
				}
				prefixes.add((String) entry.getKey());
			}
		}
		if (prefixes != null)
			return Collections.unmodifiableSet(prefixes).iterator();
		if (nsURI.length() == 0) {
			return Collections.singleton("").iterator();
		}
		List l = Collections.emptyList();
		return l.iterator();
	}
}