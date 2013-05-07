package com.sanxing.studio.team.svn;

import java.io.File;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSHAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSLAuthentication;
import org.tmatesoft.svn.core.auth.SVNUserNameAuthentication;

import com.sanxing.studio.Authentication;
import com.sanxing.studio.auth.StudioPrincipal;

public class SVNAuthenticationManager
    extends BasicAuthenticationManager
{
    private static final Logger LOG = LoggerFactory.getLogger( SVNAuthenticationManager.class );

    private final Map<String, SVNAuthentication> storage = Collections.synchronizedMap( new Hashtable() );

    public SVNAuthenticationManager( String userName, String password )
    {
        super( userName, password );
    }

    public SVNAuthenticationManager( String userName, File keyFile, String passphrase, int portNumber )
    {
        super( userName, keyFile, passphrase, portNumber );
    }

    public SVNAuthenticationManager( SVNAuthentication[] authentications )
    {
        super( authentications );
    }

    @Override
    public SVNAuthentication getFirstAuthentication( String kind, String realm, SVNURL url )
        throws SVNException
    {
        LOG.debug( kind + realm );
        Subject subject = Authentication.getSubject();
        Set<StudioPrincipal> principals;
        if ( ( subject != null ) && ( ( principals = subject.getPrincipals( StudioPrincipal.class ) ).size() > 0 ) )
        {
            StudioPrincipal principal = principals.iterator().next();
            LOG.debug( "Principal: " + principal );

            String username = principal.getName();
            String passwd = principal.getPasswd();

            SVNAuthentication auth = storage.get( kind + "-" + username );
            if ( auth != null )
            {
                String pass;
                if ( auth instanceof SVNPasswordAuthentication )
                {
                    pass = ( (SVNPasswordAuthentication) auth ).getPassword();
                }
                else
                {
                    if ( auth instanceof SVNSSHAuthentication )
                    {
                        pass = ( (SVNSSHAuthentication) auth ).getPassword();
                    }
                    else
                    {
                        if ( auth instanceof SVNSSLAuthentication )
                        {
                            pass = ( (SVNSSLAuthentication) auth ).getPassword();
                        }
                        else
                        {
                            pass = passwd;
                        }
                    }
                }
                if ( passwd.equals( pass ) )
                {
                    return auth;
                }
            }

            if ( "svn.ssh".equals( kind ) )
            {
                auth = new SVNSSHAuthentication( username, passwd, -1, false, null, false );
            }
            else if ( "svn.simple".equals( kind ) )
            {
                auth = new SVNPasswordAuthentication( username, passwd, false, null, false );
            }
            else if ( "svn.ssl.client-passphrase".equals( kind ) )
            {
                auth = new SVNSSLAuthentication( (File) null, passwd, false, null, false );
            }
            else if ( "svn.username".equals( kind ) )
            {
                auth = new SVNUserNameAuthentication( username, false, null, false );
            }
            storage.put( kind + "-" + username, auth );
            return auth;
        }

        return null;
    }
}