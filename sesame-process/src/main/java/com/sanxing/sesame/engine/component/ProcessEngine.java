package com.sanxing.sesame.engine.component;

import com.sanxing.sesame.component.EngineComponent;
import com.sanxing.sesame.component.params.AppParameters;
import com.sanxing.sesame.component.params.Parameter;
import com.sanxing.sesame.component.params.Parameter.PARAMTYPE;
import com.sanxing.sesame.engine.Engine;
import com.sanxing.sesame.engine.FlowInfo;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.callout.CalloutException;
import com.sanxing.sesame.engine.action.var.VarNotFoundException;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.DehydrateManager;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.xpath.XPathUtil;
import com.sanxing.sesame.engine.xslt.TransformerManager;
import com.sanxing.sesame.exception.FaultException;
import com.sanxing.sesame.exception.NoFaultAvailableException;
import com.sanxing.sesame.exceptions.AppException;
import com.sanxing.sesame.exceptions.SystemException;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.ManagementSupport;
import com.sanxing.sesame.service.ServiceUnit;
import com.sanxing.sesame.util.JdomUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.jaxen.Function;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMSource;
import org.w3c.dom.DocumentFragment;

public class ProcessEngine extends EngineComponent implements
		ServiceUnitManager {
	private static final Logger LOG = LoggerFactory.getLogger(ProcessEngine.class);

	public static ProcessEngine jbiInstance = null;

	private Engine engine = Engine.getInstance();

	private FaultProcessor faultProcessor = new FaultProcessor();

	private Map<String, ProcessRegistry> registries = new ConcurrentHashMap();
	private SAXBuilder _builder;

	public void init(ComponentContext cc) throws JBIException {
		super.init(cc);
		try {
			this.faultProcessor.init(new File(cc.getInstallRoot()));
			registerCustomXPathFunction(cc);
			registerTransformExtension(cc);
			registerBeanshellStaticFunction(cc);
			jbiInstance = this;
		} catch (JBIException e) {
			throw e;
		} catch (Throwable e) {
			throw new JBIException(e.getMessage(), e);
		}
	}

	private void registerBeanshellStaticFunction(ComponentContext cc)
			throws JBIException {
		File extensionFile = new File(cc.getInstallRoot() + File.separator
				+ "beanshell.ext");
		if (extensionFile.exists()) {
			SAXBuilder builder = getSAXBuilder();
			try {
				Document doc = builder.build(extensionFile);
				List<Element> list = doc.getRootElement().getChildren(
						"func-package");
				for (Element funcElement : list) {
					String packageName = funcElement.getText();
					LOG.debug("register function package [" + packageName + "]");

					Engine.getInstance().regsiterFunction(packageName);
				}
			} catch (JDOMException e) {
				throw new JBIException("please check transform.ext", e);
			} catch (IOException e) {
				throw new JBIException(e.getMessage(), e);
			}
		}
	}

	private void registerTransformExtension(ComponentContext cc)
			throws JBIException {
		File extensionFile = new File(cc.getInstallRoot() + File.separator
				+ "transform.ext");
		if (extensionFile.exists()) {
			SAXBuilder builder = getSAXBuilder();
			try {
				Document doc = builder.build(extensionFile);
				List<Element> list = doc.getRootElement().getChildren("class");
				for (Element classEl : list) {
					String prefix = classEl.getAttributeValue("prefix");
					String className = classEl.getAttributeValue("class-name");
					getClassLoader().loadClass(className);
					LOG.debug("register class [" + prefix + ":" + className
							+ "]");
					TransformerManager.registerExtension(prefix, className);
				}
			} catch (JDOMException e) {
				throw new JBIException("please check transform.ext", e);
			} catch (IOException e) {
				throw new JBIException(e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				throw new JBIException(e.getMessage(), e);
			}
		}
	}

	private SAXBuilder getSAXBuilder() {
		if (this._builder == null) {
			this._builder = new SAXBuilder();
		}

		return this._builder;
	}

	private void registerCustomXPathFunction(ComponentContext cc) {
		File functionFile = new File(cc.getInstallRoot() + File.separator
				+ "xpath.ext");
		if (!(functionFile.exists()))
			return;
		try {
			SAXBuilder builder = getSAXBuilder();
			Document doc = builder.build(functionFile);
			List fuctionEles = doc.getRootElement().getChildren();
			for (int i = 0; i < fuctionEles.size(); ++i) {
				String name = null;
				String namespaceUri = null;
				try {
					Element eleFunc = (Element) fuctionEles.get(i);
					String prefix = eleFunc.getAttributeValue("prefix");
					name = eleFunc.getAttributeValue("name");
					namespaceUri = "http://www.sanxing.net.cn/sesame/"
							+ prefix;
					String className = eleFunc.getAttributeValue("class-name");
					String desc = eleFunc.getAttributeValue("description");
					LOG.debug("add function [" + prefix + ":" + namespaceUri
							+ "]" + name);
					Class functionClazz = getClassLoader().loadClass(className);
					Function function = (Function) functionClazz.newInstance();
					XPathUtil.registerFunction(namespaceUri, prefix, name,
							function);
				} catch (Exception e) {
					LOG.error("register function [" + namespaceUri + ":" + name
							+ "] err", e);
				}
			}
		} catch (JDOMException e) {
			throw new RuntimeException("please check function.xml", e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public void onMessageExchange(MessageExchange exchange)
			throws MessagingException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Exchange [" + exchange.getExchangeId()
					+ "] arriving at EC [" + getContext().getComponentName()
					+ "]");
			LOG.debug(getContext().getComponentName()
					+ " role: "
					+ ((exchange.getRole() == MessageExchange.Role.PROVIDER) ? "provider"
							: "consumer"));
		}

		Long platformSerial = (Long) exchange
				.getProperty("sesame.exchange.platform.serial");
		String action = (String) exchange
				.getProperty("sesame.exchange.tx.action");
		Object clientType = exchange
				.getProperty("sesame.exchange.client.type");
		Object clientSerial = exchange
				.getProperty("sesame.exchange.client.serial");
		Object clientID = exchange.getProperty("sesame.exchange.client.id");
		if (platformSerial != null) {
			MDC.put("PLATFORM_SERIAL", "" + platformSerial);
		}
		if (action != null) {
			MDC.put("ACTION", action);
		}
		if (clientType != null) {
			MDC.put("CLIENT_TYPE", clientType.toString());
		}
		if (clientID != null) {
			MDC.put("CLIENT_ID", "" + clientID);
		}
		if (clientSerial != null) {
			MDC.put("CLIENT_SERIAL", "" + clientSerial);
		}
		ServiceEndpoint endpoint = exchange.getEndpoint();
		String operationName = exchange.getOperation().getLocalPart();
		String flowName = endpoint.getEndpointName() + "__" + operationName;

		if (exchange.getRole() == MessageExchange.Role.CONSUMER) {
			LOG.debug(exchange.getService().toString());
			LOG.debug(exchange.getMessage("out").toString());

			if (exchange.getProperty(DehydrateManager.DEHYDRATE_CONTEXT_ID) != null)
				handleDehydrateResponse(exchange, endpoint);
		} else {
			exchange.setProperty("sesame.exchange.provider", getContext()
					.getComponentName());
			ExecutionContext executionCtx = new ExecutionContext(
					exchange.getExchangeId());
			try {
				Variable var = exchange2var(exchange);
				executionCtx.put("ENGINE", this);
				executionCtx.put("NAMING_CONTEXT", getContext()
						.getNamingContext());
				executionCtx.getDataContext().addVariable("request", var);
				executionCtx.put("process.classloader", getClassLoader());
				executionCtx.put("process.serial", platformSerial);
				executionCtx.put("process.ACTION", action);
				executionCtx.put("process.name", operationName);
				executionCtx.put("process.group", endpoint.getEndpointName());
				executionCtx.getDataContext().addVariable("serial",
						new Variable(platformSerial, 8));

				appendBizParameter(exchange, operationName, executionCtx);

				this.engine.execute(executionCtx, flowName);
				try {
					Variable responseVar = executionCtx.getDataContext()
							.getVariable("response");
					NormalizedMessage normalizedOut = exchange.createMessage();
					if (responseVar != null) {
						Element responseEl = (Element) responseVar.get();
						responseEl.detach();
						normalizedOut.setContent(new JDOMSource(new Document(
								responseEl)));
					}
					answer(exchange, normalizedOut);
				} catch (VarNotFoundException e) {
					Variable faultVar = executionCtx.getDataContext()
							.getVariable("fault");
					Element responseEl = (Element) faultVar.get();
					responseEl.detach();

					Fault faultOut = exchange.createFault();
					faultOut.setContent(new JDOMSource(new Document(responseEl)));
					exchange.setStatus(ExchangeStatus.ERROR);
					exchange.setFault(faultOut);
					send(exchange);
				}
			} catch (Exception e) {
				Throwable cause = e;
				if (e instanceof CalloutException) {
					cause = e.getCause();
				}
				if ((e instanceof ActionException) && (e.getCause() != null)
						&& (e.getCause() instanceof AppException)) {
					cause = e.getCause();
					LOG.error("action exception", cause);
				}

				exchange.setStatus(ExchangeStatus.ERROR);

				if (cause instanceof FaultException) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Error handling, in fault exception case ["
								+ flowName + "][" + platformSerial + "]", cause);
					}
					FaultException faultEx = (FaultException) cause;
					MessageExchange me = faultEx.getExchange();
					Fault calloutFault = faultEx.getFault();
					String sourceId = (String) me
							.getProperty("sesame.exchange.provider");
					String targetId = (String) exchange
							.getProperty("sesame.exchange.consumer");
					mappingFault(exchange, calloutFault, sourceId, targetId);
					send(exchange);
				} else if (cause instanceof NoFaultAvailableException) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(
								"err handling ,in NoFaultAvailable exception case["
										+ flowName + "][" + platformSerial
										+ "]", cause);
					}
					MessageExchange me = ((NoFaultAvailableException) cause)
							.getMessageExchange();
					String sourceId = (String) me
							.getProperty("sesame.exchange.provider");
					String targetId = (String) exchange
							.getProperty("sesame.exchange.consumer");
					mappingError(exchange, me.getError(), sourceId, targetId);

					send(exchange);
				} else if ((cause instanceof AppException)
						|| (cause instanceof SystemException)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("err handling ,in local exception case",
								cause);
					}

					String targetId = (String) exchange
							.getProperty("sesame.exchange.consumer");
					String sourceId = getContext().getComponentName();
					if (sourceId.equals(targetId)) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("in subflow, forward excpetion["
									+ flowName + "][" + platformSerial + "]",
									cause);
						}
						exchange.setError((Exception) cause);
						send(exchange);
					} else {
						if (LOG.isDebugEnabled()) {
							LOG.debug(
									"in top flow, transform excpetion to fault ["
											+ flowName + "][" + platformSerial
											+ "]", cause);
						}

						mappingError(exchange, (Exception) cause, sourceId,
								targetId);

						send(exchange);
					}
				} else {
					LOG.warn("Error handling, unexpected exception[" + flowName
							+ "][" + platformSerial + "]", cause);
					exchange.setError(new ActionException(cause));
					send(exchange);
				}
			} finally {
				if ((executionCtx != null) && (!(executionCtx.isDehydrated())))
					executionCtx.close();
			}
		}
	}

	private void appendBizParameter(MessageExchange exchange,
			String operationName, ExecutionContext executionCtx) {
		String suName = exchange.getEndpoint().getServiceName().toString();
		List<String> paramNames = new LinkedList();
		paramNames.addAll(AppParameters.getInstance().getAppParamKeys());
		paramNames.addAll(AppParameters.getInstance().getSuParamKeys(suName));
		paramNames.addAll(AppParameters.getInstance().getOperationParamKeys(
				suName, operationName));
		for (String paramName : paramNames) {
			Parameter param = AppParameters.getInstance().getParamter(suName,
					operationName, paramName);
			LOG.debug("APPEND BIZ PARAMEER [" + param + "]");
			if (param.getType().equals(Parameter.PARAMTYPE.PARAM_TYPE_BOOLEAN))
				executionCtx.getDataContext().addVariable(param.getName(),
						new Variable(param.getTypedValue(), 6));
			else if ((param.getType()
					.equals(Parameter.PARAMTYPE.PARAM_TYPE_INT))
					|| (param.getType()
							.equals(Parameter.PARAMTYPE.PARAM_TYPE_DOUBLE)))
				executionCtx.getDataContext().addVariable(param.getName(),
						new Variable(param.getTypedValue(), 8));
			else if (param.getType().equals(
					Parameter.PARAMTYPE.PARAM_TYPE_STRING))
				executionCtx.getDataContext().addVariable(param.getName(),
						new Variable(param.getTypedValue(), 7));
		}
	}

	private void mappingError(MessageExchange exchange, Exception exception,
			String sourceId, String targetId) throws MessagingException {
		if (this.faultProcessor.hasMapping(sourceId, targetId)) {
			LOG.debug("Error mapping: " + sourceId + " >>>>>>>>>>> " + targetId);
			try {
				Exception ex = this.faultProcessor.processException(sourceId,
						targetId, exception);
				exchange.setError(ex);
			} catch (Exception ex) {
				exchange.setError(ex);
			}
		} else {
			exchange.setError(exception);
		}
	}

	private void mappingFault(MessageExchange exchange, Fault orgFault,
			String sourceId, String targetId) throws MessagingException {
		if (this.faultProcessor.hasMapping(sourceId, targetId)) {
			LOG.debug("Fault mapping: " + sourceId + " >>>>>>>>>>> " + targetId);
			try {
				Document doc = this.faultProcessor.processMessage(sourceId,
						targetId, orgFault);
				Fault fault = exchange.createFault();
				fault.setContent(new JDOMSource(doc));
				exchange.setFault(fault);
			} catch (Exception ex) {
				exchange.setError(ex);
			}
		} else {
			exchange.setFault(orgFault);
		}
	}

	private void handleDehydrateResponse(MessageExchange exchange,
			ServiceEndpoint endpoint) throws MessagingException {
		String uuid = (String) exchange
				.getProperty(DehydrateManager.DEHYDRATE_CONTEXT_ID);
		ExecutionContext ec = DehydrateManager
				.getDehydratedExecutionContext(uuid);

		Variable var = exchange2var(exchange);

		ec.getDataContext().addVariable("response", var);
		String operationName = exchange.getOperation().getLocalPart();
		this.engine.execute(ec, endpoint.getEndpointName() + "__"
				+ operationName);
		Variable response = ec.getDataContext().getVariable("response");

		Element responseEl = (Element) response.get();
		responseEl.detach();

		NormalizedMessage resposneMsg = exchange.createMessage();
		resposneMsg.setContent(new JDOMSource(new Document(responseEl)));
		answer(exchange, resposneMsg);
		ec.close();
	}

	public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
		return null;
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

	public void init(String serviceUnitName, String serviceUnitRootPath)
			throws DeploymentException {
		try {
			ServiceUnit unit = getServiceUnit(serviceUnitName);
			ProcessRegistry reg = loadProcesses(unit);
			this.registries.put(serviceUnitName, reg);
		} catch (Throwable e) {
			LOG.debug(e.getMessage(), e);
			throw taskFailure("init-" + serviceUnitName,
					(e.getMessage() != null) ? e.getMessage() : e.toString());
		}
	}

	public void start(String serviceUnitName) throws DeploymentException {
		super.start(serviceUnitName);
	}

	public void stop(String serviceUnitName) throws DeploymentException {
		super.stop(serviceUnitName);
	}

	public void shutDown(String serviceUnitName) throws DeploymentException {
		ProcessRegistry reg = (ProcessRegistry) this.registries
				.get(serviceUnitName);
		if (reg != null) {
			reg.clear();
			this.registries.remove(reg);
		}

		TransformerManager.clearCache(serviceUnitName);
	}

	private Variable exchange2var(MessageExchange exchange) {
		NormalizedMessage request = exchange.getMessage("in");
		Document requestDoc = JdomUtil
				.source2JDOMDocument(request.getContent());
		Element requestEl = (Element) requestDoc.getRootElement().clone();
		return new Variable(requestEl, 0);
	}

	private ProcessRegistry loadProcesses(ServiceUnit unit)
			throws WSDLException, JDOMException, IOException {
		SAXBuilder builder = getSAXBuilder();
		ProcessRegistry reg = new ProcessRegistry();

		Map portTypes = unit.getDefinition().getPortTypes();
		for (PortType portType : (Collection<PortType>) portTypes.values()) {
			List<Operation> operations = portType.getOperations();
			for (Operation opera : operations) {
				File file = new File(unit.getUnitRoot(), opera.getName()
						+ ".xml");
				if (LOG.isDebugEnabled())
					LOG.debug("Load process file: [" + file.getAbsolutePath()
							+ "]");
				Element rootEl = builder.build(file).getRootElement();

				String author = rootEl.getAttributeValue("author", "");
				String description = rootEl
						.getAttributeValue("description", "");
				FlowInfo flow = new FlowInfo();
				flow.setFlowDefination(rootEl);
				flow.setAuthor(author);
				flow.setDescription(description);
				flow.setName(opera.getName());
				this.engine.registerFlow(
						unit.getName() + "__" + opera.getName(), rootEl);
				reg.put(opera.getName(), flow);
			}
		}
		return reg;
	}

	public int getServiceCount() {
		LOG.debug("----------------------------> Count services");
		return this.registries.size();
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		MBeanAttributeInfo[] attrs = super.getAttributeInfos();

		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(), "serviceCount",
				"在本组件部署的服务单元个数");
		return AttributeInfoHelper.join(attrs, helper.getAttributeInfos());
	}

	protected ServiceUnitManager createServiceUnitManager() {
		return this;
	}
}