package com.sanxing.sesame.resolver;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import com.sanxing.sesame.exception.NoInterfaceAvailableException;

public class InterfaceNameEndpointResolver
    extends EndpointResolverSupport
{
    private QName interfaceName;

    public InterfaceNameEndpointResolver()
    {
    }

    public InterfaceNameEndpointResolver( QName interfaceName )
    {
        this.interfaceName = interfaceName;
    }

    @Override
    public ServiceEndpoint[] resolveAvailableEndpoints( ComponentContext context, MessageExchange exchange )
    {
        return context.getEndpoints( interfaceName );
    }

    public QName getInterfaceName()
    {
        return interfaceName;
    }

    public void setInterfaceName( QName interfaceName )
    {
        this.interfaceName = interfaceName;
    }

    @Override
    protected JBIException createServiceUnavailableException()
    {
        return new NoInterfaceAvailableException( interfaceName );
    }
}