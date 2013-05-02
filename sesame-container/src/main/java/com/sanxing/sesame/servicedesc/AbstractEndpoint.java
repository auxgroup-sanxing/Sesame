package com.sanxing.sesame.servicedesc;

import com.sanxing.sesame.mbean.ComponentNameSpace;
import java.io.Serializable;
import javax.jbi.servicedesc.ServiceEndpoint;

public abstract class AbstractEndpoint implements ServiceEndpoint, Serializable {
	private static final long serialVersionUID = -591733214139930976L;
	private ComponentNameSpace componentName;
	private String key;
	private String uniqueKey;

	public AbstractEndpoint(ComponentNameSpace componentName) {
		this.componentName = componentName;
	}

	protected AbstractEndpoint() {
	}

	public ComponentNameSpace getComponentNameSpace() {
		return this.componentName;
	}

	public void setComponentName(ComponentNameSpace componentName) {
		this.componentName = componentName;
	}

	public String getKey() {
		if (this.key == null) {
			this.key = EndpointSupport.getKey(getServiceName(),
					getEndpointName());
		}
		return this.key;
	}

	public String getUniqueKey() {
		if (this.uniqueKey == null) {
			this.uniqueKey = getClassifier() + ":" + getKey();
		}
		return this.uniqueKey;
	}

	protected abstract String getClassifier();
}