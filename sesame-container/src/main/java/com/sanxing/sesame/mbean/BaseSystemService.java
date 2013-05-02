package com.sanxing.sesame.mbean;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.management.BaseLifeCycle;
import javax.jbi.JBIException;

public abstract class BaseSystemService extends BaseLifeCycle {
	protected JBIContainer container;

	public String getName() {
		String name = super.getClass().getName();
		int index = name.lastIndexOf(".");
		if ((index >= 0) && (index + 1 < name.length())) {
			name = name.substring(index + 1);
		}
		return name;
	}

	public String getType() {
		return "SystemService";
	}

	public void init(JBIContainer cont) throws JBIException {
		this.container = cont;
		cont.getManagementContext().registerSystemService(this,
				getServiceMBean());
		super.init();
	}

	public void shutDown() throws JBIException {
		stop();
		super.shutDown();
		if ((this.container != null)
				&& (this.container.getManagementContext() != null))
			this.container.getManagementContext().unregisterSystemService(this);
	}

	protected abstract Class getServiceMBean();

	public JBIContainer getContainer() {
		return this.container;
	}
}