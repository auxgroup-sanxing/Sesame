package com.sanxing.ads.auth;

import com.sanxing.ads.Application;
import com.sanxing.ads.utils.HexBinary;
import com.sanxing.statenet.pwd.PasswordTool;

import java.io.File;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class StudioLoginModule implements LoginModule {
	private static final Logger LOG = Logger.getLogger(StudioLoginModule.class);
	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map<String, ?> sharedState;
	private Map<String, ?> options;
	private boolean debug;

	public boolean abort() throws LoginException {
		Set<StudioPrincipal> set = this.subject
				.getPrincipals(StudioPrincipal.class);
		for (StudioPrincipal principal : set) {
			this.subject.getPrincipals().remove(principal);
		}

		return true;
	}

	public boolean commit() throws LoginException {
		if (this.debug) {
			LOG.debug("Principals: " + this.subject.getPrincipals());
			LOG.debug("Credentials: " + this.subject.getPublicCredentials());
		}
		return true;
	}

	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;

		this.debug = "true".equalsIgnoreCase((String) options.get("debug"));
	}

	public boolean login() throws LoginException {
		if (this.callbackHandler == null) {
			throw new LoginException("No CallbackHandler");
		}

		DigestCallback callback = new DigestCallback();
		Callback[] callbacks = { callback };
		try {
			this.callbackHandler.handle(callbacks);
			String username = callback.getUser();
			Map params = callback.getParams();

			if (this.debug) {
				LOG.debug("LoginModule Options: " + this.options);
			}

			StudioPrincipal principal = userValidate(username, params);
			if (principal != null) {
				this.subject.getPrincipals().add(principal);
				return true;
			}
		} catch (LoginException e) {
			throw e;
		} catch (Exception e) {
			throw new LoginException(e.getClass() + ": " + e.getMessage());
		}
		return false;
	}

	public boolean logout() throws LoginException {
		Set<StudioPrincipal> set = this.subject
				.getPrincipals(StudioPrincipal.class);
		for (StudioPrincipal principal : set) {
			this.subject.getPrincipals().remove(principal);
		}
		if (this.debug) {
			LOG.debug("Logout studio");
		}
		return true;
	}

	private StudioPrincipal userValidate(String username, Map<String, ?> params)
			throws Exception {
		String userPath = Application.getRealPath("WEB-INF/user.xml");
		SAXReader reader = new SAXReader();
		Document document = reader.read(new File(userPath));
		Element root = document.getRootElement();
		Element userEl = (Element) root.selectSingleNode("user[@userid='"
				+ username + "']");
		if (userEl != null) {
			if (this.debug) {
				LOG.debug("Params: " + params);
			}
			String reverted = PasswordTool.decrypt(userEl.attributeValue("passwd"));

			MessageDigest md = MessageDigest.getInstance("MD5");
			String a1 = params.get("username") + ":" + params.get("realm")
					+ ":" + reverted;
			String ha1 = HexBinary.encode(md.digest(a1.getBytes()))
					.toLowerCase();
			String a2 = params.get("method") + ":" + params.get("uri");
			String ha2 = HexBinary.encode(md.digest(a2.getBytes()))
					.toLowerCase();
			byte[] digest = (params.containsKey("qop")) ? md
					.digest((ha1 + ":" + params.get("nonce") + ":"
							+ params.get("nc") + ":" + params.get("cnonce")
							+ ":" + params.get("qop") + ":" + ha2).getBytes())
					: md.digest((ha1 + ":" + params.get("nonce") + ":" + ha2)
							.getBytes());

			String md5 = HexBinary.encode(digest).toLowerCase();
			if (md5.equals(params.get("response"))) {
				StudioPrincipal principal = new StudioPrincipal(username);
				principal.setFullname(userEl.attributeValue("fullname"));
				principal.setLevel(userEl.attributeValue("userlevel"));
				principal.setDescription(userEl.attributeValue("userdsr"));
				principal.setPasswd(reverted);
				return principal;
			}

			throw new LoginException("用户名或者密码输入错误");
		}

		throw new LoginException("用户名或者密码输入错误");
	}
}