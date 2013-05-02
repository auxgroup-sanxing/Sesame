package com.sanxing.sesame.jmx.security.auth;

import com.sanxing.sesame.jmx.security.login.CertificateCallback;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JAASAuthenticationService implements AuthenticationService {
	private static final Log LOG = LogFactory
			.getLog(JAASAuthenticationService.class);

	public void authenticate(Subject subject, String domain, final String user,
			final Object credentials) throws GeneralSecurityException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Authenticating '" + user);
		}
		LoginContext loginContext = new LoginContext(domain, subject,
				new CallbackHandler() {
					public void handle(Callback[] callbacks)
							throws IOException, UnsupportedCallbackException {
						for (int i = 0; i < callbacks.length; ++i) {
							if (callbacks[i] instanceof NameCallback)
								((NameCallback) callbacks[i])
										.setName(user);
							else if ((callbacks[i] instanceof PasswordCallback)
									&& (credentials instanceof String))
								((PasswordCallback) callbacks[i])
										.setPassword(((String) credentials)
												.toCharArray());
							else if ((callbacks[i] instanceof CertificateCallback)
									&& (credentials instanceof X509Certificate))
								((CertificateCallback) callbacks[i])
										.setCertificate((X509Certificate) credentials);
							else
								throw new UnsupportedCallbackException(
										callbacks[i]);
						}
					}
				});
		try {
			loginContext.login();
			if (LOG.isDebugEnabled())
				LOG.debug("Authenticating " + user + " successfully");
		} catch (GeneralSecurityException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Error authenticating " + user, e);
			}
			throw e;
		}
	}
}