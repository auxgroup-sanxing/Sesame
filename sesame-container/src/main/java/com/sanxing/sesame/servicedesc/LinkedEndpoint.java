package com.sanxing.sesame.servicedesc;

import javax.xml.namespace.QName;

import org.w3c.dom.DocumentFragment;

public class LinkedEndpoint
    extends AbstractEndpoint
{
    private static final long serialVersionUID = 4615848436197469611L;

    private final QName fromService;

    private final String fromEndpoint;

    private final QName toService;

    private final String toEndpoint;

    private final String linkType;

    public LinkedEndpoint( QName fromService, String fromEndpoint, QName toService, String toEndpoint, String linkType )
    {
        super( null );
        this.fromService = fromService;
        this.fromEndpoint = fromEndpoint;
        this.toService = toService;
        this.toEndpoint = toEndpoint;
        this.linkType = linkType;
    }

    @Override
    public DocumentFragment getAsReference( QName operationName )
    {
        return EndpointReferenceBuilder.getReference( this );
    }

    @Override
    public String getEndpointName()
    {
        return fromEndpoint;
    }

    @Override
    public QName[] getInterfaces()
    {
        return null;
    }

    @Override
    public QName getServiceName()
    {
        return fromService;
    }

    public String getLinkType()
    {
        return linkType;
    }

    public String getToEndpoint()
    {
        return toEndpoint;
    }

    public QName getToService()
    {
        return toService;
    }

    @Override
    protected String getClassifier()
    {
        return "linked";
    }
}