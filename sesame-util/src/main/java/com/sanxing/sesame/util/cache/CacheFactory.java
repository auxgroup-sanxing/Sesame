package com.sanxing.sesame.util.cache;

public abstract interface CacheFactory {
	public abstract void addBackend(PeristenceBackend paramPeristenceBackend);

	public abstract Cache getCache(String paramString);
}