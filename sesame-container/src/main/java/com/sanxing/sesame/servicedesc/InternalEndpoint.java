package com.sanxing.sesame.servicedesc;

import com.sanxing.sesame.mbean.ComponentNameSpace;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.w3c.dom.DocumentFragment;

public class InternalEndpoint extends AbstractEndpoint {
	private static final long serialVersionUID = -2312687961530378310L;
	private String endpointName;
	private QName serviceName;
	private Set<QName> interfaces = new HashSet();
	private transient Map<ComponentNameSpace, InternalEndpoint> remotes = new HashMap();

	public InternalEndpoint(ComponentNameSpace componentName,
			String endpointName, QName serviceName) {
		super(componentName);
		this.endpointName = endpointName;
		this.serviceName = serviceName;
	}

	public DocumentFragment getAsReference(QName operationName) {
		return EndpointReferenceBuilder.getReference(this);
	}

	public String getEndpointName() {
		return this.endpointName;
	}

	public QName[] getInterfaces() {
		QName[] result = new QName[this.interfaces.size()];
		this.interfaces.toArray(result);
		return result;
	}

	public void addInterface(QName name) {
		this.interfaces.add(name);
	}

	public QName getServiceName() {
		return this.serviceName;
	}

	public InternalEndpoint[] getRemoteEndpoints() {
		InternalEndpoint[] result = new InternalEndpoint[this.remotes.size()];
		this.remotes.values().toArray(result);
		return result;
	}

	public void addRemoteEndpoint(InternalEndpoint remote) {
		this.remotes.put(remote.getComponentNameSpace(), remote);
	}

	public void removeRemoteEndpoint(InternalEndpoint remote) {
		this.remotes.remove(remote.getComponentNameSpace());
	}

	public boolean isLocal() {
		return (getComponentNameSpace() != null);
	}

	public boolean isClustered() {
		return ((this.remotes != null) && (this.remotes.size() > 0));
	}

	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof InternalEndpoint) {
			InternalEndpoint other = (InternalEndpoint) obj;
			result = (other.serviceName.equals(this.serviceName))
					&& (other.endpointName.equals(this.endpointName));
		}

		return result;
	}

	public int hashCode() {
		return (this.serviceName.hashCode() ^ this.endpointName.hashCode());
	}

	public String toString() {
		return "ServiceEndpoint[service=" + this.serviceName + ",endpoint="
				+ this.endpointName + ", clustered " + isClustered() + "]";
	}

	protected String getClassifier() {
		return "internal";
	}
}