package com.sanxing.sesame.endpoint;

import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.servicedesc.InternalEndpoint;

public abstract interface EndpointProcessor
{
    public abstract void init( Registry paramRegistry );

    public abstract void process( InternalEndpoint paramInternalEndpoint );
}