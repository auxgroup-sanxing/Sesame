package com.sanxing.sesame.deployment;

import javax.xml.namespace.QName;

public class Consumes
{
    private QName interfaceName;

    private QName serviceName;

    private String endpointName;

    private String linkType;

    public Consumes()
    {
        linkType = "standard";
    }

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

    public String getLinkType()
    {
        return linkType;
    }

    public void setLinkType( String linkType )
    {
        this.linkType = linkType;
    }

    public boolean isStandardLink()
    {
        return ( ( linkType != null ) && ( linkType.equals( "standard" ) ) );
    }

    public boolean isSoftLink()
    {
        return ( ( linkType != null ) && ( linkType.equals( "soft" ) ) );
    }

    public boolean isHardLink()
    {
        return ( ( linkType != null ) && ( linkType.equals( "hard" ) ) );
    }
}