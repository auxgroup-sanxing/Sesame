package com.sanxing.sesame.mbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.jbi.JBIException;
import javax.jbi.component.Component;

public class ComponentRegistry {
	private Map<ComponentNameSpace, ComponentMBeanImpl> idMap = new LinkedHashMap();
	private boolean runningStateInitialized;
	private Registry registry;

	protected ComponentRegistry(Registry reg) {
		this.registry = reg;
	}

	public synchronized ComponentMBeanImpl registerComponent(
			ComponentNameSpace name, String description, Component component,
			boolean binding, boolean service, String[] sharedLibraries) {
		ComponentMBeanImpl result = null;
		if (!(this.idMap.containsKey(name))) {
			result = new ComponentMBeanImpl(this.registry.getContainer(), name,
					description, component, binding, service, sharedLibraries);
			this.idMap.put(name, result);
		}
		return result;
	}

	public synchronized void start() throws JBIException {
		Iterator i;
		if (!(setInitialRunningStateFromStart()))
			for (i = getComponents().iterator(); i.hasNext();) {
				ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
				lcc.doStart();
			}
	}

	public synchronized void stop() throws JBIException {
		for (Iterator i = getReverseComponents().iterator(); i.hasNext();) {
			ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
			lcc.doStop();
		}
		this.runningStateInitialized = false;
	}

	public synchronized void shutDown() throws JBIException {
		for (Iterator i = getReverseComponents().iterator(); i.hasNext();) {
			ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
			lcc.persistRunningState();
			lcc.doShutDown();
		}
	}

	private Collection<ComponentMBeanImpl> getReverseComponents() {
		synchronized (this.idMap) {
			List l = new ArrayList(this.idMap.values());
			Collections.reverse(l);
			return l;
		}
	}

	public synchronized void deregisterComponent(ComponentMBeanImpl component) {
		this.idMap.remove(component.getComponentNameSpace());
	}

	public ComponentMBeanImpl getComponent(ComponentNameSpace id) {
		synchronized (this.idMap) {
			return ((ComponentMBeanImpl) this.idMap.get(id));
		}
	}

	public Collection<ComponentMBeanImpl> getComponents() {
		synchronized (this.idMap) {
			return new ArrayList(this.idMap.values());
		}
	}

	private boolean setInitialRunningStateFromStart() throws JBIException {
		boolean result = !(this.runningStateInitialized);
		Iterator i;
		if (!(this.runningStateInitialized)) {
			this.runningStateInitialized = true;
			for (i = getComponents().iterator(); i.hasNext();) {
				ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
				if (!(lcc.isPojo()))
					lcc.setInitialRunningState();
				else {
					lcc.doStart();
				}
			}
		}
		return result;
	}
}