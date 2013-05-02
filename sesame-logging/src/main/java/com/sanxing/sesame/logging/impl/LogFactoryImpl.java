package com.sanxing.sesame.logging.impl;

import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import java.util.Hashtable;

public class LogFactoryImpl extends LogFactory {
	protected Hashtable instances = new Hashtable();

	public Log getInstance(String name) {
		Log instance = (Log) this.instances.get(name);
		if (instance == null) {
			instance = new SesameLogger(name);
			this.instances.put(name, instance);
		}
		return instance;
	}
}