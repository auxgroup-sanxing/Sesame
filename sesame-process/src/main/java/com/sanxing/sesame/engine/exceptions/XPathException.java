package com.sanxing.sesame.engine.exceptions;

import com.sanxing.sesame.engine.action.ActionException;

public class XPathException extends ActionException {
	private static final long serialVersionUID = 6717047421696996692L;

	public XPathException() {
	}

	public XPathException(String message, Throwable cause) {
		super("00011", message, cause);
	}

	public XPathException(Throwable cause) {
		super(cause, "00011");
	}
}