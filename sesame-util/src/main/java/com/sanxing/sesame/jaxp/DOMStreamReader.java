package com.sanxing.sesame.jaxp;

import com.sanxing.sesame.util.FastStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class DOMStreamReader implements XMLStreamReader {
	protected Map properties = new HashMap();

	protected int currentEvent = 7;

	protected FastStack<ElementFrame> frames = new FastStack();
	protected ElementFrame frame;

	public DOMStreamReader(ElementFrame frame) {
		init(frame);
	}

	private void init(ElementFrame f) {
		this.frame = f;
		this.frames.push(this.frame);
		newFrame(f);
	}

	protected ElementFrame getCurrentFrame() {
		return this.frame;
	}

	public Object getProperty(String key) throws IllegalArgumentException {
		return this.properties.get(key);
	}

	public int next() throws XMLStreamException {
		if (this.frame.ended) {
			this.frames.pop();
			if (!(this.frames.empty())) {
				this.frame = ((ElementFrame) this.frames.peek());
			} else {
				this.currentEvent = 8;
				return this.currentEvent;
			}
		}

		if (!(this.frame.started)) {
			this.frame.started = true;
			this.currentEvent = 1;
		} else if (this.frame.currentAttribute < getAttributeCount() - 1) {
			this.frame.currentAttribute += 1;
			this.currentEvent = 10;
		} else if (this.frame.currentChild < getChildCount() - 1) {
			this.frame.currentChild += 1;

			this.currentEvent = moveToChild(this.frame.currentChild);

			if (this.currentEvent == 1) {
				ElementFrame newFrame = getChildFrame(this.frame.currentChild);
				newFrame.started = true;
				this.frame = newFrame;
				this.frames.push(this.frame);
				this.currentEvent = 1;

				newFrame(newFrame);
			}
		} else {
			this.frame.ended = true;
			this.currentEvent = 2;
			endElement();
		}
		return this.currentEvent;
	}

	protected void newFrame(ElementFrame newFrame) {
	}

	protected void endElement() {
	}

	protected abstract int moveToChild(int paramInt);

	protected abstract ElementFrame getChildFrame(int paramInt);

	protected abstract int getChildCount();

	public void require(int arg0, String arg1, String arg2)
			throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	public abstract String getElementText() throws XMLStreamException;

	public int nextTag() throws XMLStreamException {
		while (hasNext()) {
			if (1 == next()) {
				return 1;
			}
		}

		return this.currentEvent;
	}

	public boolean hasNext() throws XMLStreamException {
		return ((this.frames.size() != 0) || (!(this.frame.ended)));
	}

	public void close() throws XMLStreamException {
	}

	public abstract String getNamespaceURI(String paramString);

	public boolean isStartElement() {
		return (this.currentEvent == 1);
	}

	public boolean isEndElement() {
		return (this.currentEvent == 2);
	}

	public boolean isCharacters() {
		return (this.currentEvent == 4);
	}

	public boolean isWhiteSpace() {
		return (this.currentEvent == 6);
	}

	public int getEventType() {
		return this.currentEvent;
	}

	public int getTextCharacters(int sourceStart, char[] target,
			int targetStart, int length) throws XMLStreamException {
		char[] src = getText().toCharArray();

		if (sourceStart + length >= src.length) {
			length = src.length - sourceStart;
		}

		for (int i = 0; i < length; ++i) {
			target[(targetStart + i)] = src[(i + sourceStart)];
		}

		return length;
	}

	public boolean hasText() {
		return ((this.currentEvent == 4) || (this.currentEvent == 11)
				|| (this.currentEvent == 9) || (this.currentEvent == 5) || (this.currentEvent == 6));
	}

	public Location getLocation() {
		return new Location() {
			public int getCharacterOffset() {
				return 0;
			}

			public int getColumnNumber() {
				return 0;
			}

			public int getLineNumber() {
				return 0;
			}

			public String getPublicId() {
				return null;
			}

			public String getSystemId() {
				return null;
			}
		};
	}

	public boolean hasName() {
		return ((this.currentEvent == 1) || (this.currentEvent == 2));
	}

	public String getVersion() {
		return null;
	}

	public boolean isStandalone() {
		return false;
	}

	public boolean standaloneSet() {
		return false;
	}

	public String getCharacterEncodingScheme() {
		return null;
	}

	public static class ElementFrame {
		Object element;
		boolean started;
		boolean ended;
		int currentChild = -1;
		int currentAttribute = -1;
		int currentElement = -1;
		List<String> uris;
		List<String> prefixes;
		List attributes;
		final ElementFrame parent;

		public ElementFrame(Object element, ElementFrame parent) {
			this.element = element;
			this.parent = parent;
		}
	}
}