package com.sanxing.sesame.executors.impl;

import com.sanxing.sesame.executors.Callback;
import com.sanxing.sesame.executors.ExecutorFactory;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorFactoryImpl extends ExecutorFactory {
	private static Logger LOG = LoggerFactory.getLogger(ExecutorFactoryImpl.class);

	private static ExecutorFactoryImpl instance = new ExecutorFactoryImpl();

	private ExecutorConfig defaultConfig = new ExecutorConfig();

	private Map<String, ExecutorConfig> configs = new ConcurrentHashMap();

	private Map<String, ExecutorService> executorCache = new ConcurrentHashMap();

	public static ExecutorFactoryImpl getFactory() {
		return instance;
	}

	public ExecutorService createExecutor(String id) {
		if (!(this.executorCache.containsKey(id))) {
			ExecutorConfig config = getConfig(id);
			SesameExecutor executor = createExecutor(id, config);
			this.executorCache.put(id, executor);
		}

		return ((ExecutorService) this.executorCache.get(id));
	}

	public ExecutorService createExecutor(String id, Callback[] callbacks) {
		if (!(this.executorCache.containsKey(id))) {
			ExecutorConfig config = getConfig(id);
			SesameExecutor executor = createExecutor(id, config);
			for (Callback callback : callbacks) {
				executor.addCallback(callback);
			}
			this.executorCache.put(id, executor);
		}

		return ((ExecutorService) this.executorCache.get(id));
	}

	protected ExecutorConfig getConfig(String id) {
		ExecutorConfig config = null;
		if (this.configs != null) {
			config = (ExecutorConfig) this.configs.get(id);
			while ((config == null) && (id.indexOf(46) > 0)) {
				id = id.substring(0, id.lastIndexOf(46));
				config = (ExecutorConfig) this.configs.get(id);
			}
		}

		if (config == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Can not get executor config for '" + id
						+ "', use default executor");
			}
			config = this.defaultConfig;
		} else if (LOG.isDebugEnabled()) {
			LOG.debug("Got executor config for '" + id + "', " + config);
		}

		return config;
	}

	protected SesameExecutor createExecutor(String id, ExecutorConfig config) {
		if ((config.getQueueSize() != 0) && (config.getCorePoolSize() == 0))
			throw new IllegalArgumentException(
					"CorePoolSize must be > 0 when using a capacity queue");
		BlockingQueue queue;
		if (config.getQueueSize() == 0) {
			queue = new SynchronousQueue();
		} else {
			if ((config.getQueueSize() < 0)
					|| (config.getQueueSize() == 2147483647))
				queue = new LinkedBlockingQueue();
			else
				queue = new ArrayBlockingQueue(config.getQueueSize());
		}
		ThreadFactory factory = new DefaultThreadFactory(id,
				config.isThreadDaemon(), config.getThreadPriority());
		RejectedExecutionHandler handler = new CallerRunsPolicy();
		SesameExecutor executor = new SesameExecutor(
				config.getCorePoolSize(),
				(config.getMaximumPoolSize() < 0) ? 2147483647 : config
						.getMaximumPoolSize(), config.getKeepAliveTime(),
				TimeUnit.MILLISECONDS, queue, factory, handler,
				config.getShutdownDelay(), id);
		if (config.isAllowCoreThreadsTimeout()) {
			try {
				Method mth = executor.getClass().getMethod(
						"allowCoreThreadTimeOut", new Class[] { Boolean.TYPE });
				mth.invoke(executor, new Object[] { Boolean.TRUE });
			} catch (Throwable localThrowable) {
			}
		}
		return executor;
	}

	public Map<String, ExecutorConfig> getConfigs() {
		return this.configs;
	}

	public void setConfigs(Map<String, ExecutorConfig> configs) {
		this.configs = configs;
	}

	public ExecutorConfig getDefaultConfig() {
		return this.defaultConfig;
	}

	public void setDefaultConfig(ExecutorConfig defaultConfig) {
		this.defaultConfig = defaultConfig;
	}

	public String toString() {
		return "ExecutorFactoryImpl configs size [" + this.configs.size() + "]";
	}

	static class DefaultThreadFactory implements ThreadFactory {
		final ThreadGroup group;
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String namePrefix;
		final boolean daemon;
		final int priority;

		DefaultThreadFactory(String id, boolean daemon, int priority) {
			SecurityManager s = System.getSecurityManager();
			this.group = ((s != null) ? s.getThreadGroup() : Thread
					.currentThread().getThreadGroup());
			this.namePrefix = "pool-" + id + "-thread-";
			this.daemon = daemon;
			this.priority = priority;
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(this.group, r, this.namePrefix
					+ this.threadNumber.getAndIncrement(), 0L);
			if (t.isDaemon() != this.daemon) {
				t.setDaemon(this.daemon);
			}
			if (t.getPriority() != this.priority) {
				t.setPriority(this.priority);
			}
			return t;
		}
	}
}