package com.sanxing.sesame.endpoint;

import com.sanxing.sesame.deployment.Provides;
import com.sanxing.sesame.deployment.Services;
import com.sanxing.sesame.mbean.ComponentNameSpace;
import com.sanxing.sesame.mbean.Registry;
import com.sanxing.sesame.mbean.ServiceUnitLifeCycle;
import com.sanxing.sesame.servicedesc.InternalEndpoint;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SUDescriptorProcessor implements EndpointProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(SUDescriptorProcessor.class);
	private Registry registry;

	public void init(Registry reg) {
		this.registry = reg;
	}

	public void process(InternalEndpoint serviceEndpoint) {
		ServiceUnitLifeCycle[] sus = this.registry
				.getDeployedServiceUnits(serviceEndpoint
						.getComponentNameSpace().getName());
		for (int i = 0; i < sus.length; ++i) {
			Services services = sus[i].getServices();
			if (services != null) {
				Provides[] provides = services.getProvides();
				if (provides != null)
					for (int j = 0; j < provides.length; ++j) {
						if ((provides[j].getInterfaceName() == null)
								|| (!(serviceEndpoint.getServiceName()
										.equals(provides[j].getServiceName())))
								|| (!(serviceEndpoint.getEndpointName()
										.equals(provides[j].getEndpointName())))) {
							continue;
						}
						if (LOG.isDebugEnabled()) {
							LOG.debug("Endpoint " + serviceEndpoint
									+ " is provided by SU " + sus[i].getName());
							LOG.debug("Endpoint " + serviceEndpoint
									+ " implements interface "
									+ provides[j].getInterfaceName());
						}
						serviceEndpoint.addInterface(provides[j]
								.getInterfaceName());
					}
			}
		}
	}
}