package com.sanxing.sesame.container;

import java.io.Serializable;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.messaging.ExchangePacket;
import com.sanxing.sesame.messaging.MessageExchangeImpl;
import com.sanxing.sesame.resolver.SubscriptionFilter;
import com.sanxing.sesame.servicedesc.InternalEndpoint;

public class SubscriptionSpec
    implements Serializable
{
    private static final long serialVersionUID = 8458586342841647313L;

    private QName service;

    private QName interfaceName;

    private QName operation;

    private String endpoint;

    private transient SubscriptionFilter filter;

    private ComponentNameSpace name;

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint( String endpoint )
    {
        this.endpoint = endpoint;
    }

    public SubscriptionFilter getFilter()
    {
        return filter;
    }

    public void setFilter( SubscriptionFilter filter )
    {
        this.filter = filter;
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

    public QName getService()
    {
        return service;
    }

    public void setService( QName service )
    {
        this.service = service;
    }

    public ComponentNameSpace getName()
    {
        return name;
    }

    public void setName( ComponentNameSpace name )
    {
        this.name = name;
    }

    public boolean matches( Registry registry, MessageExchangeImpl exchange )
    {
        boolean result = false;

        ExchangePacket packet = exchange.getPacket();
        ComponentNameSpace sourceId = packet.getSourceId();
        if ( sourceId != null )
        {
            if ( service != null )
            {
                ServiceEndpoint[] ses = registry.getEndpointsForService( service );
                if ( ses != null )
                {
                    for ( int i = 0; i < ses.length; ++i )
                    {
                        InternalEndpoint se = (InternalEndpoint) ses[i];
                        if ( ( se.getComponentNameSpace() != null ) && ( se.getComponentNameSpace().equals( sourceId ) ) )
                        {
                            result = true;
                            break;
                        }
                    }
                }
            }
            if ( ( result ) && ( interfaceName != null ) )
            {
                ServiceEndpoint[] ses = registry.getEndpointsForInterface( interfaceName );
                if ( ses != null )
                {
                    result = false;
                    for ( int i = 0; i < ses.length; ++i )
                    {
                        InternalEndpoint se = (InternalEndpoint) ses[i];
                        if ( ( se.getComponentNameSpace() != null ) && ( se.getComponentNameSpace().equals( sourceId ) ) )
                        {
                            result = true;
                            break;
                        }
                    }
                }
            }

        }

        if ( ( service == null ) && ( interfaceName == null ) )
        {
            result = true;
        }
        if ( ( result ) && ( filter != null ) )
        {
            result = filter.matches( exchange );
        }
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        boolean result = false;
        if ( obj instanceof SubscriptionSpec )
        {
            SubscriptionSpec other = (SubscriptionSpec) obj;
            result =
                ( ( name == null ) && ( other.name == null ) )
                    || ( ( ( !( ( name != null && name.equals( other.name ) || ( other.name != null && other.name.equals( name ) ) ) ) )
                        || ( service != null ) || ( other.service != null ) )
                        && ( ( service == null ) || ( other.service == null )
                            || ( !( service.equals( other.service ) ) ) || ( interfaceName != null ) || ( other.interfaceName != null ) )
                        && ( ( interfaceName == null ) || ( other.interfaceName == null )
                            || ( !( interfaceName.equals( other.interfaceName ) ) ) || ( endpoint != null ) || ( other.endpoint != null ) ) && ( ( endpoint == null )
                        || ( other.endpoint == null ) || ( !( endpoint.equals( other.endpoint ) ) ) ) );
        }

        return result;
    }

    @Override
    public int hashCode()
    {
        return ( ( ( name != null ) ? name.hashCode() : 0 ) ^ ( ( interfaceName != null ) ? interfaceName.hashCode()
            : ( service != null ) ? service.hashCode() : super.hashCode() ) );
    }
}