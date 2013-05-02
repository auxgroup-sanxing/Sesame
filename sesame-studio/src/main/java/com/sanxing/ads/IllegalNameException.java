package com.sanxing.ads;

public class IllegalNameException extends IllegalArgumentException {
	private static final long serialVersionUID = -5337608523993994818L;

	public IllegalNameException(String reason) {
		super(reason);
	}
}