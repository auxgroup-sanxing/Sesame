package com.sanxing.sesame.jdbc;

public class DataAccessException extends RuntimeException {
	private static final long serialVersionUID = 6710999093810851425L;

	public DataAccessException() {
	}

	public DataAccessException(String msg) {
		super(msg);
	}

	public DataAccessException(String msg, Throwable t) {
		super(msg, t);
	}

	public DataAccessException(Throwable t) {
		super(t);
	}
}