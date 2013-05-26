package com.sanxing.sesame.wssecurity.handlers;

import com.sanxing.sesame.core.keymanager.KeyStoreInfo;
import com.sanxing.sesame.core.keymanager.ServiceKeyProvider;
import com.sanxing.sesame.wssecurity.commons.Constants;
import com.sanxing.sesame.wssecurity.commons.ExtendedGenericHandler;
import com.sanxing.sesame.wssecurity.commons.Utils;
import com.sanxing.sesame.wssecurity.commons.WsuIdResolver;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import org.apache.commons.codec.binary.Base64;
import org.apache.xml.security.Init;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.encryption.CipherData;
import org.apache.xml.security.encryption.CipherValue;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.EncryptionMethod;
import org.apache.xml.security.encryption.Reference;
import org.apache.xml.security.encryption.ReferenceList;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DecryptionHandler
    extends ExtendedGenericHandler
{
    private static final QName[] HEADERS_HANDLED = { Constants.WS_SECURITY_SECURITY_QNAME };

    public static final String IS_ON_REQUEST_SIDE_PARAM_NAME = "isOnRequestSide";

    boolean isOnRequestSide;

    public static final String KEY_STORE_RES_PATH_PARAMETER_NAME = "keyStoreResourcePath";

    private KeyStore keyStore;

    public static final String KEY_STORE_PASSWORD_PARAMETER_NAME = "keyStorePassword";

    public static final String OUR_KEY_ALIAS_PARAMETER_NAME = "ourKeyAlias";

    public static final String OUR_KEY_PASSWORD_PARAMETER_NAME = "ourKeyPassword";

    private Key ourKey;

    private Certificate ourCertificate;

    public static final String ALLOW_UNAUTHENTICATED_PARAM_NAME = "allowUnauthenticatedToGoPast";

    private boolean allowUnauthenticatedToGoPast;

    static Map config = new HashMap();

    static
    {
        Init.init();
        config.put( "isOnRequestSide", "fasle" );
        config.put( "keyStoreResourcePath", "/client.ks" );
        config.put( "keyStorePassword", "client-ks-pass" );
        config.put( "ourKeyAlias", "c1" );
        config.put( "ourKeyPassword", "client-ks-pass" );
    }

    public void init( KeyStoreInfo keyStoreInfo, ServiceKeyProvider skp1 )
    {
        this.logger.debug( "Initializing " + getClass().getName() );

        this.isOnRequestSide = Boolean.parseBoolean( (String) config.get( "isOnRequestSide" ) );
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
        try
        {
            this.ourKey = skp1.getKey();
            this.ourCertificate = skp1.getCert();
        }
        catch ( Exception e )
        {
            this.logger.error( e.getMessage(), e );
            throw new RuntimeException( "Failed to retrieve the key/cert to use in decryption", e );
        }

        this.allowUnauthenticatedToGoPast = Boolean.parseBoolean( "allowUnauthenticatedToGoPast" );
    }

    public boolean handleRequest( MessageContext messageContext, KeyStoreInfo keyStoreInfo, ServiceKeyProvider skp1 )
    {
        this.logger.debug( "Applying " + getClass().getName() + " to request" );
        init( keyStoreInfo, skp1 );
        if ( this.isOnRequestSide )
        {
            decryptAndOrVerifySignatures( messageContext, keyStoreInfo, skp1 );
        }

        return true;
    }

    public boolean handleResponse( MessageContext messageContext, KeyStoreInfo keyStoreInfo, ServiceKeyProvider skp1 )
    {
        this.logger.debug( "Applying " + getClass().getName() + " to response" );
        init( keyStoreInfo, skp1 );
        if ( !this.isOnRequestSide )
        {
            decryptAndOrVerifySignatures( messageContext, keyStoreInfo, skp1 );
        }
        return true;
    }

    public void decryptAndOrVerifySignatures( MessageContext messageContext, KeyStoreInfo keyStoreInfo,
                                              ServiceKeyProvider skp )
    {
        init( keyStoreInfo, skp );
        SOAPMessageContext soapContext = (SOAPMessageContext) messageContext;

        String faultActor = null;
        Set roleSet = getSOAPRoles( soapContext );
        if ( !roleSet.isEmpty() )
        {
            faultActor = (String) roleSet.iterator().next();
        }

        this.logger.debug( "Locating relevant WS-Security header" );
        SOAPMessage message = soapContext.getMessage();
        SOAPHeaderElement securityElement = getSecurityElementForThisTarget( soapContext, faultActor, roleSet, message );
        if ( securityElement == null )
        {
            this.logger.warn( "Did not find an applicable WS-Security header" );
            return;
        }

        String securityActor = securityElement.getActor();
        if ( securityActor != null )
        {
            if ( !"http://schemas.xmlsoap.org/soap/actor/next".equals( securityActor ) )
            {
                faultActor = securityActor;
            }

        }

        Map securityTokenCache = new HashMap();

        Iterator headerEntriesIter = securityElement.getChildElements();
        while ( headerEntriesIter.hasNext() )
        {
            Object child = headerEntriesIter.next();
            if ( ( child instanceof SOAPElement ) )
            {
                SOAPElement childElement = (SOAPElement) child;
                String childNamespaceURI = childElement.getNamespaceURI();

                if ( "http://www.w3.org/2001/04/xmlenc#".equals( childNamespaceURI ) )
                {
                    processEncryptionEntry( childElement, soapContext, faultActor );
                }
                else if ( "http://www.w3.org/2000/09/xmldsig#".equals( childNamespaceURI ) )
                {
                    processSignatureEntry( childElement, soapContext, faultActor, securityTokenCache );
                }
                else
                {
                    if ( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd".equals( childNamespaceURI ) )
                    {
                        if ( "BinarySecurityToken".equals( childElement.getLocalName() ) )
                        {
                            processBST( childElement, soapContext, faultActor, securityTokenCache );

                            continue;
                        }
                    }

                    this.logger.warn( "Not directly processing " + childElement.getLocalName() );
                }
            }
        }
        try
        {
            message.getSOAPPart().getEnvelope().getHeader().removeChild( securityElement );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }

        if ( ( this.isOnRequestSide ) && ( !this.allowUnauthenticatedToGoPast )
            && ( !messageContext.containsProperty( "mc_authenticatedSubject" ) ) )
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE, "Authentication failed",
                faultActor );
    }

    private Object processBST( Element bstElement, SOAPMessageContext soapContext, String faultActor,
                               Map securityTokenCache )
    {
        try
        {
            String tokenId = Utils.getElementId( bstElement );
            this.logger.debug( "Processing BST with id: " + tokenId );
            Object token = securityTokenCache.get( tokenId );
            if ( token != null )
            {
                this.logger.debug( "We already have a cached security token with id: " + tokenId
                    + ". We won't process it again!" );
                return token;
            }

            String bstType = bstElement.getAttributeNS( null, "ValueType" );

            if ( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3".equals( bstType ) )
            {
                byte[] certBytes = decodeBST( bstElement );
                Certificate cert =
                    CertificateFactory.getInstance( "X.509" ).generateCertificate( new ByteArrayInputStream( certBytes ) );
                Utils.validateCertificateChain( cert, this.keyStore );

                securityTokenCache.put( tokenId, cert );
                return cert;
            }

            if ( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd#X509PKIPathv1".equals( bstType ) )
            {
                byte[] certPathBytes = decodeBST( bstElement );
                CertPath certPath =
                    CertificateFactory.getInstance( "X.509" ).generateCertPath(
                        new ByteArrayInputStream( certPathBytes ) );
                Utils.validateCertificationChain( certPath, this.keyStore );
                securityTokenCache.put( tokenId, certPath );
                return certPath;
            }
            this.logger.debug( "Unrecognized BST ValueType: " + bstType );

            securityTokenCache.put( tokenId, bstElement );
            return bstElement;
        }
        catch ( Exception e )
        {
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE,
                "Error processing Binary Security Token: ", faultActor, e );
        }
        return null;
    }

    private byte[] decodeBST( Element bstElement )
    {
        String encodingType = bstElement.getAttributeNS( null, "EncodingType" );
        if ( ( encodingType == null )
            || ( encodingType.length() == 0 )
            || ( encodingType.equals( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary" ) ) )
        {
            try
            {
                return Base64.decodeBase64( Utils.getTextFromDOMNode( bstElement ).getBytes( "US-ASCII" ) );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
        throw new RuntimeException( "Unrecognzied EncodingType for BST: " + encodingType );
    }

    private void processSignatureEntry( SOAPElement signatureRelatedElement, SOAPMessageContext soapContext,
                                        String faultActor, Map securityTokenCache )
    {
        String tag = signatureRelatedElement.getLocalName();

        if ( !"Signature".equals( tag ) )
        {
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE,
                "Unsupported signature-related WS-Security header: " + tag, faultActor );
        }

        try
        {
            XMLSignature xmlSignature = new XMLSignature( signatureRelatedElement, "" );

            Object signingToken =
                readAndValidateSigningToken( xmlSignature, soapContext, faultActor, securityTokenCache );
            X509Certificate signingCert;
            if ( ( signingToken instanceof CertPath ) )
            {
                signingCert = (X509Certificate) ( (CertPath) signingToken ).getCertificates().get( 0 );
            }
            else
            {
                if ( ( signingToken instanceof Certificate ) )
                    signingCert = (X509Certificate) signingToken;
                else
                    throw new UnsupportedOperationException(
                        "Signing with anything other than certificates not supported" );
            }
            SOAPMessage message = soapContext.getMessage();
            SOAPPart soapPart = message.getSOAPPart();
            xmlSignature.addResourceResolver( new WsuIdResolver( soapPart ) );
            if ( xmlSignature.checkSignatureValue( signingCert ) )
            {
                Subject authenticatedSubject = new Subject();
                authenticatedSubject.getPrincipals().add( signingCert.getSubjectX500Principal() );
                soapContext.setProperty( "mc_authenticatedSubject", authenticatedSubject );
                soapContext.setProperty( "mc_username", signingCert.getSubjectX500Principal().getName() );
            }
            else
            {
                throw new RuntimeException( "Signature did not validate" );
            }
        }
        catch ( Exception e )
        {
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE,
                "Error in signature verification", faultActor, e );
        }
    }

    private Object readAndValidateSigningToken( XMLSignature xmlSignature, SOAPMessageContext soapContext,
                                                String faultActor, Map securityTokenCache )
        throws SOAPException
    {
        String signingTokenId = getSigningTokenId( xmlSignature );
        Object signingToken = securityTokenCache.get( signingTokenId );
        if ( signingToken == null )
        {
            Element signingTokenElement =
                Utils.lookupElementById( soapContext.getMessage().getSOAPPart().getEnvelope(), signingTokenId );
            if ( signingTokenElement == null )
            {
                throw new RuntimeException( "Cannot locate SecurityToken with Id: " + signingTokenId );
            }

            if ( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd".equals( signingTokenElement.getNamespaceURI() ) )
            {
                if ( "BinarySecurityToken".equals( signingTokenElement.getLocalName() ) )
                {
                    signingToken = processBST( signingTokenElement, soapContext, faultActor, securityTokenCache );

                    return signingToken;
                }
            }

            throw new RuntimeException( "Can only use a BinarySecurityToken as a signing token" );
        }

        return signingToken;
    }

    private String getSigningTokenId( XMLSignature xmlSignature )
    {
        KeyInfo keyInfo = xmlSignature.getKeyInfo();
        Element securityTokenRef =
            Utils.locateChildDOMElement( keyInfo.getElement(),
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "SecurityTokenReference" );
        if ( securityTokenRef == null )
        {
            throw new RuntimeException( "Missing SecurityTokenReference in KeyInfo" );
        }
        Element reference =
            Utils.locateChildDOMElement( securityTokenRef,
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Reference" );
        if ( reference == null )
        {
            throw new RuntimeException( "Missing Reference in SecurityTokenReference" );
        }
        String refURI = reference.getAttributeNS( null, "URI" );
        if ( ( refURI == null ) || ( refURI.trim().length() < 2 ) )
        {
            throw new RuntimeException( "URI attribute missing on Reference" );
        }
        if ( !refURI.startsWith( "#" ) )
        {
            throw new RuntimeException( "Cannot resolve any URI other than fragment Ids" );
        }
        return refURI.substring( 1 );
    }

    private void processEncryptionEntry( SOAPElement encryptionRelatedElement, SOAPMessageContext soapContext,
                                         String faultActor )
    {
        String tag = encryptionRelatedElement.getLocalName();
        if ( "ReferenceList".equals( tag ) )
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE,
                "Key encryption using a shared secret not supported", faultActor );
        else if ( "EncryptedKey".equals( tag ) )
            processEncryptedKey( encryptionRelatedElement, soapContext, faultActor );
        else
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE,
                "Unsupported encryption-related WS-Security header: " + tag, faultActor );
    }

    private SOAPHeaderElement getSecurityElementForThisTarget( SOAPMessageContext soapContext, String faultActor,
                                                               Set roleSet, SOAPMessage message )
    {
        SOAPHeaderElement securityElement = null;
        try
        {
            SOAPEnvelope soapEnvelope = message.getSOAPPart().getEnvelope();
            SOAPHeader soapHeader = soapEnvelope.getHeader();
            securityElement =
                Utils.getHeaderByNameAndActor( soapEnvelope, Constants.WS_SECURITY_SECURITY_QNAME, roleSet, true );
        }
        catch ( SOAPException e )
        {
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE, "Malformed/Invalid envelope",
                faultActor, e );
        }
        return securityElement;
    }

    private void processEncryptedKey( SOAPElement encryptedKeyElement, SOAPMessageContext soapContext, String faultActor )
    {
        try
        {
            SOAPMessage message = soapContext.getMessage();
            SOAPPart soapPart = message.getSOAPPart();
            SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
            Document soapDoc = soapEnvelope.getOwnerDocument();

            XMLCipher xmlKeyCipher = XMLCipher.getInstance();
            xmlKeyCipher.init( 2, null );
            EncryptedKey encryptedKey = xmlKeyCipher.loadEncryptedKey( soapDoc, encryptedKeyElement );

            KeyInfo keyEncryptionKeyInfo = encryptedKey.getKeyInfo();

            String keyEncryptionAlgo = getKeyTransportAlgorithm( encryptedKey );

            byte[] decryptedKeyBytes = getDecryptedKey( encryptedKey, keyEncryptionAlgo );

            ReferenceList refsToEncryptedData = encryptedKey.getReferenceList();
            decryptData( soapEnvelope, refsToEncryptedData, decryptedKeyBytes );
        }
        catch ( Exception e )
        {
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE, "Error in decryption",
                faultActor, e );
        }
    }

    private void decryptData( SOAPEnvelope soapEnvelope, ReferenceList refsToEncryptedData, byte[] decryptedKeyBytes )
        throws XMLEncryptionException, Exception
    {
        Document soapDoc = soapEnvelope.getOwnerDocument();

        Iterator i = refsToEncryptedData.getReferences();
        while ( i.hasNext() )
        {
            Reference ithRef = (Reference) i.next();
            String refURI = ithRef.getURI();
            if ( !refURI.startsWith( "#" ) )
            {
                throw new RuntimeException( "Bad URI in Reference" );
            }
            Element referred = Utils.lookupElementById( soapEnvelope, refURI.substring( 1 ) );
            if ( referred == null )
            {
                throw new RuntimeException( "No element found bearing the Id: " + refURI.substring( 1 ) );
            }

            if ( "http://www.w3.org/2001/04/xmlenc#".equals( referred.getNamespaceURI() ) )
            {
                if ( "EncryptedData".equals( referred.getLocalName() ) )
                {
                    XMLCipher xmlDataCipher = XMLCipher.getInstance();
                    xmlDataCipher.init( 2, null );
                    EncryptedData encryptedData = xmlDataCipher.loadEncryptedData( soapDoc, referred );
                    String encryptionAlgorithm = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";
                    EncryptionMethod encryptionMethod = encryptedData.getEncryptionMethod();
                    if ( encryptionMethod != null )
                    {
                        encryptionAlgorithm = encryptionMethod.getAlgorithm();
                    }
                    SecretKeySpec key =
                        new SecretKeySpec( decryptedKeyBytes, JCEMapper.getJCEKeyAlgorithmFromURI( encryptionAlgorithm ) );

                    xmlDataCipher.init( 2, key );

                    referred = soapEnvelope.getBody();

                    xmlDataCipher.doFinal( soapDoc, referred,
                        "http://www.w3.org/2001/04/xmlenc#Content".equals( encryptedData.getType() ) );

                    continue;
                }

            }

            throw new RuntimeException( "Unsupported child element in EncryptedKey/ReferenceList: {"
                + referred.getNamespaceURI() + "}" + referred.getNodeName() );
        }
    }

    private byte[] getDecryptedKey( EncryptedKey encryptedKey, String keyEncryptionAlgo )
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException,
        IllegalBlockSizeException, BadPaddingException
    {
        Cipher keyTransportCipher = Cipher.getInstance( JCEMapper.translateURItoJCEID( keyEncryptionAlgo ) );
        keyTransportCipher.init( 2, this.ourKey );
        CipherData encryptedKeyData = encryptedKey.getCipherData();
        if ( encryptedKeyData == null )
        {
            throw new RuntimeException( "CipherData missing in EncryptedKey" );
        }
        byte[] decryptedKeyBytes = (byte[]) null;
        switch ( encryptedKeyData.getDataType() )
        {
            case 1:
                byte[] encryptedKeyBase64Bytes = encryptedKeyData.getCipherValue().getValue().getBytes( "US-ASCII" );
                byte[] encryptedKeyBytes = Base64.decodeBase64( encryptedKeyBase64Bytes );
                decryptedKeyBytes = keyTransportCipher.doFinal( encryptedKeyBytes );
                break;
            case 2:
                throw new UnsupportedOperationException( "CipherReference not supported" );
            default:
                throw new RuntimeException( "CipherData cannot be anything other thanCipherValue or CipherReference" );
        }

        return decryptedKeyBytes;
    }

    private String getKeyTransportAlgorithm( EncryptedKey encryptedKey )
    {
        String keyEncryptionAlgo = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
        EncryptionMethod keyTransportMethod = encryptedKey.getEncryptionMethod();
        if ( keyTransportMethod != null )
        {
            keyEncryptionAlgo = keyTransportMethod.getAlgorithm();
        }
        return keyEncryptionAlgo;
    }

    public QName[] getHeaders()
    {
        return HEADERS_HANDLED;
    }
}