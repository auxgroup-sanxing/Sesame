package com.sanxing.ads.action;

import com.sanxing.ads.Authentication;
import com.sanxing.ads.auth.LoginConfiguration;
import com.sanxing.ads.auth.PassiveCallbackHandler;
import com.sanxing.ads.utils.WebServletRequest;
import com.sanxing.ads.utils.WebServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.httpclient.auth.AuthChallengeParser;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class LoginAction extends HttpServlet implements Servlet {
	private static final long serialVersionUID = 2L;
	private static Logger LOG = Logger.getLogger(LoginAction.class);

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		response.setContentType("text/json; charset=utf-8");
		PrintWriter out = response.getWriter();
		try {
			if (action == null) {
				JSONObject user = Authentication.getCurrentUser();
				out.print(user);
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			response.sendError(500, e.getMessage());
		} finally {
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request = new WebServletRequest(request);
		response = new WebServletResponse(response);
		request.setCharacterEncoding("UTF-8");

		response.setContentType("text/json; charset=UTF-8");

		String nonce = request.getSession(true).getId().toLowerCase();
		String auth = request.getHeader("Authorization");

		if (auth != null) {
			Map params = AuthChallengeParser.extractParams(auth);
			params.put("method", request.getMethod());
			params.put("nonce", nonce);
			try {
				String username = (String) params.get("username");
				PassiveCallbackHandler handler = new PassiveCallbackHandler(
						username, params);
				LoginContext loginCtx = new LoginContext("Studio", null,
						handler, new LoginConfiguration());
				loginCtx.login();

				request.getSession(true)
						.setAttribute("LOGIN_CONTEXT", loginCtx);
			} catch (LoginException e) {
				LOG.debug(e.getMessage());
				response.setHeader("nonce", nonce);
				response.sendError(500, e.getMessage());
			}
		} else {
			response.setHeader("WWW-Authenticate",
					"Digest realm=\"Statenet Studio\",qop=\"auth,auth-int\",nonce=\""
							+ nonce + "\"");
			response.sendError(401);
		}
	}
}