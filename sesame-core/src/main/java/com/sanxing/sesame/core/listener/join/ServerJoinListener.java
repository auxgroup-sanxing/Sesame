package com.sanxing.sesame.core.listener.join;

import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.listener.ClusterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerJoinListener implements ClusterListener {
	private static Logger logger = LoggerFactory.getLogger(ServerJoinListener.class);
	private BaseServer server;

	public void setServer(BaseServer server) {
		this.server = server;
	}

	public void listen(ClusterEvent event) {
	}
}