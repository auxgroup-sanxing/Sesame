package org.sanxing.sesame.jaxp;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public final class XMLStreamHelper implements XMLStreamConstants {
	public static void copy(XMLStreamReader reader, XMLStreamWriter writer)
			throws XMLStreamException {
		int read = 0;
		int event = reader.getEventType();

		while (reader.hasNext()) {
			switch (event) {
			case 1:
				++read;
				writeStartElement(reader, writer);
				break;
			case 2:
				writer.writeEndElement();
				--read;
				if (read <= 0)
					return;
			case 4:
				writer.writeCharacters(reader.getText());
				break;
			case 7:
			case 8:
			case 10:
			case 13:
			case 3:
			case 5:
			case 6:
			case 9:
			case 11:
			case 12:
			}
			event = reader.next();
		}
	}

	private static void writeStartElement(XMLStreamReader reader,
			XMLStreamWriter writer) throws XMLStreamException {
		String local = reader.getLocalName();
		String uri = reader.getNamespaceURI();
		String prefix = reader.getPrefix();
		if (prefix == null) {
			prefix = "";
		}
		if (uri == null) {
			uri = "";
		}

		String boundPrefix = writer.getPrefix(uri);
		boolean writeElementNS = false;
		if ((boundPrefix == null) || (!(prefix.equals(boundPrefix)))) {
			writeElementNS = true;
		}

		if (prefix.length() == 0) {
			writer.writeStartElement(local);
			writer.setDefaultNamespace(uri);
		} else {
			writer.writeStartElement(prefix, local, uri);
			writer.setPrefix(prefix, uri);
		}

		for (int i = 0; i < reader.getNamespaceCount(); ++i) {
			String nsURI = reader.getNamespaceURI(i);
			String nsPrefix = reader.getNamespacePrefix(i);
			if (nsPrefix == null) {
				nsPrefix = "";
			}

			if (nsPrefix.length() == 0)
				writer.writeDefaultNamespace(nsURI);
			else {
				writer.writeNamespace(nsPrefix, nsURI);
			}

			if ((nsURI.equals(uri)) && (nsPrefix.equals(prefix))) {
				writeElementNS = false;
			}

		}

		if (writeElementNS) {
			if ((prefix == null) || (prefix.length() == 0))
				writer.writeDefaultNamespace(uri);
			else {
				writer.writeNamespace(prefix, uri);
			}

		}

		for (int i = 0; i < reader.getAttributeCount(); ++i) {
			String ns = reader.getAttributeNamespace(i);
			String nsPrefix = reader.getAttributePrefix(i);
			if ((ns == null) || (ns.length() == 0))
				writer.writeAttribute(reader.getAttributeLocalName(i),
						reader.getAttributeValue(i));
			else if ((nsPrefix == null) || (nsPrefix.length() == 0))
				writer.writeAttribute(reader.getAttributeNamespace(i),
						reader.getAttributeLocalName(i),
						reader.getAttributeValue(i));
			else
				writer.writeAttribute(reader.getAttributePrefix(i),
						reader.getAttributeNamespace(i),
						reader.getAttributeLocalName(i),
						reader.getAttributeValue(i));
		}
	}

	public static void writeStartElement(XMLStreamWriter writer, String uri,
			String local, String prefix) throws XMLStreamException {
		if (prefix == null) {
			prefix = "";
		}
		if (uri == null) {
			uri = "";
		}

		String boundPrefix = writer.getPrefix(uri);
		boolean writeElementNS = false;
		if ((boundPrefix == null) || (!(prefix.equals(boundPrefix)))) {
			writeElementNS = true;
		}

		if (prefix.length() == 0) {
			writer.writeStartElement(local);
			writer.setDefaultNamespace(uri);
		} else {
			writer.writeStartElement(prefix, local, uri);
			writer.setPrefix(prefix, uri);
		}

		if (writeElementNS)
			if (prefix.length() == 0)
				writer.writeDefaultNamespace(uri);
			else
				writer.writeNamespace(prefix, uri);
	}

	public static void writeStartElement(XMLStreamWriter writer, QName name)
			throws XMLStreamException {
		String prefix = choosePrefix(writer, name, false);
		writeStartElement(writer, name.getNamespaceURI(), name.getLocalPart(),
				prefix);
	}

	public static void writeTextQName(XMLStreamWriter out, QName name)
			throws XMLStreamException {
		String prefix = choosePrefix(out, name, true);
		if ("".equals(prefix))
			out.writeCharacters(name.getLocalPart());
		else
			out.writeCharacters(prefix + ":" + name.getLocalPart());
	}

	protected static String choosePrefix(XMLStreamWriter out, QName name,
			boolean declare) throws XMLStreamException {
		String uri = name.getNamespaceURI();

		if ((uri == null) || ("".equals(uri))) {
			if (!("".equals(out.getNamespaceContext().getNamespaceURI("")))) {
				out.setPrefix("", "");
			}
			return "";
		}

		String defPrefix = name.getPrefix();

		if ((defPrefix != null) && (!("".equals(defPrefix)))) {
			if (!(uri.equals(out.getNamespaceContext().getNamespaceURI(
					defPrefix)))) {
				if (out.getNamespaceContext().getPrefix(uri) != null) {
					defPrefix = out.getNamespaceContext().getPrefix(uri);
				} else if (out.getPrefix(uri) != null) {
					defPrefix = out.getPrefix(uri);
				} else if (declare) {
					out.setPrefix(defPrefix, uri);
					out.writeNamespace(defPrefix, uri);
				}

			}

		} else if (out.getNamespaceContext().getPrefix(uri) != null) {
			defPrefix = out.getNamespaceContext().getPrefix(uri);
		} else if (out.getPrefix(uri) != null) {
			defPrefix = out.getPrefix(uri);
		} else {
			defPrefix = getUniquePrefix(out);
			if (declare) {
				out.setPrefix(defPrefix, uri);
				out.writeNamespace(defPrefix, uri);
			}
		}

		return defPrefix;
	}

	protected static String getUniquePrefix(XMLStreamWriter writer) {
		int n = 1;
		while (true) {
			String nsPrefix = "ns" + n;
			if (writer.getNamespaceContext().getNamespaceURI(nsPrefix) == null) {
				return nsPrefix;
			}
			++n;
		}
	}
}