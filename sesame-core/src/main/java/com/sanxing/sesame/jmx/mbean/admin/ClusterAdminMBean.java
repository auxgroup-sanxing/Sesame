package com.sanxing.sesame.jmx.mbean.admin;

import java.util.List;

public interface ClusterAdminMBean
{
    public abstract void addServer( ServerInfo server );

    public abstract void removeServer( ServerInfo server );

    public abstract void upateServer( ServerInfo server );

    public abstract ServerInfo updateState( String serverName, int status );

    public abstract List<ServerInfo> getAllServer();

    public abstract void notifyNeighbors( ClusterEvent event );

    public abstract void fireEvent( ClusterEvent event, String serverName );

    public abstract List<ServerInfo> heartBeat( String serverName );

    public abstract ServerInfo getServerInfoByName( String name );
}