package com.sanxing.sesame.binding.assist;

import com.sanxing.sesame.binding.BindingException;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.context.MessageContext.Mode;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.logging.BufferRecord;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import com.sanxing.sesame.logging.PerfRecord;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.jbi.messaging.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BlockedAcceptor extends BaseTransport implements
		Acceptor, Runnable {
	public static final int DEFAULT_BUFFER_CAPACITY = 4096;
	private static Logger LOG = LoggerFactory.getLogger(BlockedAcceptor.class);
	private Thread acceptThread;
	private Executor executor;
	private boolean active = false;

	protected Map<Object, PipeLine> pipelines = new Hashtable();

	protected abstract PipeLine accept() throws IOException;

	protected abstract byte[] allocate(int paramInt);

	protected abstract int getBufferSize();

	public synchronized void open() throws IOException {
		this.active = true;
		this.executor = ExecutorFactory.getFactory().createExecutor(
				"transports.BlockAcceptor");
		this.acceptThread = new Thread(this);
		this.acceptThread.start();
	}

	public synchronized void close() throws IOException {
		this.active = false;
		try {
			if (this.acceptThread != null)
				this.acceptThread.interrupt();
		} finally {
			this.acceptThread = null;
			this.executor = null;
		}
	}

	protected BinaryResult doSend(String contextPath, BinarySource source)
			throws MessagingException, BindingException {
		BinaryResult result = new BinaryResult();
		result.setEncoding(getCharacterEncoding());
		MessageContext message = new MessageContext(this, source);
		message.setResult(result);
		message.setPath(contextPath);
		message.setMode(MessageContext.Mode.BLOCK);

		postMessage(message);

		return result;
	}

	public void run() {
		while (isActive())
			try {
				PipeLine pipeline = accept();
				this.executor.execute(new BlockedWorker(this, pipeline));
			} catch (IOException e) {
				if (this.active)
					LOG.error(e.getMessage(), e);
			}
	}

	public boolean isActive() {
		return this.active;
	}

	public void reply(MessageContext context) throws IOException {
		Log log = LogFactory.getLog("sesame.binding");

		if (context.getResult() instanceof BinaryResult) {
			BinaryResult result = (BinaryResult) context.getResult();
			BufferRecord rec = new BufferRecord(
					context.getSerial().longValue(), result.getBytes());

			log.info(
					"[REPLY][BINARY]-----------------------------------------",
					rec);
		}

		PipeLine pipeline = (PipeLine) this.pipelines.get(context.getSerial());
		if (pipeline != null) {
			this.pipelines.remove(context.getSerial());
			try {
				BinaryResult result = (BinaryResult) context.getResult();
				byte[] bytes = result.getBytes();
				if (context.getException() != null) {
					LOG.error(context.getException().getMessage(),
							context.getException());
				}
				pipeline.write(bytes, 0, bytes.length);
			} finally {
				pipeline.close();
			}
		}

		Long startTime = (Long) context.getProperty("sendTime");

		Log sensor = LogFactory.getLog("sesame.system.sensor");
		PerfRecord rec = new PerfRecord();
		rec.setSerial(context.getSerial().longValue());
		rec.setElapsedTime(System.currentTimeMillis() - startTime.longValue());
		sensor.info(
				"--------------------------------------------------------------------total time--",
				rec);
	}
}