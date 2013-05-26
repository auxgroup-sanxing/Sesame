package com.sanxing.sesame.binding.soap;

import com.sanxing.sesame.logging.ErrorRecord;
import com.sanxing.sesame.logging.FinishRecord;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import com.sanxing.sesame.logging.LogRecord;
import com.sanxing.sesame.logging.XObjectRecord;
import com.sanxing.sesame.core.keymanager.KeyStoreInfo;
import com.sanxing.sesame.core.keymanager.KeyStoreManager;
import com.sanxing.sesame.core.keymanager.SKPManager;
import com.sanxing.sesame.core.keymanager.ServiceKeyProvider;
import com.sanxing.sesame.serial.SerialGenerator;
import com.sanxing.sesame.service.OperationContext;
import com.sanxing.sesame.service.ServiceUnit;
import com.sanxing.sesame.util.JdomUtil;
import com.sanxing.sesame.wssecurity.commons.MessageContext;
import com.sanxing.sesame.wssecurity.handlers.DecryptionHandler;
import com.sanxing.sesame.wssecurity.handlers.EncryptionHandler;
import com.sanxing.sesame.wssecurity.handlers.SigningHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.servlet.http.HttpServletRequest;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SOAPEndPoint
{
    private static Logger LOG = LoggerFactory.getLogger( SOAPEndPoint.class );

    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private static DOMOutputter outputter = new DOMOutputter();

    private SOAPServer component;

    private String SOAPHEADER = "SoapHeader";

    private String SOAPACTION = "SOAPActionheader";

    private String WSAACTION = "WS-Addressing";

    private String HTTPHEADER = "httpHeader";

    private XPath wsaXpath;

    private Map<String, OperationContext> operaContextMap = new HashMap();

    private Map<QName, BindingParameters> bindingParametersMap = new HashMap();

    private Port port;

    private ServiceUnit serviceUnit;

    private String operationName;

    private String serviceName;

    private String action;

    private String channel;

    private long serial;

    public String getOperationName()
    {
        return this.operationName;
    }

    public void setOperationName( String operationName )
    {
        this.operationName = operationName;
    }

    public String getServiceName()
    {
        return this.serviceName;
    }

    public void setServiceName( String serviceName )
    {
        this.serviceName = serviceName;
    }

    public String getAction()
    {
        return this.action;
    }

    public void setAction( String action )
    {
        this.action = action;
    }

    public String getChannel()
    {
        return this.channel;
    }

    public void setChannel( String channel )
    {
        this.channel = channel;
    }

    public long getSerial()
    {
        return this.serial;
    }

    public void setSerial( long serial )
    {
        this.serial = serial;
    }

    public SOAPEndPoint( SOAPServer _component )
    {
        this.component = _component;
    }

    public SOAPMessage processRequest( SOAPMessage input, HttpServletRequest request )
        throws SOAPException
    {
        Log log = LogFactory.getLog( "sesame.binding" );
        SOAPMessage result = this.component.rootServlet.getMessageFactory().createMessage();

        QName bindingQName = this.port.getBinding().getQName();
        BindingParameters bindingParameters = (BindingParameters) this.bindingParametersMap.get( bindingQName );
        SKPManager skpManager = SKPManager.getInstance();
        KeyStoreManager ksManager = KeyStoreManager.getInstance();

        if ( ( bindingParameters.getSignSwitch().booleanValue() )
            || ( bindingParameters.getencryptionSwitch().booleanValue() ) )
        {
            SOAPPart soapPart = result.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            envelope.setEncodingStyle( "http://schemas.xmlsoap.org/soap/encoding/" );
            SOAPHeader soaphead = envelope.getHeader();
            soaphead.addNamespaceDeclaration( "wsu",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" );
            envelope.getBody().addNamespaceDeclaration( "wsu",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" );
        }
        try
        {
            if ( bindingParameters.getencryptionSwitch().booleanValue() )
            {
                String verifyKeyProvider = bindingParameters.getVerifyKeyProvider();
                ServiceKeyProvider skp = skpManager.getSKP( verifyKeyProvider );
                decrypAndOrVerify( input, ksManager.getKeyStore( skp.getKeystoreName() ), skp );
            }

            Node child = null;
            child = getSoapBody( input );
            OperationContext operationContext = getOperationName( input, child, request, bindingParameters );
            if ( operationContext == null )
            {
                throw new RuntimeException( "can's find operation !!! the soapType is ["
                    + bindingParameters.getSoapType() + "]" );
            }
            setOperationName( operationContext.getOperationName().getLocalPart() );
            setServiceName( operationContext.getServcieName().getLocalPart() );
            setAction( operationContext.getAction() );
            setChannel( this.component.getContext().getComponentName() );

            String operationName = operationContext.getOperationName().getLocalPart();
            Source source = new DOMSource( child );
            if ( this.serviceUnit == null )
            {
                throw new RuntimeException( "serviceUnit is not defined :" + operationName );
            }
            MessageExchange exchange = processRequest( source, operationContext );

            if ( exchange.getStatus() != ExchangeStatus.ERROR )
            {
                NormalizedMessage out = exchange.getMessage( "out" );
                Source content = out.getContent();

                if ( log.isInfoEnabled() )
                {
                    LogRecord t = new XObjectRecord( getSerial(), content );
                    t.setStage( "接入组件编码前" );
                    t.setChannel( getChannel() );
                    t.setAction( getAction() );
                    t.setServiceName( getServiceName() );
                    t.setOperationName( getOperationName() );
                    log.info( "[REPLY][XML]----------------------------------------------------", t );
                }

                if ( ( content instanceof JDOMSource ) )
                {
                    JDOMSource jdomSource = (JDOMSource) content;
                    org.w3c.dom.Document document = outputter.output( jdomSource.getDocument() );
                    result.getSOAPBody().addDocument( document );
                }
                else
                {
                    DOMResult domResult = new DOMResult();
                    Transformer transformer = transformerFactory.newTransformer();

                    transformer.transform( content, domResult );
                    result.getSOAPBody().addDocument( (org.w3c.dom.Document) domResult.getNode() );
                }

                MessageContext messageContext = new MessageContext();
                messageContext.setMessage( result );

                if ( bindingParameters.getSignSwitch().booleanValue() )
                {
                    String signingKeyProvider = bindingParameters.getSigningKeyProvider();
                    ServiceKeyProvider skp = skpManager.getSKP( signingKeyProvider );
                    SigningHandler signingHandler = new SigningHandler();
                    signingHandler.handleResponse( messageContext, skp );
                }

                if ( bindingParameters.getencryptionSwitch().booleanValue() )
                {
                    String encryptionKeyProvider = bindingParameters.getEncryptionKeyProvider();
                    ServiceKeyProvider skp = skpManager.getSKP( encryptionKeyProvider );
                    EncryptionHandler encryptionHandler = new EncryptionHandler();
                    encryptionHandler.handleResponse( messageContext, ksManager.getKeyStore( skp.getKeystoreName() ),
                        skp );
                }

                if ( log.isInfoEnabled() )
                {
                    FinishRecord finish = new FinishRecord( getSerial() );
                    finish.setStage( "交易结束" );
                    finish.setChannel( getChannel() );
                    finish.setAction( getAction() );
                    finish.setServiceName( getServiceName() );
                    finish.setOperationName( getOperationName() );
                    log.info( null, finish );
                }
            }
            else
            {
                if ( exchange.getError() != null )
                {
                    throw exchange.getError();
                }
                Fault fault = exchange.getFault();
                org.jdom.Document jdoc = JdomUtil.source2JDOMDocument( fault.getContent() );

                if ( log.isInfoEnabled() )
                {
                    LogRecord t = new XObjectRecord( getSerial(), fault.getContent() );
                    t.setStage( "接入组件编码前" );
                    t.setChannel( getChannel() );
                    t.setAction( getAction() );
                    t.setServiceName( getServiceName() );
                    t.setOperationName( getOperationName() );
                    log.info( "[REPLY][XML]----------------------------------------------------", t );
                }

                org.jdom.Element rootEl = jdoc.getRootElement();

                SOAPFault soapFault = result.getSOAPBody().addFault();

                String faultCode = rootEl.getChildText( "fault-code" );
                if ( faultCode != null )
                {
                    soapFault.setFaultCode( new QName( SOAPConstants.SOAP_SENDER_FAULT.getNamespaceURI(), faultCode ) );
                }
                soapFault.setFaultString( rootEl.getChildText( "fault-reason" ) );
                soapFault.setFaultActor( rootEl.getChildText( "actor" ) == null ? "" : rootEl.getChildText( "actor" ) );

                MessageContext messageContext = new MessageContext();
                messageContext.setMessage( result );
                SOAPFaultException soapFaultException =
                    new SOAPFaultException( soapFault.getFaultCodeAsQName(), soapFault.getFaultString(),
                        soapFault.getFaultActor(), null );
                messageContext.setProperty( "savedFault", soapFaultException );

                if ( bindingParameters.getSignSwitch().booleanValue() )
                {
                    SigningHandler signingHandler = new SigningHandler();
                    signingHandler.handleFault( messageContext );
                }

                if ( bindingParameters.getencryptionSwitch().booleanValue() )
                {
                    EncryptionHandler encryptionHandler = new EncryptionHandler();
                    encryptionHandler.handleFault( messageContext );
                }

                log.error( null, new ErrorRecord( getSerial(), null ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            try
            {
                log.error( e.getMessage(), new ErrorRecord( getSerial(), e ) );
                SOAPFault soapFault = result.getSOAPBody().addFault();
                soapFault.addNamespaceDeclaration( "sesa", "http://www.sanxing.com/ns/sesame" );
                soapFault.setFaultCode( new QName( "http://www.sanxing.com/ns/sesame", e.getClass().getName() ) );
                soapFault.setFaultString( e.getMessage() == null ? e.toString() : e.getMessage(), Locale.CHINA );
            }
            catch ( SOAPException ex )
            {
                LOG.error( ex.getMessage(), ex );
            }
        }
        return result;
    }

    private void decrypAndOrVerify( SOAPMessage input, KeyStoreInfo keyStoreInfo, ServiceKeyProvider skp )
    {
        MessageContext messageContext = new MessageContext();
        messageContext.setMessage( input );
        DecryptionHandler decryptionHandler = new DecryptionHandler();
        decryptionHandler.decryptAndOrVerifySignatures( messageContext, keyStoreInfo, skp );
    }

    private Node getSoapBody( SOAPMessage input )
        throws SOAPException
    {
        NodeList list = input.getSOAPBody().getChildNodes();
        Node child = null;
        for ( int i = 0; i < list.getLength(); i++ )
        {
            Node node = list.item( i );
            if ( node.getNodeType() == 1 )
            {
                child = node;
                break;
            }
        }
        if ( child == null )
        {
            throw new RuntimeException( "SOAP Message is empty" );
        }
        return child;
    }

    public OperationContext getOperationName( SOAPMessage input, Node child, HttpServletRequest request,
                                              BindingParameters bindingParameters )
        throws SOAPException, JDOMException
    {
        String soapType = bindingParameters.getSoapType();
        OperationContext operationContext;
        if ( soapType.equals( this.WSAACTION ) )
        {
            org.jdom.Document doc = new DOMBuilder().build( input.getSOAPHeader().getOwnerDocument() );
            operationContext =
                (OperationContext) this.operaContextMap.get( this.wsaXpath.valueOf( doc.getRootElement() ) );
        }
        else
        {
            if ( soapType.equals( this.SOAPHEADER ) )
            {
                org.jdom.Document doc = new DOMBuilder().build( input.getSOAPHeader().getOwnerDocument() );
                XPath operationXpath = bindingParameters.getXpath();
                operationContext =
                    this.serviceUnit.getOperationContext( operationXpath.valueOf( doc.getRootElement() ) );
            }
            else
            {
                if ( soapType.equals( this.HTTPHEADER ) )
                {
                    String httpPara = bindingParameters.getHttpPara();
                    operationContext = this.serviceUnit.getOperationContext( request.getHeader( httpPara ) );
                }
                else
                {
                    if ( soapType.equals( this.SOAPACTION ) )
                    {
                        String url = request.getHeader( "SOAPAction" );
                        operationContext = (OperationContext) this.operaContextMap.get( url.replace( "\"", "" ) );
                    }
                    else
                    {
                        operationContext = this.serviceUnit.getOperationContext( child.getLocalName() );
                    }
                }
            }
        }
        return operationContext;
    }

    private MessageExchange processRequest( Source source, OperationContext operationContext )
        throws MessagingException
    {
        Log log = LogFactory.getLog( "sesame.binding" );
        MessageExchange exchange = this.component.createExchange( operationContext );
        NormalizedMessage msg = exchange.createMessage();
        msg.setContent( source );
        exchange.setMessage( msg, "in" );
        exchange.setProperty( "sesame.exchange.platform.serial", Long.valueOf( SerialGenerator.getSerial() ) );
        exchange.setProperty( "sesame.exchange.consumer", this.component.getContext().getComponentName() );

        setSerial( ( (Long) exchange.getProperty( "sesame.exchange.platform.serial" ) ).longValue() );

        if ( log.isInfoEnabled() )
        {
            XObjectRecord trace = new XObjectRecord( getSerial(), source );
            trace.setStage( "接入组件解码后" );
            log.info( "[REQ][XML]----------------------------------------------", trace );
        }

        this.component.sendSync( exchange );
        return exchange;
    }

    public Definition getDefinition()
    {
        return this.serviceUnit.getDefinition();
    }

    public void setServiceUnit( ServiceUnit unit )
        throws JDOMException
    {
        this.serviceUnit = unit;
    }

    public void setPort( Port port )
        throws JDOMException
    {
        this.port = port;

        Binding binding = port.getBinding();
        if ( binding == null )
        {
            throw new RuntimeException( "Port[" + port.getName() + "] Binding not found!" );
        }

        List list = binding.getExtensibilityElements();
        String soapType;
        BindingParameters bindingParameters;
        if ( list.size() > 0 )
        {
            for ( Iterator localIterator = list.iterator(); localIterator.hasNext(); )
            {
                Object element = localIterator.next();
                if ( ( element instanceof UnknownExtensibilityElement ) )
                {
                    bindingParameters = new BindingParameters();
                    UnknownExtensibilityElement node = (UnknownExtensibilityElement) element;
                    org.w3c.dom.Element extEle = node.getElement();
                    soapType = extEle.getAttribute( "type" );
                    bindingParameters.setSoapType( soapType );

                    String signSwitch = extEle.getAttribute( "signSwitch" );
                    bindingParameters.setSignSwitch( signSwitch );

                    String encryptionSwitch = extEle.getAttribute( "decryptionSwitch" );
                    bindingParameters.setencryptionSwitch( encryptionSwitch );

                    String verifySwitch = extEle.getAttribute( "verifySwitch" );
                    bindingParameters.setverifySwitch( verifySwitch );

                    String signingKeyProvider = extEle.getAttribute( "" );
                    bindingParameters.setSigningKeyProvider( signingKeyProvider );

                    String encryptionKeyProvider = extEle.getAttribute( "" );
                    bindingParameters.setEncryptionKeyProvider( encryptionKeyProvider );

                    String verifyKeyProvider = extEle.getAttribute( "" );
                    bindingParameters.setVerifyKeyProvider( verifyKeyProvider );

                    if ( soapType.equalsIgnoreCase( this.SOAPHEADER ) )
                    {
                        try
                        {
                            XPath xpath = XPath.newInstance( extEle.getAttribute( "xpath" ) );
                            bindingParameters.setXpath( xpath );
                        }
                        catch ( Exception e )
                        {
                            throw new RuntimeException( "in sesa:binding, Invalid XPath expression :["
                                + extEle.getAttribute( "xpath" ) + "]" );
                        }
                    }
                    if ( soapType.equalsIgnoreCase( this.HTTPHEADER ) )
                    {
                        bindingParameters.setHttpPara( extEle.getAttribute( "head" ) );
                    }
                    if ( soapType.equalsIgnoreCase( this.WSAACTION ) )
                    {
                        this.wsaXpath = XPath.newInstance( "*[1]/*[local-name()='Action']/text()" );
                    }

                    this.bindingParametersMap.put( port.getBinding().getQName(), bindingParameters );
                }
            }
        }

        List operations = binding.getBindingOperations();
        for ( Iterator it = operations.iterator(); it.hasNext(); )
        {
            BindingOperation opera = (BindingOperation) it.next();
            List soapoperations = opera.getExtensibilityElements();
            Iterator iter = soapoperations.iterator();
            while (iter.hasNext())
            {
                SOAPOperation op = (SOAPOperation) iter.next();
                OperationContext operationContext = this.serviceUnit.getOperationContext( opera.getName() );
                this.operaContextMap.put( op.getSoapActionURI(), operationContext );
            }
        }
    }
}