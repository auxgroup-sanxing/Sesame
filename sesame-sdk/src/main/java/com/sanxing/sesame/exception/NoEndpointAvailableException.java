package com.sanxing.sesame.exception;

import javax.jbi.JBIException;

public class NoEndpointAvailableException extends JBIException {
	private static final long serialVersionUID = 9082672125142508163L;

	public NoEndpointAvailableException(String aMessage) {
		super(aMessage);
	}
}