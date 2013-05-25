package com.sanxing.sesame.binding;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.wsdl.BindingOperation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.apache.ws.commons.schema.XmlSchema;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.DocumentResult;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.w3c.dom.Element;

import com.sanxing.sesame.address.AddressBook;
import com.sanxing.sesame.address.Location;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.codec.XMLResult;
import com.sanxing.sesame.binding.codec.XMLSource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Transport;
import com.sanxing.sesame.binding.transport.TransportFactory;
import com.sanxing.sesame.logging.BufferRecord;
import com.sanxing.sesame.logging.ErrorRecord;
import com.sanxing.sesame.logging.FinishRecord;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import com.sanxing.sesame.logging.LogRecord;
import com.sanxing.sesame.logging.XObjectRecord;
import com.sanxing.sesame.service.OperationContext;
import com.sanxing.sesame.service.ServiceUnit;

public class DefaultBinding
    implements Binding
{
    protected static final String BINDING_SERVICE_NAME = "sesame.binding.service.name";

    protected static final String BINDING_ENDPOINT_NAME = "sesame.binding.endpoint.name";

    private static final Logger LOG = LoggerFactory.getLogger( DefaultBinding.class );

    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private final Map<String, OperationContext> actionMap = new ConcurrentHashMap();

    private Codec codec;

    protected Integer txCode_start = null;

    protected Integer txCode_end = null;

    private XPath txCode_xpath = null;

    private XPath status_xpath = null;

    private XPath statusText_xpath = null;

    private String success_code = null;

    ServiceUnit serviceUnit;

    private QName serviceName;

    private Port port;

    private URI uri;

    private Transport transport;

    @Override
    public URI bind()
        throws BindingException
    {
        try
        {
            Iterator iter = port.getExtensibilityElements().iterator();
            if ( !( iter.hasNext() ) )
            {
                throw new BindingException( "Port address not specified" );
            }
            ExtensibilityElement extEl = (ExtensibilityElement) iter.next();
            SOAPAddress addr = (SOAPAddress) extEl;
            String location = addr.getLocationURI();
            uri = new URI( location );

            QName extensionAttr =
                (QName) port.getExtensionAttribute( new QName( "http://www.sanxing.com/ns/sesame", "style" ) );
            String style = ( extensionAttr != null ) ? extensionAttr.getLocalPart() : null;
            LOG.debug( "Port style: " + style );
            Location loc = AddressBook.find( ( uri.getScheme() != null ) ? uri.getHost() : location );
            if ( loc != null )
            {
                URI real = loc.getURI();
                uri =
                    new URI( real.getScheme(), real.getAuthority(), real.getPath()
                        + ( ( uri.getScheme() != null ) ? uri.getPath() : "" ), uri.getQuery(), uri.getFragment() );
                style = loc.getStyle();
            }
            else if ( uri.getScheme() == null )
            {
                throw new BindingException( "Address not found, invalid url: " + uri );
            }

            String path = ( uri.getPath().length() > 0 ) ? uri.getPath() : "/";
            Element config = ( loc != null ) ? loc.getConfig() : null;

            transport = newTransport( uri.getScheme(), uri.getAuthority(), config, style );
            transport.setConfig( path, getBindingConfig( port ) );

            LOG.debug( "portName: " + port.getName() + ", binding: " + this + ", transport: " + transport );

            javax.wsdl.Binding binding = port.getBinding();
            List<BindingOperation> operations = binding.getBindingOperations();
            for ( BindingOperation operation : operations )
            {
                OperationContext context = serviceUnit.getOperationContext( operation.getName() );
                if ( ( context != null ) && ( context.getReference() != null ) )
                {
                    actionMap.put( operation.getName(), context );
                }
                else
                {
                    Iterator extIterator = operation.getExtensibilityElements().iterator();
                    while ( extIterator.hasNext() )
                    {
                        Object ext = extIterator.next();
                        if ( ext instanceof SOAPOperation )
                        {
                            SOAPOperation op = (SOAPOperation) ext;
                            String action = op.getSoapActionURI();
                            actionMap.put( action, context );
                            break;
                        }
                    }
                }
            }
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Action map of port '" + port.getName() + "' ..." );
                for ( Map.Entry entry : actionMap.entrySet() )
                {
                    LOG.debug( ( (String) entry.getKey() ) + " -> OperationContext"
                        + ( (OperationContext) entry.getValue() ).hashCode() );
                }
                LOG.debug( "--------------------------------------------------------------------------------" );
            }

            return uri;
        }
        catch ( URISyntaxException e )
        {
            throw new BindingException( e.getMessage(), e );
        }
    }

    private Element getBindingConfig( Port port )
    {
        QName elementType = new QName( "http://www.sanxing.com/ns/sesame", "binding" );
        javax.wsdl.Binding binding = port.getBinding();
        List<ExtensibilityElement> list = binding.getExtensibilityElements();
        for ( ExtensibilityElement element : list )
        {
            if ( element.getElementType().equals( elementType ) )
            {
                UnknownExtensibilityElement unkonwn = (UnknownExtensibilityElement) element;
                Element resultEl = unkonwn.getElement();
                return resultEl;
            }
        }
        return null;
    }

    @Override
    public void unbind()
        throws BindingException
    {
        try
        {
            if ( transport != null )
            {
                transport.close();

                javax.wsdl.Binding binding = port.getBinding();
                List<BindingOperation> operations = binding.getBindingOperations();
                for ( BindingOperation operation : operations )
                {
                    Iterator extIterator = operation.getExtensibilityElements().iterator();
                    while ( extIterator.hasNext() )
                    {
                        Object ext = extIterator.next();
                        if ( ext instanceof SOAPOperation )
                        {
                            SOAPOperation op = (SOAPOperation) ext;
                            String action = op.getSoapActionURI();
                            actionMap.remove( action );
                            actionMap.remove( operation.getName() );
                            break;
                        }
                    }
                }
            }
        }
        catch ( IOException e )
        {
            throw new BindingException( e.getMessage(), e );
        }
    }

    protected boolean parseRequest( MessageContext context, Map<String, Object> params )
        throws Exception
    {
        if ( context.getSource() instanceof BinarySource )
        {
            BinarySource binSource = (BinarySource) context.getSource();

            if ( ( context.getAction() == null ) && ( txCode_start != null ) && ( txCode_end != null ) )
            {
                int len = txCode_end.intValue() - txCode_start.intValue();
                byte[] buf = new byte[len];
                System.arraycopy( binSource.getBytes(), txCode_start.intValue(), buf, 0, len );
                context.setAction( new String( buf ) );
            }

            if ( context.getAction() != null )
            {
                MDC.put( "ACTION", context.getAction() );
            }

            XmlSchema schema = null;
            QName elementName = new QName( "undefined" );

            String action = context.getAction();
            OperationContext operaContext = getOperationContext( action );
            if ( operaContext != null )
            {
                context.setProperty( "sesame.exchange.tx.proxy", operaContext.getServiceUnit().getName() );
                context.setProperty( "sesame.exchange.tx.action", action );
                MDC.put( "SU", operaContext.getServiceUnit().getName() );
                schema = operaContext.getSchema();
                elementName = operaContext.getInputElement();
            }
            else if ( action != null )
            {
                return false;
            }

            binSource.setXMLSchema( schema );
            binSource.setElementName( elementName.getLocalPart() );
            
            if ( ( txCode_start != null ) && ( txCode_end != null ) )
            {
                int len = binSource.getBytes().length - txCode_end.intValue();
                byte[] buf = new byte[len];
                System.arraycopy( binSource.getBytes(), txCode_end.intValue(), buf, 0, len );
                binSource.setBytes( buf );
            }

            XMLResult result = new XMLResult();
            codec.getDecoder().decode( binSource, result );

            XMLSource xmlSource = new XMLSource( result.getContent() );
            for ( String name : binSource.getPropertyNames() )
            {
                xmlSource.setProperty( name, binSource.getProperty( name ) );
            }
            context.setSource( xmlSource );
        }

        if ( ( context.getAction() == null ) && ( txCode_xpath != null ) )
        {
            org.jdom.Document request;
            if ( context.getSource() instanceof XMLSource )
            {
                XMLSource content = (XMLSource) context.getSource();
                request = content.getJDOMDocument();
            }
            else
            {
                if ( context.getSource() instanceof JDOMSource )
                {
                    JDOMSource content = (JDOMSource) context.getSource();
                    request = content.getDocument();
                }
                else
                {
                    JDOMResult result = new JDOMResult();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.transform( context.getSource(), result );
                    request = result.getDocument();
                }
            }
            String action = txCode_xpath.valueOf( request );
            context.setAction( action );
            LOG.debug( "Action = " + action );
        }
        String action = context.getAction();
        if ( action == null )
        {
            return false;
        }

        context.setProperty( "sesame.binding.endpoint.name", port.getName() );
        context.setProperty( "sesame.binding.service.name", serviceName );

        return true;
    }

    protected boolean parseResponse( MessageContext context, Map<String, Object> params )
        throws Exception
    {
        if ( context.getResult() instanceof BinaryResult )
        {
            BinaryResult binResult = (BinaryResult) context.getResult();

            XmlSchema schema = null;
            QName elementName = new QName( "undefined" );

            String operationName = (String) context.getProperty( "sesame.binding.operation.name" );
            OperationContext operaContext = serviceUnit.getOperationContext( operationName );
            if ( operaContext != null )
            {
                schema = operaContext.getSchema();
                elementName = operaContext.getOutputElement();
            }
            BinarySource binSource = new BinarySource();
            binSource.setBytes( binResult.getBytes() );
            binSource.setXMLSchema( schema );
            binSource.setElementName( elementName.getLocalPart() );
            binSource.setEncoding( binResult.getEncoding() );

            XMLResult xmlResult = new XMLResult();
            codec.getDecoder().decode( binSource, xmlResult );
            for ( String name : binResult.getPropertyNames() )
            {
                xmlResult.setProperty( name, binResult.getProperty( name ) );
            }
            context.setResult( xmlResult );
        }

        if ( status_xpath != null )
        {
            org.jdom.Document response;
            if ( context.getResult() instanceof XMLResult )
            {
                XMLResult content = (XMLResult) context.getResult();
                response = content.getJDOMDocument();
            }
            else
            {
                if ( context.getResult() instanceof JDOMResult )
                {
                    JDOMResult content = (JDOMResult) context.getResult();
                    response = content.getDocument();
                }
                else
                {
                    if ( context.getResult() instanceof DOMResult )
                    {
                        DOMResult content = (DOMResult) context.getResult();
                        response = new DOMBuilder().build( (org.w3c.dom.Document) content.getNode() );
                    }
                    else
                    {
                        if ( context.getResult() instanceof DocumentResult )
                        {
                            DocumentResult content = (DocumentResult) context.getResult();
                            DOMWriter writer = new DOMWriter();
                            response = new DOMBuilder().build( writer.write( content.getDocument() ) );
                        }
                        else
                        {
                            response = new org.jdom.Document();
                        }
                    }
                }
            }
            if ( ( success_code != null ) && ( success_code.length() > 0 ) )
            {
                String status = status_xpath.valueOf( response );
                if ( ( status != null ) && ( status.length() > 0 ) && ( !( status.equals( success_code ) ) ) )
                {
                    context.setStatus( MessageContext.Status.FAULT );
                    if ( context.getResult() instanceof XMLResult )
                    {
                        XMLResult xmlResult = (XMLResult) context.getResult();
                        xmlResult.setProperty( "response.status.xpath", status_xpath );
                        xmlResult.setProperty( "response.statustext.xpath", statusText_xpath );
                    }
                }
            }
        }
        return true;
    }

    @Override
    public URI getAddress()
    {
        return uri;
    }

    @Override
    public ServiceUnit getServiceUnit()
    {
        return serviceUnit;
    }

    private Transport newTransport( String scheme, String authority, Element config, String style )
        throws URISyntaxException, BindingException
    {
        URI uri = new URI( scheme, authority, null, null, null );
        return TransportFactory.getTransport( uri, config, style );
    }

    public void setCodec( Codec codec )
    {
        this.codec = codec;
        setParameters( codec.getProperties() );
    }

    public Codec getCodec()
    {
        return codec;
    }

    protected void setParameters( Map<String, String> params )
    {
        LOG.debug( "codec-params: " + params );
        try
        {
            String start = params.get( "tx-start" );
            if ( ( start != null ) && ( start.length() > 0 ) )
            {
                txCode_start = Integer.valueOf( Integer.parseInt( start ) );
            }
            String end = params.get( "tx-end" );
            if ( ( end != null ) && ( end.length() > 0 ) )
            {
                txCode_end = Integer.valueOf( Integer.parseInt( end ) );
            }
            String path = params.get( "tx-code" );
            if ( ( path != null ) && ( path.length() > 0 ) )
            {
                txCode_xpath = XPath.newInstance( path );
                txCode_xpath.addNamespace( "xsd", "http://www.w3.org/2001/XMLSchema" );
                txCode_xpath.addNamespace( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
                txCode_xpath.addNamespace( "soapenv", "http://schemas.xmlsoap.org/soap/envelope/" );
            }

            path = params.get( "status" );
            if ( ( path != null ) && ( path.length() > 0 ) )
            {
                status_xpath = XPath.newInstance( path );
                status_xpath.addNamespace( "xsd", "http://www.w3.org/2001/XMLSchema" );
                status_xpath.addNamespace( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
                status_xpath.addNamespace( "soapenv", "http://schemas.xmlsoap.org/soap/envelope/" );
            }

            String statusTextPath = params.get( "status-text" );
            if ( ( path != null ) && ( statusTextPath.length() > 0 ) )
            {
                statusText_xpath = XPath.newInstance( statusTextPath );
                statusText_xpath.addNamespace( "xsd", "http://www.w3.org/2001/XMLSchema" );
                statusText_xpath.addNamespace( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
                statusText_xpath.addNamespace( "soapenv", "http://schemas.xmlsoap.org/soap/envelope/" );
            }

            success_code = params.get( "success-code" );
        }
        catch ( JDOMException e )
        {
            LOG.debug( e.getMessage(), e );
        }
    }

    @Override
    public void init( Codec codec, ServiceUnit serviceUnit, Service service, Port port )
    {
        setCodec( codec );
        this.serviceUnit = serviceUnit;
        this.port = port;
        setServiceName( service.getQName() );
    }

    public void setServiceName( QName serviceName )
    {
        this.serviceName = serviceName;
    }

    @Override
    public OperationContext getOperationContext( String action )
    {
        if ( action == null )
        {
            return null;
        }
        return actionMap.get( action );
    }

    @Override
    public Transport getTransport()
    {
        return transport;
    }

    @Override
    public boolean assemble( Source content, MessageContext message )
        throws BindingException
    {
        Log log = LogFactory.getLog( "sesame.binding");

        String channel = null;
        String action = null;
        String serviceName = null;
        String operationName = null;
        if ( log.isInfoEnabled() )
        {
            channel = (String) message.getProperty( "sesame.exchange.consumer" );
            action = message.getAction();
            QName service = (QName) message.getProperty( "sesame.binding.service.name" );
            if ( service != null )
            {
                serviceName = service.toString();
            }
            OperationContext operation = getOperationContext( message.getAction() );
            if ( operation != null )
            {
                operationName = operation.getOperationName().getLocalPart();
            }
        }
        try
        {
            if ( message.isAccepted() )
            {
                if ( log.isInfoEnabled() )
                {
                    LogRecord t = new XObjectRecord( message.getSerial().longValue(), content );
                    t.setStage( "接入组件编码前" );
                    t.setChannel( channel );
                    t.setAction( action );
                    t.setServiceName( serviceName );
                    t.setOperationName( operationName );
                    log.info( "[REPLY][XML]----------------------------------------------------", t );
                }

                if ( message.getResult() == null )
                {
                    BinaryResult result = new BinaryResult();
                    result.setEncoding( message.getTransport().getCharacterEncoding() );
                    message.setResult( result );
                }

                assembleResponse( content, message );
                
                if ( message.getAction() != null )
                {
                    log = LogFactory.getLog( "sesame.binding." + message.getAction() );
                }

                if ( ( log.isInfoEnabled() ) && ( message.getResult() instanceof BinaryResult ) )
                {
                    BinaryResult result = (BinaryResult) message.getResult();
                    BufferRecord rec = new BufferRecord( message.getSerial().longValue(), result.getBytes() );

                    rec.setStage( "接入组件编码后" );
                    log.info( "[REPLY][BINARY]-------------------------------------------------", rec );

                    FinishRecord finish = new FinishRecord( message.getSerial().longValue() );
                    finish.setStage( "交易结束" );
                    finish.setChannel( channel );
                    finish.setAction( action );
                    finish.setServiceName( serviceName );
                    finish.setOperationName( operationName );
                    log.info( finish );
                }
            }
            else
            {
                if ( log.isInfoEnabled() )
                {
                    XObjectRecord record = new XObjectRecord( message.getSerial().longValue(), content );
                    record.setCallout( true );
                    record.setStage( "callout编码前" );
                    log.info( "[SEND][XML]------------------------------------------------------", record );
                }

                if ( message.getSource() == null )
                {
                    BinarySource source = new BinarySource();
                    source.setEncoding( message.getTransport().getCharacterEncoding() );
                    message.setSource( source );
                }

                assembleRequest( content, message );
                
                if ( message.getAction() != null )
                {
                    log = LogFactory.getLog( "sesame.binding." + message.getAction() );
                }

                if ( ( log.isInfoEnabled() ) && ( message.getSource() instanceof BinarySource ) )
                {
                    BinarySource source = (BinarySource) message.getSource();
                    BufferRecord rec = new BufferRecord( message.getSerial().longValue(), source.getBytes() );
                    rec.setEncoding( source.getEncoding() );

                    rec.setCallout( true );
                    rec.setStage( "callout编码后" );
                    log.info( "[SEND][BINARY]--------------------------------------------------", rec );
                }
            }

            return true;
        }
        catch ( BindingException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new BindingException( e.getMessage(), e );
        }
    }

    private void assembleRequest( Source content, MessageContext message )
        throws BindingException
    {
        if ( message.getSource() instanceof BinarySource )
        {
            BinarySource binSource = (BinarySource) message.getSource();
            message.setPath( ( uri.getPath().length() > 0 ) ? uri.getPath() : "/" );

            String operation = (String) message.getProperty( "sesame.binding.operation.name" );
            XmlSchema schema = null;
            QName elementName = new QName( "undefined" );
            OperationContext operaContext = serviceUnit.getOperationContext( operation );
            if ( operaContext != null )
            {
                message.setAction( ( operaContext.getAction() != null ) ? operaContext.getAction() : operation );
                schema = operaContext.getSchema();
                elementName = operaContext.getInputElement();
            }

            BinaryResult binResult = new BinaryResult();
            binResult.setXMLSchema( schema );
            binResult.setElementName( elementName.getLocalPart() );
            binResult.setEncoding( binSource.getEncoding() );
            if (message.getAction() != null && txCode_start != null && txCode_end != null)
            {
                try
                {
                    int len = txCode_end.intValue() - txCode_start.intValue();
                    byte[] buf = new byte[len];
                    System.arraycopy( message.getAction().getBytes( binResult.getEncoding() ), 0, buf, 0, len );
                    binResult.getOutputStream().write( buf );
                }
                catch ( IOException e )
                {
                    throw new BindingException( e );
                }
            }
            XMLSource xmlSource = new XMLSource( content );
            codec.getEncoder().encode( xmlSource, binResult );
            binSource.setBytes( binResult.getBytes() );
            for ( String name : binResult.getPropertyNames() )
            {
                binSource.setProperty( name, binResult.getProperty( name ) );
            }
        }
    }

    private void assembleResponse( Source content, MessageContext message )
        throws BindingException, TransformerException
    {
        if ( message.getResult() instanceof BinaryResult )
        {
            BinaryResult result = (BinaryResult) message.getResult();

            XmlSchema schema = null;
            QName elementName = new QName( "undefined" );
            OperationContext operaContext = getOperationContext( message.getAction() );
            if ( operaContext != null )
            {
                schema = operaContext.getSchema();
                elementName = operaContext.getOutputElement();
            }

            result.setXMLSchema( schema );
            result.setElementName( elementName.getLocalPart() );
            XMLSource source = new XMLSource( content );
            codec.getEncoder().encode( source, result );
        }
        else
        {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform( content, message.getResult() );
        }
    }

    @Override
    public boolean parse( MessageContext message, Map<String, Object> params )
        throws BindingException
    {
        Log log = LogFactory.getLog( "sesame.binding");
        try
        {
            if ( message.isAccepted() )
            {
                message.setBinding( this );

                MDC.put( "SERIAL", "" + message.getSerial() );
                if ( ( log.isInfoEnabled() ) && ( message.getSource() instanceof BinarySource )
                    && ( message.getProperty( "buffer-logged" ) == null ) )
                {
                    BinarySource source = (BinarySource) message.getSource();
                    BufferRecord record = new BufferRecord( message.getSerial().longValue(), source.getBytes() );
                    record.setEncoding( source.getEncoding() );

                    record.setStage( "接入组件解码前" );
                    record.setAction( message.getAction() );
                    log.info( "[REQ][BINARY]------------------------------------", record );
                    message.setProperty( "buffer-logged", Boolean.valueOf( true ) );
                }

                boolean result = parseRequest( message, params );

                if ( message.getAction() != null )
                {
                    MDC.put( "ACTION", message.getAction() );
                    log = LogFactory.getLog( "sesame.binding." + message.getAction());
                }

                Source source =
                    ( message.getSource() instanceof XMLSource ) ? ( (XMLSource) message.getSource() ).getContent()
                        : message.getSource();
                if ( ( result ) && ( log.isInfoEnabled() ) )
                {
                    OperationContext operation = getOperationContext( message.getAction() );
                    XObjectRecord trace = new XObjectRecord( message.getSerial().longValue(), source );
                    trace.setStage( "接入组件解码后" );
                    log.info( "[REQ][XML]----------------------------------------------", trace );
                }

                return result;
            }

            if ( ( log.isInfoEnabled() ) && ( message.getResult() instanceof BinaryResult ) )
            {
                BinaryResult result = (BinaryResult) message.getResult();
                BufferRecord record = new BufferRecord( message.getSerial().longValue(), result.getBytes() );
                record.setCallout( true );

                record.setStage( "callout解码前" );
                log.info( "[RECV][BINARY]-------------------------------------------", record );
            }

            boolean result = parseResponse( message, params );
            Source source =
                ( message.getResult() instanceof XMLResult ) ? ( (XMLResult) message.getResult() ).getContent()
                    : message.getSource();
            if ( ( result ) && ( log.isInfoEnabled() ) )
            {
                XObjectRecord trace = new XObjectRecord( message.getSerial().longValue(), source );
                trace.setCallout( true );
                trace.setStage( "callout解码后" );
                log.info( "[RECV][XML]------------------------------------------------", trace );
            }
            return result;
        }
        catch ( BindingException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new BindingException( e.getMessage(), e );
        }
    }

    @Override
    public boolean handle( Source fault, MessageContext context )
        throws BindingException
    {
        if ( context.isAccepted() )
        {
            return handleAccepted( fault, context );
        }

        return handleConneced( context );
    }

    private boolean handleAccepted( Source fault, MessageContext context )
        throws BindingException
    {
        Log log = LogFactory.getLog( "sesame.binding." + context.getAction() );
        if ( log.isInfoEnabled() )
        {
            LogRecord rec = new XObjectRecord( context.getSerial().longValue(), fault );
            log.info( "[FAULT][XML]----------------------------------------------", rec );
        }
        try
        {
            OperationContext operaContext = getOperationContext( context.getAction() );

            XmlSchema schema = ( operaContext != null ) ? operaContext.getSchema() : null;
            QName elementName = null;
            if ( fault == null )
            {
                if ( ( operaContext != null ) && ( elementName == null ) )
                {
                    elementName = operaContext.getFaultElement( null );
                }
                if ( ( operaContext != null ) && ( elementName == null ) )
                {
                    elementName = operaContext.getOutputElement();
                }
                if ( context.getResult() == null )
                {
                    context.setResult( new BinaryResult() );
                }
                if ( context.getResult() instanceof BinaryResult )
                {
                    BinaryResult result = (BinaryResult) context.getResult();
                    result.setXMLSchema( schema );
                    result.setElementName( ( elementName != null ) ? elementName.getLocalPart() : null );
                    result.setEncoding( getTransport().getCharacterEncoding() );
                }
                if ( codec.getFaultHandler() != null )
                {
                    codec.getFaultHandler().handle( context.getException(), context );
                }
                else if ( context.getException() != null )
                {
                    LOG.error( "Uncaught exception: ", context.getException() );
                }
                if ( ( context.getResult() instanceof BinaryResult ) && ( log.isInfoEnabled() ) )
                {
                    BinaryResult result = (BinaryResult) context.getResult();
                    BufferRecord rec = new BufferRecord( context.getSerial().longValue(), result.getBytes() );

                    rec.setStage( "接入组件编码后" );
                    log.info( "[FAULT][BUFFER]", rec );

                    ErrorRecord err = new ErrorRecord( context.getSerial().longValue(), context.getException() );
                    err.setStage( "交易结束" );
                    log.info( err );
                }

            }
            else if ( context.getResult() instanceof BinaryResult )
            {
                BinaryResult result = (BinaryResult) context.getResult();
                result.setXMLSchema( schema );
                if ( ( operaContext != null ) && ( elementName == null ) )
                {
                    elementName = operaContext.getFaultElement( (String) result.getProperty( "fault-name" ) );
                }
                if ( ( operaContext != null ) && ( elementName == null ) )
                {
                    elementName = operaContext.getOutputElement();
                }
                result.setElementName( ( elementName != null ) ? elementName.getLocalPart() : null );
                XMLSource source = new XMLSource( fault );
                if ( codec.getFaultHandler() != null )
                {
                    codec.getFaultHandler().encode( source, result );
                }
                else
                {
                    codec.getEncoder().encode( source, result );
                }

                BufferRecord rec = new BufferRecord( context.getSerial().longValue(), result.getBytes() );

                rec.setStage( "接入组件编码后" );
                log.info( "[FAULT][BINARY]---------------------------------------", rec );

                ErrorRecord err = new ErrorRecord( context.getSerial().longValue(), context.getException() );
                err.setStage( "交易结束" );
                log.info( err );
            }
            else
            {
                Transformer transformer = transformerFactory.newTransformer();
                transformer.transform( fault, context.getResult() );
            }
            return true;
        }
        catch ( BindingException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new BindingException( e.getMessage(), e );
        }
    }

    private boolean handleConneced( MessageContext context )
        throws BindingException
    {
        Log log = LogFactory.getLog( "sesame.binding" );
        try
        {
            String operationName = (String) context.getProperty( "sesame.binding.operation.name" );
            OperationContext operaContext = serviceUnit.getOperationContext( operationName );

            XmlSchema schema = ( operaContext != null ) ? operaContext.getSchema() : null;
            QName elementName = null;
            if ( context.getResult() instanceof BinaryResult )
            {
                BinaryResult binResult = (BinaryResult) context.getResult();
                if ( log.isInfoEnabled() )
                {
                    BufferRecord rec = new BufferRecord( context.getSerial().longValue(), binResult.getBytes() );

                    rec.setStage( "接入组件编码后" );
                    log.info( "[FAULT][BINARY]---------------------------------------", rec );
                }

                if ( ( operaContext != null ) && ( elementName == null ) )
                {
                    elementName = operaContext.getFaultElement( (String) binResult.getProperty( "fault-name" ) );
                }
                if ( ( operaContext != null ) && ( elementName == null ) )
                {
                    elementName = operaContext.getOutputElement();
                }
                BinarySource source = new BinarySource();
                source.setBytes( binResult.getBytes() );
                source.setXMLSchema( schema );
                source.setElementName( ( elementName != null ) ? elementName.getLocalPart() : null );

                BinarySource input = (BinarySource) context.getSource();
                String charset = input.getEncoding();
                if ( charset == null )
                {
                    charset = context.getTransport().getCharacterEncoding();
                }
                source.setEncoding( charset );

                XMLResult result = new XMLResult();
                result.setProperty( "response.status.xpath", status_xpath );
                result.setProperty( "response.statustext.xpath", statusText_xpath );
                if ( codec.getFaultHandler() != null )
                {
                    codec.getFaultHandler().decode( source, result );
                }
                else
                {
                    codec.getDecoder().decode( source, result );
                }
                context.setResult( result );

                if ( log.isInfoEnabled() )
                {
                    LogRecord rec = new XObjectRecord( context.getSerial().longValue(), result.getContent() );
                    log.info( "[FAULT][XML]----------------------------------------------", rec );
                }

                ErrorRecord err = new ErrorRecord( context.getSerial().longValue(), context.getException() );
                err.setStage( "交易结束" );
                log.info( err );
            }
            return true;
        }
        catch ( BindingException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new BindingException( e.getMessage(), e );
        }
    }
}