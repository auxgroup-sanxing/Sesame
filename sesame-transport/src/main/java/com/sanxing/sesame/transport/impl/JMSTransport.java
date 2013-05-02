package com.sanxing.sesame.transport.impl;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;
import com.sanxing.sesame.executors.ExecutorFactory;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.jbi.messaging.MessagingException;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.naming.directory.InitialDirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMSTransport extends BaseTransport implements Acceptor, Connector,
		MessageListener, ExceptionListener {
	private static Logger LOG = LoggerFactory.getLogger(JMSTransport.class);

	private boolean active = false;
	private Executor executor;
	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session sendSession;
	private Session recvSession;
	private Destination requestQ;
	private Destination responseQ;
	private MessageProducer producer;
	private MessageConsumer receiver;
	private String initialContextFactoryName;
	private String providerURL;
	private String connectionFactoryBindingName;
	private String requestQBindingName;
	private String responseQBindingName;

	private void init() throws Exception {
		Hashtable env = new Hashtable();
		env.put("java.naming.factory.initial", this.initialContextFactoryName);
		env.put("java.naming.provider.url", this.providerURL);
		InitialDirContext ctx = new InitialDirContext(env);

		this.connectionFactory = ((ConnectionFactory) ctx
				.lookup(this.connectionFactoryBindingName));
		this.requestQ = ((Destination) ctx.lookup(this.requestQBindingName));
		this.responseQ = ((Destination) ctx.lookup(this.responseQBindingName));
	}

	protected void setProperties(Map<?, ?> properties)
			throws IllegalArgumentException {
		this.initialContextFactoryName = getProperty(
				"initialContextFactoryName", properties);
		this.providerURL = getProperty("providerURL", properties);
		this.connectionFactoryBindingName = getProperty(
				"connectionFactoryBindingName", properties);
		this.requestQBindingName = getProperty("requestQBindingName",
				properties);
		this.responseQBindingName = getProperty("responseQBindingName",
				properties);
	}

	public String getProperty(String key, Map<?, ?> properties) {
		return ((String) properties.get(key));
	}

	public void reply(MessageContext context) throws IOException {
		try {
			if (context == null) {
				TextMessage message = this.sendSession.createTextMessage();
				message.setText("receive acknowledge");
				this.producer.send(message);
				return;
			}
			BinaryResult result = (BinaryResult) context.getResult();
			byte[] bytes = result.getBytes();
			LOG.debug("response is:" + new String(bytes));
			StreamMessage respMsg = this.sendSession.createStreamMessage();
			respMsg.writeBytes(bytes);
			this.producer.send(respMsg);
		} catch (JMSException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public void close() throws IOException {
		this.active = false;
		try {
			this.connection.close();
		} catch (JMSException e) {
			throw new IOException(e.getMessage());
		} finally {
			this.connection = null;
			this.executor = null;
		}
	}

	public String getCharacterEncoding() {
		return "utf-8";
	}

	public boolean isActive() {
		return this.active;
	}

	public void open() throws IOException {
		try {
			this.executor = ExecutorFactory.getFactory().createExecutor(
					"transports.jms");

			init();

			this.connection = this.connectionFactory.createConnection();
			this.connection.setExceptionListener(this);

			this.sendSession = this.connection.createSession(false, 1);
			this.recvSession = this.connection.createSession(false, 1);

			this.producer = this.sendSession.createProducer(this.responseQ);
			this.receiver = this.recvSession.createConsumer(this.requestQ);

			this.receiver.setMessageListener(this);

			this.connection.start();

			this.active = true;
		} catch (JMSException e) {
			Exception linkedE = e.getLinkedException();
			if (linkedE != null)
				linkedE.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	public void onMessage(Message msg) {
		try {
			if (msg instanceof TextMessage) {
				LOG.debug("get text msg is:" + ((TextMessage) msg).getText());

				return;
			}

			if (msg instanceof BytesMessage) {
				BytesMessage obj = (BytesMessage) msg;
				byte[] getMsg = new byte[1024];
				int len = obj.readBytes(getMsg);
				this.executor.execute(new Command(getMsg, len));
				return;
			}
			if (msg instanceof StreamMessage) {
				StreamMessage obj = (StreamMessage) msg;

				byte[] getMsg = new byte[1024];
				int len = obj.readBytes(getMsg);
				this.executor.execute(new Command(getMsg, len));
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public void sendOut(MessageContext context) throws IOException {
		LOG.debug("sendout..............................");
	}

	public void onException(JMSException e) {
		LOG.debug(e.getMessage());
		Exception linkedE = e.getLinkedException();
		if (linkedE != null)
			LOG.debug(linkedE.getMessage());
		this.sendSession = null;
		this.recvSession = null;
		this.producer = null;
		this.receiver = null;
		try {
			do {
				try {
					Thread.sleep(300000L);
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
				this.sendSession = this.connection.createSession(false, 1);
				this.recvSession = this.connection.createSession(false, 1);

				this.producer = this.sendSession.createProducer(this.responseQ);
				this.receiver = this.recvSession.createConsumer(this.requestQ);

				this.receiver.setMessageListener(this);
				if (this.sendSession != null)
					return;
			} while (this.active);
		} catch (JMSException e1) {
			LOG.debug(e.getMessage());
			Exception el = e.getLinkedException();
			if (el != null)
				LOG.debug(el.getStackTrace().toString());
		}
	}

	private class Command implements Runnable {
		byte[] getMsg;
		int len;

		Command(byte[] paramArrayOfByte, int paramInt) {
			this.getMsg = paramArrayOfByte;
			this.len = paramInt;
		}

		public void run() {
			BinarySource input = new BinarySource();
			input.setEncoding(JMSTransport.this.getCharacterEncoding());
			MessageContext ctx = new MessageContext(JMSTransport.this, input);
			try {
				ctx.setPath("/");
				ctx.setAction("trancode");
				input.setBytes(this.getMsg, this.len);

				JMSTransport.this.postMessage(ctx);
				JMSTransport.this.reply(ctx);
			} catch (MessagingException e) {
				JMSTransport.LOG.error(e.getMessage(), e);
				ctx.setException(e);
				try {
					JMSTransport.this.reply(ctx);
				} catch (IOException ex) {
					JMSTransport.LOG.error(ex.getMessage(), ex);
				}
			} catch (BindingException e) {
				JMSTransport.LOG.error(e.getMessage(), e);
				ctx.setException(e);
				try {
					JMSTransport.this.reply(ctx);
				} catch (IOException ex) {
					JMSTransport.LOG.error(ex.getMessage(), ex);
				}
			} catch (IOException e) {
				JMSTransport.LOG.error(e.getMessage(), e);
			}
		}
	}
}