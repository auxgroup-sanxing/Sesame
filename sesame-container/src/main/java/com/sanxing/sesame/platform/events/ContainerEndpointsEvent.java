package com.sanxing.sesame.platform.events;

import java.util.ArrayList;
import java.util.Collection;

import javax.jbi.servicedesc.ServiceEndpoint;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.mbean.ComponentMBeanImpl;
import com.sanxing.sesame.mbean.ComponentNameSpace;

public class ContainerEndpointsEvent
    extends ClusterEvent
{
    private static final long serialVersionUID = 1787090586538938167L;

    public ContainerEndpointsEvent( JBIContainer container )
    {
        ArrayList internalEndpoitsOnContainer = new ArrayList();
        Collection<ComponentMBeanImpl> components = container.getRegistry().getComponents();
        for ( ComponentMBeanImpl component : components )
        {
            ComponentNameSpace cns = component.getComponentNameSpace();
            ServiceEndpoint[] endpoints = container.getRegistry().getEndpointsForComponent( cns );
            for ( ServiceEndpoint endpoint : endpoints )
            {
                internalEndpoitsOnContainer.add( endpoint );
            }
        }
        setEventSource( container.getName() );
        setEventObject( internalEndpoitsOnContainer );
    }
}