package com.sanxing.sesame.executors;

import com.sanxing.sesame.executors.impl.ExecutorFactoryImpl;

import java.util.concurrent.ExecutorService;

public abstract class ExecutorFactory {
	public abstract ExecutorService createExecutor(String paramString);

	public abstract ExecutorService createExecutor(String paramString,
			Callback[] paramArrayOfCallback);

	public static ExecutorFactory getFactory() {
		return ExecutorFactoryImpl.getFactory();
	}
}