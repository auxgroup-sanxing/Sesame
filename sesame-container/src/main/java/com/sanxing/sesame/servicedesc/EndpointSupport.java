package com.sanxing.sesame.servicedesc;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

public final class EndpointSupport
{
    public static String getKey( QName service, String endpoint )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "{" );
        sb.append( service.getNamespaceURI() );
        sb.append( "}" );
        sb.append( service.getLocalPart() );
        sb.append( ":" );
        sb.append( endpoint );
        return sb.toString();
    }

    public static String getKey( ServiceEndpoint endpoint )
    {
        if ( endpoint instanceof AbstractEndpoint )
        {
            return ( (AbstractEndpoint) endpoint ).getKey();
        }
        return getKey( endpoint.getServiceName(), endpoint.getEndpointName() );
    }

    public static String getUniqueKey( ServiceEndpoint endpoint )
    {
        if ( endpoint instanceof AbstractEndpoint )
        {
            return ( (AbstractEndpoint) endpoint ).getUniqueKey();
        }
        return endpoint.getClass().getName() + ":" + getKey( endpoint );
    }
}