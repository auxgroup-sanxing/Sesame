package com.sanxing.sesame.wssecurity.handlers;

import com.sanxing.sesame.wssecurity.commons.Constants;
import com.sanxing.sesame.wssecurity.commons.ExtendedGenericHandler;
import com.sanxing.sesame.wssecurity.commons.Utils;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSSecurityUsernameHandler
    extends ExtendedGenericHandler
{
    private static final QName[] HEADERS_HANDLED = { Constants.WS_SECURITY_SECURITY_QNAME };

    public static final String USERNAMETOKEN_MANDATORY_CONFIG_PARAM = "usernameTokenMandatory";

    private boolean usernameTokenMandatory;

    public void init( HandlerInfo handlerInfo )
    {
        this.logger.debug( "Initializing " + getClass().getName() );
        this.usernameTokenMandatory = true;
        Map config = handlerInfo.getHandlerConfig();
        if ( config.containsKey( "usernameTokenMandatory" ) )
            this.usernameTokenMandatory = Boolean.parseBoolean( (String) config.get( "usernameTokenMandatory" ) );
    }

    public QName[] getHeaders()
    {
        return HEADERS_HANDLED;
    }

    public boolean handleRequest( MessageContext messageContext )
    {
        this.logger.debug( "Applying " + getClass().getName() + " to request" );
        SOAPMessageContext soapContext = (SOAPMessageContext) messageContext;

        Set roleSet = getSOAPRoles( soapContext );

        String faultActor = null;
        if ( !roleSet.isEmpty() )
        {
            faultActor = (String) roleSet.iterator().next();
        }

        this.logger.debug( "Locating relevant WS-Security header" );
        SOAPMessage message = soapContext.getMessage();
        SOAPEnvelope soapEnvelope = null;
        SOAPHeader soapHeader = null;
        SOAPHeaderElement securityElement = null;
        try
        {
            soapEnvelope = message.getSOAPPart().getEnvelope();
            soapHeader = soapEnvelope.getHeader();
            securityElement =
                Utils.getHeaderByNameAndActor( soapEnvelope, Constants.WS_SECURITY_SECURITY_QNAME, roleSet, true );
        }
        catch ( SOAPException e )
        {
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE, "Malformed/Invalid envelope",
                faultActor, e );
        }
        if ( securityElement == null )
        {
            createFaultInContextAndThrow( soapContext, Constants.WS_SECURITY_INVALID_SECURITY_FAULT_CODE,
                "Did not find an applicable WS-Security header", faultActor );
        }

        String securityActor = securityElement.getActor();
        if ( securityActor != null )
        {
            if ( !"http://schemas.xmlsoap.org/soap/actor/next".equals( securityActor ) )
            {
                faultActor = securityActor;
            }
        }
        processUsernameToken( securityElement, soapContext, faultActor );
        processFirstBinarySecurityToken( securityElement, soapContext, faultActor );

        soapHeader.removeChild( securityElement );
        return true;
    }

    private boolean processUsernameToken( SOAPHeaderElement securityElement, SOAPMessageContext soapContext,
                                          String faultActor )
    {
        this.logger.debug( "Locating UsernameToken" );
        SOAPElement usernameTokenElement =
            Utils.locateChildSOAPElement( securityElement,
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "UsernameToken" );
        if ( usernameTokenElement == null )
        {
            if ( !this.usernameTokenMandatory )
                return false;
            createFaultInContextAndThrow( soapContext, Constants.WS_SECURITY_INVALID_SECURITY_FAULT_CODE,
                "UsernameToken element not found in WS-Security header", faultActor );
        }

        this.logger.debug( "Locating username in UsernameToken" );
        SOAPElement usernameElement =
            Utils.locateChildSOAPElement( usernameTokenElement,
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Username" );

        if ( usernameElement == null )
        {
            createFaultInContextAndThrow( soapContext, Constants.WS_SECURITY_INVALID_SECURITY_FAULT_CODE,
                "Missing Username in WS-Security UsernameToken header", faultActor );
        }
        String username = usernameElement.getValue();
        this.logger.debug( "username: " + username );

        this.logger.debug( "Locating password in UsernameToken" );
        SOAPElement passwordElement =
            Utils.locateChildSOAPElement( usernameTokenElement,
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Password" );

        if ( passwordElement == null )
        {
            createFaultInContextAndThrow( soapContext, Constants.WS_SECURITY_INVALID_SECURITY_FAULT_CODE,
                "Missing Password in WS-Security UsernameToken header", faultActor );
        }
        String password = passwordElement.getValue();

        soapContext.setProperty( "mc_username", username );
        soapContext.setProperty( "mc_password", password );
        soapContext.setProperty( "mc_password_type", passwordElement.getAttribute( "Type" ) );
        soapContext.setProperty( "mc_authentication_method", "urn:oasis:names:tc:SAML:1.0:am:password" );

        byte[] nonce = readNonceFromUsernameToken( usernameTokenElement, soapContext, faultActor );
        soapContext.setProperty( "mc_nonce", nonce );

        String creationTimeAsString = null;
        this.logger.debug( "Locating creation time in UsernameToken" );
        SOAPElement creationTimeElement =
            Utils.locateChildSOAPElement( usernameTokenElement,
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Created" );
        if ( creationTimeElement != null )
        {
            creationTimeAsString = creationTimeElement.getValue();
        }
        soapContext.setProperty( "mc_creation_time_as_string", creationTimeAsString );

        if ( ( nonce != null ) && ( creationTimeAsString == null ) )
        {
            createFaultInContextAndThrow( soapContext, Constants.WS_SECURITY_INVALID_SECURITY_FAULT_CODE,
                "Creation timestamp is mandatory if Nonce is provided", faultActor );
        }

        return true;
    }

    private byte[] readNonceFromUsernameToken( SOAPElement usernameTokenElement, SOAPMessageContext soapContext,
                                               String faultActor )
        throws SOAPFaultException
    {
        byte[] nonce = (byte[]) null;
        this.logger.debug( "Locating nonce in UsernameToken" );

        SOAPElement nonceElement =
            Utils.locateChildSOAPElement( usernameTokenElement,
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Nonce" );
        if ( nonceElement != null )
        {
            String encodingType = nonceElement.getAttribute( "EncodingType" );
            if ( ( encodingType == null )
                || ( encodingType.length() == 0 )
                || ( encodingType.equals( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary" ) ) )
                try
                {
                    nonce = Base64.decodeBase64( nonceElement.getValue().getBytes( "US-ASCII" ) );
                }
                catch ( UnsupportedEncodingException e )
                {
                    throw new RuntimeException( e );
                }
            else
            {
                createFaultInContextAndThrow( soapContext, Constants.WS_SECURITY_UNSUPPORTED_ALGO_FAULT_CODE,
                    "Nonce EncodingType not understood", faultActor );
            }
        }
        return nonce;
    }

    private boolean processFirstBinarySecurityToken( SOAPHeaderElement securityElement, SOAPMessageContext soapContext,
                                                     String faultActor )
    {
        this.logger.debug( "Locating BinarySecurityToken" );
        SOAPElement tokenElement =
            Utils.locateChildSOAPElement( securityElement,
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "BinarySecurityToken" );
        if ( tokenElement == null )
            return false;

        byte[] token = (byte[]) null;
        String encodingType = tokenElement.getAttribute( "EncodingType" );
        if ( ( encodingType == null )
            || ( encodingType.length() == 0 )
            || ( encodingType.equals( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary" ) ) )
            try
            {
                token = Base64.decodeBase64( tokenElement.getValue().getBytes( "US-ASCII" ) );
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new RuntimeException( e );
            }
        else
        {
            createFaultInContextAndThrow( soapContext, Constants.WS_SECURITY_UNSUPPORTED_ALGO_FAULT_CODE,
                "BinarySecurityToken EncodingType not understood", faultActor );
        }

        String tokenType = tokenElement.getAttribute( "ValueType" );

        if ( "http://www.docs.oasis-open.org/wss/2004/07/oasis-000000-wss-kerberos-token-profile-1.0#Kerberosv5_AP_REQ".equals( tokenType ) )
        {
            soapContext.setProperty( "mc_gss_token", token );
            soapContext.setProperty( "mc_authentication_method", "urn:ietf:rfc:1510" );
        }
        else
        {
            this.logger.warn( "Ignoring unknown binary security token of type: " + tokenType );
        }
        return true;
    }
}