package com.sanxing.sesame.resolver;

import com.sanxing.sesame.jaxp.SourceTransformer;
import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class URIResolver extends EndpointResolverSupport {
	private String uri;

	public URIResolver() {
	}

	public URIResolver(String uri) {
		this.uri = uri;
	}

	protected JBIException createServiceUnavailableException() {
		return new JBIException("Unable to resolve uri: " + this.uri);
	}

	public ServiceEndpoint[] resolveAvailableEndpoints(
			ComponentContext context, MessageExchange exchange)
			throws JBIException {
		if (this.uri.startsWith("interface:")) {
			String u = this.uri.substring(10);
			String[] parts = split2(u);
			return context.getEndpoints(new QName(parts[0], parts[1]));
		}
		if (this.uri.startsWith("operation:")) {
			String u = this.uri.substring(10);
			String[] parts = split3(u);
			return context.getEndpoints(new QName(parts[0], parts[1]));
		}
		if (this.uri.startsWith("service:")) {
			String u = this.uri.substring(8);
			String[] parts = split2(u);
			return context
					.getEndpointsForService(new QName(parts[0], parts[1]));
		}
		if (this.uri.startsWith("endpoint:")) {
			String u = this.uri.substring(9);
			String[] parts = split3(u);
			ServiceEndpoint se = context.getEndpoint(new QName(parts[0],
					parts[1]), parts[2]);
			if (se != null)
				return new ServiceEndpoint[] { se };
		} else {
			DocumentFragment epr = createWSAEPR(this.uri);
			ServiceEndpoint se = context.resolveEndpointReference(epr);
			if (se != null) {
				return new ServiceEndpoint[] { se };
			}
		}
		return null;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public static DocumentFragment createWSAEPR(String uri) {
		Document doc;
		try {
			doc = new SourceTransformer().createDocument();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		DocumentFragment epr = doc.createDocumentFragment();
		Element root = doc.createElement("epr");
		Element address = doc.createElementNS(
				"http://www.w3.org/2005/08/addressing", "wsa:Address");

		Text txt = doc.createTextNode(uri);
		address.appendChild(txt);
		root.appendChild(address);
		epr.appendChild(root);
		return epr;
	}

	public static void configureExchange(MessageExchange exchange,
			ComponentContext context, String uri) {
		if (exchange == null) {
			throw new NullPointerException("exchange is null");
		}
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		if (uri == null) {
			throw new NullPointerException("uri is null");
		}
		if (uri.startsWith("interface:")) {
			String uri2 = uri.substring(10);
			String[] parts = split2(uri2);
			exchange.setInterfaceName(new QName(parts[0], parts[1]));
		} else if (uri.startsWith("operation:")) {
			String uri2 = uri.substring(10);
			String[] parts = split3(uri2);
			exchange.setInterfaceName(new QName(parts[0], parts[1]));
			exchange.setOperation(new QName(parts[0], parts[2]));
		} else if (uri.startsWith("service:")) {
			String uri2 = uri.substring(8);
			String[] parts = split2(uri2);
			exchange.setService(new QName(parts[0], parts[1]));
		} else if (uri.startsWith("endpoint:")) {
			String uri2 = uri.substring(9);
			String[] parts = split3(uri2);
			ServiceEndpoint se = context.getEndpoint(new QName(parts[0],
					parts[1]), parts[2]);
			exchange.setEndpoint(se);
		} else {
			DocumentFragment epr = createWSAEPR(uri);
			ServiceEndpoint se = context.resolveEndpointReference(epr);
			exchange.setEndpoint(se);
		}
	}

	public static String[] split3(String uri) {
		uri = uri.trim();
		char sep;
		if (uri.indexOf(47) > 0)
			sep = '/';
		else {
			sep = ':';
		}
		int idx1 = uri.lastIndexOf(sep);
		int idx2 = uri.lastIndexOf(sep, idx1 - 1);
		if ((idx1 < 0) || (idx2 < 0)) {
			throw new IllegalArgumentException(
					"Bad syntax: expected [part0][sep][part1][sep][part2]");
		}
		String epName = uri.substring(idx1 + 1);
		String svcName = uri.substring(idx2 + 1, idx1);
		String nsUri = uri.substring(0, idx2);
		return new String[] { nsUri, svcName, epName };
	}

	public static String[] split2(String uri) {
		uri = uri.trim();
		char sep;
		if (uri.indexOf(47) > 0)
			sep = '/';
		else {
			sep = ':';
		}
		int idx1 = uri.lastIndexOf(sep);
		if (idx1 < 0) {
			throw new IllegalArgumentException(
					"Bad syntax: expected [part0][sep][part1]");
		}
		String svcName = uri.substring(idx1 + 1);
		String nsUri = uri.substring(0, idx1);
		return new String[] { nsUri, svcName };
	}
}