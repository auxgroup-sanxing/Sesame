package com.sanxing.sesame.mbean;

import javax.jbi.management.LifeCycleMBean;
import javax.management.ObjectName;

public abstract interface RegistryMBean extends LifeCycleMBean {
	public abstract ObjectName[] getComponentNames();

	public abstract ObjectName[] getServiceUnitNames();

	public abstract ObjectName[] getServiceAssemblyNames();

	public abstract ObjectName[] getSharedLibraryNames();

	public abstract ObjectName[] getEndpointNames();
}