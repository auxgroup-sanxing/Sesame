package com.sanxing.sesame.platform.events;

import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.mbean.ArchiveEntry;
import com.sanxing.sesame.core.Env;
import com.sanxing.sesame.core.Platform;

public class ArchiveEvent extends ClusterEvent {
	private static final long serialVersionUID = -3029163590300853700L;
	private ArchiveEntry entry;

	public ArchiveEntry getEntry() {
		return this.entry;
	}

	public ArchiveEvent(ArchiveEntry _entry) {
		this.entry = _entry;
		setEventObject(_entry);
		setEventSource(Platform.getEnv().getServerName());
	}

	public String toString() {
		return "ArchivaEvent [entry=" + this.entry + "] + source + ["
				+ getEventSource() + "]";
	}
}