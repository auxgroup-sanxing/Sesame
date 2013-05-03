package com.sanxing.sesame.binding;

import com.sanxing.sesame.address.AddressBook;
import com.sanxing.sesame.address.Location;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.Encoder;
import com.sanxing.sesame.binding.codec.FaultHandler;
import com.sanxing.sesame.binding.codec.XMLResult;
import com.sanxing.sesame.binding.codec.XMLSource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.context.MessageContext.Status;
import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.binding.transport.TransportFactory;
import com.sanxing.sesame.logging.BufferRecord;
import com.sanxing.sesame.logging.ErrorRecord;
import com.sanxing.sesame.logging.FinishRecord;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import com.sanxing.sesame.logging.LogRecord;
import com.sanxing.sesame.logging.XObjectRecord;
import com.sanxing.sesame.service.OperationContext;
import com.sanxing.sesame.service.ServiceUnit;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.wsdl.BindingOperation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.apache.ws.commons.schema.XmlSchema;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.DocumentResult;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.w3c.dom.Element;

public class DefaultBinding implements Binding {
	protected static final String BINDING_SERVICE_NAME = "sesame.binding.service.name";
	protected static final String BINDING_ENDPOINT_NAME = "sesame.binding.endpoint.name";
	private static final Logger LOG = LoggerFactory.getLogger(DefaultBinding.class);

	private static final TransformerFactory transformerFactory = TransformerFactory
			.newInstance();

	private Map<String, OperationContext> actionMap = new ConcurrentHashMap();
	private Codec codec;
	protected Integer txCode_start = null;

	protected Integer txCode_end = null;

	private XPath txCode_xpath = null;

	private XPath status_xpath = null;

	private XPath statusText_xpath = null;

	private String success_code = null;
	ServiceUnit serviceUnit;
	private QName serviceName;
	private Port port;
	private URI uri;
	private Transport transport;

