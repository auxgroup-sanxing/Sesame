package com.sanxing.sesame.servicedesc;

import java.io.Serializable;

import javax.jbi.servicedesc.ServiceEndpoint;

import com.sanxing.sesame.mbean.ComponentNameSpace;

public abstract class AbstractEndpoint
    implements ServiceEndpoint, Serializable
{
    private static final long serialVersionUID = -591733214139930976L;

    private ComponentNameSpace componentName;

    private String key;

    private String uniqueKey;

    public AbstractEndpoint( ComponentNameSpace componentName )
    {
        this.componentName = componentName;
    }

    protected AbstractEndpoint()
    {
    }

    public ComponentNameSpace getComponentNameSpace()
    {
        return componentName;
    }

    public void setComponentName( ComponentNameSpace componentName )
    {
        this.componentName = componentName;
    }

    public String getKey()
    {
        if ( key == null )
        {
            key = EndpointSupport.getKey( getServiceName(), getEndpointName() );
        }
        return key;
    }

    public String getUniqueKey()
    {
        if ( uniqueKey == null )
        {
            uniqueKey = getClassifier() + ":" + getKey();
        }
        return uniqueKey;
    }

    protected abstract String getClassifier();
}