package com.sanxing.sesame.wssecurity.jaas;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonceCheckJAASModule
    implements LoginModule
{
    public static final String NONCE_MANDATORY_OPTION = "nonceMandatory";

    private boolean nonceMandatory;

    public static final String NONCE_CACHE_NAME_OPTION = "cacheName";

    private String cacheName;

    public static final String NONCE_EXPIRY_DURATION_OPTION = "nonceTTLInSeconds";

    private static Map nonceCaches = Collections.synchronizedMap( new HashMap() );

    private Map cache;

    protected CallbackHandler callbackHandler;

    private static final Logger logger = LoggerFactory.getLogger( NonceCheckJAASModule.class );

    public void initialize( Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options )
    {
        logger.debug( "intializing" );

        this.callbackHandler = callbackHandler;

        this.nonceMandatory = Boolean.parseBoolean( (String) options.get( "nonceMandatory" ) );
        logger.debug( "nonce is: " + ( this.nonceMandatory ? "mandatory" : "optional" ) );

        this.cacheName = ( (String) options.get( "cacheName" ) );
        if ( this.cacheName == null )
        {
            this.cacheName = "global";
        }

        synchronized ( nonceCaches )
        {
            this.cache = ( (Map) nonceCaches.get( this.cacheName ) );
            if ( this.cache == null )
            {
                String nonceTTLAsString = (String) options.get( "nonceTTLInSeconds" );
                long nonceTTL;
                if ( nonceTTLAsString != null )
                    nonceTTL = Long.parseLong( nonceTTLAsString );
                else
                {
                    nonceTTL = 300L;
                }

                this.cache = Collections.synchronizedMap( new NonceCache( nonceTTL ) );
                nonceCaches.put( this.cacheName, this.cache );
            }
        }
    }

    public boolean login()
        throws LoginException
    {
        logger.debug( "Rcvd. a new login request" );
        if ( this.callbackHandler == null )
        {
            throw new LoginException( "Error: No CallbackHandler to get nonce" );
        }

        Callback[] callbacks = { new NonceCallback() };
        try
        {
            this.callbackHandler.handle( callbacks );
        }
        catch ( IOException ioe )
        {
            logger.error( ioe.getMessage(), ioe );
            throw new LoginException( "Internal error in authentication" );
        }
        catch ( UnsupportedCallbackException uce )
        {
            logger.error( "Error: " + uce.getCallback().getClass().getName() + " not supported by authenticating app",
                uce );
            throw new LoginException( "Internal error in authentication" );
        }

        byte[] nonce = ( (NonceCallback) callbacks[0] ).getNonce();
        if ( nonce == null )
        {
            if ( this.nonceMandatory )
            {
                logger.warn( "Nonce missing" );
                throw new LoginException( "Nonce missing" );
            }
            logger.debug( "no nonce in request" );
            return false;
        }

        logger.debug( "Checking if nonce repeated" );

        String base64Nonce = null;
        try
        {
            base64Nonce = new String( Base64.encodeBase64( nonce ), "US-ASCII" );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( e );
        }

        synchronized ( this.cache )
        {
            if ( this.cache.containsKey( base64Nonce ) )
            {
                logger.warn( "Repeated nonce: Replay attack?" );
                throw new LoginException( "Nonce repeated" );
            }
            this.cache.put( base64Nonce, new Long( System.currentTimeMillis() ) );
        }
        return true;
    }

    public boolean commit()
        throws LoginException
    {
        return true;
    }

    public boolean abort()
        throws LoginException
    {
        return true;
    }

    public boolean logout()
        throws LoginException
    {
        return true;
    }
}