	public URI bind() throws BindingException {
		try {
			Iterator iter = this.port.getExtensibilityElements().iterator();
			if (!(iter.hasNext()))
				throw new BindingException("Port address not specified");
			ExtensibilityElement extEl = (ExtensibilityElement) iter.next();
			SOAPAddress addr = (SOAPAddress) extEl;
			String location = addr.getLocationURI();
			this.uri = new URI(location);

			QName extensionAttr = (QName) this.port
					.getExtensionAttribute(new QName(
							"http://www.sanxing.com/ns/sesame", "style"));
			String style = (extensionAttr != null) ? extensionAttr
					.getLocalPart() : null;
			LOG.debug("Port style: " + style);
			Location loc = AddressBook
					.find((this.uri.getScheme() != null) ? this.uri.getHost()
							: location);
			if (loc != null) {
				URI real = loc.getURI();
				this.uri = new URI(real.getScheme(), real.getAuthority(),
						real.getPath()
								+ ((this.uri.getScheme() != null) ? this.uri
										.getPath() : ""), this.uri.getQuery(),
						this.uri.getFragment());
				style = loc.getStyle();
			} else if (this.uri.getScheme() == null) {
				throw new BindingException("Address not found, invalid url: "
						+ this.uri);
			}

			String path = (this.uri.getPath().length() > 0) ? this.uri
					.getPath() : "/";
			Element config = (loc != null) ? loc.getConfig() : null;

			this.transport = newTransport(this.uri.getScheme(),
					this.uri.getAuthority(), config, style);
			this.transport.setConfig(path, getBindingConfig(this.port));

			LOG.debug("portName: " + this.port.getName() + ", binding: " + this
					+ ", transport: " + this.transport);

			javax.wsdl.Binding binding = this.port.getBinding();
			List<BindingOperation> operations = binding.getBindingOperations();
			for (BindingOperation operation : operations) {
				OperationContext context = this.serviceUnit
						.getOperationContext(operation.getName());
				if ((context != null) && (context.getReference() != null)) {
					this.actionMap.put(operation.getName(), context);
				} else {
					Iterator extIterator = operation.getExtensibilityElements()
							.iterator();
					while (extIterator.hasNext()) {
						Object ext = extIterator.next();
						if (ext instanceof SOAPOperation) {
							SOAPOperation op = (SOAPOperation) ext;
							String action = op.getSoapActionURI();
							this.actionMap.put(action, context);
							break;
						}
					}
				}
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Action map of port '" + this.port.getName()
						+ "' ...");
				for (Map.Entry entry : this.actionMap.entrySet()) {
					LOG.debug(((String) entry.getKey())
							+ " -> OperationContext"
							+ ((OperationContext) entry.getValue()).hashCode());
				}
				LOG.debug("--------------------------------------------------------------------------------");
			}

			return this.uri;
		} catch (URISyntaxException e) {
			throw new BindingException(e.getMessage(), e);
		}
	}

	private Element getBindingConfig(Port port) {
		QName elementType = new QName("http://www.sanxing.com/ns/sesame",
				"binding");
		javax.wsdl.Binding binding = port.getBinding();
		List<ExtensibilityElement> list = binding.getExtensibilityElements();
		for (ExtensibilityElement element : list) {
			if (element.getElementType().equals(elementType)) {
				UnknownExtensibilityElement unkonwn = (UnknownExtensibilityElement) element;
				Element resultEl = unkonwn.getElement();
				return resultEl;
			}
		}
		return null;
	}

	public void unbind() throws BindingException {
		try {
			if (this.transport != null) {
				this.transport.close();

				javax.wsdl.Binding binding = this.port.getBinding();
				List<BindingOperation> operations = binding.getBindingOperations();
				for (BindingOperation operation : operations) {
					Iterator extIterator = operation.getExtensibilityElements()
							.iterator();
					while (extIterator.hasNext()) {
						Object ext = extIterator.next();
						if (ext instanceof SOAPOperation) {
							SOAPOperation op = (SOAPOperation) ext;
							String action = op.getSoapActionURI();
							this.actionMap.remove(action);
							this.actionMap.remove(operation.getName());
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			throw new BindingException(e.getMessage(), e);
		}
	}

	protected boolean parseRequest(MessageContext context,
			Map<String, Object> params) throws Exception {
		if (context.getSource() instanceof BinarySource) {
			BinarySource binSource = (BinarySource) context.getSource();

			if ((context.getAction() == null) && (this.txCode_start != null)
					&& (this.txCode_end != null)) {
				int len = this.txCode_end.intValue()
						- this.txCode_start.intValue();
				byte[] buf = new byte[len];
				System.arraycopy(binSource.getBytes(),
						this.txCode_start.intValue(), buf, 0, len);
				context.setAction(new String(buf));
			}

			if (context.getAction() != null) {
				MDC.put("ACTION", context.getAction());
			}

			XmlSchema schema = null;
			QName elementName = new QName("undefined");

			String action = context.getAction();
			OperationContext operaContext = getOperationContext(action);
			if (operaContext != null) {
				context.setProperty("sesame.exchange.tx.proxy", operaContext
						.getServiceUnit().getName());
				context.setProperty("sesame.exchange.tx.action", action);
				MDC.put("SU", operaContext.getServiceUnit().getName());
				schema = operaContext.getSchema();
				elementName = operaContext.getInputElement();
			} else if (action != null) {
				return false;
			}

			binSource.setXMLSchema(schema);
			binSource.setElementName(elementName.getLocalPart());

			XMLResult result = new XMLResult();
			this.codec.getDecoder().decode(binSource, result);

			XMLSource xmlSource = new XMLSource(result.getContent());
			for (String name : binSource.getPropertyNames()) {
				xmlSource.setProperty(name, binSource.getProperty(name));
			}
			context.setSource(xmlSource);
		}

		if ((context.getAction() == null) && (this.txCode_xpath != null)) {
			org.jdom.Document request;
			if (context.getSource() instanceof XMLSource) {
				XMLSource content = (XMLSource) context.getSource();
				request = content.getJDOMDocument();
			} else {
				if (context.getSource() instanceof JDOMSource) {
					JDOMSource content = (JDOMSource) context.getSource();
					request = content.getDocument();
				} else {
					JDOMResult result = new JDOMResult();
					Transformer transformer = transformerFactory
							.newTransformer();
					transformer.transform(context.getSource(), result);
					request = result.getDocument();
				}
			}
			String action = this.txCode_xpath.valueOf(request);
			context.setAction(action);
			LOG.debug("Action = " + action);
		}
		String action = context.getAction();
		if (action == null) {
			return false;
		}

		context.setProperty("sesame.binding.endpoint.name", this.port.getName());
		context.setProperty("sesame.binding.service.name", this.serviceName);

		return true;
	}

	protected boolean parseResponse(MessageContext context,
			Map<String, Object> params) throws Exception {
		if (context.getResult() instanceof BinaryResult) {
			BinaryResult binResult = (BinaryResult) context.getResult();

			XmlSchema schema = null;
			QName elementName = new QName("undefined");

			String operationName = (String) context
					.getProperty("sesame.binding.operation.name");
			OperationContext operaContext = this.serviceUnit
					.getOperationContext(operationName);
			if (operaContext != null) {
				schema = operaContext.getSchema();
				elementName = operaContext.getOutputElement();
			}
			BinarySource binSource = new BinarySource();
			binSource.setBytes(binResult.getBytes());
			binSource.setXMLSchema(schema);
			binSource.setElementName(elementName.getLocalPart());
			binSource.setEncoding(binResult.getEncoding());

			XMLResult xmlResult = new XMLResult();
			this.codec.getDecoder().decode(binSource, xmlResult);
			for (String name : binResult.getPropertyNames()) {
				xmlResult.setProperty(name, binResult.getProperty(name));
			}
			context.setResult(xmlResult);
		}

		if (this.status_xpath != null) {
			org.jdom.Document response;
			if (context.getResult() instanceof XMLResult) {
				XMLResult content = (XMLResult) context.getResult();
				response = content.getJDOMDocument();
			} else {
				if (context.getResult() instanceof JDOMResult) {
					JDOMResult content = (JDOMResult) context.getResult();
					response = content.getDocument();
				} else {
					if (context.getResult() instanceof DOMResult) {
						DOMResult content = (DOMResult) context.getResult();
						response = new DOMBuilder()
								.build((org.w3c.dom.Document) content.getNode());
					} else {
						if (context.getResult() instanceof DocumentResult) {
							DocumentResult content = (DocumentResult) context
									.getResult();
							DOMWriter writer = new DOMWriter();
							response = new DOMBuilder().build(writer
									.write(content.getDocument()));
						} else {
							response = new org.jdom.Document();
						}
					}
				}
			}
			if ((this.success_code != null) && (this.success_code.length() > 0)) {
				String status = this.status_xpath.valueOf(response);
				if ((status != null) && (status.length() > 0)
						&& (!(status.equals(this.success_code)))) {
					context.setStatus(MessageContext.Status.FAULT);
					if (context.getResult() instanceof XMLResult) {
						XMLResult xmlResult = (XMLResult) context.getResult();
						xmlResult.setProperty("response.status.xpath",
								this.status_xpath);
						xmlResult.setProperty("response.statustext.xpath",
								this.statusText_xpath);
					}
				}
			}
		}
		return true;
	}

	public URI getAddress() {
		return this.uri;
	}

	public ServiceUnit getServiceUnit() {
		return this.serviceUnit;
	}

	private Transport newTransport(String scheme, String authority,
			Element config, String style) throws URISyntaxException,
			BindingException {
		URI uri = new URI(scheme, authority, null, null, null);
		return TransportFactory.getTransport(uri, config, style);
	}

	public void setCodec(Codec codec) {
		this.codec = codec;
		setParameters(codec.getProperties());
	}

	public Codec getCodec() {
		return this.codec;
	}

	protected void setParameters(Map<String, String> params) {
		LOG.debug("codec-params: " + params);
		try {
			String start = (String) params.get("tx-start");
			if ((start != null) && (start.length() > 0))
				this.txCode_start = Integer.valueOf(Integer.parseInt(start));
			String end = (String) params.get("tx-end");
			if ((end != null) && (end.length() > 0))
				this.txCode_end = Integer.valueOf(Integer.parseInt(end));
			String path = (String) params.get("tx-code");
			if ((path != null) && (path.length() > 0)) {
				this.txCode_xpath = XPath.newInstance(path);
				this.txCode_xpath.addNamespace("xsd",
						"http://www.w3.org/2001/XMLSchema");
				this.txCode_xpath.addNamespace("xsi",
						"http://www.w3.org/2001/XMLSchema-instance");
				this.txCode_xpath.addNamespace("soapenv",
						"http://schemas.xmlsoap.org/soap/envelope/");
			}

			path = (String) params.get("status");
			if ((path != null) && (path.length() > 0)) {
				this.status_xpath = XPath.newInstance(path);
				this.status_xpath.addNamespace("xsd",
						"http://www.w3.org/2001/XMLSchema");
				this.status_xpath.addNamespace("xsi",
						"http://www.w3.org/2001/XMLSchema-instance");
				this.status_xpath.addNamespace("soapenv",
						"http://schemas.xmlsoap.org/soap/envelope/");
			}

			String statusTextPath = (String) params.get("status-text");
			if ((path != null) && (statusTextPath.length() > 0)) {
				this.statusText_xpath = XPath.newInstance(statusTextPath);
				this.statusText_xpath.addNamespace("xsd",
						"http://www.w3.org/2001/XMLSchema");
				this.statusText_xpath.addNamespace("xsi",
						"http://www.w3.org/2001/XMLSchema-instance");
				this.statusText_xpath.addNamespace("soapenv",
						"http://schemas.xmlsoap.org/soap/envelope/");
			}

			this.success_code = ((String) params.get("success-code"));
		} catch (JDOMException e) {
			LOG.debug(e.getMessage(), e);
		}
	}

	public void init(Codec codec, ServiceUnit serviceUnit, Service service,
			Port port) {
		setCodec(codec);
		this.serviceUnit = serviceUnit;
		this.port = port;
		setServiceName(service.getQName());
	}

	public void setServiceName(QName serviceName) {
		this.serviceName = serviceName;
	}

	public OperationContext getOperationContext(String action) {
		if (action == null)
			return null;
		return ((OperationContext) this.actionMap.get(action));
	}

	public Transport getTransport() {
		return this.transport;
	}

	public boolean assemble(Source content, MessageContext message)
			throws BindingException {
		Log log = LogFactory.getLog("sesame.binding." + message.getAction());

		String channel = null;
		String action = null;
		String serviceName = null;
		String operationName = null;
		if (log.isInfoEnabled()) {
			channel = (String) message.getProperty("sesame.exchange.consumer");
			action = message.getAction();
			QName service = (QName) message
					.getProperty("sesame.binding.service.name");
			if (service != null) {
				serviceName = service.toString();
			}
			OperationContext operation = getOperationContext(message
					.getAction());
			if (operation != null) {
				operationName = operation.getOperationName().getLocalPart();
			}
		}
		try {
			if (message.isAccepted()) {
				if (log.isInfoEnabled()) {
					LogRecord t = new XObjectRecord(message.getSerial()
							.longValue(), content);
					t.setStage("接入组件编码前");
					t.setChannel(channel);
					t.setAction(action);
					t.setServiceName(serviceName);
					t.setOperationName(operationName);
					log.info(
							"[REPLY][XML]----------------------------------------------------",
							t);
				}

				if (message.getResult() == null) {
					BinaryResult result = new BinaryResult();
					result.setEncoding(message.getTransport()
							.getCharacterEncoding());
					message.setResult(result);
				}

				assembleResponse(content, message);

				if ((log.isInfoEnabled())
						&& (message.getResult() instanceof BinaryResult)) {
					BinaryResult result = (BinaryResult) message.getResult();
					BufferRecord rec = new BufferRecord(message.getSerial()
							.longValue(), result.getBytes());

					rec.setStage("接入组件编码后");
					log.info(
							"[REPLY][BINARY]-------------------------------------------------",
							rec);

					FinishRecord finish = new FinishRecord(message.getSerial()
							.longValue());
					finish.setStage("交易结束");
					finish.setChannel(channel);
					finish.setAction(action);
					finish.setServiceName(serviceName);
					finish.setOperationName(operationName);
					log.info(null, finish);
				}
			} else {
				if (log.isInfoEnabled()) {
					XObjectRecord record = new XObjectRecord(message
							.getSerial().longValue(), content);
					record.setCallout(true);
					record.setStage("callout编码前");
					log.info(
							"[SEND][XML]------------------------------------------------------",
							record);
				}

				if (message.getSource() == null) {
					BinarySource source = new BinarySource();
					source.setEncoding(message.getTransport()
							.getCharacterEncoding());
					message.setSource(source);
				}

				assembleRequest(content, message);

				if ((log.isInfoEnabled())
						&& (message.getSource() instanceof BinarySource)) {
					BinarySource source = (BinarySource) message.getSource();
					BufferRecord rec = new BufferRecord(message.getSerial()
							.longValue(), source.getBytes());
					rec.setEncoding(source.getEncoding());

					rec.setCallout(true);
					rec.setStage("callout编码后");
					log.info(
							"[SEND][BINARY]--------------------------------------------------",
							rec);
				}
			}

			return true;
		} catch (BindingException e) {
			throw e;
		} catch (Exception e) {
			throw new BindingException(e.getMessage(), e);
		}
	}

	private void assembleRequest(Source content, MessageContext message)
			throws BindingException {
		if (message.getSource() instanceof BinarySource) {
			BinarySource binSource = (BinarySource) message.getSource();
			message.setPath((this.uri.getPath().length() > 0) ? this.uri
					.getPath() : "/");

			String operation = (String) message
					.getProperty("sesame.binding.operation.name");
			XmlSchema schema = null;
			QName elementName = new QName("undefined");
			OperationContext operaContext = this.serviceUnit
					.getOperationContext(operation);
			if (operaContext != null) {
				message.setAction((operaContext.getAction() != null) ? operaContext
						.getAction() : operation);
				schema = operaContext.getSchema();
				elementName = operaContext.getInputElement();
			}

			BinaryResult binResult = new BinaryResult();
			binResult.setXMLSchema(schema);
			binResult.setElementName(elementName.getLocalPart());
			binResult.setEncoding(binSource.getEncoding());
			XMLSource xmlSource = new XMLSource(content);
			this.codec.getEncoder().encode(xmlSource, binResult);

			binSource.setBytes(binResult.getBytes());
			for (String name : binResult.getPropertyNames())
				binSource.setProperty(name, binResult.getProperty(name));
		}
	}

	private void assembleResponse(Source content, MessageContext message)
			throws BindingException, TransformerException {
		if (message.getResult() instanceof BinaryResult) {
			BinaryResult result = (BinaryResult) message.getResult();

			XmlSchema schema = null;
			QName elementName = new QName("undefined");
			OperationContext operaContext = getOperationContext(message
					.getAction());
			if (operaContext != null) {
				schema = operaContext.getSchema();
				elementName = operaContext.getOutputElement();
			}

			result.setXMLSchema(schema);
			result.setElementName(elementName.getLocalPart());
			XMLSource source = new XMLSource(content);
			this.codec.getEncoder().encode(source, result);
		} else {
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(content, message.getResult());
		}
	}

	public boolean parse(MessageContext message, Map<String, Object> params)
			throws BindingException {
		Log log = LogFactory.getLog("sesame.binding." + message.getAction());
		try {
			if (message.isAccepted()) {
				message.setBinding(this);

				MDC.put("SERIAL", "" + message.getSerial());
				if ((log.isInfoEnabled())
						&& (message.getSource() instanceof BinarySource)
						&& (message.getProperty("buffer-logged") == null)) {
					BinarySource source = (BinarySource) message.getSource();
					BufferRecord record = new BufferRecord(message.getSerial()
							.longValue(), source.getBytes());
					record.setEncoding(source.getEncoding());

					record.setStage("接入组件解码前");
					record.setAction(message.getAction());
					log.info(
							"[REQ][BINARY]------------------------------------",
							record);
					message.setProperty("buffer-logged", Boolean.valueOf(true));
				}

				boolean result = parseRequest(message, params);

				if (message.getAction() != null) {
					MDC.put("ACTION", message.getAction());
				}

				log = LogFactory
						.getLog("sesame.binding." + message.getAction());

				Source source = (message.getSource() instanceof XMLSource) ? ((XMLSource) message
						.getSource()).getContent() : message.getSource();
				if ((result) && (log.isInfoEnabled())) {
					OperationContext operation = getOperationContext(message
							.getAction());
					XObjectRecord trace = new XObjectRecord(message.getSerial()
							.longValue(), source);
					trace.setStage("接入组件解码后");
					log.info(
							"[REQ][XML]----------------------------------------------",
							trace);
				}

				return result;
			}

			if ((log.isInfoEnabled())
					&& (message.getResult() instanceof BinaryResult)) {
				BinaryResult result = (BinaryResult) message.getResult();
				BufferRecord record = new BufferRecord(message.getSerial()
						.longValue(), result.getBytes());
				record.setCallout(true);

				record.setStage("callout解码前");
				log.info(
						"[RECV][BINARY]-------------------------------------------",
						record);
			}

			boolean result = parseResponse(message, params);
			Source source = (message.getResult() instanceof XMLResult) ? ((XMLResult) message
					.getResult()).getContent() : message.getSource();
			if ((result) && (log.isInfoEnabled())) {
				XObjectRecord trace = new XObjectRecord(message.getSerial()
						.longValue(), source);
				trace.setCallout(true);
				trace.setStage("callout解码后");
				log.info(
						"[RECV][XML]------------------------------------------------",
						trace);
			}
			return result;
		} catch (BindingException e) {
			throw e;
		} catch (Exception e) {
			throw new BindingException(e.getMessage(), e);
		}
	}

	public boolean handle(Source fault, MessageContext context)
			throws BindingException {
		if (context.isAccepted()) {
			return handleAccepted(fault, context);
		}

		return handleConneced(context);
	}

	private boolean handleAccepted(Source fault, MessageContext context)
			throws BindingException {
		Log log = LogFactory.getLog("sesame.binding." + context.getAction());
		if (log.isInfoEnabled()) {
			LogRecord rec = new XObjectRecord(context.getSerial().longValue(),
					fault);
			log.info(
					"[FAULT][XML]----------------------------------------------",
					rec);
		}
		try {
			OperationContext operaContext = getOperationContext(context
					.getAction());

			XmlSchema schema = (operaContext != null) ? operaContext
					.getSchema() : null;
			QName elementName = null;
			if (fault == null) {
				if ((operaContext != null) && (elementName == null)) {
					elementName = operaContext.getFaultElement(null);
				}
				if ((operaContext != null) && (elementName == null)) {
					elementName = operaContext.getOutputElement();
				}
				if (context.getResult() == null) {
					context.setResult(new BinaryResult());
				}
				if (context.getResult() instanceof BinaryResult) {
					BinaryResult result = (BinaryResult) context.getResult();
					result.setXMLSchema(schema);
					result.setElementName((elementName != null) ? elementName
							.getLocalPart() : null);
					result.setEncoding(getTransport().getCharacterEncoding());
				}
				if (this.codec.getFaultHandler() != null) {
					this.codec.getFaultHandler().handle(context.getException(),
							context);
				} else if (context.getException() != null) {
					LOG.error("Uncaught exception: ", context.getException());
				}
				if ((context.getResult() instanceof BinaryResult)
						&& (log.isInfoEnabled())) {
					BinaryResult result = (BinaryResult) context.getResult();
					BufferRecord rec = new BufferRecord(context.getSerial()
							.longValue(), result.getBytes());

					rec.setStage("接入组件编码后");
					log.info("[FAULT][BUFFER]", rec);

					ErrorRecord err = new ErrorRecord(context.getSerial()
							.longValue(), context.getException());
					err.setStage("交易结束");
					log.info(null, err);
				}

			} else if (context.getResult() instanceof BinaryResult) {
				BinaryResult result = (BinaryResult) context.getResult();
				result.setXMLSchema(schema);
				if ((operaContext != null) && (elementName == null)) {
					elementName = operaContext.getFaultElement((String) result
							.getProperty("fault-name"));
				}
				if ((operaContext != null) && (elementName == null)) {
					elementName = operaContext.getOutputElement();
				}
				result.setElementName((elementName != null) ? elementName
						.getLocalPart() : null);
				XMLSource source = new XMLSource(fault);
				if (this.codec.getFaultHandler() != null) {
					this.codec.getFaultHandler().encode(source, result);
				} else {
					this.codec.getEncoder().encode(source, result);
				}

				BufferRecord rec = new BufferRecord(context.getSerial()
						.longValue(), result.getBytes());

				rec.setStage("接入组件编码后");
				log.info(
						"[FAULT][BINARY]---------------------------------------",
						rec);

				ErrorRecord err = new ErrorRecord(context.getSerial()
						.longValue(), context.getException());
				err.setStage("交易结束");
				log.info(null, err);
			} else {
				Transformer transformer = transformerFactory.newTransformer();
				transformer.transform(fault, context.getResult());
			}
			return true;
		} catch (BindingException e) {
			throw e;
		} catch (Exception e) {
			throw new BindingException(e.getMessage(), e);
		}
	}

	private boolean handleConneced(MessageContext context)
			throws BindingException {
		Log log = LogFactory.getLog("sesame.binding");
		try {
			String operationName = (String) context
					.getProperty("sesame.binding.operation.name");
			OperationContext operaContext = this.serviceUnit
					.getOperationContext(operationName);

			XmlSchema schema = (operaContext != null) ? operaContext
					.getSchema() : null;
			QName elementName = null;
			if (context.getResult() instanceof BinaryResult) {
				BinaryResult binResult = (BinaryResult) context.getResult();
				if (log.isInfoEnabled()) {
					BufferRecord rec = new BufferRecord(context.getSerial()
							.longValue(), binResult.getBytes());

					rec.setStage("接入组件编码后");
					log.info(
							"[FAULT][BINARY]---------------------------------------",
							rec);
				}

				if ((operaContext != null) && (elementName == null)) {
					elementName = operaContext
							.getFaultElement((String) binResult
									.getProperty("fault-name"));
				}
				if ((operaContext != null) && (elementName == null)) {
					elementName = operaContext.getOutputElement();
				}
				BinarySource source = new BinarySource();
				source.setBytes(binResult.getBytes());
				source.setXMLSchema(schema);
				source.setElementName((elementName != null) ? elementName
						.getLocalPart() : null);

				BinarySource input = (BinarySource) context.getSource();
				String charset = input.getEncoding();
				if (charset == null) {
					charset = context.getTransport().getCharacterEncoding();
				}
				source.setEncoding(charset);

				XMLResult result = new XMLResult();
				result.setProperty("response.status.xpath", this.status_xpath);
				result.setProperty("response.statustext.xpath",
						this.statusText_xpath);
				if (this.codec.getFaultHandler() != null) {
					this.codec.getFaultHandler().decode(source, result);
				} else {
					this.codec.getDecoder().decode(source, result);
				}
				context.setResult(result);

				if (log.isInfoEnabled()) {
					LogRecord rec = new XObjectRecord(context.getSerial()
							.longValue(), result.getContent());
					log.info(
							"[FAULT][XML]----------------------------------------------",
							rec);
				}

				ErrorRecord err = new ErrorRecord(context.getSerial()
						.longValue(), context.getException());
				err.setStage("交易结束");
				log.info(null, err);
			}
			return true;
		} catch (BindingException e) {
			throw e;
		} catch (Exception e) {
			throw new BindingException(e.getMessage(), e);
		}
	}
}