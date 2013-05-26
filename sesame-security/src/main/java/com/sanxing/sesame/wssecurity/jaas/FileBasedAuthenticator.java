package com.sanxing.sesame.wssecurity.jaas;

import com.sanxing.sesame.wssecurity.commons.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedAuthenticator
    implements LoginModule
{
    private static final Logger logger = LoggerFactory.getLogger( FileBasedAuthenticator.class );

    protected MessageDigest digester;

    public static final String FILE_PATH_OPTION_NAME = "filePath";

    protected File passwordFile;

    protected long lastReadTimestamp = 0L;

    protected Map cache = new HashMap();

    protected User userLoggingIn;

    protected Subject subject;

    protected CallbackHandler callbackHandler;

    public FileBasedAuthenticator()
    {
        try
        {
            this.digester = MessageDigest.getInstance( "SHA-1" );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( e );
        }
    }

    public void initialize( Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options )
    {
        this.subject = subject;
        this.callbackHandler = callbackHandler;

        String filePath = (String) options.get( "filePath" );
        if ( filePath == null )
        {
            throw new RuntimeException( "Missing a mandatory option, filePath, in FileBasedAuthenticator JAAS module" );
        }

        this.passwordFile = new File( filePath );
        try
        {
            loadPasswordFile();
        }
        catch ( IOException e )
        {
            logger.error( "Error parsing password file: " + filePath, e );
            throw new RuntimeException( e );
        }
    }

    private void loadPasswordFile()
        throws IOException
    {
        logger.debug( "reading password file" );
        this.lastReadTimestamp = System.currentTimeMillis();
        BufferedReader r = new BufferedReader( new FileReader( this.passwordFile ) );
        this.cache.clear();

        String l = r.readLine();
        while ( l != null )
        {
            int hash = l.indexOf( '#' );
            if ( hash != -1 )
                l = l.substring( 0, hash );
            l = l.trim();

            if ( l.length() != 0 )
            {
                StringTokenizer t = new StringTokenizer( l, ":" );
                User u = new User();
                u.principals = new LinkedList();
                String user = t.nextToken();

                u.hashedPassword = Utils.charsToUTF8Bytes( t.nextToken().toCharArray() );
                u.principals.add( new UsernamePrincipal( user ) );
                while ( t.hasMoreTokens() )
                {
                    u.principals.add( new MemberOfGroupPrincipal( t.nextToken() ) );
                }
                this.cache.put( user, u );
            }
            l = r.readLine();
        }
        r.close();
        logger.debug( "found " + this.cache.size() + " entries in password file" );
    }

    private void reloadPasswordFile()
        throws LoginException
    {
        if ( this.passwordFile.lastModified() > this.lastReadTimestamp )
            try
            {
                loadPasswordFile();
            }
            catch ( IOException ioe )
            {
                logger.error( ioe.getMessage(), ioe );
                throw new LoginException( "Internal error in authentication" );
            }
    }

    public boolean login()
        throws LoginException
    {
        logger.debug( "Rcvd. a new login request" );
        reloadPasswordFile();

        String username = null;
        byte[] password = (byte[]) null;
        try
        {
            if ( this.callbackHandler == null )
            {
                throw new LoginException( "Error: No CallbackHandler to get user info" );
            }

            Callback[] callbacks = new Callback[5];
            callbacks[0] = new NameCallback( "Username: " );
            callbacks[1] = new PasswordCallback( "Password: ", false );
            callbacks[2] = new PasswordTypeCallback();
            callbacks[3] = new NonceCallback();
            callbacks[4] = new TimestampCallback();
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
                logger.error( "Error: " + uce.getCallback().getClass().getName()
                    + " not supported by authenticating app", uce );
                throw new LoginException( "Internal error in authentication" );
            }

            username = ( (NameCallback) callbacks[0] ).getName();
            logger.debug( "username: " + username );
            if ( username == null )
            {
                throw new LoginException( "No username specified" );
            }
            User potentialMatch = (User) this.cache.get( username );
            if ( potentialMatch == null )
            {
                logger.warn( "no user named " + username );
                throw new LoginException( "Bad username or password" );
            }

            char[] passwordChars = (char[]) null;
            try
            {
                passwordChars = ( (PasswordCallback) callbacks[1] ).getPassword();
                ( (PasswordCallback) callbacks[1] ).clearPassword();
                if ( passwordChars != null )
                {
                    password = Utils.charsToUTF8Bytes( passwordChars );
                }
            }
            finally
            {
                if ( passwordChars != null )
                {
                    Arrays.fill( passwordChars, '\000' );
                }
            }

            boolean matched = true;
            String passwordType = ( (PasswordTypeCallback) callbacks[2] ).getPasswordType();
            if ( ( passwordType == null )
                || ( passwordType.length() == 0 )
                || ( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd#PasswordText".equals( passwordType ) ) )
            {
                if ( password != null )
                {
                    byte[] hashedPassword = Base64.encodeBase64( this.digester.digest( password ) );
                    matched = Arrays.equals( hashedPassword, potentialMatch.hashedPassword );
                }
                else
                {
                    matched = potentialMatch.hashedPassword.length == 0;
                }
            }
            else if ( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd#PasswordDigest".equals( passwordType ) )
            {
                byte[] nonce = ( (NonceCallback) callbacks[3] ).getNonce();
                if ( nonce != null )
                    this.digester.update( nonce );
                String timestamp = ( (TimestampCallback) callbacks[4] ).getTimestampAsString();
                if ( timestamp != null )
                {
                    try
                    {
                        this.digester.update( timestamp.getBytes( "UTF-8" ) );
                    }
                    catch ( UnsupportedEncodingException e )
                    {
                        logger.error( e.getMessage(), e );
                        throw new RuntimeException( e );
                    }
                }
                this.digester.update( potentialMatch.hashedPassword );
                byte[] digest = Base64.encodeBase64( this.digester.digest() );
                matched = Arrays.equals( password, digest );
            }
            else
            {
                throw new UnsupportedOperationException( "Unsupported Password type: " + passwordType );
            }

            if ( !matched )
            {
                logger.warn( "Invalid login attempt by: " + username );
                throw new LoginException( "Bad username or password" );
            }
            this.userLoggingIn = potentialMatch;
            logger.info( username + " authenticated" );
        }
        finally
        {
            if ( password != null )
                Arrays.fill( password, (byte) 0 );
        }
        return true;
    }

    public boolean commit()
        throws LoginException
    {
        this.subject.getPrincipals().addAll( this.userLoggingIn.principals );

        logger.debug( "successfully commited subject info" );
        return true;
    }

    public boolean abort()
        throws LoginException
    {
        this.userLoggingIn = null;
        logger.debug( "aborted login process" );
        return true;
    }

    public boolean logout()
        throws LoginException
    {
        this.subject.getPrincipals().removeAll( this.userLoggingIn.principals );
        this.userLoggingIn = null;
        logger.info( "logged out" );
        return true;
    }

    public static void main( String[] args )
        throws Exception
    {
        if ( args.length != 1 )
        {
            System.exit( 1 );
        }
        byte[] utf8Password = Utils.charsToUTF8Bytes( args[0].toCharArray() );
        byte[] sha1Password = MessageDigest.getInstance( "SHA-1" ).digest( utf8Password );
        byte[] base64EncodedSHA1Password = Base64.encodeBase64( sha1Password );
    }

    protected class User
    {
        byte[] hashedPassword;

        List principals;

        protected User()
        {
        }
    }
}