package com.sanxing.sesame.jmx.mbean.admin;

import java.io.Serializable;

public class ClusterEvent
    implements Serializable
{
    private static final long serialVersionUID = 6903059233404105768L;

    private String eventSource;

    private Serializable eventObj;

    public String getEventSource()
    {
        return eventSource;
    }

    public void setEventSource( String eventSource )
    {
        this.eventSource = eventSource;
    }

    public Serializable getEventObject()
    {
        return eventObj;
    }

    public void setEventObject( Serializable eventObject )
    {
        eventObj = eventObject;
    }

    @Override
    public String toString()
    {
        return "ClusterEvent [eventObj=" + eventObj + ", eventSource=" + eventSource + ", eventType="
            + super.getClass().getName() + "]";
    }
}