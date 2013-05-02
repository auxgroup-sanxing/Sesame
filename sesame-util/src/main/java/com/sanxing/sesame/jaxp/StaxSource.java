package com.sanxing.sesame.jaxp;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

public class StaxSource extends SAXSource implements XMLReader {
	private XMLStreamReader streamReader;
	private ContentHandler contentHandler;
	private LexicalHandler lexicalHandler;

	public StaxSource(XMLStreamReader streamReader) {
		this.streamReader = streamReader;
		setInputSource(new InputSource());
	}

	public XMLReader getXMLReader() {
		return this;
	}

	public XMLStreamReader getXMLStreamReader() {
		return this.streamReader;
	}

	protected void parse() throws SAXException {
		try {
			switch (this.streamReader.getEventType()) {
			case 10:
				break;
			case 12:
				if (this.lexicalHandler != null) {
					this.lexicalHandler.startCDATA();
				}
				int length = this.streamReader.getTextLength();
				int start = this.streamReader.getTextStart();
				char[] chars = this.streamReader.getTextCharacters();
				this.contentHandler.characters(chars, start, length);
				if (this.lexicalHandler != null) {
					this.lexicalHandler.endCDATA();
				}
				break;
			case 4:
				length = this.streamReader.getTextLength();
				start = this.streamReader.getTextStart();
				chars = this.streamReader.getTextCharacters();
				this.contentHandler.characters(chars, start, length);
				break;
			case 6:
				length = this.streamReader.getTextLength();
				start = this.streamReader.getTextStart();
				chars = this.streamReader.getTextCharacters();
				this.contentHandler.ignorableWhitespace(chars, start, length);
				break;
			case 5:
				if (this.lexicalHandler != null) {
					length = this.streamReader.getTextLength();
					start = this.streamReader.getTextStart();
					chars = this.streamReader.getTextCharacters();
					this.lexicalHandler.comment(chars, start, length);
				}
				break;
			case 11:
				break;
			case 8:
				this.contentHandler.endDocument();
				return;
			case 2:
				String uri = this.streamReader.getNamespaceURI();
				String localName = this.streamReader.getLocalName();
				String prefix = this.streamReader.getPrefix();
				String qname = ((prefix != null) && (prefix.length() > 0)) ? prefix
						+ ":" + localName
						: localName;
				this.contentHandler.endElement(uri, localName, qname);

				break;
			case 9:
			case 13:
			case 14:
			case 15:
				break;
			case 3:
				break;
			case 7:
				this.contentHandler.startDocument();
				break;
			case 1:
				uri = this.streamReader.getNamespaceURI();
				localName = this.streamReader.getLocalName();
				prefix = this.streamReader.getPrefix();
				qname = ((prefix != null) && (prefix.length() > 0)) ? prefix
						+ ":" + localName : localName;
				this.contentHandler.startElement((uri == null) ? "" : uri,
						localName, qname, getAttributes());
			}

			this.streamReader.next();
		} catch (XMLStreamException e) {
			SAXParseException spe;
			if (e.getLocation() != null)
				spe = new SAXParseException(e.getMessage(), null, null, e
						.getLocation().getLineNumber(), e.getLocation()
						.getColumnNumber(), e);
			else {
				spe = new SAXParseException(e.getMessage(), null, null, -1, -1,
						e);
			}
			spe.initCause(e);
			throw spe;
		}
	}

	protected String getQualifiedName() {
		String prefix = this.streamReader.getPrefix();
		if ((prefix != null) && (prefix.length() > 0)) {
			return prefix + ":" + this.streamReader.getLocalName();
		}
		return this.streamReader.getLocalName();
	}

	protected Attributes getAttributes() {
		AttributesImpl attrs = new AttributesImpl();

		for (int i = 0; i < this.streamReader.getNamespaceCount(); ++i) {
			String prefix = this.streamReader.getNamespacePrefix(i);
			String uri = this.streamReader.getNamespaceURI(i);
			if (uri == null) {
				uri = "";
			}

			if ((prefix == null) || (prefix.length() == 0))
				attrs.addAttribute("", null, "xmlns", "CDATA", uri);
			else {
				attrs.addAttribute("http://www.w3.org/2000/xmlns/", prefix,
						"xmlns:" + prefix, "CDATA", uri);
			}
		}
		for (int i = 0; i < this.streamReader.getAttributeCount(); ++i) {
			String uri = this.streamReader.getAttributeNamespace(i);
			String localName = this.streamReader.getAttributeLocalName(i);
			String prefix = this.streamReader.getAttributePrefix(i);
			String qName;
			if ((prefix != null) && (prefix.length() > 0))
				qName = prefix + ':' + localName;
			else {
				qName = localName;
			}
			String type = this.streamReader.getAttributeType(i);
			String value = this.streamReader.getAttributeValue(i);
			if (value == null) {
				value = "";
			}

			attrs.addAttribute((uri == null) ? "" : uri, localName, qName,
					type, value);
		}
		return attrs;
	}

	public boolean getFeature(String name) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		return false;
	}

	public void setFeature(String name, boolean value)
			throws SAXNotRecognizedException, SAXNotSupportedException {
	}

	public Object getProperty(String name) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		return null;
	}

	public void setProperty(String name, Object value)
			throws SAXNotRecognizedException, SAXNotSupportedException {
		if ("http://xml.org/sax/properties/lexical-handler".equals(name))
			this.lexicalHandler = ((LexicalHandler) value);
		else
			throw new SAXNotRecognizedException(name);
	}

	public void setEntityResolver(EntityResolver resolver) {
	}

	public EntityResolver getEntityResolver() {
		return null;
	}

	public void setDTDHandler(DTDHandler handler) {
	}

	public DTDHandler getDTDHandler() {
		return null;
	}

	public void setContentHandler(ContentHandler handler) {
		this.contentHandler = handler;
	}

	public ContentHandler getContentHandler() {
		return this.contentHandler;
	}

	public void setErrorHandler(ErrorHandler handler) {
	}

	public ErrorHandler getErrorHandler() {
		return null;
	}

	public void parse(InputSource input) throws SAXException {
		parse();
	}

	public void parse(String systemId) throws SAXException {
		parse();
	}
}