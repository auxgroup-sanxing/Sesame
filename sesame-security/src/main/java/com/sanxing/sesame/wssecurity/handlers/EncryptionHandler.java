package com.sanxing.sesame.wssecurity.handlers;

import com.sanxing.sesame.core.keymanager.KeyStoreInfo;
import com.sanxing.sesame.core.keymanager.ServiceKeyProvider;
import com.sanxing.sesame.wssecurity.commons.Constants;
import com.sanxing.sesame.wssecurity.commons.ExtendedGenericHandler;
import com.sanxing.sesame.wssecurity.commons.NamespaceCatalog;
import com.sanxing.sesame.wssecurity.commons.Utils;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
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
import org.apache.xml.security.Init;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.ReferenceList;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.x509.XMLX509IssuerSerial;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EncryptionHandler
    extends ExtendedGenericHandler
{
    public static final String IS_ON_REQUEST_SIDE_PARAM_NAME = "isOnRequestSide";

    boolean isOnRequestSide;

    public static final String XPATH_TO_ENCRYPT_PARAM_NAME = "xPathToEncrypt";

    private XPathExpression xpathToEncrypt;

    public static final String NS_CATALOG_RES_PATH_PARAMETER_NAME = "nsCatalogResourcePath";

    public static final String ONLY_ENCRYPT_CONTENT_PARAMETER_NAME = "onlyEncryptElementContent";

    private boolean onlyEncryptElementContent;

    public static final String DATA_ENCRYPTION_ALGO_PARAMETER_NAME = "dataEncryptionAlgo";

    private String dataEncryptionAlgo;

    public static final String KEY_ENCRYPTION_ALGO_PARAMETER_NAME = "keyEncryptionAlgo";

    private String keyEncryptionAlgo;

    public static final String KEY_STORE_RES_PATH_PARAMETER_NAME = "keyStoreResourcePath";

    private KeyStore keyStore;

    public static final String KEY_STORE_PASSWORD_PARAMETER_NAME = "keyStorePassword";

    public static final String TARGET_ACTOR_PARAMETER_NAME = "targetURI";

    private String targetActorURI;

    static Map encryptionConfig = new HashMap();

    static
    {
        encryptionConfig.put( "isOnRequestSide", "true" );
        encryptionConfig.put( "xPathToEncrypt", "soapenv:Body" );
        encryptionConfig.put( "onlyEncryptElementContent", "true" );
        encryptionConfig.put( "keyStoreResourcePath", "/client.ks" );
        encryptionConfig.put( "keyStorePassword", "client-ks-pass" );
        encryptionConfig.put( "targetURI", "s1" );
        encryptionConfig.put( "nsCatalogResourcePath", "/namespacecatalog.properties" );
        Init.init();
    }

    public void init( KeyStoreInfo keyStoreInfo, ServiceKeyProvider skp )
    {
        this.logger.debug( "Initializing " + getClass().getName() );

        this.isOnRequestSide = Boolean.parseBoolean( (String) encryptionConfig.get( "isOnRequestSide" ) );
        String xPathString = (String) encryptionConfig.get( "xPathToEncrypt" );
        if ( xPathString == null )
        {
            throw new RuntimeException( "Missing Mandatory Parameter: xPathToEncrypt" );
        }

        compileXPathToEncrypt( encryptionConfig, xPathString );

        this.onlyEncryptElementContent =
            Boolean.parseBoolean( (String) encryptionConfig.get( "onlyEncryptElementContent" ) );

        this.dataEncryptionAlgo = ( (String) encryptionConfig.get( "dataEncryptionAlgo" ) );
        if ( this.dataEncryptionAlgo == null )
        {
            this.dataEncryptionAlgo = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";
        }
        this.keyEncryptionAlgo = ( (String) encryptionConfig.get( "keyEncryptionAlgo" ) );
        if ( this.keyEncryptionAlgo == null )
        {
            this.keyEncryptionAlgo = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
        }
        try
        {
            this.keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
        }
        catch ( KeyStoreException e )
        {
            this.logger.error( e.getMessage(), e );
            throw new RuntimeException( "Error instantiating a keystore" );
        }
        String keyStoreResourcePath = keyStoreInfo.getKeystorePath();
        if ( keyStoreResourcePath == null )
        {
            throw new RuntimeException( "Missing Manadatory Parameter: keyStoreResourcePath" );
        }

        String keyStorePassword = keyStoreInfo.getStorePass();
        Utils.loadKeyStoreData( this.keyStore, keyStoreResourcePath, keyStorePassword, this );

        this.targetActorURI = skp.getAlias();

        this.logger.debug( "Initialization successful" );
    }

    private void compileXPathToEncrypt( Map config, String xPathString )
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
            this.xpathToEncrypt = xpathEngine.compile( xPathString );
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

    public boolean handleRequest( MessageContext messageContext, KeyStoreInfo keyStoreInfo, ServiceKeyProvider skp )
    {
        this.logger.debug( "Applying " + getClass().getName() + " to request" );
        init( keyStoreInfo, skp );
        if ( this.isOnRequestSide )
        {
            doEncryption( messageContext );
        }
        return true;
    }

    public boolean handleResponse( MessageContext messageContext, KeyStoreInfo keyStoreInfo, ServiceKeyProvider skp )
    {
        this.logger.debug( "Applying " + getClass().getName() + " to response" );
        init( keyStoreInfo, skp );
        if ( !this.isOnRequestSide )
        {
            doEncryption( messageContext );
        }
        return true;
    }

    private void doEncryption( MessageContext messageContext )
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
            Document soapDoc = soapEnvelope.getOwnerDocument();

            NodeList nodesToEncrypt = (NodeList) this.xpathToEncrypt.evaluate( soapEnvelope, XPathConstants.NODESET );
            int numNodesToEncrypt = nodesToEncrypt.getLength();

            String algorithmJCEName = JCEMapper.getJCEKeyAlgorithmFromURI( this.dataEncryptionAlgo );
            KeyGenerator keyGenerator = KeyGenerator.getInstance( algorithmJCEName );
            SecretKey symmetricKey = keyGenerator.generateKey();

            String prefixForEncryptedNodeIds = "EncryptedData-" + hashCode();
            for ( int i = 0; i < numNodesToEncrypt; i++ )
            {
                Node ithNodeToEncrypt = nodesToEncrypt.item( i );
                if ( !( ithNodeToEncrypt instanceof Element ) )
                {
                    throw new RuntimeException( "xPathToEncrypt matches non-elements" );
                }

                XMLCipher xmlDataCipher = XMLCipher.getInstance( this.dataEncryptionAlgo );
                xmlDataCipher.init( 1, symmetricKey );
                xmlDataCipher.getEncryptedData().setId( prefixForEncryptedNodeIds + "-" + i );
                xmlDataCipher.doFinal( soapDoc, (Element) ithNodeToEncrypt, this.onlyEncryptElementContent );
            }

            String recipientCertAlias = this.targetActorURI;
            if ( recipientCertAlias == null )
            {
                throw new UnsupportedOperationException(
                    "Yet to figure out how to get the target endpoint URI in a JAX-RPC handler" );
            }
            X509Certificate recipientCert = (X509Certificate) this.keyStore.getCertificate( recipientCertAlias );
            if ( recipientCert == null )
            {
                throw new RuntimeException( "Did not find a certificate in the keystore for: " + recipientCertAlias );
            }

            Cipher keyTransportCipher = Cipher.getInstance( JCEMapper.translateURItoJCEID( this.keyEncryptionAlgo ) );
            keyTransportCipher.init( 1, recipientCert );
            byte[] encryptedKeyBytes = keyTransportCipher.doFinal( symmetricKey.getEncoded() );

            XMLCipher xmlKeyCipher = XMLCipher.getInstance( this.keyEncryptionAlgo );
            EncryptedKey encryptedKey =
                xmlKeyCipher.createEncryptedKey( 1, new String( Base64.encodeBase64( encryptedKeyBytes ), "US-ASCII" ) );
            encryptedKey.setEncryptionMethod( xmlKeyCipher.createEncryptionMethod( this.keyEncryptionAlgo ) );
            ReferenceList encryptedDataRefList = xmlKeyCipher.createReferenceList( 1 );
            for ( int i = 0; i < numNodesToEncrypt; i++ )
            {
                encryptedDataRefList.add( encryptedDataRefList.newDataReference( "#" + prefixForEncryptedNodeIds + "-"
                    + i ) );
            }
            encryptedKey.setReferenceList( encryptedDataRefList );

            KeyInfo keyTransportKeyInfo = new KeyInfo( soapDoc );
            Element securityTokenReference =
                soapDoc.createElementNS(
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                    "SecurityTokenReference" );
            securityTokenReference.appendChild( new XMLX509IssuerSerial( soapDoc, recipientCert ).getElement() );
            keyTransportKeyInfo.addUnknownElement( securityTokenReference );
            encryptedKey.setKeyInfo( keyTransportKeyInfo );

            Element encryptedKeyElement = xmlKeyCipher.martial( soapDoc, encryptedKey );
            Set targetActorSet = new HashSet();
            targetActorSet.add( recipientCertAlias );
            SOAPHeaderElement securityElement =
                Utils.getHeaderByNameAndActor( soapEnvelope, Constants.WS_SECURITY_SECURITY_QNAME, targetActorSet,
                    this.targetActorURI == null );

            securityElement = (SOAPHeaderElement) soapEnvelope.getHeader().getFirstChild();
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

            securityElement.insertBefore( encryptedKeyElement, securityElement.getFirstChild() );
        }
        catch ( Exception e )
        {
            this.logger.error( e.getMessage(), e );
            if ( this.isOnRequestSide )
            {
                throw new JAXRPCException( e );
            }

            createFaultInContextAndThrow( soapContext, Constants.SOAP_SERVER_FAULT_CODE, " Error in encryption",
                faultActor, e );
        }
    }
}