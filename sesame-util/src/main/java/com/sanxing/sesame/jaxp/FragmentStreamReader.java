package com.sanxing.sesame.jaxp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

public class FragmentStreamReader extends StreamReaderDelegate implements
		XMLStreamReader {
	private static final int STATE_START_DOC = 0;
	private static final int STATE_FIRST_ELEM = 1;
	private static final int STATE_FIRST_RUN = 2;
	private static final int STATE_RUN = 3;
	private static final int STATE_END_DOC = 4;
	private int depth;
	private int state = 0;
	private int event = 7;
	private List rootPrefixes;

	public FragmentStreamReader(XMLStreamReader parent) {
		super(parent);
		this.rootPrefixes = new ArrayList();
		NamespaceContext ctx = getParent().getNamespaceContext();
		if (ctx instanceof ExtendedNamespaceContext) {
			Iterator it = ((ExtendedNamespaceContext) ctx).getPrefixes();
			while (it.hasNext()) {
				String prefix = (String) it.next();
				this.rootPrefixes.add(prefix);
			}
		}
	}

	public int getEventType() {
		return this.event;
	}

	public boolean hasNext() throws XMLStreamException {
		return (this.event != 8);
	}

	public int next() throws XMLStreamException {
		switch (this.state) {
		case 0:
			this.state = 1;
			this.event = 7;
			break;
		case 1:
			this.state = 2;
			this.depth += 1;
			this.event = 1;
			break;
		case 2:
		case 3:
			this.state = 3;
			this.event = getParent().next();
			if (this.event == 1) {
				this.depth += 1;
				break;
			}
			if (this.event != 2)
				break;
			this.depth -= 1;
			if (this.depth != 0)
				break;
			this.state = 4;

			break;
		case 4:
			this.event = 8;
			break;
		default:
			throw new IllegalStateException();
		}
		return this.event;
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

	public int getNamespaceCount() {
		if (this.state == 2) {
			return this.rootPrefixes.size();
		}
		return getParent().getNamespaceCount();
	}

	public String getNamespacePrefix(int i) {
		if (this.state == 2) {
			return ((String) this.rootPrefixes.get(i));
		}
		return getParent().getNamespacePrefix(i);
	}

	public String getNamespaceURI(int i) {
		if (this.state == 2) {
			return getParent().getNamespaceContext().getNamespaceURI(
					(String) this.rootPrefixes.get(i));
		}
		return getParent().getNamespaceURI(i);
	}

	public String getNamespaceURI(String prefix) {
		if (this.state == 2) {
			return getParent().getNamespaceContext().getNamespaceURI(prefix);
		}
		return getParent().getNamespaceURI(prefix);
	}

	public boolean isStartElement() {
		return (this.event == 1);
	}
}