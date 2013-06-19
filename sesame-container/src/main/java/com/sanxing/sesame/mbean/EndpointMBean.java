package com.sanxing.sesame.mbean;

import javax.xml.namespace.QName;

public interface EndpointMBean
{
    public abstract String getEndpointName();

    public abstract QName[] getInterfaces();

    public abstract QName getServiceName();

    public abstract String getComponentName();

    public abstract String loadReference();

    public abstract String loadWSDL();

    public abstract String getRemoteContainers();
}