package com.sanxing.ads.search;

import java.util.Set;

public abstract interface StringSearcher {
	public abstract void addIndex(Record paramRecord);

	public abstract void closeIndex();

	public abstract Set<Record> search(String paramString, int paramInt);
}