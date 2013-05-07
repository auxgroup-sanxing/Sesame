package com.sanxing.sesame.mbean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.container.ServiceAssemblyEnvironment;
import com.sanxing.sesame.deployment.ServiceAssembly;
import com.sanxing.sesame.deployment.ServiceUnit;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.resolver.URIResolver;
import com.sanxing.sesame.servicedesc.AbstractEndpoint;
import com.sanxing.sesame.servicedesc.DynamicEndpoint;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import com.sanxing.sesame.util.W3CUtil;

public class Registry
    extends BaseSystemService
    implements RegistryMBean
{
    private static final Logger LOG = LoggerFactory.getLogger( Registry.class );

    private final ComponentRegistry componentRegistry;

    private final EndpointRegistry endpointRegistry;

    private final ServiceAssemblyRegistry serviceAssemblyRegistry;

    private final Map<String, SharedLibrary> sharedLibraries;

    private final Map<String, ServiceUnitLifeCycle> serviceUnits;

    private final List<ServiceAssemblyLifeCycle> pendingAssemblies;

    private final List<ComponentMBeanImpl> pendingComponents;

    private Executor executor;

    public Registry()
    {
        componentRegistry = new ComponentRegistry( this );
        endpointRegistry = new EndpointRegistry( this );

        serviceAssemblyRegistry = new ServiceAssemblyRegistry( this );
        serviceUnits = new ConcurrentHashMap();
        pendingAssemblies = new CopyOnWriteArrayList();
        sharedLibraries = new ConcurrentHashMap();
        pendingComponents = new CopyOnWriteArrayList();
    }

    @Override
    public String getDescription()
    {
        return "Registry of Components/SU's and Endpoints";
    }

    @Override
    protected Class getServiceMBean()
    {
        return RegistryMBean.class;
    }

    public ComponentRegistry getComponentRegistry()
    {
        return componentRegistry;
    }

    public EndpointRegistry getEndpointRegistry()
    {
        return endpointRegistry;
    }

    @Override
    public void init( JBIContainer container )
        throws JBIException
    {
        super.init( container );
        executor = container.getExecutorFactory().createExecutor( "services.registry" );
    }

    @Override
    public void start()
        throws JBIException
    {
        componentRegistry.start();
        serviceAssemblyRegistry.start();
        super.start();
    }

    @Override
    public void stop()
        throws JBIException
    {
        serviceAssemblyRegistry.stop();
        componentRegistry.stop();
        super.stop();
    }

    @Override
    public void shutDown()
        throws JBIException
    {
        serviceAssemblyRegistry.shutDown();
        componentRegistry.shutDown();
        super.shutDown();
        container.getManagementContext().unregisterMBean( this );
    }

    protected EnvironmentContext getEnvironmentContext()
    {
        return container.getEnvironmentContext();
    }

    protected InternalEndpoint matchEndpointByName( ServiceEndpoint[] endpoints, String endpointName )
    {
        InternalEndpoint result = null;
        if ( ( endpoints != null ) && ( endpointName != null ) && ( endpointName.length() > 0 ) )
        {
            for ( int i = 0; i < endpoints.length; ++i )
            {
                if ( endpoints[i].getEndpointName().equals( endpointName ) )
                {
                    result = (InternalEndpoint) endpoints[i];
                    break;
                }
            }
        }
        return result;
    }

    public ServiceEndpoint activateEndpoint( ComponentContextImpl context, QName serviceName, String endpointName )
        throws JBIException
    {
        return endpointRegistry.registerInternalEndpoint( context, serviceName, endpointName );
    }

    public ServiceEndpoint[] getEndpointsForComponent( ComponentNameSpace cns )
    {
        return endpointRegistry.getEndpointsForComponent( cns );
    }

    public ServiceEndpoint[] getEndpointsForInterface( QName interfaceName )
    {
        return endpointRegistry.getEndpointsForInterface( interfaceName );
    }

    public void deactivateEndpoint( ComponentContext provider, InternalEndpoint serviceEndpoint )
    {
        endpointRegistry.unregisterInternalEndpoint( provider, serviceEndpoint );
    }

    public Document getEndpointDescriptor( ServiceEndpoint endpoint )
        throws JBIException
    {
        if ( !( endpoint instanceof AbstractEndpoint ) )
        {
            throw new JBIException( "Descriptors can not be queried for external endpoints" );
        }
        AbstractEndpoint se = (AbstractEndpoint) endpoint;

        ComponentMBeanImpl component = getComponent( se.getComponentNameSpace() );
        return component.getComponent().getServiceDescription( endpoint );
    }

    public ServiceEndpoint resolveEndpointReference( DocumentFragment epr )
    {
        for ( ComponentMBeanImpl connector : getComponents() )
        {
            ServiceEndpoint se = connector.getComponent().resolveEndpointReference( epr );
            if ( se != null )
            {
                return new DynamicEndpoint( connector.getComponentNameSpace(), se, epr );
            }
        }
        ServiceEndpoint se = resolveInternalEPR( epr );
        if ( se != null )
        {
            return se;
        }
        return resolveStandardEPR( epr );
    }

    public ServiceEndpoint resolveInternalEPR( DocumentFragment epr )
    {
        if ( epr == null )
        {
            throw new NullPointerException( "resolveInternalEPR(epr) called with null epr." );
        }
        NodeList nl = epr.getChildNodes();
        for ( int i = 0; i < nl.getLength(); ++i )
        {
            Node n = nl.item( i );
            if ( n.getNodeType() != 1 )
            {
                continue;
            }
            Element el = (Element) n;

            if ( el.getNamespaceURI() == null )
            {
                continue;
            }
            if ( !( el.getNamespaceURI().equals( "http://java.sun.com/jbi/end-point-reference" ) ) )
            {
                continue;
            }

            if ( el.getLocalName() == null )
            {
                continue;
            }
            if ( !( el.getLocalName().equals( "end-point-reference" ) ) )
            {
                continue;
            }
            String serviceName = el.getAttributeNS( el.getNamespaceURI(), "service-name" );

            QName serviceQName = W3CUtil.createQName( el, serviceName );
            String endpointName = el.getAttributeNS( el.getNamespaceURI(), "end-point-name" );
            return getInternalEndpoint( serviceQName, endpointName );
        }
        return null;
    }

    public ServiceEndpoint resolveStandardEPR( DocumentFragment epr )
    {
        try
        {
            NodeList children = epr.getChildNodes();
            for ( int i = 0; i < children.getLength(); ++i )
            {
                Node n = children.item( i );
                if ( n.getNodeType() != 1 )
                {
                    continue;
                }
                Element elem = (Element) n;
                String[] namespaces =
                    { "http://www.w3.org/2005/08/addressing", "http://schemas.xmlsoap.org/ws/2004/08/addressing",
                        "http://schemas.xmlsoap.org/ws/2004/03/addressing",
                        "http://schemas.xmlsoap.org/ws/2003/03/addressing" };

                NodeList nl = null;
                for ( int ns = 0; ns < namespaces.length; ++ns )
                {
                    NodeList tnl = elem.getElementsByTagNameNS( namespaces[ns], "Address" );
                    if ( tnl.getLength() == 1 )
                    {
                        nl = tnl;
                        break;
                    }
                }
                if ( nl != null )
                {
                    Element address = (Element) nl.item( 0 );
                    String uri = W3CUtil.getElementText( address );
                    if ( uri != null )
                    {
                        uri = uri.trim();
                        if ( uri.startsWith( "endpoint:" ) )
                        {
                            uri = uri.substring( "endpoint:".length() );
                            String[] parts = URIResolver.split3( uri );
                            return getInternalEndpoint( new QName( parts[0], parts[1] ), parts[2] );
                        }
                        if ( uri.startsWith( "service:" ) )
                        {
                            uri = uri.substring( "service:".length() );
                            String[] parts = URIResolver.split2( uri );
                            return getEndpoint( new QName( parts[0], parts[1] ), parts[1] );
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            LOG.debug( "Unable to resolve EPR: " + e );
        }
        return null;
    }

    public void registerExternalEndpoint( ComponentNameSpace cns, ServiceEndpoint externalEndpoint )
        throws JBIException
    {
        if ( externalEndpoint != null )
        {
            endpointRegistry.registerExternalEndpoint( cns, externalEndpoint );
        }
    }

    public void deregisterExternalEndpoint( ComponentNameSpace cns, ServiceEndpoint externalEndpoint )
    {
        endpointRegistry.unregisterExternalEndpoint( cns, externalEndpoint );
    }

    public ServiceEndpoint getEndpoint( QName service, String name )
    {
        return endpointRegistry.getEndpoint( service, name );
    }

    public ServiceEndpoint getInternalEndpoint( QName service, String name )
    {
        return endpointRegistry.getInternalEndpoint( service, name );
    }

    public ServiceEndpoint[] getEndpointsForService( QName serviceName )
    {
        return endpointRegistry.getEndpointsForService( serviceName );
    }

    public ServiceEndpoint[] getExternalEndpoints( QName interfaceName )
    {
        return endpointRegistry.getExternalEndpointsForInterface( interfaceName );
    }

    public ServiceEndpoint[] getExternalEndpointsForService( QName serviceName )
    {
        return endpointRegistry.getExternalEndpointsForService( serviceName );
    }

    public ComponentMBeanImpl registerComponent( ComponentNameSpace name, String description, Component component,
                                                 boolean binding, boolean service, String[] sharedLibs )
        throws JBIException
    {
        return componentRegistry.registerComponent( name, description, component, binding, service, sharedLibs );
    }

    public void deregisterComponent( ComponentMBeanImpl component )
    {
        componentRegistry.deregisterComponent( component );
    }

    public Collection<ComponentMBeanImpl> getComponents()
    {
        return componentRegistry.getComponents();
    }

    public ComponentMBeanImpl getComponent( ComponentNameSpace cns )
    {
        return componentRegistry.getComponent( cns );
    }

    public ComponentMBeanImpl getComponent( String name )
    {
        ComponentNameSpace cns = new ComponentNameSpace( container.getName(), name );
        return getComponent( cns );
    }

    public ObjectName[] getEngineComponents()
    {
        ObjectName[] result = null;
        List tmpList = new ArrayList();
        for ( ComponentMBeanImpl lcc : getComponents() )
        {
            if ( ( !( lcc.isPojo() ) ) && ( lcc.isService() ) && ( lcc.getMBeanName() != null ) )
            {
                tmpList.add( lcc.getMBeanName() );
            }
        }
        result = new ObjectName[tmpList.size()];
        tmpList.toArray( result );
        return result;
    }

    public ObjectName[] getBindingComponents()
    {
        ObjectName[] result = null;
        List tmpList = new ArrayList();
        for ( ComponentMBeanImpl lcc : getComponents() )
        {
            if ( ( !( lcc.isPojo() ) ) && ( lcc.isBinding() ) && ( lcc.getMBeanName() != null ) )
            {
                tmpList.add( lcc.getMBeanName() );
            }
        }
        result = new ObjectName[tmpList.size()];
        tmpList.toArray( result );
        return result;
    }

    public ObjectName[] getPojoComponents()
    {
        ObjectName[] result = null;
        List tmpList = new ArrayList();
        for ( ComponentMBeanImpl lcc : getComponents() )
        {
            if ( ( lcc.isPojo() ) && ( lcc.getMBeanName() != null ) )
            {
                tmpList.add( lcc.getMBeanName() );
            }
        }
        result = new ObjectName[tmpList.size()];
        tmpList.toArray( result );
        return result;
    }

    public ServiceAssemblyLifeCycle registerServiceAssembly( ServiceAssembly sa, ServiceAssemblyEnvironment env )
        throws DeploymentException
    {
        return serviceAssemblyRegistry.register( sa, env );
    }

    public ServiceAssemblyLifeCycle registerServiceAssembly( ServiceAssembly sa, String[] suKeys,
                                                             ServiceAssemblyEnvironment env )
        throws DeploymentException
    {
        return serviceAssemblyRegistry.register( sa, suKeys, env );
    }

    public boolean unregisterServiceAssembly( String saName )
    {
        return serviceAssemblyRegistry.unregister( saName );
    }

    public ServiceAssemblyLifeCycle getServiceAssembly( String saName )
    {
        return serviceAssemblyRegistry.getServiceAssembly( saName );
    }

    public ServiceUnitLifeCycle[] getDeployedServiceUnits( String componentName )
    {
        List tmpList = new ArrayList();
        for ( ServiceUnitLifeCycle su : serviceUnits.values() )
        {
            if ( su.getComponentName().equals( componentName ) )
            {
                tmpList.add( su );
            }
        }
        ServiceUnitLifeCycle[] result = new ServiceUnitLifeCycle[tmpList.size()];
        tmpList.toArray( result );
        return result;
    }

    public Collection<ServiceUnitLifeCycle> getServiceUnits()
    {
        return serviceUnits.values();
    }

    public Collection<ServiceAssemblyLifeCycle> getServiceAssemblies()
    {
        return serviceAssemblyRegistry.getServiceAssemblies();
    }

    public String[] getDeployedServiceAssemblies()
    {
        return serviceAssemblyRegistry.getDeployedServiceAssemblies();
    }

    public String[] getDeployedServiceAssembliesForComponent( String componentName )
    {
        return serviceAssemblyRegistry.getDeployedServiceAssembliesForComponent( componentName );
    }

    public String[] getComponentsForDeployedServiceAssembly( String saName )
    {
        return serviceAssemblyRegistry.getComponentsForDeployedServiceAssembly( saName );
    }

    public boolean isSADeployedServiceUnit( String componentName, String suName )
    {
        return serviceAssemblyRegistry.isDeployedServiceUnit( componentName, suName );
    }

    public ServiceUnitLifeCycle getServiceUnit( String suKey )
    {
        return serviceUnits.get( suKey );
    }

    public String registerServiceUnit( ServiceUnit su, String saName, File suDir )
    {
        ServiceUnitLifeCycle sulc = new ServiceUnitLifeCycle( su, saName, this, suDir );
        serviceUnits.put( sulc.getKey(), sulc );
        try
        {
            ObjectName objectName = getContainer().getManagementContext().createObjectName( sulc );
            getContainer().getManagementContext().registerMBean( objectName, sulc, ServiceUnitMBean.class );
        }
        catch ( JMException e )
        {
            LOG.error( "Could not register MBean for service unit", e );
        }
        return sulc.getKey();
    }

    public void unregisterServiceUnit( String suKey )
    {
        ServiceUnitLifeCycle sulc = serviceUnits.remove( suKey );
        if ( sulc == null )
        {
            return;
        }
        try
        {
            getContainer().getManagementContext().unregisterMBean( sulc );
        }
        catch ( JBIException e )
        {
            LOG.error( "Could not unregister MBean for service unit", e );
        }
    }

    public void registerSharedLibrary( com.sanxing.sesame.deployment.SharedLibrary sl, File installationDir )
    {
        SharedLibrary library = new SharedLibrary( sl, installationDir );
        sharedLibraries.put( library.getName(), library );
        try
        {
            ObjectName objectName = getContainer().getManagementContext().createObjectName( library );
            getContainer().getManagementContext().registerMBean( objectName, library, SharedLibraryMBean.class );
        }
        catch ( JMException e )
        {
            LOG.error( "Could not register MBean for service unit", e );
        }
        checkPendingComponents();
    }

    public void unregisterSharedLibrary( String name )
    {
        SharedLibrary sl = sharedLibraries.remove( name );
        if ( sl == null )
        {
            return;
        }
        try
        {
            getContainer().getManagementContext().unregisterMBean( sl );
            sl.dispose();
        }
        catch ( JBIException e )
        {
            LOG.error( "Could not unregister MBean for shared library", e );
        }
    }

    public SharedLibrary getSharedLibrary( String name )
    {
        return sharedLibraries.get( name );
    }

    public Collection<SharedLibrary> getSharedLibraries()
    {
        return sharedLibraries.values();
    }

    public void registerEndpointConnection( QName fromSvc, String fromEp, QName toSvc, String toEp, String link )
        throws JBIException
    {
        endpointRegistry.registerEndpointConnection( fromSvc, fromEp, toSvc, toEp, link );
    }

    public void unregisterEndpointConnection( QName fromSvc, String fromEp )
    {
        endpointRegistry.unregisterEndpointConnection( fromSvc, fromEp );
    }

    public void registerInterfaceConnection( QName fromItf, QName toSvc, String toEp )
        throws JBIException
    {
        endpointRegistry.registerInterfaceConnection( fromItf, toSvc, toEp );
    }

    public void unregisterInterfaceConnection( QName fromItf )
    {
        endpointRegistry.unregisterInterfaceConnection( fromItf );
    }

    public void registerRemoteEndpoint( ServiceEndpoint endpoint )
    {
        endpointRegistry.registerRemoteEndpoint( (InternalEndpoint) endpoint );
    }

    public void unregisterRemoteEndpoint( ServiceEndpoint endpoint )
    {
        endpointRegistry.unregisterRemoteEndpoint( (InternalEndpoint) endpoint );
    }

    public void checkPendingAssemblies()
    {
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                Registry.this.startPendingAssemblies();
            }
        } );
    }

    public void addPendingAssembly( ServiceAssemblyLifeCycle sa )
    {
        if ( !( pendingAssemblies.contains( sa ) ) )
        {
            pendingAssemblies.add( sa );
        }
    }

    protected synchronized void startPendingAssemblies()
    {
        for ( ServiceAssemblyLifeCycle sa : pendingAssemblies )
        {
            ServiceUnitLifeCycle[] sus = sa.getDeployedSUs();
            boolean ok = true;
            for ( int i = 0; i < sus.length; ++i )
            {
                ComponentMBeanImpl c = getComponent( sus[i].getComponentName() );
                if ( ( c == null ) || ( !( c.isStarted() ) ) )
                {
                    ok = false;
                    break;
                }
            }
            if ( ok )
            {
                try
                {
                    sa.restore();
                    pendingAssemblies.remove( sa );
                }
                catch ( Exception e )
                {
                    LOG.error( "Error trying to restore service assembly state", e );
                }
            }
        }
    }

    public void checkPendingComponents()
    {
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                Registry.this.startPendingComponents();
            }
        } );
    }

    public void addPendingComponent( ComponentMBeanImpl comp )
    {
        if ( !( pendingComponents.contains( comp ) ) )
        {
            pendingComponents.add( comp );
        }
    }

    protected synchronized void startPendingComponents()
    {
        ComponentMBeanImpl lcc;
        for ( Iterator i$ = pendingComponents.iterator(); i$.hasNext(); lcc = (ComponentMBeanImpl) i$.next() )
        {
            ;
        }
    }

    @Override
    public ObjectName[] getComponentNames()
    {
        List tmpList = new ArrayList();
        for ( ComponentMBeanImpl lcc : getComponents() )
        {
            tmpList.add( container.getManagementContext().createObjectName( lcc ) );
        }
        return ( (ObjectName[]) tmpList.toArray( new ObjectName[tmpList.size()] ) );
    }

    @Override
    public ObjectName[] getEndpointNames()
    {
        List tmpList = new ArrayList();
        for ( Endpoint ep : container.getRegistry().getEndpointRegistry().getEndpointMBeans() )
        {
            tmpList.add( container.getManagementContext().createObjectName( ep ) );
        }
        return ( (ObjectName[]) tmpList.toArray( new ObjectName[tmpList.size()] ) );
    }

    @Override
    public ObjectName[] getServiceAssemblyNames()
    {
        List tmpList = new ArrayList();
        for ( ServiceAssemblyLifeCycle sa : getServiceAssemblies() )
        {
            tmpList.add( container.getManagementContext().createObjectName( sa ) );
        }
        return ( (ObjectName[]) tmpList.toArray( new ObjectName[tmpList.size()] ) );
    }

    @Override
    public ObjectName[] getServiceUnitNames()
    {
        List tmpList = new ArrayList();
        for ( ServiceUnitLifeCycle su : serviceUnits.values() )
        {
            tmpList.add( container.getManagementContext().createObjectName( su ) );
        }
        return ( (ObjectName[]) tmpList.toArray( new ObjectName[tmpList.size()] ) );
    }

    @Override
    public ObjectName[] getSharedLibraryNames()
    {
        List tmpList = new ArrayList();
        for ( SharedLibrary sl : sharedLibraries.values() )
        {
            tmpList.add( container.getManagementContext().createObjectName( sl ) );
        }
        return ( (ObjectName[]) tmpList.toArray( new ObjectName[tmpList.size()] ) );
    }

    @Override
    public MBeanAttributeInfo[] getAttributeInfos()
        throws JMException
    {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute( getObjectToManage(), "componentNames", "list of components" );
        helper.addAttribute( getObjectToManage(), "serviceUnitNames", "list of service units" );
        helper.addAttribute( getObjectToManage(), "serviceAssemblyNames", "list of service assemblies" );
        helper.addAttribute( getObjectToManage(), "endpointNames", "list of endpoints" );
        helper.addAttribute( getObjectToManage(), "sharedLibraryNames", "list of shared libraries" );
        return AttributeInfoHelper.join( super.getAttributeInfos(), helper.getAttributeInfos() );
    }

    public void cancelPendingExchanges()
    {
        for ( ComponentMBeanImpl mbean : componentRegistry.getComponents() )
        {
            DeliveryChannel channel = mbean.getDeliveryChannel();
            if ( channel == null )
            {
                ;
            }
        }
    }
}