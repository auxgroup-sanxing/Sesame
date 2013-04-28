package org.sanxing.sesame.util.cache;

public abstract interface Cache {
	public abstract Object get(String paramString) throws NoSuchObjectException;

	public abstract void flush(String paramString);

	public abstract void flushAll();

	public abstract void put(String paramString, Object paramObject);

	public abstract PeristenceBackend getBackend();
}