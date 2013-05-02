package com.sanxing.sesame.component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.wsdl.Service;

import com.sanxing.sesame.listener.MessageExchangeListener;
import com.sanxing.sesame.service.OperationContext;
import com.sanxing.sesame.service.ReferenceEntry;
import com.sanxing.sesame.service.ServiceUnit;

public abstract class BindingComponent extends ComponentSupport implements
		MessageExchangeListener {
	private Map<ServiceEndpoint, ServiceUnit> endpoints = new ConcurrentHashMap();

	protected ServiceUnit getServiceUnit(ServiceEndpoint endpoint) {
		return ((ServiceUnit) this.endpoints.get(endpoint));
	}

	public final boolean isBindingComponent() {
		return true;
	}

	public final boolean isEngineComponent() {
		return false;
	}

	public void start(String serviceUnitName) throws DeploymentException {
		try {
			ServiceUnit serviceUnit = getServiceUnit(serviceUnitName);
			if (serviceUnit == null)
				throw new DeploymentException("Can not find the service unit '"
						+ serviceUnitName + "'", null);
			Service service = serviceUnit.getService();
			Set<String> portNames = service.getPorts().keySet();
			for (String portName : portNames) {
				ServiceEndpoint endpoint = getContext().activateEndpoint(
						serviceUnit.getServiceName(), portName);
				this.endpoints.put(endpoint, serviceUnit);
			}
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
			Service service = serviceUnit.getService();
			Set<String> portNames = service.getPorts().keySet();
			for (String portName : portNames) {
				ServiceEndpoint endpoint = getContext().getEndpoint(
						serviceUnit.getServiceName(), portName);
				if (endpoint != null) {
					getContext().deactivateEndpoint(endpoint);
					this.endpoints.remove(endpoint);
				}
			}
		} catch (Exception e) {
			throw taskFailure("stop-" + serviceUnitName,
					(e.getMessage() != null) ? e.getMessage() : e.toString());
		}
	}

	public MessageExchange createExchange(OperationContext operationContext)
			throws MessagingException {
		MessageExchange exchange = getExchangeFactory().createInOutExchange();

		ReferenceEntry ref = operationContext.getReference();
		if (ref != null) {
			exchange.setService(ref.getServcieName());
			exchange.setInterfaceName(ref.getInterfaceName());
			exchange.setOperation(ref.getOperationName());
			if ((ref.getServcieName() != null)
					&& (ref.getEndpointName() != null)) {
				ServiceEndpoint endpoint = getContext().getEndpoint(
						ref.getServcieName(), ref.getEndpointName());
				exchange.setEndpoint(endpoint);
			}
		} else {
			if (operationContext.getInterfaceName() == null)
				throw new MessagingException(
						"Could not find route for operation: "
								+ operationContext.getOperationName());
			exchange.setInterfaceName(operationContext.getInterfaceName());
			exchange.setOperation(operationContext.getOperationName());
		}

		exchange.setProperty("sesame.exchange.consumer", getContext()
				.getComponentName());

		return exchange;
	}
}