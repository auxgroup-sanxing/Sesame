package com.sanxing.sesame.platform.events;

import java.util.ArrayList;
import java.util.Collection;

import javax.jbi.servicedesc.ServiceEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.event.ServerLeaveEvent;
import com.sanxing.sesame.core.listener.ClusterListener;
import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.servicedesc.InternalEndpoint;

public class EndpointsListener
    implements ClusterListener
{
    JBIContainer container;

    private static Logger LOG = LoggerFactory.getLogger( EndpointsListener.class );

    public EndpointsListener( JBIContainer _container )
    {
        container = _container;
    }

    @Override
    public void listen( ClusterEvent event )
    {
        if ( event instanceof ContainerEndpointsEvent )
        {
            ContainerEndpointsEvent endpoints = (ContainerEndpointsEvent) event;
            ArrayList<InternalEndpoint> endpointsOnContainer = (ArrayList) endpoints.getEventObject();
            for ( InternalEndpoint endpoint : endpointsOnContainer )
            {
                container.getRegistry().registerRemoteEndpoint( endpoint );
            }

        }
        else if ( event instanceof SERegisteredEvent )
        {
            container.getRegistry().registerRemoteEndpoint( (InternalEndpoint) event.getEventObject() );
        }
        else if ( event instanceof SEUnregistredEvent )
        {
            LOG.debug( "ComponentNamespace:" + ( (InternalEndpoint) event.getEventObject() ).getComponentNameSpace() );
            container.getRegistry().unregisterRemoteEndpoint( (InternalEndpoint) event.getEventObject() );
        }
        else if ( event instanceof ServerLeaveEvent )
        {
            Collection<ComponentMBeanImpl> components = container.getRegistry().getComponents();
            for ( ComponentMBeanImpl component : components )
            {
                ComponentNameSpace cns = component.getComponentNameSpace();
                ServiceEndpoint[] endpoints = container.getRegistry().getEndpointsForComponent( cns );
                for ( ServiceEndpoint endpoint : endpoints )
                {
                    InternalEndpoint point = (InternalEndpoint) endpoint;
                    for ( InternalEndpoint remote : point.getRemoteEndpoints() )
                    {
                        if ( remote.getComponentNameSpace().getContainerName().equals( event.getEventObject() ) )
                        {
                            point.removeRemoteEndpoint( remote );
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setServer( BaseServer server )
    {
    }

    @Override
    public String toString()
    {
        return "listener [" + super.getClass() + "] listen on container join";
    }
}