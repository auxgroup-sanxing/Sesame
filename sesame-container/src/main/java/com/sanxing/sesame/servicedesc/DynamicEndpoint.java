package com.sanxing.sesame.servicedesc;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.w3c.dom.DocumentFragment;

import com.sanxing.sesame.mbean.ComponentNameSpace;

public class DynamicEndpoint
    extends AbstractEndpoint
{
    private static final long serialVersionUID = -9084647509619730734L;

    private final QName serviceName;

    private final String endpointName;

    private final transient DocumentFragment epr;

    public DynamicEndpoint( ComponentNameSpace componentName, ServiceEndpoint endpoint, DocumentFragment epr )
    {
        super( componentName );
        serviceName = endpoint.getServiceName();
        endpointName = endpoint.getEndpointName();
        this.epr = epr;
    }

    @Override
    public DocumentFragment getAsReference( QName operationName )
    {
        return epr;
    }

    @Override
    public String getEndpointName()
    {
        return endpointName;
    }

    @Override
    public QName[] getInterfaces()
    {
        return null;
    }

    @Override
    public QName getServiceName()
    {
        return serviceName;
    }

    @Override
    protected String getClassifier()
    {
        return "dynamic";
    }
}