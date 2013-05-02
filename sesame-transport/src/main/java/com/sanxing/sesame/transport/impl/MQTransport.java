package com.sanxing.sesame.transport.impl;

import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;
import java.io.IOException;
import java.util.Map;

public class MQTransport extends BaseTransport implements Acceptor, Connector {
	protected void setProperties(Map<?, ?> properties)
			throws IllegalArgumentException {
	}

	public void reply(MessageContext context) throws IOException {
	}

	public void close() throws IOException {
	}

	public String getCharacterEncoding() {
		return null;
	}

	public boolean isActive() {
		return false;
	}

	public void open() throws IOException {
	}

	public void sendOut(MessageContext context) throws IOException {
	}
}