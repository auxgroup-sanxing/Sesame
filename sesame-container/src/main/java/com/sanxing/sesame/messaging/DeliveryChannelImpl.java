package com.sanxing.sesame.messaging;

import com.sanxing.sesame.container.ActivationSpec;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.exception.ExchangeTimeoutException;
import com.sanxing.sesame.listener.MessageExchangeListener;
import com.sanxing.sesame.mbean.ComponentContextImpl;
import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.router.Router;
import com.sanxing.sesame.uuid.IdGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryChannelImpl implements DeliveryChannel {
	private static final Logger LOG = LoggerFactory.getLogger(DeliveryChannelImpl.class);
	private JBIContainer container;
	private ComponentContextImpl context;
	private ComponentMBeanImpl component;
	private BlockingQueue<MessageExchangeImpl> queue;
	private IdGenerator idGenerator = new IdGenerator();
	private MessageExchangeFactory inboundFactory;
	private int intervalCount;
	private AtomicBoolean closed = new AtomicBoolean(false);

	private Map<Thread, Boolean> waiters = new ConcurrentHashMap();
	private TransactionManager transactionManager;
	private Map<String, MessageExchangeImpl> exchangesById = new ConcurrentHashMap();

	public DeliveryChannelImpl(ComponentMBeanImpl component) {
		this.component = component;
		this.container = component.getContainer();
		this.queue = new ArrayBlockingQueue(component.getInboundQueueCapacity());
		this.transactionManager = ((TransactionManager) this.container
				.getTransactionManager());
	}

	public int getQueueSize() {
		return this.queue.size();
	}

	public void close() throws MessagingException {
		if (this.closed.compareAndSet(false, true)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Closing DeliveryChannel " + this);
			}
			List<MessageExchangeImpl> pending = new ArrayList(this.queue.size());
			this.queue.drainTo(pending);
			for (MessageExchangeImpl messageExchange : pending) {
				if ((messageExchange.getTransactionContext() != null)
						&& (messageExchange.getMirror().getSyncState() == 1)) {
					notifyExchange(messageExchange.getMirror(),
							messageExchange.getMirror(), "close");
				}
			}

			Thread[] threads = (Thread[]) this.waiters.keySet().toArray(
					new Thread[this.waiters.size()]);
			for (int i = 0; i < threads.length; ++i) {
				threads[i].interrupt();
			}

			ServiceEndpoint[] endpoints = this.container.getRegistry()
					.getEndpointsForComponent(
							this.component.getComponentNameSpace());
			for (int i = 0; i < endpoints.length; ++i)
				try {
					this.component.getContext()
							.deactivateEndpoint(endpoints[i]);
				} catch (JBIException e) {
					LOG.error("Error deactivating endpoint", e);
				}
		}
	}

	protected void checkNotClosed() throws MessagingException {
		if (this.closed.get())
			throw new MessagingException(this + " has been closed.");
	}

	public MessageExchangeFactory createExchangeFactory() {
		MessageExchangeFactoryImpl result = createMessageExchangeFactory();
		result.setContext(this.context);
		ActivationSpec activationSpec = this.context.getActivationSpec();
		if (activationSpec != null) {
			String componentName = this.context.getComponentNameSpace()
					.getName();

			QName serviceName = activationSpec.getDestinationService();
			if (serviceName != null) {
				result.setServiceName(serviceName);
				LOG.debug("default destination serviceName for "
						+ componentName + " = " + serviceName);
			}
			QName interfaceName = activationSpec.getDestinationInterface();
			if (interfaceName != null) {
				result.setInterfaceName(interfaceName);
				LOG.debug("default destination interfaceName for "
						+ componentName + " = " + interfaceName);
			}
			QName operationName = activationSpec.getDestinationOperation();
			if (operationName != null) {
				result.setOperationName(operationName);
				LOG.debug("default destination operationName for "
						+ componentName + " = " + operationName);
			}
			String endpointName = activationSpec.getDestinationEndpoint();
			if (endpointName != null) {
				boolean endpointSet = false;
				LOG.debug("default destination endpointName for "
						+ componentName + " = " + endpointName);
				if ((serviceName != null) && (endpointName != null)) {
					endpointName = endpointName.trim();
					ServiceEndpoint endpoint = this.container.getRegistry()
							.getEndpoint(serviceName, endpointName);
					if (endpoint != null) {
						result.setEndpoint(endpoint);
						LOG.info("Set default destination endpoint for "
								+ componentName + " to " + endpoint);
						endpointSet = true;
					}
				}
				if (!(endpointSet)) {
					LOG.warn("Could not find destination endpoint for "
							+ componentName + " service(" + serviceName
							+ ") with endpointName " + endpointName);
				}
			}
		}

		return result;
	}

	public MessageExchangeFactory createExchangeFactory(QName interfaceName) {
		MessageExchangeFactoryImpl result = createMessageExchangeFactory();
		result.setInterfaceName(interfaceName);
		return result;
	}

	public MessageExchangeFactory createExchangeFactoryForService(
			QName serviceName) {
		MessageExchangeFactoryImpl result = createMessageExchangeFactory();
		result.setServiceName(serviceName);
		return result;
	}

	public MessageExchangeFactory createExchangeFactory(ServiceEndpoint endpoint) {
		MessageExchangeFactoryImpl result = createMessageExchangeFactory();
		result.setEndpoint(endpoint);
		return result;
	}

	protected MessageExchangeFactoryImpl createMessageExchangeFactory() {
		MessageExchangeFactoryImpl messageExchangeFactory = new MessageExchangeFactoryImpl(
				this.idGenerator, this.closed);
		messageExchangeFactory.setContext(this.context);
		return messageExchangeFactory;
	}

	public MessageExchange accept() throws MessagingException {
		return accept(9223372036854775807L);
	}

	public MessageExchange accept(long timeoutMS) throws MessagingException {
		try {
			checkNotClosed();
			MessageExchangeImpl me = (MessageExchangeImpl) this.queue.poll(
					timeoutMS, TimeUnit.MILLISECONDS);
			if (me != null) {
				if (me.getPacket().isAborted()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Aborted " + me.getExchangeId() + " in "
								+ this);
					}
					me = null;
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Accepting " + me.getExchangeId() + " in "
								+ this);
					}

					if ((me.getTxLock() != null)
							&& (me.getStatus() != ExchangeStatus.ACTIVE)) {
						notifyExchange(me.getMirror(), me.getTxLock(),
								"acceptFinishedExchangeWithTxLock");
						me.handleAccept();
						if (LOG.isTraceEnabled()) {
							LOG.trace("Accepted: " + me);
						}
					} else if ((me.isTransacted())
							&& (me.getStatus() != ExchangeStatus.ACTIVE)) {
						me.handleAccept();
						if (LOG.isTraceEnabled())
							LOG.trace("Accepted: " + me);
					} else {
						resumeTx(me);
						me.handleAccept();
						if (LOG.isTraceEnabled()) {
							LOG.trace("Accepted: " + me);
						}
					}
				}
			}

			return me;
		} catch (InterruptedException e) {
			throw new MessagingException("accept failed", e);
		}
	}

	protected void autoSetPersistent(MessageExchangeImpl me) {
		Boolean persistent = me.getPersistent();
		if (persistent == null) {
			if (this.context.getActivationSpec().getPersistent() != null)
				persistent = this.context.getActivationSpec().getPersistent();
			else {
				persistent = Boolean.valueOf(this.context.getContainer()
						.isPersistent());
			}
			me.setPersistent(persistent);
		}
	}

	protected void throttle() {
		if (this.component.isExchangeThrottling()) {
			if (this.component.getThrottlingInterval() > this.intervalCount) {
				this.intervalCount = 0;
				try {
					Thread.sleep(this.component.getThrottlingTimeout());
				} catch (InterruptedException e) {
					LOG.warn("throttling failed", e);
				}
			}
			this.intervalCount += 1;
		}
	}

	protected void doSend(MessageExchangeImpl me, boolean sync)
			throws MessagingException {
		MessageExchangeImpl mirror = me.getMirror();
		boolean finished = me.getStatus() != ExchangeStatus.ACTIVE;
		try {
			if (me.getPacket().isAborted()) {
				throw new ExchangeTimeoutException(me);
			}

			autoEnlistInTx(me);

			autoSetPersistent(me);

			throttle();

			if (me.getRole() == MessageExchange.Role.CONSUMER) {
				me.setSourceId(this.component.getComponentNameSpace());
			}

			me.handleSend(sync);
			mirror.setTxState(0);

			if ((finished) && (me.getTxLock() == null)
					&& (me.getTxState() == 2) && (!(me.isPushDelivery()))
					&& (me.getRole() == MessageExchange.Role.CONSUMER)) {
				me.setTransactionContext(null);
			}

			this.container.getRouter().sendExchangePacket(mirror);
		} catch (MessagingException e) {
		} catch (JBIException e) {
		} finally {
			if (me.getTxLock() != null) {
				if (mirror.getTxState() == 1) {
					suspendTx(mirror);
				}
				synchronized (me.getTxLock()) {
					notifyExchange(me, me.getTxLock(), "doSendWithTxLock");
				}
			}
		}
	}

	public void send(MessageExchange messageExchange) throws MessagingException {
		checkNotClosed();

		messageExchange.setProperty("javax.jbi.messaging.sendSync", null);
		MessageExchangeImpl me = (MessageExchangeImpl) messageExchange;
		doSend(me, false);
	}

	public boolean sendSync(MessageExchange messageExchange)
			throws MessagingException {
		return sendSync(messageExchange, 0L);
	}

	public boolean sendSync(MessageExchange messageExchange, long timeout)
			throws MessagingException {
		checkNotClosed();

		boolean result = false;

		messageExchange.setProperty("javax.jbi.messaging.sendSync",
				Boolean.TRUE);

		MessageExchangeImpl me = (MessageExchangeImpl) messageExchange;
		String exchangeKey = me.getKey();
		try {
			this.exchangesById.put(exchangeKey, me);

			synchronized (me) {
				doSend(me, true);
				if (me.getSyncState() != 2) {
					waitForExchange(me, me, timeout, "sendSync");
				} else if (LOG.isDebugEnabled()) {
					LOG.debug("Exchange " + messageExchange.getExchangeId()
							+ " has already been answered (no need to wait)");
				}
			}

			if (me.getSyncState() == 2) {
				me.handleAccept();

				resumeTx(me);

				result = true;
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Exchange " + messageExchange.getExchangeId()
							+ " has been aborted");
				}
				me.getPacket().setAborted(true);
				result = false;
			}
		} catch (InterruptedException e) {
		} catch (RuntimeException e) {
		} finally {
			this.exchangesById.remove(exchangeKey);
		}
		return result;
	}

	public JBIContainer getContainer() {
		return this.container;
	}

	public void setContainer(JBIContainer container) {
		this.container = container;
	}

	public ComponentMBeanImpl getComponent() {
		return this.component;
	}

	public ComponentContextImpl getContext() {
		return this.context;
	}

	public void setContext(ComponentContextImpl context) {
		this.context = context;
	}

	public void processInBound(MessageExchangeImpl me)
			throws MessagingException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("Processing inbound exchange: " + me);
		}

		checkNotClosed();

		MessageExchangeImpl original = (MessageExchangeImpl) this.exchangesById
				.get(me.getKey());
		if ((original != null) && (me != original)) {
			original.copyFrom(me);
			me = original;
		}

		if (me.getSyncState() == 1) {
			suspendTx(original);
			me.setSyncState(2);
			notifyExchange(original, original,
					"processInboundSynchronousExchange");
			return;
		}

		MessageExchangeListener listener = getExchangeListener();
		if (listener != null) {
			me.handleAccept();

			me.setPushDeliver(true);

			ClassLoader old = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(
						this.component.getComponent().getClass()
								.getClassLoader());
				listener.onMessageExchange(me);
			} finally {
				Thread.currentThread().setContextClassLoader(old);
			}

			return;
		}

		if ((me.isTransacted()) && (me.getStatus() == ExchangeStatus.ACTIVE)) {
			if (me.getTxState() == 2) {
				try {
					suspendTx(me);
					this.queue.put(me);
				} catch (InterruptedException e) {
					LOG.debug("Exchange " + me.getExchangeId()
							+ " aborted due to thread interruption", e);
					me.getPacket().setAborted(true);
				}

			} else {
				Object lock = new Object();
				synchronized (lock) {
					try {
						me.setTxLock(lock);
						suspendTx(me);
						this.queue.put(me);
						waitForExchange(me, lock, 0L,
								"processInboundTransactionalExchange");
					} catch (InterruptedException e) {
						LOG.debug("Exchange " + me.getExchangeId()
								+ " aborted due to thread interruption", e);
						me.getPacket().setAborted(true);
					} finally {
						me.setTxLock(null);
						resumeTx(me);
					}
				}

			}

		} else
			try {
				this.queue.put(me);
			} catch (InterruptedException e) {
				LOG.debug("Exchange " + me.getExchangeId()
						+ " aborted due to thread interruption", e);
				me.getPacket().setAborted(true);
			}
	}

	protected MessageExchangeListener getExchangeListener() {
		Component comp = this.component.getComponent();
		if (comp instanceof MessageExchangeListener) {
			return ((MessageExchangeListener) comp);
		}
		ComponentLifeCycle lifecycle = this.component.getLifeCycle();
		if (lifecycle instanceof MessageExchangeListener) {
			return ((MessageExchangeListener) lifecycle);
		}
		return null;
	}

	protected void waitForExchange(MessageExchangeImpl me, Object lock,
			long timeout, String from) throws InterruptedException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Waiting for exchange " + me.getExchangeId() + " ("
					+ Integer.toHexString(me.hashCode())
					+ ") to be answered in " + this + " from " + from);
		}

		Thread th = Thread.currentThread();
		try {
			this.waiters.put(th, Boolean.TRUE);
			lock.wait(timeout);
		} finally {
			this.waiters.remove(th);
		}
		if (LOG.isDebugEnabled())
			LOG.debug("Notified: " + me.getExchangeId() + "("
					+ Integer.toHexString(me.hashCode()) + ") in " + this
					+ " from " + from);
	}

	protected void notifyExchange(MessageExchangeImpl me, Object lock,
			String from) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Notifying exchange " + me.getExchangeId() + "("
					+ Integer.toHexString(me.hashCode()) + ") in " + this
					+ " from " + from);
		}

		synchronized (lock) {
			lock.notify();
		}
	}

	public MessageExchangeFactory getInboundFactory() {
		if (this.inboundFactory == null) {
			this.inboundFactory = createExchangeFactory();
		}
		return this.inboundFactory;
	}

	protected void suspendTx(MessageExchangeImpl me) {
		if (this.transactionManager == null)
			return;
		try {
			Transaction oldTx = me.getTransactionContext();
			if (oldTx != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Suspending transaction for "
							+ me.getExchangeId() + " in " + this);
				}
				Transaction tx = this.transactionManager.suspend();
				if (tx != oldTx)
					throw new IllegalStateException(
							"the transaction context set in the messageExchange is not bound to the current thread");
			}
		} catch (Exception e) {
			LOG.info("Exchange " + me.getExchangeId()
					+ " aborted due to transaction exception", e);
			me.getPacket().setAborted(true);
		}
	}

	protected void resumeTx(MessageExchangeImpl me) throws MessagingException {
		if (this.transactionManager == null)
			return;
		try {
			Transaction oldTx = me.getTransactionContext();
			if (oldTx != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Resuming transaction for " + me.getExchangeId()
							+ " in " + this);
				}
				this.transactionManager.resume(oldTx);
			}
		} catch (Exception e) {
			throw new MessagingException(e);
		}
	}

	protected void autoEnlistInTx(MessageExchangeImpl me)
			throws MessagingException {
		if ((this.transactionManager == null)
				|| (!(this.container.isAutoEnlistInTransaction())))
			return;
		try {
			Transaction tx = this.transactionManager.getTransaction();
			if (tx != null) {
				Object oldTx = me.getTransactionContext();
				if (oldTx == null)
					me.setTransactionContext(tx);
				else if (oldTx != tx)
					throw new IllegalStateException(
							"the transaction context set in the messageExchange is not bound to the current thread");
			}
		} catch (Exception e) {
			throw new MessagingException(e);
		}
	}

	public String toString() {
		return "DeliveryChannel{" + this.component.getName() + "}";
	}

	public void cancelPendingExchanges() {
		for (String id : this.exchangesById.keySet()) {
			MessageExchange exchange = (MessageExchange) this.exchangesById
					.get(id);
			synchronized (exchange) {
				exchange.notifyAll();
			}
		}
	}
}