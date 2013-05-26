package com.sanxing.sesame.wssecurity.handlers;

import com.sanxing.sesame.wssecurity.commons.Constants;
import com.sanxing.sesame.wssecurity.commons.ExtendedGenericHandler;
import com.sanxing.sesame.wssecurity.jaas.MessageContextBackedCallbackHandler;
import java.util.Map;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;

public class JAASAuthenticationHandler
    extends ExtendedGenericHandler
{
    private String jaasAppName;

    public static final String JAAS_APP_CONFIG_PARAM = "jaasAppName";

    public void init( HandlerInfo handlerInfo )
    {
        this.logger.debug( "Initializing " + getClass().getName() );
        Map config = handlerInfo.getHandlerConfig();
        this.jaasAppName = ( (String) config.get( "jaasAppName" ) );
        if ( this.jaasAppName == null )
        {
            throw new JAXRPCException( "Missing mandatory config param: jaasAppName" );
        }

        this.logger.debug( "Will use " + this.jaasAppName + " as the app name when authenticatin with JAAS" );
    }

    public boolean handleRequest( MessageContext messageContext )
    {
        String username = null;
        try
        {
            this.logger.debug( "invoke()d JAASAuthenticationHandler" );

            username = (String) messageContext.getProperty( "mc_username" );

            this.logger.debug( "Authenticating " + username );
            LoginContext loginContext =
                new LoginContext( this.jaasAppName, new MessageContextBackedCallbackHandler( messageContext ) );
            loginContext.login();

            messageContext.setProperty( "mc_authenticatedSubject", loginContext.getSubject() );

            this.logger.info( "Authenticated " + username + " for " + this.jaasAppName );
        }
        catch ( LoginException e )
        {
            createFaultInContextAndThrow( (SOAPMessageContext) messageContext,
                Constants.WS_SECURITY_FAILED_AUTH_FAULT_CODE, "Failed login attempt by '" + username
                    + "' when accessing " + this.jaasAppName, e );
        }
        catch ( Exception e )
        {
            this.logger.warn( "Exception encountered while authenticating " + username + " for ", e );
            throw new RuntimeException( e );
        }
        return true;
    }

    public QName[] getHeaders()
    {
        return null;
    }
}