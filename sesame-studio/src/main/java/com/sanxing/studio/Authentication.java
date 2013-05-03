package com.sanxing.studio;

import com.sanxing.studio.auth.LoginConfiguration;
import com.sanxing.studio.auth.PassiveCallbackHandler;
import com.sanxing.studio.auth.StudioPrincipal;
import com.sanxing.studio.matcher.StringFilter;
import com.sanxing.studio.matcher.StringMatcher;
import com.sanxing.studio.utils.HexBinary;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.httpclient.auth.AuthChallengeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class Authentication extends HttpServlet implements Filter {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(Authentication.class);
	private static StringMatcher stringMatcher;
	private static List<String> allowPath = new ArrayList();

	private static ThreadLocal<Subject> currSubject = new ThreadLocal();
	private FilterConfig filterConfig;

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		this.filterConfig.getServletContext();

		allowPath.add("^/$");
		allowPath.add("^/[^/]*.jsp");
		allowPath.add("^/ext-Ajax/.*");
		allowPath.add("^/package/.*");

		allowPath.add("^/LoginAction");
		allowPath.add("^/ResourceTree");
		allowPath.add("^/images/.*");
		allowPath.add("^/.*.css");
		allowPath.add("^/.*.jpg");
		allowPath.add("^/.*.gif");
		stringMatcher = new StringFilter(allowPath, false);
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		try {
			String path = req.getRequestURI().substring(
					req.getContextPath().length());

			LoginContext loginCtx = (LoginContext) req.getSession(true)
					.getAttribute("LOGIN_CONTEXT");
			if (loginCtx == null) {
				if (stringMatcher.match(path)) {
					filterChain.doFilter(request, response);
				} else {
					String nonce = req.getSession(true).getId().toLowerCase();

					String auth = req.getHeader("Authorization");
					if (auth != null) {
						Map params = AuthChallengeParser.extractParams(auth);
						params.put("method", req.getMethod());
						params.put("nonce", nonce);
						try {
							String username = (String) params.get("username");
							PassiveCallbackHandler handler = new PassiveCallbackHandler(
									username, params);
							loginCtx = new LoginContext("Studio", null,
									handler, new LoginConfiguration());
							loginCtx.login();

							currSubject.set(loginCtx.getSubject());

							req.getSession(true).setAttribute("LOGIN_CONTEXT",
									loginCtx);
							filterChain.doFilter(request, response);
						} catch (LoginException e) {
							LOG.error(e.getMessage());
							res.setHeader("WWW-Authenticate",
									"Digest realm=\"Sesame Studio\",qop=\"auth,auth-int\",nonce=\""
											+ nonce + "\"");
							res.sendError(401);
						}
					} else {
						res.setHeader("WWW-Authenticate",
								"Digest realm=\"Sesame Studio\",qop=\"auth,auth-int\",nonce=\""
										+ nonce + "\"");
						res.sendError(401);
					}
				}
			} else {
				currSubject.set(loginCtx.getSubject());
				filterChain.doFilter(request, response);
			}
		} catch (IOException e) {
			if (LOG.isDebugEnabled())
				LOG.debug(e.getMessage());
		} catch (Throwable throwable) {
			String message = throwable.getMessage();
			try {
				Throwable cause = throwable.getCause();
				if (cause instanceof ClassNotFoundException) {
					message = " 找不到类: " + cause.getMessage();
				} else if (!(cause instanceof IllegalDataException)) {
					if (!(cause instanceof IllegalNameException)) {
						LOG.error(throwable.getMessage(), cause);
					}
				}
				if (!(res.isCommitted()))
					res.sendError(500, message);
			} catch (IllegalStateException e) {
				LOG.error(e.getMessage(), e);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	public void destroy() {
		stringMatcher.clearPattens();
		allowPath.clear();
	}

	public static JSONObject getCurrentUser() {
		Subject subject = getSubject();
		Set principals;
		if ((subject != null)
				&& ((principals = subject.getPrincipals(StudioPrincipal.class))
						.size() > 0)) {
			StudioPrincipal principal = (StudioPrincipal) principals.iterator()
					.next();
			JSONObject result = new JSONObject();
			try {
				result.put("userid", principal.getName());
				result.put("fullname", principal.getFullname());
				result.put("userlevel", principal.getLevel());
				result.put("userdsr", principal.getDescription());
			} catch (JSONException e) {
			}
			return result;
		}

		return null;
	}

	public static Subject getSubject() {
		return ((Subject) currSubject.get());
	}

	static {
		LOG.debug("login.configuration.provider="
				+ LoginConfiguration.class.getName());
	}
}