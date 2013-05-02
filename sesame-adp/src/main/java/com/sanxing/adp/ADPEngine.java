package com.sanxing.adp;

import com.sanxing.adp.runtime.ADPServer;
import com.sanxing.adp.runtime.JAXBHelper;
import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.component.EngineComponent;
import com.sanxing.sesame.component.params.AppParameters;
import com.sanxing.sesame.component.params.Parameter;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.service.ServiceUnit;
import com.sanxing.sesame.util.JdomUtil;
import com.sanxing.sesame.util.cache.ThreadLocalCache;
import java.io.File;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPath;

public class ADPEngine extends EngineComponent implements ServiceUnitManager {
	private ADPServer server = new ADPServer();
	private XPath statusXPath;
	private XPath promptXPath;
	private ThreadLocal<Long> currentSerial = new ThreadLocal();

	private static Logger LOG = LoggerFactory.getLogger(ADPEngine.class);

	protected void init() throws JBIException {
		super.init();
		try {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Initialize ADP engine...");
			}
			String sharedLib = getContext().getInstallRoot() + File.separator
					+ "adp_base" + File.separator + "lib";
			this.server = new ADPServer((JarFileClassLoader) getClassLoader());
			this.server.addJarDirCP(new File(sharedLib));

			if (LOG.isTraceEnabled()) {
				LOG.trace("ADP share library path is [" + sharedLib + "]");
			}

			SesameClient.getInstance().setEngine(this);
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw taskFailure("init", (e.getMessage() != null) ? e.getMessage()
					: e.toString());
		}
	}

	public void init(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		try {
			this.statusXPath = XPath.newInstance("//faultcode/text()");
			this.promptXPath = XPath.newInstance("//faultstring/text()");

			if (LOG.isTraceEnabled())
				LOG.trace("deploy service unit [" + serviceUnitName + "]");
			ServiceUnit unit = getServiceUnit(serviceUnitName);
			File unitClassPath = new File(serviceUnitRootPath + File.separator
					+ "adp-classes");
			this.server.resetClassLoader();
			this.server.addClassPath(unitClassPath.toURL());
			if (LOG.isTraceEnabled())
				LOG.trace("service unit classpath [" + unitClassPath + "]");
			this.server.register(unit.getDefinition());

			JAXBHelper.reset();
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw taskFailure("init", (e.getMessage() != null) ? e.getMessage()
					: e.toString());
		}
	}

	Source send(Source input, QName serviceName, QName interfaceName,
			QName operation) {
		try {
			MessageExchange exchange = getExchangeFactory()
					.createInOutExchange();
			Long serial = (Long) this.currentSerial.get();
			exchange.setProperty("sesame.exchange.platform.serial", serial);
			exchange.setService(serviceName);
			exchange.setInterfaceName(interfaceName);
			exchange.setOperation(operation);
			NormalizedMessage msg = exchange.createMessage();
			msg.setContent(input);
			exchange.setMessage(msg, "in");
			sendSync(exchange);
			if (exchange.getError() != null) {
				throw new RuntimeException(exchange.getError());
			}
			if (exchange.getFault() != null) {
				Transformer transformer = TransformerFactory.newInstance()
						.newTransformer();
				transformer.setOutputProperty("indent", "yes");
				StringWriter buffer = new StringWriter();
				transformer.transform(exchange.getFault().getContent(),
						new StreamResult(buffer));
				throw new RuntimeException(buffer.toString());
			}
			return exchange.getMessage("out").getContent();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void start(String serviceUnitName) throws DeploymentException {
		try {
			ServiceUnit unit = getServiceUnit(serviceUnitName);
			LOG.info("started adp unit " + serviceUnitName);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw taskFailure("start-" + serviceUnitName,
					(e.getMessage() != null) ? e.getMessage() : e.toString());
		}
		super.start(serviceUnitName);
	}

	public void shutDown(String serviceUnitName) throws DeploymentException {
		try {
			this.server.resetClassLoader();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw taskFailure("start-" + serviceUnitName,
					(e.getMessage() != null) ? e.getMessage() : e.toString());
		}
	}

	public void onMessageExchange(MessageExchange exchange)
			throws MessagingException {
		String exchangeId = exchange.getExchangeId();
		if (LOG.isTraceEnabled()) {
			LOG.trace("incoming exchange id is [" + exchangeId + "]");
			LOG.trace("operation (method to be invoded) is ["
					+ exchange.getOperation().getLocalPart() + "]");
		}
		this.currentSerial.set((Long) exchange
				.getProperty("sesame.exchange.platform.serial"));
		String suName = exchange.getEndpoint().getServiceName().toString();
		String operationName = exchange.getOperation().getLocalPart();
		List<String> paramNames = new LinkedList();
		paramNames.addAll(AppParameters.getInstance().getAppParamKeys());
		paramNames.addAll(AppParameters.getInstance().getSuParamKeys(suName));
		paramNames.addAll(AppParameters.getInstance().getOperationParamKeys(
				suName, operationName));
		for (String paramName : paramNames) {
			ThreadLocalCache.put(paramName, AppParameters.getInstance()
					.getParamter(suName, operationName, paramName)
					.getTypedValue());
		}
		if (exchange instanceof InOut) {
			try {
				InOut inOut = (InOut) exchange;
				Document msg = JdomUtil.source2JDOMDocument(inOut
						.getInMessage().getContent());
				if (LOG.isTraceEnabled()) {
					XMLOutputter outputter = new XMLOutputter();
					LOG.trace("Incoming msg is [ "
							+ outputter.outputString(msg) + "]");
				}

				Long platformSerial = (Long) exchange
						.getProperty("sesame.exchange.platform.serial");
				String action = (String) exchange
						.getProperty("sesame.exchange.tx.action");
				Object clientType = exchange
						.getProperty("sesame.exchange.client.type");
				Object clientSerial = exchange
						.getProperty("sesame.exchange.client.serial");
				Object clientID = exchange
						.getProperty("sesame.exchange.client.id");
				if (platformSerial != null) {
					MDC.put("PLATFORM_SERIAL", "" + platformSerial);
				}
				if (action != null) {
					MDC.put("ACTION", action);
				}
				if (clientType != null) {
					MDC.put("CLIENT_TYPE", "" + clientType);
				}
				if (clientID != null) {
					MDC.put("CLIENT_ID", "" + clientID);
				}
				if (clientSerial != null) {
					MDC.put("CLIENT_SERIAL", "" + clientSerial);
				}

				Document result = this.server.accept(exchange
						.getInterfaceName(), exchange.getOperation()
						.getLocalPart(), msg);

				response(exchange, result);
			} catch (Exception e) {
				LOG.error("invoke adp server err", e);
				exchange.setStatus(ExchangeStatus.ERROR);
				fail(exchange, e);
			}
		} else if (exchange instanceof InOptionalOut) {
			try {
				InOptionalOut inOut = (InOptionalOut) exchange;
				Document msg = JdomUtil.source2JDOMDocument(inOut
						.getInMessage().getContent());
				if (LOG.isTraceEnabled()) {
					XMLOutputter outputter = new XMLOutputter();
					LOG.trace("Incoming msg is [ "
							+ outputter.outputString(msg) + "]");
				}
				Document result = this.server.accept(exchange
						.getInterfaceName(), exchange.getOperation()
						.getLocalPart(), msg);
				response(exchange, result);
			} catch (Exception e) {
				exchange.setStatus(ExchangeStatus.ERROR);
				fail(exchange, e);
			}
		} else {
			if (!(exchange instanceof InOnly))
				return;
			Document result;
			try {
				InOnly inOnly = (InOnly) exchange;
				Document source = JdomUtil.source2JDOMDocument(inOnly
						.getInMessage().getContent());
				result = this.server.accept(exchange.getInterfaceName(),
						exchange.getOperation().getLocalPart(), source);
			} catch (Exception e) {
				LOG.error("invoke adp server err", e);
				fail(exchange, e);
			}
		}
	}

	private void response(MessageExchange exchange, Document result)
			throws MessagingException {
		if (LOG.isTraceEnabled()) {
			XMLOutputter outputter = new XMLOutputter();
			LOG.trace("result from adp server [ "
					+ outputter.outputString(result) + "]");
		}
		if (result.getRootElement().getName().equals("fault")) {
			result.getRootElement().setNamespace(
					Namespace.getNamespace(exchange.getEndpoint()
							.getServiceName().getNamespaceURI()));
			Fault fault = exchange.createFault();
			fault.setProperty("response.status.xpath", this.statusXPath);
			fault.setProperty("response.statustext.xpath", this.promptXPath);
			fault.setContent(JdomUtil.JDOMElement2DOMSource(result
					.getRootElement()));
			exchange.setStatus(ExchangeStatus.ERROR);
			fail(exchange, fault);
		} else {
			NormalizedMessage resposneMsg = exchange.createMessage();
			resposneMsg.setContent(new JDOMSource(result));
			answer(exchange, resposneMsg);
		}
	}

	protected ServiceUnitManager createServiceUnitManager() {
		return this;
	}

	public String deploy(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		return ManagementSupport.createSuccessMessage("deploy-"
				+ serviceUnitName, "部署成功");
	}

	public String undeploy(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		return ManagementSupport.createSuccessMessage("undeploy"
				+ serviceUnitName, "卸载成功");
	}
}