package com.sanxing.sesame.resolver;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import com.sanxing.sesame.exception.NoServiceAvailableException;

public class ServiceAndEndpointNameResolver
    extends EndpointResolverSupport
{
    private QName serviceName;

    private String endpointName;

    public ServiceAndEndpointNameResolver()
    {
    }

    public ServiceAndEndpointNameResolver( QName serviceName, String endpointName )
    {
        this.serviceName = serviceName;
        this.endpointName = endpointName;
    }

    @Override
    public ServiceEndpoint[] resolveAvailableEndpoints( ComponentContext context, MessageExchange exchange )
        throws JBIException
    {
        ServiceEndpoint endpoint = context.getEndpoint( serviceName, endpointName );
        if ( endpoint != null )
        {
            return new ServiceEndpoint[] { endpoint };
        }
        return new ServiceEndpoint[0];
    }

    public QName getServiceName()
    {
        return serviceName;
    }

    public void setServiceName( QName serviceName )
    {
        this.serviceName = serviceName;
    }

    public String getEndpointName()
    {
        return endpointName;
    }

    public void setEndpointName( String endpointName )
    {
        this.endpointName = endpointName;
    }

    @Override
    protected JBIException createServiceUnavailableException()
    {
        return new NoServiceAvailableException( serviceName );
    }
}