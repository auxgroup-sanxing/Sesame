package com.sanxing.sesame.dispatch.cluster;

import com.sanxing.sesame.servicedesc.InternalEndpoint;

public interface ClusterEndpointChooser
{
    public abstract InternalEndpoint choose( InternalEndpoint localPoint, InternalEndpoint[] remotes );
}