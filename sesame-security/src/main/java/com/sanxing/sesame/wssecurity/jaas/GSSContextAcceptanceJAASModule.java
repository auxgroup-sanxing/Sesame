package com.sanxing.sesame.wssecurity.jaas;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

public class GSSContextAcceptanceJAASModule
    implements LoginModule
{
    public static final String JAAS_APP_OPTION_NAME = "jaasAppName";

    public static final String SERVICE_GSS_NAME_OPTION_NAME = "serviceGSSName";

    private String serviceGSSNameStr;

    private Subject me;

    public static final String GSS_TOKEN_MANDATORY_OPTION = "gssTokenMandatory";

    private boolean gssTokenMandatory;

    private GSSContext gssContext;

    protected Subject subject;

    protected CallbackHandler callbackHandler;

    private static final Logger logger = LoggerFactory.getLogger( GSSContextAcceptanceJAASModule.class );

    public void initialize( Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options )
    {
        logger.debug( "intializing" );

        this.subject = subject;
        this.callbackHandler = callbackHandler;

        String jaasAppName = (String) options.get( "jaasAppName" );

        this.gssTokenMandatory = Boolean.parseBoolean( (String) options.get( "gssTokenMandatory" ) );
        try
        {
            logger.debug( "Getting TGT from KDC via JAAS" );

            LoginContext loginContext = new LoginContext( jaasAppName );
            loginContext.login();
            this.me = loginContext.getSubject();
            logger.info( "Obtained TGT from Kerberos" );
        }
        catch ( LoginException e )
        {
            logger.error( "Auth failure when getting TGT from KDC", e );
            throw new RuntimeException( "Auth failure when getting TGT from KDC", e );
        }
        catch ( Exception e )
        {
            logger.warn( "Error when getting TGT from KDC", e );
            throw new RuntimeException( e );
        }

        this.serviceGSSNameStr = ( (String) options.get( "serviceGSSName" ) );
    }

    public boolean login()
        throws LoginException
    {
        logger.debug( "Rcvd. a new login request" );
        if ( this.callbackHandler == null )
        {
            throw new LoginException( "Error: No CallbackHandler to get gss token" );
        }

        this.gssContext = ( (GSSContext) Subject.doAs( this.me, new GSSContextCreator( this.serviceGSSNameStr ) ) );

        Callback[] callbacks = { new GSSTokenCallback() };
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

        byte[] token = ( (GSSTokenCallback) callbacks[0] ).getToken();
        if ( token == null )
        {
            if ( this.gssTokenMandatory )
            {
                logger.warn( "GSS Token missing" );
                throw new LoginException( "GSS Token missing" );
            }
            logger.debug( "no gss token in request" );
            return false;
        }

        try
        {
            this.gssContext.acceptSecContext( token, 0, token.length );
            logger.debug( "GSS auth successful" );
        }
        catch ( GSSException e )
        {
            logger.warn( "GSSContext could not be accepted" );
            throw new LoginException( "Failed authentication" );
        }

        return true;
    }

    public boolean commit()
        throws LoginException
    {
        try
        {
            GSSName clientGSSName = this.gssContext.getSrcName();
            String username = clientGSSName.toString();
            logger.debug( username + " login commited" );
            this.subject.getPrincipals().add( new UsernamePrincipal( username ) );
        }
        catch ( GSSException e )
        {
            logger.warn( e.getMessage(), e );
            throw new LoginException( "Failed to commit client info" );
        }

        return true;
    }

    public boolean abort()
        throws LoginException
    {
        try
        {
            this.gssContext.dispose();
        }
        catch ( GSSException e )
        {
            logger.warn( e.getMessage(), e );
        }
        logger.debug( "aborted login process" );
        return true;
    }

    public boolean logout()
        throws LoginException
    {
        try
        {
            this.gssContext.dispose();
        }
        catch ( GSSException e )
        {
            logger.warn( e.getMessage(), e );
        }
        logger.debug( "logged out" );
        return true;
    }

    private class GSSContextCreator
        implements PrivilegedAction
    {
        private String serviceGSSNameStr;

        public GSSContextCreator( String serviceGSSNameStr )
        {
            this.serviceGSSNameStr = serviceGSSNameStr;
        }

        public Object run()
        {
            try
            {
                Oid kerberos5Oid = new Oid( "1.2.840.113554.1.2.2" );

                GSSManager gssManager = GSSManager.getInstance();
                GSSName serviceGSSName = gssManager.createName( this.serviceGSSNameStr, GSSName.NT_HOSTBASED_SERVICE );
                GSSCredential serviceCredentials = gssManager.createCredential( serviceGSSName, 0, kerberos5Oid, 2 );
                return gssManager.createContext( serviceCredentials );
            }
            catch ( GSSException e )
            {
                GSSContextAcceptanceJAASModule.logger.error( e.getMessage(), e );
                throw new RuntimeException( e );
            }
        }
    }
}