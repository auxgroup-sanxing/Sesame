package com.sanxing.sesame.binding.assist;

import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.binding.transport.Connector;
import java.io.IOException;

public abstract class DuplexTransport extends BaseTransport implements
		Acceptor, Connector {
	public void reply(MessageContext channel) throws IOException {
	}

	public void sendOut(MessageContext context) throws IOException {
	}
}