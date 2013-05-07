package com.sanxing.sesame.jmx.security.login;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.jmx.security.GroupPrincipal;
import com.sanxing.sesame.jmx.security.UserPrincipal;

public class CertificatesLoginModule
    implements LoginModule
{
    private static final String USER_FILE = "com.sanxing.sesame.security.certificates.user";

    private static final String GROUP_FILE = "com.sanxing.sesame.security.certificates.group";

    private static final Logger LOG = LoggerFactory.getLogger( CertificatesLoginModule.class );

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
        usersFile = ( (String) options.get( "com.sanxing.sesame.security.certificates.user" ) );
        groupsFile = ( (String) options.get( "com.sanxing.sesame.security.certificates.group" ) );

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

        Callback[] callbacks = new Callback[1];
        callbacks[0] = new CertificateCallback();
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
        X509Certificate cert = ( (CertificateCallback) callbacks[0] ).getCertificate();
        if ( cert == null )
        {
            throw new FailedLoginException( "Unable to retrieve certificate" );
        }

        Principal principal = cert.getSubjectX500Principal();
        String certName = principal.getName();
        for ( Object element : users.entrySet() )
        {
            Map.Entry entry = (Map.Entry) element;
            if ( certName.equals( entry.getValue() ) )
            {
                user = ( (String) entry.getKey() );
                principals.add( principal );
                if ( debug )
                {
                    LOG.debug( "login " + user );
                }
                return true;
            }
        }
        throw new FailedLoginException();
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