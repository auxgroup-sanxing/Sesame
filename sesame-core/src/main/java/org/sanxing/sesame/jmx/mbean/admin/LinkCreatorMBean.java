package org.sanxing.sesame.jmx.mbean.admin;

import javax.management.ObjectName;

public abstract interface LinkCreatorMBean {
	public abstract void register(ObjectName paramObjectName, String paramString);

	public abstract void unregister(ObjectName paramObjectName,
			String paramString);
}