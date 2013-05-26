package com.sanxing.sesame.wssecurity.handlers;

import com.sanxing.sesame.wssecurity.commons.Constants;
import com.sanxing.sesame.wssecurity.commons.ExtendedGenericHandler;
import com.sanxing.sesame.wssecurity.commons.Utils;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAudienceRestrictionCondition;
import org.opensaml.SAMLAuthenticationStatement;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLSubject;

public class SAMLCreationHandler
    extends ExtendedGenericHandler
{
    public static final String VALIDITY_INTERVAL_PARAM_NAME = "assertionValidityIntervalInSecs";

    private int validityInterval;

    public void init( HandlerInfo handlerInfo )
    {
        this.logger.debug( "Initializing " + getClass().getName() );
        Map config = handlerInfo.getHandlerConfig();
        this.validityInterval = 180;

        if ( config.containsKey( "assertionValidityIntervalInSecs" ) )
            this.validityInterval = Integer.parseInt( (String) config.get( "assertionValidityIntervalInSecs" ) );
    }

    public boolean handleRequest( MessageContext messageContext )
    {
        this.logger.debug( "Applying " + getClass().getName() + " to request" );
        SOAPMessageContext soapContext = (SOAPMessageContext) messageContext;

        String faultActor = null;
        Set roleSet = getSOAPRoles( soapContext );
        if ( !roleSet.isEmpty() )
        {
            faultActor = (String) roleSet.iterator().next();
        }

        if ( !messageContext.containsProperty( "mc_authenticatedSubject" ) )
        {
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE, "Authentication failed",
                faultActor );
        }

        try
        {
            this.logger.debug( "Locating WS-Security header targeted at endpoint" );
            SOAPMessage message = soapContext.getMessage();
            SOAPPart soapPart = message.getSOAPPart();
            SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
            SOAPHeaderElement securityElement =
                Utils.getHeaderByNameAndActor( soapEnvelope, Constants.WS_SECURITY_SECURITY_QNAME, null, true );
            if ( securityElement == null )
            {
                Name wsseHeaderName =
                    soapEnvelope.createName( "Security", "wsse",
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
                securityElement = soapEnvelope.getHeader().addHeaderElement( wsseHeaderName );
            }

            String username = (String) messageContext.getProperty( "mc_username" );
            if ( username == null )
            {
                Subject authenticatedSubject = (Subject) messageContext.getProperty( "mc_authenticatedSubject" );

                if ( authenticatedSubject != null )
                {
                    Set kerberosPrincipals = authenticatedSubject.getPrincipals( KerberosPrincipal.class );
                    if ( !kerberosPrincipals.isEmpty() )
                    {
                        KerberosPrincipal p = (KerberosPrincipal) kerberosPrincipals.iterator().next();
                        username = p.getName();
                    }
                }
            }
            if ( username == null )
            {
                throw new RuntimeException( "Username cannot be null if authentication succeeded" );
            }

            String authenticationMethod = (String) messageContext.getProperty( "mc_authentication_method" );

            SAMLSubject samlSubject = new SAMLSubject();
            samlSubject.setNameIdentifier( new SAMLNameIdentifier( username, null, null ) );

            SAMLAuthenticationStatement authStmt = new SAMLAuthenticationStatement();
            authStmt.setSubject( samlSubject );
            Calendar instant = Calendar.getInstance();
            authStmt.setAuthInstant( instant.getTime() );
            authStmt.setAuthMethod( authenticationMethod );

            SAMLAssertion samlAssertion = new SAMLAssertion();
            samlAssertion.addStatement( authStmt );
            samlAssertion.setIssuer( faultActor );

            samlAssertion.setIssueInstant( instant.getTime() );
            samlAssertion.setNotBefore( instant.getTime() );
            instant.add( 13, this.validityInterval );
            samlAssertion.setNotOnOrAfter( instant.getTime() );

            SOAPHeaderElement wsaToElement =
                Utils.getHeaderByNameAndActor( soapEnvelope, Constants.WS_ADDRESSING_TO_QNAME, null, true );
            if ( wsaToElement == null )
            {
                throw new RuntimeException( "To Address not found" );
            }
            String toAddress = wsaToElement.getValue();
            SAMLAudienceRestrictionCondition audienceCondition = new SAMLAudienceRestrictionCondition();
            audienceCondition.addAudience( toAddress );
            samlAssertion.addCondition( audienceCondition );

            securityElement.appendChild( soapPart.importNode( samlAssertion.toDOM(), true ) );
        }
        catch ( Exception e )
        {
            createFaultInContextAndThrow( soapContext, Constants.SOAP_CLIENT_FAULT_CODE,
                "Error creating SAML Assertion", faultActor, e );
        }
        return true;
    }

    public QName[] getHeaders()
    {
        return null;
    }
}