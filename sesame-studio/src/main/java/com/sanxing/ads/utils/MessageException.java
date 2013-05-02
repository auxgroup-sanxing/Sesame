package com.sanxing.ads.utils;

public class MessageException extends Exception {
	private static final long serialVersionUID = -9026562679434521638L;

	public MessageException(String message) {
		super(message);
	}

	public MessageException(String message, Throwable cause) {
		super(message, cause);
	}
}