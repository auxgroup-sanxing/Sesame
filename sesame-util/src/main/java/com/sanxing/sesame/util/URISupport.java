package com.sanxing.sesame.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class URISupport {
	public static Map parseQuery(String uri) throws URISyntaxException {
		try {
			Map rc = new HashMap();
			if (uri != null) {
				String[] parameters = uri.split("&");
				for (int i = 0; i < parameters.length; ++i) {
					int p = parameters[i].indexOf("=");
					if (p >= 0) {
						String name = URLDecoder.decode(
								parameters[i].substring(0, p), "UTF-8");
						String value = URLDecoder.decode(
								parameters[i].substring(p + 1), "UTF-8");
						rc.put(name, value);
					} else {
						rc.put(parameters[i], null);
					}
				}
			}
			return rc;
		} catch (UnsupportedEncodingException e) {
			throw ((URISyntaxException) new URISyntaxException(e.toString(),
					"Invalid encoding").initCause(e));
		}
	}

	public static Map parseParamters(URI uri) throws URISyntaxException {
		return ((uri.getQuery() == null) ? Collections.EMPTY_MAP
				: parseQuery(stripPrefix(uri.getQuery(), "?")));
	}

	public static URI removeQuery(URI uri) throws URISyntaxException {
		return createURIWithQuery(uri, null);
	}

	public static URI createURIWithQuery(URI uri, String query)
			throws URISyntaxException {
		return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
				uri.getPort(), uri.getPath(), query, uri.getFragment());
	}

	public static CompositeData parseComposite(URI uri)
			throws URISyntaxException {
		CompositeData rc = new CompositeData();
		rc.scheme = uri.getScheme();
		String ssp = stripPrefix(uri.getSchemeSpecificPart().trim(), "//")
				.trim();

		parseComposite(uri, rc, ssp);

		rc.fragment = uri.getFragment();
		return rc;
	}

	private static void parseComposite(URI uri, CompositeData rc, String ssp)
			throws URISyntaxException {
		if (!(checkParenthesis(ssp))) {
			throw new URISyntaxException(uri.toString(),
					"Not a matching number of '(' and ')' parenthesis");
		}

		int intialParen = ssp.indexOf("(");
		String params;
		String componentString;
		if (intialParen == 0) {
			rc.host = ssp.substring(0, intialParen);
			int p = rc.host.indexOf("/");
			if (p >= 0) {
				rc.path = rc.host.substring(p);
				rc.host = rc.host.substring(0, p);
			}
			p = ssp.lastIndexOf(")");
			componentString = ssp.substring(intialParen + 1, p);
			params = ssp.substring(p + 1).trim();
		} else {
			componentString = ssp;
			params = "";
		}

		String[] components = splitComponents(componentString);
		rc.components = new URI[components.length];
		for (int i = 0; i < components.length; ++i) {
			rc.components[i] = new URI(components[i].trim());
		}

		int p = params.indexOf("?");
		if (p >= 0) {
			if (p > 0) {
				rc.path = stripPrefix(params.substring(0, p), "/");
			}
			rc.parameters = parseQuery(params.substring(p + 1));
		} else {
			if (params.length() > 0) {
				rc.path = stripPrefix(params, "/");
			}
			rc.parameters = Collections.EMPTY_MAP;
		}
	}

	private static String[] splitComponents(String str) {
		List l = new ArrayList();

		int last = 0;
		int depth = 0;
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; ++i) {
			switch (chars[i]) {
			case '(':
				++depth;
				break;
			case ')':
				--depth;
				break;
			case ',':
				if (depth == 0) {
					String s = str.substring(last, i);
					l.add(s);
					last = i + 1;
				}
			case '*':
			case '+':
			}

		}

		String s = str.substring(last);
		if (s.length() != 0) {
			l.add(s);
		}

		String[] rc = new String[l.size()];
		l.toArray(rc);
		return rc;
	}

	public static String stripPrefix(String value, String prefix) {
		if (value.startsWith(prefix)) {
			return value.substring(prefix.length());
		}
		return value;
	}

	public static URI stripScheme(URI uri) throws URISyntaxException {
		return new URI(stripPrefix(uri.getSchemeSpecificPart().trim(), "//"));
	}

	public static String createQueryString(Map options)
			throws URISyntaxException {
		try {
			if (options.size() > 0) {
				StringBuffer rc = new StringBuffer();
				boolean first = true;
				Iterator iter = options.keySet().iterator();
				while (true) {
					if (first)
						first = false;
					else {
						rc.append("&");
					}
					String key = (String) iter.next();
					String value = (String) options.get(key);
					rc.append(URLEncoder.encode(key, "UTF-8"));
					rc.append("=");
					rc.append(URLEncoder.encode(value, "UTF-8"));

					if (!(iter.hasNext())) {
						return rc.toString();
					}
				}
			}
			return "";
		} catch (UnsupportedEncodingException e) {
			throw ((URISyntaxException) new URISyntaxException(e.toString(),
					"Invalid encoding").initCause(e));
		}
	}

	public static URI createRemainingURI(URI originalURI, Map params)
			throws URISyntaxException {
		String s = createQueryString(params);
		if (s.length() == 0) {
			s = null;
		}
		return createURIWithQuery(originalURI, s);
	}

	public static URI changeScheme(URI bindAddr, String scheme)
			throws URISyntaxException {
		return new URI(scheme, bindAddr.getUserInfo(), bindAddr.getHost(),
				bindAddr.getPort(), bindAddr.getPath(), bindAddr.getQuery(),
				bindAddr.getFragment());
	}

	public static boolean checkParenthesis(String str) {
		boolean result = true;
		if (str != null) {
			int open = 0;
			int closed = 0;

			int i = str.indexOf(40);
			while (i >= 0) {
				++open;
				i = str.indexOf(40, i + 1);
			}
			i = str.indexOf(41);
			while (i >= 0) {
				++closed;
				i = str.indexOf(41, i + 1);
			}
			result = open == closed;
		}
		return result;
	}

	public int indexOfParenthesisMatch(String str) {
		int result = -1;

		return result;
	}

	public static class CompositeData {
		String scheme;
		String path;
		URI[] components;
		Map parameters;
		String fragment;
		String host;

		public URI[] getComponents() {
			return this.components;
		}

		public String getFragment() {
			return this.fragment;
		}

		public Map getParameters() {
			return this.parameters;
		}

		public String getScheme() {
			return this.scheme;
		}

		public String getPath() {
			return this.path;
		}

		public String getHost() {
			return this.host;
		}

		public URI toURI() throws URISyntaxException {
			StringBuffer sb = new StringBuffer();
			if (this.scheme != null) {
				sb.append(this.scheme);
				sb.append(':');
			}

			if ((this.host != null) && (this.host.length() != 0)) {
				sb.append(this.host);
			} else {
				sb.append('(');
				for (int i = 0; i < this.components.length; ++i) {
					if (i != 0) {
						sb.append(',');
					}
					sb.append(this.components[i].toString());
				}
				sb.append(')');
			}

			if (this.path != null) {
				sb.append('/');
				sb.append(this.path);
			}
			if (!(this.parameters.isEmpty())) {
				sb.append("?");
				sb.append(URISupport.createQueryString(this.parameters));
			}
			if (this.fragment != null) {
				sb.append("#");
				sb.append(this.fragment);
			}
			return new URI(sb.toString());
		}
	}
}