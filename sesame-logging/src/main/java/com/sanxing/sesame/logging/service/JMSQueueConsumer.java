package com.sanxing.sesame.logging.service;

import com.sanxing.sesame.logging.processer.Processor;
import com.sanxing.sesame.logging.processer.ProcessorFactory;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMSQueueConsumer implements MessageListener {
	private static final Logger LOG = LoggerFactory.getLogger(JMSQueueConsumer.class);

	static AtomicLong total = new AtomicLong(0L);

	public void onMessage(Message message) {
		if (total.addAndGet(1L) % 1000L == 0L)
			LOG.info("" + total.get());
		try {
			LOG.debug("Received: '" + message + "'");
			if (message instanceof ObjectMessage) {
				ObjectMessage objMsg = (ObjectMessage) message;
				Serializable obj = objMsg.getObject();
				if (LOG.isDebugEnabled())
					LOG.debug("Received object: " + obj);
				Processor processor = ProcessorFactory.getInstance().produce(
						obj);
				if (processor != null) {
					processor.process(obj);
					return;
				}
			}
			LOG.debug("Received: '" + message + "'");
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}