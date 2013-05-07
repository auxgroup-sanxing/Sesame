package com.sanxing.sesame.jmx.security.login;

import com.sanxing.sesame.jmx.security.GroupPrincipal;
import com.sanxing.sesame.jmx.security.UserPrincipal;
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

public class PropertiesLoginModule implements LoginModule {
	private static final String USER_FILE = "com.sanxing.sesame.security.properties.user";
	private static final String GROUP_FILE = "com.sanxing.sesame.security.properties.group";
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesLoginModule.class);
	private Subject subject;
	private CallbackHandler callbackHandler;
	private boolean debug;
	private String usersFile;
	private String groupsFile;
	private Properties users = new Properties();
	private Properties groups = new Properties();
	private String user;
	private Set principals = new HashSet();
	private File baseDir;

	public void initialize(Subject sub, CallbackHandler handler,
			Map sharedState, Map options) {
		this.subject = sub;
		this.callbackHandler = handler;

		if (System.getProperty("java.security.auth.login.config") != null)
			this.baseDir = new File(
					System.getProperty("java.security.auth.login.config"))
					.getParentFile();
		else {
			this.baseDir = new File(".");
		}

		this.debug = "true".equalsIgnoreCase((String) options.get("debug"));
		this.usersFile = ((String) options
				.get("com.sanxing.sesame.security.properties.user"));
		this.groupsFile = ((String) options
				.get("com.sanxing.sesame.security.properties.group"));

		if (this.debug)
			LOG.debug("Initialized debug=" + this.debug + " usersFile="
					+ this.usersFile + " groupsFile=" + this.groupsFile
					+ " basedir=" + this.baseDir);
	}

	public boolean login() throws LoginException {
		File f = new File(this.baseDir, this.usersFile);
		try {
			this.users.load(new FileInputStream(f));
		} catch (IOException ioe) {
			throw new LoginException("Unable to load user properties file " + f);
		}
		f = new File(this.baseDir, this.groupsFile);
		try {
			this.groups.load(new FileInputStream(f));
		} catch (IOException ioe) {
			throw new LoginException("Unable to load group properties file "
					+ f);
		}

		Callback[] callbacks = new Callback[2];

		callbacks[0] = new NameCallback("Username: ");
		callbacks[1] = new PasswordCallback("Password: ", false);
		try {
			this.callbackHandler.handle(callbacks);
		} catch (IOException ioe) {
			throw new LoginException(ioe.getMessage());
		} catch (UnsupportedCallbackException uce) {
			throw new LoginException(uce.getMessage()
					+ " not available to obtain information from user");
		}
		this.user = ((NameCallback) callbacks[0]).getName();
		char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
		if (tmpPassword == null) {
			tmpPassword = new char[0];
		}

		String password = this.users.getProperty(this.user);

		if (password == null) {
			throw new FailedLoginException("User does not exist");
		}
		if (!(password.equals(new String(tmpPassword)))) {
			throw new FailedLoginException("Password does not match");
		}

		this.users.clear();

		if (this.debug) {
			LOG.debug("login " + this.user);
		}
		return true;
	}

	public boolean commit() throws LoginException {
		this.principals.add(new UserPrincipal(this.user));

		for (Enumeration enumeration = this.groups.keys(); enumeration
				.hasMoreElements();) {
			String name = (String) enumeration.nextElement();
			String[] userList = this.groups.getProperty(name).split(",");
			for (int i = 0; i < userList.length; ++i) {
				if (this.user.equals(userList[i])) {
					this.principals.add(new GroupPrincipal(name));
					break;
				}
			}
		}

		this.subject.getPrincipals().addAll(this.principals);

		clear();

		if (this.debug) {
			LOG.debug("commit");
		}
		return true;
	}

	public boolean abort() throws LoginException {
		clear();

		if (this.debug) {
			LOG.debug("abort");
		}
		return true;
	}

	public boolean logout() throws LoginException {
		this.subject.getPrincipals().removeAll(this.principals);
		this.principals.clear();

		if (this.debug) {
			LOG.debug("logout");
		}
		return true;
	}

	private void clear() {
		this.groups.clear();
		this.user = null;
	}
}