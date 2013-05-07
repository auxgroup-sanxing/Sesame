package com.sanxing.sesame.servicedesc;

import com.sanxing.sesame.util.W3CUtil;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

public final class EndpointReferenceBuilder {
	public static final String JBI_NAMESPACE = "http://java.sun.com/jbi/end-point-reference";
	public static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";
	private static final Logger LOG = LoggerFactory.getLogger(EndpointReferenceBuilder.class);

	public static DocumentFragment getReference(ServiceEndpoint endpoint) {
		try {
			Document doc = W3CUtil.newDocument();
			DocumentFragment fragment = doc.createDocumentFragment();
			Element epr = doc.createElementNS(
					"http://java.sun.com/jbi/end-point-reference",
					"jbi:end-point-reference");
			epr.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:sns",
					endpoint.getServiceName().getNamespaceURI());
			epr.setAttributeNS("http://java.sun.com/jbi/end-point-reference",
					"jbi:service-name", "sns:"
							+ endpoint.getServiceName().getLocalPart());
			epr.setAttributeNS("http://java.sun.com/jbi/end-point-reference",
					"jbi:end-point-name", endpoint.getEndpointName());
			fragment.appendChild(epr);
			return fragment;
		} catch (Exception e) {
			LOG.warn("Unable to create reference for ServiceEndpoint "
					+ endpoint, e);
		}
		return null;
	}
}