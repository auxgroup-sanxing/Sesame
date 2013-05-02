package com.sanxing.sesame.mbean;

import javax.jbi.management.ComponentLifeCycleMBean;

public abstract interface ComponentMBean extends ComponentLifeCycleMBean {
	public static final String TYPE_SERVICE_ENGINE = "service-engine";
	public static final String TYPE_BINDING_COMPONENT = "binding-component";
	public static final String TYPE_POJO = "pojo";

	public abstract String getName();

	public abstract boolean isExchangeThrottling();

	public abstract void setExchangeThrottling(boolean paramBoolean);

	public abstract long getThrottlingTimeout();

	public abstract void setThrottlingTimeout(long paramLong);

	public abstract int getThrottlingInterval();

	public abstract void setThrottlingInterval(int paramInt);

	public abstract String getComponentType();
}