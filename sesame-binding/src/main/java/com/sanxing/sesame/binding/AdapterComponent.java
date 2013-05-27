package com.sanxing.sesame.binding;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.JBIException;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.Encoder;
import com.sanxing.sesame.binding.codec.FaultHandler;
import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.binding.transport.TransportFactory;
import com.sanxing.sesame.component.BindingComponent;
import com.sanxing.sesame.constants.ExchangeConst;
import com.sanxing.sesame.service.ServiceUnit;

public class AdapterComponent
    extends BindingComponent
    implements ServiceUnitManager
{
    private static Logger LOG = LoggerFactory.getLogger( AdapterComponent.class );

    private final Codec codec = new Codec();

    private Class<? extends Binding> bindingImpl = DefaultBinding.class;

    private final Map<String, Class<?>> transportImpls = new HashMap();

    private final Map<String, List<Binding>> mappings = new ConcurrentHashMap();

    private final Map<ServiceEndpoint, Binding> bindings = new ConcurrentHashMap();

    protected Carrier carrier;

    @Override
    public void onMessageExchange( MessageExchange exchange )
        throws MessagingException
    {
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "Exchange [" + exchange.getExchangeId() + "] arriving at BC [" + getContext().getComponentName()
                + "]" );
            LOG.debug( "Component Role: "
                + ( ( exchange.getRole() == MessageExchange.Role.PROVIDER ) ? "provider" : "consumer" ) );
        }
        try
        {
            if ( exchange.getRole() == MessageExchange.Role.PROVIDER )
            {
                exchange.setProperty( ExchangeConst.PROVIDER, getContext().getComponentName() );

                ServiceEndpoint endpoint = exchange.getEndpoint();
                Binding binding = bindings.get( endpoint );
                if ( binding != null )
                {
                    carrier.dispatchMessage( exchange, binding );
                    return;
                }

                exchange.setStatus( ExchangeStatus.ERROR );
                exchange.setError( new BindingException( "Component[" + getName() + "], Binding not found" ) );
                send( exchange );
                return;
            }

            exchange.setProperty( ExchangeConst.CONSUMER, getContext().getComponentName() );
            QName serviceName = (QName) exchange.getProperty( Carrier.BINDING_SERVICE_NAME );
            String endpointName = (String) exchange.getProperty( Carrier.BINDING_ENDPOINT_NAME );
            ServiceEndpoint endpoint = getContext().getEndpoint( serviceName, endpointName );
            Binding binding = bindings.get( endpoint );
            if ( binding != null )
            {
                carrier.dispatchMessage( exchange, binding );
                return;
            }

            LOG.error( "Binding was not found, exchangeId:" + exchange.getExchangeId() );
        }
        catch ( MessagingException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            if ( exchange.getRole() == MessageExchange.Role.PROVIDER )
            {
                exchange.setStatus( ExchangeStatus.ERROR );
                exchange.setError( ( e.getCause() != null ) ? (Exception) e.getCause() : e );
                send( exchange );
            }
        }
    }

    @Override
    protected void init()
        throws JBIException
    {
        super.init();

        carrier = new Carrier( this );

        String className = null;
        File root = new File( getContext().getInstallRoot() );
        SAXReader reader = new SAXReader();
        try
        {
            File bindingXml = new File( root, "binding.xml" );
            if ( !( bindingXml.exists() ) )
            {
                throw new JBIException( "Can Not Found [binding.xml] in binding component: "
                    + getContext().getComponentName() );
            }
            Document doc = reader.read( bindingXml );
            Element bindingEl = doc.getRootElement();

            className = bindingEl.attributeValue( "impl" );
            if ( className != null )
            {
                bindingImpl = getClassLoader().loadClass( className ).asSubclass( DefaultBinding.class );
            }
            LOG.debug( "Binding Implementor: " + bindingImpl );

            Element allowableEl = bindingEl.element( "allowable" );
            if ( allowableEl != null )
            {
                List<Element> list = allowableEl.elements( "protocol" );

                for ( Element el : list )
                {
                    Attribute attr = el.attribute( "impl" );
                    if ( attr != null )
                    {
                        className = attr.getValue();
                        Class clazz = getClassLoader().loadClass( className );

                        TransportFactory.register( el.attributeValue( "name" ), clazz.asSubclass( Transport.class ) );
                        transportImpls.put( el.attributeValue( "name" ), clazz );
                    }
                }
            }
            Element codecEl = bindingEl.element( "codec" );
            if ( codecEl != null )
            {
                Element decodeEl = codecEl.element( "raw2xml" );

                className = decodeEl.elementText( "decoder" );
                Class decoderClazz = getClassLoader().loadClass( className );
                Decoder decoder = (Decoder) decoderClazz.asSubclass( Decoder.class ).newInstance();
                decoder.init( getContext().getWorkspaceRoot() );
                codec.setDecoder( decoder );

                Element encodeEl = codecEl.element( "xml2raw" );
                className = encodeEl.elementText( "encoder" );
                Class encoderClazz = getClassLoader().loadClass( className );

                Encoder encoder = (Encoder) encoderClazz.asSubclass( Encoder.class ).newInstance();
                encoder.init( getContext().getWorkspaceRoot() );
                codec.setEncoder( encoder );

                className = codecEl.elementTextTrim( "fault-handler" );
                if ( ( className != null ) && ( className.length() > 0 ) )
                {
                    Class handlerClazz = getClassLoader().loadClass( className );
                    FaultHandler handler = (FaultHandler) handlerClazz.asSubclass( FaultHandler.class ).newInstance();
                    codec.setFaultHandler( handler );
                }

                Map params = new HashMap();
                Element paramsEl = codecEl.element( "params" );
                if ( paramsEl != null )
                {
                    List<Element> list = paramsEl.elements();
                    for ( Element paramEl : list )
                    {
                        params.put( paramEl.getName(), paramEl.getTextTrim() );
                    }
                }
                codec.setProperties( params );
                return;
            }

            LOG.warn( "Can't find [/binding/codec] element in binding.xml" );
        }
        catch ( JBIException e )
        {
            throw e;
        }
        catch ( ClassNotFoundException e )
        {
            throw taskFailure( "init", "Class not found: " + className );
        }
        catch ( Exception e )
        {
            LOG.debug( e.getMessage(), e );
            throw taskFailure( "init", e.getMessage() );
        }
    }

    @Override
    public void shutDown()
        throws JBIException
    {
        stop();

        LOG.info( "Shutdown component: " + getContext().getComponentName() );
        Set<Map.Entry<String, Class<?>>> entrySet = transportImpls.entrySet();
        for ( Map.Entry entry : entrySet )
        {
            Class clazz = (Class) entry.getValue();
            TransportFactory.unregister( (String) entry.getKey(), clazz.asSubclass( Transport.class ) );
        }
        super.shutDown();
    }

    @Override
    public ServiceEndpoint resolveEndpointReference( DocumentFragment epr )
    {
        return null;
    }

    @Override
    public String deploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return null;
    }

    @Override
    public String undeploy( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
        return null;
    }

    @Override
    public void init( String serviceUnitName, String serviceUnitRootPath )
        throws DeploymentException
    {
    }

    @Override
    public void start( String serviceUnitName )
        throws DeploymentException
    {
        try
        {
            ServiceUnit serviceUnit = getServiceUnit( serviceUnitName );
            bind( serviceUnit );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw taskFailure( "start-" + serviceUnitName, ( e.getMessage() != null ) ? e.getMessage() : e.toString() );
        }
    }

    @Override
    public void stop( String serviceUnitName )
        throws DeploymentException
    {
        try
        {
            ServiceUnit serviceUnit = getServiceUnit( serviceUnitName );
            if ( serviceUnit == null )
            {
                throw new DeploymentException( "Can not find the service unit '" + serviceUnitName + "'", null );
            }
            unbind( serviceUnit );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw taskFailure( "stop " + serviceUnitName, ( e.getMessage() != null ) ? e.getMessage() : e.toString() );
        }
    }

    @Override
    public void shutDown( String serviceUnitName )
        throws DeploymentException
    {
    }

    protected void bind( ServiceUnit serviceUnit )
        throws BindingException, InstantiationException
    {
        try
        {
            Service service = serviceUnit.getService();
            Map ports = service.getPorts();
            for ( Iterator iter = ports.values().iterator(); iter.hasNext(); )
            {
                Port port = (Port) iter.next();
                Constructor constructor = bindingImpl.getConstructor( new Class[0] );
                Binding bindingInst = (Binding) constructor.newInstance( new Object[0] );
                bindingInst.init( codec, serviceUnit, service, port );
                URI uri = bindingInst.bind();

                Transport transport = bindingInst.getTransport();
                String contextPath = ( uri.getPath().length() > 0 ) ? uri.getPath() : "/";
                transport.addCarrier( contextPath, carrier );
                addEntry( transport.hashCode() + contextPath, bindingInst );
                ServiceEndpoint endpoint = getContext().activateEndpoint( service.getQName(), port.getName() );
                bindings.put( endpoint, bindingInst );
            }
        }
        catch ( BindingException e )
        {
            throw e;
        }
        catch ( InstantiationException e )
        {
            throw e;
        }
        catch ( InvocationTargetException e )
        {
            InstantiationException ex = new InstantiationException( e.getMessage() );
            ex.initCause( e.getTargetException() );
            throw ex;
        }
        catch ( Exception e )
        {
            InstantiationException ex = new InstantiationException( e.getMessage() );
            ex.initCause( e );
            throw ex;
        }
    }

    protected void unbind( ServiceUnit serviceUnit )
        throws BindingException, JBIException
    {
        Map ports = serviceUnit.getService().getPorts();
        for ( Iterator iter = ports.values().iterator(); iter.hasNext(); )
        {
            Port port = (Port) iter.next();
            ServiceEndpoint endpoint = getContext().getEndpoint( serviceUnit.getServiceName(), port.getName() );

            if ( endpoint != null )
            {
                getContext().deactivateEndpoint( endpoint );

                Binding binding = bindings.get( endpoint );
                if ( binding != null )
                {
                    URI uri = binding.getAddress();
                    String contextPath = ( uri.getPath().length() > 0 ) ? uri.getPath() : "/";
                    Transport transport = binding.getTransport();
                    transport.removeCarrier( contextPath, carrier );
                    binding.unbind();
                    bindings.remove( endpoint );
                    removeEntry( transport.hashCode() + contextPath, binding );
                }
            }
        }
    }

    private void addEntry( String contextPath, Binding binding )
    {
        LOG.debug( "Add binding entry [" + binding + "] for context path: " + contextPath );
        List list = mappings.get( contextPath );
        if ( list == null )
        {
            mappings.put( contextPath, list = new ArrayList() );
        }

        list.add( binding );
    }

    private void removeEntry( String contextPath, Binding binding )
    {
        List list = mappings.get( contextPath );

        if ( list != null )
        {
            list.remove( binding );
        }
    }

    protected Binding[] getBindings( Transport transport, String path )
    {
        String key = transport.hashCode() + path;
        List list = mappings.get( key );
        if ( list != null )
        {
            return ( (Binding[]) list.toArray( new Binding[list.size()] ) );
        }

        return new Binding[0];
    }

    protected Carrier getCarrier()
    {
        return carrier;
    }

    @Override
    protected ServiceUnit getServiceUnit( ServiceEndpoint endpoint )
    {
        Binding binding = bindings.get( endpoint );
        return ( ( binding != null ) ? binding.getServiceUnit() : null );
    }

    @Override
    protected ServiceUnitManager createServiceUnitManager()
    {
        return this;
    }
}