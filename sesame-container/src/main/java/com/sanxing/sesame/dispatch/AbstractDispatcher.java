package com.sanxing.sesame.dispatch;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.management.BaseLifeCycle;
import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.mbean.ManagementContext;
import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.messaging.DeliveryChannelImpl;
import com.sanxing.sesame.messaging.ExchangePacket;
import com.sanxing.sesame.messaging.MessageExchangeImpl;
import com.sanxing.sesame.router.Router;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractDispatcher extends BaseLifeCycle implements
		Dispatcher {
	protected final Log log;
	protected Router router;
	protected ExecutorFactory executorFactory;
	private ReadWriteLock lock;
	private Thread suspendThread;
	private String name;

	public AbstractDispatcher() {
		this.log = LogFactory.getLog(AbstractDispatcher.class);

		this.lock = new ReentrantReadWriteLock();
	}

	public void init(Router router) throws JBIException {
		this.router = router;
		this.executorFactory = router.getContainer().getExecutorFactory();

		ObjectName objectName = router.getContainer().getManagementContext()
				.createObjectName(this);
		try {
			router.getContainer().getManagementContext()
					.registerMBean(objectName, this, LifeCycleMBean.class);
		} catch (JMException e) {
			throw new JBIException(
					"Failed to register MBean with the ManagementContext", e);
		}
	}

	public void start() throws JBIException {
		super.start();
	}

	public void stop() throws JBIException {
		if (this.log.isDebugEnabled()) {
			this.log.debug("Called dispatcher stop");
		}
		if (this.suspendThread != null) {
			this.suspendThread.interrupt();
		}
		super.stop();
	}

	public void shutDown() throws JBIException {
		if (this.log.isDebugEnabled()) {
			this.log.debug("Called dispatcher shutdown");
		}
		this.router.getContainer().getManagementContext().unregisterMBean(this);
		super.shutDown();
	}

	public void send(MessageExchange me) throws JBIException {
		if (this.log.isDebugEnabled()) {
			this.log.debug("Called Dispatcher send");
		}
		try {
			this.lock.readLock().lock();
			doSend((MessageExchangeImpl) me);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	public synchronized void suspend() {
		if (this.log.isDebugEnabled()) {
			this.log.debug("Called dispatcher[" + getName() + "] suspend");
		}
		this.lock.writeLock().lock();
		this.suspendThread = Thread.currentThread();
	}

	public synchronized void resume() {
		if (this.log.isDebugEnabled()) {
			this.log.debug("Called dispatcher[" + getName() + "] resume");
		}
		this.lock.writeLock().unlock();
		this.suspendThread = null;
	}

	protected abstract void doSend(MessageExchangeImpl paramMessageExchangeImpl)
			throws JBIException;

	protected void doRouting(MessageExchangeImpl me) throws MessagingException {
		ComponentNameSpace id = (me.getRole() == MessageExchange.Role.PROVIDER) ? me
				.getDestinationId() : me.getSourceId();

		ComponentMBeanImpl lcc = this.router.getContainer().getRegistry()
				.getComponent(id.getName());

		if (lcc != null) {
			if (lcc.getDeliveryChannel() != null) {
				try {
					this.lock.readLock().lock();
					((DeliveryChannelImpl) lcc.getDeliveryChannel())
							.processInBound(me);
				} finally {
					this.lock.readLock().unlock();
				}
				return;
			}
			throw new MessagingException("Component " + id.getName()
					+ " is shut down");
		}

		throw new MessagingException("No component named " + id.getName()
				+ " - Couldn't route MessageExchange " + me);
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(), "description",
				"The type of flow");
		return AttributeInfoHelper.join(super.getAttributeInfos(),
				helper.getAttributeInfos());
	}

	protected boolean isPersistent(MessageExchange me) {
		ExchangePacket packet = ((MessageExchangeImpl) me).getPacket();
		if (packet.getPersistent() != null) {
			return packet.getPersistent().booleanValue();
		}
		return this.router.getContainer().isPersistent();
	}

	protected boolean isTransacted(MessageExchange me) {
		return (me.getProperty("javax.jbi.transaction.jta") != null);
	}

	protected boolean isSynchronous(MessageExchange me) {
		Boolean sync = (Boolean) me.getProperty("javax.jbi.messaging.sendSync");
		return ((sync != null) && (sync.booleanValue()));
	}

	protected boolean isClustered(MessageExchange me) {
		MessageExchangeImpl mei = (MessageExchangeImpl) me;
		if (mei.getDestinationId() == null) {
			ServiceEndpoint se = me.getEndpoint();
			if (se instanceof InternalEndpoint) {
				return ((InternalEndpoint) se).isClustered();
			}

			return false;
		}

		String destination = mei.getDestinationId().getContainerName();
		String source = mei.getSourceId().getContainerName();
		return (!(source.equals(destination)));
	}

	public Router getRouter() {
		return this.router;
	}

	public String getType() {
		return "Dispatcher";
	}

	public String getName() {
		if (this.name == null) {
			String n = super.getName();
			if (n.endsWith("Dispatcher")) {
				n = n.substring(0, n.length() - 4);
			}
			return n;
		}
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ExecutorFactory getExecutorFactory() {
		return this.executorFactory;
	}
}