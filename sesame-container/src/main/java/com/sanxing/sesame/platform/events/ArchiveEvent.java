package com.sanxing.sesame.platform.events;

import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.mbean.ArchiveEntry;

public class ArchiveEvent
    extends ClusterEvent
{
    private static final long serialVersionUID = -3029163590300853700L;

    private final ArchiveEntry entry;

    public ArchiveEntry getEntry()
    {
        return entry;
    }

    public ArchiveEvent( ArchiveEntry _entry )
    {
        entry = _entry;
        setEventObject( _entry );
        setEventSource( Platform.getEnv().getServerName() );
    }

    @Override
    public String toString()
    {
        return "ArchivaEvent [entry=" + entry + "] + source + [" + getEventSource() + "]";
    }
}