package com.sanxing.studio.team;

public class SCMException extends Exception {
	private static final long serialVersionUID = 1987972169275887194L;

	public SCMException(String message, Throwable cause) {
		super(message, cause);
	}

	public SCMException(String message) {
		super(message);
	}
}