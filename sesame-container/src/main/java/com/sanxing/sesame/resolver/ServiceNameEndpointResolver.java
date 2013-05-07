package com.sanxing.sesame.resolver;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import com.sanxing.sesame.exception.NoServiceAvailableException;

public class ServiceNameEndpointResolver
    extends EndpointResolverSupport
{
    private QName serviceName;

    public ServiceNameEndpointResolver()
    {
    }

    public ServiceNameEndpointResolver( QName serviceName )
    {
        this.serviceName = serviceName;
    }

    @Override
    public ServiceEndpoint[] resolveAvailableEndpoints( ComponentContext context, MessageExchange exchange )
    {
        return context.getEndpointsForService( serviceName );
    }

    public QName getServiceName()
    {
        return serviceName;
    }

    public void setServiceName( QName serviceName )
    {
        this.serviceName = serviceName;
    }

    @Override
    protected JBIException createServiceUnavailableException()
    {
        return new NoServiceAvailableException( serviceName );
    }
}