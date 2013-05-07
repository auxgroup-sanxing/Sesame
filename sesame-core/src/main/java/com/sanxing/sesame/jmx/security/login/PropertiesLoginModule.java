package com.sanxing.sesame.jmx.security.login;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.jmx.security.GroupPrincipal;
import com.sanxing.sesame.jmx.security.UserPrincipal;

public class PropertiesLoginModule
    implements LoginModule
{
    private static final String USER_FILE = "com.sanxing.sesame.security.properties.user";

    private static final String GROUP_FILE = "com.sanxing.sesame.security.properties.group";

    private static final Logger LOG = LoggerFactory.getLogger( PropertiesLoginModule.class );

    private Subject subject;

    private CallbackHandler callbackHandler;

    private boolean debug;

    private String usersFile;

    private String groupsFile;

    private final Properties users = new Properties();

    private final Properties groups = new Properties();

    private String user;

    private final Set principals = new HashSet();

    private File baseDir;

    @Override
    public void initialize( Subject sub, CallbackHandler handler, Map sharedState, Map options )
    {
        subject = sub;
        callbackHandler = handler;

        if ( System.getProperty( "java.security.auth.login.config" ) != null )
        {
            baseDir = new File( System.getProperty( "java.security.auth.login.config" ) ).getParentFile();
        }
        else
        {
            baseDir = new File( "." );
        }

        debug = "true".equalsIgnoreCase( (String) options.get( "debug" ) );
        usersFile = ( (String) options.get( "com.sanxing.sesame.security.properties.user" ) );
        groupsFile = ( (String) options.get( "com.sanxing.sesame.security.properties.group" ) );

        if ( debug )
        {
            LOG.debug( "Initialized debug=" + debug + " usersFile=" + usersFile + " groupsFile=" + groupsFile
                + " basedir=" + baseDir );
        }
    }

    @Override
    public boolean login()
        throws LoginException
    {
        File f = new File( baseDir, usersFile );
        try
        {
            users.load( new FileInputStream( f ) );
        }
        catch ( IOException ioe )
        {
            throw new LoginException( "Unable to load user properties file " + f );
        }
        f = new File( baseDir, groupsFile );
        try
        {
            groups.load( new FileInputStream( f ) );
        }
        catch ( IOException ioe )
        {
            throw new LoginException( "Unable to load group properties file " + f );
        }

        Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback( "Username: " );
        callbacks[1] = new PasswordCallback( "Password: ", false );
        try
        {
            callbackHandler.handle( callbacks );
        }
        catch ( IOException ioe )
        {
            throw new LoginException( ioe.getMessage() );
        }
        catch ( UnsupportedCallbackException uce )
        {
            throw new LoginException( uce.getMessage() + " not available to obtain information from user" );
        }
        user = ( (NameCallback) callbacks[0] ).getName();
        char[] tmpPassword = ( (PasswordCallback) callbacks[1] ).getPassword();
        if ( tmpPassword == null )
        {
            tmpPassword = new char[0];
        }

        String password = users.getProperty( user );

        if ( password == null )
        {
            throw new FailedLoginException( "User does not exist" );
        }
        if ( !( password.equals( new String( tmpPassword ) ) ) )
        {
            throw new FailedLoginException( "Password does not match" );
        }

        users.clear();

        if ( debug )
        {
            LOG.debug( "login " + user );
        }
        return true;
    }

    @Override
    public boolean commit()
        throws LoginException
    {
        principals.add( new UserPrincipal( user ) );

        for ( Enumeration enumeration = groups.keys(); enumeration.hasMoreElements(); )
        {
            String name = (String) enumeration.nextElement();
            String[] userList = groups.getProperty( name ).split( "," );
            for ( int i = 0; i < userList.length; ++i )
            {
                if ( user.equals( userList[i] ) )
                {
                    principals.add( new GroupPrincipal( name ) );
                    break;
                }
            }
        }

        subject.getPrincipals().addAll( principals );

        clear();

        if ( debug )
        {
            LOG.debug( "commit" );
        }
        return true;
    }

    @Override
    public boolean abort()
        throws LoginException
    {
        clear();

        if ( debug )
        {
            LOG.debug( "abort" );
        }
        return true;
    }

    @Override
    public boolean logout()
        throws LoginException
    {
        subject.getPrincipals().removeAll( principals );
        principals.clear();

        if ( debug )
        {
            LOG.debug( "logout" );
        }
        return true;
    }

    private void clear()
    {
        groups.clear();
        user = null;
    }
}