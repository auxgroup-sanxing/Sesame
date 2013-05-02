package com.sanxing.sesame.management;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

public abstract class BaseLifeCycle implements LifeCycleMBean,
		MBeanInfoProvider {
	public static final String INITIALIZED = "Initialized";
	protected String currentState = "Unknown";
	protected PropertyChangeListener listener;

	public String getName() {
		String name = super.getClass().getName();
		int index = name.lastIndexOf(".");
		if ((index >= 0) && (index + 1 < name.length())) {
			name = name.substring(index + 1);
		}
		return name;
	}

	public String getType() {
		String name = super.getClass().getName();
		int index = name.lastIndexOf(".");
		if ((index >= 0) && (index + 1 < name.length())) {
			name = name.substring(index + 1);
		}
		return name;
	}

	public String getSubType() {
		return null;
	}

	protected void init() throws JBIException {
		setCurrentState("Initialized");
	}

	public void start() throws JBIException {
		setCurrentState("Started");
	}

	public void stop() throws JBIException {
		setCurrentState("Stopped");
	}

	public void shutDown() throws JBIException {
		setCurrentState("Shutdown");
	}

	public String getCurrentState() {
		return this.currentState;
	}

	protected void setCurrentState(String newValue) {
		String oldValue = this.currentState;
		this.currentState = newValue;
		firePropertyChanged("currentState", oldValue, newValue);
	}

	public boolean isStarted() {
		return ((this.currentState != null) && (this.currentState
				.equals("Started")));
	}

	public boolean isStopped() {
		return ((this.currentState != null) && (this.currentState
				.equals("Stopped")));
	}

	public boolean isShutDown() {
		return ((this.currentState != null) && (this.currentState
				.equals("Shutdown")));
	}

	public boolean isInitialized() {
		return ((this.currentState != null) && (this.currentState
				.equals("Initialized")));
	}

	public boolean isUnknown() {
		return ((this.currentState == null) || (this.currentState
				.equals("Unknown")));
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(), "currentState",
				"Current State of Managed Item");
		helper.addAttribute(getObjectToManage(), "name", "name of the Item");
		helper.addAttribute(getObjectToManage(), "description",
				"description of the Item");
		return helper.getAttributeInfos();
	}

	public MBeanOperationInfo[] getOperationInfos() throws JMException {
		OperationInfoHelper helper = new OperationInfoHelper();
		helper.addOperation(getObjectToManage(), "start", "start the item");
		helper.addOperation(getObjectToManage(), "stop", "stop the item");
		helper.addOperation(getObjectToManage(), "shutDown",
				"shutdown the item");
		return helper.getOperationInfos();
	}

	public Object getObjectToManage() {
		return this;
	}

	public void setPropertyChangeListener(PropertyChangeListener listener) {
		this.listener = listener;
	}

	protected void firePropertyChanged(String name, Object oldValue,
			Object newValue) {
		if (this.listener != null) {
			PropertyChangeEvent event = new PropertyChangeEvent(this, name,
					oldValue, newValue);
			this.listener.propertyChange(event);
		}
	}
}