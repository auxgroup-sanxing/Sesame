package com.sanxing.sesame.container;

import com.sanxing.sesame.messaging.PojoMarshaler;
import com.sanxing.sesame.resolver.EndpointChooser;
import com.sanxing.sesame.resolver.EndpointResolver;
import com.sanxing.sesame.resolver.InterfaceNameEndpointResolver;
import com.sanxing.sesame.resolver.ServiceAndEndpointNameResolver;
import com.sanxing.sesame.resolver.ServiceNameEndpointResolver;
import com.sanxing.sesame.resolver.URIResolver;
import java.io.Serializable;
import java.util.Arrays;
import javax.xml.namespace.QName;

public class ActivationSpec implements Serializable {
	static final long serialVersionUID = 8458586342841647313L;
	private String id;
	private String componentName;
	private Object component;
	private QName service;
	private QName interfaceName;
	private QName operation;
	private String endpoint;
	private transient EndpointResolver destinationResolver;
	private transient EndpointChooser interfaceChooser;
	private transient EndpointChooser serviceChooser;
	private QName destinationService;
	private QName destinationInterface;
	private QName destinationOperation;
	private String destinationEndpoint;
	private transient PojoMarshaler marshaler;
	private SubscriptionSpec[] subscriptions = new SubscriptionSpec[0];
	private boolean failIfNoDestinationEndpoint = true;
	private Boolean persistent;
	private String destinationUri;

	public ActivationSpec() {
	}

	public ActivationSpec(Object component) {
		this.component = component;
	}

	public ActivationSpec(String id, Object component) {
		this.id = id;
		this.component = component;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getComponentName() {
		return this.componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public Object getComponent() {
		return this.component;
	}

	public void setComponent(Object component) {
		this.component = component;
	}

	public QName getService() {
		return this.service;
	}

	public void setService(QName service) {
		this.service = service;
	}

	public String getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public QName getInterfaceName() {
		return this.interfaceName;
	}

	public void setInterfaceName(QName interfaceName) {
		this.interfaceName = interfaceName;
	}

	public QName getOperation() {
		return this.operation;
	}

	public void setOperation(QName operation) {
		this.operation = operation;
	}

	public EndpointResolver getDestinationResolver() {
		if (this.destinationResolver == null) {
			this.destinationResolver = createEndpointResolver();
		}
		return this.destinationResolver;
	}

	public void setDestinationResolver(EndpointResolver destinationResolver) {
		this.destinationResolver = destinationResolver;
	}

	public EndpointChooser getInterfaceChooser() {
		return this.interfaceChooser;
	}

	public void setInterfaceChooser(EndpointChooser interfaceChooser) {
		this.interfaceChooser = interfaceChooser;
	}

	public EndpointChooser getServiceChooser() {
		return this.serviceChooser;
	}

	public void setServiceChooser(EndpointChooser serviceChooser) {
		this.serviceChooser = serviceChooser;
	}

	public QName getDestinationService() {
		return this.destinationService;
	}

	public void setDestinationService(QName destinationService) {
		this.destinationService = destinationService;
	}

	public QName getDestinationInterface() {
		return this.destinationInterface;
	}

	public void setDestinationInterface(QName destinationInterface) {
		this.destinationInterface = destinationInterface;
	}

	public QName getDestinationOperation() {
		return this.destinationOperation;
	}

	public void setDestinationOperation(QName destinationOperation) {
		this.destinationOperation = destinationOperation;
	}

	public String getDestinationEndpoint() {
		return this.destinationEndpoint;
	}

	public void setDestinationEndpoint(String destinationEndpoint) {
		this.destinationEndpoint = destinationEndpoint;
	}

	public PojoMarshaler getMarshaler() {
		return this.marshaler;
	}

	public void setMarshaler(PojoMarshaler marshaler) {
		this.marshaler = marshaler;
	}

	public boolean isFailIfNoDestinationEndpoint() {
		return this.failIfNoDestinationEndpoint;
	}

	public void setFailIfNoDestinationEndpoint(
			boolean failIfNoDestinationEndpoint) {
		this.failIfNoDestinationEndpoint = failIfNoDestinationEndpoint;
	}

	protected EndpointResolver createEndpointResolver() {
		if (this.destinationService != null) {
			if (this.destinationEndpoint != null) {
				return new ServiceAndEndpointNameResolver(
						this.destinationService, this.destinationEndpoint);
			}
			return new ServiceNameEndpointResolver(this.destinationService);
		}
		if (this.destinationInterface != null)
			return new InterfaceNameEndpointResolver(this.destinationInterface);
		if (this.destinationUri != null) {
			return new URIResolver(this.destinationUri);
		}
		return null;
	}

	public Boolean getPersistent() {
		return this.persistent;
	}

	public void setPersistent(Boolean persistent) {
		this.persistent = persistent;
	}

	public String getDestinationUri() {
		return this.destinationUri;
	}

	public void setDestinationUri(String destinationUri) {
		this.destinationUri = destinationUri;
	}

	public String toString() {
		return "ActivationSpec [component=" + this.component
				+ ", componentName=" + this.componentName
				+ ", destinationEndpoint=" + this.destinationEndpoint
				+ ", destinationInterface=" + this.destinationInterface
				+ ", destinationOperation=" + this.destinationOperation
				+ ", destinationService=" + this.destinationService
				+ ", destinationUri=" + this.destinationUri + ", endpoint="
				+ this.endpoint + ", failIfNoDestinationEndpoint="
				+ this.failIfNoDestinationEndpoint + ", id=" + this.id
				+ ", interfaceName=" + this.interfaceName + ", operation="
				+ this.operation + ", persistent=" + this.persistent
				+ ", service=" + this.service + ", subscriptions="
				+ Arrays.toString(this.subscriptions) + "]";
	}
}