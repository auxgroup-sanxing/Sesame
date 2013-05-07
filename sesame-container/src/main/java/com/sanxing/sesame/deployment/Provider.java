package com.sanxing.sesame.deployment;

import javax.xml.namespace.QName;

public class Provider
{
    private QName serviceName;

    private String endpointName;

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
}