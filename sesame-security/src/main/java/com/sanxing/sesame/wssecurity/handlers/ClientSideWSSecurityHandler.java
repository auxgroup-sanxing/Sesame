package com.sanxing.sesame.wssecurity.handlers;

import com.sanxing.sesame.wssecurity.jaas.MessageContextBackedCallbackHandler;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSideWSSecurityHandler
    extends GenericHandler
{
    private String targetActorURI;

    public static final String TARGET_ACTOR_PARAMETER_NAME = "targetURI";

    public static final String AUTH_MODE_PARAMETER_NAME = "authMode";

    public static final int NO_AUTH_MODE = 0;

    public static final int CLEAR_TEXT_PASSWORD_AUTH_MODE = 1;

    public static final int PASSWORD_DIGEST_AUTH_MODE = 2;

    public static final int KERBEROS5_AUTH_MODE = 3;

    private int authMode = 0;

    private SecureRandom nonceGenerator = null;

    public static final String NUM_BITS_IN_NONCE_PARAMETER_NAME = "numBitsInNonce";

    private int numBitsInNonce = 16;

    protected MessageDigest digester;

    private DatatypeFactory xmlDatatypeFactory;

    private String jaasAppName;

    public static final String JAAS_APP_CONFIG_PARAM = "jaasAppName";

    public static final Logger logger = LoggerFactory.getLogger( WSSecurityUsernameHandler.class );

    public void init( HandlerInfo handlerInfo )
    {
        logger.debug( "Initializing " + getClass().getName() );
        Map config = handlerInfo.getHandlerConfig();
        this.targetActorURI = ( (String) config.get( "targetURI" ) );

        String authModeString = (String) config.get( "authMode" );
        if ( authModeString == null )
            logger.warn( "No authentication mode specified. Auth skipped." );
        else
        {
            this.authMode = Integer.parseInt( authModeString );
        }

        switch ( this.authMode )
        {
            case 2:
                try
                {
                    this.nonceGenerator = SecureRandom.getInstance( "SHA1PRNG" );
                    this.digester = MessageDigest.getInstance( "SHA-1" );
                }
                catch ( NoSuchAlgorithmException e )
                {
                    logger.error( e.getMessage(), e );
                    throw new RuntimeException( e );
                }
                try
                {
                    this.xmlDatatypeFactory = DatatypeFactory.newInstance();
                }
                catch ( DatatypeConfigurationException e )
                {
                    logger.error( e.getMessage(), e );
                    throw new RuntimeException( e );
                }
                String numBitsInNonceStr = (String) config.get( "numBitsInNonce" );
                if ( numBitsInNonceStr != null )
                {
                    this.numBitsInNonce = Integer.parseInt( numBitsInNonceStr );
                }
                break;
            case 3:
                this.jaasAppName = ( (String) config.get( "jaasAppName" ) );
                if ( this.jaasAppName == null )
                {
                    throw new JAXRPCException( "Missing config param: jaasAppName" );
                }

                break;
        }

        logger.debug( "Initialization successful" );
    }

    public QName[] getHeaders()
    {
        return null;
    }

    public boolean handleRequest( MessageContext messageContext )
    {
        logger.debug( "Applying " + getClass().getName() + " to request" );
        SOAPMessageContext soapContext = (SOAPMessageContext) messageContext;
        try
        {
            SOAPMessage message = soapContext.getMessage();
            SOAPEnvelope soapEnvelope = message.getSOAPPart().getEnvelope();

            Name wsseHeaderName =
                soapEnvelope.createName( "Security", "wsse",
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
            SOAPHeaderElement securityElement = soapEnvelope.getHeader().addHeaderElement( wsseHeaderName );
            if ( this.targetActorURI != null )
            {
                securityElement.setActor( this.targetActorURI );
            }

            switch ( this.authMode )
            {
                case 1:
                case 2:
                    addUsernameToken( soapEnvelope, securityElement, messageContext );
                    break;
                case 3:
                    Subject kerberosSubject = null;
                    try
                    {
                        logger.debug( "Getting TGT from KDC via JAAS" );

                        LoginContext loginContext =
                            new LoginContext( this.jaasAppName,
                                new MessageContextBackedCallbackHandler( messageContext ) );
                        loginContext.login();
                        kerberosSubject = loginContext.getSubject();
                        logger.info( "Obtained TGT from Kerberos" );
                    }
                    catch ( LoginException e )
                    {
                        throw new JAXRPCException( "Auth failure when getting TGT from KDC", e );
                    }
                    catch ( Exception e )
                    {
                        logger.warn( "Error when getting TGT from KDC", e );
                        throw new RuntimeException( e );
                    }
                    byte[] serviceToken =
                        (byte[]) Subject.doAs( kerberosSubject, new ServiceTicketGrabber( kerberosSubject,
                            messageContext ) );
                    addKerberosToken( soapEnvelope, securityElement, messageContext, serviceToken );
            }
        }
        catch ( SOAPException e )
        {
            logger.error( e.getMessage(), e );
            throw new JAXRPCException( "Malformed/Invalid envelope: ", e );
        }

        return true;
    }

    private void addUsernameToken( SOAPEnvelope soapEnvelope, SOAPHeaderElement securityElement,
                                   MessageContext messageContext )
        throws SOAPException
    {
        String username = null;
        if ( messageContext.containsProperty( "mc_username" ) )
            username = (String) messageContext.getProperty( "mc_username" );
        else
        {
            throw new JAXRPCException( "Missing username in the message context" );
        }
        String password = null;
        if ( messageContext.containsProperty( "mc_password" ) )
        {
            password = (String) messageContext.getProperty( "mc_password" );
            messageContext.removeProperty( "mc_password" );
        }

        Name userTokenElementName =
            soapEnvelope.createName( "UsernameToken", "wsse",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
        SOAPElement userTokenElement = securityElement.addChildElement( userTokenElementName );

        Name usernameElementName =
            soapEnvelope.createName( "Username", "wsse",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
        SOAPElement usernameElement = userTokenElement.addChildElement( usernameElementName );
        usernameElement.addTextNode( username );

        if ( password != null )
        {
            Name passwordElementName =
                soapEnvelope.createName( "Password", "wsse",
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
            SOAPElement passwordElement = userTokenElement.addChildElement( passwordElementName );

            switch ( this.authMode )
            {
                case 1:
                    passwordElement.addTextNode( password );
                    break;
                case 2:
                    Name passwordTypeAttrName = soapEnvelope.createName( "Type" );
                    passwordElement.addAttribute( passwordTypeAttrName,
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd#PasswordDigest" );

                    byte[] nonce = new byte[this.numBitsInNonce];
                    this.nonceGenerator.nextBytes( nonce );

                    XMLGregorianCalendar now =
                        this.xmlDatatypeFactory.newXMLGregorianCalendar( new GregorianCalendar(
                            TimeZone.getTimeZone( "UTC" ) ) );
                    String created = now.toXMLFormat();
                    try
                    {
                        byte[] utf8Password = password.getBytes( "UTF-8" );
                        byte[] sha1Password = this.digester.digest( utf8Password );
                        byte[] base64EncodedSHA1Password = Base64.encodeBase64( sha1Password );

                        this.digester.update( nonce );
                        this.digester.update( created.getBytes( "UTF-8" ) );
                        this.digester.update( base64EncodedSHA1Password );
                        byte[] digest = Base64.encodeBase64( this.digester.digest() );
                        passwordElement.addTextNode( new String( digest, "US-ASCII" ) );

                        Name nonceElementName =
                            soapEnvelope.createName( "Nonce", "wsse",
                                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
                        SOAPElement nonceElement = userTokenElement.addChildElement( nonceElementName );
                        nonceElement.addTextNode( new String( Base64.encodeBase64( nonce ), "US-ASCII" ) );
                        Name encodingTypeAttrName = soapEnvelope.createName( "EncodingType" );
                        nonceElement.addAttribute( encodingTypeAttrName,
                            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary" );
                    }
                    catch ( UnsupportedEncodingException e )
                    {
                        logger.error( e.getMessage(), e );
                        throw new RuntimeException( e );
                    }

                    Name createdElementName =
                        soapEnvelope.createName( "Created", "wsu",
                            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" );
                    SOAPElement createdElement = userTokenElement.addChildElement( createdElementName );
                    createdElement.addTextNode( created );
            }
        }
    }

    protected void addKerberosToken( SOAPEnvelope soapEnvelope, SOAPHeaderElement securityElement,
                                     MessageContext messageContext, byte[] serviceTicket )
        throws SOAPException
    {
        Name tokenElementName =
            soapEnvelope.createName( "BinarySecurityToken", "wsse",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );

        SOAPElement tokenElement = securityElement.addChildElement( tokenElementName );

        Name valueTypeAttrName = soapEnvelope.createName( "ValueType" );
        tokenElement.addAttribute( valueTypeAttrName,
            "http://www.docs.oasis-open.org/wss/2004/07/oasis-000000-wss-kerberos-token-profile-1.0#Kerberosv5_AP_REQ" );

        Name encodingTypeAttrName = soapEnvelope.createName( "EncodingType" );
        tokenElement.addAttribute( encodingTypeAttrName,
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary" );
        try
        {
            tokenElement.addTextNode( new String( Base64.encodeBase64( serviceTicket ), "US-ASCII" ) );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( e );
        }
    }

    private class ServiceTicketGrabber
        implements PrivilegedAction
    {
        private Subject kerberosSubject;

        private MessageContext messageContext;

        public ServiceTicketGrabber( Subject kerberosSubject, MessageContext messageContext )
        {
            this.kerberosSubject = kerberosSubject;
            this.messageContext = messageContext;
        }

        public Object run()
        {
            GSSManager gssManager = GSSManager.getInstance();
            Set kerberosPrincipals = this.kerberosSubject.getPrincipals( KerberosPrincipal.class );
            if ( kerberosPrincipals.size() < 1 )
            {
                throw new RuntimeException( "ServiceTicketGrabber requires a Kerberos Principal" );
            }
            KerberosPrincipal kerberosPrincipal = (KerberosPrincipal) kerberosPrincipals.iterator().next();
            try
            {
                Oid kerberos5Oid = new Oid( "1.2.840.113554.1.2.2" );

                GSSName clientName = gssManager.createName( kerberosPrincipal.getName(), GSSName.NT_USER_NAME );
                GSSCredential clientCredentials = gssManager.createCredential( clientName, 0, kerberos5Oid, 1 );

                String serviceNameStr = (String) this.messageContext.getProperty( "mc_kerberosNameOfTargetService" );

                if ( serviceNameStr == null )
                {
                    throw new NullPointerException( "Missing on message context: mc_kerberosNameOfTargetService" );
                }

                GSSName serviceName = gssManager.createName( serviceNameStr, GSSName.NT_HOSTBASED_SERVICE );

                GSSContext gssContext = gssManager.createContext( serviceName, kerberos5Oid, clientCredentials, 0 );
                return gssContext.initSecContext( new byte[0], 0, 0 );
            }
            catch ( GSSException e )
            {
                throw new RuntimeException( e );
            }
        }
    }
}