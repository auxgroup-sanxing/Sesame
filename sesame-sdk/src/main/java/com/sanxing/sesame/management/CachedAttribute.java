package com.sanxing.sesame.management;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;

import org.apache.commons.beanutils.PropertyUtilsBean;

public class CachedAttribute {
	private Object bean;
	private String name;
	private Attribute attribute;
	private MBeanAttributeInfo attributeInfo;
	private PropertyDescriptor propertyDescriptor;

	public CachedAttribute(Attribute attr) {
		this.attribute = attr;
		this.name = attr.getName();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Attribute getAttribute() {
		return this.attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public void updateValue(PropertyUtilsBean beanUtil) throws MBeanException {
		try {
			Object value = beanUtil.getProperty(this.bean, getName());
			if (value != this.attribute.getValue())
				this.attribute = new Attribute(getName(), value);
		} catch (IllegalAccessException e) {
			throw new MBeanException(e);
		} catch (InvocationTargetException e) {
			throw new MBeanException(e);
		} catch (NoSuchMethodException e) {
			throw new MBeanException(e);
		}
	}

	public void updateAttribute(PropertyUtilsBean beanUtils, Attribute attr)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if ((this.attribute != null) && (this.propertyDescriptor != null)) {
			beanUtils.setProperty(this.bean, getName(), attr.getValue());
		}
		this.attribute = attr;
	}

	public void updateAttributeValue(Object value) {
		this.attribute = new Attribute(this.attribute.getName(), value);
	}

	public Object getBean() {
		return this.bean;
	}

	public void setBean(Object bean) {
		this.bean = bean;
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return this.propertyDescriptor;
	}

	public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
		this.propertyDescriptor = propertyDescriptor;
	}

	public MBeanAttributeInfo getAttributeInfo() {
		return this.attributeInfo;
	}

	public void setAttributeInfo(MBeanAttributeInfo attributeInfo) {
		this.attributeInfo = attributeInfo;
	}
}