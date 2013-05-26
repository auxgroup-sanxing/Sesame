package com.sanxing.sesame.binding.soap;

import com.sanxing.sesame.address.AddressBook;
import com.sanxing.sesame.address.Location;
import com.sanxing.sesame.core.keymanager.KeyStoreInfo;
import com.sanxing.sesame.core.keymanager.KeyStoreManager;
import com.sanxing.sesame.core.keymanager.SKPManager;
import com.sanxing.sesame.core.keymanager.ServiceKeyProvider;
import com.sanxing.sesame.service.ServiceUnit;
import com.sanxing.sesame.util.JdomUtil;
import com.sanxing.sesame.wssecurity.commons.MessageContext;
import com.sanxing.sesame.wssecurity.handlers.DecryptionHandler;
import com.sanxing.sesame.wssecurity.handlers.EncryptionHandler;
import com.sanxing.sesame.wssecurity.handlers.SigningHandler;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.MessageExchange;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Namespace;
import org.jdom.output.DOMOutputter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SOAPSender
{
    private Map<QName, URL> urlMap = new HashMap();

    private Map<String, String> operationActionMap = new HashMap();

    private Map<QName, BindingParameters> bindingParametersMap = new HashMap();

    private static Logger LOG = LoggerFactory.getLogger( SOAPSender.class );

    public Source sendRequest( Source request, MessageExchange exchange )
        throws Exception
    {
        Source content = null;
        try
        {
            String operation = exchange.getService().getLocalPart() + exchange.getOperation().getLocalPart();
            String operationAction = (String) this.operationActionMap.get( operation );
            BindingParameters bindingParameters =
                (BindingParameters) this.bindingParametersMap.get( exchange.getInterfaceName() );
            String type = bindingParameters.getSoapType();

            SKPManager skpManager = SKPManager.getInstance();
            KeyStoreManager ksManager = KeyStoreManager.getInstance();

            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage();
            if ( type.equalsIgnoreCase( "httpHeader" ) )
            {
                MimeHeaders httphead = soapMessage.getMimeHeaders();
                String httpPara = bindingParameters.getHttpPara();
                httphead.addHeader( httpPara, operationAction );
            }
            if ( type.equalsIgnoreCase( "SOAPActionheader" ) )
            {
                MimeHeaders httphead = soapMessage.getMimeHeaders();
                httphead.addHeader( "SOAPAction", operationAction );
            }

            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            envelope.setEncodingStyle( "http://schemas.xmlsoap.org/soap/encoding/" );
            SOAPHeader soaphead = envelope.getHeader();
            soaphead.addNamespaceDeclaration( "wsu",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" );
            if ( type.equalsIgnoreCase( "WS-Addressing" ) )
            {
                soaphead.addNamespaceDeclaration( "wsa", "http://www.w3.org/2005/08/addressing" );
                soaphead.addChildElement( "Action", "wsa", "http://www.w3.org/2005/08/addressing" );
                soaphead.getFirstChild().setTextContent( operationAction );
            }
            if ( type.equalsIgnoreCase( "SoapHeader" ) )
            {
                String xpathExpression = bindingParameters.getXpathExpression();
                String[] temp = xpathExpression.split( "/" );
                soaphead.addChildElement( temp[0], "", "" );
                QName headQname = new QName( "", temp[0] );
                SOAPElement soapele = (SOAPElement) soaphead.getChildElements( headQname ).next();
                for ( int j = 1; j < temp.length; j++ )
                {
                    soapele.addChildElement( temp[j], "", "" );
                    soapele = (SOAPElement) soapele.getChildElements().next();
                }
                soapele.setTextContent( exchange.getOperation().getLocalPart() );
            }

            SOAPBody body = envelope.getBody();
            body.addNamespaceDeclaration( "wsu",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" );
            body.addNamespaceDeclaration( "xsd", "http://www.w3.org/2001/XMLSchema" );

            Document doc = JdomUtil.source2JDOMDocument( request );
            org.jdom.Element bodyElement = doc.getRootElement();
            org.jdom.Element newBodyElement = (org.jdom.Element) bodyElement.clone();

            String pre = newBodyElement.getNamespacePrefix();
            body.addNamespaceDeclaration( newBodyElement.getNamespacePrefix(), newBodyElement.getNamespaceURI() );
            List<Namespace> nameSpaces = newBodyElement.getAdditionalNamespaces();
            for ( Namespace namespace : nameSpaces )
            {
                body.addNamespaceDeclaration( namespace.getPrefix(), namespace.getURI() );
            }
            newBodyElement.setNamespace( null );
            body.addDocument( new DOMOutputter().output( new Document( newBodyElement ) ) );
            body.getFirstChild().setPrefix( pre );

            soapMessage.saveChanges();
            soapMessage.writeTo( System.out );

            MessageContext messageContext = new MessageContext();
            messageContext.setMessage( soapMessage );

            if ( bindingParameters.getSignSwitch().booleanValue() )
            {
                String signingKeyProvider = bindingParameters.getSigningKeyProvider();
                ServiceKeyProvider skp = skpManager.getSKP( signingKeyProvider );
                SigningHandler signingHandler = new SigningHandler();
                signingHandler.handleRequest( messageContext, skp );
            }
            soapMessage.saveChanges();

            if ( bindingParameters.getencryptionSwitch().booleanValue() )
            {
                String encryptionKeyProvider = bindingParameters.getEncryptionKeyProvider();
                ServiceKeyProvider skp = skpManager.getSKP( encryptionKeyProvider );
                EncryptionHandler encryptionHandler = new EncryptionHandler();
                encryptionHandler.handleRequest( messageContext, ksManager.getKeyStore( skp.getKeystoreName() ), skp );
            }
            soapMessage.saveChanges();
            LOG.debug( "\nsender soapmessage :\n" );
            soapMessage.writeTo( System.out );
            SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            SOAPMessage response = soapConnection.call( soapMessage, this.urlMap.get( exchange.getInterfaceName() ) );

            LOG.debug( "\naccept soapmessage :\n" );
            response.writeTo( System.out );

            SOAPFault soapFault = response.getSOAPBody().getFault();
            if ( soapFault != null )
            {
                LOG.debug( "soapFault :" + soapFault.getFaultString() );
                throw new SoapFaultException( soapFault );
            }

            if ( bindingParameters.getencryptionSwitch().booleanValue() )
            {
                String verifyKeyProvider = bindingParameters.getVerifyKeyProvider();
                ServiceKeyProvider skp = skpManager.getSKP( verifyKeyProvider );
                decrypAndOrVerify( response, ksManager.getKeyStore( skp.getKeystoreName() ), skp );
            }

            Node child = getSoapBody( response );
            content = new DOMSource( child );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
        return content;
    }

    private void decrypAndOrVerify( SOAPMessage response, KeyStoreInfo keyStoreInfo, ServiceKeyProvider skp )
    {
        MessageContext messageContext = new MessageContext();
        messageContext.setMessage( response );
        DecryptionHandler decryptionHandler = new DecryptionHandler();
        decryptionHandler.decryptAndOrVerifySignatures( messageContext, keyStoreInfo, skp );
    }

    public void setconfig( ServiceUnit unit )
        throws Exception
    {
        try
        {
            Map bindings = unit.getDefinition().getBindings();
            for ( Binding binding : (Collection<Binding>) bindings.values() )
            {
                List bindingOperations = binding.getBindingOperations();
                UnknownExtensibilityElement node =
                    (UnknownExtensibilityElement) binding.getExtensibilityElements().get( 0 );
                org.w3c.dom.Element extEle = node.getElement();
                BindingParameters bindingParameters = new BindingParameters();
                String type = extEle.getAttribute( "type" );
                bindingParameters.setSoapType( type );

                String signSwitch = extEle.getAttribute( "signSwitch" );
                bindingParameters.setSignSwitch( signSwitch );

                String decryptionSwitch = extEle.getAttribute( "encryptionSwitch" );
                bindingParameters.setencryptionSwitch( decryptionSwitch );

                String verifySwitch = extEle.getAttribute( "encryptionSwitch" );
                bindingParameters.setencryptionSwitch( verifySwitch );

                String signingKeyProvider = extEle.getAttribute( "signingKeyProvider" );
                bindingParameters.setSigningKeyProvider( signingKeyProvider );

                String encryptionKeyProvider = extEle.getAttribute( "encryptionKeyProvider" );
                bindingParameters.setEncryptionKeyProvider( encryptionKeyProvider );

                String verifyKeyProvider = extEle.getAttribute( "verifyKeyProvider" );
                bindingParameters.setVerifyKeyProvider( verifyKeyProvider );

                if ( ( type.equalsIgnoreCase( "WS-Addressing" ) ) || ( type.equalsIgnoreCase( "SOAPActionheader" ) ) )
                {
                    for ( Iterator it = bindingOperations.iterator(); it.hasNext(); )
                    {
                        BindingOperation opera = (BindingOperation) it.next();
                        List soapoperations = opera.getExtensibilityElements();
                        Iterator ite = soapoperations.iterator();
                        while ( ite.hasNext() )
                        {
                            SOAPOperation op = (SOAPOperation) ite.next();
                            String operation = unit.getServiceName().getLocalPart() + opera.getName();
                            this.operationActionMap.put( operation, op.getSoapActionURI() );
                        }
                    }
                }
                else if ( type.equalsIgnoreCase( "SoapHeader" ) )
                {
                    String xpathExpression = extEle.getAttribute( "xpath" );
                    bindingParameters.setXpathExpression( xpathExpression );
                }
                else if ( type.equalsIgnoreCase( "httpHeader" ) )
                {
                    String httpParameter = extEle.getAttribute( "head" );
                    bindingParameters.setHttpPara( httpParameter );
                }
                LOG.debug( "putmap binding.getPortType().getQName() :::" + binding.getPortType().getQName() );
                this.bindingParametersMap.put( binding.getPortType().getQName(), bindingParameters );
            }
            Service service = unit.getService();
            Iterator portQNames = service.getPorts().keySet().iterator();
            while ( portQNames.hasNext() )
            {
                Port port = service.getPort( (String) portQNames.next() );
                Iterator iter = port.getExtensibilityElements().iterator();
                if ( !iter.hasNext() )
                    throw new IOException( "Port address not specified" );
                ExtensibilityElement ee = (ExtensibilityElement) iter.next();
                SOAPAddress endpoint = (SOAPAddress) ee;
                String endpointAddress = endpoint.getLocationURI();
                URI uri = new URI( endpointAddress );
                if ( ( uri.getScheme() != null ) && ( uri.getHost() == null ) )
                {
                    throw new DeploymentException( " error uri " + endpointAddress );
                }
                Location loc = AddressBook.find( uri.getScheme() != null ? uri.getHost() : endpointAddress );
                if ( loc != null )
                {
                    URI real = loc.getURI();
                    uri =
                        new URI( real.getScheme(), real.getAuthority(), real.getPath()
                            + ( uri.getScheme() != null ? uri.getPath() : "" ), uri.getQuery(), uri.getFragment() );
                }
                URL url = new URL( uri.toString() );
                this.urlMap.put( port.getBinding().getPortType().getQName(), url );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw e;
        }
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
}

/*
 * Location: D:\sesame-1.0\warehouse\components\soap-cc\ Qualified Name: com.sanxing.sesame.binding.soap.SOAPSender
 * JD-Core Version: 0.6.2
 */