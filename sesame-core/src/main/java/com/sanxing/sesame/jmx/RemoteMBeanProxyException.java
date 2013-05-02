package com.sanxing.sesame.jmx;

import javax.management.JMRuntimeException;

public class RemoteMBeanProxyException extends JMRuntimeException {
	private static final long serialVersionUID = -3979682757808179294L;
	private final Exception exception;

	public RemoteMBeanProxyException() {
		this(null, null);
	}

	public RemoteMBeanProxyException(String message) {
		this(message, null);
	}

	public RemoteMBeanProxyException(Exception exception) {
		this(null, exception);
	}

	public RemoteMBeanProxyException(String message, Exception exception) {
		super(message);
		this.exception = exception;
	}

	public Throwable getCause() {
		return this.exception;
	}
}