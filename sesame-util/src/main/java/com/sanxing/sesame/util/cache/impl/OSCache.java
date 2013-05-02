package com.sanxing.sesame.util.cache.impl;

import com.sanxing.sesame.util.cache.Cache;
import com.sanxing.sesame.util.cache.NoSuchObjectException;
import com.sanxing.sesame.util.cache.PeristenceBackend;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

public class OSCache implements Cache {
	private PeristenceBackend back;
	private String group;
	static GeneralCacheAdministrator cache = new GeneralCacheAdministrator();

	OSCache(PeristenceBackend back, String name) {
		this.back = back;
		this.group = name;
	}

	public PeristenceBackend getBackend() {
		return this.back;
	}

	public Object get(String key) throws NoSuchObjectException {
		Object obj = null;
		try {
			obj = cache.getFromCache(this.group + "_" + key);
		} catch (NeedsRefreshException e) {
			boolean updated = false;
			try {
				obj = this.back.getFromPersitence(key);
				cache.putInCache(this.group + "_" + key, obj,
						new String[] { this.group });
				updated = true;
			} catch (Exception ee) {
				ee.printStackTrace();
				obj = e.getCacheContent();
				if (!(updated)) {
					cache.cancelUpdate(this.group + "_" + key);
				}
			}
		}
		if (obj == null) {
			throw new NoSuchObjectException(key);
		}
		return obj;
	}

	public void flushAll() {
		cache.flushGroup(this.group);
	}

	public void put(String key, Object obj) {
		cache.putInCache(this.group + "_" + key, obj,
				new String[] { this.group });
	}

	public void flush(String key) {
		cache.flushEntry(this.group + "_" + key);
	}
}