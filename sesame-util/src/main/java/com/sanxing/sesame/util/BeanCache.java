package com.sanxing.sesame.util;

import java.util.HashMap;
import java.util.Map;

public class BeanCache {
	private Map<Class, BeanMetaData> beanMetaDataMap = new HashMap();

	public synchronized void putBeanMetaData(Class clazz,
			BeanMetaData beanMetaData) {
		this.beanMetaDataMap.put(clazz, beanMetaData);
	}

	public synchronized BeanMetaData getBeanMetaData(Class clazz) {
		BeanMetaData bMetaData = (BeanMetaData) this.beanMetaDataMap.get(clazz);
		return bMetaData;
	}
}