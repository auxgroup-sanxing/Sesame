package com.sanxing.sesame.util.cache;

public abstract interface PeristenceBackend {
	public abstract String getName();

	public abstract Object getFromPersitence(String paramString);

	public abstract void saveOrUpdateToPersistence(String paramString,
			Object paramObject);

	public abstract String addToPersistence(Object paramObject);

	public abstract void deleteFromPeristence(String paramString);
}