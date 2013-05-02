package com.sanxing.sesame.core.listener;

import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.core.BaseServer;

public abstract interface ClusterListener {
	public abstract void setServer(BaseServer paramBaseServer);

	public abstract void listen(ClusterEvent paramClusterEvent);
}