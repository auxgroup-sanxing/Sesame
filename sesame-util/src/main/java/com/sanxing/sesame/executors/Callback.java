package com.sanxing.sesame.executors;

public abstract interface Callback {
	public abstract void beforeExecute(Thread paramThread);

	public abstract void afterExecute(Throwable paramThrowable);

	public abstract void terminated();
}