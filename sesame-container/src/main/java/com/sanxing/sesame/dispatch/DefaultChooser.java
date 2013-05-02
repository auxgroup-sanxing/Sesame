package com.sanxing.sesame.dispatch;

import com.sanxing.sesame.messaging.ExchangePacket;
import com.sanxing.sesame.messaging.MessageExchangeImpl;
import com.sanxing.sesame.core.Env;
import com.sanxing.sesame.core.Platform;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChooser implements DispatcherChooser {
	public static final String DEFAULT_DISPATCHER_NAME = "cluster";
	private static Logger LOG = LoggerFactory.getLogger(DefaultChooser.class);

	public Dispatcher chooseDispatcher(Dispatcher[] dispatchers,
			MessageExchange exchange) throws MessagingException {
		String dispatcher = (String) exchange
				.getProperty("com.sanxing.sesame.dispatch");
		LOG.debug("dispatcher in message exchange :" + dispatcher);
		if (dispatcher != null) {
			Dispatcher found = null;
			for (int i = 0; i < dispatchers.length; ++i) {
				if (dispatchers[i].getName().equalsIgnoreCase(dispatcher)) {
					found = dispatchers[i];
					break;
				}
			}
			if (found == null) {
				throw new MessagingException("Dispatcher '" + dispatcher
						+ "' was specified but not found");
			}
			if (found.canHandle(exchange)) {
				return found;
			}

			throw new MessagingException("Dispatcher '" + dispatcher
					+ "' was specified but not able to handle exchange");
		}

		if (Platform.getEnv().isClustered()) {
			LOG.debug("in cluster env..............");
			for (Dispatcher disp : dispatchers) {
				LOG.debug("matching dispatcher " + disp.getName());
				if (disp.getName().equals("cluster"))
					return disp;
			}
		} else {
			LOG.debug("in dev mode.......");
			for (Dispatcher disp : dispatchers) {
				LOG.debug("matching dispatcher " + disp.getName());
				if (disp.getName().equals("straight")) {
					return disp;
				}
			}

		}

		for (int i = 0; i < dispatchers.length; ++i) {
			if (dispatchers[i].canHandle(exchange)) {
				((MessageExchangeImpl) exchange).getPacket().setProperty(
						"com.sanxing.sesame.dispatch",
						dispatchers[i].getName());
				return dispatchers[i];
			}
		}
		return null;
	}
}