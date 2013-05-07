package com.sanxing.sesame.resolver;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public abstract class EndpointResolverSupport
    implements EndpointResolver
{
    private EndpointChooser chooser;

    private boolean failIfUnavailable;

    public EndpointResolverSupport()
    {
        failIfUnavailable = true;
    }

    @Override
    public ServiceEndpoint resolveEndpoint( ComponentContext context, MessageExchange exchange, EndpointFilter filter )
        throws JBIException
    {
        ServiceEndpoint[] endpoints = resolveAvailableEndpoints( context, exchange );
        if ( endpoints == null )
        {
            return null;
        }
        if ( endpoints.length > 0 )
        {
            endpoints = filterEndpoints( endpoints, exchange, filter );
        }
        if ( endpoints.length == 0 )
        {
            if ( failIfUnavailable )
            {
                throw createServiceUnavailableException();
            }
            return null;
        }

        if ( endpoints.length == 1 )
        {
            return endpoints[0];
        }
        return getChooser().chooseEndpoint( endpoints, context, exchange );
    }

    public boolean isFailIfUnavailable()
    {
        return failIfUnavailable;
    }

    public void setFailIfUnavailable( boolean failIfUnavailable )
    {
        this.failIfUnavailable = failIfUnavailable;
    }

    public EndpointChooser getChooser()
    {
        if ( chooser == null )
        {
            chooser = new FirstChoicePolicy();
        }
        return chooser;
    }

    public void setChooser( EndpointChooser chooser )
    {
        this.chooser = chooser;
    }

    protected abstract JBIException createServiceUnavailableException();

    protected ServiceEndpoint[] filterEndpoints( ServiceEndpoint[] endpoints, MessageExchange exchange,
                                                 EndpointFilter filter )
    {
        int matches = 0;
        for ( int i = 0; i < endpoints.length; ++i )
        {
            ServiceEndpoint endpoint = endpoints[i];
            if ( filter.evaluate( endpoint, exchange ) )
            {
                ++matches;
            }
            else
            {
                endpoints[i] = null;
            }
        }
        if ( matches == endpoints.length )
        {
            return endpoints;
        }
        ServiceEndpoint[] answer = new ServiceEndpoint[matches];
        int j = 0;
        for ( int i = 0; i < endpoints.length; ++i )
        {
            ServiceEndpoint endpoint = endpoints[i];
            if ( endpoint != null )
            {
                answer[( j++ )] = endpoints[i];
            }
        }
        return answer;
    }
}