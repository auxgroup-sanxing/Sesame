package com.sanxing.sesame.binding.codec;

import com.sanxing.sesame.binding.BindingException;

public class FormatException extends BindingException {
	private static final long serialVersionUID = 8755294324454694865L;

	public FormatException(String message) {
		super(message);
	}

	public FormatException(Throwable cause) {
		super(cause);
	}

	public FormatException(String message, Throwable cause) {
		super(message, cause);
	}
}