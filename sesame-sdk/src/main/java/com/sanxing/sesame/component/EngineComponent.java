package com.sanxing.sesame.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.management.DeploymentException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import com.sanxing.sesame.listener.MessageExchangeListener;
import com.sanxing.sesame.service.ServiceUnit;

public abstract class EngineComponent extends ComponentSupport implements
		MessageExchangeListener {
	private Map<ServiceEndpoint, ServiceUnit> endpoints = new ConcurrentHashMap();

	protected ServiceUnit getServiceUnit(ServiceEndpoint endpoint) {
		return ((ServiceUnit) this.endpoints.get(endpoint));
	}

	public final boolean isBindingComponent() {
		return false;
	}

	public final boolean isEngineComponent() {
		return true;
	}

	public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
		return null;
	}

	public void start(String serviceUnitName) throws DeploymentException {
		try {
			ServiceUnit serviceUnit = getServiceUnit(serviceUnitName);
			if (serviceUnit == null)
				throw new DeploymentException("Can not find the service unit '"
						+ serviceUnitName + "'", null);
			QName serviceName = getService();
			if (serviceName == null) {
				serviceName = serviceUnit.getDefinition().getQName();
			}

			ServiceEndpoint endpoint = getContext().activateEndpoint(
					serviceName, serviceUnitName);
			this.endpoints.put(endpoint, serviceUnit);
		} catch (Exception e) {
			throw taskFailure("start-" + serviceUnitName,
					(e.getMessage() != null) ? e.getMessage() : e.toString());
		}
	}

	public void stop(String serviceUnitName) throws DeploymentException {
		try {
			ServiceUnit serviceUnit = getServiceUnit(serviceUnitName);
			if (serviceUnit == null)
				throw new DeploymentException("Can not find the service unit '"
						+ serviceUnitName + "'", null);
			QName serviceName = getService();
			if (serviceName == null) {
				serviceName = serviceUnit.getDefinition().getQName();
			}
			ServiceEndpoint endpoint = getContext().getEndpoint(serviceName,
					serviceUnitName);
			if (endpoint != null) {
				getContext().deactivateEndpoint(endpoint);
				this.endpoints.remove(endpoint);
			}
		} catch (Exception e) {
			throw taskFailure("stop-" + serviceUnitName,
					(e.getMessage() != null) ? e.getMessage() : e.toString());
		}
	}

	public Document getServiceDescription(ServiceEndpoint endpoint) {
		if (getServiceUnitManager() == null) {
			return null;
		}

		ServiceUnit serviceUnit = getServiceUnit(endpoint.getEndpointName());
		if (serviceUnit != null) {
			try {
				WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
				Document doc = writer.getDocument(serviceUnit.getDefinition());
				doc.setDocumentURI(serviceUnit.getDefinition()
						.getDocumentBaseURI());
				return doc;
			} catch (WSDLException e) {
				return null;
			}
		}
		return null;
	}
}