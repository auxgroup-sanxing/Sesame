package com.sanxing.sesame.util;

import java.util.Collection;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public final class QNameUtil {
	public static String toString(QName qname) {
		if (qname.getNamespaceURI() == null) {
			return "{}" + qname.getLocalPart();
		}
		return "{" + qname.getNamespaceURI() + "}" + qname.getLocalPart();
	}

	public static String toString(Element element) {
		if (element.getNamespaceURI() == null) {
			return "{}" + element.getLocalName();
		}
		return "{" + element.getNamespaceURI() + "}" + element.getLocalName();
	}

	public static String toString(Attr attr) {
		if (attr.getNamespaceURI() == null) {
			return "{}" + attr.getLocalName();
		}
		return "{" + attr.getNamespaceURI() + "}" + attr.getLocalName();
	}

	public static String toString(Collection collection) {
		StringBuffer buf = new StringBuffer();
		Iterator iter = collection.iterator();
		while (iter.hasNext()) {
			QName qname = (QName) iter.next();
			buf.append(toString(qname));
			if (iter.hasNext()) {
				buf.append(", ");
			}
		}
		return buf.toString();
	}

	public static QName parse(String name) {
		int pos = name.indexOf(125);
		if ((name.startsWith("{")) && (pos > 0)) {
			String ns = name.substring(1, pos);
			String lname = name.substring(pos + 1, name.length());
			return new QName(ns, lname);
		}
		return null;
	}
}