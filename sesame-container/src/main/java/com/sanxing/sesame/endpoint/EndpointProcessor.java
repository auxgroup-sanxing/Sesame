package com.sanxing.sesame.endpoint;

import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.servicedesc.InternalEndpoint;

public interface EndpointProcessor
{
    public abstract void init( Registry reg );

    public abstract void process( InternalEndpoint serviceEndpoint );
}