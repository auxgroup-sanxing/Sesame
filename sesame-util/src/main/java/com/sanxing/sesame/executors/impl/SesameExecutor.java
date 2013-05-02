package com.sanxing.sesame.executors.impl;

import com.sanxing.sesame.executors.Callback;
import com.sanxing.sesame.executors.ThreadLocalCacheCleaner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameExecutor extends ThreadPoolExecutor implements
		SesameExecutorMBean {
	private final Logger LOG = LoggerFactory.getLogger(SesameExecutor.class);
	private long shutdownDelay;
	private String id;
	private final ThreadLocal<Long> startTime = new ThreadLocal();

	private final AtomicLong numTasks = new AtomicLong();

	private final AtomicLong totalTime = new AtomicLong();

	private final AtomicLong lastWorkTime = new AtomicLong();

	private final AtomicBoolean monitor = new AtomicBoolean(true);

	private List<Callback> callbacks = new ArrayList();

	public SesameExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			RejectedExecutionHandler handler, long _shutdownDelay, String _id) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, handler);
		this.shutdownDelay = _shutdownDelay;
		this.id = _id;
		addCallback(new ThreadLocalCacheCleaner());
	}

	public void addCallback(Callback callback) {
		this.callbacks.add(callback);
	}

	public void removeCallback(Callback callback) {
		this.callbacks.remove(callback);
	}

	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (this.monitor.get()) {
			this.LOG.debug(String.format("%s start %s", new Object[] { t, r }));
			this.startTime.set(Long.valueOf(System.nanoTime()));
			this.numTasks.incrementAndGet();
		}
		for (Callback callback : this.callbacks)
			callback.beforeExecute(t);
	}

	public void turnOnMonitor() {
		this.totalTime.set(0L);
		this.monitor.set(true);
	}

	public boolean isMonitorOn() {
		return this.monitor.get();
	}

	public void shutdownMonitor() {
		this.monitor.set(false);
	}

	protected void afterExecute(Runnable r, Throwable t) {
		try {
			if (this.monitor.get()) {
				long endTime = System.nanoTime();
				long taskTime = endTime
						- ((Long) this.startTime.get()).longValue();
				this.totalTime.addAndGet(taskTime);
				this.lastWorkTime.set(taskTime);
				if (this.LOG.isDebugEnabled()) {
					this.LOG.debug(String.format(
							"%s end %s, time=%dns",
							new Object[] { Thread.currentThread(), r,
									Long.valueOf(taskTime) }));
				}
			}
			for (Callback callback : this.callbacks)
				callback.afterExecute(t);
		} finally {
			super.afterExecute(r, t);
		}
	}

	protected void terminated() {
		try {
			long avg = (this.numTasks.get() > 0L) ? this.totalTime.get()
					/ this.numTasks.get() : 0L;
			this.LOG.info(String.format("Terminated: avg time=%dns",
					new Object[] { Long.valueOf(avg) }));

			for (Callback callback : this.callbacks)
				callback.terminated();
		} finally {
			super.terminated();
		}
	}

	public void shutdown() {
		super.shutdown();
		if ((isTerminated()) || (this.shutdownDelay <= 0L))
			return;
		new Thread(new Runnable() {
			public void run() {
				try {
					if (SesameExecutor.this.awaitTermination(
							SesameExecutor.this.shutdownDelay,
							TimeUnit.MILLISECONDS))
						return;
					SesameExecutor.this.shutdownNow();
				} catch (InterruptedException localInterruptedException) {
				}
			}
		}).start();
	}

	public long averageWorkTime() {
		long avg = (this.numTasks.get() > 0L) ? this.totalTime.get()
				/ this.numTasks.get() : 0L;
		return avg;
	}

	public long lastWorkTime() {
		long time = this.lastWorkTime.get();
		this.lastWorkTime.set(0L);
		return time;
	}

	public String getID() {
		return this.id;
	}
}