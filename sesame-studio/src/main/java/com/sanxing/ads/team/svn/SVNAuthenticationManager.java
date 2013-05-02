package com.sanxing.ads.team.svn;

import com.sanxing.ads.Authentication;
import com.sanxing.ads.auth.StudioPrincipal;
import java.io.File;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSHAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSLAuthentication;
import org.tmatesoft.svn.core.auth.SVNUserNameAuthentication;

public class SVNAuthenticationManager extends BasicAuthenticationManager {
	private static final Logger LOG = Logger
			.getLogger(SVNAuthenticationManager.class);

	private Map<String, SVNAuthentication> storage = Collections.synchronizedMap(new Hashtable());

	public SVNAuthenticationManager(String userName, String password) {
		super(userName, password);
	}

	public SVNAuthenticationManager(String userName, File keyFile,
			String passphrase, int portNumber) {
		super(userName, keyFile, passphrase, portNumber);
	}

	public SVNAuthenticationManager(SVNAuthentication[] authentications) {
		super(authentications);
	}

	public SVNAuthentication getFirstAuthentication(String kind, String realm,
			SVNURL url) throws SVNException {
		LOG.debug(kind + realm);
		Subject subject = Authentication.getSubject();
		Set<StudioPrincipal> principals;
		if ((subject != null)
				&& ((principals = subject.getPrincipals(StudioPrincipal.class))
						.size() > 0)) {
			StudioPrincipal principal = principals.iterator().next();
			LOG.debug("Principal: " + principal);

			String username = principal.getName();
			String passwd = principal.getPasswd();

			SVNAuthentication auth = (SVNAuthentication) this.storage.get(kind + "-" + username);
			if (auth != null) {
				String pass;
				if (auth instanceof SVNPasswordAuthentication) {
					pass = ((SVNPasswordAuthentication) auth).getPassword();
				} else {
					if (auth instanceof SVNSSHAuthentication) {
						pass = ((SVNSSHAuthentication) auth).getPassword();
					} else {
						if (auth instanceof SVNSSLAuthentication) {
							pass = ((SVNSSLAuthentication) auth).getPassword();
						} else {
							pass = passwd;
						}
					}
				}
				if (passwd.equals(pass)) {
					return auth;
				}
			}

			if ("svn.ssh".equals(kind)) {
				auth = new SVNSSHAuthentication(username, passwd, -1, false,
						null, false);
			} else if ("svn.simple".equals(kind)) {
				auth = new SVNPasswordAuthentication(username, passwd, false,
						null, false);
			} else if ("svn.ssl.client-passphrase".equals(kind)) {
				auth = new SVNSSLAuthentication((File) null, passwd, false,
						null, false);
			} else if ("svn.username".equals(kind)) {
				auth = new SVNUserNameAuthentication(username, false, null,
						false);
			}
			this.storage.put(kind + "-" + username, auth);
			return auth;
		}
		
		return null;
	}
}