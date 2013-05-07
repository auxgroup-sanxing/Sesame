package com.sanxing.sesame.servicedesc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.DocumentFragment;

import com.sanxing.sesame.mbean.ComponentNameSpace;

public class InternalEndpoint
    extends AbstractEndpoint
{
    private static final long serialVersionUID = -2312687961530378310L;

    private final String endpointName;

    private final QName serviceName;

    private final Set<QName> interfaces = new HashSet();

    private transient Map<ComponentNameSpace, InternalEndpoint> remotes = new HashMap();

    public InternalEndpoint( ComponentNameSpace componentName, String endpointName, QName serviceName )
    {
        super( componentName );
        this.endpointName = endpointName;
        this.serviceName = serviceName;
    }

    @Override
    public DocumentFragment getAsReference( QName operationName )
    {
        return EndpointReferenceBuilder.getReference( this );
    }

    @Override
    public String getEndpointName()
    {
        return endpointName;
    }

    @Override
    public QName[] getInterfaces()
    {
        QName[] result = new QName[interfaces.size()];
        interfaces.toArray( result );
        return result;
    }

    public void addInterface( QName name )
    {
        interfaces.add( name );
    }

    @Override
    public QName getServiceName()
    {
        return serviceName;
    }

    public InternalEndpoint[] getRemoteEndpoints()
    {
        InternalEndpoint[] result = new InternalEndpoint[remotes.size()];
        remotes.values().toArray( result );
        return result;
    }

    public void addRemoteEndpoint( InternalEndpoint remote )
    {
        remotes.put( remote.getComponentNameSpace(), remote );
    }

    public void removeRemoteEndpoint( InternalEndpoint remote )
    {
        remotes.remove( remote.getComponentNameSpace() );
    }

    public boolean isLocal()
    {
        return ( getComponentNameSpace() != null );
    }

    public boolean isClustered()
    {
        return ( ( remotes != null ) && ( remotes.size() > 0 ) );
    }

    @Override
    public boolean equals( Object obj )
    {
        boolean result = false;
        if ( obj instanceof InternalEndpoint )
        {
            InternalEndpoint other = (InternalEndpoint) obj;
            result = ( other.serviceName.equals( serviceName ) ) && ( other.endpointName.equals( endpointName ) );
        }

        return result;
    }

    @Override
    public int hashCode()
    {
        return ( serviceName.hashCode() ^ endpointName.hashCode() );
    }

    @Override
    public String toString()
    {
        return "ServiceEndpoint[service=" + serviceName + ",endpoint=" + endpointName + ", clustered " + isClustered()
            + "]";
    }

    @Override
    protected String getClassifier()
    {
        return "internal";
    }
}