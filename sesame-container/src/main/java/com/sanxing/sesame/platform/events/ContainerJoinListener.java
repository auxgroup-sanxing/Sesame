package com.sanxing.sesame.platform.events;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.core.event.ServerJoinEvent;
import com.sanxing.sesame.core.listener.ClusterListener;
import com.sanxing.sesame.jmx.mbean.admin.ClusterAdminMBean;
import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;

public class ContainerJoinListener
    implements ClusterListener
{
    private final Logger LOG = LoggerFactory.getLogger( ContainerJoinListener.class );

    JBIContainer container;

    @Override
    public void listen( ClusterEvent event )
    {
        if ( event instanceof ServerJoinEvent )
        {
            ClusterAdminMBean clusterAdmin = MBeanHelper.getAdminMBean( ClusterAdminMBean.class, "cluster-manager" );

            if ( Platform.getEnv().isAdmin() )
            {
                Set<ArchiveEvent> installedFiles = container.getArchiveManager().getPublishedEvents();
                LOG.debug( "Notify new joined server install events(" + installedFiles.size() + ")" );
                for ( ArchiveEvent archivaEvent : installedFiles )
                {
                    clusterAdmin.fireEvent( archivaEvent, event.getEventSource() );
                }
            }
            else
            {
                ContainerEndpointsEvent myEvent = new ContainerEndpointsEvent( container );

                clusterAdmin.fireEvent( myEvent, event.getEventSource() );
            }
        }
    }

    @Override
    public void setServer( BaseServer _server )
    {
    }

    public ContainerJoinListener( JBIContainer _container )
    {
        container = _container;
    }
}