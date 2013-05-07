package com.sanxing.sesame.core.event;

import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;

public class ServerJoinEvent
    extends ClusterEvent
{
    private static final long serialVersionUID = 2607059514664405841L;

    @Override
    public String toString()
    {
        return "ServerJoinEvent : [server " + getEventSource() + " joined cluster]";
    }
}