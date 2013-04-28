package org.sanxing.sesame.jaxp;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class W3CDOMStreamReader extends DOMStreamReader {
	private Node content;
	private Document document;
	private W3CNamespaceContext context;

	public W3CDOMStreamReader(Element element) {
		super(new DOMStreamReader.ElementFrame(element, null));

		this.document = element.getOwnerDocument();
	}

	public Document getDocument() {
		return this.document;
	}

	protected void newFrame(DOMStreamReader.ElementFrame frame) {
		Element element = getCurrentElement();
		frame.uris = new ArrayList();
		frame.prefixes = new ArrayList();
		frame.attributes = new ArrayList();

		if (this.context == null) {
			this.context = new W3CNamespaceContext();
		}

		this.context.setElement(element);

		NamedNodeMap nodes = element.getAttributes();

		String ePrefix = element.getPrefix();
		if (ePrefix == null) {
			ePrefix = "";
		}

		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			String prefix = node.getPrefix();
			String localName = node.getLocalName();
			String value = node.getNodeValue();
			String name = node.getNodeName();

			if (prefix == null) {
				prefix = "";
			}

			if ((name != null) && ("xmlns".equals(name))) {
				frame.uris.add(value);
				frame.prefixes.add("");
			} else if ((prefix.length() > 0) && ("xmlns".equals(prefix))) {
				frame.uris.add(value);
				frame.prefixes.add(localName);
			} else if (name.startsWith("xmlns:")) {
				prefix = name.substring(6);
				frame.uris.add(value);
				frame.prefixes.add(prefix);
			} else {
				frame.attributes.add(node);
			}
		}
	}

	protected void endElement() {
		super.endElement();
	}

	Element getCurrentElement() {
		return ((Element) getCurrentFrame().element);
	}

	protected DOMStreamReader.ElementFrame getChildFrame(int currentChild) {
		return new DOMStreamReader.ElementFrame(getCurrentElement()
				.getChildNodes().item(currentChild), getCurrentFrame());
	}

	protected int getChildCount() {
		return getCurrentElement().getChildNodes().getLength();
	}

	protected int moveToChild(int currentChild) {
		this.content = getCurrentElement().getChildNodes().item(currentChild);
		if (this.content instanceof Text)
			return 4;
		if (this.content instanceof Element)
			return 1;
		if (this.content instanceof CDATASection)
			return 12;
		if (this.content instanceof Comment)
			return 4;
		if (this.content instanceof EntityReference) {
			return 9;
		}
		throw new IllegalStateException();
	}

	public String getElementText() throws XMLStreamException {
		this.frame.ended = true;
		this.currentEvent = 2;
		endElement();
		String result = getContent(getCurrentElement());

		return ((result != null) ? result : "");
	}

	public String getNamespaceURI(String prefix) {
		DOMStreamReader.ElementFrame frame = getCurrentFrame();

		while (frame != null) {
			int index = frame.prefixes.indexOf(prefix);
			if (index != -1) {
				return ((String) frame.uris.get(index));
			}

			frame = frame.parent;
		}

		return null;
	}

	public String getAttributeValue(String ns, String local) {
		Attr attr;
		if ((ns == null) || (ns.equals("")))
			attr = getCurrentElement().getAttributeNode(local);
		else {
			attr = getCurrentElement().getAttributeNodeNS(ns, local);
		}
		if (attr != null) {
			return attr.getValue();
		}
		return null;
	}

	public int getAttributeCount() {
		return getCurrentFrame().attributes.size();
	}

	Attr getAttribute(int i) {
		return ((Attr) getCurrentFrame().attributes.get(i));
	}

	private String getLocalName(Attr attr) {
		String name = attr.getLocalName();
		if (name == null) {
			name = attr.getNodeName();
		}
		return name;
	}

	public QName getAttributeName(int i) {
		Attr at = getAttribute(i);

		String prefix = at.getPrefix();
		String ln = getLocalName(at);

		String ns = at.getNamespaceURI();

		if (prefix == null) {
			return new QName(ns, ln);
		}
		return new QName(ns, ln, prefix);
	}

	public String getAttributeNamespace(int i) {
		return getAttribute(i).getNamespaceURI();
	}

	public String getAttributeLocalName(int i) {
		Attr attr = getAttribute(i);
		return getLocalName(attr);
	}

	public String getAttributePrefix(int i) {
		return getAttribute(i).getPrefix();
	}

	public String getAttributeType(int i) {
		return "CDATA";
	}

	public String getAttributeValue(int i) {
		return getAttribute(i).getValue();
	}

	public boolean isAttributeSpecified(int i) {
		return (getAttribute(i).getValue() != null);
	}

	public int getNamespaceCount() {
		return getCurrentFrame().prefixes.size();
	}

	public String getNamespacePrefix(int i) {
		return ((String) getCurrentFrame().prefixes.get(i));
	}

	public String getNamespaceURI(int i) {
		return ((String) getCurrentFrame().uris.get(i));
	}

	public NamespaceContext getNamespaceContext() {
		return this.context;
	}

	public String getText() {
		Node node = getCurrentElement().getChildNodes().item(
				getCurrentFrame().currentChild);
		return node.getNodeValue();
	}

	public char[] getTextCharacters() {
		return getText().toCharArray();
	}

	public int getTextStart() {
		return 0;
	}

	public int getTextLength() {
		return getText().length();
	}

	public String getEncoding() {
		return null;
	}

	public QName getName() {
		Element el = getCurrentElement();

		String prefix = getPrefix();
		String ln = getLocalName();

		if (prefix == null) {
			return new QName(el.getNamespaceURI(), ln);
		}
		return new QName(el.getNamespaceURI(), ln, prefix);
	}

	public String getLocalName() {
		String name = getCurrentElement().getLocalName();

		if (name == null) {
			name = getCurrentElement().getNodeName();
		}
		return name;
	}

	public String getNamespaceURI() {
		return getCurrentElement().getNamespaceURI();
	}

	public String getPrefix() {
		String prefix = getCurrentElement().getPrefix();
		if (prefix == null) {
			prefix = "";
		}
		return prefix;
	}

	public String getPITarget() {
		throw new UnsupportedOperationException();
	}

	public String getPIData() {
		throw new UnsupportedOperationException();
	}

	public static String getContent(Node n) {
		if (n == null) {
			return null;
		}
		Node n1 = getChild(n, 3);
		if (n1 == null) {
			return null;
		}
		String s1 = n1.getNodeValue();
		return s1.trim();
	}

	public static Node getChild(Node parent, int type) {
		Node n = parent.getFirstChild();
		while ((n != null) && (type != n.getNodeType())) {
			n = n.getNextSibling();
		}
		if (n == null) {
			return null;
		}
		return n;
	}
}