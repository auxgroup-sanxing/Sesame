package org.sanxing.sesame.executors;

import org.sanxing.sesame.util.cache.ThreadLocalCache;

public class ThreadLocalCacheCleaner implements Callback {
	public void beforeExecute(Thread thead) {
	}

	public void afterExecute(Throwable t) {
		ThreadLocalCache.clear();
	}

	public void terminated() {
	}
}