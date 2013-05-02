package com.sanxing.sesame.transport.impl;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.transport.quartz.SesameScheduler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.SchedulerException;

public class AutoAcceptor extends TaskTransport {
	private static Logger LOG = LoggerFactory.getLogger(AutoAcceptor.class);

	public void executeTask(Map<?, ?> properties) throws Exception {
		LOG.debug("jobName is:" + properties.get("jobName"));
		accept(new ByteArrayInputStream("".getBytes()), "getfile",
				(String) properties.get("jobName"));
	}

	public void reply(MessageContext context) throws BindingException,
			IOException {
	}

	public void close() throws IOException {
		try {
			this.scheduler.shutdown();
			this.workExecutor.shutdown();
			this.active = false;
		} catch (SchedulerException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public String getCharacterEncoding() {
		return this.encoding;
	}

	public void open() throws IOException {
		try {
			this.active = true;

			this.scheduler = new SesameScheduler();
			this.scheduler.init();
			this.scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
}