package com.sanxing.sesame.dispatch.cluster;

import java.util.HashMap;
import java.util.Map;

import com.sanxing.sesame.servicedesc.InternalEndpoint;

public class ClusterEndpointChooserFactory
{
    static ClusterEndpointChooser random = new RandomCEChooser();

    private static Map<InternalEndpoint, ClusterEndpointChooser> roundRobbinCache = new HashMap();

    public static ClusterEndpointChooser random()
    {
        return random;
    }

    public static ClusterEndpointChooser roundRobbin( InternalEndpoint localEndpoint )
    {
        if ( !( localEndpoint.isClustered() ) )
        {
            throw new RuntimeException( "unsupported ep , must be clusted" );
        }
        if ( !( roundRobbinCache.containsKey( localEndpoint ) ) )
        {
            roundRobbinCache.put( localEndpoint, new RoundRobbinCEChooser( localEndpoint ) );
        }
        return roundRobbinCache.get( localEndpoint );
    }
}