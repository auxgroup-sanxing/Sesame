package com.sanxing.sesame.resolver;

import java.util.HashMap;
import java.util.Map;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

public class ServiceRoundRobinPolicy
    implements EndpointChooser
{
    private final Map<QName, Integer> lastIndexMap;

    private int lastIndex;

    public ServiceRoundRobinPolicy()
    {
        lastIndexMap = new HashMap();
    }

    @Override
    public ServiceEndpoint chooseEndpoint( ServiceEndpoint[] endpoints, ComponentContext context,
                                           MessageExchange exchange )
    {
        if ( endpoints.length == 0 )
        {
            return null;
        }

        if ( exchange.getService() == null )
        {
            return endpoints[0];
        }

        if ( lastIndexMap.containsKey( exchange.getService() ) )
        {
            lastIndex = lastIndexMap.get( exchange.getService() ).intValue();
        }
        else
        {
            lastIndex = 0;
        }

        if ( ( lastIndex >= endpoints.length ) || ( lastIndex < 0 ) )
        {
            lastIndex = 0;
        }

        ServiceEndpoint result = endpoints[( lastIndex++ )];

        lastIndexMap.put( exchange.getService(), Integer.valueOf( lastIndex ) );

        return result;
    }
}