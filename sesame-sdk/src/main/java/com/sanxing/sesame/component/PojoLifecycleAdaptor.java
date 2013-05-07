package com.sanxing.sesame.component;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

public class PojoLifecycleAdaptor
    implements ComponentLifeCycle
{
    private final Object pojo;

    private final QName service;

    private final String endpoint;

    private ComponentContext context;

    private ObjectName extensionMBeanName;

    public PojoLifecycleAdaptor( Object pojo, QName service, String endpoint )
    {
        this.pojo = pojo;
        this.service = service;
        this.endpoint = endpoint;
    }

    @Override
    public ObjectName getExtensionMBeanName()
    {
        return extensionMBeanName;
    }

    @Override
    public void init( ComponentContext ctx )
        throws JBIException
    {
        context = ctx;
        if ( ( service != null ) && ( endpoint != null ) )
        {
            ctx.activateEndpoint( service, endpoint );
        }
    }

    @Override
    public void shutDown()
        throws JBIException
    {
    }

    @Override
    public void start()
        throws JBIException
    {
    }

    @Override
    public void stop()
        throws JBIException
    {
    }

    public Object getPojo()
    {
        return pojo;
    }

    public void setExtensionMBeanName( ObjectName extensionMBeanName )
    {
        this.extensionMBeanName = extensionMBeanName;
    }

    public ComponentContext getContext()
    {
        return context;
    }
}