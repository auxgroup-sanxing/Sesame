package com.sanxing.statenet.auth;

import java.io.File;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.pwd.PasswordTool;
import com.sanxing.sesame.util.HexBinary;
import com.sanxing.statenet.Configuration;
import com.sanxing.statenet.transport.SocketClient;

public class StatenetLoginModule
    implements LoginModule
{
    private static final Logger LOG = LoggerFactory.getLogger( StatenetLoginModule.class );

    private Subject subject;

    private CallbackHandler callbackHandler;

    private Map<String, ?> sharedState;

    private Map<String, ?> options;

    private boolean debug;

    @Override
    public boolean abort()
        throws LoginException
    {
        Set<StatenetPrincipal> set = subject.getPrincipals( StatenetPrincipal.class );
        for ( StatenetPrincipal principal : set )
        {
            subject.getPrincipals().remove( principal );
        }

        return true;
    }

    @Override
    public boolean commit()
        throws LoginException
    {
        if ( debug )
        {
            LOG.debug( "Principals: " + subject.getPrincipals() );
            LOG.debug( "Credentials: " + subject.getPublicCredentials() );
        }
        return true;
    }

    @Override
    public void initialize( Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                            Map<String, ?> options )
    {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

        debug = "true".equalsIgnoreCase( (String) options.get( "debug" ) );
    }

    @Override
    public boolean login()
        throws LoginException
    {
        if ( callbackHandler == null )
        {
            throw new LoginException( "No CallbackHandler" );
        }

        DigestCallback callback = new DigestCallback();
        Callback[] callbacks = { callback };
        try
        {
            callbackHandler.handle( callbacks );
            String username = callback.getUser();
            Map params = callback.getParams();

            if ( debug )
            {
                LOG.debug( "LoginModule Options: " + options );
            }

            StatenetPrincipal principal = userValidate( username, params );
            if ( principal != null )
            {
                subject.getPrincipals().add( principal );
                return true;
            }
        }
        catch ( LoginException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new LoginException( e.getClass() + ": " + e.getMessage() );
        }
        return false;
    }

    @Override
    public boolean logout()
        throws LoginException
    {
        Set<StatenetPrincipal> set = subject.getPrincipals( StatenetPrincipal.class );
        for ( StatenetPrincipal principal : set )
        {
            subject.getPrincipals().remove( principal );
        }
        if ( debug )
        {
            LOG.debug( "Logout studio" );
        }
        return true;
    }

    private StatenetPrincipal userValidate( String username, Map<String, ?> params )
        throws Exception
    {
        Properties properties = Configuration.getProperties();
        String host = properties.getProperty( "amr-service-host", "localhost" );
        String port = properties.getProperty( "amr-service-port", "8901" );
        String encoding = properties.getProperty( "amr-service-encoding", "GBK" );
        JSONObject request = new JSONObject();
        request.put( "operation", "findOprByCode" );
        request.put( "oprCode", username );
        String result = SocketClient.send( host, Integer.parseInt( port ), encoding, request.toString() );
        if ( result != null )
        {
            JSONObject response = new JSONObject( result );
            if ( !response.getBoolean( "success" ) )
                throw new LoginException( "用户名或者密码输入错误" );
            
            if ( debug )
            {
                LOG.debug( "Params: " + params );
            }
            String reverted = response.getString( "password" );

            MessageDigest md = MessageDigest.getInstance( "MD5" );
            String a1 = params.get( "username" ) + ":" + params.get( "realm" ) + ":" + reverted;
            String ha1 = HexBinary.encode( md.digest( a1.getBytes() ) ).toLowerCase();
            String a2 = params.get( "method" ) + ":" + params.get( "uri" );
            String ha2 = HexBinary.encode( md.digest( a2.getBytes() ) ).toLowerCase();
            byte[] digest =
                ( params.containsKey( "qop" ) ) ? md.digest( ( ha1 + ":" + params.get( "nonce" ) + ":"
                    + params.get( "nc" ) + ":" + params.get( "cnonce" ) + ":" + params.get( "qop" ) + ":" + ha2 ).getBytes() )
                    : md.digest( ( ha1 + ":" + params.get( "nonce" ) + ":" + ha2 ).getBytes() );

            String md5 = HexBinary.encode( digest ).toLowerCase();
            if ( md5.equals( params.get( "response" ) ) )
            {
                StatenetPrincipal principal = new StatenetPrincipal( username );
                principal.setFullname( response.getString( "name" ) );
                principal.setLevel( response.getString( "roleList" ) );
                principal.setDescription( response.getString( "name" ) );
                principal.setPasswd( reverted );
                return principal;
            }

            throw new LoginException( "用户名或者密码输入错误" );
        }

        throw new LoginException( "用户名或者密码输入错误" );
    }
}