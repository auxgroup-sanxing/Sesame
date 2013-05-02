package com.sanxing.sesame.logging.service;

import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.logging.util.JNDIUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMSTopicConsumer implements MessageListener, ExceptionListener,
		Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(JMSTopicConsumer.class);
	private Session jmsSession;
	private Connection jmsConnection;
	private MessageConsumer topicConsumer = null;

	private List<MessageConsumer> queueConsumers = new ArrayList();
	private int queueConsumerNum;
	private JMSQueueConsumer consumer = null;

	private MessageProducer queueProducer = null;
	private Session queueSession;
	private ObjectMessage queueMessage = null;

	static AtomicLong total = new AtomicLong(0L);

	public void run() {
		prepare();
		try {
			getConsumer().setMessageListener(this);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public MessageConsumer getConsumer() {
		return this.topicConsumer;
	}

	public void setConsumer(MessageConsumer consumer) {
		this.topicConsumer = consumer;
	}

	public JMSTopicConsumer() {
		this.consumer = new JMSQueueConsumer();
	}

	protected Object lookup(Context ctx, String name) throws NamingException {
		try {
			return ctx.lookup(name);
		} catch (NameNotFoundException e) {
			LOG.error("Could not find name [" + name + "].");
			throw e;
		}
	}

	public void prepare() {
		try {
			Context jndi = JNDIUtil.getInitialContext();
			String cfname = "admin-QC";
			ConnectionFactory connFactory = (ConnectionFactory) lookup(jndi,
					cfname);
			this.jmsConnection = connFactory.createConnection();
			String clientID = "client-receiver";
			this.jmsConnection.setClientID(clientID);
			this.jmsSession = this.jmsConnection.createSession(false, 1);
			String topicName = System.getProperty(
					"sesame.logging.monitor.jms.name", "LOGTOPIC");
			Topic topic = this.jmsSession.createTopic(topicName);

			this.topicConsumer = this.jmsSession.createConsumer(topic);
			setConsumer(this.topicConsumer);

			String consumers = System.getProperty(
					"sesame.logging.monitor.consumers", "5");
			this.queueConsumerNum = Integer.parseInt(consumers);

			Queue queue = this.jmsSession.createQueue("INNERQUEUE");
			for (int i = 0; i < this.queueConsumerNum; ++i) {
				Session session = this.jmsConnection.createSession(false, 1);
				MessageConsumer queueConsumer = session.createConsumer(queue);
				this.queueConsumers.add(queueConsumer);
			}
			for (int i = 0; i < this.queueConsumerNum; ++i) {
				((MessageConsumer) this.queueConsumers.get(i))
						.setMessageListener(this.consumer);
			}

			this.jmsConnection.start();
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public void onMessage(Message message) {
		try {
			LOG.debug("Received: '" + message + "'");

			this.queueSession = this.jmsConnection.createSession(false, 1);
			Queue queue = this.queueSession.createQueue("INNERQUEUE");
			this.queueProducer = this.queueSession.createProducer(queue);
			this.queueProducer.setDeliveryMode(2);
			this.queueMessage = this.queueSession.createObjectMessage();

			if (message instanceof ObjectMessage) {
				ObjectMessage objMsg = (ObjectMessage) message;
				Serializable obj = objMsg.getObject();
				if (LOG.isDebugEnabled())
					LOG.debug("Received object: " + obj);
				this.queueMessage.clearBody();
				this.queueMessage.setObject(obj);
				this.queueProducer.send(this.queueMessage);
				if (total.addAndGet(1L) % 1000L == 0L)
					LOG.info("consumer " + total.get());
			} else {
				LOG.debug("Received: '" + message + "'");
			}

			this.queueSession.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onException(JMSException arg0) {
	}

	public static void main(String[] argvs) {
		Executor executor = ExecutorFactory.getFactory().createExecutor(
				"services.registry");
		JMSTopicConsumer receiver = new JMSTopicConsumer();
		executor.execute(receiver);
	}
}