package com.sanxing.sesame.container;

import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.messaging.ExchangePacket;
import com.sanxing.sesame.messaging.MessageExchangeImpl;
import com.sanxing.sesame.resolver.SubscriptionFilter;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import java.io.Serializable;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

public class SubscriptionSpec implements Serializable {
	private static final long serialVersionUID = 8458586342841647313L;
	private QName service;
	private QName interfaceName;
	private QName operation;
	private String endpoint;
	private transient SubscriptionFilter filter;
	private ComponentNameSpace name;

	public String getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public SubscriptionFilter getFilter() {
		return this.filter;
	}

	public void setFilter(SubscriptionFilter filter) {
		this.filter = filter;
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

	public QName getService() {
		return this.service;
	}

	public void setService(QName service) {
		this.service = service;
	}

	public ComponentNameSpace getName() {
		return this.name;
	}

	public void setName(ComponentNameSpace name) {
		this.name = name;
	}

	public boolean matches(Registry registry, MessageExchangeImpl exchange) {
		boolean result = false;

		ExchangePacket packet = exchange.getPacket();
		ComponentNameSpace sourceId = packet.getSourceId();
		if (sourceId != null) {
			if (this.service != null) {
				ServiceEndpoint[] ses = registry
						.getEndpointsForService(this.service);
				if (ses != null) {
					for (int i = 0; i < ses.length; ++i) {
						InternalEndpoint se = (InternalEndpoint) ses[i];
						if ((se.getComponentNameSpace() != null)
								&& (se.getComponentNameSpace().equals(sourceId))) {
							result = true;
							break;
						}
					}
				}
			}
			if ((result) && (this.interfaceName != null)) {
				ServiceEndpoint[] ses = registry
						.getEndpointsForInterface(this.interfaceName);
				if (ses != null) {
					result = false;
					for (int i = 0; i < ses.length; ++i) {
						InternalEndpoint se = (InternalEndpoint) ses[i];
						if ((se.getComponentNameSpace() != null)
								&& (se.getComponentNameSpace().equals(sourceId))) {
							result = true;
							break;
						}
					}
				}
			}

		}

		if ((this.service == null) && (this.interfaceName == null)) {
			result = true;
		}
		if ((result) && (this.filter != null)) {
			result = this.filter.matches(exchange);
		}
		return result;
	}

	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof SubscriptionSpec) {
			SubscriptionSpec other = (SubscriptionSpec) obj;
			result = ((this.name == null) && (other.name == null))
					|| (((!((this.name != null && this.name.equals(other.name) || (other.name != null && other.name.equals(this.name)))))
							|| (this.service != null) || (other.service != null))
							&& ((this.service == null)
									|| (other.service == null)
									|| (!(this.service.equals(other.service)))
									|| (this.interfaceName != null) || (other.interfaceName != null))
							&& ((this.interfaceName == null)
									|| (other.interfaceName == null)
									|| (!(this.interfaceName
											.equals(other.interfaceName)))
									|| (this.endpoint != null) || (other.endpoint != null)) && ((this.endpoint == null)
							|| (other.endpoint == null) || (!(this.endpoint
								.equals(other.endpoint)))));
		}

		return result;
	}

	public int hashCode() {
		return (((this.name != null) ? this.name.hashCode() : 0) ^ ((this.interfaceName != null) ? this.interfaceName
				.hashCode() : (this.service != null) ? this.service.hashCode()
				: super.hashCode()));
	}
}