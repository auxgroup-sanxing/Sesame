package com.sanxing.sesame.transport.util;

import com.sanxing.sesame.executors.ExecutorFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.util.thread.ThreadPool;

public class HttpThreadPool implements ThreadPool {
	private ExecutorService executor = ExecutorFactory.getFactory()
			.createExecutor("transports.http");

	public boolean dispatch(Runnable job) {
		this.executor.execute(job);
		return true;
	}

	public void join() throws InterruptedException {
		this.executor.awaitTermination(60L, TimeUnit.SECONDS);
	}

	public int getThreads() {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) this.executor;
		return executor.getMaximumPoolSize();
	}

	public int getIdleThreads() {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) this.executor;
		return (executor.getMaximumPoolSize() - executor.getActiveCount());
	}

	public boolean isLowOnThreads() {
		return false;
	}
}