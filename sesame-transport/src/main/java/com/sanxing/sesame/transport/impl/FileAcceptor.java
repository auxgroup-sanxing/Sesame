package com.sanxing.sesame.transport.impl;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.transport.quartz.SesameScheduler;
import com.sanxing.sesame.transport.util.SesameFileClient;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileAcceptor extends TaskTransport {
	private static Logger LOG = LoggerFactory.getLogger(FileAcceptor.class);

	protected void write(OutputStream output, byte[] bytes, int length)
			throws IOException {
	}

	public String getCharacterEncoding() {
		return "utf-8";
	}

	public String getDescription() {
		return "mail transport";
	}

	public void reply(MessageContext context) throws IOException {
		String path = context.getPath();
		Map props = (Map) this.bindingPropsCache.get(path);
		SesameFileClient fileClient = new SesameFileClient();
		fileClient.setBindingProperties(props);
		BinaryResult result = (BinaryResult) context.getResult();
		byte[] resp = result.getBytes();
		LOG.debug("get response is:" + new String(resp));
		try {
			if (resp != null)
				fileClient.writeOneFile(resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		try {
			this.scheduler.shutdown();
			this.workExecutor.shutdown();
			this.active = false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public void open() throws IOException {
		try {
			this.active = true;
			this.scheduler = new SesameScheduler();
			this.scheduler.init();
			this.scheduler.start();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public void executeTask(Map<?, ?> properties) throws Exception {
		SesameFileClient fileClient = new SesameFileClient();
		fileClient.setBindingProperties(properties);
		File[] files = fileClient.getFiles();

		if (files != null)
			for (File file : files)
				if (fileClient.bakFile(file)) {
					InputStream in = fileClient.readOneFileWithName(file
							.getName());
					accept(in, file.getName(),
							(String) properties.get("jobName"));
				} else {
					LOG.error("move file:[" + file.getName() + "] error!");
				}
	}
}