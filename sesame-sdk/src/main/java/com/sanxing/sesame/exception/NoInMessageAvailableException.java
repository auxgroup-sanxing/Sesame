package com.sanxing.sesame.exception;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

public class NoInMessageAvailableException extends MessagingException {
	private static final long serialVersionUID = 1626185014415255323L;
	private final MessageExchange messageExchange;

	public NoInMessageAvailableException(MessageExchange messageExchange) {
		super("No in message available for message exchange: "
				+ messageExchange);
		this.messageExchange = messageExchange;
	}

	public MessageExchange getMessageExchange() {
		return this.messageExchange;
	}
}