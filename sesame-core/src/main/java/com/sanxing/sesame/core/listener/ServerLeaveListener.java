package com.sanxing.sesame.core.listener;

import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.core.AdminServer;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.event.ServerLeaveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLeaveListener implements ClusterListener {
	private static Logger LOG = LoggerFactory.getLogger(ServerLeaveListener.class);
	private BaseServer server;

	public void setServer(BaseServer server) {
		this.server = server;
	}

	public void listen(ClusterEvent event) {
		if (event instanceof ServerLeaveEvent) {
			if (LOG.isDebugEnabled())
				LOG.debug("Server [" + event.getEventSource() + "] leaved");
			if (this.server.isAdmin()) {
				String closedServer = event.getEventSource();
				((AdminServer) this.server).closeJMXConnector(closedServer);
			}
		}
	}
}