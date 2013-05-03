package com.sanxing.sesame.logging;

import com.sanxing.sesame.jaxp.XSLTUtil;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

public class XObjectRecord extends LogRecord {
	private static final long serialVersionUID = -8558705391039405674L;
	private String encoding = System.getProperty("file.encoding");

	private boolean callout = false;

	public XObjectRecord(long serial, Source source) {
		setSerial(serial);

		if (source == null) {
			setDocument(null);
		} else if (source instanceof JDOMSource) {
			setDocument(((JDOMSource) source).getDocument());
		} else {
			JDOMResult result = new JDOMResult();
			try {
				Transformer transformer = XSLTUtil.getTransformerfactory()
						.newTransformer();

				transformer.transform(source, result);
				setDocument(result.getDocument());
			} catch (TransformerConfigurationException e) {
				setContent(e);
			} catch (TransformerException e) {
				setContent(e);
			}
		}
	}

	public void setDocument(Document document) {
		setContent(document);
	}

	public Document getDocument() {
		if (getContent() instanceof Document) {
			return ((Document) getContent());
		}

		return null;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public boolean isCallout() {
		return this.callout;
	}

	public void setCallout(boolean callout) {
		this.callout = callout;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{");
		buf.append(" serial: " + getSerial());
		if (getServiceName() != null) {
			buf.append(", serviceUnit: '" + getServiceName() + "'");
		}
		if (getAction() != null) {
			buf.append(", action: '" + getAction() + "'");
		}
		buf.append(", content: ");
		Document document = getDocument();
		if (document == null) {
			buf.append(getContent());
		} else {
			XMLOutputter outputter = new XMLOutputter();
			buf.append(outputter.outputString(document.getRootElement()));
		}
		buf.append(" }");
		return buf.toString();
	}
}