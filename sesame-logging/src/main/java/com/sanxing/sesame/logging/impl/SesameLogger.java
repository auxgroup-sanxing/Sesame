package com.sanxing.sesame.logging.impl;

import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogRecord;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameLogger implements Log, Serializable {
	private static final long serialVersionUID = 1888531288057908895L;

	private transient Logger logger = null;

	private String name = null;

	public SesameLogger(String name) {
		this.name = name;
		this.logger = getLogger();
	}

	public Logger getLogger() {
		if (this.logger == null) {
			this.logger = LoggerFactory.getLogger(this.name);
		}

		return this.logger;
	}

	public void debug(Object message) {
		getLogger().debug(message.toString());
	}

	public void debug(Object message, LogRecord lr) {
		getLogger().debug(message.toString(), lr);
	}

	public void error(Object message) {
		getLogger().error(message.toString());
	}

	public void error(Object message, LogRecord lr) {
		getLogger().error(message.toString(), lr);
	}

	public void fatal(Object message) {
		getLogger().error(message.toString());
	}

	public void fatal(Object message, LogRecord lr) {
		getLogger().error(message.toString(), lr);
	}

	public void info(Object message) {
		getLogger().info(message.toString());
	}

	public void info(Object message, LogRecord lr) {
		getLogger().info(message.toString(), lr);
	}

	public void trace(Object message) {
		getLogger().trace(message.toString());
	}

	public void trace(Object message, LogRecord lr) {
		getLogger().trace(message.toString(), lr);
	}

	public void warn(Object message) {
		getLogger().warn(message.toString());
	}

	public void warn(Object message, LogRecord lr) {
		getLogger().warn(message.toString(), lr);
	}

	public boolean isDebugEnabled() {
		return getLogger().isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return getLogger().isInfoEnabled();
	}

	public boolean isTraceEnabled() {
		return getLogger().isTraceEnabled();
	}
}