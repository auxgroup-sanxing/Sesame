package com.sanxing.sesame.wssecurity.commons;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExtendedGenericHandler
    extends GenericHandler
{
    protected final Logger logger;

    protected static final String SAVED_FAULT_MSG_CONTEXT_PROPERTY = "savedFault";

    public ExtendedGenericHandler()
    {
        this.logger = LoggerFactory.getLogger( getClass() );
    }

    protected void createFaultInContextAndThrow( SOAPMessageContext soapContext, QName faultCode, String faultString )
        throws SOAPFaultException
    {
        createFaultInContextAndThrow( soapContext, faultCode, faultString, null, null );
    }

    protected void createFaultInContextAndThrow( SOAPMessageContext soapContext, QName faultCode, String faultString,
                                                 String faultActor )
        throws SOAPFaultException
    {
        createFaultInContextAndThrow( soapContext, faultCode, faultString, faultActor, null );
    }

    protected void createFaultInContextAndThrow( SOAPMessageContext soapContext, QName faultCode, String faultString,
                                                 Throwable cause )
        throws SOAPFaultException
    {
        createFaultInContextAndThrow( soapContext, faultCode, faultString, null, cause );
    }

    protected void createFaultInContextAndThrow( SOAPMessageContext soapContext, QName faultCode, String faultString,
                                                 String faultActor, Throwable cause )
        throws SOAPFaultException
    {
        if ( cause != null )
        {
            this.logger.error( faultString, cause );
            faultString = faultString + ": " + cause.getMessage();
        }

        if ( faultActor == null )
        {
            String[] rolesList = soapContext.getRoles();
            if ( ( rolesList != null ) && ( rolesList.length > 0 ) )
            {
                faultActor = rolesList[0];
            }
        }
        SOAPFaultException fault = new SOAPFaultException( faultCode, faultString, faultActor, null );
        fault.initCause( cause );
        soapContext.setProperty( "savedFault", fault );
        throw fault;
    }

    public boolean handleFault( MessageContext context )
    {
        SOAPMessageContext soapContext = (SOAPMessageContext) context;
        SOAPFaultException fault = (SOAPFaultException) soapContext.getProperty( "savedFault" );

        if ( fault == null )
            return true;

        try
        {
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage responseMsg = messageFactory.createMessage();
            soapContext.setMessage( responseMsg );

            QName faultCodeQName = fault.getFaultCode();
            String faultCodeLocalName = faultCodeQName.getLocalPart();
            String faultCodeNsUri = faultCodeQName.getNamespaceURI();
            String faultCodePrefix = null;

            SOAPEnvelope envelope = responseMsg.getSOAPPart().getEnvelope();
            SOAPFault faultNode =
                envelope.getBody().addFault(
                    envelope.createName( faultCodeLocalName, faultCodePrefix, faultCodeNsUri ), fault.getFaultString() );

            faultNode.setFaultActor( fault.getFaultActor() );
        }
        catch ( SOAPException e )
        {
            this.logger.error( "Error creating fault response: ", e );
            throw new JAXRPCException( e );
        }
        context.removeProperty( "savedFault" );

        return true;
    }

    protected Set getSOAPRoles( SOAPMessageContext soapContext )
    {
        this.logger.debug( "reading roles from message context" );
        Set roleSet = new HashSet();

        String[] rolesList = soapContext.getRoles();
        if ( rolesList != null )
        {
            for ( int i = 0; i < rolesList.length; i++ )
            {
                this.logger.debug( rolesList[i] );
                roleSet.add( rolesList[i] );
            }
        }
        return roleSet;
    }
}