package com.sanxing.sesame.management;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.StandardMBean;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;
import javax.management.modelmbean.ModelMBeanNotificationInfo;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseStandardMBean extends StandardMBean implements
		ModelMBeanNotificationBroadcaster, MBeanRegistration,
		PropertyChangeListener {
	private static final Logger LOG = LoggerFactory.getLogger(BaseStandardMBean.class);

	private static final Map<String, Class<?>> PRIMITIVE_CLASSES = new Hashtable(8);
	protected ExecutorService executorService;
	private Map<String, CachedAttribute> cachedAttributes;
	private PropertyUtilsBean beanUtil;
	private NotificationBroadcasterSupport broadcasterSupport;
	private MBeanAttributeInfo[] attributeInfos;
	private MBeanInfo beanInfo;
	private ObjectName objectName;
	private MBeanServer beanServer;

	public BaseStandardMBean(Object object, Class interfaceMBean,
			String description, MBeanAttributeInfo[] attrs,
			MBeanOperationInfo[] ops, ExecutorService executorService)
			throws ReflectionException, NotCompliantMBeanException {
		super(object, interfaceMBean);

		PRIMITIVE_CLASSES.put(Boolean.TYPE.toString(), Boolean.TYPE);
		PRIMITIVE_CLASSES.put(Character.TYPE.toString(), Character.TYPE);
		PRIMITIVE_CLASSES.put(Byte.TYPE.toString(), Byte.TYPE);
		PRIMITIVE_CLASSES.put(Short.TYPE.toString(), Short.TYPE);
		PRIMITIVE_CLASSES.put(Integer.TYPE.toString(), Integer.TYPE);
		PRIMITIVE_CLASSES.put(Long.TYPE.toString(), Long.TYPE);
		PRIMITIVE_CLASSES.put(Float.TYPE.toString(), Float.TYPE);
		PRIMITIVE_CLASSES.put(Double.TYPE.toString(), Double.TYPE);

		this.cachedAttributes = new LinkedHashMap();

		this.beanUtil = new PropertyUtilsBean();

		this.broadcasterSupport = new NotificationBroadcasterSupport();

		this.attributeInfos = attrs;
		buildAttributes(object, this.attributeInfos);
		this.beanInfo = new MBeanInfo(object.getClass().getName(), description,
				attrs, null, ops, getNotificationInfo());
		this.executorService = executorService;
	}

	public MBeanInfo getMBeanInfo() {
		return this.beanInfo;
	}

	public ObjectName getObjectName() {
		return this.objectName;
	}

	public MBeanServer getBeanServer() {
		return this.beanServer;
	}

	public Object getAttribute(String name) throws AttributeNotFoundException,
			MBeanException, ReflectionException {
		Object result = null;
		CachedAttribute ca = (CachedAttribute) this.cachedAttributes.get(name);
		if (ca == null) {
			for (Map.Entry entry : this.cachedAttributes.entrySet()) {
				String key = (String) entry.getKey();
				if (key.equalsIgnoreCase(name)) {
					ca = (CachedAttribute) entry.getValue();
					break;
				}
			}
		}
		if (ca != null)
			result = getCurrentValue(ca);
		else {
			throw new AttributeNotFoundException("Could not locate " + name);
		}
		return result;
	}

	public void setAttribute(Attribute attr) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
		String name = attr.getName();
		CachedAttribute ca = (CachedAttribute) this.cachedAttributes.get(name);
		if (ca != null) {
			Attribute old = ca.getAttribute();
			try {
				ca.updateAttribute(this.beanUtil, attr);
				sendAttributeChangeNotification(old, attr);
			} catch (NoSuchMethodException e) {
				throw new ReflectionException(e);
			} catch (IllegalAccessException e) {
				throw new ReflectionException(e);
			} catch (InvocationTargetException e) {
				Throwable t = e.getTargetException();
				if (t instanceof Exception) {
					throw new MBeanException(e);
				}
				throw new MBeanException(e);
			}
		} else {
			throw new AttributeNotFoundException("Could not locate " + name);
		}
	}

	public void updateAttribute(String name, Object value) {
		CachedAttribute ca = (CachedAttribute) this.cachedAttributes.get(name);
		if (ca != null) {
			Attribute old = ca.getAttribute();
			ca.updateAttributeValue(value);
			try {
				sendAttributeChangeNotification(old, ca.getAttribute());
			} catch (RuntimeOperationsException e) {
				LOG.error("Failed to update attribute: " + name
						+ " to new value: " + value, e);
			} catch (MBeanException e) {
				LOG.error("Failed to update attribute: " + name
						+ " to new value: " + value, e);
			}
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		updateAttribute(event.getPropertyName(), event.getNewValue());
	}

	public AttributeList getAttributes(String[] attributes) {
		AttributeList result = new AttributeList();
		try {
			CachedAttribute ca;
			if (attributes != null) {
				for (int i = 0; i < attributes.length; ++i) {
					ca = (CachedAttribute) this.cachedAttributes
							.get(attributes[i]);
					ca.updateValue(this.beanUtil);
					result.add(ca.getAttribute());
				}
				return result;
			}

			for (Map.Entry entry : this.cachedAttributes.entrySet()) {
				ca = (CachedAttribute) entry.getValue();
				ca.updateValue(this.beanUtil);
				result.add(ca.getAttribute());
			}
		} catch (MBeanException e) {
			LOG.error("Caught excdeption building attributes", e);
		}
		return result;
	}

	public AttributeList setAttributes(AttributeList attributes) {
		AttributeList result = new AttributeList();
		if (attributes != null) {
			for (int i = 0; i < attributes.size(); ++i) {
				Attribute attribute = (Attribute) attributes.get(i);
				try {
					setAttribute(attribute);
				} catch (AttributeNotFoundException e) {
					LOG.warn("Failed to setAttribute(" + attribute + ")", e);
				} catch (InvalidAttributeValueException e) {
					LOG.warn("Failed to setAttribute(" + attribute + ")", e);
				} catch (MBeanException e) {
					LOG.warn("Failed to setAttribute(" + attribute + ")", e);
				} catch (ReflectionException e) {
					LOG.warn("Failed to setAttribute(" + attribute + ")", e);
				}
				result.add(attribute);
			}
		}
		return result;
	}

	public Object invoke(String name, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		try {
			Class[] parameterTypes = new Class[signature.length];
			for (int i = 0; i < parameterTypes.length; ++i) {
				parameterTypes[i] = ((Class) PRIMITIVE_CLASSES
						.get(signature[i]));
				if (parameterTypes[i] == null) {
					parameterTypes[i] = Class.forName(signature[i]);
				}
			}
			Thread.currentThread().setContextClassLoader(
					getImplementation().getClass().getClassLoader());
			return MethodUtils.invokeMethod(getImplementation(), name, params,
					parameterTypes);
		} catch (ClassNotFoundException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof Exception)
				;
			throw new MBeanException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(oldCl);
		}
		return null;
	}

	public ObjectName preRegister(MBeanServer mbs, ObjectName on)
			throws Exception {
		if (mbs != null) {
			this.beanServer = mbs;
		}
		if (on != null) {
			this.objectName = on;
		}
		return on;
	}

	public void postRegister(Boolean done) {
	}

	public void preDeregister() throws Exception {
	}

	public void postDeregister() {
	}

	public void sendNotification(final Notification notification)
			throws MBeanException, RuntimeOperationsException {
		if ((notification != null) && (!(this.executorService.isShutdown())))
			this.executorService.execute(new Runnable() {
				public void run() {
					BaseStandardMBean.this.broadcasterSupport
							.sendNotification(notification);
				}
			});
	}

	public void sendNotification(String text) throws MBeanException,
			RuntimeOperationsException {
		if (text != null) {
			Notification myNtfyObj = new Notification("jmx.modelmbean.generic",
					this, 1L, text);
			sendNotification(myNtfyObj);
		}
	}

	public void addAttributeChangeNotificationListener(NotificationListener l,
			String attrName, Object handback) throws MBeanException,
			RuntimeOperationsException, IllegalArgumentException {
		AttributeChangeNotificationFilter currFilter = new AttributeChangeNotificationFilter();
		currFilter.enableAttribute(attrName);
		this.broadcasterSupport
				.addNotificationListener(l, currFilter, handback);
	}

	public void removeAttributeChangeNotificationListener(
			NotificationListener l, String attrName) throws MBeanException,
			RuntimeOperationsException, ListenerNotFoundException {
		this.broadcasterSupport.removeNotificationListener(l);
	}

	public void sendAttributeChangeNotification(
			AttributeChangeNotification notification) throws MBeanException,
			RuntimeOperationsException {
		sendNotification(notification);
	}

	public void sendAttributeChangeNotification(Attribute oldAttr,
			Attribute newAttr) throws MBeanException,
			RuntimeOperationsException {
		if (!(oldAttr.equals(newAttr))) {
			AttributeChangeNotification notification = new AttributeChangeNotification(
					this.objectName, 1L, new Date().getTime(),
					"AttributeChange", oldAttr.getName(), newAttr.getValue()
							.getClass().toString(), oldAttr.getValue(),
					newAttr.getValue());
			sendAttributeChangeNotification(notification);
		}
	}

	public final MBeanNotificationInfo[] getNotificationInfo() {
		MBeanNotificationInfo[] result = new MBeanNotificationInfo[2];
		Descriptor genericDescriptor = new DescriptorSupport(new String[] {
				"name=GENERIC", "descriptorType=notification", "log=T",
				"severity=5", "displayName=jmx.modelmbean.generic" });
		result[0] = new ModelMBeanNotificationInfo(
				new String[] { "jmx.modelmbean.generic" }, "GENERIC",
				"A text notification has been issued by the managed resource",
				genericDescriptor);
		Descriptor attributeDescriptor = new DescriptorSupport(new String[] {
				"name=ATTRIBUTE_CHANGE", "descriptorType=notification",
				"log=T", "severity=5", "displayName=jmx.attribute.change" });
		result[1] = new ModelMBeanNotificationInfo(
				new String[] { "jmx.attribute.change" }, "ATTRIBUTE_CHANGE",
				"Signifies that an observed MBean attribute value has changed",
				attributeDescriptor);
		return result;
	}

	public void addNotificationListener(NotificationListener l,
			NotificationFilter filter, Object handle)
			throws IllegalArgumentException {
		this.broadcasterSupport.addNotificationListener(l, filter, handle);
	}

	public void removeNotificationListener(NotificationListener l)
			throws ListenerNotFoundException {
		this.broadcasterSupport.removeNotificationListener(l);
	}

	private Object getCurrentValue(CachedAttribute ca) throws MBeanException {
		Object result = null;
		if (ca != null) {
			try {
				result = this.beanUtil.getProperty(ca.getBean(), ca.getName());
			} catch (IllegalAccessException e) {
				throw new MBeanException(e);
			} catch (InvocationTargetException e) {
				throw new MBeanException(e);
			} catch (NoSuchMethodException e) {
				throw new MBeanException(e);
			}
		}
		return result;
	}

	private void buildAttributes(Object obj, MBeanAttributeInfo[] attrs)
			throws ReflectionException {
		if (attrs != null)
			for (int i = 0; i < attrs.length; ++i)
				try {
					String name = attrs[i].getName();
					PropertyDescriptor pd = this.beanUtil
							.getPropertyDescriptor(obj, name);
					Object value = this.beanUtil.getProperty(obj, name);
					Attribute attribute = new Attribute(name, value);
					CachedAttribute ca = new CachedAttribute(attribute);
					ca.setBean(obj);
					ca.setPropertyDescriptor(pd);
					this.cachedAttributes.put(name, ca);
				} catch (NoSuchMethodException e) {
					throw new ReflectionException(e);
				} catch (IllegalAccessException e) {
					throw new ReflectionException(e);
				} catch (InvocationTargetException e) {
					throw new ReflectionException(e);
				}
	}
}