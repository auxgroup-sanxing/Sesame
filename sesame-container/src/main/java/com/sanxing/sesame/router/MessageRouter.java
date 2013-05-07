package com.sanxing.sesame.router;

import com.sanxing.sesame.container.ActivationSpec;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.dispatch.DefaultChooser;
import com.sanxing.sesame.dispatch.Dispatcher;
import com.sanxing.sesame.dispatch.DispatcherChooser;
import com.sanxing.sesame.dispatch.DispatcherProvider;
import com.sanxing.sesame.management.OperationInfoHelper;
import com.sanxing.sesame.mbean.BaseSystemService;
import com.sanxing.sesame.mbean.ComponentContextImpl;
import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.mbean.ManagementContext;
import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.messaging.MessageExchangeImpl;
import com.sanxing.sesame.resolver.EndpointChooser;
import com.sanxing.sesame.resolver.EndpointFilter;
import com.sanxing.sesame.resolver.EndpointResolver;
import com.sanxing.sesame.resolver.FirstChoicePolicy;
import com.sanxing.sesame.servicedesc.AbstractEndpoint;
import com.sanxing.sesame.servicedesc.ExternalEndpoint;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import com.sanxing.sesame.servicedesc.LinkedEndpoint;
import java.util.ArrayList;
import java.util.List;
import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.MBeanOperationInfo;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageRouter extends BaseSystemService implements Router {
	private static final Logger LOG = LoggerFactory.getLogger(MessageRouter.class);
	private Registry registry;
	private String dispatcherNames = "cluster,straight";
	private String subscriptionFlowName;
	private Dispatcher[] dispatchers;
	private EndpointChooser defaultServiceChooser = new FirstChoicePolicy();
	private EndpointChooser defaultInterfaceChooser = new FirstChoicePolicy();

	private DispatcherChooser defaultChooser = new DefaultChooser();

	public String getDescription() {
		return "Normalized Message Router";
	}

	public void init(JBIContainer container) throws JBIException {
		super.init(container);
		this.registry = container.getRegistry();

		if (this.dispatchers == null) {
			String[] names = this.dispatcherNames.split(",");
			this.dispatchers = new Dispatcher[names.length];
			for (int i = 0; i < names.length; ++i) {
				this.dispatchers[i] = DispatcherProvider
						.getDispatcher(names[i]);
				this.dispatchers[i].init(this);
			}
		} else {
			for (int i = 0; i < this.dispatchers.length; ++i)
				this.dispatchers[i].init(this);
		}
	}

	protected Class<RouterMBean> getServiceMBean() {
		return RouterMBean.class;
	}

	public String getContainerName() {
		return this.container.getName();
	}

	public ManagementContext getManagementContext() {
		return this.container.getManagementContext();
	}

	public Registry getRegistry() {
		return this.registry;
	}

	public void start() throws JBIException {
		for (int i = 0; i < this.dispatchers.length; ++i) {
			this.dispatchers[i].start();
		}
		super.start();
	}

	public void stop() throws JBIException {
		for (int i = 0; i < this.dispatchers.length; ++i) {
			this.dispatchers[i].stop();
		}
		super.stop();
	}

	public void shutDown() throws JBIException {
		if (getCurrentState() == "Shutdown")
			return;

		stop();
		for (int i = 0; i < this.dispatchers.length; ++i) {
			this.dispatchers[i].shutDown();
		}
		super.shutDown();
		this.container.getManagementContext().unregisterMBean(this);
	}

	public String getFlowNames() {
		return this.dispatcherNames;
	}

	public void setFlowNames(String flowNames) {
		this.dispatcherNames = flowNames;
	}

	public String getSubscriptionFlowName() {
		return this.subscriptionFlowName;
	}

	public void setSubscriptionFlowName(String subscriptionFlowName) {
		this.subscriptionFlowName = subscriptionFlowName;
	}

	public void setDispatchers(Dispatcher[] dispatchers) {
		this.dispatchers = dispatchers;
	}

	public Dispatcher[] getDispatchers() {
		return this.dispatchers;
	}

	public void suspend() {
		for (int i = 0; i < this.dispatchers.length; ++i)
			this.dispatchers[i].suspend();
	}

	public void resume() {
		for (int i = 0; i < this.dispatchers.length; ++i)
			this.dispatchers[i].resume();
	}

	public void sendExchangePacket(MessageExchange me) throws JBIException {
		MessageExchangeImpl exchange = (MessageExchangeImpl) me;
		if (LOG.isDebugEnabled()) {
			LOG.debug("send exchange " + me);
		}
		if ((exchange.getRole() == MessageExchange.Role.PROVIDER)
				&& (exchange.getDestinationId() == null)) {
			resolveAddress(exchange);
		}

		boolean foundRoute = false;

		if ((exchange.getEndpoint() != null)
				|| (exchange.getRole() == MessageExchange.Role.CONSUMER)) {
			foundRoute = true;
			Dispatcher dispatcher = this.defaultChooser.chooseDispatcher(
					this.dispatchers, exchange);
			if (LOG.isDebugEnabled()) {
				LOG.debug("choose diaptcher " + dispatcher.getName());
			}
			if (dispatcher == null) {
				throw new MessagingException(
						"Unable to choose a flow for exchange: " + exchange);
			}

			dispatcher.send(exchange);
		}

		if (!(foundRoute)) {
			boolean throwException = true;
			ActivationSpec activationSpec = exchange.getActivationSpec();
			if (activationSpec != null) {
				throwException = activationSpec.isFailIfNoDestinationEndpoint();
			}
			if (throwException) {
				throw new MessagingException(
						"Could not find route for exchange: " + exchange
								+ " for service: " + exchange.getService()
								+ " and interface: "
								+ exchange.getInterfaceName());
			}
			if (exchange.getMirror().getSyncState() != 1)
				return;
		}
	}

	protected void resolveAddress(MessageExchangeImpl exchange)
			throws JBIException {
		ServiceEndpoint theEndpoint = exchange.getEndpoint();
		if (theEndpoint != null) {
			if (theEndpoint instanceof ExternalEndpoint) {
				throw new JBIException(
						"External endpoints can not be used for routing: should be an internal or dynamic endpoint.");
			}
			if (!(theEndpoint instanceof AbstractEndpoint)) {
				throw new JBIException(
						"Component-specific endpoints can not be used for routing: should be an internal or dynamic endpoint.");
			}

		}

		if (theEndpoint instanceof LinkedEndpoint) {
			QName svcName = ((LinkedEndpoint) theEndpoint).getToService();
			String epName = ((LinkedEndpoint) theEndpoint).getToEndpoint();
			ServiceEndpoint ep = this.registry.getInternalEndpoint(svcName,
					epName);
			if (ep == null) {
				throw new JBIException("Could not resolve linked endpoint: "
						+ theEndpoint);
			}
			theEndpoint = ep;
		}

		ComponentContextImpl context = exchange.getSourceContext();
		if (theEndpoint == null) {
			QName serviceName = exchange.getService();
			QName interfaceName = exchange.getInterfaceName();

			if (serviceName != null) {
				ServiceEndpoint[] endpoints = this.registry
						.getEndpointsForService(serviceName);
				endpoints = getMatchingEndpoints(endpoints, exchange);
				theEndpoint = getServiceChooser(exchange).chooseEndpoint(
						endpoints, context, exchange);
				if (theEndpoint == null) {
					LOG.warn("ServiceName ("
							+ serviceName
							+ ") specified for routing, but can't find it registered");
				}
			}
			if ((theEndpoint == null) && (interfaceName != null)) {
				ServiceEndpoint[] endpoints = this.registry
						.getEndpointsForInterface(interfaceName);
				endpoints = getMatchingEndpoints(endpoints, exchange);
				theEndpoint = (InternalEndpoint) getInterfaceChooser(exchange)
						.chooseEndpoint(endpoints, context, exchange);
				if (theEndpoint == null) {
					LOG.warn("InterfaceName ("
							+ interfaceName
							+ ") specified for routing, but can't find any matching components");
				}
			}
			if (theEndpoint == null) {
				ActivationSpec activationSpec = exchange.getActivationSpec();
				if (activationSpec != null) {
					EndpointResolver destinationResolver = activationSpec
							.getDestinationResolver();
					if (destinationResolver != null) {
						try {
							EndpointFilter filter = createEndpointFilter(
									context, exchange);
							theEndpoint = (InternalEndpoint) destinationResolver
									.resolveEndpoint(context, exchange, filter);
						} catch (JBIException e) {
							throw new MessagingException(
									"Failed to resolve endpoint: " + e, e);
						}
					}
				}
			}
		}
		if (theEndpoint != null) {
			exchange.setEndpoint(theEndpoint);
		}
		if (LOG.isTraceEnabled())
			LOG.trace("Routing exchange " + exchange + " to: " + theEndpoint);
	}

	protected ServiceEndpoint[] getMatchingEndpoints(
			ServiceEndpoint[] endpoints, MessageExchangeImpl exchange) {
		List filtered = new ArrayList();
		ComponentMBeanImpl consumer = getRegistry().getComponent(
				exchange.getSourceId());

		for (int i = 0; i < endpoints.length; ++i) {
			ComponentNameSpace id = ((InternalEndpoint) endpoints[i])
					.getComponentNameSpace();
			ComponentMBeanImpl provider;
			if (id != null) {
				provider = getRegistry().getComponent(id);
			}

			filtered.add(endpoints[i]);
		}
		return ((ServiceEndpoint[]) filtered
				.toArray(new ServiceEndpoint[filtered.size()]));
	}

	public EndpointChooser getDefaultInterfaceChooser() {
		return this.defaultInterfaceChooser;
	}

	public void setDefaultInterfaceChooser(
			EndpointChooser defaultInterfaceChooser) {
		this.defaultInterfaceChooser = defaultInterfaceChooser;
	}

	public EndpointChooser getDefaultServiceChooser() {
		return this.defaultServiceChooser;
	}

	public void setDefaultServiceChooser(EndpointChooser defaultServiceChooser) {
		this.defaultServiceChooser = defaultServiceChooser;
	}

	public DispatcherChooser getDefaultFlowChooser() {
		return this.defaultChooser;
	}

	public void setDefaultFlowChooser(DispatcherChooser defaultFlowChooser) {
		this.defaultChooser = defaultFlowChooser;
	}

	protected EndpointChooser getServiceChooser(MessageExchangeImpl exchange) {
		EndpointChooser chooser = null;
		ActivationSpec activationSpec = exchange.getActivationSpec();
		if (activationSpec != null) {
			chooser = activationSpec.getServiceChooser();
		}
		if (chooser == null) {
			chooser = this.defaultServiceChooser;
		}
		return chooser;
	}

	protected EndpointChooser getInterfaceChooser(MessageExchangeImpl exchange) {
		EndpointChooser chooser = null;
		ActivationSpec activationSpec = exchange.getActivationSpec();
		if (activationSpec != null) {
			chooser = activationSpec.getInterfaceChooser();
		}
		if (chooser == null) {
			chooser = this.defaultInterfaceChooser;
		}
		return chooser;
	}

	protected EndpointFilter createEndpointFilter(ComponentContextImpl context,
			MessageExchangeImpl exchange) {
		Component component = null;
		if (exchange.getRole() == MessageExchange.Role.PROVIDER) {
			return null;
		}
		return null;
	}

	public MBeanOperationInfo[] getOperationInfos() throws JMException {
		OperationInfoHelper helper = new OperationInfoHelper();
		helper.addOperation(getObjectToManage(), "suspend",
				"suspend the NMR processing");
		helper.addOperation(getObjectToManage(), "resume",
				"resume the NMR processing");

		return OperationInfoHelper.join(super.getOperationInfos(),
				helper.getOperationInfos());
	}

	public JBIContainer getContainer() {
		return this.container;
	}
}