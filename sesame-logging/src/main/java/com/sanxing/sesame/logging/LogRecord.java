package com.sanxing.sesame.logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LogRecord extends Throwable {
	private static final long serialVersionUID = -3144056887643453625L;
	private static AtomicLong globalSequenceNumber = new AtomicLong(0L);

	private static AtomicInteger nextThreadId = new AtomicInteger(10);
	private static final long defaultExpireInterval = 3600000L;
	private long sequenceNumber;
	private int threadID;
	private Object content;
	private Date timestamp;
	private String serviceName;
	private String operationName;
	private String channel;
	private String action;
	private String stage;
	private long expireTime;
	private static ThreadLocal<Object> threadIds = new ThreadLocal();

	public String getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getOperationName() {
		return this.operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getChannel() {
		return this.channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public long getExpireTime() {
		return this.expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public String getStage() {
		return this.stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public LogRecord() {
		initialize();
	}

	protected LogRecord(Throwable throwable) {
		super(throwable);
		initialize();
	}

	private void initialize() {
		this.timestamp = new Date();

		this.sequenceNumber = globalSequenceNumber.incrementAndGet();
		Integer id = (Integer) threadIds.get();
		if (id == null) {
			id = new Integer(nextThreadId.incrementAndGet());
			threadIds.set(id);
		}
		this.threadID = id.intValue();
		this.expireTime = (System.currentTimeMillis() + 3600000L);
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getThreadID() {
		return this.threadID;
	}

	public void setSerial(long serial) {
		this.sequenceNumber = serial;
	}

	public long getSerial() {
		return this.sequenceNumber;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return this.action;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public Object getContent() {
		return this.content;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{");
		buf.append(" serial: " + getSerial());
		if (this.action != null) {
			buf.append(", action: '" + this.action + "'");
		}
		buf.append(" content: \"");
		buf.append(this.content);
		buf.append("\" ");
		buf.append("}");
		return buf.toString();
	}

	public void printStackTrace() {
	}

	public void printStackTrace(PrintStream s) {
		s.println(toString());
	}

	public void printStackTrace(PrintWriter writer) {
		writer.println(toString());
	}
}