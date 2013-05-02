package com.sanxing.ads.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class WebServletRequest implements HttpServletRequest {
	private HttpServletRequest request = null;

	public WebServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public Object getAttribute(String arg0) {
		return this.request.getAttribute(arg0);
	}

	public Enumeration getAttributeNames() {
		return this.request.getAttributeNames();
	}

	public String getAuthType() {
		return this.request.getAuthType();
	}

	public String getCharacterEncoding() {
		return this.request.getCharacterEncoding();
	}

	public int getContentLength() {
		return this.request.getContentLength();
	}

	public String getContentType() {
		return this.request.getContentType();
	}

	public String getContextPath() {
		return this.request.getContextPath();
	}

	public Cookie[] getCookies() {
		return this.request.getCookies();
	}

	public long getDateHeader(String arg0) {
		return this.request.getDateHeader(arg0);
	}

	public String getHeader(String arg0) {
		return this.request.getHeader(arg0);
	}

	public Enumeration getHeaderNames() {
		return this.request.getHeaderNames();
	}

	public Enumeration getHeaders(String arg0) {
		return this.request.getHeaders(arg0);
	}

	public ServletInputStream getInputStream() throws IOException {
		return this.request.getInputStream();
	}

	public int getIntHeader(String arg0) {
		return this.request.getIntHeader(arg0);
	}

	public String getLocalAddr() {
		return this.request.getLocalAddr();
	}

	public Locale getLocale() {
		return this.request.getLocale();
	}

	public Enumeration getLocales() {
		return this.request.getLocales();
	}

	public String getLocalName() {
		return this.request.getLocalName();
	}

	public int getLocalPort() {
		return this.request.getLocalPort();
	}

	public String getMethod() {
		return this.request.getMethod();
	}

	public String getParameter(String name) {
		String value = this.request.getParameter(name);
		String encoding = this.request.getCharacterEncoding();
		try {
			if ((value != null) && (this.request.getMethod().equals("GET")))
				value = new String(value.getBytes("ISO8859-1"),
						(encoding != null) ? encoding : "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return value;
	}

	public Map getParameterMap() {
		return this.request.getParameterMap();
	}

	public Enumeration getParameterNames() {
		return this.request.getParameterNames();
	}

	public String[] getParameterValues(String name) {
		String[] values = this.request.getParameterValues(name);
		String encoding = this.request.getCharacterEncoding();
		for (int i = 0; i < values.length; ++i) {
			String value = values[i];
			try {
				if ((value != null) && (this.request.getMethod().equals("GET")))
					values[i] = new String(value.getBytes("ISO8859-1"),
							(encoding != null) ? encoding : "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return values;
	}

	public String getPathInfo() {
		return this.request.getPathInfo();
	}

	public String getPathTranslated() {
		return this.request.getPathTranslated();
	}

	public String getProtocol() {
		return this.request.getProtocol();
	}

	public String getQueryString() {
		return this.request.getQueryString();
	}

	public BufferedReader getReader() throws IOException {
		return this.request.getReader();
	}

	/** @deprecated */
	public String getRealPath(String arg0) {
		return this.request.getRealPath(arg0);
	}

	public String getRemoteAddr() {
		return this.request.getRemoteAddr();
	}

	public String getRemoteHost() {
		return this.request.getRemoteHost();
	}

	public int getRemotePort() {
		return this.request.getRemotePort();
	}

	public String getRemoteUser() {
		return this.request.getRemoteUser();
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		return this.request.getRequestDispatcher(arg0);
	}

	public String getRequestedSessionId() {
		return this.request.getRequestedSessionId();
	}

	public String getRequestURI() {
		return this.request.getRequestURI();
	}

	public StringBuffer getRequestURL() {
		return this.request.getRequestURL();
	}

	public String getScheme() {
		return this.request.getScheme();
	}

	public String getServerName() {
		return this.request.getServerName();
	}

	public int getServerPort() {
		return this.request.getServerPort();
	}

	public String getServletPath() {
		return this.request.getServletPath();
	}

	public HttpSession getSession() {
		return this.request.getSession();
	}

	public HttpSession getSession(boolean arg0) {
		return this.request.getSession(arg0);
	}

	public Principal getUserPrincipal() {
		return this.request.getUserPrincipal();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return this.request.isRequestedSessionIdFromCookie();
	}

	/** @deprecated */
	public boolean isRequestedSessionIdFromUrl() {
		return this.request.isRequestedSessionIdFromUrl();
	}

	public boolean isRequestedSessionIdFromURL() {
		return this.request.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdValid() {
		return this.request.isRequestedSessionIdValid();
	}

	public boolean isSecure() {
		return this.request.isSecure();
	}

	public boolean isUserInRole(String arg0) {
		return this.request.isUserInRole(arg0);
	}

	public void removeAttribute(String arg0) {
		this.request.removeAttribute(arg0);
	}

	public void setAttribute(String arg0, Object arg1) {
		this.request.setAttribute(arg0, arg1);
	}

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		this.request.setCharacterEncoding(arg0);
	}
}