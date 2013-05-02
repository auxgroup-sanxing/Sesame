package com.sanxing.sesame.dispatch;

import com.sanxing.sesame.router.Router;
import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.messaging.MessageExchange;

public abstract interface Dispatcher extends LifeCycleMBean {
	public abstract void init(Router paramRouter) throws JBIException;

	public abstract String getDescription();

	public abstract String getName();

	public abstract void send(MessageExchange paramMessageExchange)
			throws JBIException;

	public abstract void suspend();

	public abstract void resume();

	public abstract Router getRouter();

	public abstract boolean canHandle(MessageExchange paramMessageExchange);
}