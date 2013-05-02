package com.sanxing.sesame.exception;

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

public class FaultException extends MessagingException {
	private static final long serialVersionUID = -7984115255357383751L;
	private final MessageExchange exchange;
	private final Fault fault;

	public FaultException(String text, MessageExchange exchange, Fault fault) {
		super(text);
		this.exchange = exchange;
		this.fault = fault;
	}

	public static FaultException newInstance(MessageExchange exchange)
			throws NoFaultAvailableException {
		Fault fault = exchange.getFault();
		if (fault == null) {
			throw new NoFaultAvailableException(exchange);
		}
		return new FaultException("Fault occurred invoking server: " + fault,
				exchange, fault);
	}

	public MessageExchange getExchange() {
		return this.exchange;
	}

	public Fault getFault() {
		return this.fault;
	}
}