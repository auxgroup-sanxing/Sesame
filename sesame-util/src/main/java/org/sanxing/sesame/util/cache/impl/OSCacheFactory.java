package org.sanxing.sesame.util.cache.impl;

import org.sanxing.sesame.util.cache.Cache;
import org.sanxing.sesame.util.cache.CacheFactory;
import org.sanxing.sesame.util.cache.PeristenceBackend;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OSCacheFactory implements CacheFactory {
	private Map<String, OSCache> caches = new ConcurrentHashMap();

	public void addBackend(PeristenceBackend back) {
		OSCache cache = new OSCache(back, back.getName());
		this.caches.put(back.getName(), cache);
	}

	public Cache getCache(String cacheName) {
		return ((Cache) this.caches.get(cacheName));
	}
}