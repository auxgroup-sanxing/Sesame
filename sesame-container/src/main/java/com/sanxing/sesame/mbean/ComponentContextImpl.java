package com.sanxing.sesame.mbean;

import java.util.MissingResourceException;
import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.management.MBeanNames;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import com.sanxing.sesame.container.ActivationSpec;
import com.sanxing.sesame.container.ComponentEnvironment;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.servicedesc.InternalEndpoint;

public class ComponentContextImpl
    implements ComponentContext, MBeanNames
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( ComponentContextImpl.class );

    private final ComponentNameSpace componentName;

    private ComponentEnvironment environment;

    private JBIContainer container;

    private Component component;

    private DeliveryChannel deliveryChannel;

    private ActivationSpec activationSpec;

    private boolean activated;

    public ComponentContextImpl( JBIContainer container, ComponentNameSpace componentName )
    {
        this.componentName = componentName;
        this.container = container;
    }

    public void activate( Component comp, ComponentEnvironment env, ActivationSpec spec )
    {
        component = comp;
        environment = env;
        activationSpec = spec;
        activated = true;
    }

    public ComponentNameSpace getComponentNameSpace()
    {
        return componentName;
    }

    @Override
    public String getComponentName()
    {
        return componentName.getName();
    }

    public Component getComponent()
    {
        return component;
    }

    @Override
    public ServiceEndpoint activateEndpoint( QName serviceName, String endpointName )
        throws JBIException
    {
        checkActivated();
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "Component: " + componentName.getName() + " activated endpoint: " + serviceName + " : "
                + endpointName );
        }
        return container.getRegistry().activateEndpoint( this, serviceName, endpointName );
    }

    public ServiceEndpoint[] availableEndpoints( QName serviceName )
        throws JBIException
    {
        checkActivated();
        return container.getRegistry().getEndpointsForService( serviceName );
    }

    @Override
    public void deactivateEndpoint( ServiceEndpoint endpoint )
        throws JBIException
    {
        checkActivated();
        container.getRegistry().deactivateEndpoint( this, (InternalEndpoint) endpoint );
    }

    @Override
    public DeliveryChannel getDeliveryChannel()
    {
        return deliveryChannel;
    }

    @Override
    public String getJmxDomainName()
    {
        return container.getName();
    }

    @Override
    public ObjectName createCustomComponentMBeanName( String customName )
    {
        return container.getManagementContext().createCustomComponentMBeanName( customName, componentName.getName() );
    }

    @Override
    public MBeanNames getMBeanNames()
    {
        return this;
    }

    @Override
    public MBeanServer getMBeanServer()
    {
        return container.getMBeanServer();
    }

    @Override
    public InitialContext getNamingContext()
    {
        return container.getNamingContext();
    }

    @Override
    public Object getTransactionManager()
    {
        return container.getTransactionManager();
    }

    @Override
    public String getWorkspaceRoot()
    {
        if ( environment.getWorkspaceRoot() != null )
        {
            return environment.getWorkspaceRoot().getAbsolutePath();
        }
        return null;
    }

    public JBIContainer getContainer()
    {
        return container;
    }

    public ComponentEnvironment getEnvironment()
    {
        return environment;
    }

    public void setEnvironment( ComponentEnvironment ce )
    {
        environment = ce;
    }

    public void setContainer( JBIContainer container )
    {
        this.container = container;
    }

    public void setDeliveryChannel( DeliveryChannel deliveryChannel )
    {
        this.deliveryChannel = deliveryChannel;
    }

    @Override
    public void registerExternalEndpoint( ServiceEndpoint externalEndpoint )
        throws JBIException
    {
        checkActivated();
        if ( externalEndpoint == null )
        {
            throw new IllegalArgumentException( "externalEndpoint should be non null" );
        }
        container.getRegistry().registerExternalEndpoint( getComponentNameSpace(), externalEndpoint );
    }

    @Override
    public void deregisterExternalEndpoint( ServiceEndpoint externalEndpoint )
        throws JBIException
    {
        checkActivated();
        container.getRegistry().deregisterExternalEndpoint( getComponentNameSpace(), externalEndpoint );
    }

    @Override
    public ServiceEndpoint resolveEndpointReference( DocumentFragment epr )
    {
        checkActivated();
        return container.getRegistry().resolveEndpointReference( epr );
    }

    @Override
    public ServiceEndpoint getEndpoint( QName service, String name )
    {
        checkActivated();
        return container.getRegistry().getEndpoint( service, name );
    }

    @Override
    public Document getEndpointDescriptor( ServiceEndpoint endpoint )
        throws JBIException
    {
        checkActivated();
        return container.getRegistry().getEndpointDescriptor( endpoint );
    }

    @Override
    public ServiceEndpoint[] getEndpoints( QName interfaceName )
    {
        checkActivated();
        return container.getRegistry().getEndpointsForInterface( interfaceName );
    }

    @Override
    public ServiceEndpoint[] getEndpointsForService( QName serviceName )
    {
        checkActivated();
        return container.getRegistry().getEndpointsForService( serviceName );
    }

    @Override
    public ServiceEndpoint[] getExternalEndpoints( QName interfaceName )
    {
        checkActivated();
        return container.getRegistry().getExternalEndpoints( interfaceName );
    }

    @Override
    public ServiceEndpoint[] getExternalEndpointsForService( QName serviceName )
    {
        checkActivated();
        return container.getRegistry().getExternalEndpointsForService( serviceName );
    }

    @Override
    public String getInstallRoot()
    {
        if ( environment.getInstallRoot() != null )
        {
            return environment.getInstallRoot().getAbsolutePath();
        }
        return null;
    }

    @Override
    public Logger getLogger( String suffix, String filename )
        throws MissingResourceException, JBIException
    {
        String name = ( suffix != null ) ? suffix : "";

        return container.getLogger( name, filename );
    }

    public ActivationSpec getActivationSpec()
    {
        return activationSpec;
    }

    private void checkActivated()
    {
        if ( !( activated ) )
        {
            throw new IllegalStateException( "ComponentContext not activated" );
        }
    }
}