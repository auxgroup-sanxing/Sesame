package com.sanxing.sesame.mbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.endpoint.EndpointProcessor;
import com.sanxing.sesame.jmx.mbean.admin.ClusterAdminMBean;
import com.sanxing.sesame.platform.events.SERegisteredEvent;
import com.sanxing.sesame.platform.events.SEUnregistredEvent;
import com.sanxing.sesame.servicedesc.AbstractEndpoint;
import com.sanxing.sesame.servicedesc.ExternalEndpoint;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import com.sanxing.sesame.servicedesc.LinkedEndpoint;

public class EndpointRegistry
{
    private static final Logger LOG = LoggerFactory.getLogger( EndpointRegistry.class );

    private final Registry registry;

    private final Map<AbstractEndpoint, Endpoint> endpointMBeans;

    private final Map<String, ServiceEndpoint> internalEndpoints;

    private final Map<String, ServiceEndpoint> externalEndpoints;

    private final Map<String, ServiceEndpoint> linkedEndpoints;

    private final Map<QName, InterfaceConnection> interfaceConnections;

    private final List<EndpointProcessor> endpointProcessors;

    public EndpointRegistry( Registry registry )
    {
        this.registry = registry;
        endpointMBeans = new ConcurrentHashMap();
        internalEndpoints = new ConcurrentHashMap();
        externalEndpoints = new ConcurrentHashMap();
        linkedEndpoints = new ConcurrentHashMap();
        interfaceConnections = new ConcurrentHashMap();
        endpointProcessors = getEndpointProcessors();
    }

    private List<EndpointProcessor> getEndpointProcessors()
    {
        List l = new ArrayList();
        String[] classes =
            { "com.sanxing.sesame.endpoint.SUDescriptorProcessor", "com.sanxing.sesame.endpoint.WSDL1Processor" };

        for ( int i = 0; i < classes.length; ++i )
        {
            try
            {
                EndpointProcessor p = (EndpointProcessor) Class.forName( classes[i] ).newInstance();
                p.init( registry );
                l.add( p );
            }
            catch ( Throwable e )
            {
                LOG.warn( "Disabled endpoint processor '" + classes[i] + "': " + e );
            }
        }
        return l;
    }

    public ServiceEndpoint[] getEndpointsForComponent( ComponentNameSpace cns )
    {
        Collection endpoints = new ArrayList();
        for ( Object element : getInternalEndpoints() )
        {
            InternalEndpoint endpoint = (InternalEndpoint) element;
            if ( cns.equals( endpoint.getComponentNameSpace() ) )
            {
                endpoints.add( endpoint );
            }
        }
        return asEndpointArray( endpoints );
    }

    public ServiceEndpoint[] getAllEndpointsForComponent( ComponentNameSpace cns )
    {
        Collection endpoints = new ArrayList();
        for ( Object element : getInternalEndpoints() )
        {
            InternalEndpoint endpoint = (InternalEndpoint) element;
            if ( cns.equals( endpoint.getComponentNameSpace() ) )
            {
                endpoints.add( endpoint );
            }
        }
        for ( Object element : getExternalEndpoints() )
        {
            ExternalEndpoint endpoint = (ExternalEndpoint) element;
            if ( cns.equals( endpoint.getComponentNameSpace() ) )
            {
                endpoints.add( endpoint );
            }
        }
        return asEndpointArray( endpoints );
    }

    public Collection<Endpoint> getEndpointMBeans()
    {
        return endpointMBeans.values();
    }

    public ServiceEndpoint[] getEndpointsForService( QName serviceName )
    {
        Collection collection = getEndpointsByService( serviceName, getInternalEndpoints() );
        return asEndpointArray( collection );
    }

    public ServiceEndpoint[] getEndpointsForInterface( QName interfaceName )
    {
        if ( interfaceName == null )
        {
            return asEndpointArray( internalEndpoints.values() );
        }
        InterfaceConnection conn = interfaceConnections.get( interfaceName );
        if ( conn != null )
        {
            String key = getKey( conn.service, conn.endpoint );
            ServiceEndpoint ep = internalEndpoints.get( key );
            if ( ep == null )
            {
                LOG.warn( "Connection for interface " + interfaceName + " could not find target for service "
                    + conn.service + " and endpoint " + conn.endpoint );

                return new ServiceEndpoint[0];
            }
            return new ServiceEndpoint[] { ep };
        }

        Collection result = getEndpointsByInterface( interfaceName, getInternalEndpoints() );
        return asEndpointArray( result );
    }

