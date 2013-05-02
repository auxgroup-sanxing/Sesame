package com.sanxing.sesame.binding.context;

import com.sanxing.sesame.binding.Binding;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.Connector;
import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.serial.SerialGenerator;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

public class MessageContext {
	private Source input;
	private Result output;
	private Exception exception;
	private boolean closed;
	private Transport transport;
	private long serial;
	private String path;
	private String action;
	private Binding binding;
	private Status status;
	private Mode mode;
	private boolean accepted;
	private Map<String, Object> properties;
	private long timeout;

	public MessageContext(Acceptor acceptor, Source input) {
		this.serial = SerialGenerator.getSerial();
		setTransport(acceptor);
		setAccepted(true);
		this.input = input;
	}

	public MessageContext(Connector connector, Result output) {
		setTransport(connector);
		setAccepted(false);
		this.output = output;
	}

	protected MessageContext() {
		this.closed = false;

		this.path = "";
		this.action = null;

		this.status = Status.OK;
		this.mode = Mode.BLOCK;

		this.properties = new Hashtable();

		this.path = "";
	}

	public void setSerial(long serial) throws IllegalAccessException {
		if (isAccepted()) {
			throw new IllegalAccessException("Serial number has been set");
		}
		this.serial = serial;
	}

	public Long getSerial() {
		return Long.valueOf(this.serial);
	}

	public void setSource(Source input) {
		this.input = input;
	}

	public Source getSource() {
		return this.input;
	}

	public void setResult(Result output) {
		this.output = output;
	}

	public Result getResult() {
		return this.output;
	}

	public Exception getException() {
		return this.exception;
	}

	public void close() throws IOException {
		this.closed = true;
	}

	public boolean isClosed() {
		return this.closed;
	}

	public Transport getTransport() {
		return this.transport;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAction() {
		return this.action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isAccepted() {
		return this.accepted;
	}

	protected void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public Status getStatus() {
		return this.status;
	}

	public Mode getMode() {
		return this.mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public long getTimeout() {
		return this.timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public Binding getBinding() {
		return this.binding;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}

	public Object getProperty(String name) {
		return this.properties.get(name);
	}

	public void setProperty(String name, Object value) {
		this.properties.put(name, value);
	}

	public Set<String> getPropertyNames() {
		return this.properties.keySet();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{ class: '" + super.getClass().getSimpleName() + "'");
		buf.append(", serial: " + this.serial);
		buf.append(", action: "
				+ ((this.action != null) ? "'" + this.action + "'" : "null"));
		buf.append(", contextPath: '" + this.path + "'");
		buf.append(", mode: '" + this.mode + "'");
		if (this.mode == Mode.BLOCK) {
			buf.append(", timeout: " + this.timeout + "ms");
		}
		buf.append(", status: '" + this.status + "'");
		if (this.input != null) {
			buf.append(", input: " + this.input);
		}
		if (this.output != null) {
			buf.append(", output: " + this.output);
		}
		buf.append(" }");
		return buf.toString();
	}

	public static class Mode {
		public static final Mode BLOCK = new Mode("block");

		public static final Mode NON_BLOCK = new Mode("non-block");
		private String code;

		private Mode(String code) {
			this.code = code;
		}

		public String toString() {
			return this.code;
		}
	}

	public static class Status {
		public static final Status OK = new Status("ok");
		public static final Status FAULT = new Status("fault");
		private String code;

		private Status(String code) {
			this.code = code;
		}

		public String toString() {
			return this.code;
		}
	}
}