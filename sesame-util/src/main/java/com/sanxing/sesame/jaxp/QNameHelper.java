package com.sanxing.sesame.jaxp;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

public final class QNameHelper {
	public static String getQualifiedName(QName qname) {
		String prefix = qname.getPrefix();
		String localPart = qname.getLocalPart();
		if ((prefix != null) && (prefix.length() > 0)) {
			return prefix + ":" + localPart;
		}
		return localPart;
	}

	public static QName asQName(NamespaceContext context, String text) {
		int idx = text.indexOf(58);
		if (idx >= 0) {
			String prefix = text.substring(0, idx);
			String localPart = text.substring(idx + 1);
			String uri = context.getNamespaceURI(prefix);
			return new QName(uri, localPart, prefix);
		}
		return new QName(text);
	}
}