    public InternalEndpoint registerInternalEndpoint( ComponentContextImpl provider, QName serviceName,
                                                      String endpointName )
        throws JBIException
    {
        String key = getKey( serviceName, endpointName );
        InternalEndpoint registered = (InternalEndpoint) internalEndpoints.get( key );

        if ( ( registered != null ) && ( registered.isLocal() ) )
        {
            throw new JBIException( "An internal endpoint for service " + serviceName + " and endpoint " + endpointName
                + " is already registered" );
        }

        InternalEndpoint serviceEndpoint =
            new InternalEndpoint( provider.getComponentNameSpace(), endpointName, serviceName );

        if ( provider.getActivationSpec().getInterfaceName() != null )
        {
            serviceEndpoint.addInterface( provider.getActivationSpec().getInterfaceName() );
        }

        for ( Object element : endpointProcessors )
        {
            EndpointProcessor p = (EndpointProcessor) element;
            p.process( serviceEndpoint );
        }

        if ( registered != null )
        {
            InternalEndpoint[] remote = registered.getRemoteEndpoints();
            for ( int i = 0; i < remote.length; ++i )
            {
                serviceEndpoint.addRemoteEndpoint( remote[i] );
            }
        }

        internalEndpoints.put( key, serviceEndpoint );
        registerEndpoint( serviceEndpoint );

        if ( Platform.getEnv().isClustered() )
        {
            SERegisteredEvent event = new SERegisteredEvent();
            event.setEventObject( serviceEndpoint );
            event.setEventSource( Platform.getEnv().getServerName() );
            LOG.info( "Notify " + event );
            ClusterAdminMBean clusterAdmin = MBeanHelper.getAdminMBean( ClusterAdminMBean.class, "cluster-manager" );

            clusterAdmin.notifyNeighbors( event );
        }

        return serviceEndpoint;
    }

    public void unregisterInternalEndpoint( ComponentContext provider, InternalEndpoint serviceEndpoint )
    {
        if ( Platform.getEnv().isClustered() )
        {
            SEUnregistredEvent event = new SEUnregistredEvent();
            event.setEventObject( serviceEndpoint );
            event.setEventSource( Platform.getEnv().getServerName() );
            ClusterAdminMBean clusterAdmin = MBeanHelper.getAdminMBean( ClusterAdminMBean.class, "cluster-manager" );

            clusterAdmin.notifyNeighbors( event );
        }
        if ( serviceEndpoint.isClustered() )
        {
            serviceEndpoint.setComponentName( null );
        }
        else
        {
            String key = getKey( serviceEndpoint );
            internalEndpoints.remove( key );
            unregisterEndpoint( serviceEndpoint );
        }
    }

    public void registerRemoteEndpoint( InternalEndpoint remote )
    {
        InternalEndpoint endpoint = (InternalEndpoint) internalEndpoints.get( getKey( remote ) );

        if ( endpoint == null )
        {
            endpoint = new InternalEndpoint( null, remote.getEndpointName(), remote.getServiceName() );
            internalEndpoints.put( getKey( endpoint ), endpoint );
        }

        endpoint.addRemoteEndpoint( remote );
    }

    public void unregisterRemoteEndpoint( InternalEndpoint remote )
    {
        String key = getKey( remote );
        InternalEndpoint endpoint = (InternalEndpoint) internalEndpoints.get( key );
        if ( endpoint != null )
        {
            endpoint.removeRemoteEndpoint( remote );
            if ( ( !( endpoint.isClustered() ) ) && ( !( endpoint.isLocal() ) ) )
            {
                internalEndpoints.remove( key );
                unregisterEndpoint( endpoint );
            }
        }
    }

    public ServiceEndpoint getEndpoint( QName service, String name )
    {
        String key = getKey( service, name );
        ServiceEndpoint ep = linkedEndpoints.get( key );
        if ( ep == null )
        {
            ep = internalEndpoints.get( key );
        }
        return ep;
    }

    public ServiceEndpoint getInternalEndpoint( QName service, String name )
    {
        return internalEndpoints.get( getKey( service, name ) );
    }

    public void registerExternalEndpoint( ComponentNameSpace cns, ServiceEndpoint externalEndpoint )
        throws JBIException
    {
        ExternalEndpoint serviceEndpoint = new ExternalEndpoint( cns, externalEndpoint );
        if ( externalEndpoints.get( getKey( serviceEndpoint ) ) != null )
        {
            throw new JBIException( "An external endpoint for service " + externalEndpoint.getServiceName()
                + " and endpoint " + externalEndpoint.getEndpointName() + " is already registered" );
        }

        registerEndpoint( serviceEndpoint );
        externalEndpoints.put( getKey( serviceEndpoint ), serviceEndpoint );
    }

    public void unregisterExternalEndpoint( ComponentNameSpace cns, ServiceEndpoint externalEndpoint )
    {
        ExternalEndpoint ep = (ExternalEndpoint) externalEndpoints.remove( getKey( externalEndpoint ) );
        unregisterEndpoint( ep );
    }

