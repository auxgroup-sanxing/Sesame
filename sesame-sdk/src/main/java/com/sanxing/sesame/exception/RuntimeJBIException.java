package com.sanxing.sesame.exception;

import javax.jbi.JBIException;

public class RuntimeJBIException extends RuntimeException {
	private static final long serialVersionUID = -6386553476156600457L;

	public RuntimeJBIException(JBIException cause) {
		super(cause);
	}
}