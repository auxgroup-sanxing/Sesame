package com.sanxing.sesame.servicedesc;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.w3c.dom.DocumentFragment;

import com.sanxing.sesame.mbean.ComponentNameSpace;

public class ExternalEndpoint
    extends AbstractEndpoint
{
    private static final long serialVersionUID = 4257588916448457889L;

    protected final ServiceEndpoint se;

    public ExternalEndpoint( ComponentNameSpace cns, ServiceEndpoint se )
    {
        super( cns );
        this.se = se;
    }

    @Override
    public DocumentFragment getAsReference( QName operationName )
    {
        return se.getAsReference( operationName );
    }

    @Override
    public String getEndpointName()
    {
        return se.getEndpointName();
    }

    @Override
    public QName[] getInterfaces()
    {
        return se.getInterfaces();
    }

    @Override
    public QName getServiceName()
    {
        return se.getServiceName();
    }

    @Override
    protected String getClassifier()
    {
        return "external";
    }
}