    public ServiceEndpoint[] getExternalEndpointsForInterface( QName interfaceName )
    {
        Collection endpoints = getEndpointsByInterface( interfaceName, getExternalEndpoints() );
        return asEndpointArray( endpoints );
    }

    public ServiceEndpoint[] getExternalEndpointsForService( QName serviceName )
    {
        Collection endpoints = getEndpointsByService( serviceName, getExternalEndpoints() );
        return asEndpointArray( endpoints );
    }

    protected ServiceEndpoint[] asEndpointArray( Collection<ServiceEndpoint> collection )
    {
        if ( collection == null )
        {
            return new ServiceEndpoint[0];
        }
        ServiceEndpoint[] answer = new ServiceEndpoint[collection.size()];
        answer = collection.toArray( answer );
        return answer;
    }

    protected Collection<ServiceEndpoint> getEndpointsByService( QName serviceName,
                                                                 Collection<ServiceEndpoint> endpoints )
    {
        Collection answer = new ArrayList();
        for ( Object element : endpoints )
        {
            ServiceEndpoint endpoint = (ServiceEndpoint) element;
            if ( endpoint.getServiceName().equals( serviceName ) )
            {
                answer.add( endpoint );
            }
        }
        return answer;
    }

    protected Collection<ServiceEndpoint> getEndpointsByInterface( QName interfaceName,
                                                                   Collection<ServiceEndpoint> endpoints )
    {
        if ( interfaceName == null )
        {
            return endpoints;
        }
        Set answer = new HashSet();
        for ( Object element : endpoints )
        {
            ServiceEndpoint endpoint = (ServiceEndpoint) element;
            QName[] interfaces = endpoint.getInterfaces();
            if ( interfaces != null )
            {
                for ( int k = 0; k < interfaces.length; ++k )
                {
                    QName qn = interfaces[k];
                    if ( ( qn != null ) && ( qn.equals( interfaceName ) ) )
                    {
                        answer.add( endpoint );
                        break;
                    }
                }
            }
        }
        return answer;
    }

    protected Collection<ServiceEndpoint> getInternalEndpoints()
    {
        return internalEndpoints.values();
    }

    protected Collection<ServiceEndpoint> getExternalEndpoints()
    {
        return externalEndpoints.values();
    }

    public void registerEndpointConnection( QName fromSvc, String fromEp, QName toSvc, String toEp, String link )
        throws JBIException
    {
        LinkedEndpoint ep = new LinkedEndpoint( fromSvc, fromEp, toSvc, toEp, link );
        if ( linkedEndpoints.get( getKey( ep ) ) != null )
        {
            throw new JBIException( "An endpoint connection for service " + ep.getServiceName() + " and name "
                + ep.getEndpointName() + " is already registered" );
        }

        linkedEndpoints.put( getKey( ep ), ep );
        registerEndpoint( ep );
    }

    public void unregisterEndpointConnection( QName fromSvc, String fromEp )
    {
        LinkedEndpoint ep = (LinkedEndpoint) linkedEndpoints.remove( getKey( fromSvc, fromEp ) );
        unregisterEndpoint( ep );
    }

    public void registerInterfaceConnection( QName fromItf, QName toSvc, String toEp )
        throws JBIException
    {
        if ( interfaceConnections.get( fromItf ) != null )
        {
            throw new JBIException( "An interface connection for " + fromItf + " is already registered" );
        }
        interfaceConnections.put( fromItf, new InterfaceConnection( toSvc, toEp ) );
    }

    public void unregisterInterfaceConnection( QName fromItf )
    {
        interfaceConnections.remove( fromItf );
    }

    private void registerEndpoint( AbstractEndpoint serviceEndpoint )
    {
        try
        {
            Endpoint endpoint = new Endpoint( serviceEndpoint, registry );
            ObjectName objectName = registry.getContainer().getManagementContext().createObjectName( endpoint );
            registry.getContainer().getManagementContext().registerMBean( objectName, endpoint, EndpointMBean.class );
            endpointMBeans.put( serviceEndpoint, endpoint );
        }
        catch ( JMException e )
        {
            LOG.error( "Could not register MBean for endpoint", e );
        }
    }

    private void unregisterEndpoint( AbstractEndpoint se )
    {
        Endpoint ep = endpointMBeans.remove( se );
        try
        {
            registry.getContainer().getManagementContext().unregisterMBean( ep );
        }
        catch ( JBIException e )
        {
            LOG.error( "Could not unregister MBean for endpoint", e );
        }
    }

    private String getKey( ServiceEndpoint ep )
    {
        return getKey( ep.getServiceName(), ep.getEndpointName() );
    }

    private String getKey( QName svcName, String epName )
    {
        return svcName + epName;
    }

    private static class InterfaceConnection
    {
        QName service;

        String endpoint;

        InterfaceConnection( QName service, String endpoint )
        {
            this.service = service;
            this.endpoint = endpoint;
        }
    }
}