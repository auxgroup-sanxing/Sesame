package com.sanxing.sesame.container;

import java.io.Serializable;
import java.util.Arrays;

import javax.xml.namespace.QName;

import com.sanxing.sesame.messaging.PojoMarshaler;
import com.sanxing.sesame.resolver.EndpointChooser;
import com.sanxing.sesame.resolver.EndpointResolver;
import com.sanxing.sesame.resolver.InterfaceNameEndpointResolver;
import com.sanxing.sesame.resolver.ServiceAndEndpointNameResolver;
import com.sanxing.sesame.resolver.ServiceNameEndpointResolver;
import com.sanxing.sesame.resolver.URIResolver;

public class ActivationSpec
    implements Serializable
{
    static final long serialVersionUID = 8458586342841647313L;

    private String id;

    private String componentName;

    private Object component;

    private QName service;

    private QName interfaceName;

    private QName operation;

    private String endpoint;

    private transient EndpointResolver destinationResolver;

    private transient EndpointChooser interfaceChooser;

    private transient EndpointChooser serviceChooser;

    private QName destinationService;

    private QName destinationInterface;

    private QName destinationOperation;

    private String destinationEndpoint;

    private transient PojoMarshaler marshaler;

    private final SubscriptionSpec[] subscriptions = new SubscriptionSpec[0];

    private boolean failIfNoDestinationEndpoint = true;

    private Boolean persistent;

    private String destinationUri;

    public ActivationSpec()
    {
    }

    public ActivationSpec( Object component )
    {
        this.component = component;
    }

    public ActivationSpec( String id, Object component )
    {
        this.id = id;
        this.component = component;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getComponentName()
    {
        return componentName;
    }

    public void setComponentName( String componentName )
    {
        this.componentName = componentName;
    }

    public Object getComponent()
    {
        return component;
    }

    public void setComponent( Object component )
    {
        this.component = component;
    }

    public QName getService()
    {
        return service;
    }

    public void setService( QName service )
    {
        this.service = service;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint( String endpoint )
    {
        this.endpoint = endpoint;
    }

    public QName getInterfaceName()
    {
        return interfaceName;
    }

    public void setInterfaceName( QName interfaceName )
    {
        this.interfaceName = interfaceName;
    }

    public QName getOperation()
    {
        return operation;
    }

    public void setOperation( QName operation )
    {
        this.operation = operation;
    }

    public EndpointResolver getDestinationResolver()
    {
        if ( destinationResolver == null )
        {
            destinationResolver = createEndpointResolver();
        }
        return destinationResolver;
    }

    public void setDestinationResolver( EndpointResolver destinationResolver )
    {
        this.destinationResolver = destinationResolver;
    }

    public EndpointChooser getInterfaceChooser()
    {
        return interfaceChooser;
    }

    public void setInterfaceChooser( EndpointChooser interfaceChooser )
    {
        this.interfaceChooser = interfaceChooser;
    }

    public EndpointChooser getServiceChooser()
    {
        return serviceChooser;
    }

    public void setServiceChooser( EndpointChooser serviceChooser )
    {
        this.serviceChooser = serviceChooser;
    }

    public QName getDestinationService()
    {
        return destinationService;
    }

    public void setDestinationService( QName destinationService )
    {
        this.destinationService = destinationService;
    }

    public QName getDestinationInterface()
    {
        return destinationInterface;
    }

    public void setDestinationInterface( QName destinationInterface )
    {
        this.destinationInterface = destinationInterface;
    }

    public QName getDestinationOperation()
    {
        return destinationOperation;
    }

    public void setDestinationOperation( QName destinationOperation )
    {
        this.destinationOperation = destinationOperation;
    }

    public String getDestinationEndpoint()
    {
        return destinationEndpoint;
    }

    public void setDestinationEndpoint( String destinationEndpoint )
    {
        this.destinationEndpoint = destinationEndpoint;
    }

    public PojoMarshaler getMarshaler()
    {
        return marshaler;
    }

    public void setMarshaler( PojoMarshaler marshaler )
    {
        this.marshaler = marshaler;
    }

    public boolean isFailIfNoDestinationEndpoint()
    {
        return failIfNoDestinationEndpoint;
    }

    public void setFailIfNoDestinationEndpoint( boolean failIfNoDestinationEndpoint )
    {
        this.failIfNoDestinationEndpoint = failIfNoDestinationEndpoint;
    }

    protected EndpointResolver createEndpointResolver()
    {
        if ( destinationService != null )
        {
            if ( destinationEndpoint != null )
            {
                return new ServiceAndEndpointNameResolver( destinationService, destinationEndpoint );
            }
            return new ServiceNameEndpointResolver( destinationService );
        }
        if ( destinationInterface != null )
        {
            return new InterfaceNameEndpointResolver( destinationInterface );
        }
        if ( destinationUri != null )
        {
            return new URIResolver( destinationUri );
        }
        return null;
    }

    public Boolean getPersistent()
    {
        return persistent;
    }

    public void setPersistent( Boolean persistent )
    {
        this.persistent = persistent;
    }

    public String getDestinationUri()
    {
        return destinationUri;
    }

    public void setDestinationUri( String destinationUri )
    {
        this.destinationUri = destinationUri;
    }

    @Override
    public String toString()
    {
        return "ActivationSpec [component=" + component + ", componentName=" + componentName + ", destinationEndpoint="
            + destinationEndpoint + ", destinationInterface=" + destinationInterface + ", destinationOperation="
            + destinationOperation + ", destinationService=" + destinationService + ", destinationUri="
            + destinationUri + ", endpoint=" + endpoint + ", failIfNoDestinationEndpoint="
            + failIfNoDestinationEndpoint + ", id=" + id + ", interfaceName=" + interfaceName + ", operation="
            + operation + ", persistent=" + persistent + ", service=" + service + ", subscriptions="
            + Arrays.toString( subscriptions ) + "]";
    }
}