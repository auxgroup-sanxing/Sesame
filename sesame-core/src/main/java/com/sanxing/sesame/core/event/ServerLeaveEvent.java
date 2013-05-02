package com.sanxing.sesame.core.event;

import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;

public class ServerLeaveEvent extends ClusterEvent {
	private static final long serialVersionUID = -7789515188446609981L;

	public String toString() {
		return "ServerLeaveEvent : [server " + getEventSource()
				+ " leaved cluster]";
	}
}