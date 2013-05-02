package com.sanxing.sesame.jdbc;

import com.sanxing.sesame.executors.Callback;

public class DataAccessCallback implements Callback {
	public void afterExecute(Throwable t) {
		TXHelper.destory();
	}

	public void beforeExecute(Thread thead) {
		TXHelper.destory();
	}

	public void terminated() {
	}
}