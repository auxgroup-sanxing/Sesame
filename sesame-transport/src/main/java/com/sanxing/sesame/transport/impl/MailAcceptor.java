package com.sanxing.sesame.transport.impl;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.transport.quartz.SesameScheduler;
import com.sanxing.sesame.transport.util.SesameMailClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import javax.mail.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailAcceptor extends TaskTransport {
	private static Logger LOG = LoggerFactory.getLogger(MailAcceptor.class);
	private SesameMailClient mailClient;

	public String getCharacterEncoding() {
		return "utf-8";
	}

	public String getDescription() {
		return "mail transport";
	}

	public void reply(MessageContext context) throws IOException {
		BinaryResult result = (BinaryResult) context.getResult();
		byte[] resp = result.getBytes();
		try {
			LOG.debug("reply get message is:::::::::::::::::["
					+ new String(resp) + "]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		try {
			this.scheduler.shutdown();
			this.workExecutor.shutdown();
			this.mailClient = null;
			this.active = false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public void open() throws IOException {
		try {
			this.active = true;
			this.mailClient = new SesameMailClient();
			this.mailClient.setProperties(this.properties);

			this.mailClient.initPOPServer();

			this.scheduler = new SesameScheduler();
			this.scheduler.init();
			this.scheduler.start();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public void executeTask(Map<?, ?> properties) throws Exception {
		Message[] msgs = this.mailClient.getAllMails();
		for (Message msg : msgs) {
			byte[] contentBuf = this.mailClient.processOneMail(msg);

			if (contentBuf == null)
				return;
			InputStream in = new ByteArrayInputStream(contentBuf);

			String operationName = msg.getSubject();
			accept(in, operationName, (String) properties.get("jobName"));
		}
		this.mailClient.disconnecteStore();
	}
}