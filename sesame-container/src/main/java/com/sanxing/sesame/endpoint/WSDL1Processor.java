package com.sanxing.sesame.endpoint;

import java.util.Iterator;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.servicedesc.InternalEndpoint;

public class WSDL1Processor
    implements EndpointProcessor
{
    public static final String WSDL1_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";

    private static final Logger LOG = LoggerFactory.getLogger( WSDL1Processor.class );

    private Registry registry;

    @Override
    public void init( Registry reg )
    {
        registry = reg;
    }

    @Override
    public void process( InternalEndpoint serviceEndpoint )
    {
        try
        {
            Document document = registry.getEndpointDescriptor( serviceEndpoint );
            if ( ( document == null ) || ( document.getDocumentElement() == null ) )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Endpoint " + serviceEndpoint + " has no service description" );
                }
                return;
            }
            if ( !( "http://schemas.xmlsoap.org/wsdl/".equals( document.getDocumentElement().getNamespaceURI() ) ) )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Endpoint " + serviceEndpoint + " has a non WSDL1 service description" );
                }
                return;
            }
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            reader.setFeature( "javax.wsdl.verbose", false );
            Definition definition = reader.readWSDL( document.getBaseURI(), document );

            if ( ( definition.getPortTypes().keySet().size() >= 1 ) && ( definition.getServices().keySet().size() == 0 ) )
            {
                Iterator it = definition.getPortTypes().values().iterator();
                while ( it.hasNext() )
                {
                    PortType portType = (PortType) it.next();
                    QName interfaceName = portType.getQName();
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( "Endpoint " + serviceEndpoint + " implements interface " + interfaceName );
                    }
                    serviceEndpoint.addInterface( interfaceName );
                }
            }
            else
            {
                Service service = definition.getService( serviceEndpoint.getServiceName() );
                if ( service == null )
                {
                    LOG.info( "Endpoint " + serviceEndpoint
                        + " has a service description, but no matching service found in "
                        + definition.getServices().keySet() );

                    return;
                }
                Port port = service.getPort( serviceEndpoint.getEndpointName() );
                if ( port == null )
                {
                    LOG.info( "Endpoint " + serviceEndpoint
                        + " has a service description, but no matching endpoint found in "
                        + service.getPorts().keySet() );

                    return;
                }
                if ( port.getBinding() == null )
                {
                    LOG.info( "Endpoint " + serviceEndpoint + " has a service description, but no binding found" );
                    return;
                }
                if ( port.getBinding().getPortType() == null )
                {
                    LOG.info( "Endpoint " + serviceEndpoint + " has a service description, but no port type found" );
                    return;
                }
                QName interfaceName = port.getBinding().getPortType().getQName();
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Endpoint " + serviceEndpoint + " implements interface " + interfaceName );
                }
                serviceEndpoint.addInterface( interfaceName );
            }
        }
        catch ( Exception e )
        {
            LOG.warn( "Error retrieving interfaces from service description: " + e.getMessage() );
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Error retrieving interfaces from service description", e );
            }
        }
    }
}