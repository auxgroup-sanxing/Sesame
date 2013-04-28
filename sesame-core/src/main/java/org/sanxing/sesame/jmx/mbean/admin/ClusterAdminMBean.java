package org.sanxing.sesame.jmx.mbean.admin;

import java.util.List;

public abstract interface ClusterAdminMBean {
	public abstract void addServer(ServerInfo paramServerInfo);

	public abstract void removeServer(ServerInfo paramServerInfo);

	public abstract void upateServer(ServerInfo paramServerInfo);

	public abstract ServerInfo updateState(String paramString, int paramInt);

	public abstract List<ServerInfo> getAllServer();

	public abstract void notifyNeighbors(ClusterEvent paramClusterEvent);

	public abstract void fireEvent(ClusterEvent paramClusterEvent,
			String paramString);

	public abstract List<ServerInfo> heartBeat(String paramString);

	public abstract ServerInfo getServerInfoByName(String paramString);
}