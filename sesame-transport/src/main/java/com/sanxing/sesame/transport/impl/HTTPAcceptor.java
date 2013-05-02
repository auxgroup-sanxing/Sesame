package com.sanxing.sesame.transport.impl;

import com.sanxing.sesame.binding.annotation.Description;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.transport.util.HttpThreadPool;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Description("超文本传输协议")
public class HTTPAcceptor extends BaseTransport implements Acceptor {

	private static final Logger LOG = LoggerFactory.getLogger(HTTPAcceptor.class);
	private Server webServer;
	private boolean active;
	private URI uri;
	private Map<?, ?> properties = new HashMap();
	private ByteOrder byteOrder;
	private String headEncoding;
	private boolean lengthIncludeHead;
	private int recvOffset;
	private int sendOffset;
	private int recvHeadLen;
	private int sendHeadLen;
	private int timeout = 30;

	public void reply(MessageContext context) throws IOException {
		context.getSerial();
		context.getPath();
	}

	public void close() throws IOException {
		if ((this.active) && (this.webServer != null)) {
			try {
				this.webServer.stop();
				this.webServer.destroy();
			} catch (Exception e) {
				LOG.debug(e.getMessage(), e);
			}
			this.active = false;
		}
		LOG.info("Transport closed: " + getURI());
	}

	public String getCharacterEncoding() {
		String encoding = getProperty("encoding");
		return ((encoding != null) ? encoding : System
				.getProperty("file.encoding"));
	}

	public String getProperty(String key) {
		return ((String) this.properties.get(key));
	}

	public int getProperty(String key, int def) {
		try {
			String value = getProperty(key);
			return ((value == null) ? def : Integer.parseInt(value));
		} catch (NumberFormatException e) {
		}
		return def;
	}

	public boolean isActive() {
		return this.active;
	}

	public void open() throws IOException {
		HttpThreadPool pool = new HttpThreadPool();

		this.webServer = new Server();
		this.webServer.setThreadPool(pool);

		ServletContextHandler appContext = new ServletContextHandler(1);
		appContext.setContextPath("/");
		appContext.setHandler(new InnerHandler());

		this.webServer.setHandler(appContext);

		int portNumber = (this.uri.getPort() == -1) ? 80 : this.uri.getPort();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setHost(this.uri.getHost());
		connector.setPort(portNumber);
		connector.setMaxIdleTime(30000);
		this.webServer.setConnectors(new Connector[] { connector });
		this.webServer.setSendServerVersion(true);

		LOG.debug("Connectors: " + this.webServer.getConnectors().length);
		try {
			this.webServer.start();
			this.active = this.webServer.isStarted();
			LOG.info(this + " listening......");
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw new IOException(e.getMessage());
		}
	}

	public void setURI(URI uri) {
		this.uri = uri;
	}

	public URI getURI() {
		return this.uri;
	}

	public void setConfig(String contextPath, Element config)
			throws IllegalArgumentException {
		Map properties = new HashMap();
		if ((config != null) && (contextPath == null)) {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			try {
				String expression = "*/*";
				NodeList nodes = (NodeList) xpath.evaluate(expression, config,
						XPathConstants.NODESET);
				int i = 0;
				for (int len = nodes.getLength(); i < len; ++i) {
					Element prop = (Element) nodes.item(i);
					properties.put(prop.getNodeName(), prop.getTextContent()
							.trim());
				}

			} catch (XPathExpressionException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		}
	}

	protected void setProperties(Map<?, ?> properties)
			throws IllegalArgumentException {
		this.properties = properties;
		this.timeout = getProperty("timeout", 10);
		String endian = getProperty("endian");
		this.byteOrder = (((endian == null) || (endian.equals("big"))) ? ByteOrder.BIG_ENDIAN
				: ByteOrder.LITTLE_ENDIAN);
	}

	private void received(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		BinarySource input = new BinarySource();

		MessageContext ctx = new MessageContext(this, input);
		ctx.setPath(request.getContextPath());
		try {
			input.setInputStream(request.getInputStream());
			String encoding = request.getCharacterEncoding();
			input.setEncoding((encoding != null) ? encoding
					: getCharacterEncoding());
		} catch (Throwable t) {
			LOG.debug(t.getMessage(), t);
			response.sendError(500, t.getMessage());
		}
	}

	public String toString() {
		return "{ name:'" + super.getClass().getSimpleName() + "', url: '"
				+ getURI() + "' }";
	}

	private class InnerHandler extends AbstractHandler {
		public void handle(String target, Request baseRequest,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			HTTPAcceptor.LOG.debug("Request: " + request.getMethod() + " "
					+ request.getRequestURI());
			HTTPAcceptor.this.received(request, response);
		}
	}

	private class InnerServlet extends HttpServlet {
		private static final long serialVersionUID = 6294753513239090289L;

		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			HTTPAcceptor.LOG.debug("doGet " + request.getRequestURI());
			HTTPAcceptor.this.received(request, response);
		}

		protected void doPost(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			HTTPAcceptor.LOG.debug("doPost " + request.getRequestURI());
			HTTPAcceptor.this.received(request, response);
		}
	}
}