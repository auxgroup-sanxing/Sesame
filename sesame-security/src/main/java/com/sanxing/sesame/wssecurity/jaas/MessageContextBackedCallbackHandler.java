package com.sanxing.sesame.wssecurity.jaas;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.rpc.handler.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageContextBackedCallbackHandler
    implements CallbackHandler
{
    private MessageContext messageContext;

    private final Logger logger = LoggerFactory.getLogger( MessageContextBackedCallbackHandler.class );

    public MessageContextBackedCallbackHandler( MessageContext messageContext )
    {
        this.messageContext = messageContext;
    }

    public void handle( Callback[] callbacks )
        throws IOException, UnsupportedCallbackException
    {
        for ( int i = 0; i < callbacks.length; i++ )
        {
            Callback ithCallback = callbacks[i];

            if ( ( ithCallback instanceof NameCallback ) )
            {
                this.logger.debug( "filling in username" );
                NameCallback nameCallback = (NameCallback) ithCallback;

                String username = (String) this.messageContext.getProperty( "mc_username" );
                nameCallback.setName( username );
            }
            else if ( ( ithCallback instanceof PasswordCallback ) )
            {
                this.logger.debug( "filling in password" );
                PasswordCallback passwordCallback = (PasswordCallback) ithCallback;

                String password = (String) this.messageContext.getProperty( "mc_password" );
                if ( password != null )
                    passwordCallback.setPassword( password.toCharArray() );
            }
            else if ( ( ithCallback instanceof PasswordTypeCallback ) )
            {
                this.logger.debug( "filling in password type" );
                PasswordTypeCallback passwordTypeCallback = (PasswordTypeCallback) ithCallback;
                String passwordType = (String) this.messageContext.getProperty( "mc_password_type" );
                passwordTypeCallback.setPasswordType( passwordType );
            }
            else if ( ( ithCallback instanceof NonceCallback ) )
            {
                this.logger.debug( "filling in nonce" );
                NonceCallback nonceCallback = (NonceCallback) ithCallback;
                byte[] nonce = (byte[]) this.messageContext.getProperty( "mc_nonce" );
                nonceCallback.setNonce( nonce );
            }
            else if ( ( ithCallback instanceof TimestampCallback ) )
            {
                this.logger.debug( "filling in creation timestamp" );
                TimestampCallback timestampCallback = (TimestampCallback) ithCallback;
                String timestampAsString = (String) this.messageContext.getProperty( "mc_creation_time_as_string" );

                timestampCallback.setTimestampAsString( timestampAsString );
            }
            else if ( ( ithCallback instanceof GSSTokenCallback ) )
            {
                this.logger.debug( "filling in GSS token" );
                GSSTokenCallback gssTokenCallback = (GSSTokenCallback) ithCallback;
                byte[] token = (byte[]) this.messageContext.getProperty( "mc_gss_token" );
                gssTokenCallback.setToken( token );
            }
            else
            {
                String errorMsg = "Cannot handle callbacks of type: " + ithCallback.getClass().getName();
                this.logger.error( errorMsg );
                throw new UnsupportedCallbackException( ithCallback, errorMsg );
            }
        }
    }
}