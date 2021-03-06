package com.sanxing.sesame.deployment;

import javax.xml.namespace.QName;

public class Provides
{
    private QName serviceName;

    private String endpointName;

    private QName interfaceName;

    public QName getInterfaceName()
    {
        return interfaceName;
    }

    public void setInterfaceName( QName interfaceName )
    {
        this.interfaceName = interfaceName;
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
}