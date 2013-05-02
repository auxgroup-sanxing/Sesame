package com.sanxing.sesame.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeanUtil {
	private static BeanCache beanCache = new BeanCache();

	public static BeanMetaData getBeanMetaData(Class<?> baseClazz) {
		if (baseClazz == null) {
			throw new NullPointerException(
					"get property type:base clazz is null");
		}
		synchronized (baseClazz) {
			BeanMetaData tempBeanMetaData = beanCache
					.getBeanMetaData(baseClazz);
			if (tempBeanMetaData == null) {
				tempBeanMetaData = new BeanMetaData();
				tempBeanMetaData.initMetaData(baseClazz);
				beanCache.putBeanMetaData(baseClazz, tempBeanMetaData);
			}
		}
		BeanMetaData beanMetaData = beanCache.getBeanMetaData(baseClazz);
		return beanMetaData;
	}

	public static Class getPropertyType(Object base, String propName) {
		Class baseClazz = base.getClass();
		if (baseClazz == null) {
			throw new NullPointerException(
					"get property type:base clazz is null");
		}
		synchronized (baseClazz) {
			BeanMetaData tempBeanMetaData = beanCache
					.getBeanMetaData(baseClazz);
			if (tempBeanMetaData == null) {
				tempBeanMetaData = new BeanMetaData();
				tempBeanMetaData.initMetaData(baseClazz);
				beanCache.putBeanMetaData(baseClazz, tempBeanMetaData);
			}
		}
		BeanMetaData beanMetaData = beanCache.getBeanMetaData(baseClazz);
		Method readMethod = beanMetaData.getReadMethod(propName);
		Class propType = null;
		if (readMethod != null) {
			propType = readMethod.getReturnType();
		}
		return propType;
	}

	public static Object getProperty(Object base, String propName) {
		Class baseClazz = base.getClass();
		if (baseClazz == null) {
			throw new NullPointerException("get property:base clazz is null");
		}
		synchronized (baseClazz) {
			BeanMetaData tempBeanMetaData = beanCache
					.getBeanMetaData(baseClazz);
			if (tempBeanMetaData == null) {
				tempBeanMetaData = new BeanMetaData();
				tempBeanMetaData.initMetaData(baseClazz);
				beanCache.putBeanMetaData(baseClazz, tempBeanMetaData);
			}
		}
		BeanMetaData beanMetaData = beanCache.getBeanMetaData(baseClazz);
		Method readMethod = beanMetaData.getReadMethod(propName);
		Object[] args = (Object[]) null;
		Object retValue = null;
		try {
			retValue = readMethod.invoke(base, args);
		} catch (IllegalArgumentException iage) {
			throw new RuntimeException(iage);
		} catch (IllegalAccessException iae) {
			throw new RuntimeException(iae);
		} catch (InvocationTargetException ite) {
			throw new RuntimeException(ite);
		}
		return retValue;
	}

	public static void setProperty(Object base, String propName,
			Object propValue) {
		Class baseClazz = base.getClass();
		if (baseClazz == null) {
			throw new NullPointerException("set property:base clazz is null");
		}
		synchronized (baseClazz) {
			BeanMetaData tempBeanMetaData = beanCache
					.getBeanMetaData(baseClazz);
			if (tempBeanMetaData == null) {
				tempBeanMetaData = new BeanMetaData();
				tempBeanMetaData.initMetaData(baseClazz);
				beanCache.putBeanMetaData(baseClazz, tempBeanMetaData);
			}
		}
		BeanMetaData beanMetaData = beanCache.getBeanMetaData(baseClazz);
		Method writeMethod = beanMetaData.getWriteMethod(propName);
		try {
			writeMethod.invoke(base, new Object[] { propValue });
		} catch (IllegalArgumentException iage) {
			throw new RuntimeException(iage);
		} catch (IllegalAccessException iae) {
			throw new RuntimeException(iae);
		} catch (InvocationTargetException ite) {
			throw new RuntimeException(ite);
		}
	}
}