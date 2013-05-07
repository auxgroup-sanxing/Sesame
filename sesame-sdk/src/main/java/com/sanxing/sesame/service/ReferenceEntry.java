package com.sanxing.sesame.service;

import java.io.Serializable;

import javax.xml.namespace.QName;

public class ReferenceEntry
    implements Serializable
{
    private static final long serialVersionUID = 1415388341021885499L;

    private QName servcieName;

    private QName interfaceName;

    private QName operationName;

    private String endpointName;

    public QName getServcieName()
    {
        return servcieName;
    }

    public void setServcieName( QName servcieName )
    {
        this.servcieName = servcieName;
    }

    public QName getInterfaceName()
    {
        return interfaceName;
    }

    public void setInterfaceName( QName _interfaceName )
    {
        interfaceName = _interfaceName;
    }

    public QName getOperationName()
    {
        return operationName;
    }

    public void setOperationName( QName _operationName )
    {
        operationName = _operationName;
    }

    public String getEndpointName()
    {
        return endpointName;
    }

    public void setEndpointName( String endpointName )
    {
        this.endpointName = endpointName;
    }

    @Override
    public String toString()
    {
        return "{servcieName=" + servcieName + ", interfaceName=" + interfaceName + ", endpointName=" + endpointName
            + ", operationName=" + operationName + '}';
    }
}