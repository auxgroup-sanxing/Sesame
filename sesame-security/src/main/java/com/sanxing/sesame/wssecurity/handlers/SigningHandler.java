package com.sanxing.sesame.wssecurity.handlers;

import com.sanxing.sesame.core.keymanager.ServiceKeyProvider;
import com.sanxing.sesame.wssecurity.commons.Constants;
import com.sanxing.sesame.wssecurity.commons.ExtendedGenericHandler;
import com.sanxing.sesame.wssecurity.commons.NamespaceCatalog;
import com.sanxing.sesame.wssecurity.commons.Utils;
import com.sanxing.sesame.wssecurity.commons.WsuIdResolver;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xml.security.Init;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SigningHandler
    extends ExtendedGenericHandler
{
    static Map signingConfig = new HashMap();

    public static final String IS_ON_REQUEST_SIDE_PARAM_NAME = "isOnRequestSide";

    boolean isOnRequestSide;

    public static final String KEY_STORE_RES_PATH_PARAMETER_NAME = "keyStoreResourcePath";

    private KeyStore keyStore;

    public static final String KEY_STORE_PASSWORD_PARAMETER_NAME = "keyStorePassword";

    public static final String OUR_KEY_ALIAS_PARAMETER_NAME = "ourKeyAlias";

    public static final String OUR_KEY_PASSWORD_PARAMETER_NAME = "ourKeyPassword";

    private Key ourKey;

    private String ourBase64EncodedCertPath;

    public static final String XPATH_TO_SIGN_PARAM_NAME = "xPathToSign";

    private XPathExpression xpathToSign;

    public static final String NS_CATALOG_RES_PATH_PARAMETER_NAME = "nsCatalogResourcePath";

    public static final String SIGNATURE_ALGO_PARAMETER_NAME = "signatureAlgo";

    private String signatureAlgo;

    public static final String C14N_ALGO_PARAMETER_NAME = "c14nAlgo";

    private String c14nAlgo;

    public static final String TARGET_ACTOR_PARAMETER_NAME = "targetURI";

    private String targetActorURI;

    static
    {
        Init.init();
        signingConfig.put( "isOnRequestSide", "true" );
        signingConfig.put( "keyStoreResourcePath", "/client.ks" );
        signingConfig.put( "keyStorePassword", "client-ks-pass" );
        signingConfig.put( "ourKeyAlias", "c1" );
        signingConfig.put( "ourKeyPassword", "client-ks-pass" );
        signingConfig.put( "xPathToSign", "soapenv:Body" );

        signingConfig.put( "nsCatalogResourcePath", "/namespacecatalog.properties" );
        signingConfig.put( "targetURI", null );
    }

    public void init( ServiceKeyProvider skp )
    {
        this.isOnRequestSide = Boolean.parseBoolean( (String) signingConfig.get( "isOnRequestSide" ) );
        try
        {
            this.ourKey = skp.getKey();
            String cer0 = new String( Base64.encodeBase64( skp.getCert().getEncoded() ) );
            this.ourBase64EncodedCertPath = cer0;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            this.logger.error( e.getMessage(), e );
            throw new RuntimeException( "Failed to retrieve the key/cert to use in decryption", e );
        }

        String xPathString = (String) signingConfig.get( "xPathToSign" );
        if ( xPathString == null )
        {
            throw new RuntimeException( "Missing Mandatory Parameter: xPathToSign" );
        }

        compileXPathToSign( signingConfig, xPathString );
        this.signatureAlgo = ( (String) signingConfig.get( "signatureAlgo" ) );
        if ( this.signatureAlgo == null )
        {
            this.signatureAlgo = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
        }
        this.c14nAlgo = ( (String) signingConfig.get( "c14nAlgo" ) );
        if ( this.c14nAlgo == null )
        {
            this.c14nAlgo = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
        }

        this.targetActorURI = ( (String) signingConfig.get( "targetURI" ) );

        this.logger.debug( "Initialization successful" );
    }

    public boolean handleRequest( MessageContext messageContext, ServiceKeyProvider skp )
    {
        this.logger.debug( "Applying " + getClass().getName() + " to request" );
        init( skp );
        if ( this.isOnRequestSide )
        {
            sign( messageContext );
        }
        return true;
    }

    public boolean handleResponse( MessageContext messageContext, ServiceKeyProvider skp )
    {
        this.logger.debug( "Applying " + getClass().getName() + " to response" );
        if ( !this.isOnRequestSide )
        {
            init( skp );
            sign( messageContext );
        }
        return true;
    }

    private void sign( MessageContext messageContext )
    {
        SOAPMessageContext soapContext = (SOAPMessageContext) messageContext;

        Set roleSet = null;
        String faultActor = null;

        if ( !this.isOnRequestSide )
        {
            roleSet = getSOAPRoles( soapContext );
            if ( !roleSet.isEmpty() )
            {
                faultActor = (String) roleSet.iterator().next();
            }
        }

        try
        {
            SOAPMessage message = soapContext.getMessage();
            SOAPPart soapPart = message.getSOAPPart();
            SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
            NodeList nodesToSign = (NodeList) this.xpathToSign.evaluate( soapEnvelope, XPathConstants.NODESET );
            int numNodesToSign = nodesToSign.getLength();
            if ( numNodesToSign == 0 )
                return;

            setMissingIdsExceptOnRoot( nodesToSign, soapEnvelope );

            SOAPHeaderElement securityElement = insertWSSecurityHeaderIfMissing( soapEnvelope );

            XMLSignature xmlSignature =
                new XMLSignature( soapPart, "", this.signatureAlgo, "http://www.w3.org/2001/10/xml-exc-c14n#" );
            securityElement.insertBefore( xmlSignature.getElement(), securityElement.getFirstChild() );

            String wsuIdForCertificateBST = "SignatureKey-" + hashCode();
            Element certificatePathAsBST = buildCertPathBST( soapPart, wsuIdForCertificateBST );
            securityElement.insertBefore( certificatePathAsBST, securityElement.getFirstChild() );

            populateKeyInfoInXMLSignature( xmlSignature, wsuIdForCertificateBST, soapPart );

            xmlSignature.addResourceResolver( new WsuIdResolver( soapPart ) );
            addReferencesToSign( xmlSignature, nodesToSign, soapPart );
            message.writeTo( System.out );

            xmlSignature.sign( this.ourKey );
        }
        catch ( Exception e )
        {
            this.logger.error( e.getMessage(), e );
            if ( this.isOnRequestSide )
            {
                throw new JAXRPCException( e );
            }

            createFaultInContextAndThrow( soapContext, Constants.SOAP_SERVER_FAULT_CODE, " Error in signing",
                faultActor, e );
        }
    }

    private void populateKeyInfoInXMLSignature( XMLSignature xmlSignature, String wsuIdForCertificateBST,
                                                SOAPPart soapPart )
    {
        KeyInfo keyInfo = xmlSignature.getKeyInfo();
        Element securityTokenReference =
            soapPart.createElementNS(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "SecurityTokenReference" );
        Element wsseReference =
            soapPart.createElementNS(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Reference" );
        wsseReference.setAttributeNS( null, "URI", "#" + wsuIdForCertificateBST );
        securityTokenReference.appendChild( wsseReference );
        keyInfo.addUnknownElement( securityTokenReference );
    }

    private Element buildCertPathBST( SOAPPart soapPart, String wsuIdForCertificateBST )
    {
        Element certificatePathAsBST =
            soapPart.createElementNS(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "wsse:BinarySecurityToken" );
        try
        {
            soapPart.getEnvelope().getHeader().addNamespaceDeclaration( "wsu",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" );
        }
        catch ( SOAPException e )
        {
            e.printStackTrace();
        }
        certificatePathAsBST.setAttributeNS( null, "ValueType",
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3" );
        certificatePathAsBST.setAttributeNS( null, "EncodingType",
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary" );
        certificatePathAsBST.setAttributeNS(
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id",
            wsuIdForCertificateBST );
        certificatePathAsBST.appendChild( soapPart.createTextNode( this.ourBase64EncodedCertPath ) );
        return certificatePathAsBST;
    }

    private void addReferencesToSign( XMLSignature xmlSignature, NodeList nodesToSign, SOAPPart soapDoc )
        throws SOAPException, TransformationException, XMLSignatureException
    {
        int numNodes = nodesToSign.getLength();
        Element rootElement = soapDoc.getEnvelope();
        for ( int i = 0; i < numNodes; i++ )
        {
            Element ithElement = (Element) nodesToSign.item( i );
            Transforms transforms = new Transforms( soapDoc );
            if ( Utils.isDescendantOf( xmlSignature.getElement(), ithElement ) )
            {
                transforms.addTransform( "http://www.w3.org/2000/09/xmldsig#enveloped-signature" );
            }
            transforms.addTransform( this.c14nAlgo );

            if ( rootElement.equals( ithElement ) )
                xmlSignature.addDocument( "", transforms );
            else
                xmlSignature.addDocument( "#" + Utils.getElementId( ithElement ), transforms );
        }
    }

    private SOAPHeaderElement insertWSSecurityHeaderIfMissing( SOAPEnvelope soapEnvelope )
        throws SOAPException
    {
        Set targetActorSet = new HashSet();
        targetActorSet.add( this.targetActorURI );
        SOAPHeaderElement securityElement =
            Utils.getHeaderByNameAndActor( soapEnvelope, Constants.WS_SECURITY_SECURITY_QNAME, targetActorSet,
                this.targetActorURI == null );
        if ( securityElement == null )
        {
            Name wsseHeaderName =
                soapEnvelope.createName( "Security", "wsse",
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
            securityElement = soapEnvelope.getHeader().addHeaderElement( wsseHeaderName );
            if ( this.targetActorURI != null )
            {
                securityElement.setActor( this.targetActorURI );
            }
        }
        return securityElement;
    }

    private void setMissingIdsExceptOnRoot( NodeList nodeList, Element rootElement )
    {
        String prefixForSignedNodeIds = "Signed-" + hashCode();
        int numNodes = nodeList.getLength();
        for ( int i = 0; i < numNodes; i++ )
        {
            Node ithNodeToSign = nodeList.item( i );
            if ( !( ithNodeToSign instanceof Element ) )
            {
                throw new RuntimeException( "Current impl. limited to signing elements" );
            }
            Element ithElement = (Element) ithNodeToSign;

            if ( ( !rootElement.equals( ithElement ) ) && ( Utils.getElementId( ithElement ).equals( "" ) ) )
                Utils.setElementId( ithElement, prefixForSignedNodeIds + "+" + i );
        }
    }

    private void compileXPathToSign( Map config, String xPathString )
    {
        try
        {
            XPath xpathEngine = XPathFactory.newInstance().newXPath();
            String nsCatalogResourcePath = (String) config.get( "nsCatalogResourcePath" );
            if ( nsCatalogResourcePath != null )
            {
                NamespaceCatalog nsCatalog = null;
                try
                {
                    nsCatalog = new NamespaceCatalog( nsCatalogResourcePath );
                }
                catch ( IOException e )
                {
                    this.logger.error( e.getMessage(), e );
                    throw new RuntimeException( "Error loading namespace catalog: " + nsCatalogResourcePath, e );
                }
                xpathEngine.setNamespaceContext( nsCatalog );
            }
            this.xpathToSign = xpathEngine.compile( xPathString );
        }
        catch ( XPathExpressionException e )
        {
            this.logger.error( e.getMessage(), e );
            throw new RuntimeException( "Error compiling XPath: " + xPathString, e );
        }
    }

    public QName[] getHeaders()
    {
        return null;
    }
}