package com.sanxing.sesame.dispatch;

import com.sanxing.sesame.executors.ExecutorFactory;
import com.sanxing.sesame.messaging.MessageExchangeImpl;
import com.sanxing.sesame.servicedesc.AbstractEndpoint;
import java.util.concurrent.Executor;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessagingException;

public class StraightThrough extends AbstractDispatcher {
	public static final String name = "straight";
	private Executor executor;

	public StraightThrough() {
		this.executor = ExecutorFactory.getFactory().createExecutor(
				"dispatcher");
	}

	protected void doSend(final MessageExchangeImpl me)
			throws MessagingException {
		if (me.getDestinationId() == null) {
			me.setDestinationId(((AbstractEndpoint) me.getEndpoint())
					.getComponentNameSpace());
		}
		if (me.getRole() == MessageExchange.Role.PROVIDER) {
			if ((me.getProperty("sesame.exchange.thread.switch") != null)
					&& (((Boolean) me
							.getProperty("sesame.exchange.thread.switch"))
							.booleanValue())) {
				this.executor.execute(new Runnable() {
					public void run() {
						try {
							StraightThrough.this.doRouting(me);
						} catch (MessagingException e) {
							me.setError(e);
						}
					}
				});
			} else {
				doRouting(me);
			}
		} else
			doRouting(me);
	}

	public String getName() {
		return "straight";
	}

	public String getDescription() {
		return "Straight throutgh dispatcher";
	}

	public boolean canHandle(MessageExchange me) {
		if (isPersistent(me)) {
			return false;
		}
		if (isClustered(me)) {
			return false;
		}

		return (!(isTransacted(me)));
	}
}