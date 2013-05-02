package com.sanxing.sesame.transport.impl;

import com.sanxing.sesame.binding.Carrier;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;
import com.sanxing.sesame.transport.util.SesameMailClient;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MailConnector extends BaseTransport implements Connector {
	private static Logger LOG = LoggerFactory.getLogger(MailConnector.class);
	protected boolean active = false;
	public URI uri;
	public Map<?, ?> properties = new HashMap();
	protected Map<String, Map<String, Object>> unitPropsCache = new HashMap();
	public String readLine;
	private SesameMailClient mailClient;

	public URI getURI() {
		return this.uri;
	}

	public void setURI(URI uri) {
		this.uri = uri;
	}

	public String getProperties(String key) {
		Object value = this.properties.get(key);
		return ((value == null) ? "" : (String) value);
	}

	public void setProperties(Map<?, ?> properties) {
		this.properties = properties;
		this.readLine = getProperties("readLine");
	}

	protected void write(OutputStream output, byte[] bytes, int length)
			throws IOException {
	}

	public String getCharacterEncoding() {
		return "utf-8";
	}

	public String getDescription() {
		return "ftp get connector";
	}

	public void close() throws IOException {
		this.mailClient = null;
		this.active = false;
	}

	public void open() throws IOException {
		try {
			this.active = true;
			this.mailClient = new SesameMailClient();
			this.mailClient.setProperties(this.properties);
			this.mailClient.initSMTPServer();
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw new IOException(e.getMessage());
		}
	}

	public void sendOut(MessageContext context) throws IOException {
		String result = "failure";
		try {
			String contextPath = context.getPath();
			String filename = context.getAction();
			LOG.debug("................subject is:" + filename);

			BinaryResult binaryResult = (BinaryResult) context.getResult();
			byte[] getBuf = binaryResult.getBytes();
			if (getBuf != null) {
				this.mailClient.sendMail(getBuf, filename);
				result = "success";
			}

			BinarySource source = new BinarySource();
			source.setBytes(result.getBytes(), result.getBytes().length);
			source.setEncoding(getCharacterEncoding());
			context.setSource(source);

			getCarrier(context).post(context);
		} catch (IOException ioe) {
			LOG.debug(ioe.getMessage(), ioe);
			throw ioe;
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw new IOException(e.getMessage());
		}
	}

	public boolean isActive() {
		return this.active;
	}

	public void setConfig(String contextPath, Element config)
			throws IllegalArgumentException {
		Map properties = new HashMap();

		if (contextPath == null) {
			if (config != null) {
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				try {
					String expression = "*/*";
					NodeList nodes = (NodeList) xpath.evaluate(expression,
							config, XPathConstants.NODESET);
					int i = 0;
					for (int len = nodes.getLength(); i < len; ++i) {
						Element prop = (Element) nodes.item(i);
						properties.put(prop.getNodeName(), prop
								.getTextContent().trim());
					}
				} catch (XPathExpressionException e) {
					throw new IllegalArgumentException(e.getMessage(), e);
				}
			}
			LOG.debug(" .............SET root properties is:" + properties);
			setProperties(properties);
		} else {
			NodeList list = config.getChildNodes();
			for (int i = 0; i < list.getLength(); ++i) {
				Node child = list.item(i);
				if (child.getNodeType() == 1) {
					properties.put(child.getNodeName(), child.getTextContent()
							.trim());
				}
			}
			this.unitPropsCache.put(contextPath, properties);
			LOG.debug(".....put unit props is:" + properties + ",path is:"
					+ contextPath);
		}
	}
}