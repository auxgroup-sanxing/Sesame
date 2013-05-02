package com.sanxing.sesame.dispatch;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.dispatch.cluster.ClusterEndpointChooser;
import com.sanxing.sesame.dispatch.cluster.ClusterEndpointChooserFactory;
import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.messaging.MessageExchangeImpl;
import com.sanxing.sesame.core.Env;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.router.Router;
import com.sanxing.sesame.servicedesc.AbstractEndpoint;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import com.sanxing.sesame.util.SystemProperties;
import java.io.PrintStream;
import java.util.concurrent.Executor;
import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessagingException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMSDispatcher extends AbstractDispatcher implements
		MessageListener {
	public static final String name = "cluster";
	public static final String CEChoosePolicy = SystemProperties.get(
			"com.sanxing.sesame.cluster-endpoint-choose-policy", "random");
	public static final String CEPOLICY_RANDOM = "random";
	public static final String CEPOLICY_ROUDNROBBIN = "round-robbin";
	private Executor worker = ExecutorFactory.getFactory().createExecutor(
			"services.registry");

	private Logger LOG = LoggerFactory.getLogger(JMSDispatcher.class);

	private QueueConnection con = null;

	private static ClusterEndpointChooser getCEChooser(InternalEndpoint ie) {
		if (CEChoosePolicy.equals("random")) {
			return ClusterEndpointChooserFactory.random();
		}
		if (CEChoosePolicy.equalsIgnoreCase("round-robbin")) {
			return ClusterEndpointChooserFactory.roundRobbin(ie);
		}
		throw new RuntimeException("unspported ce choooser type");
	}

	public String getName() {
		return "cluster";
	}

	public void init(Router router) throws JBIException {
		super.init(router);

		if (!(Platform.getEnv().isClustered()))
			return;
		try {
			this.LOG.info("prepare JMS provider");
			QueueConnectionFactory qcf = getConnectionFactoryByServer(Platform
					.getEnv().getServerName());

			Queue adminQueue = (Queue) getRouter().getContainer()
					.getNamingContext()
					.lookup(Platform.getEnv().getServerName() + "-NMR-QUEUE");

			QueueConnection con = qcf.createQueueConnection();
			QueueSession qs = con.createQueueSession(false, 1);

			qs.createConsumer(adminQueue).setMessageListener(this);
			con.start();
		} catch (Exception e) {
			this.LOG.error("create jms dispatcher err", e);
		}
	}

	protected void doSend(MessageExchangeImpl me) throws JBIException {
		if (me.getDestinationId() == null) {
			me.setDestinationId(((AbstractEndpoint) me.getEndpoint())
					.getComponentNameSpace());
		}

		InternalEndpoint internalEndpoint = (InternalEndpoint) me.getEndpoint();
		if (this.LOG.isDebugEnabled()) {
			this.LOG.debug("role in me ....." + me.getRole());
			this.LOG.debug("ME source " + me.getSourceId());
			this.LOG.debug("endpoint is ....." + internalEndpoint);
			this.LOG.debug("belong to component["
					+ internalEndpoint.getComponentNameSpace() + "]");
		}

		if (me.getRole() == MessageExchange.Role.CONSUMER) {
			if (this.LOG.isDebugEnabled()) {
				this.LOG.debug("handling repsonse.....");
			}
			if (!(me.getSourceId().getContainerName().equals(getRouter()
					.getContainer().getName()))) {
				if (this.LOG.isDebugEnabled()) {
					this.LOG.debug("send reponse to remote");
				}
				send(me);
				return;
			}
			if (this.LOG.isDebugEnabled()) {
				this.LOG.debug("send reponse to local");
			}
			doRouting(me);
		} else {
			if (this.LOG.isDebugEnabled()) {
				this.LOG.debug("sending request......");
				this.LOG.debug("destination endpoint is ..." + internalEndpoint);
			}
			if (internalEndpoint.isClustered()) {
				InternalEndpoint[] candiates = internalEndpoint
						.getRemoteEndpoints();

				InternalEndpoint target = choose(internalEndpoint, candiates);

				if (this.LOG.isDebugEnabled()) {
					this.LOG.debug("choosed destination is ["
							+ target.getComponentNameSpace() + "]");
				}
				me.setDestinationId(target.getComponentNameSpace());
				if (!(target.getComponentNameSpace().getContainerName()
						.equals(getRouter().getContainer().getName())))
					;
				try {
					if (this.LOG.isDebugEnabled()) {
						this.LOG.debug("send request to remote");
					}
					send(me);
					return;
				} catch (Exception e) {
					this.LOG.error("send by jms error", e);

					if (this.LOG.isDebugEnabled()) {
						this.LOG.debug("send request to local");
					}
					doRouting(me);
				}
			} else {
				if (this.LOG.isDebugEnabled()) {
					this.LOG.debug("send request to local");
				}
				doRouting(me);
			}
		}
	}

	public void onMessage(final Message msg) {
		if (this.LOG.isDebugEnabled()) {
			this.LOG.debug("receving messge from remote " + msg);
		}

		this.worker.execute(new Runnable() {
			public void run() {
				JMSDispatcher.this.recRemoteMsg(msg);
			}
		});
		if (this.LOG.isDebugEnabled())
			this.LOG.debug("finished routing remot message");
	}

	private void recRemoteMsg(Message msg) {
		try {
			this.LOG.debug("CALLING recRemoteMsg");
			ObjectMessage message = (ObjectMessage) msg;
			MessageExchangeImpl me = (MessageExchangeImpl) message.getObject();
			if (me.getRole() == MessageExchange.Role.CONSUMER) {
				if (this.LOG.isDebugEnabled()) {
					this.LOG.debug("ME ROLE IS CONSUMER");
				}

				doRouting(me);

				if (this.LOG.isDebugEnabled())
					this.LOG.debug("finished process inbound");
			} else {
				if (this.LOG.isDebugEnabled()) {
					this.LOG.debug("ME ROLE IS PROVIDER");
				}

				doRouting(me);
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		} finally {
		}
	}

	protected QueueConnectionFactory getConnectionFactoryByServer(
			String serverName) {
		try {
			this.LOG.debug("got qc for server [" + serverName + "]");

			this.LOG.debug("context is ...."
					+ getRouter().getContainer().getNamingContext());

			QueueConnectionFactory qcf = (QueueConnectionFactory) getRouter()
					.getContainer().getNamingContext()
					.lookup(serverName + "-QC");

			return qcf;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("server not registered", e);
		}
	}

	public void send(MessageExchangeImpl me) {
		String serverName;
		if (me.getRole() == MessageExchange.Role.CONSUMER) {
			this.LOG.debug("sending me to+" + me.getSourceId());
			serverName = me.getSourceId().getContainerName();
		} else {
			this.LOG.debug("sending me to+" + me.getDestinationId());
			serverName = me.getDestinationId().getContainerName();
		}
		QueueSession queueSession;
		try {
			QueueConnection con = getQueueConnection();
			queueSession = con.createQueueSession(false, 1);

			Queue queue = queueSession.createQueue(serverName + "-NMR-QUEUE");

			QueueSender queueSender = queueSession.createSender(queue);
			queueSender.send(queueSession.createObjectMessage(me));
			this.LOG.debug("sending ok.................");
		} catch (Exception e) {
		} finally {
			if (this.con == null)
				;
		}
	}

	private QueueConnection getQueueConnection() throws JMSException {
		if (this.con == null) {
			QueueConnectionFactory cf = getConnectionFactoryByServer(Platform
					.getEnv().getServerName());

			this.con = cf.createQueueConnection();
		}
		return this.con;
	}

	public InternalEndpoint choose(InternalEndpoint localPoint,
			InternalEndpoint[] remotes) {
		return getCEChooser(localPoint).choose(localPoint, remotes);
	}

	public boolean canHandle(MessageExchange me) {
		return false;
	}

	public String getDescription() {
		return "cluster dispatcher";
	}

	public static void main(String[] args) {
		System.out
				.println(MessageExchange.Role.PROVIDER == MessageExchange.Role.CONSUMER);
	}
}