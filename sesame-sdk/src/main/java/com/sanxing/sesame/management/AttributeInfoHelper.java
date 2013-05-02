package com.sanxing.sesame.management;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.ReflectionException;

import org.apache.commons.beanutils.PropertyUtilsBean;

public class AttributeInfoHelper {
	private PropertyUtilsBean beanUtil = new PropertyUtilsBean();

	private List<MBeanAttributeInfo> list = new ArrayList();

	public void addAttribute(Object theObject, String name, String description)
			throws ReflectionException {
		try {
			PropertyDescriptor pd = this.beanUtil.getPropertyDescriptor(
					theObject, name);
			MBeanAttributeInfo info = new MBeanAttributeInfo(name, description,
					pd.getReadMethod(), pd.getWriteMethod());
			this.list.add(info);
		} catch (IntrospectionException e) {
			throw new ReflectionException(e);
		} catch (IllegalAccessException e) {
			throw new ReflectionException(e);
		} catch (InvocationTargetException e) {
			throw new ReflectionException(e);
		} catch (NoSuchMethodException e) {
			throw new ReflectionException(e);
		}
		PropertyDescriptor pd;
	}

	public MBeanAttributeInfo[] getAttributeInfos() {
		MBeanAttributeInfo[] result = new MBeanAttributeInfo[this.list.size()];
		this.list.toArray(result);
		return result;
	}

	public void clear() {
		this.list.clear();
	}

	public static MBeanAttributeInfo[] join(MBeanAttributeInfo[] attrs1,
			MBeanAttributeInfo[] attrs2) {
		MBeanAttributeInfo[] result = (MBeanAttributeInfo[]) null;
		int length = 0;
		int startPos = 0;
		if (attrs1 != null) {
			length = attrs1.length;
		}
		if (attrs2 != null) {
			length += attrs2.length;
		}

		result = new MBeanAttributeInfo[length];
		if (attrs1 != null) {
			System.arraycopy(attrs1, 0, result, startPos, attrs1.length);
			startPos = attrs1.length;
		}
		if (attrs2 != null) {
			System.arraycopy(attrs2, 0, result, startPos, attrs2.length);
		}
		return result;
	}
}