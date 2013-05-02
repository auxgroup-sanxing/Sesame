package com.sanxing.sesame.jaxp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class SourceTransformer {
	public static final String DEFAULT_CHARSET_PROPERTY = "com.sanxing.sesame.default.charset";
	private static final Class DOM_2_SAX_CLASS;
	private static String defaultCharset;
	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;

	public SourceTransformer() {
	}

	public SourceTransformer(DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;
	}

	public static String getDefaultCharset() {
		return SourceTransformer.defaultCharset;
	}

	public static void setDefaultCharset(String defaultCharset) {
		SourceTransformer.defaultCharset = defaultCharset;
	}

	public void toResult(Source source, Result result)
			throws TransformerException {
		toResult(source, result, defaultCharset);
	}

	public void toResult(Source source, Result result, String charset)
			throws TransformerConfigurationException, TransformerException {
		if (source == null) {
			return;
		}
		if (charset == null) {
			charset = defaultCharset;
		}
		Transformer transformer = createTransfomer();
		if (transformer == null) {
			throw new TransformerException(
					"Could not create a transformer - JAXP is misconfigured!");
		}
		transformer.setOutputProperty("encoding", charset);
		transformer.transform(source, result);
	}

	public String toString(Source source) throws TransformerException {
		if (source == null)
			return null;
		if (source instanceof StringSource)
			return ((StringSource) source).getText();
		if (source instanceof BytesSource) {
			return new String(((BytesSource) source).getData());
		}
		StringWriter buffer = new StringWriter();
		toResult(source, new StreamResult(buffer));
		return buffer.toString();
	}

	public String toString(Node node) throws TransformerException {
		return toString(new DOMSource(node));
	}

	public String contentToString(NormalizedMessage message)
			throws MessagingException, TransformerException,
			ParserConfigurationException, IOException, SAXException {
		return toString(message.getContent());
	}

	public DOMSource toDOMSource(Source source)
			throws ParserConfigurationException, IOException, SAXException,
			TransformerException {
		if (source instanceof DOMSource)
			return ((DOMSource) source);
		if (source instanceof SAXSource)
			return toDOMSourceFromSAX((SAXSource) source);
		if (source instanceof StreamSource) {
			return toDOMSourceFromStream((StreamSource) source);
		}
		return null;
	}

	public Source toDOMSource(NormalizedMessage message)
			throws MessagingException, TransformerException,
			ParserConfigurationException, IOException, SAXException {
		Node node = toDOMNode(message);
		return new DOMSource(node);
	}

	public SAXSource toSAXSource(Source source) throws IOException,
			SAXException, TransformerException {
		if (source instanceof SAXSource)
			return ((SAXSource) source);
		if (source instanceof DOMSource)
			return toSAXSourceFromDOM((DOMSource) source);
		if (source instanceof StreamSource) {
			return toSAXSourceFromStream((StreamSource) source);
		}
		return null;
	}

	public StreamSource toStreamSource(Source source)
			throws TransformerException {
		if (source instanceof StreamSource)
			return ((StreamSource) source);
		if (source instanceof DOMSource)
			return toStreamSourceFromDOM((DOMSource) source);
		if (source instanceof SAXSource) {
			return toStreamSourceFromSAX((SAXSource) source);
		}
		return null;
	}

	public StreamSource toStreamSourceFromSAX(SAXSource source)
			throws TransformerException {
		InputSource inputSource = source.getInputSource();
		if (inputSource != null) {
			if (inputSource.getCharacterStream() != null) {
				return new StreamSource(inputSource.getCharacterStream());
			}
			if (inputSource.getByteStream() != null) {
				return new StreamSource(inputSource.getByteStream());
			}
		}
		String result = toString(source);
		return new StringSource(result);
	}

	public StreamSource toStreamSourceFromDOM(DOMSource source)
			throws TransformerException {
		String result = toString(source);
		return new StringSource(result);
	}

	public SAXSource toSAXSourceFromStream(StreamSource source) {
		InputSource inputSource;
		if (source.getReader() != null)
			inputSource = new InputSource(source.getReader());
		else {
			inputSource = new InputSource(source.getInputStream());
		}
		inputSource.setSystemId(source.getSystemId());
		inputSource.setPublicId(source.getPublicId());
		return new SAXSource(inputSource);
	}

	public Reader toReaderFromSource(Source src) throws TransformerException {
		StreamSource stSrc = toStreamSource(src);
		Reader r = stSrc.getReader();
		if (r == null) {
			r = new InputStreamReader(stSrc.getInputStream());
		}
		return r;
	}

	public DOMSource toDOMSourceFromStream(StreamSource source)
			throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilder builder = createDocumentBuilder();
		String systemId = source.getSystemId();
		Document document = null;
		Reader reader = source.getReader();
		if (reader != null) {
			document = builder.parse(new InputSource(reader));
		} else {
			InputStream inputStream = source.getInputStream();
			if (inputStream != null) {
				InputSource inputsource = new InputSource(inputStream);
				inputsource.setSystemId(systemId);
				document = builder.parse(inputsource);
			} else {
				throw new IOException("No input stream or reader available");
			}
		}
		return new DOMSource(document, systemId);
	}

	public SAXSource toSAXSourceFromDOM(DOMSource source)
			throws TransformerException {
		if (DOM_2_SAX_CLASS != null) {
			try {
				Constructor cns = DOM_2_SAX_CLASS
						.getConstructor(new Class[] { Node.class });
				XMLReader converter = (XMLReader) cns
						.newInstance(new Object[] { source.getNode() });
				return new SAXSource(converter, new InputSource());
			} catch (Exception e) {
				throw new TransformerException(e);
			}
		}
		String str = toString(source);
		StringReader reader = new StringReader(str);
		return new SAXSource(new InputSource(reader));
	}

	public DOMSource toDOMSourceFromSAX(SAXSource source) throws IOException,
			SAXException, ParserConfigurationException, TransformerException {
		return new DOMSource(toDOMNodeFromSAX(source));
	}

	public Node toDOMNodeFromSAX(SAXSource source)
			throws ParserConfigurationException, IOException, SAXException,
			TransformerException {
		DOMResult result = new DOMResult();
		toResult(source, result);
		return result.getNode();
	}

	public Node toDOMNode(Source source) throws TransformerException,
			ParserConfigurationException, IOException, SAXException {
		DOMSource domSrc = toDOMSource(source);
		return ((domSrc != null) ? domSrc.getNode() : null);
	}

	public Node toDOMNode(NormalizedMessage message) throws MessagingException,
			TransformerException, ParserConfigurationException, IOException,
			SAXException {
		Source content = message.getContent();
		return toDOMNode(content);
	}

	public Element toDOMElement(NormalizedMessage message)
			throws MessagingException, TransformerException,
			ParserConfigurationException, IOException, SAXException {
		Node node = toDOMNode(message);
		return toDOMElement(node);
	}

	public Element toDOMElement(Source source) throws TransformerException,
			ParserConfigurationException, IOException, SAXException {
		Node node = toDOMNode(source);
		return toDOMElement(node);
	}

	public Element toDOMElement(Node node) throws TransformerException {
		if (node instanceof Document) {
			return ((Document) node).getDocumentElement();
		}
		if (node instanceof Element) {
			return ((Element) node);
		}

		throw new TransformerException(
				"Unable to convert DOM node to an Element");
	}

	public Document toDOMDocument(NormalizedMessage message)
			throws MessagingException, TransformerException,
			ParserConfigurationException, IOException, SAXException {
		Node node = toDOMNode(message);
		return toDOMDocument(node);
	}

	public Document toDOMDocument(Source source) throws TransformerException,
			ParserConfigurationException, IOException, SAXException {
		Node node = toDOMNode(source);
		return toDOMDocument(node);
	}

	public Document toDOMDocument(Node node)
			throws ParserConfigurationException, TransformerException {
		if (node instanceof Document) {
			return ((Document) node);
		}
		if (node instanceof Element) {
			Element elem = (Element) node;

			if (elem.getOwnerDocument().getDocumentElement() == elem) {
				return elem.getOwnerDocument();
			}

			Document doc = createDocument();
			doc.appendChild(doc.importNode(node, true));
			return doc;
		}

		throw new TransformerException(
				"Unable to convert DOM node to a Document");
	}

	public DocumentBuilderFactory getDocumentBuilderFactory() {
		if (this.documentBuilderFactory == null) {
			this.documentBuilderFactory = createDocumentBuilderFactory();
		}
		return this.documentBuilderFactory;
	}

	public void setDocumentBuilderFactory(
			DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;
	}

	public DocumentBuilderFactory createDocumentBuilderFactory() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		return factory;
	}

	public DocumentBuilder createDocumentBuilder()
			throws ParserConfigurationException {
		DocumentBuilderFactory factory = getDocumentBuilderFactory();
		return factory.newDocumentBuilder();
	}

	public Document createDocument() throws ParserConfigurationException {
		DocumentBuilder builder = createDocumentBuilder();
		return builder.newDocument();
	}

	public TransformerFactory getTransformerFactory() {
		if (this.transformerFactory == null) {
			this.transformerFactory = createTransformerFactory();
		}
		return this.transformerFactory;
	}

	public void setTransformerFactory(TransformerFactory transformerFactory) {
		this.transformerFactory = transformerFactory;
	}

	public Transformer createTransfomer()
			throws TransformerConfigurationException {
		TransformerFactory factory = getTransformerFactory();
		return factory.newTransformer();
	}

	public TransformerFactory createTransformerFactory() {
		return TransformerFactory.newInstance();
	}

	static {
		Class cl = null;
		try {
			cl = Class.forName("org.apache.xalan.xsltc.trax.DOM2SAX");
		} catch (Throwable t) {
		}
		DOM_2_SAX_CLASS = cl;

		defaultCharset = System.getProperty(
				"com.sanxing.sesame.default.charset", "UTF-8");
	}
}